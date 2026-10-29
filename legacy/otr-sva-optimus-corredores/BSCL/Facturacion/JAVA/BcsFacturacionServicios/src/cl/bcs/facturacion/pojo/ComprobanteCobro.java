package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class ComprobanteCobro {
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Timestamp FechaSistema;
	public Long IdCarga;
	public String IdCobro;
	public Long IdComprobante;
	public Long IdComprobanteCobro;
	public String IdEstado;
	public String IdMonedaCobro;
	public String IdMonedaComprobanteCobro;
	public String IdMonedaLiquidacionCobro;
	public BigDecimal MontoCobro;
	public BigDecimal MontoComprobanteCobro;
	public BigDecimal MontoLiquidacionCobro;
	public BigDecimal Porcentaje;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
