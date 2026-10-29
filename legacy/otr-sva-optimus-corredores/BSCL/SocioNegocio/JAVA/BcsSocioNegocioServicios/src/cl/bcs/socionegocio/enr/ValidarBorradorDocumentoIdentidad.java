package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarBorradorDocumentoIdentidad implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorDireccionFisica.class);
	
	private String SQL_INSERT_TEMPORAL_CREAR_BORRADOR_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarBorradorDocumentoIdentidad_InsertTemporalCrearBorradorDocumentoIdentidad.sql");
	private String SQL_DELETE_TEMPORAL_CREAR_BORRADOR_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarBorradorDocumentoIdentidad_DeleteTemporalCrearBorradorDocumentoIdentidad.sql");
	private String SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTENTES = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarBorradorDocumentoIdentidad_SelectDocumentoIdentidadExistentes.sql");
	private String SQL_SELECT_TIPO_DOCUMENTO_IDENTIDAD_EXISTENTES = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarBorradorDocumentoIdentidad_SelectTipoDocumentoIdentidadExistentes.sql");
	
	public static class EntradaValidarBorradorDocumentoIdentidad extends BcsPojo{
		public Long IdEmpresa;
		public List<DocumentoIdentidad> p_DocumentosIdentidad;
		
		public EntradaValidarBorradorDocumentoIdentidad() {
			this.p_DocumentosIdentidad = new ArrayList<DocumentoIdentidad>();
		}
	}
	
	public static class SalidaValidarBorradorDocumentoIdentidad extends BcsPojo{
		public List<Validacion> Validaciones;
		
		public SalidaValidarBorradorDocumentoIdentidad() {
			this.Validaciones = new ArrayList<Validacion>();
		}
	}
	
	public static class DocumentoIdentidadTemporalParaValidacion extends BcsPojo{
		public Long IdTrabajo;
		public Long IdEnrolamiento;
		public Long IdDocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		
		public DocumentoIdentidadTemporalParaValidacion() {
			this.IdDocumentoIdentidad = 0L;
		}
	}
		
	public static class DocumentoIdentidadExistente extends BcsPojo{
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaValidarBorradorDocumentoIdentidad logica(Connection con, EntradaValidarBorradorDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::ValidarBorradorDocumentoIdentidad");
		SalidaValidarBorradorDocumentoIdentidad out = new SalidaValidarBorradorDocumentoIdentidad();
		out.Validaciones = validarDocumentosIdentidades(con, entrada.p_DocumentosIdentidad, entrada.IdEmpresa);
		logger.info("INICIO::ValidarBorradorDocumentoIdentidad");
		return out;
	}

	private List<Validacion> validarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> borradoresDocumentoIdentidades, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		try {
			insertarDocumentoIdentidadTemporalParaValidaciones(con, borradoresDocumentoIdentidades, idTrabajo);
			List<Validacion> documentosIdentidadesConErrores = validarDocumentosIdentidadExistentes(con, idTrabajo, idEmpresa);
			List<Validacion> tipoDocumentosIdentidadesConErrores = validarTipoDocumentosIdentidadExistentes(con, idTrabajo, idEmpresa);
			validaciones.addAll(documentosIdentidadesConErrores);
			validaciones.addAll(tipoDocumentosIdentidadesConErrores);
		} catch(Exception e) {
			logger.error("ERROR::CrearBorradorDocumentoIdentidad.validarDocumentosIdentidades", e);
			throw e;
		} finally {
			eliminarDocumentoIdentidadTemporalParaValidaciones(con, idTrabajo);
		}
		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarBorradorDocumentoIdentidad.validarDocumentosIdentidades", fin - inicio);
		return validaciones;
	}
	
	private void insertarDocumentoIdentidadTemporalParaValidaciones(Connection con, List<DocumentoIdentidad> borradoresDocumentoIdentidades, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		List<DocumentoIdentidadTemporalParaValidacion> documentosIdentidadesTemporales = borradoresDocumentoIdentidades.parallelStream()
				.map(borradorDocumentoIdentidad->{
					DocumentoIdentidadTemporalParaValidacion documentoIdentidadTemporalParaValidacion = new DocumentoIdentidadTemporalParaValidacion();
					documentoIdentidadTemporalParaValidacion.IdTrabajo = idTrabajo;
					documentoIdentidadTemporalParaValidacion.IdEnrolamiento = borradorDocumentoIdentidad.IdEnrolamiento;
					documentoIdentidadTemporalParaValidacion.IdTipoDocumentoIdentidad = borradorDocumentoIdentidad.IdTipoDocumentoIdentidad;
					documentoIdentidadTemporalParaValidacion.DocumentoIdentidad = borradorDocumentoIdentidad.DocumentoIdentidad;
					documentoIdentidadTemporalParaValidacion.IdDocumentoIdentidad = borradorDocumentoIdentidad.IdDocumentoIdentidad;
					return documentoIdentidadTemporalParaValidacion;
				}).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TEMPORAL_CREAR_BORRADOR_DOCUMENTO_IDENTIDAD, documentosIdentidadesTemporales, DocumentoIdentidadTemporalParaValidacion.class);
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBorradorDocumentoIdentidad.validarDocumentoIdentidad", fin - inicio);
	}
	
	private List<Validacion> validarDocumentosIdentidadExistentes(Connection con, Long idTrabajo, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentosIdentidadExistentes");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		
		SQL sql = new SQL();
		List<DocumentoIdentidadExistente> documentoIdentidadesExistentes = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTENTES, parametros, DocumentoIdentidadExistente.class);
		
		List<Validacion> documentosIdentidadesConErrores = new ArrayList<Validacion>();
		for(DocumentoIdentidadExistente documentoIdentidadExistente : documentoIdentidadesExistentes){
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			HashMap<String,String> datosMensajes = new HashMap<String,String>(2);
			datosMensajes.put("IdTipoDocumentoIdentidad", documentoIdentidadExistente.IdTipoDocumentoIdentidad);
			datosMensajes.put("DocumentoIdentidad", documentoIdentidadExistente.DocumentoIdentidad);
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DOCUMENTO_IDENTIDAD_YA_EXISTE", datosMensajes, con, idEmpresa);
			documentosIdentidadesConErrores.add(validacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentosIdentidadExistentes", fin - inicio);
		return documentosIdentidadesConErrores;
	}
	
	private List<Validacion> validarTipoDocumentosIdentidadExistentes(Connection con, Long idTrabajo, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarTipoDocumentosIdentidadExistentes");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		
		SQL sql = new SQL();
		List<DocumentoIdentidadExistente> tiposDocumentoIdentidadesExistentes = sql.query(con, SQL_SELECT_TIPO_DOCUMENTO_IDENTIDAD_EXISTENTES, parametros, DocumentoIdentidadExistente.class);
		
		List<Validacion> tipoDocumentosIdentidadesConErrores = new ArrayList<Validacion>();
		for(DocumentoIdentidadExistente tipoDocumentoIdentidadExistente : tiposDocumentoIdentidadesExistentes){
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
			datosMensajes.put("IdTipoDocumentoIdentidad", tipoDocumentoIdentidadExistente.IdTipoDocumentoIdentidad);
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_TIPO_DOCUMENTO_IDENTIDAD_YA_EXISTE", datosMensajes, con, idEmpresa);
			tipoDocumentosIdentidadesConErrores.add(validacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarTipoDocumentosIdentidadExistentes", fin - inicio);
		return tipoDocumentosIdentidadesConErrores;
	}
	
	private void eliminarDocumentoIdentidadTemporalParaValidaciones(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.eliminarDocumentoIdentidadTemporalParaValidaciones");
		long inicio = System.currentTimeMillis();
		
		DocumentoIdentidadTemporalParaValidacion documentosIdentidadesTemporalesPorIdTrabajo = new DocumentoIdentidadTemporalParaValidacion();
		documentosIdentidadesTemporalesPorIdTrabajo.IdTrabajo = idTrabajo;
		List<DocumentoIdentidadTemporalParaValidacion> documentosIdentidadesTemporalesEliminar = Arrays.asList(documentosIdentidadesTemporalesPorIdTrabajo);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_TEMPORAL_CREAR_BORRADOR_DOCUMENTO_IDENTIDAD, documentosIdentidadesTemporalesEliminar, DocumentoIdentidadTemporalParaValidacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBorradorDocumentoIdentidad.eliminarDocumentoIdentidadTemporalParaValidaciones", fin - inicio);
	}

}
