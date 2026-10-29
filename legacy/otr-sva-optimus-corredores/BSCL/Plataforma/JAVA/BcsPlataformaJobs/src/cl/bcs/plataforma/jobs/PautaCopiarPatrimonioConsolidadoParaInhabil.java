package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil;
import cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil.EntradaCopiarPatrimonioConsolidadoParaInhabil;
import cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil.SalidaCopiarPatrimonioConsolidadoParaInhabil;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class PautaCopiarPatrimonioConsolidadoParaInhabil implements BcsProcesoPauta{
	
	static final Logger logger = LogManager.getLogger(PautaCopiarPatrimonioConsolidadoParaInhabil.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaCopiarPatrimonioConsolidadoParaInhabil *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		
    		
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
    		CopiarPatrimonioConsolidadoParaInhabil copiarPatrimonioConsolidadoParaInhabil = new CopiarPatrimonioConsolidadoParaInhabil();
    		EntradaCopiarPatrimonioConsolidadoParaInhabil entradaCopiarPatrimonioConsolidadoParaInhabil = new EntradaCopiarPatrimonioConsolidadoParaInhabil();
    		
    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdModulo  = "PLA";
    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_Fecha 	  = fechaSistema;
    		SalidaCopiarPatrimonioConsolidadoParaInhabil salidaCopiarPatrimonioConsolidadoParaInhabil = copiarPatrimonioConsolidadoParaInhabil.logica(con, entradaCopiarPatrimonioConsolidadoParaInhabil);

    		
    		if(salidaCopiarPatrimonioConsolidadoParaInhabil.P_OUT_ERRORESCONTADOR > 0) {
    			salida.P_OUT_MENSAJE = "Error al copiar patrimonio consolidado de dia habil a inhabil";
    		}else {
    			salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente";
    		}
    		salida.P_OUT_ERRORESCONTADOR = salidaCopiarPatrimonioConsolidadoParaInhabil.P_OUT_ERRORESCONTADOR;
    		
    		
    	} catch (Exception e) {
    		logger.error("***** Error PautaCopiarPatrimonioConsolidadoParaInhabil *****",e);
    		throw e;
    	}
    	logger.info("***** Fin PautaCopiarPatrimonioConsolidadoParaInhabil *****");
		return salida;
    		 
    }

}


