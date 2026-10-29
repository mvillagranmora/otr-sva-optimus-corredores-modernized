package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.ccl.ValidacionPreventiva;
import cl.bcs.plataforma.ccl.ValidacionPreventiva.ValidacionPreventivaEntrada;
import cl.bcs.plataforma.ccl.ValidacionPreventiva.ValidacionPreventivaSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class ValidacionPreventivaJob extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ValidacionPreventivaJob.class);
		
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection optimusConn,boolean debug) throws InterruptedException, Throwable {
		logger.info("Inicio job");
		Long idEmpresa = (debug) ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
		Long idUsuario = (debug) ? 0L : Long.parseLong(this.getParam("IdUsuario"));
		String fechaSistemaEjecucion = (debug) ? "2020-09-21" : this.getParam("FechaSistemaEjecucion");
		ValidacionPreventiva v = new ValidacionPreventiva();
		ValidacionPreventivaEntrada in = new ValidacionPreventivaEntrada();
		in.p_IdEmpresa = idEmpresa;
		in.p_IdModulo = "PLA";
		in.p_IdUsuario = idUsuario;
		in.p_FechaSistemaEjecucion = fechaSistemaEjecucion;
		List<ValidacionPreventivaSalida> out = v.logica(optimusConn, in );
		return true;
	}

	@Override
	public void onError(Throwable t) {
		logger.error(t.getMessage());
        logger.error("****** ERROR TAREA  *******");
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			ValidacionPreventivaJob validacion =  new ValidacionPreventivaJob();
			boolean out = validacion.logica(con,true);
			System.out.println(out);
	    }
		 	
	}
}
