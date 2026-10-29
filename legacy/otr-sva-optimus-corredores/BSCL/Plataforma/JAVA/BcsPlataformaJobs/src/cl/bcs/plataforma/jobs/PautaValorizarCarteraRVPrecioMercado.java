package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.LogManager;

import cl.bcs.plataforma.pau.ValorizarCarteraRV;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarCarteraRVPrecioMercado implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaValorizarCarteraRVPrecioMercado.class.getName());
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraRV.ValorizarCarteraRVEntrada parametrosValorizarRV = new ValorizarCarteraRV.ValorizarCarteraRVEntrada();
			parametrosValorizarRV.p_FechaSistema = confFechaSistema;
			parametrosValorizarRV.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametrosValorizarRV.p_IdPais = empresa.IdPais;
			parametrosValorizarRV.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametrosValorizarRV.p_SimularOperacion = "N";
			parametrosValorizarRV.p_EsPauta = "S";
			
			ValorizarCarteraRV valorizarCarteraRV = new ValorizarCarteraRV();
			ValorizarCarteraRV.ValorizarCarteraRVSalida valorizarCarteraRVOut = new ValorizarCarteraRV.ValorizarCarteraRVSalida();
			valorizarCarteraRVOut = valorizarCarteraRV.logica(con, parametrosValorizarRV);
			salida.P_OUT_MENSAJE = valorizarCarteraRVOut.P_OUT_MENSAJE;
			if(salida.P_OUT_MENSAJE!=null && salida.P_OUT_MENSAJE.toLowerCase().contains("error")) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}else {
				salida.P_OUT_ERRORESCONTADOR = valorizarCarteraRVOut.P_OUT_ERRORESCONTADOR;
			}
	
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("Mensajes: " + salida.P_OUT_MENSAJE);

    	} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaValorizarCarteraRVPrecioMercado *****");
    		throw e;
    	}
		logger.info("***** Fin PautaValorizarCarteraRVPrecioMercado *****");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa", "connections.properties")){
			PautaValorizarCarteraRVPrecioMercado ipb = new PautaValorizarCarteraRVPrecioMercado();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    con.rollback();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

	
}