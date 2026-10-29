package cl.bcs.plataforma.jobs;
  

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.utils.Log;

public class PautaEstadisticaOperacionCliente implements BcsProcesoPauta {
	
	private final Log log = new Log("PLA.PautaEstadisticaOperacionCliente");
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaEstadisticaOperacionCliente *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			CallableStatement sp = null;
			final String callSp = "CALL \"BSCL.Normativa.BD.Servicios::NOR.ProcesarTareaPauta\"(?, ?, ?)";
			final String IdTipoTarea = "ESTADISTICAOPERACIONCLIENTE";		
			final long idEmpresa = (Long)parameters.get("IdEmpresa");
			sp = con.prepareCall(callSp);
			sp.setLong(1, idEmpresa);
			sp.setString(2, IdTipoTarea);
			sp.setLong(3, (Long)parameters.get("IdEjecucion"));
			long milis = System.currentTimeMillis();
			sp.execute();
	
			salida.P_OUT_ERRORESCONTADOR = 0L;
			salida.P_OUT_MENSAJE = "El proceso PautaEstadisticaOperacionCliente se ha ejecutado correctamente";
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaEstadisticaOperacionCliente *****");
    		throw e;
    	}
		log.write("***** Fin PautaEstadisticaOperacionCliente *****");
		return salida;
        	
	}
}