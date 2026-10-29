package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Log;
import cl.bcs.scheduler.BcsJob;

public class OrquestadorActualizarParametrosFinancieros extends BcsJob {
	
	private final Log log = new Log("PLA.OrquestadorActualizarParametrosFinancieros");

    /**
     * Ejecuta el job de Bursatil y luego repasa los tipos de cambio con el job de Banco Central.
     * 
     * @param con        conexión a base de datos
     * @param esManual   indica si la ejecución fue manual
     * @param usuario    usuario que ejecuta (en caso de manual)
     * @param idEmpresa  ID de la empresa a procesar
     * @return           true si alguno de los dos jobs fue exitoso, false si ambos fallaron
     */
	
	@Override
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
        return logica(conn, false, "", idEmpresa);
    }

		 		
    public boolean logica(Connection con, boolean esManual, String usuario, long idEmpresa) throws Throwable {
    	String parametroWS = "";
    	try {    		
    		parametroWS = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "MER.RepasoParamsFinancierosWSBC"); 
    	}catch (Exception ex){
    		parametroWS = "N";
    	}
    	
    	
        log.write("***** Inicio OrquestadorActualizarParametrosFinancieros *****");
        log.write("Valor del parámetro MER.RepasoParamsFinancierosWSBC: " + parametroWS);
        
        boolean resultado = false;
        
        try {
        	//Siempre intentar primero con WS Bursátil
        	log.write("Ejecutando WS Bursátil...");
        	ActualizarParametrosFinancieros jobBursatil = new ActualizarParametrosFinancieros();
            resultado = jobBursatil.logica(con, esManual, usuario, idEmpresa);
            log.write("Ejecución con WS Bursátil finalizada con éxito.");
            
            //Si parámetro = S, hacer repaso con WS Banco Central
            if("S".equalsIgnoreCase(parametroWS)) {
            	 log.write("Parámetro en 'S'. Ejecutando repaso con WS Banco Central...");
            	 try {
                     ActualizarParametrosFinancierosBancoCentral jobBC = new ActualizarParametrosFinancierosBancoCentral();
                     boolean resultadoBC = jobBC.logica(con, esManual, usuario, idEmpresa);
                     log.write("Repaso con WS Banco Central finalizado con éxito.");
                     resultado = resultado && resultadoBC;
                 } catch (Exception exBC) {
                     log.write("Error en repaso con WS Banco Central: " + exBC.getMessage());
                     // No se considera un fallo crítico, ya que Bursátil fue exitoso
                 }
            }
            
        } catch (Exception exBursatil) {
        	log.write("Fallo en WS Bursátil: " + exBursatil.getMessage());
        	// 3️⃣ Si Bursátil falla, intentar con Banco Central
            log.write("Ejecutando WS Banco Central como respaldo...");
            try {
                ActualizarParametrosFinancierosBancoCentral jobBC = new ActualizarParametrosFinancierosBancoCentral();
                resultado = jobBC.logica(con, esManual, usuario, idEmpresa);
                log.write("Ejecución con WS Banco Central finalizada con éxito.");
            } catch (Exception exBC) {
                log.write("Fallo también en WS Banco Central: " + exBC.getMessage());
                log.write("***** Fin OrquestadorActualizarParametrosFinancieros con error *****");
                return false;
            }
        }
                  
        log.write("***** Fin OrquestadorActualizarParametrosFinancieros *****");
        return resultado;
    }
    
    @Override
    public void onError(Throwable t) {
        log.write("****** ERROR en OrquestadorActualizarParametrosFinancieros *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {
     
    }
   
}
