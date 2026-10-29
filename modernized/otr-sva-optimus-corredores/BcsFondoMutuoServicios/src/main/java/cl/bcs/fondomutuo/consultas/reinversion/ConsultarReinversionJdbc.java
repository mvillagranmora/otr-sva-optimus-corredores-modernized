package cl.bcs.fondomutuo.consultas.reinversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** The one read of {@code ConsultarFondoMutuoReinversion}: {@code ConsultarFondoMutuo_SelectReinversion.sql}, verbatim, on the connection of the unit of work. */
@Component
public class ConsultarReinversionJdbc implements ConsultarReinversionDatos {

    private static final String SQL_SELECT_REINVERSION = SqlRecursos.modulo("ConsultarFondoMutuo_SelectReinversion.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarReinversionJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarReinversionContrato.FiltroFondoMutuo> reinversion(Long idEmpresa, String idEstado, String idMoneda, String fondoExtranjero,
            String idFamiliaFondo, String idEmisor, String idRegimenFondoMutuo, String idSerieFondoOrigen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdEstado", idEstado);
        params.put("IdMoneda", idMoneda);
        params.put("FondoExtranjero", fondoExtranjero);
        params.put("IdFamiliaFondo", idFamiliaFondo);
        params.put("IdEmisor", idEmisor);
        params.put("IdRegimenFondoMutuo", idRegimenFondoMutuo);
        params.put("IdSerieFondoOrigen", idSerieFondoOrigen);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_REINVERSION, params, ConsultarReinversionContrato.FiltroFondoMutuo.class));
    }
}
