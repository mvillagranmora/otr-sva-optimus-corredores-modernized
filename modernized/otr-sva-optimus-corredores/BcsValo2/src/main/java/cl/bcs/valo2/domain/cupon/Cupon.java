package cl.bcs.valo2.domain.cupon;

import java.math.BigDecimal;

public record Cupon(
        int numero,
        BigDecimal amortizacion,
        BigDecimal intereses,
        BigDecimal saldoPorAmortizar,
        double periodoEnAnos,
        double vencimientoEnAnos,
        boolean call
) {
    public double calcularAmortizacionUnitaria() { return amortizacion.doubleValue() / 100; }
    public double calcularInteresesUnitarios() { return intereses != null ? intereses.doubleValue() / 100 : 0; }
    public double calcularFlujoUnitario() { return calcularAmortizacionUnitaria() + calcularInteresesUnitarios(); }
    public double calcularSaldoPorAmortizarUnitario() { return saldoPorAmortizar.doubleValue() / 100; }
    public double calcularSaldoCapitalUnitario() { return calcularSaldoPorAmortizarUnitario() + calcularAmortizacionUnitaria(); }
}
