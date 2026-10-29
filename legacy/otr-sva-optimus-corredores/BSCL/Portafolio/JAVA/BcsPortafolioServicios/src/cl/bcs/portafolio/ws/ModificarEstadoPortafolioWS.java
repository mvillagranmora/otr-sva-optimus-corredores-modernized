package cl.bcs.portafolio.ws;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ModificarEstadoPortafolioWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarEstadoPortafolioWS.class);
	
	private final String SQL_CALL_MODIFICAR_ESTADO_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_CallModificarEstadoPortafolio.sql");
	private final String SQL_SELECT_TIPOS_BLOQUEOS            = loadResourceToString("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectTiposBloqueos.sql");
	private final String SQL_SELECT_TIPOS_BLOQUEOS_TODOS      = loadResourceToString("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectTiposBloqueosTodos.sql");
	private final String SQL_SELECT_ESTADO_PORTAFOLIO         = loadResourceToString("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectEstadoPortafolio.sql");
	
	public static class EntradaModificarEstadoPortafolioWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_DocumentoIdentidad;
		public String p_CodigoPortafolio;
		public Long   p_IdPortafolio;
		public String p_IdEstado;
		public String p_IdTipo;
		public String p_Fecha;
		public String p_FechaHasta;
		public String p_Comentario;
		
	}
	
	public static class SalidaModificarEstadoPortafolioWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarEstadoPortafolioWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ErrorDetalle extends BcsPojo {
		public String	Codigo;
		public String	Descripcion;
	}
	
	public static class AdvertenciaDetalle extends BcsPojo {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}
	
	public static class EstadoPortafolioValido extends BcsPojo {
		public String IdEstado;
		public String NombreEntidad;		
	}

	public static class TipoValido extends BcsPojo {
		public String IdTipoBloqueo;
		public String IdEstado;
		public String IdAccion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarEstadoPortafolioWS jin = gson.fromJson(in.toString(), EntradaModificarEstadoPortafolioWS.class);
		SalidaModificarEstadoPortafolioWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarEstadoPortafolioWS logica(Connection con, EntradaModificarEstadoPortafolioWS entrada) throws Exception {
		logger.info("INICIO::SalidaModificarEstadoPortafolioWS");
		long inicioServicio = System.currentTimeMillis();
	try {
		SalidaModificarEstadoPortafolioWS out = new SalidaModificarEstadoPortafolioWS();
		
		// 1. Validaciones de formato (sin BD)
		out.P_OUT_MENSAJES.addAll(validarFormato(con, entrada));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		// 2. Validaciones de catálogo (IdEstado e IdTipo desde BD)
		out.P_OUT_MENSAJES.addAll(validarCatalogos(con, entrada));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) return out;	
		
		// 3. Validaciones de negocio
		out.P_OUT_MENSAJES.addAll(validarReglasNegocio(con, entrada));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) return out;

		// 4. Ejecutar procedimiento almacenado
		out = modificarEstadoPortafolio(con, entrada);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::SalidaModificarEstadoPortafolioWS", finServicio - inicioServicio);
		return out;
	} catch (Exception e) {
        logger.error("ERROR NO CONTROLADO en ModificarEstadoPortafolioWS.logica", e);
        throw e;
	}
	}
	
	//Validaciones de formato
	private List<MensajeSalida> validarFormato(Connection con, EntradaModificarEstadoPortafolioWS entrada) throws Exception {
		logger.debug("INICIO::ModificarEstadoPortafolioWS.validarFormato");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();

		
		if (entrada.p_IdEmpresa == null) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDEMPRESA_NULO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		}
		
		if (entrada.p_DocumentoIdentidad == null || entrada.p_DocumentoIdentidad.trim().isEmpty()) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DOCUMENTOIDENTIDAD_VACIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		} else if (!entrada.p_DocumentoIdentidad.trim().matches("^\\d{1,8}-[\\dkK]$")) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DOCUMENTOIDENTIDAD_FORMATO_INVALIDO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		}
		
		if (entrada.p_CodigoPortafolio == null || entrada.p_CodigoPortafolio.trim().isEmpty()) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CODIGOPORTAFOLIO_VACIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		} else if (!entrada.p_CodigoPortafolio.trim().matches("^[a-zA-Z0-9-]+$")) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CODIGOPORTAFOLIO_CARACTERES_INVALIDOS", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		}
		
		if (entrada.p_IdEstado == null || entrada.p_IdEstado.trim().isEmpty()) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDESTADO_VACIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		}
		
		if (entrada.p_IdTipo == null || entrada.p_IdTipo.trim().isEmpty()) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDTIPO_VACIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		} else if (!entrada.p_IdTipo.trim().matches("^[a-zA-Z0-9]+$")) {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDTIPO_CARACTERES_INVALIDOS", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		}

		// Fecha (debe ser válida)
		if (entrada.p_Fecha != null && !entrada.p_Fecha.trim().isEmpty()) {
			if (!entrada.p_Fecha.trim().matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")) {			
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHA_FORMATO_INVALIDO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			}
		}
		
		// Validación p_FechaHasta
		if (entrada.p_FechaHasta != null && !entrada.p_FechaHasta.trim().isEmpty()) {
		    if (!entrada.p_FechaHasta.trim().matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")) {		        
		        mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHAHASTA_FORMATO_INVALIDO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		        }
		}
		
		// Validación lógica entre fechas
		if (entrada.p_Fecha != null && entrada.p_FechaHasta != null &&
		    !entrada.p_Fecha.trim().isEmpty() && !entrada.p_FechaHasta.trim().isEmpty()) {
		    
		    try {
		        LocalDate desde = LocalDate.parse(entrada.p_Fecha);
		        LocalDate hasta = LocalDate.parse(entrada.p_FechaHasta);

		        if (hasta.isBefore(desde)) {		            
		            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_RANGO_FECHAS_INVALIDO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		        }
		    } catch (Exception e) {		   
		        mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_RANGO_FECHAS_INVALIDO_EXCEPCION", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		    }
		}
	
		if (entrada.p_Comentario == null || entrada.p_Comentario.trim().isEmpty()) {		
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_COMENTARIO_VACIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		} else {
			entrada.p_Comentario = entrada.p_Comentario.trim();

			if (entrada.p_Comentario.length() > 255) {
				entrada.p_Comentario = entrada.p_Comentario.substring(0, 255);
			}

			if (!entrada.p_Comentario.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\.,\\-\\(\\)]+$")) {				
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_COMENTARIO_CARACTERES_INVALIDOS", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoPortafolioWS.validarFormato", fin - inicio);
		return mensajes;
	}		

		@SuppressWarnings("unchecked")
		private List<MensajeSalida> validarCatalogos(Connection con, EntradaModificarEstadoPortafolioWS entrada) throws Exception {
			logger.debug("INICIO::ModificarEstadoPortafolioWS.validarCatalogos");
			long inicio = System.currentTimeMillis();

			List<MensajeSalida> mensajes = new ArrayList<>();
			SQL sql = new SQL();		
			
			HashMap <String,Object> paramsEstado = new HashMap<>();
			paramsEstado.put("IdEstado", entrada.p_IdEstado);
			List<EstadoPortafolioValido> estadoPortafolio = sql.query(con, SQL_SELECT_ESTADO_PORTAFOLIO, paramsEstado, EstadoPortafolioValido.class);
					
			if (estadoPortafolio.isEmpty()) {
			    HashMap<String,String> parametros = new HashMap<>();
			    parametros.put("IdEstado", entrada.p_IdEstado);
			    mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDESTADO_NO_EXISTE", parametros, con, entrada.p_IdEmpresa));
			    return mensajes;
			}		
						
			HashMap <String,Object> params = new HashMap<>();
			params.put("IdTipo", entrada.p_IdTipo);
			List<TipoValido> tiposBloqueos = sql.query(con, SQL_SELECT_TIPOS_BLOQUEOS, params, TipoValido.class);
			
			if (tiposBloqueos.isEmpty()) {
			    List<TipoValido> todosLosTipos = sql.query(con, SQL_SELECT_TIPOS_BLOQUEOS_TODOS, new HashMap<>(), TipoValido.class);
			    List<String> idsTiposValidos = todosLosTipos.stream()
			            .map(t -> t.IdTipoBloqueo)
			            .collect(Collectors.toList());
			    HashMap<String, String> parametros = new HashMap<>();
			    parametros.put("IdTipo", entrada.p_IdTipo);
			    parametros.put("TiposValidos", String.join(", ", idsTiposValidos));
			    mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDTIPO_NO_EXISTE", parametros, con, entrada.p_IdEmpresa));
			    return mensajes;
			}
			
			if (!tiposBloqueos.isEmpty()) {
			    
			    boolean esValido = tiposBloqueos.stream().anyMatch(t -> entrada.p_IdEstado.equalsIgnoreCase(t.IdAccion));
			            
			    if (!esValido) {
			        HashMap<String,String> parametros = new HashMap<>();
			        parametros.put("IdTipo", entrada.p_IdTipo);
			        parametros.put("IdEstado", entrada.p_IdEstado);
			        mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IDTIPO_NO_CORRESPONDE_IDESTADO", parametros, con, entrada.p_IdEmpresa));
			        return mensajes;
			    }
			}

			long fin = System.currentTimeMillis();
			logger.debug("FIN::ModificarEstadoPortafolioWS.validarCatalogos", fin - inicio);
			return mensajes;
		}
		
		private List<MensajeSalida> validarReglasNegocio(Connection con, EntradaModificarEstadoPortafolioWS entrada) throws Exception {

		    logger.debug("INICIO::ModificarEstadoPortafolioWS.validarReglasNegocio");
		    long inicio = System.currentTimeMillis();
		    
		    String idEstado = entrada.p_IdEstado != null ? entrada.p_IdEstado.trim().toUpperCase() : "";
		    String idTipo = entrada.p_IdTipo != null ? entrada.p_IdTipo.trim().toUpperCase() : "";		            

		    boolean permiteFechaLibre = "BLO".equals(idEstado) && "BMUERT".equals(idTipo);
		           
		    List<MensajeSalida> mensajes = new ArrayList<>();
		    
		    String fechaSistema = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		    
		    if (fechaSistema != null && fechaSistema.length() >= 10) {
		        fechaSistema = fechaSistema.substring(0, 10);
		    }
		    	   
		    if (entrada.p_Fecha == null || entrada.p_Fecha.trim().isEmpty()) {

		    	mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHA_OBLIGATORIA", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		    	return mensajes;

		    } else if (!permiteFechaLibre && !fechaSistema.equals(entrada.p_Fecha.trim())) {
		            
		    	mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHA_NO_CORRESPONDE", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
		    	return mensajes;
		    }

		    boolean tieneFechaHasta = entrada.p_FechaHasta != null && !entrada.p_FechaHasta.trim().isEmpty();
		    
		    //TEMPORALES obligatorios
		    if (Arrays.asList("BTEMPO", "DESBTEMPORAL").contains(idTipo)) {

		        if (!tieneFechaHasta) {
		        	HashMap<String,String> parametros = new HashMap<>();
		            parametros.put("IdTipo", idTipo);		            
		            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHAHASTA_OBLIGATORIA", parametros, con, entrada.p_IdEmpresa));
		            return mensajes;
		        }
		    }
		    else {
		        if (tieneFechaHasta) {
		        	HashMap<String,String> parametros = new HashMap<>();
		            parametros.put("IdTipo", idTipo);
		            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FECHAHASTA_NO_PERMITIDA", parametros, con, entrada.p_IdEmpresa));
		            return mensajes;
		        }
		    }

		    long fin = System.currentTimeMillis();
		    logger.debug("FIN::ModificarEstadoPortafolioWS.validarReglasNegocio", fin - inicio);

		    return mensajes;
		}
		
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaModificarEstadoPortafolioWS modificarEstadoPortafolio(Connection con, EntradaModificarEstadoPortafolioWS entrada) throws Exception {
		logger.debug("INICIO::ModificarEstadoPortafolioWS.modificarEstadoPortafolio");
		long inicio = System.currentTimeMillis();

		String fechaDesde;
		
		boolean permiteFechaLibre = "BLO".equalsIgnoreCase(entrada.p_IdEstado) && "BMUERT".equalsIgnoreCase(entrada.p_IdTipo);
		        
		if (permiteFechaLibre) {
		    fechaDesde = entrada.p_Fecha;
		} else {
		    fechaDesde = obtenerFechaSistema(con, entrada.p_IdEmpresa);

		    if (fechaDesde != null && fechaDesde.length() >= 10) {
		        fechaDesde = fechaDesde.substring(0, 10);
		    }
		}       
		
		String fechaHasta = null;
		
		if (entrada.p_FechaHasta != null && !entrada.p_FechaHasta.trim().isEmpty()) {
		    fechaHasta = entrada.p_FechaHasta;
		}
		
		SalidaModificarEstadoPortafolioWS salidaModificarEstadoPortafolioWS = new SalidaModificarEstadoPortafolioWS();
		HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>();
		parametrosEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", entrada.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", entrada.p_IdModulo);
		parametrosEntrada.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
		parametrosEntrada.put("p_IdPortafolio", entrada.p_IdPortafolio);
		parametrosEntrada.put("p_IdEstado", entrada.p_IdEstado);
		parametrosEntrada.put("p_IdTipo", entrada.p_IdTipo);
		parametrosEntrada.put("p_Fecha", fechaDesde);
		parametrosEntrada.put("p_FechaHasta", fechaHasta);
		parametrosEntrada.put("p_Comentario", entrada.p_Comentario);
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_RefId", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
	    
	    SQL sql = new SQL();
	    HashMap<String, Object> respuestaModificarEstadoPortafolio = sql.call(con, SQL_CALL_MODIFICAR_ESTADO_PORTAFOLIO, parametrosEntrada, parametrosSalida);
	    
	    List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaModificarEstadoPortafolio.get("p_out_ErroresDetalle");
	    List<AdvertenciaDetalle> advertenciasDetalle = (List<AdvertenciaDetalle>) respuestaModificarEstadoPortafolio.get("p_out_AdvertenciasDetalle");
	    
	    salidaModificarEstadoPortafolioWS.P_OUT_MENSAJES.addAll(obtenerMensajesErrores(erroresDetalle));
	    salidaModificarEstadoPortafolioWS.P_OUT_MENSAJES.addAll(obtenerMensajesAdvertencia(advertenciasDetalle));
	    salidaModificarEstadoPortafolioWS.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarEstadoPortafolioWS.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoPortafolioWS.modificarEstadoPortafolio", fin - inicio);
		return salidaModificarEstadoPortafolioWS;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ModificarEstadoPortafolioWS.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarEstadoPortafolioWS.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}	
	
	private List<MensajeSalida> obtenerMensajesErrores(List<ErrorDetalle> erroresDetalle){
		logger.debug("INICIO::ModificarEstadoPortafolioWS.obtenerMensajesErrores");
		long inicio = System.currentTimeMillis();
		 List<MensajeSalida> mensajesError = erroresDetalle
		    		.stream()
		    		.map(errorDetalle->{
				    	MensajeSalida mensajeSalida = new MensajeSalida();
				    	mensajeSalida.id = 0L;
				    	mensajeSalida.tipo = MensajeSalida.MENSAJE_ERROR;
				    	mensajeSalida.codigo = errorDetalle.Codigo;
				    	mensajeSalida.descripcion = errorDetalle.Descripcion;
				    	return mensajeSalida;
		    		})
		    		.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoPortafolioWS.obtenerMensajesErrores", fin - inicio);
		return mensajesError;
	}
	
	private List<MensajeSalida> obtenerMensajesAdvertencia(List<AdvertenciaDetalle> advertenciasDetalle){
		logger.debug("INICIO::ModificarEstadoPortafolioWS.obtenerMensajesAdvertencia");
		long inicio = System.currentTimeMillis();
		 List<MensajeSalida> mensajesError = advertenciasDetalle
		    		.stream()
		    		.map(advertenciaDetalle->{
				    	MensajeSalida mensajeSalida = new MensajeSalida();
				    	mensajeSalida.id = 0L;
				    	mensajeSalida.tipo = "S".equals(advertenciaDetalle.RequiereAutorizacion) ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ADVERTENCIA;
				    	mensajeSalida.codigo = advertenciaDetalle.Codigo;
				    	mensajeSalida.descripcion = advertenciaDetalle.Descripcion;
				    	return mensajeSalida;
		    		})
		    		.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoPortafolioWS.obtenerMensajesAdvertencia", fin - inicio);
		return mensajesError;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa", "connections.properties")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76521,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_DocumentoIdentidad\":\"10008596-8\",\"p_CodigoPortafolio\":\"69\",\"p_IdPortafolio\":315756,\"p_IdEstado\":\"BLO\",\"p_IdTipo\":\"BCOBRANZA\",\"p_Fecha\":\"\",\"p_Comentario\":\"SADASDAS\"}";
		
		EntradaModificarEstadoPortafolioWS Objin = gson.fromJson(json, EntradaModificarEstadoPortafolioWS.class);
		ModificarEstadoPortafolioWS demo = new ModificarEstadoPortafolioWS();				
		try {
			SalidaModificarEstadoPortafolioWS ObjOut = demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.rollback();
			}else {
				con.rollback();
			}
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
			//con.commit();
    	}
		System.out.println("== END ==");
	}

}
