package cl.bcs.mesa.SPT;

import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.*;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class ConfirmarRechazarAutorizacionOperacionSpot implements JavaCall, BcsAutorizaciones {
	static final Logger logger = LogManager.getLogger(ConfirmarRechazarAutorizacionOperacionSpot.class);
	
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableEventoOperacionSpot.sql");
	private final String SQL_INSERT_TEMP_EVENTO_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempEventoOperacionSpot.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableEventoOperacionSpot.sql");
	private final String SQL_SELECT_EVENTO_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectEventoOperacionSpot.sql");
	private final String SQL_UPDATE_EVENTO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_UpdateEvento.sql");
	private final String SQL_SELECT_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectOperacionSpot.sql");
	private final String SQL_SELECT_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectFormaPago.sql");
	private final String SQL_UPDATE_ESTADO_OPERACION = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_UpdateEstadoOperacion.sql");
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableOperacionSpot.sql");
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableFormaPago.sql");
	private final String SQL_INSERT_TEMP_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempOperacionSpot.sql");
	private final String SQL_INSERT_TEMP_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempFormaPago.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableOperacionSpot.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableFormaPago.sql");
	
	private ContextoProceso contexto = new ContextoProceso();
	
	public static class OperacionSpot extends BcsPojo {
		public Long IdOperacionSpot;
		public Long NumeroOperacion;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal Margen;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String SolicitudTipoCambio;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
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
	
	public static class POutIdOperacionSpot extends BcsPojo {
		public Long IdOperacionSpot;
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
		public Long IdOperacionSpot;
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
		public Boolean RequiereConfirmarLiquidacion;
		public List<Long> OperacionSpotIds;
		public Set<String> TipoEventoIds;
		
		public ContextoProceso() {
			Entrada = new EntradaAutorizacion();
			FechaSistemaStr = "";
			EsAutorizacion = false;
			OperacionSpotIds = new ArrayList<>();
			TipoEventoIds = Stream.of("LIMSPT", "LITCSP", "LIPASP").collect(Collectors.toSet());
		}
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "AUT");
			List<OperacionSpot> operaciones = obtenerOperaciones();
			Map<Long, OperacionSpot> operacionPorId = obtenerOperacionSpotPorId(operaciones);
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionSpot = obtenerEventosPendientesPorIdOperacionSpot(operacionPorId);
			
			List<OperacionSpot> operacionesConfirmar = new ArrayList<>(operaciones.size());
			List<OperacionSpot> operacionesPendientes = new ArrayList<>(operaciones.size());
			for (OperacionSpot operacion : operaciones) {
				List<Evento> eventosFaltantes = eventosPendientesPorIdOperacionSpot.getOrDefault(operacion.IdOperacionSpot, Collections.emptyList());
				List<OperacionSpot> destino;
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
			AgregarMensajeWebsocket.agregar(conn, entrada.p_IdEmpresa, "ACTSPOT", "Refresco Movimientos Spot");
			if (contexto.RequiereConfirmarLiquidacion && operacionesConfirmar.size() > 0) {
				List<FormaPago> formasPago = obtenerFormasPago(operacionesConfirmar);
				crearOperacionSpotTesoreriaFacturacion(operacionesConfirmar, formasPago);
			}
			agregarLog(operaciones);
			
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarRechazarAutorizacionOperacionSpot.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "RZD");
			List<OperacionSpot> operaciones = obtenerOperaciones();
			Map<Long, OperacionSpot> operacionPorId = obtenerOperacionSpotPorId(operaciones);
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionSpot = obtenerEventosPendientesPorIdOperacionSpot(operacionPorId);
			modificarOperaciones(operaciones);
			modificarRestoEventosAsociados(eventosPendientesPorIdOperacionSpot);
			agregarLog(operaciones);
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarRechazarAutorizacionOperacionSpot.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	private ContextoProceso obtenerContexto(Connection conn, EntradaAutorizacion entrada, String accion) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.iniciarContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoProceso contexto = new ContextoProceso();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.FechaSistemaStr = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		contexto.EsAutorizacion = "AUT".equals(accion);
		contexto.OperacionSpotIds = entrada.p_Lista.stream().map(u -> Long.valueOf(u.NumeroOperacionConcepto)).distinct().collect(Collectors.toList());
		contexto.RequiereConfirmarLiquidacion = false;
		ConsultarConfiguracionMesa.ConfiguracionMesa requireConfirmarLiquidacionConfig = new ConsultarConfiguracionMesa().logica(conn, entrada.p_IdEmpresa, "MEME", "ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION");
		contexto.RequiereConfirmarLiquidacion = StringUtils.isNullOrBlank(requireConfirmarLiquidacionConfig.Valor) == false && "S".equals(requireConfirmarLiquidacionConfig.Valor);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.iniciarContexto {}", fin - inicio);
		
		return contexto;
	}
	
	private List<OperacionSpot> obtenerOperaciones() throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.modificarOperaciones");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdOperacionSpot", contexto.OperacionSpotIds);
		params.put("Estado", "PEN");
		
		List<OperacionSpot> operaciones = new SQL().query(contexto.Connection, SQL_SELECT_OPERACION_SPOT, params, OperacionSpot.class);
		for (OperacionSpot operacion : operaciones) {
			operacion.EstadoPosteriorAutorizacion = contexto.EsAutorizacion ? "VIG" : "ANU";
			operacion.EstadoConfirmacion = contexto.RequiereConfirmarLiquidacion ? "PEN" : "CNF";
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.modificarOperaciones {}", fin - inicio);
		
		return operaciones;
	}
	
	private Map<Long, OperacionSpot> obtenerOperacionSpotPorId(List<OperacionSpot> operaciones) {
		logger.trace("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.obtenerOperacionSpotPorId");
		long inicio = System.currentTimeMillis();
		
		Map<Long, OperacionSpot> operacionPorId = operaciones.stream().collect(Collectors.toMap(u -> u.IdOperacionSpot, u -> u));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarRechazarAutorizacionOperacionSpot.obtenerOperacionSpotPorId {}", fin - inicio);
		
		return operacionPorId;
	}
	
	private Map<Long, List<Evento>> obtenerEventosPendientesPorIdOperacionSpot(Map<Long, OperacionSpot> operacionPorId) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.obtenerEventosPorIdOperacionSpot");
		long inicio = System.currentTimeMillis();
		
		final long idTrabajo = 1L;
		SQL.exec(contexto.Connection, SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_SPOT);
		
		Set<String> clasificacionesIgnoradas = contexto.Entrada.p_Lista.stream().map(u -> u.IdTipoEvento + "-" + u.NumeroOperacionConcepto).collect(Collectors.toSet());
		List<Evento> eventos = contexto.OperacionSpotIds.stream()
				.map(operacionPorId::get)
				.filter(Objects::nonNull)
				.flatMap(u -> contexto.TipoEventoIds.stream().map(e -> Pair.of(u, e)))
				.filter(parOperacionEvento -> clasificacionesIgnoradas.contains(parOperacionEvento.getRight() + "-" + parOperacionEvento.getLeft().IdOperacionSpot) == false)
				.map(parOperacionEvento -> {
					OperacionSpot operacion = parOperacionEvento.getLeft();
					String idTipoEvento = parOperacionEvento.getRight();
					Evento evento = new Evento();
					evento.IdOperacionSpot = operacion.IdOperacionSpot;
					evento.NumeroOperacionConcepto = String.valueOf(operacion.IdOperacionSpot);
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
		sql.batch(contexto.Connection, SQL_INSERT_TEMP_EVENTO_OPERACION_SPOT, eventos, Evento.class);
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdTrabajo", idTrabajo);
		
		Map<Long, List<Evento>> eventosPorIdOperacion = sql.query(contexto.Connection, SQL_SELECT_EVENTO_OPERACION_SPOT, params, Evento.class).stream()
				.collect(Collectors.groupingBy(u -> Long.valueOf(u.NumeroOperacionConcepto)));
		
		SQL.exec(contexto.Connection, SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_SPOT);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.obtenerEventosPorIdOperacionSpot {}", fin - inicio);
		
		return eventosPorIdOperacion;
	}
	
	private void modificarOperaciones(List<OperacionSpot> operaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.modificarOperaciones");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(contexto.Connection, SQL_UPDATE_ESTADO_OPERACION, operaciones, OperacionSpot.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.modificarOperaciones {}", fin - inicio);
	}
	
	private void modificarRestoEventosAsociados(Map<Long, List<Evento>> eventosPorIdOperacionSpot) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.modificarRestoEventosAsociados");
		long inicio = System.currentTimeMillis();
		
		List<Evento> eventos = eventosPorIdOperacionSpot.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
		new SQL().batch(contexto.Connection, SQL_UPDATE_EVENTO, eventos, Evento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.modificarRestoEventosAsociados {}", fin - inicio);
	}
	
	private List<FormaPago> obtenerFormasPago(List<OperacionSpot> opearciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.obtenerFormasPago");
		long inicio = System.currentTimeMillis();
		
		List<Long> operacionIds = opearciones.stream().map(u -> u.IdOperacionSpot).distinct().collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdOperacionSpot", operacionIds);
		
		List<FormaPago> formasPago = new SQL().query(contexto.Connection, SQL_SELECT_FORMA_PAGO, params, FormaPago.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.obtenerFormasPago {}", fin - inicio);
		
		return formasPago;
	}
	
	private void crearOperacionSpotTesoreriaFacturacion(List<OperacionSpot> operaciones, List<FormaPago> formasPago) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.crearOperacionSpotTesoreriaFacturacion");
		long inicio = System.currentTimeMillis();
		
		SQL.exec(contexto.Connection, SQL_CREATE_LOCAL_TEMP_TABLE_OPERACION_SPOT);
		SQL.exec(contexto.Connection, SQL_CREATE_LOCAL_TEMP_TABLE_FORMA_PAGO);
		
		SQL sql = new SQL();
		sql.batch(contexto.Connection, SQL_INSERT_TEMP_OPERACION_SPOT, operaciones, OperacionSpot.class);
		sql.batch(contexto.Connection, SQL_INSERT_TEMP_FORMA_PAGO, formasPago, FormaPago.class);
		
		HashMap<String, Object> paramsEntrada = new HashMap<>(8);
		paramsEntrada.put("p_IdEmpresa", contexto.Entrada.p_IdEmpresa);
		paramsEntrada.put("p_IdUsuario", contexto.Entrada.p_IdUsuario);
		paramsEntrada.put("p_IdModulo", contexto.Entrada.p_IdModulo);
		paramsEntrada.put("p_UsaReglasNegocio", contexto.Entrada.p_UsaReglasNegocio);
		paramsEntrada.put("p_DatosOperacion", operaciones);
		paramsEntrada.put("p_DatosFormaPago", formasPago);
		
		HashMap<String, Class> paramsSalida = new HashMap<>(8);
		paramsSalida.put("p_out_IdOperacionSpot", POutIdOperacionSpot.class);
		paramsSalida.put("p_out_IngresoEgresoGenerado", POutIngresoEgresoGenerado.class);
		paramsSalida.put("p_out_AdvertenciasContador", Long.class);
		paramsSalida.put("p_out_AdvertenciasDetalle", POutAdvertenciasDetalle.class);
		paramsSalida.put("p_out_ErroresContador", Long.class);
		paramsSalida.put("p_out_ErroresDetalle", POutErroresDetalle.class);
		
		//aqui iba la llamada a crearOperacionSpotTesoreriaFacturacion pero no hacia nada
		//se debe reimplementar el envio a facturacion usando el java apropiado
		
		SQL.exec(contexto.Connection, SQL_DROP_LOCAL_TEMP_TABLE_OPERACION_SPOT);
		SQL.exec(contexto.Connection, SQL_DROP_LOCAL_TEMP_TABLE_FORMA_PAGO);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.crearOperacionSpotTesoreriaFacturacion {}", fin - inicio);
	}
	
	private void agregarLog(List<OperacionSpot> operaciones) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionSpot.agregarLog");
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
		for (OperacionSpot operacion : operaciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTrabajo = idTrabajo;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = contexto.Entrada.p_IdModulo;
			log.IdUsuario = contexto.Entrada.p_IdUsuario;
			log.IdEmpresa = contexto.Entrada.p_IdEmpresa;
			log.FechaSistemaLog = contexto.FechaSistemaStr;
			log.IdUsuarioAtendido = contexto.Entrada.p_IdUsuario;
			log.IdTipoRegistro = "SPOT";
			log.NombreProceso = "Confirmacion Operacion Spot excede Limite Paridad y Tipo Cambio";
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
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionSpot.agregarLog {}", fin - inicio);
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String jsonOptimus = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":254696,\"NumeroOperacionConcepto\":\"11314\",\"IdTipoEvento\":\"LITCSP\"}]}";
				EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(jsonOptimus, EntradaAutorizacion.class);
				ConfirmarRechazarAutorizacionOperacionSpot confirmarRechazarAutorizacionOperacionSpot = new ConfirmarRechazarAutorizacionOperacionSpot();
				
				SalidaAutorizacion salidaConfirmar = confirmarRechazarAutorizacionOperacionSpot.autorizarEvento(conn, entrada);
				conn.rollback();
				
				SalidaAutorizacion salidaRechazar = confirmarRechazarAutorizacionOperacionSpot.rechazarEvento(conn, entrada);
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
