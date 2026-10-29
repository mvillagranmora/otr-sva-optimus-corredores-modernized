package cl.bcs.cuentaInversion.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas;
import cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas.GenerarArchivoSaldoCuentas_Entrada;
import cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas.GenerarArchivoSaldoCuentas_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoSaldoCuentasJob extends BcsJob implements JavaCall{

	private final Log log = new Log("CTA.GenerarArchivoSaldoCuentasJob");

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
    	
    	GenerarArchivoSaldoCuentas servicioGenerarArchivoSaldoCuentas = new GenerarArchivoSaldoCuentas();
    	GenerarArchivoSaldoCuentas_Entrada entradaGenerarArchivoSaldoCuentas = new GenerarArchivoSaldoCuentas_Entrada();
    	GenerarArchivoSaldoCuentas_Salida salidaGenerarArchivoSaldoCuentas = new GenerarArchivoSaldoCuentas_Salida();

        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
    	Long 	idEjecutivo;
	    String 	idMoneda;
	    String 	soloClientesSobregirados;
	    String 	soloClientesConSaldoDisponible;
	    String 	soloClientesConSaldoDisponibleClientesSobregirados;
	    String 	incluyeProductoApv;
	    String 	incluyeOtrosSaldos;
	    String 	fechaDesde;
	    String  fechaHasta;
	    String  encabezados = null;
	    
        if (debug) 
        {
        	idEmpresa 						= 1L;       
        	idUsuario						= 5L;
        	idModulo						= "CTA";               	
		    idEjecutivo 					= null;   // puede venir en null para no considerar ejecutivo
		    idMoneda 						= "CLP";  // puede venir en null para no considerar moneda
		    soloClientesSobregirados 		= "N";  // S:N
		    soloClientesConSaldoDisponible 	= "N";  // S:N
		    soloClientesConSaldoDisponibleClientesSobregirados 	= "N";  // S:N
		    incluyeProductoApv 				= "S";  // S:N
		    incluyeOtrosSaldos 				= "S";  // S:N		    
		    fechaDesde 						= "2020-03-19";
		    fechaHasta 						= "2020-03-19";	    
        }
        else
        {
         	idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo						= this.getParam("IdModulo"); 
        	idEjecutivo 					= (this.getParam("IdEjecutivo") != "0") ? Long.parseLong(this.getParam("IdEjecutivo")) : null;
		    idMoneda 						= this.getParam("IdMoneda") != "" ? this.getParam("IdMoneda") : null;		    
		    soloClientesSobregirados 		= this.getParam("SoloClientesSobregirados");
		    soloClientesConSaldoDisponible 	= this.getParam("SoloClientesConSaldoDisponible");
		    soloClientesConSaldoDisponibleClientesSobregirados 	= this.getParam("SoloClientesConSaldoDisponibleClientesSobregirados");
		    incluyeProductoApv 				= this.getParam("IncluyeProductoApv");	
		    incluyeOtrosSaldos 				= this.getParam("IncluyeOtrosSaldos");	
		    fechaDesde 						= this.getParam("FechaDesde");
		    fechaHasta 						= this.getParam("FechaHasta");
            encabezados                     =  this.getParam("Encabezados");
		    
        }
        

	
    	entradaGenerarArchivoSaldoCuentas.p_IdEmpresa	= idEmpresa;
    	entradaGenerarArchivoSaldoCuentas.p_IdUsuario	= idUsuario;
    	entradaGenerarArchivoSaldoCuentas.p_IdModulo	= idModulo; 				
    	entradaGenerarArchivoSaldoCuentas.p_FechaDesde	= fechaDesde;
    	entradaGenerarArchivoSaldoCuentas.p_FechaHasta	= fechaHasta;    	
    	entradaGenerarArchivoSaldoCuentas.p_SoloClientesSobregirados = soloClientesSobregirados; 
    	entradaGenerarArchivoSaldoCuentas.p_SoloClientesConSaldoDisponible = soloClientesConSaldoDisponible; 
    	entradaGenerarArchivoSaldoCuentas.p_SoloClientesConSaldoDisponibleClientesSobregirados = soloClientesConSaldoDisponibleClientesSobregirados; 
    	entradaGenerarArchivoSaldoCuentas.p_IncluyeProductoApv = incluyeProductoApv;  
    	entradaGenerarArchivoSaldoCuentas.p_IncluyeOtrosSaldos = incluyeOtrosSaldos;    	
    	entradaGenerarArchivoSaldoCuentas.p_IdEjecutivo	= idEjecutivo;
    	entradaGenerarArchivoSaldoCuentas.p_IdMoneda	= idMoneda;

    	Gson gson = Utils.getGsonBuilder();			
		String[] EncabezadosLista = gson.fromJson(encabezados, String[].class);
		if ( EncabezadosLista != null   ) {
		   List<String> EncabezadosArray = Arrays.asList(EncabezadosLista);
		   entradaGenerarArchivoSaldoCuentas.p_Encabezados	= EncabezadosArray;
		}
		try{
			salidaGenerarArchivoSaldoCuentas = servicioGenerarArchivoSaldoCuentas.logica(con,entradaGenerarArchivoSaldoCuentas);
			SQL sql = new SQL();
			salidaGenerarArchivoSaldoCuentas.P_OUT_ERRORESCONTADOR=0l;
			if(salidaGenerarArchivoSaldoCuentas.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
				programaBatch.add(new ProgramaBatch( Long.valueOf(this.getJobId()), salidaGenerarArchivoSaldoCuentas.P_OUT_RUTAARCHIVO.replace(".csv", ".zip")));
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
        	GenerarArchivoSaldoCuentasJob demo = new GenerarArchivoSaldoCuentasJob();
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
