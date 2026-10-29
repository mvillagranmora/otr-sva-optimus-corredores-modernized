package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.google.gson.Gson;

import cl.bcs.normativa.exportacionserv.ProcesoExportacionServ;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.tesoreria.ban.ConsultarLibretaBanco;
import cl.bcs.tesoreria.ban.ConsultarLibretaBanco.ConsultarLibretaBanco_Entrada;
import cl.bcs.tesoreria.ban.ConsultarLibretaBanco.ConsultarLibretaBanco_Salida;
import cl.bcs.utils.Utils;

public final class BcsJobExportacionServ extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobExportacionServ.class.getName());

    @Override
    public void onError(final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de exportacion", t);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
        return logica(dbcon,null,false);
    }
    
    public boolean logica(Connection dbcon,DataSource ds, boolean debug ) throws InterruptedException, Throwable {
    	LOGGER.log(Level.INFO, "Proceso de exportacion activado con los parametros {0}",
                parameters);

        
        
        if (debug) {
        	try (final Connection conn = getDataSource().getConnection()) {
            	HashMap<String, String> params = new HashMap<>();
            	
            	params.put("NOMSAL", "03_29_04_2026_04_00_00_[IdProc]-Arch-[NUM]_optimus");
            	params.put("EMP", "1");
            	params.put("DESCCOL", "TIT");
            	params.put("FINI", "2026-04-29");
            	params.put("FTER", "2026-04-29");
            	params.put("TODOS_CLI", "S");
            	params.put("EsPauta", "S");
            	params.put("PAUTA_NCG380_NUM", "2");
            	params.put("PAUTA_NCG380", "S");
            	
                final Proceso proceso = ProcesoExportacionServ.newInstance(conn, params, 1L, ds);  
                proceso.procesar();
            }
        }
        else {
        	try (final Connection conn = getDataSource().getConnection()) {
            	
                final Proceso proceso = ProcesoExportacionServ.newInstance(conn, parameters, getJobId(), getDataSource());  
                proceso.procesar();
            }
        }

        LOGGER.log(Level.INFO, "Proceso de exportacion finalizado OK");
        return true;
    }
    
    public static void main(String[] args) throws Throwable 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("scotia", "connections.properties")) 
		{
			
			BcsJobExportacionServ demo = new BcsJobExportacionServ();
			
 			System.out.println("ENTRADA:");
 			
 			try 
 			{	
 				boolean ObjOut = demo.logica(con,null,true);
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			
 			
 			con.close();
 			
	    }
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
