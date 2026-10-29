package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
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



public class IngresoPrestamosBlockchain implements JavaCall {
	
	
	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION		= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class IngresoPrestamosBlockchain_Entrada extends BcsPojo {
		public Long p_IdEmpresa = 1L;
		public String p_Tasa;
		public String p_FechaOperacion;
		public String p_Nemo;
		public String p_Mercado;
		public String p_TipoPrestamo;
		public String p_TipoGarantia;
		public String p_CondicionLiquidacion;
		public String p_CodigoCorredorPrestamista;
		public String p_Cantidad;
		public String p_FechaVencimiento;
		public String p_IdClientePrestatario;
		public String p_IdClientePrestamista;
		public String p_CantidadPrestamista;
		
		public BigDecimal p_MontoPrima;
		public String p_DocumentoIdentidadPrestatario;
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class IngresoPrestamosBlockchain_Salida extends BcsPojo {
		public String 				p_Out_Status;
		public String 				p_Out_Mensaje;
		public String 				p_Out_FolioExterno = "";
		public Long 				p_Out_ContadorError;
		public String 				p_Out_MensajeError;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public IngresoPrestamosBlockchain_Salida() {
			this.p_Out_ContadorError 	= 0L;
			this.p_Out_Mensaje 			= "";
			this.p_Out_MensajeError 	= "";
			this.P_OUT_MENSAJES			= new ArrayList<MensajeSalida>();
		}
	}

	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class SelectConfiguracion extends BcsPojo {
		public String Valor;
		public String Descripcion;
	}

	public static class SelectDocumentoIdentidad extends BcsPojo {
		public String DocumentoIdentidad;
		public String Nombres;
		public Long IdPersona;
		public Long IdCliente;
		public String NombreCompleto;
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------

	public IngresoPrestamosBlockchain_Salida logica(Connection conn, IngresoPrestamosBlockchain_Entrada in) throws Exception {

		SQL sql 									= new SQL();
		IngresoPrestamosBlockchain_Salida salida 	= new IngresoPrestamosBlockchain_Salida();

		try {
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_INGRESO_PRESTAMO);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);

			if (!SelectConfiguracion.isEmpty()) {
				
				URL url 					= new URL(SelectConfiguracion.get(0).Valor);
				HttpURLConnection connBC 	= (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");

				LoginBlockchain loginBlockchain 								= new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();

				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;

				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);
 
				if (salidaLoginBlockchain.p_Out_ContadorError > 0) {
					salida.p_Out_Status 		= "";
					salida.p_Out_Mensaje 		= "";
					salida.p_Out_ContadorError 	= salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError 	= salidaLoginBlockchain.p_Out_MensajeError;
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, salidaLoginBlockchain.p_Out_MensajeError, new HashMap<String, String>(), conn, in.p_IdEmpresa) );
					return salida;
				}

				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

				HashMap<String, Object> parametroEntradaPrestatario = new HashMap<String, Object>();
				parametroEntradaPrestatario.put("p_IdCliente", in.p_IdClientePrestatario);
				List<SelectDocumentoIdentidad> SelectPrestatario = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaPrestatario, SelectDocumentoIdentidad.class);

