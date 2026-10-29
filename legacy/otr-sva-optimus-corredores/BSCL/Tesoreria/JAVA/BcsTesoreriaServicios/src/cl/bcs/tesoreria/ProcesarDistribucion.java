package cl.bcs.tesoreria;
	
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.APV.ValidarMandatosPAC.LogValidarMandatosPAC;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.Empleado;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden.Orden_Entrada;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.Instrumento;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.pojo.ConstantesTesoreria;
import cl.bcs.tesoreria.pojo.Traspaso.InMovimientoGenerado;
import cl.bcs.tesoreria.pojo.Traspaso.RegistroRecaudacion;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


	public class ProcesarDistribucion implements JavaCall   {
		
		static final Logger logger = LogManager.getLogger(ProcesarDistribucion.class);
		public String SQL_SELECT_TOTAL_CORREDORA   = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarTotalRendicionPac.sql"); 
		public String SQL_SELECT_MANDATOS_PAC      = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarMandatosPac.sql");
		public String SQL_SELECT_MANDATOS_Y_ARCHIVO_PRE_REND   	= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarMandatosPacYArchivoPreRend.sql");
		public String SQL_SELECT_MANDATOS_Y_ARCHIVO   	= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarMandatosPacYArchivo.sql");
		public String SQL_SELECT_MANDATOS_PAC_PRE_REND      	= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarMandatosPacPreRend.sql");
		public String SQL_SELECT_LOG_MANDATOS_PAC_PRE_REND      = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_SELECT_LogMandatosPAC.sql");
		public String SQL_INSERT_LOG_MANDATOS_PAC_PRE_REND		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_INSERT_LogMandatosPAC.sql");
		public String SQL_UPDATE_LOG_MANDATOS_PAC_PRE_REND		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_UPDATE_LogMandatosPAC.sql");
		public String SQL_DELETE_LOG_MANDATOS_PAC_PRE_REND		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_DELETE_LogMandatosPAC.sql");
		public String SQL_SELECT_MANDATOS_RZO_PRE_REND		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_SELECT_RechazadosPreRen.sql");
		public String SQL_PAC_INSERTMOVIMIENTO		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_InsertPacMovimiento.sql");
		public String SQL_SELECT_CORREDOR			= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarCorredor.sql");
		public String SQL_SELECT_EMPLEADO			= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarEmpleado.sql");
		public String SQL_CtaDistribucion 		 	= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarCuentaDistribucion.sql");
		public String SQL_SELECT_CSCD  		 		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarContratoSuscripcionCuotasDetalle.sql");
		public String SQL_SELECT_DISTRIBUCION		= 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarSerieSAAPV.sql");
		private final String SQL_SELECT_Comprobante = "SELECT \"IdComprobante\", \"IdTipoMovimiento\", 'CLI' AS \"AfectaCuentaInversion\" FROM \"BSCL.Tesoreria.BD::TES.Comprobante\" WHERE \"IdComprobante\" IN (:idsComprobantes)";
		public String SQL_SELECT_DIA_DESCUENTO_MANDATOS      = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarDiaDescuento.sql");
		public String SQL_SELECT_MANDATOS_PAC_PRE_REND_DETALLE_ARCHIVO = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_ConsultarMandatosPacPreRendDetalleArchivo.sql");
		


		public String SQL_SELECT_CAJA_TESORERIA_PAC      = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_SelectCajaTesoreriaParaPAC.sql");
		public String SQL_SELECT_PORTAFOLIO_CLIENTE		 = 	loadResourceToString("cl/bcs/tesoreria/sql/ProcesarDistribucion_PortafolioCliente.sql");
	
		
		
		
		private final ConstantesTesoreria _c 		= 	new ConstantesTesoreria();
		List<String> foliosCSC 						= 	new ArrayList<String>();
		List<String> foliosAPV 						= 	new ArrayList<String>();
		List<String> foliosCSCSimularN 				= 	new ArrayList<String>();
		List<String> foliosAPVSimularN				= 	new ArrayList<String>();
		SalidaRecaudacionTraspaso out 				= 	new SalidaRecaudacionTraspaso();
		List<RegistroRecaudacion> solicitudesPreRendicion 		=	new ArrayList<RegistroRecaudacion>();
		
		public static class InRecaudacion{
			
			public String       CuentaCorriente = null;
			public Long 		IdTraspaso;
			public String 		IdentServicio;
			public Long       	IdCliente;
			public Long       	IdPortafolio; 
			public Long       	IdPersonaEmisor;
			public String 		FechaDeposito;
			public String 		IdMedioDePago;
			public Long 		IdBanco;
			public long 		NumeroCuentaBancaria;
			public Long 		IdCuentaBanco;
			public String 	    IdSerieFondo;
			public String 		Sucursal;
			public BigDecimal 	MontoDeposito;
			public BigDecimal 	MontoCSC;
			public BigDecimal 	MontoSAPV;
			public BigDecimal 	SumaTotal;
			public String 		Observacion;
			public String 		IdRegimenTributario="";
			public String 		FolioCSC = "";
			public String 		FolioSAAPV = "";
			
			public Long			IdMandatoPAC;
			public String 		EstadoPAC = "";
			public String 		EstadoSAAPV = "";
			public String 		EstadoLargoSAAPV = "";
			public String 		EstadoCSC = "";
			public String 		EstadoLargoCSC = "";
			public String		NombreCompleto = "";
			public String		DocumentoIdentidad = "";
			public String 		EstadoCliente = "";
			public String		EstadoPortafolio = "";
			
			public String 		EstadoCargo ="";
			public String		DescripcionEstadoCargo = "";
			public String		EstadoCargoBanco = "";
			public String       MonedaCSC;
			public String       MonedaSAPV;
			public String		NombreBanco;
			public String		CuentaBancaria;
			public String		CodigoPortafolio;
			public Long			DiaDescuento;
			public Date			FechaSuscripcion;
			public Long			IdMandatoPACUI;
			public String toString() {
				return Utils.printJson(this, false);
			}
			
		};	
		
		
		public static class DatosCorredor{
			
			public Long IdPersona;
			public Long IdCliente;
			public Long IdPortafolio;
		}
		
		public static class CtaDistribucion{
			public Long		DestinoCtaBco;

			public Long		IdPersonaBanco;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}

		public static class Caja{			
			public Long       	IdCaja;
			public String       Codigo;
			public String       Nombre;
		}
		
		public static class DiaDescuento{
			public Long		DiaDescuento;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		


		public static class PortafolioCliente{
			public Long		IdPortafolio;
			public Long		IdPersona;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		
		public static class ContratoSuscripcionCuotaDetalle{
			public Long IdCSCDetalle;
			public String IdFolioCSC;
			public Long IdPortafolio;
			public String Serie;
			public BigDecimal Monto;
			public Double Porcentaje;
			public String Regimen;
			public String EstadoContrato;
			public String EstadoPortafolio;
			public String Moneda;
			public String IdMonedaInstrumento;
			public String Canal;
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public static class DistribucionInversion{
			public String FolioContratoSAAPV;
			public String Nemotecnico;
			public BigDecimal MontoPesos;
			public String Codigo;
			public String EstadoContrato;
			public String EstadoPortafolio;
			public String IdTipoInstrumento;
			public long IdPortafolio;
			public long IdInstrumento;
			public String IdEmisor;
			public String IdMonedaInstrumento;
			public String Canal;
			public BigDecimal MontoDistribucion = new BigDecimal(0);
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		
		//  ENTRADA
		public static class EntradaRecaudacionTraspaso {
			
			public 	Long						p_IdEmpresa;
			public	Long						p_IdUsuario;
			public 	String						p_IdModulo;
			public 	String						p_simular;
			public	List<InRecaudacion>			p_Recaudacion;
			
			
			public EntradaRecaudacionTraspaso(){
				p_Recaudacion 					= new ArrayList<InRecaudacion>();
				
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}	
		
		// SALIDA
		public static class SalidaRecaudacionTraspaso{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
			public List <RegistroRecaudacion> P_OUT_SOLICITUDES_PROCESADAS;
			public List <RegistroRecaudacion> P_OUT_SOLICITUDES_NO_PROCESADAS;
			public List <RegistroRecaudacion> P_OUT_SOLICITUDES_A_PROCESAR; 
			public List <ResultadoSolicitud> P_OUT_RESULTADO;
			
			public SalidaRecaudacionTraspaso(){
				this.P_OUT_ERRORESCONTADOR			=	0L;
				this.P_OUT_MENSAJES 				= 	new ArrayList<MensajeSalida>();
				this.P_OUT_RESULTADO 				= 	new ArrayList<ResultadoSolicitud>();
				this.P_OUT_SOLICITUDES_PROCESADAS 	= 	new ArrayList<RegistroRecaudacion>();
				this.P_OUT_SOLICITUDES_NO_PROCESADAS = 	new ArrayList<RegistroRecaudacion>();
				this.P_OUT_SOLICITUDES_A_PROCESAR 	= 	new ArrayList<RegistroRecaudacion>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public static class LlaveMandatoDeposito{
			public String IdentServicio;
			public BigDecimal MontoDeposito;
			
			@Override
			public int hashCode() {
				return 31 * this.IdentServicio.hashCode();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
				    return false;
				if (getClass() != obj.getClass())
				    return false;
				LlaveMandatoDeposito llave = (LlaveMandatoDeposito) obj;
				return this.IdentServicio.equals(llave.IdentServicio) && this.MontoDeposito.equals(llave.MontoDeposito);
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		@Override
		public JsonObject call(Connection conn, JsonObject in) throws Exception {
			Gson gson = new Gson();		
			EntradaRecaudacionTraspaso jin = Utils.getGsonBuilder().fromJson(in.toString(), EntradaRecaudacionTraspaso.class);
			SalidaRecaudacionTraspaso out = logica(conn,jin);
			
			JsonParser parser = new JsonParser();
	        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}

		
		
		
		
		public SalidaRecaudacionTraspaso logica(Connection con, EntradaRecaudacionTraspaso entrada) throws Exception{
			logger.info("INICIO::ProcesarDistribucion");
			/*****************************************
			 * INICIO VARIABLES 
			 *****************************************/
			
			SQL sql											=	new SQL();			
			String fechaSistema 							= 	ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String monedaSistema 							= 	ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			Long IdEmpresa 									= 	entrada.p_IdEmpresa;
			Long IdUsuario 									= 	entrada.p_IdUsuario;
			RevisarRequisitosBasicos basicos 				= 	new RevisarRequisitosBasicos();
			
			HashMap<String,Object> param 					= 	new HashMap<String,Object>();
			
			List<RegistroRecaudacion> solicitudesProcesadas = 	new ArrayList<RegistroRecaudacion>();
			BigDecimal valorUF;
			List<ContratoSuscripcionCuotaDetalle> distribucionCSC	= 	null;
			List<DistribucionInversion> distribucionSAPV 			= 	null;
			
			CalcularParidad calcularParidad 				= new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada 	= new CalcularParidadEntrada();
			CalculoEntrada calculoEntradaUF 				= new CalculoEntrada();

			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			
			calculoEntradaUF.IdTrabajo 			= 2L;
			calculoEntradaUF.IdInterno 			= 1L;
			calculoEntradaUF.Fecha 				= fechaSistema;
			calculoEntradaUF.IdMonedaOrigen 	= "UF";
			calculoEntradaUF.IdMonedaDestino 	= "CLP";
			calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaUF.MontoOrigen 		=  BigDecimal.ONE;
			calculoEntradaUF.IdUso 				= "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
			//------
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			//------
			valorUF = calculoSalida.p_out_CalculosSalida.get(0).FactorOrigenDestino;
			
			
			
			
			param.clear();
			param.put("IdEmpresa", entrada.p_IdEmpresa);			
			List<DatosCorredor> _DatosCorredor 				= 	sql.query(con, SQL_SELECT_CORREDOR, param, DatosCorredor.class);
			//------
			RevisarRequisitosBasicosSalida salidaBasicos 	= 	basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			//------
			Long IdPortafolioCorredor 						= 	_DatosCorredor.get(0).IdPortafolio;
			Long IdPersonaCorredor 							= 	_DatosCorredor.get(0).IdPersona;
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			
			agregarLogsPreRendicion(con, sql, IdEmpresa);
			
			/*****************************************
			 * CREACION DEL OBJETO DE TRASPASO 
			 *****************************************/
			
			HashMap<String,Object> params2 					= 	new HashMap<String,Object>();
			List<InRecaudacion> registrosArchivo			= 	sql.query(con, SQL_SELECT_MANDATOS_Y_ARCHIVO,params2, InRecaudacion.class);
			List<InRecaudacion> cruceArchivoMandatos		=	ValidacionSolicitudesArchivo(registrosArchivo, fechaSistema, IdEmpresa, IdUsuario, valorUF);
			List<InRecaudacion> traspasos 					= 	sql.query(con, SQL_SELECT_MANDATOS_PAC ,params2, InRecaudacion.class);
			
			if(cruceArchivoMandatos.size() > 0) {
				if(!traspasos.isEmpty()) {
					ValidaArchivoContraMandatos(cruceArchivoMandatos,traspasos, fechaSistema, IdEmpresa, IdUsuario, valorUF);
				}
			}
			
			if (traspasos.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE","EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE"));
				out.P_OUT_ERRORESCONTADOR += 1L;
				return out;
			}
			
			/*****************************************
			 * CREACION DEL OBJETO DE LA SOLICITUD A PROCESAR 
			 *****************************************/	
			solicitudesProcesadas							=	getSolicitudAProcesarPreRendicion(traspasos, cruceArchivoMandatos, fechaSistema, IdEmpresa, IdUsuario, valorUF);
			out.P_OUT_SOLICITUDES_A_PROCESAR.addAll(solicitudesProcesadas);
			
			List<RegistroRecaudacion> solicitudProcesar  	= 	getSolicitudesValidas(solicitudesProcesadas);			
			
			Long IdEmpleadoDigitador 						= 	0L;
			Long idCtaBancoCorredor 						= 	0L;
			
			Long idPersonaBanco 							= 	0L;
			
			HashMap<String,Object> params 					= 	new HashMap<>();
			params.put("IdUsuario", entrada.p_IdUsuario);
			List<Empleado> empleadoDigitador 				= 	sql.query(con, SQL_SELECT_EMPLEADO, params, Empleado.class);
			
			
			if (!empleadoDigitador.isEmpty()) {
				IdEmpleadoDigitador = empleadoDigitador.get(0).IdEmpleado;
			}
			
			
			HashMap<String,Object> paramctaBancoCorredor 	= 	new HashMap<>();
			paramctaBancoCorredor.put("IdEmpresa", entrada.p_IdEmpresa);
			List<CtaDistribucion> CtaBancoCorredor 			= 	sql.query(con, SQL_CtaDistribucion, paramctaBancoCorredor, CtaDistribucion.class);

			if (!CtaBancoCorredor.isEmpty()) {
				idCtaBancoCorredor = CtaBancoCorredor.get(0).DestinoCtaBco;
				
								
				idPersonaBanco = CtaBancoCorredor.get(0).IdPersonaBanco;
				
				
			};
			
			
			
			BigDecimal vMontoTotalCorredora 				= 	BigDecimal.ZERO;
		
			/*****************************************
			 * 1 TESORERIA 
			 *****************************************/
			
			// Obtiene el id de la caja de movimientos PAC
			Long idCajaTesoreriaPac = 0L;
			
		
			param.clear();
			param.put("IdEmpresa", entrada.p_IdEmpresa);			
			List<Caja> lstCajaTesoreriaPac 				= 	sql.query(con, SQL_SELECT_CAJA_TESORERIA_PAC, param, Caja.class);
			
			if (!lstCajaTesoreriaPac.isEmpty())
			{	
				idCajaTesoreriaPac = lstCajaTesoreriaPac.get(0).IdCaja;
			}
			
			String infoLog = "0";
			if(!solicitudProcesar.isEmpty())
				infoLog = Integer.toString(solicitudProcesar.size());
			
			logger.info("ENTRADA::ProcesarDistribucion.getSalidaTesoreria - Solicitudes a procesar: "+ infoLog);
			getSalidaTesoreria(solicitudProcesar, sql, entrada, con, monedaSistema, fechaSistema, IdPortafolioCorredor, IdPersonaCorredor, IdEmpleadoDigitador, idCtaBancoCorredor, entrada.p_simular, IdEmpresa, IdUsuario, idCajaTesoreriaPac, idPersonaBanco );

			if(!solicitudProcesar.isEmpty()) {
				infoLog = Integer.toString(solicitudProcesar.size());
				logger.info("SALIDA::ProcesarDistribucion.getSalidaTesoreria - Solicitudes procesadas: "+ infoLog);
			}
			else
				logger.info("SALIDA::ProcesarDistribucion.getSalidaTesoreria");
			
			
			
			
			/************************************************************************************************
			 * OBTENEMOS LA DISTRIBUCION DE CADA CONTRATO SAAPV O CSC | PARA VER EL ESTADO DE CADA CONTRATO
			 ************************************************************************************************/		
			for (RegistroRecaudacion regRecaudacion : solicitudProcesar) {
				if(! (regRecaudacion.FolioCSC.equals("0") || regRecaudacion.FolioCSC.equals(""))) {
					foliosCSCSimularN.add(regRecaudacion.FolioCSC.toString());
				}else {
					foliosAPVSimularN.add(regRecaudacion.FolioAPV.toString());
				}
				vMontoTotalCorredora = vMontoTotalCorredora.add(regRecaudacion.MontoDeposito);
			}
			
			distribucionCSC	 	= 	null;
			distribucionSAPV 	= 	null;	
			
			if( !foliosCSCSimularN.isEmpty() ) {
				HashMap<String, Object> parametroCSCD				 	= 	new HashMap<String, Object>();
				
				parametroCSCD.put("folios", foliosCSCSimularN);
				distribucionCSC = sql.query(con, SQL_SELECT_CSCD, parametroCSCD, ContratoSuscripcionCuotaDetalle.class);
				
			}
			if( !foliosAPVSimularN.isEmpty() ) {
				
				HashMap<String, Object> parametrosAPV 					= 	new HashMap<String,Object>();
				BigDecimal montoPesosTotal = new BigDecimal(0);
				
				parametrosAPV.put("foliosAPV", foliosAPVSimularN);
				distribucionSAPV = sql.query(con, SQL_SELECT_DISTRIBUCION, parametrosAPV, DistribucionInversion.class);	
				
				ArrayList<String> FolioSinDistribucion = new ArrayList<String>();
				
				List<String> FoliosAPVString = new ArrayList<String>();
				int cantidadDistribucionSAPV = 0;
				
				//Obtengo folios contratos a los que trajo la distribucion
				FoliosAPVString = distribucionSAPV.stream().filter(pp -> pp.FolioContratoSAAPV != null).map(pp -> pp.FolioContratoSAAPV).distinct().collect(Collectors.toList());
				cantidadDistribucionSAPV = FoliosAPVString.size();
					
				if(cantidadDistribucionSAPV < foliosAPVSimularN.size()) {
					
					for(int i = 0; i < foliosAPVSimularN.size(); i ++){
						if (!FoliosAPVString.contains(foliosAPVSimularN.get(i))){
							FolioSinDistribucion.add(foliosAPVSimularN.get(i));
						}
					};
					
					logger.info("SALIDA::ProcesarDistribucion.getSalidaDistribucion - Solicitudes sin distribucion: "+ FolioSinDistribucion);
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"Contratos sin distribucion","Contratos sin distribucion FolioContratoSAAPV " +FolioSinDistribucion));
					out.P_OUT_ERRORESCONTADOR++;
					return out;	
				}
					
				
				//Recorrer distribucionsapv para evitar temas de decimales y saldo insuficente caja
				for(int i = 0; i < distribucionSAPV.size(); i ++){
					HashMap<String, Object> parametrosAPVPar 					= 	new HashMap<String,Object>();
					List<DistribucionInversion> distribucionSAPVParcial 		= 	null;
					parametrosAPVPar.put("foliosAPV", distribucionSAPV.get(i).FolioContratoSAAPV);
					
					distribucionSAPVParcial = sql.query(con, SQL_SELECT_DISTRIBUCION, parametrosAPVPar, DistribucionInversion.class);
					
					for(int j = 0; j < distribucionSAPVParcial.size(); j ++) {
					  if ((distribucionSAPVParcial.get(j).FolioContratoSAAPV.equalsIgnoreCase(distribucionSAPV.get(i).FolioContratoSAAPV)) && (distribucionSAPVParcial.get(j).Nemotecnico.equalsIgnoreCase(distribucionSAPV.get(i).Nemotecnico))){
						  if(j == distribucionSAPVParcial.size()-1){								
								distribucionSAPV.get(i).MontoPesos = distribucionSAPV.get(i).MontoDistribucion.subtract(montoPesosTotal);	
								montoPesosTotal = new BigDecimal(0);
							}else {
							         montoPesosTotal = montoPesosTotal.add(distribucionSAPVParcial.get(j).MontoPesos.setScale(0, RoundingMode.HALF_UP));
							      }	  
					  }				
					}
				}
			}
			
			/******************************************************************************
			 * ESTADO DE CONTRATO, ESTADO DE PORTAFOLIO, SOLICITUDES PROCESADAS
			 *****************************************************************************/
			
			List<RegistroRecaudacion> clientesActivos  = new ArrayList<RegistroRecaudacion>();
			List<RegistroRecaudacion> solicitudesContratos  = new ArrayList<RegistroRecaudacion>();
			
			for(RegistroRecaudacion solicitud : solicitudProcesar) {
				if(! (solicitud.FolioCSC.equals("0") || solicitud.FolioCSC.equals(""))) {
					for(ContratoSuscripcionCuotaDetalle cscd : distribucionCSC) {
						if(solicitud.FolioCSC.equals(cscd.IdFolioCSC)) {
							solicitud.EstadoCSC = cscd.EstadoContrato;
							if(!cscd.EstadoContrato.equalsIgnoreCase("ING")) {
								solicitud.EstadoRendicion = "Contrato bloqueado, Anulado o Revocado";
								solicitud.EstadoProcesoRendicion = "N";
							}else if(cscd.EstadoPortafolio.equals("BLO")) {
								solicitud.EstadoRendicion = "Portafolio Bloqueado";
								solicitud.EstadoProcesoRendicion = "N";
							}
						}
						
					}
				}else{
					for(DistribucionInversion capv : distribucionSAPV) {
						String folioAPV = String.valueOf(solicitud.FolioAPV);
						if(folioAPV.equals(capv.FolioContratoSAAPV)) {
							solicitud.EstadoSAAPV = capv.EstadoContrato;
							if(!capv.EstadoContrato.equals("VIG")) {
								solicitud.EstadoRendicion = "Contrato no vigente";
								solicitud.EstadoProcesoRendicion = "N";
							}else if(!capv.EstadoPortafolio.equals("VIG")) {
								solicitud.EstadoRendicion = "Portafolio Bloqueado";
								solicitud.EstadoProcesoRendicion = "N";
							}
						}
					}
				}
			}
			
			for( RegistroRecaudacion solicitudProcesadasTesoreria : solicitudProcesar ) {
				
				if(!solicitudProcesadasTesoreria.EstadoPortafolio.equalsIgnoreCase("ACT")) {
					solicitudProcesadasTesoreria.EstadoRendicion = "Portafolio inactivo, la solicitud no se procesara";
					solicitudProcesadasTesoreria.EstadoProcesoRendicion = "N";
				}else if(!solicitudProcesadasTesoreria.EstadoCliente.equals("ACTIVO")) {
					solicitudProcesadasTesoreria.EstadoRendicion = "Cliente inactivo, la solicitud no se procesara";
					solicitudProcesadasTesoreria.EstadoProcesoRendicion = "N";
				}else {
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					
					solpro = copiarRecaudacion(solicitudProcesadasTesoreria);
					clientesActivos.add(solpro);
						
				}
			}		
			
			if(clientesActivos.isEmpty()) {

				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"EL ARCHIVO DE CARGA NO PRESENTA CLIENTE(S) ACTIVO(S)","EL ARCHIVO DE CARGA NO PRESENTA CLIENTE(S) ACTIVO(S)"));
				out.P_OUT_ERRORESCONTADOR += 1L;
				return out;
			}
			
			for(RegistroRecaudacion solicitud : clientesActivos) {
				if(_c.EstadoProcesado.equalsIgnoreCase(solicitud.EstadoProceso) && !solicitud.EstadoProcesoRendicion.equals("N")){
					solicitudesContratos.add(solicitud);
				}
			}
			
			/*****************************************
			 * 2 CUENTA DE INVERSION
			 *****************************************/
			infoLog = "0";
			if(!solicitudesContratos.isEmpty())
				infoLog = Integer.toString(solicitudesContratos.size());
			
			logger.info("ENTRADA::ProcesarDistribucion.getSalidaCuentaInversion - Solicitudes a procesar: "+ infoLog);
			
			getSalidaCuentaInversion(solicitudesContratos, con, entrada, monedaSistema, fechaSistema, IdEmpleadoDigitador, IdPortafolioCorredor, vMontoTotalCorredora, entrada.p_simular );
			
			if(!solicitudesContratos.isEmpty()) {
				infoLog = Integer.toString(solicitudesContratos.size());
				logger.info("SALIDA::ProcesarDistribucion.getSalidaCuentaInversion - Solicitudes procesadas: "+ infoLog);
			}
			else
				logger.info("SALIDA::ProcesarDistribucion.getSalidaCuentaInversion");
			
			List<RegistroRecaudacion> recaudacionProcesadaInversion = new ArrayList<RegistroRecaudacion>();
			
			for(RegistroRecaudacion procta : solicitudesContratos) {
				
				if (_c.EstadoProcesado.equalsIgnoreCase(procta.EstadoProceso)) {
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					solpro = Utils.copyPojo(procta, solpro);
					recaudacionProcesadaInversion.add(solpro);
				}
				
			}
			
			/*****************************************
			 * OBTENEMOS LA DISTRIBUCION DE CADA CONTRATO SAAPV O CSC y si es FFM o ACN
			 *****************************************/
			
			distribucionCSC = null;
			distribucionSAPV = null;
			foliosCSCSimularN.clear();
			foliosAPVSimularN.clear();			
			
			for (RegistroRecaudacion regRecaudacion : recaudacionProcesadaInversion) {
				if(! (regRecaudacion.FolioCSC.equals("0") || regRecaudacion.FolioCSC.equals(""))) {
					foliosCSCSimularN.add(regRecaudacion.FolioCSC.toString());
				}else {
					foliosAPVSimularN.add(regRecaudacion.FolioAPV.toString());
				}
				
			}
			
			if(!foliosCSC.isEmpty()) {
				
				HashMap<String, Object> parametroCSCD = new HashMap<String, Object>();
				parametroCSCD.put("folios", foliosCSCSimularN);
				distribucionCSC = sql.query(con, SQL_SELECT_CSCD, parametroCSCD, ContratoSuscripcionCuotaDetalle.class);
			}
			if(!foliosAPV.isEmpty()) {
				BigDecimal montoPesosTotal = new BigDecimal(0);
				HashMap<String, Object> parametrosAPV = new HashMap<String,Object>();
				parametrosAPV.put("foliosAPV", foliosAPVSimularN);
				distribucionSAPV = sql.query(con, SQL_SELECT_DISTRIBUCION, parametrosAPV, DistribucionInversion.class);
				
				//Recorrer distribucionsapv para evitar temas de decimales y saldo insuficente caja
				for(int i = 0; i < distribucionSAPV.size(); i ++){
					HashMap<String, Object> parametrosAPVPar 					= 	new HashMap<String,Object>();
					List<DistribucionInversion> distribucionSAPVParcial 		= 	null;
					parametrosAPVPar.put("foliosAPV", distribucionSAPV.get(i).FolioContratoSAAPV);
					//TODO OPTIMIZAR 
					distribucionSAPVParcial = sql.query(con, SQL_SELECT_DISTRIBUCION, parametrosAPVPar, DistribucionInversion.class);
					
					for(int j = 0; j < distribucionSAPVParcial.size(); j ++) {
						if ((distribucionSAPVParcial.get(j).FolioContratoSAAPV.equalsIgnoreCase(distribucionSAPV.get(i).FolioContratoSAAPV)) && (distribucionSAPVParcial.get(j).Nemotecnico.equalsIgnoreCase(distribucionSAPV.get(i).Nemotecnico))){
							if(j == distribucionSAPVParcial.size()-1){
								distribucionSAPV.get(i).MontoPesos = distribucionSAPV.get(i).MontoDistribucion.subtract(montoPesosTotal).compareTo(BigDecimal.ZERO) > 0 ? distribucionSAPV.get(i).MontoDistribucion.subtract(montoPesosTotal) : distribucionSAPV.get(i).MontoPesos;	
								montoPesosTotal = new BigDecimal(0);
							}else {
								montoPesosTotal = montoPesosTotal.add(distribucionSAPVParcial.get(j).MontoPesos.setScale(0, RoundingMode.HALF_UP));
							}	  
						}				
					}
				}
			}
										
			if(distribucionCSC != null) {
				for (ContratoSuscripcionCuotaDetalle contratoscsc : distribucionCSC){
					if (contratoscsc.Moneda.equalsIgnoreCase("UF")) contratoscsc.Monto = contratoscsc.Monto.multiply(valorUF);
				}	
			}
			
			List<RegistroRecaudacion> solicitudesAFFMM 		= 	new ArrayList<RegistroRecaudacion>();
			List<RegistroRecaudacion> solicitudesAOrdenes 	= 	new ArrayList<RegistroRecaudacion>();
			
			for(RegistroRecaudacion solicitudCuentaInversion : recaudacionProcesadaInversion ) {
				
				if(!(solicitudCuentaInversion.FolioCSC.equals("0") || solicitudCuentaInversion.FolioCSC.equals(""))) {
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					solpro = copiarRecaudacion(solicitudCuentaInversion);
					solicitudesAFFMM.add(solpro);
				}else {
					for(DistribucionInversion distribucion : distribucionSAPV) {
						
						RegistroRecaudacion solpro = new RegistroRecaudacion();
						solpro = copiarRecaudacion(solicitudCuentaInversion);
						
						if(distribucion.IdTipoInstrumento.equals("FFM")) {
							solicitudesAFFMM.add(solpro);
							break;
						}else if (distribucion.IdTipoInstrumento.equals("ACN")) {
							solicitudesAOrdenes.add(solpro);
						}
					}
				}
			}
			
			/*****************************************
			 * 3 FONDOS MUTUOS
			 *****************************************/

			infoLog = "0";
			if(!solicitudesAFFMM.isEmpty()) {
				infoLog = Integer.toString(solicitudesAFFMM.size());
				logger.info("ENTRADA::ProcesarDistribucion.getSalidaFFMM - Solicitudes a procesar: "+ infoLog);
				
				getSalidaFFMM(solicitudesAFFMM, con, distribucionCSC, distribucionSAPV, monedaSistema, entrada, entrada.p_simular, fechaSistema);
				
				if(!solicitudesAFFMM.isEmpty()) {
					infoLog = Integer.toString(solicitudesAFFMM.size());
					logger.info("SALIDA::ProcesarDistribucion.getSalidaFFMM - Solicitudes procesadas: "+ infoLog);
				}
				else
					logger.info("SALIDA::ProcesarDistribucion.getSalidaFFMM");
			} 
				
			/*****************************************
			 * 4 ORDENES
			 *****************************************/
			// CrearTransacciones
			if(!solicitudesAOrdenes.isEmpty()) {
				getOrdenes(solicitudesAOrdenes, con, distribucionSAPV, monedaSistema, fechaSistema, entrada, entrada.p_simular);
			}
			
			
			out.P_OUT_SOLICITUDES_PROCESADAS.addAll(recaudacionProcesadaInversion);
						
			return out;
		}
		
		private void getSalidaCuentaInversion(List<RegistroRecaudacion> solicitudProcesar, Connection con, EntradaRecaudacionTraspaso entrada, String monedaSistema, String fechaSistema, Long IdEmpleadoDigitador, Long IdPortafolioCorredor, BigDecimal vMontoTotalCorredora, String simulador ) throws Exception {
			
			
			HashMap<Long,Long> ids 							= 	new HashMap<Long,Long>();
			Long Contador 									= 	0L;
			CrearCargoAbonoEntrada entradaCrearCargoAbono 	=	new CrearCargoAbonoEntrada();
				
			Long i 									= 	1L;
			
			for(RegistroRecaudacion solicitud : solicitudProcesar){
				
				
				ObjCargoAbonoAGenerar obj 				= 	new ObjCargoAbonoAGenerar();
				ObjCargoAbonoAGenerar abonoCorredora 	= 	new ObjCargoAbonoAGenerar();
				ObjCargoAbonoAGenerar abonoCliente 		= 	new ObjCargoAbonoAGenerar();
				ObjCargoAbonoAGenerar cargoCorredora	= 	new ObjCargoAbonoAGenerar();
				
				
				
				obj.NumeroCargoAbono 				= 	i; //i;
				obj.IdPortafolio 					= 	IdPortafolioCorredor;
				obj.IdMoneda 						= 	monedaSistema;
				obj.IdTipoMovimiento 				= 	_c.CargoCuentaInversion;
				obj.Monto 							=	solicitud.MontoDeposito; //sol.SumaTotal; //
				obj.IdMonedaOperacion 				= 	monedaSistema;
				obj.MontoOperacion 					= 	solicitud.MontoDeposito; //sol.SumaTotal; //
				obj.IdConcepto 						=	solicitud.IdConcepto;
				obj.Descripcion 					= 	solicitud.DescripccionConcepto;
				obj.IdCondicionDeLiquidacion 		= 	"";
				obj.FechaCondicionDeLiquidacion 	= 	fechaSistema;
				obj.IdEjecutivo 					= 	IdEmpleadoDigitador;
				obj.IdSucursal 						= 	0L;
				obj.IdTipoProducto 					= 	_c.TipoProducto;
				obj.IdTipoOperacion 				= 	_c.TipoOperacionAporteFondoMutuo;
				obj.IdOperacion 					= 	solicitud.IdTraspaso;
				obj.FechaOperacion 					= 	fechaSistema;
				obj.FechaLiquidacionOperacion 		= 	fechaSistema;
				obj.TipoCambioOperacion 			= 	new BigDecimal(0);
				obj.IdTipoComprobante 				= 	_c.TipoComprobanteCuentaInversion;
				obj.IdComprobante 					= 	solicitud.IdTraspaso;
				obj.FolioComprobante 				= 	solicitud.IdTraspaso.toString();
				obj.IdComprobanteDetalle 			= 	solicitud.IdTraspaso;
				obj.IdTipoMovimientoAsociado 		= 	"";
				obj.IdMovimientoAsociado	 		= 	0L;
				obj.FechaComprobante 				= 	fechaSistema;
				obj.IdInstrumento 					= 	0L;
				obj.IdTipoInstrumento 				= 	"";
				obj.Nemo 							= 	"";
				obj.EsCargoAbonoPorTraspaso 		= 	_c.NO;
				obj.GeneroContraparteEnTesoreria 	= 	_c.NO;
				obj.CertificadoAutomatico 			= 	_c.SI;
				ids.put(i, solicitud.IdMandatoPAC);
				i++;
				
					
				if(Contador == 0){
					
					abonoCorredora 							= 	Utils.copyPojo(obj, abonoCorredora);
					abonoCorredora.NumeroCargoAbono 		= 	i; //i;
					abonoCorredora.IdTipoMovimiento 		= 	_c.AbonoCuentaInversion;
					abonoCorredora.Monto 					=	vMontoTotalCorredora;
					abonoCorredora.MontoOperacion 			= 	vMontoTotalCorredora; //
					
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(abonoCorredora);
					ids.put(i, solicitud.IdTraspaso);
					i++;
					
					
				}
				
				
				abonoCliente 							= 	Utils.copyPojo(obj, abonoCliente);
				abonoCliente.NumeroCargoAbono 			= 	i; //i;
				abonoCliente.Monto          			= 	solicitud.MontoDeposito; //
				abonoCliente.MontoOperacion 			= 	solicitud.MontoDeposito; //
				abonoCliente.IdPortafolio 				= 	solicitud.IdPortafolio;
				abonoCliente.IdTipoMovimiento 			= 	_c.AbonoCuentaInversion;
				
				entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(abonoCliente);
				ids.put(i, solicitud.IdMandatoPAC);
				i++;
				
				
		
				cargoCorredora 							= 	Utils.copyPojo(obj, cargoCorredora);
				cargoCorredora.IdTipoMovimiento 		= 	_c.CargoCuentaInversion;
				cargoCorredora.Monto          			= 	solicitud.MontoDeposito;
				cargoCorredora.MontoOperacion 			= 	solicitud.MontoDeposito;
				cargoCorredora.IdPortafolio 			= 	IdPortafolioCorredor;
				entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoCorredora);
				ids.put(i, solicitud.IdMandatoPAC);
				i++;
				
			}
			
				
			entradaCrearCargoAbono.p_IdEmpresa 			= 	entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario 			= 	entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo 			= 	entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion 	=  	simulador;
			entradaCrearCargoAbono.p_UsaReglasNegocio 	=  	"S";
			entradaCrearCargoAbono.p_FechaSistema 		= 	fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= 	"N";
			
			CrearCargoAbono salida 							= 	new CrearCargoAbono();
			CrearCargoAbonoSalida salidaCrearCargoAbono 	= 	new CrearCargoAbonoSalida();
			logger.info("Llamada a CrearCargoAbono");
			salidaCrearCargoAbono = salida.logica(con, entradaCrearCargoAbono);
			logger.info("Fin CrearCargoAbono");

			out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES) {

				
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo) ) {
					Long id 	= 	ids.get(mje.id);
					
					if(id != null) {
						for(RegistroRecaudacion solicitud : solicitudProcesar) {
							if(solicitud.IdMandatoPAC.equals(id)) {
								
								solicitud.EstadoRendicion 			= mje.descripcion;
								solicitud.EstadoProcesoRendicion	= mje.codigo;
								solicitud.IdentServicio				= solicitud.IdentServicio;
								solicitud.IdMandatoPAC				= solicitud.IdMandatoPAC;

								out.P_OUT_ERRORESCONTADOR++;
								out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, solicitud.EstadoProcesoRendicion, solicitud.EstadoRendicion));
								out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
								
								
								//out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion));
								
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
									solicitud.EstadoProceso = _c.EstadoRechazado;
									solicitud.Codigo = mje.codigo;
									solicitud.DescripcionSolicitud = mje.descripcion;
								}
							}
						}
							
					}
				}
			}
					
			
			for(ObjCargoAbonoGenerado  gen : salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO) {
				Long id 	= 	ids.get(gen.NumeroCargoAbono);
				
				for(RegistroRecaudacion solicitud : solicitudProcesar) {
					if(solicitud.equals(id)) {
						InMovimientoGenerado movimientoGenerado 	= 	new InMovimientoGenerado();
						movimientoGenerado.IdSecuenciaMovimiento	= 	gen.IdCargoAbonoMovimiento;
						movimientoGenerado.IdEmpresa				=	solicitud.IdEmpresa;
						movimientoGenerado.IdUsuario				=	solicitud.IdUsuario;
						movimientoGenerado.IdModulo					=	_c.ModuloCtaInversion;
						movimientoGenerado.FechaIngreso				=	solicitud.FechaIngreso;
						movimientoGenerado.IdSolicitud				=	solicitud.IdentServicio;
						movimientoGenerado.FolioAPV					=	solicitud.FolioAPV;
						movimientoGenerado.FolioCSC					=	solicitud.FolioCSC;
				
						solicitud.MovimientoGenerado.add(movimientoGenerado); 
					}
					
				}
			}
			
		}
		
		
		private void getSalidaFFMM(List<RegistroRecaudacion> solicitudProcesar, Connection con, List<ContratoSuscripcionCuotaDetalle> distribucionCSC, List<DistribucionInversion> distribucionSAPV, String monedaSistema, EntradaRecaudacionTraspaso entrada, String simulador, String fechaSistema) throws Exception {
			
			long iterator = 1L;
			HashMap<Long,String> ids 															= 	new HashMap<Long,String>();
			SalidaCrearSolicitudAporteRescateNormal salida 										=	new SalidaCrearSolicitudAporteRescateNormal();
			EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal 	= 	new EntradaCrearSolicitudAporteRescateNormal();
			CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal 				= 	new CrearSolicitudAporteRescateNormal();
			logger.info("INFO::ProcesarDistribucion.getSalidaFFMM - Recorre Solicitudes");
			for(RegistroRecaudacion solicitud : solicitudProcesar){
					
				if(! (solicitud.FolioCSC.equals("0") || solicitud.FolioCSC.equals(""))) {
						
					for(ContratoSuscripcionCuotaDetalle cscd : distribucionCSC) { // RECORREMOS LOS DETALLES Y CREAMOS UNA SOLICITUD POR CADA DISTRIBUCION DEL CONTRATO

						if(cscd.IdFolioCSC.equalsIgnoreCase(solicitud.FolioCSC) ) {
								
							CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
								
							s.IdInterno 					= 	iterator;
							s.IdCliente 					= 	solicitud.IdCliente;
							s.IdPortafolio 					= 	cscd.IdPortafolio;
							s.TipoSolicitud 				= 	_c.TipoSolicitudAporte;
							s.IdSerieFondo 					= 	cscd.Serie;
							s.CalculoPorMontoCuota 			=  	_c.CalculoPorMonto;
							s.EsPorMontoLiquido 			= 	_c.NO;
							s.Monto 						= 	cscd.Monto;
							s.NumeroCuotas 					=  	BigDecimal.ZERO;
							s.IdMoneda 						= 	cscd.IdMonedaInstrumento;
							s.IdMedioDePago 				= 	_c.CuentaInversion;
							s.IdPreferenciaPagoPortafolio 	= 	0L;
							s.Visado 						= 	_c.NO;
							s.SolicitudBatch 				= 	_c.NO;
							s.OrigenOperacion 				= 	_c.OrigenManual;
							s.FechaOperacion 				= 	"0001-01-01";
							s.IdRegimenTributario 			= 	cscd.Regimen;
							s.Glosa 						= 	solicitud.Observacion;
							s.IndicadorMLT 					= 	_c.NO;
							s.IdSerieFondoDestino 			= 	"";
							s.IdEmisorDestino 				= 	"";
							s.IdAutorizadoOrigen 			= 	0L;
							s.Porcentaje 					= 	BigDecimal.ZERO;
							s.FolioContrato 				= 	0L;
							s.IngresoAutomatico				= _c.SI;
							s.Canal							= cscd.Canal;
							s.TipoContratoOperacion			= "CSC";
							s.FolioContratoOperacion		= cscd.IdFolioCSC;
							ids.put(iterator, solicitud.FolioCSC);
								
							entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
							iterator++;
						}
					}
						
				}else {
						
					for(DistribucionInversion di: distribucionSAPV) {// RECORREMOS LOS DETALLES Y CREAMOS UNA SOLICITUD POR CADA DISTRIBUCION DEL CONTRATO
							
						if(di.FolioContratoSAAPV.equals(solicitud.FolioAPV.toString()) && di.IdTipoInstrumento.equals("FFM")) {
								
								CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
								
								s.IdInterno 					= 	iterator;
								s.IdCliente						= 	solicitud.IdCliente;
								s.IdPortafolio 					= 	solicitud.IdPortafolio;
								s.TipoSolicitud 				= 	_c.TipoSolicitudAporte;
								s.IdSerieFondo 					= 	di.Nemotecnico;
								s.CalculoPorMontoCuota 			=  	_c.CalculoPorMonto;
								s.EsPorMontoLiquido 			= 	_c.NO;
								s.Monto 						= 	di.MontoPesos;
								s.NumeroCuotas 					=  	BigDecimal.ZERO;
								s.IdMoneda 						= 	di.IdMonedaInstrumento;
								s.IdMedioDePago 				= 	_c.CuentaInversion;
								s.IdPreferenciaPagoPortafolio 	= 	0L;
								s.Visado 						= 	_c.NO;
								s.SolicitudBatch 				= 	_c.NO;
								s.OrigenOperacion 				= 	_c.OrigenManual;
								s.FechaOperacion 				= 	"0001-01-01";
								s.IdRegimenTributario	 		= 	di.Codigo;
								s.Glosa 						= 	solicitud.Observacion;
								s.IndicadorMLT 					= 	_c.NO;
								s.IdSerieFondoDestino 			= 	"";
								s.IdEmisorDestino 				= 	"";
								s.IdAutorizadoOrigen 			= 	0L;
								s.Porcentaje 					= 	BigDecimal.ZERO;
								s.FolioContrato 				= 	0L;
								s.IngresoAutomatico 			= 	_c.SI;
								s.Canal							=  di.Canal;
								s.TipoContratoOperacion			= "SAAPV";
								s.FolioContratoOperacion		= di.FolioContratoSAAPV;
								ids.put(iterator, solicitud.FolioAPV+"");
								
								entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
								iterator++;
						}
					}
				}
			}
			if(!entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.isEmpty())
				logger.info("INFO::ProcesarDistribucion.getSalidaFFMM - Cantidad Solicitudes Aporte Rescate: " + Integer.toString(entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size()) );
			
			AtomicLong indexSpot = new AtomicLong(1L);
			CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
			EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
			entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCalculo.p_Series = entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.parallelStream()
					.filter(solicitud->_c.Dolar.equals(solicitud.IdMoneda))
					.map(solicitud->{
						CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
						serie.IdInterno = solicitud.IdInterno;
						serie.Fecha = fechaSistema;
						serie.IdSerieFondo = solicitud.IdSerieFondo;
						serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
						serie.Monto = solicitud.Monto;
						serie.NumeroCuotas = solicitud.NumeroCuotas;
						serie.IdMonedaFondo = solicitud.IdMoneda;
						serie.IdMonedaInversion = "CLP";
						serie.TipoSolicitud = solicitud.TipoSolicitud;
						serie.TipoFactorAjuste = _c.UF;
						serie.IdEmpresa = entrada.p_IdEmpresa;
						return serie;
					})
					.collect(Collectors.toList());
			
			SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
			Map<Long,CalcularMontoNumeroCuota.Resultado> hashSalidaCalculo = salidaCalculo.P_OUT_RESULTADO.stream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
			entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.stream()
			.filter(solicitud->_c.Dolar.equals(solicitud.IdMoneda))
			.forEach(solicitud->{
				CalcularMontoNumeroCuota.Resultado resultado = hashSalidaCalculo.get(solicitud.IdInterno);
				if(resultado != null) {
					List<InFormaPagoSpot> p_FormaPagoSpot = new ArrayList<>();
					p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
					
					InFormaPagoSpot FormaPagoSpot = new InFormaPagoSpot();
					FormaPagoSpot.IdInternoPagoOperacion = indexSpot.getAndIncrement();
					FormaPagoSpot.IdComprobantePagoOperacion= 0L;
					FormaPagoSpot.IdTipoMovimiento = "MOVEGR";
					FormaPagoSpot.IdFormaDePago    = "VENSPT_CIN_USD_0";
					FormaPagoSpot.IdOperacion      = 	solicitud.IdInterno;
					FormaPagoSpot.RecibePagaPortafolio = "P";
					FormaPagoSpot.IdMoneda         =  solicitud.IdMoneda;
					FormaPagoSpot.IdMonedaLiquidacion  =  solicitud.IdMoneda;
					FormaPagoSpot.MontoFormaDePago = resultado.Monto;
					FormaPagoSpot.AfectarTesoreria =  "N";
					p_FormaPagoSpot.add(FormaPagoSpot);
					
					InFormaPagoSpot FormaPagoSpot2 = new InFormaPagoSpot();
					FormaPagoSpot2.IdInternoPagoOperacion = indexSpot.getAndIncrement();
					FormaPagoSpot2.IdComprobantePagoOperacion= 0L;
					FormaPagoSpot2.IdTipoMovimiento = "MOVING";
					FormaPagoSpot2.IdFormaDePago    = "COMSPT_CIN_CLP_0";
					FormaPagoSpot2.IdOperacion      = 	solicitud.IdInterno;
					FormaPagoSpot2.RecibePagaPortafolio = "R";
					FormaPagoSpot2.IdMoneda         = "CLP";
					FormaPagoSpot2.IdMonedaLiquidacion =  "CLP";
					FormaPagoSpot2.MontoFormaDePago = solicitud.Monto;
					FormaPagoSpot2.AfectarTesoreria =  "N";
					p_FormaPagoSpot.add(FormaPagoSpot2);
					
					solicitud.Monto = resultado.Monto;
					solicitud.NumeroCuotas=	resultado.NumeroCuotas;	
					solicitud.IdMonedaPago=monedaSistema;
					solicitud.ForzarSpot=_c.SI;
					entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.addAll(p_FormaPagoSpot);
				}else {
					solicitud.Monto = BigDecimal.ZERO;
				}
			});
			
			
			entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa 			=	entrada.p_IdEmpresa;
			entradaCrearSolicitudAporteRescateNormal.p_IdUsuario 			= 	entrada.p_IdUsuario;
			entradaCrearSolicitudAporteRescateNormal.p_IdModulo 			= 	entrada.p_IdModulo;
			entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion 	= 	simulador;
			entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio 	= 	_c.SI;
			entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso 	= 	_c.CanalAgente;
			entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo 		= 	0L;
			entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso 		= 	"PAC";
			
			logger.info("ENTRADA::ProcesarDistribucion.getSalidaFFMM.crearSolicitudAporteRescateNormal - Llamada CrearSolicitudAporteRescateNormal");
			salida =  crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
			logger.info("SALIDA::ProcesarDistribucion.getSalidaFFMM.crearSolicitudAporteRescateNormal - Llamada CrearSolicitudAporteRescateNormal");
			
			out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES.parallelStream().filter(pM -> !MensajeSalida.MENSAJE_ERROR.equals(pM.tipo)).collect(Collectors.toList()));
			
			// FALTA AGREGAR LAS SOLICITUDES QUE SIGUEN EN PROCESO
			for(MensajeSalida mje : salida.P_OUT_MENSAJES){
				String id 	=  ids.get(mje.id);
				if(id!=null) {
					for(RegistroRecaudacion solicitud : solicitudProcesar) {
						
						try {
							String folioSapv = id;
							if(solicitud.FolioCSC.equals(id) || solicitud.FolioAPV.equalsIgnoreCase(folioSapv) || solicitud.FolioAPV.equals(id)) {
								if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
									
									solicitud.EstadoRendicion 			= mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC;
									solicitud.EstadoProcesoRendicion	= mje.codigo;
									solicitud.IdentServicio				= solicitud.IdentServicio;
									solicitud.IdMandatoPAC				= solicitud.IdMandatoPAC;

									out.P_OUT_ERRORESCONTADOR++;
									out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC));
									out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
									
									
									//out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC));
								} else {
									out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion));
								}
								
							}
						} catch (Exception e) {
						}
					}
				}
				
			}
			
			//InMovimientoGenerado movimientoGenerado 	= 	new InMovimientoGenerado();
			if(salida.P_OUT_ERRORESCONTADOR == 0) {
				for(ResultadoSolicitud res : salida.P_OUT_RESULTADO){
					String codigoMensajeExito = "";
					if (res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(_c.AporteFondoMutuo)) {
						codigoMensajeExito = "UI_SE_CREO_EL_APORTE_FONDO_MUTUO_N";
					} else if(res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(_c.RescateFondoMutuo)) {
						codigoMensajeExito = "UI_SE_CREO_EL_RESCATE_FONDO_MUTUO_N";
					}
					
					String mensajeExito = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensajeExito).add("IdSolicitudAporteRescate", res.IdSolicitudAporteRescate).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida((long) 225,MensajeSalida.MENSAJE_INFORMACION,codigoMensajeExito,mensajeExito));
				}
			
			
				for(ResultadoSolicitud  gen : salida.P_OUT_RESULTADO) {
					for(RegistroRecaudacion solicitud : solicitudProcesar) {
						InMovimientoGenerado movimientoGenerado 	= 	new InMovimientoGenerado();
						movimientoGenerado.IdSecuenciaMovimiento	= 	gen.IdSolicitudAporteRescate;
						movimientoGenerado.IdEmpresa				=	solicitud.IdEmpresa;
						movimientoGenerado.IdUsuario				=	solicitud.IdUsuario;
						movimientoGenerado.IdModulo					=	_c.ModuloCtaInversion;
						movimientoGenerado.FechaIngreso				=	solicitud.FechaIngreso;
						movimientoGenerado.IdSolicitud				=	solicitud.IdentServicio;
						movimientoGenerado.FolioAPV					=	solicitud.FolioAPV;
						movimientoGenerado.FolioCSC					=	solicitud.FolioCSC;
				
						solicitud.MovimientoGenerado.add(movimientoGenerado); 
					}
				} 
			}
			
		}
		
		private ConsultarInstrumentoPrecioBolsa_Salida getInstrumentos(List<RegistroRecaudacion> solicitudProcesar, Connection con, EntradaRecaudacionTraspaso entrada, List<DistribucionInversion> distribucionSAPV,  String fechaSistema) throws Exception {
			
			ConsultarInstrumentoPrecioBolsa_Entrada entradaPrecioBolsa 	= 	new ConsultarInstrumentoPrecioBolsa_Entrada();
			ConsultarInstrumentoPrecioBolsa precioBolsa 				= 	new ConsultarInstrumentoPrecioBolsa();
			
			List<Long> idsInstrumento 									= 	new ArrayList<Long>();
			entradaPrecioBolsa.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaPrecioBolsa.p_IdUsuario = entrada.p_IdUsuario;
			
			long iteradorInstrumento = 1l;
			
			entradaPrecioBolsa.p_Instrumentos = distribucionSAPV.stream()
					.filter(di -> di.IdTipoInstrumento.equals("ACN") && !idsInstrumento.contains(di.IdInstrumento))
					.map(di ->{
						Instrumento instrumento 				= 	new Instrumento();
						
						instrumento.IdInterno					=	iteradorInstrumento;
						instrumento.IdInstrumento 				= 	di.IdInstrumento;
						instrumento.IdBolsa						= 	"BCS";
						instrumento.Fecha						= 	fechaSistema;
						instrumento.IdCondicionDeLiquidacion 	= 	"T2";
						instrumento.IdEmpresa					= 	entrada.p_IdEmpresa;
						return instrumento;
					})
					.collect(Collectors.toList());

			return precioBolsa.logica(con, entradaPrecioBolsa);
		}
		
		
	
		private void getOrdenes(List<RegistroRecaudacion> solicitudProcesar, Connection con, List<DistribucionInversion> distribucionSAPV, String monedaSistema, String fechaSistema, EntradaRecaudacionTraspaso entrada,  String simulador) throws Exception {
			
			long iterator = 1L;

			
			CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada 				= 	new CrearOrdenes.CrearOrdenesEntrada();
			List<CrearOrdenes.ObjOrdenesEntrada> ObjOrden 						= 	new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
			
			List<CalcularMontoYCobrosOrden.Orden_Entrada> ObjCalculoMontoOrden 	= 	new ArrayList<CalcularMontoYCobrosOrden.Orden_Entrada>();
			List<Orden_Entrada> calculos										= 	new ArrayList<Orden_Entrada>();
			
			ConsultarInstrumentoPrecioBolsa_Salida salidaPrecioBolsa = getInstrumentos(solicitudProcesar, con, entrada, distribucionSAPV, fechaSistema);
			
			HashMap<Long,Object> instrumentos = new HashMap<Long,Object>();
			
			for(DatosInstrumentosPrecioBolsa_Salida precio : salidaPrecioBolsa.P_OUT_INSTRUMENTO) {
				if(instrumentos.get(precio.IdInstrumento) == null) {
					instrumentos.put(precio.IdInstrumento, precio);
				}
			}
			
			if (salidaPrecioBolsa.P_OUT_ERRORESCONTADOR != null){out.P_OUT_ERRORESCONTADOR += salidaPrecioBolsa.P_OUT_ERRORESCONTADOR;}
			
			if (salidaPrecioBolsa.P_OUT_ERRORESCONTADOR == 0 && !salidaPrecioBolsa.P_OUT_INSTRUMENTO.isEmpty() ) {
				
				
				
				
				long y = 1l;	
				for(DistribucionInversion di: distribucionSAPV) {
					DatosInstrumentosPrecioBolsa_Salida i = (DatosInstrumentosPrecioBolsa_Salida) instrumentos.get(di.IdInstrumento);
					
					if(i != null) {
						Orden_Entrada calculo  	=  	getCalculoOrdenes(di,fechaSistema,i,y);
						calculos.add(calculo);
						y++;
					}
					
				}
				
				
			}
		

					
			for(DistribucionInversion di: distribucionSAPV) {
				
				if(di.IdTipoInstrumento.equals("ACN")) {

					for (Orden_Entrada c : calculos) {
						if(c.IdInstrumento.equals(di.IdInstrumento) && c.IdPortafolio.equals(di.IdPortafolio) ) {
							
							DatosInstrumentosPrecioBolsa_Salida i = (DatosInstrumentosPrecioBolsa_Salida) instrumentos.get(c.IdInstrumento);
								
							if(i!=null) {
								
								ObjCalculoMontoOrden.add(c);
								CrearOrdenes.ObjOrdenesEntrada Orden 	= 	new CrearOrdenes.ObjOrdenesEntrada();
								Orden.IdInterno 						= 	iterator;
								Orden.FechaRecepcion		 			= 	"0001-01-01 00:00:00";
								Orden.FechaVigencia 					= 	"0001-01-01";
								Orden.FechaEmision 						= 	"0001-01-01";
								Orden.FechaOperacion 					= 	"0001-01-01";
								Orden.IdCondicionDeLiquidacion 			= 	"T2"; 
								Orden.IdTipoOperacion 					= 	"COMACN";
								Orden.IdTipoVigencia 					= 	"OP";
								Orden.Glosa	 							= 	"";
								
								// COMPRA
								Orden.MontoNeto							=	di.MontoPesos;
								Orden.EsPorMonto						=	"S";
								Orden.Cantidad 							= 	BigDecimal.ZERO;
								
								
								Orden.ValorRescate 						= 	BigDecimal.ZERO;
								Orden.IdBoveda 							= 	"DCV";
								Orden.IdFormaCustodia 					= 	"CUS"; 
								Orden.IdPorcentajeVisibilidad 			=	1l;
								Orden.PrecioLimite 						= 	i.PrecioCierre;
								Orden.EsFacturaInmediata 				= 	"N";
								Orden.IdTipoPrecio 						= 	"M";
								Orden.IdPortafolio 						= 	c.IdPortafolio;
								Orden.IdTipoCanalIngreso 				= 	"AGE"; 
								Orden.IdTipoReajuste 					= 	"";
								Orden.IdEmisor 							= 	c.IdEmisor;
								Orden.IdBolsa 							= 	"BCS";
								Orden.EspecificaBolsa 					= 	"S";
								Orden.IdMonedaLiquidacionOrden 			= 	i.IdMoneda; 
								Orden.TipoCambio 						= 	BigDecimal.ZERO;
								Orden.IdInstrumento 					= 	c.IdInstrumento;
								Orden.IdInstrumentoSubyacente 			= 	0l;
								Orden.IdCondicionDeLiquidacionPlazo 	= 	"CN";
								Orden.TasaVentaCorta 					= 	BigDecimal.ZERO; 
								Orden.PlazoVentaCorta 					= 	0l;
								Orden.EsVentaCorta 						= 	"N";
								Orden.TasaSimultanea 					= 	BigDecimal.ZERO;
								Orden.PlazoSimultanea 					= 	0l;
								Orden.PlazoIF 							= 	0l;
								Orden.TasaRFIF 							= 	BigDecimal.ZERO;
								Orden.EsOD 								= 	"N";
								Orden.EsFungible 						= 	"N";
								Orden.IdTipoLibroOrden 					= 	"NORMAL";
								Orden.IdLibroOrdenes 					= 	null;
								Orden.IdCuentaCustodioPortafolio 		= 	null;
								Orden.IdCorredorCorresponsal 			= 	null;
								Orden.IdPersonaAutorizadoDarOrdenes 	= 	0l;
								Orden.IdTipoCanalDistribucion 			= 	"MAN";
								Orden.EsOrdenEspecial 					= 	"N";
								Orden.PagoCobroCuentaInversion 			= 	"S"; 
								Orden.IdPreferenciaPagoPortafolio 		= 	c.IdPortafolio;
								Orden.BaseTasa 							= 	0l;
								Orden.TipoInteres 						= 	"";
								Orden.IdMecanismoCaptacion 				= 	"OM";
								
								ObjOrden.add(Orden);
								iterator++;
							}
						}
						
					}
					
					
				}
			}
				
			
			/*ORDENES*/
			if (!ObjOrden.isEmpty()){
				
				//CalculoMontoyCobroOrden
				CalcularMontoYCobrosOrden procesoCalcularMonto = new CalcularMontoYCobrosOrden();
				CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada paramsCalcularMonto_Entrada = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
					
				paramsCalcularMonto_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
				paramsCalcularMonto_Entrada.p_IdUsuario = entrada.p_IdUsuario;
				paramsCalcularMonto_Entrada.p_IdModulo 	= entrada.p_IdModulo;  
				paramsCalcularMonto_Entrada.p_Ordenes 	= ObjCalculoMontoOrden;
	
				CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida OutCalcularmonto = procesoCalcularMonto.logica(con, paramsCalcularMonto_Entrada); 

				for(int i = OutCalcularmonto.p_out_Ordenes.size()-1; i >= 0; i --){
					for(int j = ObjOrden.size()-1; j >= 0; j --){
						if(OutCalcularmonto.p_out_Ordenes.get(i).IdInterno.toString().equals(ObjOrden.get(j).IdInterno.toString())){
							ObjOrden.get(j).Cantidad = OutCalcularmonto.p_out_Ordenes.get(i).CantidadOperacion;
						}
					}
				}
					
				
			    
				paramsOrdenesEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				paramsOrdenesEntrada.p_IdUsuario = entrada.p_IdUsuario;
				paramsOrdenesEntrada.p_IdModulo = entrada.p_IdModulo;
				paramsOrdenesEntrada.p_simularOperacion = entrada.p_simular;
				paramsOrdenesEntrada.p_UsaReglasNegocio = "S"; // FELIPE
				paramsOrdenesEntrada.p_Ordenes = ObjOrden;
			    
				CrearOrdenes procesoOrdenes = new CrearOrdenes();
				logger.info("Llamada a CrearOrdenes");
				CrearOrdenes.CrearOrdenesSalida salida = procesoOrdenes.logica(con, paramsOrdenesEntrada);
				logger.info("Fin CrearOrdenes");
				
				// FALTA AGREGAR LAS SOLICITUDES QUE SIGUEN EN PROCESO
				for(MensajeSalida mje : salida.P_OUT_MENSAJES){
					
					for(RegistroRecaudacion solicitud : solicitudProcesar) {
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
							solicitud.EstadoRendicion 			= mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC;
							solicitud.EstadoProcesoRendicion	= mje.codigo;
							solicitud.IdentServicio				= solicitud.IdentServicio;
							solicitud.IdMandatoPAC				= solicitud.IdMandatoPAC;

							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC));
							out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
							
							//out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + " ,IdMandatoPAC: " + solicitud.IdMandatoPAC));
						} else {
							out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion));
						}
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
							solicitud.EstadoProceso = _c.EstadoRechazado;
							solicitud.Codigo = mje.codigo;
							solicitud.DescripcionSolicitud = mje.descripcion;
						}
						
						
					}
				}
				
				
				for(ObjRESULTADO  gen : salida.P_OUT_RESULTADO) {
					for(RegistroRecaudacion solicitud : solicitudProcesar) {
						InMovimientoGenerado movimientoGenerado 	= 	new InMovimientoGenerado();
						movimientoGenerado.IdSecuenciaMovimiento	= 	gen.IdOrden;
						movimientoGenerado.IdEmpresa				=	solicitud.IdEmpresa;
						movimientoGenerado.IdUsuario				=	solicitud.IdUsuario;
						movimientoGenerado.IdModulo					=	_c.ModuloCtaInversion;
						movimientoGenerado.FechaIngreso				=	solicitud.FechaIngreso;
						movimientoGenerado.IdSolicitud				=	solicitud.IdentServicio;
						movimientoGenerado.FolioAPV					=	solicitud.FolioAPV;
						movimientoGenerado.FolioCSC					=	solicitud.FolioCSC;
				
						solicitud.MovimientoGenerado.add(movimientoGenerado); 
					}
				}

			}
			
		}
		
	
		private void getSalidaTesoreria(List<RegistroRecaudacion> solicitudProcesar, SQL sql, EntradaRecaudacionTraspaso entrada, Connection con, String monedaSistema, String fechaSistema, Long IdPortafolioCorredor, Long IdPersonaCorredor, Long IdEmpleadoDigitador, Long idCtaBancoCorredor, String simulacion, Long IdEmpresa, Long IdUsuario, Long idCajaTesoreriaPac, Long idPersonaBanco ) throws Exception {
			
			CrearIngresoEgreso crearIngresoEgreso 					= 	new CrearIngresoEgreso();
			CrearIngresoEgreso_Entrada entradacrearIngresoEgreso 	= 	new CrearIngresoEgreso_Entrada();
			CrearIngresoEgreso_Salida salida 						=	new CrearIngresoEgreso_Salida();
			
			HashMap<Long,Long> mapIdSolicitud = new HashMap<Long,Long>();
			
			
			List<PortafolioCliente> lstPortafolioCliente = new ArrayList<PortafolioCliente>();
			HashMap<String, Object> hashPortafolioCliente = new HashMap<String, Object>();
			List<Long> idsPortafolios = new ArrayList<Long>();
			HashMap<String, Object> param = new HashMap<String,Object>();
			
			for(RegistroRecaudacion solicitud : solicitudProcesar)
			{
				if (!idsPortafolios.contains(solicitud.IdPortafolio))
				{
					idsPortafolios.add(solicitud.IdPortafolio);
				}
			}
			
			if (!idsPortafolios.isEmpty())
			{
				param.clear();
				param.put("IdsPortafolio", idsPortafolios);
				lstPortafolioCliente = sql.query(con, SQL_SELECT_PORTAFOLIO_CLIENTE, param, PortafolioCliente.class);
				
				hashPortafolioCliente = Utils.List2Hash(lstPortafolioCliente, "IdPortafolio");
				
			}
			
			long i = 1l;
			long j = "S".equals(simulacion) ? 1 : -1;
			for(RegistroRecaudacion solicitud : solicitudProcesar){
				
				
				PortafolioCliente infoPortafolioCliente = (PortafolioCliente) hashPortafolioCliente.get(solicitud.IdPortafolio);
				
				
				IngresoEgreso_Entrada ie 					= 	new IngresoEgreso_Entrada();
				IngresoEgreso_Entrada ie2 					= 	new IngresoEgreso_Entrada();
				IngresoEgreso_Entrada ie3 					= 	new IngresoEgreso_Entrada();
				FormaDePago_Entrada fp 						= 	new FormaDePago_Entrada();
				FormaDePago_Entrada fp2						= 	new FormaDePago_Entrada();
				FormaDePago_Entrada fp3						= 	new FormaDePago_Entrada();

				
				// -------------------------------------------------------------------------------------
				// 111.ENTRADA INGRESO Recaudacion	
				// -------------------------------------------------------------------------------------
				ie.NumeroReferencia 						= 	i * j;
				ie.IdTipoMovimiento 						=  	_c.IngresoTesoreria ; 
				
				
				
				// Socio de Negocio: Cliente
				ie.IdTipoPersona 							= 	_c.TipoPersonaCliente;				
				ie.IdPersona 								= 	infoPortafolioCliente != null ? infoPortafolioCliente.IdPersona : 0L;
				ie.IdPortafolio 							= 	solicitud.IdPortafolio;
				
				//Titular Pago: Corredora
				ie.IdTipoPersonaTitularPago 				=  	_c.TipoPersonaCorredor;
				ie.IdPersonaTitularPago 					=  	IdPersonaCorredor;
				ie.IdPortafolioTitularPago 					= 	IdPortafolioCorredor; 

				
				ie.IdMoneda 								= 	monedaSistema;
				ie.Monto    								= 	solicitud.MontoDeposito;
				ie.FechaLiquidacion 						= 	fechaSistema;
				if(solicitud.IdConcepto.equals("ABNAPV")) {
					ie.IdConcepto 							= 	_c.AAPVIR;
				}else{
					ie.IdConcepto 							= 	_c.ACSCIR;
				}

			
				ie.IdCaja 									= 	idCajaTesoreriaPac; 
				ie.SecuenciaAgrupacion 						= 	"";
				ie.SecuenciaRelacionada 					= 	0L;
				ie.Glosa 									= 	"";
				ie.IdEmpleadoDigitador 						= 	IdEmpleadoDigitador;
				ie.IdSucursalEmpleadoDigitador 				= 	0L;
				ie.IdTipoProducto 							= 	_c.TipoProducto;
				ie.IdTipoOperacion 							= 	_c.TipoOperacionAporteFondoMutuo;
				ie.IdTipoComprobante 						= 	_c.TipoComprobanteTesoreria;
				ie.IdComprobante 							= 	solicitud.IdTraspaso;
				ie.FechaComprobante 						= 	Fecha.stringToSqlDate(solicitud.FechaAceptacion);
				ie.FechaLiquidacionComprobante 				= 	"0001-01-01";
				ie.IdOperacion 								= 	solicitud.IdTraspaso;
				ie.FechaOperacion 							= 	fechaSistema;
				ie.IdCondicionLiquidacion 					= 	_c.T0;
				ie.IdBolsa 									= 	_c.BCS;
				ie.IdMonedaOperacion 						= 	monedaSistema;
				ie.MontoOperacion 							= 	solicitud.MontoDeposito; 
				ie.IdTipoReajuste 							= 	"";
				ie.FechaParidad 							= 	"0001-01-01";
				ie.ValorParidad 							= 	BigDecimal.valueOf(0);	
				ie.FolioCartera 							= 	0L;
				ie.IdInstrumento 							= 	0L;
				ie.IdTipoInstrumento 						= 	"";
				ie.IdClasificacionTipoInstrumento 			= 	"";
				ie.Bilateral 								= 	_c.NO;
				ie.GeneraComprobante 						= 	_c.SI;
				ie.AgruparEnComprobante 					= 	_c.NO;
				ie.MueveCuentaInversion 					= 	_c.SI;
				ie.TitularPago 								= 	_c.NO;
				mapIdSolicitud.put(i * j,solicitud.IdMandatoPAC);
					
					
				// FORMA DE PAGO
				fp.NumeroReferencia 						= 	i * j;
				fp.NumeroReferenciaFormaDePago 				=  	i * j;
				fp.IdMedioDePago 							= 	solicitud.IdMedioDePago;
				fp.IdModalidadDePago 						= 	_c.MODCAJ;
				fp.IdMoneda 								= 	monedaSistema;
				fp.Monto 									=  	solicitud.MontoDeposito; 
				fp.IdCuentaBancariaCorredora 				=  	idCtaBancoCorredor;
				fp.IdTarjetaBancariaCorredora 				= 	0L;
				fp.IdChequera 								= 	0L;
				fp.NumeroDocumentoCorredora 				= 	0L;
				fp.FechaDocumentoPagoCorredora 				= 	"0001-01-01";
				fp.IdCuentaBancariaSocio 					= 	0L;
				fp.IdBancoSocio 							= 	0L;
				fp.NombreCuentaBancariaSocio 				= 	"";
				fp.NumeroCuentaBancariaSocio 				= 	"";
				fp.IdTipoCuentaBancariaSocio 				= 	"";
				fp.NombreTipoCuentaBancariaSocio 			= 	"";
				fp.IdTarjetaBancariaSocio 					= 	0L;
				fp.IdTipoTarjetaBancariaSocio 				= 	"";
				fp.NombreTipoTarjetaBancariaSocio 			= 	"";
				fp.NumeroTarjetaBancariaSocio 				= 	"";
				fp.NumeroSerieSocio 						= 	"";
				fp.NumeroDocumentoSocio 					= 	0L;
				fp.FechaDocumentoPagoSocio 					= 	"0001-01-01";
				
					
				entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie);
				entradacrearIngresoEgreso.p_FormaDePago.add(fp);
				
				//---------------------------------------------------------------
				//222.CORREDOR : EGRESO	CTA. Recaudacion	
				//---------------------------------------------------------------
				i++;
				
				ie2 										= 	Utils.copyPojo(ie, ie2);
				if(solicitud.IdConcepto.equals("ABNAPV")) {
					ie2.IdConcepto 							= 	_c.AAPVER;
				}else{
					ie2.IdConcepto 							= 	_c.ACSCER;
				}
				ie2.IdTipoMovimiento 						=  	_c.EgresoTesoreria;  
				ie2.NumeroReferencia 						= 	i * j;	
				

				
				// Socio de Negocio: Corredora
				ie2.IdTipoPersona 							= 	_c.TipoPersonaCorredor;	
				ie2.IdPersona 								= 	IdPersonaCorredor;
				ie2.IdPortafolio 							= 	IdPortafolioCorredor;
				
				
				// Titular Pago: Corredora
				ie2.IdTipoPersonaTitularPago 				=  	_c.TipoPersonaCorredor;
				ie2.IdPersonaTitularPago 					= 	IdPersonaCorredor;
				ie2.IdPortafolioTitularPago 				= 	IdPortafolioCorredor;
				
				
				mapIdSolicitud.put(i * j,solicitud.IdMandatoPAC);
				
				fp2 										= 	Utils.copyPojo(fp, fp2);
				fp2.NumeroReferencia 						= 	i * j;
				fp2.NumeroReferenciaFormaDePago 			= 	i * j;
				
				entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie2);
				entradacrearIngresoEgreso.p_FormaDePago.add(fp2);
				
				
				//--------------------------------------------------------------------------------
				//333 CORREDOR : INGRESO Cta. Disribucion
				//--------------------------------------------------------------------------------
				i++;
			
				ie3 = Utils.copyPojo(ie, ie3);
				if(solicitud.IdConcepto.equals("ABNAPV")) {
					ie3.IdConcepto 							= 	_c.AAPVID;
				}else{
					ie3.IdConcepto 							= 	_c.ACSCID;
				}
				ie3.IdTipoMovimiento 						=  	_c.IngresoTesoreria;
				ie3.NumeroReferencia 						= 	i * j;
		

				
				
				// Socio de Negocio: Corredora
				ie3.IdTipoPersona 							= 	_c.TipoPersonaCorredor;				
				ie3.IdPersona 								= 	IdPersonaCorredor;
				ie3.IdPortafolio 							= 	IdPortafolioCorredor;
				
				//Titular Pago: Entidad que paga (Banco)
				ie3.IdTipoPersonaTitularPago 				=  	_c.TipoPersonaContraparte;
				ie3.IdPersonaTitularPago 					=  	idPersonaBanco;
				ie3.IdPortafolioTitularPago 				= 	0L; // Banco como contraparte no posea portafolio
				
				
				
				mapIdSolicitud.put(i * j,solicitud.IdMandatoPAC);
				
				
				fp3 = Utils.copyPojo(fp, fp3);
				fp3.NumeroReferencia 						= 	i * j;
				fp3.NumeroReferenciaFormaDePago 			= 	i * j;
				fp3.IdCuentaBancariaCorredora 				=  	idCtaBancoCorredor;
				entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie3);
				entradacrearIngresoEgreso.p_FormaDePago.add(fp3);
				i++;
			}
			
			entradacrearIngresoEgreso.p_IdEmpresa 			= 	entrada.p_IdEmpresa; 
			entradacrearIngresoEgreso.p_IdUsuario 			= 	entrada.p_IdUsuario;
			entradacrearIngresoEgreso.p_IdModulo 			= 	entrada.p_IdModulo;
			entradacrearIngresoEgreso.p_IdOrigenProceso		= 	"PAC";
			entradacrearIngresoEgreso.p_SimularOperacion 	= 	simulacion;
			entradacrearIngresoEgreso.p_UsaReglasNegocio 	= 	"S";
			entradacrearIngresoEgreso.p_EsIngresoAutomatico =  	"S";
			
			logger.info("Llamada a CrearIngresoEgreso");
			
			salida =  crearIngresoEgreso.logica(con, entradacrearIngresoEgreso);
			
			logger.info("Fin CrearIngresoEgreso");
			
			if (salida.P_OUT_ERRORESCONTADOR != null){out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;}
			
			for(MensajeSalida mensaje : salida.P_OUT_MENSAJES) {
				if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) ) {
					Long id 	= 	mapIdSolicitud.get(mensaje.id);
					
					if(id != null) {
						for( RegistroRecaudacion solicitud : solicitudProcesar ) {
							
							if(id.equals(solicitud.IdMandatoPAC)) {
								
								solicitud.EstadoRendicion 			= mensaje.descripcion;
								solicitud.EstadoProcesoRendicion	= mensaje.codigo;
								solicitud.IdentServicio				= solicitud.IdentServicio;
								solicitud.IdMandatoPAC				= solicitud.IdMandatoPAC;

								out.P_OUT_ERRORESCONTADOR++;
								out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, solicitud.EstadoProcesoRendicion, solicitud.EstadoRendicion));
								out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
								
								//out.P_OUT_MENSAJES.add(new MensajeSalida(id, mensaje.tipo, mensaje.codigo,  mensaje.descripcion));
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
									solicitud.EstadoProceso = _c.EstadoRechazado;
									solicitud.Codigo = mensaje.codigo;
									solicitud.DescripcionSolicitud = mensaje.descripcion;
								}
							}
						}
					}
				}
			}
			
			if(simulacion.equals("N")) {
				
				ArrayList<Long> idResultadoComrpobante = new ArrayList<Long>();
				for(ResultadoProceso_Salida resultadoComprobante : salida.P_OUT_RESULTADOPROCESO) {
					if(resultadoComprobante.Campo.equalsIgnoreCase("IdComprobante")) {
						idResultadoComrpobante.add(resultadoComprobante.Id);
					}
				}
				
				if(!idResultadoComrpobante.isEmpty()) {
					SQL _sql											=	new SQL();
					
					
					logger.info("SE INVOCA SERVICIO LIQUIDAR COMPROBANTE (LiquidarComprobante.java)");
					LiquidarComprobante_Entrada liquidarComprobanteEntrada = new LiquidarComprobante_Entrada(); 
					
					liquidarComprobanteEntrada.p_IdEmpresa = IdEmpresa;
					liquidarComprobanteEntrada.p_IdUsuario = IdUsuario;
					liquidarComprobanteEntrada.p_IdModulo = "TES";
					liquidarComprobanteEntrada.p_SimularOperacion = "N"; 
					liquidarComprobanteEntrada.p_UsaReglasNegocio = "S";
					liquidarComprobanteEntrada.p_FechaMovimiento = fechaSistema;
					
					
					//Comprobantes a Liquidar	
					HashMap<String,Object> parametrosConsultarComprobantes = new HashMap<>();
					parametrosConsultarComprobantes.put("idsComprobantes", idResultadoComrpobante);
					List<ComprobanteLiquidar_Entrada> _comprobanteLiquidar = _sql.query(con, SQL_SELECT_Comprobante, parametrosConsultarComprobantes, ComprobanteLiquidar_Entrada.class);
		
					liquidarComprobanteEntrada.p_ComprobanteLiquidar = _comprobanteLiquidar;
						
					LiquidarComprobante liquidarComprobante = new LiquidarComprobante();
					logger.info("Llamada a LiquidarComprobanteTesoreria");
					LiquidarComprobante_Salida liquidarComprobante_Salida = liquidarComprobante.logica(con, liquidarComprobanteEntrada);
					logger.info("Fin LiquidarComprobanteTesoreria");
					String codigoMensajeExito 					= 	"";
					String mensajeExito 						= 	"";
					if (MensajeSalida.getNumeroErrores(liquidarComprobante_Salida.P_OUT_MENSAJES) > 0) {

						//out.P_OUT_MENSAJES.addAll(liquidarComprobante_Salida.P_OUT_MENSAJES);
					} else {

						//out.P_OUT_MENSAJES.addAll(liquidarComprobante_Salida.P_OUT_MENSAJES);
						codigoMensajeExito = "UI_LIQUIDACION_EXITOSA";
						mensajeExito  = Diccionario.getMensaje(con, 1l, codigoMensajeExito).add("Liquidacion", _comprobanteLiquidar.size()).msg();
					}
					
					//Obtiene Id de comprobantes liquidados
					for (LiquidarComprobante.ResultadoProceso_Salida resultadoProcesoLC: liquidarComprobante_Salida.P_OUT_RESULTADOPROCESO) {	
						//Obtiene y agrega a la salida el resultado del proceso de liquidar comprobante	
						ResultadoProceso_Salida resultadoProcesoBCP = new ResultadoProceso_Salida();
						Utils.copyPojo(resultadoProcesoLC, resultadoProcesoBCP);
						
						if (resultadoProcesoLC.Resultado.equalsIgnoreCase("LIQ")) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(resultadoProcesoLC.Id,MensajeSalida.MENSAJE_INFORMACION,codigoMensajeExito,mensajeExito));
							
						}
					}
					
				}
				
				
				
				
				for(IngresoEgresoGenerado_Salida  gen : salida.P_OUT_INGRESOEGRESOGENERADO){
					Long id 	= 	mapIdSolicitud.get(gen.NumeroReferencia);
					
					
					for(RegistroRecaudacion solicitud : solicitudProcesar ) {
						
						if(solicitud.IdMandatoPAC.equals(id)) {
							InMovimientoGenerado movimientoGenerado 	= 	new InMovimientoGenerado();
							movimientoGenerado.IdSecuenciaMovimiento	= 	gen.IdIngresoEgreso;
							movimientoGenerado.IdEmpresa				=	solicitud.IdEmpresa;
							movimientoGenerado.IdUsuario				=	solicitud.IdUsuario;
							movimientoGenerado.IdModulo					=	_c.ModuloTesoreria;
							movimientoGenerado.FechaIngreso				=	solicitud.FechaIngreso;
							movimientoGenerado.IdSolicitud				=	solicitud.IdentServicio;
							movimientoGenerado.FolioAPV					=	solicitud.FolioAPV;
							movimientoGenerado.FolioCSC					=	solicitud.FolioCSC;
							
							solicitud.MovimientoGenerado.add(movimientoGenerado);  
							String codigoMensajeExito 					= 	"";
							
							if (gen.IdTipoMovimiento.equalsIgnoreCase(_c.IngresoTesoreria)) {
								codigoMensajeExito = "UI_CREADO_MOVIMIENTO_INGRESO";
							} else if(gen.IdTipoMovimiento.equalsIgnoreCase(_c.EgresoTesoreria)) {
								codigoMensajeExito = "UI_CREADO_MOVIMIENTO_EGRESO";
							}
								
							String mensajeExito 						= 	Diccionario.getMensaje(con, solicitud.IdEmpresa, codigoMensajeExito).add("IdIngresoEgreso", gen.IdIngresoEgreso).msg();	
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdMandatoPAC,MensajeSalida.MENSAJE_INFORMACION,codigoMensajeExito,mensajeExito));
						}
						
					}
					
				}
			}
	
		}
		
		private List<RegistroRecaudacion> getSolicitudesValidas(List<RegistroRecaudacion> todasLasSolicitudes){
			
			List<RegistroRecaudacion> soloParaTesoreria 	= new ArrayList<RegistroRecaudacion>();
			
			for (RegistroRecaudacion registros : todasLasSolicitudes) {
			
				RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
				
				if(registros.EstadoPAC.equals("ING") && registros.EstadoCargo.equals("ACT") && (!"REV".equalsIgnoreCase(registros.EstadoSAAPV) || "RVC".equalsIgnoreCase(registros.EstadoCSC))) {
					
					RegistroRecaudacion reg 	= 	new RegistroRecaudacion();
					reg.FechaDeposito	 		= 	registros.FechaDeposito;
					reg.IdBanco					= 	registros.IdBanco;
					reg.IdMedioDePago			= 	registros.IdMedioDePago;
					reg.IdTraspaso				= 	registros.IdTraspaso;
					reg.MontoDeposito			= 	registros.MontoDeposito;
					reg.SumaTotal				= 	registros.SumaTotal;
					reg.NumeroCuentaBancaria	= 	registros.NumeroCuentaBancaria;
					reg.IdCuentaBanco			= 	registros.IdCuentaBanco;
					reg.Observacion				= 	registros.Observacion;
					reg.Sucursal				= 	registros.Sucursal;
					reg.EstadoProceso			= 	registros.EstadoProceso;
					reg.IdEmpresa				= 	registros.IdEmpresa;
					reg.IdUsuario				= 	registros.IdUsuario;
					reg.IdEstado				= 	registros.IdEstado;
					reg.FechaIngreso			= 	registros.FechaIngreso;
					reg.FechaAceptacion 		= 	registros.FechaAceptacion;
					reg.IdCliente       		= 	registros.IdCliente;
					reg.IdPortafolio    		= 	registros.IdPortafolio; 
					reg.IdPersonaEmisor 		= 	registros.IdPersonaEmisor;
					reg.IdMandatoPAC			=	registros.IdMandatoPAC;
					reg.EstadoPAC				= 	registros.EstadoPAC;
					reg.NombreCompleto			= 	registros.NombreCompleto;
					reg.DocumentoIdentidad		= 	registros.DocumentoIdentidad;
					reg.EstadoCliente			= 	registros.EstadoCliente;
					reg.IdentServicio			= 	registros.IdentServicio;
					reg.EstadoPortafolio 		= 	registros.EstadoPortafolio;
					reg.EstadoCargo				= 	registros.EstadoCargo;
					reg.DescripcionEstadoCargo  =	registros.DescripcionEstadoCargo; //EnPAC EstadoCargoBanco
					reg.FolioAPV				= 	registros.FolioAPV;
					reg.FolioCSC				= 	registros.FolioCSC;
					reg.EstadoCargoBanco		= 	registros.EstadoCargoBanco;
					reg.IdConcepto				=	registros.IdConcepto;
					reg.DescripccionConcepto	= 	registros.DescripccionConcepto;
					reg.EstadoCSC				= 	registros.EstadoCSC;
					reg.EstadoLargoCSC			= 	registros.EstadoLargoCSC;
					reg.EstadoSAAPV				= 	registros.EstadoSAAPV;
					reg.EstadoLargoSAAPV		= 	registros.EstadoLargoSAAPV;
					reg.NombreBanco				=	registros.NombreBanco;
					reg.CuentaBancaria			= 	registros.CuentaBancaria;
					reg.CodigoPortafolio		= 	registros.CodigoPortafolio;
					reg.FechaSuscripcion		= 	registros.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : registros.FechaSuscripcion;;
					reg.DiaDescuento			= 	registros.DiaDescuento;
					reg.IdMandatoPACUI			=	registros.IdMandatoPACUI;
					reg.EstadoRendicion			= 	"Mandato OK - Pendiente de procesar";
															
					soloParaTesoreria.add(reg);
					
				}else if(!registros.EstadoPAC.equals("ING")) {
					regNoProcesado = copiarRecaudacion(registros);
					regNoProcesado.EstadoRendicion 			= "El contrato PAC: +" + registros.IdMandatoPAC + "+ no tiene un estado valido.";
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registros.IdentServicio;
					regNoProcesado.IdMandatoPAC				= registros.IdMandatoPAC;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, registros.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					
					
				}
				else if(!registros.EstadoCargo.equals("ACT")) {
					regNoProcesado = copiarRecaudacion(registros);
					regNoProcesado.EstadoRendicion 			= "Debe cargar el archivo universo, ya que el Estado Cargo del Mandato: " + registros.IdMandatoPAC + " es INA.";
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registros.IdentServicio;
					regNoProcesado.IdMandatoPAC				= registros.IdMandatoPAC;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, registros.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					
				}
				else if ("REV".equalsIgnoreCase(registros.EstadoSAAPV))  {
					regNoProcesado = copiarRecaudacion(registros);
					regNoProcesado.EstadoRendicion 			= "El estado del Contrato SAAPV: Folio Contrato " + registros.FolioAPV + ", se encuentra en estado: " + registros.EstadoLargoSAAPV + ". Para el cliente:" + registros.DocumentoIdentidad;
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registros.IdentServicio;
					regNoProcesado.IdMandatoPAC				= registros.IdMandatoPAC;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, registros.EstadoSAAPV, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
										
				}
				else if ("RVC".equalsIgnoreCase(registros.EstadoCSC)) {
					regNoProcesado = copiarRecaudacion(registros);
					regNoProcesado.EstadoRendicion 			= "El estado del Contrato CSC: Folio CSC " + registros.FolioCSC + ", se encuentra en estado: " + registros.EstadoLargoCSC + ". Para el cliente:" + registros.DocumentoIdentidad;
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registros.IdentServicio;
					regNoProcesado.IdMandatoPAC				= registros.IdMandatoPAC;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, registros.EstadoCargoBanco, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
				}
			}
			
			return soloParaTesoreria;
		}	
		
		private List<RegistroRecaudacion> getSolicitudAProcesarPreRendicion(List<InRecaudacion> traspasos, List<InRecaudacion> traspasosArchivo, String fechaSistema, Long IdEmpresa, Long IdUsuario, BigDecimal valorUF){
			Long contador 									= 	(long) 500;
			
			List<RegistroRecaudacion> solicitudTesoreria 	= 	new ArrayList<RegistroRecaudacion>();
			
			Map<String, List<InRecaudacion>> hashTraspasosAgrupados = traspasos.parallelStream().collect(Collectors.groupingBy(traspaso->traspaso.IdentServicio));
			
			for(Entry<String, List<InRecaudacion>> listaTraspaso : hashTraspasosAgrupados.entrySet()) {
				boolean soloSAAPV 	= false;
				boolean soloCSC 	= false;
				List<InRecaudacion> traspasosIntServicio 		= listaTraspaso.getValue();
				List<InRecaudacion> traspasosIntServicioArchivo = traspasosArchivo.parallelStream().filter(contrato->contrato.IdentServicio.equalsIgnoreCase(listaTraspaso.getKey())).collect(Collectors.toList());
				
				BigDecimal depositoSumado 	= traspasosIntServicioArchivo	.stream().map(registro->registro.MontoDeposito)	.reduce(BigDecimal.ZERO,BigDecimal::add);
				BigDecimal montoSumado 		= traspasosIntServicio			.stream().map(contrato->contrato.MontoCSC.add(contrato.MontoSAPV, MathContext.DECIMAL128)).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal montoSumadoSAAPV = traspasosIntServicio			.stream().map(contrato->contrato.MontoSAPV)		.reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal montoSumadoCSC 	= traspasosIntServicio			.stream().map(contrato->contrato.MontoCSC)		.reduce(BigDecimal.ZERO, BigDecimal::add);
				

				if(depositoSumado.compareTo(montoSumadoCSC) == 0) 
					soloCSC = true;
				if(depositoSumado.compareTo(montoSumadoSAAPV) == 0) {
					soloSAAPV = true;
					soloCSC = false;
				}
				if(depositoSumado.compareTo(montoSumado) != 0 && !soloSAAPV && !soloCSC) {
					Long contadorRegistro = 	(long) 500;
					for(InRecaudacion solicitud : traspasosIntServicio) {
						RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
						regNoProcesado = obtenerRegistroRecaudacion(solicitud, fechaSistema, IdEmpresa, IdUsuario, valorUF,contadorRegistro, montoSumado);
						regNoProcesado.EstadoRendicion 			= "Monto mandato no corresponde a SAAPV ni CSC." + regNoProcesado.DocumentoIdentidad;
						regNoProcesado.EstadoProcesoRendicion	= "N";
						regNoProcesado.IdentServicio			= solicitud.IdentServicio;
						regNoProcesado.IdMandatoPAC				= -1L;
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
						out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
						contador++;
					}
				}
				else {
					List<InRecaudacion> traspasosSAPV = traspasosIntServicio.parallelStream().filter(contrato->contrato.MontoSAPV.compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
					List<InRecaudacion> traspasosCSC = traspasosIntServicio.parallelStream().filter(contrato->contrato.MontoCSC.compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());

					/*Long cantRegistroArchivo = traspasosIntServicioArchivo.parallelStream().count();
					Long cantRegistroSAAPV = traspasosSAPV.parallelStream().count();
					Long cantRegistroCSC = traspasosCSC.parallelStream().count();
					*/
					if(soloSAAPV || depositoSumado.compareTo(montoSumado) == 0)
						for(InRecaudacion in : traspasosSAPV){
								RegistroRecaudacion reg = obtenerRegistroRecaudacion(in,fechaSistema,IdEmpresa,IdUsuario,valorUF,contador, depositoSumado);
								solicitudTesoreria.add(reg);
								contador++;
					}
					if(soloCSC || depositoSumado.compareTo(montoSumado) == 0) {
						for(InRecaudacion in : traspasosCSC){
								RegistroRecaudacion reg = obtenerRegistroRecaudacion(in,fechaSistema,IdEmpresa,IdUsuario,valorUF,contador, depositoSumado);
								solicitudTesoreria.add(reg);
								contador++;
						}
					}
				}
			}
			
			return solicitudTesoreria;
		}
		
		private RegistroRecaudacion obtenerRegistroRecaudacion(InRecaudacion in, String fechaSistema, Long IdEmpresa, Long IdUsuario, BigDecimal valorUF, Long contador, BigDecimal MontoSumado) {
			RegistroRecaudacion reg 	= 	new RegistroRecaudacion();
			
			reg.FechaDeposito	 		= 	Fecha.toDate(fechaSistema);
			reg.IdBanco					= 	(long) 1;
			reg.IdMedioDePago			= 	"EFE";
			reg.IdTraspaso				= 	contador;
			if(in.MontoCSC.compareTo(BigDecimal.ZERO) == 0 ) {
				reg.MontoDeposito			= 	in.MonedaSAPV.equalsIgnoreCase("UF") ? in.MontoSAPV.multiply(valorUF) : in.MontoSAPV;
				reg.Moneda                  =   in.MonedaSAPV;
			}else {
				reg.MontoDeposito			= 	in.MonedaCSC.equalsIgnoreCase("UF") ? in.MontoCSC.multiply(valorUF) : in.MontoCSC;
				reg.Moneda                  =   in.MonedaCSC;
			}
			
			reg.SumaTotal				= 	reg.Moneda.equalsIgnoreCase("UF") ? MontoSumado.multiply(valorUF) : MontoSumado;
			reg.NumeroCuentaBancaria	= 	(long) 0;
			reg.IdCuentaBanco			= 	(long) 0;
			reg.Observacion				= 	"";
			reg.Sucursal				= 	"";
			reg.EstadoProceso			= 	_c.EstadoProcesado;
			reg.IdEmpresa				= 	IdEmpresa;
			reg.IdUsuario				= 	IdUsuario;
			reg.IdEstado				= 	_c.EstadoActivo;
			reg.FechaIngreso			= 	Fecha.toDate(fechaSistema);
			reg.FechaAceptacion 		= 	Fecha.toDate(fechaSistema);
			reg.IdCliente       		= 	in.IdCliente;
			reg.IdPortafolio    		= 	in.IdPortafolio; 
			reg.IdPersonaEmisor 		= 	(long) 1;
			
			
			reg.IdMandatoPAC			=	in.IdMandatoPAC;
			reg.EstadoPAC				= 	in.EstadoPAC;
			reg.NombreCompleto			= 	in.NombreCompleto;
			reg.DocumentoIdentidad		= 	in.DocumentoIdentidad;
			reg.EstadoCliente			= 	in.EstadoCliente;
			reg.IdentServicio			= 	in.IdentServicio;
			reg.EstadoPortafolio 		= 	in.EstadoPortafolio;
			reg.EstadoCargo				= 	in.EstadoCargo;
			reg.DescripcionEstadoCargo  =	in.DescripcionEstadoCargo;
			reg.EstadoCargoBanco		=   in.EstadoCargoBanco;
			reg.EstadoProcesoRendicion 	= 	"OK";
			reg.EstadoCSC				=	in.EstadoCSC;
			reg.EstadoLargoCSC			=	in.EstadoLargoCSC;
			reg.EstadoSAAPV				=	in.EstadoSAAPV;
			reg.EstadoLargoSAAPV		=	in.EstadoLargoSAAPV;			
			reg.NombreBanco				=	in.NombreBanco;
			reg.CuentaBancaria			= 	in.CuentaBancaria;
			reg.CodigoPortafolio		= 	in.CodigoPortafolio;
			reg.FechaSuscripcion		= 	in.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : in.FechaSuscripcion;
			reg.DiaDescuento			= 	in.DiaDescuento;
			reg.IdMandatoPACUI			=	in.IdMandatoPACUI;
			
			if(!in.FolioCSC.equalsIgnoreCase("0")) {
				reg.IdConcepto = _c.ABNCSC;
				reg.FolioCSC = in.FolioCSC;
				foliosCSC.add(in.FolioCSC.toString());
				
			}else if(!in.FolioSAAPV.equalsIgnoreCase("0")) {
				reg.IdConcepto = _c.ABNAPV;
				reg.FolioAPV = in.FolioSAAPV;
				foliosAPV.add(in.FolioSAAPV.toString());
			}
			
			return reg;
		}
		
		private RegistroRecaudacion copiarRecaudacion(RegistroRecaudacion recaudacion) {
			
			RegistroRecaudacion registroRecaudacion = new RegistroRecaudacion();
			
			registroRecaudacion.IdTraspaso 				= 	recaudacion.IdTraspaso;
			registroRecaudacion.FechaDeposito 			= 	recaudacion.FechaDeposito;
			registroRecaudacion.IdMedioDePago 			= 	recaudacion.IdMedioDePago;
			registroRecaudacion.IdBanco 				= 	recaudacion.IdBanco;
			registroRecaudacion.NumeroCuentaBancaria 	= 	recaudacion.NumeroCuentaBancaria;
			registroRecaudacion.IdCuentaBanco 			= 	recaudacion.IdCuentaBanco;
			registroRecaudacion.Sucursal 				= 	recaudacion.Sucursal;
			registroRecaudacion.MontoDeposito 			= 	recaudacion.MontoDeposito;
			registroRecaudacion.SumaTotal 				= 	recaudacion.SumaTotal;
			registroRecaudacion.Observacion 			= 	recaudacion.Observacion;

			registroRecaudacion.IdSerieFondo 			= 	recaudacion.IdSerieFondo;
			registroRecaudacion.IdCliente 				= 	recaudacion.IdCliente;
			registroRecaudacion.IdPortafolio 			= 	recaudacion.IdPortafolio;
			registroRecaudacion.EstadoProceso 			= 	recaudacion.EstadoProceso;
			registroRecaudacion.Codigo 					= 	recaudacion.Codigo;		
			registroRecaudacion.IdRecaudacion 			= 	recaudacion.IdRecaudacion;
			registroRecaudacion.IdEmpresa				= 	recaudacion.IdEmpresa;
			registroRecaudacion.IdUsuario				= 	recaudacion.IdUsuario;
			registroRecaudacion.IdEstado 				=	recaudacion.IdEstado;
			registroRecaudacion.FechaIngreso 			=	recaudacion.FechaIngreso;
			registroRecaudacion.FechaAceptacion 		= 	recaudacion.FechaAceptacion;  
			registroRecaudacion.IdPersonaEmisor			= 	recaudacion.IdPersonaEmisor;		
			registroRecaudacion.IdConcepto 				= 	recaudacion.IdConcepto;
			
			registroRecaudacion.DescripccionConcepto 	= 	recaudacion.DescripccionConcepto;
			registroRecaudacion.FolioCSC 				= 	recaudacion.FolioCSC;
			registroRecaudacion.FolioAPV 				= 	recaudacion.FolioAPV;
			
			registroRecaudacion.IdMandatoPAC			= 	recaudacion.IdMandatoPAC;
			registroRecaudacion.EstadoPAC 				=	recaudacion.EstadoPAC;
			registroRecaudacion.NombreCompleto 			= 	recaudacion.NombreCompleto;
			registroRecaudacion.DocumentoIdentidad 		=	recaudacion.DocumentoIdentidad;
			registroRecaudacion.EstadoCliente 			= 	recaudacion.EstadoCliente;
			registroRecaudacion.IdentServicio 			= 	recaudacion.IdentServicio;
			registroRecaudacion.EstadoPortafolio 		= 	recaudacion.EstadoPortafolio;
			registroRecaudacion.DescripcionSolicitud 	= 	recaudacion.DescripcionSolicitud;
			registroRecaudacion.EstadoCargo 			= 	recaudacion.EstadoCargo;
			registroRecaudacion.DescripcionEstadoCargo 	= 	recaudacion.DescripcionEstadoCargo;
			registroRecaudacion.EstadoRendicion 		= 	recaudacion.EstadoRendicion;
			registroRecaudacion.EstadoCargoBanco 		= 	recaudacion.EstadoCargoBanco;
			registroRecaudacion.EstadoProcesoRendicion  =	recaudacion.EstadoProcesoRendicion;
			registroRecaudacion.NombreBanco				=	recaudacion.NombreBanco;
			registroRecaudacion.CuentaBancaria			= 	recaudacion.CuentaBancaria;
			registroRecaudacion.CodigoPortafolio		= 	recaudacion.CodigoPortafolio;
			registroRecaudacion.FechaSuscripcion		= 	recaudacion.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : recaudacion.FechaSuscripcion;
			registroRecaudacion.DiaDescuento			= 	recaudacion.DiaDescuento;
			registroRecaudacion.IdMandatoPACUI			=	recaudacion.IdMandatoPACUI;
			return registroRecaudacion;
		}
		
		
		private Orden_Entrada getCalculoOrdenes(DistribucionInversion di, String fechaSistema, DatosInstrumentosPrecioBolsa_Salida precioBolsaSalida, long iterator) {
			
			CalcularMontoYCobrosOrden.Orden_Entrada CalculoMontoOrdenes = new CalcularMontoYCobrosOrden.Orden_Entrada();
			
			CalculoMontoOrdenes.PrecioOrden 				= 	precioBolsaSalida.PrecioCierre;
			
			
			CalculoMontoOrdenes.IdInterno 					=	iterator;
			CalculoMontoOrdenes.CantidadOrden				=	BigDecimal.ZERO;
			CalculoMontoOrdenes.EsFungible					=	"N";
			CalculoMontoOrdenes.IdBolsa						=	"BCS";
			CalculoMontoOrdenes.IdCorredorCorresponsal		=	null;
			CalculoMontoOrdenes.IdInstrumento				= 	di.IdInstrumento;
			CalculoMontoOrdenes.IdInstrumentoSubyacente		=	0L;
			CalculoMontoOrdenes.IdMonedaLiquidacionOrden	=	precioBolsaSalida.IdMoneda;
			CalculoMontoOrdenes.IdPortafolio				=	di.IdPortafolio;
			CalculoMontoOrdenes.IdTipoProducto				=	"INAC";
			CalculoMontoOrdenes.IdTipoOperacion				=  	"VENACN";
			CalculoMontoOrdenes.IdTipoCanalIngreso			=	"AGE";
			CalculoMontoOrdenes.FechaOperacion				=	fechaSistema;
			CalculoMontoOrdenes.MontoOrden					=	BigDecimal.ZERO;
			CalculoMontoOrdenes.TasaSimultanea				=	BigDecimal.ZERO;
			CalculoMontoOrdenes.TasaRFIF					=	BigDecimal.ZERO;
			CalculoMontoOrdenes.TasaVentaCorta				=	BigDecimal.ZERO;
			CalculoMontoOrdenes.TipoCambio					=	BigDecimal.ZERO;
			CalculoMontoOrdenes.ValorRescateOrden			=	BigDecimal.ZERO;
			CalculoMontoOrdenes.PlazoSimultanea				=	0L;
			CalculoMontoOrdenes.PlazoVentaCorta				=	0L;
			CalculoMontoOrdenes.PlazoIF						=	0L;
			CalculoMontoOrdenes.EsAPlazo					=	"N";
			
			// COMPRA
			CalculoMontoOrdenes.MontoTotal					=	di.MontoPesos;
			CalculoMontoOrdenes.EsPorMonto					=	"S";
			CalculoMontoOrdenes.CantidadOrden 				= 	BigDecimal.ZERO;
			
			
			CalculoMontoOrdenes.TasaReporte					=	BigDecimal.ZERO;
			CalculoMontoOrdenes.TasaReportado				=	BigDecimal.ZERO;
			CalculoMontoOrdenes.PlazoReporte				=	0L;
			CalculoMontoOrdenes.BaseTasa					=	0L;
			
			return CalculoMontoOrdenes;
		}
		
		public SalidaRecaudacionTraspaso ValidaPreRendicion(Connection con, EntradaRecaudacionTraspaso entrada) throws Exception{
			
			/*****************************************
			 * INICIO VARIABLES 
			 *****************************************/
			
			SQL sql											=	new SQL();			
			String fechaSistema 							= 	ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdEmpresa 									= 	entrada.p_IdEmpresa;
			Long IdUsuario 									= 	entrada.p_IdUsuario;
			RevisarRequisitosBasicos basicos 				= 	new RevisarRequisitosBasicos();
			HashMap<String,Object> ParamsDatosCorredor 		= 	new HashMap<String,Object>();
			List<RegistroRecaudacion> solicitudesProcesadas =	new ArrayList<RegistroRecaudacion>();
			BigDecimal valorUF;
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			CalculoEntrada calculoEntradaUF = new CalculoEntrada();
			calculoEntradaUF.IdTrabajo = 2L;
			calculoEntradaUF.IdInterno = 1L;
			calculoEntradaUF.Fecha = fechaSistema;
			calculoEntradaUF.IdMonedaOrigen = "UF";
			calculoEntradaUF.IdMonedaDestino = "CLP";
			calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaUF.MontoOrigen =  BigDecimal.ONE;
			calculoEntradaUF.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			valorUF = calculoSalida.p_out_CalculosSalida.get(0).FactorOrigenDestino;
			
			ParamsDatosCorredor.put("IdEmpresa", entrada.p_IdEmpresa);
			
			RevisarRequisitosBasicosSalida salidaBasicos 	= 	basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			
			
			/*****************************************
			 * CREACION DEL OBJETO DE TRASPASO Y VALIDACION DE REGISTROS DE ARCHIVO
			 *****************************************/
			HashMap<String,Object> params2 					= 	new HashMap<String,Object>();
			
			List<InRecaudacion> registrosArchivo			= 	sql.query(con, SQL_SELECT_MANDATOS_Y_ARCHIVO_PRE_REND,params2, InRecaudacion.class);
			List<InRecaudacion> registrosDetalleArchivo		= 	sql.query(con, SQL_SELECT_MANDATOS_PAC_PRE_REND_DETALLE_ARCHIVO,params2, InRecaudacion.class);
			List<InRecaudacion> cruceArchivoMandatos		=	ValidacionSolicitudesArchivo(registrosArchivo, registrosDetalleArchivo, fechaSistema, IdEmpresa, IdUsuario, valorUF);
			
			List<InRecaudacion> traspasos 					= 	sql.query(con, SQL_SELECT_MANDATOS_PAC_PRE_REND,params2, InRecaudacion.class);

			if(!cruceArchivoMandatos.isEmpty()) {
				if(!traspasos.isEmpty()) {
					ValidaArchivoContraMandatos(cruceArchivoMandatos,traspasos, fechaSistema, IdEmpresa, IdUsuario, valorUF);
				}
			}
			if (traspasos.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE","EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE"));
				out.P_OUT_ERRORESCONTADOR += 1L;
				return out;
			}
			
			/*****************************************
			 * CREACION DEL OBJETO DE LA SOLICITUD A PROCESAR 
			 *****************************************/
			solicitudesProcesadas							=	getSolicitudAProcesarPreRendicion(traspasos, cruceArchivoMandatos, fechaSistema, IdEmpresa, IdUsuario, valorUF);
			
			List<RegistroRecaudacion> solicitudProcesar  	= 	getSolicitudesValidas(solicitudesProcesadas);
			
			solicitudProcesar.addAll(out.P_OUT_SOLICITUDES_NO_PROCESADAS);
			//LOG
			List<InRecaudacion> param =  new ArrayList<InRecaudacion>();
			sql.batch(con, SQL_DELETE_LOG_MANDATOS_PAC_PRE_REND, param, InRecaudacion.class);
			List<DiaDescuento>  DiaDescuentoList = sql.query(con, SQL_SELECT_DIA_DESCUENTO_MANDATOS,params2, DiaDescuento.class);
			String DiaDescuento = "5";
			if(!DiaDescuentoList.isEmpty()) {
				DiaDescuento = DiaDescuentoList.get(0).DiaDescuento.toString();
			}
			
			params2.put("EstadoProcesamiento", "PEN");
			params2.put("fechaSistema", fechaSistema);
			params2.put("DiaDescuento", DiaDescuento);
			List<InRecaudacion> mandatosLogExistentes		= 	sql.query(con, SQL_SELECT_LOG_MANDATOS_PAC_PRE_REND,params2, InRecaudacion.class);
			params2.clear();
			params2.put("EstadoProcesamiento", "PRO");
			params2.put("fechaSistema", fechaSistema);
			params2.put("DiaDescuento", DiaDescuento);
			List<InRecaudacion> mandatosLogExistentesProc	= 	sql.query(con, SQL_SELECT_LOG_MANDATOS_PAC_PRE_REND,params2, InRecaudacion.class);
			
			Map<Object, List<InRecaudacion>> hashSolicitudesConLog = mandatosLogExistentes.parallelStream().collect(Collectors.groupingBy(tra -> tra.IdentServicio));
			Map<Object, List<InRecaudacion>> hashSolicitudesConLogProc = mandatosLogExistentesProc.parallelStream().collect(Collectors.groupingBy(tra -> tra.IdentServicio));
			
			solicitudProcesar 								= 	SetEstadosProcesados(solicitudProcesar,hashSolicitudesConLogProc);
			
			List<RegistroRecaudacion> logUpdate		= new ArrayList<RegistroRecaudacion>();
			List<RegistroRecaudacion> logInsert		= new ArrayList<RegistroRecaudacion>();
			List<RegistroRecaudacion> logInsertErrores		= new ArrayList<RegistroRecaudacion>();
			
			logUpdate 		= solicitudProcesar.parallelStream().filter(aP -> hashSolicitudesConLog.containsKey(aP.IdentServicio)).filter(registro->registro.IdMandatoPAC != -1).collect(Collectors.toList());
			logInsert 		= solicitudProcesar.parallelStream().filter(aP -> !hashSolicitudesConLog.containsKey(aP.IdentServicio)).filter(aPro -> !hashSolicitudesConLogProc.containsKey(aPro.IdentServicio)).filter(registro->registro.IdMandatoPAC != -1).collect(Collectors.toList());			
			logInsertErrores = solicitudProcesar.parallelStream().filter(registro->registro.IdMandatoPAC == -1).collect(Collectors.toList());
			/*****************************************************
			 ** 		INSERTAR LOG MANDATOS       			**
			 *****************************************************/
			if(!logInsert.isEmpty()) {
				InsertarMandatos(con, sql, logInsert, Fecha.toDate(fechaSistema), IdUsuario);
			}
			if(!logInsertErrores.isEmpty()) {
				InsertarMandatos(con, sql, logInsertErrores, Fecha.toDate(fechaSistema), IdUsuario);
			}
			
			/*****************************************************
			 ** 		UPDATE LOG MANDATOS						**
			 *****************************************************/
			if (!logUpdate.isEmpty()) {
				UpdateMandatos(con, sql, logUpdate, Fecha.toDate(fechaSistema), IdUsuario);
			}
			
			//LOG
			for(int i = solicitudProcesar.size()-1; i >= 0; i--) {
				if(!("N".equalsIgnoreCase(solicitudProcesar.get(i).EstadoProcesoRendicion))) {	
					out.P_OUT_SOLICITUDES_PROCESADAS.add(solicitudProcesar.get(i));
				}
					
			}
			
			return out;
		}
		
		private void InsertarMandatos(Connection con, SQL sql, List<RegistroRecaudacion> solAGuardarOK, Date fechaSistema, Long IdUsuario) throws SQLException {
			
			List<LogValidarMandatosPAC> LogMandatos = new ArrayList<LogValidarMandatosPAC>();
			for (RegistroRecaudacion reg : solAGuardarOK) {
				
				LogValidarMandatosPAC np 	= new LogValidarMandatosPAC();
				
				if(reg.IdMandatoPAC == null)
					np.IdMandatoPAC = (long) -1;
				else
					np.IdMandatoPAC = reg.IdMandatoPAC;
				np.IdentServicio		= reg.IdentServicio;
				np.FechaSistema			= fechaSistema;
				np.Usuario				= IdUsuario.toString();
				if(reg.EstadoProcesoRendicion.equalsIgnoreCase("N"))
					np.PreRendicion			= "N";
				else 
					np.PreRendicion			= "S";
				np.EstadoProcesamiento 	= "PEN";
				np.Glosa				= reg.EstadoRendicion;
				if(np.IdMandatoPAC != null)
					LogMandatos.add(np);
			}

			if (!LogMandatos.isEmpty()) {
				sql.batch(con, SQL_INSERT_LOG_MANDATOS_PAC_PRE_REND, LogMandatos, LogValidarMandatosPAC.class);  //Cambiar por Update
			}
			
		}
		

		private void UpdateMandatos(Connection con, SQL sql, List<RegistroRecaudacion> solAGuardarOK, Date fechaSistema, Long IdUsuario) throws SQLException {
			
			List<LogValidarMandatosPAC> LogMandatos = new ArrayList<LogValidarMandatosPAC>();
			for (RegistroRecaudacion reg : solAGuardarOK) {
				
				LogValidarMandatosPAC np 	= new LogValidarMandatosPAC();
				
				np.IdentServicio		= reg.IdentServicio;
				np.IdMandatoPAC			= reg.IdMandatoPAC;
				np.FechaSistema			= fechaSistema;
				np.Usuario				= IdUsuario.toString();
				if(reg.EstadoProcesoRendicion.equalsIgnoreCase("N")) 
					np.PreRendicion		= "N";
				
				else  	
					np.PreRendicion		= "S";
				
				np.EstadoProcesamiento 	= "PEN";
				np.Glosa				= reg.EstadoRendicion;
				
				LogMandatos.add(np);
			}

			if (!LogMandatos.isEmpty()) {
				sql.batch(con, SQL_UPDATE_LOG_MANDATOS_PAC_PRE_REND, LogMandatos, LogValidarMandatosPAC.class);  
			}
			
		}
		
		private List<RegistroRecaudacion> SetEstadosProcesados(List<RegistroRecaudacion> todasLasSolicitudes,Map<Object, List<InRecaudacion>> solicitudesProcesadasOk){
			
			List<RegistroRecaudacion> solicitudesCorregidas 	= new ArrayList<RegistroRecaudacion>();
			for (RegistroRecaudacion reg : todasLasSolicitudes) {
				RegistroRecaudacion regCorregido = new RegistroRecaudacion();
				regCorregido = reg;
				if (solicitudesProcesadasOk.containsKey(reg.IdentServicio) && reg.IdMandatoPAC !=-1) {
					regCorregido.EstadoRendicion = "Mandato se encuentra procesado";
					regCorregido.EstadoProcesoRendicion = "S";
				}

				solicitudesCorregidas.add(regCorregido);
			}
			return solicitudesCorregidas;
			
		}
		
		private List<InRecaudacion> ValidacionSolicitudesArchivo(List<InRecaudacion> solicitudesArchivo, String fechaSistema, Long IdEmpresa, Long IdUsuario, BigDecimal valorUF){
			
			List<InRecaudacion> solicitudesSinError 	= new ArrayList<InRecaudacion>();
			
			Long contador = 	(long) 500;
			for (InRecaudacion registro : solicitudesArchivo) {
				if (registro.Observacion.equalsIgnoreCase("N")) {
					RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
					regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
					regNoProcesado.EstadoRendicion 			= "Mandato informado no existe.";
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registro.IdentServicio;
					regNoProcesado.IdMandatoPAC				= -1L;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					contador++;
				}
				else if(!registro.EstadoCargoBanco.equalsIgnoreCase("3")) {
					RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
					regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
					regNoProcesado.EstadoRendicion 			= "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado";
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registro.IdentServicio;
					regNoProcesado.IdMandatoPAC				= -1L;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					contador++;
				}
				else if(!registro.EstadoCargo.equalsIgnoreCase("ACT")) {
					RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
					regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
					regNoProcesado.EstadoRendicion 			= "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado o estado PAC distinto de ingresado. Para el cliente: " + registro.DocumentoIdentidad;
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registro.IdentServicio;
					regNoProcesado.IdMandatoPAC				= -1L;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					contador++;
				}
				else {
					solicitudesSinError.add(registro);
				}
				
			}
			
			return solicitudesSinError;
		}
		
		private List<InRecaudacion> ValidacionSolicitudesArchivo(List<InRecaudacion> solicitudesArchivo, List<InRecaudacion> solicitudesArchivoDetalle, String fechaSistema, Long IdEmpresa, Long IdUsuario, BigDecimal valorUF){
			
			List<InRecaudacion> solicitudesSinError 	= new ArrayList<InRecaudacion>();
			
			Map<String, List<InRecaudacion>> hashTraspasosAgrupados = solicitudesArchivoDetalle.parallelStream().collect(Collectors.groupingBy(traspaso->traspaso.IdentServicio));
			
			Long contador = 	(long) 500;
			for (InRecaudacion registro : solicitudesArchivo) {
				if (registro.Observacion.equalsIgnoreCase("N")) {
					RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
					regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
					regNoProcesado.EstadoRendicion 			= "Mandato informado no existe.";
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= registro.IdentServicio;
					regNoProcesado.IdMandatoPAC				= -1L;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					contador++;
				}
				else if(!registro.EstadoCargoBanco.equalsIgnoreCase("3")) {
					
					List<InRecaudacion> traspasosArchivo = hashTraspasosAgrupados.get(registro.IdentServicio);
					
					if(traspasosArchivo != null && traspasosArchivo.size() > 0) {
						for(InRecaudacion traspasoArchivo : traspasosArchivo) {
							RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
							regNoProcesado = obtenerRegistroRecaudacion(traspasoArchivo, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
							regNoProcesado.EstadoRendicion 			= "IdentServicio: " + traspasoArchivo.IdentServicio + " con codigo de estado cargo rechazado";
							regNoProcesado.EstadoProcesoRendicion	= "N";
							regNoProcesado.IdentServicio			= traspasoArchivo.IdentServicio;
							regNoProcesado.IdMandatoPAC				= -1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
							out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
						}
					}else {
						RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
						regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
						regNoProcesado.EstadoRendicion 			= "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado";
						regNoProcesado.EstadoProcesoRendicion	= "N";
						regNoProcesado.IdentServicio			= registro.IdentServicio;
						regNoProcesado.IdMandatoPAC				= -1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
						out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					}
					
					out.P_OUT_ERRORESCONTADOR++;
					contador++;
				}
				else if(!registro.EstadoCargo.equalsIgnoreCase("ACT")) {
					List<InRecaudacion> traspasosArchivo = hashTraspasosAgrupados.get(registro.IdentServicio);
					if(traspasosArchivo != null && traspasosArchivo.size() > 0) {
						for(InRecaudacion traspasoArchivo : traspasosArchivo) {
							RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
							regNoProcesado = obtenerRegistroRecaudacion(traspasoArchivo, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
							regNoProcesado.EstadoRendicion 			= "IdentServicio: " + traspasoArchivo.IdentServicio + " con codigo de estado cargo rechazado o estado PAC distinto de ingresado. Para el cliente: " + traspasoArchivo.DocumentoIdentidad;
							regNoProcesado.EstadoProcesoRendicion	= "N";
							regNoProcesado.IdentServicio			= traspasoArchivo.IdentServicio;
							regNoProcesado.IdMandatoPAC				= -1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
							out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
						}
					}else {
						RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
						regNoProcesado = obtenerRegistroRecaudacion(registro, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador, BigDecimal.ZERO);
						regNoProcesado.EstadoRendicion 			= "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado o estado PAC distinto de ingresado. Para el cliente: " + registro.DocumentoIdentidad;
						regNoProcesado.EstadoProcesoRendicion	= "N";
						regNoProcesado.IdentServicio			= registro.IdentServicio;
						regNoProcesado.IdMandatoPAC				= -1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
						out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					}
					
					out.P_OUT_ERRORESCONTADOR++;
					
					contador++;
				}
				else {
					solicitudesSinError.add(registro);
				}
				
			}
			
			return solicitudesSinError;
		}
		
		private void ValidaArchivoContraMandatos(List<InRecaudacion> solicitudesArchivo,List<InRecaudacion> mandatos, String fechaSistema, Long IdEmpresa, Long IdUsuario, BigDecimal valorUF){
			
			Map<String,List<InRecaudacion>> hashMandatos = mandatos.parallelStream().collect(Collectors.groupingBy(traspaso->traspaso.IdentServicio));
			List<InRecaudacion> solicitudesDescartadas		= new ArrayList<InRecaudacion>();
			solicitudesDescartadas = solicitudesArchivo.parallelStream().filter(aP -> !hashMandatos.containsKey(aP.IdentServicio)).collect(Collectors.toList());
			if(!solicitudesDescartadas.isEmpty()) {
				Long contador = 	(long) 500;
				for(InRecaudacion solicitud : solicitudesDescartadas) {
					RegistroRecaudacion regNoProcesado 	= 	new RegistroRecaudacion();
					//regNoProcesado = obtenerRegistroRecaudacion(solicitud, fechaSistema, IdEmpresa, IdUsuario, valorUF,contador);
					regNoProcesado.EstadoRendicion 			= "Mandato o Contrato se encuentra mal configurado, se encuentra anulado o no se encuentra activo. Para el cliente: " + regNoProcesado.DocumentoIdentidad;
					regNoProcesado.EstadoProcesoRendicion	= "N";
					regNoProcesado.IdentServicio			= solicitud.IdentServicio;
					regNoProcesado.IdMandatoPAC				= (solicitud.IdMandatoPAC != null) ? solicitud.IdMandatoPAC : -1L;

					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(regNoProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, regNoProcesado.EstadoProcesoRendicion, regNoProcesado.EstadoRendicion));
					out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(regNoProcesado);
					contador++;
				}
				
			}
		}
		
		private void agregarLogsPreRendicion(Connection con, SQL sql, Long idEmpresa) throws Exception{
			try {
				MensajeSalida msg             							= new MensajeSalida();
				List<LogValidarMandatosPAC> solicitudesDescartadas		= new ArrayList<LogValidarMandatosPAC>();
				HashMap<String,String> valoresDiccionario 				= new HashMap<String,String>();

			
				HashMap<String,Object> params 		= 	new HashMap<String,Object>();
				solicitudesDescartadas = sql.query(con, SQL_SELECT_MANDATOS_RZO_PRE_REND,params, LogValidarMandatosPAC.class);
			
				for (LogValidarMandatosPAC MandatoRechazado : solicitudesDescartadas) {
					msg					= new MensajeSalida();
					valoresDiccionario 	= new HashMap<>();
					valoresDiccionario.put("Glosa", MandatoRechazado.Glosa);
					
					msg.id = MandatoRechazado.IdMandatoPAC;
					msg.tipo = MensajeSalida.MENSAJE_ERROR;
					msg.codigo = "MANDATO_RECHAZADO_PRERENDICION";
					msg.descripcion = Diccionario.getMensaje(con, idEmpresa, "UI_MANDATO_RECHAZADO_PRERENDICION", valoresDiccionario);
					
					out.P_OUT_MENSAJES.add(msg);
				}		
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		/********************
		 * Metodo Principal *
		 ********************/

		public static void main(String[] args) throws Exception {

			try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
				try {
					Gson gson = Utils.getGsonBuilder();	
					
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2024-10-21\",\"p_CargaMasiva\":[{\"IdInterno\":1,\"NombreArchivo\":\"Rendicion Conv.023_28.10.2024 - copia.txt\",\"CodigoTipoCargaMasiva\":\"CARGA_RENDICION_PAC\"}],\"p_CargaMasivaDetalle\":[{\"CodigoBanco\":\"051\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"00168000289700515     \",\"Filler1\":\"\",\"MontoCargo\":\"15000\",\"FechaVencimiento\":\"20241025\",\"FechaCargo\":\"20241025\",\"EstadoCargo\":\"3\",\"CodigoRechazo\":\"00\",\"InfAdicional\":\"0         \",\"Filler\":\"\"},{\"CodigoBanco\":\"051\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"00168000289700515     \",\"Filler1\":\"\",\"MontoCargo\":\"15000\",\"FechaVencimiento\":\"20241025\",\"FechaCargo\":\"20241025\",\"EstadoCargo\":\"3\",\"CodigoRechazo\":\"00\",\"InfAdicional\":\"0         \",\"Filler\":\"\"},{\"CodigoBanco\":\"051\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"00168000289700515     \",\"Filler1\":\"\",\"MontoCargo\":\"15000\",\"FechaVencimiento\":\"20241025\",\"FechaCargo\":\"20241025\",\"EstadoCargo\":\"3\",\"CodigoRechazo\":\"00\",\"InfAdicional\":\"0         \",\"Filler\":\"\"},{\"CodigoBanco\":\"051\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"00168000289700515     \",\"Filler1\":\"\",\"MontoCargo\":\"15000\",\"FechaVencimiento\":\"20241025\",\"FechaCargo\":\"20241025\",\"EstadoCargo\":\"3\",\"CodigoRechazo\":\"00\",\"InfAdicional\":\"0         \",\"Filler\":\"\"}],\"p_simular\":\"N\"}";
					EntradaRecaudacionTraspaso Objin_json = gson.fromJson(json, EntradaRecaudacionTraspaso.class);
					
					ProcesarDistribucion demo = new ProcesarDistribucion();	
		 			System.out.println("ENTRADA");
		 			System.out.println(Objin_json.toString());
		 			System.out.println("SALIDA");

		 			try { 
		 				SalidaRecaudacionTraspaso ObjOut = demo.logica(con, Objin_json);
		 				//con.commit();
		 				con.rollback();
		 				System.out.println(ObjOut.toString());
		 			} 
		 			catch(Exception er) {
						er.printStackTrace();
						con.rollback();
					}
		 			
			} catch (Exception e) {

				e.printStackTrace();			
			}	
			System.out.println("== END ==");
			};
		}
	}