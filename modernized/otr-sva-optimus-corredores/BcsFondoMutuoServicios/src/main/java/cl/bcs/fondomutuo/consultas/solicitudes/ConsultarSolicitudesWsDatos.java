package cl.bcs.fondomutuo.consultas.solicitudes;

import java.util.List;
import java.util.Map;

/** Read port: the rows of the legacy SELECT (verbatim SQL) with the same named parameters the legacy {@code HashMap} carries. */
public interface ConsultarSolicitudesWsDatos {

    List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes(Map<String, Object> parametros);
}
