package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.CambioNemos;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaCambioNemosSD implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaCambioNemosSD");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaCambioNemosSD *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
        try {
			CambioNemos cambioNemo = new CambioNemos();
			CambioNemos.InCambioNemos inCambioNemo = new CambioNemos.InCambioNemos();
			inCambioNemo.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			inCambioNemo.p_IdModulo = "CUS";
			inCambioNemo.p_IdUsuario = (Long)parameters.get("IdUsuario");
			
			CambioNemos.OutCambioNemos outCambioNemos = cambioNemo.logica(con, inCambioNemo);
			if (outCambioNemos.P_OUT_ERRORESCONTADOR == 0) {
				salida.P_OUT_MENSAJE = "El proceso CambioNemosSD se ha ejecutado correctamente";
			} else {
				salida.P_OUT_MENSAJE = "Ha ocurrido un error al ejecutar el proceso CambioNemosSD";
			}
			salida.P_OUT_ERRORESCONTADOR = outCambioNemos.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
        } catch (Exception e) {
        	log.write(e);
        	log.write("***** Error PautaCambioNemosSD *****");
        	throw e;
        }
		log.write("***** Fin PautaCambioNemosSD *****");
		return salida;
    }

   
}