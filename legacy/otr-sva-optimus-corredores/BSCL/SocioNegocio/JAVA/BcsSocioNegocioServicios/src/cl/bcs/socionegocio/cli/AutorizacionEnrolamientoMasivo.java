package cl.bcs.socionegocio.cli;

import java.sql.Connection;
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

public class AutorizacionEnrolamientoMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizacionEnrolamientoMasivo.class);
	
	private final String SQL_UPDATE_ENROLAMIENTO_PERSONA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cli/sql/AutorizacionEnrolamientoMasivo_Update_EnrolamientoPersona.sql");
	private final String SQL_UPDATE_ENROLAMIENTO_CLIENTE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cli/sql/AutorizacionEnrolamientoMasivo_Update_EnrolamientoCliente.sql");
	
	public static class Cliente {
		public Long IdCliente;
		public String EsEnrolado;  // Nuevo estado a aplicar
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de clientes desde entrada y crear objetos con estado confirmado
			List<Cliente> clientes = entrada.p_Lista.stream()
				.map(evento -> {
					Cliente c = new Cliente();
					c.IdCliente = Long.valueOf(evento.NumeroOperacionConcepto);
					c.EsEnrolado = "S";  // Estado confirmado
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar persona asociada (solo si está en 'P' o 'N')
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO_PERSONA, clientes, Cliente.class);
			
			// Actualizar cliente (solo si está en 'P' o 'N')
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO_CLIENTE, clientes, Cliente.class);
			
			// Agregar resultado por cada cliente procesado
			for (Cliente cliente : clientes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = cliente;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionEnrolamientoMasivo.autorizarEvento");
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de clientes desde entrada y crear objetos con estado rechazado
			List<Cliente> clientes = entrada.p_Lista.stream()
				.map(evento -> {
					Cliente c = new Cliente();
					c.IdCliente = Long.valueOf(evento.NumeroOperacionConcepto);
					c.EsEnrolado = "N";  // Estado rechazado
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar persona asociada (solo si está en 'P')
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO_PERSONA, clientes, Cliente.class);
			
			// Actualizar cliente (solo si está en 'P')
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO_CLIENTE, clientes, Cliente.class);
			
			// Agregar resultado por cada cliente procesado
			for (Cliente cliente : clientes) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = cliente;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionEnrolamientoMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"ENRCLI\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionEnrolamientoMasivo servicio = new AutorizacionEnrolamientoMasivo();
				
				// Probar CONFIRMAR
				System.out.println("\n=== Probando CONFIRMAR ===");
				SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
				System.out.println("Salida confirmar: " + salidaConfirmar);
				System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
				conn.rollback();
				
				// Probar RECHAZAR
				System.out.println("\n=== Probando RECHAZAR ===");
				SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
				System.out.println("Salida rechazar: " + salidaRechazar);
				System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
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
