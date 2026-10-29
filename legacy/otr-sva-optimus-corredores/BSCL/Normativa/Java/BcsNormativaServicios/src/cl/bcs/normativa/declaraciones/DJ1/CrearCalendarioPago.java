package cl.bcs.normativa.declaraciones.DJ1;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCalendarioPago implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearCalendarioPago.class);
	
	private final String SQL_INSERT_CALENDARIO_PAGO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearCalendarioPago_Insert_Calendario_Pago.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearCalendarioPago_Insert_Tabla_Temporal_Calendario_Pago.sql");
	private final String SQL_SELECT_DATOS_CABECERA_MANUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/DeclaracionesDJ1_Select_Datos_Cabecera.sql");
	private final String SQL_SELECT_DATOS_CABECERA_ARCHIVO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearCalendarioPago_Select_Datos_Cabecera_Calendario_Pago_Archivo.sql");
	private final String SQL_SELECT_CALENDARIO_PAGO_BY_NUMERO_FLUJO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearCalendarioPago_Select_Calendario_Pago_By_Numero_Flujo.sql");
	private final String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	private final String SQL_SELECT_DATOS_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearCalendarioPago_Select_Datos_Instrumento.sql");
	
	public static class EntradaCrearCalendarioPago extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_TipoCarga;
		public List<CalendarioPagoEntrada> p_CalendarioPagos; 
		
		public EntradaCrearCalendarioPago(){
			this.p_CalendarioPagos = new ArrayList<CalendarioPagoEntrada>();
		}
	}
	
	public static class CalendarioPagoEntrada extends BcsPojo{
		public Long p_Seccion;
		public String p_RutContraparte;
		public String p_IdContrato;
		public String p_FechaContrato;
		public String p_IdOperacion;
		public String p_Producto;
		public String p_PeriodoComercial;
		public Long p_NumeroFlujo;
		public String p_TipoFlujo;
		public BigDecimal p_FactorTasa;
		public BigDecimal p_SpreadTasa;
		public BigDecimal p_TasaFija;
		public String p_FechaFixTasa;
		public String p_FechaInicio;
		public String p_FechaTermino;
		public BigDecimal p_MontoNocional;
		public BigDecimal p_MontoIntercambio;
		public String p_MonedaMonto;
	}
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}
	
	public static class RegistroInstrumento extends BcsPojo {
		public String 		IdContrato;
		public String 		S2_Instrumento;
	}
	
	public static class RegistroCamposNuevo extends BcsPojo {
		public String		IdContrato;
		public Long			KS4_XR_NroFlu;
		public String		S4_XR_TipoFlujo;
		public BigDecimal	S4_XR_FacTasa;
		public BigDecimal	S4_XR_SpreadTasa;
		public BigDecimal	S4_XR_TasaFija;
		public String		S4_XR_FecFixTasa;
		public String		S4_XR_FecIni;
		public String		S4_XR_FecTer;
		public BigDecimal	S4_XR_MontoNoc;
		public BigDecimal	S4_XR_MontoInt;
		public String		S4_X_MonMonto;
	}
	
	public static class SalidaCrearCalendarioPago extends BcsPojo{
		public Long P_OUT_INSERTADOS;
		public Long P_OUT_CABECERANOEXISTE;
		public Long P_OUT_REPETIDOS;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearCalendarioPago() {
			this.P_OUT_INSERTADOS = 0L;
			this.P_OUT_CABECERANOEXISTE = 0L;
			this.P_OUT_REPETIDOS = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class CalendarioPago extends BcsPojo{
		public Long IdDocumento;
		public String VboUsuario;
		public String Observaciones;
		public String FuenteDatos;
		public Long Seccion;
		public String KRutCtraparte1;
		public String KIdContrato;
		public Date KFechaContrato;
		public String HoraContrato;
		public String KIdOpeEstruc;
		public String kProducto;
		public Date kPeriodoComercial;
		public Long KS4_XR_NroFlu;
		public String S4_XR_TipoFlujo;
		public BigDecimal S4_XR_FacTasa;
		public BigDecimal S4_XR_SpreadTasa;
		public BigDecimal S4_XR_TasaFija;
		public String S4_XR_FecFixTasa;
		public String S4_XR_FecIni;
		public String S4_XR_FecTer;
		public BigDecimal S4_XR_MontoNoc;
		public BigDecimal S4_XR_MontoInt;
		public String S4_X_MonMonto;
		public String S2_Instrumento;
		public String UsrUltMod;
		public String FecUltMod;
		public String Estado;
		public String Rectifica;
	}
	
	public static class Cabecera extends BcsPojo{
		public Long IdDocumento;
		public String VboUsuario;
		public String Observaciones;
		public String KRutCtraparte1;
		public String KIdContrato;
		public Date KFechaContrato;
		public String HoraContrato;
		public String KIdOpeEstruc;
		public Date kPeriodoComercial;
		public String kProducto;
		public String Rectifica;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearCalendarioPago jin = gson.fromJson(in.toString(), EntradaCrearCalendarioPago.class);
		SalidaCrearCalendarioPago out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearCalendarioPago logica(Connection con, EntradaCrearCalendarioPago entrada) throws Exception {
		logger.info("INICIO::CrearCalendarioPago");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearCalendarioPago out = new SalidaCrearCalendarioPago();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String codigoUsuario = Utils.getCodigoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		// Selecciona marca Rectifica de los contratos
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(CalendarioPagoEntrada r : entrada.p_CalendarioPagos) {
			if (!lstIdContrato.contains(Long.parseLong(r.p_IdContrato))) {
				lstIdContrato.add(Long.parseLong(r.p_IdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);
		
		// Selecciona marca instrumento de cada contrato
		List<RegistroInstrumento> registroInstrumento = sql.query(con, SQL_SELECT_DATOS_INSTRUMENTO, params, RegistroInstrumento.class);
		
		// armar listado de objeto con nombre de los campos y valores a crear
		List<RegistroCamposNuevo> registroCamposNuevo = entrada.p_CalendarioPagos.parallelStream().map(solicitud->{
			RegistroCamposNuevo registroNuevo = new RegistroCamposNuevo();
			registroNuevo.IdContrato = solicitud.p_IdContrato;
			registroNuevo.KS4_XR_NroFlu = solicitud.p_NumeroFlujo;
			registroNuevo.S4_XR_TipoFlujo = solicitud.p_TipoFlujo;
			registroNuevo.S4_XR_FacTasa = solicitud.p_FactorTasa;
			registroNuevo.S4_XR_SpreadTasa = solicitud.p_SpreadTasa;
			registroNuevo.S4_XR_TasaFija = solicitud.p_TasaFija;
			registroNuevo.S4_XR_FecFixTasa = solicitud.p_FechaFixTasa;
			registroNuevo.S4_XR_FecIni = solicitud.p_FechaInicio;
			registroNuevo.S4_XR_FecTer = solicitud.p_FechaTermino;
			registroNuevo.S4_XR_MontoNoc = solicitud.p_MontoNocional;
			registroNuevo.S4_XR_MontoInt = solicitud.p_MontoIntercambio;
			registroNuevo.S4_X_MonMonto = solicitud.p_MonedaMonto;
			return registroNuevo;
		}).collect(Collectors.toList());
		
		if (entrada.p_CalendarioPagos.size() > 0) {		
			if("ARCHIVO".equals(entrada.p_TipoCarga)) {
				out = cargarCalendarioPagoArchivo(con, entrada, registroInstrumento, fechaSistema, codigoUsuario); 
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Calendarios de pago insertados: " + out.P_OUT_INSERTADOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Calendarios de pago repetidos: " + out.P_OUT_REPETIDOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Cabecera no existe: " + out.P_OUT_CABECERANOEXISTE));
			}
			
			if("MANUAL".equals(entrada.p_TipoCarga)) {
				out = cargarCalendarioPagoManual(con, entrada, registroInstrumento, fechaSistema, codigoUsuario);
				if(out.P_OUT_REPETIDOS > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR", "Numero de flujo ya existe"));
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Calendario de pago insertado"));
				}
			}
			
			// Registra LOG si el contrato estaba marcado para Rectificar
			agregarLog(con,entrada,registroRectifica,registroCamposNuevo);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCalendarioPago", fin - inicio);
		return out;
	}
	
	private SalidaCrearCalendarioPago cargarCalendarioPagoManual(Connection con, EntradaCrearCalendarioPago entrada, List<RegistroInstrumento> instrumento, String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearCalendarioPago.cargarCalendarioPagoManual");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearCalendarioPago out = new SalidaCrearCalendarioPago();
		Cabecera cabecera = obtenerDatosCabeceraManual(con, entrada.p_CalendarioPagos);
		
		List<CalendarioPago> lstCalendarioPago = new ArrayList<CalendarioPago>();
		
		for (CalendarioPagoEntrada calendarioPagoEntrada : entrada.p_CalendarioPagos) {					
			CalendarioPago calendarioPago = new CalendarioPago();
			calendarioPago.IdDocumento = cabecera.IdDocumento;
			calendarioPago.VboUsuario = cabecera.VboUsuario;
			calendarioPago.Observaciones = cabecera.Observaciones;
			calendarioPago.FuenteDatos = "MANTENEDOR";
			calendarioPago.KRutCtraparte1 = cabecera.KRutCtraparte1; 
			calendarioPago.KIdContrato = cabecera.KIdContrato;
			calendarioPago.KFechaContrato = cabecera.KFechaContrato;
			calendarioPago.HoraContrato = cabecera.HoraContrato;
			calendarioPago.KIdOpeEstruc = cabecera.KIdOpeEstruc;
			calendarioPago.kProducto = cabecera.kProducto;
			calendarioPago.kPeriodoComercial = cabecera.kPeriodoComercial;
			calendarioPago.Seccion = 4L;
			calendarioPago.KS4_XR_NroFlu = calendarioPagoEntrada.p_NumeroFlujo;
			calendarioPago.S4_XR_TipoFlujo = calendarioPagoEntrada.p_TipoFlujo;
			calendarioPago.S4_XR_FacTasa = calendarioPagoEntrada.p_FactorTasa;
			calendarioPago.S4_XR_SpreadTasa = calendarioPagoEntrada.p_SpreadTasa;
			calendarioPago.S4_XR_TasaFija = calendarioPagoEntrada.p_TasaFija;
			calendarioPago.S4_XR_FecFixTasa = calendarioPagoEntrada.p_FechaFixTasa;
			calendarioPago.S4_XR_FecIni = calendarioPagoEntrada.p_FechaInicio;
			calendarioPago.S4_XR_FecTer = calendarioPagoEntrada.p_FechaTermino;
			calendarioPago.S4_XR_MontoNoc = calendarioPagoEntrada.p_MontoNocional;
			calendarioPago.S4_XR_MontoInt = calendarioPagoEntrada.p_MontoIntercambio;
			calendarioPago.S4_X_MonMonto = calendarioPagoEntrada.p_MonedaMonto;
			calendarioPago.S2_Instrumento = instrumento.stream().filter(registro -> registro.IdContrato.equals(cabecera.KIdContrato)).findFirst().get().S2_Instrumento;
			calendarioPago.UsrUltMod = codigoUsuario;
			calendarioPago.FecUltMod = fechaSistema;
			calendarioPago.Estado = "V";
			calendarioPago.Rectifica = cabecera.Rectifica;
			
			if(validarRepetido(con, calendarioPago)) {
				out.P_OUT_REPETIDOS++;
			}else {
				lstCalendarioPago.add(calendarioPago);
				out.P_OUT_INSERTADOS++;
			}
		}
		
		if(lstCalendarioPago.size() > 0) {
			sql.batch(con, SQL_INSERT_CALENDARIO_PAGO, lstCalendarioPago, CalendarioPago.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCalendarioPago.cargarCalendarioPagoManual", fin - inicio);
		return out;
	}

	private SalidaCrearCalendarioPago cargarCalendarioPagoArchivo(Connection con, EntradaCrearCalendarioPago entrada, List<RegistroInstrumento> instrumento, String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearCalendarioPago.cargarCalendarioPagoArchivo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearCalendarioPago out = new SalidaCrearCalendarioPago();
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, entrada.p_CalendarioPagos, CalendarioPagoEntrada.class);
		HashMap<String, Cabecera> cabeceraHash = new HashMap<String, Cabecera>();
		List<Cabecera> lstCabeceras = obtenerDatosCabeceraArchivo(con, entrada);				
		
		for (Cabecera cabecera : lstCabeceras) {
			String keyCabecera = "";

			keyCabecera = cabecera.KRutCtraparte1 + "_" + cabecera.KIdContrato + "_" + cabecera.KFechaContrato + "_" + cabecera.KIdOpeEstruc + "_" + cabecera.kProducto + "_" + cabecera.kPeriodoComercial;

			if (!cabeceraHash.containsKey(keyCabecera)) {
				cabeceraHash.put(keyCabecera, cabecera);
			}
		}
		
		List<CalendarioPago> lstCalendarioPago = new ArrayList<CalendarioPago>();
		
		for (CalendarioPagoEntrada calendarioPagoEntrada : entrada.p_CalendarioPagos) {				
			String keyCabeceraEntrada = calendarioPagoEntrada.p_RutContraparte + "_" + calendarioPagoEntrada.p_IdContrato + "_" + calendarioPagoEntrada.p_FechaContrato + "_" 
										+ calendarioPagoEntrada.p_IdOperacion + "_" + calendarioPagoEntrada.p_Producto + "_" + calendarioPagoEntrada.p_PeriodoComercial;
			Cabecera cabeceraExiste = cabeceraHash.get(keyCabeceraEntrada);
				
			if(cabeceraExiste == null) {
				out.P_OUT_CABECERANOEXISTE++;
				continue;
			} else {	
				CalendarioPago calendarioPago = new CalendarioPago();
				calendarioPago.IdDocumento = cabeceraExiste.IdDocumento;
				calendarioPago.VboUsuario = cabeceraExiste.VboUsuario;
				calendarioPago.Observaciones = cabeceraExiste.Observaciones;
				calendarioPago.FuenteDatos = "EXTERNA";
				calendarioPago.KRutCtraparte1 = cabeceraExiste.KRutCtraparte1; 
				calendarioPago.KIdContrato = cabeceraExiste.KIdContrato;
				calendarioPago.KFechaContrato = cabeceraExiste.KFechaContrato;
				calendarioPago.HoraContrato = cabeceraExiste.HoraContrato;
				calendarioPago.KIdOpeEstruc = cabeceraExiste.KIdOpeEstruc;
				calendarioPago.kProducto = cabeceraExiste.kProducto;
				calendarioPago.kPeriodoComercial = cabeceraExiste.kPeriodoComercial;
				calendarioPago.Seccion = 4L;
				calendarioPago.KS4_XR_NroFlu = calendarioPagoEntrada.p_NumeroFlujo;
				calendarioPago.S4_XR_TipoFlujo = calendarioPagoEntrada.p_TipoFlujo;
				calendarioPago.S4_XR_FacTasa = calendarioPagoEntrada.p_FactorTasa;
				calendarioPago.S4_XR_SpreadTasa = calendarioPagoEntrada.p_SpreadTasa;
				calendarioPago.S4_XR_TasaFija = calendarioPagoEntrada.p_TasaFija;
				calendarioPago.S4_XR_FecFixTasa = calendarioPagoEntrada.p_FechaFixTasa;
				calendarioPago.S4_XR_FecIni = calendarioPagoEntrada.p_FechaInicio;
				calendarioPago.S4_XR_FecTer = calendarioPagoEntrada.p_FechaTermino;
				calendarioPago.S4_XR_MontoNoc = calendarioPagoEntrada.p_MontoNocional;
				calendarioPago.S4_XR_MontoInt = calendarioPagoEntrada.p_MontoIntercambio;
				calendarioPago.S4_X_MonMonto = calendarioPagoEntrada.p_MonedaMonto;
				calendarioPago.S2_Instrumento = instrumento.stream().filter(registro -> registro.IdContrato.equals(cabeceraExiste.KIdContrato)).findFirst().get().S2_Instrumento;
				calendarioPago.UsrUltMod = codigoUsuario;
				calendarioPago.FecUltMod = fechaSistema;
				calendarioPago.Estado = "V";
				calendarioPago.Rectifica = cabeceraExiste.Rectifica;
				
				if(validarRepetido(con, calendarioPago)) {
					out.P_OUT_REPETIDOS++;
				} else {
					lstCalendarioPago.add(calendarioPago);
					out.P_OUT_INSERTADOS++;
				}
			}
		}
		
		if(lstCalendarioPago.size() > 0) {
			sql.batch(con, SQL_INSERT_CALENDARIO_PAGO, lstCalendarioPago, CalendarioPago.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCalendarioPago.cargarCalendarioPagoArchivo", fin - inicio);
		return out;
	}

	private boolean validarRepetido(Connection con, CalendarioPago calendarioPago) throws Exception {
		logger.debug("INICIO::CrearCalendarioPago.validarRepetido");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("KRutCtraparte1", calendarioPago.KRutCtraparte1);
		parametros.put("KIdContrato", calendarioPago.KIdContrato);
		parametros.put("KFechaContrato", calendarioPago.KFechaContrato);
		parametros.put("KIdOpeEstruc", calendarioPago.KIdOpeEstruc);
		parametros.put("kProducto", calendarioPago.kProducto);
		parametros.put("kPeriodoComercial", calendarioPago.kPeriodoComercial);
		parametros.put("KS4_XR_NroFlu", calendarioPago.KS4_XR_NroFlu);
		SQL sql = new SQL();
		Long contador = sql.countQuery(con, SQL_SELECT_CALENDARIO_PAGO_BY_NUMERO_FLUJO, parametros);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCalendarioPago.validarRepetido", fin - inicio);
		return contador > 0;
	}
	
	private Cabecera obtenerDatosCabeceraManual(Connection con, List<CalendarioPagoEntrada> lstCalendarioPago) throws Exception {
		logger.debug("INICIO::CrearCalendarioPago.obtenerDatosCabeceraManual");
		long inicio = System.currentTimeMillis();
		CalendarioPagoEntrada calendarioPago = lstCalendarioPago.parallelStream().findFirst().orElse(new CalendarioPagoEntrada());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("KRutCtraparte1", calendarioPago.p_RutContraparte);
		parametros.put("KIdContrato", calendarioPago.p_IdContrato);
		parametros.put("KFechaContrato", calendarioPago.p_FechaContrato);
		parametros.put("KIdOpeEstruc", calendarioPago.p_IdOperacion);
		parametros.put("kProducto", calendarioPago.p_Producto);
		parametros.put("kPeriodoComercial", calendarioPago.p_PeriodoComercial);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_MANUAL, parametros, Cabecera.class);
		Cabecera cabecera = lstCabecera.parallelStream().findFirst().orElse(new Cabecera());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCalendarioPago.obtenerDatosCabeceraManual", fin - inicio);
		return cabecera;
	}
	
	private List<Cabecera> obtenerDatosCabeceraArchivo(Connection con, EntradaCrearCalendarioPago entrada) throws Exception {
		logger.debug("INICIO::CrearCalendarioPago.obtenerDatosCabeceraArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_ARCHIVO, parametros, Cabecera.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCalendarioPago.obtenerDatosCabeceraArchivo", fin - inicio);
		return lstCabecera;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------

	private void agregarLog(Connection con, EntradaCrearCalendarioPago entrada, List<RegistroRectifica> registroRectifica, List<RegistroCamposNuevo> registroCamposNuevo) throws Exception {
		logger.debug("INICIO::GrabarDetalle.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Field> fields =  Arrays.asList(RegistroCamposNuevo.class.getDeclaredFields());
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		// filtra los registros rectificados
		List<RegistroCamposNuevo> rectificadosCamposNuevo = registroCamposNuevo.parallelStream().filter(registro -> {
			for (RegistroRectifica r: registroRectifica) {
				if (r.IdContrato.equals(registro.IdContrato))
					return true;
			}
			return false;
		}).collect(Collectors.toList());
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValor = new ArrayList<AgregarLog.InAgregarLog.DetalleLog>();
		for(RegistroCamposNuevo r: rectificadosCamposNuevo) {
			for(Field f : fields) {
				if (!"IdContrato".equals(f.getName())) {
					AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
					detalle.IdCabeceraLog = Long.parseLong(r.IdContrato,10);
					detalle.SubRegistro = r.IdContrato;
					detalle.CampoRegistro = f.getName();
					detalle.ValorAntiguo = "";
					detalle.ValorNuevo =  String.valueOf(f.get(r));
					detalleLogValor.add(detalle);
				}
			}
		}
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValor);
		
		entradaAgregarLog.p_Log = rectificadosCamposNuevo.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = Long.parseLong(solicitud.IdContrato,10);
			log.IdTipoLog = "CREACION";
			log.IdModulo = "TRI";
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
			log.IdRegistro = solicitud.IdContrato;
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GrabarDetalle.agregarLog", fin - inicio);
	}
	
	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
		public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String jsonManual = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"NOR\",\"p_TipoCarga\":\"MANUAL\",\"p_CalendarioPagos\":[{\"p_RutContraparte\":\"90947000-1\",\"p_IdContrato\":\"20694163\",\"p_FechaContrato\":\"2023-07-05\",\"p_IdOperacion\":\"0\",\"p_Producto\":\"FWD\",\"p_PeriodoComercial\":\"2023-07-19\",\"p_NumeroFlujo\":7,\"p_TipoFlujo\":\"R\",\"p_FactorTasa\":7,\"p_SpreadTasa\":7,\"p_TasaFija\":7,\"p_FechaFixTasa\":\"2023-11-30\",\"p_FechaInicio\":\"2023-11-30\",\"p_FechaTermino\":\"2023-11-30\",\"p_MontoNocional\":7,\"p_MontoIntercambio\":7,\"p_MonedaMonto\":\"EUR\"}]}";
				//String jsonArchivo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"NOR\",\"p_TipoCarga\":\"ARCHIVO\",\"p_IDCab\":0,\"p_CalendarioPagos\":[{\"p_RutContraparte\":\"2-7\",\"p_IdContrato\":\"idcon1\",\"p_FechaContrato\":\"2020-01-21\",\"p_IdOperacion\":\"ss\",\"p_Producto\":\"FW\",\"p_PeriodoComercial\":\"2020-01-21\",\"p_NumeroFlujo\":\"1\",\"p_TipoFlujo\":\"E\",\"p_FactorTasa\":\"7\",\"p_SpreadTasa\":\"4\",\"p_TasaFija\":\"7\",\"p_FechaFixTasa\":\"2019-02-05\",\"p_FechaInicio\":\"2019-02-05\",\"p_FechaTermino\":\"2019-02-05\",\"p_MontoNocional\":\"1\",\"p_MontoIntercambio\":\"4\",\"p_MonedaMonto\":\"7\"}]}";
				EntradaCrearCalendarioPago Objin = gson.fromJson(jsonManual, EntradaCrearCalendarioPago.class);
				
				CrearCalendarioPago demo = new CrearCalendarioPago();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
		 		
	 			try {
	 				SalidaCrearCalendarioPago ObjOut = demo.logica(con, Objin);
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
					er.printStackTrace();
				}
				finally {
					con.rollback();
					con.close();
				}
			}
			System.out.println("== END ==");
		}
	
}
