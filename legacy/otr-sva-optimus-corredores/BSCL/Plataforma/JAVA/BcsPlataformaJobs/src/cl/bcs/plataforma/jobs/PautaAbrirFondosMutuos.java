package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.fondomutuo.AperturaAutomaticoFondos;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaAbrirFondosMutuos implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaAbrirFondosMutuos");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaAbrirFondosMutuos *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			AperturaAutomaticoFondos.AperturaAutomaticoFondosEntrada parametrosAperturaAutomaticoFondos = new AperturaAutomaticoFondos.AperturaAutomaticoFondosEntrada();
			parametrosAperturaAutomaticoFondos.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametrosAperturaAutomaticoFondos.p_IdUsuario = (Long)parameters.get("IdUsuario");
			
			AperturaAutomaticoFondos aperturaAutomaticoFondos = new AperturaAutomaticoFondos();
			AperturaAutomaticoFondos.AperturaAutomaticoFondosSalida salidaffmm = aperturaAutomaticoFondos.logica(con, parametrosAperturaAutomaticoFondos);
			salida.P_OUT_MENSAJE = salidaffmm.P_OUT_MENSAJE;
			salida.P_OUT_ERRORESCONTADOR = salidaffmm.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaAbrirFondosMutuos *****");
    		throw e;
    	}
		log.write("***** Fin PautaAbrirFondosMutuos *****");
		return salida;
    		  
    }

   
}