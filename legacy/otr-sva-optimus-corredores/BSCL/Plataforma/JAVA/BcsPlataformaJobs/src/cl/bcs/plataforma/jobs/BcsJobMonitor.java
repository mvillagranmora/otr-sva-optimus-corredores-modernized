package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class BcsJobMonitor extends BcsJob implements JavaCall {
	private static final Logger logger = LogManager.getLogger(BcsJobMonitor.class);
	static String IdTipoPrograma = "JobMonitor";
	
	// ---------------------------------------------------------------
	// QUERYs
	// ---------------------------------------------------------------
	private final static String SQL_UPSERT_HEARTBET = "UPSERT \"BSCL.Plataforma.BD::PLA.Heartbeat\" (\"Id\", \"LastTimestamp\") VALUES (:Id, CURRENT_TIMESTAMP) WITH PRIMARY KEY;";
	

	
	// ---------------------------------------------------------------
	// CLASES
	// ---------------------------------------------------------------
	
	public static class Heartbeat {
	    public String Id;

	    public Heartbeat(String idTipoPrograma) {
	        this.Id = idTipoPrograma;
	    }
	}

	
	private static final List<Heartbeat> idProgram = new ArrayList<>(Arrays.asList(
		    new Heartbeat(IdTipoPrograma)
	));


	// ---------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------
    public boolean procesar(Connection con, boolean debug) throws InterruptedException, Throwable{
		logger.info(":JOBMONITOR:  ****** INICIO  *******");

		SQL sql = new SQL();
		sql.batch(con, SQL_UPSERT_HEARTBET, idProgram, Heartbeat.class);	
		
		logger.info(":JOBMONITOR:  ****** FIN  *******");
		return true;
    }

    
	// ------------------------------------------------------------------------------------------------------
	// XXX
	// ------------------------------------------------------------------------------------------------------	
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return procesar(con, false);
		
	}

	@Override
    public void onError(Throwable t) {
        logger.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}	
		
}