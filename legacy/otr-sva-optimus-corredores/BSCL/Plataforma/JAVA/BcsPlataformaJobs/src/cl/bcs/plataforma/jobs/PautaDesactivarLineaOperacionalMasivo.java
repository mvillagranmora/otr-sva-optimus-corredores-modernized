package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;


import cl.bcs.socionegocio.por.DesactivarLineaOperacionalMasivo;
import cl.bcs.socionegocio.por.DesactivarLineaOperacionalMasivo.EntradaDesactivarLineaOperacionalMasivo;
import cl.bcs.socionegocio.por.DesactivarLineaOperacionalMasivo.SalidaDesactivarLineaOperacionalMasivo;
import cl.bcs.utils.Log;

public class PautaDesactivarLineaOperacionalMasivo implements BcsProcesoPauta{
	private final Log log = new Log("SOC.PautaDesactivarLineaOperacionalMasivo");

	//@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable{

 		log.write("***** Inicio PautaDesactivarLineaOperacionalMasivo ****");
		    
 		SalidaProcesoPauta salida = new SalidaProcesoPauta();
 	
 	    try{
 	    	
 	    	EntradaDesactivarLineaOperacionalMasivo entradaDesactivarLineaOperacionalMasivo = new EntradaDesactivarLineaOperacionalMasivo();
 	    	entradaDesactivarLineaOperacionalMasivo.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
 	    	entradaDesactivarLineaOperacionalMasivo.p_IdUsuario = (Long)parameters.get("IdUsuario");
 	    	entradaDesactivarLineaOperacionalMasivo.p_IdModulo = (String)parameters.get("IdModulo");
 	    	
 	    	DesactivarLineaOperacionalMasivo  desactivarLineaOperacionalMasivo = new DesactivarLineaOperacionalMasivo();
 	    	
 	    	SalidaDesactivarLineaOperacionalMasivo salidaDesactivarLineaOperacionalMasivo = desactivarLineaOperacionalMasivo.logica(con, entradaDesactivarLineaOperacionalMasivo);
 	    	
 	    	salida.P_OUT_ERRORESCONTADOR = salidaDesactivarLineaOperacionalMasivo.P_OUT_ERRORESCONTADOR;
 	    	if(salida.P_OUT_ERRORESCONTADOR >0L) {
 	    		salida.P_OUT_MENSAJE= "Error al desactivar linea operacional";
 	    	}else {
 	    		salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente";
 	    	}
 	    	
 	    }catch( Exception e) {
 	    	log.write(e);
 	    	log.write("**** Error en PautaDesactivarLineaOperacionalMasivo ****");
 	    	throw e;
 	    }
 	    log.write("***** Fin PautaDesactivarLineaOperacionalMasivo *****");
 		return salida;
 	}
}
