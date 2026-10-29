package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.ControlRetiroCustodia;
import cl.bcs.custodia.utils.ControlRetiroCustodia.ControlRetiroCustodiaEntrada;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito;
import cl.bcs.custodia.utils.InformacionConceptosCustodia;
import cl.bcs.custodia.utils.InformacionConceptosCustodia.ObjDatosSalida;
import cl.bcs.custodia.utils.InformacionConfiguracionCustodia;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.facturacion.CrearComprobante.Comprobante_Proceso;
import cl.bcs.facturacion.CrearComprobante.InfoMovimientoComprobante_Proceso;
import cl.bcs.facturacion.CrearComprobante.MovimientoComprobanteDetalle_Proceso;
import cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Proceso;
import cl.bcs.facturacion.CrearComprobante.SelectCondicionDeLiquidacion;
import cl.bcs.facturacion.CrearComprobante.SelectTipoPrestamo;
import cl.bcs.facturacion.ReversarFacturacionCustodia.ReversarFacturacionCustodia_Entrada;
import cl.bcs.facturacion.ReversarFacturacionCustodia.ReversarFacturacionCustodia_Salida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class ProcesarFacturacionCustodia implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ProcesarFacturacionCustodia.class);
	private final String SQL_CONSULTAR_CONTRAPARTE = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarContraparte.sql");
	private final String SQL_CONSULTAR_CONCEPTOS = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarConcepto.sql");
	private final String SQL_PRESTAMO_VENTA_CORTA = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarPrestamoVentaCorta.sql");
	private final String SQL_CONSULTAR_SUBYACENTE = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarSubyacente.sql");
	private final String SQL_CONSULTAR_GARANTIA   =  loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarGarantia.sql");
	private final String SQL_CONSULTAR_GARANTIA_ANTICIPO_PARAMETRO_ENCENDIDO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarGarantiaAnticipoParametroEncendido.sql");
	private final String SQL_CONSULTAR_CONCEPTOSCLNOMINAL = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarConceptoCLNominalCertificaAuto.sql");
	
	private final String SQL_CONSULTAR_CARTERA_RELACIONADA   =  loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarCarteraRelacionada.sql");
	
	
	private final String SQL_CONSULTAR_OSA_GARANTIA   =  loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarOSAGarantia.sql");
	private final String SQL_CONSULTAR_INDICADOR_PORTAFOLIO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarIndicadorPortafolio.sql");
	
	private final String SQL_SELECT_CONDICION_DE_LIQUIDACION	= loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectCondicionDeLiquidacion.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");
	private final String SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA = loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarContraparteSimultanea.sql");

	private final String SQL_SELECT_FOLIOCARTERARELACIONADO =  loadResourceToString("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarFolioCarteraRelacionado.sql");

	private List<LogProcesoPrestamoBlockChain> logProcesoCrearEmprestitoBlockChain = new ArrayList<>();
	public HashMap<String,ConfiguracionMovimientoCustodia> hashTipoOperacionConfiguracion = new HashMap<>();
	
	private static final String MERCADO_RENTA_FIJA = "RF";
	
	public static class ConceptoGarantia {
		public String IdConcepto;
		public String NombreConcepto;
		public String IdTipoOperacion;
		public String Uso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Subyacente {
		public Long IdSubyacente;
	    public Long FolioSubyacente;
	    public Long FolioCarteraRelacionada; 
	    public String IdEstado;
	    public BigDecimal Cantidad;
	    public BigDecimal ValorRescate;	    
	    public Long IdInstrumento;
	    public BigDecimal PrecioCompraOperacionRelacionada;    
	    public BigDecimal TasaCompraOperacionRelacionada;	    
	    public Long IdCarteraOrigen; 
	    public String IdTipoOperacionOrigen;
	    
	    public String IdTipoCanalIngreso;
	    public String IdEmisor;
	    
	    public Long IdCarteraRetirada;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Garantia {
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long FolioCarteraOrigen;
		public Long IdCarteraOrigen;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoATasaGarantiaActual;
		public BigDecimal PrecioCompraOperacionOrigen;
		public BigDecimal TasaCompraOperacionOrigen;	
		public String IdTipoOperacionOrigen;    
	    public Long IdCliente; 
	    public Long IdPortafolio; 
	    public Long IdInstrumento;
	    public String IdEmisor;
	    public String IdOrigenGarantia;
	    public String IdConcepto;
	    public String IdMonedaInstrumento;
		public String IdBoveda;
		public String IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera {

		public Long 		IdCartera;
		public Long 		FolioCartera;
		public Long 		FolioCarteraRelacionada;	   
		public BigDecimal 	INGCantidad;
		public BigDecimal 	INGValorRescate;
		public String 		IdTipoOperacion;
	    public Long 		IdCliente;
	    public Long 		IdPortafolio;
	    public Long 		IdInstrumento;
	    public String 		IdEmisor;	  
	    public String 		IdConcepto;
	    public String 		IdBoveda;
	    public String 		IdTipoCanalIngreso;  
	    public String 		IdMonedaInstrumento;
	    
	    public Long 		IdSubyacente;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ConfiguracionMovimientoCustodia {
		public String IdTipoOperacion;
		public String IdAccionOperacion;
		public String PoseeInstrumentoSubyacente;
		public String PoseeInstrumentoRelacionado;
		public String CertificaEn;
	}
	
	
	public static class EntradaNeteo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<ClienteNeteo> p_PortafoliosInstrumento;
	}
	public static class ClienteNeteo {
		public Long IdPortafolio;
		public Long IdInstrumento;
	}
	
	public static class Indicador {
		public Long IdPortafolio;
		public String PermiteVentaDescubierta;
		public String SobregiroRV;
		public String SobregiroRF;
		public String SobregiroIF;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContraparteGarantia_Proceso {
		public Long IdContraparteGarantia;
		public String RazonSocialContraparteGarantia;
		

		public String IdEntidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContraparteSimultanea {
		public Long IdPortafolioDefecto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConceptoClNominalCertificaAuto {
		public String IdConcepto ;
		public String IdCondicionDeLiquidacion ;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Paso {
		
		public String codigo;		//CODIGO PASO, DENTRO DE CADA NEGOCIO, CON ESTO SABEMOS QUE HACER.
		public String estado;		//OK, ERROR
		public List<Object> movimiento;	//AQUI VA EL MOVIMIENTO A CUSTODIA
		public GrupoOperacionCustodia grupo;			//REFERENCIA AL GRUPO
		public List<Object> resultado;
		public List<MensajeSalida> mensajes;
		
		public Paso(String codigo, Object objEntrada, GrupoOperacionCustodia g) {
			this.codigo = codigo;
			this.estado = "PEN";
			if (this.movimiento == null) {
				this.movimiento = new ArrayList<>();
			}
			this.movimiento.add(objEntrada);
			this.grupo = g;
			this.mensajes = new ArrayList<>();
			this.resultado = new ArrayList<>();
		}
		
		public Paso(String codigo, List<Object> objsEntrada, GrupoOperacionCustodia g) {
			this.codigo = codigo;
			this.estado = "PEN";
			if (this.movimiento == null) {
				this.movimiento = new ArrayList<>();
			}
			this.movimiento.addAll(objsEntrada);
			this.grupo = g;
			this.mensajes = new ArrayList<>();
			this.resultado = new ArrayList<>();
		}
		
	}
	
	public static class GrupoOperacionCustodia {
		
		public Long IdMovimientoInicial;		//puede ser un transito o un movimiento comprobante detalle
		public InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia Configuracion;
		public GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito TransitoPrincipal;
		public GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito TransitoRelacionado;
		public Paso	pasoPorEjecutar;		//primer paso pendiente de ejecutar, puntero que va avanzando
		public List<Paso> pasos; //DEBEN SER INGRESADOS EN ORDEN DE EJECUCION
		public String estado;
		public List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> transitosActualizar;
		public List<RetiroCustodia.ObjDatosEntrada> retirosFin;	
		
		
		public GrupoOperacionCustodia(Long idMovimientoInicial,InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia conf, ObjMovimientosEnTransito mov,ObjMovimientosEnTransito objMovimientosEnTransitoRelacionado) {
			this.pasos = new ArrayList<>();
			this.transitosActualizar = new ArrayList<>();
			this.Configuracion = conf;
			this.TransitoPrincipal = mov;
			this.TransitoRelacionado = objMovimientosEnTransitoRelacionado;
			this.estado = "PEN";
			this.IdMovimientoInicial = idMovimientoInicial;
		}
		
	}
	
	public static class ProcesarFacturacionCustodia_Entrada {
		public Long											p_IdEmpresa;
	    public Long											p_IdUsuario;
	    public String										p_IdModulo;
	    public String										p_simularOperacion;
		public String										p_UsaReglasNegocio;
		public List<Long>									p_MovimientosEnTransito;
		public List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> 	p_Movimientos;
		public List<CrearComprobante.Comprobante_Proceso> 	p_Comprobantes;
		public List<Comprobante_Proceso> 					p_NotasCredito;
		
		public ProcesarFacturacionCustodia_Entrada() {
			p_Movimientos = new ArrayList<>();
			p_MovimientosEnTransito = new ArrayList<>();
			p_NotasCredito = new ArrayList<>();
			p_Comprobantes = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCustodiaSalida {
		public Long IdMovimiento;	//PUEDE SER IdMovimientoTransito O IdMovimientoComprobanteDetalle
		public String TablaOrigen;
		public Long FolioCustodia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarFacturacionCustodia_Salida {
		
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MovimientoCustodiaSalida> P_OUT_MOVIMIENTOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public List<RetiroCustodia.ObjDatosEntrada> retirosFin;	
		
		public ProcesarFacturacionCustodia_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();	
			P_OUT_MOVIMIENTOS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LogProcesoPrestamoBlockChain extends BcsPojo {
		public Long 	IdEmpresa;
		public Long 	FolioCartera;
		public String 	FechaProceso;
		public String 	Descripcion;
		public String 	OrigenProcesoClase;
		public String 	OrigenProcesoMetodo;
		public String 	CreadorParametro;
		
		public LogProcesoPrestamoBlockChain(Long idEmpresa, Long folioCartera, String fechaProceso, String descripcion, String origenProcesoClase, String origenProcesoMetodo, String creadoPor) {
			this.IdEmpresa 				= idEmpresa;
			this.FolioCartera 			= folioCartera;
			this.FechaProceso 			= fechaProceso;
			this.Descripcion 			= descripcion;
			this.OrigenProcesoClase 	= origenProcesoClase;
			this.OrigenProcesoMetodo 	= origenProcesoMetodo;
			this.CreadorParametro 		= creadoPor;
		}
	}
	public  List<Object> movsCabecera;
	
	public static class BuscaCartera {
		public Long IdCartera;
		public Long FolioCartera;	
		public Long IdMovimientoCarteraEnTransito;

	}
	public static class BuscaIdComprobante {
		public Long IdMovimientoComprobante;
		public Long IdOrden;	
		public Long IdTransaccion;
		public Long IdAsignacion;
		public Long IdAsignacionRef;
		public String NemotecnicoInstrumento;
		public String NemotecnicoSubyacente;

	}
	
	
	@Trace
	public ProcesarFacturacionCustodia_Salida logica(Connection conn,ProcesarFacturacionCustodia_Entrada entrada) throws Exception {
		ProcesarFacturacionCustodia_Salida salida = new ProcesarFacturacionCustodia_Salida();
		logger.info("INICIO");
		List<IngresoCustodia.ObjDatosEntrada> transitos = new ArrayList<>();
		List<Long> transitosPorCertificar = new ArrayList<>();
		HashMap<Long,Long> hashMovimientosIngreso_MovimientosTransito = new HashMap<>();
		HashMap<Long,Long> hashSecuenciaTransitos = new HashMap<>();
		
		if (entrada.p_NotasCredito.size() > 0){
			ReversarFacturacionCustodia reversarFacturacionCustodiaService = new ReversarFacturacionCustodia();
			ReversarFacturacionCustodia_Entrada entradaReversarFacturacionCustodia = new ReversarFacturacionCustodia_Entrada();
			entradaReversarFacturacionCustodia.p_Comprobantes = entrada.p_NotasCredito.stream().map(t -> t.IdComprobanteReferencia).collect(Collectors.toList());
			entradaReversarFacturacionCustodia.p_NotasCreditos = entrada.p_NotasCredito;
			entradaReversarFacturacionCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaReversarFacturacionCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaReversarFacturacionCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaReversarFacturacionCustodia.p_simularOperacion = entrada.p_simularOperacion;
			entradaReversarFacturacionCustodia.p_ValorizacionAutomatica = "N";
			ReversarFacturacionCustodia_Salida salidaReversarFacturacionCustodia = reversarFacturacionCustodiaService.logica(conn, entradaReversarFacturacionCustodia);
			salida.P_OUT_MENSAJES.addAll(salidaReversarFacturacionCustodia.P_OUT_MENSAJES);
		}
		
		List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> _configuracion = InformacionConfiguracionCustodia.informacionConfiguracion(conn, entrada.p_IdEmpresa);
		HashMap<String,Object> ConfigCUS = Utils.List2Hash(_configuracion, "IdTipoOperacion");
 		List<InformacionConceptosCustodia.ObjDatosEntrada> IdConceptos = new ArrayList<InformacionConceptosCustodia.ObjDatosEntrada>();
 		
 		
		
		//CREA TRANSITOS
		//TODO CASO COMPRA O VENTA NOMINAL, DEBE SER OPERACION CONTRARIA
		if (!entrada.p_Movimientos.isEmpty()) {
			
			List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> movimientoProcesados =  procesarMovimientoVentaCorta (conn,entrada.p_Movimientos,entrada.p_Comprobantes, entrada.p_IdEmpresa, entrada.p_IdModulo, entrada.p_IdUsuario );
		
			for (CrearComprobante.MovimientoComprobanteDetalle_Proceso mov : movimientoProcesados) {
				
				InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia conf = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(mov.IdTipoOperacion);				
				if (conf == null) {
					throw new Exception("operacion no configuracion en custodia :" + mov.IdTipoOperacion);
				}
				
				//transito para compras ventas normales y cabecera de compras ventas a plazo
				{
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= mov.IdMovimientoComprobanteDetalle;
					ObjEntrada.TablaAfectada					= "TRANSITO";
					ObjEntrada.AccionOperacion					= mov.IdFormaCustodia.equalsIgnoreCase("CUS")? "INGRESO" : "RETIRO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.FolioCarteraOrigen				= mov.FolioCartera;		//este es para vender?	
					ObjEntrada.FolioCustodia                    = mov.FolioCartera;
					ObjEntrada.IdCliente						= mov.IdCliente;
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= Fecha.toDate(Fecha.toFormat(mov.FechaComprobante,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoOperacion		= Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoInstrumento		= "S".equals(mov.EsVentaCorta) && mov.IdClasificacionTipoInstrumento.equals(MERCADO_RENTA_FIJA) ? mov.FechaVencimientoInstrumento : Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.PlazoEnDiasOperacion				= mov.DiasPlazo.longValue();
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					ObjEntrada.IdConcepto						= mov.IdConceptoMovimiento;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionDeLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS;
					ObjEntrada.Nemotecnico						= mov.NemotecnicoInstrumento;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.IdComprobante;
					ObjEntrada.IdMovimientoComprobante			= mov.IdMovimientoComprobante;
					ObjEntrada.FolioExternoReferencia			= mov.FolioTransaccion;
					ObjEntrada.IdOperacion						= mov.IdOperacion;
					ObjEntrada.IdBolsa							= mov.IdBolsa;	
					ObjEntrada.IdUsuario						= entrada.p_IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursalEmpleadoEjecutivoPortafolio; 
					ObjEntrada.IdAgente							= mov.IdEmpleadoEjecutivoPortafolio;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumento;
					ObjEntrada.IdInstrumentoAnterior			= mov.IdInstrumentoRelacionado; //IdInstrumentoAnterior
					ObjEntrada.IdMonedaInstrumento				= mov.IdMoneda;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.IdFormaCustodia.equalsIgnoreCase("CUS")? "N":"S";			
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioRaiz; 
					ObjEntrada.TasaCompra						= "S".equals(mov.EsVentaCorta) ? mov.TasaOperacion : (mov.Tasa != null? mov.Tasa : mov.TasaOperacion);
					ObjEntrada.MontoCompra						= mov.MontoMovimientoComprobanteDetalle;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoRaiz;
					ObjEntrada.EsVentaCorta						= mov.EsVentaCorta;
					ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.Precio, MathContext.DECIMAL128);
					ObjEntrada.IdMesa							= mov.IdMesa;
					ObjEntrada.PrecioAPlazo						= "S".equals(mov.EsVentaCorta) ? mov.PrecioAPlazo : BigDecimal.ZERO;
					ObjEntrada.MontoAPlazo						= "S".equals(mov.EsVentaCorta) ? mov.MontoAPlazo : BigDecimal.ZERO;
					ObjEntrada.TasaTIR							= mov.Tasa;
					
					InformacionConceptosCustodia.ObjDatosEntrada concepto = new InformacionConceptosCustodia.ObjDatosEntrada();
					concepto.IdConcepto = mov.IdConceptoMovimiento;
					concepto.IdNombreEntidad = "CUS.INGRESORETIROTITULO";
					IdConceptos.add(concepto);
					transitos.add(ObjEntrada);
				}
				
				
				//REPORTES Y SIMULTANEAS
				if (conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("S")) {
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					
					ObjEntrada.IdGenerarMovimientoCustodia		= -1*mov.IdMovimientoComprobanteDetalle;
					ObjEntrada.IdMovimientoCarteraEnTransitoRelacionado = mov.IdMovimientoComprobanteDetalle;
					ObjEntrada.PermiteCertificacionPorUsuario 	= "N";
					ObjEntrada.TablaAfectada					= "TRANSITO";
					ObjEntrada.AccionOperacion					= "INGRESO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.FolioCarteraOrigen				= mov.FolioCartera;		
					ObjEntrada.FolioCarteraRelacionada          = mov.FolioCartera;
					ObjEntrada.IdCliente						= mov.IdCliente; 
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= Fecha.toDate(Fecha.toFormat(mov.FechaComprobante,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoOperacion		= Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoInstrumento		= Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.PlazoEnDiasOperacion				= mov.DiasPlazo.longValue();
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					ObjEntrada.IdConcepto						= mov.IdConceptoMovimiento;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionDeLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS==null || mov.NemoSVS.equalsIgnoreCase("")?mov.NemotecnicoSubyacente:mov.NemoSVS;
					ObjEntrada.Nemotecnico						= mov.NemotecnicoSubyacente;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.IdComprobante; 
					ObjEntrada.FolioExternoReferencia			= mov.FolioTransaccion;
					ObjEntrada.IdBolsa							= mov.IdBolsa;
					ObjEntrada.IdMovimientoComprobante			= mov.IdMovimientoComprobante;
					if (mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
						ObjEntrada.IdOperacion						= mov.IdOrden;
					}
					else {
						ObjEntrada.IdOperacion						= mov.IdMovimientoComprobante;	
					}
					ObjEntrada.IdUsuario						= entrada.p_IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursalEmpleadoEjecutivoPortafolio; 
					ObjEntrada.IdAgente							= mov.IdEmpleadoEjecutivoPortafolio;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumentoSubyacente;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaSubyacente;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= Arrays.asList("RRV","RRF","SIM").contains(mov.IdTipoInstrumento) ? mov.IdEmisorSubyacente: mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.IdFormaCustodia.equalsIgnoreCase("CUS")? "N":"S";			
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.Precio; 
					ObjEntrada.TasaCompra						= mov.Tasa != null? mov.Tasa : mov.TasaOperacion;
					ObjEntrada.MontoCompra						= mov.MontoMovimientoComprobanteDetalle;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.Monto;
					ObjEntrada.FolioCustodia                    = mov.FolioCartera;
					ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.Precio, MathContext.DECIMAL128);
					ObjEntrada.IdMesa							= mov.IdMesa;
					
					InformacionConceptosCustodia.ObjDatosEntrada concepto = new InformacionConceptosCustodia.ObjDatosEntrada();
					concepto.IdConcepto = mov.IdConceptoMovimiento;
					concepto.IdNombreEntidad = "CUS.INGRESORETIROTITULO";
					IdConceptos.add(concepto);
					transitos.add(ObjEntrada);
				}	
				
				// nuevo transito de simultaneas 
			   if ( mov.IdConceptoMovimiento.equalsIgnoreCase("CMSIMU")||mov.IdConceptoMovimiento.equalsIgnoreCase("VNSIMU"))
			    {	  
				   
				   	long ComprobanteDetalleId = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ProcesarFacturaCustodia",1L).get(0);
				   	
				   	hashSecuenciaTransitos.put( ComprobanteDetalleId,mov.IdMovimientoComprobanteDetalle) ;
				   	
				   	IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				    ObjEntrada.IdGenerarMovimientoCustodia = ComprobanteDetalleId;
					ObjEntrada.IdMovimientoCarteraEnTransitoRelacionado = mov.IdMovimientoComprobanteDetalle;
					ObjEntrada.TablaAfectada					= "TRANSITO";
					ObjEntrada.AccionOperacion					= mov.IdFormaCustodia.equalsIgnoreCase("CUS")? "INGRESO" : "RETIRO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.FolioCarteraOrigen				= mov.FolioCartera;	
					ObjEntrada.FolioCarteraRelacionada          = mov.FolioCartera;
					ObjEntrada.FolioCustodia                    = mov.FolioCartera;
					ObjEntrada.IdCliente						= mov.IdCliente;
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= Fecha.toDate(Fecha.toFormat(mov.FechaComprobante,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoOperacion		= Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.FechaVencimientoInstrumento		= Fecha.toDate(Fecha.toFormat(mov.FechaVencimiento,"yyyy-MM-dd"));
					ObjEntrada.PlazoEnDiasOperacion				= mov.DiasPlazo.longValue();
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					ObjEntrada.IdConcepto						= mov.IdConceptoMovimiento;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionDeLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS==null || mov.NemoSVS.equalsIgnoreCase("")?mov.NemotecnicoSubyacente:mov.NemoSVS;
					ObjEntrada.Nemotecnico						= mov.NemotecnicoSubyacente;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.IdComprobante;
					ObjEntrada.IdMovimientoComprobante			= mov.IdMovimientoComprobante;
					ObjEntrada.FolioExternoReferencia			= mov.FolioTransaccion;
					ObjEntrada.IdBolsa							= mov.IdBolsa;
					if (mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
						ObjEntrada.IdOperacion						= mov.IdOrden;
					}
					else {
						ObjEntrada.IdOperacion						= mov.IdMovimientoComprobante;	
					}
					ObjEntrada.IdUsuario						= entrada.p_IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursalEmpleadoEjecutivoPortafolio; 
					ObjEntrada.IdAgente							= mov.IdEmpleadoEjecutivoPortafolio;
					ObjEntrada.Glosa							= "TRANSITO-CARTERA"; //mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumentoSubyacente;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaSubyacente;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= Arrays.asList("RRV","RRF","SIM").contains(mov.IdTipoInstrumento) ? mov.IdEmisorSubyacente: mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.IdFormaCustodia.equalsIgnoreCase("CUS")? "N":"S";			
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioRaiz; 
					ObjEntrada.TasaCompra						= "S".equals(mov.EsVentaCorta) ? mov.TasaOperacion : (mov.Tasa != null? mov.Tasa : mov.TasaOperacion);
					ObjEntrada.MontoCompra						= mov.MontoMovimientoComprobanteDetalle;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoRaiz;
                    ObjEntrada.FolioCustodia                    = mov.FolioCartera;
					ObjEntrada.EsVentaCorta						= mov.EsVentaCorta;
					ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.Precio, MathContext.DECIMAL128);
					ObjEntrada.IdMesa							= mov.IdMesa;
					ObjEntrada.PrecioAPlazo						= "S".equals(mov.EsVentaCorta) ? mov.PrecioAPlazo : BigDecimal.ZERO;
					ObjEntrada.MontoAPlazo						= "S".equals(mov.EsVentaCorta) ? mov.MontoAPlazo : BigDecimal.ZERO;
					ObjEntrada.TasaTIR							= mov.Tasa;
					
					InformacionConceptosCustodia.ObjDatosEntrada concepto = new InformacionConceptosCustodia.ObjDatosEntrada();
					concepto.IdConcepto = mov.IdConceptoMovimiento ;
					concepto.IdNombreEntidad = "CUS.INGRESORETIROTITULO";
					IdConceptos.add(concepto);
					transitos.add(ObjEntrada);
				}			
				
			}
	
			
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa				= entrada.p_IdEmpresa;		
			paramInSP.p_IdModulo				= entrada.p_IdModulo;
			paramInSP.p_IdUsuario				= entrada.p_IdUsuario;
			paramInSP.p_SimularOperacion		= entrada.p_simularOperacion;
			paramInSP.p_TicketAtencion 			= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada			= transitos;
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP);		//1***		
	
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresos.P_OUT_DATOSSALIDA) {
				for (MovimientoComprobanteDetalle_Proceso mov : entrada.p_Movimientos) {
					if (reg.IdGenerarMovimientoCustodia.equals(mov.IdMovimientoComprobanteDetalle)) {
						
						if (reg.RegistroConProblemas.equalsIgnoreCase("S")) {
							String codigo = "UI_ERROR_AL_PROCESAR_MOVIMIENTO"; //Error al procesar movimiento ::IdMovimientoComprobante::
							String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("IdMovimientoComprobante", mov.movimientoComprobanteProceso.IdMovimientoComprobante).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobanteDetalle,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
							
						}
						else {
							hashMovimientosIngreso_MovimientosTransito.put(reg.IdCustodia,mov.IdMovimientoComprobanteDetalle);
							transitosPorCertificar.add(reg.IdCustodia);	
						}
						break;
					}
					
					if ( mov.IdConceptoMovimiento.equalsIgnoreCase("CMSIMU")||mov.IdConceptoMovimiento.equalsIgnoreCase("VNSIMU")) {					    
						for (Map.Entry<Long, Long> SecuenciaID : hashSecuenciaTransitos.entrySet()) {						
							if ((SecuenciaID.getKey().equals(reg.IdGenerarMovimientoCustodia)) && (!reg.IdGenerarMovimientoCustodia.equals(mov.IdMovimientoComprobanteDetalle))  ) {							
								hashMovimientosIngreso_MovimientosTransito.put(reg.IdCustodia,SecuenciaID.getValue());
								transitosPorCertificar.add(reg.IdCustodia);	
							}
						}
					}
				}
				
			}
		}
		else {
			transitosPorCertificar.addAll(entrada.p_MovimientosEnTransito);
			for (long l : entrada.p_MovimientosEnTransito) {
				hashMovimientosIngreso_MovimientosTransito.put(l,l);
			}
		}		
		
		//PROCESA LOS TRANSITOS
		IdConceptos.clear();
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(conn, transitosPorCertificar);
		List<ObjMovimientosAActualizar> transitosOK = new ArrayList<>();
		{
			List<String> idsTipoOperacion = new ArrayList<>();
			for (ObjMovimientosEnTransito mov : MovimientosEnTransito ) {

				if (!idsTipoOperacion.contains(mov.IdTipoOperacion)) {
					idsTipoOperacion.add(mov.IdTipoOperacion);
				}
		
				if (mov.IdMovimientoCarteraEnTransitoRelacionado > 0L && !"N".equalsIgnoreCase(mov.PermiteCertificacionPorUsuario)  && ("COMSIM".equalsIgnoreCase(mov.IdTipoOperacion)||"VENSIM".equalsIgnoreCase(mov.IdTipoOperacion)))		 {
					mov.esEnTransito= "S";	
				}
			
				InformacionConceptosCustodia.ObjDatosEntrada concepto = new InformacionConceptosCustodia.ObjDatosEntrada();
				
				concepto.IdConcepto = mov.IdConcepto;
				concepto.IdNombreEntidad =!"DIACGS".equalsIgnoreCase(mov.IdConcepto)?"CUS.INGRESORETIROTITULO":"CUS.GARANTIA";
				IdConceptos.add(concepto);
			}
		}
		
		
		List<InformacionConceptosCustodia.ObjDatosSalida> _conceptos = InformacionConceptosCustodia.informacionConcepto(conn, IdConceptos);
		HashMap<String,Object> conceptos = Utils.List2Hash(_conceptos, "IdConcepto");
		SQL sql = new SQL();
		List<ConceptoClNominalCertificaAuto>NomimalConceptoClCertificaAuto = new ArrayList<>();
		try{ NomimalConceptoClCertificaAuto = sql.query(conn, SQL_CONSULTAR_CONCEPTOSCLNOMINAL, new HashMap<>(), ConceptoClNominalCertificaAuto.class);}catch(Exception e) {};
		String NominalCertificaAutomatico = "N";
		try {NominalCertificaAutomatico = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "NominalCertificaAutomaticoTransaccion");}catch(Exception e) {};
		
		
		if (MovimientosEnTransito.size() > 0) {
			HashMap<Long,Boolean>ActualizacionSinRelacionado = new HashMap<>();
			//IdTransitoOriginal, TransitoAsociado
			HashMap<Long,GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> transitosRelacionados = new HashMap<>();
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito mov : MovimientosEnTransito) {
				if (mov.IdMovimientoCarteraEnTransitoRelacionado.equals(0L) || "S".equalsIgnoreCase( mov.esEnTransito)) {
					continue;
				}
				transitosRelacionados.put(mov.IdMovimientoCarteraEnTransitoRelacionado, mov);
			}
			
			List<GrupoOperacionCustodia> comprasConCustodia = new ArrayList<>();
			List<GrupoOperacionCustodia> ventasConCustodia = new ArrayList<>();
			List<GrupoOperacionCustodia> comprasReporteNominales = new ArrayList<>();
			List<GrupoOperacionCustodia> comprasSimultaneas = new ArrayList<>();
			List<GrupoOperacionCustodia> ventasReporteSimultaneas = new ArrayList<>();
			List<GrupoOperacionCustodia> suscripcionOsaCustodia = new ArrayList<>();
			List<GrupoOperacionCustodia> ventasCortasNominal = new ArrayList<>();
			List<GrupoOperacionCustodia> dividendosGarantias = new ArrayList<>();
			List<GrupoOperacionCustodia> anticipoSimultaneaVenta = new ArrayList<>();						
			List<GrupoOperacionCustodia> anticipoSimultaneaCompra = new ArrayList<>();	
			List<GrupoOperacionCustodia> compraSimultaneaTransito = new ArrayList<>();									
			
			
		
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito mov : MovimientosEnTransito) {
				
				InformacionConceptosCustodia.ObjDatosSalida concepto = (ObjDatosSalida) conceptos.get(mov.IdConcepto);
				InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia conf = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(mov.IdTipoOperacion);				
				boolean ConceptoNominalCertifica = NomimalConceptoClCertificaAuto.stream().anyMatch(c -> mov.IdConcepto.equals(c.IdConcepto) && mov.IdCondicionLiquidacion.equals(c.IdCondicionDeLiquidacion));

				if (conf == null) {
					String codigo = "UI_ERROR_OPERACION_NO_CONFIGURADA";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("Operacion", mov.IdTipoOperacion).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));					
					//throw new Exception("operacion no configuracion en custodia :" + mov.IdTipoOperacion);
				}
				if (concepto == null) {
					
					String codigo = "UI_ERROR_CONCEPTO_NO_CONFIGURADO";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("Concepto", mov.IdConcepto).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));

					//throw new Exception("Concepto " + mov.IdConcepto + " no configurado");
					//POR AHORA QUE SE CAIGA
				}
				if (!mov.IdMovimientoCarteraEnTransitoRelacionado.equals(0L) && !"S".equalsIgnoreCase(mov.esEnTransito)) {
					//solo proceso los movimientos principales
					continue;
				}
				
				// No aplica para Anticipo de Simultaneas ni Ventas Cortas
				if (!mov.IdTipoOperacion.equalsIgnoreCase("ANSIMV") && !mov.IdTipoOperacion.equalsIgnoreCase("ANSIMC") && !mov.EsVentaCorta.equalsIgnoreCase("S") )
				{

					if (mov.EsNominal.equalsIgnoreCase("S") && !entrada.p_MovimientosEnTransito.contains(mov.IdMovimientoCarteraEnTransito) &&( !NominalCertificaAutomatico.equalsIgnoreCase("S")  || !ConceptoNominalCertifica  || !concepto.ConCertificadoAutomatico.equalsIgnoreCase("S"))){
						//SI EL CONCEPTO DEL MOVIMIENTO ES POR CERTIFICAR, QUEDAMOS HASTA AQUI CON EL MOVIMIENTO
						//CREARMOS EL MENSAJE DE SALIDA Y LISTO
						if (!"SIM".equalsIgnoreCase( mov.IdTipoInstrumento)||mov.esEnTransito.equalsIgnoreCase("S")) {
							if (!mov.IdTipoOperacion.equalsIgnoreCase("VENSIM")) {
								HashMap<String,String> valores = new HashMap<String,String>();
								valores.put("Folio", mov.IdMovimientoCarteraEnTransito.toString());
								salida.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraEnTransito, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_INGRESO_TITULO_TRANSITO_OK", valores ,conn,entrada.p_IdEmpresa));
							}
							continue;
						}
					}	
					
				}
				
				// No aplica para Anticipo de Simultaneas ni Ventas Cortas				
				if (mov.EsNominal.equalsIgnoreCase("S") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("N") && !mov.EsVentaCorta.equalsIgnoreCase("S")) {
					//1 COMPRA Y VENTAS NOMINALES NORMALES
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = mov.IdMovimientoCarteraEnTransito;
					MOV.EsCertificacion = "S";
					MOV.IdUsuario = entrada.p_IdUsuario;
					transitosOK.add(MOV);
					continue;
				}					
				
				
				GrupoOperacionCustodia g = new GrupoOperacionCustodia(hashMovimientosIngreso_MovimientosTransito.get(mov.IdMovimientoCarteraEnTransito),conf,mov,transitosRelacionados.get(mov.IdMovimientoCarteraEnTransito));
				
				//COMPRAS REPORTE NOMINAL
				if (mov.IdTipoOperacion.equalsIgnoreCase("SUSOSA")) {
					suscripcionOsaCustodia.add(g);
				}
				else if (mov.EsNominal.equalsIgnoreCase("S") && (mov.IdTipoOperacion.equalsIgnoreCase("COMRRV") || mov.IdTipoOperacion.equalsIgnoreCase("COMRRF"))) {
				//else if (mov.EsNominal.equalsIgnoreCase("S") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("S") && conf.IdAccionOperacion.equalsIgnoreCase("ING") && mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
				comprasReporteNominales.add(g);
					
				}
				//COMPRAS SIM NOMINALES
				else if (mov.EsNominal.equalsIgnoreCase("S") && mov.IdTipoOperacion.equalsIgnoreCase("COMSIM") && !mov.esEnTransito.equalsIgnoreCase("S") ) {
				//else if (mov.EsNominal.equalsIgnoreCase("S") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("S") && conf.IdAccionOperacion.equalsIgnoreCase("ING") && mov.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
					comprasSimultaneas.add(g);
				}
				//VENTAS SIMMULTENEA NOMINAL , REPORTE NOMINAL Y CON CUSTODIA 
				else if ( !mov.esEnTransito.equalsIgnoreCase("S") && (mov.IdTipoOperacion.equalsIgnoreCase("VENSIM") || mov.IdTipoOperacion.equalsIgnoreCase("VENRRV") || mov.IdTipoOperacion.equalsIgnoreCase("VENRRF"))) {
				//else if (mov.EsNominal.equalsIgnoreCase("S") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("S") && conf.IdAccionOperacion.equalsIgnoreCase("RET")) {
					ventasReporteSimultaneas.add(g);
				}
				
				//COMPRAS REPORTE Y SIM CON CUSTODIA
				else if (mov.EsNominal.equalsIgnoreCase("N") && (mov.IdTipoOperacion.equalsIgnoreCase("COMRRV") || mov.IdTipoOperacion.equalsIgnoreCase("COMRRF") || mov.IdTipoOperacion.equalsIgnoreCase("COMSIM"))&& !mov.esEnTransito.equalsIgnoreCase("S")) {
				//else if (mov.EsNominal.equalsIgnoreCase("N") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("S") && conf.PoseeInstrumentoRelacionado.equalsIgnoreCase("N") && conf.IdAccionOperacion.equalsIgnoreCase("ING")) {
					comprasSimultaneas.add(g);
				}
				//VENTA CORTA NOMINAL
				else if (mov.EsNominal.equalsIgnoreCase("S") && mov.EsVentaCorta.equalsIgnoreCase("S")) {
					ventasCortasNominal.add(g);
				}
				//VENTA CORTA CON CUSTODIA
				else if (mov.EsNominal.equalsIgnoreCase("N") && mov.EsVentaCorta.equalsIgnoreCase("S")) {
					//ventasCortasCustodia.add(g);
					ventasCortasNominal.add(g);
				}
				//DIVIDENDO EN ACCIONES
				else if (mov.EsNominal.equalsIgnoreCase("N") && mov.IdTipoOperacion.equalsIgnoreCase("DIVACC")) {
					comprasConCustodia.add(g);
				} 
				// DIVIDENDO ACCIONES NEGATIVAS
				else if (mov.EsNominal.equalsIgnoreCase("N") && mov.IdTipoOperacion.equalsIgnoreCase("DVACCN")) {
					ventasConCustodia.add(g);
				}
				//DIVIDENDO EN GARANTIAS
				else if (mov.EsNominal.equalsIgnoreCase("N") && mov.IdTipoOperacion.equalsIgnoreCase("DIVACG")) {
					//ventasCortasCustodia.add(g);
					dividendosGarantias.add(g);
				}
				//COMPRAS CON CUSTODIA
				else if (mov.EsNominal.equalsIgnoreCase("N") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("N") && conf.PoseeInstrumentoRelacionado.equalsIgnoreCase("N") && conf.IdAccionOperacion.equalsIgnoreCase("ING")) {
					comprasConCustodia.add(g);
				}
				//VENTAS CON CUSTODIA
				else if (mov.EsNominal.equalsIgnoreCase("N") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("N") && conf.PoseeInstrumentoRelacionado.equalsIgnoreCase("N") && conf.IdAccionOperacion.equalsIgnoreCase("RET")) {
					ventasConCustodia.add(g);
				}
				//NOTA DE DEBITO 
				else if (mov.EsNominal.equalsIgnoreCase("N")  && mov.IdTipoOperacion.equalsIgnoreCase("ANVACN") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("N") && conf.PoseeInstrumentoRelacionado.equalsIgnoreCase("N") && conf.IdAccionOperacion.equalsIgnoreCase("ANU")){					
					comprasConCustodia.add(g); 				
				}
				else if (mov.EsNominal.equalsIgnoreCase("N")  && mov.IdTipoOperacion.equalsIgnoreCase("ANCACN") && conf.PoseeInstrumentoSubyacente.equalsIgnoreCase("N") && conf.PoseeInstrumentoRelacionado.equalsIgnoreCase("N") && conf.IdAccionOperacion.equalsIgnoreCase("ANU")) {					
					ventasConCustodia.add(g);				
				}
				//ANTICIPO SIMULTANEA VENTA (Es lo mismo nominal que custodia)
				else if ((mov.EsNominal.equalsIgnoreCase("N") || mov.EsNominal.equalsIgnoreCase("S")) && mov.IdTipoOperacion.equalsIgnoreCase("ANSIMV")) {
					anticipoSimultaneaVenta.add(g);
				}
				
				//ANTICIPO SIMULTANEA COMPRA (Es lo mismo nominal que custodia)
				else if ((mov.EsNominal.equalsIgnoreCase("N") || mov.EsNominal.equalsIgnoreCase("S")) && mov.IdTipoOperacion.equalsIgnoreCase("ANSIMC")) {
					anticipoSimultaneaCompra.add(g);
				}
				
				//SIMULTANEA COMPRA EN TRANSITO (TERCER MOVIMIENTO)
				else if ( mov.IdTipoOperacion.equalsIgnoreCase("COMSIM") && mov.esEnTransito.equalsIgnoreCase("S")  ) {
					compraSimultaneaTransito.add(g);
				}

			}

			//COMPRAS CON CUSTODIA
			if (comprasConCustodia.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaComprasConCustodia = ProcesarComprasConCustodia(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, comprasConCustodia);
				for (GrupoOperacionCustodia g : salidaComprasConCustodia) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}	
			}
			
			//DIVIDENDOS GARANTIAS
			if (dividendosGarantias.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaDividendosGarantias = ProcesarDividendosGarantias(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, dividendosGarantias);
				for (GrupoOperacionCustodia g : salidaDividendosGarantias) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}	
			}
			
			//VENTAS CON CUSTODIA
			if (ventasConCustodia.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaVentasConCustodia = ProcesarVentasConCustodia(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, ventasConCustodia);
				for (GrupoOperacionCustodia g : salidaVentasConCustodia) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
					salida.retirosFin=g.retirosFin;
				}
			}
			
			//VENTAS CORTAS NOMINALES
			if (ventasCortasNominal.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaVentasCortas = ProcesarVentasCortasNominales(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, ventasCortasNominal);
				for (GrupoOperacionCustodia g : salidaVentasCortas) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}
			}
			
			//VENTAS CORTAS CUSTODIA
			/*if (ventasCortasNominal.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaVentasCortas = ProcesarVentasCortas(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, ventasCortasNominal);
				for (GrupoOperacionCustodia g : salidaVentasCortas) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.get(0).mensajes);
				}
			}*/

			
			//SIM COMPRA NOMINALES Y CUSTODIA
			if (comprasSimultaneas.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaCompraSim = ProcesarCompraSimultaneas(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, comprasSimultaneas);
				for (GrupoOperacionCustodia g : salidaCompraSim) {
					for ( GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MovimientoTransito:  g.transitosActualizar  ) {
						 ActualizacionSinRelacionado.put(MovimientoTransito.IdMovimientoCarteraEnTransito,true );			
					}
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}
			}
			
			//SIM VENTA NOMINALES Y CUSTODIA
			if (ventasReporteSimultaneas.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaVentaSimNominales = ProcesarVentaSimultaneas(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, ventasReporteSimultaneas);
				for (GrupoOperacionCustodia g : salidaVentaSimNominales) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}
			}
			
			//INGRESO DE SUSCRIPCION OSA
			if (suscripcionOsaCustodia.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaSuscripcionOsaCustodia = ProcesarSuscripcionOsaCustodia(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, suscripcionOsaCustodia);
				for (GrupoOperacionCustodia g : salidaSuscripcionOsaCustodia) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}
			}
			
			//ANTICIPO SIMULTANEA VENTA
			if (anticipoSimultaneaVenta.size() > 0)	
			{
				List<GrupoOperacionCustodia> salidaAnticipoSimultaneaVenta = ProcesarAnticipoSimultaneaVenta(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, anticipoSimultaneaVenta);
				for (GrupoOperacionCustodia g : salidaAnticipoSimultaneaVenta) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.get(0).mensajes);
				}
			}
			
			//ANTICIPO SIMULTANEA COMPRA
			if (anticipoSimultaneaCompra.size() > 0)	
			{
				List<GrupoOperacionCustodia> salidaAnticipoSimultaneaCompra = ProcesarAnticipoSimultaneaCompra(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, anticipoSimultaneaCompra);
				for (GrupoOperacionCustodia g : salidaAnticipoSimultaneaCompra) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.get(0).mensajes);
					
					if (g.pasos != null && g.pasos.size() > 2 && g.pasos.get(2).mensajes != null && !g.pasos.get(2).mensajes.isEmpty()) {
						if("ANTICIPO_PARCIALMENTE_CUBIERTA".equalsIgnoreCase(g.pasos.get(2).mensajes.get(0).codigo)) {
							salida.P_OUT_MENSAJES.addAll(g.pasos.get(2).mensajes);
						}
					}		
				}
			}
			
			
			// COMPRAS SIMULTANEAS EN TRANSITO (TERCER MOVIMIENTO)
			if (compraSimultaneaTransito.size() > 0)
			{
				List<GrupoOperacionCustodia> salidaCompraSimultaneaTransito = ProcesarCompraSimTransito(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_simularOperacion, compraSimultaneaTransito);
				for (GrupoOperacionCustodia g : salidaCompraSimultaneaTransito) {
					transitosOK.addAll(g.transitosActualizar);
					salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
				}
			}
			

			
			
			CacheValorizacion.valorizar(conn);
		
			//ACTUALIZACION DE TRANSITOS
			if (transitosOK.size() > 0) {
				if (ActualizacionSinRelacionado.size() >0 ) {
					List<ObjMovimientosAActualizar> transitosTrue = new ArrayList<>();
					List<ObjMovimientosAActualizar> transitosFalse = new ArrayList<>();
					
					for (ObjMovimientosAActualizar OMAA :transitosOK ) {
					    if (Boolean.TRUE.equals(ActualizacionSinRelacionado.get(OMAA.IdMovimientoCarteraEnTransito))) {
					    	transitosTrue.add(OMAA);    			    	
					    }else {
					    	transitosFalse.add(OMAA);					    	
					    }
					}
					if (transitosTrue.size()>0) {
						GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransitoNoRelacionado(conn, transitosTrue);
						}
					if (transitosFalse.size()>0) {				
						
						GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, transitosFalse);
						}
					
				}else {
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, transitosOK);
				}
			}
			
		}
		
		//FALTA DEVOLVER PARA LA NOTIFICACION DE MOVS EN TRANSITO
		
		return salida;
	}


	
	private List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> procesarMovimientoVentaCorta (Connection conn,List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> movimientos , List<CrearComprobante.Comprobante_Proceso> comprobantes, Long idEmpresa, String idModulo, Long idUsuario ) throws Exception{
		logger.debug("Inicio procesar Movimiento Venta Corta");
 		HashMap <Long,Object> hashComprobantes     = new HashMap<>();
 		HashMap <Long,CrearComprobante.MovimientoComprobanteDetalle_Proceso> hashMovimientosVc      = new HashMap<>();
 		List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> movimientosVentaCorta = new ArrayList<>();
 		List<CrearComprobante.MovimientoComprobanteDetalle_Proceso> movimientosProcesados = new ArrayList<>();
 		
 		movimientosVentaCorta = movimientos.stream().filter(vc ->("S").equals(vc.EsVentaCorta)).collect(Collectors.toList());
 		movimientosProcesados = movimientos.stream().filter(vc ->!("S").equals(vc.EsVentaCorta)).collect(Collectors.toList());
 		hashComprobantes = Utils.List2Hash(comprobantes, "IdComprobante");
 		
 		for(CrearComprobante.MovimientoComprobanteDetalle_Proceso movimiento : movimientosVentaCorta) {
 			CrearComprobante.MovimientoComprobanteDetalle_Proceso mov = new CrearComprobante.MovimientoComprobanteDetalle_Proceso();
 			Utils.copyPojo(movimiento, mov);
 			CrearComprobante.Comprobante_Proceso movVc  = (CrearComprobante.Comprobante_Proceso) hashComprobantes.get(mov.IdComprobante);
 			if(hashMovimientosVc.get(mov.IdComprobante) == null && ("S").equals(mov.EsVentaCorta)) {
 				mov.Tasa 								= movVc.comprobanteDetalles.get( 0 ).Tasa;
 				mov.TasaOperacion						= mov.TasaOperacion;
 				mov.Monto 								= movVc.Monto;
 				mov.MontoMovimientoComprobanteDetalle 	= movVc.Monto;
 				mov.Cantidad 							= movVc.Cantidad;
 				mov.Precio 								= movVc.Precio;
 				mov.PrecioAPlazo 						= movVc.Monto.
																multiply(mov.TasaOperacion.divide(new BigDecimal(100), MathContext.DECIMAL128).
																		multiply((movVc.DiasPlazo.divide(new BigDecimal(30), MathContext.DECIMAL128)),MathContext.DECIMAL128), MathContext.DECIMAL128);
 				
				mov.MontoAPlazo							= movVc.Monto.add(mov.PrecioAPlazo);
 				hashMovimientosVc.put(mov.IdComprobante, mov);
 			}
 		}
 		
 		for(Map.Entry<Long, CrearComprobante.MovimientoComprobanteDetalle_Proceso> obj : hashMovimientosVc.entrySet()){
 			movimientosProcesados.add(obj.getValue());
		}
 		
 		logger.debug("Fin procesar Movimiento Venta Corta");
		return movimientosProcesados;
	}
	
	
	
	private List<GrupoOperacionCustodia> ProcesarAnticipoSimultaneaCompra(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		logger.debug("Inicio Anticipo Compra simultanea");
		SQL sql = new SQL();

		List<Long> foliosCarteras = new ArrayList<>();
		List<Cartera> carteraRelacionada  = new ArrayList<>();
		HashMap<String,Object> paramsCarteraCompra = new HashMap<>();
		List<Subyacente> carteraSubyacente  = new ArrayList<>();
		List<IngresoCustodia.ObjDatosEntrada> transitos = new ArrayList<>();
		List<IngresoCustodia.ObjDatosEntrada> transitosOK = new ArrayList<>();
		
		//Parametro que indica si la cartera prestada por simultanea compra quedo en garantia o solo en cartera
		String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
	    for (GrupoOperacionCustodia grupo : grupos) {			

		    ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
		    foliosCarteras.add(mov.FolioCustodia);
		}	
	    paramsCarteraCompra.put("FolioCarteras", foliosCarteras);

			
		if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("N")) 
		{
			carteraRelacionada = sql.query(conn, SQL_CONSULTAR_CARTERA_RELACIONADA, paramsCarteraCompra, Cartera.class);  //Consulta datos de cartera relacionada
		}
			
			
	    

	
		// 1. RETIRO (ANTICIPO) DE SUBYACENTE
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		
		for (GrupoOperacionCustodia grupo : grupos) 
		{
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;			
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			//Obtiene datos faltantes del subyacente
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("FolioCarteraRelacionada", mov.FolioCustodia);
			params.put("IdInstrumento", mov.IdInstrumento);
			carteraSubyacente = sql.query(conn, SQL_CONSULTAR_SUBYACENTE, params, Subyacente.class);
			
			if (carteraSubyacente.size() <= 0)
			{
				continue;
			}
			
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;			
			ObjEntrada.TablaAfectada					= "SUBYACENTE";   //CARTERA - SUBYACENTE - TRANSITO - GARANTIA			
			ObjEntrada.AccionOperacion					= "ANTICIPO";     //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION			
			ObjEntrada.EsRetiroNegocio 					= "S";
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;	
			ObjEntrada.Cantidad							= mov.Cantidad;
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate;
			ObjEntrada.PrecioCompra						= mov.PrecioCompra;
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;						
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.FolioCustodia 					= carteraSubyacente.get(0).FolioSubyacente;
			ObjEntrada.IdCustodia 						= carteraSubyacente.get(0).IdSubyacente;
			ObjEntrada.IdCarteraOrigen					= carteraSubyacente.get(0).IdCarteraOrigen;
			ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);

			Paso p = new Paso("ANTICIPOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		if (retiros.size() > 0) 
		{
			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_ValorizacionAutomatica = "N";
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //1***

			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					for(Paso paso : grupo.pasos) {
						if("ANTICIPOSUBYACENTE".equals(paso.codigo)) {
							if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
								paso.estado = "OK";
								grupo.estado = "OK";
								
								String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
							}else {
								paso.estado = "ERR";
								grupo.estado = "ERR";
								
								String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
							}
						}
					}
				}
			}
		}
		
		

		
		//2.  RETIRO(ANTICIPO) DE CARTERA (CABECERA SIMULTANEA)
		retiros.clear();
	
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}

		    ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "ANTICIPO";
			ObjEntrada.EsRetiroNegocio 					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCustodia 					= mov.FolioCustodia;
			ObjEntrada.IdCustodia 						= mov.IdCustodiaOrigen;			
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;			
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
			
		if (retiros.size() > 0) {			

			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP);//3***
			
			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					for(Paso paso : grupo.pasos) {
						if("INGRESOSUBYACENTE".equals(paso.codigo)) {
							paso.resultado.add(datoSalida);
							
							if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
								grupo.estado = "OK";
								paso.estado = "OK";
								
								String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
							}
							else {
								grupo.estado = "ERR";
								paso.estado = "ERR";

								String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::

								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
							}
						}
					}
				}
			}
		}

		
		//3.  RETIRO DE GARANTIAS(con parametro encendido) O RETIRO DE CARTERA RELACIONADA(con parametro apagado)
		retiros.clear();
	
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}

		    ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
		     
		    
		    if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S"))
		    { // RETIRO DESDE GARANTIA
				// Busca cartera en garantia
				HashMap<String,Object> params = new HashMap<>();
				List<Garantia> carteraGarantia = new ArrayList<>();
				 
				params.put("FolioCarteras", mov.FolioCustodia);
				carteraGarantia = sql.query(conn, SQL_CONSULTAR_GARANTIA, params, Garantia.class);
				
				//Caso anticipo parametro encendido
				if (carteraGarantia != null && carteraGarantia.isEmpty()) {
					HashMap<String,Object> params2 = new HashMap<>();
					params2.put("FolioCarteras", mov.FolioCustodia);
	
					carteraGarantia = sql.query(conn, SQL_CONSULTAR_GARANTIA_ANTICIPO_PARAMETRO_ENCENDIDO, params2, Garantia.class);
				}
				
				for (Garantia gar: carteraGarantia)
				{
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();	
					
					ObjEntrada.IdGenerarMovimientoCustodia = gar.IdGarantia;		//ID Interno del proceso
					ObjEntrada.TablaAfectada 		  = "GARANTIA"; 				   //CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					ObjEntrada.EsRetiroNegocio 		  = "S";
					ObjEntrada.IdCustodia			  = gar.IdGarantia;						
					ObjEntrada.FolioCustodia 		  = gar.FolioGarantia;
					ObjEntrada.AccionOperacion		  = "ANTICIPO";     //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					ObjEntrada.IdCliente			  = gar.IdCliente; 
					ObjEntrada.IdPortafolio			  = gar.IdPortafolio;
					ObjEntrada.FechaOperacion 		  = mov.FechaOperacion;
					ObjEntrada.IdTipoOperacion 		  = mov.IdTipoOperacion;		
					ObjEntrada.IdOperacion 			  = 0L;
					ObjEntrada.IdConcepto			  = gar.IdConcepto;
					ObjEntrada.IdCarteraOrigen		  = gar.IdCarteraOrigen;					
					ObjEntrada.IdTipoOperacion		  = gar.IdTipoOperacionOrigen;
					ObjEntrada.PrecioCompra			  = gar.PrecioCompraOperacionOrigen;
					ObjEntrada.TasaCompra			  = gar.TasaCompraOperacionOrigen;
					ObjEntrada.IdCondicionLiquidacion = mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion 	  = mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso 	  = gar.IdTipoCanalIngreso;
					ObjEntrada.IdUsuario			  = mov.IdUsuario;
					ObjEntrada.IdModulo				  = mov.IdModulo;
					ObjEntrada.Glosa				  = "ANTICIPO SIMUTANEA";
					ObjEntrada.IdOrigenGarantia       = gar.IdOrigenGarantia;
					ObjEntrada.IdInstrumento 		  = gar.IdInstrumento;
					ObjEntrada.IdEmisor				  = gar.IdEmisor;
					ObjEntrada.IdMonedaInstrumento	  = gar.IdMonedaInstrumento;
					ObjEntrada.Cantidad				  = mov.Cantidad;     //La cantidad a anticipar puede ser parcial
					ObjEntrada.ValorRescate 		  = mov.ValorRescate;
					ObjEntrada.IdBoveda				  = gar.IdBoveda;
					ObjEntrada.PrecioCompra			  = gar.PrecioCompraOperacionOrigen; 
					ObjEntrada.TasaCompra			  = gar.TasaCompraOperacionOrigen; 
					ObjEntrada.MontoCompraMonedaInstrumento	= gar.INGMontoATasaGarantiaActual;
					ObjEntrada.MontoARecibirAnticipo = mov.Cantidad.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
					ObjEntrada.IdTipoProducto		 = mov.IdTipoProducto;
					retiros.add(ObjEntrada);
					
					Paso p = new Paso("RETIROGARANTIACARTERA", ObjEntrada, grupo);
					grupo.pasos.add(p);
				}
		    }
		    else
		    { // RETIRO DESDE CARTERA (ya que no entro a garatia)  	

				for (Subyacente sub: carteraSubyacente)	
				{	
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();	
					
					ObjEntrada.IdGenerarMovimientoCustodia = sub.IdSubyacente;		//ID Interno del proceso
					ObjEntrada.TablaAfectada 		  = "CARTERA"; 				   //CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					ObjEntrada.EsRetiroNegocio 		  = "S";
					ObjEntrada.AccionOperacion		  = "ANTICIPO";     //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					ObjEntrada.IdCliente			  = mov.IdCliente;
					ObjEntrada.IdPortafolio			  = mov.IdPortafolio;
					ObjEntrada.FechaOperacion 		  = mov.FechaOperacion;
					ObjEntrada.IdTipoOperacion 		  = mov.IdTipoOperacion;		
					ObjEntrada.FolioComprobante		  = mov.FolioComprobante;
					ObjEntrada.IdOperacion 			  = 0L;
					ObjEntrada.IdConcepto			  = mov.IdConcepto;					
					ObjEntrada.IdCondicionLiquidacion = "T0"; 
					ObjEntrada.FechaLiquidacion 	  = Fecha.toDate(fechaSistema);
					ObjEntrada.IdTipoCanalIngreso 	  = sub.IdTipoCanalIngreso;
					ObjEntrada.IdUsuario			  = mov.IdUsuario;
					ObjEntrada.IdModulo				  = mov.IdModulo;
					ObjEntrada.Glosa				  = "ANTICIPO SIMUTANEA COMPRA";					
					ObjEntrada.IdInstrumento 		  = sub.IdInstrumento;
					ObjEntrada.IdEmisor				  = sub.IdEmisor;
					ObjEntrada.IdMonedaInstrumento	  = mov.IdMonedaInstrumento;
					ObjEntrada.Cantidad				  = mov.Cantidad;     //La cantidad a anticipar puede ser parcial
					ObjEntrada.ValorRescate 		  = mov.ValorRescate;
					ObjEntrada.IdBoveda				  = mov.IdBoveda;
					ObjEntrada.MontoARecibirAnticipo  = mov.Cantidad.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
					ObjEntrada.IdTipoProducto		  = mov.IdTipoProducto;					
					
					
					for (Cartera carRel: carteraRelacionada){
						if (carRel.IdSubyacente.compareTo(sub.IdSubyacente) == 0 && carRel.IdInstrumento.compareTo(sub.IdInstrumento) == 0)
						{	//Si existe cartera relacionada, hacemos referencia a ella (cuando la custodia prestada no queda en garantia sino directamente en cartera)
							ObjEntrada.IdCustodia 			 = carRel.IdCartera;						
							ObjEntrada.FolioCustodia 		 = carRel.FolioCartera;
						}							
					}
					
					
					retiros.add(ObjEntrada);
					
					Paso p = new Paso("RETIROCARTERARELACIONADA", ObjEntrada, grupo);
					grupo.pasos.add(p);
				
					
					//Se genera objeto de ingreso de mov en transito de RETIRO en caso que falle el retiro anterior por falta de custodia
					IngresoCustodia.ObjDatosEntrada	ObjEntradaTransito = new IngresoCustodia.ObjDatosEntrada();
					ObjEntradaTransito.IdGenerarMovimientoCustodia		= sub.IdSubyacente;
					ObjEntradaTransito.TablaAfectada					= "TRANSITO";
					ObjEntradaTransito.AccionOperacion					= "RETIRO";
					ObjEntradaTransito.EsRegistroNuevo					= "S";
					ObjEntradaTransito.IdCliente						= mov.IdCliente;
					ObjEntradaTransito.IdPortafolio						= mov.IdPortafolio;
					ObjEntradaTransito.FechaOperacion					= Fecha.toDate(fechaSistema);
					ObjEntradaTransito.FechaComprobante 				= Fecha.toDate(fechaSistema);
					ObjEntradaTransito.IdTipoOperacion					= "RETTIT";      	// RETIRO DE TITULOS
					ObjEntradaTransito.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS					
					ObjEntradaTransito.IdConcepto						= "ANTSIC";         // ANTICPO SIMULTANEA COMPRA					
					ObjEntradaTransito.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
					ObjEntradaTransito.IdCondicionLiquidacion			= "T0"; 
					ObjEntradaTransito.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					ObjEntradaTransito.IdUsuario						= mov.IdUsuario;
					ObjEntradaTransito.Glosa							= "ANTICIPO SIMULTANEA COMPRA (Subyacente: " + sub.IdSubyacente + " )";
					ObjEntradaTransito.IdInstrumento					= sub.IdInstrumento;
					ObjEntradaTransito.IdEmisor							= sub.IdEmisor;
					ObjEntradaTransito.Cantidad							= mov.Cantidad;
					ObjEntradaTransito.EsVentaCorta						= "N";
					ObjEntradaTransito.IdBoveda							= mov.IdBoveda;
					ObjEntradaTransito.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
					ObjEntradaTransito.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					
					transitos.add(ObjEntradaTransito);
				}
		    }	    

		}


		if (retiros.size() > 0) {			

			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP);
			
			//Se revisa si la garantia retirada es igual al monto anticipado, si es menor se envia mensaje de advertencia
			boolean enviarMensajeAdvertencia = false;
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S"))
		    {
				BigDecimal cantidadTotalAnticipo = BigDecimal.ZERO;
				for (GrupoOperacionCustodia grupo : grupos) {			
				    ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
					
				    if(mov.Cantidad != null) {
				    	cantidadTotalAnticipo = cantidadTotalAnticipo.add(mov.Cantidad);
					}
				}
				
				BigDecimal garantiaRetirada = resultVentas.P_OUT_DATOSSALIDA.stream()
						.map(result ->{
							BigDecimal cantidad = BigDecimal.ZERO;
							if(result.Cantidad != null) {
								cantidad = result.Cantidad;
							}
							return cantidad;
						}).reduce(BigDecimal.ZERO, BigDecimal::add);
				
				if(garantiaRetirada.compareTo(cantidadTotalAnticipo) == -1) {
					enviarMensajeAdvertencia = true;
				}			
		    }
			
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S") && enviarMensajeAdvertencia ) {
				for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
					for (GrupoOperacionCustodia grupo : grupos) {
						for(Paso paso : grupo.pasos) {
							if("RETIROGARANTIACARTERA".equals(paso.codigo)) {
								if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
									grupo.estado = "OK";
									paso.estado = "OK";
									paso.mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "ANTICIPO_PARCIALMENTE_CUBIERTA", new HashMap<String,String>(), conn, p_IdEmpresa));	
								} 
								else {
									grupo.estado = "ERR";
									paso.estado = "ERR";
									paso.mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AL_RETIRAR_CARTERA", new HashMap<String,String>(), conn, p_IdEmpresa));	

								}
							}
						}
					}
				}
			}
				
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("N")) 
			{

					for  (MensajeSalida mensaje : resultVentas.P_OUT_MENSAJES)
					{
						if (mensaje.tipo.equalsIgnoreCase("ERROR"))
						{	//Si se produjo un error en el retiro, se genera un movimiento en transito de retiro por la cantidad total anticipada
							for (IngresoCustodia.ObjDatosEntrada tra: transitos)
							{
								if (mensaje.id.compareTo(tra.IdGenerarMovimientoCustodia) == 0)
								{
									transitosOK.add(tra);
								}
							}
						}
						else
						{
							//Si no se produce err en el retiro, se chequea que se haya retirado el total del anticipo. Si no se rerira el total (sino una parte), se genera un mov en transito de retiro por la cantidad faltante
							for (IngresoCustodia.ObjDatosEntrada tra: transitos)
							{
								
								BigDecimal custodiaRetirada = resultVentas.P_OUT_DATOSSALIDA.stream()
										.filter(result -> tra.IdGenerarMovimientoCustodia.compareTo(result.IdGenerarMovimientoCustodia) == 0)
										.map(result ->{
											BigDecimal cantidad = BigDecimal.ZERO;
											if(result.Cantidad != null) {
												cantidad = result.Cantidad;
											}
											return cantidad;
										}).reduce(BigDecimal.ZERO, BigDecimal::add);
								
								if(custodiaRetirada.compareTo(tra.Cantidad) == -1) {
									tra.Glosa						= "ANTICIPO SIMULTANEA COMPRA (Subyacente: " + tra.IdGenerarMovimientoCustodia + " ) MOV PARCIAL " + tra.Cantidad.subtract(custodiaRetirada) + " DE " + tra.Cantidad;
									tra.Cantidad					= tra.Cantidad.subtract(custodiaRetirada);
									transitosOK.add(tra);
								}
								
							}
						}
					}	

			}		

			
			
			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					for(Paso paso : grupo.pasos) {
						if("RETIROGARANTIACARTERA".equals(paso.codigo)) {
							paso.resultado.add(datoSalida);
							if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
								grupo.estado = "OK";
								paso.estado = "OK";
								
								String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
							}
							else {
								grupo.estado = "ERR";
								paso.estado = "ERR";

								String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::

								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								paso.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
							}
						}
					}
					
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						/*---------------
						  INICIO NETEO	
						---------------*/
						SQL _sql = new SQL();
						List<ContraparteSimultanea> contr = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA, new HashMap<>(), ContraparteSimultanea.class);
				
						List<ClienteNeteo> clientesANetear = new ArrayList<>();
						Long idEmpresa = null;
						Long idUsuario = null;	
						
						if (idEmpresa == null) idEmpresa = grupo.TransitoRelacionado.IdEmpresa;
						if (idUsuario == null) idUsuario = grupo.TransitoRelacionado.IdUsuario;
							
						ClienteNeteo cliente = new ClienteNeteo();
						cliente.IdInstrumento = grupo.TransitoRelacionado.IdInstrumento;
						cliente.IdPortafolio = grupo.TransitoRelacionado.IdPortafolio;
						clientesANetear.add(cliente);
						
						ClienteNeteo contraparte = new ClienteNeteo();
						contraparte.IdInstrumento = grupo.TransitoRelacionado.IdInstrumento;
						contraparte.IdPortafolio = contr.get(0).IdPortafolioDefecto;
						clientesANetear.add(contraparte);
						
						if (clientesANetear.size() > 0) {				
							EntradaNeteo eee = new EntradaNeteo();
							eee.p_IdEmpresa = idEmpresa;
							eee.p_IdModulo = "FAC";
							eee.p_IdUsuario = idUsuario;
							eee.p_PortafoliosInstrumento = clientesANetear;
							eee.p_SimularOperacion = "N";
							eee.p_UsaReglasNegocio = "S";
							
							AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
							paramsIn.p_IdEmpresa		= idEmpresa;
							paramsIn.p_IdModulo			= "FAC";
							paramsIn.p_IdUsuario		= idUsuario;
							paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
							paramsIn.p_Segundos			= 0L;
							paramsIn.p_FechaSistema		= fechaSistema;
							List<Parametro> paramsParametros = new ArrayList<>();
							Parametro ppp = new Parametro();
							ppp.NombreParametro = "Entrada";
							Gson gson = Utils.getGsonBuilder();
							ppp.Valor = "";
							ppp.ValorBlob = gson.toJson(eee);
							paramsParametros.add(ppp );
							paramsIn.p_Parametros		= paramsParametros ;
							AgregarPrograma AP = new AgregarPrograma();
							AP.logica(conn, paramsIn);
						}
						/*---------------
						  FIN NETEO	
						---------------*/
						
						grupo.estado = "OK";
						grupo.pasos.get(0).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(0).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
			}
		}

		if (transitosOK.size() > 0)
		{	//Se ingresan movimientos en transito de RETIRO (cuando el cliente no tenia custodia disponible para retirar)
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada ingCusIn = new IngresoCustodia.IngresoCustodiaEntrada();
			ingCusIn.p_IdEmpresa				= p_IdEmpresa;		
			ingCusIn.p_IdModulo					= p_IdModulo;
			ingCusIn.p_IdUsuario				= p_IdUsuario;
			ingCusIn.p_SimularOperacion			= p_simularOperacion;
			ingCusIn.p_TicketAtencion 			= 0L;
			ingCusIn.p_ValorizacionAutomatica	= "N";
			ingCusIn.p_DatosEntrada				= transitosOK;
			IngresoCustodia.IngresoCustodiaSalida ingCusOut = ingresoCustodia.logica(conn, ingCusIn);				

		}
		
		
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
		return grupos;
	}	
	
	

	private List<GrupoOperacionCustodia> ProcesarAnticipoSimultaneaVenta(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		SQL sql = new SQL();

		// 1. RETIRO (ANTICIPO) DE SUBYACENTE (Se restituye la cartera original por cada subyacente)
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		
		List<IngresoCustodia.ObjDatosEntrada> retornoCartera = new ArrayList<>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		for (GrupoOperacionCustodia grupo : grupos) 
		{
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;			
			
			//Obtiene datos faltantes del subyacente
			List<Subyacente> carteraSubyacente  = new ArrayList<>();
			HashMap<String,Object> params = new HashMap<>();
			params.put("FolioCarteraRelacionada", mov.FolioCustodia);
			params.put("IdInstrumento", mov.IdInstrumento);
			carteraSubyacente = sql.query(conn, SQL_CONSULTAR_SUBYACENTE, params, Subyacente.class);
			
			if (carteraSubyacente.size() <= 0)
			{
				continue;
			}		

			
			BigDecimal saldoPorAnticipar = mov.Cantidad;
			 
			
			for (Subyacente sub :carteraSubyacente)
			{				
				BigDecimal cantidadAnticipo  = BigDecimal.ZERO;
				
				if (saldoPorAnticipar.compareTo(BigDecimal.ZERO) == 1) 
				{
					if (sub.Cantidad.compareTo(saldoPorAnticipar) == -1 || sub.Cantidad.compareTo(saldoPorAnticipar) == 0)
					{
						cantidadAnticipo = 	sub.Cantidad;
					}	
					else
					{
						cantidadAnticipo =  saldoPorAnticipar;
					}
				}
				else
				{
					break;
				}
				

				
				RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
				
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial + sub.IdSubyacente;	 // Llave para retiro Subyacente	
				ObjEntrada.TablaAfectada					= "SUBYACENTE";   //CARTERA - SUBYACENTE - TRANSITO - GARANTIA			
				ObjEntrada.AccionOperacion					= "ANTICIPO";     //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION			
				ObjEntrada.EsRetiroNegocio 					= "S";
				ObjEntrada.FechaOperacion					= mov.FechaOperacion;
				ObjEntrada.IdConcepto						= mov.IdConcepto;
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= mov.IdCliente; 
				ObjEntrada.IdPortafolio						= mov.IdPortafolio;
				ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
				ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
				ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
				ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
				ObjEntrada.IdUsuario						= mov.IdUsuario;
				ObjEntrada.IdSucursal						= mov.IdSucursal; 
				ObjEntrada.IdAgente							= mov.IdAgente;
				ObjEntrada.Glosa							= mov.Glosa;
				ObjEntrada.IdInstrumento					= mov.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
				ObjEntrada.IdEmisor							= mov.IdEmisor;
				ObjEntrada.EsFungible						= mov.EsFungible;
				ObjEntrada.IdBoveda							= mov.IdBoveda;
				ObjEntrada.EsNominal						= mov.EsNominal;
				ObjEntrada.Cantidad							= cantidadAnticipo;
				ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
				ObjEntrada.ValorRescate						= mov.ValorRescate;
				ObjEntrada.PrecioCompra						= mov.PrecioCompra;
				ObjEntrada.TasaCompra						= mov.TasaCompra;
				ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;						
				ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
				ObjEntrada.NemoSVS							= mov.NemoSVS;
				ObjEntrada.IdModulo							= "FAC";
				ObjEntrada.FolioCustodia 					= sub.FolioSubyacente;
				ObjEntrada.IdCustodia 						= sub.IdSubyacente;
				ObjEntrada.IdCarteraOrigen					= sub.IdCarteraOrigen;
				ObjEntrada.MontoARecibirAnticipo			= cantidadAnticipo.multiply(mov.PrecioCompra, MathContext.DECIMAL128);		
				ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
				
				retiros.add(ObjEntrada);	
				
				Paso p = new Paso("ANTICIPOSUBYACENTE " + sub.IdSubyacente, ObjEntrada, grupo);
				grupo.pasos.add(p);
				
				
				
				if (sub.IdCarteraRetirada.compareTo(0L) == 0)
				{
					// Solo si no esta relacionado con el IdCarteraRetirada
					IngresoCustodia.ObjDatosEntrada	ObjEntradaRetorno = new IngresoCustodia.ObjDatosEntrada();
					ObjEntradaRetorno.IdGenerarMovimientoCustodia		= sub.IdSubyacente;
					ObjEntradaRetorno.TablaAfectada						= "CARTERA";
					ObjEntradaRetorno.AccionOperacion					= "INGRESO";
					ObjEntradaRetorno.EsRegistroNuevo					= "S";
					ObjEntradaRetorno.IdCliente							= mov.IdCliente;
					ObjEntradaRetorno.IdPortafolio						= mov.IdPortafolio;
					ObjEntradaRetorno.FechaOperacion					= Fecha.toDate(fechaSistema);
					ObjEntradaRetorno.FechaComprobante 					= Fecha.toDate(fechaSistema);
					ObjEntradaRetorno.IdTipoOperacion					= "INGTIT";
					ObjEntradaRetorno.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS					
					ObjEntradaRetorno.IdConcepto						= "ANTSIV";  	    // ANTICIPO SIMULTANEA VENTA						
					ObjEntradaRetorno.FechaVencimientoOperacion        	= Fecha.toDate(fechaSistema);
					ObjEntradaRetorno.IdCondicionLiquidacion			= "T0"; 
					ObjEntradaRetorno.FechaLiquidacion					= Fecha.toDate(fechaSistema);					
					ObjEntradaRetorno.IdUsuario							= mov.IdUsuario;
					ObjEntradaRetorno.Glosa								= "ANTICIPO SIMULTANEA VENTA";					
					ObjEntradaRetorno.IdInstrumento						= sub.IdInstrumento;
					ObjEntradaRetorno.IdEmisor							= sub.IdEmisor;
					ObjEntradaRetorno.Cantidad							= cantidadAnticipo;			
					ObjEntradaRetorno.EsVentaCorta						= "N";					
					ObjEntradaRetorno.IdBolsa							= mov.IdBolsa;
					ObjEntradaRetorno.IdBoveda							= mov.IdBoveda;
					ObjEntradaRetorno.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntradaRetorno.MontoCompra                       = cantidadAnticipo.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
					
					retornoCartera.add(ObjEntradaRetorno);	
				}
				

				
				saldoPorAnticipar = saldoPorAnticipar.subtract(cantidadAnticipo); //descuenta la cantidad anticipada al saldo
			}			

		}
		
		
		if (retiros.size() > 0) 
		{
			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_ValorizacionAutomatica = "N";
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //4***

			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					Long llaveSubyacente = grupo.IdMovimientoInicial + datoSalida.IdCustodia;   // Llave de retiro del Subyacente
					
					if (!(llaveSubyacente).equals(datoSalida.IdGenerarMovimientoCustodia)) {	
						continue;
					}
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) 
					{
						grupo.pasos.get(0).estado = "OK";
						grupo.estado = "OK";
						
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else 
					{
						grupo.pasos.get(0).estado = "ERR";
						grupo.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
						
					}
				}
			}
		}		
	
		
		//2  RETIRO(ANTICIPO) DE CARTERA (CABECERA)
		retiros.clear();
				
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			

		    ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "ANTICIPO";
			ObjEntrada.EsRetiroNegocio 					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCustodia 					= mov.FolioCustodia;
			ObjEntrada.IdCustodia 						= mov.IdCustodiaOrigen;			
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.MontoARecibirAnticipo			= mov.Cantidad.multiply(mov.PrecioCompra, MathContext.DECIMAL128);
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
				
		if (retiros.size() > 0) {			

			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //5***
			
			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					grupo.pasos.get(1).resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						/*---------------
						  INICIO NETEO	
						---------------*/
						SQL _sql = new SQL();
						List<ContraparteSimultanea> contr = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA, new HashMap<>(), ContraparteSimultanea.class);
						
						List<ClienteNeteo> clientesANetear = new ArrayList<>();
						Long idEmpresa = null;
						Long idUsuario = null;	
							
						if (idEmpresa == null) idEmpresa = grupo.TransitoRelacionado.IdEmpresa;
						if (idUsuario == null) idUsuario = grupo.TransitoRelacionado.IdUsuario;
							
						ClienteNeteo cliente = new ClienteNeteo();
						cliente.IdInstrumento = grupo.TransitoRelacionado.IdInstrumento;
						cliente.IdPortafolio = grupo.TransitoRelacionado.IdPortafolio;
						clientesANetear.add(cliente);
						
						ClienteNeteo contraparte = new ClienteNeteo();
						contraparte.IdInstrumento = grupo.TransitoRelacionado.IdInstrumento;
						contraparte.IdPortafolio = contr.get(0).IdPortafolioDefecto;
						clientesANetear.add(contraparte);
						
						if (clientesANetear.size() > 0) {				
							EntradaNeteo eee = new EntradaNeteo();
							eee.p_IdEmpresa = idEmpresa;
							eee.p_IdModulo = "FAC";
							eee.p_IdUsuario = idUsuario;
							eee.p_PortafoliosInstrumento = clientesANetear;
							eee.p_SimularOperacion = "N";
							eee.p_UsaReglasNegocio = "S";
							
							AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
							paramsIn.p_IdEmpresa		= idEmpresa;
							paramsIn.p_IdModulo			= "FAC";
							paramsIn.p_IdUsuario		= idUsuario;
							paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
							paramsIn.p_Segundos			= 0L;
							paramsIn.p_FechaSistema		= fechaSistema;
							List<Parametro> paramsParametros = new ArrayList<>();
							Parametro ppp = new Parametro();
							ppp.NombreParametro = "Entrada";
							Gson gson = Utils.getGsonBuilder();
							ppp.Valor = "";
							ppp.ValorBlob = gson.toJson(eee);
							paramsParametros.add(ppp );
							paramsIn.p_Parametros		= paramsParametros ;
							AgregarPrograma AP = new AgregarPrograma();
							AP.logica(conn, paramsIn);
						}
						/*---------------
						  FIN NETEO	
						---------------*/
						
						grupo.estado = "OK";
						grupo.pasos.get(0).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(0).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						grupo.estado = "OK";
						grupo.pasos.get(1).estado = "OK";
						
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						grupo.estado = "ERR";
						grupo.pasos.get(1).estado = "ERR";

						String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::

						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
			}
		}
		
		
		if (retornoCartera.size() > 0)
		{
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada ingCusIn = new IngresoCustodia.IngresoCustodiaEntrada();
			ingCusIn.p_IdEmpresa				= p_IdEmpresa;		
			ingCusIn.p_IdModulo					= p_IdModulo;
			ingCusIn.p_IdUsuario				= p_IdUsuario;
			ingCusIn.p_SimularOperacion			= p_simularOperacion;
			ingCusIn.p_TicketAtencion 			= 0L;
			ingCusIn.p_ValorizacionAutomatica	= "N";
			ingCusIn.p_DatosEntrada				= retornoCartera;
			IngresoCustodia.IngresoCustodiaSalida ingCusOut = ingresoCustodia.logica(conn, ingCusIn);
		}
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
		return grupos;
	}
	
	
	
	private List<GrupoOperacionCustodia> ProcesarVentaSimultaneas(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		
		//***** 1 . RETIRA CUSTODIA DE CARTERA 
		// SOLO SIMULTANEAS CON CUSTODIA
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
				continue;
			}
						
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;
			
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "RETIRO";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;	
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);
			
			Paso p = new Paso("RETIROCARTERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		if (retiros.size() > 0) {
			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_ValorizacionAutomatica = "N";
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP);		 //8***	
						
			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						grupo.pasos.get(0).estado = "OK";
						grupo.pasos.get(0).resultado.add(datoSalida);
						grupo.estado = "OK";
						
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						grupo.pasos.get(0).estado = "ERR";
						grupo.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
						
					}
				}
			}
		}		
		
		//2  INGRESA CARTERA DE CABECERA DE SIMULTANEA 
			
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK") && !"S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)  ) {
				continue;
			}

				ObjMovimientosEnTransito movPrincipal = grupo.TransitoPrincipal;
				
				IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = movPrincipal.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= movPrincipal.IdCliente; 
				ObjEntrada.IdPortafolio						= movPrincipal.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		= movPrincipal.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					= movPrincipal.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.FechaVencimientoInstrumento		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= movPrincipal.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion					= movPrincipal.IdTipoOperacion;	
				ObjEntrada.IdConcepto						= movPrincipal.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion			= movPrincipal.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= movPrincipal.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= movPrincipal.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						= movPrincipal.CodigoIFRS;
				ObjEntrada.NemoSVS							= movPrincipal.NemoSVS;
				ObjEntrada.IdOperacion						= movPrincipal.IdOperacion;
				ObjEntrada.IdTipoComprobante				= movPrincipal.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= movPrincipal.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			= movPrincipal.FolioExternoReferencia;
				ObjEntrada.IdBolsa							= movPrincipal.IdBolsa;	
				ObjEntrada.IdUsuario						= movPrincipal.IdUsuario;
				ObjEntrada.IdSucursal						= movPrincipal.IdSucursal; 
				ObjEntrada.IdAgente							= movPrincipal.IdAgente;
				ObjEntrada.Glosa							= movPrincipal.Glosa;
				ObjEntrada.IdInstrumento					= movPrincipal.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				= movPrincipal.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					= movPrincipal.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= movPrincipal.IdTipoReajuste;
				ObjEntrada.IdEmisor							= movPrincipal.IdEmisor;
				ObjEntrada.EsFungible						= movPrincipal.EsFungible;
				ObjEntrada.IdBoveda							= movPrincipal.IdBoveda;
				ObjEntrada.EsNominal						= movPrincipal.EsNominal;		
				ObjEntrada.Cantidad							= movPrincipal.Cantidad; 
				ObjEntrada.CorteMinimo						= movPrincipal.CorteMinimo; 
				ObjEntrada.ValorRescate						= movPrincipal.ValorRescate; 
				ObjEntrada.PrecioCompra						= movPrincipal.PrecioCompra; 
				ObjEntrada.TasaCompra						= movPrincipal.TasaCompra;
				ObjEntrada.MontoCompra						= movPrincipal.MontoCompra;
				ObjEntrada.IdTipoProducto					= movPrincipal.IdTipoProducto;
				if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) {
					BigDecimal tasa 							= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal periodos 						= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
					BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));	
					ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn, movPrincipal.PrecioCompra.multiply(factor), MonedaFormato.PRECIO, movPrincipal.IdMonedaInstrumento).valor;
				}else {
					if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("VENRRV")) {
						BigDecimal tasa 						= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						BigDecimal periodos 					= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(360),20,RoundingMode.HALF_UP);
						Double factor 							= Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
						ObjEntrada.PrecioAPlazo 				= movPrincipal.PrecioCompra.multiply(new BigDecimal(factor));
					}
					else if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("VENRRF")) {
						BigDecimal tasa 						= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						BigDecimal periodos 					= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(360),20,RoundingMode.HALF_UP);

						ObjEntrada.PrecioAPlazo 				= movPrincipal.MontoCompra.divide(movPrincipal.Cantidad,20,RoundingMode.HALF_UP);
					}
					else if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("VENSIM")) {
						BigDecimal tasa 							= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						BigDecimal periodos 						= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
						BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));
						ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn, movPrincipal.PrecioCompra.multiply(factor), MonedaFormato.PRECIO, movPrincipal.IdMonedaInstrumento).valor;
					}
					else {
						ObjEntrada.PrecioAPlazo					= BigDecimal.ZERO;
					}		
				}	
				
				ObjEntrada.MontoAPlazo 						= movPrincipal.Cantidad.multiply(ObjEntrada.PrecioAPlazo);
				ObjEntrada.MontoCompraMonedaInstrumento		= movPrincipal.MontoCompraMonedaInstrumento;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCABECERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
	
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //2***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  0 : 1;

					Paso p =grupo.pasos.get(x);	
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(x).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		//3
		ingresos.clear();
				
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;
			
			Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  0 : 1;
			List<Object> movsCabecera = (List<Object>) grupo.pasos.get(x).resultado;
			
			if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) {

			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "SUBYACENTE";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ingresos.add(ObjEntrada);
			
			}else {
				
				List<Object> movsRetiro = (List<Object>) grupo.pasos.get(0).resultado;
				for (Object ret : movsRetiro) {
					cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida retiro = (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida) ret;
					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					
					ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial*10000 + movsRetiro.indexOf(ret);
					ObjEntrada.TablaAfectada					= "SUBYACENTE";
					ObjEntrada.AccionOperacion					= "INGRESO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
					ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;
					ObjEntrada.IdCarteraOrigen					= retiro.IdCustodia;
					ObjEntrada.IdCliente						= mov.IdCliente; 
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= mov.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
					ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
					ObjEntrada.IdConcepto						= mov.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.FolioComprobante;
					ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
					ObjEntrada.IdBolsa							= mov.IdBolsa;	
					ObjEntrada.IdUsuario						= mov.IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursal; 
					ObjEntrada.IdAgente							= mov.IdAgente;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.EsNominal;		
					ObjEntrada.Cantidad							= retiro.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= retiro.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
					ObjEntrada.TasaCompra						= mov.TasaCompra;
					ObjEntrada.MontoCompra						= mov.MontoCompra.multiply(retiro.Cantidad).divide(mov.Cantidad,20,RoundingMode.HALF_UP);
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento.multiply(retiro.Cantidad).divide(mov.Cantidad,20,RoundingMode.HALF_UP);
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					ingresos.add(ObjEntrada);
				}
			
			}
			Paso p = new Paso("INGRESOSUBYACENTE", ingresos, grupo);
			grupo.pasos.add(p);		
		
		}
				
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //3***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) {			
						if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
							continue;
						}				
					}else {
						Long idMovimientoInicial = datoSalida.IdGenerarMovimientoCustodia /10000;					
						if (!grupo.IdMovimientoInicial.equals(idMovimientoInicial)) {
							continue;
						}	
					}
					
					Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  1 : 2;
	
					Paso p =grupo.pasos.get(x);					
					grupo.pasos.get(x).resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(x).estado = "OK";
						
						//NO ES NECESARIO INDICAR QUE SE CREO EL SUBYACENTE
						//String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						//grupo.pasos.get(2).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(x).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_SUBYACENTE_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(x).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV2 = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV2.IdMovimientoCarteraEnTransito = grupo.TransitoRelacionado.IdMovimientoCarteraEnTransito;
				MOV2.EsCertificacion = "S";
				MOV2.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV2);
			}
		}
		
		
		
		return grupos;
		
	}
	
	private List<GrupoOperacionCustodia> ProcesarSuscripcionOsaCustodia (Connection conn,Long p_IdEmpresa,  Long p_IdUsuario,String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		SQL sql = new SQL();
		String tablaAfectada = "CARTERA";
		
		//1  RETIRO DE OSA (De custodia)
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		for (GrupoOperacionCustodia grupo : grupos) {			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
		
			ObjEntrada.TablaAfectada					= tablaAfectada;
			ObjEntrada.AccionOperacion					= "RETIRO";
			ObjEntrada.IdOrigenGarantia                 = mov.IdOrigenGarantia;
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumentoAnterior;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;	
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);
			
			Paso p = new Paso("RETIROCARTERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		if (retiros.size() > 0) {
			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_ValorizacionAutomatica = "N";
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //6***

			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						grupo.pasos.get(0).estado = "OK";
						grupo.estado = "OK";
						
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						grupo.pasos.get(0).estado = "ERR";
						grupo.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
						
					}
				}
			}
		}
		
		//2   INGRESO DE CUSTODIA
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdOperacion						= mov.IdOperacion;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOCABECERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //4***
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					Paso p = grupo.pasos.get(1);
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						p.estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
			}
		}
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		return grupos;
	}
	
	private List<GrupoOperacionCustodia> ProcesarVentasConCustodia(Connection conn,Long p_IdEmpresa,  Long p_IdUsuario,String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		SQL sql = new SQL();
		
		//ProcesarFacturacionCustodia_Salida salida = new ProcesarFacturacionCustodia_Salida();
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		List<RetiroCustodia.ObjDatosEntrada> retirosFin = new ArrayList<>();		
		List<FlujoUso> Uso = new ArrayList<>();		
		List<FlujoFiltrosBusqueda> filtroBusqueda = new ArrayList<>();
		List<FlujoEsAgrupadoPor> EsAgrupadoPor = new ArrayList<>();
		
		FlujoUso uso, usoVDes;
		FlujoFiltrosBusqueda filtro;
		FlujoEsAgrupadoPor Agrupa;
		
		//Fecha Sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Agrupa  = new FlujoEsAgrupadoPor();
		HashMap<String,String> idClientePortafolio = new HashMap<>();
		//HashMap<Long,Long> idInstrumento = new HashMap<>();
		HashMap<String,String> idNemotecnico = new HashMap<>();
		ArrayList<RetiroCustodia.ObjDatosEntrada> Errores = new ArrayList<>();
		HashMap<String, List<RetiroCustodia.ObjDatosEntrada>> hashObjetos = new HashMap<String, List<RetiroCustodia.ObjDatosEntrada>>();
		String EnviaFacturacionSeparadaPorFechaLiquidacion = "N";
		try {EnviaFacturacionSeparadaPorFechaLiquidacion = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "FACTURACION", "FacturacionSeparadaPorFechaLiquidacion");}catch(Exception e) {};
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			 uso = new FlujoUso();			 
			 usoVDes = new FlujoUso();
			 filtro  = new FlujoFiltrosBusqueda();
			 
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			String fecha = mov.FechaLiquidacion.compareTo(Fecha.toDate(fechaSistema)) == -1 ? fechaSistema : Fecha.toStringDate(mov.FechaLiquidacion);
			
			ControlRetiroCustodia.ControlRetiroObjDatosEntrada	ObjEntrada = new ControlRetiroCustodia.ControlRetiroObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "RETIRO";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= Fecha.toDate(fecha);
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
			ObjEntrada.FolioCustodia					= mov.FolioCustodia; 
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.Nemotecnico                      = mov.Nemotecnico;
			ObjEntrada.NemoSVS                          = mov.NemoSVS;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;	
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.EsVentaDescubierta				= "N";
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ObjEntrada.IdClasificacionTipoInstrumento   = mov.IdClasificacionTipoInstrumento;
			ObjEntrada.NombreCliente                    = mov.NombreCliente;
			ObjEntrada.CodigoPortafolio                 = mov.CodigoPortafolio;
			if (mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
				ObjEntrada.FechaVencimientoInstrumento  = mov.FechaVencimientoInstrumento;
			}
			
			String key = ObjEntrada.IdCliente + "-" + ObjEntrada.IdPortafolio + "-" + ObjEntrada.IdInstrumento + "-" + fecha + "-" + ObjEntrada.IdEmisor;
			String keyUso = ObjEntrada.IdCliente + "-" + ObjEntrada.IdPortafolio;
			//Long keyfiltro = ObjEntrada.IdInstrumento;
			String keyfiltroNemo = "IF".equalsIgnoreCase(ObjEntrada.IdClasificacionTipoInstrumento)? ObjEntrada.NemoSVS: ObjEntrada.Nemotecnico;
			
			if(hashObjetos.get(key) != null) {
				hashObjetos.get(key).add(ObjEntrada);
			}else {
				List<RetiroCustodia.ObjDatosEntrada> carterasRetiros = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
				carterasRetiros.add(ObjEntrada);
				hashObjetos.put(key, carterasRetiros);
			}
			
			retiros.add(ObjEntrada);
			
			//Uso para validar flujo custodia	
			if (!idClientePortafolio.containsKey(keyUso)) {
				
				uso.UsoTipoOperacion = "CARDIS";		
				uso.IdCliente = mov.IdCliente;
				uso.IdPortafolio = mov.IdPortafolio;
				uso.BusquedaEspecifica = "CARTERA_DISPONIBLE";
				uso.IdTipoInstrumentoEspecifico = "";
				Uso.add(uso);			
				
				usoVDes.UsoTipoOperacion = "VENDES";		
				usoVDes.IdCliente = mov.IdCliente;
				usoVDes.IdPortafolio = mov.IdPortafolio;
				usoVDes.BusquedaEspecifica = "VENTA_DESCUBIERTA";
				usoVDes.IdTipoInstrumentoEspecifico = "";			
				Uso.add(usoVDes);
				
				idClientePortafolio.put(keyUso, keyUso);
			}
			
			//if (!idInstrumento.containsKey(keyfiltro)) {
			if (!idNemotecnico.containsKey(keyfiltroNemo)) {	
					
				//Flujo para validar flujo custodia
				filtro.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
				filtro.IdTipoInstrumento = null;
				filtro.IdInstrumento = mov.IdInstrumento;
				filtro.IdEmisor = mov.IdEmisor;
				filtro.FechaVencimiento = "IF".equalsIgnoreCase(mov.IdClasificacionTipoInstrumento)? mov.FechaVencimientoInstrumento: null;
				filtro.IdTipoReajuste = null;
				filtro.EsFungible = null;
				filtro.IdBoveda = null;
				filtroBusqueda.add(filtro);		
				
				//idInstrumento.put(keyfiltro, keyfiltro);
				idNemotecnico.put(keyfiltroNemo, keyfiltroNemo);
			}
			
			Paso p = new Paso("RETIROCARTERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		//Agrupa para validar flujo custodia
		Agrupa.ClasificacionTipoInstrumento = "S";
		Agrupa.TipoInstrumento = null;
		Agrupa.Instrumento = "S";
		Agrupa.Emisor = "S";
		Agrupa.Nemotecnico = null;
		Agrupa.NemoSVS = null;
		Agrupa.FechaEmisionInstrumento = null;
		Agrupa.FechaVencimientoInstrumento = "S";
		Agrupa.FechaLiquidacionInstrumento = null;
		Agrupa.TipoReajuste = "S";
		Agrupa.EsFungible = "S";
		Agrupa.CodigoIFRS = null;
		Agrupa.Boveda = "S";
		Agrupa.Bolsa = null;
		Agrupa.TipoOperacion = null;
		Agrupa.Mesa = null;
		Agrupa.FolioRegistro = null;
		Agrupa.ItemOrigen = null;
		EsAgrupadoPor.add(Agrupa);
		
		if (retiros.size() > 0) {			
			//Llama flujo custodia
			FlujoCustodiaEntrada inFlujo = new FlujoCustodiaEntrada();  
			inFlujo.p_IdEmpresa =  p_IdEmpresa;
			inFlujo.p_IdUsuario = p_IdUsuario;
			inFlujo.p_IdModulo = p_IdModulo;
			inFlujo.p_FechaSaldo = fechaSistema;			
			inFlujo.p_IdPais = 44L;
			inFlujo.p_DiasProyeccion = 5; //FALLA SI LOS MOVIMIENTOS SON DE MAS DE 5 DIAS DE ANTIGUEDAD 
			inFlujo.p_DiaHabil = "S";
			inFlujo.p_ValorizarFlujo = "N"; 
			inFlujo.p_Uso.addAll(Uso);
			inFlujo.p_FiltrosBusqueda.addAll(filtroBusqueda);
			inFlujo.p_EsAgrupadoPor.addAll(EsAgrupadoPor);
					
			ConsultarFlujoCustodia flujoCus = new ConsultarFlujoCustodia();
			FlujoCustodiaSalida flujoCustOut = flujoCus.logica(conn, inFlujo);
			
			List<IngresoCustodia.ObjDatosEntrada> transitosVDes = new ArrayList<>();
			HashMap<String, FlujoCustodia> hashCARDIS = new HashMap<>();
			HashMap<String, FlujoCustodia> hashVENDES = new HashMap<>();
			HashMap<String, String> hashKEY = new HashMap<>();
						
			//Lista el flujo resultante de la consulta segun cliente, portafolio, instrumento
			for (FlujoCustodia flujo : flujoCustOut.p_out_FlujoTotalizado) 
			{
				String keyFlujo = flujo.IdCliente + "-" +flujo.IdPortafolio + "-" + flujo.IdInstrumento + "-" + flujo.FechaSaldo + "-" + flujo.IdEmisor;
				
				for (String keyEntrada : hashObjetos.keySet())
				{
					if (keyFlujo.equalsIgnoreCase(keyEntrada))
					{
						hashKEY.put(keyFlujo, keyFlujo);
						if ("CARDIS".equals(flujo.UsoTipoOperacion)) {
							hashCARDIS.put(keyFlujo, flujo);
						} else if ("VENDES".equals(flujo.UsoTipoOperacion)) {
							hashVENDES.put(keyFlujo, flujo);
						}
					}	
				}
			}
			
			
			//Consulta indicador de venta descubierta
			List<Long> idsPortafolio = new ArrayList<>();
			for (String key : hashKEY.values()) {
				List<RetiroCustodia.ObjDatosEntrada> elemento = hashObjetos.get(key);
				if (elemento != null) {
					idsPortafolio.addAll(elemento.parallelStream().map(cartera->cartera.IdPortafolio).collect(Collectors.toList()));	
				}
				
			}
			
			idsPortafolio = idsPortafolio.parallelStream().distinct().collect(Collectors.toList());
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsPortafolio", idsPortafolio);
			List<Indicador> indicadores = sql.query(conn, SQL_CONSULTAR_INDICADOR_PORTAFOLIO, params, Indicador.class);
			
			HashMap<Long, Indicador> hashIndicador = new HashMap<>();
			Long hashkey;
			for(Indicador ind : indicadores) {
				hashkey = ind.IdPortafolio;
				hashIndicador.put(hashkey, ind);
			}
			
			
			//aqui pasa por movimiento?
			
			//Crear movimiento a custodia segun resultado de flujo
			for (String key : hashKEY.values()) { 
				FlujoCustodia objCARDIS = hashCARDIS.get(key);
				FlujoCustodia objVENDES = hashVENDES.get(key);
				List<RetiroCustodia.ObjDatosEntrada> elementos = hashObjetos.get(key);
				Indicador indicadoresPortafolio = null;
				
				if (elementos != null && elementos.size() > 0) {
					indicadoresPortafolio = hashIndicador.get(elementos.get(0).IdPortafolio);	
				}
				
				for(RetiroCustodia.ObjDatosEntrada elemento : elementos) {
					if (objCARDIS != null) {
						BigDecimal SaldoCustodiaDisponibleOriginal = objCARDIS.Cantidad;	
						Long idMovimientoTransitoOperacionVAN = 0L;
					 
						boolean tengo_sobregiro = false;
						if (elemento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
							tengo_sobregiro = indicadoresPortafolio.SobregiroRF.equalsIgnoreCase("S")?true:false;
						}
						else if (elemento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
							tengo_sobregiro = indicadoresPortafolio.SobregiroRV.equalsIgnoreCase("S")?true:false;
						}
						else if (elemento.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
							tengo_sobregiro = indicadoresPortafolio.SobregiroIF.equalsIgnoreCase("S")?true:false;
						}
						else {
							tengo_sobregiro = false;
						}
 
						//CASO NO IF
						if(!"IF".equalsIgnoreCase(elemento.IdClasificacionTipoInstrumento)) {
							//Si tengo 0 disponible y no puedo hacer venta descubierta, jodi
							if (objCARDIS.Cantidad.compareTo(BigDecimal.ZERO)== 0 && ("N").equalsIgnoreCase(indicadoresPortafolio.PermiteVentaDescubierta) && !tengo_sobregiro) { // && no tengo sobregiro
								Errores.add(elemento);
							}
							//si tengo disponible o tengo venta descubierta habilitada o tengo sobregiro, avanzo en el flujo
							else {						  
								//lo que tengo que retirar no me alcanza con el disponible
							  
								if (elemento.Cantidad.compareTo(objCARDIS.Cantidad) == 1 && !tengo_sobregiro) {
									//caso en que no tengo flujo de venta descubierta
									if (objVENDES!=null && objVENDES.Cantidad.compareTo(BigDecimal.ZERO) == -1) {
										Errores.add(elemento);
									} 
									//caso en que si tengo flujo con venta descubierta
									else {
										//validaciones pencas para venta descubierta
										if (("S").equalsIgnoreCase(indicadoresPortafolio.PermiteVentaDescubierta) && ("T2").equalsIgnoreCase(elemento.IdCondicionLiquidacion) && ("VENACN").equalsIgnoreCase(elemento.IdTipoOperacion)) {
											IngresoCustodia.ObjDatosEntrada	ObjEntradaVDesMvtoTran = new IngresoCustodia.ObjDatosEntrada();						
												
											ControlRetiroCustodia.ControlRetiroObjDatosEntrada	ObjEntradaVDes = new ControlRetiroCustodia.ControlRetiroObjDatosEntrada();
												
											//Retiro Venta Descubierta
											if (!(objCARDIS.Cantidad.compareTo(BigDecimal.ZERO)== 0)) {
												ObjEntradaVDes.IdGenerarMovimientoCustodia		= elemento.IdGenerarMovimientoCustodia;						
												ObjEntradaVDes.TablaAfectada						= "CARTERA";
												ObjEntradaVDes.AccionOperacion					= "RETIRO";
												ObjEntradaVDes.IdMovimientoCarteraEnTransitoOrigen = elemento.IdMovimientoCarteraEnTransitoOrigen;
												ObjEntradaVDes.IdCliente							= elemento.IdCliente; 
												ObjEntradaVDes.IdPortafolio						= elemento.IdPortafolio;
												ObjEntradaVDes.FechaOperacion						= elemento.FechaOperacion;
												ObjEntradaVDes.IdTipoOperacion					= elemento.IdTipoOperacion;		
												ObjEntradaVDes.IdConcepto							= elemento.IdConcepto; //RETTRS
												ObjEntradaVDes.IdCondicionLiquidacion				= elemento.IdCondicionLiquidacion; 
												ObjEntradaVDes.FechaLiquidacion					= elemento.FechaLiquidacion;
												ObjEntradaVDes.IdTipoCanalIngreso					= elemento.IdTipoCanalIngreso; 		
												ObjEntradaVDes.IdTipoComprobante					= elemento.IdTipoComprobante; 
												ObjEntradaVDes.FolioComprobante					= elemento.FolioComprobante;
												ObjEntradaVDes.FolioExternoReferencia				= elemento.FolioExternoReferencia;
												ObjEntradaVDes.IdUsuario							= p_IdUsuario;
												ObjEntradaVDes.IdSucursal							= elemento.IdSucursal; 
												ObjEntradaVDes.IdAgente							= elemento.IdAgente;
												ObjEntradaVDes.Glosa								= elemento.Glosa;
												ObjEntradaVDes.IdInstrumento						= elemento.IdInstrumento;
												ObjEntradaVDes.IdMonedaInstrumento				= elemento.IdMonedaInstrumento;
												ObjEntradaVDes.IdMonedaEmision					= elemento.IdMonedaEmision;
												ObjEntradaVDes.IdTipoReajusteInstrumento			= elemento.IdTipoReajusteInstrumento;
												ObjEntradaVDes.IdEmisor							= elemento.IdEmisor;
												ObjEntradaVDes.EsFungible							= elemento.EsFungible;
												ObjEntradaVDes.IdBoveda							= elemento.IdBoveda;
												ObjEntradaVDes.EsNominal							= elemento.EsNominal;	
												ObjEntradaVDes.Cantidad							= objCARDIS.Cantidad; 
												ObjEntradaVDes.CorteMinimo						= elemento.CorteMinimo; 
												ObjEntradaVDes.ValorRescate						= elemento.ValorRescate; 
												ObjEntradaVDes.PrecioCompra						= elemento.PrecioCompra; 
												ObjEntradaVDes.TasaCompra							= elemento.TasaCompra;
												ObjEntradaVDes.MontoCompraMonedaInstrumento		= elemento.MontoCompraMonedaInstrumento;
												ObjEntradaVDes.IdModulo							= "FAC";
												ObjEntradaVDes.EsVentaDescubierta               	= "S";
											
												retirosFin.add(ObjEntradaVDes);
											}
											else if (SaldoCustodiaDisponibleOriginal.compareTo(BigDecimal.ZERO) == 0){
												idMovimientoTransitoOperacionVAN = elemento.IdMovimientoCarteraEnTransitoOrigen;
											
												final String updateMovimientoTransitoOperacionVAN = "UPDATE \"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito\" SET  \"IdEstado\" = 'CRT' WHERE \"IdMovimientoCarteraEnTransito\" = " + idMovimientoTransitoOperacionVAN;
												SQL.exec(conn, updateMovimientoTransitoOperacionVAN);
											}	
												
												
											//Movimiento en transito venta descubierta
											ObjEntradaVDesMvtoTran.IdGenerarMovimientoCustodia	= elemento.IdGenerarMovimientoCustodia;
											ObjEntradaVDesMvtoTran.TablaAfectada					= "TRANSITO";
											ObjEntradaVDesMvtoTran.AccionOperacion				= "RETIRO";
											ObjEntradaVDesMvtoTran.EsRegistroNuevo				= "S";
											ObjEntradaVDesMvtoTran.FolioCarteraOrigen				= 0L;	
											ObjEntradaVDesMvtoTran.FolioCustodia                	= 0L;
											ObjEntradaVDesMvtoTran.IdCliente						= elemento.IdCliente;
											ObjEntradaVDesMvtoTran.IdPortafolio					= elemento.IdPortafolio;
											ObjEntradaVDesMvtoTran.IdCuentaCustodioPortafolio		= 0L;
											ObjEntradaVDesMvtoTran.FechaOperacion					= Fecha.toDate(Fecha.toFormat(elemento.FechaOperacion,"yyyy-MM-dd"));
											ObjEntradaVDesMvtoTran.FechaVencimientoOperacion		= Fecha.toDate(Fecha.toFormat(elemento.FechaOperacion,"yyyy-MM-dd"));
											ObjEntradaVDesMvtoTran.FechaVencimientoInstrumento	= Fecha.toDate(Fecha.toFormat(elemento.FechaOperacion,"yyyy-MM-dd"));
											ObjEntradaVDesMvtoTran.PlazoEnDiasOperacion			= 0L;
											ObjEntradaVDesMvtoTran.IdTipoOperacion				= "RETTIT";
											ObjEntradaVDesMvtoTran.IdTipoProducto					= elemento.IdTipoProducto;
											ObjEntradaVDesMvtoTran.IdConcepto						= "VTADES";
											ObjEntradaVDesMvtoTran.IdCondicionLiquidacion			= "T0"; 
											ObjEntradaVDesMvtoTran.FechaLiquidacion				= Fecha.toDate(fechaSistema);
											ObjEntradaVDesMvtoTran.IdTipoCanalIngreso				= elemento.IdTipoCanalIngreso; 		
											ObjEntradaVDesMvtoTran.CodigoIFRS						= "";
											ObjEntradaVDesMvtoTran.NemoSVS						= elemento.NemoSVS;
											ObjEntradaVDesMvtoTran.Nemotecnico					= elemento.Nemotecnico;
											ObjEntradaVDesMvtoTran.IdTipoComprobante				= elemento.IdTipoComprobante; 
											ObjEntradaVDesMvtoTran.FolioComprobante				= elemento.FolioComprobante; 
											ObjEntradaVDesMvtoTran.FolioExternoReferencia			= "";
											ObjEntradaVDesMvtoTran.IdOperacion					= elemento.IdOperacion;
											ObjEntradaVDesMvtoTran.IdBolsa						= "";	
											ObjEntradaVDesMvtoTran.IdUsuario						= p_IdUsuario;
											ObjEntradaVDesMvtoTran.IdSucursal						= elemento.IdSucursal; 
											ObjEntradaVDesMvtoTran.IdAgente						= elemento.IdAgente;
											ObjEntradaVDesMvtoTran.Glosa							= elemento.Glosa;
											ObjEntradaVDesMvtoTran.IdInstrumento					= elemento.IdInstrumento;
											ObjEntradaVDesMvtoTran.IdInstrumentoAnterior			= 0L;
											ObjEntradaVDesMvtoTran.IdMonedaInstrumento			= elemento.IdMonedaInstrumento;
											ObjEntradaVDesMvtoTran.IdMonedaEmision				= elemento.IdMonedaEmision;
											ObjEntradaVDesMvtoTran.IdTipoReajusteInstrumento		= elemento.IdTipoReajusteInstrumento;
											ObjEntradaVDesMvtoTran.IdEmisor						= elemento.IdEmisor;
											ObjEntradaVDesMvtoTran.EsFungible						= elemento.EsFungible;
											ObjEntradaVDesMvtoTran.IdBoveda						= elemento.IdBoveda;
											ObjEntradaVDesMvtoTran.EsNominal						= "N";			
											ObjEntradaVDesMvtoTran.Cantidad						= elemento.Cantidad.subtract(objCARDIS.Cantidad).abs(); 
											ObjEntradaVDesMvtoTran.CorteMinimo					= elemento.CorteMinimo; 
											ObjEntradaVDesMvtoTran.ValorRescate					= elemento.ValorRescate; 
											ObjEntradaVDesMvtoTran.PrecioCompra					= elemento.PrecioCompra; 
											ObjEntradaVDesMvtoTran.TasaCompra						= elemento.TasaCompra;
											ObjEntradaVDesMvtoTran.MontoCompra					= elemento.MontoCompraMonedaInstrumento;
											ObjEntradaVDesMvtoTran.MontoCompraMonedaInstrumento	= elemento.MontoCompraMonedaInstrumento;
											ObjEntradaVDesMvtoTran.EsVentaCorta					= "N";
											ObjEntradaVDesMvtoTran.EsVentaDescubierta		    	= "S";
									
											transitosVDes.add(ObjEntradaVDesMvtoTran);
											objVENDES.Cantidad = objVENDES.Cantidad.abs().subtract(elemento.Cantidad, MathContext.DECIMAL128);
											objCARDIS.Cantidad = BigDecimal.ZERO;
										}
										else {
											Errores.add(elemento);
										}
									}
								}
								//me alcanza para retirar
								else if (elemento.Cantidad.compareTo(objCARDIS.Cantidad) <= 0 || tengo_sobregiro) {
									if (tengo_sobregiro) {
										((ControlRetiroCustodia.ControlRetiroObjDatosEntrada)elemento).PermiteSobregiro = true;
									}
									retirosFin.add(elemento);
									//retirar cartera de operacion a cartera disponible del flujo entregado
									objCARDIS.Cantidad = objCARDIS.Cantidad.subtract(elemento.Cantidad, MathContext.DECIMAL128);
								}
							}						  
						}
						//Caso para IF
						else {//if(!"IF".equalsIgnoreCase(elemento.IdClasificacionTipoInstrumento))
							if (elemento.ValorRescate.compareTo(objCARDIS.ValorRescate) <= 0 || tengo_sobregiro) {
								if (tengo_sobregiro) {
									((ControlRetiroCustodia.ControlRetiroObjDatosEntrada)elemento).PermiteSobregiro = true;
								}
								retirosFin.add(elemento);
						         
								//retirar cartera de operacion a cartera disponible del flujo entregado
								objCARDIS.ValorRescate = objCARDIS.ValorRescate.subtract(elemento.ValorRescate, MathContext.DECIMAL128);
							}
							else {
								Errores.add(elemento);
							}
						}//else
					} //Aqui if (objCARDIS != null) {					
				}
			}
			 			
			//Retiro Custodia Else venta descubierta
			if(transitosVDes.size()> 0) {
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
		        IngresoCustodia.IngresoCustodiaEntrada paramInSPVDes = new IngresoCustodia.IngresoCustodiaEntrada();
		        paramInSPVDes.p_IdEmpresa				= p_IdEmpresa;		
		        paramInSPVDes.p_IdModulo				= p_IdModulo;
		        paramInSPVDes.p_IdUsuario				= p_IdUsuario;
		        paramInSPVDes.p_SimularOperacion		= p_simularOperacion;
		        paramInSPVDes.p_TicketAtencion 			= 0L;
		        paramInSPVDes.p_ValorizacionAutomatica	= "N";
		        paramInSPVDes.p_DatosEntrada			= transitosVDes;
		        
			    IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSPVDes);	 //5***
			}
			
			
			if(retirosFin.size()> 0) {
				Map<java.sql.Date, List<RetiroCustodia.ObjDatosEntrada>> retirosMap = new TreeMap<>();
				
				if(EnviaFacturacionSeparadaPorFechaLiquidacion.equalsIgnoreCase("S")) {
					retirosMap = retirosFin.stream().collect(Collectors.groupingBy(x -> x.FechaLiquidacion, TreeMap::new,Collectors.toList()						              ));
				}else {
					retirosMap.put(Fecha.toDate("0001-01-01"), retirosFin);
					
				}
				
				retirosMap.forEach((fecha, var) -> {
					try{
						ControlRetiroCustodia retirosCustodia = new ControlRetiroCustodia();
						ControlRetiroCustodiaEntrada paramInSP = new ControlRetiroCustodiaEntrada();
						paramInSP.p_IdEmpresa			= p_IdEmpresa;		
						paramInSP.p_IdModulo			= p_IdModulo;
						paramInSP.p_IdUsuario			= p_IdUsuario;
						paramInSP.p_ValorizacionAutomatica = "N";
						paramInSP.p_SimularOperacion	= p_simularOperacion;
						paramInSP.p_TicketAtencion 		= 0L;
						paramInSP.p_DatosEntrada		= var;				
						RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP);	//7***			
					
						for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
							for (GrupoOperacionCustodia grupo : grupos) {
								
								if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
									continue;
								}
								
								if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
									grupo.pasos.get(0).estado = "OK";
									grupo.estado = "OK";
									
									if (datoSalida.TablaOrigen.equalsIgnoreCase("NEGATIVA")) {
										String codigo = "UI_CARTERA_NEGATIVA_INGRESADA";
										//AGREGAR NEMOSVS a la salida 
										String nemo = grupo.TransitoPrincipal.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")?grupo.TransitoPrincipal.NemoSVS:grupo.TransitoPrincipal.Nemotecnico;
										String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.IdCustodia).add("Nemotecnico",nemo).msg();
										grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
									}
									else {
										String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
										String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
										grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));	
									}
									
								}
								else {
									grupo.pasos.get(0).estado = "ERR";
									grupo.estado = "ERR";
									
									String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
									String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
											.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
									grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
									
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();	
					}
				});
				

		    }	
		}
		HashMap<Long, Object> ErroresHash = Utils.List2Hash(Errores, "IdGenerarMovimientoCustodia");
			
		for (GrupoOperacionCustodia grupo : grupos) {
			RetiroCustodia.ObjDatosEntrada err = (RetiroCustodia.ObjDatosEntrada) ErroresHash.get(grupo.IdMovimientoInicial);
			if (err != null){
				HashMap<String,String> mapMsg = new HashMap<>();
				grupo.pasos.get(0).estado = "ERR";
				grupo.estado = "ERR";						
				
				String codigo = "CUSTODIA_SALDO_DISPONIBLE_NO_ES_SUFICIENTE_CLIENTE"; 
				mapMsg.put("NombreCliente", String.valueOf(err.NombreCliente));
				mapMsg.put("CodigoPortafolio", String.valueOf(err.CodigoPortafolio));
				mapMsg.put("RutCliente", grupo.TransitoPrincipal.DocumentoIdentidad);
				mapMsg.put("Nemotecnico", err.NemoSVS);
				grupo.pasos.get(0).mensajes.add(new MensajeSalida(err.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,mapMsg,conn,p_IdEmpresa));
			}
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
				grupo.retirosFin=(retirosFin);
				
				
			}
		}
		
		return grupos;
	}
	
	
	public static class PrestamoVentaCorta {
		 public Long IdInstrumentoPrestamo;
		 public String CobraPrimaAlInicio;
		 public String IdEmisor;
		 public String TipoPrestamoSistemaExterno;
		 public String IdClasificacionTipoInstrumento;
	}
	
	private List<GrupoOperacionCustodia> ProcesarVentasCortasNominales(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
		SQL _sql = new SQL();
		List<PrestamoVentaCorta> _infoPrestamos = _sql.query(conn, SQL_PRESTAMO_VENTA_CORTA, new HashMap<String,Object>(), PrestamoVentaCorta.class);
		if (_infoPrestamos.size() != 2) {
			throw new Exception ("Prestamo venta corta mal configurado");
		}
		PrestamoVentaCorta infoPrestamo;
		String prestamosRentaFija = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "CUS.PrestamosRentaFija");
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			{
				ObjMovimientosEnTransito movTransito = grupo.TransitoPrincipal;
				infoPrestamo = _infoPrestamos.stream()
						.filter( prestamo_tipo -> prestamo_tipo.IdClasificacionTipoInstrumento.equals( movTransito.IdClasificacionTipoInstrumento ) )
						.collect( Collectors.toList() )
						.get(0);
				String folioSistemaExterno = informarVentaCortaEnBlockChain( conn, p_IdEmpresa, p_IdUsuario, movTransito, prestamosRentaFija, infoPrestamo );
				
				cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= movTransito.IdCliente; 
				ObjEntrada.IdPortafolio						= movTransito.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		= movTransito.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					= movTransito.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= movTransito.FechaVencimientoOperacion;
				ObjEntrada.FechaVencimientoInstrumento		= movTransito.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= movTransito.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion					= "PRESTA";
				ObjEntrada.IdTipoPrestamo					= movTransito.IdClasificacionTipoInstrumento.equals("RV") ? "PRTVTC" :  "PRVCRF";
				ObjEntrada.CobraPrimaAlInicio 				= infoPrestamo.CobraPrimaAlInicio;
				ObjEntrada.IdConcepto						= movTransito.IdConcepto;		
				ObjEntrada.IdCondicionLiquidacion			= movTransito.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= movTransito.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= movTransito.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						= movTransito.CodigoIFRS;
				ObjEntrada.NemoSVS							= movTransito.NemoSVS;
				ObjEntrada.IdTipoComprobante				= movTransito.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= movTransito.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			= movTransito.FolioExternoReferencia;
				ObjEntrada.IdBolsa							= movTransito.IdBolsa;	
				ObjEntrada.IdUsuario						= movTransito.IdUsuario;
				ObjEntrada.IdSucursal						= movTransito.IdSucursal; 
				ObjEntrada.IdAgente							= movTransito.IdAgente;
				ObjEntrada.Glosa							= movTransito.Glosa;
				ObjEntrada.IdInstrumento					= infoPrestamo.IdInstrumentoPrestamo;			//PRESTA
				ObjEntrada.IdMonedaInstrumento				= movTransito.IdMonedaInstrumento;		//MONEDA PRESTA
				ObjEntrada.IdMonedaEmision					= movTransito.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= movTransito.IdTipoReajuste;
				ObjEntrada.IdEmisor							= infoPrestamo.IdEmisor;
				ObjEntrada.EsFungible						= movTransito.EsFungible;
				ObjEntrada.IdBoveda							= movTransito.IdBoveda;
				ObjEntrada.EsNominal						= movTransito.EsNominal;			
				ObjEntrada.Cantidad							= movTransito.Cantidad; 
				ObjEntrada.CorteMinimo						= movTransito.CorteMinimo; 
				ObjEntrada.ValorRescate						= movTransito.ValorRescate; 
				ObjEntrada.PrecioCompra						= movTransito.PrecioCompra; 
				ObjEntrada.TasaCompra						= movTransito.TasaCompra;
				ObjEntrada.MontoCompra						= movTransito.MontoCompra;
				ObjEntrada.MontoCompraMonedaInstrumento		= movTransito.MontoCompraMonedaInstrumento;
				ObjEntrada.PrecioAPlazo						= movTransito.PrecioAPlazo;
				ObjEntrada.MontoAPlazo						= movTransito.MontoAPlazo;
				ObjEntrada.IdTipoProducto					= "PRES";
				ObjEntrada.FolioSistemaExterno				= folioSistemaExterno;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCABECERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
			}
			
			
		
		}
		
		if (!logProcesoCrearEmprestitoBlockChain.isEmpty()) {
			_sql.batch( conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoCrearEmprestitoBlockChain, LogProcesoPrestamoBlockChain.class );	
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //8***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					Paso p = grupo.pasos.get(0);
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						p.estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		
		
		//3
		ingresos.clear();
				
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			List<Object> movsCabecera = (List<Object>) grupo.pasos.get(0).resultado;
			
			//cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida movIngreso = (cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida) grupo.pasos.get(0).resultado;
			//cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida movCabecera = (cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida) grupo.pasos.get(1).resultado;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "SUBYACENTE";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.IdClasificacionTipoInstrumento.equals(MERCADO_RENTA_FIJA) ? mov.FechaVencimientoInstrumento : mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= "PRESTA";
			if(mov.IdClasificacionTipoInstrumento.equals("RV")){
				ObjEntrada.IdTipoPrestamo					= "PRTVTC";					
			}else {
				ObjEntrada.IdTipoPrestamo					= "PRVCRF";
			}
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.IdClasificacionTipoInstrumento.equals(MERCADO_RENTA_FIJA) ? mov.TasaTIR : BigDecimal.ZERO;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.PrecioAPlazo						= mov.PrecioAPlazo;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;
			ObjEntrada.IdTipoProducto					= "PRES";
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
			
		
		}
				
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "S";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //9***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					grupo.pasos.get(1).resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(1).estado = "OK";
						
						//NO ES NECESARIO INDICAR QUE SE CREO EL SUBYACENTE
						//String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						//grupo.pasos.get(2).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(1).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_SUBYACENTE_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
		return grupos;
		
		
	}
	
	
	private List<GrupoOperacionCustodia> ProcesarDividendosGarantias(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
		
		//0
		ingresos.clear();
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			{
				ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
				cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= mov.IdCliente; 
				ObjEntrada.IdPortafolio						= mov.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					= mov.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
				ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
				ObjEntrada.IdConcepto						= mov.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
				ObjEntrada.NemoSVS							= mov.NemoSVS;
				ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= mov.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
				ObjEntrada.IdBolsa							= mov.IdBolsa;	
				ObjEntrada.IdUsuario						= mov.IdUsuario;
				ObjEntrada.IdSucursal						= mov.IdSucursal; 
				ObjEntrada.IdAgente							= mov.IdAgente;
				ObjEntrada.Glosa							= mov.Glosa;
				ObjEntrada.IdInstrumento					= mov.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
				ObjEntrada.IdEmisor							= mov.IdEmisor;
				ObjEntrada.EsFungible						= mov.EsFungible;
				ObjEntrada.IdBoveda							= mov.IdBoveda;
				ObjEntrada.EsNominal						= mov.EsNominal;			
				ObjEntrada.Cantidad							= mov.Cantidad; 
				ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
				ObjEntrada.ValorRescate						= mov.ValorRescate; 
				ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
				ObjEntrada.TasaCompra						= mov.TasaCompra;
				ObjEntrada.MontoCompra						= mov.MontoCompra;
				ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
				ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCARTERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
			}
			
		
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //14***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					Paso p = grupo.pasos.get(0);
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						p.estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		//1
		List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			List<Object> movsIngreso = (List<Object>) grupo.pasos.get(0).resultado;
			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "RETIRO";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCustodia						= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsIngreso.get(0)).IdCustodia;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;	
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdModulo							= "FAC";
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			retiros.add(ObjEntrada);
			
			Paso p = new Paso("RETIROCARTERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
		}
		
		if (retiros.size() > 0) {
			RetiroCustodia retirosCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;	
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_ValorizacionAutomatica = "N";
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= retiros;				
			RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //10***
			
			for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					Paso p = grupo.pasos.get(1);
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						p.estado = "OK";
						p.resultado.add(datoSalida);
						grupo.estado = "OK";
						
						//NO ES NECESARIO MOSTRAR EL RETIRO
						//String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						//grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						p.estado = "ERR";
						grupo.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
						
					}
				}
			}
		}
		
		SQL _sql = new SQL();
		List<ContraparteGarantia_Proceso> contraparte = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE, new HashMap<>(), ContraparteGarantia_Proceso.class);
		
		if (contraparte.size() == 0) {
			MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRAPARTE_GARANTIA_NO_CONFIGURADA","");
			String desc = Diccionario.getMensaje(conn, p_IdEmpresa,msg.codigo, new HashMap());
			msg.descripcion = desc;
			
			for (GrupoOperacionCustodia grupo : grupos) {
				grupo.estado = "ERR";
				grupo.pasos.get(0).mensajes.add(msg);
			}
		}
		
		//2 INGRESO GARANTIA
		ingresos.clear();
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			List<Object> movsRetiro = (List<Object>) grupo.pasos.get(1).resultado;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.IdInstrumentoCarteraGarantizada	= 0L;
			ObjEntrada.IdContraparteGarantia 			= contraparte.get(0).IdContraparteGarantia;
			ObjEntrada.RazonSocialContraparteGarantia 	= contraparte.get(0).RazonSocialContraparteGarantia;
			ObjEntrada.IdOperacion						= mov.IdOperacion;
			ObjEntrada.IdCarteraOrigen					= ((cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida)movsRetiro.get(0)).IdCustodia;
			ObjEntrada.FolioCarteraOrigen				= ((cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida)movsRetiro.get(0)).FolioCustodia;
			ObjEntrada.IdOrigenGarantia					= "CUS";
			ObjEntrada.TablaAfectada					= "GARANTIA";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= "INGGTA";
			ObjEntrada.IdOperacion						= mov.IdOperacion;
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOGARANTIA", ObjEntrada, grupo);
			grupo.pasos.add(p);
			
		
		}
				
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //15***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					Paso p = grupo.pasos.get(2);
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						p.estado = "OK";
						
						String codigo = "UI_GARANTIA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_GARANTIA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
				
				
			}
		}
		
		
		
		return grupos;
		
	}

	
	
	private List<GrupoOperacionCustodia> ProcesarCompraSimultaneas(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
	
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
		
		//0 encabezados
		for (GrupoOperacionCustodia grupo : grupos) {
			
			{
				ObjMovimientosEnTransito movPrincipal = grupo.TransitoPrincipal;
				
				IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = movPrincipal.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= movPrincipal.IdCliente; 
				ObjEntrada.IdPortafolio						= movPrincipal.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		= movPrincipal.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					= movPrincipal.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.FechaVencimientoInstrumento		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= movPrincipal.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion					= movPrincipal.IdTipoOperacion;		
				ObjEntrada.IdConcepto						= movPrincipal.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion			= movPrincipal.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= movPrincipal.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= movPrincipal.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						= movPrincipal.CodigoIFRS;
				ObjEntrada.NemoSVS							= movPrincipal.NemoSVS;
				ObjEntrada.IdOperacion						= movPrincipal.IdOperacion;
				ObjEntrada.IdTipoComprobante				= movPrincipal.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= movPrincipal.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			= movPrincipal.FolioExternoReferencia;
				ObjEntrada.IdBolsa							= movPrincipal.IdBolsa;	
				ObjEntrada.IdUsuario						= movPrincipal.IdUsuario;
				ObjEntrada.IdSucursal						= movPrincipal.IdSucursal; 
				ObjEntrada.IdAgente							= movPrincipal.IdAgente;
				ObjEntrada.Glosa							= movPrincipal.Glosa;
				ObjEntrada.IdInstrumento					= movPrincipal.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				= movPrincipal.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					= movPrincipal.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= movPrincipal.IdTipoReajuste;
				ObjEntrada.IdEmisor							= movPrincipal.IdEmisor;
				ObjEntrada.EsFungible						= movPrincipal.EsFungible;
				ObjEntrada.IdBoveda							= movPrincipal.IdBoveda;
				ObjEntrada.EsNominal						= movPrincipal.EsNominal;		
				ObjEntrada.Cantidad							= movPrincipal.Cantidad; 
				ObjEntrada.CorteMinimo						= movPrincipal.CorteMinimo; 
				ObjEntrada.ValorRescate						= movPrincipal.ValorRescate; 
				ObjEntrada.PrecioCompra						= movPrincipal.PrecioCompra; 
				ObjEntrada.TasaCompra						= movPrincipal.TasaCompra;
				ObjEntrada.MontoCompra						= movPrincipal.MontoCompra;
				ObjEntrada.IdTipoProducto					= movPrincipal.IdTipoProducto;
				
				if ("S".equalsIgnoreCase(movPrincipal.EsNominal)) {
					BigDecimal tasa 							= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal periodos 						= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
					BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));			
					ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn, movPrincipal.PrecioCompra.multiply(factor), MonedaFormato.PRECIO, movPrincipal.IdMonedaInstrumento).valor;
			
				}else {
					if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("COMRRV")) {
						BigDecimal tasa 							=  movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						BigDecimal periodos 						= new BigDecimal( movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(360),20,RoundingMode.HALF_UP);
						Double factor 								= Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
						
						ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn,  movPrincipal.PrecioCompra.multiply(new BigDecimal(factor)), MonedaFormato.PRECIO,  movPrincipal.IdMonedaInstrumento).valor;
					}
					else if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("COMSIM")) {
						BigDecimal tasa 							=  movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						BigDecimal periodos 						= new BigDecimal( movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
						BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));
						
						ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn,  movPrincipal.PrecioCompra.multiply(factor), MonedaFormato.PRECIO,  movPrincipal.IdMonedaInstrumento).valor;
					}
					else if (ObjEntrada.IdTipoOperacion.equalsIgnoreCase("COMRRF")) {
						ObjEntrada.PrecioAPlazo 				=  movPrincipal.MontoCompra.divide( movPrincipal.Cantidad,20,RoundingMode.HALF_UP);
					}
					else {
						ObjEntrada.PrecioAPlazo					= BigDecimal.ZERO;
					}
					
				}
				
				ObjEntrada.MontoAPlazo 						= movPrincipal.Cantidad.multiply(ObjEntrada.PrecioAPlazo);
				ObjEntrada.MontoCompraMonedaInstrumento		= movPrincipal.MontoCompraMonedaInstrumento;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCABECERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
			}
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //16***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					Paso p = grupo.pasos.get(0); 
					p.resultado.add(datoSalida);
					
					 movsCabecera = (List<Object>)p.resultado ;  // se almacena valor para tercer movimiento
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(0).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		
		//1   CARTERA prestada , FUNCION DISPONIBLE EN SOLO PARA CUSTODIA EN ESTE METODO, 
		//    PARA NOMINALES  UTILIZA ProcesarCompraSimTransito
		
		ingresos.clear();
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
				continue;
			}
			

			{
				ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;
				cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				
				List<Object> movsCabecera = (List<Object>) grupo.pasos.get(0).resultado;
								
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen =  mov.IdMovimientoCarteraEnTransito;
				ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;			
				ObjEntrada.IdCliente						=  mov.IdCliente; 
				ObjEntrada.IdPortafolio						=  mov.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		=  mov.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					=  mov.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= Fecha.toDate("0001-01-01");
				ObjEntrada.FechaVencimientoInstrumento		= Fecha.toDate("0001-01-01");
				ObjEntrada.PlazoEnDiasOperacion				= 0L;
				ObjEntrada.IdTipoOperacion					=  mov.IdTipoOperacion;		
				ObjEntrada.IdConcepto						=  mov.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion			=  mov.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					=  mov.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				=  mov.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						=  mov.CodigoIFRS;
				ObjEntrada.NemoSVS							=  mov.NemoSVS;
				ObjEntrada.IdTipoComprobante				=  mov.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					=  mov.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			=  mov.FolioExternoReferencia;
				ObjEntrada.IdBolsa							=  mov.IdBolsa;	
				ObjEntrada.IdUsuario						=  mov.IdUsuario;
				ObjEntrada.IdSucursal						=  mov.IdSucursal; 
				ObjEntrada.IdAgente							=  mov.IdAgente;
				ObjEntrada.Glosa							=  mov.Glosa;
				ObjEntrada.IdInstrumento					=  mov.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				=  mov.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					=  mov.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		=  mov.IdTipoReajuste;
				ObjEntrada.IdEmisor							=  mov.IdEmisor;
				ObjEntrada.EsFungible						=  mov.EsFungible;
				ObjEntrada.IdBoveda							=  mov.IdBoveda;
				ObjEntrada.EsNominal						=  mov.EsNominal;			
				ObjEntrada.Cantidad							=  mov.Cantidad; 
				ObjEntrada.CorteMinimo						=  mov.CorteMinimo; 
				ObjEntrada.ValorRescate						=  mov.ValorRescate; 
				ObjEntrada.PrecioCompra						=  mov.PrecioCompra; 
				ObjEntrada.TasaCompra						=  mov.TasaCompra;
				ObjEntrada.MontoCompra						=  mov.MontoCompra;
				ObjEntrada.MontoCompraMonedaInstrumento		=  mov.MontoCompraMonedaInstrumento;
				ObjEntrada.IdTipoProducto					=  mov.IdTipoProducto;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCARTERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
			}
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //17***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					Paso p = grupo.pasos.get(1);
					p.resultado.add(datoSalida);					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {						
						grupo.estado = "OK";
						p.estado = "OK";						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {						
						grupo.estado = "ERR";
						p.estado = "ERR";						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}				
			}
		}
		
		
		//2
		ingresos.clear();
				
		for (GrupoOperacionCustodia grupo : grupos) {
			
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;
	
			Long IdCarteraOrigen = 0L;
			
			if (!"S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
				List<Object> movsIngreso = (List<Object>) grupo.pasos.get(1).resultado;
				IdCarteraOrigen	= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsIngreso.get(0)).IdCustodia;
			}
			
			List<Object> movsCabecera = (List<Object>) grupo.pasos.get(0).resultado;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "SUBYACENTE";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;
			ObjEntrada.IdCarteraOrigen                  = IdCarteraOrigen;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
			
		
		}
				
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //18***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
			
					Paso p = grupo.pasos.get(1);
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						p.estado = "OK";
						
						//NO ES NECESARIO INDICAR QUE SE CREO EL SUBYACENTE
						//String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						//grupo.pasos.get(2).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_SUBYACENTE_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}	

			//3 RETIRO CARTERA   //FUNCION DISPONIBLE EN ProcesarCompraSimTransito

			//4 INGRESO GARANTIA //FUNCION DISPONIBLE EN ProcesarCompraSimTransito
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV2 = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV2.IdMovimientoCarteraEnTransito = grupo.TransitoRelacionado.IdMovimientoCarteraEnTransito;
				MOV2.EsCertificacion = "S";
				MOV2.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV2);
			}
		}
		
		return grupos;
	}
	
	private List<GrupoOperacionCustodia> ProcesarComprasConCustodia(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
		
		for (GrupoOperacionCustodia grupo : grupos) {
			
			ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= mov.IdTipoOperacion.equals("EMILIB") && mov.Cantidad.longValue() < 0 ? "NEGATIVA":"CARTERA";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCarteraOrigen				= 0L;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.Nemotecnico						= mov.Nemotecnico;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.IdTipoOperacion.equals("EMILIB") && mov.Cantidad.longValue() < 0 ? mov.Cantidad.multiply(BigDecimal.valueOf(-1)) : mov.Cantidad;
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ObjEntrada.IdMesa							= mov.IdMesa;
			ObjEntrada.FolioCustodia					= mov.FolioCustodia;
			ObjEntrada.IdMovimientoComprobante			= 0L;								
			
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOCARTERA", ObjEntrada, grupo);
			grupo.pasos.add(p);
			
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //20***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			List<ClienteNeteo> clientesANetear = new ArrayList<>();
			Long idEmpresa = null;
			Long idUsuario = null;
			
			String operacionesParam = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "CUSTODIA", "CUS.OperacionesNeteo");
			List<String> operaciones = new ArrayList<>();
			
			try {
				String[] array = operacionesParam.split(",");
				operaciones = Arrays.asList(array);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					grupo.pasos.get(0).resultado.add(datoSalida);
					
					if (operaciones.contains(grupo.TransitoPrincipal.IdTipoOperacion)) {
						
						if (idEmpresa == null) idEmpresa = grupo.TransitoPrincipal.IdEmpresa;
						if (idUsuario == null) idUsuario = grupo.TransitoPrincipal.IdUsuario;
						
						ClienteNeteo c = new ClienteNeteo();
						c.IdInstrumento = grupo.TransitoPrincipal.IdInstrumento;
						c.IdPortafolio = grupo.TransitoPrincipal.IdPortafolio;
						clientesANetear.add(c);
					}
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(0).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(0).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
			}
			
			if (clientesANetear.size() > 0) {
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				
				EntradaNeteo eee = new EntradaNeteo();
				eee.p_IdEmpresa = idEmpresa;
				eee.p_IdModulo = "FAC";
				eee.p_IdUsuario = idUsuario;
				eee.p_PortafoliosInstrumento = clientesANetear;
				eee.p_SimularOperacion = "N";
				eee.p_UsaReglasNegocio = "S";
				
				AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
				paramsIn.p_IdEmpresa		= idEmpresa;
				paramsIn.p_IdModulo			= "FAC";
				paramsIn.p_IdUsuario		= idUsuario;
				paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
				paramsIn.p_Segundos			= 0L;
				paramsIn.p_FechaSistema		= fechaSistema;
				List<Parametro> paramsParametros = new ArrayList<>();
				Parametro ppp = new Parametro();
				ppp.NombreParametro = "Entrada";
				Gson gson = Utils.getGsonBuilder();
				ppp.Valor = "";
				ppp.ValorBlob = gson.toJson(eee);
				paramsParametros.add(ppp );
				paramsIn.p_Parametros		= paramsParametros ;
				AgregarPrograma AP = new AgregarPrograma();
				AP.logica(conn, paramsIn);
			}
			
		}
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
		return grupos;
		
	}
	
	
	
	private List<GrupoOperacionCustodia> ProcesarCompraSimTransito(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
			
			List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
//1
			ingresos.clear();
			
			for (GrupoOperacionCustodia grupo : grupos) {
				grupo.estado = "OK";
				
				if (!"S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
					continue;
				}

				{
		///		List<Object> movsCabecera = (List<Object>) grupo.pasos.get(0).resultado;
					ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
					cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
					ObjEntrada.TablaAfectada					= "CARTERA";
					ObjEntrada.AccionOperacion					= "INGRESO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
					ObjEntrada.FolioCarteraRelacionada			= BuscaFolioCarteraRelacionada(conn,mov.IdMovimientoCarteraEnTransitoRelacionado);
					ObjEntrada.IdCliente						= mov.IdCliente; 
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= mov.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
					ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
					ObjEntrada.IdConcepto						= mov.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.FolioComprobante;
					ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
					ObjEntrada.IdBolsa							= mov.IdBolsa;	
					ObjEntrada.IdUsuario						= mov.IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursal; 
					ObjEntrada.IdAgente							= mov.IdAgente;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.EsNominal;			
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
					ObjEntrada.TasaCompra						= mov.TasaCompra;
					ObjEntrada.MontoCompra						= mov.MontoCompra;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					
					ingresos.add(ObjEntrada);
					
					Paso p = new Paso("INGRESOCARTERA", ObjEntrada, grupo);
					grupo.pasos.add(p);
				}
				
			
			}
			
			if (ingresos.size() > 0) {
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSP.p_IdEmpresa			= p_IdEmpresa;		
				paramInSP.p_IdModulo			= p_IdModulo;
				paramInSP.p_IdUsuario			= p_IdUsuario;
				paramInSP.p_SimularOperacion	= p_simularOperacion;
				paramInSP.p_TicketAtencion 		= 0L;
				paramInSP.p_ValorizacionAutomatica	= "N";
				paramInSP.p_DatosEntrada		= ingresos;				
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //17***
				//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
				
				for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
					for (GrupoOperacionCustodia grupo : grupos) {
						
						if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
							continue;
						}
						Paso p = grupo.pasos.get(0);
						p.resultado.add(datoSalida);
						
						if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
							
							grupo.estado = "OK";
							p.estado = "OK";
							
							String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
							String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
						}
						else {
							
							grupo.estado = "ERR";
							p.estado = "ERR";
							
							String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
							String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
									.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
						}
					}
					
				}
			}
			
			
			String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
			
			
			//3 RETIRO CARTERA
        	if ("S".equalsIgnoreCase(pasaAGarantiaReporteSimultanea)) 
        	{

				List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
				Boolean EsNominal= false;
				for (GrupoOperacionCustodia grupo : grupos) {
					
					
					
					if (!"S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
						if (!grupo.estado.equalsIgnoreCase("OK")) {
							continue;
						}
					}

					ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;
					
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
					ObjEntrada.TablaAfectada					= "CARTERA";
					ObjEntrada.AccionOperacion					= "RETIRO";
					ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
					if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
						ObjEntrada.IdCustodia						= BuscaIdCartera(conn, mov.IdMovimientoCarteraEnTransito)  ;
					}else {
						ObjEntrada.IdCustodia 						= BuscaIdCartera(conn, mov.IdMovimientoCarteraEnTransito-1)  ;
					}
					ObjEntrada.IdCliente						= mov.IdCliente; 
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.FechaOperacion					= mov.FechaOperacion;
					ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
					ObjEntrada.IdConcepto						= mov.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){
						ObjEntrada.FechaVencimientoInstrumento	= mov.FechaVencimientoInstrumento;
						EsNominal= true;
					}		
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.FolioComprobante; 
					ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
					ObjEntrada.IdUsuario						= mov.IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursal; 
					ObjEntrada.IdAgente							= mov.IdAgente;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.EsNominal;	
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
					ObjEntrada.TasaCompra						= mov.TasaCompra;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
					ObjEntrada.IdModulo							= "FAC";
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					retiros.add(ObjEntrada);
					
					Paso p = new Paso("RETIROCARTERA", ObjEntrada, grupo);
					grupo.pasos.add(p);
				}
				
				if (retiros.size() > 0) {
					RetiroCustodia retirosCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
					paramInSP.p_IdEmpresa			= p_IdEmpresa;	
					paramInSP.p_IdModulo			= p_IdModulo;
					paramInSP.p_IdUsuario			= p_IdUsuario;
					paramInSP.p_ValorizacionAutomatica = "N";
					paramInSP.p_SimularOperacion	= p_simularOperacion;
					paramInSP.p_TicketAtencion 		= 0L;
					paramInSP.p_DatosEntrada		= retiros;				
					RetiroCustodia.RetiroCustodiaSalida resultVentas = retirosCustodia.logica(conn, paramInSP); //11***
					
					
					for (cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultVentas.P_OUT_DATOSSALIDA) {
						for (GrupoOperacionCustodia grupo : grupos) {
							
							if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
								continue;
							}
							
							Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  1 : 0;
							
							Paso p = grupo.pasos.get(x);

							if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
								p.estado = "OK";
								p.resultado.add(datoSalida);
								grupo.estado = "OK";
								
								//NO ES NECESARIO MOSTRAR EL RETIRO
								//String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
								//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								//grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
							}
							else {
								p.estado = "ERR";
								grupo.estado = "ERR";
								
								String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; //Error al retirar ::TipoCartera::, movimiento ::IdMovimientoComprobante::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
										.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
								
							}
						}
					}
				}
				
				SQL _sql = new SQL();
				List<ContraparteGarantia_Proceso> contraparte = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE, new HashMap<>(), ContraparteGarantia_Proceso.class);
				HashMap<String, Object> hashContraparte =null;
				if (!EsNominal){
					hashContraparte = Utils.List2Hash(contraparte, "IdEntidad");
				}
				if (contraparte.size() == 0) {
					MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRAPARTE_GARANTIA_NO_CONFIGURADA","");
					String desc = Diccionario.getMensaje(conn, p_IdEmpresa,msg.codigo, new HashMap());
					msg.descripcion = desc;
					
					for (GrupoOperacionCustodia grupo : grupos) {
						Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  1 : 0;
						grupo.estado = "ERR";
						grupo.pasos.get(x).mensajes.add(msg);
					}
				}
				
				//4 INGRESO GARANTIA
				ingresos.clear();
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.estado.equalsIgnoreCase("OK")) {
						continue;
					}
					
					List<ConceptoGarantia> concepto = _sql.query(conn, SQL_CONSULTAR_CONCEPTOS, new HashMap<>(), ConceptoGarantia.class);
					HashMap<String, Object> concepHash = Utils.List2Hash(concepto, "IdTipoOperacion");
					
					ObjMovimientosEnTransito mov = grupo.TransitoPrincipal;  //relacionado
					ObjMovimientosEnTransito movCabecera = grupo.TransitoPrincipal;
					
					Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  1 : 0;
					List<Object> movsRetiro = (List<Object>) grupo.pasos.get(x).resultado;
					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
					ObjEntrada.IdInstrumentoCarteraGarantizada	= movCabecera.IdInstrumento;
					if ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal) ){		
						ObjEntrada.IdContraparteGarantia 			= contraparte.get(0).IdContraparteGarantia;
						ObjEntrada.RazonSocialContraparteGarantia 	= contraparte.get(0).RazonSocialContraparteGarantia;
					}else{
						ContraparteGarantia_Proceso infoContraparte = ((ContraparteGarantia_Proceso) hashContraparte.get(mov.IdBolsa));
						ObjEntrada.IdContraparteGarantia 			= infoContraparte != null ? infoContraparte.IdContraparteGarantia : contraparte.get(0).IdContraparteGarantia;
						ObjEntrada.RazonSocialContraparteGarantia 	= infoContraparte != null ? infoContraparte.RazonSocialContraparteGarantia : contraparte.get(0).RazonSocialContraparteGarantia;	
					}
					ObjEntrada.IdTipoOperacionGestora			= movCabecera.IdTipoOperacion;
					ObjEntrada.IdOperacion						= mov.IdOperacion;
					ObjEntrada.IdCarteraOrigen					= ((cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida)movsRetiro.get(0)).IdCustodia;
					ObjEntrada.FolioCarteraOrigen				= ((cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida)movsRetiro.get(0)).FolioCustodia;
					ObjEntrada.IdOrigenGarantia					= "CUS";
					ObjEntrada.TablaAfectada					= "GARANTIA";
					ObjEntrada.AccionOperacion					= "INGRESO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
					ObjEntrada.IdCliente						= mov.IdCliente; 
					ObjEntrada.IdPortafolio						= mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion					= mov.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
					ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion					= "INGGTA";
					ObjEntrada.IdOperacion						= mov.IdOperacion;
					ObjEntrada.IdConcepto						= ((ConceptoGarantia) concepHash.get(mov.IdTipoOperacion)).IdConcepto;
					ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
					ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
					ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
					ObjEntrada.NemoSVS							= mov.NemoSVS;
					ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
					ObjEntrada.FolioComprobante					= mov.FolioComprobante;
					ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
					ObjEntrada.IdBolsa							= mov.IdBolsa;	
					ObjEntrada.IdUsuario						= mov.IdUsuario;
					ObjEntrada.IdSucursal						= mov.IdSucursal; 
					ObjEntrada.IdAgente							= mov.IdAgente;
					ObjEntrada.Glosa							= mov.Glosa;
					ObjEntrada.IdInstrumento					= mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
					ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
					ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
					ObjEntrada.IdEmisor							= mov.IdEmisor;
					ObjEntrada.EsFungible						= mov.EsFungible;
					ObjEntrada.IdBoveda							= mov.IdBoveda;
					ObjEntrada.EsNominal						= mov.EsNominal;		
					ObjEntrada.Cantidad							= mov.Cantidad; 
					ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
					ObjEntrada.ValorRescate						= mov.ValorRescate; 
					ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
					ObjEntrada.TasaCompra						= mov.TasaCompra;
					ObjEntrada.MontoCompra						= mov.MontoCompra;
					ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
					ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
					ingresos.add(ObjEntrada);
					
					Paso p = new Paso("INGRESOGARANTIA", ObjEntrada, grupo);
					grupo.pasos.add(p);
					
				
				}
						
				if (ingresos.size() > 0) {
					IngresoCustodia ingresoCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSP.p_IdEmpresa			= p_IdEmpresa;		
					paramInSP.p_IdModulo			= p_IdModulo;
					paramInSP.p_IdUsuario			= p_IdUsuario;
					paramInSP.p_SimularOperacion	= p_simularOperacion;
					paramInSP.p_TicketAtencion 		= 0L;
					paramInSP.p_ValorizacionAutomatica	= "N";
					paramInSP.p_DatosEntrada		= ingresos;				
					IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //19***
					//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
					
					for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
						for (GrupoOperacionCustodia grupo : grupos) {
							
							if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
								continue;
							}
							Integer  x = ("S".equalsIgnoreCase(grupo.TransitoPrincipal.EsNominal)) ?  2 : 1;		
							Paso p = grupo.pasos.get(x);

							p.resultado.add(datoSalida);
							
							if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
								
								grupo.estado = "OK";
								p.estado = "OK";
								
								String codigo = "UI_GARANTIA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
							}
							else {
								
								grupo.estado = "ERR";
								p.estado = "ERR";
								
								String codigo = "UI_ERROR_AL_CREAR_GARANTIA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
								String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
										.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
								p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
							}
						}
						
					}
				}
			}
			
			for (GrupoOperacionCustodia grupo : grupos) {
				
				if (grupo.estado.equalsIgnoreCase("OK")) {
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
					MOV.EsCertificacion = "S";
					MOV.IdUsuario = p_IdUsuario;
					grupo.transitosActualizar.add(MOV);
					
				}
			}
			
		
		return grupos;

	};
		
	private List<GrupoOperacionCustodia> ProcesarVentaSimTransito(Connection conn,Long p_IdEmpresa,Long p_IdUsuario, String p_IdModulo, String p_simularOperacion, List<GrupoOperacionCustodia> grupos) throws Exception {
	List<IngresoCustodia.ObjDatosEntrada> ingresos = new ArrayList<>();
	//0	
		for (GrupoOperacionCustodia grupo : grupos) {
			
			{
				ObjMovimientosEnTransito movPrincipal = grupo.TransitoPrincipal;
				
				IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
				ObjEntrada.TablaAfectada					= "CARTERA";
				ObjEntrada.AccionOperacion					= "INGRESO";
				ObjEntrada.EsRegistroNuevo					= "S";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = movPrincipal.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdCliente						= movPrincipal.IdCliente; 
				ObjEntrada.IdPortafolio						= movPrincipal.IdPortafolio;
				ObjEntrada.IdCuentaCustodioPortafolio		= movPrincipal.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion					= movPrincipal.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.FechaVencimientoInstrumento		= movPrincipal.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion				= movPrincipal.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion					= movPrincipal.IdTipoOperacion;	
				ObjEntrada.IdConcepto						= movPrincipal.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion			= movPrincipal.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion					= movPrincipal.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso				= movPrincipal.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS						= movPrincipal.CodigoIFRS;
				ObjEntrada.NemoSVS							= movPrincipal.NemoSVS;
				ObjEntrada.IdOperacion						= movPrincipal.IdOperacion;
				ObjEntrada.IdTipoComprobante				= movPrincipal.IdTipoComprobante; 
				ObjEntrada.FolioComprobante					= movPrincipal.FolioComprobante;
				ObjEntrada.FolioExternoReferencia			= movPrincipal.FolioExternoReferencia;
				ObjEntrada.IdBolsa							= movPrincipal.IdBolsa;	
				ObjEntrada.IdUsuario						= movPrincipal.IdUsuario;
				ObjEntrada.IdSucursal						= movPrincipal.IdSucursal; 
				ObjEntrada.IdAgente							= movPrincipal.IdAgente;
				ObjEntrada.Glosa							= movPrincipal.Glosa;
				ObjEntrada.IdInstrumento					= movPrincipal.IdInstrumento;
				ObjEntrada.IdMonedaInstrumento				= movPrincipal.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision					= movPrincipal.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento		= movPrincipal.IdTipoReajuste;
				ObjEntrada.IdEmisor							= movPrincipal.IdEmisor;
				ObjEntrada.EsFungible						= movPrincipal.EsFungible;
				ObjEntrada.IdBoveda							= movPrincipal.IdBoveda;
				ObjEntrada.EsNominal						= movPrincipal.EsNominal;		
				ObjEntrada.Cantidad							= movPrincipal.Cantidad; 
				ObjEntrada.CorteMinimo						= movPrincipal.CorteMinimo; 
				ObjEntrada.ValorRescate						= movPrincipal.ValorRescate; 
				ObjEntrada.PrecioCompra						= movPrincipal.PrecioCompra; 
				ObjEntrada.TasaCompra						= movPrincipal.TasaCompra;
				ObjEntrada.MontoCompra						= movPrincipal.MontoCompra;
				ObjEntrada.IdTipoProducto					= movPrincipal.IdTipoProducto;
				
				BigDecimal tasa 							= movPrincipal.TasaCompra.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
				BigDecimal periodos 						= new BigDecimal(movPrincipal.PlazoEnDiasOperacion).divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
				BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));
				
				ObjEntrada.PrecioAPlazo 					= MonedaFormato.formatear(conn, movPrincipal.PrecioCompra.multiply(factor), MonedaFormato.PRECIO, movPrincipal.IdMonedaInstrumento).valor;
			
				ObjEntrada.MontoAPlazo 						= movPrincipal.Cantidad.multiply(ObjEntrada.PrecioAPlazo);
				ObjEntrada.MontoCompraMonedaInstrumento		= movPrincipal.MontoCompraMonedaInstrumento;
				ingresos.add(ObjEntrada);
				
				Paso p = new Paso("INGRESOCABECERA", ObjEntrada, grupo);
				grupo.pasos.add(p);
			}
			
			
		}
		
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); 
			
			//2***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					Paso p = grupo.pasos.get(0); 
					p.resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(0).estado = "OK";
						
						String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						p.estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						p.mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		//3
		ingresos.clear();
				
		for (GrupoOperacionCustodia grupo : grupos) {
			
			if (!grupo.estado.equalsIgnoreCase("OK")) {
				continue;
			}
			
			ObjMovimientosEnTransito mov = grupo.TransitoRelacionado;
			
			List<Object> movsCabecera = (List<Object>) grupo.pasos.get(0).resultado;
			
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= grupo.IdMovimientoInicial;
			ObjEntrada.TablaAfectada					= "SUBYACENTE";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCarteraRelacionada			= ((cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)movsCabecera.get(0)).FolioCustodia;
			ObjEntrada.IdCliente						= mov.IdCliente; 
			ObjEntrada.IdPortafolio						= mov.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolio		= mov.IdCuentaCustodioPortafolio;
			ObjEntrada.FechaOperacion					= mov.FechaOperacion;
			ObjEntrada.FechaVencimientoOperacion		= mov.FechaVencimientoOperacion;
			ObjEntrada.FechaVencimientoInstrumento		= mov.FechaVencimientoOperacion;
			ObjEntrada.PlazoEnDiasOperacion				= mov.PlazoEnDiasOperacion;
			ObjEntrada.IdTipoOperacion					= mov.IdTipoOperacion;		
			ObjEntrada.IdConcepto						= mov.IdConcepto;
			ObjEntrada.IdCondicionLiquidacion			= mov.IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= mov.FechaLiquidacion;
			ObjEntrada.IdTipoCanalIngreso				= mov.IdTipoCanalIngreso; 		
			ObjEntrada.CodigoIFRS						= mov.CodigoIFRS;
			ObjEntrada.NemoSVS							= mov.NemoSVS;
			ObjEntrada.IdTipoComprobante				= mov.IdTipoComprobante; 
			ObjEntrada.FolioComprobante					= mov.FolioComprobante;
			ObjEntrada.FolioExternoReferencia			= mov.FolioExternoReferencia;
			ObjEntrada.IdBolsa							= mov.IdBolsa;	
			ObjEntrada.IdUsuario						= mov.IdUsuario;
			ObjEntrada.IdSucursal						= mov.IdSucursal; 
			ObjEntrada.IdAgente							= mov.IdAgente;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdInstrumento					= mov.IdInstrumento;
			ObjEntrada.IdMonedaInstrumento				= mov.IdMonedaInstrumento;
			ObjEntrada.IdMonedaEmision					= mov.IdMonedaEmision;
			ObjEntrada.IdTipoReajusteInstrumento		= mov.IdTipoReajuste;
			ObjEntrada.IdEmisor							= mov.IdEmisor;
			ObjEntrada.EsFungible						= mov.EsFungible;
			ObjEntrada.IdBoveda							= mov.IdBoveda;
			ObjEntrada.EsNominal						= mov.EsNominal;		
			ObjEntrada.Cantidad							= mov.Cantidad; 
			ObjEntrada.CorteMinimo						= mov.CorteMinimo; 
			ObjEntrada.ValorRescate						= mov.ValorRescate; 
			ObjEntrada.PrecioCompra						= mov.PrecioCompra; 
			ObjEntrada.TasaCompra						= mov.TasaCompra;
			ObjEntrada.MontoCompra						= mov.MontoCompra;
			ObjEntrada.MontoCompraMonedaInstrumento		= mov.MontoCompraMonedaInstrumento;
			ObjEntrada.IdTipoProducto					= mov.IdTipoProducto;
			ingresos.add(ObjEntrada);
			
			Paso p = new Paso("INGRESOSUBYACENTE", ObjEntrada, grupo);
			grupo.pasos.add(p);
			
		
		}
				
		if (ingresos.size() > 0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= p_IdEmpresa;		
			paramInSP.p_IdModulo			= p_IdModulo;
			paramInSP.p_IdUsuario			= p_IdUsuario;
			paramInSP.p_SimularOperacion	= p_simularOperacion;
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_ValorizacionAutomatica	= "N";
			paramInSP.p_DatosEntrada		= ingresos;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSP); //3***
			//salida.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES); -> todavia no trae na
			
			for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resultIngresos.P_OUT_DATOSSALIDA) {
				for (GrupoOperacionCustodia grupo : grupos) {
					
					if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
						continue;
					}
					
					grupo.pasos.get(1).resultado.add(datoSalida);
					
					if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
						
						grupo.estado = "OK";
						grupo.pasos.get(1).estado = "OK";
						
						//NO ES NECESARIO INDICAR QUE SE CREO EL SUBYACENTE
						//String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
						//String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						//grupo.pasos.get(2).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						
						grupo.estado = "ERR";
						grupo.pasos.get(1).estado = "ERR";
						
						String codigo = "UI_ERROR_AL_CREAR_SUBYACENTE_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
						String descripcion = Diccionario.getMensaje(conn, p_IdEmpresa, codigo)
								.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(1).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
				}
				
			}
		}
		
		
		for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV2 = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV2.IdMovimientoCarteraEnTransito = grupo.TransitoRelacionado.IdMovimientoCarteraEnTransito;
				MOV2.EsCertificacion = "S";
				MOV2.IdUsuario = p_IdUsuario;
				grupo.transitosActualizar.add(MOV2);
			}
		}
		
		
		
		return grupos;
		
	};
	
	
	public Long BuscaFolioCarteraRelacionada( Connection conn ,Long MovimientoCarteraEnTransitoRelacionado  ) {
		Long result =0L;
		SQL sql = new SQL();
		try {
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdMovimientoCarteraEnTransito", MovimientoCarteraEnTransitoRelacionado);
	  
			List <BuscaCartera> BuscaFolioCartera  = sql.query(conn, SQL_SELECT_FOLIOCARTERARELACIONADO, params, BuscaCartera.class);
			result = BuscaFolioCartera.get(0).FolioCartera;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	};
	
	public Long BuscaIdCartera( Connection conn ,Long MovimientoCarteraEnTransitoRelacionado  ) {
		Long result =0L;
		SQL sql = new SQL();
		try {
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdMovimientoCarteraEnTransito", MovimientoCarteraEnTransitoRelacionado);
	  
			List <BuscaCartera> BuscaIdCartera  = sql.query(conn,SQL_SELECT_FOLIOCARTERARELACIONADO, params, BuscaCartera.class);
			result = BuscaIdCartera.get(0).IdCartera;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	};
	
	
public String informarVentaCortaEnBlockChain( Connection conn, Long idEmpresa, Long idUsuario, ObjMovimientosEnTransito movimientoEnTransito, String prestamosRentaFija, PrestamoVentaCorta infoPrestamo) throws Exception {
		SQL _sql 					= new SQL();
		String p_EsBlockChain 		= ConstantesBCK.NO;
		String folioSistemaExterno 	= "";
				
		ConsultaDataCorredoraBlockChain consultaDataCorredora 													= new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora 	= new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = idEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
		
		ContratoEsBlockChainFirmado ContratoEsBlockChain = new ContratoEsBlockChainFirmado();
		ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada ContratoEsBlockChainEntrada = new ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada();
		ContratoEsBlockChainEntrada.p_IdEmpresa 			= idEmpresa;
		ContratoEsBlockChainEntrada.p_IdUsuario 			= idUsuario;
		ContratoEsBlockChainEntrada.p_ClaveUsuario 			= "";
		ContratoEsBlockChainFirmado.ConsultaEstadoCntBC ConsultaEstadoCntBC 			= new ContratoEsBlockChainFirmado.ConsultaEstadoCntBC();
		ConsultaEstadoCntBC.p_IdCliente 					= movimientoEnTransito.IdCliente.toString();
		ConsultaEstadoCntBC.p_IdContrato 					= ConstantesBCK.CONTRATOPRESTAMOANEXO42;
		ContratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= ConsultaEstadoCntBC;
		
		ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida ContratoEsBlockChainFirmado_Salida = ContratoEsBlockChain.logica(conn,ContratoEsBlockChainEntrada);
		for (ContratoEsBlockChainFirmado.Resultado ResultadoBC : ContratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO) {
			p_EsBlockChain = ResultadoBC.EsBlockChain;
			break;
		}
		
		if (movimientoEnTransito.EsVentaCorta.equalsIgnoreCase("S") && p_EsBlockChain.equalsIgnoreCase(ConstantesBCK.SI) 
				&& ((movimientoEnTransito.IdClasificacionTipoInstrumento.equals(ConstantesBCK.MERCADO_PRESTAMOS_BLOCKCHAIN_RENTA_FIJA) && prestamosRentaFija.equals(ConstantesBCK.SI)) || movimientoEnTransito.IdClasificacionTipoInstrumento.equals(ConstantesBCK.MERCADO_PRESTAMOS_BLOCKCHAIN)) ) {
			
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdCondicionDeLiquidacion", movimientoEnTransito.IdCondicionLiquidacion);
			List<SelectCondicionDeLiquidacion> selectCondicionDeLiquidacion = _sql.query(conn, SQL_SELECT_CONDICION_DE_LIQUIDACION, parametroEntrada, SelectCondicionDeLiquidacion.class);
			
			IngresoPrestamosBlockchain ingresoPrestamosBlockchain = new IngresoPrestamosBlockchain();
			IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Entrada entradaIngresoPrestamos = new IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Entrada();
			entradaIngresoPrestamos.p_IdEmpresa 				= idEmpresa;
			entradaIngresoPrestamos.p_Tasa 						= movimientoEnTransito.TasaCompra.toString();
			entradaIngresoPrestamos.p_FechaOperacion 			= new SimpleDateFormat("yyyy-MM-dd").format( movimientoEnTransito.FechaOperacion );				
			entradaIngresoPrestamos.p_Nemo 						= movimientoEnTransito.Nemotecnico;
			entradaIngresoPrestamos.p_Mercado 					= movimientoEnTransito.IdClasificacionTipoInstrumento;
			entradaIngresoPrestamos.p_TipoPrestamo 				= infoPrestamo.TipoPrestamoSistemaExterno;
			entradaIngresoPrestamos.p_TipoGarantia 				= "T";
			entradaIngresoPrestamos.p_CondicionLiquidacion 		= selectCondicionDeLiquidacion.get(0).Codigo;
			entradaIngresoPrestamos.p_CodigoCorredorPrestamista = salidaConsultaDataCorredora.p_Out_BrokerageID;
			entradaIngresoPrestamos.p_Cantidad 					= movimientoEnTransito.Cantidad.intValue() + "";
			entradaIngresoPrestamos.p_FechaVencimiento 			= new SimpleDateFormat("yyyy-MM-dd").format( movimientoEnTransito.FechaVencimientoOperacion );
			entradaIngresoPrestamos.p_IdClientePrestatario 		= movimientoEnTransito.IdCliente.toString();
			entradaIngresoPrestamos.p_IdClientePrestamista 		= "";
			entradaIngresoPrestamos.p_CantidadPrestamista 		= "0";
			IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Salida salidaIngresoPrestamos = ingresoPrestamosBlockchain.logica(conn, entradaIngresoPrestamos);
			
			if ( salidaIngresoPrestamos.p_Out_ContadorError == 0 ) {
				folioSistemaExterno = salidaIngresoPrestamos.p_Out_FolioExterno;
			} else {
				logProcesoCrearEmprestitoBlockChain.add(new LogProcesoPrestamoBlockChain( idEmpresa, movimientoEnTransito.FolioComprobante, new SimpleDateFormat("yyyy-MM-dd").format( movimientoEnTransito.FechaOperacion ), salidaIngresoPrestamos.p_Out_Mensaje, "ProcesarFacturacionCustodia", "informarVentaCortaEnBlockChain", idUsuario.toString())); 
			}
		}
		return folioSistemaExterno;	
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main(String[] args) throws Exception {
		
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				
				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_Glosa\":\"\",\"P_MOVIMIENTOFACTURAR\":[{\"IdPortafolio\":40694,\"IdCliente\":174951,\"IdMovimientoComprobante\":4500,\"IdTipoComprobante\":\"POL\",\"FechaComprobante\":\"2018-02-28\"},{\"IdPortafolio\":40694,\"IdCliente\":174951,\"IdMovimientoComprobante\":4499,\"IdTipoComprobante\":\"POL\",\"FechaComprobante\":\"2018-02-28\"}]}";
				
				//ProcesarFacturacionCustodia_Entrada Objin = gson.fromJson(json, ProcesarFacturacionCustodia_Entrada.class);
				ProcesarFacturacionCustodia_Entrada Objin = new ProcesarFacturacionCustodia_Entrada();
				
				Objin.p_IdEmpresa = 1L;
				Objin.p_IdModulo = "FAC";
				Objin.p_IdUsuario = 69L;
				Objin.p_Movimientos = new ArrayList<>();
				Objin.p_MovimientosEnTransito =  new ArrayList<>();;
				Objin.p_simularOperacion = "N";
				Objin.p_UsaReglasNegocio = "S";
				Objin.p_MovimientosEnTransito.add(9037L);
				
				ProcesarFacturacionCustodia  kk = new ProcesarFacturacionCustodia();
				ProcesarFacturacionCustodia_Salida Objout = kk.logica(con, Objin);				
				System.out.println(Objout.toString());
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};

}
