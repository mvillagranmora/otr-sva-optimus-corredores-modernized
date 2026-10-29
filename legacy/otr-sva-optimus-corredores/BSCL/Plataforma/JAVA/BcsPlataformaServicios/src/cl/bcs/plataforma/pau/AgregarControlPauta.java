package cl.bcs.plataforma.pau;

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

public class AgregarControlPauta implements JavaCall {
	private static Long idTrabajoLog = new Long(0);

	//QUERIES
	private final String SQL_AgregarControlPautaSP = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarControlPautaSP.sql");
	private final String SQL_EntradaAgregarControlPauta = loadResourceToString("cl/bcs/plataforma/pau/sql/EntradaAgregarControlPauta.sql");

	/*******************
	 * clases in/out
	 *******************/
	public static class AgregarControlPautaEntrada {
		public Long 			p_IdTrabajo;
		public List<Registro>  	p_Registros;
		public static class Registro{
			public Long 		IdTrabajo;
		    public Long 		IdPrograma; 
		    public String 		IdTipoPrograma;
		    public String 		Ejecucion; 
			public String 		FechaSistema;  //java.sql.Date
			public String 		IdEstado;	// PEN, TER, ERR
			public String 		Mensaje;		
		}
	}
	
	public static class AgregarControlPautaSalida {
		public Long P_OUT_ERRORESCONTADOR;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		AgregarControlPautaEntrada in=gson.fromJson(jin.toString() ,AgregarControlPautaEntrada.class);
		AgregarControlPautaSalida out=logica(con,in);
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
			AgregarControlPautaEntrada entrada = new AgregarControlPautaEntrada();
			entrada.p_IdTrabajo = new Long(1);
			List<AgregarControlPautaEntrada.Registro> registros = new ArrayList<AgregarControlPautaEntrada.Registro>();
			AgregarControlPautaEntrada.Registro item = new AgregarControlPautaEntrada.Registro();
			item.IdTrabajo = new Long(1);
			item.IdPrograma = new Long(1);
			item.IdTipoPrograma = "VENCIMIENTO_RF";
			item.Ejecucion = "PROGRAMADA"; 
			item.FechaSistema = "2017-01-01";
			item.IdEstado = "PEN";	// PEN, TER, ERR
			item.Mensaje = "EJEMPLO";	
			registros.add(item);	
			entrada.p_Registros = registros;
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        AgregarControlPauta demo=new AgregarControlPauta();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public AgregarControlPautaSalida logica(Connection con, AgregarControlPautaEntrada entrada) throws Exception {
		AgregarControlPautaSalida out = new AgregarControlPautaSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL sql=new SQL();
		sql.batch(con,SQL_EntradaAgregarControlPauta, entrada.p_Registros, AgregarControlPautaEntrada.Registro.class);
		HashMap<String,Object> parmsInSP =new HashMap<String,Object>();
		parmsInSP.put("p_IdTrabajo",entrada.p_IdTrabajo);
		HashMap<String,Class> parmsOutSP =new HashMap<String,Class>();
		parmsOutSP.put("P_OUT_ERRORESCONTADOR", Long.class);
		HashMap<String,Object> respuestaCCA = sql.call(con, SQL_AgregarControlPautaSP, parmsInSP, parmsOutSP);
		out.P_OUT_ERRORESCONTADOR = (Long)respuestaCCA.get("P_OUT_ERRORESCONTADOR");
		return out;
	}
	
	//Clase estática para simplificar ejecucion:
	public static void logPauta(Connection con, Log log, Long idPrograma, String idTipoPrograma, String ejecucion, String fechaSistema, String estado, String mensaje) throws Exception{
		idTrabajoLog++;
    	AgregarControlPauta cnpt = new AgregarControlPauta();
    	AgregarControlPauta.AgregarControlPautaEntrada cnptIn = new AgregarControlPauta.AgregarControlPautaEntrada();
    	cnptIn.p_IdTrabajo = idTrabajoLog;
		List<AgregarControlPauta.AgregarControlPautaEntrada.Registro> registros = new ArrayList<AgregarControlPauta.AgregarControlPautaEntrada.Registro>();
		AgregarControlPauta.AgregarControlPautaEntrada.Registro item = new AgregarControlPauta.AgregarControlPautaEntrada.Registro();
		item.IdTrabajo = idTrabajoLog;
		item.IdPrograma = idPrograma;
		item.IdTipoPrograma = idTipoPrograma;
		item.Ejecucion = ejecucion; 
		item.FechaSistema = fechaSistema;
		item.IdEstado = estado;	// PEN, TER, ERR
		item.Mensaje = mensaje;	
		registros.add(item);	
		cnptIn.p_Registros = registros;
		cnpt.logica(con, cnptIn);
		//adicionalmente loguea en servidor el mensaje:
		log.write("Log Pauta: "+mensaje);
    }
	/*
	//Clase estática para simplificar ejecucion:
		public static void logPauta(Connection con, Long idPrograma, String idTipoPrograma, String ejecucion, String fechaSistema, String estado, String mensaje) throws Exception{
			idTrabajoLog++;
	    	AgregarControlPauta cnpt = new AgregarControlPauta();
	    	AgregarControlPauta.AgregarControlPautaEntrada cnptIn = new AgregarControlPauta.AgregarControlPautaEntrada();
	    	cnptIn.p_IdTrabajo = idTrabajoLog;
			List<AgregarControlPauta.AgregarControlPautaEntrada.Registro> registros = new ArrayList<AgregarControlPauta.AgregarControlPautaEntrada.Registro>();
			AgregarControlPauta.AgregarControlPautaEntrada.Registro item = new AgregarControlPauta.AgregarControlPautaEntrada.Registro();
			item.IdTrabajo = idTrabajoLog;
			item.IdPrograma = idPrograma;
			item.IdTipoPrograma = idTipoPrograma;
			item.Ejecucion = ejecucion; 
			item.FechaSistema = fechaSistema;
			item.IdEstado = estado;	// PEN, TER, ERR
			item.Mensaje = mensaje;	
			registros.add(item);	
			cnptIn.p_Registros = registros;
			cnpt.logica(con, cnptIn);
	    }*/
}
