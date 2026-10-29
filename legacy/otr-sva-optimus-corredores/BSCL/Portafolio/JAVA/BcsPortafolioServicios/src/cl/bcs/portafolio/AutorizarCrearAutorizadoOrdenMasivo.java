package cl.bcs.portafolio;

import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import java.sql.Connection;
import java.util.ArrayList;
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
import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para autorizar/rechazar creación y modificación de Autorizados según Portafolio.
 * Migración de:
 * - ConfirmarCrearAutorizadoOrdenMasivo.sql -> autorizarEvento()
 * - RechazaAutorizacionAutorizadoOrdenMasivo.sql -> rechazarEvento()
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class AutorizarCrearAutorizadoOrdenMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearAutorizadoOrdenMasivo.class);

	private final SQL sql = new SQL();

	// SQLs para operaciones
	private final String SQL_UPDATE_AUTORIZADO = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearAutorizadoOrdenMasivo_Update_Autorizado.sql");

	private final String SQL_UPDATE_AUTORIZADO_SOLO_ESTADO_AUTORIZACION = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearAutorizadoOrdenMasivo_Update_AutorizadoSoloEstadoAutorizacion.sql");

	private final String SQL_SELECT_PORTAFOLIO_APV = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearAutorizadoOrdenMasivo_Select_PortafolioAPV.sql");

	private final String SQL_SELECT_SOLICITUD_CAMBIO = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearAutorizadoOrdenMasivo_Select_SolicitudCambio.sql");

	// Clases internas para mapeo de datos
	public static class Autorizado {
		public Long IdAutorizado;
		public String IdEstadoAutorizacion;
		public String IdEstado;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Autorizado that = (Autorizado) o;
			return IdAutorizado != null && IdAutorizado.equals(that.IdAutorizado);
		}

		@Override
		public int hashCode() {
			return IdAutorizado != null ? IdAutorizado.hashCode() : 0;
		}
	}

	public static class PortafolioAPVInfo {
		public Long IdAutorizado;
		public Long IdPortafolio;
		public String EsAPV;
	}

	public static class SolicitudCambio {
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearAutorizadoOrdenMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Separar autorizados por tipo de evento
			List<Autorizado> autorizadosNAUTOR = entrada.p_Lista.stream()
					.filter(evento -> "NAUTOR".equals(evento.IdTipoEvento))
					.map(evento -> {
						Autorizado a = new Autorizado();
						a.IdAutorizado = Long.valueOf(evento.NumeroOperacionConcepto);
						a.IdEstadoAutorizacion = "AUT";
						a.IdEstado = "ACT";  // Nuevo autorizado → Activo
						return a;
					})
					.distinct()
					.collect(Collectors.toList());

			List<Autorizado> autorizadosCEATOR = entrada.p_Lista.stream()
					.filter(evento -> "CEATOR".equals(evento.IdTipoEvento))
					.map(evento -> {
						Autorizado a = new Autorizado();
						a.IdAutorizado = Long.valueOf(evento.NumeroOperacionConcepto);
						a.IdEstadoAutorizacion = "AUT";
						// No se modifica IdEstado para CEATOR
						return a;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch para NAUTOR (actualiza ambos campos)
			if (!autorizadosNAUTOR.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO, autorizadosNAUTOR, Autorizado.class);
			}

			// Ejecutar UPDATE en batch para CEATOR (solo IdEstadoAutorizacion)
			if (!autorizadosCEATOR.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO_SOLO_ESTADO_AUTORIZACION, autorizadosCEATOR, Autorizado.class);
			}

			// Combinar todas las listas para el resto del procesamiento
			List<Autorizado> autorizados = new ArrayList<>();
			autorizados.addAll(autorizadosNAUTOR);
			autorizados.addAll(autorizadosCEATOR);

			// Obtener información de portafolios APV
			HashMap<String, Object> paramsPortafolio = new HashMap<>();
			List<Long> idsAutorizados = autorizados.stream()
					.map(a -> a.IdAutorizado)
					.collect(Collectors.toList());
			paramsPortafolio.put("IdAutorizado", idsAutorizados);

			List<PortafolioAPVInfo> portafoliosInfo = sql.query(conn, SQL_SELECT_PORTAFOLIO_APV, paramsPortafolio, PortafolioAPVInfo.class);

			// Procesar réplicas APV para portafolios que lo requieran
			ReplicaPortafolioAPV replicaAPV = new ReplicaPortafolioAPV();
			for (PortafolioAPVInfo info : portafoliosInfo) {
				if ("S".equals(info.EsAPV) && info.IdPortafolio != null && info.IdPortafolio != 0) {
					logger.debug("Replicando portafolio APV: {}", info.IdPortafolio);

					EntradaReplicaPortafolioAPV entradaReplica = new EntradaReplicaPortafolioAPV();
					entradaReplica.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaReplica.p_IdUsuario = entrada.p_IdUsuario;
					entradaReplica.p_IdModulo = entrada.p_IdModulo;
					entradaReplica.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					entradaReplica.p_Portafolio = info.IdPortafolio;
					entradaReplica.p_IdEntidad = info.IdAutorizado;
					entradaReplica.p_TipoEntidad = "AUTORIZACION";
					entradaReplica.p_TipoOperacion = "I";

					SalidaReplicaPortafolioAPV salidaReplica = replicaAPV.logica(conn, entradaReplica);

					// Propagar errores
					if (salidaReplica.P_OUT_ERRORESCONTADOR > 0) {
						salida.P_OUT_ERRORESCONTADOR += salidaReplica.P_OUT_ERRORESCONTADOR;
						logger.warn("Errores en réplica APV para autorizado {}: {}", info.IdAutorizado, salidaReplica.P_OUT_ERRORESCONTADOR);
					}
				}
			}

			// Obtener solicitudes de cambio pendientes
			HashMap<String, Object> paramsSolicitud = new HashMap<>();
			List<String> idsAutorizadosStr = autorizados.stream()
					.map(a -> String.valueOf(a.IdAutorizado))
					.collect(Collectors.toList());
			paramsSolicitud.put("IdEntidadOrigen", idsAutorizadosStr);
			paramsSolicitud.put("IdTipoEntidad", "ORDEN-AUTORIZADO");

			List<SolicitudCambio> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUD_CAMBIO, paramsSolicitud, SolicitudCambio.class);

			// Actualizar estado de solicitudes de cambio
			if (!solicitudes.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();

				List<EntidadOrigen> entidadesOrigen = solicitudes.stream()
						.map(s -> {
							EntidadOrigen eo = new EntidadOrigen();
							eo.IdEntidadOrigen = s.IdEntidadOrigen;
							eo.IdTipoEntidad = s.IdTipoEntidad;
							return eo;
						})
						.collect(Collectors.toList());

				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesOrigen;
				entradaSolicitud.p_IdEstadoAutorizacion = "AUT";

				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}

			// Agregar resultados
			for (Autorizado autorizado : autorizados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = autorizado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearAutorizadoOrdenMasivo.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearAutorizadoOrdenMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Separar autorizados por tipo de evento
			List<Autorizado> autorizadosNAUTOR = entrada.p_Lista.stream()
					.filter(evento -> "NAUTOR".equals(evento.IdTipoEvento))
					.map(evento -> {
						Autorizado a = new Autorizado();
						a.IdAutorizado = Long.valueOf(evento.NumeroOperacionConcepto);
						a.IdEstadoAutorizacion = "RZD";
						a.IdEstado = "INA";  // Nuevo rechazado
						return a;
					})
					.distinct()
					.collect(Collectors.toList());

			List<Autorizado> autorizadosCEATOR = entrada.p_Lista.stream()
					.filter(evento -> "CEATOR".equals(evento.IdTipoEvento))
					.map(evento -> {
						Autorizado a = new Autorizado();
						a.IdAutorizado = Long.valueOf(evento.NumeroOperacionConcepto);
						a.IdEstadoAutorizacion = "RZD";
						// No se modifica IdEstado para CEATOR
						return a;
					})
					.distinct()
					.collect(Collectors.toList());

			// Procesar solicitudes de cambio para eventos de modificación (CEATOR)
			List<EntidadOrigen> entidadesCEATOR = entrada.p_Lista.stream()
					.filter(evento -> {
						String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						return "CEATOR".equals(tipo);
					})
					.map(evento -> {
						EntidadOrigen eo = new EntidadOrigen();
						eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
						eo.IdTipoEntidad = "ORDENAUTORIZADO-ESTADO";
						return eo;
					})
					.collect(Collectors.toList());

			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo si hay eventos CEATOR
			if (!entidadesCEATOR.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesCEATOR;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";

				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}

			// Ejecutar UPDATE en batch según tipo de evento
			// NAUTOR: actualiza IdEstadoAutorizacion e IdEstado
			if (!autorizadosNAUTOR.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO, autorizadosNAUTOR, Autorizado.class);
			}

			// CEATOR: solo actualiza IdEstadoAutorizacion (mantiene IdEstado actual)
			if (!autorizadosCEATOR.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO_SOLO_ESTADO_AUTORIZACION, autorizadosCEATOR, Autorizado.class);
			}

			// Agregar resultados para todos los autorizados procesados
			List<Autorizado> todosAutorizados = new ArrayList<>();
			todosAutorizados.addAll(autorizadosNAUTOR);
			todosAutorizados.addAll(autorizadosCEATOR);

			for (Autorizado autorizado : todosAutorizados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = autorizado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearAutorizadoOrdenMasivo.rechazarEvento {}", fin - inicio);

		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarCrearAutorizadoOrdenMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				// Test AUTORIZAR - evento NAUTOR (Nuevo Autorizado)
				System.out.println("\n=== TEST AUTORIZAR (NAUTOR) ===");
				String jsonAutorizar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NAUTOR\"}]}";
				EntradaAutorizacion entradaAutorizar = Utils.getGsonBuilder().fromJson(jsonAutorizar, EntradaAutorizacion.class);

				AutorizarCrearAutorizadoOrdenMasivo servicio = new AutorizarCrearAutorizadoOrdenMasivo();
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entradaAutorizar);
				
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento NAUTOR (Nuevo Autorizado)
				System.out.println("\n=== TEST RECHAZAR (NAUTOR) ===");
				String jsonRechazarNuevo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NAUTOR\"}]}";
				EntradaAutorizacion entradaRechazarNuevo = Utils.getGsonBuilder().fromJson(jsonRechazarNuevo, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarNuevo = servicio.rechazarEvento(conn, entradaRechazarNuevo);
				System.out.println("Errores: " + salidaRechazarNuevo.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarNuevo.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento CEATOR (Cambio Estado Autorizado)
				System.out.println("\n=== TEST RECHAZAR (CEATOR) ===");
				String jsonRechazarMod = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":124,\"NumeroOperacionConcepto\":\"789\",\"IdTipoEvento\":\"CEATOR\"}]}";
				EntradaAutorizacion entradaRechazarMod = Utils.getGsonBuilder().fromJson(jsonRechazarMod, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarMod = servicio.rechazarEvento(conn, entradaRechazarMod);
				System.out.println("Errores: " + salidaRechazarMod.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarMod.P_OUT_RESULTADO.size());
				conn.rollback();

			} catch (Exception e) {
				e.printStackTrace();
				conn.rollback();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n== FIN TEST ==");
	}
}
