package cl.bcs.fondomutuo.consultas.fecha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** The one read of {@code ConsultarFechaSolicitudAporteRescate}: {@code ..._SelectSerieFondo.sql}, verbatim, on the connection of the unit of work. */
@Component
public class ConsultarFechaJdbc implements ConsultarFechaDatos {

    private static final String SQL_SELECT_SERIE_FONDO = SqlRecursos.modulo("ConsultarFechaSolicitudAporteRescate_SelectSerieFondo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarFechaJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarFechaContrato.ConfiguracionSerie> configuracionSeries(List<String> idsSerieFondo, Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerieFondo);
        params.put("IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO, params, ConsultarFechaContrato.ConfiguracionSerie.class));
    }
}
