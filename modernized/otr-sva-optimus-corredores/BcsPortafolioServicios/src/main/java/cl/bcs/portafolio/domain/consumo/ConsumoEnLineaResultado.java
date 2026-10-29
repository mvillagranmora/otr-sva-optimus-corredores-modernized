package cl.bcs.portafolio.domain.consumo;

public record ConsumoEnLineaResultado(
    java.math.BigDecimal limiteDisponible,
    java.math.BigDecimal consumoActual,
    String estado
) {}
