package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.plataforma.pau.EnviarNotificacionFolioDocumento;
import cl.bcs.plataforma.pau.EnviarNotificacionFolioDocumento.EnviarNotificacionFolioDocumentoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PautaNotificacionStockCriticoFolioDte implements BcsProcesoPauta {
	
	private final Logger logger = LogManager.getLogger(PautaNotificacionStockCriticoFolioDte.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> entrada) throws InterruptedException, Throwable {
		
        logger.info("INICIO::PautaNotificacionFolioDocumentos");
        long inicio = System.currentTimeMillis();
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
		EnviarNotificacionFolioDocumento enviarNotificacionFolioDocumento = new EnviarNotificacionFolioDocumento();
		EnviarNotificacionFolioDocumento.EnviarNotificacionFolioDocumentoEntrada entradaEnviarNotificacionFolioDocumento = new EnviarNotificacionFolioDocumento.EnviarNotificacionFolioDocumentoEntrada();
		entradaEnviarNotificacionFolioDocumento.p_IdEmpresa = (Long) entrada.get("IdEmpresa");
		entradaEnviarNotificacionFolioDocumento.p_IdUsuario = (Long) entrada.get("IdUsuario");
		EnviarNotificacionFolioDocumentoSalida salidaEnviarNotificacionFolioDocumento = enviarNotificacionFolioDocumento.logica(con, entradaEnviarNotificacionFolioDocumento);
    	
		salida.P_OUT_ERRORESCONTADOR = salidaEnviarNotificacionFolioDocumento.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJE = salidaEnviarNotificacionFolioDocumento.P_OUT_MENSAJE;

		logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		logger.info("Salida: " + salida.P_OUT_MENSAJE);
		
		long fin = System.currentTimeMillis();
        logger.info("FIN::PautaNotificacionFolioDocumentos: ", fin - inicio);
    	
		return salida;
    }
	

	public static void main(String[] args) throws Throwable {
		System.out.println("== START ==");
		Connection con = PlataformaUtils.getDESAConnection();
		PautaNotificacionStockCriticoFolioDte apf = new PautaNotificacionStockCriticoFolioDte();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("IdEmpresa", 1L);
		parameters.put("IdUsuario", 1L);
		System.out.println(apf.transaction(con,parameters));	      
		con.commit();
		//con.rollback();
		System.out.println("== END ==");
	}

    
}