package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.normativa.fatca.GenerarDatosFatca;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaFatca implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaFatca");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio PautaFatca *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			GenerarDatosFatca generarDatosFatca = new GenerarDatosFatca();
			
			GenerarDatosFatca.GenerarDatosFatcaEntrada entradaDatosFatca = new GenerarDatosFatca.GenerarDatosFatcaEntrada();
			entradaDatosFatca.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			
			GenerarDatosFatca.GenerarDatosFatcaSalida salidaDatosFatca = generarDatosFatca.logica(con, entradaDatosFatca);
			salida.P_OUT_ERRORESCONTADOR = salidaDatosFatca.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = salidaDatosFatca.P_OUT_MENSAJE;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaFatca *****");
    		throw e;
    	}
    	log.write("***** Fin PautaFatca *****");
		return salida;
    		  
    }

   
}