package cl.bcs.normativa.jobs;
  

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonObject;
import java.util.HashMap;
import cl.bcs.normativa.ValorizarNCG499;
import cl.bcs.normativa.ValorizarNCG499.ValorizarNCGEntrada;
import cl.bcs.normativa.ValorizarNCG499.ValorizarNCGSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import javax.sql.DataSource;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobValorizarNCG499 extends BcsJob implements JavaCall{

	AtomicBoolean running = new AtomicBoolean(false); 
	
	SQL sql=new SQL();

	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
    	running.set(true);
        DataSource datasource = super.getDataSource();
        Connection dbConnection = null;
	
		try {
			
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			final long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			final long idPais = Long.parseLong(this.getParam("IdPais"));
			final String idModulo = this.getParam("IdModulo");
			final String Fecha = this.getParam("Fecha");
			
			//---- Para debug ------
			/*dbConnection = dbcon;
			final long idEmpresa = 1L;
			final long idUsuario = 6L;
			final long idPais = 44L;
			final String idModulo = "CUS";
			final String Fecha = "2024-03-31";
			*/
	        HashMap<String,Object> params = new HashMap<String,Object>();
    		params.put("IdEmpresa", idEmpresa);

        	try {
        		ValorizarNCG499 valorizar = new ValorizarNCG499();
        		ValorizarNCGEntrada entrada = new ValorizarNCGEntrada();
        		
        		System.out.println("====== INICIO DE PROCESO ===========");
        		entrada.p_IdEmpresa = idEmpresa;
        		entrada.p_IdUsuario = idUsuario;
        		entrada.p_IdPais = idPais;
        		entrada.p_IdModulo = idModulo;
        		entrada.p_Fecha = Fecha;

				ValorizarNCGSalida salida = valorizar.logica(dbConnection, entrada);
				System.out.println(salida);
				System.out.println("====== TERMINO DE PROCESO ===========");
                
				dbConnection.commit();
        		
        	} catch(Exception ex) {
        		dbConnection.rollback();
        		ex.printStackTrace(); 
        	}
		} catch(Exception e) {
			dbConnection.rollback();
    		e.printStackTrace(); 
		}
	
		return true;
	}

	@Override
    public void onError(Throwable t) {
        System.out.println(":BcsJobProcesarServ:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Connection con = PlataformaUtils.getDESAConnection();
		BcsJobValorizarNCG499 a = new BcsJobValorizarNCG499();
		
		try {
			boolean  b = a.transaction(con);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
}
