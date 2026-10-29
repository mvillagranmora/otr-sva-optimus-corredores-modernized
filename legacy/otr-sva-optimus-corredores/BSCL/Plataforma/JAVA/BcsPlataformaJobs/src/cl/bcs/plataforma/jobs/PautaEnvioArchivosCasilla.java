package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;

public class PautaEnvioArchivosCasilla extends BcsJob implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaEjecutarReplicaBatch.class.getName());
	
	public SalidaProcesoPauta ejecutarTarea(Connection con, Long IdEmpresa, String tarea, SalidaProcesoPauta salida) throws Exception {
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
		inJenkins.p_IdModulo = "PLA";
		inJenkins.p_IdUsuario = 0L;
		inJenkins.p_IdEmpresa = IdEmpresa;
		inJenkins.p_TipoTarea = tarea;
		
		EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
		SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
		logger.debug(outJenkins.toString());
		
		if (outJenkins.CodigoHttp == 201L) {
			salida.P_OUT_MENSAJE = "Tarea creada correctamente";
		}
		else {
			//Si no esta configurada la tarea notificarlo pero no arrojar error
			if(outJenkins.P_OUT_MENSAJES.get(0).descripcion.equals("No se encuentra TipoTarea")) {
				salida.P_OUT_MENSAJE = "No se encuentra TipoTarea";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,tarea,"0", "No se encuentra TipoTarea configurada en PLA.TareaJenkins"));
			}
			else {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJE = "ERROR Envio archivos. Revisar mensajes con detalle.";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,tarea,"Error al crear tarea, codigo " + outJenkins.CodigoHttp + " - " + outJenkins.MensajeHttp, outJenkins.P_OUT_MENSAJES.toString()));
			}
		}
		return salida;
	}
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
		
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		salida = ejecutarTarea(con, (Long)parameters.get("IdEmpresa"), "ENVIAR_ARCHIVO_P40", salida);
		salida = ejecutarTarea(con, (Long)parameters.get("IdEmpresa"), "ENVIAR_ARCHIVO_NCG380", salida);
		
		logger.info("***** Fin *****");
		return salida;
    }
	
    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa", "connections.properties")){
			PautaEjecutarReplicaBatch ipb = new PautaEjecutarReplicaBatch();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    con.rollback();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

    @Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		try {
			logger.info("Inicio");
			
			SalidaProcesoPauta salida = new SalidaProcesoPauta();
			salida = ejecutarTarea(con, Long.parseLong(this.getParam("IdEmpresa")), "ENVIAR_ARCHIVO_P40", salida);
			salida = ejecutarTarea(con, Long.parseLong(this.getParam("IdEmpresa")), "ENVIAR_ARCHIVO_NCG380", salida);
			
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				logger.error("El proceso PautaEnvioArchivosCasilla ha finalizado con errores");
				return false;
			}
			logger.info("El proceso PautaEnvioArchivosCasilla se ha ejecutado correctamente");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
    		return false;
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t);
    }

	@Override
	public void pause() {

	}
}