package cl.bcs.normativa.circulares;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.ws.CargarArchivoTasas;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsolidarClientes implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarArchivoTasas.class);
	
	private String SQL_SELECT_PRIMERA_CARGA = loadResourceToString("cl/bcs/normativa/circulares/sql/ConsolidarClientes_SelectPrimeraCarga.sql");
	private String SQL_ACTUALIZAR_PRIMERA_CARGA = loadResourceToString("cl/bcs/normativa/circulares/sql/ConsolidarClientes_ActualizarPrimeraCarga.sql");
	private String SQL_CALL_CONSOLIDAR_CLIENTES = loadResourceToString("cl/bcs/normativa/circulares/sql/ConsolidarClientes_CallConsolidarClientes.sql");
	
	public static class EntradaConsolidarClientes extends BcsPojo {
		public Long p_IdEmpresa;
	}
	
	public static class SalidaConsolidarClientes extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConsolidarClientes() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsolidarClientes jin = gson.fromJson(in.toString(), EntradaConsolidarClientes.class);
		SalidaConsolidarClientes out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsolidarClientes logica(Connection con, EntradaConsolidarClientes entrada) throws Exception {
		logger.info("INICIO::ConsolidarClientes");
		
		SalidaConsolidarClientes out = new SalidaConsolidarClientes();
		boolean esPrimeraCarga = esPrimeraCarga(con);
		consolidar(con);
		
		if(esPrimeraCarga) {
			actualizarPrimeraCarga(con);
		}
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CONSOLIDAR_CLIENTES_MENSAJE_EXITOSO", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
		
		logger.info("FIN::ConsolidarClientes");
		
		return out;
	}
	
	private boolean esPrimeraCarga(Connection con) throws Exception {
		logger.trace("INICIO::ConsolidarClientes.esPrimeraCarga");
		
		SQL sql = new SQL();
		Long cantidad = sql.countQuery(con, SQL_SELECT_PRIMERA_CARGA, new HashMap<String, Object>(2));
		
		logger.trace("FIN::ConsolidarClientes.esPrimeraCarga");
		
		return cantidad == 0;
	}
	
	@SuppressWarnings("rawtypes")
	private void consolidar(Connection con) throws Exception {
		logger.trace("INICIO::ConsolidarClientes.consolidar");
		SQL sql = new SQL();
		HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(1);
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(1);
		
		sql.call(con, SQL_CALL_CONSOLIDAR_CLIENTES, parametrosEntrada, parametrosSalida);
		logger.trace("FIN::ConsolidarClientes.consolidar");
	}
	
	private void actualizarPrimeraCarga(Connection con) throws Exception {
		logger.trace("INICIO::ConsolidarClientes.actualizarPrimeraCarga");
		SQL.exec(con, SQL_ACTUALIZAR_PRIMERA_CARGA);
		logger.trace("FIN::ConsolidarClientes.actualizarPrimeraCarga");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaConsolidarClientes Objin = new EntradaConsolidarClientes();
		Objin.p_IdEmpresa = 1L;
		ConsolidarClientes demo = new ConsolidarClientes();				
		try {
			SalidaConsolidarClientes ObjOut = demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	} 
}
