package cl.bcs.ordenes;

import cl.bcs.facturacion.AnularComprobanteFinal;
import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.ordenes.pojo.Validacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RechazarInstruccionMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(RechazarInstruccionMasivo.class);

	private final String SQL_SELECT_COMPROBANTE_TESORERIA = loadResourceToString("cl/bcs/ordenes/sql/RechazarInstruccionMasivo_SelectComprobanteTesoreria.sql");
	private final String SQL_UPDATE_ESTADO_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/RechazarInstruccionMasivo_UpdateEstadoInstruccion.sql");

	private static final String ESTADO_LIQUIDADA = "LIQ";
	private static final String ESTADO_RECHAZADA = "RZD";

	private static final Set<Object> TIPOS_MENSAJES_SALIDA_ERROR;

	static {
		TIPOS_MENSAJES_SALIDA_ERROR = new HashSet<>(8);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_ERROR);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_RECHAZO);
	}

	public static class EntradaRechazarInstruccionMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public String p_Motivo;
		public String p_Observaciones;
		public String p_ComentarioRechazo;
		public List<Long> p_IdsInstrucciones;

		public EntradaRechazarInstruccionMasivo() {
			p_Motivo = "";
			p_Observaciones = "";
			p_ComentarioRechazo = "";
			p_IdsInstrucciones = new ArrayList<>();
		}
	}

	public static class SalidaRechazarInstruccionMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public SalidaRechazarInstruccionMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}

		public void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}
			P_OUT_MENSAJES.add(validacion.Mensaje);
		}

		public void agregarValidaciones(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}
		}
	}

	public static class InstruccionProceso extends BcsPojo {
		public Long IdInstruccion;
		public Long IdCliente;

		public Long IdPortafolioCliente;
		public String IdTipoOperacion;
		public String Nemotecnico;
		public String IdMonedaInstrumento;
		public String IdMonedaLiquidacion;
		public String IdEstadoPreRechazo;
		public String IdEstadoPostRechazo;
		public BigDecimal Cantidad;
		public BigDecimal TirTasa;
		public BigDecimal MontoNeto;
		public java.sql.Date FechaModificacion;

		public Long IdTransaccion;
		public Long IdOrden;
		public Long IdAsignacion;
		public Long IdMovimientoComprobante;
		public Long IdComprobante;

	}

	public static class ComprobanteTesoreria extends BcsPojo {
		public Long IdInstruccion;
		public Long IdTransaccion;
		public Long IdComprobanteFacturacion;
		public Long IdComprobanteTesoreria;
		public String IdEstado;
	}

	public static class ResultadoObtenerInstrucciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<InstruccionProceso> p_out_Instrucciones;

		public ResultadoObtenerInstrucciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Instrucciones = new ArrayList<>();
		}
	}

	public static class ResultadoAnularComprobantes extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public ResultadoAnularComprobantes() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class ResultadoAnularMovimientosComprobantes extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public ResultadoAnularMovimientosComprobantes() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class ResultadoAnularAsignaciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public ResultadoAnularAsignaciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class ResultadoAnularTransacciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public ResultadoAnularTransacciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRechazarInstruccionMasivo entrada = gson.fromJson(in, EntradaRechazarInstruccionMasivo.class);
		SalidaRechazarInstruccionMasivo salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaRechazarInstruccionMasivo logica(Connection conn, EntradaRechazarInstruccionMasivo entrada) throws Exception {
		logger.info("INICIO::RechazarInstruccionMasivo");
		long inicio = System.currentTimeMillis();

		SalidaRechazarInstruccionMasivo salida = new SalidaRechazarInstruccionMasivo();

		{
			String codigoProductoInstrucciones = "PRO.Instrucciones";
			boolean noEstaActivado = !"ACTIVO".equals(ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PRODUCTO", codigoProductoInstrucciones));
			if (noEstaActivado) {
				HashMap<String, String> params = new HashMap<>(2);
				params.put("CodigoParametro", codigoProductoInstrucciones);

				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_PRODUCTO_NO_ACTIVO", params, conn, entrada.p_IdEmpresa));

				return salida;
			}
		}

		final String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		List<Long> idsInstrucciones = entrada.p_IdsInstrucciones.stream().distinct().collect(Collectors.toList());
		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = obtenerInstrucciones(conn, entrada, idsInstrucciones, fechaSistema);
		salida.P_OUT_ERRORESCONTADOR += resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoObtenerInstrucciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}
		List<InstruccionProceso> instrucciones = resultadoObtenerInstrucciones.p_out_Instrucciones;

		salida.agregarValidaciones(validarInstrucciones(conn, entrada.p_IdEmpresa, instrucciones));
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		List<InstruccionProceso> instruccionesSinComprobante = new ArrayList<>(instrucciones.size());
		List<InstruccionProceso> instruccionesConComprobante = new ArrayList<>(instrucciones.size());
		List<InstruccionProceso> instruccionesConfirmadas = new ArrayList<>(instrucciones.size());

		for (InstruccionProceso instruccion : instrucciones) {
			if ("ING".equals(instruccion.IdEstadoPreRechazo)) {
				continue;

			} else if (instruccion.IdComprobante == null) {
				instruccionesSinComprobante.add(instruccion);
				instruccionesConfirmadas.add(instruccion);

			} else {
				instruccionesConComprobante.add(instruccion);
				instruccionesConfirmadas.add(instruccion);
			}
		}

		ResultadoAnularComprobantes resultadoAnularComprobantes = anularComprobantes(conn, entrada, instruccionesConComprobante);
		salida.P_OUT_ERRORESCONTADOR += resultadoAnularComprobantes.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoAnularComprobantes.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		ResultadoAnularMovimientosComprobantes resultadoAnularMovimientosComprobantes = anularMovimientosComprobantes(conn, entrada, instruccionesSinComprobante);
		salida.P_OUT_ERRORESCONTADOR += resultadoAnularMovimientosComprobantes.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoAnularMovimientosComprobantes.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		ResultadoAnularAsignaciones resultadoAnularAsignaciones = anularAsignaciones(conn, entrada, instruccionesConfirmadas);
		salida.P_OUT_ERRORESCONTADOR += resultadoAnularAsignaciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoAnularAsignaciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		ResultadoAnularTransacciones resultadoAnularTransacciones = anularTransacciones(conn, entrada, instruccionesConfirmadas, fechaSistema);
		salida.P_OUT_ERRORESCONTADOR += resultadoAnularTransacciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoAnularTransacciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		actualizarEstadoInstrucciones(conn, instrucciones);
		agregarBitacoras(conn, entrada, instrucciones);
		agregarLogs(conn, entrada, instrucciones);

		long fin = System.currentTimeMillis();
		logger.info("FIN::RechazarInstruccionMasivo {}", fin - inicio);

		return salida;
	}

	private List<PlataformaUtils.MensajeSalida> filtrarMensajesSalidaError(List<PlataformaUtils.MensajeSalida> mensajes) {
		logger.trace("INICIO::RechazarInstruccionMasivo.filtrarMensajesSalidaError");
		long inicio = System.currentTimeMillis();

		List<PlataformaUtils.MensajeSalida> mensajesFiltrados = mensajes.stream().filter(u -> TIPOS_MENSAJES_SALIDA_ERROR.contains(u.tipo)).collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.trace("FIN::RechazarInstruccionMasivo.filtrarMensajesSalidaError {}", fin - inicio);

		return mensajesFiltrados;
	}

	private List<ComprobanteTesoreria> obtenerComprobantesTesoreria(Connection conn, List<Long> idsInstrucciones) throws SQLException {
		logger.debug("INICIO::RechazarInstruccionMasivo.obtenerComprobantesTesoreria");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdsInstrucciones", idsInstrucciones);

		SQL sql = new SQL();
		List<ComprobanteTesoreria> comprobanteTesoreria = sql.query(conn, SQL_SELECT_COMPROBANTE_TESORERIA, params, ComprobanteTesoreria.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.obtenerComprobantesTesoreria {}", fin - inicio);

		return comprobanteTesoreria;
	}

	private List<Validacion> validarInstrucciones(Connection conn, Long idEmpresa, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.validarInstrucciones");
		long inicio = System.currentTimeMillis();

		List<Long> idsInstrucciones = instrucciones.stream().map(ins -> ins.IdInstruccion).collect(Collectors.toList());
		List<ComprobanteTesoreria> detallesIngresosEgresos = obtenerComprobantesTesoreria(conn, idsInstrucciones);
		Map<Long, ComprobanteTesoreria> comprobanteTesoreriaPorIdInstruccion = detallesIngresosEgresos.stream().collect(Collectors.toMap(ct -> ct.IdInstruccion, ct -> ct));

		HashMap<String, String> parametrosMensaje = new HashMap<>(2);
		List<Validacion> validaciones = new ArrayList<>();
		for (InstruccionProceso instruccion : instrucciones) {
			ComprobanteTesoreria comprobanteTesoreria = comprobanteTesoreriaPorIdInstruccion.get(instruccion.IdComprobante);

			boolean instruccionEstaLiquidada = ESTADO_LIQUIDADA.equals(instruccion.IdEstadoPreRechazo) || (Objects.nonNull(comprobanteTesoreria) && comprobanteTesoreria.IdEstado.equals(ESTADO_LIQUIDADA));
			if (instruccionEstaLiquidada) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.Mensaje = new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_INSTRUCCION_LIQUIDADA", parametrosMensaje, conn, idEmpresa);

				validaciones.add(validacion);
			}

			if (ESTADO_RECHAZADA.equals(instruccion.IdEstadoPreRechazo)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.Mensaje = new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_INSTRUCCION_RECHAZADA", parametrosMensaje, conn, idEmpresa);

				validaciones.add(validacion);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::RechazarInstruccionMasivo.validarInstrucciones {}", fin - inicio);

		return validaciones;
	}

	private ResultadoAnularComprobantes anularComprobantes(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.anularComprobantes");
		long inicio = System.currentTimeMillis();

		ResultadoAnularComprobantes resultadoAnularComprobantes = new ResultadoAnularComprobantes();
		if (instrucciones.size() > 0) {
			AnularComprobanteFinal.AnularComprobante_Entrada entradaAnularComprobanteFinal = new AnularComprobanteFinal.AnularComprobante_Entrada();
			entradaAnularComprobanteFinal.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularComprobanteFinal.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularComprobanteFinal.p_IdModulo = entrada.p_IdModulo;
			entradaAnularComprobanteFinal.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaAnularComprobanteFinal.p_EsIngresoAutomatico = "N";
			entradaAnularComprobanteFinal.p_EjecutaAutorizacion = "N";
			entradaAnularComprobanteFinal.p_Glosa = "";
			entradaAnularComprobanteFinal.p_simularOperacion = "N";
			entradaAnularComprobanteFinal.p_OmiteRegistroSinAutorizar = "N";
			entradaAnularComprobanteFinal.P_COMPROBANTEAANULAR = instrucciones
					.stream()
					.map(instruccion -> {
						AnularComprobanteFinal.Comprobante_Entrada comprobanteEntrada = new AnularComprobanteFinal.Comprobante_Entrada();
						comprobanteEntrada.IdComprobante = instruccion.IdComprobante;

						return comprobanteEntrada;
					})
					.collect(Collectors.toList());

			AnularComprobanteFinal anularComprobanteFinal = new AnularComprobanteFinal();
			AnularComprobanteFinal.AnularComprobante_Salida salidaAnularComprobanteFinal = anularComprobanteFinal.logica(conn, entradaAnularComprobanteFinal);

			resultadoAnularComprobantes.P_OUT_ERRORESCONTADOR += salidaAnularComprobanteFinal.P_OUT_ERRORESCONTADOR;
			resultadoAnularComprobantes.P_OUT_MENSAJES.addAll(salidaAnularComprobanteFinal.P_OUT_MENSAJES);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.anularComprobantes {}", fin - inicio);

		return resultadoAnularComprobantes;
	}

	private ResultadoAnularMovimientosComprobantes anularMovimientosComprobantes(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.anularMovimientosComprobantes");
		long inicio = System.currentTimeMillis();

		ResultadoAnularMovimientosComprobantes resultadoAnularMovimientosComprobantes = new ResultadoAnularMovimientosComprobantes();
		if (instrucciones.size() > 0) {

			AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada entradaAnularMovimientoComprobante = new AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada();
			entradaAnularMovimientoComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaAnularMovimientoComprobante.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaAnularMovimientoComprobante.p_simularOperacion = "N";
			entradaAnularMovimientoComprobante.p_EsIngresoAutomatico = "N";
			entradaAnularMovimientoComprobante.p_Glosa = "";
			entradaAnularMovimientoComprobante.p_MovimientoAAnular = instrucciones
					.stream()
					.map(instruccion -> {
						AnularMovimientoComprobante.Movimiento_Entrada movimientoEntrada = new AnularMovimientoComprobante.Movimiento_Entrada();
						movimientoEntrada.IdMovimientoComprobante = instruccion.IdMovimientoComprobante;

						return movimientoEntrada;
					})
					.collect(Collectors.toList());

			AnularMovimientoComprobante anularMovimientoComprobante = new AnularMovimientoComprobante();
			AnularMovimientoComprobante.AnularMovimientoComprobante_Salida salidaAnularMovimientoComprobante = anularMovimientoComprobante.logica(conn, entradaAnularMovimientoComprobante);

			resultadoAnularMovimientosComprobantes.P_OUT_ERRORESCONTADOR += salidaAnularMovimientoComprobante.P_OUT_ERRORESCONTADOR;
			resultadoAnularMovimientosComprobantes.P_OUT_MENSAJES.addAll(salidaAnularMovimientoComprobante.P_OUT_MENSAJES);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.anularMovimientosComprobantes {}", fin - inicio);

		return resultadoAnularMovimientosComprobantes;
	}

	/**
	 * Anula asignaciones y órdenes postreras
	 */
	private ResultadoAnularAsignaciones anularAsignaciones(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.anularAsignaciones");
		long inicio = System.currentTimeMillis();

		ResultadoAnularAsignaciones resultadoAnularAsignaciones = new ResultadoAnularAsignaciones();
		if (instrucciones.size() > 0) {
			AnularAsignaciones.AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones.AnularAsignaciones_Entrada();
			entradaAnularAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularAsignaciones.p_IdModulo = entrada.p_IdModulo;
			entradaAnularAsignaciones.asignacionesParaAnular = instrucciones
					.stream()
					.map(instruccion -> {
						AnularAsignaciones.Asignacion_Entrada asignacionEntrada = new AnularAsignaciones.Asignacion_Entrada();
						asignacionEntrada.IdAsignacion = instruccion.IdAsignacion;

						return asignacionEntrada;
					})
					.collect(Collectors.toList());

			AnularAsignaciones anularAsignaciones = new AnularAsignaciones();
			AnularAsignaciones.AnularAsignaciones_Salida salidaAnularAsignaciones = anularAsignaciones.logica(conn, entradaAnularAsignaciones);

			resultadoAnularAsignaciones.P_OUT_ERRORESCONTADOR += salidaAnularAsignaciones.P_OUT_ERRORESCONTADOR;
			resultadoAnularAsignaciones.P_OUT_MENSAJES.addAll(salidaAnularAsignaciones.p_out_Mensajes);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.anularAsignaciones {}", fin - inicio);

		return resultadoAnularAsignaciones;
	}

	private ResultadoAnularTransacciones anularTransacciones(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones, String fechaSistema) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.anularTransacciones");
		long inicio = System.currentTimeMillis();

		ResultadoAnularTransacciones resultadoAnularTransacciones = new ResultadoAnularTransacciones();
		if (instrucciones.size() > 0) {
			AnularTransaccionMasiva.EntradaAnularTransaccionMasiva entradaAnularTransaccionMasiva = new AnularTransaccionMasiva.EntradaAnularTransaccionMasiva();
			entradaAnularTransaccionMasiva.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularTransaccionMasiva.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularTransaccionMasiva.p_IdModulo = entrada.p_IdModulo;
			entradaAnularTransaccionMasiva.p_Reproceso = "N";
			entradaAnularTransaccionMasiva.p_transacciones = instrucciones
					.stream()
					.map(instruccion -> {
						AnularTransaccionMasiva.InTransaccion transaccionEntrada = new AnularTransaccionMasiva.InTransaccion();
						transaccionEntrada.IdTransaccion = instruccion.IdTransaccion;
						transaccionEntrada.FechaAnulacion = fechaSistema;
						transaccionEntrada.Motivo = entrada.p_Motivo;
						transaccionEntrada.Observaciones = entrada.p_Observaciones;

						return transaccionEntrada;
					})
					.collect(Collectors.toList());

			AnularTransaccionMasiva anularTransaccionMasiva = new AnularTransaccionMasiva();
			AnularTransaccionMasiva.SalidaAnularTransaccionMasiva salidaAnularTransaccionMasiva = anularTransaccionMasiva.logica(conn, entradaAnularTransaccionMasiva);

			resultadoAnularTransacciones.P_OUT_ERRORESCONTADOR += salidaAnularTransaccionMasiva.P_OUT_ERRORESCONTADOR;
			resultadoAnularTransacciones.P_OUT_MENSAJES.addAll(salidaAnularTransaccionMasiva.P_OUT_MENSAJES);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.anularTransacciones {}", fin - inicio);

		return resultadoAnularTransacciones;
	}

	private void actualizarEstadoInstrucciones(Connection conn, List<InstruccionProceso> instrucciones) throws SQLException {
		logger.debug("INICIO::RechazarInstruccionMasivo.actualizarEstadoInstrucciones");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ESTADO_INSTRUCCION, instrucciones, InstruccionProceso.class);

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::RechazarInstruccionMasivo.actualizarEstadoInstrucciones {}", fin - inicio);
	}

	private ResultadoObtenerInstrucciones obtenerInstrucciones(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<Long> idsInstrucciones, String fechaSistema) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.obtenerInstrucciones");
		long inicio = System.currentTimeMillis();

		ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo entradaConsultarInstruccionMasivo = new ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo();
		entradaConsultarInstruccionMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarInstruccionMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarInstruccionMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarInstruccionMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultarInstruccionMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaConsultarInstruccionMasivo.p_IdsInstrucciones = idsInstrucciones;

		ConsultarInstruccionMasivo consultarInstruccionMasivo = new ConsultarInstruccionMasivo();
		ConsultarInstruccionMasivo.SalidaConsultarInstruccionMasivo salidaConsultarInstruccionMasivo = consultarInstruccionMasivo.logica(conn, entradaConsultarInstruccionMasivo);

		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = new ResultadoObtenerInstrucciones();
		resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR += salidaConsultarInstruccionMasivo.P_OUT_ERRORESCONTADOR;
		resultadoObtenerInstrucciones.P_OUT_MENSAJES.addAll(salidaConsultarInstruccionMasivo.P_OUT_MENSAJES);

		java.sql.Date fechaSistemaDate = Fecha.toDate(fechaSistema);
		resultadoObtenerInstrucciones.p_out_Instrucciones = salidaConsultarInstruccionMasivo.p_out_Instrucciones.stream().map(instruccion -> {
			InstruccionProceso ins = new InstruccionProceso();
			ins.IdInstruccion = instruccion.IdInstruccion;
			ins.IdCliente = instruccion.IdCliente;
			ins.IdPortafolioCliente = instruccion.IdPortafolioCliente;
			ins.IdTipoOperacion = instruccion.IdTipoOperacion;
			ins.Cantidad = instruccion.Cantidad;
			ins.TirTasa = instruccion.TirTasa;
			ins.MontoNeto = instruccion.MontoNeto;
			ins.Nemotecnico = instruccion.Nemotecnico;
			ins.IdMonedaInstrumento = instruccion.IdMonedaInstrumento;
			ins.IdMonedaLiquidacion = instruccion.IdMonedaLiquidacion;
			ins.IdTransaccion = instruccion.IdTransaccion;
			ins.IdOrden = instruccion.IdOrden;
			ins.IdAsignacion = instruccion.IdAsignacion;
			ins.IdMovimientoComprobante = instruccion.IdMovimientoComprobante;
			ins.IdComprobante = instruccion.IdComprobante;
			ins.IdEstadoPreRechazo = instruccion.IdEstadoInstruccion;
			ins.IdEstadoPostRechazo = ESTADO_RECHAZADA;
			ins.FechaModificacion = fechaSistemaDate;

			return ins;
		}).collect(Collectors.toList());

		if (resultadoObtenerInstrucciones.p_out_Instrucciones.isEmpty()) {
			resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR++;
			resultadoObtenerInstrucciones.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_HAY_INSTRUCCIONES_POR_RECHAZAR", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.obtenerInstrucciones {}", fin - inicio);

		return resultadoObtenerInstrucciones;
	}

	private void agregarBitacoras(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.agregarBitacoras");
		long inicio = System.currentTimeMillis();

		AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo entradaAgregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo();
		entradaAgregarEventoBitacoraMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarEventoBitacoraMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarEventoBitacoraMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarEventoBitacoraMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAgregarEventoBitacoraMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaAgregarEventoBitacoraMasivo.p_eventos = instrucciones.stream().map(instruccion -> {
			AgregarEventoBitacoraMasivo.EventoBitacora eventoBitacora = new AgregarEventoBitacoraMasivo.EventoBitacora();
			eventoBitacora.IdInterno = instruccion.IdInstruccion;
			eventoBitacora.NombreEntidad = "INSTRUCCION";
			eventoBitacora.IdEntidad = instruccion.IdInstruccion;
			eventoBitacora.IdOrden = instruccion.IdOrden == null ? 0L : instruccion.IdOrden;
			eventoBitacora.IdAsignacion = instruccion.IdAsignacion == null ? 0L : instruccion.IdAsignacion;
			eventoBitacora.IdTransaccion = instruccion.IdTransaccion == null ? 0L : instruccion.IdTransaccion;
			eventoBitacora.MensajeEventoBitacora = entrada.p_ComentarioRechazo;
			eventoBitacora.IdTipoEventoBitacora = "INSTRUCCION_RECHAZADA";

			return eventoBitacora;
		}).collect(Collectors.toList());

		AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
		agregarEventoBitacoraMasivo.logica(conn, entradaAgregarEventoBitacoraMasivo);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.agregarBitacoras {}", fin - inicio);
	}

	private void agregarLogs(Connection conn, EntradaRechazarInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::RechazarInstruccionMasivo.agregarLogs");
		long inicio = System.currentTimeMillis();

		long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = idTrabajo;

		for (InstruccionProceso instruccion : instrucciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = instruccion.IdInstruccion;
			log.IdTipoLog = "RECHAZAR";
			log.IdModulo = "ORD";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = instruccion.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = instruccion.IdPortafolioCliente;
			log.IdTipoRegistro = "RECHAZAR INSTRUCCION";
			log.IdTipoOperacion = instruccion.IdTipoOperacion;
			log.Folio = instruccion.IdInstruccion;
			log.Cantidad = instruccion.Cantidad;
			log.PrecioTasa = instruccion.TirTasa;
			log.Monto = instruccion.MontoNeto;
			log.Nemo = instruccion.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";
			log.IdMonedaOrigen = instruccion.IdMonedaInstrumento;
			log.IdMonedaLiquidacion = instruccion.IdMonedaLiquidacion;
			log.TipoCambio = "";
			log.IdPersona = 0L;

			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog estadoInstruccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			estadoInstruccionDetalleLog.IdTrabajo = idTrabajo;
			estadoInstruccionDetalleLog.IdCabeceraLog = instruccion.IdInstruccion;
			estadoInstruccionDetalleLog.SubRegistro = "Instruccion";
			estadoInstruccionDetalleLog.CampoRegistro = "IdEstadoInstruccion";
			estadoInstruccionDetalleLog.ValorAntiguo = instruccion.IdEstadoPreRechazo;
			estadoInstruccionDetalleLog.ValorNuevo = instruccion.IdEstadoPostRechazo;

			entradaAgregarLog.p_LogDetalle.add(estadoInstruccionDetalleLog);
		}

		AgregarLog agregarLog = new AgregarLog();
		agregarLog.logica(conn, entradaAgregarLog);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarInstruccionMasivo.agregarLogs {}", fin - inicio);
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_Motivo\":\"Motivo\",\"p_Observaciones\":\"Observaciones em si\",\"p_IdsInstrucciones\":[48]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaRechazarInstruccionMasivo entradaRechazarInstruccionMasivo = gson.fromJson(jsonRequest, EntradaRechazarInstruccionMasivo.class);
			System.out.println("Entrada");
			System.out.println(entradaRechazarInstruccionMasivo);

			try {
				RechazarInstruccionMasivo rechazarInstruccionMasivo = new RechazarInstruccionMasivo();
				SalidaRechazarInstruccionMasivo salidaRechazarInstruccionMasivo = rechazarInstruccionMasivo.logica(conn, entradaRechazarInstruccionMasivo);
				System.out.println("Salida");
				System.out.println(salidaRechazarInstruccionMasivo);
				if (salidaRechazarInstruccionMasivo.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
