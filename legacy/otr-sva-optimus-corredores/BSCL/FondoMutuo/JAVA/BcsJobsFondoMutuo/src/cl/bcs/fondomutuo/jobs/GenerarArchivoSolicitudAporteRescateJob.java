package cl.bcs.fondomutuo.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import cl.bcs.fondomutuo.GenerarArchivoSolicitudAporteRescate;
import cl.bcs.fondomutuo.GenerarArchivoSolicitudAporteRescate.GenerarArchivoSolicitudAporteRescate_Entrada;
import cl.bcs.fondomutuo.GenerarArchivoSolicitudAporteRescate.GenerarArchivoSolicitudAporteRescate_Salida;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarArchivoSolicitudAporteRescateJob extends BcsJob implements JavaCall{

	private final Log log = new Log("FMM.GenerarArchivoSolicitudAporteRescateJob");

	private final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	
	
	
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
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
    	
    	GenerarArchivoSolicitudAporteRescate servicioGenerarArchivoSolicitudAporteRescate = new GenerarArchivoSolicitudAporteRescate();
    	GenerarArchivoSolicitudAporteRescate_Entrada entradaGenerarArchivoSolicitudAporteRescate = new GenerarArchivoSolicitudAporteRescate_Entrada();
    	GenerarArchivoSolicitudAporteRescate_Salida salidaGenerarArchivoSolicitudAporteRescate = new GenerarArchivoSolicitudAporteRescate_Salida();
    	
    	
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
	    String 	fechaDesde;
	    String  fechaHasta;	    

	    String 	idTipoMovimientoFondoMutuo; 
		String 	estado; 	   
		String  idEmisor; 	   
		String 	idSerieFondo; 
		Long 	idCliente; 
		Long 	idProductoAgrupador; 
		Long 	idPortafolio; 	   
		String 	idTipoSolicitud; 
		String  idMoneda; 
		


        if (debug) 
        {
        	idEmpresa 					= 1L;       
        	idUsuario					= 5L;
        	idModulo					= "FMM";  
        	fechaDesde 					= "2020-07-23";
 		    fechaHasta 					= "2020-08-21"; 		    

 		    idTipoMovimientoFondoMutuo 	= ""; 
 			estado 						= ""; 	   
 			idEmisor 					= ""; 	   
 			idSerieFondo 				= "";
 			idCliente 					= 0L; 
 			idProductoAgrupador 		= 0L; 
 			idPortafolio 				= 0L; 	   
 			idTipoSolicitud 			= ""; 
 			idMoneda 					= ""; 
 			
        }
        else
        {
         	idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo						= this.getParam("IdModulo"); 
		    fechaDesde 						= this.getParam("FechaDesde");
		    fechaHasta 						= this.getParam("FechaHasta");
		    
			idTipoMovimientoFondoMutuo		= this.getParam("IdTipoMovimientoFondoMutuo"); 
			estado							= this.getParam("Estado"); 	   
			idEmisor						= this.getParam("IdEmisor"); 	   
			idSerieFondo					= this.getParam("IdSerieFondo"); 
			idCliente						= Long.parseLong(this.getParam("IdCliente")); 
			idProductoAgrupador				= Long.parseLong(this.getParam("IdProductoAgrupador")); 
			idPortafolio					= Long.parseLong(this.getParam("IdPortafolio")); 	   
			idTipoSolicitud					= this.getParam("TipoSolicitud"); 
			idMoneda						= this.getParam("IdMoneda"); 
		   
        }

	
        entradaGenerarArchivoSolicitudAporteRescate.p_IdEmpresa	 = idEmpresa;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdUsuario	 = idUsuario;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdModulo	 = idModulo;			
        entradaGenerarArchivoSolicitudAporteRescate.p_FechaDesde = fechaDesde;
        entradaGenerarArchivoSolicitudAporteRescate.p_FechaHasta = fechaHasta;	

        entradaGenerarArchivoSolicitudAporteRescate.p_IdTipoMovimientoFondoMutuo = idTipoMovimientoFondoMutuo;
        entradaGenerarArchivoSolicitudAporteRescate.p_Estado 			= estado;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdEmisor 			= idEmisor;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdSerieFondo 		= idSerieFondo;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdCliente 		= idCliente;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdProductoAgrupador = idProductoAgrupador;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdPortafolio 		= idPortafolio;
        entradaGenerarArchivoSolicitudAporteRescate.p_TipoSolicitud 	= idTipoSolicitud;
        entradaGenerarArchivoSolicitudAporteRescate.p_IdMoneda 			= idMoneda;
        
		

		try{
			salidaGenerarArchivoSolicitudAporteRescate = servicioGenerarArchivoSolicitudAporteRescate.logica(con, entradaGenerarArchivoSolicitudAporteRescate);
			
			
			SQL sql = new SQL();
			if(salidaGenerarArchivoSolicitudAporteRescate.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
				
				if (!debug) 
			    {
					programaBatch.add(new ProgramaBatch(this.getJobId(), salidaGenerarArchivoSolicitudAporteRescate.P_OUT_RUTAARCHIVO));
					sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
			    }
				  
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
        	GenerarArchivoSolicitudAporteRescateJob demo = new GenerarArchivoSolicitudAporteRescateJob();
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
