package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionPersonaTelefonoMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaTelefonoMasivo.class);
	
	private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaTelefonoMasivo_Update_EstadoSolicitud.sql");
	private final String SQL_UPDATE_TELEFONO_RECHAZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaTelefonoMasivo_Update_TelefonoRechazado.sql");
	
	public static class TelefonoAutorizacion {
		public Long IdTelefono;
		public String IdEstadoAutorizacion;
		public String IdConcepto;  // MODTEL (solo modificaciones)
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaTelefonoMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de teléfonos desde entrada
			List<TelefonoAutorizacion> telefonos = entrada.p_Lista.stream()
				.map(evento -> {
					TelefonoAutorizacion t = new TelefonoAutorizacion();
					t.IdTelefono = Long.valueOf(evento.NumeroOperacionConcepto);
					t.IdEstadoAutorizacion = "AUT";  // Estado autorizado
					t.IdConcepto = evento.IdTipoEvento;  // MODTEL
					return t;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Llamar al servicio de ConfirmarModificar existente
			ConfirmarModificarPersonaTelefonoMasivo confirmar = new ConfirmarModificarPersonaTelefonoMasivo();
			EntradaConfirmarEjecutarCambiosPersona entradaConfirmar = new EntradaConfirmarEjecutarCambiosPersona();
			
			entradaConfirmar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmar.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdEstadoAutorizacion = "AUT";
			
			// Agregar teléfonos a la entrada
			for (TelefonoAutorizacion telefono : telefonos) {
				InEntidad entidad = new InEntidad();
				entidad.IdEntidad = telefono.IdTelefono;
				entradaConfirmar.p_Entidad.add(entidad);
			}
			
			SalidaConfirmarEjecutarCambiosPersona salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
			
			// Si hay errores, propagar
			if (salidaConfirmar.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR = salidaConfirmar.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_MENSAJES.addAll(salidaConfirmar.P_OUT_MENSAJES);
			} else {
				// Agregar resultado por cada teléfono procesado
				for (TelefonoAutorizacion telefono : telefonos) {
					BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
					resultado.evento = telefono;
					resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
					salida.P_OUT_RESULTADO.add(resultado);
				}
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaTelefonoMasivo.autorizarEvento");
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionModificacionPersonaTelefonoMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de teléfonos desde entrada
			List<TelefonoAutorizacion> telefonos = entrada.p_Lista.stream()
				.map(evento -> {
					TelefonoAutorizacion t = new TelefonoAutorizacion();
					t.IdTelefono = Long.valueOf(evento.NumeroOperacionConcepto);
					t.IdEstadoAutorizacion = "RZD";  // Estado rechazado
					t.IdConcepto = evento.IdTipoEvento;  // MODTEL
					return t;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Solo MODTEL: actualizar estado de SolicitudCambio y Telefono
			new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, telefonos, TelefonoAutorizacion.class);
			new SQL().batch(conn, SQL_UPDATE_TELEFONO_RECHAZADO, telefonos, TelefonoAutorizacion.class);
			
			// Agregar resultado por cada teléfono procesado
			for (TelefonoAutorizacion telefono : telefonos) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = telefono;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionModificacionPersonaTelefonoMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"MODTEL\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionModificacionPersonaTelefonoMasivo servicio = new AutorizacionModificacionPersonaTelefonoMasivo();
				
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
