package cl.bcs.socionegocio.emp;

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
import cl.bcs.utils.Utils;

public class EliminarSucursalAtencion implements JavaCall {
	private final Log log = new Log("CLI_SucursalAtencion");

	// QUERIES
	private final String SQL_DELETE = "UPDATE \"BSCL.Plataforma.BD::Empleado.SucursalAtencion\" SET \"Borrado\" = 'S' WHERE \"IdSucursalAtencion\" = :IdSucursalAtencion AND \"IdEmpleado\" = :IdEmpleado";
	/*******************
	 * pojos
	 *******************/
	/*******************
	 * pojos
	 *******************/
	public static class EliminarSucursalAtencionPojo {
		public Long IdSucursalAtencion;
		public Long IdEmpleado;
	}

	/*******************
	 * clases in/out
	 *******************/
	public static class InEliminarSucursalAtencion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdSucursalAtencion;
		public Long p_IdEmpleado;
	}

	public static class OutEliminarSucursalAtencion{
		public Long P_OUT_ERRORESCONTADOR =  new Long(0);
		public String P_OUT_DESCRIPCION = "";
	}

	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		InEliminarSucursalAtencion in = gson.fromJson(jin.toString(), InEliminarSucursalAtencion.class);
		OutEliminarSucursalAtencion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/**********************
	 * lógica del programa
	 **********************/
	public OutEliminarSucursalAtencion logica(Connection conn, InEliminarSucursalAtencion entrada) throws Exception {
		log.write("[INI]Ejecucion del SP");
		log.writeAsJson("Entrada SP", entrada);
		
		SQL sql = new SQL();
		OutEliminarSucursalAtencion salida = new OutEliminarSucursalAtencion();
		
		List<EliminarSucursalAtencionPojo> params = new ArrayList<EliminarSucursalAtencionPojo>(); 
		EliminarSucursalAtencionPojo input = new EliminarSucursalAtencionPojo();
		
		input.IdEmpleado = entrada.p_IdEmpleado;
		input.IdSucursalAtencion = entrada.p_IdSucursalAtencion;
		params.add(input);
		
		try{
			SQL.exec(conn, "SET SCHEMA "+Utils.getSchema());
			sql.batch(conn, SQL_DELETE, params, EliminarSucursalAtencionPojo.class);
			conn.commit();
		}catch(Exception e){
			salida.P_OUT_DESCRIPCION = e.getMessage();
			salida.P_OUT_ERRORESCONTADOR = new Long(1);
			log.writeAsJson("Salida SP", salida);
		}
		log.write("[FIN]Finaliza la ejecución");
		return salida;
	}

	public static void main(String[] args) {
		// Settings by file
		String dbURL = "jdbc:sap://161.238.34.246:30215/?";
		String dbprop = "currentSchema=BSCL;";
		String dbdrv = "com.sap.db.jdbc.Driver";
		String dbqry = "select 1 from dummy";
		String user = "JOBMAN";
		String pass = "Bolsa123";
		// Datasource
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(dbdrv);
		ds.setUsername(user);
		ds.setPassword(pass);
		ds.setUrl(dbURL);
		ds.setValidationQuery(dbqry);
		ds.setConnectionProperties(dbprop);
		ds.setDefaultAutoCommit(false);

		// Example
		System.out.println("== START ==");
		try {
			Connection con = ds.getConnection();
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				JsonParser parser = new JsonParser();
				JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1313,\"p_IdUsuario\":101,\"p_IdModulo\":\"SOC\",\"p_IdSucursal\":4,\"p_IdEmpleado\":11442}").getAsJsonObject();
				EliminarSucursalAtencion demo = new EliminarSucursalAtencion();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
