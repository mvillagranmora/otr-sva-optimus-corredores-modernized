package cl.bcs.portafolio.domain.portafolio;

public record CrearPortafolioResultado(
    String codigoPortafolio,
    Long idPortafolio,
    String requiereAutorizacion,
    String mensaje
) {}
