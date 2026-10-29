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

public class CrearGarantia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearGarantia.class);
	
	private final String SQL_INSERT_GARANTIA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearGarantia_Insert_Garantia.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearGarantia_Insert_Tabla_Temporal_Garantia.sql");
	private final String SQL_SELECT_DATOS_CABECERA_MANUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/DeclaracionesDJ1_Select_Datos_Cabecera.sql");
	private final String SQL_SELECT_DATOS_CABECERA_ARCHIVO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearGarantia_Select_Datos_Cabecera_Garantia_Archivo.sql");
	private final String SQL_SELECT_GARANTIA_BY_ID_GARANTIA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/CrearGarantia_Select_Garantia_By_Id_Garantia.sql");
	private final String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	
	public static class EntradaCrearGarantia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_TipoCarga;
		public List<GarantiaEntrada> p_Garantias;
		
		public EntradaCrearGarantia() {
			this.p_Garantias =  new ArrayList<GarantiaEntrada>();
		}
	}
	
	public static class GarantiaEntrada extends BcsPojo{
		public Long p_Seccion;
		public String p_RutContraparte;
		public String p_IdContrato;
		public String p_FechaContrato;
		public String p_IdOperacion;
		public String p_Producto;
		public String p_PeriodoComercial;
		public String p_TipoGarantia;
		public String p_MonedaValorizacion;
		public String p_IdGarantia;  
		public BigDecimal p_MontoUmbral;   
		public BigDecimal p_ValorGarantia;   
	}
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}

	public static class RegistroCamposNuevo extends BcsPojo {
		public String		IdContrato;
		public String		S6_TipoGar;
		public String		S6_MonVal;
		public String		KS6_IdGar;
		public BigDecimal	S6_MontoUmb;
		public BigDecimal	S6_ValGtia;
	}
	
	public static class SalidaCrearGarantia extends BcsPojo{
		public Long P_OUT_INSERTADOS;
		public Long P_OUT_CABECERANOEXISTE;
		public Long P_OUT_REPETIDOS;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearGarantia() {
			this.P_OUT_INSERTADOS = 0L;
			this.P_OUT_CABECERANOEXISTE = 0L;
			this.P_OUT_REPETIDOS = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Garantia extends BcsPojo{
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
		public String S6_TipoGar;
		public String S6_MonVal;
		public String KS6_IdGar;
		public BigDecimal S6_MontoUmb;
		public BigDecimal S6_ValGtia;
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
		EntradaCrearGarantia jin = gson.fromJson(in.toString(), EntradaCrearGarantia.class);
		SalidaCrearGarantia out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearGarantia logica(Connection con, EntradaCrearGarantia entrada) throws Exception {
		logger.info("INICIO::CrearGarantia");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearGarantia out = new SalidaCrearGarantia();
	
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String codigoUsuario = Utils.getCodigoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		// Selecciona marca Rectifica de los contratos
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(GarantiaEntrada r : entrada.p_Garantias) {
			if (!lstIdContrato.contains(Long.parseLong(r.p_IdContrato))) {
				lstIdContrato.add(Long.parseLong(r.p_IdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);
		
		// armar listado de objeto con nombre de los campos y valores a crear
		List<RegistroCamposNuevo> registroCamposNuevo = entrada.p_Garantias.parallelStream().map(solicitud->{
			RegistroCamposNuevo registroNuevo = new RegistroCamposNuevo();
			registroNuevo.IdContrato = solicitud.p_IdContrato;
			registroNuevo.S6_TipoGar = solicitud.p_TipoGarantia;
			registroNuevo.S6_MonVal = solicitud.p_MonedaValorizacion;
			registroNuevo.KS6_IdGar = solicitud.p_IdGarantia;
			registroNuevo.S6_MontoUmb = solicitud.p_MontoUmbral;
			registroNuevo.S6_ValGtia = solicitud.p_ValorGarantia;
			return registroNuevo;
		}).collect(Collectors.toList());
		
		if(entrada.p_Garantias.size() > 0) {
			if("ARCHIVO".equals(entrada.p_TipoCarga)) {
				out = cargarGarantiasArchivo(con, entrada, fechaSistema, codigoUsuario); 
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Garantias insertadas: " + out.P_OUT_INSERTADOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Garantias repetidas: " + out.P_OUT_REPETIDOS));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Cabecera no existe: " + out.P_OUT_CABECERANOEXISTE));
				
			}
			
			if("MANUAL".equals(entrada.p_TipoCarga)) {
				out = cargarGarantiasManual(con, entrada, fechaSistema, codigoUsuario);
				if(out.P_OUT_REPETIDOS > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR", "Id Garantia ya existe"));
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"RESUMEN", "Garantia insertada"));
				}
			}
			
			// Registra LOG si el contrato estaba marcado para Rectificar
			agregarLog(con,entrada,registroRectifica,registroCamposNuevo);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearGarantia", finServicio - inicioServicio);
		return out;
	}
	
	private SalidaCrearGarantia cargarGarantiasManual(Connection con, EntradaCrearGarantia entrada, String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearGarantia.cargarGarantiasManual");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearGarantia out = new SalidaCrearGarantia();
		Cabecera cabecera = obtenerDatosCabeceraManual(con, entrada.p_Garantias);
		
		List<Garantia> lstGarantias = new ArrayList<Garantia>();

		for(GarantiaEntrada garantiaEntrada: entrada.p_Garantias) {
			Garantia garantia = new Garantia();
			garantia.IdDocumento = cabecera.IdDocumento;
			garantia.VboUsuario = cabecera.VboUsuario;
			garantia.Observaciones = cabecera.Observaciones;
			garantia.FuenteDatos = "MANTENEDOR";
			garantia.KRutCtraparte1 = cabecera.KRutCtraparte1;
			garantia.KIdContrato = cabecera.KIdContrato;
			garantia.KFechaContrato = cabecera.KFechaContrato;
			garantia.HoraContrato = cabecera.HoraContrato;
			garantia.KIdOpeEstruc = cabecera.KIdOpeEstruc;
			garantia.kProducto = cabecera.kProducto;
			garantia.kPeriodoComercial = cabecera.kPeriodoComercial;
			garantia.Seccion = 6L;
			garantia.S6_TipoGar = garantiaEntrada.p_TipoGarantia;
			garantia.S6_MonVal = garantiaEntrada.p_MonedaValorizacion;
			garantia.KS6_IdGar = garantiaEntrada.p_IdGarantia;
			garantia.S6_MontoUmb = garantiaEntrada.p_MontoUmbral;
			garantia.S6_ValGtia = garantiaEntrada.p_ValorGarantia;
			garantia.UsrUltMod = codigoUsuario;
			garantia.FecUltMod = fechaSistema;
			garantia.Estado = "V";
			garantia.Rectifica = cabecera.Rectifica;
	
			if(validarRepetido(con, garantia)) {
				out.P_OUT_REPETIDOS++;
			} else {
				lstGarantias.add(garantia);
				out.P_OUT_INSERTADOS++;
			}
		}
		
		if(lstGarantias.size() > 0) {
			sql.batch(con, SQL_INSERT_GARANTIA, lstGarantias, Garantia.class);
		}
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearGarantia.cargarGarantiasManual", fin - inicio);
		return out;
	}



	private SalidaCrearGarantia cargarGarantiasArchivo(Connection con, EntradaCrearGarantia entrada, String fechaSistema, String codigoUsuario) throws Exception {
		logger.info("INICIO::CrearGarantia.cargarGarantiasArchivo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaCrearGarantia out = new SalidaCrearGarantia();
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, entrada.p_Garantias, GarantiaEntrada.class);
		HashMap<String, Cabecera> cabeceraHash = new HashMap<String, Cabecera>();
		List<Cabecera> lstCabeceras = obtenerDatosCabeceraArchivo(con, entrada);
		
		for (Cabecera cabecera : lstCabeceras) {
			String keyCabecera = "";

			keyCabecera = cabecera.KRutCtraparte1 + "_" + cabecera.KIdContrato + "_" + cabecera.KFechaContrato + "_" + cabecera.KIdOpeEstruc + "_" + cabecera.kProducto + "_" + cabecera.kPeriodoComercial;

			if (!cabeceraHash.containsKey(keyCabecera)) {
				cabeceraHash.put(keyCabecera, cabecera);
			}
		}

		List<Garantia> lstGarantias = new ArrayList<Garantia>();
		
		for(GarantiaEntrada garantiaEntrada: entrada.p_Garantias) {
			String  keyCabeceraEntrada = garantiaEntrada.p_RutContraparte + "_" + garantiaEntrada.p_IdContrato + "_" + garantiaEntrada.p_FechaContrato + "_" + garantiaEntrada.p_IdOperacion + "_" + garantiaEntrada.p_Producto + "_" + garantiaEntrada.p_PeriodoComercial;
			Cabecera cabeceraExiste = cabeceraHash.get(keyCabeceraEntrada);
			
			if(cabeceraExiste == null) {
				out.P_OUT_CABECERANOEXISTE++;
				continue;
			} else {
				Garantia garantia = new Garantia();
				garantia.IdDocumento = cabeceraExiste.IdDocumento;
				garantia.VboUsuario = cabeceraExiste.VboUsuario;
				garantia.Observaciones = cabeceraExiste.Observaciones;
				garantia.FuenteDatos = "EXTERNA";
				garantia.KRutCtraparte1 = cabeceraExiste.KRutCtraparte1;
				garantia.KIdContrato = cabeceraExiste.KIdContrato;
				garantia.KFechaContrato = cabeceraExiste.KFechaContrato;
				garantia.HoraContrato = cabeceraExiste.HoraContrato;
				garantia.KIdOpeEstruc = cabeceraExiste.KIdOpeEstruc;
				garantia.kProducto = cabeceraExiste.kProducto;
				garantia.kPeriodoComercial = cabeceraExiste.kPeriodoComercial;
				garantia.Seccion = 6L;
				garantia.S6_TipoGar = garantiaEntrada.p_TipoGarantia;
				garantia.S6_MonVal = garantiaEntrada.p_MonedaValorizacion;
				garantia.KS6_IdGar = garantiaEntrada.p_IdGarantia;
				garantia.S6_MontoUmb = garantiaEntrada.p_MontoUmbral;
				garantia.S6_ValGtia = garantiaEntrada.p_ValorGarantia;
				garantia.UsrUltMod = codigoUsuario;
				garantia.FecUltMod = fechaSistema;
				garantia.Estado = "V";
				garantia.Rectifica = cabeceraExiste.Rectifica;
				
				if(validarRepetido(con, garantia)) {
					out.P_OUT_REPETIDOS++;
				} else {
					lstGarantias.add(garantia);
					out.P_OUT_INSERTADOS++;
				}
			}
		}
		
		if(lstGarantias.size() > 0) {
			sql.batch(con, SQL_INSERT_GARANTIA, lstGarantias, Garantia.class);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearGarantia.cargarGarantiasArchivo", fin - inicio);
		return out;
	}
	
	private boolean validarRepetido(Connection con, Garantia garantia) throws SQLException {
		logger.debug("INICIO::CrearGarantia.validarRepetido");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("KRutCtraparte1", garantia.KRutCtraparte1);
		parametros.put("KIdContrato", garantia.KIdContrato);
		parametros.put("KFechaContrato", garantia.KFechaContrato);
		parametros.put("KIdOpeEstruc", garantia.KIdOpeEstruc);
		parametros.put("kProducto", garantia.kProducto);
		parametros.put("kPeriodoComercial", garantia.kPeriodoComercial);
		parametros.put("KS6_IdGar", garantia.KS6_IdGar);
		SQL sql = new SQL();
		Long contador = sql.countQuery(con, SQL_SELECT_GARANTIA_BY_ID_GARANTIA, parametros);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearGarantia.validarRepetido", fin - inicio);
		return contador > 0;
	}
	
	private Cabecera obtenerDatosCabeceraManual(Connection con, List<GarantiaEntrada> lstGarantias) throws Exception {
		logger.debug("INICIO::CrearGarantia.obtenerDatosCabeceraManual");
		long inicio = System.currentTimeMillis();
		GarantiaEntrada garantia = lstGarantias.parallelStream().findFirst().orElse(new GarantiaEntrada()); 
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("KRutCtraparte1", garantia.p_RutContraparte);
		parametros.put("KIdContrato", garantia.p_IdContrato);
		parametros.put("KFechaContrato", garantia.p_FechaContrato);
		parametros.put("KIdOpeEstruc", garantia.p_IdOperacion);
		parametros.put("kProducto", garantia.p_Producto);
		parametros.put("kPeriodoComercial", garantia.p_PeriodoComercial);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_MANUAL, parametros, Cabecera.class);
		Cabecera cabecera = lstCabecera.parallelStream().findFirst().orElse(new Cabecera());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearGarantia.obtenerDatosCabeceraManual", fin - inicio);
		return cabecera;
	}
	
	private List<Cabecera> obtenerDatosCabeceraArchivo(Connection con, EntradaCrearGarantia entrada) throws Exception {
		logger.debug("INICIO::CrearGarantia.obtenerDatosCabeceraArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		SQL sql = new SQL();
		List<Cabecera> lstCabecera = sql.query(con, SQL_SELECT_DATOS_CABECERA_ARCHIVO, parametros, Cabecera.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearGarantia.obtenerDatosCabeceraArchivo", fin - inicio);
		return lstCabecera;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------

	private void agregarLog(Connection con, EntradaCrearGarantia entrada, List<RegistroRectifica> registroRectifica, List<RegistroCamposNuevo> registroCamposNuevo) throws Exception {
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
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"NOR\",\"p_TipoCarga\":\"MANUAL\",\"p_Garantias\":[{\"p_RutContraparte\":\"90947000-1\",\"p_IdContrato\":\"20694163\",\"p_FechaContrato\":\"2023-07-05\",\"p_IdOperacion\":\"0\",\"p_Producto\":\"FWD\",\"p_PeriodoComercial\":\"2023-07-19\",\"p_TipoGarantia\":\"R\",\"p_MonedaValorizacion\":\"EUR\",\"p_IdGarantia\":\"7\",\"p_MontoUmbral\":7,\"p_ValorGarantia\":7}]}";
						
				EntradaCrearGarantia Objin = gson.fromJson(json, EntradaCrearGarantia.class);
				
				CrearGarantia demo = new CrearGarantia();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
		 		
	 			try {
	 				SalidaCrearGarantia ObjOut = demo.logica(con, Objin);
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
