package cl.bcs.cuentaInversion.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import cl.bcs.cuentaInversion.GenerarArchivoCargoAbono;
import cl.bcs.cuentaInversion.GenerarArchivoCargoAbono.GenerarArchivoCargoAbono_Entrada;
import cl.bcs.cuentaInversion.GenerarArchivoCargoAbono.GenerarArchivoCargoAbono_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarArchivoCargoAbonoJob extends BcsJob implements JavaCall{

	private final Log log = new Log("CTA.GenerarArchivoCargoAbonoJob");
	
	private final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo, \"Comentario\" = :Comentario  WHERE \"IdPrograma\" = :IdPrograma ";
	
	
	
	public static class ProgramaBatch{
		public Long 	IdPrograma;
		public String 	RutaArchivo;
		
		
		public String 	Comentario;
		
				
		public ProgramaBatch(Long idPrograma, String rutaArchivo, String comentario)
		{
			this.IdPrograma = idPrograma;
			this.RutaArchivo = rutaArchivo;	
			this.Comentario = comentario;
		}
	}

	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
    	
    	GenerarArchivoCargoAbono servicioGenerarArchivoCargoAbono = new GenerarArchivoCargoAbono();
    	GenerarArchivoCargoAbono_Entrada entradaGenerarArchivoCargoAbono = new GenerarArchivoCargoAbono_Entrada();
    	GenerarArchivoCargoAbono_Salida salidaGenerarArchivoCargoAbono = new GenerarArchivoCargoAbono_Salida();
    	
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
    	Long 	idEjecutivo;
	    String 	idMoneda;
	    String 	idTipoMovimiento;
	    String 	idConcepto;
	    String 	idEstado;
	    String 	fechaDesde;
	    String  fechaHasta;
	    
        if (debug) 
        {
        	idEmpresa 			= 1L;       
        	idUsuario			= 1006L;
        	idModulo			= "TES";               	
		    idEjecutivo 		= 0L;
		    idMoneda 			= "0";       // desde la UI los string llegan con cero
		    idTipoMovimiento 	= "0";  // MOVABN, MOVCAR, 0
		    idConcepto 			= "CAREAB";
		    idEstado 			= "0";
		    fechaDesde 			= "2020-09-10";
		    fechaHasta 			= "2020-09-10";	
	    
        } 
        else
        {        	
         	idEmpresa			= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario			= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo			= this.getParam("IdModulo");        	  
		    idEjecutivo 		= Long.parseLong(this.getParam("IdEjecutivo"));
		    idMoneda 			= this.getParam("IdMoneda"); // desde la UI los string llegan con cero
		    idTipoMovimiento 	= this.getParam("IdTipoMovimiento");
		    idConcepto 			= this.getParam("IdConcepto");
		    idEstado 			= this.getParam("IdEstado");
		    fechaDesde 			= this.getParam("FechaDesde");
		    fechaHasta 			= this.getParam("FechaHasta");			   
        }
        
    	entradaGenerarArchivoCargoAbono.p_IdEmpresa			= idEmpresa;
    	entradaGenerarArchivoCargoAbono.p_IdUsuario			= idUsuario;
    	entradaGenerarArchivoCargoAbono.p_IdModulo			= idModulo; 
    	entradaGenerarArchivoCargoAbono.p_FechaDesde		= fechaDesde;
    	entradaGenerarArchivoCargoAbono.p_FechaHasta		= fechaHasta;
    	entradaGenerarArchivoCargoAbono.p_IdEjecutivo		= idEjecutivo;
    	entradaGenerarArchivoCargoAbono.p_IdMoneda			= idMoneda;
    	entradaGenerarArchivoCargoAbono.p_IdTipoMovimiento	= idTipoMovimiento;
    	entradaGenerarArchivoCargoAbono.p_IdConcepto		= idConcepto;
    	entradaGenerarArchivoCargoAbono.p_IdEstado			= idEstado;
	
		

		try{
			salidaGenerarArchivoCargoAbono = servicioGenerarArchivoCargoAbono.logica(con,entradaGenerarArchivoCargoAbono);
			SQL sql = new SQL();
			if(salidaGenerarArchivoCargoAbono.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();

				

				String comentario = (salidaGenerarArchivoCargoAbono.P_OUT_CANTIDADREGISTROS.compareTo(0L) == 0 ? "No existen movimientos para generar archivo" : "");
				
				programaBatch.add(new ProgramaBatch(this.getJobId(), salidaGenerarArchivoCargoAbono.P_OUT_RUTAARCHIVO, comentario));

				
				
				sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
				
				
						
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
        	GenerarArchivoCargoAbonoJob demo = new GenerarArchivoCargoAbonoJob();
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
