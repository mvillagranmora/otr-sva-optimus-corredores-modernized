package cl.bcs.fondomutuo.custodia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** {@link FlujoCustodiaFmDatos} over the legacy SQL, verbatim. Read only. */
@Component
public class FlujoCustodiaFmJdbc implements FlujoCustodiaFmDatos {

    private static final String SQL_SELECT_RESCATE_TOTAL_TRANSITO = SqlRecursos.modulo("ConsultarFlujoCustodiaFondoMutuo_SelectRescateTotalTransito.sql");
    private static final String SQL_SELECT_SERIE_FONDO = SqlRecursos.modulo("ConsultarFlujoCustodiaFondoMutuo_SelectSerieFondo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public FlujoCustodiaFmJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<SerieFondo> series(List<Long> idsInstrumento) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdInstrumento", idsInstrumento);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO, params, SerieFondo.class));
    }

    @Override
    public List<RescateTotal> rescatesTotalesEnTransito(List<Long> idsInstrumento, List<Long> idsCliente, List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdCliente", idsCliente);
        params.put("IdPortafolio", idsPortafolio);
        params.put("IdInstrumento", idsInstrumento);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_RESCATE_TOTAL_TRANSITO, params, RescateTotal.class));
    }
}
