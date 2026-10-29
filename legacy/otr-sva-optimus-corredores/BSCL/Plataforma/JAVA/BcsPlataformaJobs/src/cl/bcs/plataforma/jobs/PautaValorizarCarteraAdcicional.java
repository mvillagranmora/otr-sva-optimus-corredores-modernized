package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch;
import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch.InValorizacionValoresAdicionales;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarCarteraAdcicional implements BcsProcesoPauta {
	private static final Logger logger = LogManager.getLogger(PautaValorizarCarteraAdcicional.class);

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.debug("***** Inicio PautaValorizarCarteraAdcicional *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));

			InValorizacionValoresAdicionales entrada = new InValorizacionValoresAdicionales();			
			entrada.p_IdModulo  					= "CUS";
			entrada.p_IdEmpresa 					= (Long)parameters.get("IdEmpresa");
			entrada.p_IdUsuario 					= (Long)parameters.get("IdUsuario");
			entrada.p_FechaSistema  				= confFechaSistema;
			entrada.p_IdPais      					= empresa.IdPais;
			entrada.p_IdPortafolio      			= 0L;
			entrada.p_ValorizaCarteraParaInterfaces = "S";
		    ValorizacionValoresAdicionalesBatch  kk = new ValorizacionValoresAdicionalesBatch();
		    ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales salidaValoresAdicionales= kk.logica(con, entrada);
		  
    		logger.debug("***** "+salidaValoresAdicionales.P_OUT_MENSAJE+" *****");
    		/*public List<MensajeSalida>	P_OUT_MENSAJES;	//DETALLES
    		public String				P_OUT_MENSAJE;	//RESUMEN
    		public Long					P_OUT_ERRORESCONTADOR = 0L;*/
    		salida.P_OUT_ERRORESCONTADOR = salidaValoresAdicionales.P_OUT_ERRORESCONTADOR;
    		salida.P_OUT_MENSAJE = salidaValoresAdicionales.P_OUT_MENSAJE;
		    
    	} catch (Exception e) {
    		logger.debug(e);
    		logger.debug("***** Error PautaValorizarCarteraAdcicional *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaValorizarCarteraAdcicional *****");
		return salida;
    		  
    }
 

    
}