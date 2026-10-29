package cl.bcs.plataforma.demo;

import java.sql.Connection;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;

public class ConsoleCall {

	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://161.238.34.39:30215/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SYSTEM";
        String pass   = "B0lsa123";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        
        //Example
		try {
			 Connection con = ds.getConnection();
	        String pSchema="BSCL";
	        String pProcedure="BSCL.Plataforma.BD.Servicios::ACC.ValidaHashServicioUsuario";        
	        StoredProcedure spDemo=new StoredProcedure(pProcedure);	        
	    	JsonObject din=new JsonObject();
	    	din.addProperty("p_IdEmpresa", 1);
	    	din.addProperty("p_IdUsuario", 3);
	    	din.addProperty("p_Hash", "1");	    	
	    	JsonObject call=spDemo.call(con, din);
	    	System.out.println(call.toString());	        	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}
