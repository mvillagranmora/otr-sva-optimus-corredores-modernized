package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCarteras implements JavaView{
	
	private final Logger logger =  LogManager.getLogger(ConsultarCarteras.class.getName());
	private final String SQL_SELECT_CARTERA_UI 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraUI.sql");
	private final String SQL_SELECT_CARTERA_UI_HIS 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraUI_HIS.sql");
	private final String SQL_SELECT_CARTERA_FWD_UI 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraFwdUI.sql");
	private final String SQL_SELECT_CARTERA_FWD_UI_HIS 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraFwdUI_HIS.sql");
	
	
	//SOLO RF CORTE CUPON Y SORETO DE LETRAS 
		private final String CONSULTA_RF_CUPONES_SORTEOLETRAS				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_SelectSorteoCupon.sql");
		private final String CONSULTA_RF_CUPONES_SORTEOLETRAS_HIS			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_SelectSorteoCuponHistorico.sql");
		private final String CONSULTA_RF_CUPONES_SORTEOLETRAS_TBCECCRF		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_SelectSorteoCupon_TBCECCRF.sql");
		private final String CONSULTA_RF_CUPONES_SORTEOLETRAS_TBCECCRF_HIS 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_SelectSorteoCuponHistorico_TBCECCRF.sql");
		//SOLO RF CORTE CUPON Y SORETO DE LETRAS 
	
	private final String SQL_SELECT_MESA_FORWARD 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarMesaForward.sql");

	protected Integer skip;
	protected Integer take;
	protected Long total;
	protected String sort;
	
	public static class ConsultarCarteraIn {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo; 
		public String p_FechaConsulta;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Long p_IdInstrumento;
		public String p_Nemotecnico;
		public String p_IdMoneda;
		public String p_tipoFecha;
		public Long p_FolioCartera;
		public String p_IdEstado;
		public Long p_IdProductoAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ConsultarCarterasOut {
		public List<Tabla_Salida> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ConsultarCarterasOut() {
			this.P_OUT_TABLA = new ArrayList<Tabla_Salida>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
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
	
	
	public static class Tabla_Salida{
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
	    public BigDecimal DiferenciaInversion;
	    public BigDecimal INGMontoCompraActualMonedaInstrumento;
	    public String INGFuenteTasaPrecioMercadoActual;
	    public BigDecimal INGPrecioMercadoActual;
	    public BigDecimal Rentabilidad;
	    public BigDecimal RentabilidadReal;
	    public BigDecimal CostoUF;
	    public BigDecimal INGTasaMercadoActual;
	    public BigDecimal INGMontoATasaMercadoActual;
	    public BigDecimal INGMontoMercadoActualMonedaInstrumento;
	    public Date INGFechaPagoCorteCupon;
	    public BigDecimal INGInteresesPagoCorteCupon;
	    public BigDecimal INGMontoCorteCupon;
	    public BigDecimal INGMontoCapitalCorteCupon;
	    public BigDecimal INGMontoInteresCorteCupon;
	    public BigDecimal INGMontoInteresCompraActual;
	    public BigDecimal INGMontoReajusteCompraActual;
	    public BigDecimal INGMontoCompraProximoMonedaInstrumento;
	    public BigDecimal INGMontoMercadoProximoMonedaInstrumento;
	    public BigDecimal InteresCorridoMonedaInstrumento;
	    public BigDecimal ReajusteCorridoMonedaInstrumento;
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
	    public String TipoOpcion;
	    public String NombreProductoAgrupador;
	    public Timestamp INGFechaValorizacionCartera;
	    public String RegimenTributario;
	    public Date FechaOrigenCompra;
	    public String EsNegativa;
	    public BigDecimal MontoCorteCuponAcumulado;
	    public BigDecimal MontoCapitalCorteCuponAcumulado;
	    public BigDecimal MontoInteresCorteCuponAcumulado;
	    
	    public Tabla_Salida(){
	    	this.Cantidad = BigDecimal.ZERO;
		    this.CorteMinimo = BigDecimal.ZERO;
		    this.ValorRescate = BigDecimal.ZERO;
		    this.PrecioCompra = BigDecimal.ZERO;
		    this.MontoCompra = BigDecimal.ZERO;
		    this.MontoCompraInicial = BigDecimal.ZERO;
		    this.ValorCompraInicial = BigDecimal.ZERO;
		    this.INGCantidad = BigDecimal.ZERO;
		    this.TasaCompra = BigDecimal.ZERO;
		    this.INGTasaCompra = BigDecimal.ZERO;
		    this.INGValorRescate = BigDecimal.ZERO;
		    this.INGPrecioCompra = BigDecimal.ZERO;
		    this.INGPrecioPromedioCompra = BigDecimal.ZERO;
		    this.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
		    this.INGMontoATasaCompraActual = BigDecimal.ZERO;
		    this.DiferenciaInversion = BigDecimal.ZERO;
		    this.INGMontoCompraActualMonedaInstrumento = BigDecimal.ZERO;
		    this.INGPrecioMercadoActual = BigDecimal.ZERO;
		    this.Rentabilidad = BigDecimal.ZERO;
		    this.RentabilidadReal = BigDecimal.ZERO;
		    this.CostoUF = BigDecimal.ZERO;
		    this.INGTasaMercadoActual = BigDecimal.ZERO;
		    this.INGMontoATasaMercadoActual = BigDecimal.ZERO;
		    this.INGMontoMercadoActualMonedaInstrumento = BigDecimal.ZERO;
		    this.INGInteresesPagoCorteCupon = BigDecimal.ZERO;
		    this.INGMontoCorteCupon = BigDecimal.ZERO;
		    this.INGMontoCapitalCorteCupon = BigDecimal.ZERO;
		    this.INGMontoInteresCorteCupon = BigDecimal.ZERO;
		    this.INGMontoInteresCompraActual = BigDecimal.ZERO;
		    this.INGMontoReajusteCompraActual = BigDecimal.ZERO;
		    this.INGMontoCompraProximoMonedaInstrumento = BigDecimal.ZERO;
		    this.INGMontoMercadoProximoMonedaInstrumento = BigDecimal.ZERO;
		    this.InteresCorridoMonedaInstrumento = BigDecimal.ZERO;
		    this.ReajusteCorridoMonedaInstrumento = BigDecimal.ZERO;
		    this.EGRCantidad = BigDecimal.ZERO;
		    this.EGRValorRescate = BigDecimal.ZERO;
		    this.EGRPrecio = BigDecimal.ZERO;
		    this.EGRTasa = BigDecimal.ZERO;
		    this.EGRMonto = BigDecimal.ZERO;
		    this.EGRMontoMonedaInstrumento = BigDecimal.ZERO;
		    this.MontoCorteCuponAcumulado = BigDecimal.ZERO;
		    this.MontoCapitalCorteCuponAcumulado = BigDecimal.ZERO;
		    this.MontoInteresCorteCuponAcumulado = BigDecimal.ZERO;
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	private Tabla_Salida copyData(Cartera car){
		Tabla_Salida tbl = new Tabla_Salida();
		tbl.IdCartera		= car.IdCartera;
		tbl.FolioCartera	= car.FolioCartera;
		tbl.IdEmpresa		= car.IdEmpresa;
		tbl.NombreEmpresa	= car.NombreEmpresa;
		tbl.FechaOperacion	= car.FechaOperacion;
		tbl.IdCliente		= car.IdCliente;
		tbl.IdPortafolio	= car.IdPortafolio;
		tbl.DocumentoIdentidad	= car.DocumentoIdentidad;
		tbl.RazonSocial			= car.RazonSocial;
		tbl.CodigoPortafolio	= car.CodigoPortafolio;
		tbl.DescripcionPortafolio	= car.DescripcionPortafolio;
		tbl.IdEstado			= car.IdEstado;
		tbl.NombreEstado		= car.NombreEstado;
		tbl.IdMonedaSistema		= car.IdMonedaSistema;
		tbl.MonedaSistema		= car.MonedaSistema;
		tbl.CodigoIFRS			= car.CodigoIFRS;
		tbl.IdConcepto			= car.IdConcepto;
		tbl.NombreConcepto		= car.NombreConcepto;
		tbl.IdTipoOperacion		= car.IdTipoOperacion;
		tbl.IdTipoComprobante	= car.IdTipoComprobante;
		tbl.FolioComprobante	= car.FolioComprobante;
		tbl.FechaComprobante	= car.FechaComprobante;
		tbl.IdMesa				= car.IdMesa;
		tbl.IdBolsa				= car.IdBolsa;
		tbl.NombreBolsa			= car.NombreBolsa;
		tbl.IdSucursal			= car.IdSucursal;
		tbl.IdAgente			= car.IdAgente;
		tbl.IdCondicionLiquidacion = car.IdCondicionLiquidacion;
		tbl.NombreCondicionLiquidacion = car.NombreCondicionLiquidacion;
		tbl.FechaLiquidacion				= car.FechaLiquidacion;
		tbl.IdClasificacionTipoInstrumento	= car.IdClasificacionTipoInstrumento;
		tbl.ClasificacionTipoInstrumento	= car.ClasificacionTipoInstrumento;
		tbl.IdTipoInstrumento				= car.IdTipoInstrumento;
		tbl.TipoInstrumento					= car.TipoInstrumento;
		tbl.IdInstrumento					= car.IdInstrumento;
		tbl.RazonSocialInstrumento			= car.RazonSocialInstrumento;
		tbl.ISIN							= car.ISIN;
		tbl.Nemotecnico				= car.Nemotecnico;
		tbl.IdEmisor				= car.IdEmisor;
		tbl.NemoSVS					= car.NemoSVS;
		tbl.IdMonedaEmision			= car.IdMonedaEmision;
		tbl.IdTipoReajuste			= car.IdTipoReajuste;
		tbl.NombreTipoReajuste		= car.NombreTipoReajuste;
		tbl.IdMonedaInstrReajuste	= car.IdMonedaInstrReajuste;
		tbl.NombreMonedaInstrReajuste	=  car.NombreMonedaInstrReajuste;
		tbl.FechaEmisionInstrumento		=  car.FechaEmisionInstrumento;
		tbl.FechaVencimientoInstrumento	=  car.FechaVencimientoInstrumento;
		tbl.EsFungible					= car.EsFungible;
		tbl.IdBoveda					= car.IdBoveda;
		tbl.NombreBoveda				= car.NombreBoveda;
		tbl.NumeroCuentaBoveda			= car.NumeroCuentaBoveda;
		tbl.IdTipoCustodia			= car.IdTipoCustodia;
		tbl.NombreTipoCustodia		= car.NombreTipoCustodia;
		tbl.IdMonedaInstrumento		= car.IdMonedaInstrumento;
		tbl.MonedaInstrumento		= car.MonedaInstrumento;
		tbl.Cantidad				= car.Cantidad;
		tbl.CorteMinimo				= car.CorteMinimo;
		tbl.ValorRescate			= car.ValorRescate;
		tbl.PrecioCompra			= car.PrecioCompra;
		tbl.MontoCompra				= car.MontoCompra;
		tbl.MontoCompraInicial		= car.MontoCompraInicial;
		tbl.ValorCompraInicial		= car.ValorCompraInicial;
		tbl.INGFechaOperacion		= car.INGFechaOperacion;
		tbl.INGFechaLiquidacion		= car.INGFechaLiquidacion;
		tbl.INGCantidad				= car.INGCantidad;
		tbl.TasaCompra				= car.TasaCompra;
		tbl.INGTasaCompra			= car.INGTasaCompra;	
		tbl.INGValorRescate			= car.INGValorRescate;
		tbl.INGPrecioCompra			= car.INGPrecioCompra;
		tbl.INGPrecioPromedioCompra	= car.INGMontoPrecioPromedioCompra;
		tbl.INGMontoPrecioPromedioCompra	= car.INGMontoPrecioPromedioCompra;
		tbl.INGMontoATasaCompraActual	= car.INGMontoATasaCompraActual;
		tbl.DiferenciaInversion			= car.DiferenciaInversion;
		tbl.INGMontoCompraActualMonedaInstrumento = car.INGMontoCompraActualMonedaInstrumento;
		tbl.INGFuenteTasaPrecioMercadoActual	= car.INGFuenteTasaPrecioMercadoActual;
		tbl.INGPrecioMercadoActual	= car.INGPrecioMercadoActual;
		tbl.Rentabilidad			= car.Rentabilidad;
		tbl.RentabilidadReal		= car.RentabilidadReal;
		tbl.CostoUF					= car.CostoUF;
		tbl.INGTasaMercadoActual	= car.INGTasaMercadoActual;
		tbl.INGMontoATasaMercadoActual = car.INGMontoATasaMercadoActual;
		tbl.INGMontoMercadoActualMonedaInstrumento = car.INGMontoMercadoActualMonedaInstrumento;
		tbl.INGFechaPagoCorteCupon	= car.INGFechaPagoCorteCupon;
		tbl.INGInteresesPagoCorteCupon = car.INGInteresesPagoCorteCupon;
		tbl.INGMontoCorteCupon	= car.INGMontoCorteCupon;
		tbl.INGMontoCapitalCorteCupon = car.INGMontoCapitalCorteCupon;
		tbl.INGMontoInteresCorteCupon = car.INGMontoInteresCorteCupon;
		tbl.INGMontoInteresCompraActual = car.INGMontoInteresCompraActual;
		tbl.INGMontoReajusteCompraActual = car.INGMontoReajusteCompraActual;
		tbl.InteresCorridoMonedaInstrumento = car.InteresCorridoMonedaInstrumento;
		tbl.ReajusteCorridoMonedaInstrumento = car.ReajusteCorridoMonedaInstrumento;
		tbl.EGRFechaOperacion		= car.EGRFechaOperacion;
		tbl.EGRFechaLiquidacion		= car.EGRFechaLiquidacion;
		tbl.EGRIdTipoComprobante	= car.IdTipoComprobante;
		tbl.EGRFolioComprobante		= car.EGRFolioComprobante;
		tbl.EGRFechaComprobante		= car.EGRFechaComprobante;
		tbl.EGRIdConcepto			= car.EGRIdConcepto;
		tbl.EGRNombreConcepto		= car.EGRNombreConcepto;
		tbl.EGRCantidad				= car.EGRCantidad;
		tbl.EGRValorRescate			= car.EGRValorRescate;
		tbl.EGRPrecio				= car.EGRPrecio;
		tbl.EGRTasa					= car.EGRTasa;
		tbl.EGRMonto				= car.EGRMonto;
		tbl.EGRMontoMonedaInstrumento = car.EGRMontoMonedaInstrumento;
		tbl.TipoOpcion					= car.TipoOpcion;
		tbl.NombreProductoAgrupador		= car.NombreProductoAgrupador;
		tbl.INGFechaValorizacionCartera	= car.INGFechaValorizacionCartera;
		tbl.RegimenTributario			= car.RegimenTributario;
		tbl.FechaOrigenCompra			= car.FechaOrigenCompra;
		tbl.EsNegativa = car.EsNegativa;
		tbl.MontoCorteCuponAcumulado = car.MontoCorteCuponAcumulado;
	    tbl.MontoCapitalCorteCuponAcumulado = car.MontoCapitalCorteCuponAcumulado;
	    tbl.MontoInteresCorteCuponAcumulado = car.MontoInteresCorteCuponAcumulado;
		return tbl;
	}
	
	
	public static class Mesa{		
		public Long 	IdMesa;
		public Long 	IdCliente;			
		public String 	DocumentoIdentidad;
		public Long 	IdPortafolio;
		public String 	CodigoPortafolio;
			
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuponYSorteoLetra{
		public Long FolioCartera;
		public BigDecimal Disponible;
		public BigDecimal InteresesPagados;
		
		public CuponYSorteoLetra() {
			this.InteresesPagados = BigDecimal.ZERO;
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarCarterasOut logica (Connection conn, ConsultarCarteraIn entrada) throws Exception {
		logger.info("INICIO::ConsultarCarteras");
		SQL sql=new SQL();
		ConsultarCarterasOut salida = new ConsultarCarterasOut();
		logger.debug("1");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String usaDividendosTBCECCRF = "N";
		try	{usaDividendosTBCECCRF		= ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.DividendosTBCEDVRV_TBCECCRF");} catch(Exception ex){logger.info(ex);};

		logger.debug("2");
		

		
		//Consultamos si el portafolio consultado corresponde a la mesa forward para incluir las carteras forward
		HashMap<String,Object> paramsMesaForward = new HashMap<String,Object>();
		paramsMesaForward.put("IdEmpresa", entrada.p_IdEmpresa);
		paramsMesaForward.put("IdPortafolio", entrada.p_IdPortafolio);	
		List<Mesa> v_MesaForward = new ArrayList<>();
		
		logger.debug("3");
		
		if("FWD".equals(entrada.p_IdTipoInstrumento)) {
			v_MesaForward = sql.query(conn, SQL_SELECT_MESA_FORWARD, paramsMesaForward, Mesa.class);
		}
		if (v_MesaForward.size() > 0) {
			paramsMesaForward.clear();
			paramsMesaForward.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramsMesaForward.put("p_IdEstado", entrada.p_IdEstado);
			paramsMesaForward.put("p_IdCliente", entrada.p_IdCliente);
			paramsMesaForward.put("p_IdInstrumento", entrada.p_IdInstrumento);
			paramsMesaForward.put("p_Nemotecnico", entrada.p_Nemotecnico);
			paramsMesaForward.put("p_FolioCartera", entrada.p_FolioCartera);
			paramsMesaForward.put("p_IdMoneda", entrada.p_IdMoneda);
			paramsMesaForward.put("p_tipoFecha", entrada.p_tipoFecha);
			paramsMesaForward.put("p_FechaConsulta", entrada.p_FechaConsulta);
			paramsMesaForward.put("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEstado", entrada.p_IdEstado);
		params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_IdPortafolio", entrada.p_IdPortafolio);
		params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		params.put("p_IdTipoInstrumento", entrada.p_IdTipoInstrumento);
		params.put("p_IdInstrumento", entrada.p_IdInstrumento);
		params.put("p_Nemotecnico", entrada.p_Nemotecnico);
		params.put("p_FolioCartera", entrada.p_FolioCartera);
		params.put("p_IdMoneda", entrada.p_IdMoneda);
		params.put("p_tipoFecha", entrada.p_tipoFecha);
		params.put("p_FechaConsulta", entrada.p_FechaConsulta);
		params.put("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
		
		List<Cartera> carteras = new ArrayList<Cartera>();
		List<Cartera> carterasFWD = new ArrayList<Cartera>();
		//select Consulta cartera
		logger.debug("4");
		if(Fecha.toDate(entrada.p_FechaConsulta).compareTo(Fecha.toDate(fechaSistema)) != -1){
			logger.debug("4.0");//insert filtrando por fecha
			params.put("p_ExcluyeVigentes", 'N');
			this.total= sql.countQuery(conn, SQL_SELECT_CARTERA_UI, params);
						
			carteras = sql.query(conn, SQL_SELECT_CARTERA_UI, params, Cartera.class, this.take, this.skip, this.sort);
			
			carteras = carteras.parallelStream().filter(car->("VIG".equals(car.IdEstado) && car.RANKG > 1L) || car.RANKG.equals(1L)).collect(Collectors.toList());
			
			if (v_MesaForward.size() > 0) {
				logger.debug("4.1.1");
				carterasFWD = sql.query(conn, SQL_SELECT_CARTERA_FWD_UI, paramsMesaForward, Cartera.class);
				carterasFWD = carterasFWD.parallelStream().filter(car->("VIG".equals(car.IdEstado) && car.RANKG > 1L) || car.RANKG.equals(1L)).collect(Collectors.toList());
				carteras.addAll(carterasFWD);
			}
		}
		//select Consulta cartera historica
		else {
			logger.debug("4.2");
			this.total= sql.countQuery(conn, SQL_SELECT_CARTERA_UI_HIS, params);
			
			carteras = sql.query(conn, SQL_SELECT_CARTERA_UI_HIS, params, Cartera.class, this.take, this.skip, this.sort);
			carteras = carteras.parallelStream().filter(car->("VIG".equals(car.IdEstado) && car.RANKG > 1L) || car.RANKG.equals(1L)).collect(Collectors.toList());
			
			/**** OBTIENE CARTERAS LIQUIDADAS Y VENCIDAS A LA FECHA ****/

			List<Cartera> carterasVCDLIQ = new ArrayList<Cartera>();
			params.put("p_ExcluyeVigentes", 'S');
			long totalLiqVcd = sql.countQuery(conn, SQL_SELECT_CARTERA_UI, params);
			this.total = this.total+totalLiqVcd;
			
			carterasVCDLIQ = sql.query(conn, SQL_SELECT_CARTERA_UI, params, Cartera.class, this.take, this.skip, this.sort);
			carterasVCDLIQ = carterasVCDLIQ.parallelStream().filter(car->("VIG".equals(car.IdEstado) && car.RANKG > 1L) || car.RANKG.equals(1L)).collect(Collectors.toList());
			carteras.addAll(carterasVCDLIQ);
			
			
			if (v_MesaForward.size() > 0) {
				logger.debug("4.2.1");
				carterasFWD = sql.query(conn, SQL_SELECT_CARTERA_FWD_UI_HIS, paramsMesaForward, Cartera.class);
				carterasFWD = carterasFWD.parallelStream().filter(car->("VIG".equals(car.IdEstado) && car.RANKG > 1L) || car.RANKG.equals(1L)).collect(Collectors.toList());
				carteras.addAll(carterasFWD);
			}
		}
		
		List<Long> folioCarterasRF = carteras.stream()
			    .filter(car -> "RF".equals(car.IdClasificacionTipoInstrumento))
			    .map(car -> car.FolioCartera)
			    .collect(Collectors.toList());
		
		
		if(folioCarterasRF.size() > 0) {
			//Obtiene cortes de cupon y sorteo de letras
			List<CuponYSorteoLetra> cuponesYSorteoLetras = new ArrayList<CuponYSorteoLetra>();
			List<CuponYSorteoLetra> cuponesYSorteoLetrasTBCECCRF = new ArrayList<CuponYSorteoLetra>();
			
			{
				HashMap<String,Object> paramsCupones = new HashMap<String,Object>();
				paramsCupones.put("p_FechaConsulta", entrada.p_FechaConsulta);
				paramsCupones.put("p_FolioCarteras",folioCarterasRF);

				logger.debug("INICIO::ConsultarCarterasCUPONES ");
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(fechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(fechaSistema))){
					cuponesYSorteoLetras = sql.query(conn,CONSULTA_RF_CUPONES_SORTEOLETRAS,paramsCupones,CuponYSorteoLetra.class);
				} else {
					cuponesYSorteoLetras = sql.query(conn,CONSULTA_RF_CUPONES_SORTEOLETRAS_HIS,paramsCupones,CuponYSorteoLetra.class);
				}
				logger.debug("FIN::ConsultarCarterasCUPONES ");
				
				if("S".equalsIgnoreCase(usaDividendosTBCECCRF)) {
					logger.debug("INICIO::ConsultarCarterasCUPONES_TBCECCRF");
					String FechaGoLive	= ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaGoLive");
					paramsCupones.put("FechaGoLive", FechaGoLive);

					if(Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(fechaSistema))){
						cuponesYSorteoLetrasTBCECCRF= sql.query(conn, CONSULTA_RF_CUPONES_SORTEOLETRAS_TBCECCRF, paramsCupones, CuponYSorteoLetra.class);	
					}else {
						cuponesYSorteoLetrasTBCECCRF= sql.query(conn, CONSULTA_RF_CUPONES_SORTEOLETRAS_TBCECCRF_HIS, paramsCupones, CuponYSorteoLetra.class);
					}
					logger.debug("FIN::ConsultarCarterasCUPONES_TBCECCRF");
				}
			}
			
			HashMap<Long,Object> hashEventosCapitalCliente = Utils.List2Hash(cuponesYSorteoLetras, "FolioCartera");
			for(CuponYSorteoLetra cupon : cuponesYSorteoLetrasTBCECCRF){
				if(hashEventosCapitalCliente.containsKey(cupon.FolioCartera)) {
					CuponYSorteoLetra registroEvento = (CuponYSorteoLetra) hashEventosCapitalCliente.get(cupon.FolioCartera);
					registroEvento.Disponible = registroEvento.Disponible.add(cupon.Disponible,  MathContext.DECIMAL128);
					registroEvento.InteresesPagados = registroEvento.InteresesPagados.add(cupon.InteresesPagados,  MathContext.DECIMAL128);
				}
				else {
					hashEventosCapitalCliente.put(cupon.FolioCartera, cupon);
				}
				
			}
			BigDecimal interesesPagados = BigDecimal.ZERO;
			BigDecimal totalPagados = BigDecimal.ZERO;
			BigDecimal capitalPagados = BigDecimal.ZERO;
			CuponYSorteoLetra cuponesPagados = new CuponYSorteoLetra();
				for(Cartera cartera : carteras) {
					if("RF".equals(cartera.IdClasificacionTipoInstrumento)) {
						if(!hashEventosCapitalCliente.isEmpty()) {
							if(hashEventosCapitalCliente.get(cartera.FolioCartera) != null) {
								cuponesPagados = (CuponYSorteoLetra)hashEventosCapitalCliente.get(cartera.FolioCartera);
								interesesPagados = cuponesPagados.InteresesPagados;
								totalPagados = cuponesPagados.Disponible;
								capitalPagados = totalPagados.subtract(interesesPagados);
							}
							
						}
						cartera.MontoCompraInicial = cartera.MontoCompraInicial.setScale(0, RoundingMode.HALF_UP);
						cartera.MontoCorteCuponAcumulado = totalPagados.setScale(0, RoundingMode.HALF_UP);
						cartera.MontoInteresCorteCuponAcumulado = interesesPagados.setScale(0, RoundingMode.HALF_UP);
						cartera.MontoCapitalCorteCuponAcumulado = capitalPagados.setScale(0, RoundingMode.HALF_UP);
						cartera.Rentabilidad = (cartera.MontoCompraInicial.compareTo(BigDecimal.ZERO) != 0 ? cartera.INGMontoATasaMercadoActual.subtract(cartera.MontoCompraInicial, MathContext.DECIMAL128).add(totalPagados,  MathContext.DECIMAL128).divide(cartera.MontoCompraInicial, MathContext.DECIMAL128) : BigDecimal.ZERO).multiply(new BigDecimal("100"), MathContext.DECIMAL128);
						interesesPagados = BigDecimal.ZERO;
						totalPagados = BigDecimal.ZERO;
						capitalPagados = BigDecimal.ZERO;
					}
					
				
			}
		}
		
		
	
		salida.P_OUT_TABLA = carteras.parallelStream().map(car -> copyData(car)).collect(Collectors.toList());
		
		logger.debug("FIN::ConsultarCarteras size() : " + salida.P_OUT_TABLA.size());
		logger.info("FIN::ConsultarCarteras");
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin, Integer skip, Integer take, String sort, String filters) throws Exception {
		//implementacion basica de limit y offset
		this.skip = skip;
		this.take = take;
		this.sort = sort;
		
		Gson gson = Utils.getGsonBuilder();	
		ConsultarCarteraIn in =gson.fromJson(jin.toString() ,ConsultarCarteraIn.class);
		ConsultarCarterasOut out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        dataout.add("total", new JsonPrimitive(this.total));
		return dataout;
	}

	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getPropertiesConnection("master02", "connections.properties")){
			Gson gson = Utils.getGsonBuilder();
			
			String json = 	"{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9524,\r\n"
					+ "    \"p_IdModulo\": \"CUS\",\r\n"
					+ "    \"p_tipoFecha\": \"ADQ\",\r\n"
					+ "    \"p_FechaConsulta\": \"2026-06-10\",\r\n"
					+ "    \"p_IdClasificacionTipoInstrumento\": \"RF\",\r\n"
					+ "    \"p_IdTipoInstrumento\": \"\",\r\n"
					+ "    \"p_IdInstrumento\": 0,\r\n"
					+ "    \"p_Nemotecnico\": \"\",\r\n"
					+ "    \"p_FolioCartera\": 0,\r\n"
					+ "    \"p_IdMoneda\": \"\",\r\n"
					+ "    \"p_IdEstado\": \"VIG\",\r\n"
					+ "    \"p_IdProductoAgrupador\": null,\r\n"
					+ "    \"p_IdCliente\": 5160,\r\n"
					+ "    \"p_IdPortafolio\": 4160\r\n"
					+ "}";
			//String jsonHis= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_tipoFecha\":\"ADQ\",\"p_FechaConsulta\":\"2019-06-21\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_FolioCartera\":0,\"p_IdMoneda\":\"\",\"p_IdEstado\":\"\",\"p_IdProductoAgrupador\":null,\"p_IdCliente\":123569}";
			String jsonPresura = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CUS\",\"p_tipoFecha\":\"ADQ\",\"p_FechaConsulta\":\"2020-09-02\",\"p_IdClasificacionTipoInstrumento\":\"FM\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_FolioCartera\":0,\"p_IdMoneda\":\"\",\"p_IdEstado\":\"\",\"p_IdProductoAgrupador\":null}";
			String jsonDesaMbi = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"CUS\",\"p_tipoFecha\":\"ADQ\",\"p_FechaConsulta\":\"2020-06-29\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_FolioCartera\":0,\"p_IdMoneda\":\"\",\"p_IdEstado\":\"\",\"p_IdProductoAgrupador\":null}";
			
			ConsultarCarteraIn entrada = gson.fromJson(json, ConsultarCarteraIn.class);
			ConsultarCarteras demo = new ConsultarCarteras();
			demo.skip = 0;
			demo.take = 200;
			demo.sort = "FolioCartera::desc";
			long init = System.currentTimeMillis();
			ConsultarCarterasOut salida = demo.logica(con, entrada);
			System.out.println(salida);
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));
			System.out.println(salida.P_OUT_TABLA.size());
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}
