package cl.bcs.portafolio.domain.portafolio;

public record PortafolioResultado(
    Long idPortafolio,
    String codigoPortafolio,
    String estado,
    String mensaje
) {}
