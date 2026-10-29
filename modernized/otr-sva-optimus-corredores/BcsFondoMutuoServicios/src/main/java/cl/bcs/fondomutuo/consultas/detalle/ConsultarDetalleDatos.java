package cl.bcs.fondomutuo.consultas.detalle;

import java.util.List;

/** Read port: the rows of the delegate SQL {@code ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql}, verbatim, for the company and the id. */
public interface ConsultarDetalleDatos {

    List<ConsultarDetalleContrato.SolicitudUi> solicitudes(Long idEmpresa, Long idSolicitudAporteRescate);
}
