package cl.bcs.cuentaInversion.jobs;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar;
import cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar.EntradaGenerarArchivoClientesPorPagarYCobrar;
import cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar.SalidaGenerarArchivoClientePorPagarYCobrar;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarArchivoClientesPorPagarYCobrarJob extends BcsJob implements JavaCall{

	private final Log log = new Log("CTA.GenerarArchivoClientesPorPagarYCobrarJob");

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
    	GenerarArchivoClientesPorPagarYCobrar servicioClientesPorPagarCobrar = new GenerarArchivoClientesPorPagarYCobrar();
    	EntradaGenerarArchivoClientesPorPagarYCobrar entradaClientesPorPagarCobrar =  new EntradaGenerarArchivoClientesPorPagarYCobrar(); 
    	SalidaGenerarArchivoClientePorPagarYCobrar salidaClientesPorPagarCobrar = new SalidaGenerarArchivoClientePorPagarYCobrar();
    	
    	
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;
    	String FechaSistema;
    	String EsBusqueda;
    	String IdMoneda;
    	String TipoSaldo;//TOD;POS;NEG
		


        if (debug) 
        {
        	idEmpresa 					= 1L;       
        	idUsuario					= 4L;
        	idModulo					= "CTA"; 
        	FechaSistema				= "2021-11-16";
        	IdMoneda 					= "CLP";
        	EsBusqueda 					= "N";
        	//EsBusqueda = "S";			/*Si quieres probar busqueda descomenta esta linea y comenta la anterior*/
        	TipoSaldo 					= "NEG";
        }
        else
        {
         	idEmpresa						= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario						= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo						= this.getParam("IdModulo"); 
        	FechaSistema					= this.getParam("FechaSistema");
        	EsBusqueda						= this.getParam("EsBusqueda");
        	IdMoneda						= this.getParam("IdMoneda");  
        	TipoSaldo						= this.getParam("TipoSaldo");  
        }

	
        entradaClientesPorPagarCobrar.p_IdEmpresa	 = idEmpresa;
        entradaClientesPorPagarCobrar.p_IdUsuario	 = idUsuario;
        entradaClientesPorPagarCobrar.p_IdModulo	 = idModulo;	
        entradaClientesPorPagarCobrar.FechaSistema = FechaSistema;
        entradaClientesPorPagarCobrar.EsBusqueda = EsBusqueda;
        entradaClientesPorPagarCobrar.IdMoneda = IdMoneda;
        entradaClientesPorPagarCobrar.TipoSaldo = TipoSaldo;
        
		

		try{
			salidaClientesPorPagarCobrar = servicioClientesPorPagarCobrar.logica(con, entradaClientesPorPagarCobrar);
			SQL sql = new SQL();
			if(salidaClientesPorPagarCobrar.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0){
				List<ProgramaBatch> programaBatch = new ArrayList<ProgramaBatch>();
				
				if (!debug) {
					programaBatch.add(new ProgramaBatch(this.getJobId(), salidaClientesPorPagarCobrar.P_OUT_RUTAARCHIVO));
					sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch,ProgramaBatch.class);
			    }	  
				con.commit();
				return true;
			}
			else {
				String mensajeError = salidaClientesPorPagarCobrar.P_OUT_MENSAJES.stream()
						.reduce("", (stringParcial,elem)->stringParcial+elem.tipo+": "+elem.descripcion+"  "
						,String::concat);
				throw new Error(mensajeError);
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

    
    public static void main(String[] args) throws Throwable { 
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
        	GenerarArchivoClientesPorPagarYCobrarJob demo = new GenerarArchivoClientesPorPagarYCobrarJob();
        	demo.logica(con,true);
        	
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }

    
}
