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
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionPersonaDireccionFisicaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaDireccionFisicaMasivo.class);

	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaDireccionFisicaMasivo_Update_EstadoSolicitud.sql");
	private final String SQL_UPDATE_DIRECCION_RECHAZADA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaDireccionFisicaMasivo_Update_DireccionRechazada.sql");
	
	public static class DireccionAutorizacion {
		public Long IdDireccionFisica;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODDIF (solo modificaciones)
	}
	
	/**
	 * AUTORIZAR: Delega al servicio existente ConfirmarModificarPersonaDireccionFisicaMasivo
	 * Solo maneja MODDIF (concepto de modificación)
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaDireccionFisicaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones desde entrada
			List<DireccionAutorizacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionAutorizacion d = new DireccionAutorizacion();
					d.IdDireccionFisica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					d.IdConcepto = evento.IdTipoEvento;  // MODDIF
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Llamar al servicio de ConfirmarModificar existente
			ConfirmarModificarPersonaDireccionFisicaMasivo confirmar = new ConfirmarModificarPersonaDireccionFisicaMasivo();
			EntradaConfirmarEjecutarCambiosPersona entradaConfirmar = new EntradaConfirmarEjecutarCambiosPersona();
			
			entradaConfirmar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmar.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdEstadoAutorizacion = "AUT";
			
			// Agregar direcciones a la entrada
			for (DireccionAutorizacion direccion : direcciones) {
				InEntidad entidad = new InEntidad();
				entidad.IdEntidad = direccion.IdDireccionFisica;
				entradaConfirmar.p_Entidad.add(entidad);
			}
			
			SalidaConfirmarEjecutarCambiosPersona salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
			
			// Si hay errores, propagar
			if (salidaConfirmar.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR = salidaConfirmar.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_MENSAJES.addAll(salidaConfirmar.P_OUT_MENSAJES);
			} else {
				// Agregar resultado por cada dirección procesada
				for (DireccionAutorizacion direccion : direcciones) {
					BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
					resultado.evento = direccion;
					resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
					salida.P_OUT_RESULTADO.add(resultado);
				}
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaDireccionFisicaMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * RECHAZAR: Implementación directa con SQL batch
	 * Solo maneja MODDIF (modificar dirección)
	 * NUEDIF se migrará en otro servicio
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaDireccionFisicaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de direcciones desde entrada
			List<DireccionAutorizacion> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionAutorizacion d = new DireccionAutorizacion();
					d.IdDireccionFisica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					d.IdConcepto = evento.IdTipoEvento;  // MODDIF
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Solo MODDIF: actualizar estado de SolicitudCambio y DireccionFisica
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, direcciones, DireccionAutorizacion.class);
			new SQL().batch(conn, SQL_UPDATE_DIRECCION_RECHAZADA, direcciones, DireccionAutorizacion.class);
			
			// Agregar resultado por cada dirección procesada
			for (DireccionAutorizacion direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaDireccionFisicaMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODDIF\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionPersonaDireccionFisicaMasivo servicio = new AutorizacionModificacionPersonaDireccionFisicaMasivo();
				
				// Probar CONFIRMAR
				System.out.println("\n=== Probando CONFIRMAR ===");
				SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Salida confirmar: " + salidaConfirmar);
				System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
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
