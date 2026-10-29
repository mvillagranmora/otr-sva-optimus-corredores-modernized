package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.replica.IniciarReplica.EntradaIniciarReplica;
import cl.bcs.replica.IniciarReplica.SalidaIniciarReplica;
import cl.bcs.replica.IniciarReplicaFull;
import cl.bcs.utils.Log;

public class PautaIniciarReplicaFull implements BcsProcesoPauta {
	
	private final Log log = new Log("PLA.PautaIniciarReplicaFull");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaIniciarReplicaFull *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			IniciarReplicaFull iniciarReplica = new IniciarReplicaFull();
			EntradaIniciarReplica entrada = new EntradaIniciarReplica();
			entrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		entrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
			SalidaIniciarReplica salidaIniciarReplica = iniciarReplica.logica(con, entrada);
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaIniciarReplica.P_OUT_MENSAJES);
    		salida.P_OUT_ERRORESCONTADOR = salidaIniciarReplica.P_OUT_ERRORESCONTADOR;
    		log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaIniciarReplicaFull *****");
    		throw e;
    	}
		log.write("***** Fin PautaIniciarReplicaFull *****");
		return salida;
	}

}
