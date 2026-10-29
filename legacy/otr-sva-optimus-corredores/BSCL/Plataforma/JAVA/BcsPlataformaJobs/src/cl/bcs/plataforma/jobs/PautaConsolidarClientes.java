package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.circulares.ConsolidarClientes;
import cl.bcs.normativa.circulares.ConsolidarClientes.EntradaConsolidarClientes;
import cl.bcs.normativa.circulares.ConsolidarClientes.SalidaConsolidarClientes;

public class PautaConsolidarClientes implements BcsProcesoPauta {
	static final Logger logger = LogManager.getLogger(PautaConsolidarClientes.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.debug("***** Inicio PautaConsolidarClientes *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		
    		EntradaConsolidarClientes entradaConsolidarClientes = new EntradaConsolidarClientes();
    		entradaConsolidarClientes.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		ConsolidarClientes consolidarClientes = new ConsolidarClientes();
    		SalidaConsolidarClientes salidaConsolidarClientes = consolidarClientes.logica(con, entradaConsolidarClientes);
			salida.P_OUT_MENSAJE = salidaConsolidarClientes.P_OUT_MENSAJES.get(0).descripcion;
			salida.P_OUT_ERRORESCONTADOR = salidaConsolidarClientes.P_OUT_ERRORESCONTADOR;
	
			logger.debug("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.debug("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) logger.debug(mensaje);
    	} catch (Exception e) {
    		logger.error("***** Error PautaConsolidarClientes *****", e);
    		throw e;
    	}
    	logger.debug("***** Fin PautaConsolidarClientes *****");
		return salida;
    		  
    }
	
}
