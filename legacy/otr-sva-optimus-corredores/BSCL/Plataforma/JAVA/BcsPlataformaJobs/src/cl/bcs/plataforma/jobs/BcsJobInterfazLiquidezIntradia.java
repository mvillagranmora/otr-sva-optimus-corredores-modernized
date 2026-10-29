package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pau.InterfazLiquidezIntradia;
import cl.bcs.plataforma.pau.InterfazLiquidezIntradia.EntradaInterfazLiquidezIntradia;
import cl.bcs.plataforma.pau.InterfazLiquidezIntradia.SalidaInterfazLiquidezIntradia;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;


public final class BcsJobInterfazLiquidezIntradia extends BcsJob {
	
	private static final Logger LOGGER = LogManager.getLogger(BcsJobInterfazLiquidezIntradia.class.getName());
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_SELECT_RANGO_HORARIO = fileLoader.loadResourceToString("cl/bcs/plataforma/jobs/sql/InterfazLiquidezIntradia_SelectRangoHorario.sql");
	private final String SQL_UPDATE_ID_PROGRAMA =  fileLoader.loadResourceToString("cl/bcs/plataforma/jobs/sql/BcsJobInterfazLiquidezIntradia_UpdateSalidaPrograma.sql");
	
	public static class RangoHorario extends BcsPojo {
		public java.sql.Time HoraInicio;
		public java.sql.Time HoraFin;
		
	}
	
	public static class ActualizarPrograma extends BcsPojo {
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
    @Override
    public void onError(final Throwable t) {
    	LOGGER.info("****** ERROR  *******");
        LOGGER.info(t);
        t.printStackTrace();
    }

    @Override
    public void pause() {
    	
    }

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	return logica(con, false);
    }
    
    public boolean logica(Connection con, boolean debug ) throws Exception {
    	LOGGER.info("INICIO::BcsJobInterfazLiquidezIntradia");
    	
    	SQL sql = new SQL();
    	HashMap<String,Object> params = new HashMap<>();
    	
    	Long idEmpresa = 0L;
    	String esManual = "";
    	String enviarArchivoACasilla = "";
    	String fechaProceso = "";
    	
    	if(debug) {
    		idEmpresa = 1L;
        	esManual = "S";
        	enviarArchivoACasilla = "S";
        	fechaProceso = "2025-02-21";
    	} else {
    		idEmpresa = Long.valueOf(getParam("IdEmpresa"));
        	esManual = getParam("EsManual");
        	enviarArchivoACasilla = getParam("EnviarArchivoACasilla");
        	fechaProceso = getParam("FechaProceso");
    	}
    	
    	InterfazLiquidezIntradia interfz = new InterfazLiquidezIntradia();
    	SalidaInterfazLiquidezIntradia out = new SalidaInterfazLiquidezIntradia();
    	EntradaInterfazLiquidezIntradia entrada = new EntradaInterfazLiquidezIntradia();
    	
    	if("S".equals(esManual)) {
    		LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Se rellena entrada de servicio por Ejecucion Manual");
    		entrada.p_FechaProceso = fechaProceso;
        	entrada.p_IdEmpresa = idEmpresa;
        	entrada.p_Manual = esManual;
    	} else {

        	params.put("TipoProposito", "GENERAR_INTERFAZ_LIQUIDEZ_INTRADIA");
        	List<RangoHorario> RangosHorarios = sql.query(con, SQL_SELECT_RANGO_HORARIO, params, RangoHorario.class);
        	
        	if (RangosHorarios.size() < 1 && "N".equals(esManual))
        		return false;
        	
        	RangoHorario rangoHorario = RangosHorarios.get(0);
        	params.clear();
        	
    		String horaProceso = FechaDB.ObtenerHoraSistemaLocal(con, idEmpresa);
    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    		long ms = sdf.parse(horaProceso).getTime();
        	
    		LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Hora Inicio: "+rangoHorario.HoraInicio+" Hora Sistema: "+horaProceso+" Hora Fin: "+rangoHorario.HoraFin);
    		if (ms>rangoHorario.HoraInicio.getTime() && ms<rangoHorario.HoraFin.getTime()) {
    			LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Se rellena entrada de servicio por Ejecucion Agenda");
    			entrada.p_FechaProceso = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    			entrada.p_IdEmpresa = idEmpresa;
    			entrada.p_Manual = "N";
    		} else {
    			LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Fuera de horario");
    			return true;
    		}
    	}
    	
    	out = interfz.logica(con, entrada, false);
    	
    	if (out.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
    		//actualizar salida job
    		LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Se agrega la ruta del archivo a la salida del Job - RUTA: " + out.P_OUT_RUTA_ARCHIVO);
    		List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
    		actualizar.add(new ActualizarPrograma(out.P_OUT_RUTA_ARCHIVO, this.getJobId()));
    		sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
    		
    		if("N".equals(esManual) || ("S".equals(enviarArchivoACasilla) && "S".equals(esManual) )) {
    			LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Se procede a ejecutar la tarea Jenkins para enviar a la casilla el Archivo");
    			EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
    			inJenkins.p_IdModulo = "PLA";
    			inJenkins.p_IdUsuario = 0L;
    			inJenkins.p_IdEmpresa = idEmpresa;
    			inJenkins.p_TipoTarea = "ENVIAR_INTERFAZ_LIQUIDEZ_INTRADIA";
    			inJenkins.p_Parametros.add(new ParametroJenkins("FechaProceso", entrada.p_FechaProceso));
    			
    			EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
    			SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
    			
    			LOGGER.info("INFO::BcsJobInterfazLiquidezIntradia - Salida jenkins: "+outJenkins);
    		}
    		
		}
    	    	
    	LOGGER.info("FIN::BcsJobInterfazLiquidezIntradia");
    	
    	return true;
    }
    
	public static void main(String[] args) throws SQLException {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
			BcsJobInterfazLiquidezIntradia p1 = new BcsJobInterfazLiquidezIntradia();
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