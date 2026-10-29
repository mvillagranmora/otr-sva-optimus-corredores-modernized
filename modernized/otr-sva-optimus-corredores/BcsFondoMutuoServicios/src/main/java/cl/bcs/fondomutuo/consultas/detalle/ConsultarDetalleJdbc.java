package cl.bcs.fondomutuo.consultas.detalle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * The delegate SELECT of {@code ConsultarDetalleSolicitud} ({@code ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql}), verbatim, on the connection of the
 * unit of work, with the named parameters the legacy delegate builds for one id: the id filter on (1), the other eleven filters null / off (0).
 */
@Component
public class ConsultarDetalleJdbc implements ConsultarDetalleDatos {

    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarDetalleJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarDetalleContrato.SolicitudUi> solicitudes(Long idEmpresa, Long idSolicitudAporteRescate) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("FechaAceptacionDesde", null);
        params.put("FechaAceptacionHasta", null);
        params.put("IdTipoMovimientoFondoMutuo", null);
        params.put("Estado", null);
        params.put("IdEmisor", null);
        params.put("IdSerieFondo", null);
        params.put("IdCliente", null);
        params.put("IdProductoAgrupador", null);
        params.put("IdPortafolio", null);
        params.put("TipoSolicitud", null);
        params.put("IdMoneda", null);
        params.put("IdSolicitudAporteRescate", idSolicitudAporteRescate);
        params.put("FiltraPorFechaAceptacionDesde", 0L);
        params.put("FiltraPorFechaAceptacionHasta", 0L);
        params.put("FiltraPorIdTipoMovimientoFondoMutuo", 0L);
        params.put("FiltraPorEstado", 0L);
        params.put("FiltraPorIdEmisor", 0L);
        params.put("FiltraPorIdSerieFondo", 0L);
        params.put("FiltraPorIdCliente", 0L);
        params.put("FiltraPorIdProductoAgrupador", 0L);
        params.put("FiltraPorIdPortafolio", 0L);
        params.put("FiltraPorTipoSolicitud", 0L);
        params.put("FiltraPorIdMoneda", 0L);
        params.put("FiltraPorIdSolicitudAporteRescate", 1L);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SOLICITUDES, params, ConsultarDetalleContrato.SolicitudUi.class));
    }
}
