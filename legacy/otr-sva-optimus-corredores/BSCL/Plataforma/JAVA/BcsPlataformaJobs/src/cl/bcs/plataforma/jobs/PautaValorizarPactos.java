package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarCarteraPactos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarPactos implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarPactos"); 

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaValorizarPactos *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraPactos.ValorizarCarteraPactosEntrada valorizarCarteraPactosParametros = new ValorizarCarteraPactos.ValorizarCarteraPactosEntrada(); 
			
			valorizarCarteraPactosParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraPactosParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraPactosParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraPactosParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraPactosParametros.p_SimularOperacion = "N";
			valorizarCarteraPactosParametros.p_EsPauta = "S";
			
			ValorizarCarteraPactos valorizarCarteraPactos = new ValorizarCarteraPactos();
			ValorizarCarteraPactos.ValorizarCarteraPactosSalida valorizarCarteraPactosOut = valorizarCarteraPactos.logica(con, valorizarCarteraPactosParametros);
			salida.P_OUT_MENSAJE = valorizarCarteraPactosOut.P_OUT_MENSAJE;
			if(salida.P_OUT_MENSAJE!=null && salida.P_OUT_MENSAJE.toLowerCase().contains("error")) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}else {
				salida.P_OUT_ERRORESCONTADOR = valorizarCarteraPactosOut.P_OUT_ERRORESCONTADOR;
			}
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValorizarPactos *****");
    		throw e;
    	}
		log.write("***** Fin PautaValorizarPactos *****");
		return salida;
    }

    
}