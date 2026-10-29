package cl.bcs.valo2.domain.bono;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Bono(
        String nemo,
        String isin,
        String monedaEmision,
        String monedaPago,
        String convencionDias,
        String calendario,
        boolean mercadoLocal,
        boolean ajusteFinDeMes,
        int periodoPago,
        int tipoCalculoInteresesCupon,
        int tipoCalculoInteresesDevengados,
        BigDecimal tasaEmision,
        LocalDate fechaDevengo,
        LocalDate fechaVencimiento,
        String tipoInflacion,
        String tenorInflacion,
        String tipoLibor,
        boolean cuponeraVariable,
        boolean callConfirmado,
        boolean valorizarEnDiaHabil
) {
    public double calcularCantidadPagosAnuales() { return 12.0 / periodoPago; }
}
