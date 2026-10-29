package cl.bcs.facturacion.jobs.dte;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.dte.CallerWSDte;
import cl.bcs.facturacion.dte.SendDte;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class Dte extends BcsJob implements JavaCall {
	private CallerWSDte callDte = null;
	private static final Logger logger = LogManager.getLogger(Dte.class);
	
	// ---------------------------------------------------------------
	// QUERYs
	// ---------------------------------------------------------------
	private final String SQL_SELECT_TAREA_DTE 	= "SELECT DISTINCT \"IdTareaDTEGrupo\" FROM \"BSCL.Facturacion.BD::FAC.TareaDTE\" WHERE \"IdEstadoTareaDTE\" = 'PEN'";
	
	
	// ---------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------
    public boolean procesar(Connection con, boolean debug) throws InterruptedException, Throwable{
		logger.info(":DTE:  ****** INICIO  *******");

		SQL sql = new SQL();
		Long idTareaDTEGrupo = 0L;

		if (debug) {
			idTareaDTEGrupo = 10739L;
		} else {
			idTareaDTEGrupo = Long.parseLong(super.getParam("IdTareaDTEGrupo"));
		}

		SendDte c = new SendDte();

		c.procesar(con, idTareaDTEGrupo);

		logger.info(":DTE:  ****** FIN  *******");
		return true;
    }	
	
	// ------------------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ------------------------------------------------------------------------------------------------------	
	public static void main(String[] args) throws InterruptedException, Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Dte demo = new Dte();
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
    public boolean transaction(Connection con) throws InterruptedException, Throwable{
		return procesar(con, false);
		
	}
	
    @Override
    public void onError(Throwable t) {
    	logger.error(t);
    }

	@Override
	public void pause() {

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
		
}
