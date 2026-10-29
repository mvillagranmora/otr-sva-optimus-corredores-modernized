package cl.bcs.normativa.svc.domain.ils;

import java.math.BigDecimal;

/**
 * Obligaciones y derechos en moneda extranjera para el reporte ILS (Indicadores de Liquidez).
 * Agrupa los valores por tasa papel y tasa pactos, con separación por plazo (&lt;8 días y general).
 */
public class ObligacionesDerechos {

    public BigDecimal obligacionesTasaPapel = BigDecimal.ZERO;
    public BigDecimal derechosTasaPapel = BigDecimal.ZERO;
    public BigDecimal derechosTasaPactos = BigDecimal.ZERO;
    public BigDecimal obligacionesTasaPactos = BigDecimal.ZERO;
    public BigDecimal obligacionesTasaPapelMenores8Dias = BigDecimal.ZERO;
    public BigDecimal derechosTasaPapelMenores8Dias = BigDecimal.ZERO;
    public BigDecimal derechosTasaPactosMenores8Dias = BigDecimal.ZERO;
    public BigDecimal obligacionesTasaPactosMenores8Dias = BigDecimal.ZERO;

    public ObligacionesDerechos() {}
}
