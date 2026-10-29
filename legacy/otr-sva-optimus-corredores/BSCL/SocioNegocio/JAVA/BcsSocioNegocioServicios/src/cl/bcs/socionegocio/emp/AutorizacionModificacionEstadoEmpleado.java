package cl.bcs.socionegocio.emp;

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

/**
 * Confirmar/Rechazar cambio de estado de Empleado (concepto CAEEMP).
 * Migrado de: EMP.ConfirmarModificarEstadoEmpleado (autorizarEvento)
 *             EMP.RechazaAutorizacionEmpleadoDatosGenerales, rama IdConcepto='CAEEMP' (rechazarEvento)
 */
public class AutorizacionModificacionEstadoEmpleado implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoEmpleado.class);

	private static final String ESTADO_AUTORIZADO = "AUT";
	private static final String ESTADO_RECHAZADO = "RZD";
	private static final String ESTADO_PENDIENTE = "PEN";
	private static final String TIPO_ENTIDAD_ESTADO = "EMPLEADO-ESTADO";

	private final SQL sql = new SQL();

	private final String SQL_COUNT_SOLICITUD_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleado_Count_SolicitudEstado.sql");
	private final String SQL_SELECT_DATOS_SOLICITUD_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleado_Select_DatosSolicitudEstado.sql");
	private final String SQL_UPDATE_EMPLEADO_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleado_Update_EmpleadoEstado.sql");
	private final String SQL_SELECT_DATOS_DOCUMENTO_EMPLEADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEstadoEmpleado_Select_DatosDocumentoEmpleado.sql");

	public static class EmpleadoEvento {
		public Long IdEmpleado;
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
		public Long IdEmpleado;
	}

	public static class UpdateEmpleadoEstado {
		public Long IdEmpleado;
	}

	public static class DatosDocumentoEmpleado {
		public Long IdEmpleado;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoEmpleado.autorizarEvento");
		SalidaAutorizacion salida = confirmarCambioEstado(conn, entrada, ESTADO_AUTORIZADO);
		logger.info("FIN::AutorizacionModificacionEstadoEmpleado.autorizarEvento");
		return salida;
	}

	/**
	 * Lógica de confirmación reutilizable: aplica los cambios de estado al Empleado,
	 * actualiza la SolicitudCambio al estado indicado y registra la auditoría.
	 * La usa autorizarEvento() con "AUT", y ModificarEstadoEmpleado con "ING" (ingreso directo sin autorización).
	 */
	public SalidaAutorizacion confirmarCambioEstado(Connection conn, EntradaAutorizacion entrada, String idEstadoAutorizacion) {
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<EmpleadoEvento> empleadosEvento = obtenerEmpleadosEvento(entrada);
			List<Long> idsEmpleado = empleadosEvento.stream().map(e -> e.IdEmpleado).distinct().collect(Collectors.toList());

			if (!existenSolicitudesPendientes(conn, idsEmpleado)) {
				return agregarErrorSolicitudNoExiste(conn, entrada, salida);
			}

			List<DatosSolicitud> datosSolicitud = obtenerDatosSolicitud(conn, idsEmpleado);
			if (datosSolicitud.isEmpty()) {
				return agregarErrorSolicitudNoExiste(conn, entrada, salida);
			}

			aplicarCambiosEstadoEmpleado(conn, datosSolicitud);

			modificarEstadoSolicitud(conn, entrada, idsEmpleado, idEstadoAutorizacion, salida);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			agregarLog(conn, entrada, datosSolicitud, idEstadoAutorizacion);
			agregarResultados(salida, empleadosEvento);
		} catch (Exception e) {
			logger.error("Error en confirmarCambioEstado: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_AUTORIZACION", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", descripcion));
		}

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEstadoEmpleado.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<EmpleadoEvento> empleadosEvento = obtenerEmpleadosEvento(entrada);
			List<Long> idsEmpleado = empleadosEvento.stream().map(e -> e.IdEmpleado).distinct().collect(Collectors.toList());

			if (!existenSolicitudesPendientes(conn, idsEmpleado)) {
				return agregarErrorSolicitudNoExiste(conn, entrada, salida);
			}

			modificarEstadoSolicitud(conn, entrada, idsEmpleado, ESTADO_RECHAZADO, salida);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			agregarResultados(salida, empleadosEvento);
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_RECHAZO", e.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RECHAZO", descripcion));
		}

		logger.info("FIN::AutorizacionModificacionEstadoEmpleado.rechazarEvento");
		return salida;
	}

	private List<EmpleadoEvento> obtenerEmpleadosEvento(EntradaAutorizacion entrada) {
		Map<Long, EmpleadoEvento> mapa = new LinkedHashMap<>();
		for (Evento evento : entrada.p_Lista) {
			EmpleadoEvento e = new EmpleadoEvento();
			e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
			e.IdConcepto = evento.IdTipoEvento;
			e.IdEvento = evento.IdEvento;
			mapa.put(e.IdEmpleado, e);
		}
		return new ArrayList<>(mapa.values());
	}

	private boolean existenSolicitudesPendientes(Connection conn, List<Long> idsEmpleado) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdsEmpleado", idsEmpleado);
		params.put("IdTipoEntidad", TIPO_ENTIDAD_ESTADO);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);

		List<Contador> resultado = sql.query(conn, SQL_COUNT_SOLICITUD_ESTADO, params, Contador.class);
		Long contador = (resultado != null && !resultado.isEmpty() && resultado.get(0).Contador != null)
			? resultado.get(0).Contador : 0L;
		return contador > 0;
	}

	private List<DatosSolicitud> obtenerDatosSolicitud(Connection conn, List<Long> idsEmpleado) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdsEmpleado", idsEmpleado);
		params.put("IdTipoEntidad", TIPO_ENTIDAD_ESTADO);
		params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);
		return sql.query(conn, SQL_SELECT_DATOS_SOLICITUD_ESTADO, params, DatosSolicitud.class);
	}

	/**
	 * Un solo UPDATE con los 7 campos de estado; cada campo se auto-resuelve
	 * (COALESCE/EXISTS) según si existe o no una SolicitudCambio pendiente para ese campo.
	 */
	private void aplicarCambiosEstadoEmpleado(Connection conn, List<DatosSolicitud> datosSolicitud) throws Exception {
		List<Long> idsEmpleado = datosSolicitud.stream().map(d -> d.IdEmpleado).distinct().collect(Collectors.toList());
		List<UpdateEmpleadoEstado> updates = idsEmpleado.stream().map(id -> {
			UpdateEmpleadoEstado u = new UpdateEmpleadoEstado();
			u.IdEmpleado = id;
			return u;
		}).collect(Collectors.toList());
		sql.batch(conn, SQL_UPDATE_EMPLEADO_ESTADO, updates, UpdateEmpleadoEstado.class);
	}

	private void modificarEstadoSolicitud(Connection conn, EntradaAutorizacion entrada, List<Long> idsEmpleado,
			String idEstadoAutorizacion, SalidaAutorizacion salida) throws Exception {

		ModificarEstadoRegistroSolicitudCambioMasivo servicio = new ModificarEstadoRegistroSolicitudCambioMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado in =
			new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();

		in.p_IdEmpresa = entrada.p_IdEmpresa;
		in.p_IdUsuario = entrada.p_IdUsuario;
		in.p_IdModulo = entrada.p_IdModulo;
		in.p_IdEstadoAutorizacion = idEstadoAutorizacion;

		for (Long idEmpleado : idsEmpleado) {
			ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen entidad =
				new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
			entidad.IdEntidadOrigen = String.valueOf(idEmpleado);
			entidad.IdTipoEntidad = TIPO_ENTIDAD_ESTADO;
			in.p_EntidadOrigen.add(entidad);
		}

		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado out = servicio.logica(conn, in);
		if (out.p_out_AdvertenciasContador > 0 && out.p_out_AdvertenciasDetalle != null) {
			for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : out.p_out_AdvertenciasDetalle) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, adv.Codigo, adv.Descripcion));
			}
		}
		if (out.p_out_ErroresContador > 0) {
			salida.P_OUT_ERRORESCONTADOR = out.p_out_ErroresContador;
			for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : out.p_out_ErroresDetalle) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
			}
		}
	}

	private void agregarLog(Connection conn, EntradaAutorizacion entrada, List<DatosSolicitud> datosSolicitud,
			String idEstadoAutorizacion) throws Exception {

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());

		List<Long> idsEmpleado = datosSolicitud.stream().map(d -> d.IdEmpleado).distinct().collect(Collectors.toList());
		Map<Long, DatosDocumentoEmpleado> documentosPorEmpleado = obtenerDocumentosEmpleado(conn, idsEmpleado);

		Map<Long, List<DatosSolicitud>> porEmpleado = datosSolicitud.stream()
			.collect(Collectors.groupingBy(d -> d.IdEmpleado, LinkedHashMap::new, Collectors.toList()));

		AgregarLog.InAgregarLog inLog = new AgregarLog.InAgregarLog();

		long idCabecera = 1L;
		for (Map.Entry<Long, List<DatosSolicitud>> item : porEmpleado.entrySet()) {
			Long idEmpleado = item.getKey();
			DatosDocumentoEmpleado documento = documentosPorEmpleado.get(idEmpleado);

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = idCabecera;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "SOC";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdTipoRegistro = TIPO_ENTIDAD_ESTADO;
			log.IdRegistro = String.valueOf(idEmpleado);
			log.EstadoProceso = idEstadoAutorizacion;
			log.EstadoRegistro = idEstadoAutorizacion;
			log.NombreProceso = "Confirmar Cambio de Estado";
			if (documento != null) {
				log.IdTipoDocumentoIdentidad = documento.TipoDocumentoIdentidad != null ? documento.TipoDocumentoIdentidad : "";
				log.IdDocumentoIdentidad = documento.DocumentoIdentidad != null ? documento.DocumentoIdentidad : "";
			}
			inLog.p_Log.add(log);

			for (DatosSolicitud dato : item.getValue()) {
				AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
				detalle.IdCabeceraLog = idCabecera;
				detalle.IdTrabajo = 0L;
				detalle.SubRegistro = dato.IdTipoEntidad;
				detalle.CampoRegistro = dato.CampoModificado;
				detalle.ValorAntiguo = dato.ValorAntiguo != null ? dato.ValorAntiguo : "";
				detalle.ValorNuevo = dato.ValorNuevo != null ? dato.ValorNuevo : "";
				inLog.p_LogDetalle.add(detalle);
			}

			idCabecera++;
		}

		AgregarLog servicio = new AgregarLog();
		servicio.logica(conn, inLog);
	}

	private Map<Long, DatosDocumentoEmpleado> obtenerDocumentosEmpleado(Connection conn, List<Long> idsEmpleado) throws Exception {
		Map<Long, DatosDocumentoEmpleado> mapa = new LinkedHashMap<>();
		if (idsEmpleado.isEmpty()) {
			return mapa;
		}
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdsEmpleado", idsEmpleado);
		List<DatosDocumentoEmpleado> documentos = sql.query(conn, SQL_SELECT_DATOS_DOCUMENTO_EMPLEADO, params, DatosDocumentoEmpleado.class);
		for (DatosDocumentoEmpleado documento : documentos) {
			mapa.put(documento.IdEmpleado, documento);
		}
		return mapa;
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

	private void agregarResultados(SalidaAutorizacion salida, List<EmpleadoEvento> empleados) {
		for (EmpleadoEvento empleado : empleados) {
			ResultadoAutorizacion resultado = new ResultadoAutorizacion();
			resultado.evento = empleado;
			resultado.resultado = EXITO_AUTORIZACION;
			salida.P_OUT_RESULTADO.add(resultado);
		}
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CAEEMP\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizacionModificacionEstadoEmpleado servicio = new AutorizacionModificacionEstadoEmpleado();

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
