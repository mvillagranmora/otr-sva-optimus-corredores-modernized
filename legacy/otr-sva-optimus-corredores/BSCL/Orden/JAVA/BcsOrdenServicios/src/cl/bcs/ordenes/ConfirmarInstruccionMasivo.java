package cl.bcs.ordenes;

import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.ordenes.pojo.Asignacion;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfirmarInstruccionMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConfirmarInstruccionMasivo.class);

	private static final Set<Object> TIPOS_MENSAJES_SALIDA_ERROR;

	static {
		TIPOS_MENSAJES_SALIDA_ERROR = new HashSet<>(8);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_ERROR);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA);
		TIPOS_MENSAJES_SALIDA_ERROR.add(PlataformaUtils.MensajeSalida.MENSAJE_RECHAZO);
	}

	private final String SQL_SELECT_INDICADORES_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarInstruccionMasivo_SelectIndicadoresPortafolios.sql");
	private final String SQL_SELECT_PREFERENCIAS_PAGOS_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarInstruccionMasivo_SelectPreferenciasPagosPortafolios.sql");
	private final String SQL_UPDATE_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarInstruccionMasivo_UpdateInstruccion.sql");

	public static class EntradaConfirmarInstruccionMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public String p_ComentarioConfirmacion;
		public List<Long> p_IdsInstrucciones;

		public EntradaConfirmarInstruccionMasivo() {
			p_ComentarioConfirmacion = "";
			p_IdsInstrucciones = new ArrayList<>();
		}
	}

	public static class SalidaConfirmarInstruccionMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoOperacionesConfirmacion> P_OUT_RESULTADOS;

		public SalidaConfirmarInstruccionMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_RESULTADOS = new ArrayList<>();
		}
	}

	public static class ResultadoCrearTransacciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Transaccion> P_OUT_TRANSACCIONES;

		public ResultadoCrearTransacciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_TRANSACCIONES = new ArrayList<>();
		}
	}

	public static class ResultadoObtenerInstrucciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<ConsultarInstruccionMasivo.Instruccion> p_out_Instrucciones;

		public ResultadoObtenerInstrucciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Instrucciones = new ArrayList<>();
		}
	}

	public static class ResultadoObtenerCobros extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<CrearOrdenesPostreras.Cobro_Fijo> P_OUT_COBROS;

		public ResultadoObtenerCobros() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_COBROS = new ArrayList<>();
		}
	}

	public static class ResultadoCrearOrdenesPostreras extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<CrearOrdenesPostreras.Cobro_Salida> P_OUT_COBROS;
		public List<Resultado> P_OUT_RESULTADOS;

		public static class Resultado extends BcsPojo {
			public Long IdAsignacion;
			public Long IdOrden;
			public Long IdInstruccion;
		}

		public ResultadoCrearOrdenesPostreras() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_COBROS = new ArrayList<>();
			P_OUT_RESULTADOS = new ArrayList<>();
		}
	}

	public static class ResultadoEnviarFacturas extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<EnviarAsignacionesFacturar.Resultado> P_OUT_RESULTADOS;

		public ResultadoEnviarFacturas() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_RESULTADOS = new ArrayList<>();
		}
	}

	public static class ResultadoOperacionesConfirmacion extends BcsPojo {
		public Long IdInstruccion;
		public Long IdTransaccion;
		public Long IdOrden;
		public Long IdAsignacion;
		public Long IdComprobante;
		public Long IdMovimientoComprobante;
		public String IdEstadoInstruccion;
	}

	public static class Portafolio extends BcsPojo {
		public Long IdPortafolio;
		public Long IdInstruccion;
		public IndicadoresPortafolio Indicadores;
		public List<PreferenciaPagoPorDefecto> PreferenciasPagosPorDefecto;

		public static class Indicador extends BcsPojo {
			public Long IdTrabajo;
			public Long IdPortafolio;
			public String CodigoTipoIndicador;
			public String ValorIndicador;
		}

		public static class PreferenciaPagoPorDefecto extends BcsPojo {
			public Long IdPortafolio;
			public Long IdPreferenciaPagoPortafolio;
			public String IdMoneda;
			public String IdTipoOperacion;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarInstruccionMasivo entrada = gson.fromJson(in, EntradaConfirmarInstruccionMasivo.class);
		SalidaConfirmarInstruccionMasivo salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaConfirmarInstruccionMasivo logica(Connection conn, EntradaConfirmarInstruccionMasivo entrada) throws Exception {
		logger.info("INICIO::ConfirmarInstruccionMasivo");
		long inicio = System.currentTimeMillis();

		SalidaConfirmarInstruccionMasivo salida = new SalidaConfirmarInstruccionMasivo();

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

		List<Long> idsInstrucciones = entrada.p_IdsInstrucciones.stream().distinct().collect(Collectors.toList());

		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = obtenerInstrucciones(conn, entrada, idsInstrucciones);
		salida.P_OUT_ERRORESCONTADOR += resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoObtenerInstrucciones.P_OUT_MENSAJES); //INSTRUCCIONES FALTANTES
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);
			return salida;
		}
		resultadoObtenerInstrucciones.p_out_Instrucciones
				.stream()
				.filter(instruccion->instruccion.IdInstruccion != null)
				.forEach(instruccion->{
					HashMap<String, String> param = new HashMap<>(2);
					param.put("IdTransaccion", String.valueOf(instruccion.IdInstruccion));
					try {
						salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, PlataformaUtils.MensajeSalida.MENSAJE_INFORMACION, "FOLIO_TRANSACCION_INFORMATIVO", param, conn, entrada.p_IdEmpresa));
					} catch (Exception e) {
						salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE","FOLIO_TRANSACCION_INFORMATIVO sin traduccion."));
					}
				});
		
		ResultadoCrearTransacciones resultadoCrearTransacciones = crearTransacciones(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		salida.P_OUT_ERRORESCONTADOR += resultadoCrearTransacciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoCrearTransacciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		ResultadoObtenerCobros resultadoObtenerCobros = obtenerCobros(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		salida.P_OUT_ERRORESCONTADOR += resultadoObtenerCobros.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoObtenerCobros.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		Map<Long, Portafolio> portafolioPorIdInstruccion = obtenerPortafoliosClientesPorIdInstruccion(conn, resultadoObtenerInstrucciones.p_out_Instrucciones);
		Map<Long, Transaccion> transaccionPorIdInstruccion = resultadoCrearTransacciones.P_OUT_TRANSACCIONES.stream().collect(Collectors.toMap(transaccion -> transaccion.IdInterno, transaccion -> transaccion));

		ResultadoCrearOrdenesPostreras resultadoCrearOrdenesPostreras = crearOrdenesPostreras(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones, portafolioPorIdInstruccion, transaccionPorIdInstruccion, resultadoObtenerCobros.P_OUT_COBROS);
		salida.P_OUT_ERRORESCONTADOR += resultadoCrearOrdenesPostreras.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoCrearOrdenesPostreras.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		ResultadoEnviarFacturas resultadoEnviarFacturas = enviarFacturas(conn, entrada, resultadoCrearOrdenesPostreras);
		salida.P_OUT_ERRORESCONTADOR += resultadoEnviarFacturas.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoEnviarFacturas.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES = filtrarMensajesSalidaError(salida.P_OUT_MENSAJES);

			return salida;
		}

		List<ResultadoOperacionesConfirmacion> resultadosOperaciones = obtenerResultadoOperacionesConfirmacion(entrada.p_IdsInstrucciones, resultadoCrearOrdenesPostreras, resultadoEnviarFacturas);
		actualizarInstrucciones(conn, resultadosOperaciones);
		agregarBitacoras(conn, entrada, resultadosOperaciones);
		agregarLogs(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones, resultadosOperaciones);

		salida.P_OUT_RESULTADOS.addAll(resultadosOperaciones);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarInstruccionMasivo {}", fin - inicio);

		return salida;
	}

	private List<PlataformaUtils.MensajeSalida> filtrarMensajesSalidaError(List<PlataformaUtils.MensajeSalida> mensajes) {
		logger.trace("INICIO::ConfirmarInstruccionMasivo.filtrarMensajesSalidaError");
		long inicio = System.currentTimeMillis();

		List<PlataformaUtils.MensajeSalida> mensajesFiltrados = mensajes.stream().filter(u -> TIPOS_MENSAJES_SALIDA_ERROR.contains(u.tipo)).collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarInstruccionMasivo.filtrarMensajesSalidaError {}", fin - inicio);

		return mensajesFiltrados;
	}

	private ResultadoObtenerInstrucciones obtenerInstrucciones(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<Long> idsInstrucciones) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.obtenerInstrucciones");
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
		resultadoObtenerInstrucciones.p_out_Instrucciones.addAll(salidaConsultarInstruccionMasivo.p_out_Instrucciones);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.obtenerInstrucciones {}", fin - inicio);

		return resultadoObtenerInstrucciones;
	}

	private Map<Long, Portafolio> obtenerPortafoliosClientesPorIdInstruccion(Connection conn, List<ConsultarInstruccionMasivo.Instruccion> instrucciones) throws SQLException {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.obtenerPortafoliosClientes");
		long inicio = System.currentTimeMillis();

		List<Long> idsPortafolios = instrucciones.stream().map(instruccion -> instruccion.IdPortafolioCliente).distinct().collect(Collectors.toList());
		SQL sql = new SQL();

		HashMap<String, Object> indicadoresParams = new HashMap<>(2);
		indicadoresParams.put("IdsPortafolios", idsPortafolios);
		Map<Long, IndicadoresPortafolio> indicadoresPortafolioPorIdPortafolio = sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIOS, indicadoresParams, IndicadoresPortafolio.class)
				.stream()
				.collect(Collectors.toMap(u -> u.IdPortafolio, u -> u));

		HashMap<String, Object> preferenciasPagosParams = new HashMap<>(2);
		preferenciasPagosParams.put("IdsPortafolios", new ArrayList<>(idsPortafolios));

		Map<Long, List<Portafolio.PreferenciaPagoPorDefecto>> preferenciasPagosDefectoPorIdPortafolio = sql.query(conn, SQL_SELECT_PREFERENCIAS_PAGOS_PORTAFOLIOS, preferenciasPagosParams, Portafolio.PreferenciaPagoPorDefecto.class)
				.stream()
				.collect(Collectors.groupingBy(pref -> pref.IdPortafolio));

		Map<Long, Portafolio> portafolioPorIdInstruccion = new HashMap<>(instrucciones.size());
		for (ConsultarInstruccionMasivo.Instruccion instruccion : instrucciones) {
			Portafolio portafolio = new Portafolio();
			portafolio.IdInstruccion = instruccion.IdInstruccion;
			portafolio.IdPortafolio = instruccion.IdPortafolioCliente;
			portafolio.Indicadores = indicadoresPortafolioPorIdPortafolio.getOrDefault(instruccion.IdPortafolioCliente, new IndicadoresPortafolio());
			portafolio.PreferenciasPagosPorDefecto = preferenciasPagosDefectoPorIdPortafolio.getOrDefault(portafolio.IdPortafolio, Collections.emptyList());

			portafolioPorIdInstruccion.put(portafolio.IdInstruccion, portafolio);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.obtenerPortafoliosClientes {}", fin - inicio);

		return portafolioPorIdInstruccion;
	}

	private ResultadoCrearTransacciones crearTransacciones(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<ConsultarInstruccionMasivo.Instruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.crearTransacciones");
		long inicio = System.currentTimeMillis();

		CrearTransaccionInstruccionMasivo.EntradaCrearTransaccionInstruccionMasivo entradaCrearTransaccionInstruccionMasivo = new CrearTransaccionInstruccionMasivo.EntradaCrearTransaccionInstruccionMasivo();
		entradaCrearTransaccionInstruccionMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransaccionInstruccionMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransaccionInstruccionMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearTransaccionInstruccionMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearTransaccionInstruccionMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaCrearTransaccionInstruccionMasivo.p_Instrucciones.addAll(instrucciones);

		CrearTransaccionInstruccionMasivo crearTransaccionInstruccionMasivo = new CrearTransaccionInstruccionMasivo();
		CrearTransaccionInstruccionMasivo.SalidaCrearTransaccionInstruccionMasivo salidaCrearTransaccionInstruccionMasivo = crearTransaccionInstruccionMasivo.logica(conn, entradaCrearTransaccionInstruccionMasivo);

		ResultadoCrearTransacciones resultado = new ResultadoCrearTransacciones();
		resultado.P_OUT_ERRORESCONTADOR += salidaCrearTransaccionInstruccionMasivo.P_OUT_ERRORESCONTADOR;
		resultado.P_OUT_MENSAJES.addAll(salidaCrearTransaccionInstruccionMasivo.P_OUT_MENSAJES);
		resultado.P_OUT_TRANSACCIONES.addAll(salidaCrearTransaccionInstruccionMasivo.p_out_Transacciones);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.crearTransacciones {}", fin - inicio);

		return resultado;
	}

	private ResultadoCrearOrdenesPostreras crearOrdenesPostreras(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<ConsultarInstruccionMasivo.Instruccion> instrucciones, Map<Long, Portafolio> portafolioPorIdInstruccion, Map<Long, Transaccion> transaccionPorIdInstruccion, List<CrearOrdenesPostreras.Cobro_Fijo> cobros) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.crearOrdenesPostreras");
		long inicio = System.currentTimeMillis();

		CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada();
		entradaCrearOrdenesPostreras.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearOrdenesPostreras.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearOrdenesPostreras.p_IdModulo = entrada.p_IdModulo;
		entradaCrearOrdenesPostreras.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearOrdenesPostreras.p_Ordenes = obtenerOrdenesDesdeInstruccion(instrucciones, portafolioPorIdInstruccion, transaccionPorIdInstruccion);
		entradaCrearOrdenesPostreras.p_CobrosFijos = cobros;

		CrearOrdenesPostreras crearOrdenesPostreras = new CrearOrdenesPostreras();
		CrearOrdenesPostreras.CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = crearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);

		ResultadoCrearOrdenesPostreras resultadoCrearOrdenesPostreras = new ResultadoCrearOrdenesPostreras();
		resultadoCrearOrdenesPostreras.P_OUT_ERRORESCONTADOR += salidaCrearOrdenesPostreras.P_OUT_ERRORESCONTADOR;
		resultadoCrearOrdenesPostreras.P_OUT_MENSAJES.addAll(salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
		resultadoCrearOrdenesPostreras.P_OUT_COBROS.addAll(salidaCrearOrdenesPostreras.P_OUT_COBROS);

		List<ResultadoCrearOrdenesPostreras.Resultado> resultados = salidaCrearOrdenesPostreras.P_OUT_RESULTADO.stream().map(res -> {
			ResultadoCrearOrdenesPostreras.Resultado resultado = new ResultadoCrearOrdenesPostreras.Resultado();
			resultado.IdOrden = res.IdOrden;
			resultado.IdAsignacion = res.IdAsignacion;
			resultado.IdInstruccion = res.IdInterno;

			return resultado;
		}).collect(Collectors.toList());
		resultadoCrearOrdenesPostreras.P_OUT_RESULTADOS.addAll(resultados);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.crearOrdenesPostreras {}", fin - inicio);

		return resultadoCrearOrdenesPostreras;
	}

	private List<CrearOrdenesPostreras.OrdenPostrera_Entrada> obtenerOrdenesDesdeInstruccion(List<ConsultarInstruccionMasivo.Instruccion> instrucciones, Map<Long, Portafolio> portafolioPorIdInstruccion, Map<Long, Transaccion> transaccionPorIdInstruccion) {
		logger.trace("INICIO.ConfirmarInstruccionMasivo.obtenerOrdenesDesdeInstruccion");
		long inicio = System.currentTimeMillis();

		List<CrearOrdenesPostreras.OrdenPostrera_Entrada> ordenes = new ArrayList<>(instrucciones.size());
		for (ConsultarInstruccionMasivo.Instruccion instruccion : instrucciones) {
			Portafolio portafolio = portafolioPorIdInstruccion.get(instruccion.IdInstruccion);
			Transaccion transaccion = transaccionPorIdInstruccion.get(instruccion.IdInstruccion);

			CrearOrdenesPostreras.OrdenPostrera_Entrada ordenPostrera = new CrearOrdenesPostreras.OrdenPostrera_Entrada();
			ordenPostrera.IdInterno = transaccion.IdInterno;
			ordenPostrera.IdTransaccion = transaccion.IdTransaccion;
			ordenPostrera.IdBoveda = transaccion.IdBoveda;

			ordenPostrera.IdFormaCustodia = "NOM";
			if (("RV".equals(instruccion.IdClasificacionTipoInstrumento) && "S".equals(portafolio.Indicadores.OperaCustodiaRV))
					|| ("DV".equals(instruccion.IdClasificacionTipoInstrumento) && "S".equals(portafolio.Indicadores.OperaCustodiaRV))
					|| ("RF".equals(instruccion.IdClasificacionTipoInstrumento) && "S".equals(portafolio.Indicadores.OperaCustodiaRFIF))
					|| ("IF".equals(instruccion.IdClasificacionTipoInstrumento) && "S".equals(portafolio.Indicadores.OperaCustodiaRFIF))
			) {
				ordenPostrera.IdFormaCustodia = "CUS";
			}

			ordenPostrera.IdPortafolio = portafolio.IdPortafolio;
			ordenPostrera.IdCuentaCustodioPortafolio = instruccion.IdCuentaCustodioPortafolio;
			ordenPostrera.IdMonedaLiquidacionOrden = instruccion.IdMonedaLiquidacion;
			ordenPostrera.Glosa = instruccion.Glosa;
			ordenPostrera.EsFacturaInmediata = "N";
			ordenPostrera.FolioCartera = transaccion.FolioCartera;
			ordenPostrera.Cantidad = transaccion.Cantidad;
			ordenPostrera.ValorRescate = transaccion.ValorRescate;
			ordenPostrera.TipoCambio = BigDecimal.ZERO;
			ordenPostrera.TasaVentaCorta = BigDecimal.ZERO;
			ordenPostrera.PlazoVentaCorta = 0L;
			ordenPostrera.PagoCobroCuentaInversion = portafolio.Indicadores.OperaCuentaInversion;
			if ("N".equals(ordenPostrera.PagoCobroCuentaInversion)) {
				ordenPostrera.IdPreferenciaPagoPortafolio = 0L;
				for (Portafolio.PreferenciaPagoPorDefecto preferenciaPagoPorDefecto : portafolio.PreferenciasPagosPorDefecto) {
					if (ordenPostrera.IdMonedaLiquidacionOrden.equals(preferenciaPagoPorDefecto.IdMoneda) && transaccion.IdTipoOperacion.equals(preferenciaPagoPorDefecto.IdTipoOperacion)) {
						ordenPostrera.IdPreferenciaPagoPortafolio = preferenciaPagoPorDefecto.IdPreferenciaPagoPortafolio;
						break;
					}
				}
			}

			ordenPostrera.PorcentajeAllIn = new BigDecimal("100");
			ordenPostrera.FechaRecepcion = Fecha.toStringDate(transaccion.FechaIngresoOferta);
			ordenPostrera.CodigoIFRS = portafolio.Indicadores.CodigoIfrs;
			ordenPostrera.FlujoOrigen = "INGRESOINSTRUCCION";
			ordenPostrera.FechaTrade = transaccion.FechaIngresoOferta;//instruccion.FechaTrade;--para que quede con fecha y hora de la Transaccion

			ordenes.add(ordenPostrera);
		}

		long fin = System.currentTimeMillis();
		logger.trace("INICIO.ConfirmarInstruccionMasivo.obtenerOrdenesDesdeInstruccion {}", fin - inicio);

		return ordenes;
	}

	private ResultadoObtenerCobros obtenerCobros(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<ConsultarInstruccionMasivo.Instruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.obtenerCobros");
		long inicio = System.currentTimeMillis();

		ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada entradaConsultarCobroOrdenes = new ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada();
		entradaConsultarCobroOrdenes.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCobroOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCobroOrdenes.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCobroOrdenes.p_Operaciones = instrucciones.stream().map(ins -> {
			ConsultarCobrosOrdenes.InformacionOperacion informacionOperacion = new ConsultarCobrosOrdenes.InformacionOperacion();
			informacionOperacion.IdBolsa = ins.IdBolsa;
			informacionOperacion.IdInterno = ins.IdInstruccion;
			informacionOperacion.IdPortafolio = ins.IdPortafolioCliente;
			informacionOperacion.IdTipoOperacion = ins.IdTipoOperacionInstruccion;
			informacionOperacion.IdCorredoraCorresponsal = 0L;
			informacionOperacion.IdTipoProducto = ins.IdTipoProductoInstruccion;
			informacionOperacion.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;

			return informacionOperacion;
		}).collect(Collectors.toList());

		ConsultarCobrosOrdenes consultarCobrosOrdenes = new ConsultarCobrosOrdenes();
		ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Salida salidaConsultarCobrosOrdenes = consultarCobrosOrdenes.logica(conn, entradaConsultarCobroOrdenes);

		Map<Long, List<CrearOrdenesPostreras.Cobro_Fijo>> cobrosPorIdInstruccion = salidaConsultarCobrosOrdenes.p_out_MatrizOperacional.stream().map(mp -> {
			CrearOrdenesPostreras.Cobro_Fijo cobroFijo = new CrearOrdenesPostreras.Cobro_Fijo();
			cobroFijo.IdInterno = mp.IdInterno;
			cobroFijo.IdInternoCobro = mp.IdInterno;
			cobroFijo.IdPortafolio = mp.IdPortafolio;
			cobroFijo.IdBolsa = mp.IdBolsa;
			cobroFijo.IdTipoProducto = mp.IdTipoProducto;
			cobroFijo.IdTipoOperacion = mp.IdTipoOperacion;
			cobroFijo.IdTipoCanalIngreso = mp.IdTipoCanalIngreso;
			cobroFijo.IdCobro = mp.IdCobro;
			cobroFijo.ValorCobro = mp.ValorCobro;
			cobroFijo.FormaCobro = mp.FormaCobro;
			cobroFijo.ValorTopeMinimoOrden = mp.ValorTopeMinimoOrden;
			cobroFijo.IdMoneda = mp.IdMoneda;
			cobroFijo.CodigoCobroClasificacion = mp.CodigoCobroClasificacion;
			cobroFijo.PermiteCambiarValor = mp.PermiteCambiarValor;

			return cobroFijo;
		}).collect(Collectors.groupingBy(u -> u.IdInterno));

		ResultadoObtenerCobros resultadoObtenerCobros = new ResultadoObtenerCobros();
		resultadoObtenerCobros.P_OUT_COBROS.addAll(cobrosPorIdInstruccion.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
		resultadoObtenerCobros.P_OUT_MENSAJES.addAll(salidaConsultarCobrosOrdenes.p_out_Mensajes);
		resultadoObtenerCobros.P_OUT_ERRORESCONTADOR += PlataformaUtils.MensajeSalida.getNumeroErrores(resultadoObtenerCobros.P_OUT_MENSAJES);

		HashMap<String, String> mensajeParams = new HashMap<>(8);
		for (ConsultarInstruccionMasivo.Instruccion instruccion : instrucciones) {
			List<CrearOrdenesPostreras.Cobro_Fijo> cobros = cobrosPorIdInstruccion.getOrDefault(instruccion.IdInstruccion, Collections.emptyList());
			if (cobros.isEmpty()) {
				mensajeParams.put("IdPortafolio", String.valueOf(instruccion.IdPortafolioCliente));
				mensajeParams.put("IdCliente", String.valueOf(instruccion.IdCliente));
				mensajeParams.put("IdTipoProducto", instruccion.IdTipoProductoInstruccion);
				mensajeParams.put("IdTipoOperacion", instruccion.IdTipoOperacionInstruccion);

				resultadoObtenerCobros.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_PORTAFOLIO_SIN_COBROS_CONFIGURADOS_PARA_PRODUCTO", mensajeParams, conn, entrada.p_IdEmpresa));
				resultadoObtenerCobros.P_OUT_ERRORESCONTADOR++;

				mensajeParams.clear();
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.obtenerCobros {}", fin - inicio);

		return resultadoObtenerCobros;
	}

	private ResultadoEnviarFacturas enviarFacturas(Connection conn, EntradaConfirmarInstruccionMasivo entrada, ResultadoCrearOrdenesPostreras resultadoCrearOrdenesPostreras) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.enviarFacturas");
		long inicio = System.currentTimeMillis();

		String generaComprobante = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.InstruccionGeneraCompFactAuto");

		EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada();
		entradaEnviarAsignacionesFacturar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEnviarAsignacionesFacturar.p_IdUsuario = entrada.p_IdUsuario;
		entradaEnviarAsignacionesFacturar.p_IdModulo = entrada.p_IdModulo;
		entradaEnviarAsignacionesFacturar.p_simularOperacion = "N";
		entradaEnviarAsignacionesFacturar.p_Asignaciones = resultadoCrearOrdenesPostreras.P_OUT_RESULTADOS.stream().map(res -> {
			EnviarAsignacionesFacturar.Asignacion_Entrada asignacion = new EnviarAsignacionesFacturar.Asignacion_Entrada();
			asignacion.IdAsignacion = res.IdAsignacion;
			asignacion.GeneraComprobante = generaComprobante;
			asignacion.IdTipoComprobante = "POL";

			return asignacion;
		}).collect(Collectors.toList());

		EnviarAsignacionesFacturar enviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
		EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = enviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);

		ResultadoEnviarFacturas resultadoEnviarFacturas = new ResultadoEnviarFacturas();
		resultadoEnviarFacturas.P_OUT_ERRORESCONTADOR += salidaEnviarAsignacionesFacturar.P_OUT_ERRORESCONTADOR;
		resultadoEnviarFacturas.P_OUT_MENSAJES.addAll(salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES);
		resultadoEnviarFacturas.P_OUT_RESULTADOS.addAll(salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.enviarFacturas {}", fin - inicio);

		return resultadoEnviarFacturas;
	}

	private List<ResultadoOperacionesConfirmacion> obtenerResultadoOperacionesConfirmacion(List<Long> idsInstrucciones, ResultadoCrearOrdenesPostreras resultadoCrearOrdenesPostreras, ResultadoEnviarFacturas resultadoEnviarFacturas) {
		logger.trace("INICIO::ConfirmarInstruccionMasivo.obtenerResultadoOperacionesConfirmacion");
		long inicio = System.currentTimeMillis();

		Map<Long, Long> idAsignacionPorIdInstruccion = new HashMap<>(idsInstrucciones.size());
		Map<Long, Long> idOrdenPostreraPorIdInstruccion = new HashMap<>(idsInstrucciones.size());

		Map<Long, Long> idTransaccionPorIdAsignacion = new HashMap<>(idsInstrucciones.size());
		Map<Long, Long> idComprobantePorIdAsignacion = new HashMap<>(idsInstrucciones.size());
		Map<Long, Long> idMovimientoComprobantePorIdAsignacion = new HashMap<>(idsInstrucciones.size());

		for (ResultadoCrearOrdenesPostreras.Resultado res : resultadoCrearOrdenesPostreras.P_OUT_RESULTADOS) {
			idAsignacionPorIdInstruccion.put(res.IdInstruccion, res.IdAsignacion);
			idOrdenPostreraPorIdInstruccion.put(res.IdInstruccion, res.IdOrden);
		}

		for (EnviarAsignacionesFacturar.Resultado res : resultadoEnviarFacturas.P_OUT_RESULTADOS) {
			idTransaccionPorIdAsignacion.put(res.IdAsignacion, res.IdTransaccion);
			idComprobantePorIdAsignacion.put(res.IdAsignacion, res.IdComprobante);
			idMovimientoComprobantePorIdAsignacion.put(res.IdAsignacion, res.IdMovimientoComprobante);
		}

		List<ResultadoOperacionesConfirmacion> resultadosOperaciones = new ArrayList<>(idsInstrucciones.size());
		for (Long idInstruccion : idsInstrucciones) {
			ResultadoOperacionesConfirmacion resultadoOperacionesConfirmacion = new ResultadoOperacionesConfirmacion();
			resultadoOperacionesConfirmacion.IdInstruccion = idInstruccion;
			resultadoOperacionesConfirmacion.IdAsignacion = idAsignacionPorIdInstruccion.get(idInstruccion);
			resultadoOperacionesConfirmacion.IdOrden = idOrdenPostreraPorIdInstruccion.get(idInstruccion);

			resultadoOperacionesConfirmacion.IdTransaccion = idTransaccionPorIdAsignacion.get(resultadoOperacionesConfirmacion.IdAsignacion);
			resultadoOperacionesConfirmacion.IdComprobante = idComprobantePorIdAsignacion.get(resultadoOperacionesConfirmacion.IdAsignacion);
			resultadoOperacionesConfirmacion.IdMovimientoComprobante = idMovimientoComprobantePorIdAsignacion.get(resultadoOperacionesConfirmacion.IdAsignacion);

			resultadoOperacionesConfirmacion.IdEstadoInstruccion = "CNF";

			resultadosOperaciones.add(resultadoOperacionesConfirmacion);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarInstruccionMasivo.obtenerResultadoOperacionesConfirmacion {}", fin - inicio);

		return resultadosOperaciones;
	}

	private void actualizarInstrucciones(Connection conn, List<ResultadoOperacionesConfirmacion> resultadosOperaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.actualizarInstrucciones");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_INSTRUCCION, resultadosOperaciones, ResultadoOperacionesConfirmacion.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.actualizarInstrucciones {}", fin - inicio);
	}

	private void agregarBitacoras(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<ResultadoOperacionesConfirmacion> resultadosOperaciones) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.agregarBitacoras");
		long inicio = System.currentTimeMillis();

		AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo entradaAgregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo();
		entradaAgregarEventoBitacoraMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarEventoBitacoraMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarEventoBitacoraMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarEventoBitacoraMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAgregarEventoBitacoraMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaAgregarEventoBitacoraMasivo.p_eventos = resultadosOperaciones.stream().map(resultadoOperacion -> {
			AgregarEventoBitacoraMasivo.EventoBitacora eventoBitacora = new AgregarEventoBitacoraMasivo.EventoBitacora();
			eventoBitacora.IdInterno = resultadoOperacion.IdInstruccion;
			eventoBitacora.NombreEntidad = "INSTRUCCION";
			eventoBitacora.IdEntidad = resultadoOperacion.IdInstruccion;
			eventoBitacora.IdOrden = resultadoOperacion.IdOrden;
			eventoBitacora.IdAsignacion = resultadoOperacion.IdAsignacion;
			eventoBitacora.IdTransaccion = resultadoOperacion.IdTransaccion;
			eventoBitacora.MensajeEventoBitacora = entrada.p_ComentarioConfirmacion;
			eventoBitacora.IdTipoEventoBitacora = "INSTRUCCION_CONFIRMADA";

			return eventoBitacora;
		}).collect(Collectors.toList());

		AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
		agregarEventoBitacoraMasivo.logica(conn, entradaAgregarEventoBitacoraMasivo);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.agregarBitacoras {}", fin - inicio);
	}

	private void agregarLogs(Connection conn, EntradaConfirmarInstruccionMasivo entrada, List<ConsultarInstruccionMasivo.Instruccion> instrucciones, List<ResultadoOperacionesConfirmacion> confirmacionResultados) throws Exception {
		logger.debug("INICIO::ConfirmarInstruccionMasivo.agregarLogs");
		long inicio = System.currentTimeMillis();

		long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = idTrabajo;

		Map<Long, ConsultarInstruccionMasivo.Instruccion> instruccionPorIdInstruccion = new HashMap<>(instrucciones.size());

		for (ConsultarInstruccionMasivo.Instruccion instruccion : instrucciones) {
			instruccionPorIdInstruccion.put(instruccion.IdInstruccion, instruccion);

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = instruccion.IdInstruccion;
			log.IdTipoLog = "CONFIRMACION";
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
			log.IdTipoRegistro = "CONFIRMAR INSTRUCCION";
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
		}

		for (ResultadoOperacionesConfirmacion resultadoConfirmacion : confirmacionResultados) {
			ConsultarInstruccionMasivo.Instruccion instruccion = instruccionPorIdInstruccion.get(resultadoConfirmacion.IdInstruccion);

			AgregarLog.InAgregarLog.DetalleLog transaccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			transaccionDetalleLog.IdTrabajo = idTrabajo;
			transaccionDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			transaccionDetalleLog.SubRegistro = "Instruccion";
			transaccionDetalleLog.CampoRegistro = "IdTransaccion";
			transaccionDetalleLog.ValorAntiguo = Objects.nonNull(instruccion.IdTransaccion) ? String.valueOf(instruccion.IdTransaccion) : null;
			transaccionDetalleLog.ValorNuevo = String.valueOf(resultadoConfirmacion.IdTransaccion);
			entradaAgregarLog.p_LogDetalle.add(transaccionDetalleLog);

			AgregarLog.InAgregarLog.DetalleLog estadoInstruccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			estadoInstruccionDetalleLog.IdTrabajo = idTrabajo;
			estadoInstruccionDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			estadoInstruccionDetalleLog.SubRegistro = "Instruccion";
			estadoInstruccionDetalleLog.CampoRegistro = "IdEstadoInstruccion";
			estadoInstruccionDetalleLog.ValorAntiguo = instruccion.IdEstadoInstruccion;
			estadoInstruccionDetalleLog.ValorNuevo = resultadoConfirmacion.IdEstadoInstruccion;
			entradaAgregarLog.p_LogDetalle.add(estadoInstruccionDetalleLog);

			AgregarLog.InAgregarLog.DetalleLog ordenDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			ordenDetalleLog.IdTrabajo = idTrabajo;
			ordenDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			ordenDetalleLog.SubRegistro = "Instruccion";
			ordenDetalleLog.CampoRegistro = "IdOrden";
			ordenDetalleLog.ValorAntiguo = Objects.nonNull(instruccion.IdOrden) ? String.valueOf(instruccion.IdOrden) : null;
			ordenDetalleLog.ValorNuevo = String.valueOf(resultadoConfirmacion.IdOrden);
			entradaAgregarLog.p_LogDetalle.add(ordenDetalleLog);

			AgregarLog.InAgregarLog.DetalleLog asignacionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			asignacionDetalleLog.IdTrabajo = idTrabajo;
			asignacionDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			asignacionDetalleLog.SubRegistro = "Instruccion";
			asignacionDetalleLog.CampoRegistro = "IdAsignacion";
			asignacionDetalleLog.ValorAntiguo = Objects.nonNull(instruccion.IdAsignacion) ? String.valueOf(instruccion.IdAsignacion) : null;
			asignacionDetalleLog.ValorNuevo = String.valueOf(resultadoConfirmacion.IdAsignacion);
			entradaAgregarLog.p_LogDetalle.add(asignacionDetalleLog);

			AgregarLog.InAgregarLog.DetalleLog comprobanteDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			comprobanteDetalleLog.IdTrabajo = idTrabajo;
			comprobanteDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			comprobanteDetalleLog.SubRegistro = "Instruccion";
			comprobanteDetalleLog.CampoRegistro = "IdComprobante";
			comprobanteDetalleLog.ValorAntiguo = Objects.nonNull(instruccion.IdComprobante) ? String.valueOf(instruccion.IdComprobante) : null;
			comprobanteDetalleLog.ValorNuevo = String.valueOf(resultadoConfirmacion.IdComprobante);
			entradaAgregarLog.p_LogDetalle.add(comprobanteDetalleLog);

			AgregarLog.InAgregarLog.DetalleLog movimientoComprobanteDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			movimientoComprobanteDetalleLog.IdTrabajo = idTrabajo;
			movimientoComprobanteDetalleLog.IdCabeceraLog = resultadoConfirmacion.IdInstruccion;
			movimientoComprobanteDetalleLog.SubRegistro = "Instruccion";
			movimientoComprobanteDetalleLog.CampoRegistro = "IdMovimientoComprobante";
			movimientoComprobanteDetalleLog.ValorAntiguo = Objects.nonNull(instruccion.IdMovimientoComprobante) ? String.valueOf(instruccion.IdMovimientoComprobante) : null;
			movimientoComprobanteDetalleLog.ValorNuevo = String.valueOf(resultadoConfirmacion.IdMovimientoComprobante);
			entradaAgregarLog.p_LogDetalle.add(movimientoComprobanteDetalleLog);
		}

		AgregarLog agregarLog = new AgregarLog();
		agregarLog.logica(conn, entradaAgregarLog);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.agregarLogs {}", fin - inicio);
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_ComentarioConfirmacion\":\"ASAS\",\"p_IdsInstrucciones\":[\"592\"]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConfirmarInstruccionMasivo entrada = gson.fromJson(jsonRequest, EntradaConfirmarInstruccionMasivo.class);
			System.out.println("Entrada");
			System.out.println(entrada);

			{
				// Reiniciar instrucciones en la entrada
				List<Long> idsInstrucciones = new ArrayList<>(entrada.p_IdsInstrucciones);
				HashMap<String, Object> params = new HashMap<>(2);
				params.put("IdsInstrucciones", idsInstrucciones);

				SQL sql = new SQL();
				List<ConsultarInstruccionMasivo.Instruccion> instrucciones = sql.query(conn, "SELECT INS.\"IdInstruccion\", INS.\"IdTransaccion\", INS.\"IdOrden\", INS.\"IdAsignacion\", INS.\"IdComprobante\", INS.\"IdMovimientoComprobante\" FROM \"BSCL.Orden.BD::ORD.Instruccion\" INS WHERE INS.\"IdInstruccion\" IN (:IdsInstrucciones);", params, ConsultarInstruccionMasivo.Instruccion.class);

				List<Transaccion> transaccionesEliminar = new ArrayList<>(instrucciones.size());
				List<Asignacion> asignacionesEliminar = new ArrayList<>(instrucciones.size());
				List<Orden> ordenesEliminar = new ArrayList<>(instrucciones.size());
				List<Comprobante> comprobantesEliminar = new ArrayList<>(instrucciones.size());
				List<MovimientoComprobante> movimientosComprobantesEliminar = new ArrayList<>(instrucciones.size());
				for (ConsultarInstruccionMasivo.Instruccion instruccion : instrucciones) {
					if (Objects.nonNull(instruccion.IdTransaccion)) {
						Transaccion transaccion = new Transaccion();
						transaccion.IdTransaccion = instruccion.IdTransaccion;
						transaccionesEliminar.add(transaccion);
					}
					if (Objects.nonNull(instruccion.IdAsignacion)) {
						Asignacion asignacion = new Asignacion();
						asignacion.IdAsignacion = instruccion.IdAsignacion;
						asignacionesEliminar.add(asignacion);
					}
					if (Objects.nonNull(instruccion.IdOrden)) {
						Orden orden = new Orden();
						orden.IdOrden = instruccion.IdOrden;
						ordenesEliminar.add(orden);
					}
					if (Objects.nonNull(instruccion.IdComprobante)) {
						Comprobante comprobante = new Comprobante();
						comprobante.IdComprobante = instruccion.IdComprobante;
						comprobantesEliminar.add(comprobante);
					}
					if (Objects.nonNull(instruccion.IdMovimientoComprobante)) {
						MovimientoComprobante movimientoComprobante = new MovimientoComprobante();
						movimientoComprobante.IdMovimientoComprobante = instruccion.IdMovimientoComprobante;
						movimientosComprobantesEliminar.add(movimientoComprobante);
					}
				}

				try {
					sql.batch(conn, "DELETE FROM \"BSCL.Orden.BD::ORD.Transaccion\" WHERE \"IdTransaccion\"= :IdTransaccion;", transaccionesEliminar, Transaccion.class);
					sql.batch(conn, "DELETE FROM \"BSCL.Orden.BD::ORD.Asignacion\" WHERE \"IdAsignacion\"= :IdAsignacion;", asignacionesEliminar, Asignacion.class);
					sql.batch(conn, "DELETE FROM \"BSCL.Orden.BD::ORD.Orden\" WHERE \"IdOrden\"= :IdOrden;", ordenesEliminar, Orden.class);
					sql.batch(conn, "DELETE FROM \"BSCL.Facturacion.BD::FAC.Comprobante\" WHERE \"IdComprobante\"= :IdComprobante;", comprobantesEliminar, Comprobante.class);
					sql.batch(conn, "DELETE FROM \"BSCL.Facturacion.BD::FAC.MovimientoComprobante\" WHERE \"IdMovimientoComprobante\"= :IdMovimientoComprobante;\n", movimientosComprobantesEliminar, MovimientoComprobante.class);

					sql.batch(conn, "UPDATE \"BSCL.Orden.BD::ORD.Instruccion\" SET \"IdOrden\"=NULL,\"IdAsignacion\"=NULL,\"IdEstadoInstruccion\"='ING',\"IdTransaccion\"=NULL,\"IdMovimientoComprobante\"=NULL,\"IdComprobante\"=NULL WHERE \"IdInstruccion\"= :IdInstruccion;", instrucciones, ConsultarInstruccionMasivo.Instruccion.class);
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					conn.rollback();
				}
			}

			ConfirmarInstruccionMasivo confirmarInstruccionMasivo = new ConfirmarInstruccionMasivo();
			try {
				SalidaConfirmarInstruccionMasivo salidaConfirmarInstruccionMasivo = confirmarInstruccionMasivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaConfirmarInstruccionMasivo);
				if (salidaConfirmarInstruccionMasivo.P_OUT_ERRORESCONTADOR > 0) {
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
