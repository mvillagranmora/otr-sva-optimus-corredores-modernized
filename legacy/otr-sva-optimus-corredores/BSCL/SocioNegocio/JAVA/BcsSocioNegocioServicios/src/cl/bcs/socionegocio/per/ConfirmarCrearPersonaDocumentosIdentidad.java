package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearPersonaDocumentosIdentidad implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaDocumentosIdentidad.class);
	
	private final String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDocumentosIdentidad_SelectPersona.sql");
	private final String SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDocumentosIdentidad_UpdateDocumentoIdentidadNoPredeterminado.sql");
	private final String SQL_UPDATE_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDocumentosIdentidad_UpdatePersona.sql");
	private final String SQL_UPDATE_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDocumentosIdentidad_UpdateDocumentoIdentidad.sql");
	
	public static class EntradaConfirmarCrearPersonaDocumentosIdentidad extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDocumentoIdentidad;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearPersonaDocumentosIdentidad extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarCrearPersonaDocumentosIdentidad() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Persona extends BcsPojo{
		public Long IdPersona;
		public Long IdDocumentoIdentidad;
	}
	
	public static class DocumentoIdentidad extends BcsPojo{
		public Long IdPersona;
		public Long IdDocumentoIdentidad;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearPersonaDocumentosIdentidad jin = gson.fromJson(in.toString(), EntradaConfirmarCrearPersonaDocumentosIdentidad.class);
		SalidaConfirmarCrearPersonaDocumentosIdentidad out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarCrearPersonaDocumentosIdentidad logica(Connection con, EntradaConfirmarCrearPersonaDocumentosIdentidad entrada) throws Exception {
		logger.info("INICIO::ConfirmarCrearPersonaDocumentosIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarCrearPersonaDocumentosIdentidad out = new SalidaConfirmarCrearPersonaDocumentosIdentidad();
		Persona persona = obtenerPersona(con, entrada.p_IdDocumentoIdentidad);
		
		if(persona == null) {
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DOCUMENTO_IDENTIDAD_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		actualizarDocumentoIdentidadNoPredeterminado(con, persona);
		actualizarPersona(con, persona, entrada.p_IdDocumentoIdentidad);
		DocumentoIdentidad documentoIdentidad = obtenerDocumentoIdentidadActualizar(persona, entrada.p_IdDocumentoIdentidad, entrada.p_IdEstadoAutorizacion);
		actualizarDocumentoIdentidad(con, documentoIdentidad);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearPersonaDocumentosIdentidad", finServicio - inicioServicio);
		return out;
	}
	
	private Persona obtenerPersona(Connection con, Long idDocumentoIdentidad) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaDocumentosIdentidad.obtenerPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdDocumentoIdentidad", idDocumentoIdentidad);
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_PERSONA, parametros, Persona.class);
		Persona persona = personas.parallelStream().findFirst().orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearPersonaDocumentosIdentidad.obtenerPersona", fin - inicio);
		return persona;
	}
	
	private void actualizarDocumentoIdentidadNoPredeterminado(Connection con, Persona persona) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Persona> personas = Arrays.asList(persona);
		sql.batch(con, SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS, personas, Persona.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado", fin - inicio);
	}
	
	private void actualizarPersona(Connection con, Persona persona, Long idDocumentoIdentidad) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		Persona personaActualizar = new Persona();
		personaActualizar.IdPersona = persona.IdPersona;
		personaActualizar.IdDocumentoIdentidad = idDocumentoIdentidad;
		List<Persona> personas = Arrays.asList(personaActualizar);
		sql.batch(con, SQL_UPDATE_PERSONA, personas, Persona.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado", fin - inicio);
	}
	
	private DocumentoIdentidad obtenerDocumentoIdentidadActualizar(Persona persona, Long idDocumentodIdentidad, String idEstadoAutorizacion) {
		logger.trace("INICIO::ConfirmarCrearPersonaDocumentosIdentidad.obtenerDocumentoIdentidadActualizar");
		long inicio = System.currentTimeMillis();
		DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
		documentoIdentidad.IdPersona = persona.IdPersona;
		documentoIdentidad.IdDocumentoIdentidad = idDocumentodIdentidad;
		documentoIdentidad.IdEstadoAutorizacion = idEstadoAutorizacion;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarCrearPersonaDocumentosIdentidad.obtenerDocumentoIdentidadActualizar", fin - inicio);
		return documentoIdentidad;
		
	}
	
	private void actualizarDocumentoIdentidad(Connection con, DocumentoIdentidad documentoIdentiodad) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<DocumentoIdentidad> documentosIdentidad = Arrays.asList(documentoIdentiodad);
		sql.batch(con, SQL_UPDATE_DOCUMENTO_IDENTIDAD, documentosIdentidad, DocumentoIdentidad.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearPersonaDocumentosIdentidad.actualizarDocumentoIdentidadNoPredeterminado", fin - inicio);
	}

}
