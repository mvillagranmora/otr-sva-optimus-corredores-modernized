package cl.bcs.apv.application.autorizaciones;

/**
 * The part of the legacy {@code cl.bcs.plataforma.aut.pojo.Evento} (row of {@code AUT.Evento}) that the authorization hooks of APV read: the event id, the
 * company, module and user of the event and the client the event was raised for. Any other field of the platform event is ignored.
 */
public record EventoAutorizacion(Long idEvento, Long idEmpresa, String idModulo, Long idUsuario, Long idCliente) {
}
