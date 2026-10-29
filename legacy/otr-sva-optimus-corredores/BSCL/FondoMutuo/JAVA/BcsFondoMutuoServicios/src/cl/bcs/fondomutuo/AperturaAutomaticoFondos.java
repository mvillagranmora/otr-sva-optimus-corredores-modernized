package cl.bcs.fondomutuo;

import java.sql.Connection;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;

public class AperturaAutomaticoFondos implements JavaCall {
	
	private final Log log = new Log("AperturaAutomaticoFondos");
	
	public static class AperturaAutomaticoFondosEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
	}
	
	public static class AperturaAutomaticoFondosSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	

	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		AperturaAutomaticoFondosEntrada in=gson.fromJson(jin.toString() ,AperturaAutomaticoFondosEntrada.class);
		AperturaAutomaticoFondosSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public AperturaAutomaticoFondosSalida logica(Connection con, AperturaAutomaticoFondosEntrada entrada) throws Exception {
		AperturaAutomaticoFondosSalida out = new AperturaAutomaticoFondosSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		
		StoredProcedure plog=new StoredProcedure("BSCL.FondoMutuo.BD.Servicios::AperturaAutomaticoFondos");
		JsonObject entradaAperturaAutomaticoFondos =new JsonObject();
		entradaAperturaAutomaticoFondos.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
		entradaAperturaAutomaticoFondos.addProperty("p_IdUsuario", entrada.p_IdUsuario);
		
		log.write("Se envia a Abrir fondos automaticamente:",entradaAperturaAutomaticoFondos);
		//Salida
		JsonObject salidaAperturaAutomaticoFondosSalida=plog.call(con,entradaAperturaAutomaticoFondos);
		 
		out.P_OUT_ERRORESCONTADOR =  salidaAperturaAutomaticoFondosSalida.get("P_OUT_ERRORESCONTADOR").getAsLong();
				
		JsonArray errores  = salidaAperturaAutomaticoFondosSalida.get("P_OUT_ERRORESDETALLE").getAsJsonArray();
		
		for(JsonElement error : errores){
			out.P_OUT_MENSAJE = error.getAsJsonObject().get("Descripcion").getAsString();
		}
		
		//log.write("Se termina de abrir los fondos",salidaAperturaAutomaticoFondosSalida.get("P_OUT_RESULTADO").getAsJsonArray());
		log.write("Se termina de abrir los fondos");
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
  				
  				AperturaAutomaticoFondosEntrada entrada= new AperturaAutomaticoFondosEntrada();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				AperturaAutomaticoFondos demo= new AperturaAutomaticoFondos();
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
