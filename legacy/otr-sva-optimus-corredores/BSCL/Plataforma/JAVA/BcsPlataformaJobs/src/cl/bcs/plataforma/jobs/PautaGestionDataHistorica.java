package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pla.GestionDataHistorica;
import cl.bcs.plataforma.pla.GestionDataHistorica.GestionDataHistoricaEntrada;
import cl.bcs.plataforma.pla.GestionDataHistorica.GestionDataHistoricaSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class PautaGestionDataHistorica  implements BcsProcesoPauta {
static final Logger logger 	= LogManager.getLogger(PautaGestionDataHistorica.class.getName());	
   
    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	
    	logger.debug("***** Inicio PautaGestionDataHistorica *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
    	try {
    		
    		GestionDataHistoricaEntrada gestionDataHistoricaEntrada = new GestionDataHistoricaEntrada();
    		gestionDataHistoricaEntrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			gestionDataHistoricaEntrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
			gestionDataHistoricaEntrada.p_Fecha  = ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
			GestionDataHistorica gestionDataHistorica = new GestionDataHistorica();
			
			GestionDataHistoricaSalida gestionDataHistoricaSalida = gestionDataHistorica.logica(con, gestionDataHistoricaEntrada);
			
			salida.P_OUT_MENSAJE = "";
			gestionDataHistoricaSalida.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaGestionDataHistorica *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaGestionDataHistorica *****");
		return salida;
	}
	
}
