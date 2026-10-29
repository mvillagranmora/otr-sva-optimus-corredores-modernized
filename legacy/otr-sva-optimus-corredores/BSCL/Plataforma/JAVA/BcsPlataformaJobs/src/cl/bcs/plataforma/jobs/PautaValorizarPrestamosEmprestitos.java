package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarCarteraPrestEmpres;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarPrestamosEmprestitos implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarPrestamosEmprestitos"); 

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaValorizarPrestamosEmprestitos *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresEntrada valorizarCarteraPrestEmpreParametros = new ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresEntrada();
			
			valorizarCarteraPrestEmpreParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraPrestEmpreParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraPrestEmpreParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraPrestEmpreParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraPrestEmpreParametros.p_SimularOperacion = "N";
			valorizarCarteraPrestEmpreParametros.p_EsPauta = "S";
			
			ValorizarCarteraPrestEmpres valorizarCarteraPrestEmpres = new ValorizarCarteraPrestEmpres();
			ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresSalida valorizarCarteraPrestEmpresOut = new ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresSalida();
			
			valorizarCarteraPrestEmpresOut = valorizarCarteraPrestEmpres.logica(con, valorizarCarteraPrestEmpreParametros);
			salida.P_OUT_MENSAJE = valorizarCarteraPrestEmpresOut.P_OUT_MENSAJE;
			if(salida.P_OUT_MENSAJE!=null && salida.P_OUT_MENSAJE.toLowerCase().contains("error")) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}else {
				salida.P_OUT_ERRORESCONTADOR = valorizarCarteraPrestEmpresOut.P_OUT_ERRORESCONTADOR;
			}
			
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValorizarPrestamosEmprestitos *****");
    		throw e;
    	}
		log.write("***** Fin PautaValorizarPrestamosEmprestitos *****");
		return salida;
    		
    }

   
}