package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.custodia.ConsultarCarteras (BcsCustodiaServicios). Only the data class Cartera is needed (it is imported by
 * InformeAtributtionAnalisys, which never calls the service); it is copied VERBATIM (ConsultarCarteras.java:90-212).
 */
public class ConsultarCarteras {

	public static class Cartera {
		public Long RANKG;
	    public Long IdCartera;
	    public Long FolioCartera;
	    public Long IdEmpresa;
	    public String NombreEmpresa;
	    public Date FechaOperacion;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public String DocumentoIdentidad;
	    public String RazonSocial;
	    public String CodigoPortafolio;
	    public String DescripcionPortafolio;
	    public Long IdMatrizAgrupadorAPV;
	    public String IdEstado;
	    public String NombreEstado;
	    public String IdMonedaSistema;
	    public String MonedaSistema;
	    public String CodigoIFRS;
	    public String IdConcepto;
	    public String NombreConcepto;
	    public String IdTipoOperacion;
	    public String IdTipoComprobante;
	    public Long FolioComprobante;
	    public Date FechaComprobante;
	    public Long IdMesa;
	    public String IdBolsa;
	    public String NombreBolsa;
	    public Long IdSucursal;
	    public Long IdAgente;
	    public String IdCondicionLiquidacion;
	    public String NombreCondicionLiquidacion;
	    public Date FechaLiquidacion;
	    public String IdClasificacionTipoInstrumento;
	    public String ClasificacionTipoInstrumento;
	    public String IdTipoInstrumento;
	    public String TipoInstrumento;
	    public Long IdInstrumento;
	    public String RazonSocialInstrumento;
	    public String ISIN;
	    public BigDecimal INGMontoCompraProximoMonedaInstrumento;
	    public BigDecimal INGMontoMercadoProximoMonedaInstrumento;
	    public String Nemotecnico;
	    public String IdEmisor;
	    public String NemoSVS;
	    public String IdMonedaEmision;
	    public String IdTipoReajuste;
	    public String NombreTipoReajuste;
	    public String IdMonedaInstrReajuste;
	    public String NombreMonedaInstrReajuste;
	    public Date FechaEmisionInstrumento;
	    public Date FechaVencimientoInstrumento;
	    public String EsFungible;
	    public String IdBoveda;
	    public String NombreBoveda;
	    public String NumeroCuentaBoveda;
	    public String IdTipoCustodia;
	    public String NombreTipoCustodia;
	    public String IdMonedaInstrumento;
	    public String MonedaInstrumento;
	    public BigDecimal Cantidad;
	    public BigDecimal CorteMinimo;
	    public BigDecimal ValorRescate;
	    public BigDecimal PrecioCompra;
	    public BigDecimal MontoCompra;
	    public BigDecimal MontoCompraInicial;
	    public BigDecimal ValorCompraInicial;
	    public Date INGFechaOperacion;
	    public Date INGFechaLiquidacion;
	    public BigDecimal INGCantidad;  
	    public BigDecimal TasaCompra;
	    public BigDecimal INGTasaCompra;
	    public BigDecimal INGValorRescate;    
	    public BigDecimal INGPrecioCompra;
	    public BigDecimal INGPrecioPromedioCompra;
	    public BigDecimal INGMontoPrecioPromedioCompra;
	    public BigDecimal INGMontoATasaCompraActual;
	    public Timestamp INGFechaValorizacionCartera;
	    public BigDecimal DiferenciaInversion;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
	    public String INGFuenteTasaPrecioMercadoActual;
	    public BigDecimal INGPrecioMercadoActual;
	    public BigDecimal Rentabilidad;
	    public BigDecimal RentabilidadReal;
	    public BigDecimal INGTasaMercadoActual;
	    public BigDecimal INGMontoATasaMercadoActual;
	    public BigDecimal INGMontoMercadoActualMonedaInstrumento;
	    public Date INGFechaPagoCorteCupon;
	    public BigDecimal INGInteresesPagoCorteCupon;
	    public BigDecimal INGMontoCorteCupon;
	    public BigDecimal INGMontoCapitalCorteCupon;
	    public BigDecimal INGMontoInteresCorteCupon;
	    public Date EGRFechaOperacion; 
	    public Date EGRFechaLiquidacion; 
	    public String EGRIdTipoComprobante;
	    public Long EGRFolioComprobante;
	    public Date EGRFechaComprobante;
	    public String EGRIdConcepto;
	    public String EGRNombreConcepto;
	    public BigDecimal EGRCantidad;
	    public BigDecimal EGRValorRescate;
	    public BigDecimal EGRPrecio;
	    public BigDecimal EGRTasa;
	    public BigDecimal EGRMonto;
	    public BigDecimal EGRMontoMonedaInstrumento;
	    public String FechaRespaldo;
	    public BigDecimal INGMontoReajusteCompraActual;
	    public BigDecimal INGMontoInteresCompraActual;
	    public BigDecimal InteresCorridoMonedaInstrumento;
	    public BigDecimal ReajusteCorridoMonedaInstrumento;
	    public String TipoOpcion;
	    public String RegimenTributario;
	    public Date FechaOrigenCompra;
	    public BigDecimal CostoUF;
	    public String NombreProductoAgrupador;
	    public String EsNegativa;
	    public BigDecimal MontoCorteCuponAcumulado;
	    public BigDecimal MontoCapitalCorteCuponAcumulado;
	    public BigDecimal MontoInteresCorteCuponAcumulado;
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
