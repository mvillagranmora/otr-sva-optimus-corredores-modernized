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
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

	public class ConsultaListaPorFolioBlockChain implements JavaCall {
		//-------------------------------------------------
		// QUERIES
		//-------------------------------------------------
		private final String SQL_SELECT_CONFIGURACION							= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");	

		
		
		// -------------------------------------------------
		// CLASES DE ENTRADA
		// -------------------------------------------------
		public static class ConsultaListaPorFolioBlockChain_Entrada extends BcsPojo {
			public Long 		p_IdEmpresa = 1L;
			public List<Folio> 	p_FolioExterno;
		}
		
		public static class Folio extends BcsPojo {
			public String p_laid = "";
			public Folio(String laid) {
				this.p_laid = laid;
			}
		}
		
		/* no se ocupa aqui.. al menos */
		public static class Secuencia extends BcsPojo {
			public Long   p_IdEmpresa;
			public String p_CodigoParametro;
			public String p_Valor;
		}
		
		// -------------------------------------------------
		// CLASES DE SALIDA
		// -------------------------------------------------
		public static class ConsultaListaPorFolioBlockChain_Salida extends BcsPojo {
			public String 						p_Out_Status;
			public String 						p_Out_Mensaje;
			public Long 						p_Out_ContadorError;
			public String 						p_Out_MensajeError;
			public List<PrestamosBlockChain> 	p_lista_prestamos;

			public ConsultaListaPorFolioBlockChain_Salida()
			{
				this.p_lista_prestamos 		= new ArrayList<>();
				this.p_Out_ContadorError 	= 0L;
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
			public String 	DocumentoIdentidad;
			public String 	Nombres;
			public Long 	IdPersona;
			public Long 	IdCliente;
			public String 	NombreCompleto;
		}
		
		public static class retrieveLasByIDsResponse extends BcsPojo {
			public String 						status;
			public String 						message;
			public List<PrestamosBlockChain> 	output;
		}
		
		public static class PrestamosBlockChain extends BcsPojo	{ 
			public laDetails			laDetails;
			public List<lendingClients>	lendingClients;
			public borrowingClients 	borrowingClients;
			
			public PrestamosBlockChain() {
				this.laDetails 		= new laDetails();
				this.lendingClients = new ArrayList<>();
			}
		}
		
		public static class laDetails extends BcsPojo {
			public String	LAID						= "";
			public String	Status						= "";
			public String	Symbol						= "";
			public String	LADate						= "";
			public String	SecurityType				= "";
			public String	LendingAgreementType		= "";
			public String	SettlType					= "";
			public String	BorrowingBrokerageHouseID	= "";
			public String	LendingBrokerageHouseID		= "";
			public String	LendingQty					= "";
			public String	MaturityDate				= "";
			public String	Yield						= "";
			public String	LegacyLAID					= "";
		}
		
		public static class lendingClients extends BcsPojo	{
			public String	LAID				= "";
			public String	LendingClientRUT	= "";
			public String	LendingClientFund	= "";
			public String	LendingClientQty	= "";
			public String	CollateralType		= "";
			public String	MandatoryCollateral	= "";
			public String	LCStatus			= "";
			public String	SignatureDate		= "";
			public String	SignatureUser		= "";
		}
		
		public static class borrowingClients extends BcsPojo {
			public String	LAID				= "";
			public String	BorrowingClientRUT	= "";
		}

		// -------------------------------------------------
		// LOGICA DEL SERVICIO
		// -------------------------------------------------
		public ConsultaListaPorFolioBlockChain_Salida logica(Connection conn, ConsultaListaPorFolioBlockChain_Entrada in) throws Exception {	
			
			ConsultaListaPorFolioBlockChain_Salida salida 	= new ConsultaListaPorFolioBlockChain_Salida();
			SQL sql 										= new SQL();
			Gson gson 										= new Gson();
			HashMap<String, Object> parametroEntrada 		= new HashMap<String, Object>();
			
			try {
				
				parametroEntrada.put("p_IdEmpresa"		, in.p_IdEmpresa);
				parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_PRESTAMOSBYID);
				List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
				parametroEntrada.clear();

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
						return salida;
					}

					connBC.setRequestProperty("BcsUser"			, salidaLoginBlockchain.p_Out_UserLogin);
					connBC.setRequestProperty("Authorization"	, salidaLoginBlockchain.p_Out_Token);
					connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

					JSONObject entrada 	= new JSONObject();
					JSONArray arrglo 	= new JSONArray();

					for(Folio folios : in.p_FolioExterno){

						arrglo.put(folios.p_laid);
					}
					
					entrada.put("laids", arrglo);
					
					OutputStream outputStream = connBC.getOutputStream();
					outputStream.write(entrada.toString().getBytes());
					outputStream.flush();

					if (connBC.getResponseCode() != 200) {
						if (connBC.getResponseCode() == 401) {
							salida.p_Out_Status 		= "";
							salida.p_Out_Mensaje 		= "";
							salida.p_Out_ContadorError 	= 1L;
							salida.p_Out_MensajeError 	= "NOTOKEN";
							return salida;
						} else {
							salida.p_Out_Status 		= "";
							salida.p_Out_Mensaje 		= "";
							salida.p_Out_ContadorError 	= 1L;
							salida.p_Out_MensajeError	= "ERROR";
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
								retrieveLasByIDsResponse consultaPrestamoResponse = gson.fromJson(obj.toString(), retrieveLasByIDsResponse.class);
							
								salida.p_Out_ContadorError 	= 0L;
								salida.p_lista_prestamos 	= consultaPrestamoResponse.output;
								return salida;
							}
							catch(Exception ex)
							{
								salida.p_Out_ContadorError = 0L; 
								return salida;
							}
						}
						else
						{
							if(obj.getString("status").equals("ERROR"))
							{
								try
								{
									String message 				= obj.getString("message");
									salida.p_Out_Status 		= obj.getString("status");
									salida.p_Out_Mensaje 		= "";
									salida.p_Out_ContadorError 	= 1L;
									salida.p_Out_MensajeError 	= message;
									return salida;
								}
								catch(Exception ex)
								{
									salida.p_Out_ContadorError += 1;
									break; 
								}
							}
						}
					}//FIN While output = br.readLine())

					connBC.disconnect();
				
				} else {
					salida.p_Out_Status 		= "";
					salida.p_Out_Mensaje 		= "";
					salida.p_Out_ContadorError 	= 1L;
					salida.p_Out_MensajeError 	= "SERVICIO_NO_POSEE_CONFIGURACION";
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
			return salida;
		}
		

		// -------------------------------------------------------------------------------------------
		// OBJETO DE TRANSPORTE - CALL
		// -------------------------------------------------------------------------------------------
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = Utils.getGsonBuilder();
			ConsultaListaPorFolioBlockChain_Entrada in = gson.fromJson(jin.toString(), ConsultaListaPorFolioBlockChain_Entrada.class);
			ConsultaListaPorFolioBlockChain_Salida out = logica(conn, in);

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
						"\"p_FolioExterno\": [{\"p_laid\":\"61-0000000010435\",\"p_laid\":\"61-0000000010435\"}]\r\n" +  
						" }";
				ConsultaListaPorFolioBlockChain_Entrada Objin = gson.fromJson(jsonCL, ConsultaListaPorFolioBlockChain_Entrada.class);

				ConsultaListaPorFolioBlockChain demo = new ConsultaListaPorFolioBlockChain();
				System.out.println("ENTRADA");
				System.out.println(Objin.toString());

				try {
					ConsultaListaPorFolioBlockChain_Salida ObjOut = demo.logica(con, Objin);
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