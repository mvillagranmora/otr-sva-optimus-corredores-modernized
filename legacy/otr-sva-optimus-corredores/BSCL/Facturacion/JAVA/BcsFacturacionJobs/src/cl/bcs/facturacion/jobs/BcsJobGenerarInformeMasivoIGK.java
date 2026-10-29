package cl.bcs.facturacion.jobs;

import java.sql.Connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import cl.bcs.facturacion.GenerarInformeIGK.Datos;

import cl.bcs.facturacion.GenerarInformeIGK;
import cl.bcs.facturacion.GenerarInformeIGK.EntradaGenerarInformeIGK;
import cl.bcs.facturacion.GenerarInformeIGK.SalidaGenerarInformeIGK;



import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;


public class BcsJobGenerarInformeMasivoIGK extends BcsJob  {
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	
	
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarCertificadosIGK";
	private final Log log = new Log("FM.BcsJobGenerarInformeMasivoIGK");
	
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
		log.write("*BcsJobGenerarInformeMasivoIGK* Inicio de Job");
		running.set(true);
		
		
		try {
			List<Datos> p_listaRut = new ArrayList<Datos>();
			
			
			
			
			/*
			Long idEmpresa = 1L;
			Long idUsuario = 6L;
			String idModulo = "NOR"; 
			String listaRutStr ="16895045-4";//389,387
			String IdTipoDocumento="CERT21";
			String VersionArchivo="2020";
			Long IdSolicitudCircular =1373555L;
			*/
			
			Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			String idModulo = this.getParam("IdModulo"); 
			String FechaConsulta=this.getParam("FechaConsulta");
			
			Datos Datos = new Datos();
			p_listaRut.add(Datos);		
			
			
			SQL sql = new SQL();
			log.write( "*BcsJobGenerarInformeMasivoIGK* Parametros de entrada");
			log.write( "*BcsJobGenerarInformeMasivoIGK* p_IdEmpresa: " + idEmpresa);
	        log.write( "*BcsJobGenerarInformeMasivoIGK* p_IdUsuario: " + idUsuario);
	        log.write( "*BcsJobGenerarInformeMasivoIGK* p_IdModulo: " + idModulo);
	        log.write( "*BcsJobGenerarInformeMasivoIGK* p_FechaConsulta: " + FechaConsulta);
	        
	 				
					GenerarInformeIGK certificadosMasivo = new GenerarInformeIGK();
				    EntradaGenerarInformeIGK entradaCert = new EntradaGenerarInformeIGK();
				    SalidaGenerarInformeIGK salidaCert = new SalidaGenerarInformeIGK();
					
					entradaCert.p_IdEmpresa= idEmpresa;
					entradaCert.p_IdUsuario =idUsuario;
					entradaCert.p_IdModulo= idModulo;
					
					entradaCert.p_FechaConsulta= FechaConsulta;
					entradaCert.p_IdProceso=this.getJobId();
					entradaCert.p_Datos=p_listaRut;
					entradaCert.p_DataSource = super.getDataSource();
					
					log.write( "*GenerarCertificadosIGK* Se ejecuta llamada");
					salidaCert = certificadosMasivo.logica(con, entradaCert);
					
					log.write( "*BcsJobGenerarInformeMasivoIGK* entrada p_FechaConsulta: ",FechaConsulta);
			        List<GenerarInformeIGK.ProgramaBatch> programaBatchCert21 = new ArrayList<GenerarInformeIGK.ProgramaBatch>();
			        log.write( "*BcsJobGenerarGenerarCertificadosIGK* salida.P_OUT_ERRORESCONTADOR",salidaCert.P_OUT_ERRORESCONTADOR);
			        if(salidaCert.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			        	log.write( "*BcsJobGenerarGenerarCertificadosIGK* UPdate  salida batch");
			        	programaBatchCert21.add(new GenerarInformeIGK.ProgramaBatch(this.getJobId(), salidaCert.p_out_RutaZip));
			        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert21,GenerarInformeIGK.ProgramaBatch.class);
			        }
		

	        
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
    	BcsJobGenerarInformeMasivoIGK p = new BcsJobGenerarInformeMasivoIGK();
        try {
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
	
	}
	
	

}
