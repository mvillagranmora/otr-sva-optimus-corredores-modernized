package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.cli.BloqueoTemporalClientePorDesactualizacionDatos;
import cl.bcs.socionegocio.cli.BloqueoTemporalClientePorDesactualizacionDatos.EntradaBloqueoTemporalClientePorDesactualizacionDatos;
import cl.bcs.socionegocio.cli.BloqueoTemporalClientePorDesactualizacionDatos.SalidaBloqueoTemporalClientePorDesactualizacionDatos;

public class PautaBloqueoTemporalClientePorDesactualizacionDatos  implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaBloqueoTemporalClientePorDesactualizacionDatos.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.debug("***** Inicio PautaFatca *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			EntradaBloqueoTemporalClientePorDesactualizacionDatos entradaBloqueoTemporalClientePorDesactualizacionDatos = new EntradaBloqueoTemporalClientePorDesactualizacionDatos();
			entradaBloqueoTemporalClientePorDesactualizacionDatos.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			entradaBloqueoTemporalClientePorDesactualizacionDatos.p_IdUsuario = (Long)parameters.get("IdUsuario");
			
			BloqueoTemporalClientePorDesactualizacionDatos bloqueoTemporalClientePorDesactualizacionDatos = new BloqueoTemporalClientePorDesactualizacionDatos();
			SalidaBloqueoTemporalClientePorDesactualizacionDatos salidaBloqueoTemporalClientePorDesactualizacionDatos = bloqueoTemporalClientePorDesactualizacionDatos.logica(con, entradaBloqueoTemporalClientePorDesactualizacionDatos);
			salida.P_OUT_MENSAJE = "";
			salidaBloqueoTemporalClientePorDesactualizacionDatos.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
	
			logger.debug("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.debug("Salida: ");
    	} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaBloqueoTemporalClientePorDesactualizacionDatos *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaBloqueoTemporalClientePorDesactualizacionDatos *****");
		return salida;
	}

}
