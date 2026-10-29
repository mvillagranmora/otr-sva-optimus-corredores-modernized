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

import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.borrowingClients;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.laDetails;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaLogBlockChain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_CONFIGURACION		= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	
	public static class ConsultaLogBlockChain_Entrada extends BcsPojo {
		public Long		p_IdEmpresa				;
		public String	p_Bloque		= null	;
	}
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ConsultaLogBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida>			P_OUT_MENSAJES			;
		public Long							P_OUT_ERRORESCONTADOR	;
		public ElementoSalida				P_OUT_RESULTADO			;
		public String						P_OUT_SECUENCIA			;
		
		public ConsultaLogBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES			= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
			this.P_OUT_RESULTADO 		= new  ElementoSalida();
		}
	}
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class ElementoSalida extends BcsPojo {
		public List<PrestamoCreado>					PrestamosCreados;
		public List<PrestamoExtendido> 				PrestamosExtendidos;
		public List<String>							PrestamosAnulados;
		public List<PrestamoAsignado>				Prestamistas;
		public List<PrestamoAnticipado>				PrestamosAnticipados;
		public List<String>							PrestamosVencidos;
		public List<PrestamoPrestamistaDesasignado>	PrestamistasDesasignado;
		public List<PrestamoModificado>				PrestamosModificado;
		public List<PrestamistaAnticipado>			PrestamistasAnticipado;
		
		public ElementoSalida()
		{
			this.PrestamosCreados			= new ArrayList<>();
			this.PrestamosExtendidos		= new ArrayList<>();
			this.PrestamosAnulados 			= new ArrayList<>();
			this.Prestamistas				= new ArrayList<>();
			this.PrestamosAnticipados 		= new ArrayList<>();
			this.PrestamosVencidos 			= new ArrayList<>();
			this.PrestamistasDesasignado	= new ArrayList<>();
			this.PrestamosModificado		= new ArrayList<>();
			this.PrestamistasAnticipado		= new ArrayList<>();
		}
	}
	
	public static class PrestamoCreado extends BcsPojo {
		public laDetails 		laDetails;
		public borrowingClients borrowingClients;
	}
	
	public static class PrestamoAsignado extends BcsPojo {
		public laDetails 			laDetails;
		public borrowingClients 	borrowingClients;
		public List<lendingClients>	lendingClients;
	}
	
	public static class PrestamoPrestamistaDesasignado extends BcsPojo {
		public String				LAID;
		public List<lendingClients>	lendingClients;
	}
	
	public static class PrestamoModificado extends BcsPojo {
		public laDetails 			laDetails;
		public borrowingClients 	borrowingClients;
	}
	
	public static class PrestamoExtendido extends BcsPojo {
		public String FolioSistemaExterno;
		public String FechaVencimiento;
		
		public PrestamoExtendido(String folioSistemaExterno, String fechaVencimiento) {
			this.FolioSistemaExterno 	= folioSistemaExterno;
			this.FechaVencimiento 		= fechaVencimiento;
		}
	}
	
	public static class PrestamoAnticipado extends BcsPojo {
		public String FolioSistemaExterno;
		public String Tasa;
		public String Cantidad;
		
		public PrestamoAnticipado() {
			this.FolioSistemaExterno 	= "";
			this.Tasa 					= "0";
			this.Cantidad 				= "0";
		}
	}
	
	public static class PrestamistaAnticipado extends BcsPojo {
		public String					FolioSistemaExterno;
		public List<lendingClients>		Prestamistas;
	}
	
	public static class SelectConfiguracion extends BcsPojo	{
		public String	CodigoParametro	;
        public String	Valor			;
        public String	Descripcion		;
	}
	
	public static class ElementoLogBlockChain extends BcsPojo {
		public String	Action;
		public String	BorrowingBrokerageHouseID;
		public String	Key;
		public String	LAStatus;
		public String	LendingBrokerageHouseID;
		public String	RequestDate;
		public String	Status;
	}
	
	public static class Secuencia extends BcsPojo {
		public Long   p_IdEmpresa;
		public String p_CodigoParametro;
		public String p_Valor;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ConsultaLogBlockChain_Salida logica(Connection conn, ConsultaLogBlockChain_Entrada in) throws Exception
	{
		SQL sql 									= new SQL();
		List<ElementoLogBlockChain> lstElementoLog 	= new ArrayList<>();
		ConsultaLogBlockChain_Salida salida 		= new ConsultaLogBlockChain_Salida();
		List<String> lstvaloresEntrada 				= new ArrayList<>();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		String strURLConsultaPrestamo;
		String strSecuencia;
		
		try
		{
			lstvaloresEntrada.add(ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_LOG);
			lstvaloresEntrada.add(ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_PRESTAMOSBYID);
			lstvaloresEntrada.add(ConstantesBCK.NOMBRE_PARAMETRO_BLOCKCHAIN_SECUENCIA);
			parametroEntrada.put("p_IdEmpresa"		, in.p_IdEmpresa);
			parametroEntrada.put("p_CodigoParametro", lstvaloresEntrada);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroEntrada, SelectConfiguracion.class);
			
			if ( SelectConfiguracion.size() > 2 ) {
				
				/*** INFORMACION CORREDORA ***/
				ConsultaDataCorredoraBlockChain consultaDataCorredora 													= new ConsultaDataCorredoraBlockChain();
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora 	= new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
				entradaConsultaDataCorredora.p_IdEmpresa = in.p_IdEmpresa;
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
				
				HashMap<String,Object> cnfUrleBlockChain = Utils.List2Hash(SelectConfiguracion, "CodigoParametro");
				
				//lee la URL que consulta un prestamo por ID en BlockChain
				SelectConfiguracion objcnf = (SelectConfiguracion) cnfUrleBlockChain.get(ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_PRESTAMOSBYID);
				strURLConsultaPrestamo = objcnf.Valor; 
				
				//lee la ultima secuencia retornada en la lectura BlockChain
				//esto permite hacer una lectura incremental del Log en lugar de leer todos los registros todas las ve
				objcnf = (SelectConfiguracion) cnfUrleBlockChain.get(ConstantesBCK.NOMBRE_PARAMETRO_BLOCKCHAIN_SECUENCIA);
				strSecuencia = objcnf.Valor; 
				
				
				//lee la URL que consulta el Log BlockChain
				objcnf = (SelectConfiguracion) cnfUrleBlockChain.get(ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_LOG);
				URL url = new URL( objcnf.Valor );
				
				HttpURLConnection connBC = (HttpURLConnection) url.openConnection();
				connBC.setDoOutput(true);
				connBC.setRequestMethod("POST");
				connBC.setRequestProperty("Content-Type", "application/json");

				LoginBlockchain loginBlockchain 								= new LoginBlockchain();
				LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();
				
				entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;
				
				LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);
				
				if( salidaLoginBlockchain.p_Out_ContadorError > 0 ) {
					salida.P_OUT_ERRORESCONTADOR = salidaLoginBlockchain.p_Out_ContadorError;
					GeneraElementosError(conn,in.p_IdEmpresa,0L,salidaLoginBlockchain.p_Out_MensajeError,salida.P_OUT_MENSAJES);

					return salida;
				}
				
				connBC.setRequestProperty("BcsUser"			, salidaLoginBlockchain.p_Out_UserLogin);
				connBC.setRequestProperty("Authorization"	, salidaLoginBlockchain.p_Out_Token);
				connBC.setRequestProperty("BcsAuthorization", salidaLoginBlockchain.p_Out_Token);

				//Puede continuar con el proceso
				if( salida.P_OUT_ERRORESCONTADOR == 0L ) {
					
					JSONObject input = new JSONObject();
					input.put("fromBlock", in.p_Bloque == null ? strSecuencia : in.p_Bloque);
					
					OutputStream outputStream = connBC.getOutputStream();
					outputStream.write(input.toString().getBytes());
					outputStream.flush();
					
					if (connBC.getResponseCode() != 200) {
						if (connBC.getResponseCode() == 401) {
							//RequestList.clear();
							//RequestList.add(new Request("NOTOKEN"));
							return salida;
						}
						else throw new RuntimeException("Failed: HTTP error code: " + connBC.getResponseCode());
					}
					
					BufferedReader br = new BufferedReader( new InputStreamReader( (connBC.getInputStream()) ) );
					
					String output;
					while ( (output = br.readLine()) != null ) {
						
						JSONObject obj = new JSONObject(output);
						
						try{
							JSONArray logs 	= obj.getJSONArray("logs");
							String sequence = obj.getString("sequence");
							if ( logs.length() == 0 ) { //no encontro registro para leer
								salida.P_OUT_SECUENCIA = sequence;
								return salida;
							}
							Gson gson = new Gson();
							for ( int i = 0; i < logs.length(); i++ ) {
								JSONObject request 					= logs.getJSONObject( i );
								ElementoLogBlockChain objItemLog 	= gson.fromJson( request.toString(), ElementoLogBlockChain.class );
								lstElementoLog.add(objItemLog);
							}
							
							//va a funcion que consulta datos de prestamos en BlockChain
							ElementoSalida objRetorno = ObtienePrestamosBlockChain(conn, strURLConsultaPrestamo, in.p_IdEmpresa, salidaLoginBlockchain, lstElementoLog, salidaConsultaDataCorredora.p_Out_BrokerageID, salida);
							
							//actualiza ultima secuencia leida desde BlockChain
							//ActualizaSecuenciaLogBlockChain(conn, in.p_IdEmpresa, sequence ); //no actualiza el Nro de Block leido inmediatamente, sino que desde donde es llamada esta clase alli se actualiza si los procesos tuvieron exito
							
							salida.P_OUT_SECUENCIA 							= sequence;
							salida.P_OUT_RESULTADO.PrestamosCreados			= objRetorno.PrestamosCreados ; 
							salida.P_OUT_RESULTADO.PrestamosExtendidos		= objRetorno.PrestamosExtendidos; 
							salida.P_OUT_RESULTADO.PrestamosAnulados		= objRetorno.PrestamosAnulados;
							salida.P_OUT_RESULTADO.Prestamistas				= objRetorno.Prestamistas;
							salida.P_OUT_RESULTADO.PrestamosAnticipados		= objRetorno.PrestamosAnticipados;
							salida.P_OUT_RESULTADO.PrestamosVencidos		= objRetorno.PrestamosVencidos;
							salida.P_OUT_RESULTADO.PrestamistasAnticipado	= objRetorno.PrestamistasAnticipado;
							salida.P_OUT_RESULTADO.PrestamosModificado		= objRetorno.PrestamosModificado;
							salida.P_OUT_RESULTADO.PrestamistasDesasignado	= objRetorno.PrestamistasDesasignado;
							
						} catch( Exception ex ) {
							salida.P_OUT_ERRORESCONTADOR = 1L;
							GeneraElementosError(conn, in.p_IdEmpresa, 0L, "SERVICIO_BLOCKCHAIN_NO_TRAE_ESTRUCTURA_ESPERADA", salida.P_OUT_MENSAJES);
							return salida;
						}
					}
					connBC.disconnect();
					
				}
				else //en caso de que no logra hacer login
				{
					salida.P_OUT_ERRORESCONTADOR = 1L;
					GeneraElementosError(conn, in.p_IdEmpresa, 0L, "SERVICIO_BLOCKCHAIN_NO_TRAE_ESTRUCTURA_ESPERADA", salida.P_OUT_MENSAJES);
					return salida;
				}
			}
			else
			{
				salida.P_OUT_ERRORESCONTADOR = 1L;
				GeneraElementosError(conn, in.p_IdEmpresa, 0L, "SERVICIO_NO_POSEE_CONFIGURACION", salida.P_OUT_MENSAJES);
				return salida;
			}
		}
		catch ( MalformedURLException e ) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			GeneraElementosError(conn, in.p_IdEmpresa, 0L, "UI_BLOCKCHAIN_ERROR", salida.P_OUT_MENSAJES);
		}
		catch ( IOException e ) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			GeneraElementosError(conn, in.p_IdEmpresa, 0L, "UI_BLOCKCHAIN_ERROR", salida.P_OUT_MENSAJES);
		}
		
		return salida;
	}
	
	//-------------------------------------------------
	// LEE EL DETALLE DE UN PRESTAMOS BLOCKCHAIN 
	//-------------------------------------------------
	public ElementoSalida ObtienePrestamosBlockChain(Connection conn, String strURL, Long IdEmpresa, LoginBlockchain.LoginBlockchain_Salida LoginBlockchain, List<ElementoLogBlockChain> elementosLog, String strIdCorredora, ConsultaLogBlockChain_Salida salida ) throws Exception 
	{
		ElementoSalida elementoSalida 	= new ElementoSalida();
		
		try {
			
			String prestamosRentaFija																				= ConfiguracionSistema.getParametroConfiguracionString(conn, IdEmpresa, "PLATAFORMA", "CUS.PrestamosRentaFija");
			ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
			
			List<Folio>	folios = elementosLog.stream()
					.map( elemento_log -> elemento_log.Key.split("-")[0].concat("-").concat(elemento_log.Key.split("-")[1]) )
					.distinct()
					.map( LAID -> new Folio( LAID.split("-")[0].concat("-").concat(LAID.split("-")[1]) ) )
					.collect( Collectors.toList() );
			
			consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= IdEmpresa;
			consultaPrestamoPorLAIDEntrada.p_FolioExterno	= folios;
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
			
			
			for ( ElementoLogBlockChain elementoLog : elementosLog ) {
				String LAID 									= elementoLog.Key.split("-")[0].concat("-").concat(elementoLog.Key.split("-")[1]);
				List<PrestamosBlockChain> prestamoBlockChain 	= consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
				.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equals( LAID ) )
				.collect( Collectors.toList() );
				
				if ( prestamoBlockChain.size() == 0 ) continue;
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACCTION_CREATE ) 
						&& !elementoSalida.PrestamosCreados.stream().anyMatch( prestamo_creado -> prestamo_creado.laDetails.LAID.equals( LAID ) ) ) {
					
					if ( prestamosRentaFija.equalsIgnoreCase( ConstantesBCK.NO )
							&& prestamoBlockChain.get( 0 ).laDetails.SecurityType.equalsIgnoreCase( ConstantesBCK.MERCADO_PRESTAMOS_BLOCKCHAIN_RENTA_FIJA ) 
							&& prestamoBlockChain.get( 0 ).laDetails.LendingAgreementType.equalsIgnoreCase( ConstantesBCK.ACUERDO_TIPO_TRANSACCIONAL ) ) {
						continue;
					}
					
					PrestamoCreado prestamoCreado = new PrestamoCreado();
					prestamoCreado.laDetails 		= prestamoBlockChain.get( 0 ).laDetails;
					prestamoCreado.borrowingClients = prestamoBlockChain.get( 0 ).borrowingClients;
					elementoSalida.PrestamosCreados.add(prestamoCreado);
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACCTION_ADDLENDINGCLIENTE ) 
						&& !elementoSalida.Prestamistas.stream().anyMatch( prestamo_asignado -> prestamo_asignado.laDetails.LAID.equals( LAID ) ) ) {
					PrestamoAsignado prestamoAsignado = new PrestamoAsignado();
					prestamoAsignado.laDetails			= prestamoBlockChain.get( 0 ).laDetails;
					prestamoAsignado.borrowingClients	= prestamoBlockChain.get( 0 ).borrowingClients;
					prestamoAsignado.lendingClients		= prestamoBlockChain.get( 0 ).lendingClients;
					elementoSalida.Prestamistas.add( prestamoAsignado );
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACTION_MODIFY ) ) {
					if ( prestamoBlockChain.stream().anyMatch( prestamo_asignado -> new BigDecimal( prestamo_asignado.laDetails.LendingQty ).compareTo( new BigDecimal( "0" ) ) == 0 )) {
						if ( elementoSalida.PrestamosAnulados.stream().anyMatch( prestamo_anulado_laid -> prestamo_anulado_laid.equals( LAID ) ) ) continue;
						elementoSalida.PrestamosAnulados.add(LAID);
					} else {
						if ( elementoSalida.PrestamistasDesasignado.stream().anyMatch( prestamista_desasignado -> prestamista_desasignado.LAID.equals( LAID ) ) ) continue;
						PrestamoPrestamistaDesasignado prestamoDesasignado = new PrestamoPrestamistaDesasignado();
						prestamoDesasignado.LAID 			= prestamoBlockChain.get( 0 ).laDetails.LAID;
						prestamoDesasignado.lendingClients	= prestamoBlockChain.get( 0 ).lendingClients;
						elementoSalida.PrestamistasDesasignado.add( prestamoDesasignado );
						
						PrestamoModificado prestamoModificado = new PrestamoModificado();
						prestamoModificado.laDetails		= prestamoBlockChain.get( 0 ).laDetails;
						prestamoModificado.borrowingClients	= prestamoBlockChain.get( 0 ).borrowingClients;
						elementoSalida.PrestamosModificado.add( prestamoModificado );
					}
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACTION_REQUESTEXTENSION ) 
						|| !elementoSalida.PrestamosModificado.stream().anyMatch( prestamo_modificado -> prestamo_modificado.laDetails.LAID.equals( LAID ) ) ) {
					PrestamoModificado prestamoModificado = new PrestamoModificado();
					prestamoModificado.laDetails		= prestamoBlockChain.get( 0 ).laDetails;
					prestamoModificado.borrowingClients	= prestamoBlockChain.get( 0 ).borrowingClients;
					elementoSalida.PrestamosModificado.add( prestamoModificado );
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACCTION_REQUESTTERMINATION ) 
						&& !elementoSalida.PrestamosAnticipados.stream().anyMatch( prestamo_anticipado -> prestamo_anticipado.FolioSistemaExterno.equals( LAID ) ) ) {
					PrestamoAnticipado prestamoAnticipado = new PrestamoAnticipado();
					prestamoAnticipado.FolioSistemaExterno 	= prestamoBlockChain.get( 0 ).laDetails.LAID;
					prestamoAnticipado.Tasa 				= prestamoBlockChain.get( 0 ).laDetails.Yield;
					prestamoAnticipado.Cantidad 			= prestamoBlockChain.get( 0 ).laDetails.LendingQty;
					elementoSalida.PrestamosAnticipados.add (prestamoAnticipado );
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACCTION_REQUESTPARTIALTERMINATION ) 
						&& !elementoSalida.PrestamistasAnticipado.stream().anyMatch( prestamista_anticipado -> prestamista_anticipado.FolioSistemaExterno.equals( LAID ) ) ) {
					PrestamistaAnticipado prestamistaAnticipado = new PrestamistaAnticipado();
					prestamistaAnticipado.FolioSistemaExterno 	= prestamoBlockChain.get( 0 ).laDetails.LAID;
					prestamistaAnticipado.Prestamistas			= prestamoBlockChain.get( 0 ).lendingClients;
					elementoSalida.PrestamistasAnticipado.add( prestamistaAnticipado );
				}
				
				if ( elementoLog.Action.equals( ConstantesBCK.ACCTION_REQUESTTERMINATION ) && elementoLog.LAStatus.equals( "MATURED" ) ) {
					elementoSalida.PrestamosVencidos.add( LAID );
				}
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return elementoSalida;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultaLogBlockChain_Entrada in = gson.fromJson(jin.toString(), ConsultaLogBlockChain_Entrada.class);
		ConsultaLogBlockChain_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private void GeneraElementosError(Connection con, Long idEmpresa, Long idInterno,  String codMsg, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(idInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
		
	}
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			String json = "{\"p_IdEmpresa\":1,\"p_Bloque\":\"1856\"}";
			//String json = "{\"p_IdEmpresa\":1}";
			ConsultaLogBlockChain_Entrada Objin = gson.fromJson(json, ConsultaLogBlockChain_Entrada.class);
			ConsultaLogBlockChain demo = new ConsultaLogBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ConsultaLogBlockChain_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut.toString());
 				con.commit();
 				//con.rollback();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}


}
