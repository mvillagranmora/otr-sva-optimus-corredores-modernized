package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.facturacion.NotificarCargoGananciaCapital;
import cl.bcs.facturacion.NotificarCargoGananciaCapital.NotificarCargoGananciaCapital_Entrada;
import cl.bcs.facturacion.NotificarCargoGananciaCapital.NotificarCargoGananciaCapital_Salida;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		
 **********************************/
public class PautaNotificacionCargoGananciaCapital implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaNotificacionCargoGananciaCapital");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaNotificacionCargoGananciaCapital *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");

    		log.write("Fecha sistema: ",fechaSistema);
    		NotificarCargoGananciaCapital servicioNotificarCargoGananciaCapital = new NotificarCargoGananciaCapital();
    		NotificarCargoGananciaCapital_Entrada entradaNotificarCargoGananciaCapital = new NotificarCargoGananciaCapital_Entrada();
    		
    		entradaNotificarCargoGananciaCapital.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entradaNotificarCargoGananciaCapital.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		entradaNotificarCargoGananciaCapital.p_IdModulo  = "PLA";
    		entradaNotificarCargoGananciaCapital.p_FechaEjecucion = fechaSistema;    		
    	
    		NotificarCargoGananciaCapital_Salida salidaNotificarCargoGananciaCapital = servicioNotificarCargoGananciaCapital.logica(con, entradaNotificarCargoGananciaCapital);
    		
    		salida.P_OUT_MENSAJES = salidaNotificarCargoGananciaCapital.P_OUT_MENSAJES;
			salida.P_OUT_ERRORESCONTADOR = salidaNotificarCargoGananciaCapital.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = "";
			salidaNotificarCargoGananciaCapital.P_OUT_MENSAJES.parallelStream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);			
			
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaNotificacionCargoGananciaCapital *****");
    		throw e;
    	}
		log.write("***** Fin PautaNotificacionCargoGananciaCapital *****");
		return salida;
    		  
    }

	 public static void main(String[] args) throws Throwable {
		 System.out.println("== START ==");
		 
		Connection con = PlataformaUtils.getDESAConnection();
		PautaNotificacionCargoGananciaCapital pncs = new PautaNotificacionCargoGananciaCapital();
				
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa",Long.parseLong("1"));
		params.put("IdUsuario",Long.parseLong("4"));
		params.put("IdModulo","PLA");
		params.put("FechaEjecucion", "2022-07-27");
		System.out.println(pncs.transaction(con,params));			    
		con.commit();
		//con.rollback();
		System.out.println("== END ==");
			  	  
	}
}