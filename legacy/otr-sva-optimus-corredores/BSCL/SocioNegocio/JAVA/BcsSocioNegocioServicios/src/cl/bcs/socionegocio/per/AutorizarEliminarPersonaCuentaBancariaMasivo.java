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
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarEliminarPersonaCuentaBancariaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarEliminarPersonaCuentaBancariaMasivo.class);
	
	// Archivos SQL - Confirmar
	private final String SQL_SELECT_SOLICITUDES_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaCuentaBancariaMasivo_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_BORRADO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaCuentaBancariaMasivo_UpdateBorrado.sql");
	private final String SQL_UPDATE_PREFERENCIA_PAGO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaCuentaBancariaMasivo_DeletePrefPago.sql");
	private final String SQL_SELECT_PERSONA_CUENTA_BANCARIA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaCuentaBancaria_SelectPersonaCuentaBancaria.sql");
	
	private final String ID_TIPO_ENTIDAD = "CUENTABANCARIA";
	private final String ID_TIPO_ENTIDAD_BORRAR = "CUENTABANCARIA-BORRAR";
	private final String USA_REGLAS_NEGOCIO = "N";
	
	// Clases de entrada
	public static class EntradaAutorizarEliminarPersonaCuentaBancariaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<CuentaBancariaPorAutorizar> p_CuentaBancaria;
		public String p_IdEstadoAutorizacion;
		
		public EntradaAutorizarEliminarPersonaCuentaBancariaMasivo() {
			this.p_CuentaBancaria = new ArrayList<>();
		}
	}
	
	public static class CuentaBancariaPorAutorizar {
		public Long p_IdCuentaBancaria;
		
		public CuentaBancariaPorAutorizar() {}
		
		public CuentaBancariaPorAutorizar(Long idCuentaBancaria) {
			this.p_IdCuentaBancaria = idCuentaBancaria;
		}
	}
	
	// Clases de entrada para Rechazar
	public static class EntradaRechazaAutorizacionPersonaCuentaBancariaMasivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<CuentaBancariaPorAutorizar> p_CuentaBancaria;
		public String p_IdConcepto;
		
		public EntradaRechazaAutorizacionPersonaCuentaBancariaMasivo() {
			this.p_CuentaBancaria = new ArrayList<>();
		}
	}
	
	// Clases de salida
	public static class SalidaAutorizarEliminarPersonaCuentaBancariaMasivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaAutorizarEliminarPersonaCuentaBancariaMasivo() {
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
	public static class CuentaBancariaUpdateBorrado {
		public Long p_IdCuentaBancaria;
		public String p_Borrado;
	}
	
	public static class SolicitudCambioData {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class PersonaCuentaBancaria {
		public Long IdCuentaBancaria;
		public Long IdPersona;
		public String Borrado;
		public String NumeroCuentaBancaria;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
	}
	
	// ========== LÓGICA DE CONFIRMACIÓN ==========
	
	public SalidaAutorizarEliminarPersonaCuentaBancariaMasivo logica(Connection conn, EntradaAutorizarEliminarPersonaCuentaBancariaMasivo in) throws Exception {
		logger.info("INICIO::AutorizarEliminarPersonaCuentaBancariaMasivo");
		SalidaAutorizarEliminarPersonaCuentaBancariaMasivo salida = new SalidaAutorizarEliminarPersonaCuentaBancariaMasivo();
		SQL sql = new SQL();
		
		try {
			// 1. Validar existencia de solicitud PEN (CRÍTICO)
			logger.debug("Validar existencia de solicitud PEN");
			List<SolicitudCambioData> solicitudesTotal = new ArrayList<>();
			HashMap<String, Object> paramsValidar = new HashMap<>();
			paramsValidar.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD_BORRAR);
			
			for (CuentaBancariaPorAutorizar cuenta : in.p_CuentaBancaria) {
				paramsValidar.put("p_IdEntidadOrigen", String.valueOf(cuenta.p_IdCuentaBancaria));
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
			
			// 2. Actualizar campo Borrado - solo para solicitudes con CampoModificado='Borrado'
			logger.debug("UPDATE cuenta bancaria Borrado");
			
			// Filtrar solicitudes donde CampoModificado = 'Borrado' y crear actualizaciones
			List<CuentaBancariaUpdateBorrado> cuentasUpdate = solicitudesTotal.stream()
				.filter(sol -> "Borrado".equals(sol.CampoModificado))
				.map(sol -> {
					CuentaBancariaUpdateBorrado update = new CuentaBancariaUpdateBorrado();
					update.p_IdCuentaBancaria = Long.parseLong(sol.IdEntidadOrigen);
					update.p_Borrado = sol.ValorNuevo;
					return update;
				}).collect(Collectors.toList());
			
			if (!cuentasUpdate.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_BORRADO, cuentasUpdate, CuentaBancariaUpdateBorrado.class);

				// 2b. Desactivar preferencias de pago asociadas a las cuentas eliminadas (paridad con ConfirmarEliminarPersonaCuentaBancaria.sql)
				logger.debug("UPDATE preferencia pago portafolio a INA");
				List<EntidadPersonaEliminar> cuentasParaDesactivarPreferencia = cuentasUpdate.stream().map(cuenta -> {
					EntidadPersonaEliminar entidad = new EntidadPersonaEliminar();
					entidad.IdEntidadPersonaEliminar = cuenta.p_IdCuentaBancaria;
					return entidad;
				}).collect(Collectors.toList());
				sql.batch(conn, SQL_UPDATE_PREFERENCIA_PAGO, cuentasParaDesactivarPreferencia, EntidadPersonaEliminar.class);
			}

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

			// 4. Registrar auditoria (paridad con ConfirmarEliminarPersonaCuentaBancaria.sql)
			// Aislado en su propio try/catch: un fallo de auditoria no debe reportar como fallida
			// una aprobacion que ya actualizo la cuenta y cerro la solicitud correctamente.
			try {
				registrarLog(conn, sql, in, solicitudesTotal);
			} catch (Exception exLog) {
				logger.error("Error registrando auditoria en AutorizarEliminarPersonaCuentaBancariaMasivo: " + exLog.getMessage(), exLog);
			}

		} catch (Exception ex) {
			logger.error("Error en AutorizarEliminarPersonaCuentaBancariaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_CUENTA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("FIN::AutorizarEliminarPersonaCuentaBancariaMasivo");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaAutorizarEliminarPersonaCuentaBancariaMasivo in, List<SolicitudCambioData> solicitudesTotal) throws Exception {
		logger.debug("INICIO::AutorizarEliminarPersonaCuentaBancariaMasivo.registrarLog");

		List<Long> idsCuentaBancaria = solicitudesTotal.stream()
				.map(sol -> Long.parseLong(sol.IdEntidadOrigen))
				.distinct()
				.collect(Collectors.toList());
		if (idsCuentaBancaria.isEmpty()) {
			return;
		}

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdCuentaBancaria", idsCuentaBancaria);
		List<PersonaCuentaBancaria> datosPersonaCuenta = sql.query(conn, SQL_SELECT_PERSONA_CUENTA_BANCARIA, params, PersonaCuentaBancaria.class);
		if (datosPersonaCuenta.isEmpty()) {
			logger.debug("FIN::AutorizarEliminarPersonaCuentaBancariaMasivo.registrarLog - sin datos de cuenta/persona");
			return;
		}

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		InAgregarLog entrada = new InAgregarLog();

		for (PersonaCuentaBancaria datos : datosPersonaCuenta) {
			InAgregarLog.Log log = new InAgregarLog.Log();
			// IdCabeceraLog = IdCuentaBancaria (no IdPersona): AgregarLog deduplica cabeceras por
			// (IdCabeceraLog, IdTipoLog, IdTipoRegistro), y si dos cuentas del mismo cliente se
			// autorizan en el mismo lote, usar IdPersona colapsaria la segunda cabecera perdiendo su IdRegistro.
			log.IdCabeceraLog = datos.IdCuentaBancaria;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "SOC";
			log.IdTipoRegistro = ID_TIPO_ENTIDAD_BORRAR;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdPersona = datos.IdPersona;
			log.IdRegistro = String.valueOf(datos.IdCuentaBancaria);
			log.NombreProceso = "Confirmar Cambio de Estado";
			log.EstadoProceso = in.p_IdEstadoAutorizacion;
			entrada.p_Log.add(log);

			String idEntidadOrigenCuenta = String.valueOf(datos.IdCuentaBancaria);
			List<SolicitudCambioData> solicitudesCuenta = solicitudesTotal.stream()
					.filter(sol -> idEntidadOrigenCuenta.equals(sol.IdEntidadOrigen))
					.collect(Collectors.toList());

			for (SolicitudCambioData sol : solicitudesCuenta) {
				boolean esBorrado = "S".equals(sol.ValorNuevo);
				String valorAntiguo = esBorrado ? "Numero Cuenta Bancaria " + datos.NumeroCuentaBancaria : sol.ValorAntiguo;
				String valorNuevo = esBorrado ? "" : sol.ValorNuevo;
				entrada.agregarDetalleLogBasico(datos.IdCuentaBancaria, 0L, sol.CampoModificado, valorAntiguo, valorNuevo, sol.IdTipoEntidad);
			}
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::AutorizarEliminarPersonaCuentaBancariaMasivo.registrarLog");
	}
	
	// ========== IMPLEMENTACIÓN DE BcsAutorizaciones ==========
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarEliminarPersonaCuentaBancariaMasivo.autorizarEvento");
		
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		EntradaAutorizarEliminarPersonaCuentaBancariaMasivo entradaConfirmar = new EntradaAutorizarEliminarPersonaCuentaBancariaMasivo();
		
		for(Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING"; // Estado ingresado (autorizado)
			entradaConfirmar.p_CuentaBancaria.add(new CuentaBancariaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaAutorizarEliminarPersonaCuentaBancariaMasivo salidaConfirmacion = logica(conn, entradaConfirmar);
			
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
		
		logger.info("FIN::AutorizarEliminarPersonaCuentaBancariaMasivo.autorizarEvento");
		return salidaAutoriza;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarEliminarPersonaCuentaBancariaMasivo.rechazarEvento");
		
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		
		try {
			EntradaRechazaAutorizacionPersonaCuentaBancariaMasivo entradaRechazar = new EntradaRechazaAutorizacionPersonaCuentaBancariaMasivo();
			
			for(Evento evento : entrada.p_Lista) {
				entradaRechazar.p_IdEmpresa = evento.IdEmpresa;
				entradaRechazar.p_IdModulo = evento.IdModulo;
				entradaRechazar.p_IdUsuario = evento.IdUsuario;
				entradaRechazar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
				entradaRechazar.p_IdConcepto = evento.IdConcepto;
				entradaRechazar.p_CuentaBancaria.add(new CuentaBancariaPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
			}
			
			SalidaAutorizarEliminarPersonaCuentaBancariaMasivo salidaRechazado = rechazarLogica(conn, entradaRechazar);
			
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
		
		logger.info("FIN::AutorizarEliminarPersonaCuentaBancariaMasivo.rechazarEvento");
		return salidaAutorizacion;
	}
	
	// ========== LÓGICA DE RECHAZO ==========
	
	public SalidaAutorizarEliminarPersonaCuentaBancariaMasivo rechazarLogica(Connection conn, EntradaRechazaAutorizacionPersonaCuentaBancariaMasivo in) throws Exception {
		logger.info("INICIO::RechazaAutorizacionPersonaCuentaBancariaMasivo");
		SalidaAutorizarEliminarPersonaCuentaBancariaMasivo salida = new SalidaAutorizarEliminarPersonaCuentaBancariaMasivo();
		SQL sql = new SQL();
		
		try {
			final String v_IdEstadoAutorizacion = "RZD";
			
			// Validar existencia de solicitud PEN
			logger.debug("Validar existencia de solicitud");
			
			List<SolicitudCambioData> solicitudesTotal = new ArrayList<>();
			HashMap<String, Object> paramsValidar = new HashMap<>();
			paramsValidar.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD_BORRAR);
			
			for (CuentaBancariaPorAutorizar cuenta : in.p_CuentaBancaria) {
				paramsValidar.put("p_IdEntidadOrigen", String.valueOf(cuenta.p_IdCuentaBancaria));
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
			
			// Modificar estado de solicitud de cambio a RZD (rechazado)
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
			
			// NO actualizar cuentas bancarias en rechazo de eliminación
			// El SP original NO actualiza ningún campo, solo cierra la solicitud
			logger.debug("Rechazar eliminación - sin actualización de entidad");
			
		} catch (Exception ex) {
			logger.error("Error en RechazaAutorizacionPersonaCuentaBancariaMasivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_RECHAZAR_CUENTA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}
		
		logger.info("FIN::RechazaAutorizacionPersonaCuentaBancariaMasivo");
		return salida;
	}
	
	public static void main(String[] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarEliminarPersonaCuentaBancariaMasivo e = new AutorizarEliminarPersonaCuentaBancariaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = 0L;
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "10056";
			evento.IdEmpresa = entrada.p_IdEmpresa;
			evento.IdModulo = entrada.p_IdModulo;
			evento.IdUsuario = entrada.p_IdUsuario;
			evento.IdConcepto = "BORCTB";
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
