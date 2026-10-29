package cl.bcs.custodia.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.FusionCarteras;
import cl.bcs.custodia.FusionCarteras.FusionCarterasEntrada;
import cl.bcs.custodia.FusionCarteras.FusionCarterasSalida;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		njohnson
 **********************************/

 public class PautaFusionCartera implements BcsProcesoPauta {

    private final Log log = new Log("CUS.PautaFusionCarteras"); 
	
	

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable  {
        log.write("****** INICIO PautaFusionCarteras  *******");
        
        
        SalidaProcesoPauta salida = new SalidaProcesoPauta();
      
        try{
        	FusionCarterasEntrada entradaServicioFusionCarteras = new FusionCarterasEntrada();
            FusionCarterasSalida salidaServicioFusionCarteras = new FusionCarterasSalida();
            FusionCarteras servicioFusionCarteras = new FusionCarteras();
	        entradaServicioFusionCarteras.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			entradaServicioFusionCarteras.p_IdUsuario = (Long)parameters.get("IdUsuario");
			entradaServicioFusionCarteras.p_IdModulo = "CUS";
		        			
			salidaServicioFusionCarteras = servicioFusionCarteras.logica(con, entradaServicioFusionCarteras);
            
			salida.P_OUT_MENSAJES = salidaServicioFusionCarteras.P_OUT_MENSAJES;	
			salida.P_OUT_ERRORESCONTADOR = salidaServicioFusionCarteras.P_OUT_ERRORESCONTADOR;
			
			salida.P_OUT_MENSAJE = "";
			salidaServicioFusionCarteras.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
			
			
			log.write("ERRORES: ", salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			
			//for( MensajeSalida mensaje: salida.P_OUT_MENSAJES) {
			//	log.write(mensaje.descripcion);
			//}
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje : mensajes) log.write(mensaje);
			
			
		}catch(Exception e){
			log.write(e);
    		log.write("***** Error PautaFusionCartera *****");
    		throw e;
		}
        log.write("***** Fin PautaFusionCartera *****");
		return salida;
    }

	
}
