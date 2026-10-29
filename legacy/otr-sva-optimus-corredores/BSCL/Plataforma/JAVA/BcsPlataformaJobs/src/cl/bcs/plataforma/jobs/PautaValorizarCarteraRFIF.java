package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pau.ValorizarCarteraRFIF;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pau.utils.PautaUtils;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarCarteraRFIF implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarCarteraRFIF"); 
	
    @Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio PautaValorizarCarteraRFIF *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraRFIF.ValorizarCarteraRFIFEntrada valorizarCarteraParametros = new ValorizarCarteraRFIF.ValorizarCarteraRFIFEntrada();
	    	valorizarCarteraParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraParametros.p_SimularOperacion = "N";
			
			ValorizarCarteraRFIF valorizarCartera = new ValorizarCarteraRFIF();
			ValorizarCarteraRFIF.ValorizarCarteraRFIFSalida valorizarCarteraRFIFSalidaOut = new ValorizarCarteraRFIF.ValorizarCarteraRFIFSalida(); 
			
			valorizarCarteraRFIFSalidaOut = valorizarCartera.logica(con, valorizarCarteraParametros);
			salida.P_OUT_ERRORESCONTADOR = valorizarCarteraRFIFSalidaOut.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = valorizarCarteraRFIFSalidaOut.P_OUT_MENSAJE;
			
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);	
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaValorizarCarteraRFIF *****");
			throw e;
		}
		log.write("***** Fin PautaValorizarCarteraRFIF *****");
		return salida;
	}

}