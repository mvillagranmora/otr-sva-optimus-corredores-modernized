package cl.bcs.mesa.domain;

import java.math.BigDecimal;

/**
 * Movimiento Spot — réplica del inner-class de la legacy CalculosCalculadoraSpot.java.
 * Campos públicos mutables para mantener equivalencia con el patrón legacy.
 */
public class MovimientoSpot {
    public String Estado;
    public String Nemotecnico;
    public String IdTipoCanalIngreso;
    public String IdTipoOperacion;
    public BigDecimal MontoMonedaPrincipal = BigDecimal.ZERO;
    public BigDecimal MontoMonedaSecundaria = BigDecimal.ZERO;
    public BigDecimal TipoCambioCierre = BigDecimal.ZERO;
    public BigDecimal TipoCambioCosto = BigDecimal.ZERO;
    public BigDecimal Margen = BigDecimal.ZERO;
    public String FechaValutaRecibimos;
    public String FechaValutaEntregamos;
    public String IdMonedaPrincipal = "";
    public String IdMonedaSecundaria = "";
    public String IdMonedaLocal = "";
    public String IdMonedaEquivalente = "";
    public String IdMonedaRecibimos = "";
    public String IdMonedaEntregamos = "";
    public BigDecimal ParidadMercado = BigDecimal.ZERO;
    public BigDecimal ParidadBid = BigDecimal.ZERO;
    public BigDecimal MontoMonedaPrincipalEquivalente = BigDecimal.ZERO;
    public String MultiplicaODivide = "";
}
