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

public class AutorizacionModificacionPersonaDireccionElectronicaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaDireccionElectronicaMasivo.class);

	private final String SQL_UPDATE_DIRECCION_AUTORIZADA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaDireccionElectronicaMasivo_Update_DireccionAutorizada.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaDireccionElectronicaMasivo_Update_EstadoSolicitud.sql");
	
	public static class DireccionElectronicaAutorizacion {
		public Long IdDireccionElectronica;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODDIE (modificación dirección electrónica)
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarModificarPersonaDireccionElectronicaMasivo
	 * Maneja MODDIE (modificación dirección electrónica)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaDireccionElectronicaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones electrónicas desde entrada
			List<DireccionElectronicaAutorizacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronicaAutorizacion d = new DireccionElectronicaAutorizacion();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					d.IdConcepto = evento.IdTipoEvento;  // MODDIE
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// 1. Actualizar DireccionElectronica con valores desde SolicitudCambio (4 campos)
			new SQL().batch(conn, SQL_UPDATE_DIRECCION_AUTORIZADA, direcciones, DireccionElectronicaAutorizacion.class);
			
			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, direcciones, DireccionElectronicaAutorizacion.class);
			
			// Agregar resultado por cada dirección procesada
			for (DireccionElectronicaAutorizacion direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaDireccionElectronicaMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionPersonaDireccionElectronicaMasivo
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica DireccionElectronica)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaDireccionElectronicaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones electrónicas desde entrada
			List<DireccionElectronicaAutorizacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronicaAutorizacion d = new DireccionElectronicaAutorizacion();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					d.IdConcepto = evento.IdTipoEvento;  // MODDIE
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, direcciones, DireccionElectronicaAutorizacion.class);
			
			// Agregar resultado por cada dirección procesada
			for (DireccionElectronicaAutorizacion direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaDireccionElectronicaMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODDIE\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionPersonaDireccionElectronicaMasivo servicio = new AutorizacionModificacionPersonaDireccionElectronicaMasivo();
				
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
