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
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionEliminacionPersonaDireccionElectronicaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionEliminacionPersonaDireccionElectronicaMasivo.class);

	private final String SQL_UPDATE_DIRECCION_BORRADA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionEliminacionPersonaDireccionElectronicaMasivo_Update_DireccionBorrada.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionEliminacionPersonaDireccionElectronicaMasivo_Update_EstadoSolicitud.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA_LOG = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionElectronica_SelectDireccionElectronicaLog.sql");
	
	public static class DireccionElectronicaEliminacion {
		public Long IdDireccionElectronica;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // BORDIE (borrar dirección electrónica)
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarEliminarPersonaDireccionElectronica
	 * Maneja BORDIE (borrar dirección electrónica) - marca campo Borrado = 'S'
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones electrónicas desde entrada
			List<DireccionElectronicaEliminacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronicaEliminacion d = new DireccionElectronicaEliminacion();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					d.IdConcepto = evento.IdTipoEvento;  // BORDIE
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// 1. Marcar DireccionElectronica como Borrado = 'S'
			new SQL().batch(conn, SQL_UPDATE_DIRECCION_BORRADA, direcciones, DireccionElectronicaEliminacion.class);
			
			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, direcciones, DireccionElectronicaEliminacion.class);
			
			// Agregar resultado por cada dirección procesada
			for (DireccionElectronicaEliminacion direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

			// Registrar auditoria (paridad con ConfirmarEliminarPersonaDireccionElectronica.sql)
			// Aislado en su propio try/catch: un fallo de auditoria no debe reportar como fallida
			// una aprobacion que ya marco la direccion como borrada y cerro la solicitud correctamente.
			try {
				registrarLog(conn, entrada, direcciones);
			} catch (Exception exLog) {
				logger.error("Error registrando auditoria en AutorizacionEliminacionPersonaDireccionElectronicaMasivo: " + exLog.getMessage(), exLog);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.autorizarEvento");
		return salida;
	}

	private void registrarLog(Connection conn, EntradaAutorizacion entrada, List<DireccionElectronicaEliminacion> direcciones) throws Exception {
		logger.debug("INICIO::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.registrarLog");

		List<Long> idsDireccionElectronica = direcciones.stream().map(d -> d.IdDireccionElectronica).distinct().collect(Collectors.toList());
		if (idsDireccionElectronica.isEmpty()) {
			return;
		}

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdDireccionElectronica", idsDireccionElectronica);
		List<ConfirmarEliminarPersonaDireccionElectronica.DireccionElectronicaLog> datosDireccion = sql.query(conn, SQL_SELECT_DIRECCION_ELECTRONICA_LOG, params, ConfirmarEliminarPersonaDireccionElectronica.DireccionElectronicaLog.class);
		if (datosDireccion.isEmpty()) {
			logger.debug("FIN::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.registrarLog - sin datos de direccion/persona");
			return;
		}

		Long idEmpresa = entrada.p_Lista.isEmpty() ? null : entrada.p_Lista.get(0).IdEmpresa;
		Long idUsuario = entrada.p_Lista.isEmpty() ? null : entrada.p_Lista.get(0).IdUsuario;
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), idEmpresa);
		InAgregarLog entradaLog = new InAgregarLog();

		for (ConfirmarEliminarPersonaDireccionElectronica.DireccionElectronicaLog datos : datosDireccion) {
			InAgregarLog.Log log = new InAgregarLog.Log();
			// IdCabeceraLog = IdDireccionElectronica (no IdPersona): AgregarLog deduplica cabeceras por
			// (IdCabeceraLog, IdTipoLog, IdTipoRegistro), y si dos direcciones del mismo cliente se
			// autorizan en el mismo lote, usar IdPersona colapsaria la segunda cabecera perdiendo su IdRegistro.
			log.IdCabeceraLog = datos.IdDireccionElectronica;
			log.IdTipoLog = "ELIMINACION";
			log.IdModulo = "SOC";
			log.IdTipoRegistro = "DIRECCIONELECTRONICA-BORRAR";
			log.IdUsuario = idUsuario;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = idUsuario;
			log.IdPersona = datos.IdPersona;
			log.IdRegistro = String.valueOf(datos.IdDireccionElectronica);
			log.NombreProceso = "Confirmar Solicitud de Cambio";
			log.EstadoProceso = "AUT";
			entradaLog.p_Log.add(log);

			entradaLog.agregarDetalleLogBasico(datos.IdDireccionElectronica, 0L, "Borrado", datos.Direccion, "", "DIRECCIONELECTRONICA-BORRAR");
		}

		new AgregarLog().logica(conn, entradaLog);

		logger.debug("FIN::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.registrarLog");
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaEliminacionPersonaDireccionElectronica
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica DireccionElectronica)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones electrónicas desde entrada
			List<DireccionElectronicaEliminacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronicaEliminacion d = new DireccionElectronicaEliminacion();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					d.IdConcepto = evento.IdTipoEvento;  // BORDIE
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, direcciones, DireccionElectronicaEliminacion.class);
			
			// Agregar resultado por cada dirección procesada
			for (DireccionElectronicaEliminacion direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionEliminacionPersonaDireccionElectronicaMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"BORDIE\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionEliminacionPersonaDireccionElectronicaMasivo servicio = new AutorizacionEliminacionPersonaDireccionElectronicaMasivo();
				
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
