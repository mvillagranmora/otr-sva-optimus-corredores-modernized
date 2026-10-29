package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class ComprobanteFormaDePago {
	public String CodigoBanco;
	public java.sql.Date FechaDocumentoPago;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Date FechaVctoTarjetaBancaria;
	public Long IdBanco;
	public Long IdComprobante;
	public Long IdComprobanteFormaDePago;
	public Long IdCuentaBancaria;
	public String IdEstado;
	public String IdMedioDePago;
	public String IdModalidadDePago;
	public String IdMoneda;
	public Long IdTarjetaBancaria;
	public String IdTipoCuentaBancaria;
	public String IdTipoTarjetaBancaria;
	public BigDecimal MontoLiquidacionFormaDePago;
	public BigDecimal MontoLiquidacionFormaDePagoTesoreria;
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
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
