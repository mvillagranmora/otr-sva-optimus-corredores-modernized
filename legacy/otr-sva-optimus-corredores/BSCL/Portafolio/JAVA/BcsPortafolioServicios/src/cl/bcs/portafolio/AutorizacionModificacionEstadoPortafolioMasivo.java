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

import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionEstadoPortafolioMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoPortafolioMasivo.class);

	private static final String ESTADO_AUTORIZADO = "AUT";
	private static final String ESTADO_RECHAZADO = "RZD";
	private static final String ESTADO_PENDIENTE = "PEN";

	private static final String TIPO_ENTIDAD_PORTAFOLIO_ESTADO = "PORTAFOLIO-ESTADO";
	private static final String TIPO_ENTIDAD_PORTAFOLIO = "PORTAFOLIO";
	private static final String CONCEPTO_MODIFICAR_PORTAFOLIO = "MODPOR";
	private static final String CONCEPTO_CAMBIO_ESTADO_PORTAFOLIO = "CAEPOR";

	private final SQL sql = new SQL();

	private final String SQL_COUNT_SOLICITUD_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Count_SolicitudEstado.sql");
	private final String SQL_SELECT_DATOS_SOLICITUD_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Select_DatosSolicitudEstado.sql");
	private final String SQL_UPDATE_PORTAFOLIO_ID_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioIdEstado.sql");
	private final String SQL_UPDATE_PORTAFOLIO_ID_TIPO_BLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioIdTipoBloqueo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_FECHA_BLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioFechaBloqueo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_COMENTARIO_BLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioComentarioBloqueo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_ID_TIPO_DESBLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioIdTipoDesbloqueo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_FECHA_DESBLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioFechaDesbloqueo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_COMENTARIO_DESBLOQUEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioComentarioDesbloqueo.sql");
	private final String SQL_COUNT_PORTAFOLIO_APV = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Count_PortafolioAPV.sql");

	private final String SQL_COUNT_PORTAFOLIO_PENDIENTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Count_PortafolioPendiente.sql");
	private final String SQL_SELECT_DATOS_SOLICITUD_RECHAZO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Select_DatosSolicitudRechazo.sql");
	private final String SQL_UPDATE_PORTAFOLIO_RECHAZO = PlataformaUtils.loadResourceToString(
		"cl/bcs/portafolio/sql/AutorizacionModificacionEstadoPortafolioMasivo_Update_PortafolioRechazo.sql");

	public static class PortafolioEvento {
		public Long IdPortafolio;
		public String IdConcepto;
		public Long IdEvento;
	}

	public static class DatosSolicitud {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
		public Long IdPortafolio;
	}

	public static class Contador {
		public Long Contador;
	}

	public static class PortafolioId {
		public Long IdPortafolio;
	}

	public static class UpdateIdEstado {
		public Long IdPortafolio;
		public String ValorNuevo;
	}

	public static class UpdateIdTipoBloqueo {
		public Long IdPortafolio;
		public String ValorNuevo;
	}

	public static class UpdateFechaBloqueo {
		public Long IdPortafolio;
	}

	public static class UpdateComentarioBloqueo {
		public Long IdPortafolio;
		public String ValorNuevo;
	}

	public static class UpdateIdTipoDesbloqueo {
		public Long IdPortafolio;
		public String ValorNuevo;
	}

	public static class UpdateFechaDesbloqueo {
		public Long IdPortafolio;
	}

	public static class UpdateComentarioDesbloqueo {
		public Long IdPortafolio;
		public String ValorNuevo;
	}

	public static class UpdatePortafolioRechazo {
		public Long IdPortafolio;
		public String IdEstadoAutorizacion;
		public String IdEstado;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoPortafolioMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<PortafolioEvento> portafoliosEvento = obtenerPortafoliosEvento(entrada);
			List<Long> idsPortafolio = portafoliosEvento.stream().map(p -> p.IdPortafolio).distinct().collect(Collectors.toList());

			if (!existenSolicitudesPendientesEstado(conn, idsPortafolio)) {
				return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
			}

			List<DatosSolicitud> datosSolicitud = obtenerDatosSolicitudEstado(conn, idsPortafolio);
			if (datosSolicitud.isEmpty()) {
				return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
			}

			aplicarCambiosEstadoPortafolio(conn, datosSolicitud);
			replicarPortafoliosAPV(conn, entrada, idsPortafolio, salida);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			modificarEstadoSolicitud(conn, entrada, datosSolicitud, ESTADO_AUTORIZADO, salida);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			agregarLog(conn, entrada, datosSolicitud, ESTADO_AUTORIZADO);
			agregarResultados(salida, portafoliosEvento);
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_AUTORIZACION", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", descripcion));
		}

		logger.info("FIN::AutorizacionModificacionEstadoPortafolioMasivo.autorizarEvento");
		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoPortafolioMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<PortafolioEvento> portafoliosEvento = obtenerPortafoliosEvento(entrada);
			List<Long> idsPortafolio = portafoliosEvento.stream().map(p -> p.IdPortafolio).distinct().collect(Collectors.toList());
			String idConcepto = portafoliosEvento.isEmpty() ? "" : portafoliosEvento.get(0).IdConcepto;

			if (!existenSolicitudesPendientesEstado(conn, idsPortafolio)) {
				return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "NO_EXISTE_PORTAFOLIO", "No existen portafolios pendientes");
			}

			if (esConceptoRechazoConSolicitud(idConcepto)) {
				List<DatosSolicitud> datosSolicitud = obtenerDatosSolicitudRechazo(conn, idsPortafolio);
				if (!datosSolicitud.isEmpty()) {
					modificarEstadoSolicitud(conn, entrada, datosSolicitud, ESTADO_RECHAZADO, salida);
					if (salida.P_OUT_ERRORESCONTADOR > 0) {
						return salida;
					}
				}
			}

			List<UpdatePortafolioRechazo> updates = idsPortafolio.stream().map(id -> {
				UpdatePortafolioRechazo u = new UpdatePortafolioRechazo();
				u.IdPortafolio = id;
				u.IdEstadoAutorizacion = ESTADO_RECHAZADO;
				u.IdEstado = "INA";
				return u;
			}).collect(Collectors.toList());

			if (!updates.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_PORTAFOLIO_RECHAZO, updates, UpdatePortafolioRechazo.class);
			}

			agregarResultados(salida, portafoliosEvento);
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_RECHAZO", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RECHAZO", descripcion));
		}

		logger.info("FIN::AutorizacionModificacionEstadoPortafolioMasivo.rechazarEvento");
		return salida;
	}

	private List<PortafolioEvento> obtenerPortafoliosEvento(EntradaAutorizacion entrada) {
		Map<String, PortafolioEvento> mapa = new LinkedHashMap<>();
		for (Evento evento : entrada.p_Lista) {
			PortafolioEvento p = new PortafolioEvento();
			p.IdPortafolio = Long.valueOf(evento.NumeroOperacionConcepto);
			p.IdConcepto = evento.IdTipoEvento;
			p.IdEvento = evento.IdEvento;
			mapa.put(p.IdPortafolio + "_" + p.IdConcepto, p);
		}
		return new ArrayList<>(mapa.values());
	}

	private boolean existenSolicitudesPendientesEstado(Connection conn, List<Long> idsPortafolio) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPortafolios", idsPortafolio);
		params.put("IdTipoEntidad", TIPO_ENTIDAD_PORTAFOLIO_ESTADO);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		Long contador = obtenerContador(conn, SQL_COUNT_SOLICITUD_ESTADO, params);
		return contador > 0;
	}

	private List<DatosSolicitud> obtenerDatosSolicitudEstado(Connection conn, List<Long> idsPortafolio) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPortafolios", idsPortafolio);
		params.put("IdTipoEntidad", TIPO_ENTIDAD_PORTAFOLIO_ESTADO);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		return sql.query(conn, SQL_SELECT_DATOS_SOLICITUD_ESTADO, params, DatosSolicitud.class);
	}

	private void aplicarCambiosEstadoPortafolio(Connection conn, List<DatosSolicitud> datosSolicitud) throws Exception {
		Map<String, Map<Long, DatosSolicitud>> agrupadoPorCampo = new HashMap<>();
		for (DatosSolicitud dato : datosSolicitud) {
			Map<Long, DatosSolicitud> porId = agrupadoPorCampo.computeIfAbsent(dato.CampoModificado, k -> new LinkedHashMap<>());
			porId.put(dato.IdPortafolio, dato);
		}

		ejecutarUpdateIdEstado(conn, agrupadoPorCampo.get("IdEstado"));
		ejecutarUpdateIdTipoBloqueo(conn, agrupadoPorCampo.get("IdTipoBloqueo"));
		ejecutarUpdateFechaBloqueo(conn, agrupadoPorCampo.get("FechaBloqueo"));
		ejecutarUpdateComentarioBloqueo(conn, agrupadoPorCampo.get("ComentarioBloqueo"));
		ejecutarUpdateIdTipoDesbloqueo(conn, agrupadoPorCampo.get("IdTipoDesbloqueo"));
		ejecutarUpdateFechaDesbloqueo(conn, agrupadoPorCampo.get("FechaDesbloqueo"));
		ejecutarUpdateComentarioDesbloqueo(conn, agrupadoPorCampo.get("ComentarioDesbloqueo"));
	}

	private void ejecutarUpdateIdEstado(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateIdEstado> updates = datos.values().stream().map(d -> {
			UpdateIdEstado u = new UpdateIdEstado();
			u.IdPortafolio = d.IdPortafolio;
			u.ValorNuevo = d.ValorNuevo;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_ID_ESTADO, updates, UpdateIdEstado.class);
	}

	private void ejecutarUpdateIdTipoBloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateIdTipoBloqueo> updates = datos.values().stream().map(d -> {
			UpdateIdTipoBloqueo u = new UpdateIdTipoBloqueo();
			u.IdPortafolio = d.IdPortafolio;
			u.ValorNuevo = d.ValorNuevo;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_ID_TIPO_BLOQUEO, updates, UpdateIdTipoBloqueo.class);
	}

	private void ejecutarUpdateFechaBloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateFechaBloqueo> updates = datos.values().stream().map(d -> {
			UpdateFechaBloqueo u = new UpdateFechaBloqueo();
			u.IdPortafolio = d.IdPortafolio;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_FECHA_BLOQUEO, updates, UpdateFechaBloqueo.class);
	}

	private void ejecutarUpdateComentarioBloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateComentarioBloqueo> updates = datos.values().stream().map(d -> {
			UpdateComentarioBloqueo u = new UpdateComentarioBloqueo();
			u.IdPortafolio = d.IdPortafolio;
			u.ValorNuevo = d.ValorNuevo;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_COMENTARIO_BLOQUEO, updates, UpdateComentarioBloqueo.class);
	}

	private void ejecutarUpdateIdTipoDesbloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateIdTipoDesbloqueo> updates = datos.values().stream().map(d -> {
			UpdateIdTipoDesbloqueo u = new UpdateIdTipoDesbloqueo();
			u.IdPortafolio = d.IdPortafolio;
			u.ValorNuevo = d.ValorNuevo;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_ID_TIPO_DESBLOQUEO, updates, UpdateIdTipoDesbloqueo.class);
	}

	private void ejecutarUpdateFechaDesbloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateFechaDesbloqueo> updates = datos.values().stream().map(d -> {
			UpdateFechaDesbloqueo u = new UpdateFechaDesbloqueo();
			u.IdPortafolio = d.IdPortafolio;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_FECHA_DESBLOQUEO, updates, UpdateFechaDesbloqueo.class);
	}

	private void ejecutarUpdateComentarioDesbloqueo(Connection conn, Map<Long, DatosSolicitud> datos) throws Exception {
		if (datos == null || datos.isEmpty()) {
			return;
		}
		List<UpdateComentarioDesbloqueo> updates = datos.values().stream().map(d -> {
			UpdateComentarioDesbloqueo u = new UpdateComentarioDesbloqueo();
			u.IdPortafolio = d.IdPortafolio;
			u.ValorNuevo = d.ValorNuevo;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_PORTAFOLIO_COMENTARIO_DESBLOQUEO, updates, UpdateComentarioDesbloqueo.class);
	}

	private void replicarPortafoliosAPV(Connection conn, EntradaAutorizacion entrada, List<Long> idsPortafolio,
			SalidaAutorizacion salida) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPortafolios", idsPortafolio);
		List<PortafolioId> portafoliosAPV = sql.query(conn, SQL_COUNT_PORTAFOLIO_APV, params, PortafolioId.class);

		if (portafoliosAPV == null || portafoliosAPV.isEmpty()) {
			return;
		}

		ReplicaPortafolioAPV servicioReplica = new ReplicaPortafolioAPV();
		for (PortafolioId por : portafoliosAPV) {
			ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV in = new ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV();
			in.p_IdEmpresa = entrada.p_IdEmpresa;
			in.p_IdUsuario = entrada.p_IdUsuario;
			in.p_IdModulo = entrada.p_IdModulo;
			in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			in.p_Portafolio = por.IdPortafolio;
			in.p_IdEntidad = por.IdPortafolio;
			in.p_TipoEntidad = "PORTAFOLIO";
			in.p_TipoOperacion = "U";

			ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV out = servicioReplica.logica(conn, in);
			if (out.P_OUT_MENSAJES != null && !out.P_OUT_MENSAJES.isEmpty()) {
				salida.P_OUT_MENSAJES.addAll(out.P_OUT_MENSAJES);
			}
			if (out.P_OUT_ERRORESCONTADOR != null && out.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR += out.P_OUT_ERRORESCONTADOR;
				return;
			}
		}
	}

	private boolean existenPortafoliosPendientes(Connection conn, List<Long> idsPortafolio) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPortafolios", idsPortafolio);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		Long contador = obtenerContador(conn, SQL_COUNT_PORTAFOLIO_PENDIENTE, params);
		return contador > 0;
	}

	private List<DatosSolicitud> obtenerDatosSolicitudRechazo(Connection conn, List<Long> idsPortafolio) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPortafolios", idsPortafolio);
		params.put("IdTipoEntidad", TIPO_ENTIDAD_PORTAFOLIO_ESTADO);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		List<DatosSolicitud> datos = sql.query(conn, SQL_SELECT_DATOS_SOLICITUD_RECHAZO, params, DatosSolicitud.class);
		if (datos != null && !datos.isEmpty()) {
			return datos;
		}

		params.put("IdTipoEntidad", TIPO_ENTIDAD_PORTAFOLIO);
		return sql.query(conn, SQL_SELECT_DATOS_SOLICITUD_RECHAZO, params, DatosSolicitud.class);
	}

	private boolean esConceptoRechazoConSolicitud(String idConcepto) {
		return CONCEPTO_MODIFICAR_PORTAFOLIO.equals(idConcepto)
			|| CONCEPTO_CAMBIO_ESTADO_PORTAFOLIO.equals(idConcepto);
	}

	private Long obtenerContador(Connection conn, String consulta, HashMap<String, Object> params) throws Exception {
		List<Contador> list = sql.query(conn, consulta, params, Contador.class);
		return (list != null && !list.isEmpty() && list.get(0).Contador != null) ? list.get(0).Contador : 0L;
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

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
		AgregarLog.InAgregarLog inLog = new AgregarLog.InAgregarLog();

		Map<Long, List<DatosSolicitud>> porPortafolio = datosSolicitud.stream()
			.collect(Collectors.groupingBy(d -> d.IdPortafolio));

		long idCabecera = 1L;
		for (Map.Entry<Long, List<DatosSolicitud>> item : porPortafolio.entrySet()) {
			Long idPortafolio = item.getKey();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = idCabecera;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "POR";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdTipoRegistro = TIPO_ENTIDAD_PORTAFOLIO_ESTADO;
			log.IdRegistro = String.valueOf(idPortafolio);
			log.EstadoProceso = idEstadoAutorizacion;
			log.NombreProceso = "Confirmar Cambio de Estado";
			inLog.p_Log.add(log);

			for (DatosSolicitud dato : item.getValue()) {
				AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
				det.IdCabeceraLog = idCabecera;
				det.IdTrabajo = 0L;
				det.SubRegistro = dato.IdTipoEntidad;
				det.CampoRegistro = dato.CampoModificado;
				det.ValorAntiguo = dato.ValorAntiguo != null ? dato.ValorAntiguo : "";
				det.ValorNuevo = dato.ValorNuevo != null ? dato.ValorNuevo : "";
				inLog.p_LogDetalle.add(det);
			}

			idCabecera++;
		}

		AgregarLog servicio = new AgregarLog();
		servicio.logica(conn, inLog);
	}

	private SalidaAutorizacion agregarMensajeError(Connection conn, Long idEmpresa, SalidaAutorizacion salida,
			String codigo, String fallback) {
		salida.P_OUT_ERRORESCONTADOR = 1L;
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
			codigo, obtenerMensajeDiccionario(conn, idEmpresa, codigo, fallback)));
		return salida;
	}

	private String obtenerMensajeDiccionario(Connection conn, Long idEmpresa, String codigo, String fallback) {
		try {
			return Diccionario.getMensaje(conn, idEmpresa, codigo).msg();
		} catch (Exception e) {
			logger.warn("No se pudo resolver mensaje diccionario {}: {}", codigo, e.getMessage());
			return fallback;
		}
	}

	private void agregarResultados(SalidaAutorizacion salida, List<PortafolioEvento> portafolios) {
		for (PortafolioEvento p : portafolios) {
			ResultadoAutorizacion r = new ResultadoAutorizacion();
			r.evento = p;
			r.resultado = EXITO_AUTORIZACION;
			salida.P_OUT_RESULTADO.add(r);
		}
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":873143,\"NumeroOperacionConcepto\":\"40512\",\"IdTipoEvento\":\"CAEPOR\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionModificacionEstadoPortafolioMasivo servicio = new AutorizacionModificacionEstadoPortafolioMasivo();

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
