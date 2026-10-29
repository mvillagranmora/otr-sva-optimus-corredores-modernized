package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class ConfirmarRechazarAutorizacionOperacionForward implements JavaCall, BcsAutorizaciones {
	
	static final Logger logger = LogManager.getLogger(ConfirmarRechazarAutorizacionOperacionForward.class);
	
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_CreateLocalTempTableEventoOperacionForward.sql");
	private final String SQL_INSERT_TEMP_EVENTO_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_InsertTempEventoOperacionForward.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_DropLocalTempTableEventoOperacionForward.sql");
	private final String SQL_SELECT_EVENTO_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_SelectEventoOperacionForward.sql");
	private final String SQL_UPDATE_EVENTO = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEvento.sql");
	private final String SQL_SELECT_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_SelectOperacionForward.sql");
	private final String SQL_UPDATE_ESTADO_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEstadoOperacion.sql");
	private final String SQL_UPDATE_ESTADO_CARTERA = loadResourceToString("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEstadoCartera.sql");
	
	
	private ContextoProceso contexto = new ContextoProceso();
	
	public static class OperacionForward extends BcsPojo {
		public Long IdOperacion;
		public String IdTipoOperacion;
		public java.sql.Timestamp FechaOperacion;
		public String Estado;
		public String EstadoConfirmacion;
		public Long IdEmpresa;
		
		public String EstadoPrevioAutorizacion;
		public String EstadoPosteriorAutorizacion;
	}
	
	public static class FormaPago extends BcsPojo {
		public Long NumeroOperacion;
		public String IdTipoProducto;
		public String IdTipoMovimiento;
		public String IdMedioDePago;
		public String NombreMedioDePago;
		public String IdModalidadPago;
		public String IdMoneda;
		public BigDecimal Monto;
		public java.sql.Date FechaIngresoRegistro;
		public java.sql.Date FechaCondicionLiquidacion;
		public String AfectarTesoreria;
	}
	
	public static class POutIdOperacionForward extends BcsPojo {
		public Long IdOperacion;
		public String Estado;
	}
	
	public static class POutIngresoEgresoGenerado extends BcsPojo {
		public Long NumeroReferencia;
		public Long IdIngresoEgreso;
		public String IdTipoMovimiento;
	}
	
	public static class POutAdvertenciasDetalle extends BcsPojo {
		public String IdInterno;
		public String IdDatatec;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class POutErroresDetalle extends BcsPojo {
		public String IdInterno;
		public String IdDatatec;
		public String Codigo;
		public String Descripcion;
	}
	
	public static class Evento extends BcsPojo {
		public Long IdTrabajo;
		public Long IdEvento;
		public Long IdOperacion;
		public String IdTipoEvento;
		public String NumeroOperacionConcepto;
		public Long IdEmpresa;
		public String IdTipoOperacion;
		public java.sql.Date Fecha;
		public String IdEstado;
		public String IdEstadoPosteriorAutorizacion;
	}
	
	private static class ContextoProceso {
		public Connection Connection;
		public EntradaAutorizacion Entrada;
		public String FechaSistemaStr;
		public Boolean EsAutorizacion;
		public List<Long> OperacionForwardIds;
		public Set<String> TipoEventoIds;
		
		public ContextoProceso() {
			Entrada = new EntradaAutorizacion();
			FechaSistemaStr = "";
			EsAutorizacion = false;
			OperacionForwardIds = new ArrayList<>();
			TipoEventoIds = Stream.of("LIFDCP").collect(Collectors.toSet());
		}
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionForward.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "AUT");
			List<OperacionForward> operaciones = obtenerOperaciones();
			Map<Long, OperacionForward> operacionPorId = obtenerOperacionForwardPorId(operaciones);
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionForward = obtenerEventosPendientesPorIdOperacionForward(operacionPorId);
			
			List<OperacionForward> operacionesConfirmar = new ArrayList<>(operaciones.size());
			List<OperacionForward> operacionesPendientes = new ArrayList<>(operaciones.size());
			for (OperacionForward operacion : operaciones) {
				List<Evento> eventosFaltantes = eventosPendientesPorIdOperacionForward.getOrDefault(operacion.IdOperacion, Collections.emptyList());
				List<OperacionForward> destino;
				if (eventosFaltantes.isEmpty()) {
					destino = operacionesConfirmar;
					operacion.EstadoPosteriorAutorizacion = "VIG";
				} else {
					destino = operacionesPendientes;
					operacion.EstadoPosteriorAutorizacion = "PEN";
				}
				destino.add(operacion);
			}
			
			modificarOperaciones(operacionesConfirmar);
			modificarCarteraOperaciones(operaciones);
//			AgregarMensajeWebsocket.agregar(conn, entrada.p_IdEmpresa, "ACTFORWARD", "Refresco Movimientos Forward");
//			if (contexto.RequiereConfirmarLiquidacion && operacionesConfirmar.size() > 0) {
//				List<FormaPago> formasPago = obtenerFormasPago(operacionesConfirmar);
//				crearOperacionForwardTesoreriaFacturacion(operacionesConfirmar, formasPago);
//			}
			agregarLog(operaciones);
			
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarRechazarAutorizacionOperacionForward.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionForward.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "RZD");
			List<OperacionForward> operaciones = obtenerOperaciones();
			Map<Long, OperacionForward> operacionPorId = obtenerOperacionForwardPorId(operaciones);
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionForward = obtenerEventosPendientesPorIdOperacionForward(operacionPorId);
			modificarOperaciones(operaciones);
			modificarCarteraOperaciones(operaciones);
			modificarRestoEventosAsociados(eventosPendientesPorIdOperacionForward);
			agregarLog(operaciones);
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarRechazarAutorizacionOperacionForward.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	private ContextoProceso obtenerContexto(Connection conn, EntradaAutorizacion entrada, String accion) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.iniciarContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoProceso contexto = new ContextoProceso();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.FechaSistemaStr = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		contexto.EsAutorizacion = "AUT".equals(accion);
		contexto.OperacionForwardIds = entrada.p_Lista.stream().map(u -> Long.valueOf(u.NumeroOperacionConcepto)).distinct().collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.iniciarContexto {}", fin - inicio);
		
		return contexto;
	}
	
	private List<OperacionForward> obtenerOperaciones() throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.modificarOperaciones");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdOperacion", contexto.OperacionForwardIds);
		params.put("IdEstado", "PEN");
		
		List<OperacionForward> operaciones = new SQL().query(contexto.Connection, SQL_SELECT_OPERACION_FORWARD, params, OperacionForward.class);
		for (OperacionForward operacion : operaciones) {
			operacion.EstadoPosteriorAutorizacion = contexto.EsAutorizacion ? "VIG" : "ANU";
			operacion.EstadoConfirmacion = "CNF";
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.modificarOperaciones {}", fin - inicio);
		
		return operaciones;
	}
	
	private Map<Long, OperacionForward> obtenerOperacionForwardPorId(List<OperacionForward> operaciones) {
		logger.trace("INICIO::ConfirmarRechazarAutorizacionOperacionForward.obtenerOperacionForwardPorId");
		long inicio = System.currentTimeMillis();
		
		Map<Long, OperacionForward> operacionPorId = operaciones.stream().collect(Collectors.toMap(u -> u.IdOperacion, u -> u));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarRechazarAutorizacionOperacionForward.obtenerOperacionForwardPorId {}", fin - inicio);
		
		return operacionPorId;
	}
	
	private Map<Long, List<Evento>> obtenerEventosPendientesPorIdOperacionForward(Map<Long, OperacionForward> operacionPorId) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.obtenerEventosPorIdOperacionForward");
		long inicio = System.currentTimeMillis();
		
		final long idTrabajo = 1L;
		SQL.exec(contexto.Connection, SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_FORWARD);
		
		Set<String> clasificacionesIgnoradas = contexto.Entrada.p_Lista.stream().map(u -> u.IdTipoEvento + "-" + u.NumeroOperacionConcepto).collect(Collectors.toSet());
		List<Evento> eventos = contexto.OperacionForwardIds.stream()
				.map(operacionPorId::get)
				.filter(Objects::nonNull)
				.flatMap(u -> contexto.TipoEventoIds.stream().map(e -> Pair.of(u, e)))
				.filter(parOperacionEvento -> clasificacionesIgnoradas.contains(parOperacionEvento.getRight() + "-" + parOperacionEvento.getLeft().IdOperacion) == false)
				.map(parOperacionEvento -> {
					OperacionForward operacion = parOperacionEvento.getLeft();
					String idTipoEvento = parOperacionEvento.getRight();
					Evento evento = new Evento();
					evento.IdOperacion = operacion.IdOperacion;
					evento.NumeroOperacionConcepto = String.valueOf(operacion.IdOperacion);
					evento.IdTipoEvento = idTipoEvento;
					evento.IdEmpresa = operacion.IdEmpresa;
					evento.IdTipoOperacion = operacion.IdTipoOperacion;
					evento.Fecha = Fecha.toDate(Fecha.toStringDate(operacion.FechaOperacion));
					evento.IdTrabajo = idTrabajo;
					evento.IdEstadoPosteriorAutorizacion = contexto.EsAutorizacion ? "AUT" : "RZD";
					return evento;
				})
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(contexto.Connection, SQL_INSERT_TEMP_EVENTO_OPERACION_FORWARD, eventos, Evento.class);
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdTrabajo", idTrabajo);
		
		Map<Long, List<Evento>> eventosPorIdOperacion = sql.query(contexto.Connection, SQL_SELECT_EVENTO_OPERACION_FORWARD, params, Evento.class).stream()
				.collect(Collectors.groupingBy(u -> Long.valueOf(u.NumeroOperacionConcepto)));
		
		SQL.exec(contexto.Connection, SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_FORWARD);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.obtenerEventosPorIdOperacionForward {}", fin - inicio);
		
		return eventosPorIdOperacion;
	}
	
	private void modificarOperaciones(List<OperacionForward> operaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.modificarOperaciones");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(contexto.Connection, SQL_UPDATE_ESTADO_OPERACION, operaciones, OperacionForward.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.modificarOperaciones {}", fin - inicio);
	}
	
	private void modificarCarteraOperaciones(List<OperacionForward> operaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.modificarCarteraOperaciones");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(contexto.Connection, SQL_UPDATE_ESTADO_CARTERA, operaciones, OperacionForward.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.modificarCarteraOperaciones {}", fin - inicio);
	}
	
	private void modificarRestoEventosAsociados(Map<Long, List<Evento>> eventosPorIdOperacionForward) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.modificarRestoEventosAsociados");
		long inicio = System.currentTimeMillis();
		
		List<Evento> eventos = eventosPorIdOperacionForward.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
		new SQL().batch(contexto.Connection, SQL_UPDATE_EVENTO, eventos, Evento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.modificarRestoEventosAsociados {}", fin - inicio);
	}
	
	
	private void agregarLog(List<OperacionForward> operaciones) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionForward.agregarLog");
		long inicio = System.currentTimeMillis();
		
		final long idTrabajo = 1L;
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdUsuario = contexto.Entrada.p_IdUsuario;
		entradaAgregarLog.p_IdModulo = contexto.Entrada.p_IdModulo;
		entradaAgregarLog.p_UsaReglasNegocio = contexto.Entrada.p_UsaReglasNegocio;
		entradaAgregarLog.p_IdTrabajo = idTrabajo;
		entradaAgregarLog.p_IdEstadoAutorizacion = contexto.EsAutorizacion ? "VIG" : "ANU";
		
		List<AgregarLog.InAgregarLog.Log> logs = new ArrayList<>(operaciones.size());
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogs = new ArrayList<>(operaciones.size());
		for (OperacionForward operacion : operaciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTrabajo = idTrabajo;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = contexto.Entrada.p_IdModulo;
			log.IdUsuario = contexto.Entrada.p_IdUsuario;
			log.IdEmpresa = contexto.Entrada.p_IdEmpresa;
			log.FechaSistemaLog = contexto.FechaSistemaStr;
			log.IdUsuarioAtendido = contexto.Entrada.p_IdUsuario;
			log.IdTipoRegistro = "SPOT";
			log.NombreProceso = "Confirmacion Operacion Forward excede Limite Paridad y Tipo Cambio";
			log.IdRegistro = "455"; // Viene en duro desde el SP migrado
			log.EstadoProceso = contexto.EsAutorizacion ? "AUT" : "RZD";
			logs.add(log);
			
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdTrabajo = idTrabajo;
			detalle.SubRegistro = "LimiteParidadYTipoCambio";
			detalle.CampoRegistro = "Estado";
			detalle.ValorAntiguo = operacion.EstadoPrevioAutorizacion;
			detalle.ValorNuevo = operacion.EstadoPosteriorAutorizacion;
			detalleLogs.add(detalle);
		}
		
		entradaAgregarLog.p_Log.addAll(logs);
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogs);
		new AgregarLog().logica(contexto.Connection, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionForward.agregarLog {}", fin - inicio);
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String jsonOptimus = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":254696,\"NumeroOperacionConcepto\":\"11314\",\"IdTipoEvento\":\"LITCSP\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(jsonOptimus, EntradaAutorizacion.class);
				ConfirmarRechazarAutorizacionOperacionForward ConfirmarRechazarAutorizacionOperacionForward = new ConfirmarRechazarAutorizacionOperacionForward();
				
				SalidaAutorizacion salidaConfirmar = ConfirmarRechazarAutorizacionOperacionForward.autorizarEvento(conn, entrada);
				conn.rollback();
				
				SalidaAutorizacion salidaRechazar = ConfirmarRechazarAutorizacionOperacionForward.rechazarEvento(conn, entrada);
				conn.rollback();
				
				System.out.println("Salida confirmar");
				System.out.println(salidaConfirmar);
				
				System.out.println("\nSalida rechazar");
				System.out.println(salidaRechazar);
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
