package cl.bcs.normativa.fatca;

import java.sql.Connection;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class GenerarDatosFatca implements JavaCall {
	
	private final Log log = new Log("GenerarDatosFatca");
	private final String SP_GENERA_DATOS_FATCA = "BSCL.Normativa.BD.Servicios.FATCA::GenerarDatosFatca";
	
	public static class GenerarDatosFatcaEntrada {
		public Long p_IdEmpresa;
	}
	
	public static class GenerarDatosFatcaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		GenerarDatosFatcaEntrada in=gson.fromJson(jin.toString() ,GenerarDatosFatcaEntrada.class);
		GenerarDatosFatcaSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public GenerarDatosFatcaSalida logica(Connection con, GenerarDatosFatcaEntrada entrada) throws Exception {
		GenerarDatosFatcaSalida out = new GenerarDatosFatcaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
		try{
			StoredProcedure plog=new StoredProcedure(SP_GENERA_DATOS_FATCA);
			JsonObject entradaDatosFatca =new JsonObject();
			entradaDatosFatca.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
			entradaDatosFatca.addProperty("p_SimularOperacion", "N");
			
			log.write("Se envia a generar data FATCA:",entradaDatosFatca);
			//Salida
			JsonObject salidaDatosFatca =plog.call(con,entradaDatosFatca);
			log.write("Se termina de generar data FATCA:", salidaDatosFatca.get("P_OUT_RESULTADO").getAsJsonArray());
			out.P_OUT_MENSAJE = "El proceso GenerarDatosFatca se ha ejecutado correctamente";
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "Error al procesar archivo Cobertura patrimonial";
			log.write("Error",e);
		}

		return out;
		 
	}
	
	public static void main (String [] args) {
		
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
       
        
        //prueba de la clase
  		try {
  			Connection con = ds.getConnection();
  			try{
  				
  				GenerarDatosFatcaEntrada entrada= new GenerarDatosFatcaEntrada();
  				entrada.p_IdEmpresa		= new Long(1);
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarDatosFatca demo= new GenerarDatosFatca();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
        System.out.println("== END ==");
	}
	
	
}
