package cl.bcs.socionegocio.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;
import cl.bcs.fondomutuo.AdministracionLiquidez.FechaActual;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.per.PurgaProspectoCliente;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.EntradaPurgaProspectoCliente;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.SalidaPurgaProspectoCliente;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class BcsJobPurgaProspectoCliente extends BcsJob implements JavaCall {
	String IdTipoPrograma = "CARGA_BORRAR_DATOS_PERSONA";
	
	private final String SQL_SELECT_RANGO_HORARIO = loadResourceToString("cl/bcs/socionegocio/jobs/sql/BcsJobPurgaProspectoCliente_SelectRangoHorario.sql");
	private final String OBTENER_FECHA_ACTUAL = loadResourceToString("cl/bcs/socionegocio/jobs/sql/BcsJobPurgaProspectoCliente_ObtenerFechaActual.sql");
	private static final Logger logger = LogManager.getLogger(BcsJobPurgaProspectoCliente.class);
	
	public static class RangoHorario extends BcsPojo {
		public String HoraInicio;
		public String HoraFin;
		
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
		logger.info("INICIO :: BcsJobPurgaProspectoCliente");
		
		try {
			SQL sql = new SQL();
	    	HashMap<String,Object> params = new HashMap<>(2);
			
			EntradaPurgaProspectoCliente entradaPURGA = new EntradaPurgaProspectoCliente(); 
			PurgaProspectoCliente servicioPURGA = new PurgaProspectoCliente();

			Long idEmpresa = debug ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = debug ? 0L : Long.parseLong(this.getParam("IdUsuario"));
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String horaProceso = FechaDB.ObtenerHoraSistemaLocal(con, idEmpresa);
			
			List<FechaActual> fechaActual = sql.query(con, OBTENER_FECHA_ACTUAL, params, FechaActual.class);
			
			String fechaCarga = debug ? "2026-07-09" : fechaActual.get(0).Fecha;
			
			List<Cabecera> cabecerasProcesadas = servicioPURGA.getCabeceras(con, fechaCarga, "PRO");
			  if(!cabecerasProcesadas.isEmpty()) {
				  String mensajesSalida =  Diccionario.getMensaje(con,idEmpresa,"UI_CARGA_BORRADO_DATOS_PERSONA_YA_CARGADO", new HashMap<String, String>(0)); 
				  logger.info("INFO :: BcsJobPurgaProspectoCliente - Se han encontrado errores: "+ mensajesSalida); 
				  agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, mensajesSalida);
				  logger.info("FIN :: BcsJobPurgaProspectoCliente");
				  return true; 
			  }
			  
			List<Cabecera> cabeceras = servicioPURGA.getCabeceras(con, fechaCarga, "PEN");
			  if(cabeceras.isEmpty()) { 
				  String mensajesSalida =Diccionario.getMensaje(con,idEmpresa, "UI_CARGA_BORRADO_DATOS_PERSONA_SIN_ARCHIVO", new HashMap<String,String>(0)); 
				  logger.info("INFO :: BcsJobPurgaProspectoCliente - Se han encontrado errores: "+ mensajesSalida); 
				  agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, mensajesSalida);
				  logger.info("FIN :: BcsJobPurgaProspectoCliente"); 
				  return true;
			  }
	
			params.put("TipoProposito", IdTipoPrograma);
        	List<RangoHorario> RangosHorarios = sql.query(con, SQL_SELECT_RANGO_HORARIO, params, RangoHorario.class);
        	if (RangosHorarios.size() < 1) {
        		String mensaje = "No esta configurado el Horario de ejecucion";
				logger.info("INFO :: BcsJobPurgaProspectoCliente -- No esta configurado el Horario de ejecucion");
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, mensaje);
				logger.info("FIN :: BcsJobPurgaProspectoCliente");
        		return true;
        	}
    		RangoHorario rangoHorario = RangosHorarios.get(0);
        	params.clear();
        	DateTimeFormatter sdf = DateTimeFormatter.ofPattern("HH:mm:ss");
    		LocalTime ms = LocalTime.parse(horaProceso, sdf);
    		logger.info("INFO :: BcsJobPurgaProspectoCliente - Hora Inicio: "+rangoHorario.HoraInicio+" Hora Sistema: "+horaProceso+" Hora Fin: "+rangoHorario.HoraFin);
    		logger.info(ms.isAfter(LocalTime.parse(rangoHorario.HoraInicio,sdf )) && ms.isBefore(LocalTime.parse(rangoHorario.HoraFin,sdf )));
    		if (ms.isAfter(LocalTime.parse(rangoHorario.HoraInicio,sdf )) && ms.isBefore(LocalTime.parse(rangoHorario.HoraFin,sdf ))) {
    			logger.info("INFO :: BcsJobPurgaProspectoCliente - Se rellena entrada de servicio por Ejecucion Agenda");
    			entradaPURGA.p_IdEmpresa = idEmpresa; 
    			entradaPURGA.p_IdUsuario = idUsuario;
    			entradaPURGA.p_FechaCarga = fechaCarga;
    			entradaPURGA.p_IdModulo = "SOC";
    		} else {
    			String mensaje = "El proceso se encuentra fuera de Horario ";
    			logger.info("INFO :: BcsJobPurgaProspectoCliente - Fuera de horario");
				agregarLog(con, idEmpresa, idUsuario, "N", fechaSistema, mensaje);
				logger.info("FIN :: BcsJobPurgaProspectoCliente");
    			return true;
    		}
    		
	        SalidaPurgaProspectoCliente salidaPURGA = servicioPURGA.logica(con,entradaPURGA);
			logger.info("INFO :: BcsJobPurgaProspectoCliente : " + salidaPURGA);	  
			if(salidaPURGA.P_OUT_ERRORESCONTADOR > 0L && salidaPURGA.P_OUT_REGISTROSPROCESADOSCONTADOR == 0L) { 
				String mensajesSalida = "";
				for (MensajeSalida mensaje : salidaPURGA.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
						mensajesSalida += mensaje.descripcion + ". "; 
					} 
				}
				logger.info("INFO :: BcsJobPurgaProspectoCliente - Se han encontrado errores: " + mensajesSalida); 
				logger.info("FIN :: BcsJobPurgaProspectoCliente");
				return true;
			}else {
				String mensaje = "Proceso de Borrado datos persona realizado correctamente, se han ingresado un total de : " + salidaPURGA.P_OUT_REGISTROSPROCESADOSCONTADOR + " registros";
				agregarLog(con, idEmpresa, idUsuario, "S", fechaSistema, mensaje); 
				logger.info("FIN :: BcsJobPurgaProspectoCliente"); 
				return true; 
			}
				 
		} catch(Exception e) {
			logger.error("ERROR", e.getMessage());
			e.printStackTrace();
			throw e;
		} 
	}
	
	private void agregarLog(Connection con, Long idEmpresa, Long idUsuario, String estadoProcesado, String fechaSistema, String mensajes) throws Exception {
		logger.info("INICIO::BcsJobPurgaProspectoCliente.agregarLog");
		long inicio = System.currentTimeMillis();
		
		AgregarLog agregarLogService = new AgregarLog();
		InAgregarLog entradaAgregarLog = new InAgregarLog ();
		Log log = new Log();
		
		log.IdCabeceraLog = idUsuario;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = "SOC";
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
		log.IdTipoRegistro = "CARGA_BORRAR_DATOS_PERSONA";
		log.EstadoRegistro = "S".equals(estadoProcesado) ? "PRO" : "RZD";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "N".equals(estadoProcesado) ? mensajes : mensajes;
		log.IdProceso = 0L;
		log.EstadoProceso = "S".equals(estadoProcesado) ? "ING" : "RZD";
		log.NombreProceso = "Proceso de Borrado datos persona Ley Arco";
		
		entradaAgregarLog.p_Log.add(log);
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobPurgaProspectoCliente.agregarLog", fin - inicio);
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
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")){
			BcsJobPurgaProspectoCliente p1 = new BcsJobPurgaProspectoCliente();
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
