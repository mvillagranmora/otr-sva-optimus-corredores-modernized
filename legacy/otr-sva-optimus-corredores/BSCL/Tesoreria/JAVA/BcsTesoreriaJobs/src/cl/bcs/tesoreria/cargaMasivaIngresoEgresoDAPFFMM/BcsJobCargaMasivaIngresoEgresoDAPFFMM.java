package cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.fondomutuo.AdministracionLiquidez.FechaActual;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo;
import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM.EntradaCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM.SalidaCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class BcsJobCargaMasivaIngresoEgresoDAPFFMM extends BcsJob implements JavaCall {
	String IdTipoPrograma = "CARGA_MASIVA_INGRESO_EGRESO_DAP_FFMM";
	
	private final String SQL_SELECT_RANGO_HORARIO = loadResourceToString("cl/bcs/tesoreria/cargaMasivaIngresoEgresoDAPFFMM/sql/BcsJobCargaMasivaIngresoEgresoDAPFFMM_SelectRangoHorario.sql");
	private final String SQL_UPDATE_ID_PROGRAMA =  loadResourceToString("cl/bcs/tesoreria/cargaMasivaIngresoEgresoDAPFFMM/sql/BcsJobCargaMasivaIngresoEgresoDAPFFMM_UpdateSalidaPrograma.sql");
	private final String OBTENER_FECHA_ACTUAL = loadResourceToString("cl/bcs/tesoreria/cargaMasivaIngresoEgresoDAPFFMM/sql/BcsJobCargaMasivaIngresoEgresoDAPFFMM_ObtenerFechaActual.sql");
	private final String OBTENER_ESTADO_EJECUCION_PAUTA = loadResourceToString("cl/bcs/tesoreria/cargaMasivaIngresoEgresoDAPFFMM/sql/BcsJobCargaMasivaIngresoEgresoDAPFFMM_ObtenerEstadoEjecucionPauta.sql");
	
	private static final Logger logger = LogManager.getLogger(BcsJobCargaMasivaIngresoEgresoDAPFFMM.class);
	private static final String BASE_NOMBRE_ARCHIVO_SALIDA_ERRORES = "CDB_Errores_IngresoEgreso_";
	private static final String MODULO_ESTADO_CERRADO = "CER";
	
	public static class RangoHorario extends BcsPojo {
		public String HoraInicio;
		public String HoraFin;
		
	}

	public static class EstadoPauta extends BcsPojo {
		public String EstadoPauta;
		
		public EstadoPauta () {
			this.EstadoPauta = "PEN";
		}
	}
	
	public static class ActualizarPrograma extends BcsPojo {
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con, boolean debug) throws Exception {
		logger.info("INICIO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
		
		try {
			SQL sql = new SQL();
	    	HashMap<String,Object> params = new HashMap<>(2);
			
			EntradaCargaMasivaIngresoEgresoDAPFFMM entradaDAPFFMM = new EntradaCargaMasivaIngresoEgresoDAPFFMM();
			CargaMasivaIngresoEgresoDAPFFMM servicioDAPFFM = new CargaMasivaIngresoEgresoDAPFFMM();

			Long idPrograma = debug ? 324234343L : this.getJobId();
			Long idEmpresa = debug ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = debug ? 6408L : Long.parseLong(this.getParam("IdUsuario"));
			String tipoArchivoCarga = debug ? "DAP" : this.getParam("TipoArchivoCarga");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
			String horaProceso = FechaDB.ObtenerHoraSistemaLocal(con, idEmpresa);
			
			List<FechaActual> fechaActual = sql.query(con, OBTENER_FECHA_ACTUAL, params, FechaActual.class);
			
			String fechaCarga = debug ? "2025-10-03" : fechaActual.get(0).Fecha;

			String validacionesEstadoModulo = consultarEstadoModulos(con, idEmpresa, idUsuario, "TES");
			
			if(!validacionesEstadoModulo.isEmpty()) {
				logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se han encontrado errores: " + validacionesEstadoModulo);
    			String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, validacionesEstadoModulo, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return true;
			}
			
			List<Cabecera> cabecerasProcesadas = servicioDAPFFM.getCabeceras(con, tipoArchivoCarga, fechaCarga, "PRO");
			if(!cabecerasProcesadas.isEmpty()) {
				String mensajesSalida = Diccionario.getMensaje(con,idEmpresa,"UI_CARGA_MASIVA_DAPFFMM_YA_CARGADO", new HashMap<String, String>(0));
	    		logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se han encontrado errores: " + mensajesSalida);
    			String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensajesSalida, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return true;
			}
			
			List<Cabecera> cabeceras = servicioDAPFFM.getCabeceras(con, tipoArchivoCarga, fechaCarga, "PEN");
			
			if(cabeceras.isEmpty()) {
				String mensajesSalida = Diccionario.getMensaje(con,idEmpresa,"UI_CARGA_MASIVA_DAPFFMM_SIN_ARCHIVO_PARA_CARGAR", new HashMap<String, String>(0));
	    		logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se han encontrado errores: " + mensajesSalida);
    			String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensajesSalida, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return true;
	        	
			}
			
			InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
			ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
			InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
			fechasIn.fechaInicial = fechaCarga;
			fechasIn.numeroDias = 1L;
			inObtenerDiaAnterior.fechas.add(fechasIn);
			inObtenerDiaAnterior.idPais = idPaisSistema;
			inObtenerDiaAnterior.diaHabil = "S";
			
			OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
			
			String diaHabilAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
			params.put("FechaEjecucion", diaHabilAnterior);
			params.put("IdEmpresa", idEmpresa);
			
			EstadoPauta estadoEjecucionPautaDiaAnterior = sql.query(con, OBTENER_ESTADO_EJECUCION_PAUTA, params, EstadoPauta.class).stream().findFirst().orElse(new EstadoPauta());
			params.clear();
			
  			if("PEN".equals(estadoEjecucionPautaDiaAnterior.EstadoPauta)) {
  				String mensaje = "No se ha procesado la Pauta para el dia " + diaHabilAnterior;
				logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM -- No se ha procesado la Pauta para el dia " + diaHabilAnterior);
				String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, tipoArchivoCarga, mensaje);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
        		return true;
			}
			
			params.put("TipoProposito", IdTipoPrograma);
        	List<RangoHorario> RangosHorarios = sql.query(con, SQL_SELECT_RANGO_HORARIO, params, RangoHorario.class);
        	
        	if (RangosHorarios.size() < 1) {
        		String mensaje = "No esta configurado el Horario de ejecucion";
				logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM -- No esta configurado el Horario de ejecucion");
				String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, tipoArchivoCarga, mensaje);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
        		return true;
        	}
        	
    		RangoHorario rangoHorario = RangosHorarios.get(0);
        	params.clear();
        	
        	DateTimeFormatter sdf = DateTimeFormatter.ofPattern("HH:mm:ss");
    		LocalTime ms = LocalTime.parse(horaProceso, sdf);
    		
    		logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Hora Inicio: "+rangoHorario.HoraInicio+" Hora Sistema: "+horaProceso+" Hora Fin: "+rangoHorario.HoraFin);
    		logger.info(ms.isAfter(LocalTime.parse(rangoHorario.HoraInicio,sdf )) && ms.isBefore(LocalTime.parse(rangoHorario.HoraFin,sdf )));
    		if (ms.isAfter(LocalTime.parse(rangoHorario.HoraInicio,sdf )) && ms.isBefore(LocalTime.parse(rangoHorario.HoraFin,sdf ))) {
    			logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se rellena entrada de servicio por Ejecucion Agenda");
    			
    			entradaDAPFFMM.p_IdEmpresa = idEmpresa;
    			entradaDAPFFMM.p_IdUsuario = idUsuario;
    			entradaDAPFFMM.p_TipoArchivoCarga = tipoArchivoCarga;
    			entradaDAPFFMM.p_EnviarArchivoACasilla = debug ? "N" : this.getParam("EnviarArchivoACasilla");
    			entradaDAPFFMM.p_FechaCarga = fechaCarga;
    			entradaDAPFFMM.p_IdModulo = "TES";
    			entradaDAPFFMM.p_SimularOperacion = "N";
    			entradaDAPFFMM.p_UsaReglasNegocio = "S";
    			
    		} else {
    			String mensaje = "El proceso se encuentra fuera de Horario ";
    			logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Fuera de horario");
    			String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, tipoArchivoCarga, mensaje);
				
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
    			return true;
    		}
    		
    		if(entradaDAPFFMM.p_TipoArchivoCarga == null || "".equals(entradaDAPFFMM.p_TipoArchivoCarga)) {
    			logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - No se ha ingresado el Tipo Archivo Carga");
    			logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
    			return false;
    		}
    		
	        SalidaCargaMasivaIngresoEgresoDAPFFMM salidaDAPFFMM = servicioDAPFFM.logica(con,entradaDAPFFMM);
	        
	        logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM : " + salidaDAPFFMM);
	        
	        if(salidaDAPFFMM.P_OUT_ERRORESCONTADOR > 0L && salidaDAPFFMM.P_OUT_REGISTROSINGRESADOSCONTADOR == 0L) {
	        	String mensajesSalida = "";
	    		for (MensajeSalida mensaje : salidaDAPFFMM.P_OUT_MENSAJES) {
	    			if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
	    				mensajesSalida += mensaje.descripcion + ". ";
	    			}
	    		}
	    		
	    		logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se han encontrado errores: " + mensajesSalida);
    			String rutaArchivoSalida  = generarArchivoSalidaErrores(con, fechaCarga, horaProceso, mensajesSalida, tipoArchivoCarga, idEmpresa);
				
				actualizarRutaSalidaPrograma(con, rutaArchivoSalida, idPrograma);
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, tipoArchivoCarga, mensajesSalida);
				logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return true;
	        }
	        
	        if(!"".equals(salidaDAPFFMM.P_OUT_RUTA_ARCHIVO)) {
	        	String mensaje = "Proceso de Carga Masiva realizado correctamente, se han ingresado un total de : "+ salidaDAPFFMM.P_OUT_REGISTROSINGRESADOSCONTADOR + " registros";
	        	actualizarRutaSalidaPrograma(con, salidaDAPFFMM.P_OUT_RUTA_ARCHIVO , idPrograma);
	        	agregarLog(con, idEmpresa, idUsuario, "S", fechaSistema, tipoArchivoCarga, mensaje);
	        	logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return true;
    		} else {
    			logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - No se ha encontrado la Ruta para el Archivo de Salida");
	        	logger.info("FIN :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
	        	return false;
    		}
	        
		} catch(Exception e) {
			logger.error("ERROR", e.getMessage());
			e.printStackTrace();
			throw e;
		} 
	}
	
	private String consultarEstadoModulos(Connection con, Long idEmpresa, Long idUsuario, String idModulo) throws Exception{
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.consultarEstadoModulos");
		long inicio = System.currentTimeMillis();
		
		InConsultarEstadoModulo entrada = new InConsultarEstadoModulo();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = idModulo;
		ConsultarEstadoModulo objConsulta 	= new ConsultarEstadoModulo();
		String mensajes = "";
		HashMap<String,String> parmsMsj = new HashMap<>(1);
		
		OutConsultarEstadoModulo salida = objConsulta.logica(con, entrada);
		for (Modulo modulo : salida.p_out_ListaModulos) {
			if(("TES".equals(modulo.NombreModulo) ||"CTA".equals(modulo.NombreModulo)) && MODULO_ESTADO_CERRADO.equalsIgnoreCase(modulo.IdEstadoModulo)) {
				parmsMsj.put("NombreModulo", "TES".equals(modulo.NombreModulo) ? "TESORERIA" : "CUENTA INVERSION");
				mensajes += Diccionario.getMensaje(con,idEmpresa,"UI_ERROR_MODULO_CERRADO", parmsMsj) + ". ";
				parmsMsj.clear();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.consultarEstadoModulos", fin - inicio);
		return mensajes;
	}

	private void agregarLog(Connection con, Long idEmpresa, Long idUsuario, String estadoProcesado, String fechaSistema, String tipoArchivoCarga, String mensajes) throws Exception {
		logger.info("INICIO::BcsJobCargaMasivaIngresoEgresoDAPFFMM.agregarLog");
		long inicio = System.currentTimeMillis();
		
		AgregarLog agregarLogService = new AgregarLog();
		InAgregarLog entradaAgregarLog = new InAgregarLog ();
		Log log = new Log();
		
		log.IdCabeceraLog = idUsuario;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = "TES";
		log.IdUsuario = idUsuario;
		log.IdEmpresa = idEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = idUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "CARGA_MASIVA_INGRESO_EGRESO_" + tipoArchivoCarga;
		log.EstadoRegistro = "S".equals(estadoProcesado) ? "PRO" : "RZD";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "N".equals(estadoProcesado) ? "Proceso de Carga Masiva ha terminado con errores: " + mensajes : mensajes;
		log.IdProceso = 0L;
		log.EstadoProceso = "S".equals(estadoProcesado) ? "ING" : "RZD";
		log.NombreProceso = "N".equals(estadoProcesado) ? "Proceso de Carga Masiva ha terminado con errores: " + mensajes : mensajes;
		
		entradaAgregarLog.p_Log.add(log);
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.agregarLog", fin - inicio);
	}

	private void actualizarRutaSalidaPrograma(Connection con, String rutaArchivoSalida, Long idPrograma) throws Exception {
		logger.info("INICIO::BcsJobCargaMasivaIngresoEgresoDAPFFMM.actualizarRutaSalidaPrograma");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
		logger.info("INFO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM - Se agrega la ruta del archivo a la salida del Job - RUTA: " + rutaArchivoSalida);
		actualizar.add(new ActualizarPrograma(rutaArchivoSalida, idPrograma));
		sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.actualizarRutaSalidaPrograma", fin - inicio);
	}

	private String generarArchivoSalidaErrores(Connection con, String fechaActual, String horaProceso, String mensaje, String tipoArchivoCarga, Long idEmpresa) throws Exception {
		logger.info("INICIO::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarArchivoSalidaErrores");
		long inicio = System.currentTimeMillis();
		
		String rutaArchivoSalida = "";
		
		List<String[]> lineasArchivoSalida = new ArrayList<String[]>(1);
		String[] linea = { "0" , "0" , "Rechazado" , mensaje };
		lineasArchivoSalida.add(linea);
		
		String fechaHora = fechaActual.replace("-","") + "_" + horaProceso.replace(":", "");
		String nombreArchivoSalida = BASE_NOMBRE_ARCHIVO_SALIDA_ERRORES + tipoArchivoCarga +"_"+ fechaHora + ".csv";
		
		String rutaAbsolutaBase = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
//		String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator ;
		String rutaRelativaCargaMasivaTeasoreria = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "TESORERIA", "TES.RutaSalidaArchivosCargaMasiva");
		String rutaBaseArchivoSalida = rutaAbsolutaBase + rutaRelativaCargaMasivaTeasoreria;
		
		String rutaRelativaArchivo = rutaRelativaCargaMasivaTeasoreria + nombreArchivoSalida ;
		File folder = new File(rutaBaseArchivoSalida);
		if(!folder.exists()){
			logger.info("INFO::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarArchivoSalidaErrores - Carpeta destino no existe "+ rutaBaseArchivoSalida);
			long fin = System.currentTimeMillis();
			logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarArchivoSalidaErrores", fin - inicio);
			return rutaArchivoSalida;
		}
		
		Boolean resultadoGenerarCSV = generarReporteCSV(rutaAbsolutaBase, rutaRelativaArchivo, lineasArchivoSalida);
		
		rutaArchivoSalida = resultadoGenerarCSV ? rutaRelativaArchivo : "";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarArchivoSalidaErrores", fin - inicio);
		return rutaArchivoSalida;
	}

	private Boolean generarReporteCSV(String rutaAbsolutaBase, String rutaRelativaArchivo, List<String[]> lineasArchivoSalida) throws Exception {
		logger.info("INICIO::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV");
		long inicio = System.currentTimeMillis();
		
		String rutaReporte = rutaAbsolutaBase + rutaRelativaArchivo;
		
		FileOutputStream fos = new FileOutputStream(rutaReporte);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		CSVWriter csvWriter = new CSVWriter(osw, ';', CSVWriter.NO_QUOTE_CHARACTER);
		try {
			csvWriter.writeAll(lineasArchivoSalida);
		} catch (Exception e) {
			logger.error(e.getMessage());
			long fin = System.currentTimeMillis();
			logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
			return false;
		} finally {
        	try {
        		csvWriter.close();
        	} catch (Exception ex) {
        		logger.error(ex.getMessage());
        		long fin = System.currentTimeMillis();
        		logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
        		return false;
        	}
        }
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
		return true;
	}

	@Override
	public void onError(Throwable t) {
        logger.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public static void main(String[] args) throws SQLException  {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("scotia-pre")){
			BcsJobCargaMasivaIngresoEgresoDAPFFMM p1 = new BcsJobCargaMasivaIngresoEgresoDAPFFMM();
			p1.logica(con,true);
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
