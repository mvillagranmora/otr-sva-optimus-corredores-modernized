package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionEstadoPersonaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoPersonaMasivo.class);

	private final String SQL_UPDATE_PERSONA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_Persona.sql");
	private final String SQL_UPDATE_CLIENTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_Cliente.sql");
	private final String SQL_UPDATE_PORTAFOLIO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_Portafolio.sql");
	private final String SQL_UPDATE_EMPLEADO_MUERTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_EmpleadoMuerte.sql");
	private final String SQL_UPDATE_CONTRAPARTE_MUERTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_ContraparteMuerte.sql");
	private final String SQL_UPDATE_PROVEEDOR_MUERTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_ProveedorMuerte.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaMasivo_Update_EstadoSolicitud.sql");

	public static class PersonaAutorizacion {
		public Long IdPersona;
		public String IdEstadoAutorizacion;
	}

	/**
	 * AUTORIZAR: Migración del SP ConfirmarModificarEstadoPersonaMasivo (PERSONA-ESTADO / CAEPER)
	 * Actualiza Persona, Cliente, Portafolio y entidades relacionadas por muerte (Empleado, Contraparte, Proveedor)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoPersonaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<PersonaAutorizacion> personas = entrada.p_Lista.stream()
				.map(evento -> {
					PersonaAutorizacion p = new PersonaAutorizacion();
					p.IdPersona = Long.valueOf(evento.NumeroOperacionConcepto);
					p.IdEstadoAutorizacion = "AUT";
					return p;
				})
				.distinct()
				.collect(Collectors.toList());

			// 1. Actualizar Persona con valores desde SolicitudCambio
			new SQL().batch(conn, SQL_UPDATE_PERSONA, personas, PersonaAutorizacion.class);

			// 2. Propagar cambios a Cliente
			new SQL().batch(conn, SQL_UPDATE_CLIENTE, personas, PersonaAutorizacion.class);

			// 3. Propagar cambios a Portafolio (a través de Cliente)
			new SQL().batch(conn, SQL_UPDATE_PORTAFOLIO, personas, PersonaAutorizacion.class);

			// 4. Actualizar entidades por muerte (solo aplica si existe cambio BMUERT)
			new SQL().batch(conn, SQL_UPDATE_EMPLEADO_MUERTE, personas, PersonaAutorizacion.class);
			new SQL().batch(conn, SQL_UPDATE_CONTRAPARTE_MUERTE, personas, PersonaAutorizacion.class);
			new SQL().batch(conn, SQL_UPDATE_PROVEEDOR_MUERTE, personas, PersonaAutorizacion.class);

			// 5. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, personas, PersonaAutorizacion.class);

			for (PersonaAutorizacion persona : personas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = persona;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionModificacionEstadoPersonaMasivo.autorizarEvento");
		return salida;
	}

	/**
	 * RECHAZAR: Migración del SP RechazaAutorizacionPersonaDatosGeneralesMasivo (p_IdConcepto='CAEPER')
	 * Solo actualiza el estado de SolicitudCambio a Rechazado, sin modificar entidades
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoPersonaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<PersonaAutorizacion> personas = entrada.p_Lista.stream()
				.map(evento -> {
					PersonaAutorizacion p = new PersonaAutorizacion();
					p.IdPersona = Long.valueOf(evento.NumeroOperacionConcepto);
					p.IdEstadoAutorizacion = "RZD";
					return p;
				})
				.distinct()
				.collect(Collectors.toList());

			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, personas, PersonaAutorizacion.class);

			for (PersonaAutorizacion persona : personas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = persona;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionModificacionEstadoPersonaMasivo.rechazarEvento");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76816,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CAEPER\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionModificacionEstadoPersonaMasivo servicio = new AutorizacionModificacionEstadoPersonaMasivo();

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

		System.out.println("== FIN TEST ==");
	}
}
