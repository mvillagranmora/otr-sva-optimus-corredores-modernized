package cl.bcs.normativa.declaraciones.DJ1;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GrabarDetalle implements JavaCall {
	
	static final Logger log = LogManager.getLogger(RectificarRegistro.class);
	
	private final String SQL_UPDATE_DETALLE		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/GrabarDetalle_UpdateDetalle.sql");	
	private final String SQL_INSERT_DETALLE		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/GrabarDetalle_InsertDetalle.sql");
	private final String SQL_SELECT_ACTUAL		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/GrabarDetalle_SelectActual.sql");
	private final String SQL_SELECT_NUEVO		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/GrabarDetalle_SelectNuevo.sql");
	
	private static final List<String> REGISTRO_DIFERENCIAS_NO_COMPARA = Arrays.asList("p_kPeriodoComercial","p_IdEmpresa","p_IdUsuario","p_IdModulo","p_Rectifica","p_KRutCtraparte1","p_KIdOpeEstruc","p_IdDocumento","p_FuenteDatos","p_Seccion","p_Estado","p_kProducto","p_IdBaseDocumento","p_KS4_XR_NroFlu","p_S4_XR_TipoFlujo","p_S4_XR_FacTasa","p_S4_XR_SpreadTasa","p_S4_XR_TasaFija","p_S4_XR_FecFixTasa","p_S4_XR_FecIni","p_S4_XR_FecTer","p_S4_XR_MontoNoc","p_S4_XR_MontoInt","p_S4_X_MonMonto","p_KS5_CorrPago","p_KS5_IdPago","p_S5_FecPago","p_S5_MontoPago","p_S5_TipoPago","p_S6_TipoGar","p_S6_MonVal","p_KS6_IdGar","p_S6_MontoUmb","p_S6_ValGtia","p_S7_TipoGar","p_KS7_IdGar","p_S7_TipoActGar","p_S7_PorcGar");

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class GrabarDetalle_Entrada 
	{		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_UsrUltMod;
		public String p_IdModulo;
		public Long p_ID;
		public String p_Rectifica;
		public String p_KRutCtraparte1;
		public Long p_KIdContrato;
		public String p_KFechaContrato;
		public String p_HoraContrato;
		public String p_KIdOpeEstruc;
		public String p_S1_NaturaInfo;
		public String p_S1_EventoReporte;
		public String p_S1_LEICtraparte1;
		public String p_S1_RutCtraparte2;
		public String p_S1_LEICtraparte2;
		public String p_S1_NomCtraparte2;
		public String p_S1_PaisCtraparte2;
		public String p_S1_RutEntidad;
		public String p_S1_LEIEntidad;
		public String p_S1_RUTLiqCtraparte1;
		public String p_S1_LEILiqCtraparte1;
		public String p_S1_RutAgenteCal;
		public String p_S1_LEIAgenteCal;
		public String p_S1_NomAgenteCal;
		public String p_S1_RutCedente;
		public String p_S1_LEICedente;
		public String p_S1_PlataformaNego;
		public String p_S1_RutIntermediario;
		public String p_S1_LEIIntermediario;
		public String p_kPeriodoComercial;
		public String p_S2_Instrumento;
		public String p_S2_IdContratoAgente;
		public String p_S2_OpsClase;
		public String p_S2_OpPosicionOp;
		public String p_S2_ModalidadLiq;
		public String p_S2_MonedaComp;
		public String p_S2_FechaInicio;
		public String p_S2_FechaFixing1;
		public String p_S2_FechaTermino;
		public String p_S2_FechaPagOpe;
		public String p_S2_FechaInicioMod;
		public String p_S2_MonedaPrima;
		public String p_S2_MontoPrima;
		public String p_S2_ObjetivoOpe;
		public String p_S2_ConvenioMarco;
		public String p_S2_PaisContrato;
		public String p_S2_Garantias;
		public String p_S2_ClausulaRecou;
		public String p_S2_ClausulaTermino;
		public String p_S2_FechaEfecTermino;
		public String p_S2_CompresionCartera;
		public String p_S2_NumTotIntercam;
		public String p_S2_NumTotPagos;
		public String p_S3_XR_OpInstruSub;
		public String p_S3_MonedaComprada;
		public String p_S3_MontoComprada;
		public String p_S3_XR_MonedaVendida;
		public String p_S3_XR_MontoVendida;
		public String p_S3_XR_TipoTasaEnt;
		public String p_S3_XR_TipoTasaRec;
		public String p_S3_R_TasaFija;
		public String p_S3_R_Spread;
		public String p_S3_I_TipoActivoSub;
		public String p_S3_I_IdActivoSuby;
		public String p_S3_I_TasaPactada;
		public String p_S3_XR_FechaFixing2;
		public String p_S3_XR_ConvTipoCambio;
		public String p_S3_PrecioFuturo;
		public String p_S3_XR_PuntosForward;
		public String p_S3_XR_PrecioEntrada;
		public String p_S3_XR_PrecioSalida;
		public String p_S3_I_PosicionOpe;
		public Long p_KS4_XR_NroFlu;
		public String p_S4_XR_TipoFlujo;
		public Long p_S4_XR_FacTasa;
		public Long p_S4_XR_SpreadTasa;
		public Long p_S4_XR_TasaFija;
		public String p_S4_XR_FecFixTasa;
		public String p_S4_XR_FecIni;
		public String p_S4_XR_FecTer;
		public Long p_S4_XR_MontoNoc;
		public Long p_S4_XR_MontoInt;
		public String p_S4_X_MonMonto;
		public Long p_KS5_CorrPago;
		public String p_KS5_IdPago;
		public String p_S5_FecPago;
		public BigDecimal p_S5_MontoPago;
		public String p_S5_TipoPago;
		public String p_S6_TipoGar;
		public String p_S6_MonVal;
		public String p_KS6_IdGar;
		public Long p_S6_MontoUmb;
		public Long p_S6_ValGtia;
		public String p_S7_TipoGar;
		public String p_KS7_IdGar;
		public String p_S7_TipoActGar;
		public Long p_S7_PorcGar;
		public String p_S8_MtodoValoriza;
		public String p_S8_MonedaValoriza;
		public String p_S8_ValorMercado;
		public String p_S8_OpVolatilidad;
		public String p_S8_OpDelta;
		public String p_S8_OpGamma;
		public String p_S8_OpVega;
		public String p_S8_AjusteRiesgo;
		public String p_S8_AjusteBID;
		public String p_S8_OtrosAjustes;
		public String p_S8_ValorMercadoActivo;
		public String p_S8_ValorMercadoPasivo;
		public Long p_IdDocumento;
		public String p_VboUsuario;
		public String p_Observaciones;
		public String p_FuenteDatos;
		public Long p_Seccion;
		public String p_Estado;
		public String p_kProducto;
		public String p_kTipoArchivo;
		public Long p_IdBaseDocumento;
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CambioRegistro 
	{
		public Long			ID;
		public String		KIdContrato;
		public String		Campo;
		public String		ValorAntiguo;
		public String		ValorNuevo;
	 	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GrabarDetalle_Compara
	{
		public Long p_ID;
		public String p_VboUsuario;
		public String p_Observaciones;
		public String p_UsrUltMod;
		public String p_HoraContrato;
		public String p_KIdContrato;
		public String p_KFechaContrato;
		public String p_S1_NaturaInfo;
		public String p_S1_EventoReporte;
		public String p_S1_LEICtraparte1;
		public String p_S1_RutCtraparte2;
		public String p_S1_LEICtraparte2;
		public String p_S1_NomCtraparte2;
		public String p_S1_PaisCtraparte2;
		public String p_S1_RutEntidad;
		public String p_S1_LEIEntidad;
		public String p_S1_RUTLiqCtraparte1;
		public String p_S1_LEILiqCtraparte1;
		public String p_S1_RutAgenteCal;
		public String p_S1_LEIAgenteCal;
		public String p_S1_NomAgenteCal;
		public String p_S1_RutCedente;
		public String p_S1_LEICedente;
		public String p_S1_PlataformaNego;
		public String p_S1_RutIntermediario;
		public String p_S1_LEIIntermediario;
		public String p_S2_Instrumento;
		public String p_S2_IdContratoAgente;
		public String p_S2_OpsClase;
		public String p_S2_OpPosicionOp;
		public String p_S2_ModalidadLiq;
		public String p_S2_MonedaComp;
		public String p_S2_FechaInicio;
		public String p_S2_FechaFixing1;
		public String p_S2_FechaTermino;
		public String p_S2_FechaPagOpe;
		public String p_S2_FechaInicioMod;
		public String p_S2_MonedaPrima;
		public BigDecimal p_S2_MontoPrima;
		public String p_S2_ObjetivoOpe;
		public String p_S2_ConvenioMarco;
		public String p_S2_PaisContrato;
		public String p_S2_Garantias;
		public String p_S2_ClausulaRecou;
		public String p_S2_ClausulaTermino;
		public String p_S2_FechaEfecTermino;
		public String p_S2_CompresionCartera;
		public Long p_S2_NumTotIntercam;
		public Long p_S2_NumTotPagos;
		public String p_S3_XR_OpInstruSub;
		public String p_S3_MonedaComprada;
		public BigDecimal p_S3_MontoComprada;
		public String p_S3_XR_MonedaVendida;
		public BigDecimal p_S3_XR_MontoVendida;
		public String p_S3_XR_TipoTasaEnt;
		public String p_S3_XR_TipoTasaRec;
		public BigDecimal p_S3_R_TasaFija;
		public BigDecimal p_S3_R_Spread;
		public String p_S3_I_TipoActivoSub;
		public String p_S3_I_IdActivoSuby;
		public BigDecimal p_S3_I_TasaPactada;
		public String p_S3_XR_FechaFixing2;
		public String p_S3_XR_ConvTipoCambio;
		public BigDecimal p_S3_PrecioFuturo;
		public BigDecimal p_S3_XR_PuntosForward;
		public BigDecimal p_S3_XR_PrecioEntrada;
		public BigDecimal p_S3_XR_PrecioSalida;
		public String p_S3_I_PosicionOpe;
		public String p_S8_MtodoValoriza;
		public String p_S8_MonedaValoriza;
		public BigDecimal p_S8_ValorMercado;
		public BigDecimal p_S8_OpVolatilidad;
		public BigDecimal p_S8_OpDelta;
		public BigDecimal p_S8_OpGamma;
		public BigDecimal p_S8_OpVega;
		public BigDecimal p_S8_AjusteRiesgo;
		public BigDecimal p_S8_AjusteBID;
		public BigDecimal p_S8_OtrosAjustes;
		public BigDecimal p_S8_ValorMercadoActivo;
		public BigDecimal p_S8_ValorMercadoPasivo;
		public String p_kTipoArchivo;
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class GrabarDetalle_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		
		public GrabarDetalle_Salida()
		{	
			this.P_OUT_ERRORESCONTADOR 		= 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		GrabarDetalle_Entrada in = gson.fromJson(jin.toString(), GrabarDetalle_Entrada.class);
		GrabarDetalle_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void agregarLog(Connection con, GrabarDetalle_Entrada entrada, List<CambioRegistro> cambioRegistro) throws Exception {
		log.debug("INICIO::GrabarDetalle.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValor = cambioRegistro.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = solicitud.ID;
			detalle.SubRegistro = solicitud.KIdContrato;
			detalle.CampoRegistro = solicitud.Campo;
			detalle.ValorAntiguo = solicitud.ValorAntiguo;
			detalle.ValorNuevo = solicitud.ValorNuevo;
			return detalle;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValor);
		
		entradaAgregarLog.p_Log = cambioRegistro.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = solicitud.ID;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "RECTIFICACION";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.NombreProceso = "Cambios en contrato SIID marcado para Rectificar";
			log.IdRegistro = String.valueOf(solicitud.ID);
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		log.debug("FIN::GrabarDetalle.agregarLog", fin - inicio);
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public GrabarDetalle_Salida logica(Connection con, GrabarDetalle_Entrada in) throws Exception {
		
		final Logger log = LogManager.getLogger(GrabarDetalle_Salida.class);
		
		log.info("== INICIO GRABAR DETALLE  ==");		
		log.info("ENTRADA > " + in.toString());
		
		SQL _sql = new SQL();
		GrabarDetalle_Salida out = new GrabarDetalle_Salida();
		
		List<GrabarDetalle_Entrada> GrabarDetalle_Entrada = new ArrayList<>();
		GrabarDetalle_Entrada.add(in);
		
		// Actualiza registro
		if (in.p_ID>0) {
			
			HashMap<String,Object> params = new HashMap<>();
			List<GrabarDetalle_Compara> GrabarDetalle_Sel = new ArrayList<>();
			List<GrabarDetalle_Compara> GrabarDetalle_Nue = new ArrayList<>();
			List<CambioRegistro> Cambios = new ArrayList<>();
			
			// Detalles actuales
			if (in.p_Rectifica!=null && in.p_Rectifica.equals("S")) {
				params.put("p_ID", in.p_ID);
				try {
					GrabarDetalle_Sel = _sql.query(con, SQL_SELECT_ACTUAL, params, GrabarDetalle_Compara.class);
				} catch(Exception er) {
					log.error("ERROR::GrabarDetalle al seleccionar detalle actual del registro ", er);
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al seleccionar detalle actual del registro"));
				}
				
				
				// Detalles nuevos
				params.clear();
				List<Field> fields =  Arrays.asList(GrabarDetalle_Entrada.class.getDeclaredFields());
				for(Field f : fields) {
					if (!REGISTRO_DIFERENCIAS_NO_COMPARA.contains(f.getName())) {
						params.put(f.getName(),f.get(in));	
					}
				}
				
				
				try {
					GrabarDetalle_Nue = _sql.query(con, SQL_SELECT_NUEVO, params, GrabarDetalle_Compara.class);
				} catch(Exception er) {
					log.error("ERROR::GrabarDetalle al seleccionar valores nuevos de registro ", er);
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al seleccionar valores nuevos de registro"));
				}
				
				// Identifica cambios
				fields =  Arrays.asList(GrabarDetalle_Compara.class.getDeclaredFields());
				GrabarDetalle_Compara ActualDetalle = GrabarDetalle_Sel.get(0);
				GrabarDetalle_Compara NuevoDetalle = GrabarDetalle_Nue.get(0);
				for(Field f : fields) {
					if (!REGISTRO_DIFERENCIAS_NO_COMPARA.contains(f.getName()) && !f.getName().equals("p_ID")) {
						if (!f.get(NuevoDetalle).toString().equals(f.get(ActualDetalle).toString())) {
							CambioRegistro cambio = new CambioRegistro();
							cambio.ID = ActualDetalle.p_ID;
							cambio.KIdContrato = in.p_KIdContrato.toString();
							cambio.Campo = f.getName().substring(2);
							cambio.ValorNuevo = f.get(NuevoDetalle).toString();
							cambio.ValorAntiguo = f.get(ActualDetalle).toString();
							Cambios.add(cambio);
						}
					}
				}
			}

			// Actualiza
			try {
				_sql.batch(con, SQL_UPDATE_DETALLE, GrabarDetalle_Entrada, GrabarDetalle_Entrada.class);
				if (in.p_Rectifica!=null && in.p_Rectifica.equals("S")) {
					agregarLog(con,in,Cambios);
				}
			} catch(Exception er) {
				log.error("ERROR::GrabarDetalle al actualizar registro ", er);
				out.P_OUT_ERRORESCONTADOR += 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al actualizar registro"));
			}
		// Inserta registro
		} else {
			try {
				_sql.batch(con, SQL_INSERT_DETALLE, GrabarDetalle_Entrada, GrabarDetalle_Entrada.class);
			} catch(Exception er) {
				log.error("ERROR::GrabarDetalle al insertar registro ", er);
				out.P_OUT_ERRORESCONTADOR 	= 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al insertar registro"));
			}
		}
		
		log.info("SALIDA > " + out);
		log.info("== TERMINO GRABAR DETALLE ==");
		
		return out;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{  \"p_IdEmpresa\": 1,  \"p_IdUsuario\": 97,  \"p_UsrUltMod\": \"SISTEMA\",  \"p_IdModulo\": \"TRI\",  \"p_ID\": 1,  \"p_Rectifica\": \"S\",  \"p_KRutCtraparte1\": \"90947000-1\",  \"p_KIdContrato\": \"411633\",  \"p_KFechaContrato\": \"2023-04-07\",  \"p_HoraContrato\": \"14:02:34\",  \"p_KIdOpeEstruc\": \"0\",  \"p_S1_NaturaInfo\": \"Y\",  \"p_S1_EventoReporte\": \"CES\",  \"p_S1_LEICtraparte1\": \"9598009TN7F4HTGJD246\",  \"p_S1_RutCtraparte2\": \"76081879-8\",  \"p_S1_LEICtraparte2\": \"\",  \"p_S1_NomCtraparte2\": \"INVERSIONES TOTALPACK S.A.\",  \"p_S1_PaisCtraparte2\": \"CHL\",  \"p_S1_RutEntidad\": \"\",  \"p_S1_LEIEntidad\": \"\",  \"p_S1_RUTLiqCtraparte1\": \"\",  \"p_S1_LEILiqCtraparte1\": \"\",  \"p_S1_RutAgenteCal\": \"96572920-8\",  \"p_S1_LEIAgenteCal\": \"54930003WCM4YZ84AU06\",  \"p_S1_NomAgenteCal\": \"CCLV Contraparte Central S. A.\",  \"p_S1_RutCedente\": \"76081879-8\",  \"p_S1_LEICedente\": \"\",  \"p_S1_PlataformaNego\": \"XSGO\",  \"p_S1_RutIntermediario\": \"90947000-1\",  \"p_S1_LEIIntermediario\": \"9598009TN7F4HTGJD246\",  \"p_kPeriodoComercial\": \"2023-07-03\",  \"p_S2_Instrumento\": \"FWD\",  \"p_S2_IdContratoAgente\": \"\",  \"p_S2_OpsClase\": \"\",  \"p_S2_OpPosicionOp\": \"\",  \"p_S2_ModalidadLiq\": \"CO\",  \"p_S2_MonedaComp\": \"CLP\",  \"p_S2_FechaInicio\": \"2023-04-03\",  \"p_S2_FechaFixing1\": \"2023-07-03\",  \"p_S2_FechaTermino\": \"2023-07-03\",  \"p_S2_FechaPagOpe\": \"2023-07-03\",  \"p_S2_FechaInicioMod\": \"0001-01-01\",  \"p_S2_MonedaPrima\": \"\",  \"p_S2_MontoPrima\": \"0\",  \"p_S2_ObjetivoOpe\": \"NEG\",  \"p_S2_ConvenioMarco\": \"CCGG76081879\",  \"p_S2_PaisContrato\": \"CHL\",  \"p_S2_Garantias\": \"SC\",  \"p_S2_ClausulaRecou\": \"N\",  \"p_S2_ClausulaTermino\": \"NOT\",  \"p_S2_FechaEfecTermino\": \"0001-01-01\",  \"p_S2_CompresionCartera\": \"N\",  \"p_S2_NumTotIntercam\": \"1\",  \"p_S2_NumTotPagos\": \"1\",  \"p_S3_XR_OpInstruSub\": \"\",  \"p_S3_MonedaComprada\": \"USD\",  \"p_S3_MontoComprada\": \"1000000\",  \"p_S3_XR_MonedaVendida\": \"CLP\",  \"p_S3_XR_MontoVendida\": \"801650000\",  \"p_S3_XR_TipoTasaEnt\": \"\",  \"p_S3_XR_TipoTasaRec\": \"\",  \"p_S3_R_TasaFija\": \"0\",  \"p_S3_R_Spread\": \"0\",  \"p_S3_I_TipoActivoSub\": \"NEM\",  \"p_S3_I_IdActivoSuby\": \"SECANO\",  \"p_S3_I_TasaPactada\": \"0\",  \"p_S3_XR_FechaFixing2\": \"0001-01-01\",  \"p_S3_XR_ConvTipoCambio\": \"USD/CLP\",  \"p_S3_PrecioFuturo\": \"801.65\",  \"p_S3_XR_PuntosForward\": \"0\",  \"p_S3_XR_PrecioEntrada\": \"0\",  \"p_S3_XR_PrecioSalida\": \"0\",  \"p_S3_I_PosicionOpe\": \"BYER\",  \"p_KS4_XR_NroFlu\": 0,  \"p_S4_XR_TipoFlujo\": \"\",  \"p_S4_XR_FacTasa\": 0,  \"p_S4_XR_SpreadTasa\": 0,  \"p_S4_XR_TasaFija\": 0,  \"p_S4_XR_FecFixTasa\": \"1970-01-01T03:00:00.001Z\",  \"p_S4_XR_FecIni\": \"1970-01-01T03:00:00.001Z\",  \"p_S4_XR_FecTer\": \"1970-01-01T03:00:00.001Z\",  \"p_S4_XR_MontoNoc\": 0,  \"p_S4_XR_MontoInt\": 0,  \"p_S4_X_MonMonto\": \"\",  \"p_KS5_CorrPago\": 0,  \"p_KS5_IdPago\": \"\",  \"p_S5_FecPago\": \"1970-01-01T03:00:00.001Z\",  \"p_S5_MontoPago\": 0,  \"p_S5_TipoPago\": \"\",  \"p_S6_TipoGar\": \"\",  \"p_S6_MonVal\": \"\",  \"p_KS6_IdGar\": \"\",  \"p_S6_MontoUmb\": 0,  \"p_S6_ValGtia\": 0,  \"p_S7_TipoGar\": \"\",  \"p_KS7_IdGar\": \"\",  \"p_S7_TipoActGar\": \"\",  \"p_S7_PorcGar\": 0,  \"p_S8_MtodoValoriza\": \"M\",  \"p_S8_MonedaValoriza\": \"CLP\",  \"p_S8_ValorMercado\": \"0\",  \"p_S8_OpVolatilidad\": \"0\",  \"p_S8_OpDelta\": \"0\",  \"p_S8_OpGamma\": \"0\",  \"p_S8_OpVega\": \"0\",  \"p_S8_AjusteRiesgo\": \"0\",  \"p_S8_AjusteBID\": \"0\",  \"p_S8_OtrosAjustes\": \"0\",  \"p_IdDocumento\": 527,  \"p_VboUsuario\": \"N\",  \"p_Observaciones\": \"\",  \"p_FuenteDatos\": \"INICIAL\",  \"p_Seccion\": 1,  \"p_FecUltMod\": \"2023-09-26\",  \"p_Estado\": \"V\",  \"p_kProducto\": \"FWD\",  \"p_kTipoArchivo\": \"FX\",  \"p_IdBaseDocumento\": 0}";
			//String json = "{  \"p_IdEmpresa\": 1,  \"p_IdUsuario\": 97,  \"p_UsrUltMod\": \"SISTEMA\",  \"p_IdModulo\": \"TRI\",  \"p_KRutCtraparte1\": \"13\",  \"p_KIdContrato\": \"1337\",  \"p_KFechaContrato\": \"2023-07-11\",  \"p_HoraContrato\": \"13:11:11\",  \"p_KIdOpeEstruc\": \"\",  \"p_S1_NaturaInfo\": \"Y\",  \"p_S1_EventoReporte\": \"ACS\",  \"p_S1_LEICtraparte1\": \"13\",  \"p_S1_RutCtraparte2\": \"13\",  \"p_S1_LEICtraparte2\": \"\",  \"p_S1_NomCtraparte2\": \"13\",  \"p_S1_PaisCtraparte2\": \"13\",  \"p_S1_RutEntidad\": \"\",  \"p_S1_LEIEntidad\": \"\",  \"p_S1_RUTLiqCtraparte1\": \"\",  \"p_S1_LEILiqCtraparte1\": \"\",  \"p_S1_RutAgenteCal\": \"13\",  \"p_S1_LEIAgenteCal\": \"13\",  \"p_S1_NomAgenteCal\": \"13\",  \"p_S1_RutCedente\": \"\",  \"p_S1_LEICedente\": \"\",  \"p_S1_PlataformaNego\": \"XSGO\",  \"p_S1_RutIntermediario\": \"13\",  \"p_S1_LEIIntermediario\": \"13\",  \"p_kPeriodoComercial\": \"2023-07-12\",  \"p_S2_Instrumento\": \"CCS\",  \"p_S2_IdContratoAgente\": \"\",  \"p_S2_OpsClase\": \"AM\",  \"p_S2_OpPosicionOp\": \"BYER\",  \"p_S2_ModalidadLiq\": \"CO\",  \"p_S2_MonedaComp\": \"EUR\",  \"p_S2_FechaInicio\": \"2023-07-12\",  \"p_S2_FechaFixing1\": \"2023-07-12\",  \"p_S2_FechaTermino\": \"2023-07-12\",  \"p_S2_FechaPagOpe\": \"2023-07-12\",  \"p_S2_FechaInicioMod\": \"0001-01-01\",  \"p_S2_MonedaPrima\": \"EUR\",  \"p_S2_MontoPrima\": \"0\",  \"p_S2_ObjetivoOpe\": \"NEG\",  \"p_S2_ConvenioMarco\": \"13\",  \"p_S2_PaisContrato\": \"131\",  \"p_S2_Garantias\": \"CG\",  \"p_S2_ClausulaRecou\": \"Y\",  \"p_S2_ClausulaTermino\": \"CO1\",  \"p_S2_FechaEfecTermino\": \"0001-01-01\",  \"p_S2_CompresionCartera\": \"Y\",  \"p_S2_NumTotIntercam\": \"0\",  \"p_S2_NumTotPagos\": \"0\",  \"p_S3_XR_OpInstruSub\": \"CCS\",  \"p_S3_MonedaComprada\": \"EUR\",  \"p_S3_MontoComprada\": \"13\",  \"p_S3_XR_MonedaVendida\": \"EUR\",  \"p_S3_XR_MontoVendida\": \"3\",  \"p_S3_XR_TipoTasaEnt\": \"FIXEDRT\",  \"p_S3_XR_TipoTasaRec\": \"FIXEDRT\",  \"p_S3_R_TasaFija\": \"0\",  \"p_S3_R_Spread\": \"0\",  \"p_S3_I_TipoActivoSub\": \"ISI\",  \"p_S3_I_IdActivoSuby\": \"\",  \"p_S3_I_TasaPactada\": \"0\",  \"p_S3_XR_FechaFixing2\": \"0001-01-01\",  \"p_S3_XR_ConvTipoCambio\": \"CLP/USD\",  \"p_S3_PrecioFuturo\": \"13\",  \"p_S3_XR_PuntosForward\": \"0.13\",  \"p_S3_XR_PrecioEntrada\": \"0\",  \"p_S3_XR_PrecioSalida\": \"0\",  \"p_S3_I_PosicionOpe\": \"BYER\",  \"p_S8_MtodoValoriza\": \"M\",  \"p_S8_MonedaValoriza\": \"EUR\",  \"p_S8_ValorMercado\": \"13\",  \"p_S8_OpVolatilidad\": \"0\",  \"p_S8_OpDelta\": \"0\",  \"p_S8_OpGamma\": \"0\",  \"p_S8_OpVega\": \"0\",  \"p_S8_AjusteRiesgo\": \"0\",  \"p_S8_AjusteBID\": \"0\",  \"p_S8_OtrosAjustes\": \"0\",  \"p_VboUsuario\": \"S\",  \"p_Observaciones\": \"\",  \"p_Estado\": \"\",  \"p_kProducto\": \"FWD\",  \"p_kTipoArchivo\": \"FX\",  \"p_IdDocumento\": 527,  \"p_ID\": 0,  \"p_IdBaseDocumento\": 0}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_UsrUltMod\":\"SISTEMA\",\"p_IdModulo\":\"TRI\",\"p_ID\":1022,\"p_Rectifica\":\"N\",\"p_KRutCtraparte1\":\"18430958-0\",\"p_KIdContrato\":\"4444444\",\"p_KFechaContrato\":\"2023-07-04\",\"p_HoraContrato\":\"13:13:00\",\"p_KIdOpeEstruc\":\"\",\"p_S1_NaturaInfo\":\"Y\",\"p_S1_EventoReporte\":\"NUE\",\"p_S1_LEICtraparte1\":\"18430958-0\",\"p_S1_RutCtraparte2\":\"18430958-0\",\"p_S1_LEICtraparte2\":\"\",\"p_S1_NomCtraparte2\":\"18430958-0\",\"p_S1_PaisCtraparte2\":\"184\",\"p_S1_RutEntidad\":\"18430958-0\",\"p_S1_LEIEntidad\":\"18430958-0\",\"p_S1_RUTLiqCtraparte1\":\"18430958-0\",\"p_S1_LEILiqCtraparte1\":\"18430958-0\",\"p_S1_RutAgenteCal\":\"18430958-0\",\"p_S1_LEIAgenteCal\":\"18430958-0\",\"p_S1_NomAgenteCal\":\"18430958-0\",\"p_S1_RutCedente\":\"18430958-0\",\"p_S1_LEICedente\":\"p\",\"p_S1_PlataformaNego\":\"XSGO\",\"p_S1_RutIntermediario\":\"18430958-0\",\"p_S1_LEIIntermediario\":\"18430958-0\",\"p_kPeriodoComercial\":\"2023-07-31\",\"p_S2_Instrumento\":\"CCS\",\"p_S2_IdContratoAgente\":\"\",\"p_S2_OpsClase\":\"AM\",\"p_S2_OpPosicionOp\":\"BYER\",\"p_S2_ModalidadLiq\":\"CO\",\"p_S2_MonedaComp\":\"EUR\",\"p_S2_FechaInicio\":\"2023-07-03\",\"p_S2_FechaFixing1\":\"2023-07-18\",\"p_S2_FechaTermino\":\"2023-09-20\",\"p_S2_FechaPagOpe\":\"2023-08-10\",\"p_S2_FechaInicioMod\":\"2023-10-13\",\"p_S2_MonedaPrima\":\"\",\"p_S2_MontoPrima\":\"0\",\"p_S2_ObjetivoOpe\":\"NEG\",\"p_S2_ConvenioMarco\":\"a\",\"p_S2_PaisContrato\":\"a\",\"p_S2_Garantias\":\"CG\",\"p_S2_ClausulaRecou\":\"Y\",\"p_S2_ClausulaTermino\":\"CO1\",\"p_S2_FechaEfecTermino\":\"2023-08-15\",\"p_S2_CompresionCartera\":\"Y\",\"p_S2_NumTotIntercam\":\"0\",\"p_S2_NumTotPagos\":\"0\",\"p_S3_XR_OpInstruSub\":\"CCS\",\"p_S3_MonedaComprada\":\"EUR\",\"p_S3_MontoComprada\":\"33\",\"p_S3_XR_MonedaVendida\":\"EUR\",\"p_S3_XR_MontoVendida\":\"44\",\"p_S3_XR_TipoTasaEnt\":\"FIXEDRT\",\"p_S3_XR_TipoTasaRec\":\"FIXEDRT\",\"p_S3_R_TasaFija\":\"0\",\"p_S3_R_Spread\":\"0\",\"p_S3_I_TipoActivoSub\":\"ISI\",\"p_S3_I_IdActivoSuby\":\"\",\"p_S3_I_TasaPactada\":\"0\",\"p_S3_XR_FechaFixing2\":\"0001-01-01\",\"p_S3_XR_ConvTipoCambio\":\"CLP/USD\",\"p_S3_PrecioFuturo\":\"4343434\",\"p_S3_XR_PuntosForward\":\"0\",\"p_S3_XR_PrecioEntrada\":\"0\",\"p_S3_XR_PrecioSalida\":\"0\",\"p_S3_I_PosicionOpe\":\"BYER\",\"p_KS4_XR_NroFlu\":0,\"p_S4_XR_TipoFlujo\":\"\",\"p_S4_XR_FacTasa\":0,\"p_S4_XR_SpreadTasa\":0,\"p_S4_XR_TasaFija\":0,\"p_S4_XR_FecFixTasa\":\"1970-01-01T03:00:00.001Z\",\"p_S4_XR_FecIni\":\"1970-01-01T03:00:00.001Z\",\"p_S4_XR_FecTer\":\"1970-01-01T03:00:00.001Z\",\"p_S4_XR_MontoNoc\":0,\"p_S4_XR_MontoInt\":0,\"p_S4_X_MonMonto\":\"\",\"p_KS5_CorrPago\":0,\"p_KS5_IdPago\":\"\",\"p_S5_FecPago\":\"1970-01-01T03:00:00.001Z\",\"p_S5_MontoPago\":0,\"p_S5_TipoPago\":\"\",\"p_S6_TipoGar\":\"\",\"p_S6_MonVal\":\"\",\"p_KS6_IdGar\":\"\",\"p_S6_MontoUmb\":0,\"p_S6_ValGtia\":0,\"p_S7_TipoGar\":\"\",\"p_KS7_IdGar\":\"\",\"p_S7_TipoActGar\":\"\",\"p_S7_PorcGar\":0,\"p_S8_MtodoValoriza\":\"M\",\"p_S8_MonedaValoriza\":\"EUR\",\"p_S8_ValorMercado\":\"444\",\"p_S8_OpVolatilidad\":\"0\",\"p_S8_OpDelta\":\"0\",\"p_S8_OpGamma\":\"0\",\"p_S8_OpVega\":\"0\",\"p_S8_AjusteRiesgo\":\"0\",\"p_S8_AjusteBID\":\"0\",\"p_S8_OtrosAjustes\":\"0\",\"p_IdDocumento\":527,\"p_VboUsuario\":\"N\",\"p_Observaciones\":\"kjsahdjksahd\",\"p_FuenteDatos\":\"MANTENEDOR\",\"p_Seccion\":1,\"p_FecUltMod\":\"2023-10-15T03:00:00.000Z\",\"p_Estado\":\"M\",\"p_kProducto\":\"FWD\",\"p_kTipoArchivo\":\"FX\",\"p_IdBaseDocumento\":0}";
			//String json = "{\"p_IdEmpresa\": 1,\"p_IdUsuario\": 97,\"p_UsrUltMod\": \"SISTEMA\",\"p_IdModulo\": \"TRI\",\"p_KRutCtraparte1\": \"18430958-0\",\"p_KIdContrato\": \"33333\",\"p_KFechaContrato\": \"2023-07-04\",\"p_HoraContrato\": \"13:13\",\"p_KIdOpeEstruc\": \"\",\"p_S1_NaturaInfo\": \"Y\",\"p_S1_EventoReporte\": \"NUE\",\"p_S1_LEICtraparte1\": \"18430958-0\",\"p_S1_RutCtraparte2\": \"18430958-0\",\"p_S1_LEICtraparte2\": \"\",\"p_S1_NomCtraparte2\": \"v\",\"p_S1_PaisCtraparte2\": \"184\",\"p_S1_RutEntidad\": \"18430958-0\",\"p_S1_LEIEntidad\": \"18430958-0\",\"p_S1_RUTLiqCtraparte1\": \"18430958-01\",\"p_S1_LEILiqCtraparte1\": \"18430958-0\",\"p_S1_RutAgenteCal\": \"18430958-0\",\"p_S1_LEIAgenteCal\": \"18430958-018430958-0\",\"p_S1_NomAgenteCal\": \"18430958-0\",\"p_S1_RutCedente\": \"18430958-0\",\"p_S1_LEICedente\": \"\",\"p_S1_PlataformaNego\": \"XSGO\",\"p_S1_RutIntermediario\": \"18430958-0\",\"p_S1_LEIIntermediario\": \"18430958-0\",\"p_kPeriodoComercial\": \"2023-07-04\",\"p_S2_Instrumento\": \"CCS\",\"p_S2_IdContratoAgente\": \"\",\"p_S2_OpsClase\": \"AM\",\"p_S2_OpPosicionOp\": \"BYER\",\"p_S2_ModalidadLiq\": \"CO\",\"p_S2_MonedaComp\": \"EUR\",\"p_S2_FechaInicio\": \"2023-07-04\",\"p_S2_FechaFixing1\": \"2023-10-04\",\"p_S2_FechaTermino\": \"2023-10-04\",\"p_S2_FechaPagOpe\": \"2023-10-04\",\"p_S2_FechaInicioMod\": \"2023-10-15\",\"p_S2_MonedaPrima\": \"EUR\",\"p_S2_MontoPrima\": \"0\",\"p_S2_ObjetivoOpe\": \"NEG\",\"p_S2_ConvenioMarco\": \"a\",\"p_S2_PaisContrato\": \"a\",\"p_S2_Garantias\": \"CG\",\"p_S2_ClausulaRecou\": \"Y\",\"p_S2_ClausulaTermino\": \"CO1\",\"p_S2_FechaEfecTermino\": \"2023-10-04\",\"p_S2_CompresionCartera\": \"Y\",\"p_S2_NumTotIntercam\": \"0\",\"p_S2_NumTotPagos\": \"0\",\"p_S3_XR_OpInstruSub\": \"CCS\",\"p_S3_MonedaComprada\": \"EUR\",\"p_S3_MontoComprada\": \"333\",\"p_S3_XR_MonedaVendida\": \"EUR\",\"p_S3_XR_MontoVendida\": \"222\",\"p_S3_XR_TipoTasaEnt\": \"FIXEDRT\",\"p_S3_XR_TipoTasaRec\": \"FIXEDRT\",\"p_S3_R_TasaFija\": \"0\",\"p_S3_R_Spread\": \"0\",\"p_S3_I_TipoActivoSub\": \"ISI\",\"p_S3_I_IdActivoSuby\": \"\",\"p_S3_I_TasaPactada\": \"0\",\"p_S3_XR_FechaFixing2\": \"0001-01-01\",\"p_S3_XR_ConvTipoCambio\": \"CLP/USD\",\"p_S3_PrecioFuturo\": \"234242323\",\"p_S3_XR_PuntosForward\": \"0\",\"p_S3_XR_PrecioEntrada\": \"0\",\"p_S3_XR_PrecioSalida\": \"0\",\"p_S3_I_PosicionOpe\": \"BYER\",\"p_S8_MtodoValoriza\": \"M\",\"p_S8_MonedaValoriza\": \"EUR\",\"p_S8_ValorMercado\": \"232323\",\"p_S8_OpVolatilidad\": \"0\",\"p_S8_OpDelta\": \"0\",\"p_S8_OpGamma\": \"0\",\"p_S8_OpVega\": \"0\",\"p_S8_AjusteRiesgo\": \"0\",\"p_S8_AjusteBID\": \"0\",\"p_S8_OtrosAjustes\": \"0\",\"p_VboUsuario\": \"S\",\"p_Observaciones\": \"\",\"p_Estado\": \"\",\"p_kProducto\": \"FWD\",\"p_kTipoArchivo\": \"IR\",\"p_IdDocumento\": 527,\"p_ID\": 0,\"p_IdBaseDocumento\": 0}";

			GrabarDetalle_Entrada Objin = gson.fromJson(json, GrabarDetalle_Entrada.class);
			GrabarDetalle demo = new GrabarDetalle();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				GrabarDetalle_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
// 				con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			con.close(); 			
	    }		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
