package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.NetearClientes;
import cl.bcs.custodia.NetearClientes.EntradaNetearClientes;
import cl.bcs.custodia.NetearClientes.SalidaNetearClientes;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PautaNetearCarterasNegativas implements BcsProcesoPauta {
	static final Logger logger 	= LogManager.getLogger(PautaNetearCarterasNegativas.class.getName());	
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("***** Inicio NetearCarterasNegativas *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		salida.P_OUT_ERRORESCONTADOR = 1L;
    	
		NetearClientes n = new NetearClientes();
		EntradaNetearClientes in = new EntradaNetearClientes();
		in.p_IdEmpresa = (Long) parameters.get("IdEmpresa");
		in.p_IdUsuario = (Long) parameters.get("IdUsuario");
		in.p_IdModulo = "CUS";
		SalidaNetearClientes out = n.logica(con, in );
		logger.debug(out.toString());
		logger.info("***** Fin NetearCarterasNegativas *****");
		salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente.";		
		return salida;
	}
	
	
	public static void main(String[] args) throws Throwable {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa",1L);
				params.put("IdUsuario",0L);
				PautaNetearCarterasNegativas dddd = new PautaNetearCarterasNegativas();
				dddd.transaction(con, params);
				con.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
