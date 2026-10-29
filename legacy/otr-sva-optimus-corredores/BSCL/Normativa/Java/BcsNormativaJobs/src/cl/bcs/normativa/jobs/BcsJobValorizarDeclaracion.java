package cl.bcs.normativa.jobs;
  

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonObject;
import java.util.HashMap;
import cl.bcs.normativa.declaraciones.DJ1874.ValorizarDeclaracion;
import cl.bcs.normativa.declaraciones.DJ1874.ValorizarDeclaracion.ValorizarDeclaracionEntrada;
import cl.bcs.normativa.declaraciones.DJ1874.ValorizarDeclaracion.ValorizarDeclaracionSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import javax.sql.DataSource;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobValorizarDeclaracion extends BcsJob implements JavaCall{
	
	public static class RegistroValorCuota{
		public String 	FechaValorCuota;
		
		public RegistroValorCuota() {
			this.FechaValorCuota = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class RegistroPortafolioFecha{
		public Long IdPortafolio;
		public String FechaValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdMLT {
		
		public Long IdEmpresa;
	    
		
	}
	AtomicBoolean running=new AtomicBoolean(false); 
	
	SQL sql=new SQL();
	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
    	running.set(true);
        DataSource datasource = super.getDataSource();
    	
		
        Connection dbConnection = null;
	
		try {
			
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			final long IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
			final String IdModulo=this.getParam("IdModulo");
			final long Version=Long.parseLong(this.getParam("Version"));
			final String FuenteDatos=this.getParam("FuenteDatos");
			
			
			//---- para debug ------
			/*
			dbConnection=dbcon;
			final long idEmpresa =1L;	
			final long IdUsuario =6L;
			final String IdModulo="NOR";
			final long Version=2022L;
			final String FuenteDatos="INICIAL";
			
			*/
			//---- para debug ------
			
	        	HashMap<String,Object> params = new HashMap<String,Object>();
    		params.put("IdEmpresa", idEmpresa);

				
        	try{
    			
        		ValorizarDeclaracion valorizar = new ValorizarDeclaracion();
        		ValorizarDeclaracionEntrada entradaValorizar = new ValorizarDeclaracionEntrada();
        		
        		System.out.println("====== INICIO DE PROCESO ===========");
        		entradaValorizar.p_IdEmpresa = idEmpresa;
        		entradaValorizar.p_IdUsuario = IdUsuario;
				entradaValorizar.p_IdModulo = IdModulo;
				entradaValorizar.p_Version = Version;
				entradaValorizar.p_FuenteDatos = FuenteDatos;
				ValorizarDeclaracionSalida salida = valorizar.logica(dbConnection, entradaValorizar);
				System.out.println(salida);
				
				System.out.println("====== TERMINO DE PROCESO ===========");
				dbConnection.commit();
        	
        		
        	}catch(Exception ex){
        		dbConnection.rollback();
        		ex.printStackTrace(); 
        	}
        	
        	
		} catch (Exception e) {
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
		BcsJobValorizarDeclaracion a = new BcsJobValorizarDeclaracion();
		try {
			boolean  b= a.transaction(con);
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		
		return null;
	}
}
