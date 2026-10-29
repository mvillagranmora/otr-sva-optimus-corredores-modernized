
package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.normativa.circulares.NotificarNCG365FFMM;
import cl.bcs.normativa.circulares.NotificarNCG365FFMM.NotificarNCG365FFMM_Entrada;
import cl.bcs.normativa.circulares.NotificarNCG365FFMM.NotificarNCG365FFMM_Salida;


import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		
 **********************************/
public class PautaNotificarNCG365FFMM implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaNotificarNCG365FFMM");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaNotificarNCG365FFMM *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		
    		////cl.bcs.normativa.circulares.NotificarNCG365FFMM
    		NotificarNCG365FFMM servicioNotificarNCG365FFMM = new NotificarNCG365FFMM();
    		NotificarNCG365FFMM_Entrada entradaNotificarNCG365FFMM = new NotificarNCG365FFMM_Entrada();
    		
    		entradaNotificarNCG365FFMM.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entradaNotificarNCG365FFMM.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		entradaNotificarNCG365FFMM.p_IdModulo  = "NOR";
    		entradaNotificarNCG365FFMM.p_FechaCertificacion = "0001-01-01"; //Que se ejecute a fecha de sistema
    		
    	
    		NotificarNCG365FFMM_Salida salidaNotificarNCG365FFMM = servicioNotificarNCG365FFMM.logica(con, entradaNotificarNCG365FFMM);
    		

    		salida.P_OUT_MENSAJE = "";
			salida.P_OUT_ERRORESCONTADOR = salidaNotificarNCG365FFMM.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		
			
			
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaNotificarNCG365FFMM *****");
    		throw e;
    	}
		log.write("***** Fin PautaNotificarNCG365FFMM *****");
		return salida;
    		  
    }

   
}