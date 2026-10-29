package cl.bcs.socionegocio.bck;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;


public class FirmaAnexoUrl implements JavaCall {
	//private final Log log = new Log("CLI.GenerarReporteConsultaAvanzadaCliente");
	//private final String SQL_CONSULTA_AVANZADA_CLIENTE_CALL = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaCliente_Call.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	public static class OutAnexoUrl {

		public String P_OUT_ERRORESCONTADOR;

		
	}
	
	public static class Entrada {
		public Long p_IdEmpresa; // BIGINT, --ID Empresa destino
		public Long p_IdUsuario; // BIGINT, --ID del usuario que solicita el servicio
	   	public String p_IdModulo;
	   	public String p_UsaReglasNegocio;
		public String p_Url;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static boolean validatePdfContentInUrl(String url) {
        String pattern = "%PDF-[0-1].\\d"; 
        try {
               if(url.toLowerCase().contains("https")) {
                      /* Start of Fix */
             /*   TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                } };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) { return true; }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                */
               }
               
               URL urlObj = new URL(url);              
               byte[] buffer = new byte[8];
               URLConnection con = urlObj.openConnection();
               if (!con.getContentType().equalsIgnoreCase("application/pdf")) {
            	   System.out.println("no");	
                      return false;
                      
               } else {
                      if (con.getContentLength() > 50000000) return false; //Para evitar archivos > 50Mb
                      InputStream is = urlObj.openStream();
                      is.read(buffer);
                      String line = new String(buffer, "UTF-8");
                      if (line.matches(pattern)) {
                             System.out.println(line);
                             System.out.println("si");	
                             return true;
                      }
                      
                      is.close();
               }
               System.out.println("fin");	
        } catch (MalformedURLException e) { //URL no cumple formato
               return false;
        } catch (IOException e) {
               e.printStackTrace();
        }
        return false;
  }
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		OutAnexoUrl out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static OutAnexoUrl logica(Connection con, Entrada in) throws Exception {

				boolean validacion = validatePdfContentInUrl(in.p_Url);
				System.out.println("fin"+validacion);
				OutAnexoUrl respuesta = new OutAnexoUrl();			
				if (validacion){
					respuesta.P_OUT_ERRORESCONTADOR= "0";
				}else if (!validacion){
					respuesta.P_OUT_ERRORESCONTADOR= "1";
				}
				System.out.println("-->"+respuesta.P_OUT_ERRORESCONTADOR);
		return respuesta;		
	}
	
	
	

	
	/**********************
	 * Main 
	 **********************/

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			//Gson gson = Utils.getGsonBuilder();
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String jsonCL = "{\"IdEmpresa\":1,\"IdUsuario\":6,\"p_IdModulo\":\"CLI\",\"p_UsaReglasNegocio\":\"N\",\"p_Url\":\"HTTPS://SI.UA.ES/VA/DOCUMENTOS/DOCUMENTACION/PDF-S/MOZILLA12-PDF.PDF\"}";
			Entrada Objin = gson.fromJson(jsonCL, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				OutAnexoUrl ObjOut = FirmaAnexoUrl.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}