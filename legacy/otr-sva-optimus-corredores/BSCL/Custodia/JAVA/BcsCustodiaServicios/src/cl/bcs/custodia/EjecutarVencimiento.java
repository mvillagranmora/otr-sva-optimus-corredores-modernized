package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.custodia.AnularCarteraNegativa.CarteraNegativa_Entrada;
import cl.bcs.custodia.NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos;
import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.custodia.prt.VencerEmprestitos;
import cl.bcs.custodia.prt.VencerPrestamos;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MRF.VencerOperacion;
import cl.bcs.mesa.MRF.VencerOperacion.Operacion;
import cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaEntrada;
import cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaSalida;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.CarteraCompraObj_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_OUT;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.CarteraVentaObj_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_OUT;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecutarVencimiento implements JavaCall {

	private final Log log = new Log("CUS.EjecutarVencimiento");
	private static final Logger logger = LogManager.getLogger(EjecutarVencimiento.class);	
	private final ConstantesLimites _cl = new ConstantesLimites();
	private String fechaSistemaStr;
	private String tipoVencimiento = "";
	/*
	 * Queries que van a ser utilizadas por el servicio. Deben usar : para
	 * optimizar con los PreparedStatement
	 */
	private final String SQL_GarantiaVencerRF = loadResourceToString("cl/bcs/custodia/sql/GarantiaVencerRF.sql");
	private final String SQL_GarantiaVencerIF = loadResourceToString("cl/bcs/custodia/sql/GarantiaVencerIF.sql");
	private final String SQL_GarantiaVencerSIM = loadResourceToString("cl/bcs/custodia/sql/GarantiaVencerSIM.sql");
	private final String SQL_CustodiaVencerRF = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerRF.sql");
	private final String SQL_CustodiaVencerIF = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerIF.sql");
	
	private final String SQL_CustodiaVencerSIM = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerSIM.sql");
	private final String SQL_CustodiaVencerPAC = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerPAC.sql");
	private final String SQL_CustodiaVencerPRT = loadResourceToString("cl/bcs/custodia/sql/prt/CustodiaVencerPRT.sql");
	private final String SQL_CustodiaVencerPRE = loadResourceToString("cl/bcs/custodia/sql/prt/CustodiaVencerPRE.sql");
	
	private final String SQL_CustodiaVencerOPC = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerOPC.sql");
	private final String SQL_CustodiaVencerRE = loadResourceToString("cl/bcs/custodia/sql/CustodiaVencerRE.sql");
	private final String SQL_ValorMoneda = loadResourceToString("cl/bcs/custodia/sql/ValorMoneda.sql");
	
	// carttera negativa 
	private final String SQL_CustodiaNegativaVencerRF = loadResourceToString("cl/bcs/custodia/sql/CustodiaNegativaVencerRF.sql");
	private final String SQL_CustodiaNegativaVencerIF = loadResourceToString("cl/bcs/custodia/sql/CustodiaNegativaVencerIF.sql");
	
	private final String SQL_ComprobarEstadoVenPac = loadResourceToString("cl/bcs/custodia/sql/EjecutarVencimiento_SelectEstadoVenPac.sql");
	
	private final String SQL_FormaDePagoPorDefecto = loadResourceToString("cl/bcs/custodia/sql/EjecutarVencimiento_FormaDePagoPorDefecto.sql");
	
	/*
	 * Clases utilitarias que representan objetos IN/OUT Recordar que solo son
	 * permitidos tipos: - Long como Long - int como int - decimal como
	 * BigDecimal - char como string - varchar como string - date como string
	 * YYYY-MM-DD - datetime como string YYYY-MM-DD HH:MI:SS
	 */
	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_SimularOperacion;
		public String p_IdTipoVencimiento; // SIM,GAR,PAC,PRT,RF,IF
		public boolean p_EsPauta = false;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class CustodiaDisponible {
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal CustodiaDisponible;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClienteConsultarCustodia{
		public Long IdCliete;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_TICKETATENCIONCUSTODIASIMULACION;
		public Long P_OUT_TICKETATENCIONCUSTODIAEJECUCION;
		public String P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ProcesamientoCustodia> P_OUT_PROCESAMIENTO;
		public List<LogEjecucion> P_OUT_LOGEJECUCION;

		public Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class LogEjecucion {
		public Long TimeMillis;
		public String Mensaje;
		public String EsError;
		public String EsInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PortafolioOperaCuentaInversion {
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
		public String OperaContraCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrestamistaVencer {
		public Long FolioEmprestito;
	}

	public static class CustodiaVencer {
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioCartera;
		public String Nemotecnico;
		public String EsPagaderoEn;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoAPlazo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public String IdMonedaInstrumento;
		public String IdTipoProducto;
		public String IdTipoOperacion; // "VCDTIT"
		public String OperaContraCuentaInversion;
		public Long IdPersona;
		public Long IdCliente;
		public java.sql.Date FechaOperacion;
		public String IdConcepto;
		public String IdTipoCanalIngreso;
		public Long IdUsuario;
		public Long IdSucursal;
		public Long IdAgente;
		public String Glosa;
		public BigDecimal Cantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal MontoCompraMonedaInstrumento;
		//
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		public java.sql.Date FechaComprobante;
		public String IdMonedaSistema; // se usa como IdMonedaOperacion
		public String IdCondicionLiquidacion; // se esta usando T0
		public BigDecimal INGCantidad; // usado como Cantidad?
		public BigDecimal INGValorRescate;
		public BigDecimal ValorRescate;
		public BigDecimal INGTasaCompra; // es la tasa
		public BigDecimal PrecioCompra;
		public BigDecimal INGPrecioCompra;
		public String AccionOperacion; // lo usa la simultanea
		public Long IdSubyacente;
		public Long IdInstrumentoSubyacente;
		public Long FolioSubyacente;
		public String FechaLiquidacion;
		public Long FolioComprobante;
		public String IdTipoComprobante;
		public String CobraPrimaAlInicio;
		public String TablaAfecta;
		public String IdTipoComprobanteBusqueda;
		public Long FolioComprobanteBusqueda;
		public java.sql.Date FechaLiquidacionDate;
		public java.sql.Date FechaVencimientoPrestamo;
		public BigDecimal INGMontoPrima;     // Monto prima de Prestamos (se usa para enviar a facturar prestamos) 
		public BigDecimal INGMontoPremio;    // Monto premio de Emprestitos (se usa para enviar a facturar emprestitos)                    
		public String IdEmisor;
		//RF
		public String TieneCorteDeCupon;
		//IF
		public String NemoSVS;
		// MSH
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public String EsFungible;
		
		public String		PrestamoAsignadoTotal = "S";
		public String		IdTipoPrestamo;
		public BigDecimal 	CantidadAsignada; //lo usan prestamos, para saber cuantos titulos deben devolver
		public BigDecimal 	CantidadPorAsignar; //lo usan prestamos, para saber cuantos titulos faltar por asignar al prestamo
		public java.sql.Date FechaVencimientoInstrumento = Fecha.toDate("0001-01-01");
		public Long PlazoEnDiasOperacion;
		//PRT
		public String IdBolsa;
		public String IdEmisorSubyacente;
		public String IdBoveda;
		public BigDecimal INGPrecioInstrumento;
		public BigDecimal INGTasaInstrumento;
		public String esCarteraNegativa = "N";
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CustodiaNegativaVencer{
		public Long					IdCarteraNegativa; 
		public Long 				FolioCustodia;
		public String 				Nemotecnico;
		public String 				EsPagaderoEn;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public Long 				IdInstrumento;
		public Long 				IdPortafolio;
		public String 				IdMonedaInstrumento;
		public String 				IdTipoProducto;
		public String 				IdTipoOperacion;
		public Long 				IdOperacion;
		public String 				OperaContraCuentaInversion;
		public Long 				IdCliente; 
		public Long					IdPersona;
		public String				TieneCorteCupon;
		public BigDecimal 			PrecioOperacion;
		public BigDecimal 			MontoOperacionMonedaInstrumento;
		public java.sql.Date 		FechaComprobante;
		public String				IdCondicionDeLiquidacion;
		public BigDecimal 			Cantidad; 
		public BigDecimal 			ValorRescate;
		public BigDecimal 			TasaOperacion;
		public String				AccionOperacion;
		public String 				EsFungible; 
		public String 				IdTipoReajuste;
		public Date 				FechaVencimiento;
		public String				IdmonedaEmision;		 
		public Long 				IdEmpresa; 
		
		public String 				IdConcepto; 
 
		public String 				IdCondicionLiquidacion; 
		public java.sql.Date 		FechaLiquidacion; 

		public Long					FolioComprobante; 

		public String 				esCarteraNegtiva = "S";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	public static class ValorMoneda {
		public String IdParametroFinanciero;
		public BigDecimal Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiaVencer{
		
		public Long			IdGarantia;
		public BigDecimal	Cantidad;
		public BigDecimal	ValorRescate;
		public BigDecimal	MontoMonedaInstrumento;
		public BigDecimal	PrecioRetiro;
		public BigDecimal	TasaRetiro;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public Long			FolioCartera;
		public Long			IdCustodia;
		public Long			IdCartera;
		public Long			IdOperacion;
		public String		IdConcepto;
		public Long			IdInstrumento;
		public String		IdOrigenGarantia;
		public java.sql.Date FechaVencimientoInstrumento;
		public String 		EsFungible;
		public String IdTipoCanalIngreso;
		public String IdTipoOperacion;
		public String IdMonedaInstrumento;
		public BigDecimal INGTasaGarantia;
		//IF
		public String NemoSVS;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;	
		public java.sql.Date FechaOperacion;
		public String IdCondicionLiquidacion;
		public java.sql.Date FechaLiquidacion;
		public String IdTipoProducto;		
		public String CodigoIFRS;
		public String IdTipoComprobante;
		public String IdBolsa;
		public String Nemotecnico;		
		public String IdMonedaEmision;
		public String IdTipoReajusteInstrumento;
		public String IdEmisor;		
		public String IdBoveda;
		public BigDecimal  CorteMinimo;
		public BigDecimal PrecioCompra;					 
		public BigDecimal TasaCompra;					
		public BigDecimal MontoCompra;					 
		public BigDecimal MontoCompraMonedaInstrumento; 
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal MontoGarantiaMonedaInstrumento = BigDecimal.ZERO;
		public BigDecimal INGPrecioMercadoActual = BigDecimal.ZERO;
		//RF
		public String TieneCorteDeCupon;
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CustodiaReserva {
		public Long IdOperacion;
		public Long IdReserva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class FormaDePagoDefecto {
		public String IdMedioDePago;
		public Long   IdCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long   IdBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EstadoEjecucionVenPac{
		public Long IdEmpresa;
		public String FechaSistema;
		public String CodigoProceso;
		public String Descripcion;
		public String Estado;
		public String Mensaje;
	}

	public static class ProcesamientoCustodia {
		public Long IdCartera;
		public Long FolioCartera;
		public String Nemotecnico;
		public String EsPagaderoEn;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public String IdMonedaInstrumento;
		public String IdTipoProducto;
		public String IdTipoOperacion; // "VCDTIT"
		public String OperaContraCuentaInversion;
		public Long IdPersona;
		public Long IdCliente;
		//
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		public BigDecimal INGMontoAPlazo;
		public java.sql.Date FechaComprobante;
		public String IdMonedaSistema; // se usa como IdMonedaOperacion
		public String IdCondicionLiquidacion; // se esta usando T0
		public BigDecimal INGCantidad; // usado como Cantidad?
		public BigDecimal INGValorRescate;
		public BigDecimal ValorRescate;
		public BigDecimal INGTasaCompra; // es la tasa
		public BigDecimal PrecioCompra;
		public BigDecimal INGPrecioCompra;
		public String AccionOperacion; // lo usa la simultanea
		public Long IdSubyacente;
		public Long FolioSubyacente;
		public Long PlazoEnDiasOperacion; 
		public String FechaLiquidacion;
		public Long IdOperacion;
		public String CobraPrimaAlInicio;
		public String IdTipoComprobante;
		public Long FolioComprobante;		

		// Facturacion
		public String SimulacionFacTieneAdvertencias;
		public ArrayList<Detalle> SimulacionFacAdvertencias;
		public String SimulacionFacTieneErrores;
		public ArrayList<Detalle> SimulacionFacErrores;
		public String FacTieneAdvertencias;
		public ArrayList<Detalle> FacAdvertencias;
		public String FacTieneErrores;
		public ArrayList<Detalle> FacErrores;
		// custodia
		public String SimulacionGmcTieneAdvertencias;
		public ArrayList<Detalle> SimulacionGmcAdvertencias;
		public String SimulacionGmcTieneErrores;
		public ArrayList<Detalle> SimulacionGmcErrores;
		public String GmcTieneAdvertencias;
		public ArrayList<Detalle> GmcAdvertencias;
		public String GmcTieneErrores;
		public ArrayList<Detalle> GmcErrores;
		// cuenta inversion
		public String SimulacionCcaTieneAdvertencias;
		public ArrayList<Detalle> SimulacionCcaAdvertencias;
		public String SimulacionCcaTieneErrores;
		public ArrayList<Detalle> SimulacionCcaErrores;
		public String CcaTieneAdvertencias;
		public ArrayList<Detalle> CcaAdvertencias;
		public String CcaTieneErrores;
		public ArrayList<Detalle> CcaErrores;		
		// tesoreria
		public String SimulacionCieTieneAdvertencias;
		public ArrayList<Detalle> SimulacionCieAdvertencias;
		public String SimulacionCieTieneErrores;
		public ArrayList<Detalle> SimulacionCieErrores;
		public String CieTieneAdvertencias;
		public ArrayList<Detalle> CieAdvertencias;
		public String CieTieneErrores;
		public ArrayList<Detalle> CieErrores;
		// Cambio Estado Operacion
		public String CestTieneErrores;
		public ArrayList<Detalle> CestErrores;
		public String SimulacionCestTieneErrores;
		public ArrayList<Detalle> SimulacionCestErrores;

		public static class Detalle {
			public Long Id;
			public String Codigo;
			public String Descripcion;
			public Long IdGrupoOperacion;
			public String RequiereAutorizacion;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}

		// se definen valores default en el constructor
		public ProcesamientoCustodia() {
			this.SimulacionGmcTieneAdvertencias = "N";
			this.SimulacionGmcTieneErrores = "N";
			this.GmcTieneAdvertencias = "N";
			this.GmcTieneErrores = "N";
			this.SimulacionCcaTieneAdvertencias = "N";
			this.SimulacionCcaTieneErrores = "N";
			this.CcaTieneAdvertencias = "N";
			this.CcaTieneErrores = "N";
			this.SimulacionCieTieneAdvertencias = "N";
			this.SimulacionCieTieneErrores = "N";
			this.CestTieneErrores = "N";
			this.SimulacionCestTieneErrores = "N";
			this.CieTieneAdvertencias = "N";
			this.CieTieneErrores = "N";
			this.SimulacionFacAdvertencias = new ArrayList<Detalle>();
			this.SimulacionFacErrores = new ArrayList<Detalle>();
			this.FacAdvertencias = new ArrayList<Detalle>();
			this.FacErrores = new ArrayList<Detalle>();
			this.SimulacionGmcAdvertencias = new ArrayList<Detalle>();
			this.SimulacionGmcErrores = new ArrayList<Detalle>();
			this.GmcAdvertencias = new ArrayList<Detalle>();
			this.GmcErrores = new ArrayList<Detalle>();
			this.SimulacionCcaAdvertencias = new ArrayList<Detalle>();
			this.SimulacionCcaErrores = new ArrayList<Detalle>();
			this.CcaAdvertencias = new ArrayList<Detalle>();
			this.CcaErrores = new ArrayList<Detalle>();
			this.SimulacionCieAdvertencias = new ArrayList<Detalle>();
			this.SimulacionCieErrores = new ArrayList<Detalle>();
			this.CieAdvertencias = new ArrayList<Detalle>();
			this.CieErrores = new ArrayList<Detalle>();
			this.SimulacionCestErrores = new ArrayList<Detalle>();
			this.CestErrores = new ArrayList<Detalle>();
			this.SimulacionCestErrores = new ArrayList<Detalle>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}

	/*******************
	 * metodo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		logger.info("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("*** Fin de la ejecucion ***");
		return dataout;
	}

	/**********************
	 * logica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		logger.info("INICIO::EjecutarVencimiento");
		SQL sql = new SQL();
		Salida out = new Salida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		out.P_OUT_TICKETATENCIONCUSTODIASIMULACION = new Long(0);
		out.P_OUT_TICKETATENCIONCUSTODIAEJECUCION = new Long(0);
		
		if ("RF".equals(entrada.p_IdTipoVencimiento) || "IF".equals(entrada.p_IdTipoVencimiento)) {
			HashMap<String, Object> params = new HashMap<String, Object>(2);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("Fecha", entrada.p_Fecha);
			List<EstadoEjecucionVenPac> estadoVenPac = sql.query(con, SQL_ComprobarEstadoVenPac, params,
					EstadoEjecucionVenPac.class);
			logger.info("ESTADO VEN PAC -> " + estadoVenPac.size());
			if (estadoVenPac.size() > 0 && "ERR".equals(estadoVenPac.get(0).Estado)) {
				out.P_OUT_ERRORESCONTADOR += 1;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, MensajeSalida.MENSAJE_ERROR,
						String.format("VENCIMIENTO DE PACTOS TERMINO CON ERROR. PROCESO DE VENCIMIENTO %s NO EJECUTADO",
								entrada.p_IdTipoVencimiento)));
				return out;
			}
		}
			tipoVencimiento = entrada.p_IdTipoVencimiento;
			logger.info("Inicia ejecucion de vencimiento con datos de entrada: " + entrada);
			
			fechaSistemaStr = entrada.p_Fecha;
			
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String retiraGarantiaIFRF = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "CUS.RetiraGarantiaIFRFAlVencer");
			String certCargoAbonoVencimientosDAP = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa,"CUSTODIA","CUS.CertCargoAbonoVencimientosDAP");
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			boolean simulaTodosProcesos = true;
			String SQL_CustodiaVencer = obtenerQueryVencer(entrada.p_IdTipoVencimiento);
			String SQL_GarantiaVencer = obtenerQueryGarantias(entrada.p_IdTipoVencimiento);
			String SQL_CustodiaNegativaVencer = obtenerQueryVencerNegativa(entrada.p_IdTipoVencimiento);

			List<CustodiaVencer> listCustodiaVencer = new ArrayList<CustodiaVencer>();
			List<CustodiaNegativaVencer> listCustodiaNegativaVencer = new ArrayList<CustodiaNegativaVencer>();
			List<ValorMoneda> listValorMoneda = new ArrayList<ValorMoneda>();
			List<ProcesamientoCustodia> procVC = new ArrayList<ProcesamientoCustodia>();
			List<CustodiaReserva> listCustodiaReserva = new ArrayList<CustodiaReserva>();
			List<GarantiaVencer> listGarantiaVencer = new ArrayList<GarantiaVencer>();
			List<PrestamistaVencer> listPrestamistas = new ArrayList<PrestamistaVencer>();
			List<GarantiaVencer> listGarantiaVencerConTransito = new ArrayList<GarantiaVencer>();
			
			out.P_OUT_PROCESAMIENTO = procVC;
			out.P_OUT_LOGEJECUCION = new ArrayList<LogEjecucion>();

			CambiarEstadoOperacion cEstOpe = new CambiarEstadoOperacion();
			logEjecucion(out.P_OUT_LOGEJECUCION, "Inicio del procesamiento, obteniendo registros a vencer", "N", "N");
			// ----------------------------------
			// ----- Obtencion de los datos -----
			// ----------------------------------
			//Long timeini = System.currentTimeMillis();
			HashMap<String, Object> parmsCustodiaVencer = new HashMap<String, Object>();
			HashMap<String, Object> parmsGarantiaVencer = new HashMap<String, Object>();
			
			
			parmsCustodiaVencer.put("IdEmpresa", entrada.p_IdEmpresa);
			parmsCustodiaVencer.put("Fecha", entrada.p_Fecha);
			
			// condicion if
			listCustodiaVencer = sql.query(con, SQL_CustodiaVencer, parmsCustodiaVencer, CustodiaVencer.class);
			
			//Saca Carteras Negativas solo si son de IF o RF 
			if(entrada.p_IdTipoVencimiento.equalsIgnoreCase("IF")|| entrada.p_IdTipoVencimiento.equalsIgnoreCase("RF") ) {
				listCustodiaNegativaVencer = sql.query(con, SQL_CustodiaNegativaVencer, parmsCustodiaVencer, CustodiaNegativaVencer.class);
			}
			
			if(entrada.p_IdTipoVencimiento.equalsIgnoreCase("IF")) {
				HashMap<String, Object> parmsValorMoneda = new HashMap<String, Object>();
				parmsValorMoneda.put("Fecha",  entrada.p_Fecha);
				listValorMoneda = sql.query(con, SQL_ValorMoneda, parmsValorMoneda, ValorMoneda.class);
			}
			
			
			
			parmsGarantiaVencer.put("IdEmpresa", entrada.p_IdEmpresa);
			parmsGarantiaVencer.put("Fecha", entrada.p_Fecha);
			if(SQL_GarantiaVencer != null){
				//logger.info(SQL_GarantiaVencer);
				logger.info(" entrada.p_IdEmpresa= " + entrada.p_IdEmpresa);
				logger.info(" entrada.p_Fecha= " + entrada.p_Fecha);
				listGarantiaVencer = sql.query(con, SQL_GarantiaVencer, parmsGarantiaVencer, GarantiaVencer.class);
			}
			
			if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("PRT")){ //para prestamos, tambien debo ver si hay prestamistas que debo vencer, pese a que su prestamo aun no venza.
				HashMap<String, Object> parmsPrestamistasVencer = new HashMap<String, Object>();
				parmsPrestamistasVencer.put("p_IdEmpresa", entrada.p_IdEmpresa);
				parmsPrestamistasVencer.put("p_Fecha", entrada.p_Fecha);
				listPrestamistas = sql.query(con, SQL_CustodiaVencerPRE, parmsPrestamistasVencer, PrestamistaVencer.class); 
			}
			
			
			if (listCustodiaVencer.size() == 0 && listGarantiaVencer.size() == 0 && listPrestamistas.size() == 0 && listCustodiaNegativaVencer.size()== 0) {
				logger.info("Sin instrumentos para vencer");
				out.P_OUT_DESCRIPCION = "NO HAY REGISTROS PARA VENCER";
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, MensajeSalida.MENSAJE_INFORMACION, out.P_OUT_DESCRIPCION));
				out.P_OUT_ERRORESCONTADOR = 0L;
				return out;
			}
			
			logger.info("TipoVencimiento=" + tipoVencimiento + " Registros iniciales para simular="
					+ listCustodiaVencer.size());


			for (CustodiaVencer item : listCustodiaVencer) {
				item.FechaLiquidacion = entrada.p_Fecha;
				procVC.add(carteraAProcesamiento(item));
			}

			// --------------------------------
			// SIMULACION
			// --------------------------------
			if ("S".equals(entrada.p_SimularOperacion)) {
			
				logEjecucion(out.P_OUT_LOGEJECUCION, "Inicia Simulacion", "N", "N");
				if (entrada.p_IdTipoVencimiento.equals("PRT")) {
					//################## SE ELIMINA LA SIMULACION VENCIMIENTO DE PRESTAMOS ##################
				}
				
				// -------------------------------------------------------------------------
				// -- SIMULACION DE REPORTES(peru) Y SIMULTANEAS (Chile)
				// -------------------------------------------------------------------------
				
				else if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("RE") || entrada.p_IdTipoVencimiento.equalsIgnoreCase("SIM")) 
				{	
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA] Inicio Simulacion GenerarMovimientoCustodia");
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Custodia", "N", "N");
					
					List<CustodiaVencer> datosCarteraVenta = new ArrayList<>();
					List<CustodiaVencer> datosCarteraCompra = new ArrayList<>();
					
					for (CustodiaVencer car : listCustodiaVencer) 
					{
						//car.FechaLiquidacion = entrada.p_Fecha;
						if (car.IdTipoOperacion.startsWith("COM")) {
							datosCarteraCompra.add(car);
						}
						if (car.IdTipoOperacion.startsWith("VEN")) {
							datosCarteraVenta.add(car);
						}
					}
					
	
					//----- Genero Ticket de atencion para custodia ----//
					List<ObjMovimiento> entLogCus = new ArrayList<>();
					Long TicketAtencion = LogCustodia.logCustodia(con, entLogCus);
					
					
					//----- Liquido las operaciones de venta (REporte PEru, Simultaneas chile) -------//
					if (datosCarteraVenta.size() > 0)
					{
						LiquidarCustodiaOrdenVenta liqOVenta = new LiquidarCustodiaOrdenVenta();
						LiquidarCustodiaOrdenVenta_IN liqOVentaIn = new LiquidarCustodiaOrdenVenta_IN();
						LiquidarCustodiaOrdenVenta_OUT liqOVentaOut = new LiquidarCustodiaOrdenVenta_OUT();
						{
							liqOVentaIn.p_IdPais = entrada.p_IdPais;
							liqOVentaIn.p_IdEmpresa = entrada.p_IdEmpresa;
							liqOVentaIn.p_IdUsuario = entrada.p_IdUsuario;
							liqOVentaIn.p_IdModulo = entrada.p_IdModulo;
							liqOVentaIn.p_UsaReglasNegocio = "S";						
							liqOVentaIn.p_SimularOperacion = "S"; 
							liqOVentaIn.p_TicketAtencion = TicketAtencion;
							liqOVentaIn.p_OrigenLiquidacion = "VENCIMIENTO";
							for (CustodiaVencer ven : datosCarteraVenta) 
							{
									CarteraVentaObj_IN obj = new CarteraVentaObj_IN();
									obj.AccionOperacion = "VENTA";// ant.AccionOperacion;
									obj.IdCartera = ven.IdCartera;
									obj.FolioCartera = ven.FolioCartera;
									obj.IdTipoOperacion = ven.IdTipoOperacion;
									if("PE".equals(localizacionSistema))
										obj.IdOperacion = ven.IdOperacion;
									else
										obj.IdOperacion = -1L;
									
									obj.IdConcepto = ven.IdConcepto;
									obj.Cantidad = ven.INGCantidad;
									obj.Monto = ven.INGMontoATasaCompraActual;
									obj.Tasa = ven.INGTasaCompra;
									obj.IdCondicionLiquidacion = "T0";// ant.IdCondicionLiquidacion;
									obj.Fecha = entrada.p_Fecha;
									obj.IdInstrumento = ven.IdInstrumento;							
									obj.IdCliente = ven.IdCliente;
									obj.IdPortafolio = ven.IdPortafolio;
									obj.IdEmisor = ven.IdEmisor;
	
									liqOVentaIn.p_DatosCartera.add(obj);
							}
						}
						liqOVentaOut = liqOVenta.logica(con, liqOVentaIn);
						out = this.actualizarMensajesErroresYContador(liqOVentaOut.P_OUT_MENSAJES, out);
						if (out.P_OUT_ERRORESCONTADOR > 0) 
						{
							return out;
						}					
	
					} // FIN - if (datosCarteraVenta.size() > 0)
					
					
					//-------- Liquido las operaciones de Compra (REporte PEru, y Simultaneas chile) y vence garantias---//				
					if (datosCarteraCompra.size() > 0)
					{
						LiquidarCustodiaOrdenCompra liqOCompra = new LiquidarCustodiaOrdenCompra();
						LiquidarCustodiaOrdenCompra_IN liqOCompraIn = new LiquidarCustodiaOrdenCompra_IN();
						LiquidarCustodiaOrdenCompra_OUT liqOCompraOut = new LiquidarCustodiaOrdenCompra_OUT();
						{
							liqOCompraIn.p_IdPais = entrada.p_IdPais;
							liqOCompraIn.p_IdEmpresa = entrada.p_IdEmpresa;
							liqOCompraIn.p_IdUsuario = entrada.p_IdUsuario;
							liqOCompraIn.p_IdModulo = entrada.p_IdModulo;
							liqOCompraIn.p_UsaReglasNegocio = "S";
							liqOCompraIn.p_SimularOperacion = "S";
							liqOCompraIn.p_TicketAtencion = TicketAtencion;
							liqOCompraIn.p_OrigenLiquidacion = "VENCIMIENTO";
	
							for (CustodiaVencer ven : datosCarteraCompra) {
								CarteraCompraObj_IN obj = new CarteraCompraObj_IN();
								obj.AccionOperacion = "COMPRA";// ant.AccionOperacion;
								obj.IdCartera = ven.IdCartera;
								obj.FolioCartera = ven.FolioCartera;
								obj.IdTipoOperacion = ven.IdTipoOperacion;
								if("PE".equals(localizacionSistema))
									obj.IdOperacion = ven.IdOperacion;
								else
									obj.IdOperacion = -1L;						
								obj.IdConcepto = ven.IdConcepto;
								obj.Cantidad = ven.INGCantidad;
								obj.Monto = ven.INGMontoATasaCompraActual;
								obj.Tasa = ven.INGTasaCompra;
								obj.IdCondicionLiquidacion = "T0";
								obj.Fecha = entrada.p_Fecha;
								
								obj.IdInstrumento = ven.IdInstrumento;
								obj.IdCliente = ven.IdCliente;
								obj.IdPortafolio = ven.IdPortafolio;						
								obj.IdEmisor = ven.IdEmisor;
	
								liqOCompraIn.p_DatosCartera.add(obj);
							}
						}
						liqOCompraOut = liqOCompra.logica(con, liqOCompraIn);
						out = this.actualizarMensajesErroresYContador(liqOCompraOut.P_OUT_MENSAJES, out);
						if (out.P_OUT_ERRORESCONTADOR > 0) {
							return out;
						}
					}  // FIN -  if (datosCarteraCompra.size() > 0)
						
	
					
					//------ Crear CargoAbono Cuenta Inversion -------//
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Inicio Simulacion CrearCargoAbono");
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Cuenta de Inversion", "N", "N");
					
					CrearCargoAbono ctaInv = new CrearCargoAbono();
					CrearCargoAbonoEntrada CtaInvIn = prepararDatosCuentaInversion(con, "S", listCustodiaVencer,listValorMoneda ,entrada, fechaSistemaStr,"N");
					if(CtaInvIn.p_CargoAbonoAGenerar.size() > 0){
						CrearCargoAbonoSalida CtaInvOut = ctaInv.logica(con, CtaInvIn);
					logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Fin Simulacion CrearCargoAbono");
						
						out = this.actualizarMensajesErroresYContador(CtaInvOut.P_OUT_MENSAJES, out);
						if (out.P_OUT_ERRORESCONTADOR > 0) {
							return out;
						}
					}
					
					//----- Crear IngresoEgreso Tesoreria -------//
				logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Inicio Simulacion CrearIngresoEgreso");
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Tesoreria", "N", "N");
					cl.bcs.tesoreria.CrearIngresoEgreso tes = new cl.bcs.tesoreria.CrearIngresoEgreso();
					CrearIngresoEgreso_Entrada entTes = prepararDatosTesoreria(con, "S", listCustodiaVencer, entrada, fechaSistemaStr, listValorMoneda);
					CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
					
					if (entTes.p_IngresoEgresoGenerar.size() > 0)
					{
						tesOut = tes.logica(con, entTes);
						
					logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Fin Simulacion CrearIngresoEgreso");
						
						out = this.actualizarMensajesErroresYContador(tesOut.P_OUT_MENSAJES, out);
						if (out.P_OUT_ERRORESCONTADOR > 0) {
							return out;
						}
					}
	
				} 
				else if (entrada.p_IdTipoVencimiento.equals("PAC")) {
					if(listCustodiaVencer.size() > 0){
					logger.info("TipoVencimiento=" + tipoVencimiento
								+ " [PACTOS] Inicio Simulacion VencimientoPactos");
						VencerOperacion venPac = new VencerOperacion();
						VencerOperacionMesaEntrada ingmcSimular = preparaDatosMesa( entrada.p_SimularOperacion, listCustodiaVencer, entrada);
					logger.info("TipoVencimiento=" + tipoVencimiento
								+ " [CUSTODIA] GenerarMovimientoCustodia => Cantidad Registros=" + ingmcSimular.p_Operacion.size());
						VencerOperacionMesaSalida outMesa = venPac.logica(con, ingmcSimular);
						out.P_OUT_MENSAJES.addAll(outMesa.P_OUT_MENSAJES);
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
							return out;
						}
					}
				}
				else if (!entrada.p_IdTipoVencimiento.equals("OPC")) { // p_IdTipoVencimiento:
																	   // RF,IF
				parmsCustodiaVencer.clear();
				parmsCustodiaVencer.put("IdEmpresa", entrada.p_IdEmpresa);
				parmsCustodiaVencer.put("Fecha", entrada.p_Fecha);
				listCustodiaVencer = sql.query(con, SQL_CustodiaVencer, parmsCustodiaVencer, CustodiaVencer.class);
					// -------------------------------------
					// ----- GenerarMovimientoGarantia -----
					// -------------------------------------
					if(listGarantiaVencer.size() > 0){
						RetiroCustodia retiroCustodia = new RetiroCustodia();
					logger.info("TipoVencimiento=" + tipoVencimiento	+ " [GARANTIA] Inicio Simulacion RetirarGarantia");
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Garantia", "N", "N");
					
					
					if(!"IF".equals(entrada.p_IdTipoVencimiento) || !"RF".equals(entrada.p_IdTipoVencimiento)) {
						RetiroCustodia.RetiroCustodiaEntrada garantiaSimular = preparaDatosGarantia("S", listGarantiaVencer, entrada, fechaSistemaStr);
						
						logger.info("TipoVencimiento=" + tipoVencimiento+ " [GARANTIA] GenerarMovimientoCustodia => Cantidad Registros=" + garantiaSimular.p_DatosEntrada.size());
						
						RetiroCustodia.RetiroCustodiaSalida outgarantiaSimular = retiroCustodia.logica(con, garantiaSimular);
						// Tratamiento de la salida
						for (MensajeSalida errores : outgarantiaSimular.P_OUT_MENSAJES) {
							if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
								out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[GARANTIA] " + errores.descripcion));
							}
							
						}
						
						procesarDetalleCustodia("S", procVC, outgarantiaSimular);
						logger.info("TipoVencimiento=" + tipoVencimiento + " [GARANTIA] Fin Simulacion GenerarMovimientoCustodia");
						if (outgarantiaSimular.P_OUT_DATOSSALIDA.size() == 0 && simulaTodosProcesos == false) return out;
					}else {
						//preparo funcion que ingrese movimientos en transito 
						IngresoCustodiaSalida outCrearTransito = this.crearMovimientosEnTransito(con,listGarantiaVencer,entrada);
						List<MensajeSalida> mensajesMovTransito = outCrearTransito.P_OUT_MENSAJES;
						//reterio garantias segun parametro
						List<ObjDatosSalida> movTransitosGenerados = new ArrayList<>();
						movTransitosGenerados = outCrearTransito.P_OUT_DATOSSALIDA;
						HashMap<Long, Object> hashCalculosParidadGarantias = new HashMap<>();
						if("S".equalsIgnoreCase(retiraGarantiaIFRF) && movTransitosGenerados.size() > 0) {
							HashMap<Long, Object> hashMovimientosEnTransito = Utils.List2Hash(movTransitosGenerados, "IdOperacion");
							listGarantiaVencerConTransito = listGarantiaVencer.stream()
									.filter( g -> hashMovimientosEnTransito.containsKey(g.IdGarantia))
									.collect(Collectors.toList());
							
							hashCalculosParidadGarantias = calcularParidadRetiroGarantia(entrada,con,listGarantiaVencerConTransito,monedaSistema);
							RetirarGarantiaSalida garantiasRetiradas = retirarGarantiasIFRF(entrada,con,listGarantiaVencerConTransito,hashCalculosParidadGarantias);
							for (MensajeSalida errores : garantiasRetiradas.P_OUT_MENSAJES) {
								if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
									out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
								}
								
							}
							
						}
						//fin retiro garantias segun parametro
						
						//valida parametro
						
						for (MensajeSalida errores : mensajesMovTransito) {
							if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
								out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
							}
							
						};
					}
				
					
				}
					
					if(listCustodiaVencer.size() > 0){
						// -------------------------------------
						// ----- GenerarMovimientoCustodia -----
						// -------------------------------------
						RetiroCustodia retiroCustodia = new RetiroCustodia();
					logger.info("TipoVencimiento=" + tipoVencimiento
								+ " [CUSTODIA] Inicio Simulacion GenerarMovimientoCustodia");
						logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Custodia", "N", "N");
						RetiroCustodia.RetiroCustodiaEntrada ingmcSimular = preparaDatosCustodia(entrada.p_SimularOperacion, listCustodiaVencer, entrada, fechaSistemaStr);
					logger.info("TipoVencimiento=" + tipoVencimiento
								+ " [CUSTODIA] GenerarMovimientoCustodia => Cantidad Registros=" + ingmcSimular.p_DatosEntrada.size());
						RetiroCustodia.RetiroCustodiaSalida outgmcSimular = retiroCustodia.logica(con, ingmcSimular);
						// Tratamiento de la salida
						for (MensajeSalida errores : outgmcSimular.P_OUT_MENSAJES) {
							if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
								out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
							}
							
						};
						
						procesarDetalleCustodia(entrada.p_SimularOperacion, procVC, outgmcSimular);
					logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA] Fin Simulacion GenerarMovimientoCustodia");
						if (outgmcSimular.P_OUT_DATOSSALIDA.size() == 0 && simulaTodosProcesos == false) return out;
						
						// ---------------------------
						// ----- Vencimiento IF -----
						// ---------------------------
						if(entrada != null && "IF".equals(entrada.p_IdTipoVencimiento)) {
						
							IngresoCustodiaSalida outCrearTransitoIF = this.crearMovimientosEnTransitoIF(con,listCustodiaVencer,entrada);
							
							if(outCrearTransitoIF != null) {
								
								if(outCrearTransitoIF.P_OUT_MENSAJES != null && !outCrearTransitoIF.P_OUT_MENSAJES.isEmpty()) {
									List<MensajeSalida> mensajesMovTransitoIF = outCrearTransitoIF.P_OUT_MENSAJES;
									for (MensajeSalida errores : mensajesMovTransitoIF) {
										if(errores != null && MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
											out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
										}
									};
								}
								
								List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_TransitosIF = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();

								if(outCrearTransitoIF.P_OUT_DATOSSALIDA != null && !outCrearTransitoIF.P_OUT_DATOSSALIDA.isEmpty()) {
									List<IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida> salidaDatosCrearTransitoIF = outCrearTransitoIF.P_OUT_DATOSSALIDA;						
									for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida Mov : salidaDatosCrearTransitoIF) {
										if(Mov != null) {
											GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
											MOV.IdMovimientoCarteraEnTransito = Mov.IdCustodia;
											MOV.EsCertificacion = "S";
											MOV.IdUsuario = entrada.p_IdUsuario;
											p_TransitosIF.add(MOV);
										}
									}
								}
								
								if(p_TransitosIF != null) {
									//certifica los movimientos
									GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_TransitosIF);
								}	
							}
						}

						// ---------------------------
						// ----- CrearCargoAbono -----
						// ---------------------------
						
					logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Inicio Simulacion CrearCargoAbono");
						logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Cuenta de Inversion", "N", "N");
					CrearCargoAbono.CrearCargoAbonoEntrada inccaSimular = prepararDatosCuentaInversion(con, entrada.p_SimularOperacion,listCustodiaVencer, listValorMoneda ,entrada, fechaSistemaStr, "N");
					
						if (inccaSimular.p_CargoAbonoAGenerar.size() > 0) {
						logger.info("TipoVencimiento=" + tipoVencimiento+ " [CUENTA INVERSION] CrearCargoAbono => Cantidad Registros="+ inccaSimular.p_CargoAbonoAGenerar.size());
								
							CrearCargoAbono CCC = new CrearCargoAbono();
							CrearCargoAbono.CrearCargoAbonoSalida outccaSimular = CCC.logica(con, inccaSimular);
							// tratamiento de la salida
							for (MensajeSalida Errores : outccaSimular.P_OUT_MENSAJES) {
								if (Errores.tipo.equalsIgnoreCase("ERROR")) {
									out.P_OUT_MENSAJES.add(new MensajeSalida(Errores.id, MensajeSalida.MENSAJE_ERROR,
											Errores.codigo, "[CUENTA INVERSION] " + Errores.descripcion));
								}
							}
							
							procesarDetalleCuentaInversion(entrada.p_SimularOperacion, procVC, outccaSimular);
						logger.info("TipoVencimiento=" + tipoVencimiento
									+ " [CUENTA INVERSION] Fin Simulacion CrearCargoAbono");
							if (inccaSimular.p_CargoAbonoAGenerar.size() == listCustodiaVencer.size()
									&& outccaSimular.P_OUT_CARGOABONOGENERADO.size() == 0 && simulaTodosProcesos == false)
								return out;
						} else {
						logger.info("TipoVencimiento=" + tipoVencimiento + " Sin datos para crear cargo/abono");
						}
	
						
						// ------------------------------
						// ----- CrearIngresoEgreso -----
						// ------------------------------
					logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Inicio Simulacion CrearIngresoEgreso");
						logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Tesoreria", "N", "N");
						
						cl.bcs.tesoreria.CrearIngresoEgreso servicioCrearIngresoEgreso = new cl.bcs.tesoreria.CrearIngresoEgreso();
						CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = prepararDatosTesoreria(con, entrada.p_SimularOperacion, listCustodiaVencer, entrada, fechaSistemaStr, listValorMoneda);	
						CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = new CrearIngresoEgreso_Salida();
	
						if (entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size() > 0)
						{
						logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] CrearIngresoEgreso => Cantidad Registros=" + entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size());					
							salidaCrearIngresoEgreso = servicioCrearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);					
							out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
							
						logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Fin Simulacion CrearIngresoEgreso");
							
							procesarDetalleTesoreria(entrada.p_SimularOperacion, procVC, salidaCrearIngresoEgreso);
						}
						else 
						{
						logger.info("TipoVencimiento=" + tipoVencimiento + " Sin datos para crear ingreso/egreso");
						}				
						
						// ------------------------------
						// ----- Cambiar Estado Operacion -----
						// ------------------------------
						if ( !entrada.p_IdTipoVencimiento.equals("IF") && !entrada.p_IdTipoVencimiento.equals("RF") ) { 
						logger.info("TipoVencimiento=" + tipoVencimiento + " [MESA] Inicio Simulacion CambiarEstadoOperacion");
							logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Mesa", "N", "N");
							CambiarEstadoOperacion.CambiarEstadoOperacionEntrada inSimOpeMesa = prepararDatosEstadoMesa("S",
									listCustodiaVencer, listCustodiaReserva, entrada);
							if (inSimOpeMesa.p_DataCambiarEstado.size() > 0) {
							logger.info("TipoVencimiento=" + tipoVencimiento
										+ " [MESA] CambiarEstadoOperacion => Cantidad Registros="
										+ inSimOpeMesa.p_DataCambiarEstado.size());
								CambiarEstadoOperacion.CambiarEstadoOperacionSalida outCambioSimEstado = cEstOpe.logica(con,
										inSimOpeMesa);
								procesarDetalleCambiarEstadoOperacion("S", procVC, outCambioSimEstado);
							logger.info("TipoVencimiento=" + tipoVencimiento + " [MESA] Fin Simulacion CambiarEstadoOperacion");
								if (outCambioSimEstado.P_OUT_ERRORESCONTADOR > 0 && simulaTodosProcesos == false)
									return out;
							} else {
							logger.info("TipoVencimiento=" + tipoVencimiento + " Sin datos para cambio de Estado Operacion");
							}
						}
					}
				} // p_IdTipoVencimiento: RF,IF,SIM

				con.rollback(); // D:
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_ADVERTENCIASCONTADOR = 0L;
				logEjecucion(out.P_OUT_LOGEJECUCION, "Fin de la simulacion", "N", "N");
				log.write("TipoVencimiento=" + tipoVencimiento + " Finaliza la simulacion, se realiza rollback para reset globales temporales y evitar errores no manejados");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Fin de la ejecucion (Vencimiento configurado para simular)", "N","N");
				return out;
			}

			
			
			// --------------------------------
			// EJECUCION
			// --------------------------------

			// --------------------------------------
			// ----- Filtro simulados con exito -----
			// --------------------------------------
			
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_ADVERTENCIASCONTADOR = 0L;
			logEjecucion(out.P_OUT_LOGEJECUCION, "Inicio de la ejecucion de los procesos", "N", "N");
			logEjecucion(out.P_OUT_LOGEJECUCION, "Seleccionando datos simulada correctamente", "N", "N");
			// Enviar a vencer solo los datos con simulacion exitosa.
			List<CustodiaVencer> listCustodiaVencerSimulado = new ArrayList<CustodiaVencer>();
			if (!entrada.p_IdTipoVencimiento.equals("OPC")) {
				for (ProcesamientoCustodia item : procVC) {
					if ((!item.OperaContraCuentaInversion.equals("S") && !item.SimulacionGmcTieneErrores.equals("S")
							&& !item.SimulacionCcaTieneErrores.equals("S")
							&& !item.SimulacionCieTieneErrores.equals("S")
							&& !item.SimulacionCestTieneErrores.equals("S"))
							|| (item.OperaContraCuentaInversion.equals("S")
									&& !item.SimulacionGmcTieneErrores.equals("S")
									&& !item.SimulacionCcaTieneErrores.equals("S")
									&& !item.SimulacionCestTieneErrores.equals("S"))) {
						listCustodiaVencerSimulado.add(procesamientoACartera(item));
					}
				}
				logger.info("TipoVencimiento=" + tipoVencimiento + ": cantidad registros simulados con exito: "
						+ listCustodiaVencerSimulado.size());
			}

			//-------------------------------------------------------------------------
			//--- EJECUCION PRESTAMOS/EMPRESTITOS (prestamo llama a vencer emprestitos)
			//-------------------------------------------------------------------------
			if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("PRT")) {
					List<CustodiaVencer> listCustodiaVencerReal = new ArrayList<CustodiaVencer>();					
					if(listCustodiaVencer.size() > 0){
						//Separar Renovaciones de Vencimientos (Criterio CustodiaDisponible >= CantidadPrestamo)
						for (CustodiaVencer reg : listCustodiaVencer){							
							reg.PrestamoAsignadoTotal = reg.CantidadPorAsignar.compareTo(BigDecimal.ZERO) == 0 ? "S": "N";
							listCustodiaVencerReal.add(reg);
						}
					}			

					if (listCustodiaVencerReal.size() > 0){
						VencerPrestamos objVencimientoPrt = new VencerPrestamos();
						VencerPrestamos.Entrada paramsVencPrt = new VencerPrestamos.Entrada();
						VencerPrestamos.Salida  resulVencPrt = new VencerPrestamos.Salida();
						
						paramsVencPrt.p_IdEmpresa = entrada.p_IdEmpresa;
						paramsVencPrt.p_IdModulo = entrada.p_IdModulo;
						paramsVencPrt.p_IdUsuario = entrada.p_IdUsuario;
						paramsVencPrt.p_IdPais = entrada.p_IdPais;
						paramsVencPrt.p_Fecha = entrada.p_Fecha;
						paramsVencPrt.p_lstPrestamos = new ArrayList<VencerPrestamos.ObjRegistroPrestamo>();
						paramsVencPrt.p_EsPauta= entrada.p_EsPauta;
						
						for (CustodiaVencer reg : listCustodiaVencerReal){
							VencerPrestamos.ObjRegistroPrestamo newReg = new VencerPrestamos.ObjRegistroPrestamo();
							Utils.copyPojo(reg, newReg ) ;
							paramsVencPrt.p_lstPrestamos.add(newReg);
						}
						
						resulVencPrt = objVencimientoPrt.logica(con, paramsVencPrt);
						out.P_OUT_ERRORESCONTADOR += resulVencPrt.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(resulVencPrt.P_OUT_MENSAJES);
						out.P_OUT_DESCRIPCION = resulVencPrt.P_OUT_DESCRIPCION;
					
					
						//-- vuelvo a consultar x emprestitos x vencer, ya que al vencer prestamos es probable haya vencido tambien emprestitos que vencian hoy  
						HashMap<String, Object> parmsPrestamistasVencer = new HashMap<String, Object>();
						parmsPrestamistasVencer.put("p_IdEmpresa", entrada.p_IdEmpresa);
						parmsPrestamistasVencer.put("p_Fecha", entrada.p_Fecha);
						
						listPrestamistas = new ArrayList<PrestamistaVencer>();
						listPrestamistas = sql.query(con, SQL_CustodiaVencerPRE, parmsPrestamistasVencer, PrestamistaVencer.class);
						if (listPrestamistas.size() > 0){
							VencerEmprestitos objVencEmprestitos = new VencerEmprestitos();
							VencerEmprestitos.Entrada paramsVencEmp = new VencerEmprestitos.Entrada();
							VencerEmprestitos.Salida  resulVencEmp = new VencerEmprestitos.Salida();
							paramsVencEmp.p_IdEmpresa = entrada.p_IdEmpresa;
							paramsVencEmp.p_IdModulo = entrada.p_IdModulo;
							paramsVencEmp.p_IdPais = entrada.p_IdPais;
							paramsVencEmp.p_IdUsuario = entrada.p_IdUsuario;
							paramsVencEmp.p_Fecha = entrada.p_Fecha;
				
							resulVencEmp = objVencEmprestitos.logica(con, paramsVencEmp);
							out.P_OUT_ERRORESCONTADOR += resulVencEmp.P_OUT_ERRORESCONTADOR;
							out.P_OUT_MENSAJES.addAll(resulVencEmp.P_OUT_MENSAJES);
							out.P_OUT_DESCRIPCION = resulVencEmp.P_OUT_DESCRIPCION;
						}
					}
			//-------------------------------
			//-- FIN VENCIMIENTO PRESTAMOS --
			//-------------------------------
			} else if (entrada.p_IdTipoVencimiento.equals("OPC")) {
				// Gat-Blac Vencimiento de opciones
				Long TicketAtencion = LogCustodia.logEjecutarVencimiento(con, new RetiroCustodia.ObjDatosEntrada());
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa = entrada.p_IdEmpresa;
				paramInSP.p_SimularOperacion = "N";
				paramInSP.p_TicketAtencion = TicketAtencion;
				for (CustodiaVencer item : listCustodiaVencer) {
					RetiroCustodia.ObjDatosEntrada ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia = item.IdCartera;
					ObjEntrada.TablaAfectada = "CARTERA";
					ObjEntrada.IdTipoComprobanteBusqueda = item.IdTipoComprobanteBusqueda;
					ObjEntrada.FolioComprobanteBusqueda = item.FolioComprobanteBusqueda;
					ObjEntrada.AccionOperacion = "VENCIMIENTO";
					ObjEntrada.IdCliente = item.IdCliente;
					ObjEntrada.IdPortafolio = item.IdPortafolio;
					ObjEntrada.FechaOperacion = item.FechaOperacion;
					ObjEntrada.IdTipoOperacion = item.IdTipoOperacion;
					ObjEntrada.IdConcepto = item.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
					ObjEntrada.FechaLiquidacion = item.FechaLiquidacionDate;
					ObjEntrada.IdTipoCanalIngreso = item.IdTipoCanalIngreso;
					ObjEntrada.IdTipoComprobante = item.IdTipoComprobante;
					ObjEntrada.FolioComprobante = item.FolioComprobante;
					ObjEntrada.IdUsuario = item.IdUsuario;
					ObjEntrada.IdSucursal = item.IdSucursal;
					ObjEntrada.IdAgente = item.IdAgente;
					ObjEntrada.Glosa = item.Glosa;
					ObjEntrada.IdInstrumento = item.IdInstrumento;
					//ObjEntrada.EsPagaderoEn = item.EsPagaderoEn;
					ObjEntrada.Cantidad = item.Cantidad;
					ObjEntrada.CorteMinimo = item.CorteMinimo;
					ObjEntrada.ValorRescate = item.ValorRescate;
					ObjEntrada.PrecioCompra = item.PrecioCompra;
					ObjEntrada.TasaCompra = item.INGTasaCompra;
					ObjEntrada.MontoCompraMonedaInstrumento = item.MontoCompraMonedaInstrumento;
					p_RetiroCartera.add(ObjEntrada);
				}
				paramInSP.p_DatosEntrada = p_RetiroCartera;
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSP);

				if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)) {
					out.P_OUT_ERRORESCONTADOR = resultRetiros.P_OUT_ERRORESCONTADOR;
					return out;
				}
				;
				out.P_OUT_MENSAJES.add(new MensajeSalida(TicketAtencion, MensajeSalida.MENSAJE_INFORMACION, "",
						"[CUSTODIA] Vencimiento realizado"));
			} 
			
			// ------------------------------------------------------------------------
			// -- EJECUCION REPORTES(Peru) Y SIMUALTANEAS(Chile)
			// ------------------------------------------------------------------------
			else if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("RE") || entrada.p_IdTipoVencimiento.equalsIgnoreCase("SIM")) 
			{
				logger.info("TipoVencimiento=" + tipoVencimiento
						+ " [CUSTODIA] Inicio Simulacion GenerarMovimientoCustodia");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Custodia", "N", "N");
				List<CustodiaVencer> datosCarteraVenta = new ArrayList<>();
				List<CustodiaVencer> datosCarteraCompra = new ArrayList<>();
				for (CustodiaVencer car : listCustodiaVencer) {
					//car.FechaLiquidacion = entrada.p_Fecha;
					if (car.IdTipoOperacion.startsWith("COM")) {
						datosCarteraCompra.add(car);
					}
					if (car.IdTipoOperacion.startsWith("VEN")) {
						datosCarteraVenta.add(car);
					}
				}

				/****** Genero Ticket de atencion para custodia **************/
				List<ObjMovimiento> entLogCus = new ArrayList<>();
				Long TicketAtencion = LogCustodia.logCustodia(con, entLogCus);
				
				/*** Liquido las operaciones de venta (REporte PEru, y Simultaneas chile) ***/
				if (datosCarteraVenta.size() > 0)
				{
					LiquidarCustodiaOrdenVenta liqOVenta = new LiquidarCustodiaOrdenVenta();
					LiquidarCustodiaOrdenVenta_IN liqOVentaIn = new LiquidarCustodiaOrdenVenta_IN();
					LiquidarCustodiaOrdenVenta_OUT liqOVentaOut = new LiquidarCustodiaOrdenVenta_OUT();
					{
						liqOVentaIn.p_IdPais = entrada.p_IdPais;
						liqOVentaIn.p_IdEmpresa = entrada.p_IdEmpresa;
						liqOVentaIn.p_IdUsuario = entrada.p_IdUsuario;
						liqOVentaIn.p_IdModulo = entrada.p_IdModulo;
						liqOVentaIn.p_UsaReglasNegocio = "S";
						liqOVentaIn.p_SimularOperacion = "N";
						liqOVentaIn.p_TicketAtencion = TicketAtencion;
						liqOVentaIn.p_OrigenLiquidacion = "VENCIMIENTO";
						for (CustodiaVencer ven : datosCarteraVenta) 
						{
								CarteraVentaObj_IN obj = new CarteraVentaObj_IN();
								obj.AccionOperacion = "VENTA";// ant.AccionOperacion;
								obj.IdCartera = ven.IdCartera;
								obj.FolioCartera = ven.FolioCartera;
								obj.IdTipoOperacion = ven.IdTipoOperacion;
								if("PE".equals(localizacionSistema))
									obj.IdOperacion = ven.IdOperacion;
								else
									obj.IdOperacion = -1L;
								
								if(entrada.p_IdTipoVencimiento.equals("SIM")) {
									obj.IdConcepto = ven.IdTipoOperacion.equals("VENSIM") ? "VCTSIV" : "VCTSIC";
								}else {
									obj.IdConcepto = ven.IdConcepto;
								}	
								obj.Cantidad = ven.INGCantidad;
								obj.Monto = ven.INGMontoATasaCompraActual;
								obj.Tasa = ven.INGTasaCompra;
								obj.IdCondicionLiquidacion = "T0";// ant.IdCondicionLiquidacion;
								obj.Fecha = entrada.p_Fecha;
								obj.IdInstrumento = ven.IdInstrumento;
								obj.IdCliente = ven.IdCliente;
								obj.IdPortafolio = ven.IdPortafolio;
								obj.IdEmisor = ven.IdEmisor;
								
								liqOVentaIn.p_DatosCartera.add(obj);
						}
					}
					liqOVentaOut = liqOVenta.logica(con, liqOVentaIn);
					out = this.actualizarMensajesErroresYContador(liqOVentaOut.P_OUT_MENSAJES, out);
					if (out.P_OUT_ERRORESCONTADOR > 0) 
					{
						return out;
					}
				} // FIN - if (datosCarteraVenta.size() > 0)

				
				/*** Liquido las operaciones de Compra (REporte PEru, y Simultaneas chile) ***/
				if (datosCarteraCompra.size() > 0)
				{
					LiquidarCustodiaOrdenCompra liqOCompra = new LiquidarCustodiaOrdenCompra();
					LiquidarCustodiaOrdenCompra_IN liqOCompraIn = new LiquidarCustodiaOrdenCompra_IN();
					LiquidarCustodiaOrdenCompra_OUT liqOCompraOut = new LiquidarCustodiaOrdenCompra_OUT();
					{
						liqOCompraIn.p_IdPais = entrada.p_IdPais;
						liqOCompraIn.p_IdEmpresa = entrada.p_IdEmpresa;
						liqOCompraIn.p_IdUsuario = entrada.p_IdUsuario;
						liqOCompraIn.p_IdModulo = entrada.p_IdModulo;
						liqOCompraIn.p_UsaReglasNegocio = "S";
						liqOCompraIn.p_SimularOperacion = "N";
						liqOCompraIn.p_TicketAtencion = TicketAtencion;
						liqOCompraIn.p_OrigenLiquidacion = "VENCIMIENTO";

						for (CustodiaVencer ven : datosCarteraCompra) {
							CarteraCompraObj_IN obj = new CarteraCompraObj_IN();
							obj.AccionOperacion = "COMPRA";// ant.AccionOperacion;
							obj.IdCartera = ven.IdCartera;
							obj.FolioCartera = ven.FolioCartera;
							obj.IdTipoOperacion = ven.IdTipoOperacion;
							if("PE".equals(localizacionSistema))
								obj.IdOperacion = ven.IdOperacion;
							else
								obj.IdOperacion = -1L;
							
							if(entrada.p_IdTipoVencimiento.equals("SIM")) {
								obj.IdConcepto = ven.IdTipoOperacion.equals("COMSIM") ? "VCTSIC" : "VCTSIV";
							}else {
								obj.IdConcepto = ven.IdConcepto;
							}
							
							obj.Cantidad = ven.INGCantidad;
							obj.Monto = ven.INGMontoATasaCompraActual;
							obj.Tasa = ven.INGTasaCompra;
							obj.IdCondicionLiquidacion = "T0";
							obj.Fecha = entrada.p_Fecha;

							obj.IdInstrumento = ven.IdInstrumento;
							obj.IdCliente = ven.IdCliente;
							obj.IdPortafolio = ven.IdPortafolio;
							obj.IdEmisor = ven.IdEmisor;
							
							liqOCompraIn.p_DatosCartera.add(obj);
						}
					}
					liqOCompraOut = liqOCompra.logica(con, liqOCompraIn);
					out = this.actualizarMensajesErroresYContador(liqOCompraOut.P_OUT_MENSAJES, out);
					if (out.P_OUT_ERRORESCONTADOR > 0) 
					{
						return out;
					}
				} // FIN - if (datosCarteraCompra.size() > 0)

				
				
				
				/*** Crear CargoAbono CuentaInversion ******/
				logger.info(
						"TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Inicio Simulacion CrearCargoAbono");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Cuenta de Inversion", "N", "N");
				CrearCargoAbono ctaInv = new CrearCargoAbono();
				CrearCargoAbonoEntrada CtaInvIn = prepararDatosCuentaInversion(con, entrada.p_SimularOperacion, listCustodiaVencer, listValorMoneda ,entrada,fechaSistemaStr,"N");
				
				if(CtaInvIn.p_CargoAbonoAGenerar.size() > 0){
					CrearCargoAbonoSalida CtaInvOut = ctaInv.logica(con, CtaInvIn);
					logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Fin Simulacion CrearCargoAbono");
					out = this.actualizarMensajesErroresYContador(CtaInvOut.P_OUT_MENSAJES, out);
					if (out.P_OUT_ERRORESCONTADOR > 0) 
					{
						return out;
					}
				}
				
				/*** Crear IngresoEgreso Tesoreria ******/
				logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Inicio Simulacion CrearIngresoEgreso");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Tesoreria", "N", "N");
				cl.bcs.tesoreria.CrearIngresoEgreso tes = new cl.bcs.tesoreria.CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entTes = prepararDatosTesoreria(con, entrada.p_SimularOperacion, listCustodiaVencer, entrada,fechaSistemaStr, listValorMoneda);
				CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
				if (entTes.p_IngresoEgresoGenerar.size() > 0){
					tesOut = tes.logica(con, entTes);
					logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Fin Simulacion CrearIngresoEgreso");
					out = this.actualizarMensajesErroresYContador(tesOut.P_OUT_MENSAJES, out);
					if (out.P_OUT_ERRORESCONTADOR > 0) 
					{
						return out;
					}
				}
				
				if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
			    	parametrosMensaje.put("Cantidad", String.valueOf(datosCarteraCompra.size() + datosCarteraVenta.size()));
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CARTERAS_VENCIDAS",parametrosMensaje,con,entrada.p_IdEmpresa));
				}

			}
			else if (entrada.p_IdTipoVencimiento.equals("PAC")) {
				if(listCustodiaVencer.size() > 0){
					logger.info("TipoVencimiento=" + tipoVencimiento
							+ " [PACTOS] Inicio Simulacion VencimientoPactos");
					VencerOperacion venPac = new VencerOperacion();
					VencerOperacionMesaEntrada ingmcSimular = preparaDatosMesa("S",listCustodiaVencer, entrada);//, fechaSistemaStr);
					logger.info("TipoVencimiento=" + tipoVencimiento
							+ " [CUSTODIA] GenerarMovimientoCustodia => Cantidad Registros=" + ingmcSimular.p_Operacion.size());
					VencerOperacionMesaSalida outMesa = venPac.logica(con, ingmcSimular);
					out.P_OUT_MENSAJES.clear();
					out.P_OUT_MENSAJES.addAll(outMesa.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
						return out;
					}
				}
			}
			else { // p_IdTipoVencimiento: RF,IF
				
				// -------------------------------------
				// ----- GenerarMovimientoGarantia -----
				// -------------------------------------
							
				
				if(listGarantiaVencer.size() > 0){
					
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					logger.info("TipoVencimiento=" + tipoVencimiento+ " [GARANTIA] Inicio Simulacion RetirarGarantia");
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca simulacion Garantia", "N", "N");
					//garantias IF y RF no deben retirarse 
					
					//IF y RF
					
					IngresoCustodiaSalida outCrearTransito = this.crearMovimientosEnTransito(con,listGarantiaVencer,entrada);
					List<MensajeSalida> mensajesMovTransito = outCrearTransito.P_OUT_MENSAJES;
					List<ObjDatosSalida> movTransitosGenerados = new ArrayList<>();
					movTransitosGenerados = outCrearTransito.P_OUT_DATOSSALIDA;
					HashMap<Long, Object> hashCalculosParidadGarantias = new HashMap<>();
					if("S".equalsIgnoreCase(retiraGarantiaIFRF) && movTransitosGenerados.size() > 0) {
						HashMap<Long, Object> hashMovimientosEnTransito = Utils.List2Hash(movTransitosGenerados, "IdOperacion");
						listGarantiaVencerConTransito = listGarantiaVencer.stream()
								.filter( g -> hashMovimientosEnTransito.containsKey(g.IdGarantia))
								.collect(Collectors.toList());
						
						hashCalculosParidadGarantias = calcularParidadRetiroGarantia(entrada,con,listGarantiaVencerConTransito,monedaSistema);
						RetirarGarantiaSalida garantiasRetiradas = retirarGarantiasIFRF(entrada,con,listGarantiaVencerConTransito,hashCalculosParidadGarantias);
						for (MensajeSalida errores : garantiasRetiradas.P_OUT_MENSAJES) {
							if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
								out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
							}
							
						}
						
					}
					
					for (MensajeSalida errores : mensajesMovTransito) {
						if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
							out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
						}
						
					};
						
				}
				
				parmsCustodiaVencer.clear();
				parmsCustodiaVencer.put("IdEmpresa", entrada.p_IdEmpresa);
				parmsCustodiaVencer.put("Fecha", entrada.p_Fecha);
				listCustodiaVencer = sql.query(con, SQL_CustodiaVencer, parmsCustodiaVencer, CustodiaVencer.class);
				
				// -------------------------------------
				// ----- GenerarMovimientoCustodia -----
				// -------------------------------------
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA] Inicio Ejecucion GenerarMovimientoCustodia");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Custodia", "N", "N");
				RetiroCustodia.RetiroCustodiaEntrada ingmc = preparaDatosCustodia(entrada.p_SimularOperacion,listCustodiaVencer, entrada, fechaSistemaStr);
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA] GenerarMovimientoCustodia => Cantidad Registros=" + ingmc.p_DatosEntrada.size());
				
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA NEGATIVA] Inicio Ejecucion GenerarMovimientoCustodia");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Custodia", "N", "N");
				
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA NEGATIVA] GenerarMovimientoCustodia => Cantidad Registros=" );
				RetiroCustodia.RetiroCustodiaSalida outgmc = retiroCustodia.logica(con, ingmc);
				// Tratamiento de la salida
				procesarDetalleCustodia(entrada.p_SimularOperacion, procVC, outgmc);
				
				if (outgmc.P_OUT_DATOSSALIDA.size() == 0 && listGarantiaVencer.size() == 0 && listCustodiaNegativaVencer.size() == 0) //agregar al if valodar las garantias
					return out;
				// Se acota el universo a las simulaciones sin error
				List<CustodiaVencer> listaCarteraSimuladaOK = new ArrayList<CustodiaVencer>();
				
				AnularCarteraNegativa anularCarteraNegativa = new AnularCarteraNegativa();
				AnularCarteraNegativa.AnularCarteraNegativa_Entrada entradaCarteraNegativa = new AnularCarteraNegativa.AnularCarteraNegativa_Entrada();
				
				entradaCarteraNegativa.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCarteraNegativa.p_IdUsuario = entrada.p_IdUsuario;
				entradaCarteraNegativa.p_IdModulo = entrada.p_IdModulo;
				entradaCarteraNegativa.p_simularOperacion = entrada.p_SimularOperacion;
	
				List<CarteraNegativa_Entrada> p_CarterasNegativas = new ArrayList<CarteraNegativa_Entrada>();
				for (CustodiaNegativaVencer item : listCustodiaNegativaVencer) {
					CarteraNegativa_Entrada in = new CarteraNegativa_Entrada();
					in.IdCarteraNegativa = item.IdCarteraNegativa;
					p_CarterasNegativas.add(in);
				}
				
				entradaCarteraNegativa.p_CarterasNegativas = p_CarterasNegativas;

				AnularCarteraNegativa.AnularCarteraNegativa_Salida salidaCarteraNegativa = anularCarteraNegativa.logica(con, entradaCarteraNegativa);
				
				List<String> ListaCarterasVencidas = new ArrayList<String>();
				List<String> ListaCarterasVencidasError = new ArrayList<String>();
				
				for (CustodiaVencer procC : listCustodiaVencer) {
					for (RetiroCustodiaSalida.ObjDatosSalida itemC : outgmc.P_OUT_DATOSSALIDA) {
						if (procC.IdCartera.equals(itemC.IdGenerarMovimientoCustodia) && "N".equals(itemC.RegistroConProblemas)) {							
							listaCarteraSimuladaOK.add(procC);
							ListaCarterasVencidas.add("Se vencio cartera Folio "+procC.FolioCartera+" Nemotecnico "+procC.Nemotecnico);							
						};
						if (procC.IdCartera.equals(itemC.IdGenerarMovimientoCustodia) && "S".equals(itemC.RegistroConProblemas)) {							
							ListaCarterasVencidas.add("Error cartera Folio "+procC.FolioCartera+" Nemotecnico "+procC.Nemotecnico);							
						};
					}
				}
				
				if(ListaCarterasVencidas.size()>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, "", ListaCarterasVencidas.size()+" carteras vencidas"));
				}
				
				if(ListaCarterasVencidasError.size()>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "", ListaCarterasVencidasError.size()+" carteras con problemas"));
				}
				
				for (MensajeSalida errores : salidaCarteraNegativa.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA NEGATIVA] " + errores.descripcion));
					}
				};
				
				for (MensajeSalida errores : outgmc.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
					}
				};
				
				for (CustodiaNegativaVencer item : listCustodiaNegativaVencer) {
					CustodiaVencer paso = new CustodiaVencer();
					paso.IdCartera = item.IdCarteraNegativa;
					paso.IdPortafolio = item.IdPortafolio;
					paso.IdClasificacionTipoInstrumento = item.IdClasificacionTipoInstrumento;
					paso.IdMonedaInstrumento = item.IdMonedaInstrumento;
					paso.IdTipoReajuste = item.IdTipoReajuste;				
					paso.IdCondicionLiquidacion = item.IdCondicionLiquidacion; 
					paso.IdTipoInstrumento = item.IdTipoInstrumento;
					paso.FechaLiquidacionDate =item.FechaLiquidacion;
					paso.INGValorRescate = item.ValorRescate;
					paso.INGMontoCompraActualMonedaInstrumento = item.MontoOperacionMonedaInstrumento;
					
					paso.IdTipoProducto = item.IdTipoProducto;
					paso.IdOperacion = item.IdOperacion;

					paso.FolioCartera = item.FolioComprobante; 
					paso.IdInstrumento = item.IdInstrumento;
					paso.Nemotecnico = item.Nemotecnico;
					paso.IdTipoOperacion =item.IdTipoOperacion;
					paso.IdConcepto = item.IdConcepto;
					paso.IdTipoOperacion = item.IdTipoOperacion;
					paso.esCarteraNegativa = item.esCarteraNegtiva;

					listaCarteraSimuladaOK.add(paso);
					
				}
				
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUSTODIA] Fin Ejecucion GenerarMovimientoCustodia");
				
				// ---------------------------
				// ----- Vencimiento IF -----
				// ---------------------------
				if(entrada != null && "IF".equals(entrada.p_IdTipoVencimiento)) {
				
					IngresoCustodiaSalida outCrearTransitoIF = this.crearMovimientosEnTransitoIF(con,listCustodiaVencer,entrada);
					
					if(outCrearTransitoIF != null) {
						
						if(outCrearTransitoIF.P_OUT_MENSAJES != null && !outCrearTransitoIF.P_OUT_MENSAJES.isEmpty()) {
							List<MensajeSalida> mensajesMovTransitoIF = outCrearTransitoIF.P_OUT_MENSAJES;
							for (MensajeSalida errores : mensajesMovTransitoIF) {
								if(errores != null && MensajeSalida.MENSAJE_ERROR.equals(errores.tipo)){
									out.P_OUT_MENSAJES.add(new MensajeSalida(errores.id, MensajeSalida.MENSAJE_ERROR, errores.codigo,"[CUSTODIA] " + errores.descripcion));
								}
							};
						}
						
						List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_TransitosIF = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();

						if(outCrearTransitoIF.P_OUT_DATOSSALIDA != null && !outCrearTransitoIF.P_OUT_DATOSSALIDA.isEmpty()) {
							List<IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida> salidaDatosCrearTransitoIF = outCrearTransitoIF.P_OUT_DATOSSALIDA;						
							for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida Mov : salidaDatosCrearTransitoIF) {
								if(Mov != null) {
									GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
									MOV.IdMovimientoCarteraEnTransito = Mov.IdCustodia;
									MOV.EsCertificacion = "S";
									MOV.IdUsuario = entrada.p_IdUsuario;
									p_TransitosIF.add(MOV);
								}
							}
						}
						
						if(p_TransitosIF != null) {
							//certifica los movimientos
							GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_TransitosIF);
						}	
					}
				}
				
				// ---------------------------
				// ----- CrearCargoAbono -----
				// ---------------------------
				
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Inicio Ejecucion CrearCargoAbono");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Cuenta de Inversion", "N", "N");
				// preparar los datos
				CrearCargoAbono.CrearCargoAbonoEntrada incca = prepararDatosCuentaInversion(con, entrada.p_SimularOperacion,listaCarteraSimuladaOK, listValorMoneda ,entrada, fechaSistemaStr, certCargoAbonoVencimientosDAP);
				if(listGarantiaVencer.size() > 0 && "N".equals(retiraGarantiaIFRF)){
					
					CrearCargoAbono.CrearCargoAbonoEntrada cargoAbonoGarantia = prepararDatosCuentaInversionGarantia(con, entrada.p_SimularOperacion,listGarantiaVencer, entrada, fechaSistemaStr);
					incca.p_CargoAbonoAGenerar.addAll(cargoAbonoGarantia.p_CargoAbonoAGenerar);
				}
				
				if (incca.p_CargoAbonoAGenerar.size() > 0) {
					logger.info("TipoVencimiento=" + tipoVencimiento	+ " [CUENTA INVERSION] CrearCargoAbono => Cantidad Registros="+ incca.p_CargoAbonoAGenerar.size());
					CrearCargoAbono CCC = new CrearCargoAbono();
					CrearCargoAbono.CrearCargoAbonoSalida outcca = CCC.logica(con, incca);

					// tratamiento de la salida
					procesarDetalleCuentaInversion(entrada.p_SimularOperacion, procVC, outcca);
					if (outcca.P_OUT_CARGOABONOGENERADO.size() == 0)
						return out;
				} else {
					logger.info("Sin datos para crear cargo/abono");
				}
				logger.info("TipoVencimiento=" + tipoVencimiento + " [CUENTA INVERSION] Fin Ejecucion CrearCargoAbono");
				
				
				
				// ------------------------------
				// ----- CrearIngresoEgreso -----
				// ------------------------------
				
				logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Inicio Ejecucion CrearIngresoEgreso");
				
				cl.bcs.tesoreria.CrearIngresoEgreso servicioCrearIngresoEgreso = new cl.bcs.tesoreria.CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = prepararDatosTesoreria(con, entrada.p_SimularOperacion, listaCarteraSimuladaOK, entrada, fechaSistemaStr, listValorMoneda);
				CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = new CrearIngresoEgreso_Salida();

				if (entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size() > 0) 
				{
					logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] CrearIngresoEgreso => Cantidad Registros=" + entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size());
					
					salidaCrearIngresoEgreso = servicioCrearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);					
					procesarDetalleTesoreria(entrada.p_SimularOperacion, procVC, salidaCrearIngresoEgreso);
				} 
				else 
				{
					logger.info("Sin datos para crear ingreso/egreso");
				}
				logger.info("TipoVencimiento=" + tipoVencimiento + " [TESORERIA] Fin Ejecucion CrearCargoAbono");

				// ------------------------------
				// ----- Cambiar Estado Operacion -----
				// ------------------------------
				logger.info("TipoVencimiento=" + tipoVencimiento + " [MESA] Inicio CambiarEstadoOperacion");
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Mesa", "N", "N");
				CambiarEstadoOperacion.CambiarEstadoOperacionEntrada inOpeMesa = prepararDatosEstadoMesa("N",
						listCustodiaVencer, listCustodiaReserva, entrada);
				if (inOpeMesa.p_DataCambiarEstado.size() > 0) {
					logger.info("TipoVencimiento=" + tipoVencimiento
							+ " [MESA] CambiarEstadoOperacion => Cantidad Registros="
							+ inOpeMesa.p_DataCambiarEstado.size());
					CambiarEstadoOperacion.CambiarEstadoOperacionSalida outCambioEstado = cEstOpe.logica(con,
							inOpeMesa);
					logger.info("TipoVencimiento=" + tipoVencimiento + " [MESA] Fin CambiarEstadoOperacion");
					if (outCambioEstado.P_OUT_ERRORESCONTADOR > 0)
						return out;
				} else {
					logger.info("TipoVencimiento=" + tipoVencimiento + " Sin datos para cambio de Estado Operacion");
				}
			}
			// --------------------------------
			// PROCESAR LA SALIDA
			// --------------------------------


		
		// notificar vencimientos
		if (out.P_OUT_ERRORESCONTADOR == 0) {
			try {
				NotificarVencimientoInstrumentos notificar = new NotificarVencimientoInstrumentos();
				EntradaNotificarVencimientoInstrumentos entradaNotificar = new NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos();
				List<Long> idsCarteras = out.P_OUT_PROCESAMIENTO.stream().map( vencimiento -> vencimiento.IdCartera ).collect(Collectors.toList());
				entradaNotificar.p_IdEmpresa= entrada.p_IdEmpresa;
				entradaNotificar.p_IdModulo= entrada.p_IdModulo;
				entradaNotificar.p_IdUsuario = entrada.p_IdUsuario;
				entradaNotificar.vencimientos = idsCarteras; 
				if (entradaNotificar.vencimientos != null && !entradaNotificar.vencimientos.isEmpty()) {
					notificar.logica(con, entradaNotificar);
				}
			} catch (Exception e) {
				logger.info(e);
			}
		}
		logger.info("FIN::EjecutarVencimiento");
		return out;
	}
	
	public HashMap<Long, Object> calcularParidadRetiroGarantia(Entrada entrada,Connection con, List<GarantiaVencer> listGarantiaVencerConTransito,String monedaSistema) {
		
		HashMap<Long, Object> hashCalculosParidad = new HashMap<>();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		CalcularParidad calcularParidad = new CalcularParidad();
		
		List<CalculoEntrada> calculos = new ArrayList<>();
		
		for(GarantiaVencer garantia: listGarantiaVencerConTransito) {
			CalculoEntrada calculo = new CalculoEntrada();
			BigDecimal MontoARetirar      		= BigDecimal.ZERO;
			BigDecimal PrecioRetiro				= BigDecimal.ZERO;
			BigDecimal MontoGarantiaMonedaInstrumento = BigDecimal.ZERO;
			BigDecimal CantidadARetirar = BigDecimal.ZERO;
			
			CantidadARetirar = "IF".equals(garantia.IdClasificacionTipoInstrumento) ? garantia.ValorRescate : garantia.Cantidad;
			MontoGarantiaMonedaInstrumento = garantia.MontoGarantiaMonedaInstrumento;
			PrecioRetiro = garantia.INGPrecioMercadoActual;
			if(MontoGarantiaMonedaInstrumento.compareTo(BigDecimal.ZERO) > 0) {
				MontoARetirar = (MontoGarantiaMonedaInstrumento.divide(CantidadARetirar)).multiply(CantidadARetirar);
			}
			else {
				MontoARetirar = PrecioRetiro.multiply(CantidadARetirar);
			}
			
			calculo.IdTrabajo = garantia.IdGarantia;
			calculo.IdInterno = 	garantia.IdGarantia;
			calculo.Fecha = entrada.p_Fecha;
			calculo.IdMonedaOrigen = garantia.IdMonedaInstrumento;
			calculo.IdMonedaDestino = monedaSistema;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculo.MontoOrigen = MontoARetirar;
			calculo.IdUso = "";
			
			calculos.add(calculo);
		}
		
		if(listGarantiaVencerConTransito.size() > 0) {
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			calcularParidadEntrada.p_CalculosEntrada = calculos;
			if(calculos.size() > 0) {
				try {
					CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
					if(calculoSalida.p_out_CalculosSalida.size() > 0) {
						hashCalculosParidad = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
					}
				}
				catch(Exception e) {
					logger.info("Error al calcular paridad retiro garantia", e);
				}
			}
		}
		return hashCalculosParidad;
	}
	
	public RetirarGarantiaSalida retirarGarantiasIFRF(Entrada entrada,Connection con, List<GarantiaVencer> listGarantiaVencerConTransito,HashMap<Long, Object> hashCalculosParidadGarantias) {
		
		// crear objeto para llamar el retiro de garantias.  considerar si es RF tiene que ser la cantidad y no el valor rescate
		RetirarGarantiaEntrada retirarGarantiaEntrada = new RetirarGarantiaEntrada();
		RetirarGarantiaJava  retirarGarantia = new RetirarGarantiaJava();				
		List<ObjGarantiasARetirar> listGarantiasARetirar = new ArrayList<>();
		RetirarGarantiaSalida retirarGarantiaSalida = new RetirarGarantiaSalida();
		
		for(GarantiaVencer garantia: listGarantiaVencerConTransito) {
			ObjGarantiasARetirar garantiaARetirar = new ObjGarantiasARetirar();
			BigDecimal montoMonedaInstrumento = BigDecimal.ZERO;
			if(hashCalculosParidadGarantias.size() > 0 && hashCalculosParidadGarantias.containsKey(garantia.IdGarantia)) {
				CalculoSalida calculoAsociadoAGarantia = (CalculoSalida) hashCalculosParidadGarantias.get(garantia.IdGarantia);
				montoMonedaInstrumento = calculoAsociadoAGarantia.MontoOrigen;
			}
			
			garantiaARetirar.IdGarantia = garantia.IdGarantia;
			garantiaARetirar.Cantidad =  "IF".equals(garantia.IdClasificacionTipoInstrumento) ? garantia.ValorRescate : garantia.Cantidad;
			garantiaARetirar.ValorRescate =  "IF".equals(garantia.IdClasificacionTipoInstrumento) ? garantia.ValorRescate : garantia.Cantidad;
			garantiaARetirar.MontoMonedaInstrumento = montoMonedaInstrumento;//garantia.MontoMonedaInstrumento != null ? garantia.MontoMonedaInstrumento : BigDecimal.ZERO;
			garantiaARetirar.PrecioRetiro = garantia.PrecioRetiro != null ? garantia.PrecioRetiro : BigDecimal.ZERO;
			garantiaARetirar.TasaRetiro = garantia.TasaRetiro != null ? garantia.TasaRetiro : BigDecimal.ZERO;
			garantiaARetirar.IdConcepto = garantia.IdConcepto;
			garantiaARetirar.ConCertificadoAutomatico = "S";
			garantiaARetirar.IdClasificacionTipoInstrumento = garantia.IdClasificacionTipoInstrumento;
			
			listGarantiasARetirar.add(garantiaARetirar);
		}
		
		if(listGarantiasARetirar.size() > 0) {
			retirarGarantiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			retirarGarantiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
			retirarGarantiaEntrada.p_IdModulo = "CUS";
		    retirarGarantiaEntrada.p_IdTipoOperacion= "RETGTA";
			retirarGarantiaEntrada.p_UsaReglasNegocio = "N";
			retirarGarantiaEntrada.p_SimulaOperacion = entrada.p_SimularOperacion;
			retirarGarantiaEntrada.p_FechaMovimiento = entrada.p_Fecha;
			retirarGarantiaEntrada.p_tablaRetiro = listGarantiasARetirar;
			try {
				retirarGarantiaSalida = retirarGarantia.logica(con, retirarGarantiaEntrada);
			}
			catch(Exception e) {
				logger.info("Error al retirar garantias IF / RF", e);
			}
		}
		return retirarGarantiaSalida;
	}
	
			
	
	private IngresoCustodiaSalida crearMovimientosEnTransito (Connection con, List<GarantiaVencer> listaGarantiasVencer, Entrada entradaVencimiento) throws Exception {
		//logger.trace("INICIO::EjecutarVencimiento.crearMovimientosEnTransito");
		long inicioMetodo = System.currentTimeMillis();
		
		List<MensajeSalida> salidaMovTransito = new ArrayList<>();		
		IngresoCustodiaEntrada entradaCustodiaTransito = new IngresoCustodiaEntrada();
		IngresoCustodiaSalida salidaCustodiaTransito = new IngresoCustodiaSalida();	
		List<IngresoCustodia.ObjDatosEntrada> detalleIngresosCustodia= new ArrayList<>();
		IngresoCustodia ingresoTransitos = new IngresoCustodia();
		
		
		detalleIngresosCustodia = listaGarantiasVencer.parallelStream().map(gar->{
			IngresoCustodia.ObjDatosEntrada detalle = new IngresoCustodia.ObjDatosEntrada();
			
			
			detalle.IdGenerarMovimientoCustodia 		= gar.IdGarantia;	
			detalle.TablaAfectada						= "TRANSITO"; 		
			detalle.EsRegistroNuevo						= "S";				
			detalle.IdCustodia 							= 0L;//gar.IdCustodia;				
			detalle.FolioCustodia 						= 0L;//gar.FolioCartera;					 		
			detalle.IdMovimientoCarteraEnTransitoRelacionado = 0L;//gar.IdGarantia;			
			detalle.AccionOperacion						= "RETIRO";	
			detalle.IdCliente							= gar.IdCliente;
			detalle.IdPortafolio						= gar.IdPortafolio;			
			detalle.IdClienteRelacionado				= gar.IdClienteCorredor;
			detalle.IdPortafolioRelacionado			    = gar.IdPortafolioCorredor;	
			detalle.FechaOperacion						= gar.FechaOperacion;
			detalle.FechaVencimientoOperacion 		    = gar.FechaVencimientoInstrumento;
			detalle.IdTipoOperacion					    = "RETTIT";
			detalle.IdOperacion							= gar.IdGarantia;
			detalle.IdConcepto							= "VCDTIT";
			detalle.IdCondicionLiquidacion				= gar.IdCondicionLiquidacion; 
			detalle.FechaLiquidacion                    = gar.FechaLiquidacion;
			detalle.IdTipoProducto						= gar.IdTipoProducto; 
			detalle.IdTipoCanalIngreso					= gar.IdTipoCanalIngreso;
			detalle.CodigoIFRS							= gar.CodigoIFRS;
			detalle.IdTipoComprobante					= gar.IdTipoComprobante;
			detalle.FolioComprobante					= gar.IdGarantia; 
			detalle.FechaComprobante					= gar.FechaOperacion;			
			detalle.IdBolsa							    = gar.IdBolsa;
			detalle.IdUsuario							= entradaVencimiento.p_IdUsuario;
			detalle.PermiteCertificacionPorUsuario      = "S";
			detalle.EsPendienteAutorizacion 			= "N";
			detalle.IdInstrumento						= gar.IdInstrumento;
			detalle.Nemotecnico							= gar.Nemotecnico;
			detalle.NemoSVS								= gar.NemoSVS;
			detalle.EsNominal							= "N"; 
			detalle.FechaVencimientoInstrumento			= gar.FechaVencimientoInstrumento;
			detalle.FechaEmisionInstrumento			    = gar.FechaVencimientoInstrumento;
			detalle.Glosa								= "VENCIMIENTO GARANTIA";
			detalle.IdEmisor							= gar.IdEmisor;
			detalle.Cantidad							= gar.Cantidad; 
			detalle.CorteMinimo							= gar.CorteMinimo;
			detalle.ValorRescate				    	= gar.ValorRescate;
			detalle.PrecioCompra						= gar.PrecioCompra; 
			detalle.TasaCompra							= gar.TasaCompra;
			detalle.MontoCompra							= gar.MontoCompra; 
			detalle.MontoCompraMonedaInstrumento		= gar.MontoCompraMonedaInstrumento; 
			detalle.IdTipoReajusteInstrumento           = gar.IdTipoReajusteInstrumento;
			detalle.IdMonedaInstrumento					= gar.IdMonedaInstrumento;
			detalle.IdMonedaEmision						= gar.IdMonedaEmision;
			detalle.IdBoveda							= gar.IdBoveda;
			detalle.EsFungible							= gar.EsFungible;
			
			
			return detalle;
			
		}).collect(Collectors.toList());
		
		entradaCustodiaTransito.p_IdEmpresa = entradaVencimiento.p_IdEmpresa;
		entradaCustodiaTransito.p_IdModulo  = entradaVencimiento.p_IdModulo;
		entradaCustodiaTransito.p_TicketAtencion  =0L;
		
		entradaCustodiaTransito.p_DatosEntrada=detalleIngresosCustodia ;
		
		salidaCustodiaTransito = ingresoTransitos.logica(con, entradaCustodiaTransito);
		
		salidaMovTransito = salidaCustodiaTransito.P_OUT_MENSAJES;
		
		long finMetodo = System.currentTimeMillis();
		//logger.trace("FIN::EjecutarVencimiento.crearMovimientosEnTransito :" , finMetodo-inicioMetodo);

		return salidaCustodiaTransito;
	}
	
	//Genera vencimiento de titulos IF para insertarlo en la tabla MovimientosCarteraEnTransito
	private IngresoCustodiaSalida crearMovimientosEnTransitoIF (Connection con, List<CustodiaVencer> listCustodiaVencer, Entrada entradaVencimiento) throws Exception {
		//logger.trace("INICIO::EjecutarVencimiento.crearMovimientosEnTransitoIF");
		long inicioMetodo = System.currentTimeMillis();
		
		List<MensajeSalida> salidaMovTransito = new ArrayList<>();		
		IngresoCustodiaEntrada entradaCustodiaTransito = new IngresoCustodiaEntrada();
		IngresoCustodiaSalida salidaCustodiaTransito = new IngresoCustodiaSalida();	
		List<IngresoCustodia.ObjDatosEntrada> detalleIngresosCustodia= new ArrayList<>();
		IngresoCustodia ingresoTransitos = new IngresoCustodia();
		
		detalleIngresosCustodia = listCustodiaVencer.stream().map(tra->{
			IngresoCustodia.ObjDatosEntrada detalle = new IngresoCustodia.ObjDatosEntrada();
			
			
			detalle.IdGenerarMovimientoCustodia 		= tra.IdCartera;
			detalle.TablaAfectada						= "TRANSITO";		
			detalle.EsRegistroNuevo						= "S";			
			detalle.IdCustodia 							= 0L;			
			detalle.FolioCustodia 						= 0L;			 		
			detalle.IdMovimientoCarteraEnTransitoRelacionado = 0L;
			detalle.AccionOperacion						= "RETIRO";
			detalle.IdCliente							= tra.IdCliente;
			detalle.IdPortafolio						= tra.IdPortafolio;	
			detalle.IdClienteRelacionado				= 0L;
			detalle.IdPortafolioRelacionado			    = 0L;
			detalle.FechaOperacion						= tra.FechaVencimientoInstrumento;
			detalle.FechaVencimientoOperacion 		    = tra.FechaVencimientoInstrumento;
			detalle.IdTipoOperacion					    = "RETTIT";
			detalle.IdOperacion							= 0L;
			detalle.IdConcepto							= "VCDTIT";
			detalle.IdCondicionLiquidacion				= tra.IdCondicionLiquidacion;
			detalle.FechaLiquidacion                    = tra.FechaLiquidacionDate;
			detalle.IdTipoProducto						= tra.IdTipoProducto;
			detalle.IdTipoCanalIngreso					= tra.IdTipoCanalIngreso;
			detalle.CodigoIFRS							= "";
			detalle.IdTipoComprobante					= tra.IdTipoComprobante;
			detalle.FolioComprobante					= 0L;
			detalle.FechaComprobante					= tra.FechaComprobante;
			detalle.IdBolsa							    = tra.IdBolsa;
			detalle.IdUsuario							= entradaVencimiento.p_IdUsuario;
			detalle.PermiteCertificacionPorUsuario      = "S";
			detalle.EsPendienteAutorizacion 			= "N";
			detalle.IdInstrumento						= tra.IdInstrumento;
			detalle.Nemotecnico							= tra.Nemotecnico;
			detalle.NemoSVS								= tra.NemoSVS;
			detalle.EsNominal							= "N";
			detalle.FechaVencimientoInstrumento			= tra.FechaVencimientoInstrumento;
			detalle.FechaEmisionInstrumento			    = tra.FechaVencimientoInstrumento;
			detalle.Glosa								= "VENCIMIENTO TITULO";
			detalle.IdEmisor							= tra.IdEmisor;
			detalle.Cantidad							= tra.Cantidad;
			detalle.CorteMinimo							= tra.CorteMinimo;
			detalle.ValorRescate				    	= tra.ValorRescate;
			detalle.PrecioCompra						= tra.PrecioCompra;
			detalle.TasaCompra							= tra.INGTasaCompra;
			detalle.MontoCompra							= tra.INGMontoATasaCompraActual;
			detalle.MontoCompraMonedaInstrumento		= tra.MontoCompraMonedaInstrumento;
			detalle.IdTipoReajusteInstrumento           = tra.IdTipoReajuste;
			detalle.IdMonedaInstrumento					= tra.IdMonedaInstrumento;
			detalle.IdMonedaEmision						= tra.IdMonedaEmision;
			detalle.IdBoveda	                        = tra.IdBoveda != null ? tra.IdBoveda : "";
			detalle.EsFungible							= tra.EsFungible;

			
			return detalle;
			
		}).collect(Collectors.toList());
		
		entradaCustodiaTransito.p_IdEmpresa = entradaVencimiento.p_IdEmpresa;
		entradaCustodiaTransito.p_IdModulo  = entradaVencimiento.p_IdModulo;
		entradaCustodiaTransito.p_TicketAtencion  =0L;
		
		entradaCustodiaTransito.p_DatosEntrada=detalleIngresosCustodia;
		
		salidaCustodiaTransito = ingresoTransitos.logica(con, entradaCustodiaTransito);
		
		salidaMovTransito = salidaCustodiaTransito.P_OUT_MENSAJES;
		
		long finMetodo = System.currentTimeMillis();
		//logger.trace("FIN::EjecutarVencimiento.crearMovimientosEnTransitoIF :" , finMetodo-inicioMetodo);

		return salidaCustodiaTransito;
	}
	
	private List<MensajeSalida> validarLimitesCuentaPropia(Connection conn, Entrada entrada, List<GarantiaVencer> garantias, List<CustodiaVencer> custodias) throws Exception {
		logger.info("INICIO::EjecutarVencimiento.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		List<ValidarLimiteCuentaPropiaIFRF.Operacion> operaciones = new ArrayList<>(garantias.size() + custodias.size());
		AtomicLong atomicLong = new AtomicLong(0L);
		garantias.forEach(garantia -> {
			long idInterno = atomicLong.getAndIncrement();
			ValidarLimiteCuentaPropiaIFRF.Operacion operacion = new ValidarLimiteCuentaPropiaIFRF.Operacion();
			operacion.IdInternoGrupo = null;
			operacion.IdInterno = idInterno; 
			operacion.IdOperacion = idInterno; 
			operacion.Folio = String.valueOf(garantia.FolioCartera);
			operacion.IdCliente = garantia.IdCliente;
			operacion.IdPortafolio = garantia.IdPortafolio;
			operacion.IdInstrumento = garantia.IdInstrumento;
			operacion.IdTipoCanalIngreso = garantia.IdTipoCanalIngreso;
			operacion.IdTipoOperacion = garantia.IdTipoOperacion;
			operacion.IdMonedaOperacion = garantia.IdMonedaInstrumento;
			operacion.Cantidad = garantia.Cantidad;
			operacion.ValorRescate = garantia.ValorRescate;
			operacion.MontoCompraMonedaSistema = garantia.MontoMonedaInstrumento;
			operacion.FechaVencimientoInstrumento = garantia.FechaVencimientoInstrumento;
			operacion.Tasa = garantia.INGTasaGarantia;
			operacion.NemoSVS = garantia.NemoSVS;
			operaciones.add(operacion);
		});
		custodias.forEach(custodia -> {
			long idInterno = atomicLong.getAndIncrement();
			ValidarLimiteCuentaPropiaIFRF.Operacion operacion = new ValidarLimiteCuentaPropiaIFRF.Operacion();
			operacion.IdInternoGrupo = null;
			operacion.IdInterno = idInterno; 
			operacion.IdOperacion = idInterno; 
			operacion.Folio = String.valueOf(custodia.FolioCartera);
			operacion.IdCliente = custodia.IdCliente;
			operacion.IdPortafolio = custodia.IdPortafolio;
			operacion.IdInstrumento = custodia.IdInstrumento;
			operacion.IdTipoCanalIngreso = custodia.IdTipoCanalIngreso;
			operacion.IdTipoOperacion = custodia.IdTipoOperacion;
			operacion.IdMonedaOperacion = custodia.IdMonedaInstrumento;
			operacion.Cantidad = custodia.INGCantidad;
			operacion.ValorRescate = custodia.ValorRescate;
			operacion.MontoCompraMonedaSistema = custodia.INGMontoCompraActualMonedaInstrumento;
			operacion.FechaVencimientoInstrumento = custodia.FechaVencimientoInstrumento;
			operacion.Tasa = custodia.INGTasaCompra;
			operacion.NemoSVS = custodia.NemoSVS;
			operaciones.add(operacion);
		});
		
		List<MensajeSalida> advertencias = new ArrayList<>(1);
		try {
			ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF entradaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF();
			entradaValidarLimiteCuentaPropiaIFRF.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaValidarLimiteCuentaPropiaIFRF.p_IdUsuario = entrada.p_IdUsuario;
			entradaValidarLimiteCuentaPropiaIFRF.p_IdModulo = entrada.p_IdModulo;
			entradaValidarLimiteCuentaPropiaIFRF.p_UsaReglasNegocio = _cl.SI;
			entradaValidarLimiteCuentaPropiaIFRF.p_IdTipoProceso = _cl.PROCESO_PAUTA_VENCIMIENTO;
			entradaValidarLimiteCuentaPropiaIFRF.p_Operaciones = operaciones;
			
			ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF().logica(conn, entradaValidarLimiteCuentaPropiaIFRF);
			advertencias = salidaValidarLimiteCuentaPropiaIFRF.P_OUT_MENSAJES;
		} catch (Exception e) {
			logger.info("Error al validar limites cuenta propia", e);
			advertencias.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_VALIDAR_LIMITES_CUENTA_PROPIA", new HashMap<>(0), conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EjecutarVencimiento.validarLimitesCuentaPropia " + (fin - inicio));
		
		return advertencias;
	}
	
	/**********************
	 * metodos del programa
	 **********************/
	public String obtenerQueryVencerNegativa(String tipoVencimiento) {
		if (tipoVencimiento.equals("RF"))
			return SQL_CustodiaNegativaVencerRF;
		if (tipoVencimiento.equals("IF"))
			return SQL_CustodiaNegativaVencerIF;
		return null;
	}
	
	public String obtenerQueryGarantias(String tipoVencimiento){
		if ("RF".equals(tipoVencimiento))
			return SQL_GarantiaVencerRF;
		if ("IF".equals(tipoVencimiento))
			return SQL_GarantiaVencerIF;
		if("SIM".equals(tipoVencimiento))
			return SQL_GarantiaVencerSIM;
		return null;
	}
	
	public String obtenerQueryVencer(String tipoVencimiento) {
		if (tipoVencimiento.equals("RF"))
			return SQL_CustodiaVencerRF;
		if (tipoVencimiento.equals("IF"))
			return SQL_CustodiaVencerIF;
		if (tipoVencimiento.equals("SIM"))
			return SQL_CustodiaVencerSIM;
		if (tipoVencimiento.equals("PAC"))
			return SQL_CustodiaVencerPAC;
		if (tipoVencimiento.equals("PRT"))
			return SQL_CustodiaVencerPRT;
		if (tipoVencimiento.equals("OPC"))
			return SQL_CustodiaVencerOPC;
		if (tipoVencimiento.equals("RE"))
			return SQL_CustodiaVencerRE;
		return null;
	}

	public void logEjecucion(List<LogEjecucion> logej, String mensaje, String esError, String esInterno) {
		logger.info("TipoVencimiento=" + tipoVencimiento + " => logEjecucion[esError=" + esError + "]: " + mensaje);
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}

	public String diaAnteriorFechaSistema(Connection con, String fechaSistema, Long idPais) throws Exception {
		ObtenerDiaAnterior o = new ObtenerDiaAnterior();
		InObtenerDiaAnterior entrada = new InObtenerDiaAnterior();
		entrada.idPais = idPais;
		entrada.diaHabil = "S";
		InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn();
		f1.fechaInicial = fechaSistema;
		f1.numeroDias = new Long(1);
		List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<InObtenerDiaAnterior.FechasIn>();
		fechasIn.add(f1);
		entrada.fechas = fechasIn;
		OutObtenerDiaAnterior salida = o.logica(con, entrada);
		return salida.fechas.get(0).fechaCalculada;
	}

	public ProcesamientoCustodia carteraAProcesamiento(CustodiaVencer item) {
		ProcesamientoCustodia aux = new ProcesamientoCustodia();
		aux.IdCartera = item.IdCartera;
		aux.FolioCartera = item.FolioCartera;
		aux.Nemotecnico = item.Nemotecnico;
		aux.EsPagaderoEn = item.EsPagaderoEn;
		aux.INGMontoAPlazo = item.INGMontoAPlazo;
		aux.INGMontoATasaCompraActual = item.INGMontoATasaCompraActual;
		aux.INGMontoCompraActualMonedaInstrumento = item.INGMontoCompraActualMonedaInstrumento;
		aux.IdClasificacionTipoInstrumento = item.IdClasificacionTipoInstrumento;
		aux.IdTipoInstrumento = item.IdTipoInstrumento;
		aux.IdInstrumento = item.IdInstrumento;
		aux.IdPortafolio = item.IdPortafolio;
		aux.IdMonedaInstrumento = item.IdMonedaInstrumento;
		aux.IdTipoProducto = item.IdTipoProducto;
		aux.IdTipoOperacion = item.IdTipoOperacion;
		aux.OperaContraCuentaInversion = item.OperaContraCuentaInversion;
		aux.IdPersona = item.IdPersona;
		aux.IdCliente = item.IdCliente;
		aux.CobraPrimaAlInicio = item.CobraPrimaAlInicio;
		// sacados de sp simultanea antiguo
		aux.PrecioAPlazo = item.PrecioAPlazo;
		aux.MontoAPlazo = item.MontoAPlazo;
		aux.FechaComprobante = item.FechaComprobante;
		aux.FechaLiquidacion = item.FechaLiquidacion;
		aux.IdOperacion = item.IdOperacion;
		aux.IdMonedaSistema = item.IdMonedaSistema;
		aux.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
		aux.INGCantidad = item.INGCantidad;
		aux.INGValorRescate = item.INGValorRescate;
		aux.ValorRescate = item.ValorRescate;
		aux.INGTasaCompra = item.INGTasaCompra;
		aux.PrecioCompra = item.PrecioCompra;
		aux.INGPrecioCompra = item.INGPrecioCompra;		
		aux.AccionOperacion = item.AccionOperacion; // lo usa la simultanea		
		aux.IdTipoComprobante = item.IdTipoComprobante; // lo usa simultanea
		aux.FolioComprobante = item.FolioComprobante; // lo usa simultanea
		
		return aux;
	}

	public CustodiaVencer procesamientoACartera(ProcesamientoCustodia item) {
		CustodiaVencer aux = new CustodiaVencer();
		aux.IdCartera = item.IdCartera;
		aux.FolioCartera = item.FolioCartera;
		aux.Nemotecnico = item.Nemotecnico;
		aux.EsPagaderoEn = item.EsPagaderoEn;
		aux.INGMontoAPlazo = item.INGMontoAPlazo;
		aux.INGMontoATasaCompraActual = item.INGMontoATasaCompraActual;
		aux.INGMontoCompraActualMonedaInstrumento = item.INGMontoCompraActualMonedaInstrumento;
		aux.IdClasificacionTipoInstrumento = item.IdClasificacionTipoInstrumento;
		aux.IdTipoInstrumento = item.IdTipoInstrumento;
		aux.IdInstrumento = item.IdInstrumento;
		aux.IdPortafolio = item.IdPortafolio;
		aux.IdMonedaInstrumento = item.IdMonedaInstrumento;
		aux.IdTipoProducto = item.IdTipoProducto;
		aux.IdTipoOperacion = item.IdTipoOperacion;
		aux.OperaContraCuentaInversion = item.OperaContraCuentaInversion;
		aux.IdPersona = item.IdPersona;
		aux.IdCliente = item.IdCliente;
		aux.AccionOperacion = item.AccionOperacion; // lo usa la simultanea
		// sacados de sp simultanea antiguo
		aux.PrecioAPlazo = item.PrecioAPlazo;
		aux.MontoAPlazo = item.MontoAPlazo;
		aux.FechaComprobante = item.FechaComprobante;
		aux.FechaLiquidacion = item.FechaLiquidacion.toString();
		aux.IdMonedaSistema = item.IdMonedaSistema;
		aux.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
		aux.INGCantidad = item.INGCantidad;
		aux.INGValorRescate = item.INGValorRescate;
		aux.ValorRescate = item.ValorRescate;
		aux.INGTasaCompra = item.INGTasaCompra;
		aux.PrecioCompra = item.PrecioCompra;
		aux.INGPrecioCompra = item.INGPrecioCompra;
		aux.IdTipoComprobante = item.IdTipoComprobante; // lo usa simultanea
		aux.FolioComprobante = item.FolioComprobante; // lo usa simultanea
		
		return aux;
	}

	private RetiroCustodia.RetiroCustodiaEntrada preparaDatosCustodia(String simular,List<CustodiaVencer> lCV, Entrada entrada, String fechaSistemaAnterior){
		RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
		paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
		paramInSPRetiro.p_IdModulo			= entrada.p_IdModulo;
		paramInSPRetiro.p_IdUsuario			= entrada.p_IdUsuario;
		paramInSPRetiro.p_SimularOperacion	= simular;
		paramInSPRetiro.p_TicketAtencion	= 0L;
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		for(CustodiaVencer item : lCV){
			if ("PRT".equals(entrada.p_IdTipoVencimiento)) {
				if ("N".equals(item.CobraPrimaAlInicio)) {
					continue;
				}
			}
			RetiroCustodia.ObjDatosEntrada ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia = item.IdCartera;
			ObjEntrada.TablaAfectada = "CARTERA";
			ObjEntrada.AccionOperacion = "VENCIMIENTO";
			ObjEntrada.IdCliente  = item.IdCliente;
			ObjEntrada.IdPortafolio	= item.IdPortafolio;
			ObjEntrada.FolioCustodia = item.FolioCartera;
			ObjEntrada.IdCustodia = item.IdCartera;
			ObjEntrada.IdOperacion = item.IdOperacion;
			if (entrada.p_IdTipoVencimiento.equals("SIM")) {
				ObjEntrada.IdConcepto = (item.IdTipoOperacion.equals("COMSIM")) ? "VCTSIC" : "VCTSIV";
				ObjEntrada.IdTipoOperacion = (item.IdTipoOperacion.equals("COMSIM")) ? "VESIMC" : "VESIMV";// "VESIMC":"VESIMV"
			} else {
				if (entrada.p_IdTipoVencimiento.equals("PAC")) {
					ObjEntrada.IdConcepto = "VCDPAC";
					ObjEntrada.IdTipoOperacion = "VCDPAC";
				} else if(entrada.p_IdTipoVencimiento.equals("IF") ||entrada.p_IdTipoVencimiento.equals("RF")) {
					ObjEntrada.IdConcepto = "VCDTIT";
					ObjEntrada.IdTipoOperacion = "VCDTIT";
				} else {
					// OCT
					if (entrada.p_IdTipoVencimiento.equals("PRT")) {
						ObjEntrada.IdConcepto = "VENPRT";
						ObjEntrada.IdTipoOperacion = "VENPRT";
					} else {
						ObjEntrada.IdConcepto = "VCDTIT";
						ObjEntrada.IdTipoOperacion = "VCDTIT";
					}
				}
			}
			ObjEntrada.Cantidad	= item.INGCantidad;
			ObjEntrada.IdInstrumento = item.IdInstrumento;
			ObjEntrada.FechaOperacion = Fecha.toDate(fechaSistemaAnterior);
			ObjEntrada.ValorRescate = item.ValorRescate;
			ObjEntrada.FechaLiquidacion = item.FechaLiquidacion != null ? Fecha.toDate(item.FechaLiquidacion) : null;
			ObjEntrada.IdAgente = item.IdAgente;
			ObjEntrada.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
			ObjEntrada.TasaCompra = item.INGTasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento = item.INGMontoCompraActualMonedaInstrumento;
			ObjEntrada.MontoAPlazo = item.INGMontoAPlazo;
			ObjEntrada.PrecioAPlazo = item.INGMontoAPlazo;
			
			ObjEntrada.IdTipoReajusteInstrumento = item.IdTipoReajuste;
			ObjEntrada.IdMonedaEmision = item.IdMonedaEmision;
			ObjEntrada.IdMonedaInstrumento = item.IdMonedaInstrumento;
			ObjEntrada.EsFungible = item.EsFungible;	
			if("RF".equalsIgnoreCase(item.IdClasificacionTipoInstrumento)) {
				ObjEntrada.FechaVencimientoInstrumento = item.FechaVencimientoInstrumento;
			}
			
			
			p_RetiroCartera.add(ObjEntrada);
		}
		paramInSPRetiro.p_DatosEntrada = p_RetiroCartera;
		return paramInSPRetiro;
	}
	
	public RetiroCustodia.RetiroCustodiaEntrada preparaDatosGarantia(String simular, List<GarantiaVencer> lGV, Entrada entrada, String fechaSistemaStr) throws Exception {
		RetiroCustodia.RetiroCustodiaEntrada retirarGarantiaEntrada = new RetiroCustodia.RetiroCustodiaEntrada();
		List<RetiroCustodia.ObjDatosEntrada> p_DatosGarantia = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		for (GarantiaVencer gv : lGV) {
			RetiroCustodia.ObjDatosEntrada objGarantia = new RetiroCustodia.ObjDatosEntrada();
			objGarantia = Utils.copyPojo(gv, objGarantia);
			objGarantia.IdGenerarMovimientoCustodia = gv.IdGarantia;
			objGarantia.TablaAfectada = "GARANTIA";
			objGarantia.AccionOperacion = "RETIRO";
			objGarantia.FechaOperacion = Fecha.toDate(fechaSistemaStr);
			objGarantia.FechaLiquidacion = Fecha.toDate(fechaSistemaStr);
			objGarantia.IdTipoOperacion = "RETGTA";
			p_DatosGarantia.add(objGarantia);
		}
		
		retirarGarantiaEntrada.p_IdEmpresa			= entrada.p_IdEmpresa;
		retirarGarantiaEntrada.p_IdModulo			= entrada.p_IdModulo;
		retirarGarantiaEntrada.p_IdUsuario			= entrada.p_IdUsuario;
		retirarGarantiaEntrada.p_SimularOperacion	= "N";
		retirarGarantiaEntrada.p_TicketAtencion		= 0L;
		retirarGarantiaEntrada.p_DatosEntrada		= p_DatosGarantia;
		
		return retirarGarantiaEntrada;
	}
	
	
	public CrearCargoAbono.CrearCargoAbonoEntrada prepararDatosCuentaInversionGarantia(Connection con, String simular, List<GarantiaVencer> lGV,Entrada entrada, String fechaSistemaStr) {
		
		CrearCargoAbono.CrearCargoAbonoEntrada incca = new CrearCargoAbono.CrearCargoAbonoEntrada();
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> p_DatosIncca = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
		for (GarantiaVencer item : lGV) {
			if(item.TieneCorteDeCupon == null || "N".equals(item.TieneCorteDeCupon)){
				CrearCargoAbono.ObjCargoAbonoAGenerar aux = new CrearCargoAbono.ObjCargoAbonoAGenerar();
				aux.NumeroCargoAbono = item.IdCartera;
				aux.IdPortafolio = item.IdPortafolio;
				aux.IdMoneda = item.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ? ("UF".equalsIgnoreCase(item.IdTipoReajusteInstrumento) ? item.IdMonedaInstrumento : item.IdTipoReajusteInstrumento) : item.IdMonedaInstrumento;
				aux.Descripcion = "Generado por Vencimiento";
				aux.IdCondicionDeLiquidacion = item.IdCondicionLiquidacion;
				aux.FechaCondicionDeLiquidacion =entrada.p_Fecha;
				
				if ( entrada.p_IdTipoVencimiento.equalsIgnoreCase("IF") ) 
					aux.Monto = item.ValorRescate;
				else 
					aux.Monto = item.MontoCompraMonedaInstrumento;			
				
				aux.IdEjecutivo = new Long(0);
				aux.IdSucursal = new Long(0);
				aux.IdTipoProducto = item.IdTipoProducto;
				aux.IdTipoOperacion = null;
				aux.IdOperacion = item.IdOperacion;
				aux.IdTipoComprobante = "CUS";
				aux.IdComprobante = item.IdCartera;
				aux.FolioComprobante = item.FolioCartera.toString();
				aux.FechaComprobante = fechaSistemaStr;
				aux.IdInstrumento = item.IdInstrumento;
				aux.IdTipoInstrumento = item.IdTipoInstrumento;
				aux.Cantidad = BigDecimal.ZERO;
				aux.Nemo = item.Nemotecnico;
				aux.EsIndicadorBilateral = "N";
				aux.EsCargoAbonoPorTraspaso = "N";
				aux.CertificadoAutomatico = "S";			
				aux.IdConcepto = "VCDTIT";
				aux.IdTipoMovimiento = "MOVABN";
				aux.FolioComprobante = item.FolioCartera.toString();
				
				p_DatosIncca.add(aux);
			}
		}
		incca.p_IdEmpresa = entrada.p_IdEmpresa;
		incca.p_IdUsuario = entrada.p_IdUsuario;
		incca.p_IdModulo = entrada.p_IdModulo;
		incca.p_simularOperacion = simular;
		incca.p_UsaReglasNegocio = "S";
		incca.p_FechaSistema = fechaSistemaStr;
		incca.p_EsIngresoManual = "N";
		incca.p_CargoAbonoAGenerar = p_DatosIncca;
		return incca;
	}
	
	
	public BigDecimal obtenerValoresFinancieros (List<ValorMoneda> listValorMoneda, String parametroFinanciero) {
		
		logger.trace("INICIO::EjecutarVencimiento.validacionDeTipoMoneda");
		long inicio = System.currentTimeMillis();
		
		BigDecimal valor=  BigDecimal.ONE;
		
		List<ValorMoneda> obtenerValorFinancieroList = listValorMoneda.stream()
		        .filter(item -> parametroFinanciero.equalsIgnoreCase(item.IdParametroFinanciero))
		        .collect(Collectors.toList());
		
		if (!obtenerValorFinancieroList.isEmpty()) {
			valor = obtenerValorFinancieroList.get(0).Valor;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EjecutarVencimiento.validacionDeTipoMoneda tiempo: " + (fin - inicio));
		
		return valor;
	}

	
	
	public BigDecimal calcularValorMonto (BigDecimal valorRescate, String IdTipoReajuste, String IdMonedaInstrumento, BigDecimal UF, BigDecimal DO, BigDecimal EUR, String destino) {
		logger.trace("INICIO::EjecutarVencimiento.calcularValorMonto");
		long inicio = System.currentTimeMillis();
		
		BigDecimal valorCalculado = valorRescate;
		
		if (!"CLP".equals(IdTipoReajuste) && (destino.equals("CINV") && !IdTipoReajuste.equals(IdMonedaInstrumento) || destino.equals("TES"))) {
			switch(IdTipoReajuste) {
				case "UF":
					return valorRescate.multiply(UF);
				case "USD":
					return valorRescate.multiply(DO);
				case "EUR":
					if (DO.compareTo(BigDecimal.ONE) == 0) return valorRescate;
					BigDecimal valorEur = EUR.multiply(DO);
					return valorRescate.multiply(valorEur);		
				default:
					return valorRescate;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EjecutarVencimiento.calcularValorMonto tiempo: " + (fin - inicio));
		
		return valorCalculado;
	}


	public CrearCargoAbono.CrearCargoAbonoEntrada prepararDatosCuentaInversion(Connection con, String simular, List<CustodiaVencer> lCV, List<ValorMoneda> listValorMoneda,
			Entrada entrada, String fechaSistemaStr, String certCargoAbonoVencimientosDAP) {
		
		CrearCargoAbono.CrearCargoAbonoEntrada incca = new CrearCargoAbono.CrearCargoAbonoEntrada();
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> p_DatosIncca = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
		
		
		BigDecimal UF = BigDecimal.ZERO;
		BigDecimal DO = BigDecimal.ZERO;
		BigDecimal EUR = BigDecimal.ZERO;
		
		if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("IF")) {
			UF = this.obtenerValoresFinancieros(listValorMoneda, "UF");
			DO = this.obtenerValoresFinancieros(listValorMoneda, "DO");	
			EUR = this.obtenerValoresFinancieros(listValorMoneda, "EUR");
		}
		
		

		
		for (CustodiaVencer item : lCV) {
			// if("S".equals(item.OperaContraCuentaInversion)){ Siempre se llama
			// a Cuenta Inversion
			if(item.TieneCorteDeCupon == null || "N".equals(item.TieneCorteDeCupon)){
				CrearCargoAbono.ObjCargoAbonoAGenerar aux = new CrearCargoAbono.ObjCargoAbonoAGenerar();
				aux.NumeroCargoAbono = item.IdCartera;
				aux.IdPortafolio = item.IdPortafolio;
				
				
				aux.IdMoneda =  item.IdMonedaInstrumento;
				
				
				aux.Descripcion = "Generado por Vencimiento";
				aux.IdCondicionDeLiquidacion = item.IdCondicionLiquidacion; // buscars
				aux.FechaCondicionDeLiquidacion = "PAC".equals(item.IdTipoInstrumento) ? item.FechaLiquidacion
						: entrada.p_Fecha;
				
				
				if (item.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) 
					aux.Monto = item.INGMontoAPlazo;				
				else if (item.IdTipoInstrumento.equalsIgnoreCase("SIM"))
					// Para simultanea debe tomar el MontoAPlazo ya que este se calculo correctamente en la query CustodiaVencerSIM del inicio 
					try	{ aux.Monto = MonedaFormato.formatear(con, item.MontoAPlazo , MonedaFormato.MONTO, aux.IdMoneda).valor;	} catch (Exception e) {	aux.Monto = item.MontoAPlazo; }
				else if ( item.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ) 
					aux.Monto = this.calcularValorMonto(item.INGValorRescate, item.IdTipoReajuste,item.IdMonedaInstrumento, UF, DO, EUR, "CINV");
				else 
					aux.Monto = item.INGMontoCompraActualMonedaInstrumento;
				
				
						
						
				
				aux.IdEjecutivo = new Long(0);
				aux.IdSucursal = new Long(0);
				aux.IdTipoProducto = item.IdTipoProducto;
				aux.IdTipoOperacion = null; // ;(simular.equals("S"))?null:item.IdTipoOperacion;
				aux.IdOperacion = item.IdOperacion;
				aux.IdTipoComprobante = "CUS";
				aux.IdComprobante = item.IdCartera;
				aux.FolioComprobante = item.FolioCartera.toString();
				aux.FechaComprobante = this.obtenerFechaComprobante(item,entrada.p_Fecha); 
				aux.IdInstrumento = item.IdInstrumento;
				aux.IdTipoInstrumento = item.IdTipoInstrumento;
				aux.Cantidad = BigDecimal.ZERO;
				aux.Nemo = item.Nemotecnico;
				aux.EsIndicadorBilateral = "N";
				aux.EsCargoAbonoPorTraspaso = "N";		
				//Cuando el movimiento provenga de un Vto IF DAP, para certificar manualmente: certCargoAbonoVencimientosDAP = S
				aux.CertificadoAutomatico = "S".equalsIgnoreCase(certCargoAbonoVencimientosDAP) && (("PAGARE R".equalsIgnoreCase(item.Nemotecnico) || "PAGARE NR".equalsIgnoreCase(item.Nemotecnico)) 
						&& "IF".equalsIgnoreCase(entrada.p_IdTipoVencimiento) && "IF".equalsIgnoreCase(item.IdClasificacionTipoInstrumento) && (!"CENTRAL".equalsIgnoreCase(item.IdEmisor) && !"TESORERIA".equalsIgnoreCase(item.IdEmisor))) ? "N" : "S";				
				
				if (entrada.p_IdTipoVencimiento.equals("SIM")) {
					aux.IdConcepto = (item.IdTipoOperacion.equals("COMSIM")) ? "VCTSIV" : "VCTSIC";
					aux.IdTipoMovimiento = (item.IdTipoOperacion.equals("COMSIM")) ? "MOVABN" : "MOVCAR";
				} else if (item.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
					aux.IdConcepto = item.IdConcepto;
					aux.IdTipoMovimiento = (item.IdTipoOperacion.contains("COM")) ? "MOVABN" : "MOVCAR";
				} else {
					// MSH
					if (entrada.p_IdTipoVencimiento.equals("PAC")) {
						aux.IdConcepto = "VCDPAC";
						aux.IdTipoOperacion = "VCDPAC";
						aux.FolioComprobante = item.FolioCartera.toString();
						aux.IdTipoMovimiento = (item.IdTipoOperacion.equals("COMPAC")) ? "MOVABN" : "MOVCAR";
					} else {
						aux.IdConcepto = "VCDTIT";
						aux.IdTipoMovimiento = item.esCarteraNegativa.equalsIgnoreCase("N") ? "MOVABN":"MOVCAR";
						aux.FolioComprobante = item.FolioCartera.toString();
					}
				}
				p_DatosIncca.add(aux);
			}
			
			// }
		}
		incca.p_IdEmpresa = entrada.p_IdEmpresa;
		incca.p_IdUsuario = entrada.p_IdUsuario;
		incca.p_IdModulo = entrada.p_IdModulo;
		incca.p_simularOperacion = simular;
		incca.p_UsaReglasNegocio = "S";
		incca.p_FechaSistema = fechaSistemaStr;
		incca.p_EsIngresoManual = "N";
		incca.p_CargoAbonoAGenerar = p_DatosIncca;
		return incca;
	}
	
	
	private String obtenerFechaComprobante (CustodiaVencer item, String FechaEntrada) {
		
		String fecha = "";
		
		if("PAC".equals(item.IdTipoInstrumento) || "SIM".equals(item.IdTipoInstrumento)) {
			fecha=item.FechaLiquidacion;
		}else {
			if(item.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE") || "IF".equals(item.IdClasificacionTipoInstrumento) || "RF".equals(item.IdClasificacionTipoInstrumento)) {
				fecha = FechaEntrada;
			}else {
				fecha = Fecha.toStringDate(item.FechaComprobante);
			}
		}		
		return fecha;
	} 

	public CrearIngresoEgreso_Entrada prepararDatosTesoreria(Connection con, String simular, List<CustodiaVencer> lCV, Entrada entrada,
			String fechaSistemaStr, List<ValorMoneda> listValorMoneda) {
		CrearIngresoEgreso_Entrada incie = new CrearIngresoEgreso_Entrada();
		
		BigDecimal UF = BigDecimal.ZERO;
		BigDecimal DO = BigDecimal.ZERO;
		BigDecimal EUR = BigDecimal.ZERO;
		
		if (entrada.p_IdTipoVencimiento.equalsIgnoreCase("IF")) {
			UF = this.obtenerValoresFinancieros(listValorMoneda, "UF");
			DO = this.obtenerValoresFinancieros(listValorMoneda, "DO");	
			EUR = this.obtenerValoresFinancieros(listValorMoneda, "EUR");
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		
		String IdTipoMovimientoVenTitTesoreria = "MOVING";
		try {
			IdTipoMovimientoVenTitTesoreria = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.TipoMovimientoVCDTITTesoreria");
		
		}catch(Exception e) {
			logger.debug("ConsultaPosicion::No existe parametro TipoMovimientoVCDTITTesoreria, se deja por defecto MOVING");
		}
		
		incie.p_IdEmpresa = entrada.p_IdEmpresa;
		incie.p_IdUsuario = entrada.p_IdUsuario;
		incie.p_IdModulo = entrada.p_IdModulo;
		incie.p_SimularOperacion = simular;
		incie.p_UsaReglasNegocio = "S";
		incie.p_EsIngresoAutomatico = "S";
		incie.p_FechaMovimiento = fechaSistemaStr;

		for (CustodiaVencer item : lCV) {
			if (!"S".equals(item.OperaContraCuentaInversion) && (item.TieneCorteDeCupon == null || "N".equals(item.TieneCorteDeCupon))) {
				IngresoEgreso_Entrada aux = new IngresoEgreso_Entrada();
				aux.NumeroReferencia = item.IdCartera;
				aux.IdTipoPersona = "CLI";
				aux.IdPersona = item.IdPersona;
				IdTipoMovimientoVenTitTesoreria = item.esCarteraNegativa.equalsIgnoreCase("S")?"MOVEGR":IdTipoMovimientoVenTitTesoreria;
				
				aux.IdMoneda = item.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ? ("UF".equalsIgnoreCase(item.IdTipoReajuste) ? item.IdMonedaInstrumento : item.IdTipoReajuste) : item.IdMonedaInstrumento;
				
				
				
				if( item.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) 
					aux.Monto = item.INGMontoAPlazo;
				else if (item.IdTipoInstrumento.equalsIgnoreCase("SIM"))
					// Para simultanea debe tomar el MontoAPlazo ya que este se calculo correctamente en la query CustodiaVencerSIM del inicio 
					try	{ aux.Monto = MonedaFormato.formatear(con, item.MontoAPlazo , MonedaFormato.MONTO, aux.IdMoneda).valor;	} catch (Exception e) {	aux.Monto = item.MontoAPlazo; }
				
				else if ( item.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ) 
					aux.Monto = this.calcularValorMonto(item.INGValorRescate, item.IdTipoReajuste,item.IdMonedaInstrumento, UF, DO, EUR, "TES");
				else 
					aux.Monto = item.INGMontoCompraActualMonedaInstrumento;
				

				
				aux.FechaLiquidacion = "PAC".equals(item.IdTipoInstrumento) ? item.FechaLiquidacion : entrada.p_Fecha;
				aux.IdTipoPersonaTitularPago = "CLI";
				aux.IdPersonaTitularPago = item.IdPersona;
				aux.IdPortafolio = item.IdPortafolio;
				aux.IdPortafolioTitularPago = item.IdPortafolio;
				aux.IdCaja = new Long(0);
				aux.SecuenciaAgrupacion = "";
				aux.SecuenciaRelacionada = new Long(0);
				aux.Glosa = "Ingreso por vencimiento de custodia";
				aux.IdEmpleadoDigitador = new Long(0);
				aux.IdSucursalEmpleadoDigitador = new Long(0);
				aux.IdTipoProducto = item.IdTipoProducto;
				aux.IdTipoOperacion = item.IdTipoOperacion;
				aux.IdTipoComprobante = item.IdTipoComprobante == null ? "" : item.IdTipoComprobante;
				aux.IdComprobante = item.FolioComprobante == null ? 0 : item.FolioComprobante;
				aux.FechaComprobante = "PAC".equals(item.IdTipoInstrumento) ? item.FechaLiquidacion : "0001-01-01";
				aux.FechaLiquidacionComprobante = "PAC".equals(item.IdTipoInstrumento) ? item.FechaLiquidacion
						: "0001-01-01";
				aux.IdOperacion = item.IdOperacion == null ? new Long(0) : item.IdOperacion;
				aux.FechaOperacion = "0001-01-01";
				aux.IdCondicionLiquidacion = "";
				aux.IdBolsa = "";
				aux.IdMonedaOperacion = "";
				aux.MontoOperacion = new BigDecimal(0);
				aux.IdTipoReajuste = "";
				aux.FechaParidad = "0001-01-01";
				aux.ValorParidad = new BigDecimal(0);
				aux.FolioCartera = item.FolioCartera;
				aux.IdInstrumento = new Long(0);
				aux.IdTipoInstrumento = "";
				aux.IdClasificacionTipoInstrumento = "";
				aux.Bilateral = "N";
				aux.GeneraComprobante = "N";
				aux.AgruparEnComprobante = "N";
				aux.MueveCuentaInversion = "N";
				aux.TitularPago = "N";
				if (entrada.p_IdTipoVencimiento.equals("SIM")) {
					aux.IdConcepto = (item.IdTipoOperacion.equals("COMSIM")) ? "VCTSIV" : "VCTSIC";
					aux.IdTipoMovimiento = (item.IdTipoOperacion.equals("COMSIM")) ? "MOVEGR" : "MOVING";
				} else if (item.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
					aux.IdConcepto = item.IdConcepto;
					aux.IdTipoMovimiento = (item.IdTipoOperacion.contains("COM")) ? "MOVING" : "MOVEGR";
				} else {
					// MSH
					if (entrada.p_IdTipoVencimiento.equals("PAC")) {
						aux.IdConcepto = "VCDPAC";
						aux.IdTipoMovimiento = "MOVING";
					} else {
						aux.IdConcepto = "VCDTIT";
						aux.IdTipoMovimiento = IdTipoMovimientoVenTitTesoreria;
					}
				}
				incie.p_IngresoEgresoGenerar.add(aux);
				
				//Recupera forma de pago por defecto desde preferencia de portafolio
				params.clear();
				params.put("IdPortafolio", item.IdPortafolio);
				params.put("IdEmpresa", entrada.p_IdEmpresa);	
				//List<FormaDePagoDefecto> formaPagoDefecto = sql.query(con, SQL_FormaDePagoPorDefecto, params, FormaDePagoDefecto.class);
				
				List<FormaDePagoDefecto> formaPagoDefecto = new ArrayList<>();
				try {
					formaPagoDefecto.addAll(sql.query(con, SQL_FormaDePagoPorDefecto, params, FormaDePagoDefecto.class));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
				FPagTes.NumeroReferencia = item.IdCartera; // BIGINT Id
																// Referencial
																// (para unir el
																// movimiento
																// con su forma
																// de pago)
				FPagTes.NumeroReferenciaFormaDePago = item.IdCartera; // BIGINT Id
															// Referencia para
															// FP (1,2,3,..)
															// debe ser unico
															// por FP aunque
															// sean de distintos
															// movimientos
				//FPagTes.IdMedioDePago = "CHE"; // NVARCHAR(3) Ej.: CHE, EFE, 
												// TRA, VVI, CIN
												
				if(formaPagoDefecto.size() > 0) {
					FPagTes.IdMedioDePago = formaPagoDefecto.get(0).IdMedioDePago;
					FPagTes.IdCuentaBancariaSocio = formaPagoDefecto.get(0).IdCuentaBancaria;
					FPagTes.IdTipoCuentaBancariaSocio = formaPagoDefecto.get(0).IdTipoCuentaBancaria;
					FPagTes.NombreTipoCuentaBancariaSocio = formaPagoDefecto.get(0).NombreTipoCuentaBancaria;
					FPagTes.IdBancoSocio = formaPagoDefecto.get(0).IdBancoCuentaBancaria;
					FPagTes.NumeroCuentaBancariaSocio = formaPagoDefecto.get(0).NumeroCuentaBancaria;
					
				}else {
					FPagTes.IdMedioDePago = "TRA";					
					FPagTes.IdCuentaBancariaSocio = 0L; // BIGINT
					FPagTes.IdTipoCuentaBancariaSocio = ""; // NVARCHAR(4)
					FPagTes.NombreTipoCuentaBancariaSocio = ""; // NVARCHAR(100)
					FPagTes.IdBancoSocio = 0L; // BIGINT
					FPagTes.NumeroCuentaBancariaSocio = ""; // NVARCHAR(100)
				}
				
				//FPagTes.IdModalidadDePago = "MODCAJ"; // NVARCHAR(6) Ej.:
				// MODCAJ: Por Caja
				// MODDEP: Deposito
				FPagTes.IdModalidadDePago = "MODDEP";				
				FPagTes.IdMoneda = aux.IdMoneda; // NVARCHAR(3)
				FPagTes.Monto = aux.Monto; // DECIMAL(26,8)
				
				FPagTes.IdTarjetaBancariaCorredora = 0L; // BIGINT
				FPagTes.IdCuentaBancariaCorredora = 0L; // BIGINT
				FPagTes.IdChequera = 0L; // BIGINT
				FPagTes.NumeroDocumentoCorredora = 0L; // BIGINT
				FPagTes.FechaDocumentoPagoCorredora = "0001-01-01 00:00:00"; // NVARCHAR(40)
								
				FPagTes.NombreCuentaBancariaSocio = ""; // NVARCHAR(100)				
				FPagTes.IdTarjetaBancariaSocio = 0L; // BIGINT
				FPagTes.IdTipoTarjetaBancariaSocio = ""; // NVARCHAR(40)
				FPagTes.NombreTipoTarjetaBancariaSocio = ""; // NVARCHAR(100)
				FPagTes.NumeroTarjetaBancariaSocio = "";
				FPagTes.NumeroSerieSocio = ""; // NVARCHAR(40)
				FPagTes.NumeroDocumentoSocio = 0L; // BIGINT
				//FPagTes.FechaDocumentoPagoSocio = Fecha.toFormat(Fecha.toDate(aux.FechaLiquidacion), "YYYY-MM-dd hh:mm:ss"); // NVARCHAR(40)
				FPagTes.FechaDocumentoPagoSocio = "0001-01-01 00:00:00";
				incie.p_FormaDePago.add(FPagTes);
			}
		}

		return incie;
	}
	
	private VencerOperacionMesaEntrada preparaDatosMesa(String simular,List<CustodiaVencer> lCV, Entrada entrada){//, String fechaSistemaAnterior){
		VencerOperacionMesaEntrada paramInSPRetiro = new VencerOperacionMesaEntrada();
		paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
		paramInSPRetiro.p_IdModulo			= entrada.p_IdModulo;
		paramInSPRetiro.p_IdUsuario			= entrada.p_IdUsuario;
		paramInSPRetiro.p_UsaReglasNegocio	= "S";
		paramInSPRetiro.p_FechaVencimiento	= entrada.p_Fecha;//fechaSistemaAnterior;
		//public List<Operacion> 		p_Operacion;
		
		List<Operacion> p_RetiroCartera = new ArrayList<Operacion>();
		for(CustodiaVencer item : lCV){
			Operacion ObjEntrada = new Operacion();
			ObjEntrada.IdInternoOperacion = new Long(lCV.indexOf(item));
			ObjEntrada.IdOperacion = item.IdOperacion;
			ObjEntrada.IdEstadoSiguiente = "LIQ";
			ObjEntrada.MontoOperacion=item.INGMontoCompraActualMonedaInstrumento;
			ObjEntrada.IdCondicionLiquidacion=item.IdCondicionLiquidacion;
			p_RetiroCartera.add(ObjEntrada);
		}
		paramInSPRetiro.p_Operacion = p_RetiroCartera;
		return paramInSPRetiro;
	}

	public CambiarEstadoOperacion.CambiarEstadoOperacionEntrada prepararDatosEstadoMesa(String simular,
			List<CustodiaVencer> lCV, List<CustodiaReserva> lRes, Entrada entrada) {
		CambiarEstadoOperacion.CambiarEstadoOperacionEntrada inEstadoMesa = new CambiarEstadoOperacion.CambiarEstadoOperacionEntrada(
				entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, simular, "S", entrada.p_IdPais);

		for (CustodiaVencer item : lCV) {
			for (CustodiaReserva itemRes : lRes) {
				if (item.IdOperacion.equals(itemRes.IdOperacion)) {
					CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN datos = new CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN();
					datos.IdOperacion = item.IdOperacion;
					datos.IdTipoProducto = item.IdTipoProducto;
					datos.IdNuevoEstado = "LIQ";
					datos.IdReserva = itemRes.IdReserva;
					datos.FolioCarteraCustodia = item.FolioCartera;
					datos.MontoOperacionAnticipo = new BigDecimal(0);
					datos.MontoOperacionInicialAnticipo = new BigDecimal(0);
					datos.MontoOperacionFinalAnticipo = new BigDecimal(0);
					datos.TasaAnticipo = new BigDecimal(0);
					datos.MontoOperacionMonedaAnticipo = new BigDecimal(0);
					datos.MontoOperacionInicialMonedaAnticipo = new BigDecimal(0);
					datos.MontoOperacionFinalMonedaAnticipo = new BigDecimal(0);
					datos.IdMonedaLiquidacionAnticipo = "";
					datos.IdCondicionLiquidacionAnticipo = "";
					datos.FechaCondicionLiquidacionAnticipo = new Date();

					inEstadoMesa.p_DataCambiarEstado.add(datos);
				}
			}
		}

		return inEstadoMesa;
	}

	
	private void procesarDetalleCustodia(String simular, List<ProcesamientoCustodia> procVC,
			RetiroCustodia.RetiroCustodiaSalida outgmc) {
		for (ProcesamientoCustodia itemCV : procVC) {
			if (!outgmc.P_OUT_ERRORESCONTADOR.equals(0L)) {
				for (MensajeSalida AdveError : outgmc.P_OUT_MENSAJES) {
					if (AdveError.tipo.equalsIgnoreCase("ERROR")) {
						ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
						// det.IdGrupoOperacion = AdveError.IdGrupoOperacion;
						det.Codigo = AdveError.codigo;
						det.Descripcion = AdveError.descripcion;

						if ("S".equals(simular)) {
							itemCV.SimulacionGmcErrores.add(det);
							itemCV.SimulacionGmcTieneErrores = "S";
						} else {
							itemCV.GmcErrores.add(det);
							itemCV.GmcTieneErrores = "S";
						}
					}

					if (AdveError.tipo.equalsIgnoreCase("ADVERTENCIA")) {
						ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
						// det.IdGrupoOperacion = itemAD.IdGrupoOperacion;
						det.Codigo = AdveError.codigo;
						det.Descripcion = AdveError.descripcion;
						if ("S".equals(simular)) {
							itemCV.SimulacionGmcAdvertencias.add(det);
							itemCV.SimulacionGmcTieneAdvertencias = "S";
						} else {
							itemCV.GmcAdvertencias.add(det);
							itemCV.GmcTieneAdvertencias = "S";
						}
					}
					//log.write(AdveError);
				}
				;
			}
		}
	}

	public void procesarDetalleCuentaInversion(String simular, List<ProcesamientoCustodia> procVC,
			CrearCargoAbono.CrearCargoAbonoSalida outcca) {
		for (ProcesamientoCustodia itemCV : procVC) {
			if (!outcca.P_OUT_ERRORESCONTADOR.equals(0L)) {
				for (MensajeSalida AdveError : outcca.P_OUT_MENSAJES) {
					if (AdveError.tipo.equalsIgnoreCase("ERROR")) {
						ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
						// det.IdGrupoOperacion = AdveError.IdGrupoOperacion;
						det.Codigo = AdveError.codigo;
						det.Descripcion = AdveError.descripcion;

						if ("S".equals(simular)) {
							itemCV.SimulacionCcaErrores.add(det);
							itemCV.SimulacionCcaTieneErrores = "S";
						} else {
							itemCV.CcaErrores.add(det);
							itemCV.CcaTieneErrores = "S";
						}
					}

					if (AdveError.tipo.equalsIgnoreCase("ADVERTENCIA")) {
						ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
						// det.IdGrupoOperacion = itemAD.IdGrupoOperacion;
						det.Codigo = AdveError.codigo;
						det.Descripcion = AdveError.descripcion;
						if ("S".equals(simular)) {
							itemCV.SimulacionCcaAdvertencias.add(det);
							itemCV.SimulacionCcaTieneAdvertencias = "S";
						} else {
							itemCV.CcaErrores.add(det);
							itemCV.CcaTieneErrores = "S";
						}
					}
					//log.write(AdveError);
				}
			}
		}
	}


	public void procesarDetalleTesoreria(String simular, List<ProcesamientoCustodia> procVC, CrearIngresoEgreso_Salida outcie) {
		
		for (ProcesamientoCustodia itemCV : procVC) 
		{
			for (MensajeSalida itemED : outcie.P_OUT_MENSAJES) 
			{
				if (itemED.id.equals(itemCV.IdCartera) && itemED.tipo.equalsIgnoreCase("ERROR")) 
				{
					ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
					det.Codigo = itemED.codigo;
					det.Descripcion = itemED.descripcion;
					if ("S".equals(simular)) 
					{
						itemCV.SimulacionCieErrores.add(det);
						itemCV.SimulacionCieTieneErrores = "S";
					} 
					else 
					{
						itemCV.CieErrores.add(det);
						itemCV.CieTieneErrores = "S";
					}
				}
			}
			
			for (MensajeSalida itemAD : outcie.P_OUT_MENSAJES) 
			{
				if (itemAD.id.equals(itemCV.IdCartera) && itemAD.tipo.equalsIgnoreCase("ADVERTENCIA")) 
				{
					ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
					det.Codigo = itemAD.codigo;
					det.Descripcion = itemAD.descripcion;
					if ("S".equals(simular)) 
					{
						itemCV.SimulacionCieAdvertencias.add(det);
						itemCV.SimulacionCieTieneAdvertencias = "S";
					} 
					else 
					{
						itemCV.CieAdvertencias.add(det);
						itemCV.CieTieneAdvertencias = "S";
					}
				}
			}
		}
	}
	
	
	public void procesarDetalleCambiarEstadoOperacion(String simular, List<ProcesamientoCustodia> procVC,
			CambiarEstadoOperacion.CambiarEstadoOperacionSalida outcest) {
		for (ProcesamientoCustodia itemCV : procVC) {
			if (outcest.P_OUT_ERRORESCONTADOR > 0) {
				for (PlataformaUtils.MensajeSalida item : outcest.P_OUT_MENSAJES) {
					if (!"OK".equals(item.tipo) && item.id.equals(itemCV.IdOperacion)) {
						ProcesamientoCustodia.Detalle det = new ProcesamientoCustodia.Detalle();
						det.Codigo = item.codigo;
						det.Descripcion = item.descripcion;
						if ("S".equals(simular)) {
							itemCV.SimulacionCestErrores.add(det);
							itemCV.SimulacionCestTieneErrores = "S";
						} else {
							itemCV.CestErrores.add(det);
							itemCV.CestTieneErrores = "S";
						}
					}
				}
			}
		}
	}
	
	private Salida actualizarMensajesErroresYContador(List<MensajeSalida> mensajesSalidaServicio, Salida salidaVencimiento) {
		
		for(MensajeSalida mensaje : mensajesSalidaServicio) {
			if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
				salidaVencimiento.P_OUT_MENSAJES.add(mensaje);
				salidaVencimiento.P_OUT_ERRORESCONTADOR++;
			}
		}
		
		return salidaVencimiento;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa2", "connections.properties"))  {	
		//try (Connection con = PlataformaUtils.getQAConnection()) {			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9497,\"p_IdModulo\":\"PLA\",\"p_SimularOperacion\":\"N\",\"p_IdTipoVencimiento\":\"IF\",\"p_Fecha\":\"2026-06-19\",\"p_IdPais\":44}";
			Entrada Objin = gson.fromJson(json, Entrada.class);
			EjecutarVencimiento ven = new EjecutarVencimiento();
			try {
				Salida resp = ven.logica(con, Objin);
				if(resp.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				} else {
					con.rollback();
				
				}
				con.close();
				System.out.println(resp);
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
			System.out.println("== END ==");
		}
	}
}