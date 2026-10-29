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

public class AutorizarCrearPersonaCuentaBancariaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearPersonaCuentaBancariaMasivo.class);
	
	private final SQL sql = new SQL();
	
	private final String SQL_UPDATE_CUENTA_BANCARIA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaCuentaBancariaMasivo_Update_CuentaBancaria.sql"
		);
	
	private final String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaCuentaBancariaMasivo_Update_PreferenciaPagoPortafolio.sql"
		);
	
	private final String SQL_UPDATE_CUENTA_BANCARIA_RECHAZAR = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaCuentaBancariaMasivo_Update_CuentaBancariaRechazar.sql"
		);
	
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaCuentaBancariaMasivo_Insert_LogGenerarContratos.sql"
		);
	
	private final String SQL_SELECT_PERSONA_POR_CUENTA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearPersonaCuentaBancariaMasivo_Select_PersonaPorCuenta.sql"
		);
	
	public static class CuentaBancaria {
		public Long IdCuentaBancaria;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CuentaBancaria that = (CuentaBancaria) o;
			return IdCuentaBancaria != null && IdCuentaBancaria.equals(that.IdCuentaBancaria);
		}
		
		@Override
		public int hashCode() {
			return IdCuentaBancaria != null ? IdCuentaBancaria.hashCode() : 0;
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
	
	public static class PersonaCuenta {
		public Long IdPersona;
		public Long IdCuentaBancaria;
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaCuentaBancariaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<CuentaBancaria> cuentas = entrada.p_Lista.stream()
				.map(evento -> {
					CuentaBancaria c = new CuentaBancaria();
					c.IdCuentaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
					c.IdEstadoAutorizacion = "AUT";
					c.IdEstado = null;
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_CUENTA_BANCARIA, cuentas, CuentaBancaria.class);
			
			sql.batch(conn, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO, cuentas, CuentaBancaria.class);
			
			List<Long> idsCuenta = cuentas.stream()
				.map(c -> c.IdCuentaBancaria)
				.collect(Collectors.toList());
			
			String inClause = idsCuenta.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
			
			String queryPersonas = SQL_SELECT_PERSONA_POR_CUENTA.replace("{ID_LIST}", inClause);
			
			List<PersonaCuenta> personasCuentas = sql.query(conn, queryPersonas, new HashMap<>(), PersonaCuenta.class);
			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			String fechaSistemaStr = fechaSistema.substring(0, 10);
			
			List<Long> secuencias = Utils.getSecuencia(
				conn,
				"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos",
				Long.valueOf(personasCuentas.size())
			);
			
			List<LogGenerarContratos> logsContratos = new java.util.ArrayList<>();
			for (int i = 0; i < personasCuentas.size(); i++) {
				PersonaCuenta pc = personasCuentas.get(i);
				LogGenerarContratos log = new LogGenerarContratos();
				log.Idlog = secuencias.get(i);
				log.CodigoFormulario = "CUENTA_BANCARIA_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.IdCliente = 0L;
				log.IdPersona = pc.IdPersona;
				log.IdUsuario = entrada.p_IdUsuario;
				log.FechaSistema = fechaSistemaStr;
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Persona - ConfirmarCrearPersonaCuentaBancariaMasivo.sql";
				logsContratos.add(log);
			}
			
			sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);
			
			for (CuentaBancaria cuenta : cuentas) {
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
		logger.info("FIN::AutorizarCrearPersonaCuentaBancariaMasivo.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPersonaCuentaBancariaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<CuentaBancaria> cuentas = entrada.p_Lista.stream()
				.map(evento -> {
					CuentaBancaria c = new CuentaBancaria();
					c.IdCuentaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
					c.IdEstadoAutorizacion = "RZD";
					c.IdEstado = "INA";
					return c;
				})
				.distinct()
				.collect(Collectors.toList());
			
			List<EntidadOrigen> entidadesNUECTB = entrada.p_Lista.stream()
				.filter(evento -> {
					String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					return "NUECTB".equals(tipo);
				})
				.map(evento -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
					eo.IdTipoEntidad = "CUENTABANCARIA";
					return eo;
				})
				.collect(Collectors.toList());
			
			if (!entidadesNUECTB.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
				
				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesNUECTB;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
				
				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
				
				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
					return salida;
				}
			}
			
			sql.batch(conn, SQL_UPDATE_CUENTA_BANCARIA_RECHAZAR, cuentas, CuentaBancaria.class);
			
			for (CuentaBancaria cuenta : cuentas) {
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
		logger.info("FIN::AutorizarCrearPersonaCuentaBancariaMasivo.rechazarEvento {}", fin - inicio);
		
		return salida;
	}
}
