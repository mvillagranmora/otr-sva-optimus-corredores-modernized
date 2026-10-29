package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.ValidarBorradorDocumentoIdentidad.EntradaValidarBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorDocumentoIdentidad implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorDocumentoIdentidad.class);
	
	private String SQL_INSERT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorDocumentoIdentidad_InsertDocumentoIdentidad.sql");
	
	public static class EntradaCrearBorradorDocumentoIdentidad extends EntradaAdministrarBorrador{
		public List<DocumentoIdentidad> p_DocumentosIdentidades;
		
		public EntradaCrearBorradorDocumentoIdentidad() {
			this.p_DocumentosIdentidades = new ArrayList<DocumentoIdentidad>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorDocumentoIdentidad jin = gson.fromJson(in.toString(), EntradaCrearBorradorDocumentoIdentidad.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::CrearBorradorDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DocumentosIdentidades, DocumentoIdentidad.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DocumentosIdentidades, "DocumentosIdentidades", out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Validacion> validaciones = validarDocumentosIdentidades(con, entrada.p_DocumentosIdentidades, entrada.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<DocumentoIdentidad> documentosIdentidades = obtenerDocumentosIdentidades(con, entrada.p_DocumentosIdentidades);
		
		documentosIdentidades = actualizarDocumentoIdentidadAsociado(con, documentosIdentidades, entrada.p_IdEmpresa);
		List<DocumentoIdentidad> documentosIdentidadesAsociados = obtenerDocumentosIdentidadesAsociados(con, documentosIdentidades);
		
		insertarDocumentosIdentidades(con, documentosIdentidades);
		insertarDocumentosIdentidades(con, documentosIdentidadesAsociados);
		
		out.P_OUT_REFID = obtenerIdsInsertados(documentosIdentidades);
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorDocumentoIdentidad", finServicio - inicioServicio);
		return out;
	}
		
	private List<Validacion> validarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> borradoresDocumentoIdentidades, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.validarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		ValidarBorradorDocumentoIdentidad validarBorradorDocumentoIdentidad = new ValidarBorradorDocumentoIdentidad();
		EntradaValidarBorradorDocumentoIdentidad entradaValidarBorradorDocumentoIdentidad = new EntradaValidarBorradorDocumentoIdentidad();
		entradaValidarBorradorDocumentoIdentidad.IdEmpresa = idEmpresa;
		entradaValidarBorradorDocumentoIdentidad.p_DocumentosIdentidad = borradoresDocumentoIdentidades;
		
		List<Validacion> validaciones = validarBorradorDocumentoIdentidad.logica(con, entradaValidarBorradorDocumentoIdentidad).Validaciones;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.validarDocumentosIdentidades", fin - inicio);
		return validaciones;
	}
		
	private List<DocumentoIdentidad> obtenerDocumentosIdentidades(Connection con, List<DocumentoIdentidad> borradoresDocumentoIdentidades) throws Exception{
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.obtenerDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorDocumentoIdentidad", Long.valueOf(borradoresDocumentoIdentidades.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<DocumentoIdentidad> documentosIdentidades = borradoresDocumentoIdentidades.parallelStream().map(borradorDocumentoIdentidad->{
			Long idDocumentoIdentidad = secuencias.get(index.getAndIncrement());
			DocumentoIdentidad telefono = obtenerEntidadDocumentoIdentidad(borradorDocumentoIdentidad, idDocumentoIdentidad);
			return telefono;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.obtenerDocumentosIdentidades", fin - inicio);
		return documentosIdentidades;
	}
	
	private DocumentoIdentidad obtenerEntidadDocumentoIdentidad(DocumentoIdentidad borradorDocumentoIdentidad, Long idDocumentoIdentidad) {
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.obtenerEntidadDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
		documentoIdentidad.IdDocumentoIdentidad = idDocumentoIdentidad;
		documentoIdentidad.IdInterno = borradorDocumentoIdentidad.IdInterno;
		documentoIdentidad.IdEnrolamiento = borradorDocumentoIdentidad.IdEnrolamiento;
		documentoIdentidad.IdTipoDocumentoIdentidad = borradorDocumentoIdentidad.IdTipoDocumentoIdentidad;
		documentoIdentidad.DocumentoIdentidad = borradorDocumentoIdentidad.DocumentoIdentidad;
		documentoIdentidad.NumeroSerie = borradorDocumentoIdentidad.NumeroSerie;
		documentoIdentidad.FechaEmision = borradorDocumentoIdentidad.FechaEmision;
		documentoIdentidad.FechaVencimiento = borradorDocumentoIdentidad.FechaVencimiento;
		documentoIdentidad.IdTipoDocumentoIdentidadAsociado  = borradorDocumentoIdentidad.IdTipoDocumentoIdentidadAsociado;
		documentoIdentidad.TipoPersona = borradorDocumentoIdentidad.TipoPersona;
		documentoIdentidad.Predeterminado = borradorDocumentoIdentidad.Predeterminado;
		documentoIdentidad.IdDocumentoIdentidadPersona = borradorDocumentoIdentidad.IdDocumentoIdentidadPersona;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.obtenerEntidadDocumentoIdentidad", fin - inicio);
		return documentoIdentidad;
	}
	
	private List<DocumentoIdentidad> actualizarDocumentoIdentidadAsociado(Connection con, List<DocumentoIdentidad> documentosIdentidades, Long idEmpresa) throws Exception{
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.actualizarDocumentoIdentidadAsociado");
		long inicio = System.currentTimeMillis();
		
		String codigoPais = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.LocalizacionSistema");
		
		if("PE".equals(codigoPais)) {
			documentosIdentidades.stream()
			.filter(documentoIdentidad->"IUP".equals(documentoIdentidad.IdTipoDocumentoIdentidadAsociado))
			.forEach(documentoIdentidad->{
				documentoIdentidad.DocumentoIdentidadAsociado = IUP.obtenerDocumentoIdentidadAsociado(documentoIdentidad.DocumentoIdentidad, documentoIdentidad.IdTipoDocumentoIdentidadAsociado, documentoIdentidad.TipoPersona);
			});
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.actualizarDocumentoIdentidadAsociado", fin - inicio);
		return documentosIdentidades;
	}
	
	private List<DocumentoIdentidad> obtenerDocumentosIdentidadesAsociados(Connection con, List<DocumentoIdentidad> documentosIdentidades) throws Exception{
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.obtenerDocumentosIdentidadesAsociados");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> documentosIdentidadesAsociado = documentosIdentidades.parallelStream()
				.filter(documentoIdentidad->esDocumentoAdicional(documentoIdentidad.IdTipoDocumentoIdentidadAsociado, documentoIdentidad.DocumentoIdentidadAsociado))
				.map(documentoIdentidad->{
					DocumentoIdentidad documentoIdentidadAsociado = new DocumentoIdentidad();
					documentoIdentidadAsociado.IdInterno = 0L;
					documentoIdentidadAsociado.IdEnrolamiento = documentoIdentidad.IdEnrolamiento;
					documentoIdentidadAsociado.IdTipoDocumentoIdentidad = documentoIdentidad.IdTipoDocumentoIdentidadAsociado;
					documentoIdentidadAsociado.DocumentoIdentidad = documentoIdentidad.DocumentoIdentidadAsociado;
					documentoIdentidadAsociado.NumeroSerie = documentoIdentidad.NumeroSerie;
					documentoIdentidadAsociado.FechaEmision = documentoIdentidad.FechaEmision;
					documentoIdentidadAsociado.FechaVencimiento = documentoIdentidad.FechaVencimiento;
					documentoIdentidadAsociado.IdTipoDocumentoIdentidadAsociado  = documentoIdentidad.IdTipoDocumentoIdentidadAsociado;
					documentoIdentidadAsociado.TipoPersona = documentoIdentidad.TipoPersona;
					return documentoIdentidadAsociado;
				}).collect(Collectors.toList());
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorDocumentoIdentidad", Long.valueOf(documentosIdentidadesAsociado.size()));
		
		AtomicInteger index = new AtomicInteger();
		documentosIdentidadesAsociado.parallelStream().forEach(documentoIdentidadAsociado->{
			documentoIdentidadAsociado.IdDocumentoIdentidad = secuencias.get(index.getAndIncrement());
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.obtenerDocumentosIdentidadesAsociados", fin - inicio);
		return documentosIdentidadesAsociado;
	}
	
	private Boolean esDocumentoAdicional(String idTipoDocumentoIdentidadAsociado, String documentoIdentidadAsociado) {
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.esDocumentoAdicional");
		long inicio = System.currentTimeMillis();
		
		Boolean esDocumentoAdicional = "IUP".equals(idTipoDocumentoIdentidadAsociado) && !documentoIdentidadAsociado.isEmpty();
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.esDocumentoAdicional", fin - inicio);
		return esDocumentoAdicional;
	}
	
	private void insertarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> documentosIdentidades) throws Exception {
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.insertarDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DOCUMENTO_IDENTIDAD, documentosIdentidades, DocumentoIdentidad.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.insertarDocumentosIdentidades", fin - inicio);
	}

	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<DocumentoIdentidad> documentosIdentidades){
		logger.trace("INICIO::CrearBorradorDocumentoIdentidad.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = documentosIdentidades.parallelStream().map(telefono->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = telefono.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = telefono.IdDocumentoIdentidad;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDocumentoIdentidad.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}	
}
