package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionPersonaTarjetaBancariaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaTarjetaBancariaMasivo.class);

	private final String SQL_UPDATE_TARJETA_AUTORIZADA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaTarjetaBancariaMasivo_Update_TarjetaAutorizada.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaTarjetaBancariaMasivo_Update_EstadoSolicitud.sql");
	private final String SQL_SELECT_ID_PERSONA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_SelectIdPersona.sql");
	private final String SQL_SELECT_DETALLE_LOG = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_SelectDetalleLog.sql");
	private final String SQL_SELECT_SOLICITUDES_PENDIENTES = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaTarjetaBancariaMasivo_SelectSolicitudesPendientes.sql");
	
	public static class TarjetaBancariaAutorizacion {
		public Long IdTarjetaBancaria;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODTJB o CAETJB
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarModificarPersonaTarjetaBancariaMasivo
	 * Maneja MODTJB (modificación tarjeta bancaria) y CAETJB (cambio estado)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaTarjetaBancariaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de tarjetas bancarias desde entrada
			List<TarjetaBancariaAutorizacion> tarjetas = entrada.p_Lista.stream()
				.map(evento -> {
					TarjetaBancariaAutorizacion t = new TarjetaBancariaAutorizacion();
					t.IdTarjetaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
					t.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					t.IdConcepto = evento.IdTipoEvento;  // MODTJB o CAETJB
					return t;
				})
				.distinct()
				.collect(Collectors.toList());

			// Se capturan las solicitudes pendientes (con su IdSolicitudCambio) ANTES de aplicar los UPDATE que
			// cierran el estado de la solicitud: una vez cerradas, un filtro por IdEstadoAutorizacion='PEN' ya
			// no encontraria estas filas y el log de auditoria quedaria siempre vacio.
			List<String> idsEntidadOrigen = tarjetas.stream().map(t -> String.valueOf(t.IdTarjetaBancaria)).distinct().collect(Collectors.toList());
			HashMap<String, Object> paramsPendientes = new HashMap<>();
			paramsPendientes.put("p_IdEntidadOrigen", idsEntidadOrigen);
			List<ConfirmarModificarPersonaTarjetaBancaria.SolicitudCambio> solicitudesPendientes = new SQL().query(conn, SQL_SELECT_SOLICITUDES_PENDIENTES, paramsPendientes, ConfirmarModificarPersonaTarjetaBancaria.SolicitudCambio.class);

			// 1. Actualizar TarjetaBancaria con valores desde SolicitudCambio (6 campos)
			new SQL().batch(conn, SQL_UPDATE_TARJETA_AUTORIZADA, tarjetas, TarjetaBancariaAutorizacion.class);

			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, tarjetas, TarjetaBancariaAutorizacion.class);

			// Agregar resultado por cada tarjeta procesada
			for (TarjetaBancariaAutorizacion tarjeta : tarjetas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = tarjeta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

			// Registrar auditoria (paridad con ConfirmarModificarPersonaTarjetaBancaria.sql)
			// Aislado en su propio try/catch: un fallo de auditoria no debe reportar como fallida
			// una autorizacion que ya actualizo la tarjeta y cerro la solicitud correctamente.
			try {
				registrarLog(conn, entrada, tarjetas, solicitudesPendientes);
			} catch (Exception exLog) {
				logger.error("Error registrando auditoria en AutorizacionModificacionPersonaTarjetaBancariaMasivo: " + exLog.getMessage(), exLog);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionModificacionPersonaTarjetaBancariaMasivo.autorizarEvento");
		return salida;
	}

	private String tipoEntidadPorConcepto(String idConcepto){
		return "CAETJB".equals(idConcepto) ? "TARJETABANCARIA-ESTADO" : "TARJETABANCARIA";
	}

	private void registrarLog(Connection conn, EntradaAutorizacion entrada, List<TarjetaBancariaAutorizacion> tarjetas, List<ConfirmarModificarPersonaTarjetaBancaria.SolicitudCambio> solicitudesPendientes) throws Exception {
		logger.debug("INICIO::AutorizacionModificacionPersonaTarjetaBancariaMasivo.registrarLog");

		List<Long> idsTarjetaBancaria = tarjetas.stream().map(t -> t.IdTarjetaBancaria).distinct().collect(Collectors.toList());
		if (idsTarjetaBancaria.isEmpty()) {
			return;
		}

		SQL sql = new SQL();
		HashMap<String, Object> paramsPersona = new HashMap<>();
		paramsPersona.put("IdTarjetaBancaria", idsTarjetaBancaria);
		List<ConfirmarModificarPersonaTarjetaBancaria.TarjetaBancariaPersona> datosTarjetas = sql.query(conn, SQL_SELECT_ID_PERSONA, paramsPersona, ConfirmarModificarPersonaTarjetaBancaria.TarjetaBancariaPersona.class);
		if (datosTarjetas.isEmpty()) {
			logger.debug("FIN::AutorizacionModificacionPersonaTarjetaBancariaMasivo.registrarLog - sin datos de tarjeta/persona");
			return;
		}
		Map<Long, ConfirmarModificarPersonaTarjetaBancaria.TarjetaBancariaPersona> datosPorId = datosTarjetas.stream()
				.collect(Collectors.toMap(d -> d.IdTarjetaBancaria, d -> d));

		Map<String, List<Long>> idsSolicitudPorEntidad = solicitudesPendientes.stream()
				.collect(Collectors.groupingBy(s -> s.IdEntidadOrigen, Collectors.mapping(s -> s.IdSolicitudCambio, Collectors.toList())));

		Long idEmpresa = entrada.p_Lista.isEmpty() ? null : entrada.p_Lista.get(0).IdEmpresa;
		Long idUsuario = entrada.p_Lista.isEmpty() ? null : entrada.p_Lista.get(0).IdUsuario;
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), idEmpresa);
		InAgregarLog entradaLog = new InAgregarLog();

		for (TarjetaBancariaAutorizacion tarjeta : tarjetas) {
			ConfirmarModificarPersonaTarjetaBancaria.TarjetaBancariaPersona datos = datosPorId.get(tarjeta.IdTarjetaBancaria);
			if (datos == null) {
				continue;
			}

			String tipoEntidad = tipoEntidadPorConcepto(tarjeta.IdConcepto);
			String idEntidadOrigen = String.valueOf(tarjeta.IdTarjetaBancaria);
			List<Long> idsSolicitud = idsSolicitudPorEntidad.get(idEntidadOrigen);
			if (idsSolicitud == null || idsSolicitud.isEmpty()) {
				continue;
			}
			HashMap<String, Object> paramsDetalle = new HashMap<>();
			paramsDetalle.put("p_IdSolicitudCambio", idsSolicitud);
			List<ConfirmarModificarPersonaTarjetaBancaria.DetalleLog> detalles = sql.query(conn, SQL_SELECT_DETALLE_LOG, paramsDetalle, ConfirmarModificarPersonaTarjetaBancaria.DetalleLog.class);
			if (detalles.isEmpty()) {
				continue;
			}

			InAgregarLog.Log log = new InAgregarLog.Log();
			// IdCabeceraLog = IdTarjetaBancaria (no IdPersona): AgregarLog deduplica cabeceras por
			// (IdCabeceraLog, IdTipoLog, IdTipoRegistro), y si dos tarjetas del mismo cliente se
			// autorizan en el mismo lote, usar IdPersona colapsaria la segunda cabecera perdiendo su IdRegistro.
			log.IdCabeceraLog = tarjeta.IdTarjetaBancaria;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "SOC";
			log.IdTipoRegistro = tipoEntidad;
			log.IdUsuario = idUsuario;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = idUsuario;
			log.IdPersona = datos.IdPersona;
			log.IdRegistro = idEntidadOrigen;
			log.NombreProceso = "Confirmar Solicitud de Cambio";
			log.EstadoProceso = "AUT";
			entradaLog.p_Log.add(log);

			for (ConfirmarModificarPersonaTarjetaBancaria.DetalleLog detalle : detalles) {
				entradaLog.agregarDetalleLogBasico(tarjeta.IdTarjetaBancaria, 0L, detalle.CampoRegistro, detalle.ValorAntiguo, detalle.ValorNuevo, detalle.SubRegistro);
			}
		}

		if (entradaLog.p_Log.isEmpty()) {
			logger.debug("FIN::AutorizacionModificacionPersonaTarjetaBancariaMasivo.registrarLog - sin detalle de cambios");
			return;
		}

		new AgregarLog().logica(conn, entradaLog);

		logger.debug("FIN::AutorizacionModificacionPersonaTarjetaBancariaMasivo.registrarLog");
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionPersonaTarjetaBancaria
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica TarjetaBancaria)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaTarjetaBancariaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de tarjetas bancarias desde entrada
			List<TarjetaBancariaAutorizacion> tarjetas = entrada.p_Lista.stream()
				.map(evento -> {
					TarjetaBancariaAutorizacion t = new TarjetaBancariaAutorizacion();
					t.IdTarjetaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
					t.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					t.IdConcepto = evento.IdTipoEvento;  // MODTJB o CAETJB
					return t;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, tarjetas, TarjetaBancariaAutorizacion.class);
			
			// Agregar resultado por cada tarjeta procesada
			for (TarjetaBancariaAutorizacion tarjeta : tarjetas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = tarjeta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaTarjetaBancariaMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODTJB\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionPersonaTarjetaBancariaMasivo servicio = new AutorizacionModificacionPersonaTarjetaBancariaMasivo();
				
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
