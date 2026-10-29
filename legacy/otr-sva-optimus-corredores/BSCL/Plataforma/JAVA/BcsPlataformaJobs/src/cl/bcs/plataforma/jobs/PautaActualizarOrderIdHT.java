package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.APV.ValidarMandatosPAC.entrada_ValidarMandatosPAC;
import cl.bcs.ordenes.utils.ActualizarOrderIdHT;
import cl.bcs.ordenes.utils.ActualizarOrderIdHT.Entrada_ActualizarOrderIdHT;
import cl.bcs.ordenes.utils.ActualizarOrderIdHT.Salida_ActualizarOrderIdHT;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaActualizarOrderIdHT implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaActualizarOrderIdHT");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaActualizarOrderIdHT *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		ActualizarOrderIdHT actualizar = new ActualizarOrderIdHT();
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");

    		long idEmpresa = (long) parameters.get("IdEmpresa");
    		long idUsuario = (long) parameters.get("IdUsuario");
    		Entrada_ActualizarOrderIdHT entrada = new Entrada_ActualizarOrderIdHT();
    		entrada.p_IdEmpresa = idEmpresa;
    		entrada.p_FechaDesde = confFechaSistema;
    		entrada.p_FechaHasta = confFechaSistema;
    		entrada.p_IdModulo = "PLA";
    		entrada.p_IdUsuario = idUsuario;
			Salida_ActualizarOrderIdHT salidaActualizar = actualizar.logica(con, entrada);
    		
    		salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente.";
    		salida.P_OUT_ERRORESCONTADOR = salidaActualizar.P_OUT_ERRORESCONTADOR;
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaActualizarOrderIdHT *****");
    		throw e;
    	}
    	log.write("***** Fin PautaActualizarOrderIdHT *****");
		return salida;
	}
	
	public static void main(String[] args) throws Throwable {
		PautaActualizarOrderIdHT p = new PautaActualizarOrderIdHT();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("IdEmpresa", 1L);
		parameters.put("IdUsuario", 0L);
		Connection c  = PlataformaUtils.getPropertiesConnection("bice","connections.properties");
		SalidaProcesoPauta asdf = p.transaction(c, parameters);
		System.out.println(asdf.toString());
	}
}
