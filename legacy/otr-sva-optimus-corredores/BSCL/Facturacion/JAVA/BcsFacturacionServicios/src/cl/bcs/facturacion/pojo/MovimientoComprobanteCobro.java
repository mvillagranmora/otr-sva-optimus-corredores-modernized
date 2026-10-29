package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class MovimientoComprobanteCobro {
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Timestamp FechaSistema;
	public String IdCobro;
	public String IdEstado;
	public String IdMonedaCobro;
	public String IdMonedaLiquidacionCobro;
	public String IdMonedaMovimientoComprobanteCobro;
	public Long IdMovimientoComprobante;
	public Long IdMovimientoComprobanteCobro;
	
	//MONTOS COBRO
	public BigDecimal MontoMovimientoComprobanteCobro;
	public BigDecimal MontoCobro;
	public BigDecimal MontoLiquidacionCobro;
	
	//MONTOS MOVIMIENTO
	public BigDecimal Monto;
	public BigDecimal MontoLiquidacion;
	public BigDecimal MontoMovimientoComprobante;
	
	public BigDecimal Porcentaje;
	public Long IdCliente;
	public Long IdPortafolio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
