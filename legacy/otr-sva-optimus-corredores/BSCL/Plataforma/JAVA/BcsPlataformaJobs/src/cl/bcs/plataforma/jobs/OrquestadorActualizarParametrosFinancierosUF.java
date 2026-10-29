package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Log;
import cl.bcs.scheduler.BcsJob;

public class OrquestadorActualizarParametrosFinancierosUF extends BcsJob {
	
	private final Log log = new Log("PLA.OrquestadorActualizarParametrosFinancierosUF");

	@Override
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
        return logica(conn, false, "", idEmpresa);
    }

		 		
    public boolean logica(Connection con, boolean esManual, String usuario, long idEmpresa) throws Throwable {
    	
        log.write("***** Inicio OrquestadorActualizarParametrosFinancierosUF *****");
        
        boolean resultado = false;
        
        try {
        	//Siempre intentar primero con WS Banco Central
        	log.write("Ejecutando WS Banco Central...");
        	ActualizarParametrosFinancierosBancoCentralUF jobBC = new ActualizarParametrosFinancierosBancoCentralUF();
            resultado = jobBC.logica(con, esManual, usuario, idEmpresa);
            log.write("Ejecución con WS Banco Central finalizada con éxito.");
            
            //Si Banco Central no devolvio elementos, intentar con Bursátil
            if(resultado == false) {
            	log.write("WS Banco Central sin elementos, ejecutando WS Bursátil como respaldo...");
            	try {
                	ActualizarParametrosFinancierosUF jobBursatil = new ActualizarParametrosFinancierosUF();
                    resultado = jobBursatil.logica(con, esManual, usuario, idEmpresa);
                    log.write("Ejecución con WS Bursátil finalizada con éxito.");
                } catch (Exception exBursatil) {
                    log.write("Fallo también en WS Bursátil: " + exBursatil.getMessage());
                    log.write("***** Fin OrquestadorActualizarParametrosFinancierosUF con error *****");
                    return false;
                }
            }
            	 
        } catch (Exception exBC) {
        	log.write("Fallo en WS Banco Central: " + exBC.getMessage());
        	//Si Banco Central falla, intentar con Bursátil
            log.write("Ejecutando WS Bursátil como respaldo...");
            try {
            	ActualizarParametrosFinancierosUF jobBursatil = new ActualizarParametrosFinancierosUF();
                resultado = jobBursatil.logica(con, esManual, usuario, idEmpresa);
                log.write("Ejecución con WS Bursátil finalizada con éxito.");
            } catch (Exception exBursatil) {
                log.write("Fallo también en WS Bursátil: " + exBursatil.getMessage());
                log.write("***** Fin OrquestadorActualizarParametrosFinancierosUF con error *****");
                return false;
            }
        }        
        log.write("***** Fin OrquestadorActualizarParametrosFinancierosUF *****");
        return resultado;
    }
    
    @Override
    public void onError(Throwable t) {
        log.write("****** ERROR en OrquestadorActualizarParametrosFinancierosUF *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {
     
    }
}
