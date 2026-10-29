package cl.bcs.plataforma.pau;

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

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class CambiarEstadoSistema implements JavaCall {	
	private final Log log = new Log("CambiarEstadoModulo");
	//QUERIES
	private final String SQL_SELECT_MODULO_SISTEMA = "SELECT A.\"Resultado\",A.\"CodigoModulo\",\"A\".\"IdEmpresa\",\"A\".\"CodigoProceso\" FROM "+
														"(SELECT "+
																 "\"P\".\"CodigoModulo\" "+	
														  		 ",\"P\".\"CodigoProceso\" "+
														  		 ",\"P\".\"IdEmpresa\" "+
														  		 ",MAX(\"CP\".\"FechaRegistro\") "+
														  		 ",CASE WHEN COALESCE(\"CP\".\"CodigoProceso\",'-1') = '-1' THEN 'ERR' ELSE 'OK' END AS \"Resultado\" "+
														    "FROM \"BSCL.Plataforma.BD::PAU.Proceso\" AS \"P\" "+
														    "LEFT OUTER JOIN \"BSCL.Plataforma.BD::PAU.ControlProceso\" AS \"CP\" ON (\"CP\".\"CodigoModulo\" = \"P\".\"CodigoModulo\" AND "+
														  	              														 "\"CP\".\"CodigoProceso\" = \"P\".\"CodigoProceso\" AND "+
														  	              														 "\"CP\".\"FechaSistema\" = :FechaSistema AND "+
														  	              														 "\"P\".\"IdEmpresa\" = :IdEmpresa) "+
														  	"WHERE \"P\".\"CodigoModulo\" = 'SIS' "+
														  	"AND :IdEmpresa = \"P\".\"IdEmpresa\" "+
														  	"AND \"P\".\"EsApertura\" = 'N' "+
														  	"AND \"P\".\"CodigoProceso\" != 'ABRIR_SISTEMA' "+
														  	"GROUP BY \"P\".\"CodigoModulo\",\"P\".\"CodigoProceso\",\"P\".\"IdEmpresa\",\"CP\".\"CodigoProceso\") A "+
													  "WHERE \"A\".\"Resultado\" = 'ERR'";
	
	
	private final String SQL_UPDATE_MODULO_SISTEMA="update \"BSCL.Plataforma.BD::PAU.Modulo\" set \"IdEstado\"='ABT', \"CodigoUsuario\" = 'SISTEMA', \"FechaUltimaActualizacion\" = CURRENT_UTCTIMESTAMP where \"CodigoModulo\"=:CodigoModulo and \"IdEmpresa\"=:IdEmpresa";  
	private final String SQL_UPDATECONFIGURACION = "UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\"=:Valor where \"CodigoParametro\" = :CodigoParametro and \"IdEmpresa\" = :IdEmpresa";
	//clases
	public static class Modulo {
		public String				Resultado; 
		public String				CodigoModulo; 
		public Long					IdEmpresa; 
		public String               CodigoProceso;
	}
	public static class UpdateModulo{
		public String				CodigoModulo; 
		public Long					IdEmpresa;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InCambiarEstadoSistema {
		public Long 	IdEmpresa;
		public String	FechaSistema;
	}
	public static class OutCambiarEstadoSistema {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public OutCambiarEstadoSistema() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class UpdateConfiguracion{
		public String Valor;
		public Long IdEmpresa;
		public String CodigoParametro;
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InCambiarEstadoSistema in=gson.fromJson(jin.toString() ,InCambiarEstadoSistema.class);
		OutCambiarEstadoSistema out=logica(con,in);
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
			InCambiarEstadoSistema entrada = new InCambiarEstadoSistema();
			entrada.IdEmpresa = 1L;
			entrada.FechaSistema = "2017-06-04";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambiarEstadoSistema demo=new CambiarEstadoSistema();		
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
	public OutCambiarEstadoSistema logica(Connection con, InCambiarEstadoSistema entrada) throws Exception {
		OutCambiarEstadoSistema salida = new OutCambiarEstadoSistema();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql=new SQL();
		/*
		HashMap<String,Object> parmsselect = new HashMap<String,Object>();
		parmsselect.put("IdEmpresa", entrada.IdEmpresa);
		parmsselect.put("FechaSistema", entrada.FechaSistema);
		List<Modulo> listaModulo = sql.query(con, SQL_SELECT_MODULO_SISTEMA,parmsselect,Modulo.class);		
		if(listaModulo.size() > 0){
			salida.ErroresContador++;
			salida.Mensaje="Módulo Sistema con procesos no ejecutados";
			return salida;
		}
		log.writeAsJson("Consulta Modulo Sistema:",listaModulo);
		*/
		List<UpdateConfiguracion> configuracion = new ArrayList<UpdateConfiguracion>();
		UpdateConfiguracion itemconf = new UpdateConfiguracion();
		itemconf.Valor = "";
		itemconf.CodigoParametro = "PLA.FechaSistemaDestino";
		itemconf.IdEmpresa = entrada.IdEmpresa;
		configuracion.add(itemconf);
		sql.batch(con,SQL_UPDATECONFIGURACION, configuracion, UpdateConfiguracion.class);
		
		//realizar update
		List<UpdateModulo> listaUpdate = new ArrayList<UpdateModulo>();
		UpdateModulo parmsupdate = new UpdateModulo();
		parmsupdate.CodigoModulo= "SIS";
		parmsupdate.IdEmpresa=entrada.IdEmpresa;
		listaUpdate.add(parmsupdate);
		sql.batch(con,SQL_UPDATE_MODULO_SISTEMA, listaUpdate, UpdateModulo.class);
		log.writeAsJson("Cambiar Estado Módulo, salida:",salida);
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, MensajeSalida.MENSAJE_OK, "El Modulo" + parmsupdate.CodigoModulo +" ha cambiado de estado correctamente"));
		return salida;
	}
}
