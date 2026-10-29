package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

import cl.bcs.fondomutuo.jobs.GenerarArchivoSolicitudAporteRescateJob.ProgramaBatch;
import cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419;
import cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419.EntradaGenerarInformeMensualInversionistasRV13419;
import cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419.SalidaGenerarInformeMensualInversionistasRV13419;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


public class BcsJobGenerarInformeMensualInversionistasRV13419 extends BcsJob {
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarInformeMensualInversionistasRV";
	private final Log log = new Log("FM.BcsJobGenerarInformeMensualInversionistasRV13419");

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*GenerarInformeMensualInversionistasRV13419* Inicio de Job");
		running.set(true);

		try {	
			Long idEmpresa = 1L;
			Long idUsuario = 3L;
			String idModulo = "NOR"; 
			String fechaInicio = "2020-01-01";
			String fechaTermino= "2020-02-01";
			
			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			idModulo = this.getParam("IdModulo"); 
			fechaInicio = this.getParam("FechaInicio");
			fechaTermino = this.getParam("FechaTermino");
		
			
            log.write( "*GenerarInformeMensualInversionistasRV13419* idEmpresa: " + idEmpresa);
            log.write( "*GenerarInformeMensualInversionistasRV13419* idUsuario: " + idUsuario);
            log.write( "*GenerarInformeMensualInversionistasRV13419* fechaInicio: " + fechaInicio);
            log.write( "*GenerarInformeMensualInversionistasRV13419* fechaTermino: " + fechaTermino);
            
            
            log.write( "*GenerarInformeMensualInversionistasRV13419* Preparando llamada");
            
            GenerarInformeMensualInversionistasRV13419 generarInformeMensualInversionistasRV13419 = new GenerarInformeMensualInversionistasRV13419();
            EntradaGenerarInformeMensualInversionistasRV13419 entrada = new EntradaGenerarInformeMensualInversionistasRV13419();
            
            entrada.p_IdEmpresa = idEmpresa;
			entrada.p_IdUsuario = idUsuario;
			entrada.p_IdModulo = idModulo;
			entrada.p_FechaInicio =  fechaInicio;
			entrada.p_FechaTermino = fechaTermino;
            
            entrada.p_DataSource = this.getDataSource();
            
            log.write( "*GenerarInformeMensualInversionistasRV13419* Se ejecuta llamada");
            
            SalidaGenerarInformeMensualInversionistasRV13419 salida = new SalidaGenerarInformeMensualInversionistasRV13419();
            
            salida = generarInformeMensualInversionistasRV13419.logica(con, entrada);
            
            SQL sql = new SQL();
            List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
            if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
            	programaBatch.add(new ProgramaBatch(this.getJobId(), salida.p_out_RutaCSV));
            	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
            }
            
        	log.write( "**GenerarInformeMensualInversionistasRV13419** Fin ejecucion de servicio");
            

		} catch (Exception e) {
			log.write("Error", e);
			throw e;
		} finally {
			running.set(false);
		}

		running.set(false);
		return true;
	}

	@Override
	public void onError(Throwable t) {
    	log.write(":ProcesadorTransacciones:  ****** ERROR  ******", t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getBOLSAConnection();
    	BcsJobGenerarInformeMensualInversionistasRV13419 p = new BcsJobGenerarInformeMensualInversionistasRV13419();
        try {
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
