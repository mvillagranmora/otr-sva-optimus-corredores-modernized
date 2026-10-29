package cl.bcs.portafolio.domain.consumo;

public record ConsumoLimiteResultado(
    java.math.BigDecimal limite,
    java.math.BigDecimal consumido,
    java.math.BigDecimal disponible
) {}
