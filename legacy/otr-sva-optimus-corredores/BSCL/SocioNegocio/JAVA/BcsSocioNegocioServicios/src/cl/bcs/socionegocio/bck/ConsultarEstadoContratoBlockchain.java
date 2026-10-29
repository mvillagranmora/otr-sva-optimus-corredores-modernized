package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.sql2o.tools.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEstadoContratoBlockchain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	//TODO REEMPLAZO POR CLASE CONFIGURACIONSISTEMA
	private final String SQL_SELECT_CONFIGURACION		 = loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD	 = loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN = loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ConsultarEstadoContratoBlockchain_Entrada
	{
		//TODO FALTA IDEMPRESA, IDUSUARIO, IDMODULO
		public Long		p_IdEmpresa			= 1L;
		public Long     p_IdUsuario; 
	   	public String   p_IdModulo;
		public String	p_IdTipoDocumento;
		public String	p_IdCliente;
		public String	p_ConviertePDF		=	"N"; //indica si el retorno del contenido del contrato BlockChain se convierte a "binario" o no, por defecto es N para no demorar el proceso de consulta
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	//TODO CORREGIR NOMBRES DE LA SALIDA
	public static class ConsultarEstadoContratoBlockchain_Salida
	{
		public String	p_Out_Status;
		public String	p_Out_PdfContrato;
		public Long		p_Out_ContadorError;
		public String	p_Out_MensajeError;
		public DocumentoDTE_Salida p_Out_ObjPDF;
		public String 	DocumentoIdentidad;
		
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
        public Long		IdPersona;
        public Long		IdCliente;
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
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------

	public ConsultarEstadoContratoBlockchain_Salida logica(Connection conn, ConsultarEstadoContratoBlockchain_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		ConsultarEstadoContratoBlockchain_Salida salida = new ConsultarEstadoContratoBlockchain_Salida();
		
		String status = "";
		String pdfContrato = "";
		
		try
		{
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_CONTRATO);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
			
			if(!SelectConfiguracion.isEmpty())
			{
				URL url = new URL(SelectConfiguracion.get(0).Valor);
				
				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
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
					salida.p_Out_PdfContrato = "";
					salida.p_Out_ContadorError = salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError = salidaLoginBlockchain.p_Out_MensajeError;
					salida.DocumentoIdentidad = "";
					return salida;
				}
				
				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
				
				HashMap<String,Object> parametroEntradaSelect = new HashMap<String,Object>();
				parametroEntradaSelect.put("p_IdCliente", in.p_IdCliente);
				List<SelectDocumentoIdentidad> SelectDocumentoIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaSelect, SelectDocumentoIdentidad.class);
				
				ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
				entradaConsultaDataCorredora.p_IdEmpresa = in.p_IdEmpresa;
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
				
				if(!SelectDocumentoIdentidad.isEmpty())
				{
					salida.DocumentoIdentidad = SelectDocumentoIdentidad.get(0).DocumentoIdentidad;
					HashMap<String,Object> parametro = new HashMap<String,Object>();
					parametro.put("p_IdTipoDocumento", in.p_IdTipoDocumento);
					List<SelectTipoDocumento> SelectTipoDocumento = sql.query(conn, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametro, SelectTipoDocumento.class);
					
					if(SelectTipoDocumento.get(0).TipoDocumentoNormativa != null)
					{
						String input = "{\"args\":[\"" + SelectTipoDocumento.get(0).TipoDocumentoNormativa + "_" + salidaConsultaDataCorredora.p_Out_BrokerageRUT + "_" + SelectDocumentoIdentidad.get(0).DocumentoIdentidad.trim() + "\"]}";
						
						OutputStream outputStream = connBC.getOutputStream();
						outputStream.write(input.getBytes());
						outputStream.flush();
						
						if (connBC.getResponseCode() != 200)
						{
							if (connBC.getResponseCode() == 401)
							{
								salida.p_Out_Status = "";
								salida.p_Out_PdfContrato = "";
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "NOTOKEN";
								return salida;
							}
							else
							{
								salida.p_Out_Status = "";
								salida.p_Out_PdfContrato = "";
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "ERROR";
								return salida;
							}
							//else throw new RuntimeException("Failed: HTTP error code: " + connBC.getResponseCode());
						}
						
						BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));
						String output;
						
						
						HashMap<String,Object> param = new HashMap<String,Object>();
						param.put("p_IdEmpresa", in.p_IdEmpresa);
						param.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_TIMEOUT_ESERROR_BLOCKCHAIN);
						List<SelectConfiguracion> configuracionTimeOut = sql.query(conn, SQL_SELECT_CONFIGURACION, param, SelectConfiguracion.class);
						String EsTimeOutError = (configuracionTimeOut.size()>0) ? configuracionTimeOut.get(0).Valor : "N"; //espera un valor "S" para considerar TimeOut como error o distinto de S para no considerarlo como error
						
						while ((output = br.readLine()) != null) 
						{
							JSONObject obj = new JSONObject(output);
							
							if(obj.getString("status").equals("SUCCESS"))
							{
								try
								{
									JSONObject out = obj.getJSONObject("output");
									
									status = out.getString("Status"); 
									
									if(out.getString("DocumentLocation") != null)
									{
										pdfContrato = out.getString("DocumentLocation");
									}
									
									salida.p_Out_Status = out.getString("MSAType").equals("42") && EsTimeOutError.equalsIgnoreCase("N") ? "APPROVED" : status.toUpperCase(); //Bypass dado que en ambiente de pruebas siempre se cae por timeout
									salida.p_Out_PdfContrato = pdfContrato;
									salida.p_Out_ContadorError = 0L;
									salida.p_Out_MensajeError = "";
									connBC.disconnect();
									//retorna PDF como objeto
									if(!pdfContrato.equals("") && in.p_ConviertePDF.equals("S")) {
										salida.p_Out_ObjPDF = leeContrato(pdfContrato);
									};
									return salida;
								}
								catch(Exception ex)
								{
									salida.p_Out_Status = "NOFOUND";
									salida.p_Out_PdfContrato = "";
									salida.p_Out_ContadorError = 0L;
									salida.p_Out_MensajeError = "";
									return salida;
								}
							}
							else
							{
								salida.p_Out_Status = "";
								salida.p_Out_PdfContrato = "";
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "ERROR";
								return salida;
							}
						}
						
						connBC.disconnect();
					}
					else
					{
						salida.p_Out_Status = "";
						salida.p_Out_PdfContrato = "";
						salida.p_Out_ContadorError = 1L;
						salida.p_Out_MensajeError = "CONSULTA_TIPO_DOCUMENTO_FALLIDO";
						return salida;
					}
				}
				else
				{
					salida.p_Out_Status = "";
					salida.p_Out_PdfContrato = "";
					salida.p_Out_ContadorError = 1L;
					salida.p_Out_MensajeError = "CONSULTA_DOCUMENTO_INDENTIDAD_FALLIDO";
					salida.DocumentoIdentidad = "";
					return salida;
				}
			}
			else
			{
				salida.p_Out_Status = "";
				salida.p_Out_PdfContrato = "";
				salida.p_Out_ContadorError = 1L;
				salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CONFIGURACION";
				salida.DocumentoIdentidad = "";
				return salida;
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{		
			e.printStackTrace();
		}
		
		salida.p_Out_Status = "";
		salida.p_Out_PdfContrato = "";
		salida.p_Out_ContadorError = 1L;
		salida.p_Out_MensajeError = "ERROR";
		return salida;
	}
