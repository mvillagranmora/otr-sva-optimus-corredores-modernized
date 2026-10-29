package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import cl.bcs.custodia.pojo.ConstantesCustodia;
import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.SelectConfiguracion;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ExtenderPlazoPrestamoBlockChain implements JavaCall  {

	static final Logger logger = LogManager.getLogger(ExtenderPlazoPrestamoBlockChain.class);
	
	private final String SQL_SELECT_CONFIGURACION			= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	public static class ExtenderPlazoPrestamoBlockChain_Entrada extends BcsPojo {
		public Long 			p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_FechaSolicitud;
	    public DatosPrestamo	p_DatosPrestamo;
	}
	
	public static class ExtenderPlazoPrestamoBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ExtenderPlazoPrestamoBlockChain_Salida() {
			this.P_OUT_MENSAJES			= new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
		}
	}
	
	public static class DatosPrestamo extends BcsPojo {
		public String	FolioSistemaExterno;
		public String	FechaVencimiento;
		public String	Tasa;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ExtenderPlazoPrestamoBlockChain_Salida logica( Connection conn, ExtenderPlazoPrestamoBlockChain_Entrada in ) throws Exception {
		
		logger.info("INICIO::logica");
		long inicio = System.currentTimeMillis();
		
		ExtenderPlazoPrestamoBlockChain_Salida salida 	= new ExtenderPlazoPrestamoBlockChain_Salida();
		HashMap<String,String> mapMsg 					= new HashMap<>();
		HashMap<String, Object> parametroQuery 			= new HashMap<String, Object>();
		SQL sql 										= new SQL();
		
		try {
			
			LoginBlockchain loginBlockchain 								= new LoginBlockchain();
			LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();

			entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;

			LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);

			if (salidaLoginBlockchain.p_Out_ContadorError > 0) {
				mapMsg.clear();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,salidaLoginBlockchain.p_Out_MensajeError, mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = salidaLoginBlockchain.p_Out_ContadorError;
				return salida;
			}
			
			parametroQuery.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroQuery.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_RENOVAR_PRESTAMO);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroQuery, SelectConfiguracion.class);
			parametroQuery.clear();
			
			if (SelectConfiguracion.isEmpty()) {
				mapMsg.clear();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_SERVICIO_NO_POSEE_CONFIGURACION", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			/********** CONSUME SERVICIO RENOVAR PRESTAMO BLOCKCHAIN **********/
			
			URL url = new URL(SelectConfiguracion.get(0).Valor);
			HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
			connBC.setDoOutput(true);
			connBC.setRequestMethod("POST");
			connBC.setRequestProperty("Content-Type"	, "application/json");
			connBC.setRequestProperty("BcsUser"			, salidaLoginBlockchain.p_Out_UserLogin);
			connBC.setRequestProperty("Authorization"	, salidaLoginBlockchain.p_Out_Token);
			connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);
			
			JSONObject entrada 		= new JSONObject();
			JSONArray argsArreglo 	= new JSONArray();
			JSONObject argsObjeto 	= new JSONObject();
			// LADetails
			JSONObject LADetails = new JSONObject();
			LADetails.put( "MaturityDate"	, in.p_DatosPrestamo.FechaVencimiento );
			LADetails.put( "Yield"			, in.p_DatosPrestamo.Tasa );
			// RequestDetails
			JSONObject RequestDetails = new JSONObject();
			RequestDetails.put("Action"		, ConstantesBCK.ACTION_REQUESTEXTENSION);
			RequestDetails.put("RequestDate", in.p_FechaSolicitud);
			RequestDetails.put("LAID"		, in.p_DatosPrestamo.FolioSistemaExterno);
			
			argsObjeto.put("LADetails", LADetails);
			argsObjeto.put("RequestDetails", RequestDetails);
			
			argsArreglo.put(argsObjeto);
			argsArreglo.put(salidaLoginBlockchain.p_Out_BrokerageID);
			
			entrada.put("args", argsArreglo);
			
			OutputStream outputStream = connBC.getOutputStream();
			outputStream.write(entrada.toString().getBytes());
			outputStream.flush();
			
			if ( connBC.getResponseCode() != 200 ) {
				if ( connBC.getResponseCode() == 401 ) {
					mapMsg.clear();
					salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA, "NOTOKEN", mapMsg, conn, in.p_IdEmpresa ) );
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					mapMsg.clear();
					salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA,"ERROR", mapMsg, conn, in.p_IdEmpresa ) );
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				}
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));

			String output;
			
			while ( (output = br.readLine() ) != null ) {
				JSONObject obj = new JSONObject(output);

				if ( obj.getString("status").equals("SUCCESS") ) {
					try {
						mapMsg.clear();
						salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L,MensajeSalida.MENSAJE_OK, "UI_BC_PRESTAMO_EXTENDER_PLAZO_OK", mapMsg, conn, in.p_IdEmpresa ) );
						return salida;
					} catch (Exception ex) {
						mapMsg.clear();
						salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L,MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_EXTENDER_PLAZO_ERROR", mapMsg, conn, in.p_IdEmpresa ) );
						salida.P_OUT_ERRORESCONTADOR = 1L;
						return salida;
					}
				} else {
					if ( obj.getString("status").equals("ERROR") ) {
						try {
							String message 							= obj.getString("message");
							JSONArray messageArray 					= new JSONArray(message);
							ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
							
							for (int i = 0, size = messageArray.length(); i < size; i++) {
								String messageInArray = messageArray.getString(i);
								String vCodigo = constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", ""));
								salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA, vCodigo, mapMsg, conn, in.p_IdEmpresa ) );
							}
							mapMsg.clear();
							salida.P_OUT_ERRORESCONTADOR = 1L;
							return salida;
						} catch ( Exception ex ) {
							mapMsg.clear();
							salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_EXTENDER_PLAZO_ERROR", mapMsg, conn, in.p_IdEmpresa ) );
							salida.P_OUT_ERRORESCONTADOR = 1L;
							return salida;
						}
					}
				}
			}
			
			
		} catch ( MalformedURLException e ) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa ) );
		} catch ( IOException e ) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add( new MensajeSalida( 1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa ) );
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::logica", fin - inicio);
		
		return salida;
	}

}
