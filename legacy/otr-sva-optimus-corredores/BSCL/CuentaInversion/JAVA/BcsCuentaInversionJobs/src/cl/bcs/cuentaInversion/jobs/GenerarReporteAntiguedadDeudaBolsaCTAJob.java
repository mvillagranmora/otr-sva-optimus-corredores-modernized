package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaBolsaCTA;
import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaBolsaCTA.EntradaGenerarReporteAntiguedadDeudaBolsaCTA;
import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaBolsaCTA.SalidaGenerarReporteAntiguedadDeudaBolsaCTA;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class GenerarReporteAntiguedadDeudaBolsaCTAJob extends BcsJob implements JavaCall {
	
	private AtomicBoolean running = new AtomicBoolean(false);
	private final static Logger logger = LogManager.getLogger(GenerarReporteAntiguedadDeudaBolsaCTAJob.class);
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida , \"Comentario\" = :Comentario WHERE \"IdPrograma\" = :IdPrograma ";

	public static class ProgramaBatch{
		public Long 	IdPrograma;
		public String 	RutaSalida;
		public String 	Comentario;
		public ProgramaBatch(Long idPrograma, String rutaSalida, String Comentario) {
			this.IdPrograma 	= idPrograma;
			this.RutaSalida 	= rutaSalida;
			this.Comentario = Comentario;
		}
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
		logger.error(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaBolsaCTAJob");
		running.set(true);
    	boolean resultado = false;
    	
    	Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
	    String 	fechaSolicitud;
	    String 	idMoneda;
	    String 	separaIntermediacion;
	    String 	agrupaPortafolios;
	    String	consolidar;
	    String 	nombreInternoArchivo;
	    
	    if (debug) {
	    	
        	idEmpresa 						= 1L;       
        	idUsuario						= 9L;
        	idModulo						= "CTA";               	    
		    fechaSolicitud 					= "2023-08-29";
		    idMoneda 						= "CLP"; 
			separaIntermediacion			= "S"; 	// S/N
			agrupaPortafolios				= "N";	// S/N 
			consolidar						= "S";	// S/N 
			nombreInternoArchivo			= "ReporteAntiguedadCLPConsolidadoConSeparacion";
        }
        else{
         	idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo						= "CTA"; 		    
		    fechaSolicitud					= this.getParam("P_FechaSolicitud");
		    idMoneda 						= this.getParam("P_Moneda");
		    separaIntermediacion			= this.getParam("P_SeparaIntermediacion");		
			agrupaPortafolios				= this.getParam("P_AgrupaPortafolios");
			consolidar						= this.getParam("P_Consolidar");
			nombreInternoArchivo			= this.getParam("NombreInternoArchivo");	
        }
	    
	    GenerarReporteAntiguedadDeudaBolsaCTA servicioGenerarArchivoSaldoCuentasBolsa = new GenerarReporteAntiguedadDeudaBolsaCTA();
	    EntradaGenerarReporteAntiguedadDeudaBolsaCTA EntradaservicioGenerarArchivoSaldoCuentasBolsa = new EntradaGenerarReporteAntiguedadDeudaBolsaCTA();
	    
	    EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_IdEmpresa = idEmpresa;
	    EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_IdUsuario = idUsuario;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_IdModulo = idModulo;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_FechaSolicitud = fechaSolicitud;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_IdMoneda = idMoneda; 
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_SeparaIntermediacion = separaIntermediacion;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_AgrupaPortafolios = agrupaPortafolios;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_Consolidar= consolidar;
    	EntradaservicioGenerarArchivoSaldoCuentasBolsa.p_NombreInternoArchivo = nombreInternoArchivo;
    	
    	try{
	    	SalidaGenerarReporteAntiguedadDeudaBolsaCTA SalidaservicioGenerarArchivoSaldoCuentasBolsa = servicioGenerarArchivoSaldoCuentasBolsa.logica(con, EntradaservicioGenerarArchivoSaldoCuentasBolsa);
	    	SQL sql = new SQL();
			List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
			if(SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_RUTAARCHIVO.trim().length()!=0 && SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_CODIGOCORREDOR.trim().length()!= 0){
					programaBatch.add(new ProgramaBatch(this.getJobId(), SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_RUTAARCHIVO, SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_CODIGOCORREDOR));
					sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
				}
				else {
					MensajeSalida Mensaje = SalidaservicioGenerarArchivoSaldoCuentasBolsa.P_OUT_MENSAJES.get(0);
					programaBatch.add(new ProgramaBatch(this.getJobId(), "", Mensaje.descripcion));
					sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
				}
				resultado = true;
    	}catch(Exception e){
    		logger.error(e);
			con.rollback();
			e.printStackTrace();
			throw e;
		}
		finally {
			running.set(false);
		}
    	logger.info("FIN::GenerarReporteAntiguedadDeudaBolsaCTAJob");
		return resultado;
		}
	}
