package cl.bcs.plataforma.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Sesion;
import cl.bcs.utils.Utils;

public class SSOUtils {
	
	private static final Logger logger = LogManager.getLogger(SSOUtils.class);

	public static String queryConfPredeterminada = "SELECT TOP 1 \"URLAuth\",\"ClientID\",\"RedirectURL\" FROM \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" WHERE \"Predeterminado\" = 'S'";
	public static String queryConfiguracion = "SELECT \"URLAuth\",\"ClientID\",\"RedirectURL\",\"ClientSecret\",\"URLToken\" FROM \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" WHERE \"IdEntidad\" = :IdEntidad";
    
    public static class ConfiguracionSSO {
    	public String URLAuth;
    	public String ClientID;
    	public String RedirectURL;
    	public String ClientSecret;
    	public String URLToken;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }

	
	static String selectUsuario = "SELECT u.\"IdUsuario\",u.\"IdEstado\", FEC.\"Valor\" AS \"FechaSistema\" , CURRENT_UTCTIMESTAMP as \"FechaReal\",u.\"FechaBloqueoTemporal\",CURRENT_UTCTIMESTAMP as \"CurrentUtcTimestamp\", "
			+ "CASE emp.\"IdEstado\" WHEN 'ACT' THEN CASE emp.\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstadoEmpleado\",\"EsAutorizado\" "
			+ "FROM BSCL.\"BSCL.SocioNegocio.BD::PER.DireccionElectronica\" dir "
			+ "LEFT JOIN BSCL.\"BSCL.SocioNegocio.BD::EMP.Empleado\" emp ON emp.\"IdPersona\"  = dir.\"IdPersona\" AND emp.\"IdEmpresa\"  = :IdEmpresa "
			+ "LEFT JOIN BSCL.\"BSCL.Plataforma.BD::ACC.Usuario\" u ON emp.\"IdEmpleado\"  = u.\"IdEntidad\" AND u.\"TipoEntidad\" = 'EMP' AND u.\"UsuarioCorredores\" = 'S' "
			+ "CROSS JOIN BSCL.\"BSCL.Plataforma.BD::PLA.Configuracion\" FEC "
			+ "WHERE UPPER(\"Direccion\")  = UPPER(:Direccion) AND FEC.\"CodigoParametro\" = 'PLA.FechaSistema' AND dir.\"IdEstado\" ='ACT' AND dir.\"Predeterminado\" = 'S' AND FEC.\"IdEmpresa\" = :IdEmpresa AND u.\"IdUsuario\" IS NOT NULL";
	
