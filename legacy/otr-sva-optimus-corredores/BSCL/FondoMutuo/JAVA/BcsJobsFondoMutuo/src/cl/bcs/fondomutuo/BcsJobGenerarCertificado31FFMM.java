package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.fondomutuo.GenerarCertificado31Masivo.EntradaGenerarCertificado31Masivo;
import cl.bcs.fondomutuo.GenerarCertificado31Masivo.SalidaGenerarCertificado31Masivo;
import cl.bcs.fondomutuo.GenerarCertificado31Masivo.Traspaso;
import cl.bcs.fondomutuo.jobs.GenerarArchivoSolicitudAporteRescateJob.ProgramaBatch;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class BcsJobGenerarCertificado31FFMM extends BcsJob  {
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarCertificado31FFMM";
	private final Log log = new Log("FM.BcsJobGenerarCertificado31FFMM");
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*BcsJobGenerarCertificado31FFMM* Inicio de Job");
		running.set(true);
		
		
		try {
			/*Long idEmpresa = 1L;
			Long idUsuario = 3L;
			String idModulo = "FMM"; 
			String CiudadCertificado ="";
			String FechaCertificado = "";
			List<Traspaso> p_listaTraspasos = new ArrayList<Traspaso>();
			String listaTraspasosStr ="99953855";//389,387
			*/
			List<Traspaso> p_listaTraspasos = new ArrayList<Traspaso>();
			
			Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			String idModulo = this.getParam("IdModulo"); 
			String CiudadCertificado = this.getParam("CiudadCertificado");
			String FechaCertificado = this.getParam("FechaCertificado");;
			String listaTraspasosStr = this.getParam("ListaTraspasos");
			
			
			String arrTraspaso [] = listaTraspasosStr.split(",");
			
			for (String id : arrTraspaso) {
				Traspaso traspaso = new Traspaso();
				traspaso.IdTraspaso =  Long.parseLong(id);
				p_listaTraspasos.add(traspaso);				
			}
			
			log.write( "*BcsJobGenerarCertificado31FFMM* idEmpresa: " + idEmpresa);
	        log.write( "*BcsJobGenerarCertificado31FFMM* idUsuario: " + idUsuario);
	        log.write( "*BcsJobGenerarCertificado31FFMM* CiudadCertificado: " + CiudadCertificado);
	        log.write( "*BcsJobGenerarCertificado31FFMM* FechaCertificado: " + FechaCertificado);
	       
	        
			
			GenerarCertificado31Masivo certificados31Masivo = new GenerarCertificado31Masivo();
			EntradaGenerarCertificado31Masivo entrada= new EntradaGenerarCertificado31Masivo();
			SalidaGenerarCertificado31Masivo salida = new SalidaGenerarCertificado31Masivo();
			
			entrada.p_IdEmpresa= idEmpresa;
			entrada.p_IdUsuario =idUsuario;
			entrada.p_IdModulo= idModulo;
			entrada.p_Ciudad = CiudadCertificado;
			entrada.p_Fecha = FechaCertificado;
			entrada.p_listaTraspasos = p_listaTraspasos;
			entrada.p_DataSource = this.getDataSource();
			
			log.write( "*GenerarCertificado31* Se ejecuta llamada");
			
			salida = certificados31Masivo.logica(con, entrada);
			
			SQL sql = new SQL();
	        List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
	        if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
	        	programaBatch.add(new ProgramaBatch(this.getJobId(), salida.p_out_RutaZip));
	        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
	        }
	        
	    	log.write( "*BcsJobGenerarCertificado31FFMM* Fin ejecucion de servicio");
			
			
		}catch(Exception e) {
			log.write("Error", e);
			throw e;
		}finally {
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
		
	}
	public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getBOLSAConnection();
    	BcsJobGenerarCertificado31FFMM p = new BcsJobGenerarCertificado31FFMM();
        try {
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
	
	}

}
