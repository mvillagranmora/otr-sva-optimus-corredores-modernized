package cl.bcs.portafolio.domain.portafolio;

public record ModificarEstadoPortafolioEntrada(
    Long idPortafolio,
    String codigoPortafolio,
    String estado,
    String tipo,
    String idUsuario,
    String motivo
) {}
