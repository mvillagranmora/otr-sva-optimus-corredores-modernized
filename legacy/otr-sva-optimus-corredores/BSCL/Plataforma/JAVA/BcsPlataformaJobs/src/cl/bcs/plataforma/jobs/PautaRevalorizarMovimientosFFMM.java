package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.RevalorizarMovimientosFFMM;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaRevalorizarMovimientosFFMM implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaRevalorizarMovimientosFFMM");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaRevalorizarMovimientosFFMM *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
			RevalorizarMovimientosFFMM.RevalorizarMovimientosFFMMEntrada parametros = new RevalorizarMovimientosFFMM.RevalorizarMovimientosFFMMEntrada();
			parametros.p_FechaSistema = confFechaSistema;
			parametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametros.p_IdModulo = "FMM";
			
			RevalorizarMovimientosFFMM revalorizarMovimientos = new RevalorizarMovimientosFFMM();
			RevalorizarMovimientosFFMM.RevalorizarMovimientosFFMMSalida salidaRevalorizarMovimientos = revalorizarMovimientos.logica(con, parametros);
	
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaRevalorizarMovimientos.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = salidaRevalorizarMovimientos.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaRevalorizarMovimientosFFMM *****");
			throw e;
		}
		log.write("***** Fin PautaRevalorizarMovimientosFFMM *****");
		return salida;
	}

}
