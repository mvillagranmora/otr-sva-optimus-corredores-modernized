package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TestAgendaJob extends BcsJob implements JavaCall {	

	private static final Logger logger = LogManager.getLogger(TestAgendaJob.class);
	
	public static class TestAgendaEntrada {
		
	}
	
	public static class TestAgendaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public TestAgendaSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		throw new Exception();
	}
	
	public static void main(String[] args) throws SQLException, Exception {
		
	    TestAgendaJob demo=new TestAgendaJob();
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa","connections.properties")) {
	    	TestAgendaEntrada entrada = new TestAgendaEntrada();
	    	TestAgendaSalida salida = demo.logica(conn, entrada);
			conn.commit();
	    }
	        
	}
	
	
	public class FechaHora {
		public String FechaHora;
	}
	
	public TestAgendaSalida logica(Connection conn, TestAgendaEntrada entrada) throws Exception {
		TestAgendaSalida out = new TestAgendaSalida();
		SQL _sql = new SQL();
		
		List<FechaHora> f = _sql.query(conn, "SELECT '' || CURRENT_TIMESTAMP as \"FechaHora\" FROM DUMMY", new HashMap<>(), FechaHora.class);
		FechaHora ff = f.get(0);
		logger.info("TestAgenda: " + ff.FechaHora);
		return new TestAgendaSalida();
		
	}

	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		this.logica(con, new TestAgendaEntrada());
		return true;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	
}
