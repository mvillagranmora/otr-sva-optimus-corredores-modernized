package cl.bcs.socionegocio.emp;

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

public class AutorizacionModificacionEstadoEmpleadoMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoEmpleadoMasivo.class);

	private final String SQL_UPDATE_EMPLEADO_AUTORIZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleadoMasivo_Update_EmpleadoAutorizado.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleadoMasivo_Update_EstadoSolicitud.sql");

	public static class EmpleadoEstadoAutorizacion {
		public Long IdEmpleado;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // CAEEMP (cambio estado empleado)
	}

	/**
	 * AUTORIZAR: Migración de ConfirmarModificarEstadoEmpleadoMasivo.sql
	 * Actualiza campos de estado (IdEstado, IdTipoBloqueo, IdTipoDesbloqueo, FechaBloqueo, FechaDesbloqueo, ComentarioBloqueo)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoEmpleadoMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<EmpleadoEstadoAutorizacion> empleados = entrada.p_Lista.stream()
				.map(evento -> {
					EmpleadoEstadoAutorizacion e = new EmpleadoEstadoAutorizacion();
					e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
					e.IdEstadoAutorizacion = "AUT";
					e.IdConcepto = evento.IdTipoEvento;  // CAEEMP
					return e;
				})
				.distinct()
				.collect(Collectors.toList());

			// 1. Actualizar campos de estado en Empleado
			new SQL().batch(conn, SQL_UPDATE_EMPLEADO_AUTORIZADO, empleados, EmpleadoEstadoAutorizacion.class);

			// 2. Actualizar estado de solicitudes a AUT
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, empleados, EmpleadoEstadoAutorizacion.class);

			// Agregar resultado por cada empleado procesado
			for (EmpleadoEstadoAutorizacion empleado : empleados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = empleado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionModificacionEstadoEmpleadoMasivo.autorizarEvento");
		return salida;
	}

	/**
	 * RECHAZAR: Migración de RechazaAutorizacionEmpleadoDatosGeneralesMasivo.sql (con concepto CAEEMP)
	 * Solo actualiza estado de solicitudes a RZD (NO modifica tabla Empleado)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoEmpleadoMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<EmpleadoEstadoAutorizacion> empleados = entrada.p_Lista.stream()
				.map(evento -> {
					EmpleadoEstadoAutorizacion e = new EmpleadoEstadoAutorizacion();
					e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
					e.IdEstadoAutorizacion = "RZD";
					e.IdConcepto = evento.IdTipoEvento;  // CAEEMP
					return e;
				})
				.distinct()
				.collect(Collectors.toList());

			// Solo actualizar estado de solicitudes a RZD (no toca la tabla Empleado)
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, empleados, EmpleadoEstadoAutorizacion.class);

			// Agregar resultado por cada empleado procesado
			for (EmpleadoEstadoAutorizacion empleado : empleados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = empleado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionModificacionEstadoEmpleadoMasivo.rechazarEvento");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CAEEMP\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionModificacionEstadoEmpleadoMasivo servicio = new AutorizacionModificacionEstadoEmpleadoMasivo();

				System.out.println("\n=== Probando AUTORIZAR ===");
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Salida autorizar: " + salidaAutorizar);
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				conn.rollback();

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
