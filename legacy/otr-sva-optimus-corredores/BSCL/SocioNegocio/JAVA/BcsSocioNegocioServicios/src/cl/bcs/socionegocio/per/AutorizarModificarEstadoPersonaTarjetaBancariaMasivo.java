package cl.bcs.socionegocio.per;

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
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;

public class AutorizarModificarEstadoPersonaTarjetaBancariaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarModificarEstadoPersonaTarjetaBancariaMasivo.class);
	
	// Archivos SQL - Confirmar
	private final String SQL_UPDATE_TARJETA_ESTADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarEstadoPersonaTarjetaBancariaMasivo_UpdateTarjetaEstado.sql");
	
	// Archivos SQL - Rechazar
	private final String SQL_SELECT_SOLICITUDES_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionPersonaTarjetaBancariaMasivo_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_TARJETA_RECHAZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionPersonaTarjetaBancariaMasivo_UpdateTarjeta.sql");
	
	private final String ID_TIPO_ENTIDAD = "TARJETABANCARIA";
	private final String ID_TIPO_ENTIDAD_ESTADO = "TARJETABANCARIA-ESTADO";
	private final String USA_REGLAS_NEGOCIO = "N";
	
	// Clases de entrada
	public static class EntradaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<TarjetaBancariaPorAutorizar> p_TarjetaBancaria;
		public String p_IdEstadoAutorizacion;
		
		public EntradaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo() {
			this.p_TarjetaBancaria = new ArrayList<>();
		}
	}
	
	public static class TarjetaBancariaPorAutorizar {
		public Long p_IdTarjetaBancaria;
		
		public TarjetaBancariaPorAutorizar() {}
		
		public TarjetaBancariaPorAutorizar(Long idTarjetaBancaria) {
			this.p_IdTarjetaBancaria = idTarjetaBancaria;
		}
	}
	
	// Clases de entrada para Rechazar
	public static class EntradaRechazaAutorizacionPersonaTarjetaBancariaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<TarjetaBancariaPorAutorizar> p_TarjetaBancaria;
		public String p_IdConcepto;
		
		public EntradaRechazaAutorizacionPersonaTarjetaBancariaMasivo() {
			this.p_TarjetaBancaria = new ArrayList<>();
		}
	}
	
	// Clases de salida
	public static class SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo() {
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
	public static class TarjetaBancariaUpdate {
		public Long p_IdTarjetaBancaria;
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
	
	public SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo logica(Connection conn, EntradaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo in) throws Exception {
		logger.info("INICIO::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo");
		SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo salida = new SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo();
		SQL sql = new SQL();
		
		try {
			// 1. Validar existencia de solicitud PEN (CRÍTICO)
			logger.debug("Validar existencia de solicitud PEN");
			List<SolicitudCambioData> solicitudesTotal = new ArrayList<>();
			HashMap<String, Object> paramsValidar = new HashMap<>();
			paramsValidar.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD_ESTADO);
			
			for (TarjetaBancariaPorAutorizar tarjeta : in.p_TarjetaBancaria) {
				paramsValidar.put("p_IdEntidadOrigen", String.valueOf(tarjeta.p_IdTarjetaBancaria));
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
			
			// 2. Actualizar estado de autorización de las tarjetas bancarias
			logger.debug("UPDATE tarjeta bancaria IdEstadoAutorizacion");
			List<TarjetaBancariaUpdate> tarjetasUpdate = in.p_TarjetaBancaria.stream().map(tarjeta -> {
				TarjetaBancariaUpdate update = new TarjetaBancariaUpdate();
				update.p_IdTarjetaBancaria = tarjeta.p_IdTarjetaBancaria;
				update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_TARJETA_ESTADO, tarjetasUpdate, TarjetaBancariaUpdate.class);
			
			// 3. Cerrar solicitud de cambio (CRÍTICO)
			logger.debug("Modificar estado solicitud cambio masivo");
			ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();
			EntradaModificarEstado entradaModificar = new EntradaModificarEstado();
			
			entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
			entradaModificar.p_IdUsuario = in.p_IdUsuario;
			entradaModificar.p_IdModulo = in.p_IdModulo;
			entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
			
			// Agregar entidades origen
			for (SolicitudCambioData sol : solicitudesTotal) {
				EntidadOrigen entidad = new EntidadOrigen();
				entidad.IdEntidadOrigen = sol.IdEntidadOrigen;
				entidad.IdTipoEntidad = sol.IdTipoEntidad;
				entradaModificar.p_EntidadOrigen.add(entidad);
			}
			
			SalidaModificarEstado salidaModificar = modificarEstado.logica(conn, entradaModificar);
			
			// Propagar errores y advertencias
			salida.P_OUT_ADVERTENCIASCONTADOR += salidaModificar.p_out_AdvertenciasContador;
			for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : salidaModificar.p_out_AdvertenciasDetalle) {
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
			}
			
			salida.P_OUT_ERRORESCONTADOR += salidaModificar.p_out_ErroresContador;
			for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificar.p_out_ErroresDetalle) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
			}
			
		} catch (Exception ex) {
			logger.error("Error en AutorizarModificarEstadoPersonaTarjetaBancariaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_TARJETA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo");
		return salida;
	}
	
	// ========== IMPLEMENTACIÓN DE BcsAutorizaciones ==========
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo.autorizarEvento");
		
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		EntradaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo entradaConfirmar = new EntradaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo();
		
		for(Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING"; // Estado ingresado (autorizado)
			entradaConfirmar.p_TarjetaBancaria.add(new TarjetaBancariaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo salidaConfirmacion = logica(conn, entradaConfirmar);
			
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
		
		logger.info("FIN::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo.autorizarEvento");
		return salidaAutoriza;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo.rechazarEvento");
		
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		
		try {
			EntradaRechazaAutorizacionPersonaTarjetaBancariaMasivo entradaRechazar = new EntradaRechazaAutorizacionPersonaTarjetaBancariaMasivo();
			
			for(Evento evento : entrada.p_Lista) {
				entradaRechazar.p_IdEmpresa = evento.IdEmpresa;
				entradaRechazar.p_IdModulo = evento.IdModulo;
				entradaRechazar.p_IdUsuario = evento.IdUsuario;
				entradaRechazar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
				entradaRechazar.p_IdConcepto = evento.IdConcepto;
				entradaRechazar.p_TarjetaBancaria.add(new TarjetaBancariaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
			}
			
			SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo salidaRechazado = rechazarLogica(conn, entradaRechazar);
			
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
		
		logger.info("FIN::AutorizarModificarEstadoPersonaTarjetaBancariaMasivo.rechazarEvento");
		return salidaAutorizacion;
	}
	
	// ========== LÓGICA DE RECHAZO ==========
	
	public SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo rechazarLogica(Connection conn, EntradaRechazaAutorizacionPersonaTarjetaBancariaMasivo in) throws Exception {
		logger.info("INICIO::RechazaAutorizacionPersonaTarjetaBancariaMasivo");
		SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo salida = new SalidaAutorizarModificarEstadoPersonaTarjetaBancariaMasivo();
		SQL sql = new SQL();
		
		try {
			final String v_IdEstadoAutorizacion = "RZD";
			String v_TipoEntidad = ID_TIPO_ENTIDAD;
			
			// Determinar tipo de entidad según concepto
			if ("CAETJB".equals(in.p_IdConcepto)) {
				v_TipoEntidad = ID_TIPO_ENTIDAD_ESTADO;
			}
			
			// Validar existencia de solicitud para MODTJB o CAETJB (NO para NUETJB)
			if ("MODTJB".equals(in.p_IdConcepto) || "CAETJB".equals(in.p_IdConcepto)) {
				logger.debug("Validar existencia de solicitud");
				
				List<SolicitudCambioData> solicitudesTotal = new ArrayList<>();
				HashMap<String, Object> paramsValidar = new HashMap<>();
				paramsValidar.put("p_IdTipoEntidad", v_TipoEntidad);
				
				for (TarjetaBancariaPorAutorizar tarjeta : in.p_TarjetaBancaria) {
					paramsValidar.put("p_IdEntidadOrigen", String.valueOf(tarjeta.p_IdTarjetaBancaria));
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
				
				// Modificar estado de solicitud de cambio
				logger.debug("Modificar estado solicitud cambio masivo");
				ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();
				EntradaModificarEstado entradaModificar = new EntradaModificarEstado();
				
				entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
				entradaModificar.p_IdUsuario = in.p_IdUsuario;
				entradaModificar.p_IdModulo = in.p_IdModulo;
				entradaModificar.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
				
				// Agregar entidades origen
				for (SolicitudCambioData sol : solicitudesTotal) {
					EntidadOrigen entidad = new EntidadOrigen();
					entidad.IdEntidadOrigen = sol.IdEntidadOrigen;
					entidad.IdTipoEntidad = sol.IdTipoEntidad;
					entradaModificar.p_EntidadOrigen.add(entidad);
				}
				
				SalidaModificarEstado salidaModificar = modificarEstado.logica(conn, entradaModificar);
				
				// Propagar errores y advertencias
				salida.P_OUT_ADVERTENCIASCONTADOR += salidaModificar.p_out_AdvertenciasContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.AdvertenciaDetalle adv : salidaModificar.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}
				
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.p_out_ErroresContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificar.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
				
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}
			}
			
			// Actualizar tarjetas bancarias rechazadas
			logger.debug("UPDATE tarjeta bancaria rechazada");
			List<TarjetaBancariaUpdate> tarjetasUpdate = in.p_TarjetaBancaria.stream().map(tarjeta -> {
				TarjetaBancariaUpdate update = new TarjetaBancariaUpdate();
				update.p_IdTarjetaBancaria = tarjeta.p_IdTarjetaBancaria;
				update.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
				// Si es NUETJB, marcar como inactivo
				if ("NUETJB".equals(in.p_IdConcepto)) {
					update.p_IdEstado = "INA";
				} else {
					update.p_IdEstado = null; // Mantener estado actual
				}
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_TARJETA_RECHAZADO, tarjetasUpdate, TarjetaBancariaUpdate.class);
			
		} catch (Exception ex) {
			logger.error("Error en RechazaAutorizacionPersonaTarjetaBancariaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_RECHAZAR_TARJETA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::RechazaAutorizacionPersonaTarjetaBancariaMasivo");
		return salida;
	}
	
	public static void main(String[] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarModificarEstadoPersonaTarjetaBancariaMasivo e = new AutorizarModificarEstadoPersonaTarjetaBancariaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = 0L;
		
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "10056";
			evento.IdEmpresa = entrada.p_IdEmpresa;
			evento.IdModulo = entrada.p_IdModulo;
			evento.IdUsuario = entrada.p_IdUsuario;
			evento.IdConcepto = "MODTJB";
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
