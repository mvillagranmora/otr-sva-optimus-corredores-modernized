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
import org.json.JSONException;
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
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificaPrestamoBlockChain implements JavaCall {
	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");

    // -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ModificaPrestamoBlockChain_Entrada {	
		public Long   				p_IdEmpresa;
		public String 				p_codCorrOpe;
		public String 				p_cantidad; 
		public String 				p_fechaLiq; 
		public String 				p_tipoModificacion;
		public List<Prestamista> 	p_prestamistas;
		public String 				p_rutAnular;
		public String 				p_tipoGarantia;
		public String 				p_origenModificacion; 
		public String 				p_tasa;
		public String 				p_id;
		public String				p_FechaVencimiento;
		
		public ModificaPrestamoBlockChain_Entrada() {
			p_prestamistas = new ArrayList<>();	
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class Prestamista {
		public String rut = "";
		public String codCorr = "";
		public String cantidad = "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class ModificaPrestamoBlockChain_Salida {
		public String	p_Out_Status;
		public String	p_Out_Mensaje;
		public Long		p_Out_ContadorError	= 0L;
		public String	p_Out_MensajeError;
		public List<MensajeSalida>	P_OUT_MENSAJES;

		public ModificaPrestamoBlockChain_Salida() {
			this.p_Out_Mensaje 		= "";
			this.p_Out_MensajeError = "";
			this.P_OUT_MENSAJES 	= new ArrayList<MensajeSalida>();
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
		public String 	DocumentoIdentidad;
		public String 	Nombres;
		public Long 	IdPersona;
		public Long 	IdCliente;
		public String 	NombreCompleto;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//APTMO: ANULA PRESTAMO, APTMTA: ANULA PRESTAMISTA, MPTMO: MODIFICA DATOS PRESTAMO
	public static ModificaPrestamoBlockChain_Salida llamadaBCModificacion (Connection conn,  HttpURLConnection connBC, Long IdEmpresa, String tasa, String tipoModificacion, String id, String codCorrOpe, String fechaVencimiento) throws JSONException, IOException{

		ModificaPrestamoBlockChain_Salida salida = new ModificaPrestamoBlockChain_Salida();

		//Construccion request de entrada
		JSONObject input 		= new JSONObject();
		JSONArray args	 		= new JSONArray();
		JSONObject arg 			= new JSONObject();
		JSONObject LADetails 	= new JSONObject();
		
		LADetails.put("Yield", tasa);
		if ( fechaVencimiento != null ) {
			LADetails.put("MaturityDate", fechaVencimiento);
		}
		arg.put("LADetails", LADetails);
		
		JSONObject RequestDetails = new JSONObject();
		RequestDetails.put("Action", tipoModificacion);
		RequestDetails.put("LAID", id);
		arg.put("RequestDetails", RequestDetails);
		
		args.put(arg);
		args.put(codCorrOpe);
		input.put("args", args);
		//FIN construccion request de entrada

		
		OutputStream outputStream = connBC.getOutputStream();
		outputStream.write(input.toString().getBytes());
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
			JSONObject obj = new JSONObject(output);
			if(obj.getString("status").equals("SUCCESS")) {
				try {
					
					salida.p_Out_Status = obj.getString("status");
					salida.p_Out_ContadorError = 0L;
					salida.p_Out_MensajeError = "UI_BC_PRESTAMO_MODIFICADO_OK";
					salida.p_Out_ContadorError = 0L;
					salida.p_Out_Mensaje = "";
					break;
				
				} catch(Exception ex) {
					salida.p_Out_Status = "NOFOUND";
					break;
				}
			}
			else {
				if(obj.getString("status").equals("ERROR")) {
					try {
						String message = obj.getString("message");
						JSONArray messageArray = new JSONArray(message);
						
						for (int i = 0, size = messageArray.length(); i < size; i++) {
							String messageInArray = messageArray.getString(i);
							
							ConstantesCustodia constantesCustodia 		= new ConstantesCustodia();
							HashMap<String, String> valoresDiccionario 	= new HashMap<String, String>();
							
							String vCodigo = constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", ""));
							String mensaje = Diccionario.getMensaje(conn, 1L, vCodigo, valoresDiccionario);
							
							salida.p_Out_Mensaje 	   += (i > 0 ? ", " : "") + mensaje;
							salida.p_Out_ContadorError += 1L;
							salida.p_Out_MensajeError   = vCodigo;
						}
						
						salida.p_Out_Status 			= obj.getString("status");
						
						return salida; 
					} catch (Exception ex) {
						HashMap<String, String> valoresDiccionario = new HashMap<String, String>();
						salida.p_Out_Status 		= "ERROR";
						salida.p_Out_Mensaje 		= obj.getString("message");
						salida.p_Out_ContadorError 	= 1L;
						salida.p_Out_MensajeError 	= "UI_BC_ERROR_MODIFICAR_PRESTAMO";
						return salida;
					}
				}
			}
		}
		connBC.disconnect();
		return salida;
	}
	
	public static ModificaPrestamoBlockChain_Salida llamadaBCAnulacion (Connection conn, HttpURLConnection connBC, String cantidad, String tasa, String fechaLiq, 
			String rutAnular, String tipoModificacion, String id, List<Prestamista> p_prestamistas, String codCorrOpe, String tipoGarantia, Long idEmpresa) throws Exception {
		
		ModificaPrestamoBlockChain_Salida salida = new ModificaPrestamoBlockChain_Salida();

		//********************  Construccion request de entrada
		JSONObject input 		= new JSONObject();
		JSONArray args 			= new JSONArray();
		JSONObject arg 			= new JSONObject();
		JSONObject LADetails 	= new JSONObject();
		
		LADetails.put("LendingQty", cantidad);

		if(Double.parseDouble(tasa) > 0)
			LADetails.put("Yield", tasa);
	
		if(!fechaLiq.equals(""))
			LADetails.put("MaturityDate", fechaLiq);
		
		if("".equals(rutAnular)) //SOLO SI ES ANULACION DE PRESTAMO, MODIFICA DATOS DEL PRESTAMO
			arg.put("LADetails", LADetails);
	
		JSONObject RequestDetails = new JSONObject();
		RequestDetails.put("Action", tipoModificacion);
		RequestDetails.put("LAID", id);
		arg.put("RequestDetails", RequestDetails);
		
		JSONArray LendingClients = new JSONArray();
		
		ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
		List<Folio>	folios																						= new ArrayList<>();
		folios.add( new Folio(id) );
		consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= idEmpresa;
		consultaPrestamoPorLAIDEntrada.p_FolioExterno	= folios;
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
		
		if ( consultaPrestamoPorLAIDSalida.p_Out_ContadorError > 0 || consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream().count() == 0 ) {
			salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_NO_ENCONTRADO", new HashMap<String, String>(), conn, idEmpresa) );
			return salida;
		}
		
		if ( consultaPrestamoPorLAIDSalida.p_lista_prestamos.get(0).lendingClients == null ) {
			salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_PRESTAMISTA_NO_ENCONTRADO", new HashMap<String, String>(), conn, idEmpresa) );
			return salida;
		}
		
		List<ConsultaListaPorFolioBlockChain.lendingClients> clientePrestamista = consultaPrestamoPorLAIDSalida.p_lista_prestamos.get(0).lendingClients
				.parallelStream()
				.filter( lending_client -> lending_client.LendingClientRUT.trim().equalsIgnoreCase( rutAnular ) )
				.collect( Collectors.toList() );
		
		if ( clientePrestamista.size() == 0 ) {
			salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_PRESTAMISTA_NO_ENCONTRADO", new HashMap<String, String>(), conn, idEmpresa) );
			return salida;
		}
		
		if ( consultaPrestamoPorLAIDSalida.p_lista_prestamos
				.get(0)
				.lendingClients
				.size() > 1 ) {  //ANULA PRESTAMISTA ESPECIFICO
			for ( ConsultaListaPorFolioBlockChain.lendingClients clientePrestamistaItem : consultaPrestamoPorLAIDSalida.p_lista_prestamos.get(0).lendingClients ) {
				JSONObject LendingClientsObject = new JSONObject();
				if ( clientePrestamistaItem.LendingClientRUT.trim().equalsIgnoreCase( rutAnular ) ) continue;
				LendingClientsObject.put("LendingClientQty", clientePrestamistaItem.LendingClientQty);
				LendingClientsObject.put("MandatoryCollateral"		, clientePrestamistaItem.MandatoryCollateral);
				LendingClientsObject.put("LendingClientRUT"			, clientePrestamistaItem.LendingClientRUT);
				LendingClientsObject.put("CollateralType"			, clientePrestamistaItem.CollateralType);
				LendingClientsObject.put("LendingClientFund"		, clientePrestamistaItem.LendingClientFund);
				LendingClientsObject.put("LendingBrokerageHouseID"	, codCorrOpe);
				LendingClients.put(LendingClientsObject);
			}
			arg.put("LendingClients", LendingClients);
		}
		else { //ANULA TODO REGISTRO DE PRESTAMISTA
			JSONObject LendingClient = new JSONObject();
			LendingClient.put("LendingClientQty", "0");
			
			LendingClient.put("MandatoryCollateral", "TRUE");
			LendingClients.put(LendingClient);
			arg.put("LendingClients", LendingClients);
		}
		
		
		args.put(arg);
		args.put(codCorrOpe);
		input.put("args", args);
		//******************** FIN construccion request de entrada

		OutputStream outputStream = connBC.getOutputStream();
		outputStream.write(input.toString().getBytes());
		outputStream.flush();
		
		if (connBC.getResponseCode() != 200) {
			if (connBC.getResponseCode() == 401) {
				salida.p_Out_Status 		= "";
				salida.p_Out_Mensaje 		= "";
				salida.p_Out_ContadorError 	= 1L;
				salida.p_Out_MensajeError 	= "NOTOKEN";
				GeneraMensajeSalida(conn, idEmpresa, "UI_ADVENTENCIA_ANULAR_PRESTAMISTA", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
				return salida;
			} else {
				salida.p_Out_Status 		= "";
				salida.p_Out_Mensaje 		= "";
				salida.p_Out_ContadorError 	= 1L;
				salida.p_Out_MensajeError 	= "ERROR";
				GeneraMensajeSalida(conn, idEmpresa, "UI_ADVENTENCIA_ANULAR_PRESTAMISTA", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
				return salida;
			}
		}
	
		BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));		
		String output;
		
		while ((output = br.readLine()) != null) {
			
			ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
			JSONObject obj 							= new JSONObject(output);
			
			if(obj.getString("status").equals("SUCCESS")) {
				try
				{
					JSONObject outputResponse 	= obj.getJSONObject("output");
					JSONObject response 		= outputResponse.getJSONObject("response");
					String info 				= response.getString("info");								
					String vCodigo 				= constantesCustodia.codigoErroresBlockChain.get(info.replace(".", ""));
					
					salida.p_Out_Status 		= obj.getString("status");
					salida.p_Out_ContadorError 	= 0L;
					GeneraMensajeSalida(conn, idEmpresa, rutAnular.equals("") ? "UI_BC_OK_ANULAR_PRESTAMOS" : "UI_BC_OK_ANULAR_PRESTAMISTA", MensajeSalida.MENSAJE_INFORMACION, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
					break;
				}
				catch(Exception ex) {
					salida.p_Out_Status = "NOFOUND";
					break;
				}
			}
			else {
				if( obj.getString("status").equals("ERROR") ) {
					
					try{
						String message 			= obj.getString("message");
						JSONArray messageArray 	= new JSONArray(message);
						
						for (int i = 0, size = messageArray.length(); i < size; i++) {
							String messageInArray 		= messageArray.getString(i);
							String vCodigo 				= constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", ""));
							salida.p_Out_ContadorError += 1L;
							GeneraMensajeSalida(conn, idEmpresa, vCodigo, MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
						}
						salida.p_Out_MensajeError	= message;
						salida.p_Out_Status 		= obj.getString("status");
						break;
					} catch ( Exception ex ) {
						salida.p_Out_ContadorError 	= 1L;
						salida.p_Out_MensajeError 	= obj.has("message") ? "BLOCKCHAIN: " + obj.getString("message") : "BLOCKCHAIN: ERROR => " + ex.getMessage();
						GeneraMensajeSalida(conn, idEmpresa, rutAnular.equals("") ? "UI_ERROR_ANULAR_PRESTAMOS" : "UI_ADVENTENCIA_ANULAR_PRESTAMISTA", MensajeSalida.MENSAJE_ADVERTENCIA, new HashMap<String, String>(), salida.P_OUT_MENSAJES);
						break;
					}
				}
			}
		}
		connBC.disconnect();
		return salida;
	}
	
	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public ModificaPrestamoBlockChain_Salida logica(Connection conn, ModificaPrestamoBlockChain_Entrada in) throws Exception {
		SQL sql 									= new SQL();
		ModificaPrestamoBlockChain_Salida salida 	= new ModificaPrestamoBlockChain_Salida();

		try {
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_MODIFICAR_PRESTAMO);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);

			if (!SelectConfiguracion.isEmpty()) {
				URL url = new URL(SelectConfiguracion.get(0).Valor);

				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json"); 

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

				connBC.setRequestProperty("BcsUser", salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization", salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

				if (ConstantesBCK.ANULAR_APTMO.equals(in.p_origenModificacion)) { //ANULACION PRESTAMO
					salida = llamadaBCAnulacion(conn, connBC, in.p_cantidad, in.p_tasa, in.p_fechaLiq, "", in.p_tipoModificacion, in.p_id, in.p_prestamistas, in.p_codCorrOpe, in.p_tipoGarantia, in.p_IdEmpresa);
				}
				else if (ConstantesBCK.ANULAR_APTMTA.equals(in.p_origenModificacion)) { //ANULACION DE PRESTAMISTA
					salida = llamadaBCAnulacion(conn, connBC, in.p_cantidad, in.p_tasa, in.p_fechaLiq, in.p_rutAnular, in.p_tipoModificacion, in.p_id, in.p_prestamistas, in.p_codCorrOpe, in.p_tipoGarantia, in.p_IdEmpresa);
				}
				else if (ConstantesBCK.ANULAR_MPTMO.equals(in.p_origenModificacion)) { //MODIFICACION DE PRESTAMO
					salida = llamadaBCModificacion(conn, connBC, in.p_IdEmpresa, in.p_tasa, in.p_tipoModificacion, in.p_id, in.p_codCorrOpe, in.p_FechaVencimiento);
				}
				return salida;
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
	
	private static void GeneraMensajeSalida(Connection con, Long idEmpresa, String codigoMensaje, String tipoMensaje, HashMap<String, String> valoresDiccionarioMensaje, List<MensajeSalida> P_OUT_MENSAJES ) throws Exception {
		
		codigoMensaje 	= codigoMensaje == null ? "UI_BC_MENSAJE_NO_IDENTIFICADO" : codigoMensaje;
		String mensaje	=	Diccionario.getMensaje(con, idEmpresa, codigoMensaje)
							.msg();
		mensaje			= mensaje == null ? "" : mensaje;
		
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L, tipoMensaje, codigoMensaje, mensaje));
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificaPrestamoBlockChain_Entrada in = gson.fromJson(jin.toString(), ModificaPrestamoBlockChain_Entrada.class);
		ModificaPrestamoBlockChain_Salida out = logica(conn, in);

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
					"\"p_codCorrOpe\": \"61\",\r\n" + 
					"\"p_cantidad\": \"1000\",\r\n" + 
					"\"p_fechaLiq\": \"2019-07-01\",\r\n" + 
					"\"p_tipoModificacion\": \"MODIFYLENDINGAGREEMENT\",\r\n" + 
					"\"prestamistas\": [{\"rut\":\"15731790-3\",\"codCorr\":\"\",\"cantidad\":\"1000\"}],\r\n" +  
					"\"p_rutAnular\": \"15731790-3\",\r\n" + 
					"\"p_tipoGarantia\": \"C\",\r\n" + 
					"\"p_origenModificacion\": \"APTMO\",\r\n" + 
					"\"p_tasa\": \"1\",\r\n" + 
					"\"p_id\": \"61-0000000019696\"\r\n" + 
					" }";

			ModificaPrestamoBlockChain_Entrada Objin = gson.fromJson(jsonCL, ModificaPrestamoBlockChain_Entrada.class);

			ModificaPrestamoBlockChain demo = new ModificaPrestamoBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				ModificaPrestamoBlockChain_Salida ObjOut = demo.logica(con, Objin);
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
	
	//**********************************************************************************************************************************
	//**********************************************************************************************************************************
}