	public static class UsuarioProceso {
		public Long IdUsuario;
		public String CodigoUsuario;
		public String FechaSistema;
		public String IdEstado;
		public String EsAutorizado;
		public String IdEstadoEmpleado;
		public Timestamp FechaBloqueoTemporal;
		public Timestamp CurrentUtcTimestamp;
		public Timestamp FechaReal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//genera una sesion para un usuario que entro por SSO
	public static String createSSOOptimusSession(Connection conn,Long idEmpresa,String entidad, String mail) throws Exception {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("Direccion", mail);
		params.put("IdEmpresa", idEmpresa);
		List<UsuarioProceso> usuario = sql.query(conn, selectUsuario , params   , UsuarioProceso.class);

		//0 usuarios = error usuario no existe
		//1 usuario = OK
		//>1 usuarios error, usuarios mal configurados
		
		//INTENTO DE SESION
		
		if (usuario.size() == 0) {
			logger.error("No existe usuario asociado al correo:" + mail);
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INTENTO_SESION,"Correo SSO",mail);
			return "USUARIO_NO_ENCONTRADO";
		}
		else if (usuario.size()>1) {
			logger.error("Existen multiples usuarios asociados al mismo correo:" + usuario.toString());
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INTENTO_SESION,"Correo SSO",mail);
			return "USUARIO_MAL_CONFIGURADO";
		}
		else if (usuario.size()==1 && usuario.get(0).IdEstado != null && usuario.get(0).IdEstado.equalsIgnoreCase("BLO")) {
			logger.error("Usuario bloqueado:" + usuario.toString());
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INICIO_SESION_FALLIDA,"Correo SSO",mail);
			return "USUARIO_BLOQUEADO";
		}
		//EsAutorizado != 'S' en tabla usuarios => ERROR USUARIO NO AUTORIZADO
		else if (!usuario.get(0).EsAutorizado.equalsIgnoreCase("S")) {
			logger.error("Usuario no autorizado:" + usuario.toString());
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INICIO_SESION_FALLIDA,"Correo SSO",mail);
			return "USUARIO_NO_AUTORIZADO";
		}
		//UsuarioVigente --todo
		//FechaBloqueoTemporal
		else if (usuario.get(0).FechaBloqueoTemporal.after(usuario.get(0).CurrentUtcTimestamp)) {
			logger.error("Usuario con bloqueo temporal:" + usuario.toString());
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INICIO_SESION_FALLIDA,"Correo SSO",mail);
			return "USUARIO_BLOQUEO_TEMPORAL";
		}
		//Empleado bloqueado
		else if (usuario.get(0).IdEstadoEmpleado != null && !usuario.get(0).IdEstadoEmpleado.equalsIgnoreCase("ACT")) {
			logger.error("Empleado bloqueado:" + usuario.toString());
			CrearSesion.agregarLogSesion(conn,0L,java.time.LocalDate.now().toString(),"SSO",idEmpresa,CrearSesion.INICIO_SESION_FALLIDA,"Correo SSO",mail);
			return "EMPLEADO_BLOQUEADO";
		}
		
		String token = Sesion.insertarSesion(conn, idEmpresa, usuario.get(0).CodigoUsuario, usuario.get(0).IdUsuario, usuario.get(0).FechaReal , "SSO", usuario.get(0).FechaSistema,entidad);
		
		CrearSesion.agregarLogSesion(conn,usuario.get(0).IdUsuario,usuario.get(0).FechaSistema,"SSO",idEmpresa,CrearSesion.INTENTO_SESION,"Correo SSO",mail);
				
		return token;
	}
	
	public static String decodeIdToken(String token) {
		return "";
	}
	
	public static String callRestPostOAuth2Token(String url,String input, boolean debug) throws Exception
	{
		String outputJson = "";
		//Serializar objeto
		try {
			URL urlToCall = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			BufferedReader br;
			if (conn.getResponseCode() < 200 || conn.getResponseCode()>299 ) {
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}
			else {
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    outputJson = strBldr.toString();
			conn.disconnect();
			if(debug) System.out.println("***** DEPURACION ****\nllamada url: "+url+"\nllamada body: "+input + "\nllamada respuesta: "+outputJson+"\n*********************");
		 } catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;

		 } catch (IOException e) {
			e.printStackTrace();
			throw e;
		 }
		return outputJson;
	}
	
	public static String callRestPostOAuth2UserInfo(String url, String bearer, boolean debug) throws Exception
	{
		String outputJson = "";
		//Serializar objeto
		try {
			URL urlToCall = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + bearer);
			BufferedReader br;
			if (conn.getResponseCode() < 200 || conn.getResponseCode()>299 ) {
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}
			else {
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    outputJson = strBldr.toString();
			conn.disconnect();
			if(debug) System.out.println("***** DEPURACION ****\nllamada url: "+url+"\nllamada respuesta: "+outputJson+"\n*********************");
		 } catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;

		 } catch (IOException e) {
			e.printStackTrace();
			throw e;
		 }
		return outputJson;
	}

	public static String procesar(Connection conn,HttpServletRequest request,String entidad) throws Exception {
		SQL sql = new SQL();
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("IdEntidad", entidad);
		List<ConfiguracionSSO> confs = sql.query(conn, SSOUtils.queryConfiguracion, params  , SSOUtils.ConfiguracionSSO.class);
		if (confs.size() == 0) {
			throw new ServletException("SSO no configurado");
		}
		ConfiguracionSSO conf = confs.get(0);
		
		String leCode = "";
		Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
        	String paramName = parameterNames.nextElement();
        	if (paramName.equalsIgnoreCase("code")) {
        		 String[] paramValues = request.getParameterValues(paramName);
        		 leCode = paramValues[0];
        	}
        }
        
        String clientId=conf.ClientID;
        String clientSecret=conf.ClientSecret;
        String redirectUri = conf.RedirectURL;
        String tokenURL = conf.URLToken;
        
		String leClientId = "client_id="+URLEncoder.encode(clientId,"UTF-8");
		String leClientSecret = "client_secret=" + URLEncoder.encode(clientSecret,"UTF-8");
		String leRedirectUri ="redirect_uri=" + URLEncoder.encode(redirectUri,"UTF-8");
		String query = "code="+URLEncoder.encode(leCode,"UTF-8")+"&"+leClientId +"&" + leClientSecret + "&" + leRedirectUri + "&grant_type=authorization_code";
		logger.info("query" + query);
		
		String outputString = SSOUtils.callRestPostOAuth2Token(tokenURL,query,false);
		logger.info("salida" + outputString);
		JsonObject jsonObject = new JsonParser().parse(outputString).getAsJsonObject();
		//String access_token = jsonObject.get("access_token").getAsString();
		JsonElement id_tokenObj = jsonObject.get("id_token");
		JsonElement invalid_grant = jsonObject.get("error");
		String html = "";
		HashMap<String, HashMap<String, String>> parametrosConfiguracion = Utils.getConfiguraciones(conn,1);
		HashMap<String,String> configPla = parametrosConfiguracion.get("PLATAFORMA");
		
		if (id_tokenObj != null) {
			String id_token = id_tokenObj.getAsString();

			String[] parts = id_token.split("\\.");
			JsonObject payload = new JsonParser().parse(new String(Base64.decodeBase64((parts[1])))).getAsJsonObject();
			logger.info("payload:" + Utils.printJson(payload, true));
			
			String email = "";
			try {
				email = payload.get("email").getAsString();
			}catch(Exception ex) {
				try {
					email = payload.get("mail").getAsString();
				}catch(Exception ex2) {
					throw new ServletException("SSO payload sin direccion electronica");
				}
			}
			
			String tokenOptimus = SSOUtils.createSSOOptimusSession(conn, 1L, entidad, email);
			
			if (tokenOptimus.equalsIgnoreCase("USUARIO_NO_ENCONTRADO")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "No se encontró el usuario asociado al identificador SSO, por favor revisar configuracion de correo predeterminado del empleado en el modulo Socio de Negocio");
			}
			else if (tokenOptimus.equalsIgnoreCase("USUARIO_MAL_CONFIGURADO")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "Usuario mal configurado, existen multiples usuarios asociados al mismo correo");
			}
			else if (tokenOptimus.equalsIgnoreCase("USUARIO_BLOQUEADO")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "El usuario se encuentra bloqueado");
			}
			else if (tokenOptimus.equalsIgnoreCase("EMPLEADO_BLOQUEADO")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "El empleado se encuentra bloqueado o pendiente de autorizar");
			}
			else if (tokenOptimus.equalsIgnoreCase("USUARIO_BLOQUEO_TEMPORAL")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "El usuario se encuentra con bloqueo temporal");
			}
			else if (tokenOptimus.equalsIgnoreCase("USUARIO_NO_AUTORIZADO")) {
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
				html = StringUtils.replace(html, "<::MENSAJE::>", "El usuario no se encuentra autorizado");
			}
			
			else {
				
				
				html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","autologin.html");
	    		String libbase=Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_lib.html");

				html = StringUtils.replace(html, "<::TOKEN::>", tokenOptimus);
				html = StringUtils.replace(html, "<::LIB_BASE::>", libbase);
			}
		}
		else if (invalid_grant != null) {
			html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","ssoerror.html");
			html = StringUtils.replace(html, "<::MENSAJE::>", "Error de autorizacion de sistema SSO");
		}
		else {
			
		}
		
		return html;
	}

	/*public static void main(String [] args) {
		try {
			SSOUtils.createSSOOptimusSession(PlataformaUtils.getPropertiesConnection("merrill", "connections.properties"), 1L, "BOFA", "LUCA.BENATTI@BOFA.COM");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/
	
	
	
	
}