				if (!SelectPrestatario.isEmpty()) {

					JSONObject entrada 	= new JSONObject();
					JSONArray arrglo 	= new JSONArray();
					JSONObject objeto 	= new JSONObject();

					// RequestDetails
					JSONObject RequestDetails = new JSONObject();
					RequestDetails.put("Action", "CREATE");
					objeto.put("RequestDetails", RequestDetails);

					// LADetails
					JSONObject LADetails = new JSONObject();
					LADetails.put("Yield", in.p_Tasa);
					LADetails.put("LADate", in.p_FechaOperacion);
					LADetails.put("Symbol", in.p_Nemo.trim());
					LADetails.put("SecurityType", in.p_Mercado);
					LADetails.put("LendingAgreementType", in.p_TipoPrestamo);
					LADetails.put("SettlType", in.p_CondicionLiquidacion);
					LADetails.put("BorrowingBrokerageHouseID", salidaLoginBlockchain.p_Out_BrokerageID);
					LADetails.put("LendingBrokerageHouseID", in.p_CodigoCorredorPrestamista);/****************************************/
					LADetails.put("LendingQty", in.p_Cantidad);
					LADetails.put("MaturityDate", in.p_FechaVencimiento);
					objeto.put("LADetails", LADetails);
					// BorrowingClient
					JSONObject BorrowingClient = new JSONObject();
					BorrowingClient.put("BorrowingClientRUT", SelectPrestatario.get(0).DocumentoIdentidad.trim());
					objeto.put("BorrowingClient", BorrowingClient);

					// LendingClients
					JSONArray LendingClients = new JSONArray();

					if (!in.p_IdClientePrestamista.equals("")) {
						HashMap<String, Object> parametroEntradaPrestamista = new HashMap<String, Object>();
						parametroEntradaPrestamista.put("p_IdCliente", in.p_IdClientePrestamista);
						List<SelectDocumentoIdentidad> SelectPrestamista = sql.query(conn,SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntradaPrestamista,SelectDocumentoIdentidad.class);

						if (!SelectPrestamista.isEmpty()) {
							JSONObject LendingClient = new JSONObject();
							LendingClient.put("LendingClientRUT", SelectPrestamista.get(0).DocumentoIdentidad.trim());
							LendingClient.put("LendingClientFund", "");
							LendingClient.put("LendingClientQty", "0");
							LendingClient.put("CollateralType", in.p_TipoGarantia);

							if (in.p_TipoGarantia.trim().equals("S")) {
								LendingClient.put("MandatoryCollateral", "FALSE");
							} else {
								LendingClient.put("MandatoryCollateral", "TRUE");
							}

							LendingClient.put("LendingBrokerageHouseID", in.p_CodigoCorredorPrestamista);/****************************************/
							LendingClients.put(LendingClient);
						}
					}

					objeto.put("LendingClients", LendingClients);

					arrglo.put(objeto);
					arrglo.put(salidaLoginBlockchain.p_Out_BrokerageID);
					entrada.put("args", arrglo);

					OutputStream outputStream = connBC.getOutputStream();
					outputStream.write(entrada.toString().getBytes());
					outputStream.flush();

					if (connBC.getResponseCode() != 200) {
						if (connBC.getResponseCode() == 401) {
							salida.p_Out_Status 		= "";
							salida.p_Out_Mensaje 		= "";
							salida.p_Out_ContadorError 	= 1L;
							salida.p_Out_MensajeError 	= "UI_BLOCKCHAIN_NOTOKEN";
							salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_NOTOKEN", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
							return salida;
						} else {
							salida.p_Out_Status 		= "";
							salida.p_Out_Mensaje 		= "";
							salida.p_Out_ContadorError 	= 1L;
							salida.p_Out_MensajeError 	= "UI_ERROR_BLOCKCHAIN_INDEFINIDO";
							salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_NOTOKEN", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
							return salida;
						}
						// else throw new RuntimeException("Failed: HTTP error
						// code: " + connBC.getResponseCode());
					}

					BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));

					String output;

					while ((output = br.readLine()) != null) {
						JSONObject obj = new JSONObject(output);
						
						if (obj.getString("status").equals("SUCCESS")) {
							try {
								HashMap<String, String> valores = new HashMap<String, String>();
								JSONArray LA 					= obj.getJSONArray("LA");
								JSONObject out 					= new JSONObject(LA.get(0).toString());
								JSONObject requestDetails 		= out.getJSONObject("RequestDetails");

								salida.p_Out_Status 		= obj.getString("status");
								salida.p_Out_Mensaje 		= "";
								salida.p_Out_FolioExterno 	= requestDetails.getString("LAID");
								salida.p_Out_ContadorError 	= 0L;
								salida.p_Out_MensajeError 	= "";
								
								valores.put("TipoCartera", "BlockChain");
								valores.put("FolioCartera", salida.p_Out_FolioExterno);
								salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_CARTERA_CREADA_FOLIO", valores, conn, in.p_IdEmpresa) );
								return salida;
							} catch (Exception ex) {
								salida.p_Out_Status 		= "NOFOUND";
								salida.p_Out_Mensaje 		= "";
								salida.p_Out_ContadorError 	= 0L;
								salida.p_Out_MensajeError 	= "";
								return salida;
							}
						} else {
							if (obj.getString("status").equals("ERROR")) {
								try {
									String message = obj.getString("message");
									JSONArray messageArray = new JSONArray(message);
									
									for (int i = 0, size = messageArray.length(); i < size; i++) {
										String messageInArray 					= messageArray.getString(i);
										ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
										String vCodigo 							= constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", "").replace(in.p_Cantidad + " ", ""));
										
										salida.p_Out_Mensaje 	   += (i > 0 ? ", " : "") + messageInArray;
										salida.p_Out_ContadorError += 1L;
										salida.p_Out_MensajeError   = vCodigo;
										salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, vCodigo, new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									}
									
									salida.p_Out_Status 			= obj.getString("status");
									
									return salida; 
								} catch (Exception ex) {
									salida.p_Out_Status 		= "ERROR";
									salida.p_Out_Mensaje 		= obj.getString("message");
									salida.p_Out_ContadorError 	= 1L;
									salida.p_Out_MensajeError 	= "UI_ERROR_SERVICIO_BLOCKCHAIN_INGRESO_PRESTAMO";
									salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_SERVICIO_BLOCKCHAIN_INGRESO_PRESTAMO", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									return salida;
								}
							}
						}
					}

					connBC.disconnect();
				} else {
					salida.p_Out_Status 		= "";
					salida.p_Out_Mensaje 		= "";
					salida.p_Out_ContadorError 	= 1L;
					salida.p_Out_MensajeError 	= "CONSULTA_DOCUMENTO_INDENTIDAD_PRESTATARIO_FALLIDO";
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CONSULTA_DOCUMENTO_INDENTIDAD_PRESTATARIO_FALLIDO", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
					return salida;
				}
			} else {
				salida.p_Out_Status 		= "";
				salida.p_Out_Mensaje 		= "";
				salida.p_Out_ContadorError 	= 1L;
				salida.p_Out_MensajeError 	= "SERVICIO_NO_POSEE_CONFIGURACION";
				salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "SERVICIO_NO_POSEE_CONFIGURACION", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
				return salida;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		salida.p_Out_Status 		= "";
		salida.p_Out_Mensaje 		= "";
		salida.p_Out_ContadorError 	= 1L;
		salida.p_Out_MensajeError 	= "ERROR";
		salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_ERROR_SERVICIO_BLOCKCHAIN_INGRESO_PRESTAMO", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		IngresoPrestamosBlockchain_Entrada in = gson.fromJson(jin.toString(), IngresoPrestamosBlockchain_Entrada.class);
		IngresoPrestamosBlockchain_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\r\n" + "	\"p_IdEmpresa\": 1,\r\n" + "	\"p_Tasa\": \"0.6000\",\r\n"
					+ "	\"p_Fecha\": \"2018-08-22\",\r\n" + "	\"p_Nemo\": \"COPEC\",\r\n"
					+ "	\"p_Mercado\": \"RV\",\r\n" + "	\"p_TipoPrestamo\": \"C\",\r\n"
					+ "	\"p_TipoGarantia\": \"G\",\r\n" + "	\"p_CondicionLiquidacion\": \"PH\",\r\n"
					+ "	\"p_CodigoCorredorPrestamista\": \"58\",\r\n" + "	\"p_Cantidad\": \"4500\",\r\n"
					+ "	\"p_FechaLiquidacion\": \"2018-08-27\",\r\n"
					+ "	\"p_IdClientePrestatario\": \"1421454\",\r\n" + "	\"p_IdClientePrestamista\": \"\",\r\n"
					+ "	\"p_CantidadPrestamista\": \"0\"\r\n" + "}";

			IngresoPrestamosBlockchain_Entrada Objin = gson.fromJson(jsonCL, IngresoPrestamosBlockchain_Entrada.class);

			IngresoPrestamosBlockchain demo = new IngresoPrestamosBlockchain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				IngresoPrestamosBlockchain_Salida ObjOut = demo.logica(con, Objin);
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