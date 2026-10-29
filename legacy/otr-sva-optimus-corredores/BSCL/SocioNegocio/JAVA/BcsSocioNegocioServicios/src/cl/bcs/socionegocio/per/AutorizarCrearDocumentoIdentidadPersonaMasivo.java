package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils; 

public class AutorizarCrearDocumentoIdentidadPersonaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearDocumentoIdentidadPersonaMasivo.class);
	
	private final SQL sql = new SQL();
	
	private final String SQL_UPDATE_DOCUMENTO_IDENTIDAD = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDocumentoIdentidadPersonaMasivo_Update_DocumentoIdentidad.sql"
		);
	
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDocumentoIdentidadPersonaMasivo_Insert_LogGenerarContratos.sql"
		);
	
	private final String SQL_SELECT_PERSONA_POR_DOCUMENTO = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDocumentoIdentidadPersonaMasivo_Select_PersonaPorDocumento.sql"
		);
	
	public static class DocumentoIdentidad {
		public Long IdDocumentoIdentidad;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		public String Borrado;
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DocumentoIdentidad that = (DocumentoIdentidad) o;
			return IdDocumentoIdentidad != null && IdDocumentoIdentidad.equals(that.IdDocumentoIdentidad);
		}
		
		@Override
		public int hashCode() {
			return IdDocumentoIdentidad != null ? IdDocumentoIdentidad.hashCode() : 0;
		}
	}
	
	public static class LogGenerarContratos {
		public Long Idlog;
		public String CodigoFormulario;
		public String CampoBackend;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdUsuario;
		public String FechaSistema;
		public Long IdPrograma;
		public String Estado;
		public String Mensaje;
	}
	
	public static class PersonaDocumento {
		public Long IdPersona;
		public Long IdDocumentoIdentidad;
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDocumentoIdentidadPersonaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DocumentoIdentidad> documentos = entrada.p_Lista.stream()
				.map(evento -> {
					DocumentoIdentidad d = new DocumentoIdentidad();
					d.IdDocumentoIdentidad = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";
					d.IdEstado = null;
					d.Borrado = null;
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DOCUMENTO_IDENTIDAD, documentos, DocumentoIdentidad.class);
			
			HashMap<String, Object> paramsPersona = new HashMap<>();
			List<Long> idsDocumento = documentos.stream()
				.map(d -> d.IdDocumentoIdentidad)
				.collect(Collectors.toList());
			paramsPersona.put("IdDocumentoIdentidad", idsDocumento);
			
			List<PersonaDocumento> personasDocumentos = sql.query(conn, SQL_SELECT_PERSONA_POR_DOCUMENTO, paramsPersona, PersonaDocumento.class);
			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			String fechaSistemaStr = fechaSistema.substring(0, 10);
			
			List<Long> secuencias = Utils.getSecuencia(
				conn,
				"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos",
				Long.valueOf(personasDocumentos.size())
			);
			
			List<LogGenerarContratos> logsContratos = new java.util.ArrayList<>();
			for (int i = 0; i < personasDocumentos.size(); i++) {
				PersonaDocumento pd = personasDocumentos.get(i);
				LogGenerarContratos log = new LogGenerarContratos();
				log.Idlog = secuencias.get(i);
				log.CodigoFormulario = "DOCUMENTO_IDENTIDAD_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.IdCliente = 0L;
				log.IdPersona = pd.IdPersona;
				log.IdUsuario = entrada.p_IdUsuario;
				log.FechaSistema = fechaSistemaStr;
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Persona - ConfirmarCrearPersonaDocumentosIdentidadaMasivo.sql";
				logsContratos.add(log);
			}
			
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);
			
			for (DocumentoIdentidad documento : documentos) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = documento;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDocumentoIdentidadPersonaMasivo.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDocumentoIdentidadPersonaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DocumentoIdentidad> documentos = entrada.p_Lista.stream()
				.map(evento -> {
					DocumentoIdentidad d = new DocumentoIdentidad();
					d.IdDocumentoIdentidad = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";
					
					String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					if ("NUEDOC".equals(idTipoEvento)) {
						d.IdEstado = "INA";
						d.Borrado = "S";
					} else {
						d.IdEstado = null;
						d.Borrado = "N";
					}
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DOCUMENTO_IDENTIDAD, documentos, DocumentoIdentidad.class);
			
			List<EntidadOrigen> entidadesMODDOC = entrada.p_Lista.stream()
				.filter(evento -> {
					String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					return "MODDOC".equals(tipo) || "CAEDOC".equals(tipo);
				})
				.map(evento -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
					eo.IdTipoEntidad = "DOCUMENTOIDENTIDAD";
					return eo;
				})
				.collect(Collectors.toList());
			
			if (!entidadesMODDOC.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
				
				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMODDOC;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
				
				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
				
				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}
			
			for (DocumentoIdentidad documento : documentos) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = documento;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDocumentoIdentidadPersonaMasivo.rechazarEvento {}", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarCrearDocumentoIdentidadPersonaMasivo servicio = new AutorizarCrearDocumentoIdentidadPersonaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 76770L;
			entrada.p_IdModulo = "AUT";
			
			cl.bcs.plataforma.aut.pojo.Evento evento = new cl.bcs.plataforma.aut.pojo.Evento();
			evento.NumeroOperacionConcepto = "123731";
			evento.IdTipoEvento = "MODDOC";  // MODDOC, CAEDOC, NUEDOC
			evento.IdEmpresa = 1L;
			evento.IdUsuario = 76770L;
			evento.IdModulo = "AUT";
			entrada.p_Lista.add(evento);
			
			System.out.println("\n=== Probando CONFIRMAR ===");
			SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
			System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaConfirmar.P_OUT_RESULTADO.size());

			System.out.println("\n=== Probando RECHAZAR ===");
			SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
			System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
