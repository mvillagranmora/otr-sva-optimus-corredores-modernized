package cl.bcs.socionegocio.per;

import java.sql.Connection;
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
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarCrearPersonaTelefonoMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearPersonaTelefonoMasivo.class);
	
	// Archivos SQL - Confirmar
	private final String SQL_UPDATE_TELEFONO_ESTADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTelefonoMasivo_UpdateTelefonoEstado.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTelefonoMasivo_InsertLogGenerarContratos.sql");
	
	// Archivos SQL - Rechazar
	private final String SQL_SELECT_SOLICITUDES_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionPersonaTelefonoMasivo_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_TELEFONO_RECHAZADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionPersonaTelefonoMasivo_UpdateTelefono.sql");
	
	private final String ID_TIPO_ENTIDAD = "TELEFONO";
	private final String USA_REGLAS_NEGOCIO = "N";
	
	// Clases de entrada
	public static class EntradaAutorizarCrearPersonaTelefonoMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<TelefonoPorAutorizar> p_Telefono;
		public String p_IdEstadoAutorizacion;
		
		public EntradaAutorizarCrearPersonaTelefonoMasivo() {
			this.p_Telefono = new ArrayList<>();
		}
	}
	
	public static class TelefonoPorAutorizar {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaSistema;
		public Long p_IdTelefono;
		
		public TelefonoPorAutorizar() {}
		
		public TelefonoPorAutorizar(Long idTelefono) {
			this.p_IdTelefono = idTelefono;
		}
	}
	
	// Clases de entrada para Rechazar
	public static class EntradaRechazaAutorizacionPersonaTelefonoMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<TelefonoPorAutorizar> p_Telefono;
		public String p_IdConcepto;
		
		public EntradaRechazaAutorizacionPersonaTelefonoMasivo() {
			this.p_Telefono = new ArrayList<>();
		}
	}
	
	// Clases de salida
	public static class SalidaAutorizarCrearPersonaTelefonoMasivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaAutorizarCrearPersonaTelefonoMasivo() {
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
	public static class TelefonoUpdate {
		public Long p_IdTelefono;
		public String p_IdEstadoAutorizacion;
		public String p_IdEstado;
		public String p_Borrado;
	}
	
	public static class LogGenerarContratos {
		public String CodigoFormulario;
		public String CampoBackend;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdUsuario;
		public String FechaSistema;
		public Long IdPrograma;
		public String Estado;
		public String Mensaje;
	}
	
	public static class SolicitudCambioData {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
		public Long IdPersona;
	}
	

	public SalidaAutorizarCrearPersonaTelefonoMasivo logica(Connection conn, EntradaAutorizarCrearPersonaTelefonoMasivo in) throws Exception {
		logger.info("INICIO::AutorizarCrearPersonaTelefonoMasivo");
		SalidaAutorizarCrearPersonaTelefonoMasivo salida = new SalidaAutorizarCrearPersonaTelefonoMasivo();
		SQL sql = new SQL();
		
		try {
			// Obtener fecha del sistema
			String FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(
				conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			// 1. Actualizar estado de autorización de los teléfonos
			logger.debug("UPDATE telefono IdEstadoAutorizacion");
			List<TelefonoUpdate> telefonosUpdate = in.p_Telefono.stream().map(tel -> {
				TelefonoUpdate update = new TelefonoUpdate();
				update.p_IdTelefono = tel.p_IdTelefono;
				update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_TELEFONO_ESTADO, telefonosUpdate, TelefonoUpdate.class);
			
			// 2. Insertar log para regeneración automática de documentos
			logger.debug("INSERT log generar contratos");
			List<TelefonoPorAutorizar> telefonosLog = new ArrayList<>();
			for (TelefonoPorAutorizar tel : in.p_Telefono) {
				TelefonoPorAutorizar logTel = new TelefonoPorAutorizar();
				logTel.p_IdTelefono = tel.p_IdTelefono;
				logTel.p_IdEmpresa = in.p_IdEmpresa;
				logTel.p_IdUsuario = in.p_IdUsuario;
				logTel.p_FechaSistema = FechaIngreso;
				telefonosLog.add(logTel);
			}
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, telefonosLog, TelefonoPorAutorizar.class);
			
		} catch (Exception ex) {
			logger.error("Error en AutorizarCrearPersonaTelefonoMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_TELEFONO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::AutorizarCrearPersonaTelefonoMasivo");
		return salida;
	}
	
	// ========== IMPLEMENTACIÓN DE BcsAutorizaciones ==========
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaTelefonoMasivo.autorizarEvento");
		
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		EntradaAutorizarCrearPersonaTelefonoMasivo entradaConfirmar = new EntradaAutorizarCrearPersonaTelefonoMasivo();
		
		for(Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING"; // Estado confirmado
			entradaConfirmar.p_Telefono.add(new TelefonoPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaAutorizarCrearPersonaTelefonoMasivo salidaConfirmacion = logica(conn, entradaConfirmar);
			
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
		
		logger.info("FIN::AutorizarCrearPersonaTelefonoMasivo.autorizarEvento");
		return salidaAutoriza;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaTelefonoMasivo.rechazarEvento");
		
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		SQL sql = new SQL();
		
		try {
			EntradaRechazaAutorizacionPersonaTelefonoMasivo entradaRechazar = new EntradaRechazaAutorizacionPersonaTelefonoMasivo();
			
			for(Evento evento : entrada.p_Lista) {
				entradaRechazar.p_IdEmpresa = evento.IdEmpresa;
				entradaRechazar.p_IdModulo = evento.IdModulo;
				entradaRechazar.p_IdUsuario = evento.IdUsuario;
				entradaRechazar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
				entradaRechazar.p_IdConcepto = evento.IdConcepto;
				entradaRechazar.p_Telefono.add(new TelefonoPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
			}
			
			SalidaAutorizarCrearPersonaTelefonoMasivo salidaRechazado = rechazarLogica(conn, entradaRechazar);
			
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
		
		logger.info("FIN::AutorizarCrearPersonaTelefonoMasivo.rechazarEvento");
		return salidaAutorizacion;
	}
	
	// ========== LÓGICA DE RECHAZO ==========
	
	public SalidaAutorizarCrearPersonaTelefonoMasivo rechazarLogica(Connection conn, EntradaRechazaAutorizacionPersonaTelefonoMasivo in) throws Exception {
		logger.info("INICIO::RechazaAutorizacionPersonaTelefonoMasivo");
		SalidaAutorizarCrearPersonaTelefonoMasivo salida = new SalidaAutorizarCrearPersonaTelefonoMasivo();
		SQL sql = new SQL();
		
		try {
			String v_IdEstadoAutorizacion = "RZD";
			
			// Validar existencia de solicitud para MODTEL
			if ("MODTEL".equals(in.p_IdConcepto)) {
				logger.debug("Validar existencia de solicitud");
				HashMap<String, Object> paramsValidar = new HashMap<>();
			paramsValidar.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD);
			
			List<SolicitudCambioData> solicitudes = new ArrayList<>();
			for (TelefonoPorAutorizar tel : in.p_Telefono) {
				paramsValidar.put("p_IdEntidadOrigen", String.valueOf(tel.p_IdTelefono));
				}
				
				if (solicitudes.isEmpty()) {
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
				for (SolicitudCambioData sol : solicitudes) {
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
			
			// Actualizar teléfonos rechazados
			logger.debug("UPDATE telefono rechazado");
			List<TelefonoUpdate> telefonosUpdate = in.p_Telefono.stream().map(tel -> {
				TelefonoUpdate update = new TelefonoUpdate();
				update.p_IdTelefono = tel.p_IdTelefono;
				update.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
				// Si es NUETEL, marcar como inactivo y borrado
				if ("NUETEL".equals(in.p_IdConcepto)) {
					update.p_IdEstado = "INA";
					update.p_Borrado = "S";
				} else {
					update.p_IdEstado = null; // Mantener estado actual
					update.p_Borrado = "N";
				}
				return update;
			}).collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_TELEFONO_RECHAZADO, telefonosUpdate, TelefonoUpdate.class);
			
		} catch (Exception ex) {
			logger.error("Error en RechazaAutorizacionPersonaTelefonoMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_RECHAZAR_TELEFONO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::RechazaAutorizacionPersonaTelefonoMasivo");
		return salida;
	}
}
