package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class ComprobanteDetalle {
	public BigDecimal Cantidad;
	public String EsVentaCorta;
	public java.sql.Date FechaComprobanteDetalle;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Date FechaLiquidacion;
	public Long FolioComprobanteFacturacionMigracion;
	public String IdBolsa;
	public Long IdCarga;
	public String IdClasificacionTipoInstrumento;
	public String IdClasificacionTipoInstrumentoRelacionado;
	public Long IdComprobante;
	public Long IdComprobanteDetalle;
	public String IdConcepto;
	public Long IdEmpleadoEjecutivoPortafolio;
	public String IdEstadoComprobanteDetalle;
	public String IdFormaCustodia;
	public Long IdInstrumento;
	public Long IdInstrumentoAnterior;
	public Long IdInstrumentoRelacionado;
	public Long IdInstrumentoSubyacente;
	public Long IdMesa;
	public String IdMoneda;
	public String IdMonedaComprobanteDetalle;
	public String IdMonedaLiquidacion;
	public String IdMonedaSubyacente;
	public String IdMonedaSpot;
	public String IdMonedaNegociacion;
	public Long IdSucursalEmpleadoEjecutivoPortafolio;
	public String IdTipoInstrumento;
	public String IdTipoInstrumentoRelacionado;
	public String IdTipoInstrumentoSubyacente;
	public String IdTipoLiquidacionComisionFactura;
	public String IdTipoOperacion;
	public String IdTipoProducto;
	public Long IdTransaccion;
	public String FolioTransaccion;
	public java.sql.Timestamp FechaTransaccion;
	public String IndicadorOrdenDirecta;
	public String ModPrefFact;
	public BigDecimal Monto;
	public BigDecimal MontoComprobanteDetalle;
	public BigDecimal MontoLiquidacion;
	public String NemoSVS;
	public String NemotecnicoInstrumento;
	public String NemotecnicoRelacionado;
	public String NemotecnicoSubyacente;
	public String NombreClasificacionTipoInstrumento;
	public String NombreClasificacionTipoInstrumentoRelacionado;
	public String NombreTipoInstrumento;
	public String NombreTipoInstrumentoRelacionado;
	public String NombreTipoInstrumentoSubyacente;
	public Integer NroLinea;	
	public BigDecimal Precio;
	public BigDecimal Tasa;
	public BigDecimal ValorRescate;
	public BigDecimal ValorTipoCambio;
	public String CodigoIFRS;
	public BigDecimal ValorParidad;
	public String GlosaSimplificada;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
