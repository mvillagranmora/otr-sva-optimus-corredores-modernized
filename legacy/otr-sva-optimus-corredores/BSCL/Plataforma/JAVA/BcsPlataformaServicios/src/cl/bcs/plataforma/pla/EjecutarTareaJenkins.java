package cl.bcs.plataforma.pla;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class EjecutarTareaJenkins implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(EjecutarTareaJenkins.class);
	
	private final String SQL_Select_TareaJenkins = loadResourceToString("cl/bcs/plataforma/pla/sql/EjecutarTareaJenkins_SelectTarea.sql");

	public static class EntradaEjecutarTareaJenkins {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_IdModulo;
		public String	p_TipoTarea;
		public List<ParametroJenkins> p_Parametros;
		
		public EntradaEjecutarTareaJenkins(){
			this.p_Parametros = new ArrayList<ParametroJenkins>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroJenkins extends BcsPojo{
		public String	Nombre;
		public String	Valor;
		
		public ParametroJenkins (){
			this.Nombre = "";
			this.Valor = "";
		}
		
		public ParametroJenkins(String Nombre, String Valor) {
			this.Nombre = Nombre;
			this.Valor = Valor;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaEjecutarTareaJenkins {
		public Long CodigoHttp = 0L;
		public String MensajeHttp;
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEjecutarTareaJenkins() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjConnectJenkins {
		public String server;
		public String username;
		public String userToken;
		public String protocol;
	}
	
	public static class TareaJenkins {
		public String Ruta;
		public String Token;
	}
	
	private ObjConnectJenkins inicializaCallerJenkins() throws Exception {
		ObjConnectJenkins objConnJ = new ObjConnectJenkins();
		Properties prop = new Properties();
		
		InputStream input = new FileInputStream("jenkins.properties");
		prop.load(input);
		objConnJ.server = prop.getProperty("server");
		objConnJ.username = prop.getProperty("username");
		objConnJ.userToken = prop.getProperty("usertoken");
		objConnJ.protocol = prop.getProperty("protocol");
		
		return objConnJ;
	}
	
	public SalidaEjecutarTareaJenkins logica(Connection con, EntradaEjecutarTareaJenkins in) throws Exception {
		
		SQL sql = new SQL();
		SalidaEjecutarTareaJenkins out = new SalidaEjecutarTareaJenkins();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("TipoTarea",in.p_TipoTarea);
		List<TareaJenkins> TareasJ = sql.query(con, SQL_Select_TareaJenkins, params, TareaJenkins.class);
		
		if (TareasJ.size()>0) {
			TareaJenkins tarea = TareasJ.get(0);
			ObjConnectJenkins objConnJ = inicializaCallerJenkins();
			
			String parametros = "";
			for (ParametroJenkins param: in.p_Parametros) {
				parametros += "&"+param.Nombre+"="+param.Valor;
			}
			String build = parametros.length()>0 ? "/buildWithParameters?" : "/build?";
			
			String strUrl = objConnJ.protocol+"://"+objConnJ.server+tarea.Ruta+build+"token="+tarea.Token+parametros;
			logger.info(strUrl);
			URL urlToCall = new URL(strUrl);
			HttpURLConnection connJenkins = (HttpURLConnection) urlToCall.openConnection();
			
			String auth = objConnJ.username + ":" + objConnJ.userToken;
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			connJenkins.setRequestProperty("Authorization", authHeaderValue);
			connJenkins.setConnectTimeout(10000);
			connJenkins.setReadTimeout(10000);
			connJenkins.setDoOutput(true);
			connJenkins.setRequestMethod("GET");
			connJenkins.setRequestProperty("Content-Type", "application/json");
			
			int responseCode = connJenkins.getResponseCode();
			String responseMsg = connJenkins.getResponseMessage();
			
			out.CodigoHttp = (long)responseCode;
			out.MensajeHttp = responseMsg;
			
			connJenkins.disconnect();
		} else {
			MensajeSalida msj = new MensajeSalida();
			msj.id = 0L;
			msj.codigo = "0";
			msj.tipo = "ERROR";
			msj.descripcion = "No se encuentra TipoTarea";
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(msj);
			out.CodigoHttp = 404L;
			out.MensajeHttp = "Tarea no configurada";
		}
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaEjecutarTareaJenkins jin = gson.fromJson(in.toString() ,EntradaEjecutarTareaJenkins.class);
		SalidaEjecutarTareaJenkins out = logica(conn,jin);
		JsonParser parser 	= new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa","connections.properties")){
			EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
    		inJenkins.p_IdModulo = "PLA";
    		inJenkins.p_IdUsuario = 0L;
    		inJenkins.p_IdEmpresa = 1L;
    		inJenkins.p_TipoTarea = "REPLICA_BATCH";
    		    		
    		EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
    		SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
}