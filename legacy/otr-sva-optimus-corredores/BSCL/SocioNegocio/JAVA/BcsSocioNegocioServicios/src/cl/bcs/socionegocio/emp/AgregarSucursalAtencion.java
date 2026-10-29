package cl.bcs.socionegocio.emp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarSucursalAtencion implements JavaCall {
	private final Log log = new Log("CLI_SucursalAtencion");

	// QUERIES
	private final String SQL_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::Empleado.SucursalAtencion\"(\"IdSucursalAtencion\", \"IdEmpresa\", \"IdSucursal\", \"IdEmpleado\",\"Borrado\")VALUES(:IdSucursalAtencion, :IdEmpresa,:IdSucursal,:IdEmpleado,'N');";
	private final String SQL_SELECT = "SELECT \"IdSucursalAtencion\", \"IdEmpresa\", \"IdSucursal\", \"IdEmpleado\" FROM \"BSCL.Plataforma.BD::Empleado.SucursalAtencion\" WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdSucursal\" = :IdSucursal AND \"IdEmpleado\" = :IdEmpleado AND \"Borrado\" = 'N'";
	/*******************
	 * pojos
	 *******************/
	public static class AgregarSucursalAtencionPojo {
		public Long IdEmpresa;
		public Long IdSucursal;
		public Long IdEmpleado;
		public Long IdSucursalAtencion;
	}

	/*******************
	 * clases in/out
	 *******************/
	public static class InAgregarSucursalAtencion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdSucursal;
		public Long p_IdEmpleado;
	}

	public static class OutAgregarSucursalAtencion {
		public Long P_ID_SUCURSAL_ATENCION;
		public Long P_OUT_ERRORESCONTADOR = new Long(0);
		public String P_OUT_DESCRIPCION = "";
	}

	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		InAgregarSucursalAtencion in = gson.fromJson(jin.toString(), InAgregarSucursalAtencion.class);
		OutAgregarSucursalAtencion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/**********************
	 * lógica del programa
	 **********************/
	public OutAgregarSucursalAtencion logica(Connection conn, InAgregarSucursalAtencion entrada) throws Exception {
		log.write("[INI]Ejecucion del SP");
		log.writeAsJson("Entrada SP", entrada);
		
		SQL sql = new SQL();
		SQL.exec(conn, "SET SCHEMA "+Utils.getSchema());
		
		OutAgregarSucursalAtencion salida = new OutAgregarSucursalAtencion();
		List<Long> idSucursalesAtencion = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SucursalAtencion", new Long(1));
		
		if(idSucursalesAtencion!=null && !idSucursalesAtencion.isEmpty()){
			
			HashMap<String, Object> queryParams = new HashMap<>();
			queryParams.put("IdEmpresa", entrada.p_IdEmpresa);
			queryParams.put("IdSucursal", entrada.p_IdSucursal);
			queryParams.put("IdEmpleado", entrada.p_IdEmpleado);
			List<InAgregarSucursalAtencion> sucursalesAtencion = sql.query(conn, SQL_SELECT, queryParams, InAgregarSucursalAtencion.class);
			if(sucursalesAtencion.isEmpty()){
				List<AgregarSucursalAtencionPojo> params = new ArrayList<AgregarSucursalAtencionPojo>(); 
				AgregarSucursalAtencionPojo input = new AgregarSucursalAtencionPojo();
				input.IdSucursalAtencion = idSucursalesAtencion.get(0);
				input.IdEmpleado = entrada.p_IdEmpleado;
				input.IdEmpresa = entrada.p_IdEmpresa;
				input.IdSucursal = entrada.p_IdSucursal;
				params.add(input);
				sql.batch(conn, SQL_INSERT, params, AgregarSucursalAtencionPojo.class);
				conn.commit();
				salida.P_ID_SUCURSAL_ATENCION = input.IdSucursalAtencion;
			}else{
				salida.P_OUT_DESCRIPCION = "Empleado ya tiene asignada la sucursal.";
				salida.P_OUT_ERRORESCONTADOR =  new Long(1);
			}
			
		}else{
			salida.P_OUT_DESCRIPCION = "Error al obtener secuencia de ingreso valida.";
			salida.P_OUT_ERRORESCONTADOR =  new Long(1);
		}
		
		
		
		log.write("[EJE]Ejecución SQL Batch");
		log.writeAsJson("Salida SP", salida);
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
				JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1313,\"p_IdUsuario\":101,\"p_IdModulo\":\"SOC\",\"p_IdSucursal\":1,\"p_IdEmpleado\":1313}").getAsJsonObject();
				AgregarSucursalAtencion demo = new AgregarSucursalAtencion();
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
