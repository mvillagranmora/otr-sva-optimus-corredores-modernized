package cl.bcs.mesa.domain;

import java.math.BigDecimal;

/** Posición inicial de una moneda al comienzo del día. */
public class PosicionInicial {
    public String IdMoneda;
    public BigDecimal PosicionInicial  = BigDecimal.ZERO;
    public BigDecimal TipoCambioInicial = BigDecimal.ZERO;
    public BigDecimal CierreIngresado   = BigDecimal.ZERO;
}
