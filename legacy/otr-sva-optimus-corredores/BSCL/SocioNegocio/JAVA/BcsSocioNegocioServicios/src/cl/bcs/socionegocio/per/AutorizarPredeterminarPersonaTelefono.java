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
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarPredeterminarPersonaTelefono implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarPredeterminarPersonaTelefono.class);
	
	private final SQL sql = new SQL();
	
	private final String SQL_UPDATE_DESMARCAR_TELEFONOS = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Update_DesmarcarTelefonos.sql"
		);
	
	private final String SQL_UPDATE_MARCAR_TELEFONO_PREDETERMINADO = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Update_MarcarTelefonoPredeterminado.sql"
		);
	
	private final String SQL_UPDATE_TELEFONO_PRINCIPAL_PERSONA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Update_TelefonoPrincipalPersona.sql"
		);
	
	private final String SQL_UPDATE_TELEFONO_CONTACTO_PORTAFOLIO = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Update_TelefonoContactoPortafolio.sql"
		);
	
	private final String SQL_SELECT_ID_PERSONA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Select_IdPersona.sql"
		);
	
	private final String SQL_SELECT_DATOS_PERSONA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Select_DatosPersona.sql"
		);
	
	private final String SQL_SELECT_NUMERO_TELEFONO = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaTelefono_Select_NumeroTelefonoNuevo.sql"
		);
	
	public static class Telefono {
		public Long IdTelefono;
		public Long IdPersona;
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Telefono that = (Telefono) o;
			return IdTelefono != null && IdTelefono.equals(that.IdTelefono);
		}
		
		@Override
		public int hashCode() {
			return IdTelefono != null ? IdTelefono.hashCode() : 0;
		}
	}
	
	public static class TelefonoPersona {
		public Long IdPersona;
	}
	
	public static class DesmarcarTelefonos {
		public Long IdPersona;
	}
	
	public static class TelefonoPrincipal {
		public Long IdPersona;
		public Long IdTelefono;
	}
	
	public static class DatosPersona {
		public String Nombres;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
	}
	
	public static class NumeroTelefonoNuevo {
		public String Numero;
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarPredeterminarPersonaTelefono.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<Telefono> telefonos = entrada.p_Lista.stream()
				.map(evento -> {
					Telefono t = new Telefono();
					t.IdTelefono = Long.valueOf(evento.NumeroOperacionConcepto);
					return t;
				})
				.distinct()
				.collect(Collectors.toList());
			
			for (Telefono telefono : telefonos) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdTelefono", telefono.IdTelefono);
				
				// Obtener IdPersona del teléfono
				List<TelefonoPersona> personaList = sql.query(conn, SQL_SELECT_ID_PERSONA, params, TelefonoPersona.class);
				if (personaList.isEmpty()) {
					continue;
				}
				Long idPersona = personaList.get(0).IdPersona;
				telefono.IdPersona = idPersona;
			}
			
			// Desmarcar todos los teléfonos de las personas afectadas
			List<DesmarcarTelefonos> desmarcarList = telefonos.stream()
				.map(tel -> {
					DesmarcarTelefonos d = new DesmarcarTelefonos();
					d.IdPersona = tel.IdPersona;
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			for (DesmarcarTelefonos desmarcar : desmarcarList) {
				List<DesmarcarTelefonos> singleList = new java.util.ArrayList<>();
				singleList.add(desmarcar);
				sql.batch(conn, SQL_UPDATE_DESMARCAR_TELEFONOS, singleList, DesmarcarTelefonos.class);
			}
			
			// Marcar los teléfonos como predeterminados
			sql.batch(conn, SQL_UPDATE_MARCAR_TELEFONO_PREDETERMINADO, telefonos, Telefono.class);
			
			// Actualizar teléfono principal en Persona
			List<TelefonoPrincipal> principalList = telefonos.stream()
				.map(tel -> {
					TelefonoPrincipal tp = new TelefonoPrincipal();
					tp.IdPersona = tel.IdPersona;
					tp.IdTelefono = tel.IdTelefono;
					return tp;
				})
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_TELEFONO_PRINCIPAL_PERSONA, principalList, TelefonoPrincipal.class);
			
			// Actualizar teléfono de contacto en Portafolio
			sql.batch(conn, SQL_UPDATE_TELEFONO_CONTACTO_PORTAFOLIO, principalList, TelefonoPrincipal.class);
			
			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo
			List<EntidadOrigen> entidadesOrigen = telefonos.stream()
				.map(tel -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = String.valueOf(tel.IdTelefono);
					eo.IdTipoEntidad = "TELEFONO-PREDETERMINAR";
					return eo;
				})
				.collect(Collectors.toList());
			
			ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
			EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
			entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
			entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
			entradaSolicitud.p_EntidadOrigen = entidadesOrigen;
			entradaSolicitud.p_IdEstadoAutorizacion = "AUT";
			
			SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
			
			if (salidaSolicitud.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				return salida;
			}
			
			// Generar logs de auditoría
			try {
				String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
				String fechaSistemaStr = fechaSistema.substring(0, 10);
				
				AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
				entradaLog.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaLog.p_IdUsuario = entrada.p_IdUsuario;
				entradaLog.p_IdModulo = "SOC";
				
				for (Telefono telefono : telefonos) {
					try {
						HashMap<String, Object> params = new HashMap<>();
						params.put("IdPersona", telefono.IdPersona);
						params.put("IdTelefono", telefono.IdTelefono);
						
						// Obtener datos de persona
						List<DatosPersona> datosPersonaList = sql.query(conn, SQL_SELECT_DATOS_PERSONA, params, DatosPersona.class);
						if (datosPersonaList.isEmpty()) {
							continue;
						}
						DatosPersona datosPersona = datosPersonaList.get(0);
						
						// Obtener número de teléfono nuevo
						List<NumeroTelefonoNuevo> telefonoNuevoList = sql.query(conn, SQL_SELECT_NUMERO_TELEFONO, params, NumeroTelefonoNuevo.class);
						String numeroTelefonoNuevo = telefonoNuevoList.isEmpty() ? "" : telefonoNuevoList.get(0).Numero;
						
						// Crear log de cabecera
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = telefono.IdTelefono;
						log.IdTipoLog = "MODIFICACION";
						log.IdModulo = "SOC";
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistemaStr;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdPersona = telefono.IdPersona;
						log.NombreCliente = datosPersona.Nombres != null ? datosPersona.Nombres : "";
						log.IdDocumentoIdentidad = datosPersona.DocumentoIdentidad != null ? datosPersona.DocumentoIdentidad : "";
						log.IdTipoDocumentoIdentidad = datosPersona.IdTipoDocumentoIdentidad != null ? datosPersona.IdTipoDocumentoIdentidad : "";
						log.IdTipoRegistro = "TELEFONO-PREDETERMINAR";
						log.IdRegistro = String.valueOf(telefono.IdTelefono);
						log.EstadoProceso = "AUT";
						log.NombreProceso = "Confirmar Predeterminar Telefono";
						entradaLog.p_Log.add(log);
						
						// Crear detalle del log
						AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
						detalle.IdCabeceraLog = telefono.IdTelefono;
						detalle.SubRegistro = "TELEFONO";
						detalle.CampoRegistro = "Predeterminado";
						detalle.ValorAntiguo = "N";
						detalle.ValorNuevo = numeroTelefonoNuevo;
						entradaLog.p_LogDetalle.add(detalle);
						
					} catch (Exception logEx) {
						logger.warn("Error al preparar log de auditoría para telefono {}: {}", telefono.IdTelefono, logEx.getMessage());
					}
				}
				
				// Llamar al servicio de logs
				if (!entradaLog.p_Log.isEmpty()) {
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaLog);
				}
				
			} catch (Exception logEx) {
				logger.warn("Error al generar logs de auditoría: {}", logEx.getMessage());
			}
			
			// Agregar resultados
			for (Telefono telefono : telefonos) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = telefono;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarPredeterminarPersonaTelefono.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarPredeterminarPersonaTelefono.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<Telefono> telefonos = entrada.p_Lista.stream()
				.map(evento -> {
					Telefono t = new Telefono();
					t.IdTelefono = Long.valueOf(evento.NumeroOperacionConcepto);
					return t;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo
			List<EntidadOrigen> entidadesOrigen = telefonos.stream()
				.map(tel -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = String.valueOf(tel.IdTelefono);
					eo.IdTipoEntidad = "Telefono-BORRAR";
					return eo;
				})
				.collect(Collectors.toList());
			
			ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
			EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
			entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
			entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
			entradaSolicitud.p_EntidadOrigen = entidadesOrigen;
			entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
			
			SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
			
			if (salidaSolicitud.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				return salida;
			}
			
			// Agregar resultados
			for (Telefono telefono : telefonos) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = telefono;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarPredeterminarPersonaTelefono.rechazarEvento {}", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarPredeterminarPersonaTelefono servicio = new AutorizarPredeterminarPersonaTelefono();
			
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "SOC";
			entrada.p_UsaReglasNegocio = "N";
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "1725885"; // IdTelefono
			entrada.p_Lista.add(evento);
			
			System.out.println("\n=== Probando AUTORIZAR ===");
			SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
			System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());

			System.out.println("\n=== Probando RECHAZAR ===");
			SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
			System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n== FIN TEST ==");
	}
}
