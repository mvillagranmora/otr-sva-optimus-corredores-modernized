package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarMovimientoCarteraEnTransitoMasivo implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarMovimientoCarteraEnTransitoMasivo.class);

	private String SQL_UPDATE_MOVIMIENTO_CARTERA_EN_TRANSITO = loadResourceToString(
			"cl/bcs/custodia/sql/ModificarMovimientoCarteraEnTransitoMasivo_UpdateMovimientoCarteraEnTransito.sql");

	public static class EntradaModificarMovimientoCarteraEnTransitoMasivo extends BcsPojo {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<MovimientoCarteraEnTransito> p_MovimientoCarteraEnTransito;

		public EntradaModificarMovimientoCarteraEnTransitoMasivo() {
			p_MovimientoCarteraEnTransito = new ArrayList<>();
		}
	}

	public static class SalidaModificarMovimientoCarteraEnTransitoMasivo extends BcsPojo {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaModificarMovimientoCarteraEnTransitoMasivo() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}

	}

	public static class MovimientoCarteraEnTransito extends BcsPojo {

		public Long IdMovimientoCarteraEnTransito;
		public BigDecimal PrecioCompraAntiguo;
		public BigDecimal PrecioCompraNuevo;
		public Long Cantidad;
		public transient BigDecimal MontoCompra;
		public transient BigDecimal MontoCompraMonedaInstrumento;
		public transient BigDecimal MontoCompraMonedaAdicional;
		public String IdMonedaInstrumento;

		public MovimientoCarteraEnTransito() {
			IdMovimientoCarteraEnTransito = 0L;
			PrecioCompraNuevo = BigDecimal.ZERO;
			PrecioCompraAntiguo = BigDecimal.ZERO;
			Cantidad = 0L;
			MontoCompra = BigDecimal.ZERO;
			MontoCompraMonedaInstrumento = BigDecimal.ZERO;
			MontoCompraMonedaAdicional = BigDecimal.ZERO;
			IdMonedaInstrumento = "";
		
		}
	}

	public SalidaModificarMovimientoCarteraEnTransitoMasivo logica(Connection conexion,
			EntradaModificarMovimientoCarteraEnTransitoMasivo entrada) throws Exception {

		logger.info("INICIO::ModificarMovimientoCarteraEnTransitoMasivo");
		Long inicio = System.currentTimeMillis();

		SalidaModificarMovimientoCarteraEnTransitoMasivo salida = new SalidaModificarMovimientoCarteraEnTransitoMasivo();

		BigDecimal valorDolar = obtenerValorDolar(conexion, entrada.p_IdEmpresa);

		if (valorDolar.compareTo(BigDecimal.ZERO) <= 0) {
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_OBTENER_VALOR_DOLAR",
					new HashMap<>(), conexion, entrada.p_IdEmpresa);
			salida.P_OUT_MENSAJES.add(mensajeSalida);
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}

		for (MovimientoCarteraEnTransito movimiento : entrada.p_MovimientoCarteraEnTransito) {
			BigDecimal cantidad = BigDecimal.valueOf(movimiento.Cantidad);
			BigDecimal precioCompra= MonedaFormato.formatear(conexion, movimiento.PrecioCompraNuevo, MonedaFormato.PRECIO, movimiento.IdMonedaInstrumento).valor;
			cantidad= MonedaFormato.formatear(conexion, cantidad, MonedaFormato.CANTIDAD, movimiento.IdMonedaInstrumento).valor;
			movimiento.MontoCompra = MonedaFormato.formatear(conexion, precioCompra.multiply(cantidad,MathContext.DECIMAL128), MonedaFormato.MONTO, movimiento.IdMonedaInstrumento).valor;
			movimiento.MontoCompraMonedaInstrumento = movimiento.MontoCompra;
			movimiento.MontoCompraMonedaAdicional = movimiento.MontoCompra.divide(valorDolar, MathContext.DECIMAL128);
		}

		modificarMovimientosCartera(conexion, entrada.p_MovimientoCarteraEnTransito);

		agregarLogs(conexion,entrada);
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarMovimientoCarteraEnTransitoMasivo {}", fin - inicio);

		return salida;

	}

	private BigDecimal obtenerValorDolar(Connection conexion, Long idEmpresa) throws Exception {

		logger.debug("INICIO:: ModificarMovimientoCarteraEnTransitoMasivo.obtenerValorDolar");
		Long inicio = System.currentTimeMillis();

		CalculoEntrada tipoCambioFondoLocalRespectoLocal = new CalculoEntrada();
		tipoCambioFondoLocalRespectoLocal.IdTrabajo = 1L;
		tipoCambioFondoLocalRespectoLocal.IdInterno = 1L;
		tipoCambioFondoLocalRespectoLocal.Fecha = ConfiguracionSistema.getParametroConfiguracionString(conexion,
				idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		tipoCambioFondoLocalRespectoLocal.IdMonedaOrigen = "USD";
		tipoCambioFondoLocalRespectoLocal.IdMonedaDestino = "CLP";
		tipoCambioFondoLocalRespectoLocal.ValorCambioLocalModificado = BigDecimal.ZERO;
		tipoCambioFondoLocalRespectoLocal.MontoOrigen = BigDecimal.ONE;
		tipoCambioFondoLocalRespectoLocal.IdUso = "";

		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdTrabajo = 1L;
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoLocalRespectoLocal);

		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadSalida calculoSalida = calcularParidad.logica(conexion, calcularParidadEntrada);

		Map<Long, CalcularParidad.CalculoSalida> calculosPorIdInterno = calculoSalida.p_out_CalculosSalida.stream()
				.collect(Collectors.toMap(calculo -> calculo.IdInterno, calculo -> calculo));

		BigDecimal valorDolar = calculosPorIdInterno.getOrDefault(1L, new CalcularParidad.CalculoSalida()).MontoDestino;

		Long fin = System.currentTimeMillis();
		logger.debug("FIN:: ModificarMovimientoCarteraEnTransitoMasivo.obtenerValorDolar {}", fin - inicio);

		return valorDolar;
	}

	private void modificarMovimientosCartera(Connection conexion, List<MovimientoCarteraEnTransito> movimientos)
			throws Exception {

		logger.debug("INICIO::ModificarMovimientoCarteraEnTransitoMasivo.modificarMovimientosCartera");
		Long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conexion, SQL_UPDATE_MOVIMIENTO_CARTERA_EN_TRANSITO, movimientos, MovimientoCarteraEnTransito.class);

		Long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoCarteraEnTransitoMasivo.modificarMovimientosCartera {}", fin - inicio);

	}

	private void agregarLogs(Connection conn, EntradaModificarMovimientoCarteraEnTransitoMasivo entrada)
			throws Exception {

		logger.debug("INICIO::ConfirmarInstruccionMasivo.agregarLogs");
		long inicio = System.currentTimeMillis();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 0L;

		for (MovimientoCarteraEnTransito movimiento : entrada.p_MovimientoCarteraEnTransito) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = movimiento.IdMovimientoCarteraEnTransito;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido =0L;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.IdTipoRegistro = "SESION";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.valueOf(movimiento.Cantidad);
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = movimiento.MontoCompra;
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.IdPersona = 0L;

			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog transaccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			transaccionDetalleLog.IdTrabajo = 0L;
			transaccionDetalleLog.IdCabeceraLog = movimiento.IdMovimientoCarteraEnTransito;
			transaccionDetalleLog.SubRegistro = "MOVIMIENTO";
			transaccionDetalleLog.CampoRegistro = "PRECIO";
			transaccionDetalleLog.ValorAntiguo = (movimiento.PrecioCompraAntiguo).toString();
			transaccionDetalleLog.ValorNuevo = (movimiento.PrecioCompraNuevo).toString();
			entradaAgregarLog.p_LogDetalle.add(transaccionDetalleLog);

			AgregarLog agregarLog = new AgregarLog();
			agregarLog.logica(conn, entradaAgregarLog);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarInstruccionMasivo.agregarLogs {}", fin - inicio);

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {

		Gson gson = Utils.getGsonBuilder();
		EntradaModificarMovimientoCarteraEnTransitoMasivo jin = gson.fromJson(in.toString(),
				EntradaModificarMovimientoCarteraEnTransitoMasivo.class);
		SalidaModificarMovimientoCarteraEnTransitoMasivo out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;

	}

	public static void main(String[] args) throws Exception {

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FAC\",\"p_MovimientoCarteraEnTransito\":[{\"CampoRegistro\":\"Precio\",\"ValorAntiguo\":0,\"ValorNuevo\":1900,\"Cantidad\":1000,\"MontoCompra\":1900000,\"IdMovimientoCarteraEnTransito\":31296}]}";
			EntradaModificarMovimientoCarteraEnTransitoMasivo entradaModificarMovimientoCarteraEnTransitoMasivo = gson
					.fromJson(jsonEntrada, EntradaModificarMovimientoCarteraEnTransitoMasivo.class);
			ModificarMovimientoCarteraEnTransitoMasivo crearBorradorMovimientoCarteraEnTransito = new ModificarMovimientoCarteraEnTransitoMasivo();
			try {
				SalidaModificarMovimientoCarteraEnTransitoMasivo salidaModificarMovimientoCarteraEnTransitoMasivo = crearBorradorMovimientoCarteraEnTransito
						.logica(con, entradaModificarMovimientoCarteraEnTransitoMasivo);
				if (salidaModificarMovimientoCarteraEnTransitoMasivo.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				}
				System.out.println(salidaModificarMovimientoCarteraEnTransitoMasivo);
			} catch (Exception er) {
				er.printStackTrace();
			} finally {
			//	con.rollback();
				 con.commit();
			}
		}
	}
}
