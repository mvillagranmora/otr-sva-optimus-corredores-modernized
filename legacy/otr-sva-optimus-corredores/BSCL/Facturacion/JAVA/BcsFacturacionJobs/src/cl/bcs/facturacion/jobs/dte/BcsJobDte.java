package cl.bcs.facturacion.jobs.dte;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.dte.CallerWSDte;
import cl.bcs.facturacion.dte.SendDte;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class BcsJobDte extends BcsJob implements JavaCall {
	private static final Logger logger = LogManager.getLogger(BcsJobDte.class);
	String IdTipoPrograma = "EnviarDteJob";
	
	// ---------------------------------------------------------------
	// QUERYs
	// ---------------------------------------------------------------
	private final static String SQL_SELECT_TAREA_DTE = "SELECT DISTINCT \"IdTareaDTEGrupo\" FROM \"BSCL.Facturacion.BD::FAC.TareaDTE\" WHERE \"IdEstadoTareaDTE\" = 'PEN' AND \"FechaEnvioDte\" > ADD_SECONDS (CURRENT_TIMESTAMP, -3600) ORDER BY \"IdTareaDTEGrupo\" DESC;";
	
	
	// ---------------------------------------------------------------
	// CLASES
	// ---------------------------------------------------------------
	
	public static class TareaDteGrupo {		
		//		TIPO VAL			NOMBRE					               in|out	- Definicion		
		//-------------------------------------------------------------------------------------------------------	
		public Long					IdTareaDTEGrupo;		            // E|S 	- Id agrupador de Tarea DTE, se inicializa en cada nuevo envio

		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	// ---------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------
    public boolean procesar(Connection con, boolean debug) throws InterruptedException, Throwable{
		logger.info(":JOBDTE:  ****** INICIO  *******");

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		List<TareaDteGrupo> ListIdTareaDTE = sql.query(con, SQL_SELECT_TAREA_DTE, params, TareaDteGrupo.class);
	
		SendDte c = new SendDte();
		for (TareaDteGrupo id : ListIdTareaDTE) {
			c.procesar(con, id.IdTareaDTEGrupo);
		}
		
		logger.info(":JOBDTE:  ****** FIN  *******");
		return true;
    }

	// ------------------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ------------------------------------------------------------------------------------------------------	
	public static void main(String[] args) throws InterruptedException, Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				BcsJobDte demo = new BcsJobDte();
				demo.procesar(con, true);
				//con.commit();
				con.rollback();
				System.out.println("SALIDA");
			} catch (Throwable er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
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