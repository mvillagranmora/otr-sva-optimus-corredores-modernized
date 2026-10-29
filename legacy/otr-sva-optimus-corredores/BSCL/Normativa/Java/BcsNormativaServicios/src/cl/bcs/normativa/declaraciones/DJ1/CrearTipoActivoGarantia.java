package cl.bcs.normativa.declaraciones.DJ1;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

public class CrearTipoActivoGarantia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearTipoActivoGarantia.class);
	
	private final String SQL_INSERT_TIPO_ACTIVO_GARANTIA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearTipoActivoGarantia_Insert_TipoActivoGarantia.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearTipoActivoGarantia_Insert_Tabla_Temporal_Tipo_Activo_Garantia.sql");
	private final String SQL_SELECT_DATOS_CABECERA_MANUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/DeclaracionesDJ1_Select_Datos_Cabecera.sql");
	private final String SQL_SELECT_DATOS_CABECERA_ARCHIVO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearTipoActivoGarantia_Select_Datos_Cabecera_Tipo_Activo_Garantia_Archivo.sql");
	private final String SQL_SELECT_TIPO_ACTIVO_GARANTIA_BY_ID_GARANTIA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearTipoActivoGarantia_Select_Tipo_Activo_Garantia_By_Id_Garantia.sql");
	private final String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	
	public static class EntradaCrearTipoActivoGarantia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_TipoCarga;
		public List<TipoActivoGarantiaEntrada> p_TipoActivoGarantias;
		
		public EntradaCrearTipoActivoGarantia() {
			this.p_TipoActivoGarantias =  new ArrayList<TipoActivoGarantiaEntrada>();
		}
	}
	
	public static class TipoActivoGarantiaEntrada{
		public Long p_Seccion;
		public String p_RutContraparte;
		public String p_IdContrato;
		public String p_FechaContrato;
		public String p_IdOperacion;
		public String p_Producto;
		public String p_PeriodoComercial;
		public String p_TipoGarantia;
		public String p_IdGarantia;  
		public String p_TipoActivoGarantia;   
		public BigDecimal p_PorcentajeGarantia;   
	}
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}

	public static class RegistroCamposNuevo extends BcsPojo {
		public String		IdContrato;
		public String		S7_TipoGar;
		public String		KS7_IdGar;
		public String		S7_TipoActGar;
		public BigDecimal	S7_PorcGar;
	}
	
	public static class SalidaCrearTipoActivoGarantia extends BcsPojo{
		public Long P_OUT_INSERTADOS;
		public Long P_OUT_CABECERANOEXISTE;
		public Long P_OUT_REPETIDOS;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearTipoActivoGarantia() {
			this.P_OUT_INSERTADOS = 0L;
			this.P_OUT_CABECERANOEXISTE = 0L;
			this.P_OUT_REPETIDOS = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class TipoActivoGarantia extends BcsPojo{
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
		public String S7_TipoGar;
		public String KS7_IdGar;
		public String S7_TipoActGar;
		public BigDecimal S7_PorcGar;
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
		EntradaCrearTipoActivoGarantia jin = gson.fromJson(in.toString(), EntradaCrearTipoActivoGarantia.class);
		SalidaCrearTipoActivoGarantia out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearTipoActivoGarantia logica(Connection con, EntradaCrearTipoActivoGarantia entrada) throws Exception {
		logger.info("INICIO::CrearTipoActivoGarantia");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearTipoActivoGarantia out = new SalidaCrearTipoActivoGarantia();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String codigoUsuario = Utils.getCodigoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		// Selecciona marca Rectifica de los contratos
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(TipoActivoGarantiaEntrada r : entrada.p_TipoActivoGarantias) {
			if (!lstIdContrato.contains(Long.parseLong(r.p_IdContrato))) {
				lstIdContrato.add(Long.parseLong(r.p_IdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);

		// armar listado de objeto con nombre de los campos y valores a crear
		List<RegistroCamposNuevo> registroCamposNuevo = entrada.p_TipoActivoGarantias.parallelStream().map(solicitud->{
			RegistroCamposNuevo registroNuevo = new RegistroCamposNuevo();
			registroNuevo.IdContrato = solicitud.p_IdContrato;
			registroNuevo.S7_TipoGar = solicitud.p_TipoGarantia;
			registroNuevo.KS7_IdGar = solicitud.p_IdGarantia;
			registroNuevo.S7_TipoActGar = solicitud.p_TipoActivoGarantia;
			registroNuevo.S7_PorcGar = solicitud.p_PorcentajeGarantia;
			return registroNuevo;
		}).collect(Collectors.toList());
		
		if(entrada.p_TipoActivoGarantias.size() > 0) {
			if("ARCHIVO".equals(entrada.p_TipoCarga)) {
				out = cargarTipoActivoGarantiaArchivo(con, entrada, fechaSistema, codigoUsuario); 
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Tipo Activo Garantias insertados: " + out.P_OUT_INSERTADOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Tipo Activo Garantias repetidos: " + out.P_OUT_REPETIDOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Cabecera no existe: " + out.P_OUT_CABECERANOEXISTE));
			}
			
			if("MANUAL".equals(entrada.p_TipoCarga)) {
				out = cargarTipoActivoGarantiaManual(con, entrada, fechaSistema, codigoUsuario);
				if(out.P_OUT_REPETIDOS > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR", "Id Garantia ya existe"));
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Tipo Activo Garantia insertada"));
				}
			}
			
			// Registra LOG si el contrato estaba marcado para Rectificar
			agregarLog(con,entrada,registroRectifica,registroCamposNuevo);
		}
				
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearTipoActivoGarantia", finServicio - inicioServicio);
		return out;
	}
	
	private SalidaCrearTipoActivoGarantia cargarTipoActivoGarantiaManual(Connection con, EntradaCrearTipoActivoGarantia entrada, String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearTipoActivoGarantia.cargarTipoActivoGarantiaManual");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearTipoActivoGarantia out = new SalidaCrearTipoActivoGarantia();
		Cabecera cabecera = obtenerDatosCabeceraManual(con, entrada.p_TipoActivoGarantias);
		
		List<TipoActivoGarantia> lstTipoActivoGarantias = new ArrayList<TipoActivoGarantia>();
		
		for(TipoActivoGarantiaEntrada tipoActivoGarantiaEntrada : entrada.p_TipoActivoGarantias) {
			TipoActivoGarantia tipoActivoGarantia = new TipoActivoGarantia();
			tipoActivoGarantia.IdDocumento = cabecera.IdDocumento;
			tipoActivoGarantia.VboUsuario = cabecera.VboUsuario;
			tipoActivoGarantia.Observaciones = cabecera.Observaciones;
			tipoActivoGarantia.FuenteDatos = "MANTENEDOR";
			tipoActivoGarantia.KRutCtraparte1 = cabecera.KRutCtraparte1;
			tipoActivoGarantia.KIdContrato = cabecera.KIdContrato;
			tipoActivoGarantia.KFechaContrato = cabecera.KFechaContrato;
			tipoActivoGarantia.HoraContrato = cabecera.HoraContrato;
			tipoActivoGarantia.KIdOpeEstruc = cabecera.KIdOpeEstruc;
			tipoActivoGarantia.kProducto = cabecera.kProducto;
			tipoActivoGarantia.kPeriodoComercial = cabecera.kPeriodoComercial;
			tipoActivoGarantia.Seccion = 7L;
			tipoActivoGarantia.S7_TipoGar = tipoActivoGarantiaEntrada.p_TipoGarantia;
			tipoActivoGarantia.KS7_IdGar = tipoActivoGarantiaEntrada.p_IdGarantia;
			tipoActivoGarantia.S7_TipoActGar = tipoActivoGarantiaEntrada.p_TipoActivoGarantia;
			tipoActivoGarantia.S7_PorcGar = tipoActivoGarantiaEntrada.p_PorcentajeGarantia;
			tipoActivoGarantia.UsrUltMod = codigoUsuario;
			tipoActivoGarantia.FecUltMod = fechaSistema;
			tipoActivoGarantia.Estado = "V";
			tipoActivoGarantia.Rectifica = cabecera.Rectifica;

			if(validarRepetido(con, tipoActivoGarantia)) {
				out.P_OUT_REPETIDOS++;
			} else {
				lstTipoActivoGarantias.add(tipoActivoGarantia);
				out.P_OUT_INSERTADOS++;
			}
		}
		
		if(lstTipoActivoGarantias.size() > 0) {
			sql.batch(con, SQL_INSERT_TIPO_ACTIVO_GARANTIA, lstTipoActivoGarantias, TipoActivoGarantia.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTipoActivoGarantia.cargarTipoActivoGarantiaManual", fin - inicio);
		return out;
	}

	private SalidaCrearTipoActivoGarantia cargarTipoActivoGarantiaArchivo(Connection con, EntradaCrearTipoActivoGarantia entrada, String fechaSistema, String codigoUsuario) throws SQLException {
		logger.info("INICIO::CrearTipoActivoGarantia.cargarTipoActivoGarantiaArchivo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearTipoActivoGarantia out = new SalidaCrearTipoActivoGarantia();
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, entrada.p_TipoActivoGarantias, TipoActivoGarantiaEntrada.class);
		HashMap<String, Cabecera> cabeceraHash = new HashMap<String, Cabecera>();
		List<Cabecera> lstCabeceras = obtenerDatosCabeceraArchivo(con, entrada);				
		
		for (Cabecera cabecera : lstCabeceras) {
			String keyCabecera = "";

			keyCabecera = cabecera.KRutCtraparte1 + "_" + cabecera.KIdContrato + "_" + cabecera.KFechaContrato + "_" + cabecera.KIdOpeEstruc + "_" + cabecera.kProducto + "_" + cabecera.kPeriodoComercial;

			if (!cabeceraHash.containsKey(keyCabecera)) {
				cabeceraHash.put(keyCabecera, cabecera);
			}
		}
		
		List<TipoActivoGarantia> lstTipoActivoGarantia = new ArrayList<TipoActivoGarantia>();
		
		for(TipoActivoGarantiaEntrada tipoActivoGarantiaEntrada : entrada.p_TipoActivoGarantias) {
			String keyCabeceraEntrada = tipoActivoGarantiaEntrada.p_RutContraparte + "_" + tipoActivoGarantiaEntrada.p_IdContrato + "_" + tipoActivoGarantiaEntrada.p_FechaContrato + "_" + tipoActivoGarantiaEntrada.p_IdOperacion + "_" + tipoActivoGarantiaEntrada.p_Producto + "_" + tipoActivoGarantiaEntrada.p_PeriodoComercial;
			Cabecera cabeceraExiste = cabeceraHash.get(keyCabeceraEntrada);
			
			if(cabeceraExiste == null) {
				out.P_OUT_CABECERANOEXISTE++;
				continue;
			} else {
				TipoActivoGarantia tipoActivoGarantia = new TipoActivoGarantia();
				tipoActivoGarantia.IdDocumento = cabeceraExiste.IdDocumento;
				tipoActivoGarantia.VboUsuario = cabeceraExiste.VboUsuario;
				tipoActivoGarantia.Observaciones = cabeceraExiste.Observaciones;
				tipoActivoGarantia.FuenteDatos = "EXTERNA";
				tipoActivoGarantia.KRutCtraparte1 = cabeceraExiste.KRutCtraparte1;
				tipoActivoGarantia.KIdContrato = cabeceraExiste.KIdContrato;
				tipoActivoGarantia.KFechaContrato = cabeceraExiste.KFechaContrato;
				tipoActivoGarantia.HoraContrato = cabeceraExiste.HoraContrato;
				tipoActivoGarantia.KIdOpeEstruc = cabeceraExiste.KIdOpeEstruc;
				tipoActivoGarantia.kProducto = cabeceraExiste.kProducto;
				tipoActivoGarantia.kPeriodoComercial = cabeceraExiste.kPeriodoComercial;
				tipoActivoGarantia.Seccion = 7L;
				tipoActivoGarantia.S7_TipoGar = tipoActivoGarantiaEntrada.p_TipoGarantia;
				tipoActivoGarantia.KS7_IdGar = tipoActivoGarantiaEntrada.p_IdGarantia;
				tipoActivoGarantia.S7_TipoActGar = tipoActivoGarantiaEntrada.p_TipoActivoGarantia;
				tipoActivoGarantia.S7_PorcGar = tipoActivoGarantiaEntrada.p_PorcentajeGarantia;
				tipoActivoGarantia.UsrUltMod = codigoUsuario;
				tipoActivoGarantia.FecUltMod = fechaSistema;
				tipoActivoGarantia.Estado = "V";
				tipoActivoGarantia.Rectifica = cabeceraExiste.Rectifica;
		
				if(validarRepetido(con, tipoActivoGarantia)) {
					out.P_OUT_REPETIDOS++;
				} else {
					lstTipoActivoGarantia.add(tipoActivoGarantia);
					out.P_OUT_INSERTADOS++;
				}
			}
		}
		
		if(lstTipoActivoGarantia.size() > 0) {
			sql.batch(con, SQL_INSERT_TIPO_ACTIVO_GARANTIA, lstTipoActivoGarantia, TipoActivoGarantia.class);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTipoActivoGarantia.cargarTipoActivoGarantiaArchivo", fin - inicio);
		return out;
	}
	
	private boolean validarRepetido(Connection con, TipoActivoGarantia tipoActivoGarantia) throws SQLException {
		logger.debug("INICIO::CrearTipoActivoGarantia.validarRepetido");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("KRutCtraparte1", tipoActivoGarantia.KRutCtraparte1);
		parametros.put("KIdContrato", tipoActivoGarantia.KIdContrato);
		parametros.put("KFechaContrato", tipoActivoGarantia.KFechaContrato);
		parametros.put("KIdOpeEstruc", tipoActivoGarantia.KIdOpeEstruc);
		parametros.put("kProducto", tipoActivoGarantia.kProducto);
		parametros.put("kPeriodoComercial", tipoActivoGarantia.kPeriodoComercial);
		parametros.put("KS7_IdGar", tipoActivoGarantia.KS7_IdGar);
		SQL sql = new SQL();
		Long contador = sql.countQuery(con, SQL_SELECT_TIPO_ACTIVO_GARANTIA_BY_ID_GARANTIA, parametros);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTipoActivoGarantia.validarRepetido", fin - inicio);
		return contador > 0;
	}

	private Cabecera obtenerDatosCabeceraManual(Connection con, List<TipoActivoGarantiaEntrada> lstTipoActivoGarantia) throws Exception {
		logger.debug("INICIO::CrearTipoActivoGarantia.obtenerDatosCabeceraManual");
		long inicio = System.currentTimeMillis();
		TipoActivoGarantiaEntrada tipoActivoGarantia = lstTipoActivoGarantia.parallelStream().findFirst().orElse(new TipoActivoGarantiaEntrada());
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("KRutCtraparte1", tipoActivoGarantia.p_RutContraparte);
		parametros.put("KIdContrato", tipoActivoGarantia.p_IdContrato);
		parametros.put("KFechaContrato", tipoActivoGarantia.p_FechaContrato);
		parametros.put("KIdOpeEstruc", tipoActivoGarantia.p_IdOperacion);
		parametros.put("kProducto", tipoActivoGarantia.p_Producto);
		parametros.put("kPeriodoComercial", tipoActivoGarantia.p_PeriodoComercial);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_MANUAL, parametros, Cabecera.class);
		Cabecera cabecera = lstCabecera.parallelStream().findFirst().orElse(new Cabecera());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTipoActivoGarantia.obtenerDatosCabeceraManual", fin - inicio);
		return cabecera;
	}
	
	private List<Cabecera> obtenerDatosCabeceraArchivo(Connection con, EntradaCrearTipoActivoGarantia entrada) throws SQLException {
		logger.debug("INICIO::CrearTipoActivoGarantia.obtenerDatosCabeceraArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_ARCHIVO, parametros, Cabecera.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTipoActivoGarantia.obtenerDatosCabeceraArchivo", fin - inicio);
		return lstCabecera;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------

	private void agregarLog(Connection con, EntradaCrearTipoActivoGarantia entrada, List<RegistroRectifica> registroRectifica, List<RegistroCamposNuevo> registroCamposNuevo) throws Exception {
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
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"NOR\",\"p_TipoCarga\":\"MANUAL\",\"p_TipoActivoGarantias\":[{\"p_RutContraparte\":\"90947000-1\",\"p_IdContrato\":\"20694163\",\"p_FechaContrato\":\"2023-07-05\",\"p_IdOperacion\":\"0\",\"p_Producto\":\"FWD\",\"p_PeriodoComercial\":\"2023-07-19\",\"p_TipoGarantia\":\"R\",\"p_IdGarantia\":\"7\",\"p_TipoActivoGarantia\":\"CASH\",\"p_PorcentajeGarantia\":7}]}";
						
				EntradaCrearTipoActivoGarantia Objin = gson.fromJson(json, EntradaCrearTipoActivoGarantia.class);
				
				CrearTipoActivoGarantia demo = new CrearTipoActivoGarantia();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
		 		
	 			try {
	 				SalidaCrearTipoActivoGarantia ObjOut = demo.logica(con, Objin);
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
