package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecucionContinuaSQL extends BcsJob {

	private Logger logger = LogManager.getLogger(EjecucionContinuaSQL.class);
	
	private final String SQL_ACTUALIZA_NOMINALES = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/jobs/sql/EjecucionContinuaSQL_ActualizaNominales.sql");
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	class ConfiguracionFix {
		String nombreTabla;
		String tipoMensaje;
	}
	
	
	
	public boolean logica(Connection conn,boolean debug) throws Exception {
			
		logger.debug("Inicio Ciclo Ejecucion Batch ");
		SQL.exec(conn,SQL_ACTUALIZA_NOMINALES);
		conn.commit();
		logger.info("Fin Ciclo Ejecucion Batch ");
		Thread.sleep(1000*60);
		return true;
	}
	
	
	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getMBIConnection();
            // ---------------------------------
            try {
            	EjecucionContinuaSQL demo=new EjecucionContinuaSQL();
            	while (true) {
            		demo.logica(con,true);
            		Thread.sleep(5000);
            	}
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
	
}
