package cl.bcs.ordenes.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * B-ORD-MONTO-*: fórmula de cálculo de monto paridad.
 * Source: CalcularMontoYCobrosOrden.java:130-153.
 */
@Service
public class CalcularMontoService {

    private static final BigDecimal PUNTO_CINCO = new BigDecimal("0.5");

    /**
     * B-ORD-MONTO-01: y = precio × factor; si y < 0.5 → scale(3, DOWN); else → scale(2, HALF_UP).
     * Retorna y × cantidad.
     */
    public static BigDecimal calculaMontoParidad(BigDecimal precio, BigDecimal cantidad,
            BigDecimal factor) {
        BigDecimal y = precio.multiply(factor);
        if (y.compareTo(PUNTO_CINCO) < 0) {
            y = y.setScale(3, RoundingMode.DOWN);
        } else {
            y = y.setScale(2, RoundingMode.HALF_UP);
        }
        return y.multiply(cantidad);
    }
}
