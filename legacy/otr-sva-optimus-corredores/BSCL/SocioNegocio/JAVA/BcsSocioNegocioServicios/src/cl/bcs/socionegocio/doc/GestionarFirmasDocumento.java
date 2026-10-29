package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.doc.pojo.Anexo;
import cl.bcs.socionegocio.doc.pojo.FirmaDocumento;
import cl.bcs.socionegocio.doc.pojo.SolicitudFirmaDocumento;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.socionegocio.soc.pojo.ResumenUsuario;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GestionarFirmasDocumento implements JavaCall {
	static final Logger logger = LogManager.getLogger(GestionarFirmasDocumento.class);

	private final String SQL_INSERT_SOLICITUD_FIRMA_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_InsertSolicitudFirmaAnexo.sql");
	private final String SQL_INSERT_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_InsertFirmaRepresentanteDocumentoAnexo.sql");
	private final String SQL_UPDATE_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_UpdateFirmaRepresentanteDocumentoAnexo.sql");
	private final String SQL_UPDATE_ANEXO 				= loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_UpdateAnexo.sql");
	private final String SQL_SELECT_CANALES_DOCUMENTO 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarFirmaCanalesDocumento.sql");
	private final String SQL_CONSULTAR_DOCUMENTOS_PENDIENTES = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_ConsultarDocumentosPendientes.sql");
	private final String SQL_CONSULTAR_DOCUMENTOS_VENCIDOS = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_ConsultarDocumentosVencidos.sql");

	public static class EntradaGestionarFirmasDocumento extends BcsPojo { 
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public java.sql.Timestamp p_FechaVigencia;
		public String p_SolicitarFechaFirma;
		public String p_DocumentoSubido;
		public java.sql.Timestamp p_FechaFirma;
		
		public Long p_IdAnexo;
		public List<Long> p_IdsPersonasFirmantes;

		public EntradaGestionarFirmasDocumento() {
			p_IdsPersonasFirmantes = new ArrayList<>();
		}
	}

	public static class SalidaGestionarFirmasDocumento extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;

		public SalidaGestionarFirmasDocumento() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
		}

		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}

		public void agregarValidacionSalida(Validacion validacion) {
			if (validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
			}

			this.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
	}

	public static class Persona extends BcsPojo {
		public Long IdInterno;
		public Long IdPersona;
		public String DocumentoIdentidad;
	}

	public static class ResultadoCrearEventoAutorizacion extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public ResultadoCrearEventoAutorizacion() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGestionarFirmasDocumento entrada = gson.fromJson(in, EntradaGestionarFirmasDocumento.class);
		SalidaGestionarFirmasDocumento salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaGestionarFirmasDocumento logica(Connection conn, EntradaGestionarFirmasDocumento entrada) throws Exception {
		logger.info("INICIO::GestionarFirmasDocumento");
		long inicio = System.currentTimeMillis();

		SalidaGestionarFirmasDocumento salida = new SalidaGestionarFirmasDocumento();
		
		
		
        try {
        	SQL sql = new SQL();
        	HashMap<String,Object> paramsIN = new HashMap<>();
        	paramsIN.put("IdAnexo", entrada.p_IdAnexo ); 
 
        	if (sql.countQuery(conn, SQL_CONSULTAR_DOCUMENTOS_PENDIENTES, paramsIN) > 0) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTOS_PENDIENTES", new HashMap<>(), conn, entrada.p_IdEmpresa);
                salida.agregarValidacionSalida(validacion);
                return salida;
        	}
        }catch (Exception e) {
        	System.out.println(e);
        }
        
        try {
        	SQL sql = new SQL();
        	HashMap<String,Object> paramsIN = new HashMap<>();
        	paramsIN.put("IdAnexo", entrada.p_IdAnexo ); 
 
        	if (sql.countQuery(conn, SQL_CONSULTAR_DOCUMENTOS_VENCIDOS, paramsIN) > 0) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTO_VENCIDO", new HashMap<>(), conn, entrada.p_IdEmpresa);
                salida.agregarValidacionSalida(validacion);
                return salida;
        	}
        }catch (Exception e) {
        	System.out.println(e);
        }        
		

		ResumenUsuario resumenUsuario = SocioNegocioUtils.obtenerResumenUsuario(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		ResumenDocumentoAnexo resumenDocumentoAnexo = SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdAnexo)))
				.getOrDefault(entrada.p_IdAnexo, new ResumenDocumentoAnexo());

        try {
        	SQL sql = new SQL();
        	HashMap<String,Object> paramsIN = new HashMap<>();
        	paramsIN.put("AnexoDocumento", entrada.p_IdAnexo ); 
        	paramsIN.put("Canal", entrada.p_IdTipoCanalIngreso ); 
        	if (!entrada.p_IdTipoCanalIngreso.equals("OPT") && sql.countQuery(conn, SQL_SELECT_CANALES_DOCUMENTO, paramsIN)==0) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANAL_NO_PERMITE_INGRESADO", new HashMap<>(), conn, entrada.p_IdEmpresa);
                salida.agregarValidacionSalida(validacion);
                return salida;
        	}
        }catch (Exception e) {
        	System.out.println(e);
        }   
		
		if ("FULL".equals(resumenDocumentoAnexo.TipoEnrolamiento)) {
			SocioNegocioUtils.ResultadoValidacionRequisitosEnrolamientoFull resultado = SocioNegocioUtils.validarEnrolamientoFull(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_UsaReglasNegocio, entrada.p_IdTipoCanalIngreso, Collections.singletonList(resumenDocumentoAnexo.IdCliente));
			salida.agregarValidacionesSalida(resultado.validaciones);
			salida.P_OUT_REQUISITOS_ENR_FULL.addAll(resultado.requisitos);

			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}
		}

		String firmaSiNoEsAgenteDelCliente = getParametroFirmaSiNoEsAgenteDelCliente(conn,entrada.p_IdEmpresa);
		
		ResumenPersona resumenPersonaAgenteCliente = SocioNegocioUtils.obtenerResumenAgenteCliente(conn, resumenDocumentoAnexo.IdCliente);
		List<ResumenPersona> firmantesCliente = SocioNegocioUtils.obtenerFirmantesPersona(conn, resumenDocumentoAnexo.IdPersona);
		List<TipoDocumentoIndicador> documentoTiposIndicadores = DocumentoUtils.obtenerTiposIndicadoresPorIdTipoDocumento(conn, entrada.p_IdEmpresa, new ArrayList<>(Collections.singletonList(resumenDocumentoAnexo.IdTipoDocumento)))
				.getOrDefault(resumenDocumentoAnexo.IdTipoDocumento, Collections.emptyList());

		salida.agregarValidacionesSalida(validarAnexo(conn, entrada.p_IdEmpresa, resumenDocumentoAnexo, documentoTiposIndicadores));
		salida.agregarValidacionesSalida(validarAgente(conn, entrada.p_IdEmpresa, resumenUsuario, resumenPersonaAgenteCliente, firmaSiNoEsAgenteDelCliente));
		salida.agregarValidacionesSalida(validarFirmantesCliente(conn, entrada.p_IdEmpresa, resumenDocumentoAnexo, entrada.p_IdsPersonasFirmantes, firmantesCliente));
		salida.agregarValidacionesSalida(validarFechaFirma(conn, entrada.p_IdEmpresa, entrada.p_FechaFirma, entrada.p_DocumentoSubido, entrada.p_SolicitarFechaFirma));
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		List<SolicitudFirmaDocumento> solicitudesFirmas = obtenerSolicitudesFirmasPendientes(conn, entrada.p_IdTipoCanalIngreso, resumenPersonaAgenteCliente, resumenDocumentoAnexo, entrada.p_IdsPersonasFirmantes);
		List<SolicitudFirmaDocumento> solicitudesInsertar = solicitudesFirmas.stream().filter(s -> s.IdSolicitudFirmaAnexo == null).collect(Collectors.toList());

		java.sql.Timestamp fechaFirmado = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		if (entrada.p_SolicitarFechaFirma.equals("S") && entrada.p_DocumentoSubido.equals("S")) {
			fechaFirmado = entrada.p_FechaFirma;
		}		
		updateAnexo(conn, entrada, resumenDocumentoAnexo);
		insertarSolicitudesFirma(conn, solicitudesInsertar);
		upsertFirmasDocumento(conn, entrada.p_IdEmpresa, fechaFirmado, entrada.p_IdTipoCanalIngreso, resumenPersonaAgenteCliente, solicitudesFirmas,firmaSiNoEsAgenteDelCliente,entrada.p_IdUsuario);
		
		if (firmaSiNoEsAgenteDelCliente.equalsIgnoreCase("N")) {
			
			ResultadoCrearEventoAutorizacion resultadoCrearEventoAutorizacion = crearEventoAutorizacion(conn, entrada, resumenDocumentoAnexo, solicitudesFirmas);
			salida.P_OUT_ERRORESCONTADOR += resultadoCrearEventoAutorizacion.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(resultadoCrearEventoAutorizacion.P_OUT_MENSAJES);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}
		}

		ActivarDocumentos.SalidaActivarDocumentos salidaActivarDocumentos = DocumentoUtils.activarDocumentos(conn, entrada.p_IdEmpresa, Collections.singletonList(resumenDocumentoAnexo));
		salida.P_OUT_ERRORESCONTADOR += salidaActivarDocumentos.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(salidaActivarDocumentos.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		DocumentoUtils.agregarLogFirmaDocumento(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdsPersonasFirmantes, resumenDocumentoAnexo);

		if(salida.P_OUT_MENSAJES.isEmpty() && salida.P_OUT_ERRORESCONTADOR == 0) {
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_OK, "UI_DOCUMENTO_FIRMADO", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GestionarFirmasDocumento", fin - inicio);

		return salida;
	}

	private List<Validacion> validarAnexo(Connection conn, Long idEmpresa, ResumenDocumentoAnexo resumenAnexo, List<TipoDocumentoIndicador> tiposIndicadoresDocumento) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.validarAnexo");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		if (resumenAnexo == null) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CLIENTE_ANEXO", new HashMap<>(), conn, idEmpresa);
			validaciones.add(validacion);
		}

		HashMap<String, String> parametrosMensaje = new HashMap<>(2);
		for (TipoDocumentoIndicador tipoIndicadorDocumento : tiposIndicadoresDocumento) {
			if (tipoIndicadorDocumento.IdTipoIndicador == null) {
				parametrosMensaje.put("IdTipoDocumentoIndicador", String.valueOf(tipoIndicadorDocumento.IdTipoDocumentoIndicador));

				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_TIPO_INDICADOR", parametrosMensaje, conn, idEmpresa);
				validaciones.add(validacion);

				parametrosMensaje.clear();
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.validarAnexo", fin - inicio);

		return validaciones;
	}

	private List<Validacion> validarAgente(Connection conn, Long idEmpresa, ResumenUsuario resumenAgenteEntrante, ResumenPersona resumenAgente, String firmaSiNoEsAgenteDelCliente) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.validarAgente");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		boolean noEsAgenteCliente = resumenAgenteEntrante.IdPersona == null || !Objects.equals(resumenAgenteEntrante.IdPersona, resumenAgente.IdPersona);
		
		if (noEsAgenteCliente && firmaSiNoEsAgenteDelCliente.equalsIgnoreCase("N") ) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_USUARIO_NO_ES_AGENTE_DEL_CLIENTE", new HashMap<>(), conn, idEmpresa);
			validaciones.add(validacion);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.validarAgente", fin - inicio);

		return validaciones;
	}

	private List<Validacion> validarFirmantesCliente(Connection conn, Long idEmpresa, ResumenDocumentoAnexo resumenAnexo, List<Long> idsPersonasFirmantes, List<ResumenPersona> firmantesCliente) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.validarFirmantesCliente");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();
		Set<Long> idsFirmantesCliente = firmantesCliente.stream().map(firmante -> firmante.IdPersona).collect(Collectors.toSet());

		for (Long idPersonaFirmante : idsPersonasFirmantes) {
			boolean noEsFirmante = !idsFirmantesCliente.contains(idPersonaFirmante);
			if (noEsFirmante) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String, String> params = new HashMap<>(4);
				params.put("IdPersonaFirmante", String.valueOf(idPersonaFirmante));
				params.put("IdCliente", String.valueOf(resumenAnexo.IdCliente));
				validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_PERSONA_NO_ES_FIRMANTE_DE_CLIENTE", params, conn, idEmpresa);
				validaciones.add(validacion);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.validarFirmantesCliente", fin - inicio);

		return validaciones;
	}
	
	private List<Validacion> validarFechaFirma(Connection conn, Long idEmpresa, java.sql.Timestamp fecha, String DocumentoSubido, String SolicitarFechaFirma) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.validarFechaFirma");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		if(DocumentoSubido.equals("S") && SolicitarFechaFirma.equals("S")) {
			long fechaMayor = fecha.getTime();
			long fechaMenor = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa).getTime();
			fechaMayor = (long) (Math.floor(fechaMayor/(1000 * 60 * 60 * 24)));
			fechaMenor = (long) (Math.floor(fechaMenor/(1000 * 60 * 60 * 24)));
			long diferenciaEn_Dias = fechaMayor - fechaMenor;
			if (diferenciaEn_Dias > 0) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_FECHA_FIRMA_MAYOR_SISTEMA", new HashMap<>(), conn, idEmpresa);
				validaciones.add(validacion);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.validarFechaFirma", fin - inicio);

		return validaciones;
	}

	private List<SolicitudFirmaDocumento> obtenerSolicitudesFirmasPendientes(Connection conn, String idTipoCanalIngreso, ResumenPersona resumenAgente, ResumenDocumentoAnexo resumenAnexo, List<Long> idsPersonasFirmantes) throws SQLException {
		logger.debug("INICIO::GestionarFirmasDocumento.obtenerSolicitudesFirmasPendientes");
		long inicio = System.currentTimeMillis();

		Map<Long, SolicitudFirmaDocumento> solicitudFirmaPorIdPersonaFirmante = DocumentoUtils.obtenerSolicitudFirmaPendientesPorIdPersonaFirmante(conn, resumenAnexo.IdAnexo, idsPersonasFirmantes);
		for (Long idPersonaFirmante : idsPersonasFirmantes) {
			boolean firmanteNoTieneSolicitud = !solicitudFirmaPorIdPersonaFirmante.containsKey(idPersonaFirmante);
			if (firmanteNoTieneSolicitud) {
				SolicitudFirmaDocumento solicitudFirma = new SolicitudFirmaDocumento();
				solicitudFirma.IdPersonaSolicitante = resumenAgente.IdPersona;
				solicitudFirma.IdPersonaFirmante = idPersonaFirmante;
				solicitudFirma.IdAnexo = resumenAnexo.IdAnexo;
				solicitudFirma.IdDocumento = resumenAnexo.IdDocumento;
				solicitudFirma.IdTipoCanalIngresoSolicitudFirmaAnexo = idTipoCanalIngreso;
				solicitudFirma.IdTipoCanalIngresoFirmaAnexo = idTipoCanalIngreso;
				solicitudFirmaPorIdPersonaFirmante.put(idPersonaFirmante, solicitudFirma);
			}
		}

		List<SolicitudFirmaDocumento> solicitudesPendientes = new ArrayList<>(solicitudFirmaPorIdPersonaFirmante.values());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.obtenerSolicitudesFirmasPendientes", fin - inicio);

		return solicitudesPendientes;
	}

	private void updateAnexo(Connection conn, EntradaGestionarFirmasDocumento entrada, ResumenDocumentoAnexo resumenDocumentoAnexo) throws SQLException {
		logger.debug("INICIO::GestionarFirmasDocumento.updateAnexo");
		long inicio = System.currentTimeMillis();

		Anexo anexoActualizar = new Anexo();
		anexoActualizar.IdAnexo = resumenDocumentoAnexo.IdAnexo;
		anexoActualizar.FechaVigencia = entrada.p_FechaVigencia;

		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ANEXO, new ArrayList<>(Collections.singletonList(anexoActualizar)), Anexo.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.updateAnexo", fin - inicio);
	}

	private void insertarSolicitudesFirma(Connection conn, List<SolicitudFirmaDocumento> solicitudesFirmas) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.insertarSolicitudesFirma");
		long inicio = System.currentTimeMillis();

		Iterator<Long> idsSolicitudes = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SolicitudFirmaAnexo", (long) solicitudesFirmas.size()).iterator();
		for (SolicitudFirmaDocumento solicitudFirma : solicitudesFirmas) {
			solicitudFirma.IdSolicitudFirmaAnexo = idsSolicitudes.next();
		}

		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_SOLICITUD_FIRMA_ANEXO, solicitudesFirmas, SolicitudFirmaDocumento.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.insertarSolicitudesFirma", fin - inicio);
	}

	private void upsertFirmasDocumento(Connection conn, Long idEmpresa, java.sql.Timestamp fechaFirmado, String idTipoCanalIngreso, ResumenPersona resumenAgente, List<SolicitudFirmaDocumento> solicitudesFirmas, String firmaSiNoEsAgenteDelCliente, Long firmaUsuario) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.upsertFirmasDocumento");
		long inicio = System.currentTimeMillis();

		List<SolicitudFirmaDocumento> solicitudesFirmasInsertar = new ArrayList<>();
		List<FirmaDocumento> firmasDocumentoInsertar = new ArrayList<>();
		List<FirmaDocumento> firmasDocumentoActualizar = new ArrayList<>();

		for (SolicitudFirmaDocumento solicitud : solicitudesFirmas) {
			FirmaDocumento firmaDocumento = new FirmaDocumento();
			firmaDocumento.FechaFirma = fechaFirmado;
			firmaDocumento.IdEstado = "ACT";
			firmaDocumento.IdFirmaAnexo = solicitud.IdFirmaAnexo;
			firmaDocumento.IdAnexo = solicitud.IdAnexo;
			firmaDocumento.IdSolicitudFirmaAnexo = solicitud.IdSolicitudFirmaAnexo;
			firmaDocumento.IdPersonaAgente = resumenAgente.IdPersona;
			firmaDocumento.IdTipoCanalIngreso = idTipoCanalIngreso;
			firmaDocumento.EsAutorizado = firmaSiNoEsAgenteDelCliente.equalsIgnoreCase("S")? "S": "P";
			firmaDocumento.IdUsuario = firmaUsuario;

			if (firmaDocumento.IdFirmaAnexo == null) {
				firmasDocumentoInsertar.add(firmaDocumento);
				solicitudesFirmasInsertar.add(solicitud);
			} else {
				firmasDocumentoActualizar.add(firmaDocumento);
			}
		}

		List<Long> idsFirmas = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::FirmaAnexo", (long) firmasDocumentoInsertar.size());
		for (int i = 0; i < firmasDocumentoInsertar.size(); i++) {
			SolicitudFirmaDocumento solicitud = solicitudesFirmasInsertar.get(i);
			solicitud.IdFirmaAnexo = idsFirmas.get(i);

			FirmaDocumento firmaDocumento = firmasDocumentoInsertar.get(i);
			firmaDocumento.IdFirmaAnexo = idsFirmas.get(i);
		}

		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO, firmasDocumentoInsertar, FirmaDocumento.class);
		sql.batch(conn, SQL_UPDATE_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO, firmasDocumentoActualizar, FirmaDocumento.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.upsertFirmasDocumento", fin - inicio);
	}

	private ResultadoCrearEventoAutorizacion crearEventoAutorizacion(Connection conn, EntradaGestionarFirmasDocumento entrada, ResumenDocumentoAnexo resumenAnexo, List<SolicitudFirmaDocumento> solicitudesFirmas) throws Exception {
		logger.debug("INICIO::GestionarFirmasDocumento.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();

		long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = entrada.p_UsaReglasNegocio;

		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdUnion = idTrabajo;
		evento.IdTipoEvento = "AGFICL";
		evento.IdConcepto = "AGFICL";
		evento.IdModuloSolicita = entrada.p_IdModulo;
		evento.NumeroOperacionConcepto = String.valueOf(resumenAnexo.IdAnexo);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = resumenAnexo.IdCliente;
		evento.IdUsuarioSolicita = entrada.p_IdUsuario;
		evento.Fecha = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		evento.Glosa = obtenerGlosa(resumenAnexo);

		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Collections.singletonList(evento);
		entradaCrearEventoAutorizacionMasivo.P_DETALLE = solicitudesFirmas.stream().map(sfd -> {
			CrearEventoAutorizacionMasivo.ObjTABLADETALLE detalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
			detalle.IdUnion = idTrabajo;
			detalle.NombreCampo = "IdFirmaAnexo";
			detalle.Valor = String.valueOf(sfd.IdFirmaAnexo);
			detalle.TipoDato = "LONG";
			detalle.Clasificacion = "1";

			return detalle;
		}).collect(Collectors.toList());

		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(conn, entradaCrearEventoAutorizacionMasivo);
		List<PlataformaUtils.MensajeSalida> eroresDetalleMensajes = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.stream().map(ed -> {
			PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
			mensajeSalida.codigo = ed.Codigo;
			mensajeSalida.descripcion = ed.Descripcion;
			mensajeSalida.id = 0L;
			mensajeSalida.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;

			return mensajeSalida;
		}).collect(Collectors.toList());
		List<PlataformaUtils.MensajeSalida> advertenciasDetalleMensajes = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.stream().map(ad -> {
			PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
			mensajeSalida.codigo = ad.Codigo;
			mensajeSalida.descripcion = ad.Descripcion;
			mensajeSalida.id = 0L;
			mensajeSalida.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;

			return mensajeSalida;
		}).collect(Collectors.toList());

		ResultadoCrearEventoAutorizacion resultadoCrearEventoAutorizacion = new ResultadoCrearEventoAutorizacion();
		resultadoCrearEventoAutorizacion.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		resultadoCrearEventoAutorizacion.P_OUT_MENSAJES.addAll(advertenciasDetalleMensajes);
		resultadoCrearEventoAutorizacion.P_OUT_MENSAJES.addAll(eroresDetalleMensajes);

		if (resultadoCrearEventoAutorizacion.P_OUT_ERRORESCONTADOR.equals(0L)) {
			resultadoCrearEventoAutorizacion.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(resumenAnexo.IdAnexo, PlataformaUtils.MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GestionarFirmasDocumento.crearEventoAutorizacion", fin - inicio);

		return resultadoCrearEventoAutorizacion;
	}

	private String obtenerGlosa(ResumenDocumentoAnexo resumenDocumentoAnexo) {
		logger.trace("INICIO::GestionarFirmasDocumento.obtenerGlosa");
		long inicio = System.currentTimeMillis();

		String glosa = resumenDocumentoAnexo.DocumentoIdentidadCliente + " / " + resumenDocumentoAnexo.NombreCompletoCliente;

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GestionarFirmasDocumento.obtenerGlosa", fin - inicio);

		return glosa;
	}

	/*
	 * Va a leer el parametro del modulo SOCIODENEGOCIO y para que no se caiga si no esta creado para este corredor pone un Try y asume una N
	 */
	private String getParametroFirmaSiNoEsAgenteDelCliente(Connection conn, Long idEmpresa) {
	String firmaSiNoEsAgenteDelCliente = "N";	
		try {
			firmaSiNoEsAgenteDelCliente = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "SOCIODENEGOCIO", "DOC.FirmaTambienSiNoEsAgenteDelCliente");
		}		catch (Exception e) {
			e.printStackTrace();
			firmaSiNoEsAgenteDelCliente="N";
		};
		return firmaSiNoEsAgenteDelCliente;
	}
	public static void main(String[] args) {
		System.out.println("Inicio prueba");

		EntradaGestionarFirmasDocumento entrada = new EntradaGestionarFirmasDocumento();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdUsuario = 4L;
		entrada.p_IdModulo = "SOC";
		entrada.p_UsaReglasNegocio = "N";
		entrada.p_IdTipoCanalIngreso = "OPT";

		entrada.p_IdAnexo = 431L;
		entrada.p_IdsPersonasFirmantes = new ArrayList<>(Arrays.asList(1418223L, 213957L));

		try (Connection conn = PlataformaUtils.getMASTER01Connection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OWP\",\"p_IdAnexo\":4068,\"p_IdsPersonasFirmantes\":[115245],\"p_FechaVigencia\":\"2023-03-31 03:00:00\",\"p_SolicitarFechaFirma\":\"N\",\"p_DocumentoSubido\":\"N\",\"p_FechaFirma\":null}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":106138,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_IdAnexo\":35,\"p_IdsPersonasFirmantes\":[567701],\"p_FechaVigencia\":\"2021-06-30 03:00:00\"}";
			Gson gson = Utils.getGsonBuilder();
			entrada = gson.fromJson(json, EntradaGestionarFirmasDocumento.class);

			System.out.println(entrada);

			try {
				GestionarFirmasDocumento gestionarFirmasDocumento = new GestionarFirmasDocumento();
				SalidaGestionarFirmasDocumento salida = gestionarFirmasDocumento.logica(conn, entrada);
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("Fin prueba");
	}
}
