package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.math.BigDecimal;

/**
 * Totals of one concept, or of the "Total" row. Legacy {@code ConceptoNormativo} with its {@code TotalTransaccion}
 * (Cantidad, Monto, Resultado) and {@code Saldo} (Activos, Pasivos), COCR:60-75. The legacy never computes Resultado,
 * Activos or Pasivos, so they are always zero (CONT-D-22).
 */
public record TotalConcepto(String concepto, BigDecimal cantidad, BigDecimal monto, BigDecimal resultado,
                            BigDecimal activos, BigDecimal pasivos) {

    public static TotalConcepto enCero(String concepto) {
        return new TotalConcepto(concepto, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);
    }
}
