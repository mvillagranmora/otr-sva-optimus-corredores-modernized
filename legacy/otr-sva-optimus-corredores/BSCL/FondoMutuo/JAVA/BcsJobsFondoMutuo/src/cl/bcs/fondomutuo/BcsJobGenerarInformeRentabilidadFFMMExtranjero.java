package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.fondomutuo.GenerarInformeRentabilidadFFMMExtranjeroMasivo.Cliente;
import cl.bcs.fondomutuo.GenerarInformeRentabilidadFFMMExtranjeroMasivo.EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo;
import cl.bcs.fondomutuo.GenerarInformeRentabilidadFFMMExtranjeroMasivo.SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo;
import cl.bcs.fondomutuo.jobs.GenerarArchivoSolicitudAporteRescateJob.ProgramaBatch;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


public class BcsJobGenerarInformeRentabilidadFFMMExtranjero extends BcsJob {
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	private final String SQL_UPDATE_MENSAJE_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"Resultado\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarInformeRentabilidadFFMMExtranjero";
	private final Log log = new Log("FM.BcsJobGenerarInformeRentabilidadFFMMExtranjero");

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*GenerarInformeRentabilidadFFMMExtranjero* Inicio de Job");
		running.set(true);

		try {	
			Long idEmpresa = 1L;
			Long idUsuario = 3L;
			String idModulo = "FMM"; 
			String agnoComercial = "2020";
			String esMasivo = "S";
			List<Cliente> idCliente = new ArrayList<Cliente>();
			Cliente cliente = new Cliente();
			
			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			idModulo = this.getParam("IdModulo"); 
			agnoComercial = this.getParam("AgnoComercial");
			esMasivo = this.getParam("EsMasivo");
			Long id = Long.parseLong(this.getParam("IdCliente"));
			
			if(!id.equals(0)) {
				cliente.IdCliente = id;
				idCliente.add(cliente);
			}
			
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* idEmpresa: " + idEmpresa);
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* idUsuario: " + idUsuario);
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* AgnoComercial: " + agnoComercial);
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* EsMasivo: " + esMasivo);
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* IdClientes: " + idCliente);
            
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* Preparando llamada");
            
            GenerarInformeRentabilidadFFMMExtranjeroMasivo generarInformeRentabilidadFFMMExtranjeroMasivo = new GenerarInformeRentabilidadFFMMExtranjeroMasivo();
            EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo = new EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo();
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_IdEmpresa = idEmpresa;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_IdUsuario = idUsuario;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_IdModulo = idModulo;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_AgnoComercial = agnoComercial;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_EsMasivo = esMasivo;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_IdClientes = idCliente;
            entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo.p_DataSource = this.getDataSource();
            
            log.write( "*GenerarInformeRentabilidadFFMMExtranjero* Se ejecuta llamada");
            
            SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo salida = new SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo();
            
            salida = generarInformeRentabilidadFFMMExtranjeroMasivo.logica(con, entradaGenerarInformeRentabilidadFFMMExtranjeroMasivo);
            
            SQL sql = new SQL();
            List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
            if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
            	programaBatch.add(new ProgramaBatch(this.getJobId(), salida.p_out_RutaZip));
            	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
            }
            
        	log.write( "*GenerarInformeRentabilidadFFMMExtranjero* Fin ejecucion de servicio");
            

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
    	log.write(":ProcesadorTransacciones:  ****** ERROR  *******", t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getBOLSAConnection();
    	BcsJobGenerarInformeRentabilidadFFMMExtranjero p = new BcsJobGenerarInformeRentabilidadFFMMExtranjero();
        try {
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
