package cl.bcs.plataforma.val.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

/**
* Clase que implementa llamadas al Valorizador de Negociacion mediante el consumo de WS (unitario por el momento).
*
* @author	: Jorge Garcés :: JG
* @date		: 03/11/2016
* @version	: 1.0
*/

public class CuponesWsBCS {
	static final Logger logger = LogManager.getLogger(CuponesWsBCS.class.getName());
	//Globales
	public boolean depuracion;
	public String server;
	public String user;
	public String pass;
	
    /*
     * llamaWSValorizador
     * Constructor de la clase.
     */
    public CuponesWsBCS(){
    	this.depuracion = false;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear modo depuraciï¿½n.
     */
    public CuponesWsBCS(boolean depuracion){
    	this.depuracion = depuracion;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion
     */
    public CuponesWsBCS(String server, String user, String pass){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = false;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion y modo
     */
    public CuponesWsBCS(String server, String user, String pass, boolean depuracion){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = depuracion;
    }

	//---------------------
	// METODOS PUBLICOS
	//---------------------
	public String obtenerCupon(String jsonBody)
    {	
		return createRESTPost(this.server, this.user, this.pass, jsonBody);
    }

    //---------------------
  	// METODOS PRIVADOS
  	//---------------------
	private String createRESTPost(String url, String username, String password, String jsonBody)
	{
		String jsonString = "";
		//Serializar objeto
		try {
			logger.info("inicia cupones ws bcs");
			URL urlToCall = new URL(url+"/PTCupones/api/cupones/cupones");
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			conn.setConnectTimeout(120000);
			conn.setReadTimeout(120000);
			String userPassword = username + ":" + password;
			byte[] encodedBytes = Base64.encodeBase64(userPassword.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));

			String input = jsonBody;
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (conn.getResponseCode() < 200 && conn.getResponseCode()>299 ) {
				logger.error("Failed : HTTP error code : " + conn.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    jsonString = strBldr.toString();
		    conn.disconnect();
			logger.info("fin cupones ws bcs");

			if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+url+"\nllamada body: "+jsonBody + "\nllamada respuesta: "+jsonString+"\n*********************");
		} catch (java.net.SocketTimeoutException e) {
			logger.info("CuponesWSBCS - entra a SocketTimeoutException");
			logger.debug("CuponesWSBCS - entra a SocketTimeoutException");
		} catch(Exception e) {
			logger.error(Utils.stackTraceToString(e));
		 }
		return jsonString;
	}
	
	public static void trust() {
        // Create a trust manager that does not validate certificate chains
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
        } catch (GeneralSecurityException e) {
        }
    }

	/*
	 * ------------------------------
	 * main
	 * Utilizado para Pruebas.
	 * ------------------------------
	 */
	 public static void main (String args[]){
		 String entrada = "[{\"ID\": \"1\",\"Nemo\": \"BCHIS-P12A\",\"Plazo\": \"0\",\"FechaIni\": \"20100228\",\"FechaFin\": \"20280928\",\"TipoMod\": \"D\"},{\"ID\": \"2\",\"Nemo\": \"BCHIS-P12X\",\"Plazo\": \"0\",\"FechaIni\": \"20170228\",\"FechaFin\": \"20200218\",\"TipoMod\": \"D\"}]";
		 String url = "http://tec.bcs.cl/PTCupones/api/cupones/cupones";
		 String username = null;
		 String password = null;
		 CuponesWsBCS caller = new CuponesWsBCS(true);
		 try {
			 String jsonStr = caller.createRESTPost(url, username, password,entrada);
			 System.out.println("jsonStr");
			 JSONArray salida = new JSONArray(jsonStr);
			 JSONObject cabeceraCupon = salida.getJSONObject(0).getJSONObject("cabeza");
			 System.out.println(cabeceraCupon.toString());
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