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

public class CrearPagosPeriodo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearPagosPeriodo.class);
	
	private final String SQL_INSERT_PAGO_PERIODO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearPagosPeriodo_Insert_Pago_Periodo.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearPagosPeriodo_Insert_Tabla_Temporal_Pagos_Periodo.sql");
	private final String SQL_SELECT_DATOS_CABECERA_MANUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/DeclaracionesDJ1_Select_Datos_Cabecera.sql");
	private final String SQL_SELECT_DATOS_CABECERA_ARCHIVO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearPagosPeriodo_Select_Datos_Cabecera_Pago_Periodo_Archivo.sql");
	private final String SQL_SELECT_PAGO_PERIODO_BY_ID_PAGO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearPagosPeriodo_Select_Pago_Periodo_By_Id_Pago.sql");
	private final String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	
	public static class EntradaCrearPagosPeriodo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_TipoCarga;
		public List<PagoPeriodoEntrada> p_PagosPeriodo;
		
		public EntradaCrearPagosPeriodo() {
			this.p_PagosPeriodo =  new ArrayList<PagoPeriodoEntrada>();
		}	
	}
	
	public static class PagoPeriodoEntrada extends BcsPojo{
		public Long p_Seccion;
		public String p_RutContraparte;
		public String p_IdContrato;
		public String p_FechaContrato;
		public String p_IdOperacion;
		public String p_Producto;
		public String p_PeriodoComercial;
		public Long p_CorrelativoPago;
		public String p_IdPago;
		public String p_FechaPago;  
		public BigDecimal p_MontoPago;    
		public String p_TipoPago; 
		public String p_CodMoneda;
	}
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}

	public static class RegistroCamposNuevo extends BcsPojo {
		public String		IdContrato;
		public Long			KS5_CorrPago;
		public String		KS5_IdPago;
		public String		S5_FecPago;
		public BigDecimal	S5_MontoPago;
		public String		S5_TipoPago;
		public String		S5_CodMoneda;
	}
	
	public static class SalidaCrearPagosPeriodo extends BcsPojo{
		public Long P_OUT_INSERTADOS;
		public Long P_OUT_CABECERANOEXISTE;
		public Long P_OUT_REPETIDOS;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearPagosPeriodo() {
			this.P_OUT_INSERTADOS = 0L;
			this.P_OUT_CABECERANOEXISTE = 0L;
			this.P_OUT_REPETIDOS = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class PagoPeriodo extends BcsPojo{
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
		public Long KS5_CorrPago;
		public String KS5_IdPago;
		public String S5_FecPago;
		public BigDecimal S5_MontoPago;
		public String S5_TipoPago;
		public String S5_CodMoneda;
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
		EntradaCrearPagosPeriodo jin = gson.fromJson(in.toString(), EntradaCrearPagosPeriodo.class);
		SalidaCrearPagosPeriodo out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearPagosPeriodo logica(Connection con, EntradaCrearPagosPeriodo entrada) throws Exception {
		logger.info("INICIO::CrearPagosPeriodo");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearPagosPeriodo out = new SalidaCrearPagosPeriodo();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String codigoUsuario = Utils.getCodigoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		// Selecciona marca Rectifica de los contratos
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(PagoPeriodoEntrada r : entrada.p_PagosPeriodo) {
			if (!lstIdContrato.contains(Long.parseLong(r.p_IdContrato))) {
				lstIdContrato.add(Long.parseLong(r.p_IdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);

		// armar listado de objeto con nombre de los campos y valores a crear
		List<RegistroCamposNuevo> registroCamposNuevo = entrada.p_PagosPeriodo.parallelStream().map(solicitud->{
			RegistroCamposNuevo registroNuevo = new RegistroCamposNuevo();
			registroNuevo.IdContrato = solicitud.p_IdContrato;
			registroNuevo.KS5_CorrPago = solicitud.p_CorrelativoPago;
			registroNuevo.KS5_IdPago = solicitud.p_IdPago;
			registroNuevo.S5_FecPago = solicitud.p_FechaPago;
			registroNuevo.S5_MontoPago = solicitud.p_MontoPago;
			registroNuevo.S5_TipoPago = solicitud.p_TipoPago;
			registroNuevo.S5_CodMoneda = solicitud.p_CodMoneda;
			return registroNuevo;
		}).collect(Collectors.toList());
		
		if(entrada.p_PagosPeriodo.size() > 0) {
			if("ARCHIVO".equals(entrada.p_TipoCarga)) {
				out = cargarPagosPeriodoArchivo(con, entrada, fechaSistema, codigoUsuario); 
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Pagos Periodo insertados: " + out.P_OUT_INSERTADOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Pagos Periodo repetidos: " + out.P_OUT_REPETIDOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Cabecera no existe: " + out.P_OUT_CABECERANOEXISTE));				
			}
			
			if("MANUAL".equals(entrada.p_TipoCarga)) {
				out = cargarPagosPeriodoManual(con, entrada, fechaSistema, codigoUsuario);
				if(out.P_OUT_REPETIDOS > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR", "Correlativo de pago e Id de pago ya existen"));
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Pago Periodo insertado"));
				}
			}
			
			// Registra LOG si el contrato estaba marcado para Rectificar
			agregarLog(con,entrada,registroRectifica,registroCamposNuevo);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPagosPeriodo", finServicio - inicioServicio);
		return out;
	}
	
	private SalidaCrearPagosPeriodo cargarPagosPeriodoManual(Connection con, EntradaCrearPagosPeriodo entrada,String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearPagosPeriodo.cargarPagosPeriodoManual");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearPagosPeriodo out = new SalidaCrearPagosPeriodo();
		Cabecera cabecera = obtenerDatosCabeceraManual(con, entrada.p_PagosPeriodo);

		List<PagoPeriodo> lstPagosPeriodo = new ArrayList<PagoPeriodo>();
		
		for(PagoPeriodoEntrada pagoPeriodoEntrada: entrada.p_PagosPeriodo) {
			PagoPeriodo pagoPeriodo = new PagoPeriodo();
			pagoPeriodo.IdDocumento = cabecera.IdDocumento;
			pagoPeriodo.VboUsuario = cabecera.VboUsuario;
			pagoPeriodo.Observaciones = cabecera.Observaciones;
			pagoPeriodo.FuenteDatos = "MANTENEDOR";
			pagoPeriodo.KRutCtraparte1 = cabecera.KRutCtraparte1;
			pagoPeriodo.KIdContrato = cabecera.KIdContrato;
			pagoPeriodo.KFechaContrato = cabecera.KFechaContrato;
			pagoPeriodo.HoraContrato = cabecera.HoraContrato;
			pagoPeriodo.KIdOpeEstruc = cabecera.KIdOpeEstruc;
			pagoPeriodo.kProducto = cabecera.kProducto;
			pagoPeriodo.kPeriodoComercial = cabecera.kPeriodoComercial;
			pagoPeriodo.Seccion = 5L;
			pagoPeriodo.KS5_CorrPago = pagoPeriodoEntrada.p_CorrelativoPago;
			pagoPeriodo.KS5_IdPago = pagoPeriodoEntrada.p_IdPago;
			pagoPeriodo.S5_FecPago = pagoPeriodoEntrada.p_FechaPago;
			pagoPeriodo.S5_MontoPago = pagoPeriodoEntrada.p_MontoPago;
			pagoPeriodo.S5_TipoPago = pagoPeriodoEntrada.p_TipoPago;
			pagoPeriodo.S5_CodMoneda = pagoPeriodoEntrada.p_CodMoneda;
			pagoPeriodo.UsrUltMod = codigoUsuario;
			pagoPeriodo.FecUltMod = fechaSistema;
			pagoPeriodo.Estado = "V";
			pagoPeriodo.Rectifica = cabecera.Rectifica;
			
			if(validarRepetido(con, pagoPeriodo)) {
				out.P_OUT_REPETIDOS++;
			} else {
				lstPagosPeriodo.add(pagoPeriodo);
				out.P_OUT_INSERTADOS++;
			}
		}
		
		if(lstPagosPeriodo.size() > 0) {
			sql.batch(con, SQL_INSERT_PAGO_PERIODO, lstPagosPeriodo, PagoPeriodo.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPagosPeriodo.cargarPagosPeriodoManual", fin - inicio);
		return out;
	}

	private SalidaCrearPagosPeriodo cargarPagosPeriodoArchivo(Connection con, EntradaCrearPagosPeriodo entrada,String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearPagosPeriodo.cargarPagosPeriodoArchivo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearPagosPeriodo out = new SalidaCrearPagosPeriodo();
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, entrada.p_PagosPeriodo, PagoPeriodoEntrada.class);
		HashMap<String, Cabecera> cabeceraHash = new HashMap<String, Cabecera>();
		List<Cabecera> lstCabeceras = obtenerDatosCabeceraArchivo(con, entrada);				
		
		for (Cabecera cabecera : lstCabeceras) {
			String keyCabecera = "";

			keyCabecera = cabecera.KRutCtraparte1 + "_" + cabecera.KIdContrato + "_" + cabecera.KFechaContrato + "_" + cabecera.KIdOpeEstruc + "_" + cabecera.kProducto + "_" + cabecera.kPeriodoComercial;

			if (!cabeceraHash.containsKey(keyCabecera)) {
				cabeceraHash.put(keyCabecera, cabecera);
			}
		}
		
		List<PagoPeriodo> lstPagosPeriodo = new ArrayList<PagoPeriodo>();
		
		for (PagoPeriodoEntrada pagoPeriodoEntrada : entrada.p_PagosPeriodo) {				
			String keyCabeceraEntrada = pagoPeriodoEntrada.p_RutContraparte + "_" + pagoPeriodoEntrada.p_IdContrato + "_" + pagoPeriodoEntrada.p_FechaContrato + "_" + pagoPeriodoEntrada.p_IdOperacion + "_" + pagoPeriodoEntrada.p_Producto + "_" + pagoPeriodoEntrada.p_PeriodoComercial;
			Cabecera cabeceraExiste = cabeceraHash.get(keyCabeceraEntrada);
				
			if(cabeceraExiste == null) {
				out.P_OUT_CABECERANOEXISTE++;
				continue;
			} else {	
				PagoPeriodo pagoPeriodo = new PagoPeriodo();
				pagoPeriodo.IdDocumento = cabeceraExiste.IdDocumento;
				pagoPeriodo.VboUsuario = cabeceraExiste.VboUsuario;
				pagoPeriodo.Observaciones = cabeceraExiste.Observaciones;
				pagoPeriodo.FuenteDatos = "EXTERNA";
				pagoPeriodo.KRutCtraparte1 = cabeceraExiste.KRutCtraparte1;
				pagoPeriodo.KIdContrato = cabeceraExiste.KIdContrato;
				pagoPeriodo.KFechaContrato = cabeceraExiste.KFechaContrato;
				pagoPeriodo.HoraContrato = cabeceraExiste.HoraContrato;
				pagoPeriodo.KIdOpeEstruc = cabeceraExiste.KIdOpeEstruc;
				pagoPeriodo.kProducto = cabeceraExiste.kProducto;
				pagoPeriodo.kPeriodoComercial = cabeceraExiste.kPeriodoComercial;
				pagoPeriodo.Seccion = 5L;
				pagoPeriodo.KS5_CorrPago = pagoPeriodoEntrada.p_CorrelativoPago;
				pagoPeriodo.KS5_IdPago = pagoPeriodoEntrada.p_IdPago;
				pagoPeriodo.S5_FecPago = pagoPeriodoEntrada.p_FechaPago;
				pagoPeriodo.S5_MontoPago = pagoPeriodoEntrada.p_MontoPago;
				pagoPeriodo.S5_TipoPago = pagoPeriodoEntrada.p_TipoPago;
				pagoPeriodo.UsrUltMod = codigoUsuario;
				pagoPeriodo.FecUltMod = fechaSistema;
				pagoPeriodo.Estado = "V";
				pagoPeriodo.Rectifica = cabeceraExiste.Rectifica;
				
				if(validarRepetido(con, pagoPeriodo)) {
					out.P_OUT_REPETIDOS++;
				} else {
					lstPagosPeriodo.add(pagoPeriodo);
					out.P_OUT_INSERTADOS++;
				}
			}
		}
		
		if(lstPagosPeriodo.size() > 0) {
			sql.batch(con, SQL_INSERT_PAGO_PERIODO, lstPagosPeriodo, PagoPeriodo.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPagosPeriodo.cargarPagosPeriodoArchivo", fin - inicio);
		return out;
	}
	
	private boolean validarRepetido(Connection con, PagoPeriodo pagoPeriodo) throws Exception {
		logger.debug("INICIO::CrearPagosPeriodo.validarRepetido");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("KRutCtraparte1", pagoPeriodo.KRutCtraparte1);
		parametros.put("KIdContrato", pagoPeriodo.KIdContrato);
		parametros.put("KFechaContrato", pagoPeriodo.KFechaContrato);
		parametros.put("KIdOpeEstruc", pagoPeriodo.KIdOpeEstruc);
		parametros.put("kProducto", pagoPeriodo.kProducto);
		parametros.put("kPeriodoComercial", pagoPeriodo.kPeriodoComercial);
		parametros.put("KS5_IdPago", pagoPeriodo.KS5_IdPago);
		parametros.put("KS5_CorrPago", pagoPeriodo.KS5_CorrPago);
		SQL sql = new SQL();
		Long contador = sql.countQuery(con, SQL_SELECT_PAGO_PERIODO_BY_ID_PAGO, parametros);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPagosPeriodo.validarRepetido", fin - inicio);
		return contador > 0;
	}

	private Cabecera obtenerDatosCabeceraManual(Connection con, List<PagoPeriodoEntrada> lstPagosPeriodo) throws Exception {
		logger.debug("INICIO::CrearPagosPeriodo.obtenerDatosCabeceraManual");
		long inicio = System.currentTimeMillis();
		PagoPeriodoEntrada pagoPeriodo = lstPagosPeriodo.parallelStream().findFirst().orElse(new PagoPeriodoEntrada());
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("KRutCtraparte1", pagoPeriodo.p_RutContraparte);
		parametros.put("KIdContrato", pagoPeriodo.p_IdContrato);
		parametros.put("KFechaContrato", pagoPeriodo.p_FechaContrato);
		parametros.put("KIdOpeEstruc", pagoPeriodo.p_IdOperacion);
		parametros.put("kProducto", pagoPeriodo.p_Producto);
		parametros.put("kPeriodoComercial", pagoPeriodo.p_PeriodoComercial);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_MANUAL, parametros, Cabecera.class);
		Cabecera cabecera = lstCabecera.parallelStream().findFirst().orElse(new Cabecera());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPagosPeriodo.obtenerDatosCabeceraManual", fin - inicio);
		return cabecera;
	}
	
	private List<Cabecera> obtenerDatosCabeceraArchivo(Connection con, EntradaCrearPagosPeriodo entrada) throws Exception {
		logger.debug("INICIO::CrearPagosPeriodo.obtenerDatosCabeceraArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_ARCHIVO, parametros, Cabecera.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPagosPeriodo.obtenerDatosCabeceraArchivo", fin - inicio);
		return lstCabecera;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------

	private void agregarLog(Connection con, EntradaCrearPagosPeriodo entrada, List<RegistroRectifica> registroRectifica, List<RegistroCamposNuevo> registroCamposNuevo) throws Exception {
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
			
			try (Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties")) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String jsonManual = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"NOR\",\"p_TipoCarga\":\"MANUAL\",\"p_PagosPeriodo\":[{\"p_RutContraparte\":\"90947000-1\",\"p_IdContrato\":\"479512\",\"p_FechaContrato\":\"2023-10-03\",\"p_IdOperacion\":\"0\",\"p_Producto\":\"FWD\",\"p_PeriodoComercial\":\"2023-10-03\",\"p_CorrelativoPago\":1,\"p_IdPago\":\"R\",\"p_FechaPago\":\"2024-02-16\",\"p_MontoPago\":1,\"p_TipoPago\":\"C\",\"p_CodMoneda\":\"USD\"}]}";
				EntradaCrearPagosPeriodo Objin = gson.fromJson(jsonManual, EntradaCrearPagosPeriodo.class);
				
				CrearPagosPeriodo demo = new CrearPagosPeriodo();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
		 		
	 			try {
	 				SalidaCrearPagosPeriodo ObjOut = demo.logica(con, Objin);
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
