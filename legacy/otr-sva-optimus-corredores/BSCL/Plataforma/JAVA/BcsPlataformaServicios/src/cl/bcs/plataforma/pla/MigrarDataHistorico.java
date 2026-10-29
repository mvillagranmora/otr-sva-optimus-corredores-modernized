package cl.bcs.plataforma.pla;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

public class MigrarDataHistorico{
	
	static final Logger logger 	= LogManager.getLogger(MigrarDataHistorico.class.getName());	
	FileLoader loader 			= new FileLoader();
	
	private final String SQL_SELECT_REGISTROS_HIS_CARTERA       = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisCartera.sql");
	private final String SQL_INSERT_REGISTROS_HIS_CARTERA       = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisCartera.sql");	
	private final String SQL_SELECT_REGISTROS_HIS_SUBYACENTE    = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisSubyacente.sql");
	private final String SQL_INSERT_REGISTROS_HIS_SUBYACENTE    = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisSubyacente.sql");
	private final String SQL_SELECT_REGISTROS_HIS_GARANTIA      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisGarantia.sql");
	private final String SQL_INSERT_REGISTROS_HIS_GARANTIA      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisGarantia.sql");
	private final String SQL_SELECT_REGISTROS_HIS_CARTERA_NEGATIVA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisCarteraNegativa.sql");
	private final String SQL_INSERT_REGISTROS_HIS_CARTERA_NEGATIVA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisCarteraNegativa.sql");
	private final String SQL_SELECT_REGISTROS_HIS_CARTERA_FUSION_CARTERA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisFusionCartera.sql");
	private final String SQL_INSERT_REGISTROS_HIS_CARTERA_FUSION_CARTERA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisFusionCartera.sql");
	private final String SQL_SELECT_REGISTROS_HIS_CARTERA_FUSION_CARTERA_DETALLE  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisFusionCarteraDetalle.sql");
	private final String SQL_INSERT_REGISTROS_HIS_CARTERA_FUSION_CARTERA_DETALLE  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisFusionCarteraDetalle.sql");
	private final String SQL_SELECT_REGISTROS_HIS_RESERVA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisReserva.sql");
	private final String SQL_INSERT_REGISTROS_HIS_RESERVA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisReserva.sql");
	private final String SQL_INSERT_REGISTROS_CUS_PATRIMONIO_CUSTODIA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_CusPatrimonioCustodia.sql");
	private final String SQL_SELECT_REGISTROS_CUS_PATRIMONIO_CUSTODIA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_CusPatrimonioCustodia.sql");
	private final String SQL_INSERT_REGISTROS_CTA_PATRIMONIO_CAJA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_CtaPatrimonioCaja.sql");
	private final String SQL_SELECT_REGISTROS_CTA_PATRIMONIO_CAJA  = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_CtaPatrimonioCaja.sql");
	
	
	private final String SQL_SELECT_COUNT_HIS_GENERICO_HANA      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Count_HisGenerico_Hana.sql");
	private final String SQL_SELECT_COUNT_HIS_FUSION_DETALLE_HANA      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Count_HisFusionDetalle_Hana.sql");
	private final String SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Count_HisGenerico_SQLServer.sql");
	private final String SQL_SELECT_COUNT_HIS_FUSION_DETALLE_SQLSERVER      = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Count_HisFusionDetalle_SQLServer.sql");
	
	private final String SQL_DELETE_HIS_GENERICO_SQLSERVER     	= loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Delete_HisGenerico_SQLServer.sql");
	private final String SQL_DELETE_HIS_FUSION_DETALLE_SQLSERVER   = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Delete_HisFusionDetalle_SQLServer.sql");
	
	private static String RutaArchivoConfiguracion;
	
    public static class FiltrosLimpieza{
    	public String Tabla;
    	public String Filtro;
		public String Desde;
		public String Hasta;
	}       
    
