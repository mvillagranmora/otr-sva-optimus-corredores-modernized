package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;

/** One query of ConsultarMovimientoCuentaInversion (a row of the process table); legacy field names (JSON contract). */
public class ConsultaMovimiento {
    public Long IdPortafolio;
    public String IdMoneda;
    public String IdEstado;
    public BigDecimal SaldoInicial;
    public String FechaDesde;
    public String FechaHasta;
    public Long IdMatrizAgrupador;
    public String EsADC;
    public String SaldoPorFechaEfectiva;
    public String IncluyeSaldo;
}
