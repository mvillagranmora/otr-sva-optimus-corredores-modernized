package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.ConstantesCustodia;
import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FirmarContratoBlockchain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION		  				= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD	  				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN  				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	private final String SQL_SELECT_DIRECCION	          				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_DireccionFisicaParYActiva.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA 				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_DireccionElectronicaPreYActiva.sql");
	private final String SQL_SELECT_TELEFONO	          				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_TelefonoActivo.sql");
	private final String SQL_SELECT_ARBITRADOR	          				= loadResourceToString("cl/bcs/socionegocio/bck/sql/FirmarContratoBlockchain_SelectArbitrador.sql");

	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class FirmarContratoBlockchain_Entrada
	{
		public Long		p_IdEmpresa			= 1L;
		public String	p_Fecha;
		public String	p_Nombre;
		public String	p_IdCliente;
		public String	p_Nuevo;
		public String	p_Pdf;
		public String	p_IdTipoDocu;
		public String	p_ValidaURLPDF		= "N"; //indica si valida el valor del atributo p_Pdf (S/N) por defecto es N 
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class FirmarContratoBlockchain_Salida
	{
		public String				p_Out_Status;
		public String				p_Out_Mensaje;
		public Long					p_Out_ContadorError;
		public String				p_Out_MensajeError;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long 				P_OUT_ERRORESCONTADOR;
		FirmarContratoBlockchain_Salida() {
			this.P_OUT_MENSAJES  = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectConfiguracion
	{
        public String	Valor;
        public String	Descripcion;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectDocumentoIdentidad
	{
        public String	DocumentoIdentidad;
        public String	Nombres;
        public Long		IdPersona;
        public Long		IdCliente;
        public String	NombreCompleto;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectDatosVarios
	{
        public String	NombreLargo;
        public String	Numero;
        public String	TipoDocumentoNormativa;
        public String   Direccion; //ELECTRONICA
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectTipoDocumento
	{
        public Long		IdClase;
        public String	TipoDocumentoNormativa;
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Arbitrador
	{
		public String IdTipoRelacion;
        public String NombreCompleto;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public FirmarContratoBlockchain_Salida logica(Connection conn, FirmarContratoBlockchain_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		FirmarContratoBlockchain_Salida salida = new FirmarContratoBlockchain_Salida();
		String EsTimeOutError = "S"; //esta parametro lo lee de la configuracion del sistema
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		boolean DesdeRutinaLeerRespuesta = false;
		HttpURLConnection connBC = null;
		List<Arbitrador> lstArbitradores = new ArrayList<>(); //guarda los arbitradores configurados para blockchain
		
		try
		{

			//busca valor parametro para considerar error o no el 504 (TimeOut) que arroja el servicio BlockChain
			//ya que segun se comprueba arroja error 504 pero la operacion la lleva a cabo de todas maneras
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_TIMEOUT_ESERROR_BLOCKCHAIN);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);

			//tiene al parametro configurado
			if(SelectConfiguracion.size()>0) {
				EsTimeOutError = SelectConfiguracion.get(0).Valor; //espera un valor "S" para considerar TimeOut como error o distinto de S para no considerarlo como error
			}
			
			//lee la configuracion de cual es la URL de BlockChain para Firmar
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_FIRMACNT);
			SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
			
			if(!SelectConfiguracion.isEmpty())
			{
				URL url = new URL(SelectConfiguracion.get(0).Valor);

				connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");

				LoginBlockchain loginBlockchain = new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain = new LoginBlockchain.LoginBlockchain_Entrada();
				
				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;
				
				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);
				
				if(salidaLoginBlockchain.p_Out_ContadorError > 0)
				{
					salida.p_Out_Status = "";
					salida.p_Out_Mensaje = "";
					salida.p_Out_ContadorError = salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError = salidaLoginBlockchain.p_Out_MensajeError;
					return salida;
				}
				
				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
				
				HashMap<String,Object> parametroEntradaSelect = new HashMap<String,Object>();
				parametroEntradaSelect.put("p_IdCliente", in.p_IdCliente);
				List<SelectDocumentoIdentidad> SelectDocumentoIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaSelect, SelectDocumentoIdentidad.class);
				
				if(!SelectDocumentoIdentidad.isEmpty())
				{
					//Consultar direccion y telefono
					HashMap<String,Object> parametroEntradaSelectDir = new HashMap<String,Object>();
					parametroEntradaSelectDir.put("p_IdPersona", SelectDocumentoIdentidad.get(0).IdPersona); 
					List<SelectDatosVarios> SelectDatosVariosDir = sql.query(conn, SQL_SELECT_DIRECCION, parametroEntradaSelectDir, SelectDatosVarios.class);							
					List<SelectDatosVarios> SelectDatosVariosTel = sql.query(conn, SQL_SELECT_TELEFONO, parametroEntradaSelectDir, SelectDatosVarios.class);
					List<SelectDatosVarios> SelectDatosVarioseDirElectron = sql.query(conn, SQL_SELECT_DIRECCION_ELECTRONICA, parametroEntradaSelectDir, SelectDatosVarios.class);
					
					HashMap<String,Object> parametro = new HashMap<String,Object>();
					parametro.put("p_IdTipoDocumento", in.p_IdTipoDocu);
					List<SelectTipoDocumento> SelectTipoDocumento = sql.query(conn, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametro, SelectTipoDocumento.class);
					
					if(SelectTipoDocumento.get(0).TipoDocumentoNormativa != null)
					{
					
						//va a buscar los arbitradores configurados
						lstArbitradores = ValidaArbitadores(conn, in.p_IdEmpresa, SelectDocumentoIdentidad.get(0).IdPersona, salida.P_OUT_MENSAJES);
						
						//existen al menos dos elementos de arbitradores definidos
						if(lstArbitradores.size() < 2) {
							salida.p_Out_Status = "";
							salida.p_Out_Mensaje = "";
							salida.p_Out_ContadorError = 1L;							
							return salida;
						}
							
							//si viene la URL de PDF lo valida y segun resultado sigue con el proceso
							//if ("".equals(in.p_Pdf) || (!"".equals(in.p_Pdf) && validatePdfContentInUrl(in.p_Pdf) )) {
							//se indica por parametro que debe validar el valor de la URL del PDF y esta resulta con error
							//o se indico que no se deba validar la URL
							if ("N".equals(in.p_ValidaURLPDF) || ("S".equals(in.p_ValidaURLPDF) && validatePdfContentInUrl(in.p_Pdf)) ) {
								
								ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
								ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
								entradaConsultaDataCorredora.p_IdEmpresa = in.p_IdEmpresa;
								ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
								
								String vdireccion			= null;
								String vtelefono			= null;
								String vemail				= null;
								String vFecha				= fechaSistema;  
								String vNombreCorredora		= salidaConsultaDataCorredora.p_Out_BrokerageName;
								String vRutCorredora		= salidaConsultaDataCorredora.p_Out_BrokerageRUT;
								String vDireccionCorredora	= salidaConsultaDataCorredora.p_Out_BrokerageAddress;
								String vTelefonoCorredora	= salidaConsultaDataCorredora.p_Out_BrokeragePhone;
								String vArbitrador1			= lstArbitradores.get(0).NombreCompleto;
								String vArbitrador2			= lstArbitradores.get(1).NombreCompleto;
									
								if(!SelectDatosVariosDir.isEmpty()){
									vdireccion = SelectDatosVariosDir.get(0).NombreLargo;
								}
								if(!SelectDatosVariosTel.isEmpty()){
									vtelefono = SelectDatosVariosTel.get(0).Numero;
								}	
								
								if(!SelectDatosVarioseDirElectron.isEmpty()){
									vemail = SelectDatosVarioseDirElectron.get(0).Direccion;
								}
								
								//SE DEBE HACER LLAMADA COMO STIRNG DEBIDO A LA ESTRUCTURA DE LA ENTRADA BLOCKCHAIN
								String input = ""
										+"{\"args\":[\"{"
										+"\\\"DateofMSA\\\": \\\""+ vFecha +"\\\","
										+"\\\"MSAType\\\": \\\""+ SelectTipoDocumento.get(0).TipoDocumentoNormativa +"\\\","
										+"\\\"ClientName\\\": \\\""+ SelectDocumentoIdentidad.get(0).NombreCompleto +"\\\","
										+"\\\"ClientRUT\\\": \\\""+ SelectDocumentoIdentidad.get(0).DocumentoIdentidad.trim() +"\\\","
										+"\\\"ClientAddress\\\": \\\""+ vdireccion +"\\\","
										+"\\\"ClientPhone\\\": \\\""+ vtelefono +"\\\","
										+"\\\"BrokerageName\\\": \\\""+ vNombreCorredora +"\\\","
										+"\\\"BrokerageRUT\\\": \\\""+ vRutCorredora +"\\\","
										+"\\\"BrokerageAddress\\\": \\\""+ vDireccionCorredora +"\\\","
										+"\\\"BrokeragePhone\\\": \\\""+ vTelefonoCorredora +"\\\","
										+"\\\"Arbitrator1\\\": \\\""+ vArbitrador1 +"\\\","
										+"\\\"Arbitrator2\\\": \\\""+ vArbitrador2 +"\\\",";
								
								if (in.p_Pdf != null && !in.p_Pdf.trim().isEmpty() ){
									input+="\\\"DocumentLocation\\\": \\\""+ in.p_Pdf +"\\\",";
								}
											
								input+="\\\"Email\\\": \\\""+ vemail +"\\\"}\"]"
									+",\"create\": "+ in.p_Nuevo + ""
									+ "}";
								OutputStream outputStream = connBC.getOutputStream();
								outputStream.write(input.getBytes("UTF-8"));
								outputStream.flush();
								if (connBC.getResponseCode() != 200 ){
									if (connBC.getResponseCode() == 401){
										salida.p_Out_Status = "";
										salida.p_Out_Mensaje = "";
										salida.p_Out_ContadorError = 1L;
										salida.p_Out_MensajeError = "NOTOKEN";
										GeneraElementosError(conn, in.p_IdEmpresa, "UI_BLOCKCHAIN_NOTOKEN", salida.P_OUT_MENSAJES, false );
										return salida;
									} else if(connBC.getResponseCode() == 504 && EsTimeOutError.equals("S") ) { //es error TimeOut y se esta considerado este valor como error segun configuracion 
										salida.p_Out_Status = "";
										salida.p_Out_Mensaje = "";
										salida.p_Out_ContadorError = 1L;
										salida.p_Out_MensajeError = "TIMEOUT";
										GeneraElementosError(conn, in.p_IdEmpresa, "UI_BLOCKCHAIN_TIMEOUT", salida.P_OUT_MENSAJES, false );
										return salida;
										
									} else if(connBC.getResponseCode() == 500 && SelectTipoDocumento.get(0).TipoDocumentoNormativa.equals("42") && EsTimeOutError.equals("N") ) { //Bypass dado que en ambiente de pruebas siempre se cae por timeout
										salida.p_Out_Status = "";
										salida.p_Out_Mensaje = "";
										salida.p_Out_ContadorError = 0L;
										salida.p_Out_MensajeError = "";
										GeneraElementosError(conn, in.p_IdEmpresa, "UI_ENVIO_CONTRATO_EXITOSO", salida.P_OUT_MENSAJES, true );
										return salida;
									} else {
										salida.p_Out_Status = "";
										salida.p_Out_Mensaje = "";
										salida.p_Out_ContadorError = 1L;
										salida.p_Out_MensajeError = "ERROR";
										GeneraElementosError(conn, in.p_IdEmpresa, "UI_BLOCKCHAIN_ERROR", salida.P_OUT_MENSAJES, false );
										return salida;
									}
								}
								
								DesdeRutinaLeerRespuesta = true;
								BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));
								
								String output;
								
								while ((output = br.readLine()) != null){
									JSONObject obj = new JSONObject(output);
									
									if(obj.getString("status").equals("SUCCESS"))
									{
										try
										{
											salida.p_Out_Status = obj.getString("status");
											salida.p_Out_Mensaje = obj.getString("message");
											salida.p_Out_ContadorError = 0L;
											salida.p_Out_MensajeError = "";
											GeneraElementosError(conn, in.p_IdEmpresa, "UI_ENVIO_CONTRATO_EXITOSO", salida.P_OUT_MENSAJES, true );
											return salida;
										}
										catch(Exception ex)
										{
											salida.p_Out_Status = "";
											salida.p_Out_Mensaje = "";
											salida.p_Out_ContadorError = 1L;
											salida.p_Out_MensajeError = "NOFOUND";
											GeneraElementosError(conn, in.p_IdEmpresa, "UI_NOFOUND", salida.P_OUT_MENSAJES, false );
											return salida;
										}
									}
									else
									{
										if(obj.getString("status").equals("ERROR"))
										{
											try
											{
												String message = obj.getString("message");
												
												ConstantesCustodia constantesCustodia 		= new ConstantesCustodia();
												HashMap<String, String> valoresDiccionario 	= new HashMap<String, String>();
												
												String vCodigo = constantesCustodia.codigoErroresBlockChain.get(message.replace(".", "").replace(SelectDocumentoIdentidad.get(0).DocumentoIdentidad.trim(), ""));
												String mensaje = Diccionario.getMensaje(conn, 1L, vCodigo, valoresDiccionario);
												
												salida.p_Out_Status = obj.getString("status");
												salida.p_Out_Mensaje = message;
												salida.p_Out_ContadorError = 1L;
												salida.p_Out_MensajeError = "";
												GeneraElementosError(conn, in.p_IdEmpresa, vCodigo, salida.P_OUT_MENSAJES, false );
												return salida;
											}
											catch(Exception ex)
											{
												salida.p_Out_Status = "";
												salida.p_Out_Mensaje = "";
												salida.p_Out_ContadorError = 1L;
												salida.p_Out_MensajeError = "ERROR";
												GeneraElementosError(conn, in.p_IdEmpresa, "UI_ERROR_BLOCKCHAIN_INDEFINIDO", salida.P_OUT_MENSAJES, false );
												return salida;
											}
										}
									}
								}
								
								connBC.disconnect();
							}
							else 
							{ //URL de PDF no cumple con patron y/o no existe PDF
								
								salida.p_Out_Status = "";
								salida.p_Out_Mensaje = "";
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "URL_CONTRATO_NO_CUMPLE_CON_FORMATO";
								GeneraElementosError(conn, in.p_IdEmpresa, "URL_CONTRATO_NO_CUMPLE_CON_FORMATO", salida.P_OUT_MENSAJES, false );
								return salida;
							}
					}
					else
					{
						salida.p_Out_Status = "";
						salida.p_Out_Mensaje = "";
						salida.p_Out_ContadorError = 1L;
						salida.p_Out_MensajeError = "CONSULTA_TIPO_DOCUMENTO_FALLIDO";
						GeneraElementosError(conn, in.p_IdEmpresa, "UI_CONSULTA_TIPO_DOCUMENTO_FALLIDO", salida.P_OUT_MENSAJES, false );
						return salida;
					}
				}
				else
				{
					salida.p_Out_Status = "";
					salida.p_Out_Mensaje = "";
					salida.p_Out_ContadorError = 1L;
					salida.p_Out_MensajeError = "CONSULTA_DOCUMENTO_INDENTIDAD_FALLIDO";
					GeneraElementosError(conn, in.p_IdEmpresa, "UI_CONSULTA_DOCUMENTO_IDENTIDAD_FALLIDO", salida.P_OUT_MENSAJES, false );
					return salida;
				}
			}
			else
			{
				salida.p_Out_Status = "";
				salida.p_Out_Mensaje = "";
				salida.p_Out_ContadorError = 1L;
				salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CONFIGURACION";
				GeneraElementosError(conn, in.p_IdEmpresa, "UI_SERVICIO_NO_POSEE_CONFIGURACION", salida.P_OUT_MENSAJES, false );
				return salida;
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{		
			if (DesdeRutinaLeerRespuesta && EsTimeOutError.equals("N") && e.getMessage().contains("504") ) {
				salida.p_Out_Status = "";
				salida.p_Out_Mensaje = "";
				salida.p_Out_ContadorError = 0L;
				salida.p_Out_MensajeError = "";
				GeneraElementosError(conn, in.p_IdEmpresa, "UI_ENVIO_CONTRATO_EXITOSO", salida.P_OUT_MENSAJES, true );
				if(connBC != null) {
					connBC.disconnect();
				}
				return salida;
				//no se considera el time out como error
			}
			e.printStackTrace();
			System.out.println("imprime error");
		}
		
		salida.p_Out_Status = "";
		salida.p_Out_Mensaje = "";
		salida.p_Out_ContadorError = 1L;
		salida.p_Out_MensajeError = "ERROR";
		GeneraElementosError(conn, in.p_IdEmpresa, "UI_PERDIDA_DE_COMUNICACION", salida.P_OUT_MENSAJES, false );
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		FirmarContratoBlockchain_Entrada in = gson.fromJson(jin.toString(), FirmarContratoBlockchain_Entrada.class);
		FirmarContratoBlockchain_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdTipoDocu\":\"CONTRATOVENTACORTAANEXO42\",\"p_IdCliente\":1418718,\"p_Pdf\":\"\",\"p_ValidaURLPDF\":\"N\",\"p_Nuevo\":\"true\"}";   
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdTipoDocu\":\"CONTRATOVENTACORTAANEXO42\",\"p_IdCliente\":249198,\"p_Pdf\":\"http://www.sii.cl/factura_electronica/factura_mercado/compra_venta.pdf\",\"p_ValidaURLPDF\":\"S\",\"p_Nuevo\":\"true\"}";
			
			FirmarContratoBlockchain_Entrada Objin = gson.fromJson(jsonCL, FirmarContratoBlockchain_Entrada.class);

			FirmarContratoBlockchain demo = new FirmarContratoBlockchain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				FirmarContratoBlockchain_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	public static boolean validatePdfContentInUrl(String url) {
        String pattern = "%PDF-[0-1].\\d"; 
        try {
        	   if(url==null ) {
        		   return false;
        	   }
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
	
	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES, boolean NoEsError ) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		String strTipoMsg = NoEsError ? MensajeSalida.MENSAJE_INFORMACION : MensajeSalida.MENSAJE_ERROR;
		P_OUT_MENSAJES.add(new MensajeSalida(0L,strTipoMsg,codMsg,msg));
		
	}
	
	//JALA al 22-04-2019
	//va a base de datos a buscar los arbitradores que participan en la firma del contrato BlockChain
	private List<Arbitrador> ValidaArbitadores(Connection con, Long IdEmpresa, Long IdPersona, List<MensajeSalida> P_OUT_MENSAJES ) throws Exception {
		List<Arbitrador> lstArbitradores = new ArrayList<>();
		SQL sql = new SQL();
		
		
		List<String> tipoArbitrador = new ArrayList<>(); 
		tipoArbitrador.add(cl.bcs.socionegocio.pojo.ConstantesBCK.ID_TIPO_RELACION_ARBITRADOR1);
		tipoArbitrador.add(cl.bcs.socionegocio.pojo.ConstantesBCK.ID_TIPO_RELACION_ARBITRADOR2);
		
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("p_IdEmpresa", IdEmpresa);
		parametroEntrada.put("p_IdPersona", IdPersona);
		parametroEntrada.put("p_IdTipoRelacion", tipoArbitrador);
		lstArbitradores = sql.query(con, SQL_SELECT_ARBITRADOR, parametroEntrada, Arbitrador.class);
		
		if(lstArbitradores.size() > 0) {
			if (lstArbitradores.size() < 2) {
				for (Arbitrador arb : lstArbitradores) {
					if (arb.IdTipoRelacion.equals(cl.bcs.socionegocio.pojo.ConstantesBCK.ID_TIPO_RELACION_ARBITRADOR1) ) {
						GeneraElementosError(con, IdEmpresa, "UI_ARBITRADOR2_NO_CONFIGURACION", P_OUT_MENSAJES, false );
					}else {
						GeneraElementosError(con, IdEmpresa, "UI_ARBITRADOR1_NO_CONFIGURACION", P_OUT_MENSAJES, false );
					}
				}
			}
		}
		else {
			GeneraElementosError(con, IdEmpresa, "UI_ARBITRADOR_NO_CONFIGURACION", P_OUT_MENSAJES, false);
		}
		
		return lstArbitradores;
	}
}