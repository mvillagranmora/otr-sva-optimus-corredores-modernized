package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AsignaPrestamistaBlockchain  implements JavaCall {
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------

	private final String SQL_SELECT_CONFIGURACION							= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD						= loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class AsignaPrestamistaBlockchain_Entrada extends BcsPojo {	
		public Long		p_IdEmpresa					= 1L;
		public String	p_TipoCustodia;
		public String	p_TipoGarantia;
		public String	p_CodigoCorredorPrestamista;
		public String	p_Cantidad;
		public String	p_IdClientePrestatario;
		public String	p_IdClientePrestamista;
		public String	p_CantidadPrestamista;
		public String	p_Folio;
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class AsignaPrestamistaBlockchain_Salida extends BcsPojo {
		public String	p_Out_Status;
		public Long		p_Out_ContadorError;
		public String	p_Out_MensajeError;
		public String	p_Out_Mensaje;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public AsignaPrestamistaBlockchain_Salida() {
			this.p_Out_ContadorError 	= 0L;
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
			this.p_Out_MensajeError		= "";
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectConfiguracion extends BcsPojo {
        public String	Valor;
        public String	Descripcion;
	}
	
	public static class SelectDocumentoIdentidad extends BcsPojo {
        public String	DocumentoIdentidad;
        public String	Nombres;
        public Long		IdPersona;
        public Long		IdCliente;
        public String	NombreCompleto;
	}
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public AsignaPrestamistaBlockchain_Salida logica(Connection conn, AsignaPrestamistaBlockchain_Entrada in) throws Exception
	{
		SQL sql 									= new SQL();
		AsignaPrestamistaBlockchain_Salida salida 	= new AsignaPrestamistaBlockchain_Salida();
		
		try
		{
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_INGRESO_PRESTAMISTA);
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
					salida.p_Out_Status 		= "";
					salida.p_Out_ContadorError 	= salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError	= salidaLoginBlockchain.p_Out_MensajeError;
					return salida;
				}
				
				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
				
				HashMap<String,Object> parametroEntradaPrestatario = new HashMap<String,Object>();
				parametroEntradaPrestatario.put("p_IdCliente", in.p_IdClientePrestatario);
			    List<SelectDocumentoIdentidad> SelectPrestatario = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaPrestatario, SelectDocumentoIdentidad.class);
				
				if(!SelectPrestatario.isEmpty())
				{
					HashMap<String,Object> parametroEntradaPrestamista = new HashMap<String,Object>();
					parametroEntradaPrestamista.put("p_IdCliente", in.p_IdClientePrestamista);
					List<SelectDocumentoIdentidad> SelectPrestamista = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaPrestamista, SelectDocumentoIdentidad.class);
					
					if(!SelectPrestamista.isEmpty())
					{
						
						JSONObject entrada 	= new JSONObject();
						JSONArray arrglo 	= new JSONArray();
						JSONObject objeto 	= new JSONObject();
						
						/************************************************ == ******************************************************/
						// LendingClients
						JSONArray LendingClients = new JSONArray();
						JSONObject LendingClient = new JSONObject();
						LendingClient.put("LendingBrokerageHouseID", in.p_CodigoCorredorPrestamista);
						LendingClient.put("LendingClientRUT", SelectPrestamista.get(0).DocumentoIdentidad.trim());
						if(in.p_TipoCustodia.trim().equals("C")){
							if (in.p_TipoGarantia.trim().equals("S"))
							{
								LendingClient.put("MandatoryCollateral", "TRUE");
							}
							else
							{
								LendingClient.put("MandatoryCollateral", "FALSE");
							}
						}else{
							LendingClient.put("MandatoryCollateral", "TRUE");
						}
						LendingClient.put("LendingClientFund", "");
						LendingClient.put("CollateralType", in.p_TipoCustodia);
						LendingClient.put("LendingClientQty", in.p_Cantidad);
						LendingClients.put(LendingClient);
						objeto.put("LendingClients", LendingClients);
						
						// RequestDetails
						JSONObject RequestDetails = new JSONObject();
						RequestDetails.put("Action", "ADDLENDINGCLIENT");
						RequestDetails.put("LAID", in.p_Folio);
						objeto.put("RequestDetails", RequestDetails);

						arrglo.put(objeto);
						arrglo.put(salidaLoginBlockchain.p_Out_BrokerageID);
						entrada.put("args", arrglo);
						
						OutputStream outputStream = connBC.getOutputStream();
						outputStream.write(entrada.toString().getBytes());
						outputStream.flush();
						
						if (connBC.getResponseCode() != 200)
						{
							if (connBC.getResponseCode() == 401)
							{
								salida.p_Out_Status 		= "";
								salida.p_Out_ContadorError 	= 1L;
								GeneraMensajeSalida(conn, in.p_IdEmpresa, "NOTOKEN", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
								return salida;
							} 
							else
							{
								salida.p_Out_Status 		= "";
								salida.p_Out_ContadorError 	= 1L;
								GeneraMensajeSalida(conn, in.p_IdEmpresa, "ERROR", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
								return salida;
							}
							//else throw new RuntimeException("Failed: HTTP error code: " + connBC.getResponseCode());
						}
						
						BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));
						
						String output;
						
						while ((output = br.readLine()) != null)
						{
							ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
							JSONObject obj 							= new JSONObject(output);
							
							if(obj.getString("status").equals("SUCCESS"))
							{
								try
								{
									JSONObject outputResponse 	= obj.getJSONObject("output");
									JSONObject response 		= outputResponse.getJSONObject("response");
									String info 				= response.getString("info");								
									String vCodigo 				= constantesCustodia.codigoErroresBlockChain.get(info.replace(".", ""));
									
									salida.p_Out_Status 		= obj.getString("status");
									salida.p_Out_ContadorError 	= 0L;
									GeneraMensajeSalida(conn, in.p_IdEmpresa, vCodigo, MensajeSalida.MENSAJE_INFORMACION, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
									return salida;
								}
								catch(Exception ex)
								{
									salida.p_Out_Status 		= "NOFOUND";
									salida.p_Out_ContadorError 	= 0L;
									salida.p_Out_MensajeError	= ex.getMessage();
									GeneraMensajeSalida(conn, in.p_IdEmpresa, "NOFOUND", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
									return salida;
								}
							}
							else
							 {
								if(obj.getString("status").equals("ERROR")) {
									
									try {
										String message 			= obj.getString("message");
										JSONArray messageArray 	= new JSONArray(message);
										
										for (int i = 0, size = messageArray.length(); i < size; i++) {
											String messageInArray 		= messageArray.getString(i);
											String vCodigo 				= constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", "").replace(in.p_Cantidad + " ", ""));
											salida.p_Out_ContadorError += 1L;
											GeneraMensajeSalida(conn, in.p_IdEmpresa, vCodigo, MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
										}
										
										salida.p_Out_Status 			= obj.getString("status");
										salida.p_Out_MensajeError		= message;
										return salida;
									} catch(Exception ex) {
										salida.p_Out_Status 		= "ERROR";
										salida.p_Out_ContadorError 	= 1L;
										salida.p_Out_MensajeError	= ex.getMessage();
										GeneraMensajeSalida(conn, in.p_IdEmpresa, "UI_ERROR_SERVICIO_BLOCKCHAIN_ASIGNACION_PRESTAMO", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
										return salida;
									}
								}
							}
						}
						
						connBC.disconnect();
					}
					else
					{
						salida.p_Out_Status 		= "";
						salida.p_Out_ContadorError 	= 1L;
						GeneraMensajeSalida(conn, in.p_IdEmpresa, "CONSULTA_DOCUMENTO_INDENTIDAD_PRESTAMISTA_FALLIDO", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
						return salida;
					}
				}
				else
				{
					salida.p_Out_Status 		= "";
					salida.p_Out_ContadorError 	= 1L;
					GeneraMensajeSalida(conn, in.p_IdEmpresa, "CONSULTA_DOCUMENTO_INDENTIDAD_PRESTATARIO_FALLIDO", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
					return salida;
				}
			}
			else
			{
				salida.p_Out_Status 		= "";
				salida.p_Out_ContadorError 	= 1L;
				GeneraMensajeSalida(conn, in.p_IdEmpresa, "SERVICIO_NO_POSEE_CONFIGURACION", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
				return salida;
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			salida.p_Out_MensajeError	= e.getMessage();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			salida.p_Out_MensajeError	= e.getMessage();
		}
		
		salida.p_Out_Status 		= "";
		salida.p_Out_ContadorError 	= 1L;
		GeneraMensajeSalida(conn, in.p_IdEmpresa, "UI_PERDIDA_DE_COMUNICACION", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
		return salida;
	}
	
	
	private void GeneraMensajeSalida(Connection con, Long idEmpresa, String codigoMensaje, String tipoMensaje, HashMap<String, String> valoresDiccionarioMensaje, List<MensajeSalida> P_OUT_MENSAJES ) throws Exception {
		
		codigoMensaje 	= codigoMensaje == null ? "UI_BC_MENSAJE_NO_IDENTIFICADO" : codigoMensaje;
		String mensaje	=	Diccionario.getMensaje(con, idEmpresa, codigoMensaje)
							.msg();
		mensaje			= mensaje == null ? "" : mensaje;
		
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L, tipoMensaje, codigoMensaje, mensaje));
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		AsignaPrestamistaBlockchain_Entrada in = gson.fromJson(jin.toString(), AsignaPrestamistaBlockchain_Entrada.class);
		AsignaPrestamistaBlockchain_Salida out = logica(conn, in);
		
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

			/*String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_Tasa\": \"0.6000\",\r\n" + 
					"	\"p_Fecha\": \"2018-08-22\",\r\n" + 
					"	\"p_Nemo\": \"COPEC\",\r\n" + 
					"	\"p_Mercado\": \"RV\",\r\n" + 
					"	\"p_TipoPrestamo\": \"O\",\r\n" + 
					"	\"p_TipoGarantia\": \"G\",\r\n" + 
					"	\"p_CondicionLiquidacion\": \"PH\",\r\n" +  
					"	\"p_CodigoCorredorPrestamista\": \"58\",\r\n" + 
					"	\"p_Cantidad\": \"4500\",\r\n" + 
					"	\"p_FechaLiquidacion\": \"2018-08-27\",\r\n" + 
					"	\"p_IdClientePrestatario\": \"524891\",\r\n" + 
					"	\"p_IdClientePrestamista\": \"524891\",\r\n" + 
					"	\"p_CantidadPrestamista\": \"3000\"\r\n" + 
					"}";*/
			
			String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_TipoCustodia\": \"C\",\r\n" +  
					"	\"p_TipoGarantia\": \"N\",\r\n" +  
					"	\"p_CodigoCorredorPrestamista\": \"61\",\r\n" + 
					"	\"p_Cantidad\": \"1020\",\r\n" + 
					"	\"p_IdClientePrestatario\": \"1418719\",\r\n" + 
					"	\"p_IdClientePrestamista\": \"1418719\",\r\n" + 
					"	\"p_CantidadPrestamista\": \"\",\r\n" + 
					"	\"p_Folio\": \"61-0000000024137\"\r\n" + 
					"}";

			AsignaPrestamistaBlockchain_Entrada Objin = gson.fromJson(jsonCL, AsignaPrestamistaBlockchain_Entrada.class);

			AsignaPrestamistaBlockchain demo = new AsignaPrestamistaBlockchain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				AsignaPrestamistaBlockchain_Salida ObjOut = demo.logica(con, Objin);
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
