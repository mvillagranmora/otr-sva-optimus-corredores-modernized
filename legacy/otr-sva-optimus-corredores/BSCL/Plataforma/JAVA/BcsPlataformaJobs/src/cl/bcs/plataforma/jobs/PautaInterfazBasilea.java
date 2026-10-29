package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.contabilidad.ProcesarInterfazBasilea;
import cl.bcs.contabilidad.ProcesarInterfazBasilea.EntradaProcesarInterfazBasilea;
import cl.bcs.contabilidad.ProcesarInterfazBasilea.SalidaProcesarInterfazBasilea;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaInterfazBasilea implements BcsProcesoPauta {

	private final Logger log = LogManager.getLogger(PautaInterfazBasilea.class);

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.info("***** Inicio PautaInterfazBasilea *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			EntradaProcesarInterfazBasilea entrada = new EntradaProcesarInterfazBasilea();
			entrada.p_IdEmpresa = (Long) parameters.get("IdEmpresa");
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = (Long) parameters.get("IdUsuario");

			ProcesarInterfazBasilea procesarInterfazBasilea = new ProcesarInterfazBasilea();
			SalidaProcesarInterfazBasilea salidaInterfazBasilea = procesarInterfazBasilea.logica(con, entrada);
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaInterfazBasilea.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = salidaInterfazBasilea.P_OUT_ERRORESCONTADOR;
		} catch (Exception e) {
			log.info(e);
			log.info("***** Error PautaInterfazBasilea *****");
			throw e;
		}
		log.info("***** Fin PautaInterfazBasilea *****");
		return salida;
	}
	
	public static void main(String[] args) throws InterruptedException, Throwable {
		System.out.println("== START ==");
		Connection con = PlataformaUtils.getPropertiesConnection("desa02", "C:\\optimus-corredores\\connections.properties");
		PautaInterfazBasilea demo = new PautaInterfazBasilea();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("IdEmpresa", 1L);
		parameters.put("IdUsuario", 97L);
		System.out.println(demo.transaction(con,parameters));	      
		con.commit();
		System.out.println("== END ==");
	}

}