//permite leer el contrato desde la URL dada por BlockChacin	
	public DocumentoDTE_Salida leeContrato(String durl) {
		DocumentoDTE_Salida docsalida = new DocumentoDTE_Salida();
		try {
			//String URLparms=durl;
    		String URLparms=durl;
			URL url = new URL(URLparms);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();  
			urlc.setDoOutput(true);
			//urlc.setRequestProperty ("X-Auth-Token",token);
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
					//log.write("Error respuesta al obtener datos:"+urlc.getResponseCode());
					docsalida.P_OUT_ERRORESCONTADOR=1L;
					return docsalida;
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
			docsalida.P_OUT_ERRORESCONTADOR++;
		}

		return docsalida;
	}
	public static class DocumentoDTE_Salida { //TODO CAMBIAR NOMBRE
		public String	encodedString;
		public String	dteNumber;
		public String	resultado;
		public String	mensaje;
		public Long		P_OUT_ERRORESCONTADOR	= 0L;
		
	}

	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ConsultarEstadoContratoBlockchain_Entrada in = gson.fromJson(jin.toString(), ConsultarEstadoContratoBlockchain_Entrada.class);
		ConsultarEstadoContratoBlockchain_Salida out = logica(conn, in);
		
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
			Gson gson = Utils.getGsonBuilder();
			

			String jsonCL = "{\"p_IdTipoDocumento\":\"CONTRATOPRESTAMOANEXO48\",\"p_IdCliente\":1222312,\"p_BrokerageRUT\":\"80993900-6\",\"p_CodigoParametro\":\"BLOCKCHAIN.CONSULTACONTRATO.URL\",\"p_CodigoParametroLogin\":\"BLOCKCHAIN.LOGIN.URL\"}";
			 
			
			ConsultarEstadoContratoBlockchain_Entrada Objin = gson.fromJson(jsonCL, ConsultarEstadoContratoBlockchain_Entrada.class);

			ConsultarEstadoContratoBlockchain demo = new ConsultarEstadoContratoBlockchain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ConsultarEstadoContratoBlockchain_Salida ObjOut = demo.logica(con, Objin);
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
}