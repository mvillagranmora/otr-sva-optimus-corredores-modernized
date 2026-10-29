package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pla.LimpiarDataHistorico;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoEntrada;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoSalida;
import cl.bcs.plataforma.pla.utils.FileLoader;

public class PautaLimpiarDataHistorico  implements BcsProcesoPauta {
static final Logger logger 	= LogManager.getLogger(PautaLimpiarDataHistorico.class.getName());	
	FileLoader loader 			= new FileLoader();
	
   
    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	
    	logger.debug("***** Inicio PautaLimpiarDataHistorico *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
    	try {
    		
    		LimpiarDataHistoricoEntrada LimpiarDataHistoricoEntrada = new LimpiarDataHistoricoEntrada();
			LimpiarDataHistoricoEntrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			LimpiarDataHistoricoEntrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
			
			LimpiarDataHistorico LimpiarDataHistorico = new LimpiarDataHistorico();
			
			LimpiarDataHistoricoSalida LimpiarDataHistoricoSalida = LimpiarDataHistorico.logica(con, LimpiarDataHistoricoEntrada);
			
			salida.P_OUT_MENSAJE = "";
			LimpiarDataHistoricoSalida.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaLimpiarDataHistorico *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaLimpiarDataHistorico *****");
		return salida;
	}
	
}
