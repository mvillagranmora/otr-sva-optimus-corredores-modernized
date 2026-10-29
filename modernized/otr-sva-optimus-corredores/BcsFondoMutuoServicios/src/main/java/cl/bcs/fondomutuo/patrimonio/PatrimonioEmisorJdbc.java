package cl.bcs.fondomutuo.patrimonio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * {@link PatrimonioEmisorPort} over the legacy SQL (verbatim, {@code cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_*.sql}). The step table is GLOBAL
 * TEMPORARY (rows per session): DELETE, INSERT and SELECT run inside ONE {@code usar} so they share the connection of the call even if the call is
 * not inside a Spring transaction. No row is deleted afterwards (legacy).
 */
@Component
public class PatrimonioEmisorJdbc implements PatrimonioEmisorPort {

    private static final String SQL_SELECT_SERIE_FONDO = SqlRecursos.modulo("ValidaPatrimonioEmisor_SelectSerieFondo.sql");
    private static final String SQL_DELETE_TABLA_PASO = SqlRecursos.modulo("ValidaPatrimonioEmisor_DeleteTablaPaso.sql");
    private static final String SQL_INSERT_TABLA_PASO = SqlRecursos.modulo("ValidaPatrimonioEmisor_InsertTablaPaso.sql");
    private static final String SQL_SELECT_CUSTODIA = SqlRecursos.modulo("ValidaPatrimonioEmisor_SelectCustodia.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public PatrimonioEmisorJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<SerieFondo> seriesFondo(List<String> idsSerie) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerie);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO, params, SerieFondo.class));
    }

    @Override
    public List<Custodia> custodia(List<Long> idsCliente, String fecha) {
        return conexion.usar(con -> {
            // legacy: SQL.exec(DELETE), batch(INSERT), query(SELECT): same session
            try (var st = con.createStatement()) {
                st.execute(SQL_DELETE_TABLA_PASO);
            }
            for (Long idCliente : idsCliente) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("IdCliente", idCliente);
                SqlNombrado.actualizar(con, SQL_INSERT_TABLA_PASO, fila);
            }
            Map<String, Object> params = new HashMap<>();
            params.put("Fecha", fecha);
            return SqlNombrado.consultar(con, SQL_SELECT_CUSTODIA, params, Custodia.class);
        });
    }
}
