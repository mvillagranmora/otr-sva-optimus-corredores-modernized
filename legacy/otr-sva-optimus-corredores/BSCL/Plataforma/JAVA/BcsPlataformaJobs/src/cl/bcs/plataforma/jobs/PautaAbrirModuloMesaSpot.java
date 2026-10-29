package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.EntradaAbrirModuloMesaSpot;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.Mesa;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.Modulo;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.SalidaAbrirModuloMesaSpot;
import cl.bcs.plataforma.pla.DeshabilitaTokenUsuarios;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
/**********************************
 * @version 	1.0
 * @author 		smedrano
 **********************************/

public class PautaAbrirModuloMesaSpot implements BcsProcesoPauta {
	static final Logger logger = LogManager.getLogger(PautaAbrirModuloMesaSpot.class);
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaAbrirModuloMesaSpot *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			String fechaSistemaAnterior = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistemaAnterior");
			
			CambiarEstadoModuloMesaSpot cestspt = new CambiarEstadoModuloMesaSpot();
			EntradaAbrirModuloMesaSpot incestspt = new EntradaAbrirModuloMesaSpot();
			incestspt.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			incestspt.p_IdUsuario = (Long)parameters.get("IdUsuario");
			incestspt.p_IdModulo = "PLA";
			incestspt.p_simularOperacion = "N";
			incestspt.p_UsaReglasNegocio = "N";
			incestspt.p_FechaSistema = fechaSistema;
			incestspt.p_FechaSistemaAnterior = fechaSistemaAnterior;
			
			SalidaAbrirModuloMesaSpot outcestspt = cestspt.logica(con, incestspt);
			
			salida.P_OUT_MENSAJES = outcestspt.p_out_Mensajes;
			salida.P_OUT_ERRORESCONTADOR = outcestspt.p_out_ErroresContador;
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_MENSAJE = salida.P_OUT_ERRORESCONTADOR + " error(es): ";
				for(MensajeSalida mensaje : outcestspt.p_out_Mensajes)
					salida.P_OUT_MENSAJE = salida.P_OUT_MENSAJE.concat(mensaje.descripcion + ". ");
			} else
				salida.P_OUT_MENSAJE = outcestspt.p_out_Mensaje;
			
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		} catch (Exception e) {
    		logger.info(e);
    		logger.info("***** Error PautaAbrirModuloMesaSpot *****");
    		throw e;
    	}
		logger.info("***** Fin PautaAbrirModuloMesaSpot *****");
		return salida;
    }
}
