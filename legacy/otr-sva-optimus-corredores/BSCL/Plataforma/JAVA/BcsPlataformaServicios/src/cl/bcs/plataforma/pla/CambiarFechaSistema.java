package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class CambiarFechaSistema implements JavaCall {	
	private final Log log = new Log("CambiarFechaSistema");
	//QUERIES
	private final String SQL_UpdateFecha = "update \"BSCL.Plataforma.BD::PLA.Configuracion\" set \"Valor\" = :p_FechaSistema  WHERE \"IdEmpresa\"= :p_IdEmpresa AND \"CodigoParametro\" = 'PLA.FechaSistema'";
	private final String SQL_UpdateFechaAnterior = "update \"BSCL.Plataforma.BD::PLA.Configuracion\" set \"Valor\" = :p_FechaSistemaActual  WHERE \"IdEmpresa\"= :p_IdEmpresa AND \"CodigoParametro\" = 'PLA.FechaSistemaAnterior'";
	
	/*******************
	 * clases in/out
	 *******************/
	public static class CambiarFechaSistemaEntrada {
		public Long 		p_IdEmpresa;
		public String 		p_FechaSistema;//nueva
		public String 		p_FechaSistemaActual;//fecha de sistema antes de cambiar de dia
	}
	
	public static class CambiarFechaSistemaSalida {
		public Long 		P_OUT_ERRORESCONTADOR;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		CambiarFechaSistemaEntrada in=gson.fromJson(jin.toString() ,CambiarFechaSistemaEntrada.class);
		CambiarFechaSistemaSalida out=logica(con,in);
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
			CambiarFechaSistemaEntrada entrada = new CambiarFechaSistemaEntrada();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_FechaSistema = "2017-03-22";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambiarFechaSistema demo=new CambiarFechaSistema();		
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
	public CambiarFechaSistemaSalida logica(Connection con, CambiarFechaSistemaEntrada entrada) throws Exception {
		CambiarFechaSistemaSalida salida = new CambiarFechaSistemaSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL sql=new SQL();
		List<CambiarFechaSistemaEntrada> params=new ArrayList<CambiarFechaSistemaEntrada>();
		params.add(entrada);		
		sql.batch(con,SQL_UpdateFecha, params,CambiarFechaSistemaEntrada.class);
		sql.batch(con,SQL_UpdateFechaAnterior, params,CambiarFechaSistemaEntrada.class);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
	}
}
