package cl.bcs.apv.application.autorizaciones;

import java.util.List;

/**
 * Legacy {@code BcsAutorizaciones.EntradaAutorizacion}: what the authorization engine hands a hook when the approvers decided on a batch of events of the
 * same event type ({@code lista} = the events, {@code p_Lista} of the legacy). {@code usaReglasNegocio} is carried but no APV hook reads it.
 */
public record EntradaAutorizacion(Long idEmpresa, Long idUsuario, String idModulo, String usaReglasNegocio, List<EventoAutorizacion> lista) {
}
