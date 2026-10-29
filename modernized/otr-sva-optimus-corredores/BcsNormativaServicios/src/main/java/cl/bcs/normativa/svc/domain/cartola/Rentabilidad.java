package cl.bcs.normativa.svc.domain.cartola;

import java.math.BigDecimal;

/**
 * Resultado de rentabilidad de un cliente para los períodos MTD/YTD/ITD.
 *
 * <p>Fórmula general para cada período P:
 * <pre>
 *   numerador   = patrimonioFinal - saldoInicialP - aportesP + rescatesP
 *   denominador = saldoInicialP + aportesPeriodoP - rescatesPeriodoP
 *   rentabilidad = (denominador == 0) ? 0 : numerador / denominador
 * </pre>
 */
public class Rentabilidad {

    public Long idCliente;
    public Long idEmpresa;

    /** Rentabilidad mes a la fecha (Month To Date) */
    public BigDecimal rentabilidadMTD = BigDecimal.ZERO;

    /** Rentabilidad año a la fecha (Year To Date) */
    public BigDecimal rentabilidadYTD = BigDecimal.ZERO;

    /** Rentabilidad desde inicio (Inception To Date) */
    public BigDecimal rentabilidadITD = BigDecimal.ZERO;

    public Rentabilidad() {}

    public Rentabilidad(Long idCliente, Long idEmpresa,
                        BigDecimal mtd, BigDecimal ytd, BigDecimal itd) {
        this.idCliente = idCliente;
        this.idEmpresa = idEmpresa;
        this.rentabilidadMTD = mtd;
        this.rentabilidadYTD = ytd;
        this.rentabilidadITD = itd;
    }
}
