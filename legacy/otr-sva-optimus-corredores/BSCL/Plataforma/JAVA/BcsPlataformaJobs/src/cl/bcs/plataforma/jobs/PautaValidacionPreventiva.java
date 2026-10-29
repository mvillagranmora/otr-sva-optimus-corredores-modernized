package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.plataforma.ccl.ValidacionPreventiva;
import cl.bcs.plataforma.ccl.ValidacionPreventiva.ValidacionPreventivaEntrada;
import cl.bcs.plataforma.ccl.ValidacionPreventiva.ValidacionPreventivaSalida;
import cl.bcs.utils.Log;

public class PautaValidacionPreventiva implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValidacionPreventiva");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaValidacionPreventiva *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		ValidacionPreventiva validacionPreventiva = new ValidacionPreventiva();
    		ValidacionPreventivaEntrada entrada = new ValidacionPreventivaEntrada();
    		entrada.p_IdEmpresa= (long) parameters.get("IdEmpresa");
    		entrada.p_IdUsuario= (long) parameters.get("IdUsuario");
    		entrada.p_IdModulo= parameters.get("IdModulo").toString();
    		entrada.p_FechaSistemaEjecucion = parameters.get("FechaSistema").toString();
    		List<ValidacionPreventivaSalida> resultado = validacionPreventiva.logica(con, entrada);
    		
    		salida.P_OUT_MENSAJE = String.format("RESULTADO VALIDACION PREVENTIVA: %s", 
    				((resultado.stream().filter(p -> p.Estado.equalsIgnoreCase("NO OK")).collect(Collectors.toList()).size() > 0)) ? "NOK" : "OK");
    		salida.P_OUT_ERRORESCONTADOR = 0L;
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValidacionPreventiva *****");
    		throw e;
    	}
    	log.write("***** Fin PautaValidacionPreventiva *****");
		return salida;
	}

}
