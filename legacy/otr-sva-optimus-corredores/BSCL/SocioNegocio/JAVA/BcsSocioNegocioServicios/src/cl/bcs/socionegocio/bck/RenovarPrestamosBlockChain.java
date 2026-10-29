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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.ConstantesCustodia;
import cl.bcs.plataforma.bck.LoginBlockchain;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Entrada;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Salida;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.SelectConfiguracion;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenovarPrestamosBlockChain implements JavaCall 
{
	static final Logger logger = LogManager.getLogger(RenovarPrestamosBlockChain.class);

	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_PRESTAMOS_INVOLUCRADOS	= loadResourceToString("cl/bcs/socionegocio/bck/sql/RenovarPrestamosBlockChain_SelectPrestamosInvolucrados.sql");
	private final String SQL_SELECT_CONFIGURACION			= loadResourceToString("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
	
	//-------------------------------------------------
	// CONSTANTES
	//-------------------------------------------------
	public static final String TIPO_PRESTAMO_VENTA_CORTA 	 = "PRTVTC";
	public static final String TIPO_PRESTAMO_FINES_GENERALES = "PRTFIN";
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class RenovarPrestamosBlockChain_Entrada extends BcsPojo {	
		public Long 			p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public DatosPrestamo 	p_DatosPrestamo;
	    public String 			p_FechaSolicitud;
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class RenovarPrestamosBlockChain_Salida extends BcsPojo {
		public String				P_OUT_STATUS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public String				P_OUT_FOLIOSISTEMAEXTERNO;
		
		public RenovarPrestamosBlockChain_Salida()
		{	
			this.P_OUT_STATUS				= "";
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR		= 0L;
			this.P_OUT_FOLIOSISTEMAEXTERNO 	= "";
		}
	}
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	public static class DatosPrestamo extends BcsPojo {
		public Long FolioPrestamoSolicitud;
		public Long FolioPrestamoNuevo;
	}
	
	public static class CarteraPrestamo extends BcsPojo {
		public Long 			FolioCartera;
		public Long 			FolioCarteraRelacionada;
		public String 			FolioSistemaExterno;
		public Long 			IdCliente;
		public java.sql.Date 	FechaVencimientoOperacion;
		public String 			IdTipoPrestamo;
		public String 			Nemotecnico;
		public String 			Mercado;
		public String 			CodigoCondicionLiquidacion;
		public BigDecimal 		Cantidad;
		public String 			TipoPrestamoSistemaExterno;
		public BigDecimal 		TasaOperacion;
		public BigDecimal 		MontoPrima;
	}
	
	private String ConsultarBlockChain (Connection conn, Long IdEmpresa, Long IdUsuario, CarteraPrestamo Prestamo) throws Exception {
		
		logger.info("INICIO::ConsultarBlockChain");
		long inicio = System.currentTimeMillis();

		String esBlockChain = ConstantesBCK.NO;
			
		ConsultaEstadoCntBC consultaEstadoCntBC = new ConsultaEstadoCntBC();
		consultaEstadoCntBC.p_IdTipoDocumento = (Prestamo.IdTipoPrestamo.equals(TIPO_PRESTAMO_VENTA_CORTA)) ? ConstantesBCK.IDCONTRATOPRESTAMOANEXO42 : ConstantesBCK.IDCONTRATOPRESTAMOANEXO48;
		consultaEstadoCntBC.p_IdCliente		  = Prestamo.IdCliente.toString();
		consultaEstadoCntBC.p_IdContrato      = (Prestamo.IdTipoPrestamo.equals(TIPO_PRESTAMO_VENTA_CORTA)) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48;
			
		ContratoEsBlockChainFirmado_Entrada contratoEsBlockChainEntrada = new  ContratoEsBlockChainFirmado_Entrada();
		contratoEsBlockChainEntrada.p_IdEmpresa 			= IdEmpresa;
		contratoEsBlockChainEntrada.p_IdUsuario 			= IdUsuario;
		contratoEsBlockChainEntrada.p_ClaveUsuario 			= "";
		contratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= consultaEstadoCntBC;
			
		ContratoEsBlockChainFirmado contratoEsBlockChain = new ContratoEsBlockChainFirmado();
		ContratoEsBlockChainFirmado_Salida contratoEsBlockChainFirmado_Salida = contratoEsBlockChain.logica(conn, contratoEsBlockChainEntrada);
			
		for(ContratoEsBlockChainFirmado.Resultado ResultadoBC: contratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO)
		{
		     if(ResultadoBC.EsBlockChain.equals(ConstantesBCK.SI))
		     {
		    	 esBlockChain = (ResultadoBC.ContratoFirmado.equals(ConstantesBCK.SI)) ? ConstantesBCK.SI : ConstantesBCK.NO;
		     }
		     
		     break;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarBlockChain", fin - inicio);

		return esBlockChain;
	}
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public RenovarPrestamosBlockChain_Salida logica(Connection conn, RenovarPrestamosBlockChain_Entrada in) throws Exception
	{
		logger.info("INICIO::logica");
		long inicio = System.currentTimeMillis();
		
		RenovarPrestamosBlockChain_Salida salida 	= new RenovarPrestamosBlockChain_Salida();
		HashMap<String,String> mapMsg 				= new HashMap<>();
		SQL sql 									= new SQL();
		
		try
		{
			/********** DATOS DE PRESTAMOS INVOLUCRADOS EN LA RENOVACION **********/
			
			List<CarteraPrestamo> prestamosInvolucrados = new ArrayList<>();
			HashMap<String, Object> params 				= new HashMap<>();
			List<Long> foliosInvolucrados 				= new ArrayList<>();
			foliosInvolucrados.add(in.p_DatosPrestamo.FolioPrestamoSolicitud);
			foliosInvolucrados.add(in.p_DatosPrestamo.FolioPrestamoNuevo);
			params.put("p_FoliosPrestamo"	, foliosInvolucrados);
			params.put("p_IdEmpresa"		, in.p_IdEmpresa);
			prestamosInvolucrados = sql.query(conn, SQL_SELECT_PRESTAMOS_INVOLUCRADOS, params, CarteraPrestamo.class);
			params = new HashMap<>();

			if (prestamosInvolucrados.size() == 0) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"PRESTAMOS_INVOLUCRADOS_EN_OPERACION_NO_ECONTRADOS", mapMsg, conn, in.p_IdEmpresa));
				return salida;
			}

			if (prestamosInvolucrados
			.parallelStream()
			.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoSolicitud))
			.count() == 0) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"PRESTAMO_FOLIO_SOLICITUD_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa));
				return salida;
			}
			if (prestamosInvolucrados
			.parallelStream()
			.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoNuevo))
			.count() == 0) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"PRESTAMO_FOLIO_NUEVO_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa));
				return salida;
			}
			
			CarteraPrestamo prestamoSolicitud = prestamosInvolucrados
					.parallelStream()
					.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoSolicitud))
					.collect(Collectors.toList())
					.get(0);
			CarteraPrestamo prestamoNuevo = prestamosInvolucrados
					.parallelStream()
					.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoNuevo))
					.collect(Collectors.toList())
					.get(0);
			
			/********** VALIDA CONTRATO BLOCKCHAIN **********/
			
			String EsBlockChain = ConsultarBlockChain(conn, in.p_IdEmpresa, in.p_IdUsuario, prestamosInvolucrados.get(0));
			
			if (!EsBlockChain.equalsIgnoreCase(ConstantesBCK.SI)) {
				salida.P_OUT_STATUS = ""; 
				return salida;
			}
			
			/********** CONSULTA PRESTAMO BLOCKCHAIN **********/
			List<Folio> foliosBlockChain 	= new ArrayList<>();
			Folio folioBlockChain 			= new ConsultaListaPorFolioBlockChain.Folio(prestamoNuevo.FolioSistemaExterno);
			foliosBlockChain.add(folioBlockChain);
			ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
			consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= in.p_IdEmpresa;
			consultaPrestamoPorLAIDEntrada.p_FolioExterno 	= foliosBlockChain;
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
			
			if ( consultaPrestamoPorLAIDSalida.p_Out_ContadorError > 0 || consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream().count() == 0 ) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_PRESTAMO_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa));
				return salida;
			}
			
			if ( !prestamoSolicitud.FolioSistemaExterno.isEmpty() && !consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.collect( Collectors.toList() )
					.get(0).laDetails.Status.equalsIgnoreCase("MATURED") ) {
				return renovar(conn, prestamosInvolucrados, in);
			}
			else {
				/*** INFORMACION CORREDORA ***/
				ConsultaDataCorredoraBlockChain consultaDataCorredora 													= new ConsultaDataCorredoraBlockChain();
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora 	= new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
				entradaConsultaDataCorredora.p_IdEmpresa = in.p_IdEmpresa;
				ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
				
				if (salidaConsultaDataCorredora.p_Out_ContadorError > 0) {
					mapMsg.clear();
					salida.P_OUT_STATUS = "";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA", mapMsg, conn, in.p_IdEmpresa));
					return salida;
				}
				
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BC_RENOVACION_PRESTAMO_VENCIDO", mapMsg, conn, in.p_IdEmpresa));
				
				if ( prestamoSolicitud.IdTipoPrestamo.equalsIgnoreCase(TIPO_PRESTAMO_FINES_GENERALES) || prestamoSolicitud.IdTipoPrestamo.equalsIgnoreCase(TIPO_PRESTAMO_VENTA_CORTA) ) {
					return crearPrestamo(conn, prestamosInvolucrados, in, salidaConsultaDataCorredora.p_Out_BrokerageID);
				}
				else {
					mapMsg.clear();
					salida.P_OUT_STATUS = "";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"TIPO_PRESTAMO_NO_APLICA_RENOVACION", mapMsg, conn, in.p_IdEmpresa));
					return salida;
				}
			}
			
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::logica", fin - inicio);
		
		salida.P_OUT_STATUS = ""; 
		return salida;
	}

	public RenovarPrestamosBlockChain_Salida renovar(Connection conn, Collection<CarteraPrestamo> prestamosInvolucrados, RenovarPrestamosBlockChain_Entrada in) throws Exception {
		
		logger.info("INICIO::renovar");
		long inicio = System.currentTimeMillis();
		
		RenovarPrestamosBlockChain_Salida salida 	= new RenovarPrestamosBlockChain_Salida();
		HashMap<String,String> mapMsg 				= new HashMap<>();
		SQL sql 									= new SQL();
		HashMap<String, Object> parametroQuery 		= new HashMap<String, Object>();
		
		try {
			String folioSistemaExternoPrestamoSolicitud = prestamosInvolucrados
					.parallelStream()
					.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoSolicitud))
					.map(prestamo -> prestamo.FolioSistemaExterno)
					.collect(Collectors.toList())
					.get(0);
			
			String fechaVencimientoPrestamoNuevo = prestamosInvolucrados
					.parallelStream()
					.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoNuevo))
					.map(prestamo -> prestamo.FechaVencimientoOperacion)
					.collect(Collectors.toList())
					.get(0)
					.toString();
			
			/********** LOGIN EN SISTEMA BLOCKCHAIN **********/
			
			LoginBlockchain loginBlockchain 								= new LoginBlockchain();
			LoginBlockchain.LoginBlockchain_Entrada entradaLoginBlockchain 	= new LoginBlockchain.LoginBlockchain_Entrada();

			entradaLoginBlockchain.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN;

			LoginBlockchain.LoginBlockchain_Salida salidaLoginBlockchain = loginBlockchain.logica(conn, entradaLoginBlockchain);

			if (salidaLoginBlockchain.p_Out_ContadorError > 0) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,salidaLoginBlockchain.p_Out_MensajeError, mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = salidaLoginBlockchain.p_Out_ContadorError;
				return salida;
			}
			
			/********** CONFIGURACION SERVICIO **********/
			
			parametroQuery.put("p_IdEmpresa", in.p_IdEmpresa);
			parametroQuery.put("p_CodigoParametro", ConstantesBCK.NOMBRE_PARAMETRO_URL_BLOCKCHAIN_RENOVAR_PRESTAMO);
			List<SelectConfiguracion> SelectConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, parametroQuery, SelectConfiguracion.class);
			parametroQuery.clear();
			
			if (SelectConfiguracion.isEmpty()) {
				mapMsg.clear();
				salida.P_OUT_STATUS = "";
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
			LADetails.put("MaturityDate", fechaVencimientoPrestamoNuevo);
			// RequestDetails
			JSONObject RequestDetails = new JSONObject();
			RequestDetails.put("Action", ConstantesBCK.ACTION_REQUESTEXTENSION);
			RequestDetails.put("RequestDate", in.p_FechaSolicitud);
			RequestDetails.put("LAID", folioSistemaExternoPrestamoSolicitud);
			
			argsObjeto.put("LADetails", LADetails);
			argsObjeto.put("RequestDetails", RequestDetails);
			
			argsArreglo.put(argsObjeto);
			argsArreglo.put(salidaLoginBlockchain.p_Out_BrokerageID);
			
			entrada.put("args", argsArreglo);
			
			OutputStream outputStream = connBC.getOutputStream();
			outputStream.write(entrada.toString().getBytes());
			outputStream.flush();
			
			if (connBC.getResponseCode() != 200) {
				if (connBC.getResponseCode() == 401) {
					mapMsg.clear();
					salida.P_OUT_STATUS = "";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"NOTOKEN", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					mapMsg.clear();
					salida.P_OUT_STATUS = "";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"ERROR", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				}
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((connBC.getInputStream())));

			String output;
			
			while ((output = br.readLine()) != null) {
				JSONObject obj = new JSONObject(output);

				if (obj.getString("status").equals("SUCCESS")) {
					try {
						JSONArray out = obj.getJSONArray("LA");
						JSONObject out2 = new JSONObject(out.get(0).toString());
						JSONObject response = out2.getJSONObject("RequestDetails");
						
						mapMsg.clear();
						salida.P_OUT_STATUS = obj.getString("status");
						salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,response.getString("LAID"), mapMsg, conn, in.p_IdEmpresa));
						salida.P_OUT_FOLIOSISTEMAEXTERNO = response.getString("LAID");
						return salida;
					} catch (Exception ex) {
						mapMsg.clear();
						salida.P_OUT_STATUS = "NOFOUND";
						salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"NOFOUND", mapMsg, conn, in.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = 1L;
						return salida;
					}
				} else {
					if (obj.getString("status").equals("ERROR")) {
						try {
							String message 							= obj.getString("message");
							JSONArray messageArray 					= new JSONArray(message);
							ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
							
							for (int i = 0, size = messageArray.length(); i < size; i++) {
								String messageInArray = messageArray.getString(i);
								String vCodigo = constantesCustodia.codigoErroresBlockChain.get(messageInArray.replace(".", ""));
								salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, vCodigo, mapMsg, conn, in.p_IdEmpresa));
							}
							mapMsg.clear();
							salida.P_OUT_STATUS = obj.getString("status");
							salida.P_OUT_ERRORESCONTADOR = 1L;
							return salida;
						} catch (Exception ex) {
							mapMsg.clear();
							salida.P_OUT_STATUS = "";
							salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR_SERVICIO_BLOCKCHAIN_RENOVAR_PRESTAMO", mapMsg, conn, in.p_IdEmpresa));
							salida.P_OUT_ERRORESCONTADOR = 1L;
							return salida;
						}
					}
				}
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ERROR", mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::renovar", fin - inicio);
		
		salida.P_OUT_STATUS = ""; 
		return salida;
		
	}
	
	public RenovarPrestamosBlockChain_Salida crearPrestamo(Connection conn, Collection<CarteraPrestamo> prestamosInvolucrados, RenovarPrestamosBlockChain_Entrada in, String codigoCorredor) throws Exception {
		
		logger.info("INICIO::crearPrestamo");
		long inicio = System.currentTimeMillis();
		
		String prestamosRentaFija											= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "CUS.PrestamosRentaFija");
		RenovarPrestamosBlockChain_Salida salida 							= new RenovarPrestamosBlockChain_Salida();
		IngresoPrestamosBlockchain IngresoPrestamo 							= new IngresoPrestamosBlockchain();
		IngresoPrestamosBlockchain_Entrada IngresoPrestamoBlockChainEntrada = new IngresoPrestamosBlockchain_Entrada();
		HashMap<String,String> mapMsg 										= new HashMap<>();
		
		CarteraPrestamo prestamoNuevo 						= prestamosInvolucrados
				.parallelStream()
				.filter(prestamo -> prestamo.FolioCartera.equals(in.p_DatosPrestamo.FolioPrestamoNuevo))
				.collect(Collectors.toList())
				.get(0);
		
		if ( prestamoNuevo.IdTipoPrestamo.equalsIgnoreCase( ConstantesBCK.PRESTAMO_TIPO_VENTA_CORTA_RENTA_FIJA ) && prestamosRentaFija.equalsIgnoreCase( ConstantesBCK.NO ) ) {
			return salida;
		}
		
		IngresoPrestamoBlockChainEntrada.p_IdEmpresa 					= in.p_IdEmpresa;
		IngresoPrestamoBlockChainEntrada.p_Tasa 						= prestamoNuevo.TasaOperacion.toString();
		IngresoPrestamoBlockChainEntrada.p_FechaOperacion 				= in.p_FechaSolicitud;
		IngresoPrestamoBlockChainEntrada.p_Nemo 						= prestamoNuevo.Nemotecnico;
		IngresoPrestamoBlockChainEntrada.p_Mercado 						= prestamoNuevo.Mercado;
		IngresoPrestamoBlockChainEntrada.p_TipoPrestamo 				= prestamoNuevo.TipoPrestamoSistemaExterno;
		IngresoPrestamoBlockChainEntrada.p_TipoGarantia 				= "T";
		IngresoPrestamoBlockChainEntrada.p_CondicionLiquidacion 		= prestamoNuevo.CodigoCondicionLiquidacion; 
		IngresoPrestamoBlockChainEntrada.p_CodigoCorredorPrestamista 	= codigoCorredor;
		IngresoPrestamoBlockChainEntrada.p_Cantidad 					= prestamoNuevo.Cantidad.toString();
		IngresoPrestamoBlockChainEntrada.p_FechaVencimiento 			= prestamoNuevo.FechaVencimientoOperacion.toString();
		IngresoPrestamoBlockChainEntrada.p_IdClientePrestatario 		= prestamoNuevo.IdCliente.toString(); 
		IngresoPrestamoBlockChainEntrada.p_IdClientePrestamista 		= "";
		IngresoPrestamoBlockChainEntrada.p_CantidadPrestamista 			= "0";
		IngresoPrestamoBlockChainEntrada.p_MontoPrima					= prestamoNuevo.MontoPrima;
		
		IngresoPrestamosBlockchain_Salida IngresoPrestamoBlockChainFirmado_Salida = IngresoPrestamo.logica(conn, IngresoPrestamoBlockChainEntrada);
		
		if (IngresoPrestamoBlockChainFirmado_Salida.p_Out_ContadorError > 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,IngresoPrestamoBlockChainFirmado_Salida.p_Out_Mensaje, null, conn, in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
		} else {
			salida.P_OUT_FOLIOSISTEMAEXTERNO = IngresoPrestamoBlockChainFirmado_Salida.p_Out_FolioExterno;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,IngresoPrestamoBlockChainFirmado_Salida.p_Out_Mensaje, null, conn, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::crearPrestamo", fin - inicio);
		
		return salida;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		RenovarPrestamosBlockChain_Entrada in = gson.fromJson(jin.toString(), RenovarPrestamosBlockChain_Entrada.class);
		RenovarPrestamosBlockChain_Salida out = logica(conn, in);
		
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
		System.out.println("== JOB RENOVAR PRESTAMOS EN BLOCKCHAIN ==");
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"CUS\"}";
			
			RenovarPrestamosBlockChain_Entrada Objin = gson.fromJson(jsonCL, RenovarPrestamosBlockChain_Entrada.class);

			RenovarPrestamosBlockChain demo = new RenovarPrestamosBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				RenovarPrestamosBlockChain_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
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
