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
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

//--public class ConsultarEstadoContratoBlockchain implements JavaCall 

public class ConsultaEstadoAnticipoBlockChain implements JavaCall 
{
	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString(
			"cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	//private final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_PRESTAMO = "BLOCKCHAIN.CONSULTAANTICIPOPRESTAMO.URL";
	//private final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN = "BLOCKCHAIN.LOGIN.URL";

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ConsultaEstadoAnticipoBlockChain_Entrada {
		public Long p_IdEmpresa = 1L;
		public String p_FolioExterno;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class ConsultaEstadoAnticipoBlockChain_Salida {
		public String p_Out_Status;
		public String p_Out_Mensaje;
		public Long p_Out_ContadorError;
		public String p_Out_MensajeError;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class SelectConfiguracion {
		public String Valor;
		public String Descripcion;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SelectDocumentoIdentidad {
		public String DocumentoIdentidad;
		public String Nombres;
		public Long IdPersona;
		public Long IdCliente;
		public String NombreCompleto;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public ConsultaEstadoAnticipoBlockChain_Salida logica(Connection conn, ConsultaEstadoAnticipoBlockChain_Entrada in)
			throws Exception {
		SQL sql = new SQL();
		ConsultaEstadoAnticipoBlockChain_Salida salida = new ConsultaEstadoAnticipoBlockChain_Salida();

		try {
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_PRESTAMO); 
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada,
					SelectConfiguracion.class);

			if (!SelectConfiguracion.isEmpty()) {
				URL url = new URL(SelectConfiguracion.get(0).Valor);

				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");

				LoginBlockchain loginBlockchain = new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain = new LoginBlockchain.LoginBlockchain_Entrada();

				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;

				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn,
						entradaLoginBlockchain);

				if (salidaLoginBlockchain.p_Out_ContadorError > 0) {
					salida.p_Out_Status = "";
					salida.p_Out_Mensaje = "";
					salida.p_Out_ContadorError = salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError = salidaLoginBlockchain.p_Out_MensajeError;
					return salida;
				}

				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
	
					JSONObject entrada = new JSONObject();
					JSONArray arrglo = new JSONArray();
					JSONObject objeto = new JSONObject();
							
					arrglo.put(in.p_FolioExterno);
					//objeto.put("", arrglo);
					entrada.put("args", arrglo);

					OutputStream outputStream = connBC.getOutputStream();
					outputStream.write(entrada.toString().getBytes());
					outputStream.flush();

					if (connBC.getResponseCode() != 200) {
						if (connBC.getResponseCode() == 401) {
							salida.p_Out_Status = "";
							salida.p_Out_Mensaje = "";
							salida.p_Out_ContadorError = 1L;
							salida.p_Out_MensajeError = "NOTOKEN";
							return salida;
						} else {
							salida.p_Out_Status = "";
							salida.p_Out_Mensaje = "";
							salida.p_Out_ContadorError = 1L;
							salida.p_Out_MensajeError = "ERROR";
							return salida;
						}
						// else throw new RuntimeException("Failed: HTTP error
						// code: " + connBC.getResponseCode());
					}

					BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));

					String output;

					while ((output = br.readLine()) != null) {
						
						System.out.println("ConsultaPrestamos|OUTPUT: " + output);
						JSONObject obj = new JSONObject(output);

						if (obj.getString("status").equals("SUCCESS")) {
							try {
								JSONObject out  = obj.getJSONObject("output");
								JSONObject out1 = out.getJSONObject("Request");
								JSONObject response = out1.getJSONObject("RequestDetails");
								//salida.p_Out_Status = response.getString("NewStatus");
								salida.p_Out_Status = response.getString("CurrentStatus");
								
								salida.p_Out_ContadorError = 0L;
								salida.p_Out_MensajeError = "";
								return salida;
								
							} catch (Exception ex) {
								salida.p_Out_Status = "NOFOUND";
								salida.p_Out_Mensaje = "";
								salida.p_Out_ContadorError = 0L;
								salida.p_Out_MensajeError = "";
								return salida;
							}
						} else {
							if (obj.getString("status").equals("ERROR")) {
								try {
									JSONObject message = obj.getJSONObject("message");
									String response = message.getString("details");

									String[] details = response.split(": ");
									String answer = details[2].substring(2, details[2].length() - 3);

									ConstantesCustodia constantesCustodia = new ConstantesCustodia();
									String vCodigo = constantesCustodia.codigoErroresBlockChain.get(answer);

									HashMap<String, String> valoresDiccionario = new HashMap<String, String>();
									String mensaje = Diccionario.getMensaje(conn, 1L, vCodigo, valoresDiccionario);

									salida.p_Out_Status = obj.getString("status");
									salida.p_Out_Mensaje = mensaje;
									salida.p_Out_ContadorError = 1L;
									salida.p_Out_MensajeError = "";
									return salida;
								} catch (Exception ex) {
									salida.p_Out_Status = "";
									salida.p_Out_Mensaje = "";
									salida.p_Out_ContadorError = 1L;
									salida.p_Out_MensajeError = "ERROR";
									return salida;
								}
							}
						}
					}

					connBC.disconnect();
			
			} else {
				salida.p_Out_Status = "";
				salida.p_Out_Mensaje = "";
				salida.p_Out_ContadorError = 1L;
				salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CONFIGURACION";
				return salida;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		salida.p_Out_Status = "";
		salida.p_Out_Mensaje = "";
		salida.p_Out_ContadorError = 1L;
		salida.p_Out_MensajeError = "ERROR";
		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultaEstadoAnticipoBlockChain_Entrada in = gson.fromJson(jin.toString(), ConsultaEstadoAnticipoBlockChain_Entrada.class);
		ConsultaEstadoAnticipoBlockChain_Salida out = logica(conn, in);

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


			String jsonCL = "{\r\n" + 
					"\"p_IdEmpresa\":1,\r\n" + 
					"\"p_FolioExterno\": \"61-0000000018655\"\r\n" + 
					" }";
			ConsultaEstadoAnticipoBlockChain_Entrada Objin = gson.fromJson(jsonCL, ConsultaEstadoAnticipoBlockChain_Entrada.class);

			ConsultaEstadoAnticipoBlockChain demo = new ConsultaEstadoAnticipoBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				ConsultaEstadoAnticipoBlockChain_Salida ObjOut = demo.logica(con, Objin);
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