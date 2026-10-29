package cl.bcs.socionegocio.cnt;

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

public class AutorizacionModificacionContraparteDatosGeneralesMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionContraparteDatosGeneralesMasivo.class);

	private final String SQL_UPDATE_CONTRAPARTE_AUTORIZADA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cnt/sql/AutorizacionModificacionContraparteDatosGeneralesMasivo_Update_ContraparteAutorizada.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cnt/sql/AutorizacionModificacionContraparteDatosGeneralesMasivo_Update_EstadoSolicitud.sql");
	
	public static class ContraparteAutorizacion {
		public Long IdContraparte;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODCNT o CAECNT
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarModificarContraparteDatosGeneralesMasivo
	 * Maneja MODCNT (modificación datos generales) y CAECNT (cambio estado)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionContraparteDatosGeneralesMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de contrapartes desde entrada
			List<ContraparteAutorizacion> contrapartes = entrada.p_Lista.stream()
				.map(evento -> {
					ContraparteAutorizacion c = new ContraparteAutorizacion();
					c.IdContraparte = Long.valueOf(evento.NumeroOperacionConcepto);
					c.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					c.IdConcepto = evento.IdTipoEvento;  // MODCNT o CAECNT
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// 1. Actualizar Contraparte con valores desde SolicitudCambio (8 campos)
			new SQL().batch(conn, SQL_UPDATE_CONTRAPARTE_AUTORIZADA, contrapartes, ContraparteAutorizacion.class);
			
			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, contrapartes, ContraparteAutorizacion.class);
			
			// Agregar resultado por cada contraparte procesada
			for (ContraparteAutorizacion contraparte : contrapartes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = contraparte;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionContraparteDatosGeneralesMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionContraparteDatosGeneralesMasivo
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica Contraparte)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionContraparteDatosGeneralesMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de contrapartes desde entrada
			List<ContraparteAutorizacion> contrapartes = entrada.p_Lista.stream()
				.map(evento -> {
					ContraparteAutorizacion c = new ContraparteAutorizacion();
					c.IdContraparte = Long.valueOf(evento.NumeroOperacionConcepto);
					c.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					c.IdConcepto = evento.IdTipoEvento;  // MODCNT o CAECNT
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, contrapartes, ContraparteAutorizacion.class);
			
			// Agregar resultado por cada contraparte procesada
			for (ContraparteAutorizacion contraparte : contrapartes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = contraparte;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionContraparteDatosGeneralesMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODCNT\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionContraparteDatosGeneralesMasivo servicio = new AutorizacionModificacionContraparteDatosGeneralesMasivo();
				
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
