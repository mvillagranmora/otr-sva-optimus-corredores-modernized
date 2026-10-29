package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GenerarReporteAcreedores;
import cl.bcs.cuentaInversion.GenerarReporteAcreedores.EntradaGenerarReporteAcreedores;
import cl.bcs.cuentaInversion.GenerarReporteAcreedores.SalidaGenerarReporteAcreedores;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class GenerarReporteAcreedoresJob extends BcsJob implements JavaCall{
	
	private AtomicBoolean running = new AtomicBoolean(false);
	private final static Logger logger = LogManager.getLogger(GenerarReporteAcreedoresJob.class);
	
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = loadResourceToString("cl/bcs/cuentaInversion/jobs/sql/GenerarReporteAcreedoresJob_UpdateRutaSalidaProgramaBatch.sql");
	
	public static class ProgramaBatch extends BcsPojo{
		public Long 	IdPrograma;
		public String 	RutaSalida;
		public String 	Comentario;
		public ProgramaBatch(Long idPrograma, String rutaSalida, String Comentario) {
			this.IdPrograma	= idPrograma;
			this.RutaSalida	= rutaSalida;
			this.Comentario	= Comentario;
		}
	}
	
	public boolean logica(Connection con,boolean debug) throws Exception {
		logger.info("INICIO JOB::GenerarReporteAcreedoresJob");
		
		running.set(true);
    	boolean resultado = false;
    	
    	Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
	    String 	fechaSolicitud;
	    String 	idMoneda;
	    String	consolidar;
	    String 	nombreInternoArchivo;
	    
	    idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
    	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
    	idModulo						= "CTA"; 		    
	    fechaSolicitud					= this.getParam("P_FechaSolicitud");
	    idMoneda 						= this.getParam("P_Moneda");
		consolidar						= this.getParam("P_Consolidar");
		nombreInternoArchivo			= this.getParam("NombreInternoArchivo");
		
		GenerarReporteAcreedores servicioGenerarReporteAcreedores = new GenerarReporteAcreedores();
		EntradaGenerarReporteAcreedores entradaGenerarReporteAcreedores = new EntradaGenerarReporteAcreedores();
		
		entradaGenerarReporteAcreedores.p_IdEmpresa				= idEmpresa;
		entradaGenerarReporteAcreedores.p_IdUsuario				= idUsuario;
		entradaGenerarReporteAcreedores.p_IdModulo				= idModulo;
		entradaGenerarReporteAcreedores.p_FechaSolicitud		= fechaSolicitud;
		entradaGenerarReporteAcreedores.p_IdMoneda				= idMoneda;
		entradaGenerarReporteAcreedores.p_Consolidar			= consolidar;
		entradaGenerarReporteAcreedores.p_NombreInternoArchivo	= nombreInternoArchivo;
		
		try{
			SalidaGenerarReporteAcreedores salidaGenerarReporteAcreedores = servicioGenerarReporteAcreedores.logica(con, entradaGenerarReporteAcreedores);
			SQL sql = new SQL();
			List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
			if(salidaGenerarReporteAcreedores.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 
					&& salidaGenerarReporteAcreedores.P_OUT_RUTAARCHIVO.trim().length()!=0 && salidaGenerarReporteAcreedores.P_OUT_CODIGOCORREDOR.trim().length()!= 0){
				programaBatch.add(new ProgramaBatch(this.getJobId(), salidaGenerarReporteAcreedores.P_OUT_RUTAARCHIVO, salidaGenerarReporteAcreedores.P_OUT_CODIGOCORREDOR));
				actualizarRutaPrograma(con, sql, programaBatch);
			}
			else {
				MensajeSalida Mensaje = salidaGenerarReporteAcreedores.P_OUT_MENSAJES.get(0);
				programaBatch.add(new ProgramaBatch(this.getJobId(), "", Mensaje.descripcion));
				actualizarRutaPrograma(con, sql, programaBatch);
			}
			resultado = true;
		}catch(Exception e){
			con.rollback();
			logger.error(e.getMessage());
			throw e;
		}
		finally {
			running.set(false);
		}
		
    	
    	logger.info("FIN JOB::GenerarReporteAcreedoresJob");
		return resultado;
		
	}
	
	private void actualizarRutaPrograma (Connection con, SQL sql, List<ProgramaBatch> programaBatch) throws SQLException {
		logger.debug("INICIO::GenerarReporteAcreedoresJob.actualizarRutaPrograma");
		long inicio = System.currentTimeMillis();
		
		sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAcreedoresJob.actualizarRutaPrograma tiempo: " + (fin - inicio));
	}

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
		logger.error("****** ERROR  *******");
		logger.error(t.getMessage());
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

}
