package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pau.ValorizarCarteraFFMM;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarFFMM implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaValorizarFFMM.class.getName());

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("Inicio");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			ActualizarCarterasFondosMutuos.EntradaActualizarCarterasFondosMutuos parametrosValorizarFFMM = new ActualizarCarterasFondosMutuos.EntradaActualizarCarterasFondosMutuos();
			parametrosValorizarFFMM.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametrosValorizarFFMM.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametrosValorizarFFMM.p_IdModulo = "PLA";
			
			ActualizarCarterasFondosMutuos valorizarFFMM = new ActualizarCarterasFondosMutuos();
			ActualizarCarterasFondosMutuos.SalidaActualizarCarterasFondosMutuos salidaffmm = valorizarFFMM.logica(con, parametrosValorizarFFMM);
			
			salida.P_OUT_MENSAJES = salidaffmm.P_OUT_MENSAJES;
			salida.P_OUT_ERRORESCONTADOR = salidaffmm.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = "";
			salidaffmm.P_OUT_MENSAJES.parallelStream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
	
			logger.debug("Errores: " + salida.P_OUT_ERRORESCONTADOR);
    	} catch (Exception e) {
    		logger.error(e);
    		throw e;
    	}
		logger.info("***** Fin PautaValorizarFFMM *****");
		return salida;
    }
    
    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getBolsaConnection()){
			PautaValorizarFFMM ipb = new PautaValorizarFFMM();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    con.rollback();
		  }
		  
	}
    
}