package cl.bcs.socionegocio.rel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;

public class AutorizarCrearRelacionPersonaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearRelacionPersonaMasivo.class);
	
	// Archivos SQL - Confirmar
	private final String SQL_UPDATE_RELACION_ESTADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/rel/sql/ConfirmarCrearRelacionPersonaMasivo_UpdateRelacionEstado.sql");
	
	// Archivos SQL - Rechazar
	private final String SQL_SELECT_SOLICITUDES_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/rel/sql/RechazaAutorizacionRelacionPersonaMasivo_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_RELACION_RECHAZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/rel/sql/RechazaAutorizacionRelacionPersonaMasivo_UpdateRelacion.sql");
	
	private final String ID_TIPO_ENTIDAD = "RELACION-PERSONA";
	private final String USA_REGLAS_NEGOCIO = "N";
	
	// Clases de entrada
	public static class EntradaAutorizarCrearRelacionPersonaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<RelacionPersonaPorAutorizar> p_RelacionPersona;
		public String p_IdEstadoAutorizacion;
		
		public EntradaAutorizarCrearRelacionPersonaMasivo() {
			this.p_RelacionPersona = new ArrayList<>();
		}
	}
	
	public static class RelacionPersonaPorAutorizar {
		public Long p_IdRelacionPersona;
		
		public RelacionPersonaPorAutorizar() {}
		
		public RelacionPersonaPorAutorizar(Long idRelacionPersona) {
			this.p_IdRelacionPersona = idRelacionPersona;
		}
	}
	
	// Clases de entrada para Rechazar
	public static class EntradaRechazaAutorizacionRelacionPersonaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<RelacionPersonaPorAutorizar> p_RelacionPersona;
		public String p_IdConcepto;
		
		public EntradaRechazaAutorizacionRelacionPersonaMasivo() {
			this.p_RelacionPersona = new ArrayList<>();
		}
	}
	
	// Clases de salida
	public static class SalidaAutorizarCrearRelacionPersonaMasivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaAutorizarCrearRelacionPersonaMasivo() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		
		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}
	
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}
	
	// Clases auxiliares para queries
	public static class RelacionPersonaUpdate {
		public Long p_IdRelacionPersona;
		public String p_IdEstadoAutorizacion;
		public String p_IdEstado;
	}
	
	public static class SolicitudCambioData {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
	}
	
	// ========== LÓGICA DE CONFIRMACIÓN ==========
	
	public SalidaAutorizarCrearRelacionPersonaMasivo logica(Connection conn, EntradaAutorizarCrearRelacionPersonaMasivo in) throws Exception {
		logger.info("INICIO::AutorizarCrearRelacionPersonaMasivo");
		SalidaAutorizarCrearRelacionPersonaMasivo salida = new SalidaAutorizarCrearRelacionPersonaMasivo();
		SQL sql = new SQL();
		
		try {
			// 1. Actualizar estado de autorización de las relaciones persona
			logger.debug("UPDATE relacion persona IdEstadoAutorizacion");
			List<RelacionPersonaUpdate> relacionesUpdate = in.p_RelacionPersona.stream().map(rel -> {
				RelacionPersonaUpdate update = new RelacionPersonaUpdate();
				update.p_IdRelacionPersona = rel.p_IdRelacionPersona;
				update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_RELACION_ESTADO, relacionesUpdate, RelacionPersonaUpdate.class);
			
		} catch (Exception ex) {
			logger.error("Error en AutorizarCrearRelacionPersonaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_RELACION", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::AutorizarCrearRelacionPersonaMasivo");
		return salida;
	}
	
	// ========== IMPLEMENTACIÓN DE BcsAutorizaciones ==========
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearRelacionPersonaMasivo.autorizarEvento");
		
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		EntradaAutorizarCrearRelacionPersonaMasivo entradaConfirmar = new EntradaAutorizarCrearRelacionPersonaMasivo();
		
		for(Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING"; // Estado confirmado
			entradaConfirmar.p_RelacionPersona.add(new RelacionPersonaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaAutorizarCrearRelacionPersonaMasivo salidaConfirmacion = logica(conn, entradaConfirmar);
			
			HashMap<Long, String> hashSalidaConfirmar = new HashMap<>();
			if (salidaConfirmacion.P_OUT_ERRORESCONTADOR == 0) {
				for(Evento evento : entrada.p_Lista) {
					long id = Long.parseLong(evento.NumeroOperacionConcepto);
					hashSalidaConfirmar.put(id, "OK");
				}
			}
			
			for(Evento evento : entrada.p_Lista) {
				long id = Long.parseLong(evento.NumeroOperacionConcepto);
				if(hashSalidaConfirmar.containsKey(id) && "OK".equals(hashSalidaConfirmar.get(id))) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_AUTORIZADA").msg();
					salidaAutoriza.P_OUT_ERRORESCONTADOR = 0L;
					salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					salidaAutoriza.P_OUT_RESULTADO.add(res);
				} else {
					salidaAutoriza.P_OUT_ERRORESCONTADOR = (long) salidaConfirmacion.P_OUT_ERRORESCONTADOR;
					for (ErrorDetalle err : salidaConfirmacion.P_OUT_ERRORESDETALLE) {
						salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
					}
				}
			}
			
		} catch (Exception e1) {
			logger.error("Error en autorizarEvento: " + e1.getMessage(), e1);
			salidaAutoriza.P_OUT_ERRORESCONTADOR = 1L;
			salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", e1.getMessage()));
		}
		
		logger.info("FIN::AutorizarCrearRelacionPersonaMasivo.autorizarEvento");
		return salidaAutoriza;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearRelacionPersonaMasivo.rechazarEvento");
		
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		
		try {
			EntradaRechazaAutorizacionRelacionPersonaMasivo entradaRechazar = new EntradaRechazaAutorizacionRelacionPersonaMasivo();
			
			for(Evento evento : entrada.p_Lista) {
				entradaRechazar.p_IdEmpresa = evento.IdEmpresa;
				entradaRechazar.p_IdModulo = evento.IdModulo;
				entradaRechazar.p_IdUsuario = evento.IdUsuario;
				entradaRechazar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
				entradaRechazar.p_IdConcepto = evento.IdConcepto;
				entradaRechazar.p_RelacionPersona.add(new RelacionPersonaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
			}
			
			SalidaAutorizarCrearRelacionPersonaMasivo salidaRechazado = rechazarLogica(conn, entradaRechazar);
			
			HashMap<Long, String> hashSalidaRechazar = new HashMap<>();
			if (salidaRechazado.P_OUT_ERRORESCONTADOR == 0) {
				for(Evento evento : entrada.p_Lista) {
					long id = Long.parseLong(evento.NumeroOperacionConcepto);
					hashSalidaRechazar.put(id, "OK");
				}
			}
			
			for(Evento evento : entrada.p_Lista) {
				long id = Long.parseLong(evento.NumeroOperacionConcepto);
				if(hashSalidaRechazar.containsKey(id) && "OK".equals(hashSalidaRechazar.get(id))) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_RECHAZADA").msg();
					salidaAutorizacion.P_OUT_ERRORESCONTADOR = 0L;
					salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_RECHAZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					salidaAutorizacion.P_OUT_RESULTADO.add(res);
				} else {
					salidaAutorizacion.P_OUT_ERRORESCONTADOR = (long) salidaRechazado.P_OUT_ERRORESCONTADOR;
					for (ErrorDetalle err : salidaRechazado.P_OUT_ERRORESDETALLE) {
						salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
					}
				}
			}
			
		} catch (Exception e1) {
			logger.error("Error en rechazarEvento: " + e1.getMessage(), e1);
			salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
			salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RECHAZO", e1.getMessage()));
		}
		
		logger.info("FIN::AutorizarCrearRelacionPersonaMasivo.rechazarEvento");
		return salidaAutorizacion;
	}
	
	// ========== LÓGICA DE RECHAZO ==========
	
	public SalidaAutorizarCrearRelacionPersonaMasivo rechazarLogica(Connection conn, EntradaRechazaAutorizacionRelacionPersonaMasivo in) throws Exception {
		logger.info("INICIO::RechazaAutorizacionRelacionPersonaMasivo");
		SalidaAutorizarCrearRelacionPersonaMasivo salida = new SalidaAutorizarCrearRelacionPersonaMasivo();
		SQL sql = new SQL();
		
		try {
			String v_IdEstadoAutorizacion = "RZD";
			
			// Validar existencia de solicitud para MODREL
			if ("MODREL".equals(in.p_IdConcepto)) {
				logger.debug("Validar existencia de solicitud");
				
				List<SolicitudCambioData> solicitudesTotal = new ArrayList<>();
				HashMap<String, Object> paramsValidar = new HashMap<>();
				paramsValidar.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD);
				
				for (RelacionPersonaPorAutorizar rel : in.p_RelacionPersona) {
					paramsValidar.put("p_IdEntidadOrigen", String.valueOf(rel.p_IdRelacionPersona));
					List<SolicitudCambioData> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsValidar, SolicitudCambioData.class);
					
					if (solicitudes != null && !solicitudes.isEmpty()) {
						solicitudesTotal.addAll(solicitudes);
					}
				}
				
				if (solicitudesTotal.isEmpty()) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SOLICITUD_NO_EXISTE", "La solicitud no existe"));
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
					return salida;
				}
			}//agregue esta linea 
				
			// Para MODREL, la solicitud de cambio se ha validado que existe
			// El rechazo procederá actualizando la relación directamente
			logger.debug("Solicitudes validadas para MODREL, procediendo con rechazo");
			List<RelacionPersonaUpdate> relacionesUpdate = in.p_RelacionPersona.stream().map(rel -> {
				RelacionPersonaUpdate update = new RelacionPersonaUpdate();
				update.p_IdRelacionPersona = rel.p_IdRelacionPersona;
				update.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
				// Si es NUEREL, marcar como inactivo
				if ("NUEREL".equals(in.p_IdConcepto)) {
					update.p_IdEstado = "INA";
				} else {
					update.p_IdEstado = null; // Mantener estado actual
				}
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_RELACION_RECHAZADO, relacionesUpdate, RelacionPersonaUpdate.class);
			
		} catch (Exception ex) {
			logger.error("Error en RechazaAutorizacionRelacionPersonaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_RECHAZAR_RELACION", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::RechazaAutorizacionRelacionPersonaMasivo");
		return salida;
	}

	
	public static void main(String[] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarCrearRelacionPersonaMasivo e = new AutorizarCrearRelacionPersonaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = 0L;
		
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "100";
			evento.IdEmpresa = entrada.p_IdEmpresa;
			evento.IdModulo = entrada.p_IdModulo;
			evento.IdUsuario = entrada.p_IdUsuario;
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
