package cl.bcs.portafolio.domain.autorizacion;

public record AutorizacionPortafolioResultado(
    Long idEvento,
    String estado,
    String mensaje
) {}
