package cl.bcs.socionegocio.bck;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaOtrosPrestamosBlockChain implements JavaCall  {

	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ConsultaOtrosPrestamosBlockChain_Entrada extends BcsPojo {
		public Long 	p_IdEmpresa 		= 1L;
		public String	p_FechaOperacion 	= "";
	}
	
	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class ConsultaOtrosPrestamosBlockChain_Salida extends BcsPojo {
		public String 	p_Out_Status;
		public String 	p_Out_Mensaje;
		public Long 	p_Out_ContadorError;
		public String 	p_Out_MensajeError;
		public List<OL> p_OLs;
		
		public ConsultaOtrosPrestamosBlockChain_Salida() {
			this.p_OLs					= new ArrayList<>();
			this.p_Out_ContadorError	= 0L;
		}
	}
	
	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class SelectConfiguracion extends BcsPojo {
		public String Valor;
		public String Descripcion;
	}
	
	public static class OL extends BcsPojo {
		public String AssignedQty;
		public String BorrowingBrokerageHouseID;
		public String CumulativeQty;
		public String LoanDate;
		public String LoanQty;
		public String LoanType;
		public String OLID;
		public String QtyToBeAssigned;
		public String SecuritySubType;
		public String SecurityType;
		public String SettlType;
		public String Symbol;
	}
		

	public ConsultaOtrosPrestamosBlockChain_Salida logica( Connection conn, ConsultaOtrosPrestamosBlockChain_Entrada entrada ) throws Exception {
		
		ConsultaOtrosPrestamosBlockChain_Salida salida 	= new ConsultaOtrosPrestamosBlockChain_Salida();
		SQL sql 										= new SQL();
		HashMap<String, Object> parametroEntrada 		= new HashMap<String, Object>();
		
		try {
			
			parametroEntrada.put( "p_IdEmpresa"		, entrada.p_IdEmpresa );
			parametroEntrada.put( "p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_OTROS_PRESTAMOS ); 
			List<SelectConfiguracion> SelectConfiguracion = sql.query( conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class );
			
			if ( SelectConfiguracion.isEmpty() ) {
				salida.p_Out_Status 		= "";
				salida.p_Out_Mensaje 		= "";
				salida.p_Out_ContadorError 	= 1L;
				salida.p_Out_MensajeError 	= "SERVICIO_NO_POSEE_CONFIGURACION";
				return salida;
			}
			
			LoginBlockchain loginBlockchain 								= new LoginBlockchain();
			LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();
			entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;
			LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn,entradaLoginBlockchain);
			
			if ( salidaLoginBlockchain.p_Out_ContadorError > 0 ) {
				salida.p_Out_Status 		= "";
				salida.p_Out_Mensaje 		= "";
				salida.p_Out_ContadorError 	= salidaLoginBlockchain.p_Out_ContadorError;
				salida.p_Out_MensajeError 	= salidaLoginBlockchain.p_Out_MensajeError;
				return salida;
			}
			
			URL url = new URL( SelectConfiguracion.get(0).Valor );
			HttpURLConnection connBC = ( HttpURLConnection ) url.openConnection();
			connBC.setDoOutput( true );
			connBC.setRequestMethod( "POST" );
			connBC.setRequestProperty( "Content-Type"		, "application/json" );
			connBC.setRequestProperty( "BcsUser"			, salidaLoginBlockchain.p_Out_UserLogin );
			connBC.setRequestProperty( "Authorization"		, salidaLoginBlockchain.p_Out_Token );
			connBC.setRequestProperty( "BcsAuthorization"	, salidaLoginBlockchain.p_Out_Token );

			JSONArray args 		= new JSONArray();
			JSONObject input 	= new JSONObject();
			
			args.put( "false" );
			args.put( entrada.p_FechaOperacion );
			args.put( entrada.p_FechaOperacion );
			
			input.put( "args", args );
			
			OutputStream outputStream = connBC.getOutputStream();
			outputStream.write( input.toString().getBytes() );
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

				if (obj.getString("status").equals("SUCCESS")) {
					try {
						
						Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
								.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
								.serializeNulls().create();
						String outputString 	= obj.getString( "output" );
						
						if ( !outputString.contains("[") ) {
							return salida;
						}
						
						JSONArray outputArray 	= new JSONArray( outputString );
						
						for ( int i = 0, size = outputArray.length(); i < size; i++ ) {
							String olJson 					= outputArray.getString(i);
							OL ol = gson.fromJson( olJson, OL.class );
							salida.p_OLs.add( ol );
						}
						
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
		}
		catch ( Exception ex ) {
			
		}
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultaOtrosPrestamosBlockChain_Entrada in = gson.fromJson( jin, ConsultaOtrosPrestamosBlockChain_Entrada.class );
		ConsultaOtrosPrestamosBlockChain_Salida out = logica( conn, in );
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_FechaOperacion\":\"2023-06-09\"}";
			
			ConsultaOtrosPrestamosBlockChain_Entrada Objin = gson.fromJson( json, ConsultaOtrosPrestamosBlockChain_Entrada.class );
			
			ConsultaOtrosPrestamosBlockChain demo = new ConsultaOtrosPrestamosBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				
				ConsultaOtrosPrestamosBlockChain_Salida ObjOut = demo.logica( con, Objin );
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
				
			} catch ( Exception e ) {
				e.printStackTrace();
				con.rollback();
			}
			
			
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}
		
}
