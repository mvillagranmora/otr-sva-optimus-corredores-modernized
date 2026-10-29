package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.CambiarEstadoSistema;
import cl.bcs.plataforma.pla.DeshabilitaTokenUsuarios;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		jgarces
 **********************************/
public class PautaAbrirSistema implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaAbrirSistema");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaAbrirSistema *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			String confFechaSistemaAnterior = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistemaAnterior");
			
			CambiarEstadoSistema cestsis = new CambiarEstadoSistema();
			CambiarEstadoSistema.InCambiarEstadoSistema incestsis = new CambiarEstadoSistema.InCambiarEstadoSistema();
			incestsis.IdEmpresa = (Long)parameters.get("IdEmpresa");
			incestsis.FechaSistema = confFechaSistemaAnterior;
			CambiarEstadoSistema.OutCambiarEstadoSistema outcestmod = cestsis.logica(con, incestsis);
			
			DeshabilitaTokenUsuarios deshabilitaTokenUsuarios = new DeshabilitaTokenUsuarios();
			DeshabilitaTokenUsuarios.EntradaServicio entradaServicio = new DeshabilitaTokenUsuarios.EntradaServicio();
			entradaServicio.IdEmpresa = (Long)parameters.get("IdEmpresa");
			DeshabilitaTokenUsuarios.ServicioSalida servicioSalida = deshabilitaTokenUsuarios.logica(con, entradaServicio);
			
			ConfiguracionSistema.limpiarParametros();
	
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(outcestmod.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = outcestmod.P_OUT_ERRORESCONTADOR;
			
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaAbrirSistema *****");
    		throw e;
    	}
		log.write("***** Fin PautaAbrirSistema *****");
		return salida;
    }

   
}