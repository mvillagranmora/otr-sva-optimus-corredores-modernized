package cl.bcs.cuentainversion.reportes;

import java.math.BigDecimal;

/**
 * Cash patrimony of a client / portfolio / day ({@code CTA.PatrimonioCaja}): the row written by GuardarPatrimonioCaja (CTA-B-124..130),
 * the staging row and the result row of GuardarPatrimonioCajaCalculoComision (CTA-B-117..123). Public fields with the defaults of the
 * legacy POJO: they are the JSON contract and the batch parameters of the INSERT.
 */
public class PatrimonioCaja {
    public Long IdInterno;
    public Long IdCliente;
    public Long IdPortafolio;
    public String FechaPatrimonio;
    /** LIQ (by settlement date) or ADQ (by movement date). */
    public String TipoFechaConsulta;
    public BigDecimal MontoPatrimonio = BigDecimal.ZERO;
    public BigDecimal SaldoDisponible;
    public BigDecimal SaldoEnGarantia;
    public BigDecimal SaldoRetiro;
    public BigDecimal SaldoAPlazoSimultanea;
    public BigDecimal SaldoAPlazoVentaCorta;
    public BigDecimal SaldoAPlazoCompromiso;
    public String IdMoneda;
    public String IdMonedaSistema;
    public String IdEstado;
    public BigDecimal MontoPatrimonioAfectoComision = BigDecimal.ZERO;
    public BigDecimal MontoComision = BigDecimal.ZERO;
    public BigDecimal PorcentajeCobroComision = BigDecimal.ZERO;
    public String IdEstadoComision = "INA";
    public Long IdCuentaInversion = 0L;
}
