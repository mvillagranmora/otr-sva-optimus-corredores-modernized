package cl.bcs.ordenes;

import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenUsuario;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrearTransaccionInstruccionMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearTransaccionInstruccionMasivo.class);

	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionInstruccionMasivo_SelectTransacciones.sql");

	public static class EntradaCrearTransaccionInstruccionMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public List<ConsultarInstruccionMasivo.Instruccion> p_Instrucciones;

		public EntradaCrearTransaccionInstruccionMasivo() {
			p_Instrucciones = new ArrayList<>();
		}
	}

	public static class SalidaCrearTransaccionInstruccionMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Transaccion> p_out_Transacciones;

		public SalidaCrearTransaccionInstruccionMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Transacciones = new ArrayList<>();
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	public SalidaCrearTransaccionInstruccionMasivo logica(Connection conn, EntradaCrearTransaccionInstruccionMasivo entrada) throws Exception {
		logger.info("INICIO::CrearTransaccionInstruccionMasivo");
		long inicio = System.currentTimeMillis();

		SalidaCrearTransaccionInstruccionMasivo salida = new SalidaCrearTransaccionInstruccionMasivo();

		ResumenUsuario resumenUsuario = SocioNegocioUtils.obtenerResumenUsuario(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario);

		ResultadoCrearTransacciones resultadoCrearTransacciones = crearTransacciones(conn, entrada, resumenUsuario);
		salida.P_OUT_ERRORESCONTADOR += resultadoCrearTransacciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoCrearTransacciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		List<Transaccion> transaccionesInsertadas = obtenerTransacciones(conn, resultadoCrearTransacciones.P_OUT_TRANSACCIONES);
		salida.p_out_Transacciones.addAll(transaccionesInsertadas);

		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearTransaccionInstruccionMasivo", fin - inicio);

		return salida;
	}

	private ResultadoCrearTransacciones crearTransacciones(Connection conn, EntradaCrearTransaccionInstruccionMasivo entrada, ResumenUsuario resumenUsuario) throws Exception {
		logger.trace("INICIO::CrearTransaccionInstruccionMasivo.crearTransacciones");
		long inicio = System.currentTimeMillis();

		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);

		CrearTransacciones.CrearTransacciones_Entrada entradaCrearTransacciones = new CrearTransacciones.CrearTransacciones_Entrada();
		entradaCrearTransacciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransacciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransacciones.p_IdModulo = entrada.p_IdModulo;
		entradaCrearTransacciones.p_Reproceso = null;
		entradaCrearTransacciones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearTransacciones.p_simularOperacion = "N";
		entradaCrearTransacciones.p_Transacciones = entrada.p_Instrucciones
				.stream()
				.map(instruccion -> obtenerTransaccionDesdeInstruccion(instruccion, resumenUsuario.IdEntidad, fechaHoraSistema))
				.collect(Collectors.toList());

		CrearTransacciones crearTransacciones = new CrearTransacciones();
		CrearTransacciones.CrearTransacciones_Salida salidaCrearTransacciones = crearTransacciones.logica(conn, entradaCrearTransacciones);

		ResultadoCrearTransacciones resultado = new ResultadoCrearTransacciones();
		resultado.P_OUT_ERRORESCONTADOR += salidaCrearTransacciones.P_OUT_ERRORESCONTADOR;
		resultado.P_OUT_MENSAJES.addAll(salidaCrearTransacciones.P_OUT_MENSAJES);

		Map<Long, Long> idTransaccionPorIdInterno = salidaCrearTransacciones.P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r.IdTransaccion));
		for (CrearTransacciones.Transaccion_Entrada transaccionEntrada : entradaCrearTransacciones.p_Transacciones) {
			Long idTransaccion = idTransaccionPorIdInterno.get(transaccionEntrada.IdInterno);
			if (idTransaccion ==  null) {
				resultado.P_OUT_ERRORESCONTADOR++;
			} else {
				Transaccion transaccionResultado = new Transaccion();
				transaccionResultado.IdInterno = transaccionEntrada.IdInterno;
				transaccionResultado.IdTransaccion = idTransaccion;
				resultado.P_OUT_TRANSACCIONES.add(transaccionResultado);
			}
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTransaccionInstruccionMasivo.crearTransacciones", fin - inicio);

		return resultado;
	}

	private CrearTransacciones.Transaccion_Entrada obtenerTransaccionDesdeInstruccion(ConsultarInstruccionMasivo.Instruccion instruccion, Long idEmpleado, java.sql.Timestamp fechaHoraSistema) {
		logger.trace("INICIO::CrearTransaccionInstruccionMasivo.obtenerTransaccionDesdeInstruccion");
		long inicio = System.currentTimeMillis();

		CrearTransacciones.Transaccion_Entrada transaccion = new CrearTransacciones.Transaccion_Entrada();
		transaccion.Cantidad = "IF".equals(instruccion.IdClasificacionTipoInstrumento) ? BigDecimal.ONE : "RF".equals(instruccion.IdClasificacionTipoInstrumento) ? instruccion.ValorNominal : instruccion.Cantidad;
		transaccion.CodigoOperadorCorredor = "";
		transaccion.CodigoOperadorContraparte = "";
		transaccion.FolioTransaccion = String.valueOf(instruccion.IdInstruccion);
		transaccion.EsBilateral = "S";
		transaccion.EsCuentaPropia = "N";
		transaccion.EsFueraRueda = "S";
		transaccion.EsFungible = instruccion.EsFungible;
		transaccion.EsRuteo = "N";
		transaccion.EsVentaCorta = "N";
		transaccion.FechaCreacion = Fecha.toStringDate(instruccion.FechaTrade);
		transaccion.FechaEmision = Fecha.toStringDate(instruccion.FechaEmisionInstrumento);
		//transaccion.FechaIngresoOferta = Fecha.toStringDate(instruccion.FechaTrade);
		transaccion.FechaLiquidacionContado = Fecha.toStringDate(instruccion.FechaLiquidacion);
		transaccion.FechaVencimiento = Fecha.toStringDate(instruccion.FechaVencimientoInstruccion);
		transaccion.FolioTransaccionPlazo = "";
		transaccion.GastosCorresponsal = BigDecimal.ZERO;
		transaccion.HoraCreacion = Fecha.toStringTime(fechaHoraSistema);
		transaccion.IdBoveda = instruccion.IdBoveda;
		transaccion.IdBolsa = instruccion.IdBolsa;
		transaccion.IdCondicionDeLiquidacionContado = instruccion.IdCondicionDeLiquidacion;
		transaccion.IdCorredoraContraparte = instruccion.IdIntermediario;
		transaccion.IdCorredoraCorresponsal = 0L;
		transaccion.IdEmisor = instruccion.IdEmisor;
		transaccion.IdEmpleado = idEmpleado;
		transaccion.IdInstrumento = instruccion.IdInstrumento;
		transaccion.IdInstrumentoSubyacente = 0L;
		transaccion.IdInterno = instruccion.IdInstruccion;
		transaccion.IdLibroOrdenes = instruccion.IdLibroOrdenes;
		transaccion.IdMoneda = instruccion.IdMonedaInstrumento;
		transaccion.IdMonedaEmision = instruccion.IdMonedaEmisionInstrumento;
		transaccion.IdMonedaLiquidacionCorresponsal = "";
		transaccion.IdSistemaBursatil = "MANUAL";
		transaccion.IdTipoOperacion = instruccion.IdTipoOperacion;
		transaccion.IdTipoReajuste = instruccion.IdTipoReajusteInstrumento;
		transaccion.IndicadorDCV = "";
		transaccion.IndicadorOrdenDirecta = "";
		transaccion.Intereses = BigDecimal.ZERO;
		transaccion.LiqFisicaOpciones = null;
		transaccion.MarcaNegociacion = "";
		transaccion.MejorPrecioCompra = BigDecimal.ZERO;
		transaccion.MejorPrecioVenta = BigDecimal.ZERO;
		transaccion.Monto = instruccion.MontoLiquidacion;
		transaccion.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
		transaccion.MontoPlazo = BigDecimal.ZERO;
		transaccion.NemotecnicoEspecificoBolson = "";
		transaccion.NemotecnicoSVS = instruccion.NemotecnicoSVS;
		transaccion.Plazo = "RV".equals(instruccion.IdClasificacionTipoInstrumento) ? 0L : instruccion.PlazoVencimiento;
		transaccion.Precio = instruccion.PrecioInstruccion;
		transaccion.PrecioPlazo = BigDecimal.ZERO;
		transaccion.Reajustes = BigDecimal.ZERO;
		transaccion.Tasa = instruccion.TirTasa;
		transaccion.TipoCambioCorresponsal = BigDecimal.ZERO;
		transaccion.ValorRescate = instruccion.ValorRescate;
		transaccion.FlujoOrigen = "INGRESOINSTRUCCION";

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTransaccionInstruccionMasivo.obtenerTransaccionDesdeInstruccion", fin - inicio);

		return transaccion;
	}

	private List<Transaccion> obtenerTransacciones(Connection conn, List<Transaccion> transaccionesInsertadas) throws SQLException {
		logger.debug("INICIO::CrearTransaccionInstruccionMasivo.obtenerTransacciones");
		long inicio = System.currentTimeMillis();

		Map<Long, Long> idInternoPorIdTransaccion = new HashMap<>(transaccionesInsertadas.size());
		List<Long> idsTransacciones = new ArrayList<>(transaccionesInsertadas.size());
		for (Transaccion transaccion : transaccionesInsertadas) {
			idsTransacciones.add(transaccion.IdTransaccion);
			idInternoPorIdTransaccion.put(transaccion.IdTransaccion, transaccion.IdInterno);
		}

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdsTransacciones", idsTransacciones);

		SQL sql = new SQL();
		List<Transaccion> transacciones = sql.query(conn, SQL_SELECT_TRANSACCIONES, params, Transaccion.class);
		for (Transaccion transaccion : transacciones) {
			transaccion.IdInterno = idInternoPorIdTransaccion.get(transaccion.IdTransaccion);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTransaccionInstruccionMasivo.obtenerTransacciones", fin - inicio);

		return transacciones;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_IdentificadoresInstrucciones\":[{\"IdInterno\":10,\"IdInstruccion\":1},{\"IdInterno\":20,\"IdInstruccion\":2},{\"IdInterno\":30,\"IdInstruccion\":3}]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaCrearTransaccionInstruccionMasivo entrada = gson.fromJson(jsonRequest, EntradaCrearTransaccionInstruccionMasivo.class);
			System.out.println("Entrada");
			System.out.println(entrada);

			CrearTransaccionInstruccionMasivo crearTransaccionInstruccionMasivo = new CrearTransaccionInstruccionMasivo();
			try {
				SalidaCrearTransaccionInstruccionMasivo salidaCrearTransaccionInstruccionMasivo = crearTransaccionInstruccionMasivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaCrearTransaccionInstruccionMasivo);
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
