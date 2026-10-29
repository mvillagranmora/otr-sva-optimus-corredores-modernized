package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDocumentoIdentidadMasivo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearDocumentoIdentidadMasivo.class);
	
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidadMasivo_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidadMasivo_SelectDocumentoIdentidadPersona.sql");
	private final String SQL_INSERT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidadMasivo_InsertDocumentoIdentidad.sql");
	private final String SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidadMasivo_UpdateDocumentoIdentidadNoPredeterminado.sql");
	private final String SQL_UPDATE_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidadMasivo_UpdatePersona.sql");
	
	private final String ID_CONCEPTO = "NUEDOC";
	
	public static class EntradaCrearDocumentoIdentidadMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<DocumentoIdentidad> p_DocumentosIdentidad;
		
		public EntradaCrearDocumentoIdentidadMasivo() {
			this.p_DocumentosIdentidad = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearDocumentoIdentidadMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearDocumentoIdentidadMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
		
		public void agregarValidaciones(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				if(validacion.EsError) {
					this.P_OUT_ERRORESCONTADOR += 1L;
					this.P_OUT_MENSAJES.add(validacion.MensajeError);
				}
			}
		}
	}
	
	public static class DocumentoIdentidad extends BcsPojo {
		public Long IdInterno;
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String NumeroSerie;
		public String FechaEmision;
		public String FechaVencimiento;
		public String IdTipoDocumentoIdentidadAsociado;
		public String TipoPersona;
		public String DocumentoIdentidadAsociado;
		public Long IdUsuarioCreador;
		public String Predeterminado;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		public String Borrado;
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearDocumentoIdentidadMasivo jin = gson.fromJson(in.toString(), EntradaCrearDocumentoIdentidadMasivo.class);
		SalidaCrearDocumentoIdentidadMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaCrearDocumentoIdentidadMasivo logica(Connection con, EntradaCrearDocumentoIdentidadMasivo entrada) throws Exception {
		logger.info("INICIO::CrearDocumentoIdentidadMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDocumentoIdentidadMasivo out = new SalidaCrearDocumentoIdentidadMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<Validacion> validacionDocumentoIdentidad = validarDocumentoIdentidades(con, entrada);
		List<Validacion> validacionTipoDocumentoIdentidad = validarTipoDocumentoIdentidad(con, entrada);
		out.agregarValidaciones(validacionDocumentoIdentidad);
		out.agregarValidaciones(validacionTipoDocumentoIdentidad);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		String codigoPais = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.LocalizacionSistema");
		List<DocumentoIdentidad> documentosIdentidad = obtenerDocumentosIdentidad(con, entrada.p_DocumentosIdentidad, esOperacionConAutorizacion, codigoPais);
		insertarDocumentosIdentidad(con, documentosIdentidad);
		List<DocumentoIdentidad> documentosIdentidadAsociados = obtenerDocumentosIdentidadAsociados(con, entrada.p_DocumentosIdentidad, esOperacionConAutorizacion, codigoPais);
		insertarDocumentosIdentidad(con, documentosIdentidadAsociados);
		
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, documentosIdentidad);
		} else {
			actualizarDocumentoIdentidadPredeterminados(con, documentosIdentidad);
			agregarLog(con, entrada, documentosIdentidad);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(documentosIdentidad);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDocumentoIdentidadMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarDocumentoIdentidades(Connection con, EntradaCrearDocumentoIdentidadMasivo entrada) throws Exception {
		logger.debug("INICIO::CrearDocumentoIdentidadMasivo.validarDocumentoIdentidades");
		long inicio = System.currentTimeMillis();
		
		List<String> documentosIdentidades = entrada
				.p_DocumentosIdentidad
				.parallelStream()
				.map(documentoIdentidad->documentoIdentidad.DocumentoIdentidad)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("DocumentoIdentidad", documentosIdentidades);
		
		SQL sql = new SQL();
		List<DocumentoIdentidad> documentosIdentidadesExistentes = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, DocumentoIdentidad.class);
		
		List<Validacion> validaciones = new ArrayList<>();
		for(DocumentoIdentidad documentoIdentidad : entrada.p_DocumentosIdentidad) {
			DocumentoIdentidad documentoIdentidadExistente = documentosIdentidadesExistentes
					.parallelStream()
					.filter(documentoIdentidadEncontrado->esDocumentoIdentidadEquivalente(documentoIdentidad, documentoIdentidadEncontrado))
					.findFirst()
					.orElse(null);
			
			if(documentoIdentidadExistente != null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(documentoIdentidad.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_DOCUMENTO_IDENTIDAD_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidadMasivo.validarDocumentoIdentidades", fin - inicio);
		return validaciones;
	}
	
	private Boolean esDocumentoIdentidadEquivalente(DocumentoIdentidad documentoIdentidadNuevo, DocumentoIdentidad documentoIdentidadExistente) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.esDocumentoIdentidadEquivalente");
		long inicio = System.currentTimeMillis();
		
		Boolean esDocumentoIdentidadEquivalente = documentoIdentidadExistente.DocumentoIdentidad.equals(documentoIdentidadNuevo.DocumentoIdentidad) && documentoIdentidadExistente.IdTipoDocumentoIdentidad.equals(documentoIdentidadNuevo.IdTipoDocumentoIdentidad);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.esDocumentoIdentidadEquivalente", fin - inicio);
		return esDocumentoIdentidadEquivalente;
	}
	
	private List<Validacion> validarTipoDocumentoIdentidad(Connection con, EntradaCrearDocumentoIdentidadMasivo entrada) throws Exception {
		logger.debug("INICIO::CrearDocumentoIdentidadMasivo.validarTipoDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada
				.p_DocumentosIdentidad
				.parallelStream()
				.map(documentoIdentidad->documentoIdentidad.IdPersona)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DocumentoIdentidad> documentosIdentidadesExistentes = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD_PERSONA, parametros, DocumentoIdentidad.class);
		
		List<Validacion> validaciones = new ArrayList<>();
		for(DocumentoIdentidad documentoIdentidad : entrada.p_DocumentosIdentidad) {
			DocumentoIdentidad documentoIdentidadExistente = documentosIdentidadesExistentes
					.parallelStream()
					.filter(documentoIdentidadEncontrado->esTipoDocumentoIdentidadEquivalente(documentoIdentidad, documentoIdentidadEncontrado))
					.findFirst()
					.orElse(null);
			
			if(documentoIdentidadExistente != null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(documentoIdentidad.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidadMasivo.validarTipoDocumentoIdentidad", fin - inicio);
		return validaciones;
		
	}
	
	private Boolean esTipoDocumentoIdentidadEquivalente(DocumentoIdentidad documentoIdentidadNuevo, DocumentoIdentidad documentoIdentidadExistente) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.esTipoDocumentoIdentidadEquivalente");
		long inicio = System.currentTimeMillis();
		
		Boolean esTipoDocumentoIdentidadEquivalente = documentoIdentidadExistente.IdPersona.equals(documentoIdentidadNuevo.IdPersona) && documentoIdentidadExistente.IdTipoDocumentoIdentidad.equals(documentoIdentidadNuevo.IdTipoDocumentoIdentidad);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.esTipoDocumentoIdentidadEquivalente", fin - inicio);
		return esTipoDocumentoIdentidadEquivalente;
	}
		
	private List<DocumentoIdentidad> obtenerDocumentosIdentidad(Connection con, List<DocumentoIdentidad> entradaDocumentosIdentidad, Boolean esOperacionConAutorizacion, String codigoPais) throws Exception {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerDocumentosIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDocumentosIdentidad = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", Long.valueOf(entradaDocumentosIdentidad.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<DocumentoIdentidad> documentosIdentidad = entradaDocumentosIdentidad
				.stream()
				.map(entradaDocumentoIdentidad->{
					Long idDocumentoIdentidad = idsDocumentosIdentidad.get(index.getAndIncrement());
					DocumentoIdentidad documentoIdentidad = obtenerEntidadDocumentoIdentidad(entradaDocumentoIdentidad, idDocumentoIdentidad, esOperacionConAutorizacion, codigoPais);
					return documentoIdentidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerDocumentosIdentidad", fin - inicio);
		return documentosIdentidad;
	}
	
	private DocumentoIdentidad obtenerEntidadDocumentoIdentidad(DocumentoIdentidad entradaDocumentoIdentidad, Long idDocumentoIdentidad, Boolean esOperacionConAutorizacion, String codigoPais) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
		documentoIdentidad.IdInterno = entradaDocumentoIdentidad.IdInterno;
		documentoIdentidad.IdDocumentoIdentidad = idDocumentoIdentidad;
		documentoIdentidad.IdPersona = entradaDocumentoIdentidad.IdPersona;
		documentoIdentidad.IdTipoDocumentoIdentidad = entradaDocumentoIdentidad.IdTipoDocumentoIdentidad;
		documentoIdentidad.DocumentoIdentidad = entradaDocumentoIdentidad.DocumentoIdentidad;
		documentoIdentidad.NumeroSerie = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.NumeroSerie, "");
		documentoIdentidad.FechaEmision = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.FechaEmision, "0001-01-01");
		documentoIdentidad.FechaVencimiento = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.FechaVencimiento, "0001-01-01");
		documentoIdentidad.IdTipoDocumentoIdentidadAsociado = entradaDocumentoIdentidad.IdTipoDocumentoIdentidadAsociado;
		documentoIdentidad.TipoPersona = entradaDocumentoIdentidad.TipoPersona;
		documentoIdentidad.DocumentoIdentidadAsociado = obtenerDocumentoIdentidadAsociado(codigoPais, entradaDocumentoIdentidad);
		documentoIdentidad.Predeterminado = entradaDocumentoIdentidad.Predeterminado;
		documentoIdentidad.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.IdEstado, "ACT");
		documentoIdentidad.Borrado = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.Borrado, "N");
		documentoIdentidad.IdUsuarioCreador = entradaDocumentoIdentidad.IdUsuarioCreador;
		documentoIdentidad.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad", fin - inicio);
		
		return documentoIdentidad;
	}
	
	private String obtenerDocumentoIdentidadAsociado(String codigoPais, DocumentoIdentidad documentoIdentidad) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		String documentoIdentidadAsociado = "";
		
		if(esDocumentoIdentidadIUP(codigoPais, documentoIdentidad.IdTipoDocumentoIdentidad)) {
			documentoIdentidadAsociado = IUP.obtenerDocumentoIdentidadAsociado(documentoIdentidad.DocumentoIdentidad, documentoIdentidad.IdTipoDocumentoIdentidadAsociado, documentoIdentidad.TipoPersona);
			return documentoIdentidadAsociado;
		} 
		
		documentoIdentidadAsociado = SocioNegocioUtils.obtenerValorNoNulo(documentoIdentidad.DocumentoIdentidadAsociado, "");
		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad", fin - inicio);
		return documentoIdentidadAsociado;
	}
	
	private Boolean esDocumentoIdentidadIUP(String codigoPais, String idTipoDocumentoIdentidad) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadIUP = "PE".equals(codigoPais) && "IUP".equals(idTipoDocumentoIdentidad);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad", fin - inicio);
		return esDocumentoIdentidadIUP;
	}
	
	private void insertarDocumentosIdentidad(Connection con, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.insertarDocumentosIdentidad");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DOCUMENTO_IDENTIDAD, documentosIdentidad, DocumentoIdentidad.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.insertarDocumentosIdentidad", fin - inicio);
	}
	
	private List<DocumentoIdentidad> obtenerDocumentosIdentidadAsociados(Connection con, List<DocumentoIdentidad> entradaDocumentosIdentidad, Boolean esOperacionConAutorizacion, String codigoPais) throws Exception {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerDocumentosIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> entradaDocumentosIdentidadAsociados = entradaDocumentosIdentidad
				.parallelStream()
				.filter(documentoIdentidad->esDocumentoIdentidadIUP(codigoPais, documentoIdentidad.IdTipoDocumentoIdentidad))
				.collect(Collectors.toList());
		
		List<Long> idsDocumentosIdentidad = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", Long.valueOf(entradaDocumentosIdentidad.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<DocumentoIdentidad> documentosIdentidadAsociados = entradaDocumentosIdentidadAsociados
				.stream()
				.map(entradaDocumentoIdentidad->{
					Long idDocumentoIdentidad = idsDocumentosIdentidad.get(index.getAndIncrement());
					DocumentoIdentidad documentoIdentidad = obtenerEntidadDocumentoIdentidadAsociado(entradaDocumentoIdentidad, idDocumentoIdentidad, esOperacionConAutorizacion);
					return documentoIdentidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerDocumentosIdentidad", fin - inicio);
		return documentosIdentidadAsociados;
	}
	
	private DocumentoIdentidad obtenerEntidadDocumentoIdentidadAsociado(DocumentoIdentidad entradaDocumentoIdentidad, Long idDocumentoIdentidad, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
		documentoIdentidad.IdInterno = entradaDocumentoIdentidad.IdInterno;
		documentoIdentidad.IdDocumentoIdentidad = idDocumentoIdentidad;
		documentoIdentidad.IdPersona = entradaDocumentoIdentidad.IdPersona;
		documentoIdentidad.IdTipoDocumentoIdentidad = entradaDocumentoIdentidad.IdTipoDocumentoIdentidadAsociado;
		documentoIdentidad.DocumentoIdentidad = entradaDocumentoIdentidad.DocumentoIdentidadAsociado;
		documentoIdentidad.NumeroSerie = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.NumeroSerie, "");
		documentoIdentidad.FechaEmision = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.FechaEmision, "0001-01-01");
		documentoIdentidad.FechaVencimiento = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.FechaVencimiento, "0001-01-01");
		documentoIdentidad.IdTipoDocumentoIdentidadAsociado = entradaDocumentoIdentidad.IdTipoDocumentoIdentidad;
		documentoIdentidad.TipoPersona = entradaDocumentoIdentidad.TipoPersona;
		documentoIdentidad.DocumentoIdentidadAsociado = entradaDocumentoIdentidad.DocumentoIdentidad;
		documentoIdentidad.Predeterminado = documentoIdentidad.Predeterminado;
		documentoIdentidad.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.IdEstado, "ACT");
		documentoIdentidad.Borrado = SocioNegocioUtils.obtenerValorNoNulo(entradaDocumentoIdentidad.Borrado, "N");
		documentoIdentidad.IdUsuarioCreador = entradaDocumentoIdentidad.IdUsuarioCreador;
		documentoIdentidad.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerEntidadDocumentoIdentidad", fin - inicio);
		
		return documentoIdentidad;
	}
	
	private SalidaCrearDocumentoIdentidadMasivo crearEventoAutorizacion(Connection con, EntradaCrearDocumentoIdentidadMasivo entrada, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		logger.debug("INICIO::CrearDocumentoIdentidadMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearDocumentoIdentidadMasivo salidaCrearDocumentoIdentidadMasivo = new SalidaCrearDocumentoIdentidadMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = documentosIdentidad
				.parallelStream()
				.map(documentoIdentidad->documentoIdentidad.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = documentosIdentidad.stream().map(documentoIdentidad->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(documentoIdentidad.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(documentoIdentidad.IdDocumentoIdentidad);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = documentoIdentidad.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /NuevoDoc: " + documentoIdentidad.DocumentoIdentidad;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearDocumentoIdentidadMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaCrearDocumentoIdentidadMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaCrearDocumentoIdentidadMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaCrearDocumentoIdentidadMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearDocumentoIdentidadMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidadMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaCrearDocumentoIdentidadMasivo;	
	}
	
	private void actualizarDocumentoIdentidadPredeterminados(Connection con, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		logger.debug("INICIO::CrearDocumentoIdentidadMasivo.actualizarDocumentoIdentidadPredeterminados");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> documentosIdentidadPredeterminados = documentosIdentidad
				.parallelStream()
				.filter(documentoIdentidad->"S".equals(documentoIdentidad.Predeterminado))
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS, documentosIdentidadPredeterminados, DocumentoIdentidad.class);
		sql.batch(con, SQL_UPDATE_PERSONA, documentosIdentidadPredeterminados, DocumentoIdentidad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidadMasivo.actualizarDocumentoIdentidadPredeterminados", fin - inicio);
	}
	
	private void agregarLog(Connection con, EntradaCrearDocumentoIdentidadMasivo entrada, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(DocumentoIdentidad documentoIdentidad : documentosIdentidad) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "IdTipoDocumentoIdentidad", "", documentoIdentidad.IdTipoDocumentoIdentidad));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "DocumentoIdentidad", "", documentoIdentidad.DocumentoIdentidad));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "NumeroSerie", "", documentoIdentidad.NumeroSerie));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "FechaEmision", "", documentoIdentidad.FechaEmision));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "FechaVencimiento", "", documentoIdentidad.FechaVencimiento));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "IdTipoDocumentoIdentidadAsociado", "", documentoIdentidad.IdTipoDocumentoIdentidadAsociado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "TipoPersona", "", documentoIdentidad.TipoPersona));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "DocumentoIdentidadAsociado", "", documentoIdentidad.DocumentoIdentidadAsociado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(documentoIdentidad.IdDocumentoIdentidad, "IdEstado", "", documentoIdentidad.IdEstado));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = documentoIdentidad.IdDocumentoIdentidad;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = documentoIdentidad.IdPersona;
			log.IdTipoRegistro = "CREACION_DOCUMENTO_IDENTIDAD";
			log.EstadoRegistro = documentoIdentidad.IdEstadoAutorizacion;
			log.EstadoProceso = documentoIdentidad.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Documento Identidad";
			log.IdRegistro = String.valueOf(documentoIdentidad.IdDocumentoIdentidad);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<DocumentoIdentidad> documentosIdentidad){
		logger.trace("INICIO::CrearDocumentoIdentidadMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = documentosIdentidad
				.parallelStream()
				.map(documentoIdentidad->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = documentoIdentidad.IdInterno;
					entidad.IdEntidad = documentoIdentidad.IdDocumentoIdentidad;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDocumentoIdentidadMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}
	
}
