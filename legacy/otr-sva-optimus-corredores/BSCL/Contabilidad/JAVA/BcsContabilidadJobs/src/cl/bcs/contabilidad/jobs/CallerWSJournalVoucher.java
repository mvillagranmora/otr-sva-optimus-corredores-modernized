package cl.bcs.contabilidad.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;

import cl.bcs.contabilidad.pojo.BusinessPartner;
import cl.bcs.contabilidad.pojo.JournalVoucher;
import cl.bcs.contabilidad.pojo.LoginSAP;

public class CallerWSJournalVoucher {
	
	private static final int MAXIMO_INTENTOS = 10;


	public static List<String> DoLogin(String server, LoginSAP jsonLoginSAP) throws Exception{
		List<String> cookiesHeader = new ArrayList<>();
		Gson gson = new Gson();
		String jsonLogin = gson.toJson(jsonLoginSAP);	
		
		trust();
		
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
			    new javax.net.ssl.HostnameVerifier(){
			 
			        public boolean verify(String hostname,
			                javax.net.ssl.SSLSession sslSession) {
			                return true;
			        }
			    }); 
		
		String strUrl = server+"/b1s/v1/Login";
		URL urlToCall = new URL(strUrl);
		
		boolean ok = false;
		int intento = 0;
		do {
			
			HttpURLConnection connLogin = (HttpURLConnection) urlToCall.openConnection();	
			connLogin.setDoInput(true);
			connLogin.setDoOutput(true);
			connLogin.setRequestMethod("POST");
			connLogin.setRequestProperty("Content-Type", "application/json");

			try(OutputStream os = connLogin.getOutputStream();){
				try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));){
					System.out.println("LOGIN: " + strUrl + " JSON: " + jsonLogin.toString());
					writer.write(jsonLogin);
					writer.flush();
				}
			}
			
			if (connLogin.getResponseCode() != 200) {
				System.out.println("Failed : HTTP error code : " + connLogin.getResponseCode());
				System.out.println("Reintentar login...");
				intento++;
				//esperar dos segundos para que el SL se recupere...
				Thread.sleep(2000);
			}else {
				StringBuilder strBldr = new StringBuilder();
				try(BufferedReader br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));){

					String line;
				    while( (line = br.readLine()) != null) 
				    { 
				    	strBldr.append(line); 
				    }
				}
				connLogin.disconnect();
				
				String jsonString = strBldr.toString();
			    System.out.println("***** DEPURACION ****\nllamada url: "+server+"\nllamada respuesta: "+jsonString+"\n*********************");

				String COOKIES_HEADER = "Set-Cookie";
				Map<String, List<String>> headerFields = connLogin.getHeaderFields();
				cookiesHeader = headerFields.get(COOKIES_HEADER);

				if (cookiesHeader != null) {
				    for (String cookie : cookiesHeader) {
				    	System.out.println("Cookie Obtenida del Login: "+cookie);
				    }               
				}
				ok=true;
			}
			
			if (intento == MAXIMO_INTENTOS) {
				Respuesta respuesta = new Respuesta(connLogin.getResponseCode(), "Error de conexión");
				throw new RuntimeException(respuesta.toString());
			}
			
		}while (!ok && intento < MAXIMO_INTENTOS);
		
		return cookiesHeader;
	}
	
	
	public static Respuesta ExisteClienteSAP(String server, String Rut,List<String> cookiesLogin) throws Exception{
		
		String strUrl = server+"/b1s/v1/BusinessPartners('"+Rut+"')";
		URL urlToCall = new URL(strUrl);
		
		Respuesta res = null;
		boolean ok = false;
		int intento = 0;
		HttpURLConnection connLogin = null;
		BufferedReader br = null;
		
		do {
			res = new Respuesta();
			connLogin = (HttpURLConnection) urlToCall.openConnection();
			connLogin.setDoInput(true);
			connLogin.setDoOutput(true);
			connLogin.setRequestMethod("GET");
			for (String cookie : cookiesLogin) {
		    	connLogin.setRequestProperty("Cookie", cookie.split(";")[0]);
		    }
			
			res.Codigo = connLogin.getResponseCode();
	        if(res.Codigo != 200){
				br = new BufferedReader(new InputStreamReader((connLogin.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));
			}
	        StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    res.Mensaje = strBldr.toString();
		    br.close();
		    connLogin.disconnect();
		    
		    //error de proxy
		    if (res.Codigo !=200 && res.Codigo != 400 && res.Codigo != 403 && res.Codigo!=404) {
				System.out.println("Error de solicitud de informacion de socio, reintentando: " + intento);
				intento++;
				continue;
			}
			else {
				ok = true;
			}
			
		}while (!ok && intento < MAXIMO_INTENTOS);
		
		if (intento == MAXIMO_INTENTOS) {
			Respuesta respuesta = new Respuesta(connLogin.getResponseCode(), "Error al solicitar informacion de socio, maximo de intentos superado");
			throw new Exception(respuesta.toString());
		}
		
		return res;
	}
	
	static Respuesta EnviarClienteSAP(String server, BusinessPartner partner,List<String> cookiesLogin) throws Exception {
		Gson gson = new Gson();
		String jsonVoucher = gson.toJson(partner);
			
		String strUrl = server+"/b1s/v1/BusinessPartners";
		System.out.println(strUrl);
		URL urlToCall = new URL(strUrl);

		Respuesta res = null;
		boolean ok = false;
		int intento = 0;
		HttpURLConnection connLogin = null;
		BufferedReader br = null;
		
		do {
			res = new Respuesta();
			connLogin = (HttpURLConnection) urlToCall.openConnection();
			connLogin.setDoInput(true);
			connLogin.setDoOutput(true);
			connLogin.setRequestMethod("POST");
			for (String cookie : cookiesLogin) {
		    	connLogin.setRequestProperty("Cookie", cookie.split(";")[0]);
		    }
			OutputStream os = connLogin.getOutputStream();
			System.out.println("Creacion socio de negocio " + jsonVoucher.toString());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(jsonVoucher);
			writer.flush();
			writer.close();
	        os.close();
				        
	        res.Codigo = connLogin.getResponseCode();//devuelve codigo 400, con nuevos parametros
	        
	        if(res.Codigo != 200 && res.Codigo != 201){
				br = new BufferedReader(new InputStreamReader((connLogin.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));
			}
	        StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    res.Mensaje = strBldr.toString();
		    br.close();
		    connLogin.disconnect();
		    
		    //error de proxy
			if (res.Codigo !=200 && res.Codigo != 400 && res.Codigo != 201 && res.Codigo != 403) {
				System.out.println("Error al crear socio, reintentando: " + intento);
				intento++;
				continue;
			}
			else {
				ok = true;
			}
			
		}
		while (!ok && intento < MAXIMO_INTENTOS);
		
		if (intento == MAXIMO_INTENTOS) {
			Respuesta respuesta = new Respuesta(connLogin.getResponseCode(), "Error al crear socio, maximo de intentos superado");
			throw new Exception(respuesta.toString());
		}
		
		return res ;
	}
	
	static Respuesta EnviarAsientoSAP(String server, JournalVoucher jsonVoucherSAP,List<String> cookiesLogin) throws Exception{
		Gson gson = new Gson();
		
		String jsonVoucher = gson.toJson(jsonVoucherSAP);
		String strUrl = server+"/b1s/v1/JournalVouchersService_Add";
		URL urlToCall = new URL(strUrl);
		
		Respuesta res = null;
		boolean ok = false;
		int intento = 0;
		HttpURLConnection connLogin = null;
		BufferedReader br = null;
		
		do {
			res = new Respuesta();
			connLogin = (HttpURLConnection) urlToCall.openConnection();
			connLogin.setDoInput(true);
			connLogin.setDoOutput(true);
			connLogin.setRequestMethod("POST");
			for (String cookie : cookiesLogin) {
		    	connLogin.setRequestProperty("Cookie", cookie.split(";")[0]);
		    }
			OutputStream os = connLogin.getOutputStream();
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(jsonVoucher);
			writer.flush();
			writer.close();
	        os.close();
	        
	        res.Codigo = connLogin.getResponseCode();
	        if(res.Codigo != 204){
				br = new BufferedReader(new InputStreamReader((connLogin.getErrorStream())));
			}else{
				br = new BufferedReader(new InputStreamReader((connLogin.getInputStream())));
			}
			StringBuilder strBldr = new StringBuilder();
		    String line;
		    while( (line = br.readLine()) != null) { strBldr.append(line); }
		    res.Mensaje = strBldr.toString();
		    br.close();
		    connLogin.disconnect();
		    
		    //error de proxy
		    if (res.Codigo !=200 && res.Codigo != 400 && res.Codigo!=204) {
				System.out.println("Error al enviar asiento, reintentando: " + intento);
				intento++;
				continue;
			}
			else {
				ok = true;
			}
		}
		while (!ok && intento < MAXIMO_INTENTOS);
		
		if (intento == MAXIMO_INTENTOS) {
			Respuesta respuesta = new Respuesta(connLogin.getResponseCode(), "Error al enviar asiento, maximo de intentos superado");
			throw new Exception(respuesta.toString());
		}
		
		return res;
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
	
	
}
