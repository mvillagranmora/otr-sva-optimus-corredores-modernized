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
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para autorizar/rechazar modificaciones a grupos de direcciones electrónicas.
 * Migración de:
 * - ConfirmarCrearPersonaGrupoDireccionElectronicaMasivo.sql → autorizarEvento()
 * - Lógica de rechazo basada en RechazaPredeterminarPersonaDireccionElectronica.sql → rechazarEvento()
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class AutorizarGrupoDireccionElectronicaMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarGrupoDireccionElectronicaMasivo.class);

	private final SQL sql = new SQL();

	// SQLs para operaciones
	private final String SQL_UPDATE_ESTADO_GRUPO = loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarGrupoDireccionElectronicaMasivo_Update_EstadoGrupo.sql");

	// Clases internas para mapeo de datos
	public static class ActualizarEstadoGrupo {
		public Long IdGrupoDireccion;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		public String Borrado;
	}

	// Clase de contexto compartido
	private ContextoProceso contexto = new ContextoProceso();

	private static class ContextoProceso {
		public Connection Connection;
		public EntradaAutorizacion Entrada;
		public Boolean EsAutorizacion;
		public List<Long> GrupoDireccionIds;

		public ContextoProceso() {
			Entrada = new EntradaAutorizacion();
			EsAutorizacion = false;
			GrupoDireccionIds = new ArrayList<>();
		}
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarGrupoDireccionElectronicaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "AUT");

			// Preparar lista de grupos para actualizar
			List<ActualizarEstadoGrupo> actualizarList = contexto.GrupoDireccionIds.stream()
					.map(idGrupo -> {
						ActualizarEstadoGrupo act = new ActualizarEstadoGrupo();
						act.IdGrupoDireccion = idGrupo;
						act.IdEstadoAutorizacion = "AUT";  // Estado autorizado
						act.IdEstado = null;  // Mantener estado actual
						act.Borrado = null;   // Mantener valor actual
						return act;
					})
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			if (!actualizarList.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_ESTADO_GRUPO, actualizarList, ActualizarEstadoGrupo.class);
			}

			// Agregar resultados
			for (Long idGrupo : contexto.GrupoDireccionIds) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				ActualizarEstadoGrupo grupo = new ActualizarEstadoGrupo();
				grupo.IdGrupoDireccion = idGrupo;
				grupo.IdEstadoAutorizacion = "AUT";
				grupo.IdEstado = null;
				grupo.Borrado = null;
				resultado.evento = grupo;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarGrupoDireccionElectronicaMasivo.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarGrupoDireccionElectronicaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "RZD");

			// Preparar lista de grupos para actualizar con estado RECHAZADO
			List<ActualizarEstadoGrupo> actualizarList = contexto.GrupoDireccionIds.stream()
					.map(idGrupo -> {
						ActualizarEstadoGrupo act = new ActualizarEstadoGrupo();
						act.IdGrupoDireccion = idGrupo;
						act.IdEstadoAutorizacion = "RZD";  // Estado rechazado
						// Para eventos de creación (NUGDIE), marcar como inactivo y borrado
						act.IdEstado = "INA";  // Inactivo
						act.Borrado = "S";     // Marcado para borrar
						return act;
					})
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			if (!actualizarList.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_ESTADO_GRUPO, actualizarList, ActualizarEstadoGrupo.class);
			}

			// Agregar resultados
			for (Long idGrupo : contexto.GrupoDireccionIds) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				ActualizarEstadoGrupo grupo = new ActualizarEstadoGrupo();
				grupo.IdGrupoDireccion = idGrupo;
				grupo.IdEstadoAutorizacion = "RZD";
				grupo.IdEstado = "INA";
				grupo.Borrado = "S";
				resultado.evento = grupo;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarGrupoDireccionElectronicaMasivo.rechazarEvento {}", fin - inicio);

		return salida;
	}

	private ContextoProceso obtenerContexto(Connection conn, EntradaAutorizacion entrada, String accion) {
		logger.debug("INICIO::obtenerContexto");
		long inicio = System.currentTimeMillis();

		ContextoProceso contexto = new ContextoProceso();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.EsAutorizacion = "AUT".equals(accion);

		// Extraer IDs de grupos desde entrada
		contexto.GrupoDireccionIds = entrada.p_Lista.stream()
				.map(u -> Long.valueOf(u.NumeroOperacionConcepto))
				.distinct()
				.collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::obtenerContexto {}", fin - inicio);

		return contexto;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarGrupoDireccionElectronicaMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NUGDIE\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

				AutorizarGrupoDireccionElectronicaMasivo servicio = new AutorizarGrupoDireccionElectronicaMasivo();

				// Probar AUTORIZAR
				System.out.println("\n=== TEST AUTORIZAR ===");
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Probar RECHAZAR
				System.out.println("\n=== TEST RECHAZAR ===");
				SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
				System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
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
