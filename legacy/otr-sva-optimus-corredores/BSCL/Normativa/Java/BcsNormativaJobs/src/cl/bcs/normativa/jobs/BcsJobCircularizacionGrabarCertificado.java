package cl.bcs.normativa.jobs;

import java.sql.Connection;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;



import cl.bcs.normativa.CircularizacionGrabarCertificado;
import cl.bcs.normativa.CircularizacionGrabarCertificado.EntradaCircularizacionGrabarCertificado;
import cl.bcs.normativa.CircularizacionGrabarCertificado.SalidaCircularizacionGrabarCertificado;



import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.normativa.circulares.NotificarCircularizacion;


public class BcsJobCircularizacionGrabarCertificado extends BcsJob  {
	//private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	
	
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarCertificadosIGK";
	private final Log log = new Log("FM.BcsJobCircularizacionGrabarCertificado");
	
	public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdProceso;
		public String 	RutaArchivo;
		public String 	Glosa;
		public Long TotalRegistrosGenerados;
		
		public ResumenGeneracion(Long IdProceso, String RutaArchivo, String Glosa,Long TotalRegistrosGenerados)
		{
			this.IdProceso = IdProceso;
			this.RutaArchivo = RutaArchivo;		
			this.Glosa = Glosa;
			this.TotalRegistrosGenerados = TotalRegistrosGenerados;
		}
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*BcsJobCircularizacionGrabarCertificado* Inicio de Job");
		running.set(true);
		
		
		try {
			
			/*
			Long idEmpresa = 1L;
			Long idUsuario = 70818L;
			String 	UltimoDiaMes= "2022-08-31"; 
			String  periodo ="202208";//389,387
			Long IdSolicitudCircular =0L;
			*/
			
			Long 	idEmpresa				= Long.parseLong(this.getParam("IdEmpresa"));
			Long 	idUsuario 				= Long.parseLong(this.getParam("idUsuario"));
			String 	UltimoDiaMes			= this.getParam("UltimoDiaMes");
			String  periodo 				= this.getParam("Periodo");
			Long 	IdSolicitudCircular 	= Long.parseLong(this.getParam("IdSolicitudCircular"));
		
			SQL sql = new SQL();
			log.write( "*BcsJobCircularizacionGrabarCertificado* Parametros de entrada");
			log.write( "*BcsJobCircularizacionGrabarCertificado* p_IdEmpresa: " + idEmpresa);
	        log.write( "*BcsJobCircularizacionGrabarCertificado* p_IdUsuario: " + idUsuario);
	        log.write( "*BcsJobCircularizacionGrabarCertificado* p_UltimoDiaMes: " + UltimoDiaMes);
	        
	 				
	        CircularizacionGrabarCertificado certificadosMasivo = new CircularizacionGrabarCertificado();
				    EntradaCircularizacionGrabarCertificado entradaCert = new EntradaCircularizacionGrabarCertificado();
				    SalidaCircularizacionGrabarCertificado salidaCert = new SalidaCircularizacionGrabarCertificado();
					
					entradaCert.p_IdEmpresa				= idEmpresa;
					entradaCert.p_IdUsuario 			= idUsuario;
					entradaCert.p_Periodo				= periodo;
					entradaCert.p_IdTipoDocumento		= "CIRCCarta";
					entradaCert.p_UltimoDiaMes			= UltimoDiaMes;
					entradaCert.p_IdSolicitudCircular	= IdSolicitudCircular;
					
					log.write( "*CircularizacionGrabarCertificado* Se ejecuta llamada");
					salidaCert = certificadosMasivo.logica(con, entradaCert);
					NotificarCircularizacion.NotificarCircularizacionEntrada in = new NotificarCircularizacion.NotificarCircularizacionEntrada();
					in.p_IdEmpresa = idEmpresa;
					in.p_IdSolicitudCircularizacion = IdSolicitudCircular;
					in.p_IdUsuario = idUsuario;
					in.p_IdModulo = "NOR";
					
					NotificarCircularizacion notificar = new NotificarCircularizacion();
					notificar.logica(con, in);
   
	    	log.write( "*BcsJobGenerarGenerarCertificadosIGK* Fin ejecucion de servicio");
			
			
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
    	Connection con = PlataformaUtils.getDESAConnection();
		//Connection con = PlataformaUtils.getMASTER1Connection();
    	BcsJobCircularizacionGrabarCertificado p = new BcsJobCircularizacionGrabarCertificado();
        try {
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
	
	}
	
	

}
