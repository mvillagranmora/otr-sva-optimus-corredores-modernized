package cl.bcs.portafolio;

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
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para autorizar/rechazar creación y modificación de Cuentas Custodio.
 * Migración de:
 * - ConfirmarCrearCuentaCustodioMasivo.sql -> autorizarEvento()
 * - RechazaAutorizacionCuentaCustodio.sql -> rechazarEvento()
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class AutorizarCrearCuentaCustodioMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearCuentaCustodioMasivo.class);

	private final SQL sql = new SQL();

	// SQLs para operaciones
	private final String SQL_UPDATE_CUENTA_CUSTODIO = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearCuentaCustodioMasivo_Update_CuentaCustodio.sql");

	private final String SQL_UPDATE_CUENTA_CUSTODIO_RECHAZAR = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearCuentaCustodioMasivo_Update_CuentaCustodioRechazar.sql");

	private final String SQL_SELECT_PORTAFOLIO_APV = loadResourceToString(
			"cl/bcs/portafolio/sql/AutorizarCrearCuentaCustodioMasivo_Select_PortafolioAPV.sql");

	// Clases internas para mapeo de datos
	public static class CuentaCustodio {
		public Long IdCuentaCustodioPortafolio;
		public String IdEstadoAutorizacion;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CuentaCustodio that = (CuentaCustodio) o;
			return IdCuentaCustodioPortafolio != null && IdCuentaCustodioPortafolio.equals(that.IdCuentaCustodioPortafolio);
		}

		@Override
		public int hashCode() {
			return IdCuentaCustodioPortafolio != null ? IdCuentaCustodioPortafolio.hashCode() : 0;
		}
	}

	public static class CuentaCustodioRechazar {
		public Long IdCuentaCustodioPortafolio;
		public String IdEstadoAutorizacion;
		public String MarcarInactivo;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CuentaCustodioRechazar that = (CuentaCustodioRechazar) o;
			return IdCuentaCustodioPortafolio != null && IdCuentaCustodioPortafolio.equals(that.IdCuentaCustodioPortafolio);
		}

		@Override
		public int hashCode() {
			return IdCuentaCustodioPortafolio != null ? IdCuentaCustodioPortafolio.hashCode() : 0;
		}
	}

	public static class PortafolioAPVInfo {
		public Long IdCuentaCustodioPortafolio;
		public Long IdPortafolio;
		public String EsAPV;
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearCuentaCustodioMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Preparar lista de cuentas custodio para actualizar con estado AUTORIZADO
			List<CuentaCustodio> cuentas = entrada.p_Lista.stream()
					.map(evento -> {
						CuentaCustodio c = new CuentaCustodio();
						c.IdCuentaCustodioPortafolio = Long.valueOf(evento.NumeroOperacionConcepto);
						c.IdEstadoAutorizacion = "AUT";
						return c;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_CUENTA_CUSTODIO, cuentas, CuentaCustodio.class);

			// Consultar qué portafolios son APV para replicar (query individual por cuenta)
			List<PortafolioAPVInfo> portafoliosAPV = new ArrayList<>();
			for (CuentaCustodio cuenta : cuentas) {
				HashMap<String, Object> paramsAPV = new HashMap<>();
				paramsAPV.put("IdCuentaCustodioPortafolio", cuenta.IdCuentaCustodioPortafolio);
				List<PortafolioAPVInfo> resultado = sql.query(conn, SQL_SELECT_PORTAFOLIO_APV, paramsAPV, PortafolioAPVInfo.class);
				portafoliosAPV.addAll(resultado);
			}

			// Procesar réplicas APV
			for (PortafolioAPVInfo info : portafoliosAPV) {
				if ("S".equals(info.EsAPV) && info.IdPortafolio != null && info.IdPortafolio > 0) {
					// Llamar a ReplicaPortafolioAPV
					ReplicaPortafolioAPV replicaService = new ReplicaPortafolioAPV();
					
					EntradaReplicaPortafolioAPV entradaReplica = new EntradaReplicaPortafolioAPV();
					entradaReplica.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaReplica.p_IdUsuario = entrada.p_IdUsuario;
					entradaReplica.p_IdModulo = entrada.p_IdModulo;
					entradaReplica.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					entradaReplica.p_Portafolio = info.IdPortafolio;
					entradaReplica.p_IdEntidad = info.IdCuentaCustodioPortafolio;
					entradaReplica.p_TipoEntidad = "CUENTACUSTODIO";
					entradaReplica.p_TipoOperacion = "I";

					SalidaReplicaPortafolioAPV salidaReplica = replicaService.logica(conn, entradaReplica);

					// Propagar errores si los hay
					if (salidaReplica.P_OUT_ERRORESCONTADOR > 0) {
						salida.P_OUT_ERRORESCONTADOR += salidaReplica.P_OUT_ERRORESCONTADOR;
						logger.warn("Errores en réplica APV para cuenta custodio {}: {}", 
								info.IdCuentaCustodioPortafolio, salidaReplica.P_OUT_ERRORESCONTADOR);
					}
				}
			}

			// Agregar resultados
			for (CuentaCustodio cuenta : cuentas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = cuenta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearCuentaCustodioMasivo.autorizarEvento {}", fin - inicio);

		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearCuentaCustodioMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			// Identificar eventos de modificación (MCTCUP) para llamar a SolicitudCambio
			List<EntidadOrigen> entidadesMCTCUP = new ArrayList<>();
            
			for (Evento evento : entrada.p_Lista) {
				String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
                
				// Para eventos de modificación, procesar solicitud de cambio
				// Los tipos de evento pueden variar: MCTCUP, MOECUC, MOCCUS, etc.
				if (idTipoEvento.startsWith("M")) {  // Modificación
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
					eo.IdTipoEntidad = "CUENTACUSTODIO";
					entidadesMCTCUP.add(eo);
				}
			}

			// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo si hay modificaciones
			if (!entidadesMCTCUP.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMCTCUP;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";

				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);

				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}

			// Preparar lista de cuentas custodio para actualizar con estado RECHAZADO
			List<CuentaCustodioRechazar> cuentas = entrada.p_Lista.stream()
					.map(evento -> {
						CuentaCustodioRechazar c = new CuentaCustodioRechazar();
						c.IdCuentaCustodioPortafolio = Long.valueOf(evento.NumeroOperacionConcepto);
						c.IdEstadoAutorizacion = "RZD";

						// Para eventos de creación (NPCCUS o que empiecen con 'N'), marcar como inactivo
						String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
						c.MarcarInactivo = idTipoEvento.startsWith("N") ? "S" : "N";
						
						return c;
					})
					.distinct()
					.collect(Collectors.toList());

			// Ejecutar UPDATE en batch
			sql.batch(conn, SQL_UPDATE_CUENTA_CUSTODIO_RECHAZAR, cuentas, CuentaCustodioRechazar.class);

			// Agregar resultados
			for (CuentaCustodioRechazar cuenta : cuentas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = cuenta;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}

		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearCuentaCustodioMasivo.rechazarEvento {}", fin - inicio);

		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST AutorizarCrearCuentaCustodioMasivo ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				// Test AUTORIZAR
				System.out.println("\n=== TEST AUTORIZAR ===");
				String jsonAutorizar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NPCCUS\"}]}";
				EntradaAutorizacion entradaAutorizar = Utils.getGsonBuilder().fromJson(jsonAutorizar, EntradaAutorizacion.class);

				AutorizarCrearCuentaCustodioMasivo servicio = new AutorizarCrearCuentaCustodioMasivo();
				SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entradaAutorizar);
				
				System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento NPCCUS (Nuevo)
				System.out.println("\n=== TEST RECHAZAR (NPCCUS - Nuevo) ===");
				String jsonRechazarNuevo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":123,\"NumeroOperacionConcepto\":\"456\",\"IdTipoEvento\":\"NPCCUS\"}]}";
				EntradaAutorizacion entradaRechazarNuevo = Utils.getGsonBuilder().fromJson(jsonRechazarNuevo, EntradaAutorizacion.class);
				
				SalidaAutorizacion salidaRechazarNuevo = servicio.rechazarEvento(conn, entradaRechazarNuevo);
				System.out.println("Errores: " + salidaRechazarNuevo.P_OUT_ERRORESCONTADOR);
				System.out.println("Resultados: " + salidaRechazarNuevo.P_OUT_RESULTADO.size());
				conn.rollback();

				// Test RECHAZAR - evento MCTCUP (Modificar)
				System.out.println("\n=== TEST RECHAZAR (MCTCUP - Modificar) ===");
				String jsonRechazarMod = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":124,\"NumeroOperacionConcepto\":\"789\",\"IdTipoEvento\":\"MCTCUP\"}]}";
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
