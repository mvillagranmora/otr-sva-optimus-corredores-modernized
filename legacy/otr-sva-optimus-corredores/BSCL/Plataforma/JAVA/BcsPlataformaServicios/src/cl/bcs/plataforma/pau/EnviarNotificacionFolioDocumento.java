package cl.bcs.plataforma.pau;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenCorredora;
import cl.bcs.utils.AgregarNotificacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarNotificacionFolioDocumento implements JavaCall {	

	static final Logger logger								= LogManager.getLogger(EnviarNotificacionFolioDocumento.class);
	
	private final String SQL_SELECT_NOMBRE_COMPROBANTE 		= loadResourceToString("cl/bcs/plataforma/pau/sql/EnviarNotificacionFolioDocumento_SelectNombreComprobante.sql");
	private final String SQL_SELECT_DIRECCION_CORREO 		= loadResourceToString("cl/bcs/plataforma/pau/sql/EnviarNotificacionFolioDocumento_SelectDireccionCorreoElectronico.sql");
	private final String SQL_SELECT_CORREDORA 				= loadResourceToString("cl/bcs/plataforma/pau/sql/EnviarNotificacionFolioDocumento_SelectCorredora.sql");

	public static class EnviarNotificacionFolioDocumentoEntrada extends BcsPojo {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
	}
	
	public static class EnviarNotificacionFolioDocumentoSalida extends BcsPojo {
		
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public EnviarNotificacionFolioDocumentoSalida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
	}
	
	public static class NombreComprobante extends BcsPojo {
		
		public Integer TipoComprobanteDTE;
		public String Nombre;
		
	}
	
	public static class DireccionCorreoElectronico extends BcsPojo {
		
		public Long IdPersona;
		public Long IdDireccionElectronica;
		public String Direccion;
		
	}
	
	public static class DatosCorredora extends BcsPojo {
		
		public Long IdPersona;
		public String DocumentoIdentidad;
		
	}
	
	
	public EnviarNotificacionFolioDocumentoSalida logica(Connection con, EnviarNotificacionFolioDocumentoEntrada entrada) throws Exception {
		
		logger.info("INICIO::EnviarNotificacionFolioDocumentoSalida");		
		
        Long inicio 													= System.currentTimeMillis();
		EnviarNotificacionFolioDocumentoSalida out 						= new EnviarNotificacionFolioDocumentoSalida();
		String token													= null;
		JsonArray documentosDte											= new JsonArray();
		JsonArray documentosDteANotificar								= new JsonArray();
		SQL sql 								  						= new SQL();
		HashMap<String,Object> params 									= new HashMap<String,Object>();
		List<AgregarNotificacion.IdentificadorEvento> eventosCreados 	= new ArrayList();
        ResumenCorredora resumenCorredora 								= SocioNegocioUtils.obtenerResumenCorredora(con, entrada.p_IdEmpresa); 
		String urlDte     												= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "DTE_URL");		
        String consultarParametroStockCritico 							= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "DTE_STOCK_CRITICO_FOLIOS"); 
        String consultarParametroCorreosDestinario 						= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "DTE_ENVIO_CORREO_STOCK_CRITICO_FOLIOS");

        
        if ( consultarParametroStockCritico.isEmpty() || consultarParametroCorreosDestinario.isEmpty() ) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al obtener parametros de configuracion del sistema.";
			return out;
			
		}
        
		List<String> correosDestinatarios 	= this.obtenerListaCorreoDestinatarios(consultarParametroCorreosDestinario);
		Integer stockCriticoBase 				= Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "DTE_STOCK_CRITICO_FOLIOS"));
		
		
		params.put("correosDestinatarios", correosDestinatarios);
		List<DireccionCorreoElectronico> direccionesCorreoElectronico = sql.query(con, SQL_SELECT_DIRECCION_CORREO, params, DireccionCorreoElectronico.class);
		
		if (direccionesCorreoElectronico.size() == 0) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al buscar informacion sobre los correos electronicos de destino.";
			return out;
			
		}
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);	
		List<DatosCorredora> corredora 	= sql.query(con, SQL_SELECT_CORREDORA, params, DatosCorredora.class);
		
		if (corredora.size() == 0) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al obtener datos de la corredora";
			return out;
			
		}
		
		params.clear();
		List<NombreComprobante> nombresComprobantes = sql.query(con, SQL_SELECT_NOMBRE_COMPROBANTE, params , NombreComprobante.class);
		
		if (nombresComprobantes.size() == 0) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "No se pudo obtener los nombres de los comprobantes";
			return out;
			
		}
		
		try {
			
			token			= this.obtenerTokenDte(con, urlDte, entrada.p_IdEmpresa);
			documentosDte 	= this.consultaDocumentosDte(urlDte, token, corredora.get(0).DocumentoIdentidad.toString());
		
		} catch (Exception err) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al Comunicarse con DTE.";
			return out;
		}
		
		
		for ( JsonElement documento: documentosDte) {

			Integer remanente = documento.getAsJsonObject().get("folioFin").getAsInt() - documento.getAsJsonObject().get("folioAct").getAsInt(); 
			Integer tipoDte = documento.getAsJsonObject().get("tipoDte").getAsInt();
			Integer stockCritico = 0;

			try {
				if (tipoDte != null) {
					Integer stockCriticoTipo = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "DTE_STOCK_CRITICO_FOLIOS_" + tipoDte));
					stockCritico = stockCriticoTipo;
				}
			} catch (Exception e) {
				Boolean existeTipoDte = false;
			}

			if(stockCritico == 0){
				stockCritico = stockCriticoBase;
			}

			if( remanente < stockCritico && remanente > 0 ) {
			
					documentosDteANotificar.add(documento);        
			}
		}
		
		try {
				
			eventosCreados = this.notificarCorredora(con, entrada, corredora.get(0), documentosDteANotificar, resumenCorredora, nombresComprobantes, direccionesCorreoElectronico);
			out.P_OUT_MENSAJE = "Proceso de Notificacion de Stock de Folios Criticos DTE terminado correctamente: " + eventosCreados.size() + " eventos de notificacion creados.";
			if (direccionesCorreoElectronico.size() == 0) {
				out.P_OUT_MENSAJE = "Proceso de Notificacion de Stock de Folios Criticos DTE terminado correctamente: " + eventosCreados.size() + " eventos de notificacion creados. WARNING: Algunos de los correos no se encontraban en el sistema.";
			}
		} catch (Exception err) {
			logger.error(err);
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al generar las notificaciones por correo electronico.";
			return out;
			
		}
		
        Long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarNotificacionFolioDocumentoSalida: ", fin - inicio);	

		return out;
	}


	private List<String> obtenerListaCorreoDestinatarios(String consultarParametroCorreosDestinario) {

		List<String> correosDestinatarios 	= new ArrayList();
		String[] CorreosANotificarSeparados = consultarParametroCorreosDestinario.split(";", -1); 
		
		for ( int i = 0; i < CorreosANotificarSeparados.length; i++) {
			
			correosDestinatarios.add(consultarParametroCorreosDestinario);
            
		}
		
		return correosDestinatarios;
		
	}


	private String obtenerTokenDte(Connection con, String urlDte, Long p_IdEmpresa) throws Exception {
		
		logger.info("INICIO::EnviarNotificacionFolioDocumentoSalida.obtenerToken");

		Long inicio 				= System.currentTimeMillis();
		String token     	 		= null;
		String usuario    			= ConfiguracionSistema.getParametroConfiguracionString(con, p_IdEmpresa, "FACTURACION", "DTE_USER");
		String contrasena 			= ConfiguracionSistema.getParametroConfiguracionString(con, p_IdEmpresa, "FACTURACION", "DTE_PASS");
		String URLparms 			= urlDte + "/api/authenticate?username="+ usuario +"&password=" + contrasena;    		      		
		URL url 		  			= new URL(URLparms);
		HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
		
		connect.setDoOutput(true);
		connect.setRequestMethod("POST");
		connect.setConnectTimeout(5000);
		connect.setInstanceFollowRedirects(true); 
		connect.setRequestProperty("Content-Type", "application/json"); 
		connect.setRequestProperty("charset", "UTF-8");	
		connect.setUseCaches( false );
		
		InputStream response 		= connect.getInputStream();
		JsonParser parser 			= new JsonParser();
		JsonObject consultaToken 	= parser.parse(new InputStreamReader(response, "UTF-8")).getAsJsonObject();
		token 						= consultaToken.get("token").getAsString();
		
		if ( connect.getResponseCode() != 200 ) {
			
			logger.info("Error respuesta al pedir token:"+ connect.getResponseCode() + ":" + response);
			
		}
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarNotificacionFolioDocumentoSalida.obtenerToken: ", fin - inicio);	
		return token;
		
	}
	
	private JsonArray consultaDocumentosDte(String urlDte, String token, String rutCorredora) throws IOException {
		
		logger.info("INICIO::EnviarNotificacionFolioDocumentoSalida.obtenerDocumentos");
		
        Long inicio 				= System.currentTimeMillis();
		JsonArray documentosDte 	= new JsonArray();
		JsonArray consultaDte 		= new JsonArray();
		JsonParser parser 			= new JsonParser();
		Integer pagina				= 1;
		Integer datosPorPagina		= 100;
		
			
		for( pagina = 1;  ; pagina++ ) {
			
			String URLparms 			= urlDte + "/api/autorizacions?page=" + pagina + "&per_page=" + datosPorPagina + "&empresa=" + rutCorredora;
			URL url 					= new URL(URLparms);
			HttpURLConnection connect 	= (HttpURLConnection) url.openConnection();
			
			connect.setDoOutput(true);
			connect.setRequestProperty ("X-Auth-Token", token);
			connect.setRequestProperty("Content-Type", "application/octet-stream"); 
			connect.setRequestMethod("GET");
			connect.setConnectTimeout(5000);
			connect.setInstanceFollowRedirects(true); 
			connect.setUseCaches( false );
			
			InputStream response 	= connect.getInputStream();

			if ( connect.getResponseCode() != 200 ) {
		
				logger.info("Error respuesta al pedir los documentos:"+ connect.getResponseCode() + ":" + response);
				
			}
			
			consultaDte = parser.parse(new InputStreamReader(response, "UTF-8")).getAsJsonArray();
			if (consultaDte.size() == 0 ) break;
			documentosDte.addAll(consultaDte);
			
		} 
					
        Long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarNotificacionFolioDocumentoSalida.obtenerDocumentos: ", fin - inicio);
        return documentosDte;
	}

	private List<AgregarNotificacion.IdentificadorEvento> notificarCorredora(Connection conn,  EnviarNotificacionFolioDocumentoEntrada entrada, DatosCorredora corredora, JsonArray documentosANotificar, ResumenCorredora resumenCorredora, List<NombreComprobante> nombresComprobantes, List<DireccionCorreoElectronico> direccionesCorreoElectronico) throws Exception {

		logger.debug("INICIO::EnviarNotificacionFolioDocumento.notificarUsuario");
	    
		Long inicio 																= System.currentTimeMillis();
	    String consultaFechaHoraSistema 											= Utils.getFechaHoraSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);
	    String[] divisionFechaHora 													= consultaFechaHoraSistema.split(" ");
	    String fechaSistema 														= divisionFechaHora[0];
	    String horaSistema 															= divisionFechaHora[1];
	    SimpleDateFormat fechaFormatoSalida 										= new SimpleDateFormat("dd-MM-yyyy");
	    SimpleDateFormat fechaFormatoEntrada 										= new SimpleDateFormat("yyyy-MM-dd");
	    String fechaSistemaFormateada 												= fechaFormatoSalida.format(fechaFormatoEntrada.parse(fechaSistema));
	    AgregarNotificacion.EntradaAgregarNotificacion entradaAgregarNotificacion 	= new AgregarNotificacion.EntradaAgregarNotificacion();
	    String logoCorredorRutaPublica = "";
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = entrada.p_IdEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(conn, consultarImagenIn);
		if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
			logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
		}
	   
	    entradaAgregarNotificacion.P_IDEMPRESA 			= entrada.p_IdEmpresa;
	    entradaAgregarNotificacion.P_IDUSUARIO 			= entrada.p_IdUsuario;
	    entradaAgregarNotificacion.P_IDMODULO 			= "SOC";
	    entradaAgregarNotificacion.P_EVENTOS 			= new ArrayList<>();
	    entradaAgregarNotificacion.P_VALORES 			= new ArrayList<>();
	    entradaAgregarNotificacion.P_ARCHIVOSADJUNTOS 	= new ArrayList<>();
	    entradaAgregarNotificacion.P_IDDIRECCIONES 		= new ArrayList<>();
	    
	    for ( JsonElement documento : documentosANotificar ) {
	    	
	    	long idEvento 										= documento.getAsJsonObject().get("id").getAsLong();
	    	Integer remanente 									= documento.getAsJsonObject().get("folioFin").getAsInt() - documento.getAsJsonObject().get("folioAct").getAsInt();
	    	List<NombreComprobante> busquedaNombreComprobante 	= nombresComprobantes.stream()
	    															.filter(item -> item.TipoComprobanteDTE.equals(documento.getAsJsonObject().get("tipoDte").getAsInt()))
	    															.collect(Collectors.toList());  	

	    	AgregarNotificacion.Evento eventoNotificacion 	= new AgregarNotificacion.Evento();
		    eventoNotificacion.CodigoTipoEvento 			= "NOTIF_STOCK_CRITICO_FOLIOS";
		    eventoNotificacion.IdSecuencia 					= idEvento;
		    eventoNotificacion.FechaCreacionSistema 		= fechaSistema;
		    eventoNotificacion.IdPersonaRemitente 			= 0L;
		    eventoNotificacion.IdPersonaDestinatario 		= corredora.IdPersona;
		    eventoNotificacion.IdPortafolio 				= 0L;
		    entradaAgregarNotificacion.P_EVENTOS.add(eventoNotificacion);
		    
		    for ( DireccionCorreoElectronico direccionCorreoElectronico : direccionesCorreoElectronico ) {
		    	
		        AgregarNotificacion.DireccionEvento direccionEventoNotificacion = new AgregarNotificacion.DireccionEvento();
		        direccionEventoNotificacion.IdSecuencia 						= idEvento;
		        direccionEventoNotificacion.TipoDestinatario 					= "TO";
		        direccionEventoNotificacion.IdDireccion 						= direccionCorreoElectronico.IdDireccionElectronica;
		        entradaAgregarNotificacion.P_IDDIRECCIONES.add(direccionEventoNotificacion);
		        
		    }

		    entradaAgregarNotificacion
	    		.addValor(idEvento, "LOGO_PUBLICO", logoCorredorRutaPublica)
	            .addValor(idEvento, "NOMBRE_NOTIF", "STOCK FOLIO DTE CRITICO")
	            .addValor(idEvento, "FECHA", fechaSistemaFormateada)
	            .addValor(idEvento, "HORA", horaSistema)
	            .addValor(idEvento, "TIPO_COMPROBANTE", busquedaNombreComprobante.get(0).Nombre)
	            .addValor(idEvento, "FOLIO_ACTUAL", documento.getAsJsonObject().get("folioAct").toString())
	            .addValor(idEvento, "FOLIO_FIN", documento.getAsJsonObject().get("folioFin").toString())
	            .addValor(idEvento, "REMANENTE", remanente.toString())
	            .addValor(idEvento, "CORREDORA", resumenCorredora.RazonCorredora)
	            .addValor(idEvento, "ATENCIONCLIENTE", resumenCorredora.TelefonoCorredora);
		    
	    }
	    
	    AgregarNotificacion agregarNotificacion 					= new AgregarNotificacion();
	    List<AgregarNotificacion.IdentificadorEvento> idsEventos 	= agregarNotificacion.logica(conn, entradaAgregarNotificacion).P_OUT_IDEVENTO;
	
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::RenovarTokenFirmaDocumento.notificarUsuario: ", fin - inicio);
	
	    return idsEventos;
	    
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		
		Gson gson 									= new Gson();		
		EnviarNotificacionFolioDocumentoEntrada in 	= gson.fromJson(jin.toString() ,EnviarNotificacionFolioDocumentoEntrada.class);
		EnviarNotificacionFolioDocumentoSalida out 	= logica(con,in);
		JsonParser parser 							= new JsonParser();
        JsonObject dataout							= parser.parse(gson.toJson(out)).getAsJsonObject();
		
        return dataout;
		
	}
	
	public static void main(String[] args) {
		
		System.out.println("== INIT ==");
		try {
			
			Connection con = PlataformaUtils.getMerrillConnection();
			EnviarNotificacionFolioDocumentoEntrada entrada = new EnviarNotificacionFolioDocumentoEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        EnviarNotificacionFolioDocumento demo=new EnviarNotificacionFolioDocumento();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.rollback();
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
        System.out.println("== END ==");
        
	}
}
