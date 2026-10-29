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

public class AutorizacionModificacionEmpleadoDatosGeneralesMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEmpleadoDatosGeneralesMasivo.class);

	private final String SQL_UPDATE_EMPLEADO_AUTORIZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEmpleadoDatosGeneralesMasivo_Update_EmpleadoAutorizado.sql");
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/emp/sql/AutorizacionModificacionEmpleadoDatosGeneralesMasivo_Update_EstadoSolicitud.sql");
	
	public static class EmpleadoAutorizacion {
		public Long IdEmpleado;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODEMP o CAEEMP
	}
	
	/**
	 * AUTORIZAR: Lógica completa migrada del SP ConfirmarModificarEmpleadoDatosGeneralesMasivo
	 * Maneja MODEMP (modificación datos generales) y CAEEMP (cambio estado)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEmpleadoDatosGeneralesMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de empleados desde entrada
			List<EmpleadoAutorizacion> empleados = entrada.p_Lista.stream()
				.map(evento -> {
					EmpleadoAutorizacion e = new EmpleadoAutorizacion();
					e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
					e.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					e.IdConcepto = evento.IdTipoEvento;  // MODEMP o CAEEMP
					return e;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// 1. Actualizar Empleado con valores desde SolicitudCambio (13 campos)
			new SQL().batch(conn, SQL_UPDATE_EMPLEADO_AUTORIZADO, empleados, EmpleadoAutorizacion.class);
			
			// 2. Actualizar estado de SolicitudCambio a Autorizado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, empleados, EmpleadoAutorizacion.class);
			
			// Agregar resultado por cada empleado procesado
			for (EmpleadoAutorizacion empleado : empleados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = empleado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionEmpleadoDatosGeneralesMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * RECHAZAR: Lógica completa migrada del SP RechazaAutorizacionEmpleadoDatosGeneralesMasivo
	 * Solo actualiza estado de SolicitudCambio a Rechazado (no modifica Empleado)
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionEmpleadoDatosGeneralesMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de empleados desde entrada
			List<EmpleadoAutorizacion> empleados = entrada.p_Lista.stream()
				.map(evento -> {
					EmpleadoAutorizacion e = new EmpleadoAutorizacion();
					e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
					e.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					e.IdConcepto = evento.IdTipoEvento;  // MODEMP o CAEEMP
					return e;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar estado de SolicitudCambio a Rechazado
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, empleados, EmpleadoAutorizacion.class);
			
			// Agregar resultado por cada empleado procesado
			for (EmpleadoAutorizacion empleado : empleados) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = empleado;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionEmpleadoDatosGeneralesMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODEMP\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionEmpleadoDatosGeneralesMasivo servicio = new AutorizacionModificacionEmpleadoDatosGeneralesMasivo();
				
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
