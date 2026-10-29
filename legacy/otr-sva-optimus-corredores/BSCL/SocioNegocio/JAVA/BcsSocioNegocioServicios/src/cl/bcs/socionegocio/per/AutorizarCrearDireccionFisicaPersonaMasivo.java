package cl.bcs.socionegocio.per;

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

public class AutorizarCrearDireccionFisicaPersonaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearDireccionFisicaPersonaMasivo.class);
	
	private final SQL sql = new SQL();
	
	private final String SQL_UPDATE_DIRECCION_FISICA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDireccionFisicaPersonaMasivo_Update_DireccionFisica.sql"
		);
	
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDireccionFisicaPersonaMasivo_Insert_LogGenerarContratos.sql"
		);
	
	private final String SQL_SELECT_PERSONA_POR_DIRECCION = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDireccionFisicaPersonaMasivo_Select_PersonaPorDireccion.sql"
		);
	
	public static class DireccionFisica {
		public Long IdDireccionFisica;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DireccionFisica that = (DireccionFisica) o;
			return IdDireccionFisica != null && IdDireccionFisica.equals(that.IdDireccionFisica);
		}
		
		@Override
		public int hashCode() {
			return IdDireccionFisica != null ? IdDireccionFisica.hashCode() : 0;
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
	
	public static class PersonaDireccion {
		public Long IdPersona;
		public Long IdDireccionFisica;
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDireccionFisicaPersonaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DireccionFisica> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionFisica d = new DireccionFisica();
					d.IdDireccionFisica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";
					d.IdEstado = null;
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DIRECCION_FISICA, direcciones, DireccionFisica.class);
			
			HashMap<String, Object> paramsDireccion = new HashMap<>();
			List<Long> idsDireccion = direcciones.stream()
				.map(d -> d.IdDireccionFisica)
				.collect(Collectors.toList());
			paramsDireccion.put("IdDireccionFisica", idsDireccion);
			
			List<PersonaDireccion> personasDirecciones = sql.query(conn, SQL_SELECT_PERSONA_POR_DIRECCION, paramsDireccion, PersonaDireccion.class);
			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			String fechaSistemaStr = fechaSistema.substring(0, 10);
			
			List<Long> secuencias = Utils.getSecuencia(
				conn,
				"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos",
				Long.valueOf(personasDirecciones.size())
			);
			
			List<LogGenerarContratos> logsContratos = new java.util.ArrayList<>();
			for (int i = 0; i < personasDirecciones.size(); i++) {
				PersonaDireccion pd = personasDirecciones.get(i);
				LogGenerarContratos log = new LogGenerarContratos();
				log.Idlog = secuencias.get(i);
				log.CodigoFormulario = "DIRECCION_FISICA_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.IdCliente = 0L;
				log.IdPersona = pd.IdPersona;
				log.IdUsuario = entrada.p_IdUsuario;
				log.FechaSistema = fechaSistemaStr;
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Persona - ConfirmarCrearPersonaDireccionFisicaMasivo.sql";
				logsContratos.add(log);
			}
			
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);
			
			for (DireccionFisica direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDireccionFisicaPersonaMasivo.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDireccionFisicaPersonaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DireccionFisica> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionFisica d = new DireccionFisica();
					d.IdDireccionFisica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";
					
					String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					if ("NUEDIF".equals(idTipoEvento)) {
						d.IdEstado = "INA";
					} else {
						d.IdEstado = null;
					}
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DIRECCION_FISICA, direcciones, DireccionFisica.class);
			
			List<EntidadOrigen> entidadesMODDIF = entrada.p_Lista.stream()
				.filter(evento -> {
					String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					return "MODDIF".equals(tipo);
				})
				.map(evento -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
					eo.IdTipoEntidad = "DIRECCIONFISICA";
					return eo;
				})
				.collect(Collectors.toList());
			
			if (!entidadesMODDIF.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
				
				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMODDIF;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
				
				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
				
				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}
			
			for (DireccionFisica direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDireccionFisicaPersonaMasivo.rechazarEvento {}", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarCrearDireccionFisicaPersonaMasivo servicio = new AutorizarCrearDireccionFisicaPersonaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 76770L;
			entrada.p_IdModulo = "AUT";
			
			cl.bcs.plataforma.aut.pojo.Evento evento = new cl.bcs.plataforma.aut.pojo.Evento();
			evento.NumeroOperacionConcepto = "1326948";
			evento.IdTipoEvento = "NUEDIF";  // MODDIF, NUEDIF
			evento.IdEmpresa = 1L;
			evento.IdUsuario = 76770L;
			evento.IdModulo = "AUT";
			entrada.p_Lista.add(evento);
			
			
			System.out.println("\n=== Probando CONFIRMAR ===");
			SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
			System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaConfirmar.P_OUT_RESULTADO.size());

			System.out.println("\n=== Probando RECHAZAR ===");
			SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
			System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}