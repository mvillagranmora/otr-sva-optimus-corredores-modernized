package cl.bcs.facturacion.dte;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
* Clase que implementa llamadas al Dte mediante el consumo de WS.
*
* @author	: Natalia Silva :: NSM
* @date		: 07/12/2016
* @version	: 1.0
* 
* @author	: JGarces
* @date		: 15/03/2017
* @version	: 1.1
* Se rescata y loguea el body en caso de error.
*
* @author	: JVillagran
* @date		: 06/03/2018
* @version	: 1.1 
* Se implemta llamda a WS para anulacion de documentos DTE
*/

public class CallerWSDte {
	private static final Logger logger = LogManager.getLogger(CallerWSDte.class);
	//Globales
	private boolean depuracion;
	private String server;
	private String user;
	private String pass;

	//---------------------
	// CONSTRUCTORES
	//---------------------

    /*
     * llamaWSValorizador
     * Constructor de la clase.
     */
    public CallerWSDte(){
    	this.depuracion = false;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear modo depuraci�n.
     */
    public CallerWSDte(boolean depuracion){
    	this.depuracion = depuracion;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion
     */
    public CallerWSDte(String server, String user, String pass){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = false;
    }
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion y modo
     */
    public CallerWSDte(String server, String user, String pass, boolean depuracion){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = depuracion;
    }
	//---------------------
	// getters and setters
	//---------------------
    public boolean isDepuracion() {
		return depuracion;
	}
	public void setDepuracion(boolean depuracion) {
		this.depuracion = depuracion;
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
	

	
	//****************************//
	//** Enviar DTE Comprobante **//
	//****************************//
	  
	public Map<String,String> enviarDocumento(String idComprobante, String datosEnviarDTE, String localizacionSistema,String tipoComprobante, Long segundosPorComprobante) throws Exception{
		logger.debug(":CALLERWSDTE.enviarDocumento:  ****** INICIO  *******");
		Map<String,String> respuesta = new HashMap<String, String>();
		String token = loginDTE(this.server,this.user,this.pass);
		logger.debug(idComprobante + "/" + datosEnviarDTE);
		String urlToCall = "";
		    
		if (tipoComprobante.equalsIgnoreCase("POL")) {
			urlToCall = "/api/polizaEditables";
		} else {
			if (tipoComprobante.equalsIgnoreCase("BOE") || tipoComprobante.equalsIgnoreCase("BEE")) {
				urlToCall = "/api/boletaEditables";
			} else {
				urlToCall = "/api/documentoEditables";
			}
		}
		
	    String resp = enviarDocumentoEditableDTE(this.server, this.user,this.pass,datosEnviarDTE,token, localizacionSistema,urlToCall, 1, segundosPorComprobante);
		//logica adicional
	    respuesta.put(idComprobante, resp);
	    logger.debug(":CALLERWSDTE.enviarDocumento:  ****** FIN  *******");

		return respuesta;
	}

	//MRIQUELME 28-03-2022
	public Map<String,String> enviarMultiplesDocumentos(List<Long> idComprobantes, List<String> datosEnviarDTE, String localizacionSistema, Long segundosPorComprobante) throws Exception
	{
		logger.debug(":CALLERWSDTE.enviarMultiplesDocumentos:  ****** INICIO  ******* envio multiple " + datosEnviarDTE.size() + " comprobantes ");
		
        List<String> boletas= Arrays.asList("39","41");
		Gson gson = new Gson();
		String json = datosEnviarDTE.get(0);
		JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
		String tipoComp = jsonObj.get("tipo").getAsString();

		Map<String,String> respuesta = new HashMap<String, String>();
		String token = loginDTE(this.server,this.user,this.pass);
		String urlToCall = "";	
		urlToCall = boletas.contains(tipoComp) ? "/api/boletaEditables/creaMultiple" : "/api/documentoEditables/creaMultiple";		

		
		String resp = enviarDocumentoEditableDTE(this.server, this.user, this.pass, datosEnviarDTE.toString(),token, localizacionSistema, urlToCall, datosEnviarDTE.size(), segundosPorComprobante);
		
		//logica adicional
		try {			
			JSONArray datosJson = new JSONArray(resp);
			JSONObject datos;
			
			for(int i=0; i<datosJson.length();i++) {			
				datos = datosJson.getJSONObject(i);
				respuesta.put(idComprobantes.get(i).toString(), datos.toString());
			}
		}catch(Exception e) {
			e.printStackTrace(); // Imprimimos pila de errores
			// Si llega a este punto, marcamos toods los comprobantes del grupo con error
			for(int i=0; i<datosEnviarDTE.size();i++) {
				JSONObject jsonObject = new JSONObject(datosEnviarDTE.get(i));
				respuesta.put(jsonObject.getString("claveOrigen"), "{\"estado\":\"ERROR\",\"idOrigen\":\""+jsonObject.getString("claveOrigen")+"\",\"mensaje\":\"Respuesta con error al enviar documentos\"}");
			}
			
		}

		logger.debug(":CALLERWSDTE.enviarMultiplesDocumentos:  ****** FIN  *******");
		return respuesta;
	}
	
	
	public String loginDTE(String server, String username, String password){
		String token = "";
		try {
			String strUrl = server+"/api/authenticate?username="+username+"&password="+password;
			logger.debug(strUrl);
			URL urlToCall = new URL(strUrl);
			HttpURLConnection connLogin = (HttpURLConnection) urlToCall.openConnection();
			connLogin.setConnectTimeout(10000);
			connLogin.setReadTimeout(10000);
			connLogin.setDoOutput(true);
			connLogin.setRequestMethod("POST");
			connLogin.setRequestProperty("Content-Type", "application/json");
			String input = "";
			OutputStream os = connLogin.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			int responseCode = connLogin.getResponseCode();
			BufferedReader br = null;
			if(responseCode != 200){
				br = new BufferedReader(new InputStreamReader((connLogin.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));
			}
			//br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    String jsonString = strBldr.toString();
		    //connLogin.disconnect();
			if (responseCode != 200 ) {
				throw new RuntimeException("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+jsonString);
			}else{
				JSONObject obj = new JSONObject(jsonString);
				token = obj.getString("token");
				logger.debug("token:"+token);
				if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+server+"\nllamada respuesta: "+jsonString+"\n*********************");
			}
		 } catch (JSONException e) {
			e.printStackTrace();	
		 } catch (MalformedURLException e) {
			e.printStackTrace();
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		return token;
	}
	

	private String enviarDocumentoEditableDTE(String server, String username, String password, String jsonBody, String token, String localizacionSistema,String url, Integer cantDocu, Long segundosPorComprobante) throws Exception
	{
		logger.debug(":CALLERWSDTE.enviarDocumentoEditableDTE:  ****** INICIO  *******");
		String respuesta = "";
		try {
			String input = jsonBody;
			Integer maxTimeout = (int) (segundosPorComprobante * cantDocu * 1000); // Obtenemos timeout calculado de: segundos_por_comprobante x cantidad_documentos x 1000 
			URL urlToCall = new URL(server+url);
			logger.debug("urlToCall: " + urlToCall.toString());
			
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setConnectTimeout(maxTimeout);
			conn.setReadTimeout(maxTimeout);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("X-Auth-Token", token);
			//conn.setRequestProperty("Authorization", "none");
			/*posting*/
			OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(input); 
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
			int responseCode = conn.getResponseCode();
			logger.debug("respondeCode: " + responseCode);
			BufferedReader br = null;
			if(responseCode != 201){
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			//br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    logger.debug("Inicio while ");
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    logger.debug("Fin while ");
		    String bodyResponse = strBldr.toString();
		    conn.disconnect();
			if (responseCode != 201 ) {
				logger.debug("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
				respuesta = bodyResponse;
				throw new RuntimeException("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
			}else{
				if(!url.contains("creaMultiple"))
					respuesta = conn.getHeaderField("Location");
				else 
					respuesta = bodyResponse;
				if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+server+"\nllamada body: "+jsonBody + "\nllamada respuesta: "+respuesta+"\n\n");
			}
		 } catch (MalformedURLException e) {
			e.printStackTrace();

		 } catch (Exception e) {
			e.printStackTrace();
		 }
		logger.debug(":CALLERWSDTE.enviarDocumentoEditableDTE:  ****** FIN  *******");
		return respuesta;
	}
	
	
	//***************************//
  	//** Consultar DTE         **//
  	//***************************//	
	public Map<String,String> consultarDocumento(Map<String,String>  datosConsultarDTE) throws Exception{
		Map<String,String> respuesta = new HashMap<String, String>();
						
		String token = loginDTE(this.server,this.user,this.pass);
		for (Map.Entry<String, String> entry : datosConsultarDTE.entrySet()){
			try {
				logger.debug(entry.getKey() + "/" + entry.getValue());
			    String resp = consultarDocumentoEditableDTE(this.server, this.user,this.pass,entry.getValue(),token);
			    
				//logica adicional
			    respuesta.put(entry.getKey(), resp);
			}catch(Exception e) {
				logger.error(e);
				continue;
			}  
		}
		return respuesta;
	}
	
	
	private String consultarDocumentoEditableDTE(String server, String username, String password, String location, String token) throws Exception
	{
		logger.debug(":CALLERWSDTE.consultarDocumentoEditableDTE:  ****** INICIO  *******");
		String respuesta = "";
		try {
			URL urlToCall = new URL(server+location);
			logger.debug("urlToCall: " + urlToCall.toString());
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("X-Auth-Token", token);
			/*get*/
            conn.connect();
			int responseCode = conn.getResponseCode();
			logger.debug("respondeCode: " + responseCode);
			BufferedReader br = null;
			if(responseCode != 200){
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    logger.debug("Inicio while ");
		    while( (line = br.readLine()) != null) { 
		    	strBldr.append(line); 
		    }
		    logger.debug("Fin while ");
		    respuesta = strBldr.toString();
		    logger.debug("respuesta: " + respuesta);
			conn.disconnect();
			if (responseCode != 200 ) {
				logger.debug("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+respuesta);
				throw new RuntimeException("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+respuesta);
			}else{
				if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+server+location+"\nllamada respuesta: "+respuesta+"\n\n");
			}					
		 } catch (MalformedURLException e) {
			e.printStackTrace();
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		logger.debug(":CALLERWSDTE.consultarDocumentoEditableDTE:  ****** FIN  *******");
		return respuesta;
	}
	
	
	//****************************//
	//** Enviar DTE Libro       **//
	//****************************//
	public Map<String,String> enviarWsLibro(Map<String,String> datosLibEnviarDTE) throws Exception{
		Map<String,String> respuesta = new HashMap<String, String>();
		String token = loginDTE(this.server,this.user,this.pass);
		for (Map.Entry<String, String> entry : datosLibEnviarDTE.entrySet())
		  {
			 //log.write(entry.getKey() + "/" + entry.getValue());
			 String resp = enviarLibroDTE(this.server, this.user,this.pass,entry.getValue(),token);
			 //logica adicional
			 respuesta.put(entry.getKey(), resp);

		  }
			return respuesta;
	}
	
	private String enviarLibroDTE(String server, String username, String password, String jsonBody, String token) throws Exception
	{
		String respuesta = "";
		try {
			String input = jsonBody;
			URL urlToCall = new URL(server+"/api/libroCVs/update");
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("PUT");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("X-Auth-Token", token);

			/*posting*/
			OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(input); 
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            
			int responseCode = conn.getResponseCode();
			BufferedReader br = null;
			if(responseCode != 201){
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    String bodyResponse = strBldr.toString();
			conn.disconnect();
			if (responseCode != 201 ) {
				logger.debug("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
				throw new RuntimeException("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
			}else{
				respuesta = conn.getHeaderField("Location");
				if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+server+"\nllamada body: "+jsonBody + "\nllamada respuesta: "+respuesta+"\n\n");
			}
		 } catch (MalformedURLException e) {
			e.printStackTrace();

		 } catch (Exception e) {
			e.printStackTrace();
		 }
		return respuesta;
	}


	
	
	
	// ****************************************/
	// *** INICIO ANULAR DOCUMENTO DTE ********/
	// ****************************************/ 
	
	public Map<String,String> anularDocumento(Map<String,String>  datosAnularDTE) throws Exception{	
		Map<String,String> respuesta = new HashMap<String, String>();
		String token = loginDTE(this.server,this.user,this.pass);
		for (Map.Entry<String, String> entry : datosAnularDTE.entrySet())
		{
			logger.debug(entry.getKey() + "/" + entry.getValue());
		    
		    String urlToCall = "";
		    urlToCall = "/api/polizaEditables/anular/" + entry.getValue();
		    		    
		    String resp = anularDocumentoDTE(this.server, this.user,this.pass,token, urlToCall);
		    
			//logica adicional
		    respuesta.put(entry.getKey(), resp);

		}
		return respuesta;
	}	

	
	

	private String anularDocumentoDTE(String server, String username, String password, String token, String url) throws Exception
	{
		String respuesta = "";
		try {
			///String input = jsonBody;
			URL urlToCall = new URL(server+url);			
			logger.debug(urlToCall);
			
			HttpURLConnection conn = (HttpURLConnection) urlToCall.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("X-Auth-Token", token);
			//conn.setRequestProperty("Authorization", "none");

            conn.connect();
			int responseCode = conn.getResponseCode();
			BufferedReader br = null;
			if(responseCode != 202){
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    String bodyResponse = strBldr.toString();
		    conn.disconnect();
			if (responseCode != 202 ) {
				logger.debug("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
				throw new RuntimeException("Failed : HTTP error code :\n" + responseCode + "\nBody Response: \n"+bodyResponse);
			}else{
				////respuesta = conn.getHeaderField("Location");				
				respuesta = "OK";  // Anulacion OK
				if(this.depuracion) logger.debug("***** DEPURACION ****\nllamada url: "+server+"\nllamada: "+url + "\nllamada respuesta: "+respuesta+"\n\n");
			}
		 } catch (MalformedURLException e) {
			e.printStackTrace();

		 } catch (Exception e) {
			e.printStackTrace();
		 }
		return respuesta;
	}	

	
}
