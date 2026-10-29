package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;

public class ActualizarEstadoSistema implements JavaCall {	

	//QUERIES
	private final String SQL_UPDATEESTADOSISTEMA = "UPDATE \"BSCL.Plataforma.BD::PAU.Modulo\" SET \"IdEstado\"=:IdEstado, \"CodigoUsuario\" = :CodigoUsuario, \"FechaUltimaActualizacion\" = CURRENT_UTCTIMESTAMP where \"CodigoModulo\" = 'SIS' and \"IdEmpresa\" = :IdEmpresa";
	/*******************
	 * clases in/out
	 *******************/
	
	public static class UpdateEstadoSistema{
		public Long IdEmpresa;
		public String IdEstado;
		public String CodigoUsuario;
	}
	
	public static class EstadoSistemaEntrada {
		public Long IdEmpresa;
		public String IdEstado;
		public String CodigoUsuario;
	}
	
	public static class EstadoSistemaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		EstadoSistemaEntrada in=gson.fromJson(jin.toString() ,EstadoSistemaEntrada.class);
		EstadoSistemaSalida out=logica(con,in);
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
			EstadoSistemaEntrada entrada = new EstadoSistemaEntrada();
			entrada.IdEmpresa = 1L;
			entrada.IdEstado = "ENT";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ActualizarEstadoSistema demo=new ActualizarEstadoSistema();		
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
	public EstadoSistemaSalida logica(Connection con, EstadoSistemaEntrada entrada) throws Exception {
		EstadoSistemaSalida out = new EstadoSistemaSalida();

		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();
		List<UpdateEstadoSistema> prog = new ArrayList<UpdateEstadoSistema>();
		UpdateEstadoSistema iteminsert = new UpdateEstadoSistema();
		iteminsert.IdEmpresa = entrada.IdEmpresa;
		iteminsert.IdEstado = entrada.IdEstado;
		iteminsert.CodigoUsuario = entrada.CodigoUsuario;
		prog.add(iteminsert);
		sql.batch(con,SQL_UPDATEESTADOSISTEMA, prog, UpdateEstadoSistema.class);
		
		return out;
	}
	
}
