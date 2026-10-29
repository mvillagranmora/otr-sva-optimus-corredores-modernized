package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.CartolaConsolidadaHistorico;
import cl.bcs.normativa.cartola.CartolaConsolidadaHistorico.EntradaGuardarCartolaConsolidada;
import cl.bcs.normativa.cartola.CartolaConsolidadaHistorico.SalidaGuardarCartolaConsolidada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class PautaGuardarHistoricoCartolaConsolidada implements BcsProcesoPauta{
	
	static final Logger logger = LogManager.getLogger(PautaGuardarHistoricoCartolaConsolidada.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaGuardarHistoricoCartolaConsolidada *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
    		CartolaConsolidadaHistorico guardarCartolaConsolidadaHistorico = new CartolaConsolidadaHistorico();
    		EntradaGuardarCartolaConsolidada entradaGuardarCartolaConsolidada = new EntradaGuardarCartolaConsolidada();
    		entradaGuardarCartolaConsolidada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entradaGuardarCartolaConsolidada.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		entradaGuardarCartolaConsolidada.p_IdModulo = "NOR";
    		entradaGuardarCartolaConsolidada.p_Fecha = fechaSistema;
    		SalidaGuardarCartolaConsolidada salidaGuardarCartolaConsolidada = guardarCartolaConsolidadaHistorico.logica(con, entradaGuardarCartolaConsolidada);
    		if(salidaGuardarCartolaConsolidada.P_OUT_ERRORESCONTADOR > 0) {
    			salida.P_OUT_MENSAJE = "Error al guardar cartola consolidada";
    		}else {
    			salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente";
    		}
    		salida.P_OUT_ERRORESCONTADOR = salidaGuardarCartolaConsolidada.P_OUT_ERRORESCONTADOR;
    	} catch (Exception e) {
    		logger.error("***** Error PautaGuardarHistoricoCartolaConsolidada *****",e);
    		throw e;
    	}
    	logger.info("***** Fin PautaGuardarHistoricoCartolaConsolidada *****");
		return salida;		 
    }
}