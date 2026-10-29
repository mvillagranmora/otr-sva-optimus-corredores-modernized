package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.fondomutuo.ReniciarFolioOperacionOmnibusEmisor;
import cl.bcs.fondomutuo.ReniciarFolioOperacionOmnibusEmisor.EntradaReniciarFolioOperacionOmnibusEmisor;
import cl.bcs.fondomutuo.ReniciarFolioOperacionOmnibusEmisor.SalidaReniciarFolioOperacionOmnibusEmisor;

public class PautaReniciarFolioOperacionOmnibusEmisor implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaReniciarFolioOperacionOmnibusEmisor.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.debug("***** Inicio PautaReniciarFolioOperacionOmnibusEmisor *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			EntradaReniciarFolioOperacionOmnibusEmisor entradaReniciarFolioOperacionOmnibusEmisor = new EntradaReniciarFolioOperacionOmnibusEmisor();
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdUsuario = (Long)parameters.get("IdUsuario");
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdModulo = "PLA";
			
			ReniciarFolioOperacionOmnibusEmisor reniciarFolioOperacionOmnibusEmisor = new ReniciarFolioOperacionOmnibusEmisor();
			SalidaReniciarFolioOperacionOmnibusEmisor salidaReniciarFolioOperacionOmnibusEmisor = reniciarFolioOperacionOmnibusEmisor.logica(con, entradaReniciarFolioOperacionOmnibusEmisor);
			salida.P_OUT_MENSAJE = "";
			salidaReniciarFolioOperacionOmnibusEmisor.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaReniciarFolioOperacionOmnibusEmisor *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaReniciarFolioOperacionOmnibusEmisor *****");
		return salida;
	}
	
}
