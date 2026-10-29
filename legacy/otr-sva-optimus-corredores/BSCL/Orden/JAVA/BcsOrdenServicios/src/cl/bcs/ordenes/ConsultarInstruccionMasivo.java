package cl.bcs.ordenes;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConsultarInstruccionMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarInstruccionMasivo.class);

	private final String SQL_SELECT_INSTRUCCIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInstruccionMasivo_SelectInstrucciones.sql");
	private final String SQL_SELECT_INDICADORES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInstruccionMasivo_SelectIndicadores.sql");

	public static class EntradaConsultarInstruccionMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public List<Long> p_IdsInstrucciones;
	}

	public static class SalidaConsultarInstruccionMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Instruccion> p_out_Instrucciones;

		public SalidaConsultarInstruccionMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Instrucciones = new ArrayList<>();
		}
	}

	public static class Indicador extends BcsPojo {
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public String CodigoTipoIndicador;
		public String ValorIndicador;
	}

	public static class Instruccion extends BcsPojo {
		public Long IdInstruccion;

		public Long IdCliente;
		public Long IdPortafolioCliente;
		public Long IdCuentaCustodioPortafolio;

		public Long IdTransaccion;
		public Long IdOrden;
		public Long IdAsignacion;
		public Long IdComprobante;
		public Long IdMovimientoComprobante;

		public Long IdEmpresa;
		public Long IdIntermediario;
		public Long IdLibroOrdenes;
		public String Glosa;
		public String IdBolsa;
		public String IdEstadoInstruccion;
		public String IdEmisor;
		public String IdBoveda;
		public String IdTipoOperacion;
		public String AccionTipoOperacion;
		public String IdCondicionDeLiquidacion;
		public String Nemotecnico;
		public String NemotecnicoSVS;
		public String IdMonedaLiquidacion;
		public String IdClasificacionTipoInstrumentoInstruccion;
		public String IdTipoOperacionInstruccion;
		public String IdTipoProductoInstruccion;
		public String EsFungible;
		public BigDecimal Cantidad;
		public BigDecimal MontoNeto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal TirTasa;
		public BigDecimal ValorRescate;
		public BigDecimal ValorNominal;
		public BigDecimal PrecioInstruccion;
		public Long	PlazoVencimiento;
		public java.sql.Date FechaTrade;
		public java.sql.Date FechaLiquidacion;
		public java.sql.Date FechaVencimientoInstruccion;

		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoReajusteInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaInstrumento;
		public String IdMonedaEmisionInstrumento;
		public String EsDivisibleInstrumento;
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		
		public Long IdMotivo;
		public String IdTipoCanalIngreso;
		public java.sql.Date FechaIngreso;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	public SalidaConsultarInstruccionMasivo logica(Connection conn, EntradaConsultarInstruccionMasivo entrada) throws Exception {
		logger.info("INICIO::ConsultarInstruccionMasivo");
		long inicio = System.currentTimeMillis();

		SalidaConsultarInstruccionMasivo salida = new SalidaConsultarInstruccionMasivo();

		Set<Long> idsInstruccionesSolicitadas = new HashSet<>(entrada.p_IdsInstrucciones);
		List<Instruccion> instrucciones = obtenerInstrucciones(conn, new ArrayList<>(idsInstruccionesSolicitadas));
		salida.p_out_Instrucciones.addAll(instrucciones);

		Set<Long> idsPortafoliosClientes = instrucciones.stream().map(instruccion -> instruccion.IdPortafolioCliente).collect(Collectors.toSet());
		Map<Long, Indicador> indicadorLibroOrdenesPorIdPortafolioCliente = obtenerIndicadorPorIdEntidad(conn, entrada.p_IdEmpresa, "PORTAFOLIO", "ORDLIBRO", idsPortafoliosClientes);

		Set<Long> idsInstruccionesEncontradas = instrucciones.stream().map(identificador -> identificador.IdInstruccion).collect(Collectors.toSet());
		List<PlataformaUtils.MensajeSalida> instruccionesFaltantesMensajes = obtenerInstruccionesFaltantesMesajes(conn, entrada.p_IdEmpresa, idsInstruccionesSolicitadas, idsInstruccionesEncontradas);
		salida.P_OUT_MENSAJES.addAll(instruccionesFaltantesMensajes);

		asignarCamposAdicionales(instrucciones, indicadorLibroOrdenesPorIdPortafolioCliente);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarInstruccionMasivo", fin - inicio);

		return salida;
	}

	private void asignarCamposAdicionales(List<Instruccion> instrucciones, Map<Long, Indicador> indicadorLibroOrdenesPorIdPortafolioCliente) {
		logger.trace("INICIO::ConsultarInstruccionMasivo.asignarCamposAdicionales");
		long inicio = System.currentTimeMillis();

		for (Instruccion instruccion : instrucciones) {
			Indicador libroOrdenesIndicador = indicadorLibroOrdenesPorIdPortafolioCliente.get(instruccion.IdPortafolioCliente);
			instruccion.IdLibroOrdenes = libroOrdenesIndicador == null ? null : Long.valueOf(libroOrdenesIndicador.ValorIndicador);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarInstruccionMasivo.asignarCamposAdicionales", fin - inicio);
	}

	private Map<Long, Indicador> obtenerIndicadorPorIdEntidad(Connection conn, Long idEmpresa, String idTipoEntidad, String codigoTipoIndicador, Set<Long> idsEntidades) throws SQLException {
		logger.debug("INICIO::ConsultarInstruccionMasivo.obtenerIndicadorPorIdEntidad");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdTipoEntidad", idTipoEntidad);
		params.put("IdsEntidades", new ArrayList<>(idsEntidades));
		params.put("CodigoTipoIndicador", codigoTipoIndicador);
		params.put("IdEstadoTipoIndicador", "ACT");

		SQL sql = new SQL();
		Map<Long, Indicador> indicadorPorIdEntidad = sql.query(conn, SQL_SELECT_INDICADORES, params, Indicador.class)
				.stream()
				.collect(Collectors.toMap(indicador -> indicador.IdEntidad, indicador -> indicador));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarInstruccionMasivo.obtenerIndicadorPorIdEntidad", fin - inicio);

		return indicadorPorIdEntidad;
	}

	private List<PlataformaUtils.MensajeSalida> obtenerInstruccionesFaltantesMesajes(Connection conn, Long idEmpresa, Set<Long> idsInstruccionesSolicitadas, Set<Long> idsInstruccionesEncontradas) throws Exception {
		logger.debug("INICIO::ConsultarInstruccionMasivo.obtenerInstruccionesFaltantesMesajes");
		long inicio = System.currentTimeMillis();

		Set<Long> idsInstruccionesFaltantes = new HashSet<>(idsInstruccionesSolicitadas);
		for (Long idsInstruccionesEncontrada : idsInstruccionesEncontradas) {
			idsInstruccionesFaltantes.remove(idsInstruccionesEncontrada);
		}

		HashMap<String, String> mensajeParams = new HashMap<>(2);
		List<PlataformaUtils.MensajeSalida> mensajes = new ArrayList<>(idsInstruccionesFaltantes.size());
		for (Long idInstruccionFaltante : idsInstruccionesFaltantes) {
			mensajeParams.put("IdInstruccion", String.valueOf(idInstruccionFaltante));

			mensajes.add(new PlataformaUtils.MensajeSalida(idInstruccionFaltante, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_INSTRUCCION_NO_ENCONTRADA", mensajeParams, conn, idEmpresa));

			mensajeParams.clear();
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarInstruccionMasivo.obtenerInstruccionesFaltantesMesajes", fin - inicio);

		return mensajes;
	}

	private List<Instruccion> obtenerInstrucciones(Connection conn, List<Long> idsInstrucciones) throws SQLException {
		logger.debug("INICIO::ConsultarInstruccionMasivo.obtenerInstrucciones");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdsInstrucciones", idsInstrucciones);

		SQL sql = new SQL();
		List<Instruccion> instrucciones = sql.query(conn, SQL_SELECT_INSTRUCCIONES, params, Instruccion.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarInstruccionMasivo.obtenerInstrucciones", fin - inicio);

		return instrucciones;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_IdsInstrucciones\":[26]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarInstruccionMasivo entrada = gson.fromJson(jsonRequest, EntradaConsultarInstruccionMasivo.class);
			System.out.println("Entrada");
			System.out.println(entrada);

			ConsultarInstruccionMasivo consultarInstruccionMasivo = new ConsultarInstruccionMasivo();
			try {
				SalidaConsultarInstruccionMasivo salidaConsultarInstruccionMasivo = consultarInstruccionMasivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaConsultarInstruccionMasivo);
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
