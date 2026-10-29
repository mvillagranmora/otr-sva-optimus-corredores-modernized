package cl.bcs.cuentaInversion.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA;
import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA.GenerarReporteAntiguedadDeudaCTA_Entrada;
import cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA.GenerarReporteAntiguedadDeudaCTA_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarReporteAntiguedadDeudaCTAJob extends BcsJob implements JavaCall{

	private AtomicBoolean running = new AtomicBoolean(false);
	private final Log log = new Log("CTA.GenerarReporteAntiguedadDeudaCTAJob");

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
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
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
		    fechaSolicitud 					= "2022-10-19";
		    idMoneda 						= "CLP"; 
			separaIntermediacion			= "S"; 	// S/N
			agrupaPortafolios				= "S";	// S/N 
			consolidar						= "N";	// S/N 
			nombreInternoArchivo			= "gdg353ydgdsgs5423";
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
        
    	GenerarReporteAntiguedadDeudaCTA servicioGenerarArchivoSaldoCuentas = new GenerarReporteAntiguedadDeudaCTA();
    	GenerarReporteAntiguedadDeudaCTA_Entrada EntradaservicioGenerarArchivoSaldoCuentas= new GenerarReporteAntiguedadDeudaCTA_Entrada();
    	
    	EntradaservicioGenerarArchivoSaldoCuentas.p_IdEmpresa = idEmpresa;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_IdUsuario = idUsuario;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_IdModulo = idModulo;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_FechaSolicitud = fechaSolicitud;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_IdMoneda = idMoneda; 
    	EntradaservicioGenerarArchivoSaldoCuentas.p_SeparaIntermediacion = separaIntermediacion;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_AgrupaPortafolios = agrupaPortafolios;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_Consolidar= consolidar;
    	EntradaservicioGenerarArchivoSaldoCuentas.p_NombreInternoArchivo = nombreInternoArchivo;

		try{
			GenerarReporteAntiguedadDeudaCTA_Salida SalidaservicioGenerarArchivoSaldoCuentas = servicioGenerarArchivoSaldoCuentas.logica(con,EntradaservicioGenerarArchivoSaldoCuentas);
			SQL sql = new SQL();
			List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
			if(SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 
				&& SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_RUTAARCHIVO.trim().length()!=0 && SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_CODIGOCORREDOR.trim().length()!= 0){
				programaBatch.add(new ProgramaBatch(this.getJobId(), SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_RUTAARCHIVO, SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_CODIGOCORREDOR));
				sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
			}
			else {
				MensajeSalida Mensaje = SalidaservicioGenerarArchivoSaldoCuentas.P_OUT_MENSAJES.get(0);
				programaBatch.add(new ProgramaBatch(this.getJobId(), "", Mensaje.descripcion));
				sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
			}
			resultado = true;
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
		finally {
			running.set(false);
		}
		
		log.write("FIN JOB");
		return resultado;
    }

    // @Override
    public void onError(Throwable t) {
    	log.write("****** ERROR  *******");
        log.write(t);
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
   
    public static void main(String[] args) throws IOException { 
    	System.out.println("== INICIO PRUEBA ==");
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
        	GenerarReporteAntiguedadDeudaCTAJob demo = new GenerarReporteAntiguedadDeudaCTAJob();
        	demo.logica(con,true);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("== FIN PRUEBA ==");
    }

    
}
