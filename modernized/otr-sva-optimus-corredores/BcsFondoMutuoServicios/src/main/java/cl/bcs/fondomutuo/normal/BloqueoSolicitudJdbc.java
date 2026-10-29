package cl.bcs.fondomutuo.normal;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/**
 * {@link BloqueoSolicitudPort} with the legacy SQL verbatim ({@code CrearSolicitudAporteRescateNormal_*BloqueoSolicitud*.sql}). The only DDL is the
 * session-local temporary table and its DROP (accepted, plan 12 D-2); the two CALL run procedures that already exist. The connection is the one of the
 * unit of work, so the local table, its rows and the CALL share one session. Nothing is cached (no state between requests).
 */
@Component
public class BloqueoSolicitudJdbc implements BloqueoSolicitudPort {

    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";
    private static final String SQL_SELECT = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_SelectBloqueoSolicitud.sql");
    private static final String SQL_CREATE_TABLE = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_CreateTableBloqueoSolicitudAporteRescate.sql");
    private static final String SQL_INSERT_TABLE = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_InsertTableBloqueoSolicitudAporteRescate.sql");
    private static final String SQL_CALL_CREAR = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_CallCrearBloqueoSolicitudAporteRescate.sql");
    private static final String SQL_CALL_ELIMINAR = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_CallEliminarBloqueoSolicitudAporteRescate.sql");
    private static final String SQL_DROP_TABLE = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_DropTableBloqueoSolicitudAporteRescate.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public BloqueoSolicitudJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<String> existentes(List<String> ids) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_IdBloqueoSolicitudAporteRescate", ids);
        return conexion.usar(con -> {
            List<Bloqueo> filas = SqlNombrado.consultar(con, SQL_SELECT, params, Bloqueo.class);
            List<String> salida = new ArrayList<>();
            for (Bloqueo f : filas) {
                salida.add(f.IdBloqueoSolicitudAporteRescate);
            }
            return salida;
        });
    }

    @Override
    public void crear(List<Bloqueo> bloqueos) {
        conexion.usar(con -> {
            ejecutar(con, SQL_CREATE_TABLE);
            marcarDdl();
            try {
                SqlLote.ejecutar(con, SQL_INSERT_TABLE, bloqueos);
                llamar(con, SQL_CALL_CREAR);
            } catch (SQLException | RuntimeException e) {
                soltarTrasFallo(con, e);   // H1: the session-local table must not leak on the pooled connection
                throw e;
            }
            return null;
        });
    }

    /** The CREATE / DROP of the session-local table commit the open transaction on HANA (measured; accepted legacy behaviour, option A). */
    private static void marcarDdl() {
        EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
    }

    /** Failure path of {@link #crear}: DROP in its own try/catch; "does not exist" is swallowed, any other failure is attached as suppressed. */
    private static void soltarTrasFallo(java.sql.Connection con, Exception causa) {
        try {
            ejecutar(con, SQL_DROP_TABLE);
            marcarDdl();
        } catch (SQLException e) {
            String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase(java.util.Locale.ROOT);
            if (!(m.contains("does not exist") || m.contains("invalid table name") || m.contains("could not find table"))) {
                causa.addSuppressed(e);
            }
        }
    }

    @Override
    public void llamarEliminar(List<Bloqueo> bloqueos) {
        conexion.usarAunVencido(con -> {   // M2: the release must run even when the transaction deadline has passed
            llamar(con, SQL_CALL_ELIMINAR);
            return null;
        });
    }

    @Override
    public void soltarTabla() {
        conexion.usarAunVencido(con -> {
            ejecutar(con, SQL_DROP_TABLE);
            marcarDdl();
            return null;
        });
    }

    private static void ejecutar(java.sql.Connection con, String sql) throws java.sql.SQLException {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    /** Legacy {@code SQL.call}: the comments are removed and the statement runs as a callable with no parameters (the table is read from the session). */
    private static void llamar(java.sql.Connection con, String sql) throws java.sql.SQLException {
        try (java.sql.CallableStatement cs = con.prepareCall(sql.replaceAll(COMENTARIOS, ""))) {
            cs.executeUpdate();
        }
    }
}
