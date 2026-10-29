package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class MovimientoComprobanteFormaDePago {
	public String AfectarTesoreria;
	public String AfectarCuentaInversion;
	public String CodigoBanco;
	public java.sql.Date FechaDocumentoPago;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Date FechaVctoTarjetaBancaria;
	public Long IdBanco;
	public Long IdCuentaBancaria;
	public String IdEstado;
	public String IdMedioDePago;
	public String IdModalidadDePago;
	public String IdMoneda;
	public Long IdMovimientoComprobante;
	public Long IdMovimientoComprobanteFormaDePago;
	public Long IdTarjetaBancaria;
	public String IdTipoCuentaBancaria;
	public String IdTipoMovimiento;
	public String IdTipoMovimientoSpot;
	public String IdTipoTarjetaBancaria;
	public BigDecimal MontoLiquidacionFormaDePago;
	public String NombreBanco;
	public String NombreCuentaBancaria;
	public String NombreMedioDePago;
	public String NombreModalidadDePago;
	public String NombreTipoCuentaBancaria;
	public String NombreTipoTarjetaBancaria;
	public String NumeroCuentaBancaria;
	public Long NumeroDocumento;
	public String NumeroSerie;
	public String NumeroTarjetaBancaria;
	public BigDecimal MontoComision;
	public BigDecimal TipoCambioCierre;
	public BigDecimal MontoCobro;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
