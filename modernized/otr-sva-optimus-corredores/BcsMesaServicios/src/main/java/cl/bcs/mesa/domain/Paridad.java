package cl.bcs.mesa.domain;

import java.math.BigDecimal;

/** Paridades de mercado (BID/ASK/Mercado) para una moneda. */
public class Paridad {
    public String IdMoneda;
    public BigDecimal ParidadMercado = BigDecimal.ZERO;
    public BigDecimal ParidadBID     = BigDecimal.ZERO;
    public BigDecimal ParidadASK     = BigDecimal.ZERO;
    public String MultiplicaODivide  = "";
}
