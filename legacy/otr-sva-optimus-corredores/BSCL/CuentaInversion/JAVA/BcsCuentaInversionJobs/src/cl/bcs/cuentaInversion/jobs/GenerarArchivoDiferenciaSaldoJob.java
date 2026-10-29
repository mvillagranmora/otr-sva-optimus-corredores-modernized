package cl.bcs.cuentaInversion.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.ConsultaComparacionSaldo;
import cl.bcs.cuentaInversion.ConsultaComparacionSaldo.ConsultaComparacionSaldoEntrada;
import cl.bcs.cuentaInversion.ConsultaComparacionSaldo.ConsultaComparacionSaldoSalida;
import cl.bcs.cuentaInversion.jobs.GenerarArchivoSaldoCuentasJob.ProgramaBatch;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarArchivoDiferenciaSaldoJob extends BcsJob implements JavaCall{

	private final Log log = new Log("FMM.GenerarArchivoDiferenciaSaldoJob");

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
    	
    	ConsultaComparacionSaldo servicioConsultaComparacionSaldo = new ConsultaComparacionSaldo();
    	ConsultaComparacionSaldoEntrada entradaConsultaComparacionSaldo = new ConsultaComparacionSaldoEntrada();
    	ConsultaComparacionSaldoSalida salidaConsultaComparacionSaldo = new ConsultaComparacionSaldoSalida();
    	    	
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
	    String 	fechaDesde;
	    String  fechaHasta;	    	    

        if (debug) 
        {
        	idEmpresa 					= 1L;       
        	idUsuario					= 5L;
        	idModulo					= "CTA";  
        	fechaDesde 					= "2020-08-26";
 		    fechaHasta 					= "0001-01-01"; 		    
        }
        else
        {
         	idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo						= this.getParam("IdModulo"); 
		    fechaDesde 						= this.getParam("FechaDesde");
		    fechaHasta 						= this.getParam("FechaHasta");
        }

	
        entradaConsultaComparacionSaldo.p_IdEmpresa	 = idEmpresa;
        entradaConsultaComparacionSaldo.p_IdUsuario	 = idUsuario;
        entradaConsultaComparacionSaldo.p_IdModulo	 = idModulo;			
        entradaConsultaComparacionSaldo.p_FechaDesde = fechaDesde;
        entradaConsultaComparacionSaldo.p_FechaHasta = fechaHasta;	

		try{
			salidaConsultaComparacionSaldo = servicioConsultaComparacionSaldo.logica(con, entradaConsultaComparacionSaldo);
			
			
			SQL sql = new SQL();
			if(salidaConsultaComparacionSaldo.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
				
				programaBatch.add(new ProgramaBatch(this.getJobId(), salidaConsultaComparacionSaldo.P_OUT_RUTAARCHIVO));
				
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
        	GenerarArchivoDiferenciaSaldoJob demo = new GenerarArchivoDiferenciaSaldoJob();
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
