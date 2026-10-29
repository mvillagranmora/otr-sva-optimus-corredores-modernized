package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.GuardarPatrimonioConsolidado;
import cl.bcs.custodia.GuardarPatrimonioConsolidado.EntradaGuardarPatrimonioConsolidado;
import cl.bcs.custodia.GuardarPatrimonioConsolidado.SalidaGuardarPatrimonioConsolidado;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaGuardarPatrimonioConsolidado implements BcsProcesoPauta{
	
	static final Logger logger = LogManager.getLogger(PautaGuardarPatrimonioConsolidado.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaGuardarPatrimonioConsolidado *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
    		GuardarPatrimonioConsolidado guardarPatrimonioConsolidado = new GuardarPatrimonioConsolidado();
    		EntradaGuardarPatrimonioConsolidado entradaGuardarPatrimonioConsolidado = new EntradaGuardarPatrimonioConsolidado();
    		entradaGuardarPatrimonioConsolidado.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entradaGuardarPatrimonioConsolidado.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		entradaGuardarPatrimonioConsolidado.p_IdModulo = "PLA";
    		entradaGuardarPatrimonioConsolidado.p_Fecha = fechaSistema;
    		SalidaGuardarPatrimonioConsolidado salidaGuardarPatrimonioConsolidado = guardarPatrimonioConsolidado.logica(con, entradaGuardarPatrimonioConsolidado);
    		if(salidaGuardarPatrimonioConsolidado.P_OUT_ERRORESCONTADOR > 0) {
    			salida.P_OUT_MENSAJE = "Error al guardar patrimonio consolidado";
    		}else {
    			salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente";
    		}
    		salida.P_OUT_ERRORESCONTADOR = salidaGuardarPatrimonioConsolidado.P_OUT_ERRORESCONTADOR;
    	} catch (Exception e) {
    		logger.error("***** Error PautaGuardarPatrimonioConsolidado *****",e);
    		throw e;
    	}
    	logger.info("***** Fin PautaGuardarPatrimonioConsolidado *****");
		return salida;
    		 
    }
	
	public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa", "connections.properties")){
			PautaGuardarPatrimonioConsolidado ipb = new PautaGuardarPatrimonioConsolidado();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    con.commit();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

}
