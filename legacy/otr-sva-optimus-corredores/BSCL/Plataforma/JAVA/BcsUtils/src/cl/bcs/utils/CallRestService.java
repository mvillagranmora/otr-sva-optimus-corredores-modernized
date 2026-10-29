package cl.bcs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

public class CallRestService {
	private String server;
	private String user;
	private String pass;
	private String url;
	private boolean debug;
	private boolean auth;

	//CONSTRUCTORES
	public CallRestService(String server, String url, String user, String pass, boolean auth, boolean debug) {
		super();
		this.debug = debug;
		this.server = server;
		this.user = user;
		this.pass = pass;
		this.url = url;
		this.auth = auth;
		this.debug = debug;
	}
	public CallRestService(String server, String user, String pass, String url, boolean auth) {
		super();
		this.server = server;
		this.user = user;
		this.pass = pass;
		this.url = url;
		this.auth = auth;
		this.debug = false;
	}
	public CallRestService(String server, String url) {
		super();
		this.server = server;
		this.url = url;
	}
	//GETTERS Y SETTERS	
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean needAuth(){
		return auth;
	}
	public void setAuth(boolean auth){
		this.auth = auth;
	}
	//METODOS PUBLICOS
	/**
	 * Llama a un servicio REST mediante el metodo POST con un request en formato JSON
	 * @param input entrada en formato JSON {"Llave": "Valor", "Numero": 1}
	 * @return retorna un String con el resultado.
	 * @throws Exception
	 */
	public String callRestPost(String input) throws Exception
	{
		String outputJson = "";
		//Serializar objeto
		try {
			URL urlToCall = new URL(this.server+this.url);
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			if(needAuth()){
				String userPassword = this.user + ":" + this.pass;
				byte[] encodedBytes = Base64.encodeBase64(userPassword.getBytes());
				conn.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));
			}
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (conn.getResponseCode() < 200 && conn.getResponseCode()>299 ) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    outputJson = strBldr.toString();
			conn.disconnect();
			if(this.debug) System.out.println("***** DEPURACION ****\nllamada url: "+this.url+"\nllamada body: "+input + "\nllamada respuesta: "+outputJson+"\n*********************");
		 } catch (MalformedURLException e) {
			e.printStackTrace();

		 } catch (IOException e) {
			e.printStackTrace();
		 }
		return outputJson;
	}
	//METODOS PUBLICOS
		/**
		 * Llama a un servicio REST mediante el metodo POST con un request en formato JSON
		 * @param input entrada en formato JSON {"Llave": "Valor", "Numero": 1}
		 * @return retorna un String con el resultado.
		 * @throws Exception
		 */
		public String callRestPostOAuth2(String input) throws Exception
		{
			String outputJson = "";
			//Serializar objeto
			try {
				URL urlToCall = new URL(this.server+this.url);
				HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setDoOutput(true);
//				if(needAuth()){
//					String userPassword = this.user + ":" + this.pass;
//					byte[] encodedBytes = Base64.encodeBase64(userPassword.getBytes());
//					conn.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));
//				}
				try (OutputStream os = conn.getOutputStream()) {
	                os.write(input.getBytes());
	                os.flush();
	            }
				
				if (conn.getResponseCode() < 200 && conn.getResponseCode()>299 ) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				StringBuilder strBldr = new StringBuilder();
			    String line;
			    while( (line = br.readLine()) != null) { strBldr.append(line); }
			    outputJson = strBldr.toString();
				conn.disconnect();
				if(this.debug) System.out.println("***** DEPURACION ****\nllamada url: "+this.url+"\nllamada body: "+input + "\nllamada respuesta: "+outputJson+"\n*********************");
			 } catch (MalformedURLException e) {
				e.printStackTrace();

			 } catch (IOException e) {
				e.printStackTrace();
			 }
			return outputJson;
		}
	//METODOS PUBLICOS	
	/**
	 * Llama a un servicio REST mediante el metodo GET
	 * @param queryParams entrada con parametros por metodo get Ej. user=dcaceres&firstdate=2025-07-01
	 * @return retorna un String con el resultado.
	 * @throws Exception
	 */
	public String callRestGet(String queryParams) throws Exception {
	    String outputJson = "";
	    try {
	        String baseUrl = this.server + this.url;	        

	        URL urlToCall = new URL(baseUrl + "?" + queryParams);

	        HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Accept", "application/json");

	        int statusCode = conn.getResponseCode();

	        InputStream is = (statusCode >= 200 && statusCode <= 299)
	            ? conn.getInputStream()
	            : conn.getErrorStream();

	        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
	            StringBuilder strBldr = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                strBldr.append(line);
	            }
	            outputJson = strBldr.toString();
	        }

	        if (debug) {
	            System.out.println("***** DEPURACION GET ****");
	            System.out.println("URL llamada: " + urlToCall);
	            System.out.println("HTTP status: " + statusCode);
	            System.out.println("Respuesta: " + outputJson);
	            System.out.println("*********************");
	        }

	        conn.disconnect();

	        if (statusCode < 200 || statusCode > 299) {
	            throw new RuntimeException("HTTP error code: " + statusCode + "\nRespuesta: " + outputJson);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return outputJson;
	}
		
	//METODOS PUBLICOS
	/**
	 * Llama a un servicio REST mediante el metodo POST con un request en formato JSON
	 * @return retorna un String con el resultado.
	 * @throws Exception
	 */
	public String callRestPostHeaderAuth(String input) throws Exception
	{
		String outputJson = "";
		//Serializar objeto
		try {
			URL urlToCall = new URL(this.server+this.url);
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			//HEADER DE AUTORIZACION
			conn.setRequestProperty("Authorization", "Basic " + new String(this.pass));

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (conn.getResponseCode() < 200 && conn.getResponseCode()>299 ) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    outputJson = strBldr.toString();
			conn.disconnect();
			if(this.debug) System.out.println("***** DEPURACION ****\nllamada url: "+this.url+"\nllamada body: "+input + "\nllamada respuesta: "+outputJson+"\n*********************");
		 } catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;

		 } catch (IOException e) {
			e.printStackTrace();
			throw e;
		 }
		return outputJson;
	}
	
	/**
	 * Llama a un servicio REST mediante el metodo POST con un request en formato JSON y un token Authorization.
	 *
	 * @param input JSON que se enviará en el cuerpo de la solicitud.
	 * @param jwt Token String para autorización.
	 * @return retorna un String con el resultado.
	 * @throws Exception
	 */
    public String callRestPostBearerToken(String input, String jwt) throws Exception {
    	String outputJson = "";
    	try {
			URL urlToCall = new URL(this.server+this.url);
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + jwt); 
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (conn.getResponseCode() < 200 && conn.getResponseCode()>299 ) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    outputJson = strBldr.toString();
			conn.disconnect();
			if(this.debug) System.out.println("***** DEPURACION ****\nllamada url: "+this.url+"\nllamada body: "+input + "\nllamada respuesta: "+outputJson+"\n*********************");
			
		 } catch (MalformedURLException e) {
			e.printStackTrace();

		 } catch (IOException e) {
			e.printStackTrace();
		 }
		return outputJson;
    	
    }
	
  //METODOS PUBLICOS	
  	/**
  	 * Llama a un servicio REST mediante el metodo GET y un token Authorization.
  	 * @param queryParams entrada con parametros por metodo get Ej. user=dcaceres&firstdate=2025-07-01
  	 * @return retorna un String con el resultado.
  	 * @throws Exception
  	 */
  	public String callRestGetBearerToken(String queryParams, String jwt) throws Exception {
  	    String outputJson = "";
  	    try {
  	        String baseUrl = this.server + this.url;	        

  	        URL urlToCall = new URL(baseUrl + "?" + queryParams);

  	        HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
  	        conn.setRequestMethod("GET");
  	        conn.setRequestProperty("Authorization", "Bearer " + jwt); 
  	        conn.setRequestProperty("Accept", "application/json");

  	        int statusCode = conn.getResponseCode();

  	        InputStream is = (statusCode >= 200 && statusCode <= 299)
  	            ? conn.getInputStream()
  	            : conn.getErrorStream();

  	        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
  	            StringBuilder strBldr = new StringBuilder();
  	            String line;
  	            while ((line = br.readLine()) != null) {
  	                strBldr.append(line);
  	            }
  	            outputJson = strBldr.toString();
  	        }

  	        if (debug) {
  	            System.out.println("***** DEPURACION GET ****");
  	            System.out.println("URL llamada: " + urlToCall);
  	            System.out.println("HTTP status: " + statusCode);
  	            System.out.println("Respuesta: " + outputJson);
  	            System.out.println("*********************");
  	        }

  	        conn.disconnect();

  	        if (statusCode < 200 || statusCode > 299) {
  	            throw new RuntimeException("HTTP error code: " + statusCode + "\nRespuesta: " + outputJson);
  	        }

  	    } catch (Exception e) {
  	        e.printStackTrace();
  	    }

  	    return outputJson;
  	}
  
	
}
