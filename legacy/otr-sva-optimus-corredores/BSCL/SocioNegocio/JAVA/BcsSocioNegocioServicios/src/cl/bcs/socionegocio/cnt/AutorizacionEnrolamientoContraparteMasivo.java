package cl.bcs.socionegocio.cnt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para autorizar/rechazar enrolamiento de contrapartes.
 * Migración de:
 * - ConfirmaAutorizacionEnrolamientoContraparteMasivo.sql -> autorizarEvento()
 * - RechazaAutorizacionEnrolamientoContraparteMasivo.sql -> rechazarEvento()
 * 
 * Autor: Migración Java
 * Fecha: Junio 2026
 */
public class AutorizacionEnrolamientoContraparteMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionEnrolamientoContraparteMasivo.class);

	private final String SQL_UPDATE_CONTRAPARTE_AUTORIZADO = PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/cnt/sql/AutorizacionEnrolamientoContraparteMasivo_Update_ContraparteAutorizado.sql");

	private final String SQL_UPDATE_PERSONA_AUTORIZADO = PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/cnt/sql/AutorizacionEnrolamientoContraparteMasivo_Update_PersonaAutorizado.sql");

	private final String SQL_UPDATE_CONTRAPARTE_RECHAZADO = PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/cnt/sql/AutorizacionEnrolamientoContraparteMasivo_Update_ContraparteRechazado.sql");

	private final SQL sql = new SQL();

	public static class ContraparteAutorizacion {
		public Long IdContraparte;
		public String IdEstadoAutorizacion;
	}

	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmaAutorizacionEnrolamientoContraparteMasivo
	 * - Actualiza Contraparte: EsEnrolado = 'S' (donde estaba 'P' o 'N') - SQL_UPDATE_CONTRAPARTE_AUTORIZADO
	 * - Actualiza Persona: EsEnrolado = 'S' (donde estaba 'P' o 'N') - SQL_UPDATE_PERSONA_AUTORIZADO
	 * - Actualiza estado de SolicitudCambio a 'AUT'
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoContraparteMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			// Extraer IDs de contrapartes desde entrada
			List<ContraparteAutorizacion> contrapartes = entrada.p_Lista.stream()
					.map(evento -> {
						ContraparteAutorizacion c = new ContraparteAutorizacion();
						c.IdContraparte = Long.valueOf(evento.NumeroOperacionConcepto);
						c.IdEstadoAutorizacion = "AUT";
						return c;
					})
					.distinct()
					.collect(Collectors.toList());

			if (contrapartes.isEmpty()) {
				logger.warn("No hay contrapartes a autorizar");
				return salida;
			}

			// 1. Actualizar Contraparte: EsEnrolado = 'S'
			logger.debug("Actualizando estado de contrapartes a enrollado");
			sql.batch(conn, SQL_UPDATE_CONTRAPARTE_AUTORIZADO, contrapartes, ContraparteAutorizacion.class);

			// 2. Actualizar Persona: EsEnrolado = 'S'
			logger.debug("Actualizando estado de personas a enrollado");
			sql.batch(conn, SQL_UPDATE_PERSONA_AUTORIZADO, contrapartes, ContraparteAutorizacion.class);

			// 3. Actualizar estado de SolicitudCambio a 'AUT'
			logger.debug("Actualizando estado de SolicitudCambio a autorizado");
			ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
			
			List<EntidadOrigen> entidadesOrigen = new ArrayList<>();
			for (Evento evento : entrada.p_Lista) {
				EntidadOrigen entidad = new EntidadOrigen();
				entidad.IdEntidadOrigen = evento.NumeroOperacionConcepto;
				entidad.IdTipoEntidad = "CONTRAPARTE";
				entidadesOrigen.add(entidad);
			}

			EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
			entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
			entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
			entradaSolicitud.p_SolicitudCambio = new ArrayList<>();
			entradaSolicitud.p_EntidadOrigen = entidadesOrigen;
			entradaSolicitud.p_IdEstadoAutorizacion = "AUT";

			ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

			// Propagar advertencias
			if (salidaSolicitud.p_out_AdvertenciasContador > 0) {
				for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : salidaSolicitud.p_out_AdvertenciasDetalle) {
					salida.P_OUT_MENSAJES.add(new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(0L, 
							cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, 
							adv.Descripcion, new java.util.HashMap<>(), conn, entrada.p_IdEmpresa));
				}
			}

			// Propagar errores
			if (salidaSolicitud.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR = (long) salidaSolicitud.p_out_ErroresContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaSolicitud.p_out_ErroresDetalle) {
					salida.P_OUT_MENSAJES.add(new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(0L, 
							cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida.MENSAJE_ERROR, 
							err.Descripcion, new java.util.HashMap<>(), conn, entrada.p_IdEmpresa));
				}
				logger.error("Errores al actualizar estado de solicitud de cambio: {}", salida.P_OUT_ERRORESCONTADOR);
				return salida;
			}

			// Agregar resultado por cada contraparte procesada
			for (ContraparteAutorizacion contraparte : contrapartes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = contraparte;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

			long fin = System.currentTimeMillis();
			logger.info("FIN::AutorizacionEnrolamientoContraparteMasivo.autorizarEvento en {} ms", fin - inicio);

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		return salida;
	}

	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionEnrolamientoContraparteMasivo
	 * - Actualiza Contraparte: EsEnrolado = 'N' (solo si estaba 'P')
	 * - Actualiza estado de SolicitudCambio a 'RZD'
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoContraparteMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			// Extraer IDs de contrapartes desde entrada
			List<ContraparteAutorizacion> contrapartes = entrada.p_Lista.stream()
					.map(evento -> {
						ContraparteAutorizacion c = new ContraparteAutorizacion();
						c.IdContraparte = Long.valueOf(evento.NumeroOperacionConcepto);
						c.IdEstadoAutorizacion = "RZD";
						return c;
					})
					.distinct()
					.collect(Collectors.toList());

			if (contrapartes.isEmpty()) {
				logger.warn("No hay contrapartes a rechazar");
				return salida;
			}

			// 1. Actualizar Contraparte: EsEnrolado = 'N' (solo donde estaba 'P')
			logger.debug("Actualizando estado de contrapartes a rechazado");
			sql.batch(conn, SQL_UPDATE_CONTRAPARTE_RECHAZADO, contrapartes, ContraparteAutorizacion.class);

			// 2. Actualizar estado de SolicitudCambio a 'RZD'
			logger.debug("Actualizando estado de SolicitudCambio a rechazado");
			ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
			
			List<EntidadOrigen> entidadesOrigen = new ArrayList<>();
			for (Evento evento : entrada.p_Lista) {
				EntidadOrigen entidad = new EntidadOrigen();
				entidad.IdEntidadOrigen = evento.NumeroOperacionConcepto;
				entidad.IdTipoEntidad = "CONTRAPARTE";
				entidadesOrigen.add(entidad);
			}

			EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
			entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
			entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
			entradaSolicitud.p_SolicitudCambio = new ArrayList<>();
			entradaSolicitud.p_EntidadOrigen = entidadesOrigen;
			entradaSolicitud.p_IdEstadoAutorizacion = "RZD";

			ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

			// Propagar advertencias
			if (salidaSolicitud.p_out_AdvertenciasContador > 0) {
				for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : salidaSolicitud.p_out_AdvertenciasDetalle) {
					salida.P_OUT_MENSAJES.add(new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(0L, 
							cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, 
							adv.Descripcion, new java.util.HashMap<>(), conn, entrada.p_IdEmpresa));
				}
			}

			// Propagar errores
			if (salidaSolicitud.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR = (long) salidaSolicitud.p_out_ErroresContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaSolicitud.p_out_ErroresDetalle) {
					salida.P_OUT_MENSAJES.add(new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(0L, 
							cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida.MENSAJE_ERROR, 
							err.Descripcion, new java.util.HashMap<>(), conn, entrada.p_IdEmpresa));
				}
				logger.error("Errores al actualizar estado de solicitud de cambio: {}", salida.P_OUT_ERRORESCONTADOR);
				return salida;
			}

			// Agregar resultado por cada contraparte procesada
			for (ContraparteAutorizacion contraparte : contrapartes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = contraparte;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

			long fin = System.currentTimeMillis();
			logger.info("FIN::AutorizacionEnrolamientoContraparteMasivo.rechazarEvento en {} ms", fin - inicio);

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizacionEnrolamientoContraparteMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":1,\"IdEtapa\":1}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":1,\"NumeroOperacionConcepto\":\"1\",\"IdTipoEvento\":\"NENCTR\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionEnrolamientoContraparteMasivo servicio = new AutorizacionEnrolamientoContraparteMasivo();

				// Probar AUTORIZAR
				System.out.println("\n=== Probando AUTORIZAR ===");
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Salida autorizar: " + salidaAutorizar);
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				conn.rollback();

				// Probar RECHAZAR
				System.out.println("\n=== Probando RECHAZAR ===");
				SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
				System.out.println("Salida rechazar: " + salidaRechazar);
				System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
				conn.rollback();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n== FIN TEST ==");
	}
}
