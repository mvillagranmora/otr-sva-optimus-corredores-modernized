package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaActualizarParametrosFinancieros implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaActualizarParametrosFinancieros");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaActualizarParametrosFinancieros *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		OrquestadorActualizarParametrosFinancieros orquestador = new OrquestadorActualizarParametrosFinancieros();
    		long idEmpresa = (long) parameters.get("IdEmpresa");
    		boolean resultado = orquestador.logica(con, false, "", idEmpresa);
    		
    		salida.P_OUT_MENSAJE = String.format("ACTUALIZACION DE PARAMETROS FINANCIEROS : %s", (resultado) ? "OK" : "NOK");
    		salida.P_OUT_ERRORESCONTADOR = (resultado) ? 0L : 1L;
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaActualizarParametrosFinancieros *****");
    		throw e;
    	}
    	log.write("***** Fin PautaActualizarParametrosFinancieros *****");
		return salida;
	}
	
	public static void main(String[] args) throws Throwable {
		PautaActualizarParametrosFinancieros p = new PautaActualizarParametrosFinancieros();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("IdEmpresa", 1L);
		Connection c  = PlataformaUtils.getPropertiesConnection("amm");
		p.transaction(c, parameters);
		c.commit();		
	}
}
