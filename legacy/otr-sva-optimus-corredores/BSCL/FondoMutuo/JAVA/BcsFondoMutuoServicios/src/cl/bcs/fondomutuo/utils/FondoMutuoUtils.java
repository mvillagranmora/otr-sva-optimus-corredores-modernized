package cl.bcs.fondomutuo.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;

public class FondoMutuoUtils {
	
	static final Logger logger = LogManager.getLogger(FondoMutuoUtils.class);
	
	private static final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static Boolean esFlujoCustodiaParaSolicitudProceso(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia, SolicitudAporteRescate solicitud) {
		logger.trace("INICIO::FondoMutuoUtils.obtenerFlujoCustodia");
		long inicio = System.currentTimeMillis();
		Boolean esFlujoCustodiaParaSolicitudProceso = flujoCustodia.IdPortafolio.equals(solicitud.IdPortafolio) && flujoCustodia.IdInstrumento.equals(solicitud.IdInstrumento) && (esRegimenTributarioVacio(solicitud.IdRegimenTributario) || solicitud.IdRegimenTributario.equals(flujoCustodia.IdRegimenTributario));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FondoMutuoUtils.obtenerFlujoCustodia", fin - inicio);
		return esFlujoCustodiaParaSolicitudProceso;
	}
	
	public static Boolean esRegimenTributarioVacio(String idRegimenTributario) {
		logger.trace("INICIO::FondoMutuoUtils.esRegimenTributarioVacio");
		Boolean esRegimenTributarioVacio = idRegimenTributario == null || idRegimenTributario.isEmpty();
		logger.trace("FIN::FondoMutuoUtils.esRegimenTributarioVacio");
		return esRegimenTributarioVacio;
	}
	
	public static Boolean esCanalIngresoWebServices(String idTipoCanalIngreso) {
		return _c.CanalWEB.equals(idTipoCanalIngreso) || _c.CanalEjecutivo.equals(idTipoCanalIngreso);
	}
	
	public static Boolean esCanalIngresoDistintoAWebServices(String idTipoCanalIngreso) {
		return !esCanalIngresoWebServices(idTipoCanalIngreso);
	}

}
