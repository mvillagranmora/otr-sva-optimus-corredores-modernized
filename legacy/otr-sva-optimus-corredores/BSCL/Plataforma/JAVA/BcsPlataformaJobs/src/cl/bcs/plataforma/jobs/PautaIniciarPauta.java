package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.fondomutuo.AperturaAutomaticoFondos;
import cl.bcs.utils.Log;


public class PautaIniciarPauta implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaIniciarPauta");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaIniciarPauta *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	
		//PROCESO DE PAUTA DUMMY PARA EVENTOS DE INICIO DE PAUTA
		
		salida.P_OUT_MENSAJE = "";
		salida.P_OUT_ERRORESCONTADOR = 0L;
	
		log.write("***** Fin PautaIniciarPauta *****");
		return salida;
    		  
    }

   
}