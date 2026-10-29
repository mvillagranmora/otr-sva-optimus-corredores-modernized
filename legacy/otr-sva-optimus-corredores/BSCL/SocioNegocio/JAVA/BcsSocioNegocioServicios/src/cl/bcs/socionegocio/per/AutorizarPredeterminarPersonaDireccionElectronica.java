package cl.bcs.socionegocio.per;

import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarPredeterminarPersonaDireccionElectronica implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarPredeterminarPersonaDireccionElectronica.class);

	private final SQL sql = new SQL();

	// SQLs para Autorizar (Confirmar)
	private final String SQL_SELECT_ID_PERSONA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_IdPersona.sql");
	private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_DatosPersona.sql");
	private final String SQL_SELECT_DIRECCION_ANTERIOR = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_DireccionAnterior.sql");
	private final String SQL_SELECT_DIRECCION_NUEVA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_DireccionNueva.sql");
	private final String SQL_UPDATE_DESMARCAR_DIRECCIONES = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Update_DesmarcarDirecciones.sql");
	private final String SQL_UPDATE_MARCAR_DIRECCION_PREDETERMINADA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Update_MarcarDireccionPredeterminada.sql");
	private final String SQL_UPDATE_DIRECCION_PRINCIPAL_PERSONA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Update_DireccionPrincipalPersona.sql");
	private final String SQL_SELECT_INDICADORES_EXISTENTES = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_IndicadoresExistentes.sql");
	private final String SQL_UPDATE_INDICADORES = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Update_Indicadores.sql");
	private final String SQL_SELECT_DATOS_INDICADORES = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Select_DatosIndicadores.sql");
	private final String SQL_INSERT_INDICADORES = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarPredeterminarPersonaDireccionElectronica_Insert_Indicadores.sql");

	// Clases internas para mapeo de datos
	public static class DireccionElectronicaPersona {
		public Long IdPersona;
		public Long IdDireccionElectronica;
	}

	public static class DesmarcarDirecciones {
		public Long IdPersona;
	}

	public static class DireccionPrincipal {
		public Long IdPersona;
		public Long IdDireccionElectronica;
	}

	public static class DatosPersona {
		public String Nombres;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
	}

	public static class DireccionTexto {
		public String Direccion;
	}

	public static class Contador {
		public Long Contador;
	}

	public static class ActualizarIndicadores {
		public Long IdPersona;
		public Long IdDireccionElectronica;
	}

	public static class DatosIndicador {
		public Long IdPortafolio;
		public String IdTipoCliente;
		public Long IdTipoIndicador;
		public Long IdDireccionElectronica;
	}

	public static class InsertarIndicador {
		public Long IdIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public String ModeloCliente;
		public Long IdEmpresa;
		public String ValorIndicador;
		public String FechaIngreso;
		public String FechaCreacion;
		public String FechaModificacion;
		public String IdEstadoAutorizacion;
		public String FechaVencimiento;
		public Long IdCarga;
	}

	// Clase de contexto compartido
	private ContextoProceso contexto = new ContextoProceso();

	private static class ContextoProceso {
		public Connection Connection;
		public EntradaAutorizacion Entrada;
		public Boolean EsAutorizacion;
		public List<Long> DireccionElectronicaIds;

		public ContextoProceso() {
			Entrada = new EntradaAutorizacion();
			EsAutorizacion = false;
			DireccionElectronicaIds = new ArrayList<>();
		}
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarPredeterminarPersonaDireccionElectronica.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "AUT");

			List<DireccionElectronicaPersona> direcciones = new ArrayList<>();
			for (Long idDireccion : contexto.DireccionElectronicaIds) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdDireccionElectronica", idDireccion);

				List<DireccionElectronicaPersona> personaList = sql.query(conn, SQL_SELECT_ID_PERSONA, params,
						DireccionElectronicaPersona.class);
				if (personaList.isEmpty()) {
					continue;
				}

				DireccionElectronicaPersona direccion = personaList.get(0);
				direcciones.add(direccion);
			}

			// Desmarcar todas las direcciones de las personas afectadas
			List<DesmarcarDirecciones> desmarcarList = direcciones.stream()
					.map(dir -> {
						DesmarcarDirecciones d = new DesmarcarDirecciones();
						d.IdPersona = dir.IdPersona;
						return d;
					})
					.distinct()
					.collect(Collectors.toList());

			if (!desmarcarList.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_DESMARCAR_DIRECCIONES, desmarcarList, DesmarcarDirecciones.class);
			}

			// Marcar las direcciones como predeterminadas
			if (!direcciones.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_MARCAR_DIRECCION_PREDETERMINADA, direcciones,
						DireccionElectronicaPersona.class);
			}

			// Actualizar dirección principal en Persona
			List<DireccionPrincipal> principalList = direcciones.stream()
					.map(dir -> {
						DireccionPrincipal dp = new DireccionPrincipal();
						dp.IdPersona = dir.IdPersona;
						dp.IdDireccionElectronica = dir.IdDireccionElectronica;
						return dp;
					})
					.collect(Collectors.toList());

			if (!principalList.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_DIRECCION_PRINCIPAL_PERSONA, principalList, DireccionPrincipal.class);
			}

			// Actualizar/Insertar Indicadores
			for (DireccionElectronicaPersona dir : direcciones) {
				HashMap<String, Object> paramsInd = new HashMap<>();
				paramsInd.put("IdPersona", dir.IdPersona);

				List<Contador> countList = sql.query(conn, SQL_SELECT_INDICADORES_EXISTENTES, paramsInd,
						Contador.class);
				Long cantidadIndicadores = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador
						: 0L;

				if (cantidadIndicadores > 0) {
					// Actualizar indicadores existentes
					ActualizarIndicadores actInd = new ActualizarIndicadores();
					actInd.IdPersona = dir.IdPersona;
					actInd.IdDireccionElectronica = dir.IdDireccionElectronica;

					List<ActualizarIndicadores> actList = new ArrayList<>();
					actList.add(actInd);
					sql.batch(conn, SQL_UPDATE_INDICADORES, actList, ActualizarIndicadores.class);
				} else {
					// Insertar nuevos indicadores
					try {
						String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(),
								entrada.p_IdEmpresa.intValue());

						// Obtener datos para crear indicadores
						HashMap<String, Object> paramsDatos = new HashMap<>();
						paramsDatos.put("IdPersona", dir.IdPersona);

						List<DatosIndicador> datosIndList = sql.query(conn, SQL_SELECT_DATOS_INDICADORES,
								paramsDatos, DatosIndicador.class);

						if (!datosIndList.isEmpty()) {
							// Obtener secuencias para los indicadores
							List<Long> secuencias = Utils.getSecuencia(conn,
									"BSCL.Plataforma.BD.Secuencias::Indicador", (long) datosIndList.size());

							List<InsertarIndicador> insertarList = new ArrayList<>();
							for (int i = 0; i < datosIndList.size(); i++) {
								DatosIndicador dato = datosIndList.get(i);
								InsertarIndicador ins = new InsertarIndicador();
								ins.IdIndicador = secuencias.get(i);
								ins.IdEntidad = dato.IdPortafolio;
								ins.IdTipoEntidad = "PORTAFOLIO";
								ins.IdTipoIndicador = dato.IdTipoIndicador;
								ins.ModeloCliente = dato.IdTipoCliente;
								ins.IdEmpresa = entrada.p_IdEmpresa;
								ins.ValorIndicador = String.valueOf(dato.IdDireccionElectronica);
								ins.FechaIngreso = fechaSistema;
								ins.FechaCreacion = fechaSistema;
								ins.FechaModificacion = fechaSistema;
								ins.IdEstadoAutorizacion = "ING";
								ins.FechaVencimiento = "0001-01-01 00:00:00.000";
								ins.IdCarga = 0L;
								insertarList.add(ins);
							}

							sql.batch(conn, SQL_INSERT_INDICADORES, insertarList, InsertarIndicador.class);
						}
					} catch (Exception insEx) {
						logger.warn("Error al insertar indicadores para persona {}: {}", dir.IdPersona,
								insEx.getMessage());
					}
				}
			}

			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo
			List<EntidadOrigen> entidadesOrigen = direcciones.stream()
					.map(dir -> {
						EntidadOrigen eo = new EntidadOrigen();
						eo.IdEntidadOrigen = String.valueOf(dir.IdDireccionElectronica);
						eo.IdTipoEntidad = "DIRECCIONELECTRONICA-PREDETERMINAR";
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
				String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(),
						entrada.p_IdEmpresa.intValue());
				String fechaSistemaStr = fechaSistema.substring(0, 10);

				for (DireccionElectronicaPersona dir : direcciones) {
					try {
						// Obtener datos de persona
						HashMap<String, Object> paramsPersona = new HashMap<>();
						paramsPersona.put("IdPersona", dir.IdPersona);

						List<DatosPersona> datosPersonaList = sql.query(conn, SQL_SELECT_DATOS_PERSONA, paramsPersona,
								DatosPersona.class);
						if (datosPersonaList.isEmpty()) {
							continue;
						}
						DatosPersona datosPersona = datosPersonaList.get(0);

						// Obtener dirección anterior
						HashMap<String, Object> paramsDirAnt = new HashMap<>();
						paramsDirAnt.put("IdPersona", dir.IdPersona);

						List<DireccionTexto> dirAntList = sql.query(conn, SQL_SELECT_DIRECCION_ANTERIOR,
								paramsDirAnt, DireccionTexto.class);
						String direccionAnterior = (dirAntList != null && !dirAntList.isEmpty())
								? dirAntList.get(0).Direccion
								: "";

						// Obtener dirección nueva
						HashMap<String, Object> paramsDirNueva = new HashMap<>();
						paramsDirNueva.put("IdDireccionElectronica", dir.IdDireccionElectronica);

						List<DireccionTexto> dirNuevaList = sql.query(conn, SQL_SELECT_DIRECCION_NUEVA,
								paramsDirNueva, DireccionTexto.class);
						String direccionNueva = (dirNuevaList != null && !dirNuevaList.isEmpty())
								? dirNuevaList.get(0).Direccion
								: "";

						// Crear log de cabecera
						AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
						entradaLog.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaLog.p_IdUsuario = entrada.p_IdUsuario;
						entradaLog.p_IdModulo = "SOC";

						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = dir.IdDireccionElectronica;
						log.IdTipoLog = "MODIFICACION";
						log.IdModulo = "SOC";
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistemaStr;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdPersona = dir.IdPersona;
						log.NombreCliente = datosPersona.Nombres != null ? datosPersona.Nombres : "";
						log.IdDocumentoIdentidad = datosPersona.DocumentoIdentidad != null
								? datosPersona.DocumentoIdentidad
								: "";
						log.IdTipoDocumentoIdentidad = datosPersona.IdTipoDocumentoIdentidad != null
								? datosPersona.IdTipoDocumentoIdentidad
								: "";
						log.IdTipoRegistro = "DIRECCIONELECTRONICA-PREDETERMINAR";
						log.IdRegistro = String.valueOf(dir.IdDireccionElectronica);
						log.EstadoProceso = "AUT";
						log.NombreProceso = "Confirmar Predeterminar Direccion Electronica";
						entradaLog.p_Log.add(log);

						// Crear detalle del log
						AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
						detalle.IdCabeceraLog = dir.IdDireccionElectronica;
						detalle.SubRegistro = "DIRECCIONELECTRONICA";
						detalle.CampoRegistro = "Predeterminado";
						detalle.ValorAntiguo = direccionAnterior;
						detalle.ValorNuevo = direccionNueva;
						entradaLog.p_LogDetalle.add(detalle);

						// Llamar al servicio de logs
						AgregarLog agregarLogService = new AgregarLog();
						agregarLogService.logica(conn, entradaLog);

					} catch (Exception logEx) {
						logger.warn("Error al preparar log de auditoría para dirección {}: {}",
								dir.IdDireccionElectronica, logEx.getMessage());
					}
				}

			} catch (Exception logEx) {
				logger.warn("Error al generar logs de auditoría: {}", logEx.getMessage());
			}

			// Agregar resultados
			for (DireccionElectronicaPersona dir : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = dir;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarPredeterminarPersonaDireccionElectronica.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarPredeterminarPersonaDireccionElectronica.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "RZD");

			// Obtener entidades origen
			List<EntidadOrigen> entidadesOrigen = contexto.DireccionElectronicaIds.stream()
					.map(idDireccion -> {
						EntidadOrigen eo = new EntidadOrigen();
						eo.IdEntidadOrigen = String.valueOf(idDireccion);
						eo.IdTipoEntidad = "DIRECCIONELECTRONICA-PREDETERMINAR";
						return eo;
					})
					.collect(Collectors.toList());

			// Modificar estado de solicitud a RECHAZADO
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
			for (Long idDireccion : contexto.DireccionElectronicaIds) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				DireccionElectronicaPersona dir = new DireccionElectronicaPersona();
				dir.IdDireccionElectronica = idDireccion;
				resultado.evento = dir;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarPredeterminarPersonaDireccionElectronica.rechazarEvento {}", fin - inicio);

		return salida;
	}

	private ContextoProceso obtenerContexto(Connection conn, EntradaAutorizacion entrada, String accion) {
		logger.debug("INICIO::obtenerContexto");
		long inicio = System.currentTimeMillis();

		ContextoProceso contexto = new ContextoProceso();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.EsAutorizacion = "AUT".equals(accion);

		// Extraer IDs desde entrada
		contexto.DireccionElectronicaIds = entrada.p_Lista.stream()
				.map(u -> Long.valueOf(u.NumeroOperacionConcepto))
				.distinct()
				.collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::obtenerContexto {}", fin - inicio);

		return contexto;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarPredeterminarPersonaDireccionElectronica ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"PRDDEA\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizarPredeterminarPersonaDireccionElectronica servicio = new AutorizarPredeterminarPersonaDireccionElectronica();

				// Probar AUTORIZAR
				System.out.println("=== Test Autorizar ===");
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Probar RECHAZAR
				System.out.println("=== Test Rechazar ===");
				SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
				System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
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
