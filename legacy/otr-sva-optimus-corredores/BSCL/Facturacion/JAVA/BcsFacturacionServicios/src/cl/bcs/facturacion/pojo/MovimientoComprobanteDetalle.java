package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class MovimientoComprobanteDetalle {
	public BigDecimal Cantidad;
	public String CodigoIFRS;
	public String EsEnRueda;
	public String EsVentaCorta;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String FolioTransaccion;
	public java.sql.Timestamp FechaTransaccion;
	public Long IdCarga;
	public String IdClasificacionTipoInstrumento;
	public String IdClasificacionTipoInstrumentoRelacionado;
	public Long IdComprobanteDetalle;
	public String IdConceptoMovimiento;
	public Long IdEmpleadoEjecutivoPortafolio;	
	public String IdEstadoMovimientoComprobanteDetalle;	
	public String IdFormaCustodia;
	public Long IdInstrumento;
	public Long IdInstrumentoAnterior;
	public Long IdInstrumentoRelacionado;
	public Long IdInstrumentoSubyacente;
	public Long IdMesa;
	public String IdMoneda;
	public String IdMonedaEmision;
	public String IdMonedaLiquidacion;
	public String IdMonedaMovimientoComprobanteDetalle;
	public String IdMonedaSubyacente;
	public String IdMonedaSpot = "";
	public Long IdMovimientoComprobante;
	public Long IdMovimientoComprobanteDetalle;
	public String IdMonedaNegociacion;
	public Long IdSucursalEmpleadoEjecutivoPortafolio;
	public String IdTipoInstrumento;
	public String IdTipoInstrumentoRelacionado;
	public String IdTipoInstrumentoSubyacente;
	public String IdTipoReajuste;
	public String IdTipoLiquidacionComisionFactura;
	public Long IdTransaccion;
	public String IndicadorOrdenDirecta;
	public String ModPrefFact;
	public BigDecimal Monto;
	public BigDecimal MontoRaiz;
	public BigDecimal MontoLiquidacion;
	public BigDecimal MontoMovimientoComprobanteDetalle;
	public String NemoSVS;
	public String NemotecnicoInstrumento;
	public String NemotecnicoRelacionado;
	public String NemotecnicoSubyacente;
	public String NombreClasificacionTipoInstrumento;
	public String NombreClasificacionTipoInstrumentoRelacionado;
	public String NombreTipoInstrumento;
	public String NombreTipoInstrumentoRelacionado;
	public String NombreTipoInstrumentoSubyacente;
	public BigDecimal Precio;
	public BigDecimal PrecioRaiz;
	public BigDecimal PrecioPromedio;
	public Long SecMovimientoFacturarMigracion;
	public String SecuenciaAgrupacion;
	public BigDecimal Spread;
	public BigDecimal Tasa;
	public BigDecimal TasaEmision;
	public BigDecimal TasaOperacion;
	public BigDecimal ValorCaptacionMonedaOrigen;
	public BigDecimal ValorParidad = BigDecimal.ZERO;
	public BigDecimal ValorRescate;
	public BigDecimal ValorTipoCambio;	
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
