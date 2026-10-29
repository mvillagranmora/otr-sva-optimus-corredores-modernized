package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ReplicaPortafolioAPV.Indicador;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarOrdenesPorDetallePerfilCobro implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarOrdenesPorDetallePerfilCobro.class);

	private final String SQL_SELECT_DETALLE_PERFIL_COBROS = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select.sql");
	private final String SQL_UPDATE_COBRO_ORDEN = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Update_cobroOrden.sql");
	private final String SQL_SELCT_SUM = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Porcentaje.sql");
	private final String SQL_UPDATE_ORDEN = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Update_Orden.sql");
	private final String SQL_INSERT_COBRO_ORDEN = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Insert_cobroOrden.sql");
	private final String SQL_SELCT_COBRO_ORDEN = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_CobroOrden.sql");
	private final String SQL_SELCT_EXISTE = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_existe.sql");
	private final String SQL_SELCT_PORTAFOLIO = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Portafolio.sql");
	private final String SQL_SELCT_COBRO_DEFAULT = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Cobro_Default.sql");
	private final String SQL_DELETE_ORDEN_COBRO = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Delete_OrdenCobro.sql");

	public static class EntradaModificarOrdenesPorDetallePerfilCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Long> p_IdDetallePerfilCobros;

		public EntradaModificarOrdenesPorDetallePerfilCobro() {
			this.p_IdDetallePerfilCobros = new ArrayList<>();
		}
	}

	public static class SalidaModificarOrdenesPorDetallePerfilCobro extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaModificarOrdenesPorDetallePerfilCobro() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class EntidadOrdenesCobros {

		public Long IdDetallePerfilCobros;
		public String IdMoneda;
		public String IdCobro;
		public BigDecimal Cantidad;
		public BigDecimal ValorCobro;
		public BigDecimal PrecioOrden;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoLocal;
		public BigDecimal MontoOrden;
		public BigDecimal MontoLiquidacion;
		public BigDecimal PorcentajeCobros;
		public BigDecimal ValorCobroPerfil;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public BigDecimal MontoMinimo;
		public String CodigoCobroClasificacion;
		public BigDecimal ValorCobroFinal;
		public String IdMonedaOrden;
		public Long IdCobroOrden;
		public Long IdOrden;
		public BigDecimal ValorTopeMinimoOrden;
		public String Borrado;
		public String IdTipoProducto;

		public EntidadOrdenesCobros() {
			this.IdDetallePerfilCobros = 0l;
			this.IdMoneda = "";
			this.IdCobro = "";
			this.ValorCobro = BigDecimal.ZERO;
			this.ValorTopeMinimoOrden = BigDecimal.ZERO;
			this.Cantidad = BigDecimal.ZERO;
			this.PrecioOrden = BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
			this.MontoLocal = BigDecimal.ZERO;
			this.MontoOrden = BigDecimal.ZERO;
			this.MontoLiquidacion = BigDecimal.ZERO;
			this.PorcentajeCobros = BigDecimal.ZERO;
			this.ValorCobroPerfil = BigDecimal.ZERO;
			this.ValorCobroAplicado = BigDecimal.ZERO;
			this.MontoCobroCalculado = BigDecimal.ZERO;
			this.MontoCobroCalculadoLocal = BigDecimal.ZERO;
			this.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
			this.MontoMinimo = BigDecimal.ZERO;
			this.CodigoCobroClasificacion = "";
			this.ValorCobroFinal = BigDecimal.ZERO;
			this.IdMonedaOrden = "";
			this.IdCobroOrden = 0l;
			this.IdOrden = 0l;
			this.Borrado = "";
			this.IdTipoProducto = "";

		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);

		}

	}

	public static class EntidadUpdateCobroOrden {
		public BigDecimal MontoMinimo;
		public BigDecimal ValorCobroPerfil;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal ValorCobroFinal;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public Long IdCobroOrden;
		public Long IdOrden;
		public String IdCobro;

		public EntidadUpdateCobroOrden() {
			this.MontoMinimo = BigDecimal.ZERO;
			this.ValorCobroPerfil = BigDecimal.ZERO;
			this.ValorCobroAplicado = BigDecimal.ZERO;
			this.ValorCobroFinal = BigDecimal.ZERO;
			this.MontoCobroCalculado = BigDecimal.ZERO;
			this.MontoCobroCalculadoLocal = BigDecimal.ZERO;
			this.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
			this.IdCobroOrden = 0l;
			this.IdOrden = 0l;
			this.IdCobro = "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadUpdateOrden {
		public BigDecimal PorcentajeCobros;
		public Long IdOrden;

		public EntidadUpdateOrden() {
			this.PorcentajeCobros = BigDecimal.ZERO;
			this.IdOrden = 0l;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadBuscaSuma {
		public BigDecimal PorcentajeSuma;

		public EntidadBuscaSuma() {
			this.PorcentajeSuma = BigDecimal.ZERO;

		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class resultado {
		public Long existen;

		public resultado() {
			this.existen = 0l;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class EntidadInsertCobroOrden extends BcsPojo {
		public Long IdCobroOrden;
		public Long IdGrupoCobro;
		public Long IdOrden;
		public String IdCobro;
		public BigDecimal ValorCobroPerfil;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public String FormaCobro;
		public String EsComprobante;
		public Date FechaIngresoCobro;
		public Long IdCarga;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public BigDecimal MontoMinimo;
		public String CodigoCobroClasificacion;
		public String IdMoneda;
		public String IdOrigenCobro;
		public Long IdAsociacion;
		public BigDecimal ValorCobroFinal;
		public String PermiteCambiarValor;
		public String _CheckCobroMonedaOperacion;
		public String _IdMonedaCobroOtraMoneda;
		public BigDecimal _MontoFijo_MontoCobro;
		public String _MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal _MontoFijo_ValorMontoOperacionMenor;
		public String _MontoFijo_CheckSiNo;
		public BigDecimal _MontoFijo_ValorMontoSiNo;
		public BigDecimal _PorcentualFijo_ValorCobro;
		public String _PorcentualFijo_CheckSobreMonto;
		public String _PorcentualFijo_CheckSobreCantidad;
		public String _PorcentualFijo_CheckMayorEntreDos;
		public String _PorcentualFijo_CheckMontoMinimo;
		public BigDecimal _PorcentualFijo_ValorMontoMinimo;
		public String _PorcentualFijo_CheckMontoMaximo;
		public BigDecimal _PorcentualFijo_ValorMontoMaximo;
		public String _PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal _PorcentualTasaPlazo_ValorCobro;
		public String _Otros_CheckAfectoImpuesto;
		public String _Otros_CheckPermiteAumentarValor;
		public String _Otros_CheckPermiteDisminuirValor;
		public String _Otros_CheckOperacionContado;
		public String _Otros_CheckOperacionPlazo;
		public String _PorcentualFijo_CheckMenorEntreDos;
		public String _Otros_CheckAllIn;
		public String CodigoCobroClasificacionBase;
		public String _Otros_CheckInteresCompuesto;
		public String _Otros_CheckInteresSimple;
		public String _PorcentualRango_IdMonedaRangoMonto;
		public String _PorcentualRango_IdTipoRangoCobro;
		public String _MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal _MontoFijoPorUnidad_MontoCobro;

	}

	public static class EntidadCobroDefault extends BcsPojo {
		public Long IdPortafolio;
		public String IdCobro;
		public String IdTipoProducto;
		public String IdValorCobro;
		public String IdPerfilCobros;
		public BigDecimal ValorCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;

	}

	public static class EntidadBuscaFolio extends BcsPojo {
		public Long IdPortafolio;
		public String IdCobro;
		public String IdTipoProducto;

	}

	public static class EntidadDeleteCobroOrden extends BcsPojo {
		public Long IdOrden;
		public Long IdCobroOrden;
		public String IdCobro;

	}

	/////////////// funciones/////////

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarOrdenesPorDetallePerfilCobro in = gson.fromJson(jin.toString(),
				EntradaModificarOrdenesPorDetallePerfilCobro.class);
		SalidaModificarOrdenesPorDetallePerfilCobro out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private List<EntidadOrdenesCobros> obtenerDatosDetallePerfilCobros(Connection conn,
			List<Long> IdDetallePerfilCobros) throws Exception {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdDetallePerfilCobros", IdDetallePerfilCobros);

		SQL sql = new SQL();
		List<EntidadOrdenesCobros> detalle = sql.query(conn, SQL_SELECT_DETALLE_PERFIL_COBROS, parametros,
				EntidadOrdenesCobros.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros", fin - inicio);
		return detalle;
	}

	private BigDecimal obtenerNuevosMontosComisionFija(Connection conn, EntidadOrdenesCobros Orden) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		try {
			MontoCobro = MonedaFormato.formatear(conn, Orden.ValorCobro, MonedaFormato.MONTO, Orden.IdMoneda).valor;
		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", fin - inicio);
		return MontoCobro;
	}

	private BigDecimal obtenerNuevosMontosComisionVariable(Connection conn, EntidadOrdenesCobros Orden) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		BigDecimal porcentajeReal = Orden.ValorCobro.divide(new BigDecimal(100), MathContext.DECIMAL128);
		MontoCobro = Orden.MontoOrden.multiply(porcentajeReal, MathContext.DECIMAL128);

		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, Orden.IdMoneda).valor;

		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", fin - inicio);
		return MontoCobro;
	}

	private void ModificaCobroOrdenes(Connection conn, EntidadOrdenesCobros OrdenCobro, BigDecimal CobroComision) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.ModificaCobroOrdenes");
		long inicio = System.currentTimeMillis();
		EntidadUpdateCobroOrden UpdateCobroOrden = new EntidadUpdateCobroOrden();
		List<EntidadUpdateCobroOrden> UpdateCobroOrdenList = new ArrayList<>();
		try {

			UpdateCobroOrden.MontoMinimo = OrdenCobro.MontoMinimo;
			UpdateCobroOrden.ValorCobroPerfil = OrdenCobro.ValorCobro;
			UpdateCobroOrden.ValorCobroAplicado = OrdenCobro.ValorCobro;
			UpdateCobroOrden.ValorCobroFinal = OrdenCobro.ValorCobro;
			UpdateCobroOrden.MontoCobroCalculado = CobroComision;
			UpdateCobroOrden.MontoCobroCalculadoLocal = CobroComision;
			UpdateCobroOrden.MontoCobroCalculadoLiquidacion = CobroComision;
			UpdateCobroOrden.IdOrden = OrdenCobro.IdOrden;
			UpdateCobroOrden.IdCobro = OrdenCobro.IdCobro;

			UpdateCobroOrdenList.add(UpdateCobroOrden);
			logger.debug("UPDATE:: UpdateCobro Ordenes = "+ UpdateCobroOrden);
			SQL sql = new SQL();
			sql.batch(conn, SQL_UPDATE_COBRO_ORDEN, UpdateCobroOrdenList, EntidadUpdateCobroOrden.class);
			System.out.println(UpdateCobroOrden);

		} catch (Exception e) {

			logger.debug("ERROR::ModificarOrdenesPorDetallePerfilCobro.ModificaCobroOrdenes", e);
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.ModificaCobroOrdenes", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.ModificaCobroOrdenes", fin - inicio);

	}

	private void AgregaCobroOrdenes(Connection conn, EntidadOrdenesCobros OrdenCobro, BigDecimal CobroComision) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.AgregaCobroOrdenes");
		long inicio = System.currentTimeMillis();

		try {

			List<resultado> Existen = new ArrayList<>();
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("IdOrden", OrdenCobro.IdOrden);
			param.put("IdCobro", OrdenCobro.IdCobro);

			SQL sql2 = new SQL();

			Existen = sql2.query(conn, SQL_SELCT_EXISTE, param, resultado.class);

			if (Existen.get(0).existen == 0) {

				List<EntidadInsertCobroOrden> NoComision = new ArrayList<>();

				HashMap<String, Object> parametros = new HashMap<String, Object>();
				parametros.put("IdOrden", OrdenCobro.IdOrden);

				SQL sql = new SQL();
				NoComision = sql.query(conn, SQL_SELCT_COBRO_ORDEN, parametros, EntidadInsertCobroOrden.class);

				EntidadInsertCobroOrden InsertCobroOrden = new EntidadInsertCobroOrden();
				List<EntidadInsertCobroOrden> InsertCobroOrdenList = new ArrayList<>();

				InsertCobroOrden.IdGrupoCobro = NoComision.get(0).IdGrupoCobro;
				InsertCobroOrden.IdOrden = NoComision.get(0).IdOrden;
				InsertCobroOrden.IdCobro = OrdenCobro.IdCobro;
				InsertCobroOrden.ValorCobroPerfil = OrdenCobro.ValorCobro;
				InsertCobroOrden.ValorCobroAplicado = OrdenCobro.ValorCobro;
				InsertCobroOrden.MontoCobroCalculado = CobroComision;
				InsertCobroOrden.FormaCobro = NoComision.get(0).FormaCobro;
				InsertCobroOrden.EsComprobante = NoComision.get(0).EsComprobante;
				InsertCobroOrden.FechaIngresoCobro = NoComision.get(0).FechaIngresoCobro;
				InsertCobroOrden.IdCarga = NoComision.get(0).IdCarga;
				InsertCobroOrden.MontoCobroCalculadoLocal = CobroComision;
				InsertCobroOrden.MontoCobroCalculadoLiquidacion = CobroComision;
				InsertCobroOrden.MontoMinimo = OrdenCobro.MontoMinimo;
				InsertCobroOrden.CodigoCobroClasificacion = "COMISION";
				InsertCobroOrden.IdMoneda = NoComision.get(0).IdMoneda;
				InsertCobroOrden.IdOrigenCobro = NoComision.get(0).IdOrigenCobro;
				InsertCobroOrden.IdAsociacion = NoComision.get(0).IdAsociacion;
				InsertCobroOrden.ValorCobroFinal = OrdenCobro.ValorCobro;
				InsertCobroOrden.PermiteCambiarValor = NoComision.get(0).PermiteCambiarValor;
				InsertCobroOrden._CheckCobroMonedaOperacion = NoComision.get(0)._CheckCobroMonedaOperacion;
				InsertCobroOrden._IdMonedaCobroOtraMoneda = NoComision.get(0)._IdMonedaCobroOtraMoneda;
				InsertCobroOrden._MontoFijo_MontoCobro = NoComision.get(0)._MontoFijo_MontoCobro;
				InsertCobroOrden._MontoFijo_CheckMontoOperacionMenor = NoComision
						.get(0)._MontoFijo_CheckMontoOperacionMenor;
				InsertCobroOrden._MontoFijo_ValorMontoOperacionMenor = NoComision
						.get(0)._MontoFijo_ValorMontoOperacionMenor;
				InsertCobroOrden._MontoFijo_CheckSiNo = NoComision.get(0)._MontoFijo_CheckSiNo;
				InsertCobroOrden._MontoFijo_ValorMontoSiNo = NoComision.get(0)._MontoFijo_ValorMontoSiNo;
				InsertCobroOrden._PorcentualFijo_ValorCobro = NoComision.get(0)._PorcentualFijo_ValorCobro;
				InsertCobroOrden._PorcentualFijo_CheckSobreMonto = NoComision.get(0)._PorcentualFijo_CheckSobreMonto;
				InsertCobroOrden._PorcentualFijo_CheckSobreCantidad = NoComision
						.get(0)._PorcentualFijo_CheckSobreCantidad;
				InsertCobroOrden._PorcentualFijo_CheckMayorEntreDos = NoComision
						.get(0)._PorcentualFijo_CheckMayorEntreDos;
				InsertCobroOrden._PorcentualFijo_CheckMontoMinimo = NoComision.get(0)._PorcentualFijo_CheckMontoMinimo;
				InsertCobroOrden._PorcentualFijo_ValorMontoMinimo = NoComision.get(0)._PorcentualFijo_ValorMontoMinimo;
				InsertCobroOrden._PorcentualFijo_CheckMontoMaximo = NoComision.get(0)._PorcentualFijo_CheckMontoMaximo;
				InsertCobroOrden._PorcentualFijo_ValorMontoMaximo = NoComision.get(0)._PorcentualFijo_ValorMontoMaximo;
				InsertCobroOrden._PorcentualFijo_IdMonedaMinimoMaximo = NoComision
						.get(0)._PorcentualFijo_IdMonedaMinimoMaximo;
				InsertCobroOrden._PorcentualTasaPlazo_ValorCobro = NoComision.get(0)._PorcentualTasaPlazo_ValorCobro;
				InsertCobroOrden._Otros_CheckAfectoImpuesto = NoComision.get(0)._Otros_CheckAfectoImpuesto;
				InsertCobroOrden._Otros_CheckPermiteAumentarValor = NoComision.get(0)._Otros_CheckPermiteAumentarValor;
				InsertCobroOrden._Otros_CheckPermiteDisminuirValor = NoComision
						.get(0)._Otros_CheckPermiteDisminuirValor;
				InsertCobroOrden._Otros_CheckOperacionContado = NoComision.get(0)._Otros_CheckOperacionContado;
				InsertCobroOrden._Otros_CheckOperacionPlazo = NoComision.get(0)._Otros_CheckOperacionPlazo;
				InsertCobroOrden._PorcentualFijo_CheckMenorEntreDos = NoComision
						.get(0)._PorcentualFijo_CheckMenorEntreDos;
				InsertCobroOrden._Otros_CheckAllIn = NoComision.get(0)._Otros_CheckAllIn;
				InsertCobroOrden.CodigoCobroClasificacionBase = "COMISION";
				InsertCobroOrden._Otros_CheckInteresCompuesto = NoComision.get(0)._Otros_CheckInteresCompuesto;
				InsertCobroOrden._Otros_CheckInteresSimple = NoComision.get(0)._Otros_CheckInteresSimple;
				InsertCobroOrden._PorcentualRango_IdMonedaRangoMonto = NoComision
						.get(0)._PorcentualRango_IdMonedaRangoMonto;
				InsertCobroOrden._PorcentualRango_IdTipoRangoCobro = NoComision
						.get(0)._PorcentualRango_IdTipoRangoCobro;
				InsertCobroOrden._MontoFijoPorUnidad_AllInCheckValor = NoComision
						.get(0)._MontoFijoPorUnidad_AllInCheckValor;
				InsertCobroOrden._MontoFijoPorUnidad_MontoCobro = NoComision.get(0)._MontoFijoPorUnidad_MontoCobro;

				InsertCobroOrdenList.add(InsertCobroOrden);

				sql.batch(conn, SQL_INSERT_COBRO_ORDEN, InsertCobroOrdenList, EntidadInsertCobroOrden.class);
			} else {

				logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.AgregaCobroOrdenes", "ya existen comisiones");
			}

		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.AgregaCobroOrdenes", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.AgregaCobroOrdenes", fin - inicio);

	}

	private BigDecimal BuscaValorPocentaje(Connection conn, Long idOrden, BigDecimal MontoNuevo,
			BigDecimal ValorOrden) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.BuscaValorPocentaje");
		long inicio = System.currentTimeMillis();

		EntidadBuscaSuma valorFinalRetorno = new EntidadBuscaSuma();

		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("IdOrden", idOrden);
			List<EntidadBuscaSuma> valorFinal = sql.query(conn, SQL_SELCT_SUM, parametros, EntidadBuscaSuma.class);

			valorFinalRetorno.PorcentajeSuma = ((valorFinal.get(0).PorcentajeSuma.add(MontoNuevo,
					MathContext.DECIMAL128)).multiply(BigDecimal.valueOf(100), MathContext.DECIMAL128))
							.divide(ValorOrden, MathContext.DECIMAL128);

		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.BuscaValorPocentaje", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.BuscaValorPocentaje", fin - inicio);
		return valorFinalRetorno.PorcentajeSuma;
	}

	private void ModificaOrdenes(Connection conn, List<EntidadUpdateOrden> UpdateOrden) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.ModificaOrdenes");
		long inicio = System.currentTimeMillis();

		try {
			SQL sql = new SQL();
			sql.batch(conn, SQL_UPDATE_ORDEN, UpdateOrden, EntidadUpdateOrden.class);

		} catch (Exception e) {
			logger.debug("ERROR::ModificarOrdenesPorDetallePerfilCobro.ModificaOrdenes", e);
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.ModificaOrdenes", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.ModificaOrdenes", fin - inicio);

	}

	private List<EntidadCobroDefault> obtenerCobrosDefault(Connection conn, List<Long> IdDetallePerfilCobros)
			throws Exception {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerValoresDefault");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdDetallePerfilCobros", IdDetallePerfilCobros);
		SQL sql = new SQL();
		List<EntidadBuscaFolio> Folio = sql.query(conn, SQL_SELCT_PORTAFOLIO, parametros, EntidadBuscaFolio.class);

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("IdPortafolio", Folio.get(0).IdPortafolio);
		param.put("IdCobro", Folio.get(0).IdCobro);
		param.put("IdTipoProducto", Folio.get(0).IdTipoProducto);
		List<EntidadCobroDefault> CobrosDefault = sql.query(conn, SQL_SELCT_COBRO_DEFAULT, param,
				EntidadCobroDefault.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerValoresDefault", fin - inicio);

		return CobrosDefault;
	}

	private BigDecimal obtenerNuevosMontosComisionFijaDefault(Connection conn, EntidadOrdenesCobros Orden,
			EntidadCobroDefault Default) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		try {
			MontoCobro = MonedaFormato.formatear(conn, Default.ValorCobro, MonedaFormato.MONTO, Default.IdMoneda).valor;
		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", fin - inicio);
		return MontoCobro;
	}

	private BigDecimal obtenerNuevosMontosComisionVariableDefault(Connection conn, EntidadOrdenesCobros Orden,
			EntidadCobroDefault Default) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		BigDecimal porcentajeReal = Default.ValorCobro.divide(new BigDecimal(100), MathContext.DECIMAL128);
		MontoCobro = Orden.MontoOrden.multiply(porcentajeReal, MathContext.DECIMAL128);

		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, Orden.IdMoneda).valor;
			if (MontoCobro.compareTo(Default.ValorTopeMinimoOrden) >= 0) {
				MontoCobro = Default.ValorTopeMinimoOrden;
			}

		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", fin - inicio);
		return MontoCobro;
	}

	private void EliminaCobroOrden(Connection conn, Long p_IdOrden, Long p_IdCobroOrden, String p_IdCobro)
			throws Exception {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.EliminaCobroOrden");
		long inicio = System.currentTimeMillis();

		List<EntidadDeleteCobroOrden> DeleteCobroOrdenList = new ArrayList<>();
		EntidadDeleteCobroOrden DeleteCobroOrden = new EntidadDeleteCobroOrden();
		DeleteCobroOrden.IdOrden = p_IdOrden;
		DeleteCobroOrden.IdCobroOrden = p_IdCobroOrden;
		DeleteCobroOrden.IdCobro = p_IdCobro;
		DeleteCobroOrdenList.add(DeleteCobroOrden);
		SQL sql = new SQL();
		logger.debug("DELETE::  = "+ DeleteCobroOrden);
		sql.batch(conn, SQL_DELETE_ORDEN_COBRO, DeleteCobroOrdenList, EntidadDeleteCobroOrden.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.EliminaCobroOrden", fin - inicio);

	}

	////////////////// Logica ////////////////
	public Long NumOrden;
	public List<EntidadCobroDefault> CobroDefault;

	public SalidaModificarOrdenesPorDetallePerfilCobro logica(Connection conn,
			EntradaModificarOrdenesPorDetallePerfilCobro entrada) throws Exception {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro");
		long inicio = System.currentTimeMillis();

		SalidaModificarOrdenesPorDetallePerfilCobro salida = new SalidaModificarOrdenesPorDetallePerfilCobro();

		List<EntidadOrdenesCobros> OrdenesCobros = obtenerDatosDetallePerfilCobros(conn,
				entrada.p_IdDetallePerfilCobros);
		if (OrdenesCobros.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PERFIL_COBROS_NO_EXISTE",
					new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream()
					.filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count();
			return salida;
		}

		try {
			CobroDefault = obtenerCobrosDefault(conn, entrada.p_IdDetallePerfilCobros);

		} catch (Exception e) {
			logger.debug("ModificarOrdenesPorDetallePerfilCobro", "error ", e);
		}

		NumOrden = 0l;
		OrdenesCobros.stream().forEach(OrdenCobro -> {

			if (OrdenCobro.IdOrden.compareTo(NumOrden) == 1 || OrdenCobro.IdCobro.equalsIgnoreCase("COVA") == true
					|| OrdenCobro.IdCobro.equalsIgnoreCase("COFI") == true) {
				NumOrden = OrdenCobro.IdOrden;

				if (OrdenCobro.IdCobro.equalsIgnoreCase("COVA") == true
						|| OrdenCobro.IdCobro.equalsIgnoreCase("COFI") == true) {

					BigDecimal CobroComision = BigDecimal.ZERO;
					BigDecimal PorcentajeCobroSumado = BigDecimal.ZERO;

					if (OrdenCobro.Borrado.equalsIgnoreCase("N") == true) {

						if (OrdenCobro.IdCobro.equalsIgnoreCase("COVA")) {
							CobroComision = obtenerNuevosMontosComisionVariable(conn, OrdenCobro);

						}
						if (OrdenCobro.IdCobro.equalsIgnoreCase("COFI")) {

							CobroComision = obtenerNuevosMontosComisionFija(conn, OrdenCobro);
						}

						AgregaCobroOrdenes(conn, OrdenCobro, CobroComision);
						ModificaCobroOrdenes(conn, OrdenCobro, CobroComision);

						EntidadUpdateOrden UpdateOrden = new EntidadUpdateOrden();
						List<EntidadUpdateOrden> UpdateOrdenList = new ArrayList<>();
						PorcentajeCobroSumado = BuscaValorPocentaje(conn, OrdenCobro.IdOrden, CobroComision,
								OrdenCobro.MontoOrden);
						UpdateOrden.PorcentajeCobros = PorcentajeCobroSumado;
						UpdateOrden.IdOrden = OrdenCobro.IdOrden;
						UpdateOrdenList.add(UpdateOrden);
						ModificaOrdenes(conn, UpdateOrdenList);

					} else {
						
						if (CobroDefault.isEmpty()) {
							System.out.println("IdOrden = " + OrdenCobro.IdOrden + " IdCobroOrden = "
									+ OrdenCobro.IdCobroOrden + " IdCobro = " + OrdenCobro.IdCobro);

							try {

								EliminaCobroOrden(conn, OrdenCobro.IdOrden, OrdenCobro.IdCobroOrden,
										OrdenCobro.IdCobro);

							} catch (Exception e) {
								System.out.println("Error " + e);
							}

						} else {

							if (OrdenCobro.IdCobro.equalsIgnoreCase("COVA")) {
								CobroComision = obtenerNuevosMontosComisionVariableDefault(conn, OrdenCobro,
										CobroDefault.get(0));

							}
							if (OrdenCobro.IdCobro.equalsIgnoreCase("COFI")) {

								CobroComision = obtenerNuevosMontosComisionFijaDefault(conn, OrdenCobro,
										CobroDefault.get(0));
							}
							System.out.println("Comision = " + CobroComision);

							EntidadOrdenesCobros OrdenCobroDefault = new EntidadOrdenesCobros();
							OrdenCobroDefault = OrdenCobro;

							OrdenCobroDefault.MontoMinimo = CobroDefault.get(0).ValorTopeMinimoOrden;
							OrdenCobroDefault.ValorCobro = CobroDefault.get(0).ValorCobro;
							OrdenCobroDefault.IdOrden = OrdenCobro.IdOrden;
							OrdenCobroDefault.IdCobro = OrdenCobro.IdCobro;

							ModificaCobroOrdenes(conn, OrdenCobroDefault, CobroComision);

							PorcentajeCobroSumado = BuscaValorPocentaje(conn, OrdenCobro.IdOrden, CobroComision,
									OrdenCobro.MontoOrden);
							EntidadUpdateOrden UpdateOrden = new EntidadUpdateOrden();
							List<EntidadUpdateOrden> UpdateOrdenList = new ArrayList<>();
							UpdateOrden.PorcentajeCobros = PorcentajeCobroSumado;
							UpdateOrden.IdOrden = OrdenCobro.IdOrden;
							UpdateOrdenList.add(UpdateOrden);
							ModificaOrdenes(conn, UpdateOrdenList);
						}
					}
				}
			}
		});

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			MensajeSalida mensaje = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MODIFICACION_INGRESO_EXITO",
					new HashMap<String, String>(), conn, entrada.p_IdEmpresa);
			salida.P_OUT_MENSAJES.add(mensaje);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro", fin - inicio);
		return salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetallePerfilCobros\":[2896]}";

				EntradaModificarOrdenesPorDetallePerfilCobro Objin = gson.fromJson(json,
						EntradaModificarOrdenesPorDetallePerfilCobro.class);
				System.out.println(Objin.toString());
				ModificarOrdenesPorDetallePerfilCobro modificarMovimientoComprobanteCobrosMasivo = new ModificarOrdenesPorDetallePerfilCobro();
				SalidaModificarOrdenesPorDetallePerfilCobro Objout = modificarMovimientoComprobanteCobrosMasivo
						.logica(con, Objin);

				System.out.println("____Salida_____");
				System.out.println(Objout);
				//con.rollback();
				 con.commit();

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}

}
