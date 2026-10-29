package cl.bcs.apv.application.autorizaciones;

import java.util.List;

import cl.bcs.apv.api.comun.Mensaje;

/** Legacy {@code BcsAutorizaciones.SalidaAutorizacion} (JSON {@code P_OUT_MENSAJES}, {@code P_OUT_ERRORESCONTADOR}, {@code P_OUT_RESULTADO}). */
public record SalidaAutorizacion(List<Mensaje> mensajes, Long contadorErrores, List<ResultadoAutorizacion> resultado) {
}
