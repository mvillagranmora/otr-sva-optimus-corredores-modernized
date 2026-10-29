package cl.bcs.normativa.svc.domain.cartola;

import java.math.BigDecimal;

/**
 * Saldo final del cliente para el período consultado.
 * Utilizado como denominador en cálculos de rentabilidad MTD/YTD/ITD.
 */
public class SaldoFinalCliente {

    public Long idCliente;
    public Long idEmpresa;

    /** Patrimonio total a la fecha de consulta */
    public BigDecimal totalPatrimonioFechaConsulta = BigDecimal.ZERO;

    /** Saldo inicial al comienzo del período MTD */
    public BigDecimal totalMTDSaldoFinal = BigDecimal.ZERO;

    /** Saldo inicial al comienzo del período YTD */
    public BigDecimal totalYTDSaldoFinal = BigDecimal.ZERO;

    /** Saldo inicial al comienzo del período ITD (inicio del contrato) */
    public BigDecimal totalITDSaldoFinal = BigDecimal.ZERO;

    public SaldoFinalCliente() {}
}
