package cl.bcs.plataforma.bck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class LoginBlockchain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class LoginBlockchain_Entrada
	{
		public String	p_CodigoParametro;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class LoginBlockchain_Salida
	{
		public String	p_Out_UserLogin		= "";
		public String	p_Out_Token			= "";
		public String	p_Out_BrokerageID	= "";
		public Long		p_Out_ContadorError;
		public String	p_Out_MensajeError;
		
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
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	@Trace
	public LoginBlockchain_Salida logica(Connection conn, LoginBlockchain_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		
		LoginBlockchain_Salida salida = new LoginBlockchain_Salida();
		
		try
		{
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", 1);
			parametroEntrada.put("p_CodigoParametro", in.p_CodigoParametro);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
			
			if(!SelectConfiguracion.isEmpty())
			{
				String Origen = SelectConfiguracion.get(0).Valor;

				parametroEntrada.put("p_IdEmpresa", 1);
				parametroEntrada.put("p_CodigoParametro", "BLOCKCHAIN.LOGIN.USUARIO");
				SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
				
				if(!SelectConfiguracion.isEmpty())
				{
					String Username = SelectConfiguracion.get(0).Valor;
					
					parametroEntrada.put("p_IdEmpresa", 1);
					parametroEntrada.put("p_CodigoParametro", "BLOCKCHAIN.LOGIN.CLAVE");
					SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
					
					if(!SelectConfiguracion.isEmpty())
					{
						String Password = SelectConfiguracion.get(0).Valor;
						
						URL url = new URL(Origen);
						
						HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
						connBC.setDoOutput(true);
						connBC.setRequestMethod("POST");
						connBC.setRequestProperty("Content-Type", "application/json");
						
						JSONObject input = new JSONObject();
						input.put("username", Username);
						input.put("password", Password);
						
						OutputStream outputStream = connBC.getOutputStream();
						outputStream.write(input.toString().getBytes());
						outputStream.flush();
						
						if (connBC.getResponseCode() != 200)
						{
							if (connBC.getResponseCode() == 401)
							{
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "NOUSER";
								return salida;
							}
							else {
								salida.p_Out_ContadorError = 1L;
								salida.p_Out_MensajeError = "SERVICE_TEMPORARILY_UNAVAIBLE";
								return salida;
							}
						}
						
						BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));
						
						String output;
						
						while ((output = br.readLine()) != null)
						{
							JSONObject obj = new JSONObject(output);
							JSONObject userObj = obj.getJSONObject("user");
							JSONObject loginObj = userObj.getJSONObject("loginData");

							salida.p_Out_UserLogin = userObj.getString("username");
							salida.p_Out_Token = obj.getString("id");
							salida.p_Out_BrokerageID = loginObj.getString("officeID");
							salida.p_Out_ContadorError = 0L;
							salida.p_Out_MensajeError = "";
							return salida;
						}
						
						connBC.disconnect();
					}
					else
					{
						salida.p_Out_ContadorError = 1L;
						salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CINFIGURACION_DE_CLAVE";
						return salida;
					}
				}
				else
				{
					salida.p_Out_ContadorError = 1L;
					salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CINFIGURACION_DE_USUARIO";
					return salida;
				}
			}
			else
			{
				salida.p_Out_ContadorError = 1L;
				salida.p_Out_MensajeError = "SERVICIO_NO_POSEE_CINFIGURACION";
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
		
		salida.p_Out_ContadorError = 1L;
		salida.p_Out_MensajeError = "ERROR";
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		LoginBlockchain_Entrada in = gson.fromJson(jin.toString(), LoginBlockchain_Entrada.class);
		LoginBlockchain_Salida out = logica(conn, in);
		
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

			String jsonCL = "{\"p_CodigoParametro\" : \"BLOCKCHAIN.LOGIN.URL\"}";
			
			LoginBlockchain_Entrada Objin = gson.fromJson(jsonCL, LoginBlockchain_Entrada.class);

			LoginBlockchain demo = new LoginBlockchain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				LoginBlockchain_Salida ObjOut = demo.logica(con, Objin);
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