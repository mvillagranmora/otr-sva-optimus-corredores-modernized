package cl.bcs.tesoreria.cloper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cl.bcs.utils.BcsPojo;

/** 
* Clase que obtiene información de la tabla TBCLOPER(información de transacciones) de negociación a través de WS
*
* @author	: Jorge Villagrán Vásquez
* @date		: 11/04/2017
* @version	: 1.0
*/
 
public class CloperWsBSC {
	
	private static final Logger logger = LogManager.getLogger(CloperWsBSC.class);
	
	public static class ParametrosConexionCloperWS extends BcsPojo{
		String wsdl;
		String profile;
		String password;
	}
  
   
   //---------------------
   // METODOS PUBLICOS
   //--------------------- 
	public JSONArray consultarCloper(String jsonBody, ParametrosConexionCloperWS parametrosConexion) throws Exception{
		JSONArray jsonRespuesta = null;
		if(parametrosConexion.wsdl == null || parametrosConexion.profile == null || parametrosConexion.password == null){
			logger.info("Llamada a WS cloper con parametros de conexion nulos");
			throw new Exception("Llamada a WS cloper con parametros de conexion nulos");
		}else if( "".equalsIgnoreCase(parametrosConexion.wsdl.trim()) || "".equalsIgnoreCase(parametrosConexion.profile.trim()) ||
				"".equalsIgnoreCase(parametrosConexion.password.trim())){
			logger.info("Llamada a WS cloper con parametros de conexion vacios");
			throw new Exception("Llamada a WS cloper con parametros de conexion vacios");
		}else {
			jsonRespuesta = createRESTPost( jsonBody,parametrosConexion);
		}
		
		return jsonRespuesta;
    }

	
    //---------------------
  	// METODOS PRIVADOS
  	//---------------------
	
	private JSONArray createRESTPost(String jsonBody, ParametrosConexionCloperWS parametros) throws Exception {
		JSONArray jsonArray = new JSONArray();
		HttpURLConnection conn = null;
		boolean depuracion = true;
		try {
			if(parametros.wsdl.toUpperCase().startsWith("HTTPS") && depuracion){
				trust();
			}
			
			URL urlToCall = new URL(parametros.wsdl);
			
			conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			String userPassword = parametros.profile + ":" + parametros.password;
			byte[] encodedBytes = Base64.encodeBase64(userPassword.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));
			
			String input = jsonBody;
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (conn.getResponseCode() != 200) {
				logger.error("Failed : HTTP error code : " + conn.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}else {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				StringBuilder strBldr = new StringBuilder();
				String line;
			    while( (line = br.readLine()) != null){ 
			    	strBldr.append(line);
			    }
			    
			    String jsonString = strBldr.toString();
			    JSONArray arrRespuesta = new JSONArray(jsonString);
			    
			    if(arrRespuesta.length()> 0) {
			    	JSONObject objRespuesta = arrRespuesta.getJSONObject(0);
				    if(objRespuesta.get("extraecloperResult") instanceof JSONArray) {
						jsonArray = objRespuesta.getJSONArray("extraecloperResult");
					}else {
						jsonArray.put(objRespuesta.getJSONObject("extraecloperResult"));
					}
			    }
			}
			
		 }catch (MalformedURLException e) {
			 logger.error(e.getMessage());
		 }catch (IOException e) {
			 logger.error(e.getMessage());
		 }catch (JSONException e) {
			 logger.error(e.getMessage());
		 }finally {
			 conn.disconnect();
		}
		return jsonArray;
	}
	
	public static void trust() {
		//Crear un administrador de confianza que no valide las cadenas de certificados

        TrustManager[] trustAllCerts;
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
        try { // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hv = new HostnameVerifier() {
	            public boolean verify(String urlHostName, SSLSession session) {
	                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
	                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
	                }
	                return true;
	            }
	        };
	        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (GeneralSecurityException e) {
        	logger.error(e.getMessage());
        }
    }

	/*
	 * ------------------------------
	 * Main Utilizado para Pruebas
	 * ------------------------------
	 */
	 public static void main (String args[]){
		 String url = "http://tec.bcs.cl";
		 String username = "RCERDA-WEB";
		 String password = "1";
		 

		 CloperWsBSC caller = new CloperWsBSC();
		 
		 try {
			 
			 String json = "[{\"id\":\"1\",\"fecha\":\"2024-12-04\",\"folio\":\"0\",\"usuario\":\"61\"}]";
			 
			 //Parametros PLA.CloperWsBSC
			 ParametrosConexionCloperWS parametros = new ParametrosConexionCloperWS();
			 parametros.password = password;
			 parametros.profile = username;
			 parametros.wsdl = url;
			 
			 JSONArray jsonRespuesta = caller.consultarCloper(json, parametros);
			 System.out.println(jsonRespuesta.toString());
			 
		 }
		 catch(Exception e){
			 e.printStackTrace();
			 System.out.println(e);
			 System.out.println(e.getMessage());
		 }
		 finally{
			 caller = null;
		 }
	 }
}