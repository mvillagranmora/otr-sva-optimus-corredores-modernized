package cl.bcs.socionegocio.cli;

import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para autorizar/rechazar creacion y modificacion de Declaraciones de Origen de Fondos.
 * Migracion de:
 * - ConfirmarCrearDeclaracionOrigenFondosMasivo.sql -> autorizarEvento()
 * - RechazaAutorizacionDeclaracionOrigenFondosMasivo.sql -> rechazarEvento()
 * 
 * Autor: Migracion Java
 * Fecha: Mayo 2026
 */
public class AutorizarCrearDeclaracionOrigenFondosMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearDeclaracionOrigenFondosMasivo.class);

	private final SQL sql = new SQL();

	// SQLs para operaciones
	private final String SQL_UPDATE_DECLARACION_ORIGEN_FONDOS = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/AutorizarCrearDeclaracionOrigenFondosMasivo_Update_DeclaracionOrigenFondos.sql");

	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/AutorizarCrearDeclaracionOrigenFondosMasivo_Insert_LogGenerarContratos.sql");

	private final String SQL_SELECT_CLIENTE_POR_DECLARACION = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/AutorizarCrearDeclaracionOrigenFondosMasivo_Select_ClientePorDeclaracion.sql");

	// Clases internas para mapeo de datos
	public static class DeclaracionOrigenFondos {
		public Long IdDeclaracionOrigenFondos;
		public String IdEstadoAutorizacion;
		public String IdEstado;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DeclaracionOrigenFondos that = (DeclaracionOrigenFondos) o;
			return IdDeclaracionOrigenFondos != null && IdDeclaracionOrigenFondos.equals(that.IdDeclaracionOrigenFondos);
		}

		@Override
		public int hashCode() {
			return IdDeclaracionOrigenFondos != null ? IdDeclaracionOrigenFondos.hashCode() : 0;
		}
	}

	public static class LogGenerarContratos {
		public Long Idlog;
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

	public static class ClienteDeclaracion {
		public Long IdCliente;
		public Long IdDeclaracionOrigenFondos;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDeclaracionOrigenFondosMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Preparar lista de declaraciones para actualizar con estado AUTORIZADO
			List<DeclaracionOrigenFondos> declaraciones = entrada.p_Lista.stream()
					.map(evento -> {
						DeclaracionOrigenFondos d = new DeclaracionOrigenFondos();
						d.IdDeclaracionOrigenFondos = Long.valueOf(evento.NumeroOperacionConcepto);
						d.IdEstadoAutorizacion = "AUT";
						d.IdEstado = null;  // Mantener estado actual
						return d;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_DECLARACION_ORIGEN_FONDOS, declaraciones, DeclaracionOrigenFondos.class);

			// Obtener clientes asociados a las declaraciones
			HashMap<String, Object> paramsCliente = new HashMap<>();
			List<Long> idsDeclaraciones = declaraciones.stream()
					.map(d -> d.IdDeclaracionOrigenFondos)
					.collect(Collectors.toList());
			paramsCliente.put("IdDeclaracionOrigenFondos", idsDeclaraciones);

			List<ClienteDeclaracion> clientesDeclaraciones = sql.query(conn, SQL_SELECT_CLIENTE_POR_DECLARACION, paramsCliente, ClienteDeclaracion.class);

			// Obtener fecha del sistema
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			String fechaSistemaStr = fechaSistema.substring(0, 10);

			// Obtener secuencias para LogGenerarContratos
			List<Long> secuencias = Utils.getSecuencia(
					conn,
					"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos",
					Long.valueOf(clientesDeclaraciones.size())
			);

			// Crear logs de generacion de contratos
			List<LogGenerarContratos> logsContratos = new java.util.ArrayList<>();
			for (int i = 0; i < clientesDeclaraciones.size(); i++) {
				ClienteDeclaracion cd = clientesDeclaraciones.get(i);
				LogGenerarContratos log = new LogGenerarContratos();
				log.Idlog = secuencias.get(i);
				log.CodigoFormulario = "DECLARACION_ORIGEN_FONDOS_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.IdCliente = cd.IdCliente;
				log.IdPersona = 0L;
				log.IdUsuario = entrada.p_IdUsuario;
				log.FechaSistema = fechaSistemaStr;
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Persona - ConfirmarCrearDeclaracionOrigenFondos.sql";
				logsContratos.add(log);
			}

			// Insertar logs en batch
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);

			// Agregar resultados
			for (DeclaracionOrigenFondos declaracion : declaraciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = declaracion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDeclaracionOrigenFondosMasivo.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDeclaracionOrigenFondosMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Preparar lista de declaraciones para actualizar con estado RECHAZADO
			List<DeclaracionOrigenFondos> declaraciones = entrada.p_Lista.stream()
					.map(evento -> {
						DeclaracionOrigenFondos d = new DeclaracionOrigenFondos();
						d.IdDeclaracionOrigenFondos = Long.valueOf(evento.NumeroOperacionConcepto);
						d.IdEstadoAutorizacion = "RZD";

						// Para eventos de creacion (NUEDOF), marcar como inactivo
						String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						if ("NUEDOF".equals(idTipoEvento)) {
							d.IdEstado = "INA";
						} else {
							d.IdEstado = null;  // Mantener estado actual para MODDOF
						}
						return d;
					})
					.distinct()
					.collect(Collectors.toList());

			// Procesar solicitudes de cambio para eventos de modificacion (MODDOF)
			List<EntidadOrigen> entidadesMODDOF = entrada.p_Lista.stream()
					.filter(evento -> {
						String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						return "MODDOF".equals(tipo);
					})
					.map(evento -> {
						EntidadOrigen eo = new EntidadOrigen();
						eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
						eo.IdTipoEntidad = "CLIENTEDOF";
						return eo;
					})
					.collect(Collectors.toList());

			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo si hay eventos de modificacion
			if (!entidadesMODDOF.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMODDOF;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";

				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_DECLARACION_ORIGEN_FONDOS, declaraciones, DeclaracionOrigenFondos.class);

			// Agregar resultados
			for (DeclaracionOrigenFondos declaracion : declaraciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = declaracion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDeclaracionOrigenFondosMasivo.rechazarEvento {}", fin - inicio);

		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarCrearDeclaracionOrigenFondosMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				// Test AUTORIZAR - evento NUEDOF (Nuevo)
				System.out.println("\n=== TEST AUTORIZAR (NUEDOF) ===");
				String jsonAutorizar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NUEDOF\"}]}";
				EntradaAutorizacion entradaAutorizar = Utils.getGsonBuilder().fromJson(jsonAutorizar, EntradaAutorizacion.class);

				AutorizarCrearDeclaracionOrigenFondosMasivo servicio = new AutorizarCrearDeclaracionOrigenFondosMasivo();
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entradaAutorizar);
				
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento NUEDOF (Nuevo)
				System.out.println("\n=== TEST RECHAZAR (NUEDOF) ===");
				String jsonRechazarNuevo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NUEDOF\"}]}";
				EntradaAutorizacion entradaRechazarNuevo = Utils.getGsonBuilder().fromJson(jsonRechazarNuevo, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarNuevo = servicio.rechazarEvento(conn, entradaRechazarNuevo);
				System.out.println("Errores: " + salidaRechazarNuevo.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarNuevo.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento MODDOF (Modificar)
				System.out.println("\n=== TEST RECHAZAR (MODDOF) ===");
				String jsonRechazarMod = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":124,\"NumeroOperacionConcepto\":\"789\",\"IdTipoEvento\":\"MODDOF\"}]}";
				EntradaAutorizacion entradaRechazarMod = Utils.getGsonBuilder().fromJson(jsonRechazarMod, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarMod = servicio.rechazarEvento(conn, entradaRechazarMod);
				System.out.println("Errores: " + salidaRechazarMod.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarMod.P_OUT_RESULTADO.size());
				conn.rollback();

			} catch (Exception e) {
				e.printStackTrace();
				conn.rollback();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n== FIN TEST ==");
	}
}
