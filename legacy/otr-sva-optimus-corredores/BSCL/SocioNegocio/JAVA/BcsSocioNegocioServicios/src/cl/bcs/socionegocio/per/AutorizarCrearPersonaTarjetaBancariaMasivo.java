package cl.bcs.socionegocio.per;

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
 * Servicio para autorizar/rechazar creación y modificación de tarjetas bancarias.
 * Migración de:
 * - ConfirmarCrearPersonaTarjetaBancariaMasivo.sql → autorizarEvento()
 * - RechazaAutorizacionPersonaTarjetaBancaria.sql → rechazarEvento()
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class AutorizarCrearPersonaTarjetaBancariaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearPersonaTarjetaBancariaMasivo.class);

	private final SQL sql = new SQL();

	// SQLs para operaciones
	private final String SQL_UPDATE_TARJETA_BANCARIA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaTarjetaBancariaMasivo_Update_TarjetaBancaria.sql");

	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaTarjetaBancariaMasivo_Insert_LogGenerarContratos.sql");

	private final String SQL_SELECT_PERSONA_POR_TARJETA = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaTarjetaBancariaMasivo_Select_PersonaPorTarjeta.sql");

	// Clases internas para mapeo de datos
	public static class TarjetaBancaria {
		public Long IdTarjetaBancaria;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		public String Borrado;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TarjetaBancaria that = (TarjetaBancaria) o;
			return IdTarjetaBancaria != null && IdTarjetaBancaria.equals(that.IdTarjetaBancaria);
		}

		@Override
		public int hashCode() {
			return IdTarjetaBancaria != null ? IdTarjetaBancaria.hashCode() : 0;
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

	public static class PersonaTarjeta {
		public Long IdPersona;
		public Long IdTarjetaBancaria;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaTarjetaBancariaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Preparar lista de tarjetas para actualizar con estado AUTORIZADO
			List<TarjetaBancaria> tarjetas = entrada.p_Lista.stream()
					.map(evento -> {
						TarjetaBancaria t = new TarjetaBancaria();
						t.IdTarjetaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
						t.IdEstadoAutorizacion = "AUT";
						t.IdEstado = null;  // Mantener estado actual
						t.Borrado = null;   // Mantener valor actual
						return t;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_TARJETA_BANCARIA, tarjetas, TarjetaBancaria.class);

			// Obtener personas asociadas a las tarjetas
			HashMap<String, Object> paramsPersona = new HashMap<>();
			List<Long> idsTarjetas = tarjetas.stream()
					.map(t -> t.IdTarjetaBancaria)
					.collect(Collectors.toList());
			paramsPersona.put("IdTarjetaBancaria", idsTarjetas);

			List<PersonaTarjeta> personasTarjetas = sql.query(conn, SQL_SELECT_PERSONA_POR_TARJETA, paramsPersona, PersonaTarjeta.class);

			// Obtener fecha del sistema
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			String fechaSistemaStr = fechaSistema.substring(0, 10);

			// Obtener secuencias para LogGenerarContratos
			List<Long> secuencias = Utils.getSecuencia(
					conn,
					"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos",
					Long.valueOf(personasTarjetas.size())
			);

			// Crear logs de generación de contratos
			List<LogGenerarContratos> logsContratos = new java.util.ArrayList<>();
			for (int i = 0; i < personasTarjetas.size(); i++) {
				PersonaTarjeta pt = personasTarjetas.get(i);
				LogGenerarContratos log = new LogGenerarContratos();
				log.Idlog = secuencias.get(i);
				log.CodigoFormulario = "TARJETA_BANCARIA_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.IdCliente = 0L;
				log.IdPersona = pt.IdPersona;
				log.IdUsuario = entrada.p_IdUsuario;
				log.FechaSistema = fechaSistemaStr;
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Persona - ConfirmarCrearPersonaTarjetaBancariaMasivo.sql";
				logsContratos.add(log);
			}

			// Insertar logs en batch
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);

			// Agregar resultados
			for (TarjetaBancaria tarjeta : tarjetas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = tarjeta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearPersonaTarjetaBancariaMasivo.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaTarjetaBancariaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Preparar lista de tarjetas para actualizar con estado RECHAZADO
			List<TarjetaBancaria> tarjetas = entrada.p_Lista.stream()
					.map(evento -> {
						TarjetaBancaria t = new TarjetaBancaria();
						t.IdTarjetaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
						t.IdEstadoAutorizacion = "RZD";

						// Para eventos de creación (NUETJB), marcar como inactivo
						String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						if ("NUETJB".equals(idTipoEvento)) {
							t.IdEstado = "INA";
							t.Borrado = null;  // No se marca Borrado según SP original
						} else {
							t.IdEstado = null;  // Mantener estado actual para MODTJB/CAETJB
							t.Borrado = null;
						}
						return t;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_TARJETA_BANCARIA, tarjetas, TarjetaBancaria.class);

			// Procesar solicitudes de cambio para eventos de modificación/cambio de estado
			List<EntidadOrigen> entidadesMODTJB = entrada.p_Lista.stream()
					.filter(evento -> {
						String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						return "MODTJB".equals(tipo) || "CAETJB".equals(tipo);
					})
					.map(evento -> {
						EntidadOrigen eo = new EntidadOrigen();
						eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
						// CAETJB usa tipo de entidad diferente
						String tipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						if ("CAETJB".equals(tipoEvento)) {
							eo.IdTipoEntidad = "TARJETABANCARIA-ESTADO";
						} else {
							eo.IdTipoEntidad = "TARJETABANCARIA";
						}
						return eo;
					})
					.collect(Collectors.toList());

			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo si hay eventos de modificación
			if (!entidadesMODTJB.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMODTJB;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";

				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}

			// Agregar resultados
			for (TarjetaBancaria tarjeta : tarjetas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = tarjeta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearPersonaTarjetaBancariaMasivo.rechazarEvento {}", fin - inicio);

		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarCrearPersonaTarjetaBancariaMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				// Test AUTORIZAR - evento NUETJB (Nuevo)
				System.out.println("\n=== TEST AUTORIZAR (NUETJB) ===");
				String jsonAutorizar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NUETJB\"}]}";
				EntradaAutorizacion entradaAutorizar = Utils.getGsonBuilder().fromJson(jsonAutorizar, EntradaAutorizacion.class);

				AutorizarCrearPersonaTarjetaBancariaMasivo servicio = new AutorizarCrearPersonaTarjetaBancariaMasivo();
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entradaAutorizar);
				
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento NUETJB (Nuevo)
				System.out.println("\n=== TEST RECHAZAR (NUETJB) ===");
				String jsonRechazarNuevo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NUETJB\"}]}";
				EntradaAutorizacion entradaRechazarNuevo = Utils.getGsonBuilder().fromJson(jsonRechazarNuevo, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarNuevo = servicio.rechazarEvento(conn, entradaRechazarNuevo);
				System.out.println("Errores: " + salidaRechazarNuevo.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarNuevo.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento MODTJB (Modificar)
				System.out.println("\n=== TEST RECHAZAR (MODTJB) ===");
				String jsonRechazarMod = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":124,\"NumeroOperacionConcepto\":\"789\",\"IdTipoEvento\":\"MODTJB\"}]}";
				EntradaAutorizacion entradaRechazarMod = Utils.getGsonBuilder().fromJson(jsonRechazarMod, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarMod = servicio.rechazarEvento(conn, entradaRechazarMod);
				System.out.println("Errores: " + salidaRechazarMod.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarMod.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento CAETJB (Cambiar Estado)
				System.out.println("\n=== TEST RECHAZAR (CAETJB) ===");
				String jsonRechazarCae = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":125,\"NumeroOperacionConcepto\":\"101\",\"IdTipoEvento\":\"CAETJB\"}]}";
				EntradaAutorizacion entradaRechazarCae = Utils.getGsonBuilder().fromJson(jsonRechazarCae, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarCae = servicio.rechazarEvento(conn, entradaRechazarCae);
				System.out.println("Errores: " + salidaRechazarCae.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarCae.P_OUT_RESULTADO.size());
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
