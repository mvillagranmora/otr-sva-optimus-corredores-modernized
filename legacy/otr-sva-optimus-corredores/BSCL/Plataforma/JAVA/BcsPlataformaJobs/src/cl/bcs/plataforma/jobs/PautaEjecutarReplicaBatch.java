package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaEjecutarReplicaBatch implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaEjecutarReplicaBatch.class.getName());
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();	
		
		EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
		inJenkins.p_IdModulo = "PLA";
		inJenkins.p_IdUsuario = 0L;
		inJenkins.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
		inJenkins.p_TipoTarea = "REPLICA_BATCH";
		
		EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
		SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
		logger.debug(outJenkins.toString());
		//{"CodigoHttp":201,"MensajeHttp":"Created","P_OUT_ERRORESCONTADOR":0,"P_OUT_MENSAJES":[]}
		
		if (outJenkins.CodigoHttp == 201L) {
			salida.P_OUT_ERRORESCONTADOR = 0L;
			salida.P_OUT_MENSAJE = "Tarea creada correctamente";
		}
		else {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error al crear tarea, codigo " + outJenkins.CodigoHttp + " - " + outJenkins.MensajeHttp;
		}
		logger.info("***** Fin *****");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("scotia", "connections.properties")){
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

	
}