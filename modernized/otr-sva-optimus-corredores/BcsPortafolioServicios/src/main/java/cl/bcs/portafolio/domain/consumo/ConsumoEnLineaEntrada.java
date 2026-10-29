package cl.bcs.portafolio.domain.consumo;

public record ConsumoEnLineaEntrada(
    Long idPortafolio,
    String codigoPortafolio,
    Long idPersona,
    java.math.BigDecimal montoOperacion,
    String moneda
) {}
