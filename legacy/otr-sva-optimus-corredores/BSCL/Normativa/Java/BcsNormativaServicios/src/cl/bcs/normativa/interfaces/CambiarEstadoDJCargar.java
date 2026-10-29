package cl.bcs.normativa.interfaces;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class CambiarEstadoDJCargar implements JavaCall{
	private final Log log = new Log("CambiarEstadoDJCargar");
	//QUERIES
	private final String SQL_UpdateEstadoDJ = "update \"BSCL.Normativa.BD::TRI.ControlProceso\" set \"Carga\" = 'S', \"Validacion\" = 'N', \"PreDeclaracion\" = 'N', \"EstadoCertificado\" = (case \"EstadoCertificado\" when 'GEN' then 'GEA' else \"EstadoCertificado\" end) WHERE \"Esquema\"= :Esquema AND \"Version\" = :Version";
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InCambiarEstadoDJ {
		public String 		Esquema;
		public String 		Version;
	}
	
	public static class CambiarEstadoDJSalida {
		public Long 		P_OUT_ERRORESCONTADOR;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InCambiarEstadoDJ in=gson.fromJson(jin.toString() ,InCambiarEstadoDJ.class);
		CambiarEstadoDJSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
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
			InCambiarEstadoDJ entrada = new InCambiarEstadoDJ();
			entrada.Esquema = "DJ1885";
			entrada.Version = "2017";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambiarEstadoDJCargar demo=new CambiarEstadoDJCargar();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public CambiarEstadoDJSalida logica(Connection con, InCambiarEstadoDJ entrada) throws Exception {
		CambiarEstadoDJSalida salida = new CambiarEstadoDJSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL sql=new SQL();
		List<InCambiarEstadoDJ> params=new ArrayList<InCambiarEstadoDJ>();
		params.add(entrada);		
		sql.batch(con,SQL_UpdateEstadoDJ, params,InCambiarEstadoDJ.class);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
	}
}
