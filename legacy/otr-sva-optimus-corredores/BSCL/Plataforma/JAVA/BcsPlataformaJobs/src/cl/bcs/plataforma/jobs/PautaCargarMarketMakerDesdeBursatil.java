
package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;
import cl.bcs.plataforma.ins.CargarMarketMakerDesdeBursatil;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaCargarMarketMakerDesdeBursatil implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaCargarMarketMakerDesdeBursatil");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaCargarMarketMakerDesdeBursatil *****");
		SalidaProcesoPauta salidaPau = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
			CargarMarketMakerDesdeBursatil.Entrada parametros = new CargarMarketMakerDesdeBursatil.Entrada();
			
			parametros.p_FechaSistema = confFechaSistema;
			parametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametros.p_IdModulo = "CUS";
			
			CargarMarketMakerDesdeBursatil carga = new CargarMarketMakerDesdeBursatil();
			CargarMarketMakerDesdeBursatil.Salida salida = carga.logica(con, parametros);
	
			salidaPau.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
			salidaPau.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
			salidaPau.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaPau.P_OUT_MENSAJES);
	

			log.write("Errores: " + salidaPau.P_OUT_ERRORESCONTADOR);

		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaCargarMarketMakerDesdeBursatil *****");
			throw e;
		}
		log.write("***** Fin PautaCargarMarketMakerDesdeBursatil *****");
		return salidaPau;
	}

}



