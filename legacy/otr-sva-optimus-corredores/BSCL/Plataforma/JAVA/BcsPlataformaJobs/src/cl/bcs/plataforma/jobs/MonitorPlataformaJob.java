package cl.bcs.plataforma.jobs;
import java.sql.Connection;

import cl.bcs.plataforma.pla.MonitorPlataforma;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
/**********************************
 * @version 	1.0
 * @author 		jgarces
 **********************************/
public class MonitorPlataformaJob extends BcsJob {
	private final Log log = new Log("MONITOR_PLATAFORMA_JOB"); 
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("  ****** INICIO  *******");
        try{	
    		//OBTENCIÓN DE PARÁMETROS
        	Long parIdEmpresa=Long.parseLong(super.getParam("IdEmpresa"));
        	String parNotificarAdministradoresTI=super.getParam("NotificarAdministradoresTI");
    		//EJECUTA PROCESO
        	MonitorPlataforma monitor = new MonitorPlataforma();
        	MonitorPlataforma.InMonitorPlataforma inmonitor= new MonitorPlataforma.InMonitorPlataforma();
        	inmonitor.p_IdEmpresa = parIdEmpresa;
        	inmonitor.p_NotificarAdministradoresTI = parNotificarAdministradoresTI;
        	monitor.logica(con, inmonitor);
        	log.write("****** FIN ******");
			return true;
        }catch (Exception e) {
			log.write("****** ERROR ******");
			log.write(Utils.stackTraceToString(e));
			e.printStackTrace();
			throw e;
		}
    }
    @Override
    public void onError(Throwable t) {
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
    }
	@Override
	public void pause() {
	}
}