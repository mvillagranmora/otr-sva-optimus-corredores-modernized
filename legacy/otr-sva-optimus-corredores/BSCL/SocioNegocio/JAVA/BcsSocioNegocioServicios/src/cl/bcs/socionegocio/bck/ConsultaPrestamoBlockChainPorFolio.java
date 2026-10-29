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
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.borrowingClients;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.laDetails;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

	public class ConsultaPrestamoBlockChainPorFolio implements JavaCall
	{
		//-------------------------------------------------
		// QUERIES
		//-------------------------------------------------
		private final String SQL_SELECT_CONFIGURACION							= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");	
		
		// -------------------------------------------------
		// CLASES DE ENTRADA
		// -------------------------------------------------
		public static class ConsultaPrestamoBlockChainPorFolio_Entrada {
			public Long 	p_IdEmpresa				;
			public Long		p_IdUsuario				;				
			public String	p_IdModulo				;				
			public String	p_FolioSistemaExterno	;

			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		public static class Folio {
			public String p_laid = "";

			@Override
			public String toString()
			{
				return Utils.printJson(this, false);
			}
		}
		// -------------------------------------------------
		// CLASES DE SALIDA
		// -------------------------------------------------
		public static class ConsultaPrestamoBlockChainPorFolio_Salida {
			public List<MensajeSalida>			P_OUT_MENSAJES			;
			public Long							P_OUT_ERRORESCONTADOR	;
			public PrestamosBlockChain 			P_OUT_PRESTAMOBLOCKCHAIN;
			public String						P_OUT_STATUS			;

			public ConsultaPrestamoBlockChainPorFolio_Salida()
			{
				this.P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
				this.P_OUT_ERRORESCONTADOR			= 0L;
				this.P_OUT_PRESTAMOBLOCKCHAIN 		= null;
				this.P_OUT_STATUS					= "";
			}
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
			public String	DocumentoIdentidad;
			public String	Nombres;
			public Long		IdPersona;
			public Long		IdCliente;
			public String	NombreCompleto;

			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}

		// -------------------------------------------------
		// LOGICA DEL SERVICIO
		// -------------------------------------------------
		public ConsultaPrestamoBlockChainPorFolio_Salida logica(Connection conn, ConsultaPrestamoBlockChainPorFolio_Entrada in) throws Exception
		{	
			SQL sql = new SQL();
			ConsultaPrestamoBlockChainPorFolio_Salida salida = new ConsultaPrestamoBlockChainPorFolio_Salida();
			Gson gson = new Gson();
			boolean blnConsiguePrestamo = false;
			
			try {
				HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
				parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
				parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_PRESTAMOSBYID);
						
				List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada,	SelectConfiguracion.class);

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
						salida.P_OUT_STATUS 			= "";
						salida.P_OUT_ERRORESCONTADOR	= salidaLoginBlockchain.p_Out_ContadorError;
						salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"", salidaLoginBlockchain.p_Out_MensajeError));
						return salida;
					}

					connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
					connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
					connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

					JSONObject entrada = new JSONObject();
					JSONArray arrglo = new JSONArray();

					arrglo.put(in.p_FolioSistemaExterno);
					entrada.put("laids", arrglo);
					
					OutputStream outputStream = connBC.getOutputStream();
					outputStream.write(entrada.toString().getBytes());
					outputStream.flush();

					if (connBC.getResponseCode() != 200) {
						if (connBC.getResponseCode() == 401) {
							salida.P_OUT_STATUS 			= "";
							salida.P_OUT_ERRORESCONTADOR	= 1L;
							salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"", "NOTOKEN"));
							return salida;
						} else {
							salida.P_OUT_STATUS 			= "";
							salida.P_OUT_ERRORESCONTADOR	= 1L;
							salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"", "ERROR"));
							return salida;
						}
					}

					BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));

					String output;

					while ((output = br.readLine()) != null)
					{
						JSONObject obj = new JSONObject(output);
						
						if(obj.getString("status").equals("SUCCESS"))
						{
							try
							{
								JSONArray out = obj.getJSONArray("output");
								
								PrestamosBlockChain objPrestamoBC = gson.fromJson(out.get(0).toString(), PrestamosBlockChain.class);
														
								salida.P_OUT_ERRORESCONTADOR = 0L;
								salida.P_OUT_PRESTAMOBLOCKCHAIN = objPrestamoBC;
								blnConsiguePrestamo = true;
								break; 
							}
							catch(Exception ex)
							{
								salida.P_OUT_ERRORESCONTADOR = 1L; 
								salida.P_OUT_STATUS 			= "NoEncontrado";
								salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"PRESTAMO_BLOCKCHAIN_NO_ENCONTRADO", new HashMap<String, String>(), conn, in.p_IdEmpresa ));
								break; 
							}
						}
						else
						{
							if(obj.getString("status").equals("ERROR"))
							{
								try
								{
									JSONObject message = obj.getJSONObject("message");
									String response = message.getString("details");
									
									String[] details = response.split(": ");
									//String answer = details[2].substring(2, details[2].length() - 3);
									
									String answer = "";
									if(details[2].indexOf("[")>=0 && details[2].indexOf("]")>=0) {
										answer = details[2].substring(details[2].indexOf("[")+1,details[2].indexOf("[")+details[2].indexOf("]"));
									}
									else
									{
										answer = details[2];
									}
									
									if(answer.indexOf(",")>=0) {
										answer = answer.split(",")[0];
									}
									
									answer = answer.replace(".)", "");
									answer = answer.replace("'", "");
									answer = answer.replace("!", "");
									answer = answer.replace("\"", "");
									ConstantesCustodia constantesCustodia = new ConstantesCustodia();
									String vCodigo = constantesCustodia.codigoErroresBlockChain.get(answer);
									
									HashMap<String,String> valoresDiccionario = new HashMap<String,String>();
									String mensaje = Diccionario.getMensaje(conn,in.p_IdEmpresa, vCodigo, valoresDiccionario);
									mensaje = vCodigo == null ? answer : mensaje; 
									salida.P_OUT_ERRORESCONTADOR += 1;
									salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"", mensaje));
									break; 
								}
								catch(Exception ex)
								{
									salida.P_OUT_ERRORESCONTADOR += 1;
									break; 
								}
							}
						}
					}

					connBC.disconnect();
				
				} else {
					String msg=Diccionario.getMensaje(conn, in.p_IdEmpresa,"SERVICIO_BLOCK_CHAIN_NO_POSEE_CONFIGURACION")
				      .add("NombreServicio", "[Consulta Prestamo]")
					  .msg();
							
					salida.P_OUT_STATUS 			= "";
					salida.P_OUT_ERRORESCONTADOR	= 1L;
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"SERVICIO_BLOCK_CHAIN_NO_POSEE_CONFIGURACION",msg));
					return salida;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//si el contador de errores ya registro uno no emite un error general
			if(salida.P_OUT_ERRORESCONTADOR==0L && !blnConsiguePrestamo)
			{
				salida.P_OUT_STATUS 			= "";
				salida.P_OUT_ERRORESCONTADOR	= 1L;
				salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"","ERROR"));
			}
			
			return salida;
		}
		

		// -------------------------------------------------------------------------------------------
		// OBJETO DE TRANSPORTE - CALL
		// -------------------------------------------------------------------------------------------
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = Utils.getGsonBuilder();
			ConsultaPrestamoBlockChainPorFolio_Entrada in = gson.fromJson(jin.toString(), ConsultaPrestamoBlockChainPorFolio_Entrada.class);
			ConsultaPrestamoBlockChainPorFolio_Salida out = logica(conn, in);

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
						"\"p_IdUsuario\":6,\r\n" + 
						 "\"p_IdModulo\":\"CUS\",\r\n" + 
						"\"p_FolioSistemaExterno\": \"61-0000000024106\"" +  
						" }";
				ConsultaPrestamoBlockChainPorFolio_Entrada Objin = gson.fromJson(jsonCL, ConsultaPrestamoBlockChainPorFolio_Entrada.class);

				ConsultaPrestamoBlockChainPorFolio demo = new ConsultaPrestamoBlockChainPorFolio();
				System.out.println("ENTRADA");
				System.out.println(Objin.toString());

				try {
					ConsultaPrestamoBlockChainPorFolio_Salida ObjOut = demo.logica(con, Objin);
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

