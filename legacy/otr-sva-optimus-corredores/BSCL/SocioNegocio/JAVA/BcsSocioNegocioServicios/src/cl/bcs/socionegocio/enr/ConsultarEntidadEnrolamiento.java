package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.Enrolamiento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.EntradaValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEntidadEnrolamiento implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarEntidadEnrolamiento.class);
	
	private String SQL_SELECT_COUNT_DOCUMENTO_ADICIONAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectCountDocumentoAdicional.sql");
	private String SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectPersonaDocumentoIdentidad.sql");
	private String SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD_ADICIONAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectPersonaDocumentoIdentidadAdicional.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectCliente.sql");
	private String SQL_SELECT_EMPLEADO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectEmpleado.sql");
	private String SQL_SELECT_CONTRAPARTE = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectContraparte.sql");
	private String SQL_SELECT_PROVEEDOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectProveedor.sql");
	private String SQL_SELECT_ENROLAMIENTO_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectEnrolamientoDocumentoIdentidad.sql");
	private String SQL_SELECT_ENROLAMIENTO_DOCUMENTO_IDENTIDAD_ADICIONAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectEnrolamientoDocumentoIdentidadAdicional.sql");
	private String SQL_SELECT_COUNT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectCountEnrolamiento.sql");
	private String SQL_SELECT_COUNT_DOCUMENTO_IDENTIDAD_FICTICIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectDocumentoIdentidadFicticio.sql");
	private String SQL_SELECT_CONSULTAR_ENROLAMIENTO_PERSONA_EXISTE = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectConsultarEnrolamientoPersonaExiste.sql");
	private String SQL_SELECT_CONSULTAR_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarEntidadEnrolamiento_SelectConsultarEnrolamiento.sql");
	
	public static class EntradaConsultarEntidadEnrolamiento extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_IdTipoEntidad;
		public String p_IdTipoDocumentoIdentidad;
		public String p_DocumentoIdentidad;
		public String p_IdTipoDocumentoIdentidadAdicional;
		public String p_DocumentoIdentidadAdicional;
		public String p_IdTipoPersona;
		public String p_esRutInterno;
		
		public EntradaConsultarEntidadEnrolamiento() {
			this.p_IdTipoDocumentoIdentidadAdicional = "";
			this.p_DocumentoIdentidadAdicional = "";
		}
	}
	
	public static class SalidaConsultarEntidadEnrolamiento extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public String P_OUT_PERSONAEXISTE;
		public Long P_OUT_IDPERSONA;
		public String P_OUT_ENTIDADEXISTE;
		public String P_OUT_DOCUMENTOIDENTIDADFICTICIO;
		public List<Enrolamiento> P_OUT_CONSULTARENROLAMIENTO;
		
		public SalidaConsultarEntidadEnrolamiento() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CONSULTARENROLAMIENTO = new ArrayList<Enrolamiento>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	public static class Persona extends BcsPojo{
		public Long IdPersona;
		public String PersonaExiste;
		
		public Persona() {
			this.IdPersona = 0L;
			this.PersonaExiste = "N";
		}
	}
	
	public static class Entidad extends BcsPojo{
		public Long IdEntidad;
		public String EntidadExiste;
		
		public Entidad() {
			this.IdEntidad = 0L;
			this.EntidadExiste = "N";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarEntidadEnrolamiento jin = gson.fromJson(in.toString(), EntradaConsultarEntidadEnrolamiento.class);
		SalidaConsultarEntidadEnrolamiento out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarEntidadEnrolamiento logica(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.info("INICIO::ConsultarEntidadEnrolamiento");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarEntidadEnrolamiento out = new SalidaConsultarEntidadEnrolamiento();
		String codigoPais = obtenerCodigoPais(con, entrada.p_IdEmpresa);
		
		List<Validacion> validacionesDocumentoIdentidadAdicional = validarDocumentoIdentidadAdicional(con, entrada);
		out = agregarValidacionesSalida(out, validacionesDocumentoIdentidadAdicional);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		Validacion validacionDocumentoIdentidad = validarDocumentoIdentidad(con, entrada);
		out = agregarValidacionSalida(out, validacionDocumentoIdentidad);
		
		Persona persona = obtenerPersona(con, entrada);
		out.P_OUT_IDPERSONA = persona.IdPersona;
		out.P_OUT_PERSONAEXISTE = persona.PersonaExiste;
		
		Entidad entidad = obtenerEntidad(con, entrada, persona);
		out.P_OUT_ENTIDADEXISTE = entidad.EntidadExiste;
		
		String documentoIdentidadFicticioFormateado = obtenerDocumentoIdentidadFicticio(con, entrada.p_IdEmpresa, entrada.p_DocumentoIdentidad, entrada.p_IdTipoPersona, persona.PersonaExiste);
		if(documentoIdentidadFicticioFormateado != null) entrada.p_DocumentoIdentidad = String.valueOf(documentoIdentidadFicticioFormateado);
		
		if ("PE".equals(codigoPais)) {
			out.P_OUT_DOCUMENTOIDENTIDADFICTICIO = documentoIdentidadFicticioFormateado;
		}
		
		out.P_OUT_CONSULTARENROLAMIENTO = obtenerConsultaEnrolamiento(con, entrada, persona, entidad);
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarEntidadEnrolamiento", finServicio - inicioServicio);
		return out;
	}
	
	private String obtenerCodigoPais(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.obtenerCodigoPais");
		long inicio = System.currentTimeMillis();
		String codigoPais = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.LocalizacionSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.obtenerCodigoPais", fin - inicio);
		return codigoPais;
	}
	
	private List<Validacion> validarDocumentoIdentidadAdicional(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<Validacion>();
		String codigoPais = obtenerCodigoPais(con, entrada.p_IdEmpresa);
		
		if("PE".equals(codigoPais)) {
			Validacion validacionDocumentoIdentidadAdicionalExiste = validarDocumentoIdentidadAdicionalExiste(con, entrada);
			Validacion validacionDocumentoIdentidadAdicionalObligatorio = validarDocumentoIdentidadAdicionalObligatorio(con, entrada);
			validaciones.add(validacionDocumentoIdentidadAdicionalExiste);
			validaciones.add(validacionDocumentoIdentidadAdicionalObligatorio);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicional", fin - inicio);
		return validaciones;
	}
	
	private Validacion validarDocumentoIdentidadAdicionalExiste(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicionalExiste");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("DocumentoIdentidadAdicional", entrada.p_DocumentoIdentidadAdicional);
		parametros.put("IdTipoDocumentoIdentidadAdicional", entrada.p_IdTipoDocumentoIdentidadAdicional);
		parametros.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
		
		SQL sql = new SQL();
		Long count = sql.countQuery(con, SQL_SELECT_COUNT_DOCUMENTO_ADICIONAL, parametros);
		
		Validacion validacion = new Validacion();
		if(count > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTO_ADICIONAL_EXISTE", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicionalExiste", fin - inicio);
		return validacion;
	}
	
	private Validacion validarDocumentoIdentidadAdicionalObligatorio(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicionalObligatorio");
		long inicio = System.currentTimeMillis();
		Validacion validacion = new Validacion();
		if(esDocumentoIdentidadAdicionalIncorrecto(entrada.p_IdTipoEntidad, entrada.p_DocumentoIdentidadAdicional, entrada.p_esRutInterno)) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTO_ADICIONAL_OBLIGATORIO", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.validarDocumentoIdentidadAdicionalObligatorio", fin - inicio);
		return validacion;
	}
	
	private Boolean esDocumentoIdentidadAdicionalIncorrecto(String idTipoEntidad, String documentoIdentidadAdicional, String esRutInterno){
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.esDocumentoIdentidadAdicionalIncorrecto");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadAdicionalIncorrecto = "CLIENTE".equals(idTipoEntidad) && documentoIdentidadAdicional != null && !documentoIdentidadAdicional.isEmpty() && "N".equals(esRutInterno);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.esDocumentoIdentidadAdicionalIncorrecto", fin - inicio);
		return esDocumentoIdentidadAdicionalIncorrecto;
	}
	
	private SalidaConsultarEntidadEnrolamiento agregarValidacionSalida(SalidaConsultarEntidadEnrolamiento salida, Validacion validacion) {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	private SalidaConsultarEntidadEnrolamiento agregarValidacionesSalida(SalidaConsultarEntidadEnrolamiento salida, List<Validacion> validaciones) {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.agregarValidacionesSalida");
		long inicio = System.currentTimeMillis();
		for(Validacion validacion : validaciones) {
			salida = agregarValidacionSalida(salida, validacion);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.agregarValidacionesSalida", fin - inicio);
		return salida;
	}
	
	private Validacion validarDocumentoIdentidad(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception{
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.validarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		EntradaValidarDocumentoIdentidad entradaValidarDocumentoIdentidad = new EntradaValidarDocumentoIdentidad();
		entradaValidarDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarDocumentoIdentidad.p_Entidad = entrada.p_IdTipoEntidad;
		entradaValidarDocumentoIdentidad.p_TipoDocumentoIdentidad = entrada.p_IdTipoDocumentoIdentidad;
		entradaValidarDocumentoIdentidad.p_DocumentoIdentidad = entrada.p_DocumentoIdentidad;
		entradaValidarDocumentoIdentidad.p_TipoPersona = entrada.p_IdTipoPersona;
		
		ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
		SalidaValidarDocumentoIdentidad salidaValidarDocumentoIdentidad = validarDocumentoIdentidad.logica(con, entradaValidarDocumentoIdentidad.p_IdEmpresa, entradaValidarDocumentoIdentidad.p_Entidad, entradaValidarDocumentoIdentidad.p_TipoDocumentoIdentidad, entradaValidarDocumentoIdentidad.p_DocumentoIdentidad, entradaValidarDocumentoIdentidad.p_TipoPersona);
		
		Validacion validacion = new Validacion();
		if("N".equals(salidaValidarDocumentoIdentidad.Valido)) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTO_IDENTIDAD_INVALIDO", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.validarDocumentoIdentidad", fin - inicio);
		return validacion;
	}
	
	private Persona obtenerPersona(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception{
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.obtenerPersona");
		long inicio = System.currentTimeMillis();
		
		String codigoPais = obtenerCodigoPais(con, entrada.p_IdEmpresa);
		
		if(esDocumentoIdentidadValido(entrada.p_DocumentoIdentidad)) {
			Persona persona = obtenerPersonaPorDocumentoIdentidad(con, entrada, codigoPais);
			return persona;
		}
		
		if(esDocumentoIdentidadValido(entrada.p_DocumentoIdentidadAdicional)) {
			Persona persona = obtenerPersonaPorDocumentoIdentidadAdicional(con, entrada, codigoPais);
			return persona;
		}
		
		Persona persona = new Persona();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.obtenerPersona", fin - inicio);
		return persona;
	}
	
	private Boolean esDocumentoIdentidadValido(String documentoIdentidad) {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.esDocumentoIdentidadValido");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadValido = documentoIdentidad != null && !documentoIdentidad.isEmpty();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.esDocumentoIdentidadValido", fin - inicio);
		return esDocumentoIdentidadValido;
	}
	
	private Persona obtenerPersonaPorDocumentoIdentidad(Connection con, EntradaConsultarEntidadEnrolamiento entrada, String codigoPais) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(4);
		parametros.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidad", entrada.p_IdTipoDocumentoIdentidad);
		parametros.put("IdTipoPersona", entrada.p_IdTipoPersona);
		parametros.put("CodigoPais", codigoPais);
		
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD, parametros, Persona.class);
		
		Persona persona = personas.parallelStream().findFirst().orElse(new Persona());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidad", fin - inicio);
		return persona;
	}
	
	private Persona obtenerPersonaPorDocumentoIdentidadAdicional(Connection con, EntradaConsultarEntidadEnrolamiento entrada, String codigoPais) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(4);
		parametros.put("DocumentoIdentidad", entrada.p_DocumentoIdentidadAdicional);
		parametros.put("IdTipoDocumentoIdentidad", entrada.p_IdTipoDocumentoIdentidadAdicional);
		parametros.put("IdTipoPersona", entrada.p_IdTipoPersona);
		parametros.put("CodigoPais", codigoPais);
		
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD_ADICIONAL, parametros, Persona.class);
		
		Persona persona = personas.parallelStream().findFirst().orElse(new Persona());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidadAdicional", fin - inicio);
		return persona;
	}
	
	private Entidad obtenerEntidad(Connection con, EntradaConsultarEntidadEnrolamiento entrada, Persona persona) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		if("S".equals(persona.PersonaExiste)) {
			
			if("PERSONA".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = new Entidad();
				entidad.IdEntidad = persona.IdPersona;
				entidad.EntidadExiste = persona.PersonaExiste;
				return entidad;
			}
			
			if("CLIENTE".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = obtenerEntidadCliente(con, persona.IdPersona, entrada.p_IdEmpresa);
				return entidad;
			}

			if("EMPLEADO".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = obtenerEntidadEmpleado(con, persona.IdPersona, entrada.p_IdEmpresa);
				return entidad;
			}
			
			if("CONTRAPARTE".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = obtenerEntidadContraparte(con, persona.IdPersona, entrada.p_IdEmpresa);
				return entidad;
			}
			
			if("PROVEEDOR".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = obtenerEntidadProveedor(con, persona.IdPersona, entrada.p_IdEmpresa);
				return entidad;
			}
			
			if("ENROLAMIENTO".equals(entrada.p_IdTipoEntidad)) {
				Entidad entidad = obtenerEntidadEnrolamiento(con, entrada);
				return entidad;
			}
		}
		
		Entidad entidad = new Entidad();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidadAdicional", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEntidadCliente(Connection con, Long idPersona, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEntidadCliente");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersona", idPersona);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_CLIENTE, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEntidadCliente", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEntidadEmpleado(Connection con, Long idPersona, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEntidadEmpleado");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersona", idPersona);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_EMPLEADO, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEntidadEmpleado", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEntidadContraparte(Connection con, Long idPersona, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEntidadContraparte");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersona", idPersona);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_CONTRAPARTE, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEntidadContraparte", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEntidadProveedor(Connection con, Long idPersona, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEntidadProveedor");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersona", idPersona);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_PROVEEDOR, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEntidadProveedor", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEntidadEnrolamiento(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.obtenerEntidadEnrolamiento");
		long inicio = System.currentTimeMillis();
		if(esDocumentoIdentidadValido(entrada.p_DocumentoIdentidad)) {
			Entidad entidad = obtenerEnrolamientoPorDocumentoIdentidad(con, entrada.p_DocumentoIdentidad, entrada.p_IdTipoDocumentoIdentidad);
			return entidad;
		}
		
		if(esDocumentoIdentidadValido(entrada.p_DocumentoIdentidadAdicional)) {
			Entidad entidad = obtenerEnrolamientoPorDocumentoIdentidadAdicional(con, entrada.p_DocumentoIdentidadAdicional, entrada.p_IdTipoDocumentoIdentidadAdicional);
			return entidad;
		}
		
		Entidad entidad = new Entidad();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.obtenerEntidadEnrolamiento", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEnrolamientoPorDocumentoIdentidad(Connection con, String documentoIdentidad, String idTipoDocumentoIdentidad) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("DocumentoIdentidad", documentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidad", idTipoDocumentoIdentidad);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_ENROLAMIENTO_DOCUMENTO_IDENTIDAD, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerPersonaPorDocumentoIdentidad", fin - inicio);
		return entidad;
	}
	
	private Entidad obtenerEnrolamientoPorDocumentoIdentidadAdicional(Connection con, String documentoIdentidad, String idTipoDocumentoIdentidad) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEnrolamientoPorDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("DocumentoIdentidad", documentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidad", idTipoDocumentoIdentidad);
		
		SQL sql = new SQL();
		List<Entidad> entidades = sql.query(con, SQL_SELECT_ENROLAMIENTO_DOCUMENTO_IDENTIDAD_ADICIONAL, parametros, Entidad.class);
		
		Entidad entidad = entidades.parallelStream().findFirst().orElse(new Entidad());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEnrolamientoPorDocumentoIdentidadAdicional", fin - inicio);
		return entidad;
	}
	
	private String obtenerDocumentoIdentidadFicticio(Connection con, Long IdEmpresa, String documentoIdentidad, String idTipoPersona, String personaExiste) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerEnrolamientoPorDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		String documentoIdentidadFicticioFormateado = null;
		
		String codigoPais = obtenerCodigoPais(con, IdEmpresa);
		
		if("N".equals(personaExiste) && "PE".equals(codigoPais)) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(2);
			parametros.put("DocumentoIdentidad", documentoIdentidad);
			parametros.put("IdTipoPersona", idTipoPersona);
			
			SQL sql = new SQL();
			Long countEnrolamiento = sql.countQuery(con, SQL_SELECT_COUNT_ENROLAMIENTO, parametros);
			
			if (countEnrolamiento.equals(0L)) {

			    Long countDocumentoIdentidadFicticio;
				Long documentoIdentidadFicticio = null;

			    do {
			    	List<Long> secuenciaDocumentoFicticio = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DocumentoFicticio", 1L);
			        documentoIdentidadFicticio = secuenciaDocumentoFicticio.get(0);
			        documentoIdentidadFicticioFormateado = String.format("%08d", documentoIdentidadFicticio);
			        parametros.clear();
			        parametros.put("DocumentoIdentidadFicticio", documentoIdentidadFicticioFormateado);
			        countDocumentoIdentidadFicticio = sql.countQuery(con, SQL_SELECT_COUNT_DOCUMENTO_IDENTIDAD_FICTICIO, parametros);
			    } while (countDocumentoIdentidadFicticio > 0);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerEnrolamientoPorDocumentoIdentidadAdicional", fin - inicio);
		return documentoIdentidadFicticioFormateado;
	}
	
	private List<Enrolamiento> obtenerConsultaEnrolamiento(Connection con, EntradaConsultarEntidadEnrolamiento entrada, Persona persona, Entidad entidad) throws Exception {
		logger.trace("INICIO::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento");
		long inicio = System.currentTimeMillis();
		List<Enrolamiento> consultarEnrolamiento = new ArrayList<Enrolamiento>();
		if("N".equals(entidad.EntidadExiste)) {
			if("S".equals(persona.PersonaExiste)) {
				consultarEnrolamiento = obtenerBorradorPorPersona(con, entrada, persona);
				return consultarEnrolamiento;
			}
			
			consultarEnrolamiento = obtenerBorradorPorEnrolamiento(con, entrada);
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento", fin - inicio);
		return consultarEnrolamiento;
	}
	
	private List<Enrolamiento> obtenerBorradorPorPersona(Connection con, EntradaConsultarEntidadEnrolamiento entrada, Persona persona) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(7);
		parametros.put("IdTipoEntidad", entrada.p_IdTipoEntidad);
		parametros.put("IdTipoDocumentoIdentidad", entrada.p_IdTipoDocumentoIdentidad);
		parametros.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidadAdicional", entrada.p_IdTipoDocumentoIdentidadAdicional);
		parametros.put("DocumentoIdentidadAdicional", entrada.p_DocumentoIdentidadAdicional);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdPersona", persona.IdPersona);
		
		SQL sql = new SQL();
		List<Enrolamiento> consultarEnrolamiento = sql.query(con, SQL_SELECT_CONSULTAR_ENROLAMIENTO_PERSONA_EXISTE, parametros, Enrolamiento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento", fin - inicio);
		return consultarEnrolamiento;
	}
	
	private List<Enrolamiento> obtenerBorradorPorEnrolamiento(Connection con, EntradaConsultarEntidadEnrolamiento entrada) throws Exception {
		logger.debug("INICIO::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento");
		long inicio = System.currentTimeMillis();
		
		String codigoPais = obtenerCodigoPais(con, entrada.p_IdEmpresa);
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(8);
		parametros.put("IdTipoEntidad", entrada.p_IdTipoEntidad);
		parametros.put("IdTipoDocumentoIdentidad", entrada.p_IdTipoDocumentoIdentidad);
		parametros.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidadAdicional", entrada.p_IdTipoDocumentoIdentidadAdicional);
		parametros.put("DocumentoIdentidadAdicional", entrada.p_DocumentoIdentidadAdicional);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("CodigoPais", codigoPais);
		parametros.put("IdTipoPersona", entrada.p_IdTipoPersona);
		
		SQL sql = new SQL();
		List<Enrolamiento> consultarEnrolamiento = sql.query(con, SQL_SELECT_CONSULTAR_ENROLAMIENTO, parametros, Enrolamiento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerConsultaEnrolamiento", fin - inicio);
		return consultarEnrolamiento;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CLI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoEntidad\":\"PERSONA\",\"p_IdTipoDocumentoIdentidad\":\"R\",\"p_DocumentoIdentidad\":\"21284888-3\",\"p_IdTipoPersona\":\"N\",\"p_esRutInterno\":\"N\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CLI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoEntidad\":\"CLIENTE\",\"p_IdTipoDocumentoIdentidad\":\"R\",\"p_DocumentoIdentidad\":\"21284888-3\",\"p_DocumentoIdentidadAdicional\":\"\",\"p_IdTipoDocumentoIdentidadAdicional\":\"R\",\"p_IdTipoPersona\":\"N\",\"p_esRutInterno\":\"N\"}";
			
			
			EntradaConsultarEntidadEnrolamiento Objin = gson.fromJson(json, EntradaConsultarEntidadEnrolamiento.class);

			ConsultarEntidadEnrolamiento demo = new ConsultarEntidadEnrolamiento();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaConsultarEntidadEnrolamiento ObjOut = demo.logica(con, Objin);
				con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
