package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarCarteraSimultaneas;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarSimultaneas implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarSimultaneas"); 

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaValorizarSimultaneas *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraSimultaneas.ValorizarCarteraSIMEntrada valorizarCarteraSIMParametros = new ValorizarCarteraSimultaneas.ValorizarCarteraSIMEntrada();
			
			valorizarCarteraSIMParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraSIMParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");;
			valorizarCarteraSIMParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraSIMParametros.p_IdUsuario =  (Long)parameters.get("IdUsuario");
			valorizarCarteraSIMParametros.p_SimularOperacion = "N";
			valorizarCarteraSIMParametros.p_EsPauta = "S";
			
			
			ValorizarCarteraSimultaneas valorizarCarteraSimultaneas = new ValorizarCarteraSimultaneas();
			ValorizarCarteraSimultaneas.ValorizarCarteraSIMSalida valorizarCarteraSIMOut = new ValorizarCarteraSimultaneas.ValorizarCarteraSIMSalida();
			
			valorizarCarteraSIMOut = valorizarCarteraSimultaneas.logica(con, valorizarCarteraSIMParametros);
			salida.P_OUT_MENSAJE = valorizarCarteraSIMOut.P_OUT_MENSAJE;
			if(salida.P_OUT_MENSAJE!=null && salida.P_OUT_MENSAJE.toLowerCase().contains("error")) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}else {
				salida.P_OUT_ERRORESCONTADOR = valorizarCarteraSIMOut.P_OUT_ERRORESCONTADOR;
			}
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValorizarSimultaneas *****");
    		throw e;
    	}
		log.write("***** Fin PautaValorizarSimultaneas *****");
		return salida;
    }

    
}