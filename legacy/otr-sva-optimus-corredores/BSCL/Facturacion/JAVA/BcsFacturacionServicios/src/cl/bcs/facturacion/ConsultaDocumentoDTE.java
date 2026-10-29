package cl.bcs.facturacion;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.sql2o.tools.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaDocumentoDTE implements JavaCall {
	
	private final Log log=new Log("FAC.ConsultaDocumentoDTE");
	
	public static class Entrada {
		public long 			p_IdEmpresa;
		public long 			p_IdUsuario;
		public String 			p_Tipo;
		public List<DocumentoDTE>	p_IdDocumentos;
		//public String 			p_FileName;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Entrada() {
			this.p_IdDocumentos = new ArrayList<>();
		}
	}
	
	public static class DocumentoDTE {
		public String IdDocumento;
		public String FileName;
	}
	
	public static class Salida {
		public Long 						P_OUT_ERRORESCONTADOR;
		public List<DocumentoDTE_Salida>	P_FILES;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Salida() {
			this.P_FILES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class DocumentoDTE_Salida {
		public String encodedString;
		public String dteNumber;
		public String resultado;
		public String mensaje;
		//public String			p_file;	//ruta del archivo temporal en el server		
		
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida	out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public Salida logica(Connection con, Entrada in) throws Exception {
		SQL sql=new SQL();
		Salida out=new Salida();
		log.writeAsJson("Solicitud DTE", in);
		String rutaBase = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
		String durl = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "FACTURACION", "DTE_URL");
		String duser = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "FACTURACION", "DTE_USER");
		String dpass = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "FACTURACION", "DTE_PASS");
		//solo para pruebas locales
		if (Utils.isWindowsOS()) rutaBase=System.getProperty("user.home");
	
		//Paso 1 - Pedir token a DTE
		String token = "";
    	try {
    		String URLparms=durl+"/api/authenticate?username="+duser+"&password="+dpass;    		
			byte[] postData  = in.toString().getBytes("UTF-8");
			int postDataLen=  postData.length;        		
			URL url = new URL(URLparms);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();  
			urlc.setDoOutput(true);
			urlc.setRequestMethod("POST");
			urlc.setConnectTimeout(5000);
			urlc.setInstanceFollowRedirects(true); 
			urlc.setRequestProperty("Content-Type", "application/json"); 
			urlc.setRequestProperty("charset", "UTF-8");	
			urlc.setRequestProperty( "Content-Length", Integer.toString(postDataLen));
			urlc.setUseCaches( false );
			try( DataOutputStream wr = new DataOutputStream( urlc.getOutputStream())) {
				wr.write( postData );
			}
			String response="";
			try (BufferedReader win = new BufferedReader(new InputStreamReader(urlc.getInputStream()));) {
				String inputLine;
		        while ((inputLine = win.readLine()) != null) {
		        	response+=inputLine;			           
		        }
			}
			if (urlc.getResponseCode()!=200) {
				log.write("Error respuesta al pedir token:"+urlc.getResponseCode()+":"+response);
				out.P_OUT_ERRORESCONTADOR=1L;
				return out;
			}
			JsonParser parser 	= new JsonParser();
	        JsonObject dobj		= parser.parse(response).getAsJsonObject();	
	        token				= dobj.get("token").getAsString();			
    	}catch(Exception er) {
			log.write("Error excepcion al pedir token",er);
			out.P_OUT_ERRORESCONTADOR=1L;
			return out;
    	}  		
		
		//Paso 2- Devolver ruta del archivo
    	try {
    		
    		for (DocumentoDTE doc : in.p_IdDocumentos) {
    			DocumentoDTE_Salida docsalida = new DocumentoDTE_Salida();
    			docsalida.dteNumber = doc.IdDocumento;
    			try {
    				String URLparms=durl;
            		if (in.p_Tipo.equals("PDF")) URLparms+="/files/pdf/"+doc.IdDocumento;
            		else if (in.p_Tipo.equals("XML")) URLparms+="/files/xml/"+doc.IdDocumento;    		
        			URL url = new URL(URLparms);
        			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();  
        			urlc.setDoOutput(true);
        			urlc.setRequestProperty ("X-Auth-Token",token);
        			urlc.setRequestProperty("Content-Type", "application/octet-stream"); 
        			urlc.setRequestMethod("GET");
        			urlc.setConnectTimeout(5000);
        			urlc.setInstanceFollowRedirects(true); 
        			urlc.setUseCaches( false );
        			InputStream raw = urlc.getInputStream();
        			byte[] data;
        			try {
        				data=IOUtils.toByteArray(raw);
        		        String disposition = urlc.getHeaderField("Content-Disposition");
        	            String contentType = urlc.getContentType();		
        				if (urlc.getResponseCode()!=200) {
        					log.write("Error respuesta al obtener datos:"+urlc.getResponseCode());
        					out.P_OUT_ERRORESCONTADOR=1L;
        					return out;
        				}			
        			}finally {
        				raw.close();
        			}
        			
        			docsalida.encodedString = new String(Base64.encodeBase64(data));
        			docsalida.resultado = MensajeSalida.MENSAJE_OK;
        			docsalida.mensaje = "";
    			}
    			catch (Exception e) {
    				docsalida.encodedString = "";
        			docsalida.resultado = MensajeSalida.MENSAJE_ERROR;
        			docsalida.mensaje = e.getMessage();
        			out.P_OUT_ERRORESCONTADOR++;
    			}
    			out.P_FILES.add(docsalida);
    			
    			
    		}
    		
    		/*String URLparms=durl;
    		if (in.p_Tipo.equals("PDF")) URLparms+="/files/pdf/836464";
    		else if (in.p_Tipo.equals("XML")) URLparms+="/files/xml/836464";    		
			URL url = new URL(URLparms);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();  
			urlc.setDoOutput(true);
			urlc.setRequestProperty ("X-Auth-Token",token);
			urlc.setRequestProperty("Content-Type", "application/octet-stream"); 
			urlc.setRequestMethod("GET");
			urlc.setConnectTimeout(5000);
			urlc.setInstanceFollowRedirects(true); 
			urlc.setUseCaches( false );
			InputStream raw = urlc.getInputStream();
			byte[] data;
			try {
				data=IOUtils.toByteArray(raw);
		        String disposition = urlc.getHeaderField("Content-Disposition");
	            String contentType = urlc.getContentType();		
				if (urlc.getResponseCode()!=200) {
					log.write("Error respuesta al obtener datos:"+urlc.getResponseCode());
					out.P_OUT_ERRORESCONTADOR=1L;
					return out;
				}			
			}finally {
				raw.close();
			}*/
		    
			//Escribe archivo	
		    /*File fdir=new File(rutaBase+"/dte/");
		    if (!fdir.exists()) fdir.mkdir();	
		    String relfile="/dte/dte_"+System.currentTimeMillis()+"."+in.p_Tipo;
		    if (in.p_FileName!=null && in.p_FileName.length()>0) relfile="/dte/"+in.p_FileName;
		    File fFile=new File(rutaBase+relfile);
		    if (!fFile.exists()) fFile.delete();
		    FileOutputStream fout = new FileOutputStream(fFile);
		    try {
		    	fout.write(data);
		    	fout.flush();
		    }finally {
		    	fout.close();
		    }
		    out.p_file=relfile;		   
		    out.p_encoded=new String(Base64.encodeBase64(data));*/
    	}catch(Exception er) {
			log.write("Error excepcion generar archivo",er);
			out.P_OUT_ERRORESCONTADOR=1L;
			return out;
    	}  	    			
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUATConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_Tipo\":\"PDF\",\"p_IdDocumentos\":[{\"IdDocumento\":100062}]}";
				Entrada Objin = gson.fromJson(json, Entrada.class);				
				ConsultaDocumentoDTE demo=new ConsultaDocumentoDTE();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			Salida ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.commit();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		
		System.out.println("== END ==");
	}		
}
