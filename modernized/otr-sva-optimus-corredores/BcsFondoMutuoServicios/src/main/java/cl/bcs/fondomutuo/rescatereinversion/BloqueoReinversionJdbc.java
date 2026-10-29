package cl.bcs.fondomutuo.rescatereinversion;

import java.sql.CallableStatement;
import java.sql.Connection;
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
 * {@link BloqueoReinversionPort} with the six legacy SQL files of the reinversion lock, verbatim. The only DDL is the session-local table and its DROP
 * (D-F2-1); the two CALL run procedures that already exist. Same connection for all (the unit of work). Nothing cached. A failure of the INSERT or of the
 * creating CALL drops the local table (DEV-6-H1, approved 2026-10-26; the legacy leaked it). The DDL marks
 * {@link EscrituraConfirmada} like Normal's lock (Step 6, option A: on HANA a CREATE/DROP of a local table commits what the request wrote before it).
 */
@Component
public class BloqueoReinversionJdbc implements BloqueoReinversionPort {

    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";
    private static final String SQL_SELECT = SqlRecursos.modulo("SelectReinversionRescate_Tabla_Bloqueo.sql");
    private static final String SQL_CREATE_TABLE = SqlRecursos.modulo("CrearReinversionRescate_Tabla_Bloqueo.sql");
    private static final String SQL_INSERT_TABLE = SqlRecursos.modulo("InsertReinversionRescate_Tabla_Bloqueo.sql");
    private static final String SQL_CALL_CREAR = SqlRecursos.modulo("ProcesarReinversionRescate_Call_CrearBloqueoReinversion.sql");
    private static final String SQL_CALL_ELIMINAR = SqlRecursos.modulo("ProcesarReinversion_Call_EliminarBloqueoReinversion.sql");
    private static final String SQL_DROP_TABLE = SqlRecursos.modulo("DropReinversionRescate_Tabla_Bloqueo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public BloqueoReinversionJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    /** Row of the SELECT (the legacy mapped it onto its {@code BloqueoReinversion}; only the id is read). */
    public static class Existente {
        public String IdBloqueaReinversion;
    }

    @Override
    public List<String> existentes(List<String> ids) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_IdBloqueaReinversion", ids);
        return conexion.usar(con -> {
            List<String> salida = new ArrayList<>();
            for (Existente f : SqlNombrado.consultar(con, SQL_SELECT, params, Existente.class)) {
                salida.add(f.IdBloqueaReinversion);
            }
            return salida;
        });
    }

    @Override
    public void crear(List<Bloqueo> bloqueos) {
        conexion.usar(con -> {
            ejecutar(con, SQL_CREATE_TABLE);   // legacy SQL.exec
            EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
            try {
                SqlLote.ejecutar(con, SQL_INSERT_TABLE, bloqueos);
                llamar(con, SQL_CALL_CREAR);
            } catch (SQLException | RuntimeException e) {
                soltarTrasFallo(con, e);   // DEV-6-H1 (approved 2026-10-26): the legacy left the table in the session (FM-REI-10); here it is dropped
                throw e;
            }
            return null;
        });
    }

    /** Failure path of {@link #crear}: DROP in its own try/catch; "does not exist" is swallowed, any other failure is attached as suppressed (as Normal's lock). */
    private static void soltarTrasFallo(Connection con, Exception causa) {
        try {
            ejecutar(con, SQL_DROP_TABLE);
            EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
        } catch (SQLException e) {
            String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase(java.util.Locale.ROOT);
            if (!(m.contains("does not exist") || m.contains("invalid table name") || m.contains("could not find table"))) {
                causa.addSuppressed(e);
            }
        }
    }

    @Override
    public void llamarEliminar() {
        conexion.usarAunVencido(con -> {   // M2: the release must run even when the transaction deadline has passed
            llamar(con, SQL_CALL_ELIMINAR);
            return null;
        });
    }

    @Override
    public void soltarTabla() {
        conexion.usarAunVencido(con -> {
            ejecutar(con, SQL_DROP_TABLE);
            EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
            return null;
        });
    }

    private static void ejecutar(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    /** Legacy {@code SQL.call}: comments removed, a callable with no parameters (the procedures read the local table by name; the legacy maps are unused). */
    private static void llamar(Connection con, String sql) throws SQLException {
        try (CallableStatement cs = con.prepareCall(sql.replaceAll(COMENTARIOS, ""))) {
            cs.executeUpdate();
        }
    }
}
