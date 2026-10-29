package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad.EntradaActualizarSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad.SalidaActualizarSaldosReporteAntiguedad;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

public class ActualizarSaldosReporteAntiguedadJob extends BcsJob implements JavaCall{
	 AtomicBoolean running = new AtomicBoolean(false);
	 
	 private final static Logger logger = LogManager.getLogger(ActualizarSaldosReporteAntiguedadJob.class);

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
        String	rut;
	    String	codigoPortafolio;
	    String	moneda;
		String	fechaMovimiento;
		
		if (debug) {
			idEmpresa			= 1L;
        	idUsuario			= 9L;
        	rut					= "15983577-4";
    	    codigoPortafolio	= "0";
    	    moneda				= "CLP";
    		fechaMovimiento		= "2023-03-01";
		}else {
			idEmpresa			= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario			= Long.parseLong(this.getParam("IdUsuario"));
        	rut					= this.getParam("Rut");
    	    codigoPortafolio	= this.getParam("CodigoPortafolio");
    	    moneda				= this.getParam("IdMoneda");
    		fechaMovimiento		= this.getParam("FechaMovimiento");
		}
		
		ActualizarSaldosReporteAntiguedad servicioActualizarSaldosReporteAntiguedad = new ActualizarSaldosReporteAntiguedad();
		EntradaActualizarSaldosReporteAntiguedad entradaActualizarSaldosReporteAntiguedad = new EntradaActualizarSaldosReporteAntiguedad();
		
		entradaActualizarSaldosReporteAntiguedad.p_IdEmpresa		= idEmpresa;
		entradaActualizarSaldosReporteAntiguedad.p_IdUsuario		= idUsuario;
		entradaActualizarSaldosReporteAntiguedad.p_Rut				= rut;
		entradaActualizarSaldosReporteAntiguedad.p_CodigoPortafolio	= codigoPortafolio;
		entradaActualizarSaldosReporteAntiguedad.p_Moneda			= moneda;
		entradaActualizarSaldosReporteAntiguedad.p_FechaMovimiento	= fechaMovimiento;
		
		SalidaActualizarSaldosReporteAntiguedad salidaActualizarSaldosReporteAntiguedad = servicioActualizarSaldosReporteAntiguedad.logica(con, entradaActualizarSaldosReporteAntiguedad);	

		if (salidaActualizarSaldosReporteAntiguedad.P_OUT_ERRORESCONTADOR.equals(0L)) {
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
