package cl.bcs.socionegocio.per;

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

public class AutorizacionEnrolamientoPersonaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizacionEnrolamientoPersonaMasivo.class);
	
	private final String SQL_UPDATE_ENROLAMIENTO = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/AutorizacionEnrolamientoPersonaMasivo_Update_Enrolamiento.sql");
	
	public static class Persona {
		public Long IdPersona;
		public String EsEnrolado;  // Nuevo estado a aplicar
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoPersonaMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de personas desde entrada y crear objetos con estado confirmado
			List<Persona> personas = entrada.p_Lista.stream()
				.map(evento -> {
					Persona p = new Persona();
					p.IdPersona = Long.valueOf(evento.NumeroOperacionConcepto);
					p.EsEnrolado = "S";  // Estado confirmado
					return p;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar base de datos
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO, personas, Persona.class);
			
			// Agregar resultado por cada persona procesada
			for (Persona persona : personas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = persona;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionEnrolamientoPersonaMasivo.autorizarEvento");
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizacionEnrolamientoPersonaMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		try {
			// Extraer IDs de personas desde entrada y crear objetos con estado rechazado
			List<Persona> personas = entrada.p_Lista.stream()
				.map(evento -> {
					Persona p = new Persona();
					p.IdPersona = Long.valueOf(evento.NumeroOperacionConcepto);
					p.EsEnrolado = "N";  // Estado rechazado
					return p;
				})
				.distinct()
				.collect(Collectors.toList());
			
			// Actualizar base de datos
			new SQL().batch(conn, SQL_UPDATE_ENROLAMIENTO, personas, Persona.class);
			
			// Agregar resultado por cada persona procesada
			for (Persona persona : personas) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = persona;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("FIN::AutorizacionEnrolamientoPersonaMasivo.rechazarEvento");
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Eventos\":[{\"IdEvento\":0}],\"p_Etapas\":[{\"IdEvento\":832489,\"IdEtapa\":832490}],\"p_EventoEtapa\":\"ETAPA\",\"p_IdEstado\":\"AUT\",\"p_Observaciones\":\" \",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"832489\",\"IdTipoEvento\":\"ENRPER\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
				
				AutorizacionEnrolamientoPersonaMasivo servicio = new AutorizacionEnrolamientoPersonaMasivo();
				
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
