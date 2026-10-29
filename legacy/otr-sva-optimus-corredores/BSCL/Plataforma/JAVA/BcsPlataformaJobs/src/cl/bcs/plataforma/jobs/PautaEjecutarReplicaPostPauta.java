package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.replica.ReplicaUtils.EnvioPostPauta;
import cl.bcs.utils.SQL;

public class PautaEjecutarReplicaPostPauta implements BcsProcesoPauta {
	
	private static final Logger logger = LogManager.getLogger(PautaEjecutarReplicaPostPauta.class.getName());
	private static final FileLoader loader = new FileLoader();
	private static final String SELECT_ENVIOS = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaEjecutarReplicaPostPauta_SelectEnvios.sql");
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();	
		
		
		SQL sql = new SQL();
		String fechaSistema = (String) parameters.get("FechaSistema");
		HashMap<String,Object> datos = new HashMap<>();
		datos.put("FechaSistema", fechaSistema);
		List<EnvioPostPauta> envios = sql.query(con, SELECT_ENVIOS, datos, ReplicaUtils.EnvioPostPauta.class);
		
		for (EnvioPostPauta envio: envios) {
			EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
			inJenkins.p_IdModulo = "PLA";
			inJenkins.p_IdUsuario = 0L;
			inJenkins.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			inJenkins.p_TipoTarea = "ENVIO_REPLICA_JENKINS";
			
			
			inJenkins.p_Parametros.add(new ParametroJenkins("TABLES","\"" + envio.Tabla + "\""));
			inJenkins.p_Parametros.add(new ParametroJenkins("DATE",envio.Fecha+"-"+envio.Fecha));
			
			logger.debug(inJenkins.toString());
			EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
			SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
			logger.debug(outJenkins.toString());
			
			if (outJenkins.CodigoHttp == 201L) {
				
				salida.P_OUT_ERRORESCONTADOR = 0L;
				salida.P_OUT_MENSAJE = "Tarea creada correctamente";
			}
			else {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJE = "Error al crear tarea, codigo " + outJenkins.CodigoHttp + " - " + outJenkins.MensajeHttp;
			}
		}
		salida.P_OUT_ERRORESCONTADOR = 0L;
		logger.info("***** Fin *****");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa", "connections.properties")){
			PautaEjecutarReplicaPostPauta ipb = new PautaEjecutarReplicaPostPauta();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
		    params.put("FechaSistema","2026-06-16");
		    
			
			ipb.transaction(con,params );
		    con.rollback();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

	
}