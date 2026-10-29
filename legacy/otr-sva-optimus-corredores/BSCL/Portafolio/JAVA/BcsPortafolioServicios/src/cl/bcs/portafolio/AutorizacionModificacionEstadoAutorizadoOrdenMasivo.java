package cl.bcs.portafolio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionEstadoAutorizadoOrdenMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoAutorizadoOrdenMasivo.class);

	private static final String ESTADO_AUTORIZADO = "AUT";
	private static final String ESTADO_RECHAZADO = "RZD";
	private static final String ESTADO_PENDIENTE = "PEN";
	private static final String TIPO_ENTIDAD_ESTADO = "ORDENAUTORIZADO-ESTADO";
	private static final String ID_CONCEPTO_CAMBIO_ESTADO = "CEATOR";

	private final SQL sql = new SQL();

	private final String SQL_COUNT_SOLICITUD = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoAutorizadoOrdenMasivo_Count_Solicitud.sql");
	private final String SQL_SELECT_DATOS_SOLICITUD = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoAutorizadoOrdenMasivo_Select_DatosSolicitud.sql");
	private final String SQL_UPDATE_AUTORIZADO_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoAutorizadoOrdenMasivo_Update_AutorizadoEstado.sql");
	private final String SQL_UPDATE_AUTORIZADO_RECHAZO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoAutorizadoOrdenMasivo_Update_AutorizadoRechazo.sql");

	public static class AutorizadoEvento {
		public Long IdAutorizado;
		public String IdConcepto;
		public Long IdEvento;
	}

	public static class Contador {
		public Long Contador;
	}

	public static class DatosSolicitud {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
		public Long IdAutorizado;
	}

	public static class UpdateAutorizadoEstado {
		public Long IdAutorizado;
		public String IdEstado;
	}

	public static class UpdateAutorizadoRechazo {
		public Long IdAutorizado;
		public String IdConcepto;
		public String IdEstadoAutorizacion;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoAutorizadoOrdenMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<AutorizadoEvento> autorizados = obtenerAutorizados(entrada);
			List<Long> idsAutorizado = autorizados.stream()
				.map(item -> item.IdAutorizado)
				.collect(Collectors.toList());

			if (!existenSolicitudesPendientes(conn, idsAutorizado, TIPO_ENTIDAD_ESTADO)) {
				return agregarErrorSolicitudNoExiste(conn, entrada, salida);
			}

			List<DatosSolicitud> datosSolicitud = obtenerDatosSolicitud(conn, idsAutorizado, TIPO_ENTIDAD_ESTADO);
			if (datosSolicitud.isEmpty()) {
				return agregarErrorSolicitudNoExiste(conn, entrada, salida);
			}

			List<UpdateAutorizadoEstado> updates = construirUpdatesAutorizadoEstado(datosSolicitud);
			if (!updates.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO_ESTADO, updates, UpdateAutorizadoEstado.class);
			}

			modificarEstadoSolicitud(conn, entrada, datosSolicitud, ESTADO_AUTORIZADO, salida);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			agregarLog(conn, entrada, datosSolicitud, ESTADO_AUTORIZADO);
			agregarResultados(salida, autorizados);
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_AUTORIZACION", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
				"ERROR_AUTORIZACION", descripcion));
		}

		logger.info("FIN::AutorizacionModificacionEstadoAutorizadoOrdenMasivo.autorizarEvento");
		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoAutorizadoOrdenMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<AutorizadoEvento> autorizados = obtenerAutorizados(entrada);
			List<AutorizadoEvento> cambioEstado = autorizados.stream()
				.filter(item -> ID_CONCEPTO_CAMBIO_ESTADO.equals(item.IdConcepto))
				.collect(Collectors.toList());

			if (!cambioEstado.isEmpty()) {
				List<Long> idsCambioEstado = cambioEstado.stream()
					.map(item -> item.IdAutorizado)
					.collect(Collectors.toList());

				if (!existenSolicitudesPendientes(conn, idsCambioEstado, TIPO_ENTIDAD_ESTADO)) {
					return agregarErrorSolicitudNoExiste(conn, entrada, salida);
				}

				List<DatosSolicitud> datosSolicitud = obtenerDatosSolicitud(conn, idsCambioEstado, TIPO_ENTIDAD_ESTADO);
				if (datosSolicitud.isEmpty()) {
					return agregarErrorSolicitudNoExiste(conn, entrada, salida);
				}

				modificarEstadoSolicitud(conn, entrada, datosSolicitud, ESTADO_RECHAZADO, salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}
			}

			List<UpdateAutorizadoRechazo> updatesRechazo = autorizados.stream()
				.map(item -> {
					UpdateAutorizadoRechazo update = new UpdateAutorizadoRechazo();
					update.IdAutorizado = item.IdAutorizado;
					update.IdConcepto = item.IdConcepto;
					update.IdEstadoAutorizacion = ESTADO_RECHAZADO;
					return update;
				})
				.collect(Collectors.toList());

			if (!updatesRechazo.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_AUTORIZADO_RECHAZO, updatesRechazo, UpdateAutorizadoRechazo.class);
			}

			agregarResultados(salida, autorizados);
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_RECHAZO", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
				"ERROR_RECHAZO", descripcion));
		}

		logger.info("FIN::AutorizacionModificacionEstadoAutorizadoOrdenMasivo.rechazarEvento");
		return salida;
	}

	private List<AutorizadoEvento> obtenerAutorizados(EntradaAutorizacion entrada) {
		Map<String, AutorizadoEvento> mapa = new LinkedHashMap<>();
		for (Evento evento : entrada.p_Lista) {
			AutorizadoEvento autorizado = new AutorizadoEvento();
			autorizado.IdAutorizado = Long.valueOf(evento.NumeroOperacionConcepto);
			autorizado.IdConcepto = evento.IdTipoEvento;
			autorizado.IdEvento = evento.IdEvento;
			mapa.put(autorizado.IdAutorizado + "_" + autorizado.IdConcepto, autorizado);
		}
		return new ArrayList<>(mapa.values());
	}

	private boolean existenSolicitudesPendientes(Connection conn, List<Long> idsAutorizado, String idTipoEntidad) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdsAutorizado", idsAutorizado);
		params.put("IdTipoEntidad", idTipoEntidad);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);

		List<Contador> resultado = sql.query(conn, SQL_COUNT_SOLICITUD, params, Contador.class);
		Long contador = (resultado != null && !resultado.isEmpty() && resultado.get(0).Contador != null)
			? resultado.get(0).Contador : 0L;
		return contador > 0;
	}

	private List<DatosSolicitud> obtenerDatosSolicitud(Connection conn, List<Long> idsAutorizado, String idTipoEntidad) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdsAutorizado", idsAutorizado);
		params.put("IdTipoEntidad", idTipoEntidad);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		return sql.query(conn, SQL_SELECT_DATOS_SOLICITUD, params, DatosSolicitud.class);
	}

	private List<UpdateAutorizadoEstado> construirUpdatesAutorizadoEstado(List<DatosSolicitud> datosSolicitud) {
		Map<Long, UpdateAutorizadoEstado> mapa = new LinkedHashMap<>();

		for (DatosSolicitud dato : datosSolicitud) {
			if (!"IdEstado".equals(dato.CampoModificado)) {
				continue;
			}

			UpdateAutorizadoEstado update = mapa.get(dato.IdAutorizado);
			if (update == null) {
				update = new UpdateAutorizadoEstado();
				update.IdAutorizado = dato.IdAutorizado;
				mapa.put(dato.IdAutorizado, update);
			}
			update.IdEstado = dato.ValorNuevo;
		}

		return new ArrayList<>(mapa.values());
	}

	private void modificarEstadoSolicitud(Connection conn, EntradaAutorizacion entrada, List<DatosSolicitud> datosSolicitud,
			String idEstadoAutorizacion, SalidaAutorizacion salida) throws Exception {

		ModificarEstadoRegistroSolicitudCambioMasivo servicio = new ModificarEstadoRegistroSolicitudCambioMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado in =
			new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();

		in.p_IdEmpresa = entrada.p_IdEmpresa;
		in.p_IdUsuario = entrada.p_IdUsuario;
		in.p_IdModulo = entrada.p_IdModulo;
		in.p_IdEstadoAutorizacion = idEstadoAutorizacion;

		Map<String, ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen> mapa = new LinkedHashMap<>();
		for (DatosSolicitud dato : datosSolicitud) {
			String key = dato.IdEntidadOrigen + "_" + dato.IdTipoEntidad;
			if (!mapa.containsKey(key)) {
				ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen entidad =
					new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
				entidad.IdEntidadOrigen = dato.IdEntidadOrigen;
				entidad.IdTipoEntidad = dato.IdTipoEntidad;
				mapa.put(key, entidad);
			}
		}
		in.p_EntidadOrigen.addAll(mapa.values());

		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado out = servicio.logica(conn, in);
		if (out.p_out_AdvertenciasContador > 0 && out.p_out_AdvertenciasDetalle != null) {
			for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : out.p_out_AdvertenciasDetalle) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, adv.Codigo, adv.Descripcion));
			}
		}
		if (out.p_out_ErroresContador > 0) {
			salida.P_OUT_ERRORESCONTADOR = (long) out.p_out_ErroresContador;
			for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : out.p_out_ErroresDetalle) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
			}
		}
	}

	private void agregarLog(Connection conn, EntradaAutorizacion entrada, List<DatosSolicitud> datosSolicitud,
			String idEstadoAutorizacion) throws Exception {

		String fechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
		AgregarLog.InAgregarLog logIn = new AgregarLog.InAgregarLog();

		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "EDICION";
		log.IdModulo = "SOC";
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaHoraSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdTipoRegistro = TIPO_ENTIDAD_ESTADO;
		log.IdRegistro = datosSolicitud.get(0).IdEntidadOrigen;
		log.EstadoProceso = idEstadoAutorizacion;
		log.NombreProceso = "Confirmar Cambio de Estado";
		logIn.p_Log.add(log);

		for (DatosSolicitud dato : datosSolicitud) {
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = 1L;
			detalle.IdTrabajo = 0L;
			detalle.SubRegistro = dato.IdTipoEntidad;
			detalle.CampoRegistro = dato.CampoModificado;
			detalle.ValorAntiguo = dato.ValorAntiguo != null ? dato.ValorAntiguo : "";
			detalle.ValorNuevo = dato.ValorNuevo != null ? dato.ValorNuevo : "";
			logIn.p_LogDetalle.add(detalle);
		}

		AgregarLog servicio = new AgregarLog();
		servicio.logica(conn, logIn);
	}

	private SalidaAutorizacion agregarErrorSolicitudNoExiste(Connection conn, EntradaAutorizacion entrada,
			SalidaAutorizacion salida) throws Exception {
		salida.P_OUT_ERRORESCONTADOR = 1L;
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
			"SOLICITUD_NO_EXISTE", Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "SOLICITUD_NO_EXISTE").msg()));
		return salida;
	}

	private String obtenerMensajeDiccionario(Connection conn, Long idEmpresa, String codigo, String fallback) {
		try {
			return Diccionario.getMensaje(conn, idEmpresa, codigo).msg();
		} catch (Exception ex) {
			logger.warn("No se pudo resolver mensaje diccionario {}: {}", codigo, ex.getMessage());
			return fallback;
		}
	}

	private void agregarResultados(SalidaAutorizacion salida, List<AutorizadoEvento> autorizados) {
		for (AutorizadoEvento autorizado : autorizados) {
			ResultadoAutorizacion resultado = new ResultadoAutorizacion();
			resultado.evento = autorizado;
			resultado.resultado = EXITO_AUTORIZACION;
			salida.P_OUT_RESULTADO.add(resultado);
		}
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CEATOR\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionModificacionEstadoAutorizadoOrdenMasivo servicio =
					new AutorizacionModificacionEstadoAutorizadoOrdenMasivo();

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