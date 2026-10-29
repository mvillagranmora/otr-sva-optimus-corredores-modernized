package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pau.ValorizarCarteraCupones;
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
public class PautaValorizarCupones implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarCupones");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio PautaValorizarCupones *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraCupones.ValorizarCarteraCuponesEntrada valorizarCarteraCuponesParametros = new ValorizarCarteraCupones.ValorizarCarteraCuponesEntrada();
			
			valorizarCarteraCuponesParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraCuponesParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraCuponesParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraCuponesParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraCuponesParametros.p_SimularOperacion = "N";
			
			ValorizarCarteraCupones valorizarCarteraCupones = new ValorizarCarteraCupones();
			ValorizarCarteraCupones.ValorizarCarteraCuponesSalida valorizarCarteraCuponesSalidaOut = new ValorizarCarteraCupones.ValorizarCarteraCuponesSalida();
			valorizarCarteraCuponesSalidaOut = valorizarCarteraCupones.logica(con, valorizarCarteraCuponesParametros);
			
			salida.P_OUT_ERRORESCONTADOR = valorizarCarteraCuponesSalidaOut.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = valorizarCarteraCuponesSalidaOut.P_OUT_MENSAJE;

			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaValorizarCupones *****");
			throw e;
		}
		log.write("***** Fin PautaValorizarCupones *****");
		return salida;
    		
    }

    
}