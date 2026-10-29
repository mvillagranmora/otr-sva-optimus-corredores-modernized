package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.replica.ReplicaUtils;

public class PautaRespaldoReplica implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaRespaldoReplica.class.getName());
	
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();	
		
		ReplicaUtils utils = new ReplicaUtils();
		
		try {
			Long idEmpresa = (Long)parameters.get("IdEmpresa");
			String codigoProceso = (String)parameters.get("CodigoProceso");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
			String fechaSistemaPauta = (String)parameters.get("FechaSistema");
			utils.procesarRespaldoSebra(con,fechaSistema,idEmpresa);
			
			if (codigoProceso.equalsIgnoreCase("RESPALDO_REPLICA_BATCH_DESPUES_DE")) {
				utils.guardarEnvioPostPauta(con, "Custodia_CarteraFM", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_CarteraRV", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_CarteraRFIF", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_Garantia", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_CarteraOtrosProductos", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_CustodiaFechaOperacion", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "Custodia_CustodiaFechaLiquidacion", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "CuentaInversion_SaldoFechaMovimiento", fechaSistemaPauta, fechaSistema);
				utils.guardarEnvioPostPauta(con, "CuentaInversion_SaldoFechaEfectiva", fechaSistemaPauta, fechaSistema);
				
			}
			if (codigoProceso.equalsIgnoreCase("RESPALDO_REPLICA_BATCH_ANTES_DE")) {
				utils.procesarRespaldoOptimus(con,fechaSistema,idEmpresa);
				utils.procesarRespaldoDiarioCarterasSebra(con,fechaSistema,idEmpresa);
			}
			
			
			
		}
		catch (Exception e) {
			salida.P_OUT_MENSAJE = e.getMessage();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES = new ArrayList<>();
			
			logger.info("***** Fin con Error*****");
			return salida;
		}
		
		salida.P_OUT_MENSAJE = "Respaldo realizado";
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJES = new ArrayList<>();
		
		logger.info("***** Fin *****");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("prebe", "connections.properties")){
			PautaRespaldoReplica ipb = new PautaRespaldoReplica();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
		    params.put("CodigoProceso","RESPALDO_REPLICA_BATCH_ANTES_DE");
		    params.put("FechaSistema","2026-08-26");
			ipb.transaction(con,params );
		    con.commit();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

	
}