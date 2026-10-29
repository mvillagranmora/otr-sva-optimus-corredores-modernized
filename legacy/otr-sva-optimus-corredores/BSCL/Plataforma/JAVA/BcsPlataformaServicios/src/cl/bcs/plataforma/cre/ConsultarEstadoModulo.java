package cl.bcs.plataforma.cre;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;



public class ConsultarEstadoModulo implements JavaCall {	
	
	//QUERIES
	private final String SQL_SELECT_MODULOEMPRESA = "SELECT IFNULL(\"IdEstado\",'ABT') AS \"IdEstadoModulo\", IFNULL(\"CodigoModulo\",'') as \"NombreModulo\" "
			+ "FROM \"BSCL.Plataforma.BD::PAU.Modulo\" where \"IdEmpresa\" = :IdEmpresa";
	          
	//clases
	public static class Modulo {
		public String				IdEstadoModulo;
		public String				NombreModulo;

	}
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class OutErroresDetalle{
		public String Codigo;
		public String Descripcion;
	}
	public static class OutAdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class InConsultarEstadoModulo {
		public Long 	p_IdEmpresa;
		public String	p_IdModulo;
		public Long		p_IdUsuario;
	}
	public static class OutConsultarEstadoModulo {
		public Long	 		P_OUT_ADVERTENCIASCONTADOR;
		public Long	 		P_OUT_ERRORESCONTADOR;
		public List<OutAdvertenciasDetalle> p_out_AdvertenciasDetalle;
		public List<OutErroresDetalle> 	p_out_ErroresDetalle;
		public List<Modulo> p_out_ListaModulos;
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InConsultarEstadoModulo in=gson.fromJson(jin.toString() ,InConsultarEstadoModulo.class);
		OutConsultarEstadoModulo out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://161.238.0.14:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "desarrollo";
        String pass   = "Bolsa123";		
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
			InConsultarEstadoModulo entrada = new InConsultarEstadoModulo();
			entrada.p_IdEmpresa 		= 1L;
			entrada.p_IdModulo 			= "MO3";
 			JsonParser parser 			= new JsonParser();
	        JsonObject dataJsonIn		= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ConsultarEstadoModulo demo 	= new ConsultarEstadoModulo();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutConsultarEstadoModulo logica(Connection con, InConsultarEstadoModulo entrada) throws Exception {
		OutConsultarEstadoModulo salida = new OutConsultarEstadoModulo();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		SQL sql=new SQL();
		HashMap<String,Object> parmsselect = new HashMap<String,Object>();
		parmsselect.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Modulo> listaModuloEmpresa = sql.query(con, SQL_SELECT_MODULOEMPRESA,parmsselect,Modulo.class);		
		if (listaModuloEmpresa.isEmpty()) {
			salida.p_out_ListaModulos = new ArrayList<Modulo>();
		} else {
			salida.p_out_ListaModulos = listaModuloEmpresa;
		}
		return salida;
	}
}