    public static class MigrarDataHistoricoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Tabla;
    	public String p_Filtro;
		public String p_Desde;
		public String p_Hasta;
		public String p_SoloLimpia;
	}
	
	public static class MigrarDataHistoricoSalida  {
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public MigrarDataHistoricoSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class ClaseContador{
    	public Long Count;		
	}
	
	public static class HisCartera {

	    // TIMESTAMP DEFAULT '0001-01-01' NOT NULL
	    public Timestamp FechaRespaldo;
	    public Long IdCartera;
	    public Long FolioCartera;
	    public Long FolioCarteraRelacionada;
	    // TIMESTAMP DEFAULT '0001-01-01'
	    public Timestamp FechaIngresoRegistro;	    
	    // TIMESTAMP DEFAULT '0001-01-01'
	    public Timestamp FechaModificacionRegistro;
	    public Long IdEmpresa;
	    public Date FechaOperacion;
	    //ES DATE MENOR A 1753
	    public Date FechaVencimientoOperacion;
	    public Long PlazoEnDiasOperacion;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public Long IdCuentaCustodioPortafolio;
	    public Long IdClienteRelacionado;
	    public Long IdPortafolioRelacionado;
	    public String IdEstado;
	    public String IdTipoProducto;
	    public String IdTipoCanalIngreso;
	    public String IdMonedaSistema;
	    public String IdMonedaAdicional;
	    public String IdConcepto;
	    public String IdTipoOperacion;
	    public Long IdOperacion;
	    public String IdTipoComprobante;
	    public Long FolioComprobante;
	    public Date FechaComprobante;
	    public String FolioExternoReferencia;
	    public Long IdMesa;
	    public String IdBolsa;
	    public Long IdSucursal;
	    public Long IdAgente;
	    public String IdCondicionLiquidacion;
	    public Date FechaLiquidacion;
	    public String UsaGarantiaEspecifica;
	    public String CobraPrimaAlInicio;
	    public String Glosa;	    
	    public Long IdUsuario;
	    public String IdModulo;
	    public String TipoCarteraCorredor;
	    public BigDecimal ValorFactorAjusteaFechaCurse;  
	    public BigDecimal CostoInversionEnFactorAjuste; 
	    public String IdClasificacionTipoInstrumento;
	    public String IdTipoInstrumento;
	    public Long IdInstrumento;
	    public String Nemotecnico;
	    public String IdEmisor;
	    public String NemoSVS;
	    public String IdTipoReajuste;	    
	    public Date FechaEmisionInstrumento;    
	    public Date FechaVencimientoInstrumento;
	    public String EsFungible;
	    public String TieneCorteDeCupon;
	    public String CodigoIFRS;	    
	    public String IdBoveda;
	    public String IdMonedaInstrumento;
	    public Long Tamano;
	    public String ISIN;	    
	    public BigDecimal ValorFactorMonedaInstrumento;	    
	    public BigDecimal Cantidad;	    
	    public BigDecimal CorteMinimo;	    
	    public BigDecimal ValorRescate;    
	    public BigDecimal PrecioCompra;   
	    public BigDecimal TasaCompra; 
	    public BigDecimal MontoCompra; 
	    public BigDecimal MontoCompraMonedaInstrumento;
	    public BigDecimal MontoCompraMonedaAdicional;
	    public BigDecimal PrecioAPlazo;
	    public BigDecimal MontoAPlazo;
	    public BigDecimal MontoAPlazoMonedaInstrumento;
	    public BigDecimal MontoAPlazoMonedaAdicional;
	    // DATE DEFAULT '0001-01-01'
	    public Date FechaEfectiva;
	    public Long IdPlaza;
	    public Long IdFixing;
	    // DATE DEFAULT '0001-01-01'
	    public Date FechaFixing;
	    // TIME DEFAULT '00:00:00'
	    public Time HoraFixing;
	    public Long IdModalidadCumplimiento; 
	    public Long IdReferenciaMercado;
	    public String IdMonedaPrincipal;
	    public String IdMonedaSecundaria;
	    public BigDecimal ValorTipoCambioCierre;
	    public BigDecimal ValorParidadCierre;
	    public BigDecimal ValorTipoCambioCosto;
	    public BigDecimal ValorParidadCosto;
	    public BigDecimal MontoMonedaPrincipal;
	    public BigDecimal MontoMonedaSecundaria;
	    public Long IdCarga;
	    public String INGGlosa;
	    public BigDecimal INGTasaAgente;
	    public Date INGFechaOperacion;
	    public String INGIdCondicionLiquidacion;    
	    public Date INGFechaLiquidacion;
	    public BigDecimal INGCantidad;
	    public BigDecimal INGValorRescate;	    
	    public BigDecimal INGPrecioPromedioCompra;    
	    public BigDecimal INGMontoPrecioPromedioCompra;    
	    public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;	    
	    public BigDecimal INGMontoPrecioPromedioMonedaAdicional;    	 
	    public BigDecimal INGPrecioAPlazo;
	    public BigDecimal INGMontoAPlazo;	    
	    public BigDecimal INGMontoAPlazoMonedaInstrumento;  
	    public String INGEsPuntos;	    
	    public BigDecimal INGValorTipoCambioSpot;
	    public BigDecimal INGValorTipoCambioCotizador;
	    public BigDecimal INGTasaPuntosMonedaPrincipal;
	    public BigDecimal INGTasaPuntosMonedaSecundaria;
	    // TIMESTAMP DEFAULT '0001-01-01'
	    public Timestamp INGFechaValorizacionCartera;
	    public Date INGFechaValorizacionAnterior; 
	    public Date INGFechaValorizacionActual; 
	    public BigDecimal INGPrecioCompra;
	    public BigDecimal INGTasaCompra;
	    public BigDecimal INGMontoATasaCompraAnterior;
	    public BigDecimal INGMontoInteresCompraAnterior;
	    public BigDecimal INGMontoCompraAnteriorMonedaInstrumento; 
	    public BigDecimal INGMontoInteresCompraAnteriorMonedaInstrumento;
	    public BigDecimal INGMontoReajusteCompraAnteriorMonedaInstrumento; 
	    public BigDecimal INGMontoInteresCompraAnteriorMonedaAdicional; 
	    public BigDecimal INGMontoATasaCompraActual; 
	    public BigDecimal INGMontoCompraActualMonedaInstrumento;
	    public BigDecimal INGMontoCompraActualMonedaAdicional;
	    public BigDecimal INGDurationCompraActual;
	    public BigDecimal INGConvexidadCompraActual;
	    public BigDecimal INGMontoInteresCorridoCompraActual; 
	    public BigDecimal INGMontoReajusteCorridoCompraActual; 
	    public BigDecimal INGMontoInteresCorridoCompraActualMonedaInstrumento;
	    public BigDecimal INGMontoReajusteCorridoCompraActualMonedaInstrumento; 
	    public BigDecimal INGMontoInteresCorridoCompraActualMonedaAdicional;
	    public BigDecimal INGMontoATasaCompraProximo;
	    public BigDecimal INGMontoInteresCompraProximo;
	    public BigDecimal INGMontoReajusteCompraProximo;
	    public BigDecimal INGMontoCompraProximoMonedaInstrumento;
	    public BigDecimal INGMontoInteresCompraProximoMonedaInstrumento;
	    public BigDecimal INGMontoReajusteCompraProximoMonedaInstrumento;
	    public BigDecimal INGMontoInteresCompraProximoMonedaAdicional; 
	    public BigDecimal INGPrecioMercadoAnterior; 
	    public BigDecimal INGTasaMercadoAnterior;
	    public BigDecimal INGMontoATasaMercadoAnterior; 
	    public BigDecimal INGMontoInteresMercadoAnterior; 
	    public BigDecimal INGMontoMercadoAnteriorMonedaInstrumento; 
	    public BigDecimal INGMontoInteresMercadoAnteriorMonedaInstrumento; 
	    public BigDecimal INGMontoInteresMercadoAnteriorMonedaAdicional; 
	    public Date INGFechaPrecioTasaMercadoActual;
	    public String INGFuenteTasaPrecioMercadoActual;
	    public BigDecimal INGPrecioMercadoActual; 
	    public BigDecimal INGTasaMercadoActual; 
	    public BigDecimal INGMontoATasaMercadoActual; 
	    public BigDecimal INGMontoMercadoActualMonedaInstrumento; 
	    public BigDecimal INGMontoMercadoActualMonedaAdicional; 
	    public BigDecimal INGDurationMercadoActual;
	    public BigDecimal INGConvexidadMercadoActual;
	    public BigDecimal INGMontoInteresCorridoMercadoActual;
	    public BigDecimal INGMontoInteresCorridoMercadoActualMonedaInstrumento;
	    public BigDecimal INGMontoInteresCorridoMercadoActualMonedaAdicional;
	    public BigDecimal INGPrecioMercadoProximo;
	    public BigDecimal INGTasaMercadoProximo;
	    public BigDecimal INGMontoATasaMercadoProximo;
	    public BigDecimal INGMontoInteresMercadoProximo;
	    public BigDecimal INGMontoReajusteMercadoProximo;
	    public BigDecimal INGMontoMercadoProximoMonedaInstrumento;
	    public BigDecimal INGMontoInteresMercadoProximoMonedaInstrumento;
	    public BigDecimal INGMontoInteresMercadoProximoMonedaAdicional; 
	    public BigDecimal INGTasaCorredora; 
	    public Date INGFechaTasaCorredora; 
	    public BigDecimal INGMontoATasaCorredora; 
	    public BigDecimal INGMontoATasaCorredoraMonedaInstrumento;
	    public Date INGFechaCorteCupon;
	    public Long INGNumeroCupon;
	    public String INGIdMonedaPagoCupon;
	    public BigDecimal INGMontoCorteCupon;
	    public Date INGFechaPagoCorteCupon; 
	    public BigDecimal INGMontoCapitalCorteCupon;
	    public BigDecimal INGMontoInteresCorteCupon;
	    public BigDecimal INGMontoReajusteCorteCupon;
	    public BigDecimal INGMontoCorteCuponMonedaInstrumento;
	    public BigDecimal INGMontoCapitalCorteCuponMonedaInstrumento;
	    public BigDecimal INGMontoInteresCorteCuponMonedaInstrumento; 
	    public BigDecimal INGMontoReajusteCorteCuponMonedaInstrumento;
	    public BigDecimal INGMontoMonedaPrincipal; 
	    public BigDecimal INGMontoMonedaSecundaria;
	    public BigDecimal INGMontoDiferido;
	    public BigDecimal INGMontoUtilidadDiferida;
	    public BigDecimal INGMontoPerdidaDiferida;
	    public BigDecimal INGMontoMargen;
	    public BigDecimal INGMontoValorRazonableActivo; 
	    public BigDecimal INGMontoValorRazonablePasivo;
	    public BigDecimal INGMontoValorRazonableNeto; 
	    public BigDecimal INGTasaValorRazonableActivo;
	    public BigDecimal INGTasaValorRazonablePasivo;
	    public BigDecimal INGMontoDevengoTipoCambio; 
	    public BigDecimal INGMontoDevengoTipoCambioAnterior;
	    public BigDecimal INGMontoDiferidoDiario;
	    public BigDecimal INGMontoDiferidoAcumulado; 
	    public BigDecimal INGMontoMatriz; 
	    public String EGRGlosa;
	    public Date EGRFechaOperacion;
	    public String EGRIdTipoOperacion;
	    public Long EGRIdOperacion;
	    public Long EGRIdMovimientoCarteraEnTransito;
	    public String EGRIdTipoCanalIngreso;
	    public String EGRIdCondicionLiquidacion;
	    public Date EGRFechaLiquidacion;
	    public String EGRIdTipoComprobante;
	    public Long EGRFolioComprobante;
	    public Date EGRFechaComprobante;
	    public String EGRFolioExternoReferencia;
	    public String EGRIdModulo; 
	    public String EGRIdConcepto; 
	    public Long EGRIdUsuario;
	    public Long EGRIdSucursal;
	    public Long EGRIdAgente;
	    public Date EGRFechaCurseRetiro;
	    public BigDecimal EGRCostoRetiroenFactorAjuste;
	    public BigDecimal EGRValorFactorAjusteaFechaRetiro;
	    public BigDecimal EGRCantidad;
	    public BigDecimal EGRValorRescate;
	    public BigDecimal EGRPrecio; 
	    public BigDecimal EGRTasa;
	    public BigDecimal EGRMonto;
	    public BigDecimal EGRMontoMonedaInstrumento;
	    public BigDecimal EGRSpread;
	    public BigDecimal EGRPrecioAPlazo;
	    public BigDecimal EGRMontoAPlazo; 
	    public BigDecimal EGRMontoAPlazoMonedaInstrumento;
	    public Long EGRIdSpotAsociado;
	    public BigDecimal EGRMontoMonedaPrincipal;
	    public BigDecimal EGRMontoMonedaSecundaria;
	    public String EGRIdMonedaLiquidacion;
	    public BigDecimal EGRMontoLiquidacion;
	    public BigDecimal EGRMontoLiquidacionMonedaPrincipal;
	    public BigDecimal EGRValorParidad;
	    public BigDecimal EGRValorTipoCambioCotizador;
	    public BigDecimal EGRTasaDescuentoAnticipo;
	    public BigDecimal EGRTipoCambioAnticipo;
	    public BigDecimal EGRMontoARecibirAnticipo; 
	    public BigDecimal EGRTipoCambioObservado; 
	    public String IdMonedaEmision;
	    public BigDecimal INGInteresesPagoCorteCupon;
	    public Date FechaFactor; 
	    public String TipoFactor;
	    public BigDecimal ValorFactor;
	    public BigDecimal MontoCompraFactor;
	    public String IdRegimenFondoMutuo;
	    public Date FechaOrigenCompra;
	    public BigDecimal INGCostoInversionEnFactorAjuste;
	    public BigDecimal INGValorFactorAjusteaFechaCurse;
	    public BigDecimal INGValorFactorMonedaInstrumentoProximo;
	    public BigDecimal INGValorFactorMonedaInstrumentoActual;
	    public Long IdUsuarioMod;
	}
	
	public static class HisSubyacente {
		public Long IdSubyacenteHistorico;
		public Timestamp FechaRespaldo;
		public Long IdSubyacente;
		public Long FolioSubyacente;
		public Long FolioCarteraRelacionada;
		public Timestamp FechaIngresoRegistro;
		public Timestamp FechaModificacionRegistro;
		public Long IdCarteraRetirada;
		public Long IdMovimientoCarteraEnTransito;
		public BigDecimal PrecioCompraOperacionRelacionada;
		public BigDecimal TasaCompraOperacionRelacionada;
		public BigDecimal PrecioPromedioCompraOperacionRelacionada;
		public BigDecimal MontoPrecioPromedioCompraOperacionRelacionada;
		public BigDecimal MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada;
		public BigDecimal MontoPrecioPromedioMonedaAdicionalOperacionRelacionada;
		public String IdEstado;
		public String IdTipoCanalIngreso; 
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento; 
		public Long IdInstrumento; 
		public Long IdInstrumentoAnterior;
		public String Nemotecnico; 
		public String IdEmisor;
		public String NemoSVS;
		public String IdTipoReajuste;
		public Date FechaEmisionInstrumento;
		public Date FechaVencimientoInstrumento;
		public String EsFungible;
		public String ValorizaPor;
		public String TipoValorizacion;
		public String TieneCorteDeCupon;
		public String IdMonedaInstrumento;
		public Long Tamano;
		public String TIFAfectoBeneficio;
		public BigDecimal TIFTasa; 
		public String ISIN;
		public Date FechaFactorMonedaInstrumento;
		public BigDecimal ValorFactorMonedaInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal ValorRescate;
		public BigDecimal PrecioSubyacente;
		public BigDecimal TasaSubyacente;
		public BigDecimal MontoSubyacente;
		public BigDecimal MontoSubyacenteMonedaInstrumento;
		public BigDecimal MontoSubyacenteMonedaAdicional; 
		public Date INGFechaOperacion;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public Long INGTicketValorizacion;
		public String INGValorizacionConProblemas;
		public Timestamp INGFechaValorizacionSubyacente;
		public Date INGFechaValorizacionAnterior;
		public Date INGFechaValorizacionActual;
		public Date INGFechaValorizacionProximo;
		public Date INGFechaFactorMonedaInstrumentoAnterior;
		public BigDecimal INGValorFactorMonedaInstrumentoAnterior;
		public Date INGFechaFactorMonedaInstrumentoActual;
		public BigDecimal INGValorFactorMonedaInstrumentoActual;
		public Date INGFechaFactorMonedaInstrumentoProximo;
		public BigDecimal INGValorFactorMonedaInstrumentoProximo; 
		public Date INGFechaFactorMonedaAdicionalAnterior;
		public BigDecimal INGValorFactorMonedaAdicionalAnterior;
		public Date INGFechaFactorMonedaAdicionalActual; 
		public BigDecimal INGValorFactorMonedaAdicionalActual; 
		public Date INGFechaFactorMonedaAdicionalProximo;
		public BigDecimal INGValorFactorMonedaAdicionalProximo;
		public BigDecimal INGPrecioSubyacente; 
		public BigDecimal INGTasaSubyacente;
		public BigDecimal INGMontoATasaSubyacenteAnterior; 
		public BigDecimal INGMontoInteresSubyacenteAnterior;
		public BigDecimal INGMontoReajusteSubyacenteAnterior; 
		public BigDecimal INGMontoSubyacenteAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoInteresSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal INGMontoReajusteSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteAnteriorMonedaAdicional;
		public BigDecimal INGMontoInteresSubyacenteAnteriorMonedaAdicional;
		public BigDecimal INGMontoReajusteSubyacenteAnteriorMonedaAdicional;
		public BigDecimal INGDurationSubyacenteAnterior; 
		public BigDecimal INGConvexidadSubyacenteAnterior;
		public BigDecimal INGMontoSubyacenteAnteriorPibMas;
		public BigDecimal INGMontoSubyacenteAnteriorPibMasMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteAnteriorPibMasMonedaAdicional;
		public BigDecimal INGMontoSubyacenteAnteriorPibMenos;
		public BigDecimal INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteAnteriorPibMenosMonedaAdicional;
		public BigDecimal INGMontoATasaSubyacenteActual;
		public BigDecimal INGMontoSubyacenteActualMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteActualMonedaAdicional;
		public BigDecimal INGDurationSubyacenteActual;
		public BigDecimal INGConvexidadSubyacenteActual;
		public BigDecimal INGMontoInteresCorridoSubyacenteActual;
		public BigDecimal INGMontoReajusteCorridoSubyacenteActual;
		public BigDecimal INGMontoInteresCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal INGMontoInteresCorridoSubyacenteActualMonedaAdicional; 
		public BigDecimal INGMontoReajusteCorridoSubyacenteActualMonedaAdicional; 
		public BigDecimal INGMontoSubyacenteActualPibMas;
		public BigDecimal INGMontoSubyacenteActualPibMasMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteActualPibMasMonedaAdicional;
		public BigDecimal INGMontoSubyacenteActualPibMenos;
		public BigDecimal INGMontoSubyacenteActualPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoSubyacenteActualPibMenosMonedaAdicional;
		public BigDecimal INGMontoATasaSubyacenteProximo;
		public BigDecimal INGMontoInteresSubyacenteProximo;
		public BigDecimal INGMontoReajusteSubyacenteProximo;
		public BigDecimal INGMontoSubyacenteProximoMonedaInstrumento;
		public BigDecimal INGMontoInteresSubyacenteProximoMonedaInstrumento;
		public BigDecimal INGMontoReajusteSubyacenteProximoMonedaInstrumento;
		public BigDecimal INGMontoSubyacenteProximoMonedaAdicional;
		public BigDecimal INGMontoInteresSubyacenteProximoMonedaAdicional;
		public BigDecimal INGMontoReajusteSubyacenteProximoMonedaAdicional;
		public BigDecimal INGDurationSubyacenteProximo; 
		public BigDecimal INGConvexidadSubyacenteProximo;
		public Date INGFechaPrecioTasaMercadoAnterior;
		public String INGFuenteTasaPrecioMercadoAnterior;
		public BigDecimal INGPrecioMercadoAnterior;
		public BigDecimal INGTasaMercadoAnterior;
		public BigDecimal INGMontoATasaMercadoAnterior;
		public BigDecimal INGMontoInteresMercadoAnterior;
		public BigDecimal INGMontoReajusteMercadoAnterior;
		public BigDecimal INGMontoMercadoAnteriorMonedaInstrumento;
		public BigDecimal INGMontoInteresMercadoAnteriorMonedaInstrumento;
		public BigDecimal INGMontoReajusteMercadoAnteriorMonedaInstrumento;
		public BigDecimal INGMontoMercadoAnteriorMonedaAdicional;
		public BigDecimal INGMontoInteresMercadoAnteriorMonedaAdicional;
		public BigDecimal INGMontoReajusteMercadoAnteriorMonedaAdicional;
		public BigDecimal INGDurationMercadoAnterior;
		public BigDecimal INGConvexidadMercadoAnterior;
		public BigDecimal INGMontoMercadoAnteriorPibMas;
		public BigDecimal INGMontoMercadoAnteriorPibMasMonedaInstrumento;
		public BigDecimal INGMontoMercadoAnteriorPibMasMonedaAdicional;
		public BigDecimal INGMontoMercadoAnteriorPibMenos;
		public BigDecimal INGMontoMercadoAnteriorPibMenosMonedaInstrumento;
		public BigDecimal INGMontoMercadoAnteriorPibMenosMonedaAdicional; 
		public Date INGFechaPrecioTasaMercadoActual;
		public String INGFuenteTasaPrecioMercadoActual;
		public BigDecimal INGPrecioMercadoActual;
		public BigDecimal INGTasaMercadoActual;
		public BigDecimal INGMontoATasaMercadoActual;
		public BigDecimal INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal INGMontoMercadoActualMonedaAdicional;
		public BigDecimal INGDurationMercadoActual; 
		public BigDecimal INGConvexidadMercadoActual;
		public BigDecimal INGMontoInteresCorridoMercadoActual;
		public BigDecimal INGMontoReajusteCorridoMercadoActual;
		public BigDecimal INGMontoInteresCorridoMercadoActualMonedaInstrumento;
		public BigDecimal INGMontoReajusteCorridoMercadoActualMonedaInstrumento;
		public BigDecimal INGMontoInteresCorridoMercadoActualMonedaAdicional;
		public BigDecimal INGMontoReajusteCorridoMercadoActualMonedaAdicional;
		public BigDecimal INGMontoMercadoActualPibMas;
		public BigDecimal INGMontoMercadoActualPibMasMonedaInstrumento;
		public BigDecimal INGMontoMercadoActualPibMasMonedaAdicional;
		public BigDecimal INGMontoMercadoActualPibMenos;
		public BigDecimal INGMontoMercadoActualPibMenosMonedaInstrumento;
		public BigDecimal INGMontoMercadoActualPibMenosMonedaAdicional; 
		public Date INGFechaPrecioTasaMercadoProximo;
		public String INGFuenteTasaPrecioMercadoProximo;
		public BigDecimal INGPrecioMercadoProximo; 
		public BigDecimal INGTasaMercadoProximo;
		public BigDecimal INGMontoATasaMercadoProximo;
		public BigDecimal INGMontoInteresMercadoProximo;
		public BigDecimal INGMontoReajusteMercadoProximo; 
		public BigDecimal INGMontoMercadoProximoMonedaInstrumento;
		public BigDecimal INGMontoInteresMercadoProximoMonedaInstrumento;
		public BigDecimal INGMontoReajusteMercadoProximoMonedaInstrumento;
		public BigDecimal INGMontoMercadoProximoMonedaAdicional;
		public BigDecimal INGMontoInteresMercadoProximoMonedaAdicional;
		public BigDecimal INGMontoReajusteMercadoProximoMonedaAdicional;
		public BigDecimal INGDurationMercadoProximo; 
		public BigDecimal INGConvexidadMercadoProximo;
		public BigDecimal INGMontoATasaCompraAnterior;
		public BigDecimal INGMontoInteresCompraAnterior;
		public BigDecimal INGMontoReajusteCompraAnterior;
		public BigDecimal INGMontoCompraAnteriorMonedaInstrumento;
		public BigDecimal INGMontoInteresCompraAnteriorMonedaInstrumento;
		public BigDecimal INGMontoReajusteCompraAnteriorMonedaInstrumento;
		public BigDecimal INGMontoCompraAnteriorMonedaAdicional;
		public BigDecimal INGMontoInteresCompraAnteriorMonedaAdicional;
		public BigDecimal INGMontoReajusteCompraAnteriorMonedaAdicional;
		public BigDecimal INGDurationCompraAnterior;
		public BigDecimal INGConvexidadCompraAnterior;
		public BigDecimal INGMontoCompraAnteriorPibMas;
		public BigDecimal INGMontoCompraAnteriorPibMasMonedaInstrumento;
		public BigDecimal INGMontoCompraAnteriorPibMasMonedaAdicional;
		public BigDecimal INGMontoCompraAnteriorPibMenos;
		public BigDecimal INGMontoCompraAnteriorPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoCompraAnteriorPibMenosMonedaAdicional;
		public BigDecimal INGMontoATasaCompraActual; 
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoCompraActualMonedaAdicional;
		public BigDecimal INGDurationCompraActual;
		public BigDecimal INGConvexidadCompraActual;
		public BigDecimal INGMontoInteresCorridoCompraActual;
		public BigDecimal INGMontoReajusteCorridoCompraActual;
		public BigDecimal INGMontoInteresCorridoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoReajusteCorridoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoInteresCorridoCompraActualMonedaAdicional;
		public BigDecimal INGMontoReajusteCorridoCompraActualMonedaAdicional;
		public BigDecimal INGMontoCompraActualPibMas;
		public BigDecimal INGMontoCompraActualPibMasMonedaInstrumento;
		public BigDecimal INGMontoCompraActualPibMasMonedaAdicional;
		public BigDecimal INGMontoCompraActualPibMenos;
		public BigDecimal INGMontoCompraActualPibMenosMonedaInstrumento;
		public BigDecimal INGMontoCompraActualPibMenosMonedaAdicional;
		public BigDecimal INGMontoATasaCompraProximo;
		public BigDecimal INGMontoInteresCompraProximo;
		public BigDecimal INGMontoReajusteCompraProximo;
		public BigDecimal INGMontoCompraProximoMonedaInstrumento;
		public BigDecimal INGMontoInteresCompraProximoMonedaInstrumento;
		public BigDecimal INGMontoReajusteCompraProximoMonedaInstrumento;
		public BigDecimal INGMontoCompraProximoMonedaAdicional;
		public BigDecimal INGMontoInteresCompraProximoMonedaAdicional; 
		public BigDecimal INGMontoReajusteCompraProximoMonedaAdicional;
		public BigDecimal INGDurationCompraProximo;
		public BigDecimal INGConvexidadCompraProximo;
		public BigDecimal INGTasaCorredora; 
		public Date INGFechaTasaCorredora; 
		public BigDecimal INGMontoATasaCorredora; 
		public BigDecimal INGMontoATasaCorredoraMonedaInstrumento;
		public BigDecimal INGMontoATasaCorredoraMonedaAdicional;
		public Date INGFechaCorteCupon;
		public Long INGNumeroCupon;
		public String INGIdMonedaPagoCupon;
		public BigDecimal INGMontoCorteCupon;
		public Date INGFechaPagoCorteCupon;
		public BigDecimal INGMontoCapitalCorteCupon;
		public BigDecimal INGMontoInteresCorteCupon;
		public BigDecimal INGMontoReajusteCorteCupon;
		public BigDecimal INGMontoCorteCuponMonedaInstrumento;
		public BigDecimal INGMontoCapitalCorteCuponMonedaInstrumento;
		public BigDecimal INGMontoInteresCorteCuponMonedaInstrumento;
		public BigDecimal INGMontoReajusteCorteCuponMonedaInstrumento;
		public BigDecimal INGMontoCorteCuponMonedaAdicional;
		public BigDecimal INGMontoCapitalCorteCuponMonedaAdicional;
		public BigDecimal INGMontoInteresCorteCuponMonedaAdicional;
		public BigDecimal INGMontoReajusteCorteCuponMonedaAdicional;
		public BigDecimal INGMontoPresenteTIF; 
		public BigDecimal INGMontoInteresTIF;
		public BigDecimal INGMontoReajusteTIF;
		public BigDecimal INGMontoInteresAcumTIF;
		public BigDecimal INGMontoReajusteAcumTIF;
		public BigDecimal INGMontoPresenteTIFMonedaInstrumento;
		public BigDecimal INGMontoInteresTIFMonedaInstrumento;
		public BigDecimal INGMontoReajusteTIFMonedaInstrumento;
		public BigDecimal INGMontoInteresAcumTIFMonedaInstrumento;
		public BigDecimal INGMontoReajusteAcumTIFMonedaInstrumento;
		public BigDecimal INGMontoPresenteTIFMonedaAdicional;
		public BigDecimal INGMontoInteresTIFMonedaAdicional;
		public BigDecimal INGMontoReajusteTIFMonedaAdicional;
		public BigDecimal INGMontoInteresAcumTIFMonedaAdicional;
		public BigDecimal INGMontoReajusteAcumTIFMonedaAdicional; 
		public Long EGRTicketValorizacion; 
		public String EGRValorizacionConProblemas;
		public Date EGRFechaFactorMonedaInstrumento;
		public BigDecimal EGRValorFactorMonedaInstrumento;
		public Date EGRFechaFactorMonedaAdicional;
		public BigDecimal EGRValorFactorMonedaAdicional;
		public Date EGRFechaOperacion;
		public String EGRIdTipoCanalIngreso;
		public String EGRIdCondicionLiquidacion;
		public Date EGRFechaLiquidacion;
		public String EGRIdTipoComprobante;
		public Long EGRFolioComprobante;
		public Date EGRFechaComprobante;
		public String EGRFolioExternoReferencia;
		public String EGRIdModulo;
		public String EGRIdTipoOperacion;
		public Long EGRIdOperacion;
		public String EGRIdConcepto;
		public Long EGRIdUsuario;
		public Long EGRIdSucursal;
		public Long EGRIdAgente;
		public BigDecimal EGRCantidad;
		public BigDecimal EGRValorRescate;
		public BigDecimal EGRPrecio; 
		public BigDecimal EGRTasa;
		public BigDecimal EGRMonto;
		public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRMontoMonedaAdicional; 
		public BigDecimal EGRSpread;
		public Date EGRFechaPrecioTasaMercado;
		public String EGRFuenteTasaPrecioMercado; 
		public BigDecimal EGRPrecioMercado;
		public BigDecimal EGRTasaMercado;
		public BigDecimal EGRMontoATasaMercado;
		public BigDecimal EGRMontoMercadoMonedaInstrumento;
		public BigDecimal EGRMontoMercadoMonedaAdicional;
		public BigDecimal EGRSpreadMercado;
		public BigDecimal EGRMontoPresenteTIF;
		public BigDecimal EGRMontoPresenteTIFMonedaInstrumento;
		public BigDecimal EGRMontoPresenteTIFMonedaAdicional;
		public String IdMonedaEmision;
	}
	
	public static class HisGarantia {
		public Long IdGarantiaHistorico;
		public Timestamp FechaRespaldo;
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long FolioCarteraOrigen;
		public Long IdCarteraOrigen;
		public Long FolioCarteraGarantizada;
		public Long IdInstrumentoCarteraGarantizada;
		public String EsEspecifica; 
		public Long IdContraparteGarantia; 
		public String RazonSocialContraparteGarantia; 
		public String CuentaContraparteGarantia; 
		public String IdOrigenGarantia; 
		public Long IdEspecieGarantia; 
		public Timestamp FechaIngresoRegistro; 
		public Timestamp FechaModificacionRegistro; 
		public BigDecimal PrecioCompraOperacionOrigen; 
		public BigDecimal TasaCompraOperacionOrigen; 
		public BigDecimal PrecioPromedioCompraOperacionOrigen; 
		public BigDecimal MontoPrecioPromedioCompraOperacionOrigen; 
		public BigDecimal MontoPrecioPromedioMonedaInstrumentoOperacionOrigen; 
		public BigDecimal MontoPrecioPromedioMonedaAdicionalOperacionOrigen; 
		public Long IdEmpresa; 
		public Date FechaOperacion; 
		public Date FechaVencimientoOperacion; 
		public Long PlazoEnDiasOperacion; 
		public Long IdCliente; 
		public Long IdPortafolio; 
		public Long IdCuentaCustodioPortafolio; 
		public String IdEstado; 
		public String IdTipoCanalIngreso; 
		public String IdMonedaSistema; 
		public String IdMonedaAdicional; 
		public Date FechaFactorMonedaAdicional; 
		public BigDecimal ValorFactorMonedaAdicional; 
		public String IdConcepto; 
		public String IdTipoComprobante; 
		public Long FolioComprobante; 
		public Date FechaComprobante; 
		public String FolioExternoReferencia; 
		public Long IdSucursal; 
		public Long IdAgente; 
		public String IdCondicionLiquidacion; 
		public Date FechaLiquidacion; 
		public Long IdUsuario; 
		public String IdModulo; 
		public Long IdMovimientoCarteraEnTransito; 
		public String IdClasificacionTipoInstrumento; 
		public String IdTipoInstrumento; 
		public Long IdInstrumento; 
		public Long IdInstrumentoAnterior; 
		public String Nemotecnico; 
		public String IdEmisor; 
		public String NemoSVS; 
		public String IdTipoReajuste; 
		public Date FechaEmisionInstrumento; 
		public Date FechaVencimientoInstrumento; 
		public String EsFungible; 
		public String ValorizaPor; 
		public String TipoValorizacion; 
		public String TieneCorteDeCupon; 
		public String CodigoIFRS; 
		public String IdMonedaInstrumento; 
		public Long Tamano; 
		public String TIFAfectoBeneficio; 
		public BigDecimal TIFTasa; 
		public String ISIN; 
		public Date FechaFactorMonedaInstrumento; 
		public BigDecimal ValorFactorMonedaInstrumento; 
		public BigDecimal Cantidad; 
		public BigDecimal CorteMinimo; 
		public BigDecimal ValorRescate; 
		public BigDecimal PrecioGarantia; 
		public BigDecimal TasaGarantia; 
		public BigDecimal MontoGarantia; 
		public BigDecimal MontoGarantiaMonedaInstrumento; 
		public BigDecimal MontoGarantiaMonedaAdicional; 
		public Long IdCarga; 
		public Long IdSecuenciaCarteraMigracion; 
		public String CodigoPeriodo; 
		public Date INGFechaOperacion; 
		public String INGIdCondicionLiquidacion; 
		public Date INGFechaLiquidacion; 
		public BigDecimal INGCantidad; 
		public BigDecimal INGValorRescate; 
		public Long INGTicketValorizacion; 
		public String INGValorizacionConProblemas; 
		public Timestamp INGFechaValorizacionGarantia; 
		public Date INGFechaValorizacionAnterior; 
		public Date INGFechaValorizacionActual; 
		public Date INGFechaValorizacionProximo; 
		public Date INGFechaFactorMonedaInstrumentoAnterior; 
		public BigDecimal INGValorFactorMonedaInstrumentoAnterior; 
		public Date INGFechaFactorMonedaInstrumentoActual; 
		public BigDecimal INGValorFactorMonedaInstrumentoActual; 
		public Date INGFechaFactorMonedaInstrumentoProximo; 
		public BigDecimal INGValorFactorMonedaInstrumentoProximo; 
		public Date INGFechaFactorMonedaAdicionalAnterior; 
		public BigDecimal INGValorFactorMonedaAdicionalAnterior; 
		public Date INGFechaFactorMonedaAdicionalActual; 
		public BigDecimal INGValorFactorMonedaAdicionalActual; 
		public Date INGFechaFactorMonedaAdicionalProximo; 
		public BigDecimal INGValorFactorMonedaAdicionalProximo; 
		public BigDecimal INGPrecioGarantia; 
		public BigDecimal INGTasaGarantia; 
		public BigDecimal INGMontoATasaGarantiaAnterior; 
		public BigDecimal INGMontoInteresGarantiaAnterior; 
		public BigDecimal INGMontoReajusteGarantiaAnterior; 
		public BigDecimal INGMontoGarantiaAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoInteresGarantiaAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoReajusteGarantiaAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaAnteriorMonedaAdicional; 
		public BigDecimal INGMontoInteresGarantiaAnteriorMonedaAdicional; 
		public BigDecimal INGMontoReajusteGarantiaAnteriorMonedaAdicional; 
		public BigDecimal INGDurationGarantiaAnterior; 
		public BigDecimal INGConvexidadGarantiaAnterior; 
		public BigDecimal INGMontoGarantiaAnteriorPibMas; 
		public BigDecimal INGMontoGarantiaAnteriorPibMasMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaAnteriorPibMasMonedaAdicional; 
		public BigDecimal INGMontoGarantiaAnteriorPibMenos; 
		public BigDecimal INGMontoGarantiaAnteriorPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaAnteriorPibMenosMonedaAdicional; 
		public BigDecimal INGMontoATasaGarantiaActual; 
		public BigDecimal INGMontoGarantiaActualMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaActualMonedaAdicional; 
		public BigDecimal INGDurationGarantiaActual; 
		public BigDecimal INGConvexidadGarantiaActual; 
		public BigDecimal INGMontoInteresCorridoGarantiaActual; 
		public BigDecimal INGMontoReajusteCorridoGarantiaActual; 
		public BigDecimal INGMontoInteresCorridoGarantiaActualMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCorridoGarantiaActualMonedaInstrumento; 
		public BigDecimal INGMontoInteresCorridoGarantiaActualMonedaAdicional; 
		public BigDecimal INGMontoReajusteCorridoGarantiaActualMonedaAdicional; 
		public BigDecimal INGMontoGarantiaActualPibMas; 
		public BigDecimal INGMontoGarantiaActualPibMasMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaActualPibMasMonedaAdicional; 
		public BigDecimal INGMontoGarantiaActualPibMenos; 
		public BigDecimal INGMontoGarantiaActualPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaActualPibMenosMonedaAdicional; 
		public BigDecimal INGMontoATasaGarantiaProximo; 
		public BigDecimal INGMontoInteresGarantiaProximo; 
		public BigDecimal INGMontoReajusteGarantiaProximo; 
		public BigDecimal INGMontoGarantiaProximoMonedaInstrumento; 
		public BigDecimal INGMontoInteresGarantiaProximoMonedaInstrumento; 
		public BigDecimal INGMontoReajusteGarantiaProximoMonedaInstrumento; 
		public BigDecimal INGMontoGarantiaProximoMonedaAdicional; 
		public BigDecimal INGMontoInteresGarantiaProximoMonedaAdicional; 
		public BigDecimal INGMontoReajusteGarantiaProximoMonedaAdicional; 
		public BigDecimal INGDurationGarantiaProximo; 
		public BigDecimal INGConvexidadGarantiaProximo; 
		public Date INGFechaPrecioTasaMercadoAnterior; 
		public String INGFuenteTasaPrecioMercadoAnterior; 
		public BigDecimal INGPrecioMercadoAnterior; 
		public BigDecimal INGTasaMercadoAnterior; 
		public BigDecimal INGMontoATasaMercadoAnterior; 
		public BigDecimal INGMontoInteresMercadoAnterior; 
		public BigDecimal INGMontoReajusteMercadoAnterior; 
		public BigDecimal INGMontoMercadoAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoInteresMercadoAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoReajusteMercadoAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoMercadoAnteriorMonedaAdicional; 
		public BigDecimal INGMontoInteresMercadoAnteriorMonedaAdicional; 
		public BigDecimal INGMontoReajusteMercadoAnteriorMonedaAdicional; 
		public BigDecimal INGDurationMercadoAnterior; 
		public BigDecimal INGConvexidadMercadoAnterior; 
		public BigDecimal INGMontoMercadoAnteriorPibMas; 
		public BigDecimal INGMontoMercadoAnteriorPibMasMonedaInstrumento; 
		public BigDecimal INGMontoMercadoAnteriorPibMasMonedaAdicional; 
		public BigDecimal INGMontoMercadoAnteriorPibMenos; 
		public BigDecimal INGMontoMercadoAnteriorPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoMercadoAnteriorPibMenosMonedaAdicional;
		public Date INGFechaPrecioTasaMercadoActual;
		public String INGFuenteTasaPrecioMercadoActual; 
		public BigDecimal INGPrecioMercadoActual; 
		public BigDecimal INGTasaMercadoActual; 
		public BigDecimal INGMontoATasaMercadoActual; 
		public BigDecimal INGMontoMercadoActualMonedaInstrumento; 
		public BigDecimal INGMontoMercadoActualMonedaAdicional; 
		public BigDecimal INGDurationMercadoActual; 
		public BigDecimal INGConvexidadMercadoActual; 
		public BigDecimal INGMontoInteresCorridoMercadoActual; 
		public BigDecimal INGMontoReajusteCorridoMercadoActual; 
		public BigDecimal INGMontoInteresCorridoMercadoActualMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCorridoMercadoActualMonedaInstrumento; 
		public BigDecimal INGMontoInteresCorridoMercadoActualMonedaAdicional; 
		public BigDecimal INGMontoReajusteCorridoMercadoActualMonedaAdicional; 
		public BigDecimal INGMontoMercadoActualPibMas; 
		public BigDecimal INGMontoMercadoActualPibMasMonedaInstrumento; 
		public BigDecimal INGMontoMercadoActualPibMasMonedaAdicional; 
		public BigDecimal INGMontoMercadoActualPibMenos; 
		public BigDecimal INGMontoMercadoActualPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoMercadoActualPibMenosMonedaAdicional; 
		public Date INGFechaPrecioTasaMercadoProximo; 
		public String INGFuenteTasaPrecioMercadoProximo; 
		public BigDecimal INGPrecioMercadoProximo; 
		public BigDecimal INGTasaMercadoProximo; 
		public BigDecimal INGMontoATasaMercadoProximo; 
		public BigDecimal INGMontoInteresMercadoProximo; 
		public BigDecimal INGMontoReajusteMercadoProximo; 
		public BigDecimal INGMontoMercadoProximoMonedaInstrumento; 
		public BigDecimal INGMontoInteresMercadoProximoMonedaInstrumento; 
		public BigDecimal INGMontoReajusteMercadoProximoMonedaInstrumento; 
		public BigDecimal INGMontoMercadoProximoMonedaAdicional; 
		public BigDecimal INGMontoInteresMercadoProximoMonedaAdicional; 
		public BigDecimal INGMontoReajusteMercadoProximoMonedaAdicional; 
		public BigDecimal INGDurationMercadoProximo; 
		public BigDecimal INGConvexidadMercadoProximo; 
		public BigDecimal INGMontoATasaCompraAnterior; 
		public BigDecimal INGMontoInteresCompraAnterior; 
		public BigDecimal INGMontoReajusteCompraAnterior; 
		public BigDecimal INGMontoCompraAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoInteresCompraAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCompraAnteriorMonedaInstrumento; 
		public BigDecimal INGMontoCompraAnteriorMonedaAdicional; 
		public BigDecimal INGMontoInteresCompraAnteriorMonedaAdicional; 
		public BigDecimal INGMontoReajusteCompraAnteriorMonedaAdicional; 
		public BigDecimal INGDurationCompraAnterior; 
		public BigDecimal INGConvexidadCompraAnterior; 
		public BigDecimal INGMontoCompraAnteriorPibMas; 
		public BigDecimal INGMontoCompraAnteriorPibMasMonedaInstrumento; 
		public BigDecimal INGMontoCompraAnteriorPibMasMonedaAdicional; 
		public BigDecimal INGMontoCompraAnteriorPibMenos; 
		public BigDecimal INGMontoCompraAnteriorPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoCompraAnteriorPibMenosMonedaAdicional; 
		public BigDecimal INGMontoATasaCompraActual; 
		public BigDecimal INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal INGMontoCompraActualMonedaAdicional; 
		public BigDecimal INGDurationCompraActual; 
		public BigDecimal INGConvexidadCompraActual; 
		public BigDecimal INGMontoInteresCorridoCompraActual; 
		public BigDecimal INGMontoReajusteCorridoCompraActual; 
		public BigDecimal INGMontoInteresCorridoCompraActualMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCorridoCompraActualMonedaInstrumento; 
		public BigDecimal INGMontoInteresCorridoCompraActualMonedaAdicional; 
		public BigDecimal INGMontoReajusteCorridoCompraActualMonedaAdicional; 
		public BigDecimal INGMontoCompraActualPibMas; 
		public BigDecimal INGMontoCompraActualPibMasMonedaInstrumento; 
		public BigDecimal INGMontoCompraActualPibMasMonedaAdicional; 
		public BigDecimal INGMontoCompraActualPibMenos; 
		public BigDecimal INGMontoCompraActualPibMenosMonedaInstrumento; 
		public BigDecimal INGMontoCompraActualPibMenosMonedaAdicional; 
		public BigDecimal INGMontoATasaCompraProximo; 
		public BigDecimal INGMontoInteresCompraProximo; 
		public BigDecimal INGMontoReajusteCompraProximo; 
		public BigDecimal INGMontoCompraProximoMonedaInstrumento; 
		public BigDecimal INGMontoInteresCompraProximoMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCompraProximoMonedaInstrumento; 
		public BigDecimal INGMontoCompraProximoMonedaAdicional; 
		public BigDecimal INGMontoInteresCompraProximoMonedaAdicional; 
		public BigDecimal INGMontoReajusteCompraProximoMonedaAdicional; 
		public BigDecimal INGDurationCompraProximo; 
		public BigDecimal INGConvexidadCompraProximo; 
		public BigDecimal INGTasaCorredora; 
		public Date INGFechaTasaCorredora; 
		public BigDecimal INGMontoATasaCorredora; 
		public BigDecimal INGMontoATasaCorredoraMonedaInstrumento; 
		public BigDecimal INGMontoATasaCorredoraMonedaAdicional; 
		public Date INGFechaCorteCupon; 
		public Long INGNumeroCupon; 
		public String INGIdMonedaPagoCupon; 
		public BigDecimal INGMontoCorteCupon; 
		public Date INGFechaPagoCorteCupon; 
		public BigDecimal INGMontoCapitalCorteCupon; 
		public BigDecimal INGMontoInteresCorteCupon; 
		public BigDecimal INGMontoReajusteCorteCupon; 
		public BigDecimal INGMontoCorteCuponMonedaInstrumento; 
		public BigDecimal INGMontoCapitalCorteCuponMonedaInstrumento; 
		public BigDecimal INGMontoInteresCorteCuponMonedaInstrumento; 
		public BigDecimal INGMontoReajusteCorteCuponMonedaInstrumento; 
		public BigDecimal INGMontoCorteCuponMonedaAdicional; 
		public BigDecimal INGMontoCapitalCorteCuponMonedaAdicional; 
		public BigDecimal INGMontoInteresCorteCuponMonedaAdicional; 
		public BigDecimal INGMontoReajusteCorteCuponMonedaAdicional; 
		public Long EGRTicketValorizacion; 
		public String EGRValorizacionConProblemas; 
		public Date EGRFechaFactorMonedaInstrumento; 
		public BigDecimal EGRValorFactorMonedaInstrumento; 
		public Date EGRFechaFactorMonedaAdicional; 
		public BigDecimal EGRValorFactorMonedaAdicional; 
		public Date EGRFechaOperacion; 
		public String EGRIdTipoOperacion; 
		public Long EGRIdOperacion; 
		public Long EGRIdMovimientoCarteraEnTransito; 
		public String EGRIdTipoCanalIngreso; 
		public String EGRIdCondicionLiquidacion; 
		public Date EGRFechaLiquidacion; 
		public String EGRIdTipoComprobante; 
		public Long EGRFolioComprobante;
		public Date EGRFechaComprobante;
		public String EGRFolioExternoReferencia;
		public String EGRIdModulo;
		public String EGRIdConcepto;
		public Long EGRIdUsuario;
		public Long EGRIdSucursal;
		public Long EGRIdAgente;
		public BigDecimal EGRCantidad;
		public BigDecimal EGRValorRescate;
		public BigDecimal EGRPrecio;
		public BigDecimal EGRTasa;
		public BigDecimal EGRMonto;
		public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRMontoMonedaAdicional;
		public String IdMonedaEmision;
	}
	
	public static class HisCarteraNegativa {
		public Date FechaRespaldo; 
		public Long IdCarteraNegativa;
		//SECONDDATE
		public Timestamp FechaIngresoRegistro;
		//SECONDDATE
		public Timestamp FechaNeteo;
		public String IdEstado;
		public Long IdUsuarioIngreso;
		public Long IdUsuarioNeteo;
		public Date FechaOperacion;
		public String IdTipoOperacion;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String IdConcepto;
		public Long IdCuentaCustodioPortafolio;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public String CodigoIFRS;
		public Long IdMesa;
		public String Nemotecnico;
		public String IdEmisor;
		public Date FechaVencimientoOperacion;
		public String EsFungible;
		public String IdTipoReajuste;
		public Long IdOperacionMesa;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public Long IdMovimientoComprobante;
		public BigDecimal PrecioOperacion;
		public BigDecimal MontoOperacionMonedaInstrumento;
		public BigDecimal TasaOperacion;
		public String IdBoveda;
		public String IdTipoProducto;
		public Long IdCorredoraContraparte;
		public String FolioTransaccion;
		public Date FechaTransaccion;
		public String IdBolsa;
		public Long IdOperacion;
		public Long IdAgente;
		public String IdMonedaInstrumento;
		public String IdTipoCanalIngreso;
		public BigDecimal CorteMinimo;
		public Long FolioCustodia;
		public String FolioExternoReferencia;
		public Long IdSucursal;
		public String Glosa;
		public String NemoSVS;
		public String IdMonedaEmision;
		//SECONDDATE
		public Timestamp FechaValorizacion;
		public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public Date FechaPrecioTasa;
		public String FuentePrecioTasa;
		public BigDecimal MontoCorteCupon;
		public String IdMonedaPagoCupon;
		public Date FechaCorteCupon;
		public Long NumeroCupon;
		public BigDecimal MontoCapitalCorteCupon;
		public BigDecimal MontoInteresCorteCupon;
		public BigDecimal InteresesPagoCorteCupon;
		public BigDecimal MontoCapitalCorteCuponMonedaInstrumento;
		public BigDecimal MontoReajusteCorteCupon;
		public Date FechaPagoCorteCupon;
		public BigDecimal MontoCorteCuponMonedaInstrumento;
		public Long IdInstrumentoAnterior;
	}
	
	public static class HisFusionCartera {
		 public Long IdFusion;
		 public Timestamp FechaFusion;
		 public BigDecimal CantidadFusion;
		 public BigDecimal TasaFusion;
		 public BigDecimal ValorRescateFusion;
		 public String IdClasificacionTipoInstrumento;
		 public BigDecimal MontoFusion;
		 public BigDecimal MontoValorizado;
		 public Long IdCarteraResultante;
		 public String IdModulo;
		 public Long IdEmpresa;
		 public Long IdUsuario;
	}
		
	public static class HisFusionCarteraDetalle {
		 public Long IdCartera;
		 public Long IdFusion;
		 public BigDecimal Tasa;
		 public BigDecimal Cantidad;
		 public BigDecimal ValorRescate;
		 public BigDecimal CorteMinimo;
		 public String EsFungible;
	}

	public static class HisReserva {
		 public Long IdReservaHistorico;
		 public Timestamp FechaRespaldo;
		 public Long IdReserva;
		 public Long IdOperacion;
		 public Long FolioCarteraOrigen;
		 public Long FolioCarteraCustodia;
		 public Long FolioOperacion;
		 public String IdEstado;
		 public Long IdUsuarioCreacion;
		 public String IdTipoProducto;
		 public String IdConcepto;
		 public BigDecimal Tasa;
		 public BigDecimal TasaMercado;
		 public BigDecimal Precio;
		 public BigDecimal PrecioMercado;
		 public BigDecimal MontoValorizado;
		 public String IdClasificacionTipoInstrumento;
		 public String IdInstrumento;
		 public String IdTipoInstrumento;
		 public String IdTipoReajuste;
		 public String IdEmisor;
		 public Timestamp FechaVencimiento;
		 public String EsFungible;
		 public String IdBolsa;
		 public String IdBoveda;
		 public String CodigoIFRS;
		 public BigDecimal CorteMinimo;
		 public BigDecimal CantidadInicial;
		 public BigDecimal ValorRescateInicial;
		 public BigDecimal MontoValorizadoReserva;
		 public Timestamp FechaVencimientoReserva;
		 public Timestamp FechaVencimientoProducto;
		 public Timestamp FechaCreacion;
		 public Long IngIdUsuario;
		 public BigDecimal IngCantidad;
		 public BigDecimal IngValorRescate;
		 public BigDecimal IngMontoValorizado;
		 public Timestamp IngFecha;
		 public Long EgrIdUsuario;
		 public BigDecimal EgrCantidad;
		 public BigDecimal EgrValorRescate;
		 public BigDecimal EgrMontoValorizado; 
		 public Timestamp EgrFecha;
		 public Long SecuenciaCompromisoMigracion; 
		 public Long IdCarga;
		 public Long IdCarteraCustodia;
		 public String IdMonedaLiquidacionInstrumento;
		 public String IdMonedaEmisionInstrumento;
		 public Long IdCarteraOrigen;
		 public String IdMoneda;
		 public BigDecimal MontoLiquidacionOperacion;
		 public BigDecimal MontoLiquidacion;
		 public String IdMonedaLiquidacionOperacion;
		 public String Nemotecnico;
		 public Long FolioSubyacente;
	}
	
	public static class CtaPatrimonioCaja {
	    public Long IdPatrimonioCaja;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public Date FechaPatrimonio;
	    public String TipoFechaConsulta;
	    public BigDecimal MontoPatrimonio;
	    public BigDecimal SaldoDisponible;
	    public BigDecimal SaldoEnGarantia;
	    public BigDecimal SaldoRetiro;
	    public BigDecimal SaldoAPlazoSimultanea;
	    public BigDecimal SaldoAPlazoVentaCorta;
	    public BigDecimal SaldoAPlazoCompromiso;
	    public String IdMoneda;
	    public String IdMonedaSistema;
	    public String IdEstado;
	    public BigDecimal MontoPatrimonioAfectoComision;
	    public BigDecimal MontoComision;
	    public String IdEstadoComision;
	    public BigDecimal PorcentajeCobroComision;
	}
	
	public static class CusPatrimonioCustodia {
	    public Long IdPatrimonioCustodia;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public Long IdInstrumento;
	    public String Nemotecnico;
	    public Date FechaPatrimonio;
	    public String TipoFechaConsulta;
	    public BigDecimal CantidadPatrimonio;
	    public BigDecimal MontoPatrimonio;
	    public BigDecimal CantidadCustodia;
	    public BigDecimal MontoMercadoCustodia;
	    public BigDecimal CantidadGarantia;
	    public BigDecimal MontoMercadoGarantia;
	    public BigDecimal CantidadSimulCom;
	    public BigDecimal MontoMercadoSimulCom;
	    public BigDecimal CantidadSimulVen;
	    public BigDecimal MontoMercadoSimulVen;
	    public BigDecimal CantidadPrestamo;
	    public BigDecimal MontoMercadoPrestamo;
	    public BigDecimal CantidadEmpresti;
	    public BigDecimal MontoMercadoEmpresti;
	    public BigDecimal CantidadPactosV;
	    public BigDecimal MontoMercadoPactosV;
	    public BigDecimal CantidadPactosC;
	    public BigDecimal MontoMercadoPactosC;
	    public BigDecimal CantidadTranomEgr;
	    public BigDecimal MontoMercadoTransEgrNom;
	    public BigDecimal CantidadTranomIng;
	    public BigDecimal MontoMercadoTransIngNom;
	    public BigDecimal CantidadTransIng;
	    public BigDecimal MontoMercadoTransIng;
	    public BigDecimal CantidadTransEgr;
	    public BigDecimal MontoMercadoTransEgr;
	    public BigDecimal CantidadTransVenDesEgr;
	    public BigDecimal PrecioMercadoCustodia;
	    public String IdRegimenTributario;
	    public String IdMonedaSistema;
	    public String IdEstado;
	    public Date FechaFuente;
	    public BigDecimal MontoComision;
	    public String IdEstadoComision;
	    public BigDecimal PorcentajeCobroComision;
	}
	
	public MigrarDataHistoricoSalida logica(Connection conn, MigrarDataHistoricoEntrada entrada) throws Exception {
		System.out.println("Inicio servicio MigrarDataHistorica");

		MigrarDataHistoricoSalida out = new MigrarDataHistoricoSalida();
		SQL sql = new SQL();

		
			Long cantidadRegistrosAMigrar = -1L;
			
			if(entrada.p_Tabla.equals("\"BSCL.Custodia.BD::HIS.FusionCarteraDetalle\"")) {
				cantidadRegistrosAMigrar = contarRegistrosHana(SQL_SELECT_COUNT_HIS_FUSION_DETALLE_HANA, conn, sql, entrada.p_Desde, entrada.p_Hasta, entrada.p_Tabla, entrada.p_Filtro);
		
    		}else {
    			cantidadRegistrosAMigrar = contarRegistrosHana(SQL_SELECT_COUNT_HIS_GENERICO_HANA, conn, sql, entrada.p_Desde, entrada.p_Hasta, entrada.p_Tabla, entrada.p_Filtro);
    		}
			
			
			Long cantidadRegistrosPostMigracion = -1L;
			
			if(cantidadRegistrosAMigrar > 0) {	    							    				    			    		
	    		
				if (RutaArchivoConfiguracion == null) {
					RutaArchivoConfiguracion = ConfiguracionSistema.getParametroConfiguracionString(conn,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");							
				}

				if (RutaArchivoConfiguracion == null) {
					throw new Exception("Inicializacion LimpiarDataHistorica: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion");							
				}
				InputStream input = null;
				String archivoProperties = "";
				try {
					archivoProperties = "historico.properties";
					input = new FileInputStream("historico.properties");
				} catch (FileNotFoundException fnfe) {
					archivoProperties = RutaArchivoConfiguracion + "historico.properties";
				}

				CompositeConfiguration prop = new CompositeConfiguration();
				prop.addConfiguration(new SystemConfiguration());
				prop.addConfiguration(new PropertiesConfiguration(archivoProperties));

				String dbURLSql = prop.getString("sqlserver.dbURL");
				String dbpropSql = prop.getString("sqlserver.dbprop");
				String dbdrvSql = prop.getString("sqlserver.dbdrv");
				String dbqrySql = prop.getString("sqlserver.dbquery");
				String userSql = prop.getString("sqlserver.dbuser");
				String passSql = prop.getString("sqlserver.dbpass");
				
				String dbURLHana = prop.getString("saphana.dbURL");
				String dbpropHana = prop.getString("saphana.dbprop");
				String dbdrvHana = prop.getString("saphana.dbdrv");
				String dbqryHana = prop.getString("saphana.dbquery");
				String userHana = prop.getString("saphana.dbuser");
				String passHana = prop.getString("saphana.dbpass");
				
				// Datasource
				BasicDataSource dsSqlServer = new BasicDataSource();
				dsSqlServer.setDriverClassName(dbdrvSql);
				dsSqlServer.setUsername(userSql);
				dsSqlServer.setPassword(passSql);
				dsSqlServer.setUrl(dbURLSql);
				dsSqlServer.setValidationQuery(dbqrySql);
				dsSqlServer.setConnectionProperties(dbpropSql);
				dsSqlServer.setDefaultAutoCommit(false);
				
				BasicDataSource dsHana = new BasicDataSource();
				dsHana.setDriverClassName(dbdrvHana);
				dsHana.setUsername(userHana);
				dsHana.setPassword(passHana);
				dsHana.setUrl(dbURLHana);
				dsHana.setValidationQuery(dbqryHana);
				dsHana.setConnectionProperties(dbpropHana);

				try (Connection conSQLServer = dsSqlServer.getConnection();) {
					switch (entrada.p_Tabla) {
					case "\"BSCL.Custodia.BD::HIS.Cartera\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla, entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_CARTERA, SQL_INSERT_REGISTROS_HIS_CARTERA, HisCartera.class, cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla, entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						break;
					case "\"BSCL.Custodia.BD::HIS.Subyacente\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla, entrada.p_Filtro,entrada.p_Desde, entrada.p_Hasta);			
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_SUBYACENTE, SQL_INSERT_REGISTROS_HIS_SUBYACENTE, HisSubyacente.class, cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla, entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);							
						break;
					case "\"BSCL.Custodia.BD::HIS.Garantia\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_GARANTIA,SQL_INSERT_REGISTROS_HIS_GARANTIA, HisGarantia.class, cantidadRegistrosAMigrar,entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);							
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						break;
					case "\"BSCL.Custodia.BD::HIS.CarteraNegativa\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_CARTERA_NEGATIVA,SQL_INSERT_REGISTROS_HIS_CARTERA_NEGATIVA, HisCarteraNegativa.class,cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						break;
					case "\"BSCL.Custodia.BD::HIS.FusionCartera\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);							
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_CARTERA_FUSION_CARTERA,SQL_INSERT_REGISTROS_HIS_CARTERA_FUSION_CARTERA, HisFusionCartera.class,cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						break;
					case "\"BSCL.Custodia.BD::HIS.FusionCarteraDetalle\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_FUSION_DETALLE_SQLSERVER, conSQLServer,entrada.p_Tabla, entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_CARTERA_FUSION_CARTERA_DETALLE,SQL_INSERT_REGISTROS_HIS_CARTERA_FUSION_CARTERA_DETALLE, HisFusionCarteraDetalle.class, cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);							
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_FUSION_DETALLE_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);								
						break;
					case "\"BSCL.Custodia.BD::HIS.Reserva\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);
						realizarMigracion(prop, SQL_SELECT_REGISTROS_HIS_RESERVA,SQL_INSERT_REGISTROS_HIS_RESERVA, HisReserva.class, cantidadRegistrosAMigrar,entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);								
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);								
						break;
					case "\"BSCL.Custodia.BD::CUS.PatrimonioCustodia\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);								
						realizarMigracion(prop, SQL_SELECT_REGISTROS_CUS_PATRIMONIO_CUSTODIA,SQL_INSERT_REGISTROS_CUS_PATRIMONIO_CUSTODIA, CusPatrimonioCustodia.class,cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);				
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);			
						break;
					case "\"BSCL.CuentaInversion.BD::CTA.PatrimonioCaja\"":
						eliminarRegistrosSQLServer(SQL_DELETE_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);								
						realizarMigracion(prop, SQL_SELECT_REGISTROS_CTA_PATRIMONIO_CAJA,SQL_INSERT_REGISTROS_CTA_PATRIMONIO_CAJA, CtaPatrimonioCaja.class,cantidadRegistrosAMigrar, entrada.p_Desde, entrada.p_Hasta, dsSqlServer, dsHana);						
						cantidadRegistrosPostMigracion = contarRegistrosSQLServer(SQL_SELECT_COUNT_HIS_GENERICO_SQLSERVER, conSQLServer, entrada.p_Tabla,entrada.p_Filtro, entrada.p_Desde, entrada.p_Hasta);					
						break;
					}

					// CONTROL DE DATA
					if (cantidadRegistrosAMigrar.equals(cantidadRegistrosPostMigracion)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,"Migracion de " + entrada.p_Tabla + " ejecutada correctamente entre " + entrada.p_Desde + " y " + entrada.p_Hasta + ".",new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));						
								
					} else {
						out.P_OUT_ERRORESCONTADOR = 1L;
					}

				} catch (Exception e) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR No se pudo realizar la migracion a SQL SERVER.", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));				
					logger.info(e);
					logger.info("***** Error  Proceso de Migracion *****");
					throw e;
				}
			} else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "Proceso de migracion no encontro data a migrar de " + entrada.p_Tabla + " entre " + entrada.p_Desde + " y " + entrada.p_Hasta + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));		
				logger.info("Proceso de migracion no encontro data a migrar de " + entrada.p_Tabla + ".");
			}
		return out;
	}
	

	public <T> void realizarMigracion(CompositeConfiguration prop, String selectHana, String insertSLQServer, Class<T> claseResultadoOptimus, Long cantidadRegistrosAMigrar, String desde, String hasta, BasicDataSource dsSqlServer, BasicDataSource dsHana) {
		try {
			List<InputObjetoProceso<T>> listadoPorcesos = new ArrayList<InputObjetoProceso<T>>();
			Long cantidadIntegrantesGrupo = prop.getLong("migrardatahis.amount");
			int threats = prop.getInt("migrardatahis.threats");
			Long i = 0L;			
			
			HashMap<String, Object> paramsIn = new HashMap<String, Object>();
			paramsIn.put("p_Desde", desde);
			paramsIn.put("p_Hasta", hasta);
			

			
			while(i < cantidadRegistrosAMigrar) {				
				InputObjetoProceso<T> input = new InputObjetoProceso<T>();
				
				
				
				input.paramsIn = paramsIn;
										
				String queryATrabajar = selectHana;
				queryATrabajar = queryATrabajar.replace(":p_Limit", "" + cantidadIntegrantesGrupo);
				queryATrabajar = queryATrabajar.replace(":p_Offset", "" + i);
				
				input.select = queryATrabajar;
				input.insert = insertSLQServer;
				//input.clase = claseResultadoOptimus;
				
				listadoPorcesos.add(input);
				i += cantidadIntegrantesGrupo;
			}
			
			llamadaMigracionParalela(listadoPorcesos, threats, claseResultadoOptimus, dsSqlServer, dsHana);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static class OutObj {
		public boolean funciono;
		public Long id;
		
		public OutObj(Long id, boolean funciono) {
			this.id = id;
			this.funciono = funciono;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InputObjetoProceso<T> {
		public Long id;
		public String select;
		public String insert;
		//public Class<T> clase;
		public HashMap<String, Object> paramsIn;
		public String dbURLSql;
		public String dbpropSql;
		public String dbdrvSql;
		public String dbqrySql;
		public String userSql;
		public String passSql;
		public String dbURLHana;
		public String dbpropHana;
		public String dbdrvHana;
		public String dbqryHana;
		public String userHana;
		public String passHana;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public <T> List<OutObj> llamadaMigracionParalela(List<InputObjetoProceso<T>> datos, int threats, Class <T> clase, BasicDataSource dsSqlServer, BasicDataSource dsHana) {
		logger.debug("Inicio llamada migrar data paralela");
		ExecutorService executor = Executors.newFixedThreadPool(threats);
		List<CompletableFuture<OutObj>> futures =
				datos.stream().map(t -> CompletableFuture.supplyAsync(() -> llamadaMigracionUnitaria(t, clase, dsSqlServer, dsHana), executor)).collect(Collectors.toList());
		
		CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	    
	    List<OutObj> result = allOf.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList())).join();
		executor.shutdown();
		logger.debug("Fin llamada migrar data paralela");
		return result;
	}
	
	public <T> OutObj llamadaMigracionUnitaria(InputObjetoProceso<T> input, Class <T> clase, BasicDataSource dsSqlServer, BasicDataSource dsHana) {
	
		boolean retorno = true;		
		List<T> resultadosSelectHana = new ArrayList<>();
		
		SQL sql = new SQL();
		
		try(Connection conSQLServer = dsSqlServer.getConnection(); Connection conHana = dsHana.getConnection();){
			logger.debug("Antes de select " + input.id);
			resultadosSelectHana = sql.query(conHana, input.select, input.paramsIn, clase);
			logger.debug("Despues de select " + input.id);
			if(!resultadosSelectHana.isEmpty()) {
				sql.batch(conSQLServer, input.insert, resultadosSelectHana, clase);
				conSQLServer.commit();
				logger.debug("Despues del batch " + input.id);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			retorno = false;
		}

		return new OutObj(input.id, retorno);
	}
	
	public Long contarRegistrosSQLServer(String queryConteo, Connection con, String tabla, String filtro, String desde, String hasta) throws Exception {
		
			String registrosComparativa = queryConteo.replace(":p_Tabla", tabla);
			registrosComparativa = registrosComparativa.replace(":p_Filtro", filtro);
			registrosComparativa = registrosComparativa.replace(":p_Hasta", "'" + hasta + "'");
			registrosComparativa = registrosComparativa.replace(":p_Desde", "'" + desde + "'");
			
			try(Statement stmtComparativa = con.createStatement(); ResultSet result2 = stmtComparativa.executeQuery(registrosComparativa);){			        
	        result2.next();
	        Long cantidadRegistrosPostMigracion = result2.getLong(1);	       
	        return cantidadRegistrosPostMigracion;
		}catch(Exception e){
			logger.info(e);
			throw e;
		}
        
	}
	
	public Long contarRegistrosHana(String queryConteo, Connection con, SQL sql, String desde, String hasta, String tabla, String filtro) throws Exception{
		try{
			queryConteo = queryConteo.replace(":p_Desde", "'" + desde + "'");
			queryConteo = queryConteo.replace(":p_Hasta", "'" + hasta + "'");
			queryConteo = queryConteo.replace(":p_Tabla", tabla);
			queryConteo = queryConteo.replace(":p_Filtro", filtro);
			
			Long cantidadRegistrosAMigrar = sql.countQuery(con, queryConteo, new HashMap<>());
			return cantidadRegistrosAMigrar;
		}catch(Exception e){
			logger.info(e);
			throw e;
		}
        
	}
	
	public void eliminarRegistrosSQLServer(String queryDelete,Connection con, String tabla, String filtro, String desde, String hasta) throws Exception{
		try(Statement stmtComparativa = con.createStatement();){
			String registrosAEliminar = queryDelete.replace(":p_Tabla", tabla);
			registrosAEliminar = registrosAEliminar.replace(":p_Filtro", filtro);
			registrosAEliminar = registrosAEliminar.replace(":p_Hasta", "'" + hasta + "'");
			registrosAEliminar = registrosAEliminar.replace(":p_Desde", "'" + desde + "'");
	        stmtComparativa.executeUpdate(registrosAEliminar);

		}catch(Exception e){
			logger.info(e);
			throw e;
		}
        
	}
}

