package cl.bcs.socionegocio.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.socionegocio.jobs.BloquearClientePorFechaVencimientoJob.EntradaBloquearClientePorFechaVencimientoJob;
import cl.bcs.socionegocio.jobs.BloquearClientePorFechaVencimientoJob.SalidaBloquearClientePorFechaVencimientoJob;
import cl.bcs.utils.Log;

/**********************************
 * @version 	1.0
 * @author 		nvenegas
 **********************************/
public class PautaBloquearClientePorFechaVencimiento implements BcsProcesoPauta{
	   private final Log log = new Log("SOC.PautaBloquearClientePorFechaVencimiento"); 

    //@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable{

    		log.write("***** Inicio PautaBloquearClientePorFechaVencimiento ****");
		    
    		SalidaProcesoPauta salida = new SalidaProcesoPauta(); 
    		
    	    try{
    	    	
    	    	EntradaBloquearClientePorFechaVencimientoJob entradaBloquearClientePorFechaVencimientoJob = new EntradaBloquearClientePorFechaVencimientoJob();
    	    	entradaBloquearClientePorFechaVencimientoJob.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    	    	entradaBloquearClientePorFechaVencimientoJob.p_IdUsuario = (Long)parameters.get("IdUsuario");
    	    	
    	    	BloquearClientePorFechaVencimientoJob  bloquearClientePorFechaVencimientoJob = new BloquearClientePorFechaVencimientoJob();
    	    	
    	    	SalidaBloquearClientePorFechaVencimientoJob SalidaBloquearClientePorFechaVencimientoJob = bloquearClientePorFechaVencimientoJob.logica(con, entradaBloquearClientePorFechaVencimientoJob);
    	    	
    	    	salida.P_OUT_MENSAJE = "";
    	    	SalidaBloquearClientePorFechaVencimientoJob.P_OUT_MENSAJES.stream().forEach(mensaje->{
    				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
    				});
    	    	
    	    }catch( Exception e) {
    	    	log.write(e);
    	    	log.write("**** Error en PautaBloquearClientePorFechaVencimiento ****");
    	    	throw e;
    	    }
    	    log.write("***** Fin PautaBloquearClientePorFechaVencimiento *****");
    		return salida;
    	}
    	
}