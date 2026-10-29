package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16;
import cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16.GenerarArchivoCuentasPorCobrarAnexo16_Entrada;
import cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16.GenerarArchivoCuentasPorCobrarAnexo16_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class GenerarArchivoCuentasPorCobrarAnexo16Job extends BcsJob implements JavaCall {

    private static final Logger logger = LogManager.getLogger(GenerarArchivoCuentasPorCobrarAnexo16Job.class);
	private final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH = loadResourceToString("cl/bcs/cuentaInversion/jobs/sql/GenerarArchivoCuentasPorCobrarAnexo16Job_UpdateRutaSalidaProgramaBatch.sql");
	
	public static class ProgramaBatch{
		public Long 	IdPrograma;
		public String 	RutaArchivo;
				
		public ProgramaBatch(Long IdPrograma, String RutaArchivo)
		{
			this.IdPrograma = IdPrograma;
			this.RutaArchivo = RutaArchivo;		
		}
	}
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	 // @Override
    public void onError(Throwable t) {
    	logger.debug("Error en el job GenerarArchivoCuentasPorCobrarAnexo16Job", t);
        t.printStackTrace();
    }

    // @Override
    public void pause() {

    }
    
    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
    
    
    public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	logger.debug("GenerarArchivoCuentasPorCobrarAnexo16Job.logica");
        Long 	idEmpresa;
        Long 	idUsuario;
	    String  fechaHasta;

        if (debug) 
        {
        	idEmpresa   = 1L;       
        	idUsuario	= 97L;     	
		    fechaHasta	= "2025-08-14";	    
        }
        else
        {
         	idEmpresa	= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario	= Long.parseLong(this.getParam("IdUsuario"));
        	fechaHasta 	= this.getParam("FechaHasta");
        }
        
    	GenerarArchivoCuentasPorCobrarAnexo16_Entrada entrada = new GenerarArchivoCuentasPorCobrarAnexo16_Entrada();
        entrada.p_IdEmpresa	  = idEmpresa;
        entrada.p_IdUsuario	  = idUsuario;
    	entrada.p_FechaHasta  = fechaHasta;
    	
		try{
	    	GenerarArchivoCuentasPorCobrarAnexo16 servicio	    = new GenerarArchivoCuentasPorCobrarAnexo16();
			GenerarArchivoCuentasPorCobrarAnexo16_Salida salida = servicio.logica(con ,entrada);
			SQL sql = new SQL();
			
			if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
				programaBatch.add(new ProgramaBatch(this.getJobId(), salida.P_OUT_RUTAARCHIVO));
				sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch, ProgramaBatch.class);
				con.commit();
				return true;
			}
			else
			{
				return false;
			}
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
		
    }

	public static void main(String[] args) throws Throwable {
		logger.info("INICIO::GenerarArchivoCuentasPorCobrarAnexo16Job.main");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02", "connections.properties")) {
			GenerarArchivoCuentasPorCobrarAnexo16Job demo = new GenerarArchivoCuentasPorCobrarAnexo16Job();
			demo.logica(con,true);
		} catch (Exception er) {
		    er.printStackTrace();
		}
		logger.info("FIN::GenerarArchivoCuentasPorCobrarAnexo16Job.main");
	}

}
