package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad.EntradaCargaInicialSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad.SalidaCargaInicialSaldosReporteAntiguedad;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

public class CargaInicialSaldosReporteAntiguedadJob extends BcsJob implements JavaCall{
	AtomicBoolean running = new AtomicBoolean(false);
	
	private final static Logger logger = LogManager.getLogger(CargaInicialSaldosReporteAntiguedadJob.class);

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}

	@Override
	public void onError(Throwable t) {
		logger.info("****** ERROR  *******");
		logger.info(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO JOB");
		running.set(true);
    	boolean resultado = false;
    	
    	Long 	idEmpresa;
        Long 	idUsuario;
        String	fechaSistema;
        
        if (debug) {
        	idEmpresa		= 1L;
        	idUsuario		= 1L;
        	fechaSistema	= "2023-04-19";
        }else {
        	idEmpresa		= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario		= Long.parseLong(this.getParam("IdUsuario"));
        	fechaSistema	= this.getParam("FechaSistema");
        }
    	
        CargaInicialSaldosReporteAntiguedad servicioCargaInicialSaldosReporteAntiguedad = new CargaInicialSaldosReporteAntiguedad();
        EntradaCargaInicialSaldosReporteAntiguedad entradaCargaInicialSaldosReporteAntiguedad = new EntradaCargaInicialSaldosReporteAntiguedad();
        entradaCargaInicialSaldosReporteAntiguedad.p_IdEmpresa		= idEmpresa;
        entradaCargaInicialSaldosReporteAntiguedad.p_IdUsuario		= idUsuario;
        entradaCargaInicialSaldosReporteAntiguedad.p_FechaSistema	= fechaSistema;
        
        SalidaCargaInicialSaldosReporteAntiguedad salidaCargaInicialSaldosReporteAntiguedad = servicioCargaInicialSaldosReporteAntiguedad.logica(con, entradaCargaInicialSaldosReporteAntiguedad);
        
        if(salidaCargaInicialSaldosReporteAntiguedad.P_OUT_ERRORESCONTADOR.equals(0L)) {
        	con.commit();
			resultado = true;
		}else {
			con.rollback();
		}
        
    	running.set(false);
		
		logger.info("FIN JOB");
		return resultado;
	}

}
