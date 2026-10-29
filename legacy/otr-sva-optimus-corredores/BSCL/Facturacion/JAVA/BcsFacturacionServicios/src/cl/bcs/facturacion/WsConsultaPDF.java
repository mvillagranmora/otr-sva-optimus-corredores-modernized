package cl.bcs.facturacion;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sql2o.tools.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class WsConsultaPDF implements JavaCall  {
	static final Logger logger								= LogManager.getLogger(WsConsultaPDF.class);
	
	
	public static class EntradaWsConsultaPDF extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Rut;		
		public Long p_Folio;
	}
	

	public static class SalidaWsConsultaPDF extends BcsPojo {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public DocumentoElectronico P_OUT_DOCUMENTOELECTRONICO;
		
		public SalidaWsConsultaPDF() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DocumentoElectronico {
		
		public String rutCliente ;
		public Long folio;
		public Long id;
		public Long tipoDocumento;
		public String NombreArchivo;
		public String pdf;
		public String xml;
		
		public DocumentoElectronico() {
			this.pdf= "";
			this.xml= "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private SalidaWsConsultaPDF logica(Connection conn, EntradaWsConsultaPDF entrada) throws Exception {
		logger.info("INICIO::WsConsultaPDF.logica");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWsConsultaPDF salida = new SalidaWsConsultaPDF();
		String 				urlDte = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "DTE_URL");
		String 				buscaXML = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "ActivaBusquedaXML");
		
        
        
        this.validacionesBasicas(entrada, salida, conn);
        
        if(!salida.P_OUT_MENSAJES.isEmpty()) {
        	salida.P_OUT_ERRORESCONTADOR=1L;
        	return salida;
        }
        
        String token = this.obtenerTokenDte(conn, urlDte, entrada.p_IdEmpresa);
        JsonArray consultaDocumento = this.consultaDocumento(conn, urlDte, entrada.p_Folio, token);
        DocumentoElectronico objDocumento = new DocumentoElectronico();
        
        if(consultaDocumento.size()!=0){
			
			for ( JsonElement documento: consultaDocumento) {
				
				String rutEmisor=documento.getAsJsonObject().get("rutEmisor").getAsString();
				String rutReceptor=documento.getAsJsonObject().get("rutReceptor").getAsString();
				
				if(entrada.p_Rut.equalsIgnoreCase(rutReceptor)|| entrada.p_Rut.equalsIgnoreCase(rutEmisor)){
					objDocumento.id = documento.getAsJsonObject().get("id").getAsLong();
					objDocumento.folio= entrada.p_Folio;
					objDocumento.rutCliente= entrada.p_Rut;
					objDocumento.tipoDocumento = documento.getAsJsonObject().get("tipoDte").getAsLong();
					objDocumento.NombreArchivo = objDocumento.tipoDocumento+"_"+entrada.p_Rut+"_"+entrada.p_Folio;
					objDocumento.pdf=consultarImagenPDF(objDocumento.id , urlDte , token, salida);	
					
					if("S".equals(buscaXML)){
						String xml = consultarXml(objDocumento.id, token, urlDte,salida);
						objDocumento.xml = xml;
					}	
				}
				
			}
			
			salida.P_OUT_DOCUMENTOELECTRONICO=objDocumento;
			if(salida.P_OUT_DOCUMENTOELECTRONICO==null) {
				MensajeSalida mensajeSalida = new MensajeSalida(3L, MensajeSalida.MENSAJE_ERROR, "WSCONSULTA_PDF_NO_HAY_COMPROBANTE","No existen comprobantes");					
				salida.P_OUT_MENSAJES.add(mensajeSalida);
			}
			
		}else{
			MensajeSalida mensajeSalida = new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "WSCONSULTA_PDF_FECHA_FOLIO_NO_EXISTE","Folio no existe");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
		}	
       
        if(salida.P_OUT_MENSAJES.size()>0) {
        	salida.P_OUT_ERRORESCONTADOR=1L;
        }
        
        
        long finServicio = System.currentTimeMillis();
        logger.info("INICIO::WsConsultaPDF.logica",finServicio-inicioServicio);
		return salida;
	}
	
	public String consultarXml(Long id, String token, String urlDte, SalidaWsConsultaPDF salida) {
		logger.debug("INICIO::WsConsultaPDF.consultarXml");
		long inicioMetodo = System.currentTimeMillis();
		
		
		List<String> listaErrores = new ArrayList<String>();		
	    StringBuffer xmlBuffer = new StringBuffer();
	    
	    try {
	    	String URLparms 			= urlDte + "/files/xml/"+id;			
			URL url 					= new URL(URLparms);
			HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
			
			connect.setDoOutput(true);
			connect.setRequestProperty ("X-Auth-Token", token);
			connect.setRequestProperty("responseType", "arraybuffer"); 
			connect.setRequestMethod("GET");
			connect.setConnectTimeout(5000);
			connect.setInstanceFollowRedirects(true); 
			connect.setUseCaches( false );	
			
			InputStream response 	= connect.getInputStream();
			
			if ( connect.getResponseCode() != 200 ) {	
				
				
				MensajeSalida mensajeSalida = new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "WSCONSULTA_CONEXION"," Error de conexion "+connect.getResponseCode());					
				salida.P_OUT_MENSAJES.add(mensajeSalida);	
			}
			
			InputStreamReader isReader = new InputStreamReader(response);		      
	        BufferedReader reader = new BufferedReader(isReader);
	        String str;
	        while((str = reader.readLine())!= null){
	        	xmlBuffer.append(str);
	        }
			
	    	
	    }catch (Exception e) {
	    	MensajeSalida mensajeSalida = new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "WSCONSULTA_PDF_XML","XML no encontrado");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
	    }
		
		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaPDF.consultarXml",finMetodo- inicioMetodo);	
		return xmlBuffer.toString();
	}
	
	private String consultarImagenPDF(Long id , String urlDte , String token , SalidaWsConsultaPDF salida) throws Exception {
		logger.debug("INICIO::WsConsultaPDF.consultarImagenPDF");
		long inicioMetodo = System.currentTimeMillis();
		byte[] imageBytes      = null;
		String imagenString    = "";
		
		
		String URLparms 			= urlDte + "/files/pdf/"+id;			
		URL url 					= new URL(URLparms);
		HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
		
		connect.setDoOutput(true);
		connect.setRequestProperty ("X-Auth-Token", token);
		connect.setRequestProperty("responseType", "arraybuffer"); 
		connect.setRequestMethod("GET");
		connect.setConnectTimeout(5000);
		connect.setInstanceFollowRedirects(true); 
		connect.setUseCaches( false );	
				
		InputStream response 	= connect.getInputStream();

		try {
			imageBytes =IOUtils.toByteArray(response);		  
		}
		catch (IOException e) {			
			logger.error("INICIO::WsConsultaPDF.consultarImagenPDF" , e);					   
		}
		finally {
		  if (response != null) { response.close(); }
		}
		imagenString= new String(Base64.encodeBase64(imageBytes)); 
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaPDF.consultarImagenPDF",finMetodo- inicioMetodo);	
		return imagenString;
	}

	public JsonArray consultaDocumento (Connection conn, String urlDte , Long folio , String token) throws Exception {
		logger.debug("INICIO::WsConsultaPDF.consultaDocumento");
		long inicioMetodo = System.currentTimeMillis();
		
		JsonArray documentosDte 	= new JsonArray();
		JsonArray consultaDte 		= new JsonArray();
		JsonParser parser 			= new JsonParser();		
		String URLparms = urlDte + "/api/documentos?folioMin="+ folio +"%09&folioMax="+ folio +"%09&page=0&per_page=0";
		URL url 					= new URL(URLparms);
		HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
		
		connect.setDoOutput(true);
		connect.setRequestProperty ("X-Auth-Token", token);
		connect.setRequestProperty("Content-Type", "application/octet-stream"); 
		connect.setRequestMethod("GET");
		connect.setConnectTimeout(5000);
		connect.setInstanceFollowRedirects(true); 
		connect.setUseCaches( false );
		
		InputStream response 	= connect.getInputStream();
		if ( connect.getResponseCode() != 200 ) {	
			logger.error("Error respuesta al pedir los documentos:"+ connect.getResponseCode() + ":" + response);	
		}
		
		consultaDte = parser.parse(new InputStreamReader(response, "UTF-8")).getAsJsonArray();		
		documentosDte.addAll(consultaDte);		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaPDF.consultaDocumento",finMetodo- inicioMetodo);		
		return documentosDte;
	}
	
	public void validacionesBasicas(EntradaWsConsultaPDF entrada , SalidaWsConsultaPDF Salida , Connection conn) throws Exception {
		logger.trace("INICIO::WsConsultaPDF.validacionesBasicas");
		long inicioMetodo = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		if(entrada.p_Rut==null || "".equals(entrada.p_Rut)) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CONSULTA_PDF_RUT_OBLIGATORIO","Rut Cliente es obligatorio");
			mensajes.add(mensajeSalida);			
		}
		
		if(entrada.p_Folio==null || "".equals(entrada.p_Folio)) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CONSULTA_PDF_FOLIO_OBLIGATORIO","Folio Documento es obligatorio");
			mensajes.add(mensajeSalida);			
		}
		
		Salida.P_OUT_MENSAJES.addAll(mensajes);
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::WsConsultaPDF.validacionesBasicas :" , finMetodo- inicioMetodo);		
	}
	
	private String obtenerTokenDte(Connection con, String urlDte, Long p_IdEmpresa) throws Exception {		
		logger.debug("INICIO::WsConsultaPDF.obtenerToken");

		Long inicio 				= System.currentTimeMillis();
		String token     	 		= null;
		String usuario    			= ConfiguracionSistema.getParametroConfiguracionString(con, p_IdEmpresa, "FACTURACION", "DTE_USER");
		String contrasena 			= ConfiguracionSistema.getParametroConfiguracionString(con, p_IdEmpresa, "FACTURACION", "DTE_PASS");
		String URLparms 			= urlDte + "/api/authenticate?username="+ usuario +"&password=" + contrasena;    		      		
		URL url 		  			= new URL(URLparms);
		HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
		
		connect.setDoOutput(true);
		connect.setRequestMethod("POST");
		connect.setConnectTimeout(5000);
		connect.setInstanceFollowRedirects(true); 
		connect.setRequestProperty("Content-Type", "application/json"); 
		connect.setRequestProperty("charset", "UTF-8");	
		connect.setUseCaches( false );
		
		InputStream response 		= connect.getInputStream();
		JsonParser parser 			= new JsonParser();
		JsonObject consultaToken 	= parser.parse(new InputStreamReader(response, "UTF-8")).getAsJsonObject();
		token 						= consultaToken.get("token").getAsString();
		
		if ( connect.getResponseCode() != 200 ) {
			
			logger.info("Error respuesta al pedir token:"+ connect.getResponseCode() + ":" + response);
			
		}
		
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaPDF.obtenerToken: ", fin - inicio);	
		return token;
		
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaWsConsultaPDF jin = gson.fromJson(in.toString(), EntradaWsConsultaPDF.class);
		SalidaWsConsultaPDF out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FAC\",\"p_Rut\":\"19988733-5\",\"p_Folio\":null}";

		EntradaWsConsultaPDF Objin = gson.fromJson(json, EntradaWsConsultaPDF.class);
		WsConsultaPDF demo = new WsConsultaPDF();				
		try {
			SalidaWsConsultaPDF ObjOut = demo.logica(con, Objin);			
			System.out.println(ObjOut.toString());
			con.rollback();
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			
    	}
		System.out.println("== END ==");
	}

	
	
	
	

}
