package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.normativa.circulares.ReportesBCC.GenerarRepoSicamMasivo;
import cl.bcs.normativa.circulares.ReportesBCC.GenerarRepoSicamMasivo.EntradaGenerarRepoSicamMasivo;
import cl.bcs.normativa.circulares.ReportesBCC.GenerarRepoSicamMasivo.SalidaGenerarRepoSicamMasivo;
import cl.bcs.normativa.jobs.BcsJobGenerarCertificadosTributarios.ResResumen;
import cl.bcs.normativa.jobs.BcsJobGenerarCertificadosTributarios.ResumenDJ;
import cl.bcs.normativa.jobs.BcsJobGenerarCertificadosTributarios.ResumenGeneracion;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarRepoSicam extends BcsJob  {
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarRepoSicamBCCH";
	private final Log log = new Log("BcsJobGenerarRepoSicam");
	
	private static FileLoader loader = new FileLoader();
	private static String SQL_SELECT_RESUMEN_TRIBUTARIO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectResumenTributario.sql");
	private static String SQL_INSERT_RESUMEN_TRIBUTARIO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarRepoSicam_InsertResumenTributario.sql");
	
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	
	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*BcsJobGenerarCertificadosTributarios* Inicio de Job");
		running.set(true);
		String Estado="Procesando";
		try {
			SQL sql = new SQL();
			Long idEmpresa 				= Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario 				= Long.parseLong(this.getParam("IdUsuario"));
			String idModulo 			= this.getParam("IdModulo"); 
			String IdTipoDocumento		= this.getParam("IdTipoDocumento");
			String VersionArchivo		= this.getParam("VersionArchivo");
			String FechaInicio			= this.getParam("FechaIncio");
			String FechaFin				= this.getParam("FechaFin");
			String Esquema				= this.getParam("Esquema");
			String Version				= this.getParam("Version");
			long contador = 0;
			List<ResumenGeneracion> prog = new ArrayList<ResumenGeneracion>();
			prog.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
			List<ResResumen> resResumen =new ArrayList<ResResumen>();
			HashMap<String, Object> par = new HashMap<>(2);
			par.put("IdPrograma", this.getJobId());
			resResumen = new SQL().query(con, SQL_SELECT_RESUMEN_TRIBUTARIO, par, ResResumen.class);
			
			for (ResResumen r: resResumen) {
				
				if (r.count == 0L){
					
					log.write( "*BcsJobGenerarGenerarCertificadosTributarios* iNSERT  RESUMEN tributario");
			        List<ResumenDJ> prog2 = new ArrayList<ResumenDJ>();
					prog2.add(new ResumenDJ(this.getJobId(), "","ASIGNADA",0L,Esquema,Version,idEmpresa,idUsuario));
		        	sql.batch(con, SQL_INSERT_RESUMEN_TRIBUTARIO, prog2,ResumenDJ.class);
		        	con.commit();
				};;
							
			}
			
	       	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog,ResumenGeneracion.class);
        	con.commit();
        	
        	
        	GenerarRepoSicamMasivo generarRepoSicam = new GenerarRepoSicamMasivo();
			EntradaGenerarRepoSicamMasivo entradaRepoSicam = new EntradaGenerarRepoSicamMasivo();
			SalidaGenerarRepoSicamMasivo salidaRepoSicam = new SalidaGenerarRepoSicamMasivo();
			entradaRepoSicam.p_IdEmpresa 	= 	idEmpresa;
			entradaRepoSicam.p_IdUsuario 	= 	idUsuario;
			entradaRepoSicam.p_IdModulo  	= 	idModulo;
			entradaRepoSicam.p_Version		=	Version;
			entradaRepoSicam.p_IdEsquema	= 	Esquema;
			entradaRepoSicam.p_FechaInicio 	= 	FechaInicio;
			entradaRepoSicam.p_FechaFin 	=	FechaFin;
			entradaRepoSicam.p_IdProceso	=	this.getJobId();
			log.write( "*GenerarRepoSicamMasivo* Se ejecuta llamada");
			salidaRepoSicam = generarRepoSicam.logica(con, entradaRepoSicam);
			List<GenerarRepoSicamMasivo.ProgramaBatch> programaBatch = new ArrayList<GenerarRepoSicamMasivo.ProgramaBatch>();
	        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaRepoSicam.P_OUT_ERRORESCONTADOR);
	        if(salidaRepoSicam.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
	        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
	        	programaBatch.add(new GenerarRepoSicamMasivo.ProgramaBatch(this.getJobId(), salidaRepoSicam.p_out_RutaArchivo));
	        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,GenerarRepoSicamMasivo.ProgramaBatch.class);
	        }
	        List<ResumenGeneracion> programaREpoSicam = new ArrayList<ResumenGeneracion>();
	        log.write( "*BcsJobGenerarGenerarRepoSicamMasivo* salida.P_OUT_ERRORESCONTADOR",salidaRepoSicam.P_OUT_ERRORESCONTADOR);
	        if(salidaRepoSicam.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
	        	log.write( "*BcsJobGenerarGenerarRepoSicamMasivo* UPdate  salida tributario");
	        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
	        	programaREpoSicam.add(new ResumenGeneracion(this.getJobId(), salidaRepoSicam.p_out_NombreArchivo,Estado,salidaRepoSicam.p_out_contadorArchivos));
	        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaREpoSicam,ResumenGeneracion.class);
	        	con.commit();
	        }else{
    			for (MensajeSalida s : salidaRepoSicam.P_OUT_MENSAJES) {
    				Estado=s.descripcion;
    			}
    			programaREpoSicam.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
	        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaREpoSicam,ResumenGeneracion.class);
	        	con.commit();
	        };
        	
		}catch(Exception e) {
			log.write("Error", e);
			throw e;
		}
		
		finally {
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
		//Connection con = PlataformaUtils.getDESAConnection();
    			Connection con = null;
    			BcsJobGenerarRepoSicam p = new BcsJobGenerarRepoSicam();
        try {
        	con = PlataformaUtils.getPropertiesConnection("desa01", "C:\\GIT\\OPTIMUS\\optimus-corredores\\connections.properties");
            p.transaction(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
	
	}
	
}
	