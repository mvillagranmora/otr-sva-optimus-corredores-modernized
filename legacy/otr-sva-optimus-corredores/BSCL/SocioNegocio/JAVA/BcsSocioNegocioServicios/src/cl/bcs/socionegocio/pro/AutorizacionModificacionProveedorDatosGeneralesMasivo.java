package cl.bcs.socionegocio.pro;

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

public class AutorizacionModificacionProveedorDatosGeneralesMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionProveedorDatosGeneralesMasivo.class);

	private final String SQL_UPDATE_PROVEEDOR_AUTORIZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/pro/sql/AutorizacionModificacionProveedorDatosGeneralesMasivo_Update_ProveedorAutorizado.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/pro/sql/AutorizacionModificacionProveedorDatosGeneralesMasivo_Update_EstadoSolicitud.sql");
	
	public static class ProveedorAutorizacion {
		public Long IdProveedor;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODPRO o CAEPRO
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarModificarProveedorDatosGeneralesMasivo
	 * Maneja MODPRO (modificación datos generales) y CAEPRO (cambio estado)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionProveedorDatosGeneralesMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de proveedores desde entrada
			List<ProveedorAutorizacion> proveedores = entrada.p_Lista.stream()
				.map(evento -> {
					ProveedorAutorizacion p = new ProveedorAutorizacion();
					p.IdProveedor = Long.valueOf(evento.NumeroOperacionConcepto);
					p.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					p.IdConcepto = evento.IdTipoEvento;  // MODPRO o CAEPRO
					return p;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// 1. Actualizar Proveedor con valores desde SolicitudCambio (IdEstado, IdAreaProveedor)
			new SQL().batch(conn, SQL_UPDATE_PROVEEDOR_AUTORIZADO, proveedores, ProveedorAutorizacion.class);
			
			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, proveedores, ProveedorAutorizacion.class);
			
			// Agregar resultado por cada proveedor procesado
			for (ProveedorAutorizacion proveedor : proveedores) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = proveedor;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionProveedorDatosGeneralesMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionProveedorDatosGeneralesMasivo
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica Proveedor)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionProveedorDatosGeneralesMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de proveedores desde entrada
			List<ProveedorAutorizacion> proveedores = entrada.p_Lista.stream()
				.map(evento -> {
					ProveedorAutorizacion p = new ProveedorAutorizacion();
					p.IdProveedor = Long.valueOf(evento.NumeroOperacionConcepto);
					p.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					p.IdConcepto = evento.IdTipoEvento;  // MODPRO o CAEPRO
					return p;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, proveedores, ProveedorAutorizacion.class);
			
			// Agregar resultado por cada proveedor procesado
			for (ProveedorAutorizacion proveedor : proveedores) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = proveedor;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionProveedorDatosGeneralesMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODPRO\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionProveedorDatosGeneralesMasivo servicio = new AutorizacionModificacionProveedorDatosGeneralesMasivo();
				
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
