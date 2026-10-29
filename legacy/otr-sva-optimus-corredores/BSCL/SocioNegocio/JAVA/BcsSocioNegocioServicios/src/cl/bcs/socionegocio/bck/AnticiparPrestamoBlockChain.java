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
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;

import cl.bcs.custodia.pojo.ConstantesCustodia;
import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnticiparPrestamoBlockChain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION							= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class AnticiparPrestamoBlockChain_Entrada extends BcsPojo {	
		public Long								p_IdEmpresa;
		public String							p_Fecha; //el servicio BlockChain pide 2 Fechas, por eso pido dos parametros de fecha, se espera formato yyyy-MM-dd
		public String							p_FechaLiquidacion;   
		public List<FolioPrestamoBlockChain>	p_FoliosPrestamoBlockChain	;
		public String 							esAnticipoParcial;
		public BigDecimal 						Cantidad;
		public String 							p_DocumentoIdentidadPrestamista;
		
		public AnticiparPrestamoBlockChain_Entrada() {
			this.p_FoliosPrestamoBlockChain = new ArrayList<>();	
		}
	}
	
	public static class FolioPrestamoBlockChain extends BcsPojo {
		public String	FolioPrestamoBlockChain = "";
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class AnticiparPrestamoBlockChain_Salida extends BcsPojo {
		public List<Status>			P_OUT_STATUS			;
		public List<MensajeSalida>	P_OUT_MENSAJES			;
		public Long					P_OUT_ERRORESCONTADOR	;
		
		public AnticiparPrestamoBlockChain_Salida()
		{	
			this.P_OUT_STATUS	= new ArrayList<Status>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
		}
	}
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectConfiguracion extends BcsPojo {
        public String	Valor;
        public String	Descripcion;
	}

	public static class Status extends BcsPojo {
		public Long		IdInterno	; 
		public String 	Status		;	
		
		public Status() {
			IdInterno	=	0L; 
			Status		= 	"";
		}
	}
	
	public static class SelectDocumentoIdentidad extends BcsPojo {
		public String DocumentoIdentidad;
		public String Nombres;
		public Long IdPersona;
		public Long IdCliente;
		public String NombreCompleto;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public AnticiparPrestamoBlockChain_Salida logica(Connection conn, AnticiparPrestamoBlockChain_Entrada in) throws Exception
	{
		SQL sql 									= new SQL();
		AnticiparPrestamoBlockChain_Salida salida 	= new AnticiparPrestamoBlockChain_Salida();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		try
		{
			
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			if(in.esAnticipoParcial.equals("S")) {
				parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_ANTICIPO_PRESTAMO_PARCIAL); 
			}else if(in.esAnticipoParcial.equals("N")) {
				parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_ANTICIPO_PRESTAMO_TOTAL); 
			}
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
			parametroEntrada.clear();
			
			if(!SelectConfiguracion.isEmpty())
			{
				URL url = new URL(SelectConfiguracion.get(0).Valor);
				
				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");

				LoginBlockchain loginBlockchain 								= new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();
				
				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;
				
				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);
				
				if(salidaLoginBlockchain.p_Out_ContadorError > 0)
				{
					Status objStatus = new Status();
					objStatus.IdInterno = 0L;
					objStatus.Status	= "";
					salida.P_OUT_STATUS.add(objStatus); 
					salida.P_OUT_ERRORESCONTADOR = salidaLoginBlockchain.p_Out_ContadorError;
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, salidaLoginBlockchain.p_Out_MensajeError, new HashMap<String, String>(), conn, in.p_IdEmpresa) );
					return salida;
				}
				
				connBC.setRequestProperty("BcsUser"			, salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization"	, salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

				//se paso fecha de operacion
				if(in.p_Fecha.equals("")) {
					
					Status objStatus = new Status();
					objStatus.IdInterno = 0L;
					objStatus.Status	= "";
					salida.P_OUT_STATUS.add(objStatus); 
					salida.P_OUT_ERRORESCONTADOR += 1L;
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FECHA_DE_OPERACION", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
				}

				//se paso fecha de liquidacion (que es la fecha en la que queda "vencido" el prestamo.
				if(in.p_FechaLiquidacion.equals("")) {
					
					Status objStatus = new Status();
					objStatus.IdInterno = 0L;
					objStatus.Status	= "";
					salida.P_OUT_STATUS.add(objStatus); 
					salida.P_OUT_ERRORESCONTADOR += 1L;
					salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FECHA_DE_LIQUIDACION", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
				}
				
				//Puede continuar con el proceso
				if(salida.P_OUT_ERRORESCONTADOR==0L)
				{					
					
					Long lngInterno = 0L;
					for(FolioPrestamoBlockChain objFolioBC : in.p_FoliosPrestamoBlockChain) {
						
						++lngInterno; //para indicar en que nro de la "fila" de los folio enviado se produce el error
						//se paso el Folio de Prestamo BlockChain
						if(!objFolioBC.FolioPrestamoBlockChain.equals("")) {
							
							JSONArray arreglo 			= new JSONArray();
							JSONObject entrada 			= new JSONObject();
							JSONObject objeto 			= new JSONObject();
							JSONObject RequestDetails 	= new JSONObject();
							
							if(in.esAnticipoParcial.equals("S")) {	
								// LADetails
								JSONObject LADetails = new JSONObject();
								LADetails.put("LendingQty",in.Cantidad.intValue() + "");								
								objeto.put("LADetails", LADetails);
								
								//RequestDetails
								RequestDetails.put("Action", "REQUESTPARTIALTERMINATION");
								
								//LendingClients
								ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
								ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
								List<Folio>	folios																						= new ArrayList<>();
								folios.add(new Folio(objFolioBC.FolioPrestamoBlockChain));
								consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= in.p_IdEmpresa;
								consultaPrestamoPorLAIDEntrada.p_FolioExterno	= folios;
								ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
								
								if ( consultaPrestamoPorLAIDSalida.p_Out_ContadorError > 0 || consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream().count() == 0 ) {
									salida.P_OUT_ERRORESCONTADOR += 1L;
									salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_NO_ENCONTRADO", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									return salida;
								}
								
								List<ConsultaListaPorFolioBlockChain.lendingClients> clientePrestamista = consultaPrestamoPorLAIDSalida.p_lista_prestamos.get(0).lendingClients
										.parallelStream()
										.filter( lending_client -> lending_client.LendingClientRUT.trim().equalsIgnoreCase(in.p_DocumentoIdentidadPrestamista.trim()) )
										.collect( Collectors.toList() );
								
								if ( clientePrestamista.stream().count() == 0 ) {
									salida.P_OUT_ERRORESCONTADOR = 1L;
									salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_PRESTAMISTA_NO_ENCONTRADO", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									return salida;
								}
								
								JSONArray LendingClients 		= new JSONArray();
								JSONObject LendingClientsObject = new JSONObject();
								
								LendingClientsObject.put("MandatoryCollateral", clientePrestamista.get(0).MandatoryCollateral);
								LendingClientsObject.put("LendingClientQty", in.Cantidad.intValue() + "");
								LendingClientsObject.put("LendingClientRUT", clientePrestamista.get(0).LendingClientRUT);
								LendingClientsObject.put("CollateralType", clientePrestamista.get(0).CollateralType);
								LendingClientsObject.put("LendingClientFund", clientePrestamista.get(0).LendingClientFund);
								LendingClientsObject.put("LendingBrokerageHouseID", salidaLoginBlockchain.p_Out_BrokerageID);
								
								LendingClients.put(LendingClientsObject);
								objeto.put("LendingClients", LendingClients);
								
							}else if(in.esAnticipoParcial.equals("N")) {
								// RequestDetails
								RequestDetails.put("Action", "REQUESTTERMINATION");				
							}
							
							RequestDetails.put("LAID", objFolioBC.FolioPrestamoBlockChain);
							RequestDetails.put("RequestDate", in.p_Fecha);
							objeto.put("RequestDetails", RequestDetails);
						
							arreglo.put(objeto);
							arreglo.put(salidaLoginBlockchain.p_Out_BrokerageID);
							entrada.put("args", arreglo);
							OutputStream outputStream = connBC.getOutputStream();
							outputStream.write(entrada.toString().getBytes());
							outputStream.flush();
							
							if (connBC.getResponseCode() != 200)
							{
								if (connBC.getResponseCode() == 401)
								{
									Status objStatus = new Status();
									objStatus.IdInterno = lngInterno;
									objStatus.Status	= "";
									salida.P_OUT_STATUS.add(objStatus); 
									salida.P_OUT_ERRORESCONTADOR = 1L;
									salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_SIN_TOKEN_O_SOLICITA_RENOVAR_TOKEN", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									return salida;
								} 
								else
								{
									Status objStatus = new Status();
									objStatus.IdInterno = lngInterno;
									objStatus.Status	= "";
									salida.P_OUT_STATUS.add(objStatus); 
									salida.P_OUT_ERRORESCONTADOR = 1L;
									salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FECHA_DE_LIQUIDACION", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
									return salida;
								}
								//else throw new RuntimeException("Failed: HTTP error code: " + connBC.getResponseCode());
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
										JSONObject out = obj.getJSONObject("output");
										JSONObject response = out.getJSONObject("response");
										
										Status objStatus = new Status();
										objStatus.IdInterno = lngInterno;
										objStatus.Status	= response.getString("status");
										salida.P_OUT_STATUS.add(objStatus); 
										salida.P_OUT_ERRORESCONTADOR = 0L;
										break; //return salida;
									}
									catch(Exception ex)
									{
										Status objStatus = new Status();
										objStatus.IdInterno = lngInterno;
										objStatus.Status	= "NOFOUND";
										salida.P_OUT_STATUS.add(objStatus); 
										salida.P_OUT_ERRORESCONTADOR = 0L; //en este punto diria que no encontro el prestamo para anticiparlo, pero no se si debe ser reportado como error
										break; //return salida;
									}
								}
								else
								{
									if(obj.getString("status").equals("ERROR"))
									{
										try
										{
											String message = obj.getString("message");
											
											ConstantesCustodia constantesCustodia = new ConstantesCustodia();
											String vCodigo = constantesCustodia.codigoErroresBlockChain.get(message.replace(".", ""));
											
											HashMap<String,String> valoresDiccionario = new HashMap<String,String>();
											String mensaje = Diccionario.getMensaje(conn,in.p_IdEmpresa, vCodigo, valoresDiccionario);
											mensaje = vCodigo == null ? message : mensaje; 
											Status objStatus = new Status();
											objStatus.IdInterno = lngInterno;
											objStatus.Status	= obj.getString("status");
											salida.P_OUT_STATUS.add(objStatus); 
											salida.P_OUT_ERRORESCONTADOR += 1;
											salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, vCodigo, new HashMap<String, String>(), conn, in.p_IdEmpresa) );
											break; // return salida;
										}
										catch(Exception ex)
										{
											Status objStatus = new Status();
											objStatus.IdInterno = lngInterno;
											objStatus.Status	= "";
											salida.P_OUT_STATUS.add(objStatus); 
											salida.P_OUT_ERRORESCONTADOR += 1;
											salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
											break; //return salida;
										}
									}
								}
							}//FIN While output = br.readLine())
							br.close();
							outputStream.close();
							connBC.disconnect();
							connBC = (HttpURLConnection) url.openConnection();
							connBC.setDoOutput(true);
							connBC.setRequestMethod("POST");
							connBC.setRequestProperty("Content-Type", "application/json");
							connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
							connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
							connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
							
						}
						else 
						{
							Status objStatus = new Status();
							objStatus.IdInterno = lngInterno;
							objStatus.Status	= "";
							salida.P_OUT_STATUS.add(objStatus); 
							salida.P_OUT_ERRORESCONTADOR = 1L;
							salida.P_OUT_MENSAJES.add( new MensajeSalida(lngInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FOLIO_PRESTAMOBC", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
						}
						
					}//Fin: For ciclo Folios BlockChain
					connBC.disconnect();
					return salida;
				}
				else
				{
					Status objStatus = new Status();
					objStatus.IdInterno = 0L;
					objStatus.Status	= "";
					salida.P_OUT_STATUS.add(objStatus); 
					return salida;
				}
			}
			else
			{
				Status objStatus = new Status();
				objStatus.IdInterno = 0L;
				objStatus.Status	= "";
				salida.P_OUT_STATUS.add(objStatus); 
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "SERVICIO_NO_POSEE_CONFIGURACION", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
				return salida;
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
		}
		catch (IOException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
		}
		Status objStatus = new Status();
		objStatus.IdInterno = 0L;
		objStatus.Status	= "";
		salida.P_OUT_STATUS.add(objStatus); 
		return salida;
	}

	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		AnticiparPrestamoBlockChain_Entrada in = gson.fromJson(jin.toString(), AnticiparPrestamoBlockChain_Entrada.class);
		AnticiparPrestamoBlockChain_Salida out = logica(conn, in);
		
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

			String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_Fecha\": \"2019-05-31\",\r\n" + 
					"	\"p_FechaLiquidacion\": \"2019-06-07\",\r\n" + 
					"	\"p_FoliosPrestamoBlockChain\": [ { \"FolioPrestamoBlockChain\": \"61-0000000018607\"}\r\n" + 
					"	]\r\n" + 
					"}";
			
			AnticiparPrestamoBlockChain_Entrada Objin = gson.fromJson(jsonCL, AnticiparPrestamoBlockChain_Entrada.class);

			AnticiparPrestamoBlockChain demo = new AnticiparPrestamoBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				AnticiparPrestamoBlockChain_Salida ObjOut = demo.logica(con, Objin);
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
