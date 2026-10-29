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

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaDataCorredoraBlockChain implements JavaCall 
{
	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ConsultaDataCorredoraBlockChain_Entrada {
		public Long 	p_IdEmpresa = 1L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class ConsultaDataCorredoraBlockChain_Salida {
		public String	p_Out_BrokerageAddress	= "";
		public String	p_Out_BrokerageAltID	= "";
		public String	p_Out_BrokerageAltName	= "";
		public String	p_Out_BrokerageID		= "";
		public String	p_Out_BrokerageMSPID	= "";
		public String	p_Out_BrokerageName		= "";
		public String	p_Out_BrokeragePhone	= "";
		public String	p_Out_BrokerageRUT		= "";
		public String	p_Out_BrokerageShortName= "";
		public String 	p_Out_Status;
		public String 	p_Out_Mensaje;
		public Long 	p_Out_ContadorError;
		public String 	p_Out_MensajeError;

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
	public ConsultaDataCorredoraBlockChain_Salida logica(Connection conn, ConsultaDataCorredoraBlockChain_Entrada in) throws Exception {
		SQL sql = new SQL();
		ConsultaDataCorredoraBlockChain_Salida salida = new ConsultaDataCorredoraBlockChain_Salida();

		try {
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_DATA_CORREDORA); 
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);

			if (!SelectConfiguracion.isEmpty()) {
				URL url = new URL(SelectConfiguracion.get(0).Valor);

				LoginBlockchain loginBlockchain = new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain = new LoginBlockchain.LoginBlockchain_Entrada();
				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;
				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn,entradaLoginBlockchain);

				if (salidaLoginBlockchain.p_Out_ContadorError > 0) {
					salida.p_Out_Status = "";
					salida.p_Out_Mensaje = "";
					salida.p_Out_ContadorError = salidaLoginBlockchain.p_Out_ContadorError;
					salida.p_Out_MensajeError = salidaLoginBlockchain.p_Out_MensajeError;
					return salida;
				}

				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");
				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
	
				JSONObject entrada = new JSONObject();
				entrada.put("args", new JSONArray());

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
				}

				BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));
				String output;

				while ((output = br.readLine()) != null) {
					System.out.println("ConsultaPrestamos|OUTPUT: " + output);
					JSONObject obj = new JSONObject(output);

					if (obj.getString("status").equals("SUCCESS")) {
						try {
							String info = StringEscapeUtils.unescapeJava(obj.getString("output").substring(2, (obj.getString("output").length() - 2 )  ));
							JSONObject outputJson = new JSONObject(info);
							salida.p_Out_BrokerageAddress	= outputJson.getString("BrokerageAddress");
							salida.p_Out_BrokerageAltID		= outputJson.getString("BrokerageAltID");
							salida.p_Out_BrokerageAltName	= outputJson.getString("BrokerageAltName");
							salida.p_Out_BrokerageID		= outputJson.getString("BrokerageID");
							salida.p_Out_BrokerageMSPID		= outputJson.getString("BrokerageMSPID");
							salida.p_Out_BrokerageName		= outputJson.getString("BrokerageName");
							salida.p_Out_BrokeragePhone		= outputJson.getString("BrokeragePhone");
							salida.p_Out_BrokerageRUT		= outputJson.getString("BrokerageRUT");
							salida.p_Out_BrokerageShortName	= outputJson.getString("BrokerageShortName");
							salida.p_Out_ContadorError 		= 0L;
							salida.p_Out_MensajeError 		= "";
							return salida;
							
						} catch (Exception ex) {
							salida.p_Out_Status 		= "NOFOUND";
							salida.p_Out_Mensaje 		= "";
							salida.p_Out_ContadorError 	= 0L;
							salida.p_Out_MensajeError 	= obj.getString("message");
							return salida;
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
		ConsultaDataCorredoraBlockChain_Entrada in = gson.fromJson(jin.toString(), ConsultaDataCorredoraBlockChain_Entrada.class);
		ConsultaDataCorredoraBlockChain_Salida out = logica(conn, in);

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


			String jsonCL = "{}";
			ConsultaDataCorredoraBlockChain_Entrada Objin = gson.fromJson(jsonCL, ConsultaDataCorredoraBlockChain_Entrada.class);

			ConsultaDataCorredoraBlockChain demo = new ConsultaDataCorredoraBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				ConsultaDataCorredoraBlockChain_Salida ObjOut = demo.logica(con, Objin);
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