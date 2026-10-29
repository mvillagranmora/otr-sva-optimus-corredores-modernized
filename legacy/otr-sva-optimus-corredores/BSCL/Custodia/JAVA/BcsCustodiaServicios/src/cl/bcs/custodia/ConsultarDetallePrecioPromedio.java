package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarDetallePrecioPromedio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarDetallePrecioPromedio.class);
	
	private String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectInstrumentos.sql");
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraHistorico.sql");
	
	private final String SQL_SELECT_CARTERA_NEGATIVA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraNegativaActual.sql");
	private final String SQL_SELECT_CARTERA_NEGATIVA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraNegativaHistorico.sql");
	
	private final String SQL_SELECT_CARTERA_EN_TRANSITO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraEnTransitoActual.sql");
	private final String SQL_SELECT_CARTERA_EN_TRANSITO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectCarteraEnTransitoHistorico.sql");
	
	private final String SQL_SELECT_SUBYACENTE_SIMULTANEA_VENTA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteSimultaneaVentaActual.sql");
	private final String SQL_SELECT_SUBYACENTE_SIMULTANEA_VENTA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteSimultaneaVentaHistorico.sql");
	
	private final String SQL_SELECT_SUBYACENTE_SIMULTANEA_COMPRA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteSimultaneaCompraActual.sql");
	private final String SQL_SELECT_SUBYACENTE_SIMULTANEA_COMPRA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteSimultaneaCompraHistorico.sql");
	
	private final String SQL_SELECT_SUBYACENTE_EMPRESTITO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteEmprestitoActual.sql");
	private final String SQL_SELECT_SUBYACENTE_EMPRESTITO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacenteEmprestitoHistorico.sql");
	
	private final String SQL_SELECT_SUBYACENTE_PRESTAMO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacentePrestamoActual.sql");
	private final String SQL_SELECT_SUBYACENTE_PRESTAMO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectSubyacentePrestamoHistorico.sql");
	
	private final String SQL_SELECT_GARANTIA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectGarantiaActual.sql");
	private final String SQL_SELECT_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarDetallePrecioPromedio_SelectGarantiaHistorico.sql");
	
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class EntradaConsultarDetallePrecioPromedio{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaBusqueda;
		public String p_SumaVentaPlazoEnDisponible;
		public List<InPortafolio> p_Portafolios;
		public List<InInstrumento> p_Instrumentos;
		
		public EntradaConsultarDetallePrecioPromedio() {
			this.p_Portafolios = new ArrayList<InPortafolio>();
			this.p_Instrumentos = new ArrayList<InInstrumento>();
			this.p_SumaVentaPlazoEnDisponible = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarDetallePrecioPromedio{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Cartera> P_OUT_CARTERAS;
		public List<Cartera> P_OUT_CARTERAS_EN_TRANSITO;
		public List<Subyacente> P_OUT_SIMULTANEAS_VENTA;
		public List<Subyacente> P_OUT_SIMULTANEAS_COMPRA;
		public List<Subyacente> P_OUT_EMPRESTITO;
		public List<Subyacente> P_OUT_PRESTAMO;
		public List<Garantia> P_OUT_GARANTIA;
		public List<Totalizador> P_OUT_TOTALIZADOR;
		public List<PrecioPromedio> P_OUT_PRECIO_PROMEDIO_ACTIVO;
		public List<PrecioPromedio> P_OUT_PRECIO_PROMEDIO_PASIVO;
		
		public SalidaConsultarDetallePrecioPromedio() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CARTERAS = new ArrayList<Cartera>();
			this.P_OUT_CARTERAS_EN_TRANSITO = new ArrayList<Cartera>();
			this.P_OUT_SIMULTANEAS_VENTA = new ArrayList<Subyacente>();
			this.P_OUT_SIMULTANEAS_COMPRA = new ArrayList<Subyacente>();
			this.P_OUT_EMPRESTITO = new ArrayList<Subyacente>();
			this.P_OUT_PRESTAMO = new ArrayList<Subyacente>();
			this.P_OUT_GARANTIA = new ArrayList<Garantia>();
			this.P_OUT_TOTALIZADOR = new ArrayList<Totalizador>();
			this.P_OUT_PRECIO_PROMEDIO_ACTIVO = new ArrayList<PrecioPromedio>();
			this.P_OUT_PRECIO_PROMEDIO_PASIVO = new ArrayList<PrecioPromedio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InPortafolio{
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InInstrumento{
		public Long IdInstrumento;
		public String IdBoveda;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Cartera{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public String IdTipoOperacion;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCarteraPrecioCompra;
		public BigDecimal ValorFactorMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Subyacente{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdCarteraOrigen;
		public Long FolioCarteraOrigen;
		public String IdClasificacionTipoInstrumento;
		public java.sql.Date FechaVencimientoOperacion;
		public java.sql.Date FechaLiquidacion;
		public BigDecimal Cantidad;
		public BigDecimal PrecioCompra;
		public BigDecimal PrecioAPlazo;
		public BigDecimal TasaCompra;
		public BigDecimal MontoCarteraPrecioCompra;
		public BigDecimal MontoCarteraPrecioPlazo;
		public BigDecimal ValorFactorMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Garantia{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long IdCarteraOrigen;
		public Long FolioCarteraOrigen;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCarteraPrecioCompra;
		public BigDecimal ValorFactorMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioPromedio{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public BigDecimal TotalCantidad;
		public BigDecimal TotalMonto;
		public BigDecimal PrecioPromedio;
		
		public PrecioPromedio() {
			this.TotalCantidad = BigDecimal.ZERO;
			this.TotalMonto = BigDecimal.ZERO;
			this.PrecioPromedio = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Totalizador{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public BigDecimal TotalCantidadCarteras;
		public BigDecimal TotalCantidadSubyacenteSimultaneaVenta;
		public BigDecimal TotalCantidadSubyacenteEmprestito;
		public BigDecimal TotalCantidadGarantias;
		public BigDecimal TotalCantidadCarterasEnTransito;
		public BigDecimal TotalCantidadSubyacenteSimultaneaCompra;
		public BigDecimal TotalCantidadSubyacentePrestamo;
		public BigDecimal TotalMontoCarteras;
		public BigDecimal TotalMontoSubyacentesSimultaneaVenta;
		public BigDecimal TotalMontoPlazoSubyacentesSimultaneaVenta;
		public BigDecimal TotalMontoSubyacentesEmprestito;
		public BigDecimal TotalMontoPlazoSubyacentesEmprestito;
		public BigDecimal TotalMontoGarantias;
		public BigDecimal TotalMontoCarterasEnTransito;
		public BigDecimal TotalMontoSubyacentesSimultaneaCompra;
		public BigDecimal TotalMontoPlazoSubyacentesSimultaneaCompra;
		public BigDecimal TotalMontoSubyacentesPrestamo;
		public BigDecimal TotalMontoPlazoSubyacentesPrestamo;
		
		public Totalizador() {
			this.TotalCantidadCarteras = BigDecimal.ZERO;
			this.TotalCantidadSubyacenteSimultaneaVenta = BigDecimal.ZERO;
			this.TotalCantidadSubyacenteEmprestito = BigDecimal.ZERO;
			this.TotalCantidadGarantias = BigDecimal.ZERO;
			this.TotalCantidadCarterasEnTransito = BigDecimal.ZERO;
			this.TotalCantidadSubyacenteSimultaneaCompra = BigDecimal.ZERO;
			this.TotalCantidadSubyacentePrestamo = BigDecimal.ZERO;
			this.TotalMontoCarteras = BigDecimal.ZERO;
			this.TotalMontoSubyacentesSimultaneaVenta = BigDecimal.ZERO;
			this.TotalMontoPlazoSubyacentesSimultaneaVenta = BigDecimal.ZERO;
			this.TotalMontoSubyacentesEmprestito = BigDecimal.ZERO;
			this.TotalMontoPlazoSubyacentesEmprestito = BigDecimal.ZERO;
			this.TotalMontoGarantias = BigDecimal.ZERO;
			this.TotalMontoCarterasEnTransito = BigDecimal.ZERO;
			this.TotalMontoSubyacentesSimultaneaCompra = BigDecimal.ZERO;
			this.TotalMontoPlazoSubyacentesSimultaneaCompra = BigDecimal.ZERO;
			this.TotalMontoSubyacentesPrestamo = BigDecimal.ZERO;
			this.TotalMontoPlazoSubyacentesPrestamo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento{
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMoneda;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class PortafoliosConCustodia{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdBoveda;
		public String IdClasificacionTipoInstrumento;
		
		@Override
		public int hashCode() {
			return 31 * this.IdPortafolio.hashCode() + this.IdInstrumento.hashCode() + this.IdBoveda.hashCode() + this.IdClasificacionTipoInstrumento.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			PortafoliosConCustodia portafoliosConCustodia = (PortafoliosConCustodia) obj;
			return this.IdPortafolio.equals(portafoliosConCustodia.IdPortafolio) && this.IdInstrumento.equals(portafoliosConCustodia.IdInstrumento) && this.IdBoveda.equals(portafoliosConCustodia.IdBoveda) && this.IdClasificacionTipoInstrumento.equals(portafoliosConCustodia.IdClasificacionTipoInstrumento);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarDetallePrecioPromedio jin = gson.fromJson(in.toString(), EntradaConsultarDetallePrecioPromedio.class);
		SalidaConsultarDetallePrecioPromedio out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarDetallePrecioPromedio logica(Connection con, EntradaConsultarDetallePrecioPromedio entrada) throws Exception {
		logger.debug("INICIO::ConsultarDetallePrecioPromedio");
		SalidaConsultarDetallePrecioPromedio out = new SalidaConsultarDetallePrecioPromedio();
		
		Long idTrabajo =  Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
			Long idPaisSistema = obtenerIdPaisSistema(con,entrada.p_IdEmpresa);
			String usaPrecioAmortizacion = obtenerUsaPrecioAmortizacion(con,entrada.p_IdEmpresa);
			String usaPrimaAmortizacion = obtenerUsaPrimaAmortizacion(con,entrada.p_IdEmpresa);
			boolean esBusquedaHistorico = esBusquedaHistorico(entrada.p_FechaBusqueda, fechaSistema);
			
			HashMap<String,Object> parametros = obtenerParametros(con, entrada, esBusquedaHistorico, idTrabajo);
			
			out.P_OUT_CARTERAS = obtenerCarteras(con,parametros,esBusquedaHistorico);
			out.P_OUT_CARTERAS.addAll(obtenerCarterasNegativas(con,parametros,esBusquedaHistorico));
			out.P_OUT_CARTERAS_EN_TRANSITO = obtenerCarterasEnTransito(con,parametros,esBusquedaHistorico);
			out.P_OUT_SIMULTANEAS_VENTA = obtenerSubyacentesSimultaneasVenta(con,parametros,esBusquedaHistorico);
			out.P_OUT_SIMULTANEAS_COMPRA = obtenerSubyacentesSimultaneasCompra(con,parametros,esBusquedaHistorico);
			out.P_OUT_EMPRESTITO = obtenerSubyacentesEmprestito(con,parametros,esBusquedaHistorico);
			out.P_OUT_PRESTAMO = obtenerSubyacentesPrestamo(con,parametros,esBusquedaHistorico);
			out.P_OUT_GARANTIA = obtenerGarantia(con,parametros,esBusquedaHistorico);
			
			if("N".equals(entrada.p_SumaVentaPlazoEnDisponible)) {
				out.P_OUT_CARTERAS = filtrarCarterasPlazo(out.P_OUT_CARTERAS);
			}
			
			List<PortafoliosConCustodia> portafoliosConCustodia = obtenerPortafoliosConCustodia(out.P_OUT_CARTERAS,out.P_OUT_CARTERAS_EN_TRANSITO,out.P_OUT_SIMULTANEAS_VENTA,out.P_OUT_SIMULTANEAS_COMPRA,out.P_OUT_EMPRESTITO,out.P_OUT_PRESTAMO,out.P_OUT_GARANTIA);
			
			List<Long> idInstrumentos = obtenerIdInstrumentos(portafoliosConCustodia);
			
			List<Instrumento> instrumentos = obtenerInstrumentos(con,idInstrumentos);
			
			HashMap<Long,BigDecimal> preciosMercado = obternerPrecioMercado(con,entrada.p_FechaBusqueda,instrumentos,entrada.p_IdEmpresa,entrada.p_IdModulo,entrada.p_IdUsuario,idPaisSistema);
			
			calcularMontoCartera(out.P_OUT_CARTERAS);
			calcularMontoCartera(out.P_OUT_CARTERAS_EN_TRANSITO);
			calcularMontoSubyacenteVentaSimultanea(out.P_OUT_SIMULTANEAS_VENTA,usaPrecioAmortizacion,entrada.p_FechaBusqueda);
			calcularMontoSubyacenteCompraSimultanea(out.P_OUT_SIMULTANEAS_COMPRA, preciosMercado);
			calcularMontoSubyacenteEmprestito(out.P_OUT_EMPRESTITO);
			calcularMontoSubyacentePrestamo(out.P_OUT_PRESTAMO,usaPrimaAmortizacion,entrada.p_FechaBusqueda,preciosMercado);
			calcularMontoGarantia(out.P_OUT_GARANTIA);
			
			out.P_OUT_TOTALIZADOR = obtenerTotalizador(portafoliosConCustodia, out.P_OUT_CARTERAS,out.P_OUT_CARTERAS_EN_TRANSITO,out.P_OUT_SIMULTANEAS_VENTA,out.P_OUT_SIMULTANEAS_COMPRA,out.P_OUT_EMPRESTITO,out.P_OUT_PRESTAMO,out.P_OUT_GARANTIA);
			
			out.P_OUT_PRECIO_PROMEDIO_ACTIVO = obtenerPrecioPromedio(out.P_OUT_TOTALIZADOR);
			out.P_OUT_PRECIO_PROMEDIO_PASIVO = obtenerPrecioPromedioPasivo(out.P_OUT_TOTALIZADOR);
			
		} catch (Exception e) {
			logger.error("ERROR::ConsultarDetallePrecioPromedio", e);
			out.P_OUT_ERRORESCONTADOR = 1L;
			throw e;
		} finally {
			borrarPortafolioPaso(con, idTrabajo);
		}
		
		
		logger.debug("FIN::ConsultarDetallePrecioPromedio");
		
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private Long obtenerIdPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerIdPaisSistema");
		Long idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerIdPaisSistema");
		return idPais;
	}
	
	private String obtenerUsaPrecioAmortizacion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerUsaPrecioAmortizacion");
		String usaPrecioAmortizacion = "N";
		try {
			usaPrecioAmortizacion = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "CUSTODIA", "UsaPrecioAmortizacionPrecioPromedio");
		}catch(Exception e) {
			logger.warn("ERROR:ConsultarDetallePrecioPromedio.obtenerUsaPrecioAmortizacion::Error a encontrar parametro");
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerUsaPrecioAmortizacion");
		return usaPrecioAmortizacion;
	}
	
	private String obtenerUsaPrimaAmortizacion(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerUsaPrimaAmortizacion");
		String usaPrimaAmortizacion = "N";
		try {
			usaPrimaAmortizacion = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "CUSTODIA", "UsaPrimaAmortizacionPrecioPromedio");
		}catch(Exception e) {
			logger.warn("ERROR:ConsultarDetallePrecioPromedio.obtenerUsaPrimaAmortizacion::Error a encontrar parametro");
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerUsaPrimaAmortizacion");
		return usaPrimaAmortizacion;
	}
	
	private boolean esBusquedaHistorico(String fechaBusqueda, String fechaSistema) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerFechaSistema");
		Date dateFechaBusqueda = Fecha.toDate(fechaBusqueda);
		Date dateFechaSistema =Fecha.toDate(fechaSistema);
		boolean esBusquedaHistorico = dateFechaBusqueda.compareTo(dateFechaSistema) == -1;
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerFechaSistema");
		return esBusquedaHistorico;
	}
	
	private HashMap<String,Object> obtenerParametros(Connection con, EntradaConsultarDetallePrecioPromedio entrada, boolean esBusquedaHistorico, Long idTrabajo) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerParametros");
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		List<Long> idPortafolios = entrada.p_Portafolios.parallelStream().filter(portafolio->portafolio.IdPortafolio != null && portafolio.IdPortafolio != 0).map(portafolio->portafolio.IdPortafolio).collect(Collectors.toList());
		List<Long> idInstrumentos = entrada.p_Instrumentos.parallelStream().filter(instrumento->instrumento.IdInstrumento != null && instrumento.IdInstrumento != 0).map(portafolio->portafolio.IdInstrumento).distinct().collect(Collectors.toList());
		List<String> idBovedas = entrada.p_Instrumentos.parallelStream().filter(boveda->boveda.IdBoveda != null && !boveda.IdBoveda.isEmpty()).map(boveda->boveda.IdBoveda).distinct().collect(Collectors.toList());
		List<String> idClasificacionTipoInstrumento = entrada.p_Instrumentos.parallelStream().filter(clasificacion->clasificacion.IdClasificacionTipoInstrumento != null && !clasificacion.IdClasificacionTipoInstrumento.isEmpty()).map(clasificacion->clasificacion.IdClasificacionTipoInstrumento).distinct().collect(Collectors.toList());
		String buscaPorPortafolio = idPortafolios.size() > 0 ? "S" : "N";
		String buscaPorInstrumento = idInstrumentos.size() > 0 ? "S" : "N";
		String buscaPorBoveda = idBovedas.size() > 0 ? "S" : "N";
		String buscarPorClasificacionTipoInstrumento = idClasificacionTipoInstrumento.size() > 0 ? "S" : "N";
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdInstrumento", idInstrumentos);
		parametros.put("IdBoveda", idBovedas);
		parametros.put("IdClasificacionTipoInstrumento", idClasificacionTipoInstrumento);
		parametros.put("BuscarPorPortafolio", buscaPorPortafolio);
		parametros.put("BuscarPorInstrumento", buscaPorInstrumento);
		parametros.put("BuscarPorBoveda", buscaPorBoveda);
		parametros.put("BuscarPorClasificacionTipoInstrumento", buscarPorClasificacionTipoInstrumento);
		parametros.put("IdTrabajo", idTrabajo);
		
		if("S".equals(buscaPorPortafolio)) {
			insertarPortafolioPaso(con, idTrabajo, idPortafolios);
		}
		
		if(esBusquedaHistorico) {
			parametros.put("Fecha", entrada.p_FechaBusqueda);
		}
		
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerParametros");
		return parametros;
	}
	
	private void insertarPortafolioPaso(Connection con, Long idTrabajo, List<Long> portafolios) throws Exception {
		logger.debug("INICIO::ConsultarDetallePrecioPromedio.insertarPortafolioPaso");
		
		List<Portafolio> portafoliosBusqueda = portafolios.stream()
				.map(portafolio->{
					Portafolio portafolioBusqueda = new Portafolio();
					portafolioBusqueda.IdPortafolio = portafolio;
					portafolioBusqueda.IdTrabajo = idTrabajo;
					return portafolioBusqueda;
				})
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portafoliosBusqueda, Portafolio.class);
		
		logger.debug("FIN::ConsultarDetallePrecioPromedio.insertarPortafolioPaso");
	}
	
	private List<Cartera> obtenerCarteras(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerCarteras");
		SQL sql=new SQL();
		List<Cartera> carteras = new ArrayList<Cartera>();
		if(esBusquedaHistorico) {
			carteras = sql.query(con, SQL_SELECT_CARTERA_HISTORICO, parametros, Cartera.class);
		}else {
			carteras = sql.query(con, SQL_SELECT_CARTERA_ACTUAL, parametros, Cartera.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerCarteras");
		return carteras;
	}
	
	private List<Cartera> obtenerCarterasNegativas(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerCarteras");
		SQL sql=new SQL();
		List<Cartera> carteras = new ArrayList<Cartera>();
		if(esBusquedaHistorico) {
			carteras = sql.query(con, SQL_SELECT_CARTERA_NEGATIVA_HISTORICO, parametros, Cartera.class);
		}else {
			carteras = sql.query(con, SQL_SELECT_CARTERA_NEGATIVA_ACTUAL, parametros, Cartera.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerCarteras");
		return carteras;
	}
	
	private List<Cartera> obtenerCarterasEnTransito(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerCarterasEnTransito");
		SQL sql=new SQL();
		List<Cartera> carteras = new ArrayList<Cartera>();
		if(esBusquedaHistorico) {
			carteras = sql.query(con, SQL_SELECT_CARTERA_EN_TRANSITO_HISTORICO, parametros, Cartera.class);
		}else {
			carteras = sql.query(con, SQL_SELECT_CARTERA_EN_TRANSITO_ACTUAL, parametros, Cartera.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerCarterasEnTransito");
		return carteras;
	}
	
	private List<Subyacente> obtenerSubyacentesSimultaneasVenta(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerSubyacentesSimultaneasVenta");
		SQL sql=new SQL();
		List<Subyacente> subyacentesSimultaneasVenta = new ArrayList<Subyacente>();
		if(esBusquedaHistorico) {
			subyacentesSimultaneasVenta = sql.query(con, SQL_SELECT_SUBYACENTE_SIMULTANEA_VENTA_HISTORICO, parametros, Subyacente.class);
		}else {
			subyacentesSimultaneasVenta = sql.query(con, SQL_SELECT_SUBYACENTE_SIMULTANEA_VENTA_ACTUAL, parametros, Subyacente.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerSubyacentesSimultaneasVenta");
		return subyacentesSimultaneasVenta;
	}
	
	private List<Subyacente> obtenerSubyacentesSimultaneasCompra(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerSubyacentesSimultaneasCompra");
		SQL sql=new SQL();
		List<Subyacente> subyacentesSimultaneasCompra = new ArrayList<Subyacente>();
		if(esBusquedaHistorico) {
			subyacentesSimultaneasCompra = sql.query(con, SQL_SELECT_SUBYACENTE_SIMULTANEA_COMPRA_HISTORICO, parametros, Subyacente.class);
		}else {
			subyacentesSimultaneasCompra = sql.query(con, SQL_SELECT_SUBYACENTE_SIMULTANEA_COMPRA_ACTUAL, parametros, Subyacente.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerSubyacentesSimultaneasCompra");
		return subyacentesSimultaneasCompra;
	}
	
	private List<Subyacente> obtenerSubyacentesEmprestito(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerSubyacentesEmprestito");
		SQL sql=new SQL();
		List<Subyacente> subyacentesEmprestito = new ArrayList<Subyacente>();
		if(esBusquedaHistorico) {
			subyacentesEmprestito = sql.query(con, SQL_SELECT_SUBYACENTE_EMPRESTITO_HISTORICO, parametros, Subyacente.class);
		}else {
			subyacentesEmprestito = sql.query(con, SQL_SELECT_SUBYACENTE_EMPRESTITO_ACTUAL, parametros, Subyacente.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerSubyacentesEmprestito");
		
		return subyacentesEmprestito;
	}
	
	private List<Subyacente> obtenerSubyacentesPrestamo(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerSubyacentesPrestamo");
		SQL sql=new SQL();
		List<Subyacente> subyacentesPrestamo = new ArrayList<Subyacente>();
		if(esBusquedaHistorico) {
			subyacentesPrestamo = sql.query(con,  SQL_SELECT_SUBYACENTE_PRESTAMO_HISTORICO, parametros, Subyacente.class);
		}else {
			subyacentesPrestamo = sql.query(con, SQL_SELECT_SUBYACENTE_PRESTAMO_ACTUAL, parametros, Subyacente.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerSubyacentesPrestamo");
		
		return subyacentesPrestamo;
	}
	
	private List<Garantia> obtenerGarantia(Connection con, HashMap<String,Object> parametros, boolean esBusquedaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerGarantia");
		SQL sql=new SQL();
		List<Garantia> garantias = new ArrayList<Garantia>();
		if(esBusquedaHistorico) {
			garantias = sql.query(con, SQL_SELECT_GARANTIA_HISTORICO, parametros, Garantia.class);
		}else {
			garantias = sql.query(con, SQL_SELECT_GARANTIA_ACTUAL, parametros, Garantia.class);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerGarantia");
		
		return garantias;
	}
	
	private List<Cartera> filtrarCarterasPlazo(List<Cartera> carteras){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.filtrarCarterasPlazo");
		List<Cartera> carterasFiltradas = carteras.parallelStream().filter(cartera->!"COMSIM".equals(cartera.IdTipoOperacion)).collect(Collectors.toList());
		logger.trace("FIN::ConsultarDetallePrecioPromedio.filtrarCarterasPlazo");
		return carterasFiltradas;
	}
	
	private List<Long> obtenerIdInstrumentos(List<PortafoliosConCustodia> portafoliosConCustodia){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerIdInstrumentos");
		List<Long> idInstrumentos = portafoliosConCustodia.parallelStream()
				.filter(portafolio->"RV".equals(portafolio.IdClasificacionTipoInstrumento) || "FM".equals(portafolio.IdClasificacionTipoInstrumento))
				.map(portafolio->portafolio.IdInstrumento)
				.distinct()
				.collect(Collectors.toList());
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerIdInstrumentos");
		return idInstrumentos;
	}
	
	private List<Instrumento> obtenerInstrumentos(Connection con, List<Long> idInstrumentos) throws SQLException{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerInstrumentos");
		SQL sql=new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdInstrumento", idInstrumentos);
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, parametros, Instrumento.class);
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerInstrumentos");
		return instrumentos;
	}
	
	private HashMap<Long,BigDecimal> obternerPrecioMercado(Connection con, String fecha, List<Instrumento> instrumentos,Long idEmpresa,String idModulo,Long idUsuario, Long idPais) throws Exception{
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obternerPrecioMercado");
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = idEmpresa;
		paramsPrecioTasa.p_IdModulo = idModulo;
		paramsPrecioTasa.p_IdPais = idPais;
		paramsPrecioTasa.p_IdUsuario = idUsuario;
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",new Long(instrumentos.size()));
		AtomicInteger i = new AtomicInteger(0);
		paramsPrecioTasa.p_DatosInstrumentos = instrumentos.parallelStream()
				.map(instrumento->{
					EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
					paramsRegIn.FechaConsulta=Fecha.toDate(fecha);
					paramsRegIn.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
					paramsRegIn.IdInstrumento = instrumento.IdInstrumento;
					paramsRegIn.IdMoneda = instrumento.IdMoneda;
					paramsRegIn.IdRegistro = secuencias.get(i.getAndIncrement());
					paramsRegIn.IdTipoCriterioTasa = "0";
					paramsRegIn.IdTipoInstrumento = instrumento.IdTipoInstrumento;
					paramsRegIn.IdUso = "CARTERA";
					paramsRegIn.Nemotecnico = instrumento.Nemotecnico;
					paramsRegIn.PlazoResidual = 0L;
					paramsRegIn.TipoCartera = "0";
					return paramsRegIn;
				})
				.collect(Collectors.toList());
		HashMap<Long, BigDecimal> hashSalidaPrecioTasa = new HashMap<Long,BigDecimal>();
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		for(EntregarPreciosTasas_Salida_PreciosTasas dato : salidaPrecioTasa.P_OUT_PRECIOSTASAS){
			hashSalidaPrecioTasa.put(dato.IdInstrumento, dato.Precio);
		}
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obternerPrecioMercado");
		return hashSalidaPrecioTasa;
	}
	
	private void calcularMontoCartera(List<Cartera> carteras) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoCartera");
		carteras.parallelStream().filter(cartera->"RV".equals(cartera.IdClasificacionTipoInstrumento) || "FM".equals(cartera.IdClasificacionTipoInstrumento)).forEach(cartera->cartera.MontoCarteraPrecioCompra = cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128));
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoCartera");
	}
	
	private void calcularMontoSubyacenteVentaSimultanea(List<Subyacente> subyacentes, String usaPrecioAmortizacion, String fecha) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
		subyacentes.parallelStream().filter(subyacente->"RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)).forEach(subyacente->{
			if("S".equals(usaPrecioAmortizacion)) {
				subyacente.PrecioAPlazo  = obtenerPrecioAmortizacion(subyacente,fecha);
			}
			subyacente.MontoCarteraPrecioCompra = subyacente.Cantidad.multiply(subyacente.PrecioCompra, MathContext.DECIMAL128);
			subyacente.MontoCarteraPrecioPlazo = subyacente.Cantidad.multiply(subyacente.PrecioAPlazo, MathContext.DECIMAL128);
		});
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
	}
	
	private void calcularMontoSubyacenteCompraSimultanea(List<Subyacente> subyacentes, HashMap<Long,BigDecimal> preciosMercado) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
		subyacentes.parallelStream().filter(subyacente->"RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)).forEach(subyacente->{
			BigDecimal precioMercado = preciosMercado.get(subyacente.IdInstrumento)  != null ? preciosMercado.get(subyacente.IdInstrumento) : BigDecimal.ZERO;
			subyacente.MontoCarteraPrecioCompra = subyacente.Cantidad.multiply(subyacente.PrecioCompra, MathContext.DECIMAL128);
			subyacente.MontoCarteraPrecioPlazo = subyacente.Cantidad.multiply(precioMercado, MathContext.DECIMAL128);
		});
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
	}
	
	private void calcularMontoSubyacenteEmprestito(List<Subyacente> subyacentes) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
		subyacentes.parallelStream().filter(subyacente->"RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)).forEach(subyacente->{
			subyacente.MontoCarteraPrecioCompra = subyacente.Cantidad.multiply(subyacente.PrecioCompra, MathContext.DECIMAL128);
			subyacente.MontoCarteraPrecioPlazo = subyacente.Cantidad.multiply(subyacente.PrecioAPlazo, MathContext.DECIMAL128);
		});
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
	}
	
	private void calcularMontoSubyacentePrestamo(List<Subyacente> subyacentes, String usaPrimaAmortizacion, String fecha, HashMap<Long,BigDecimal> preciosMercado) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
		subyacentes.parallelStream().filter(subyacente->"RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)).forEach(subyacente->{
			if("S".equals(usaPrimaAmortizacion)) {
				subyacente.PrecioAPlazo = obtenerPrimaAmortizacion(subyacente,fecha);
			}else {
				subyacente.PrecioAPlazo = obtenerPrimaTotal(subyacente,fecha);
			}
			BigDecimal precioMercado = preciosMercado.get(subyacente.IdInstrumento)  != null ? preciosMercado.get(subyacente.IdInstrumento) : BigDecimal.ZERO;
			subyacente.PrecioAPlazo =  subyacente.PrecioAPlazo.add(precioMercado, MathContext.DECIMAL128);
			subyacente.MontoCarteraPrecioCompra = subyacente.Cantidad.multiply(subyacente.PrecioCompra, MathContext.DECIMAL128);
			subyacente.MontoCarteraPrecioPlazo = subyacente.Cantidad.multiply(subyacente.PrecioAPlazo, MathContext.DECIMAL128);
		});
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoSubyacente");
	}
	
	private void calcularMontoGarantia(List<Garantia> garantias) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.calcularMontoGarantia");
		garantias.parallelStream().filter(garantia->"RV".equals(garantia.IdClasificacionTipoInstrumento) || "FM".equals(garantia.IdClasificacionTipoInstrumento)).forEach(garantia->garantia.MontoCarteraPrecioCompra = garantia.Cantidad.multiply(garantia.PrecioCompra, MathContext.DECIMAL128));
		logger.trace("FIN::ConsultarDetallePrecioPromedio.calcularMontoGarantia");
	}
	
	private BigDecimal obtenerPrecioAmortizacion(Subyacente subyacente, String fecha) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPrecioAmortizacion");
		BigDecimal plazoRecorrido = obtenerPlazo(subyacente.FechaVencimientoOperacion, Fecha.toDate(fecha));
		BigDecimal plazo = plazoRecorrido.divide(new BigDecimal(30), MathContext.DECIMAL128);
		BigDecimal factorTasa = subyacente.TasaCompra.multiply(plazo, MathContext.DECIMAL128);
		BigDecimal factor = BigDecimal.ONE.add(factorTasa, MathContext.DECIMAL128);
		BigDecimal precioAmortizacion = subyacente.PrecioCompra.multiply(factor, MathContext.DECIMAL128);
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPrecioAmortizacion");
		return precioAmortizacion;
	}
	
	private BigDecimal obtenerPrimaAmortizacion(Subyacente subyacente, String fecha) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPrimaAmortizacion");
		BigDecimal plazoRecorrido = obtenerPlazo(subyacente.FechaVencimientoOperacion, Fecha.toDate(fecha));
		BigDecimal plazo = plazoRecorrido.divide(new BigDecimal(30), MathContext.DECIMAL128);
		BigDecimal factorTasa = subyacente.TasaCompra.multiply(plazo, MathContext.DECIMAL128);
		BigDecimal precioCantidad = subyacente.PrecioAPlazo.multiply(subyacente.Cantidad, MathContext.DECIMAL128);
		BigDecimal precioAmortizacion = precioCantidad.multiply(factorTasa, MathContext.DECIMAL128);
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPrimaAmortizacion");
		return precioAmortizacion;
	}
	
	private BigDecimal obtenerPrimaTotal(Subyacente subyacente, String fecha) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPrimaAmortizacion");
		BigDecimal plazoRecorrido = obtenerPlazo(subyacente.FechaVencimientoOperacion, subyacente.FechaLiquidacion);
		BigDecimal plazo = plazoRecorrido.divide(new BigDecimal(30), MathContext.DECIMAL128);
		BigDecimal factorTasa = subyacente.TasaCompra.multiply(plazo, MathContext.DECIMAL128);
		BigDecimal precioCantidad = subyacente.PrecioAPlazo.multiply(subyacente.Cantidad, MathContext.DECIMAL128);
		BigDecimal precioAmortizacion = precioCantidad.multiply(factorTasa, MathContext.DECIMAL128);
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPrimaAmortizacion");
		return precioAmortizacion;
	}
	
	private BigDecimal obtenerPlazo(java.sql.Date fechaVencimiento, java.sql.Date fecha) {
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPlazoRecorrido");
		Long plazoRecorridoLong = (Long) ((fechaVencimiento.getTime()-fecha.getTime())/86400000);
		BigDecimal plazoRecorrido = new BigDecimal(plazoRecorridoLong);
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPlazoRecorrido");
		return plazoRecorrido;
	}
	
	private List<PortafoliosConCustodia> obtenerPortafoliosConCustodia(List<Cartera> carteras, List<Cartera> carterasEnTransito, List<Subyacente> subyacentesSimultaneasVenta, List<Subyacente> subyacentesSimultaneasCompra, List<Subyacente> subyacentesEmprestito, List<Subyacente> subyacentesPrestamos, List<Garantia> garantias){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPortafoliosConCustodia");
		List<PortafoliosConCustodia> portafoliosConCartera = carteras.parallelStream().map(cartera->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = cartera.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = cartera.IdInstrumento;
			portafoliosConCustodia.IdBoveda = cartera.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConCarterasEnTransito = carterasEnTransito.parallelStream().map(cartera->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = cartera.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = cartera.IdInstrumento;
			portafoliosConCustodia.IdBoveda = cartera.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConSubyacenteSimultaneaVenta = subyacentesSimultaneasVenta.parallelStream().map(subyacente->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = subyacente.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = subyacente.IdInstrumento;
			portafoliosConCustodia.IdBoveda = subyacente.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = subyacente.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConSubyacenteSimultaneaCompra = subyacentesSimultaneasCompra.parallelStream().map(subyacente->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = subyacente.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = subyacente.IdInstrumento;
			portafoliosConCustodia.IdBoveda = subyacente.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = subyacente.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConSubyacenteEmprestito = subyacentesEmprestito.parallelStream().map(subyacente->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = subyacente.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = subyacente.IdInstrumento;
			portafoliosConCustodia.IdBoveda = subyacente.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = subyacente.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConSubyacentePrestamos = subyacentesPrestamos.parallelStream().map(subyacente->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = subyacente.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = subyacente.IdInstrumento;
			portafoliosConCustodia.IdBoveda = subyacente.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = subyacente.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		List<PortafoliosConCustodia> portafoliosConGarantias = garantias.parallelStream().map(garantia->{
			PortafoliosConCustodia portafoliosConCustodia = new PortafoliosConCustodia();
			portafoliosConCustodia.IdPortafolio = garantia.IdPortafolio;
			portafoliosConCustodia.IdInstrumento = garantia.IdInstrumento;
			portafoliosConCustodia.IdBoveda = garantia.IdBoveda;
			portafoliosConCustodia.IdClasificacionTipoInstrumento = garantia.IdClasificacionTipoInstrumento;
			return portafoliosConCustodia;
		}).distinct().collect(Collectors.toList());
		
		List<PortafoliosConCustodia> portafoliosConCustodia = new ArrayList<PortafoliosConCustodia>();
		portafoliosConCustodia.addAll(portafoliosConCartera);
		portafoliosConCustodia.addAll(portafoliosConCarterasEnTransito);
		portafoliosConCustodia.addAll(portafoliosConSubyacenteSimultaneaVenta);
		portafoliosConCustodia.addAll(portafoliosConSubyacenteSimultaneaCompra);
		portafoliosConCustodia.addAll(portafoliosConSubyacenteEmprestito);
		portafoliosConCustodia.addAll(portafoliosConSubyacentePrestamos);
		portafoliosConCustodia.addAll(portafoliosConGarantias);
		portafoliosConCustodia = portafoliosConCustodia.parallelStream().distinct().collect(Collectors.toList());
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPortafoliosConCustodia");
		return portafoliosConCustodia;
	}
	
	public List<Totalizador> obtenerTotalizador(List<PortafoliosConCustodia> portafoliosConCustodia, List<Cartera> carteras, List<Cartera> carterasEnTransito, List<Subyacente> subyacentesSimultaneasVenta, List<Subyacente> subyacentesSimultaneasCompra, List<Subyacente> subyacentesEmprestito, List<Subyacente> subyacentesPrestamos, List<Garantia> garantias){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerTotalizador");
		
		Map<PortafoliosConCustodia,List<Cartera>> hashCarteras = carteras.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Cartera>> hashCarterasEnTransito = carterasEnTransito.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Subyacente>> hashSubyacentesSimultaneasVenta = subyacentesSimultaneasVenta.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Subyacente>> hashSubyacentesSimultaneasCompra = subyacentesSimultaneasCompra.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Subyacente>> hashSubyacentesEmprestito = subyacentesEmprestito.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Subyacente>> hashSubyacentesPrestamos = subyacentesPrestamos.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		Map<PortafoliosConCustodia,List<Garantia>> hashSubyacentesGarantias = garantias.parallelStream().collect(Collectors.groupingBy(cartera->{
			PortafoliosConCustodia portafolioCustodia = new PortafoliosConCustodia();
			portafolioCustodia.IdPortafolio = cartera.IdPortafolio;
			portafolioCustodia.IdInstrumento = cartera.IdInstrumento;
			portafolioCustodia.IdBoveda = cartera.IdBoveda;
			portafolioCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			return portafolioCustodia;
		}));
		
		List<Totalizador> totalizadores = portafoliosConCustodia.parallelStream().map(portafolio->{
			List<Cartera> carterasPortafolio = hashCarteras.get(portafolio) != null ? hashCarteras.get(portafolio) : new ArrayList<Cartera>();
			List<Cartera> carterasEnTransitoPortafolio = hashCarterasEnTransito.get(portafolio) != null ? hashCarterasEnTransito.get(portafolio) : new ArrayList<Cartera>();
			List<Subyacente> subyacentesSimultaneaVentaPortafolio = hashSubyacentesSimultaneasVenta.get(portafolio) != null ? hashSubyacentesSimultaneasVenta.get(portafolio) : new ArrayList<Subyacente>();
			List<Subyacente> subyacentesSimultaneaCompraPortafolio = hashSubyacentesSimultaneasCompra.get(portafolio) != null ? hashSubyacentesSimultaneasCompra.get(portafolio) : new ArrayList<Subyacente>();
			List<Subyacente> subyacentesEmprestitosPortafolio = hashSubyacentesEmprestito.get(portafolio) != null ? hashSubyacentesEmprestito.get(portafolio) : new ArrayList<Subyacente>();
			List<Subyacente> subyacentesPrestamosPortafolio = hashSubyacentesPrestamos.get(portafolio) != null ? hashSubyacentesPrestamos.get(portafolio) : new ArrayList<Subyacente>();
			List<Garantia> garantiasPortafolio = hashSubyacentesGarantias.get(portafolio) != null ? hashSubyacentesGarantias.get(portafolio) : new ArrayList<Garantia>();
			Totalizador totalizador = new Totalizador();
			totalizador.IdPortafolio = portafolio.IdPortafolio;
			totalizador.IdInstrumento = portafolio.IdInstrumento;
			totalizador.IdBoveda = portafolio.IdBoveda;
			
			carterasPortafolio.stream().forEach(cartera->{
				if("RV".equals(cartera.IdClasificacionTipoInstrumento) || "FM".equals(cartera.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadCarteras = totalizador.TotalCantidadCarteras.add(cartera.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(cartera.IdClasificacionTipoInstrumento) || "IF".equals(cartera.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadCarteras = totalizador.TotalCantidadCarteras.add(cartera.Cantidad.multiply(cartera.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				
				totalizador.TotalMontoCarteras = totalizador.TotalMontoCarteras.add(cartera.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
			});
			
			carterasEnTransitoPortafolio.stream().forEach(cartera->{
				if("RV".equals(cartera.IdClasificacionTipoInstrumento) || "FM".equals(cartera.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadCarterasEnTransito = totalizador.TotalCantidadCarterasEnTransito.add(cartera.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(cartera.IdClasificacionTipoInstrumento) || "IF".equals(cartera.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadCarterasEnTransito = totalizador.TotalCantidadCarterasEnTransito.add(cartera.Cantidad.multiply(cartera.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoCarterasEnTransito= totalizador.TotalMontoCarterasEnTransito.add(cartera.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
			});
			
			subyacentesSimultaneaVentaPortafolio.stream().forEach(subyacente->{
				if("RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteSimultaneaVenta = totalizador.TotalCantidadSubyacenteSimultaneaVenta.add(subyacente.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(subyacente.IdClasificacionTipoInstrumento) || "IF".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteSimultaneaVenta = totalizador.TotalCantidadSubyacenteSimultaneaVenta.add(subyacente.Cantidad.multiply(subyacente.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoSubyacentesSimultaneaVenta = totalizador.TotalMontoSubyacentesSimultaneaVenta.add(subyacente.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
				totalizador.TotalMontoPlazoSubyacentesSimultaneaVenta = totalizador.TotalMontoPlazoSubyacentesSimultaneaVenta.add(subyacente.MontoCarteraPrecioPlazo, MathContext.DECIMAL128);
			});
			
			subyacentesSimultaneaCompraPortafolio.stream().forEach(subyacente->{
				if("RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteSimultaneaCompra = totalizador.TotalCantidadSubyacenteSimultaneaCompra.add(subyacente.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(subyacente.IdClasificacionTipoInstrumento) || "IF".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteSimultaneaCompra = totalizador.TotalCantidadSubyacenteSimultaneaCompra.add(subyacente.Cantidad.multiply(subyacente.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoSubyacentesSimultaneaCompra = totalizador.TotalMontoSubyacentesSimultaneaCompra.add(subyacente.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
				totalizador.TotalMontoPlazoSubyacentesSimultaneaCompra = totalizador.TotalMontoPlazoSubyacentesSimultaneaCompra.add(subyacente.MontoCarteraPrecioPlazo, MathContext.DECIMAL128);
			});
			
			subyacentesEmprestitosPortafolio.stream().forEach(subyacente->{
				if("RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteEmprestito = totalizador.TotalCantidadSubyacenteEmprestito.add(subyacente.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(subyacente.IdClasificacionTipoInstrumento) || "IF".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacenteEmprestito = totalizador.TotalCantidadSubyacenteEmprestito.add(subyacente.Cantidad.multiply(subyacente.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoSubyacentesEmprestito = totalizador.TotalMontoSubyacentesEmprestito.add(subyacente.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
				totalizador.TotalMontoPlazoSubyacentesEmprestito = totalizador.TotalMontoPlazoSubyacentesEmprestito.add(subyacente.MontoCarteraPrecioPlazo, MathContext.DECIMAL128);
			});
			
			subyacentesPrestamosPortafolio.stream().forEach(subyacente->{
				if("RV".equals(subyacente.IdClasificacionTipoInstrumento) || "FM".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacentePrestamo = totalizador.TotalCantidadSubyacentePrestamo.add(subyacente.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(subyacente.IdClasificacionTipoInstrumento) || "IF".equals(subyacente.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadSubyacentePrestamo = totalizador.TotalCantidadSubyacentePrestamo.add(subyacente.Cantidad.multiply(subyacente.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoSubyacentesPrestamo = totalizador.TotalMontoSubyacentesPrestamo.add(subyacente.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
				totalizador.TotalMontoPlazoSubyacentesPrestamo = totalizador.TotalMontoPlazoSubyacentesPrestamo.add(subyacente.MontoCarteraPrecioPlazo, MathContext.DECIMAL128);
			});
			
			garantiasPortafolio.stream().forEach(garantia->{
				if("RV".equals(garantia.IdClasificacionTipoInstrumento) || "FM".equals(garantia.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadGarantias = totalizador.TotalCantidadGarantias.add(garantia.Cantidad, MathContext.DECIMAL128);
				}else if("RF".equals(garantia.IdClasificacionTipoInstrumento) || "IF".equals(garantia.IdClasificacionTipoInstrumento)) {
					totalizador.TotalCantidadGarantias = totalizador.TotalCantidadGarantias.add(garantia.Cantidad.multiply(garantia.ValorFactorMonedaInstrumento, MathContext.DECIMAL128), MathContext.DECIMAL128);
				}
				totalizador.TotalMontoGarantias = totalizador.TotalMontoGarantias.add(garantia.MontoCarteraPrecioCompra, MathContext.DECIMAL128);
			});

			return totalizador;
		}).collect(Collectors.toList());
		
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerTotalizador");
		
		return totalizadores;
	}
	
	private List<PrecioPromedio> obtenerPrecioPromedio(List<Totalizador> totalizadores){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPrecioPromedio");
		
		List<PrecioPromedio> preciosPromedios = totalizadores.parallelStream().map(totalizador->{
			PrecioPromedio precioPromedio = new PrecioPromedio();
			precioPromedio.IdPortafolio = totalizador.IdPortafolio;
			precioPromedio.IdInstrumento = totalizador.IdInstrumento;
			precioPromedio.IdBoveda = totalizador.IdBoveda;
			
			precioPromedio.TotalCantidad = totalizador.TotalCantidadCarteras.add(totalizador.TotalCantidadSubyacenteSimultaneaVenta, MathContext.DECIMAL128).add(totalizador.TotalCantidadSubyacenteEmprestito, MathContext.DECIMAL128).add(totalizador.TotalCantidadGarantias, MathContext.DECIMAL128);
			precioPromedio.TotalMonto = totalizador.TotalMontoCarteras.add(totalizador.TotalMontoSubyacentesSimultaneaVenta, MathContext.DECIMAL128).add(totalizador.TotalMontoSubyacentesEmprestito, MathContext.DECIMAL128).add(totalizador.TotalMontoGarantias, MathContext.DECIMAL128);
			precioPromedio.PrecioPromedio = precioPromedio.TotalCantidad.compareTo(BigDecimal.ZERO) != 0 ? precioPromedio.TotalMonto.divide(precioPromedio.TotalCantidad, MathContext.DECIMAL128) : BigDecimal.ZERO;
			
			return precioPromedio;
		}).collect(Collectors.toList());
		
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPrecioPromedio");
		return preciosPromedios;
	}
	
	private List<PrecioPromedio> obtenerPrecioPromedioPasivo(List<Totalizador> totalizadores){
		logger.trace("INICIO::ConsultarDetallePrecioPromedio.obtenerPrecioPromedioPasivo");
		List<PrecioPromedio> preciosPromediosPasivo = totalizadores.parallelStream().map(totalizador->{
			PrecioPromedio precioPromedio = new PrecioPromedio();
			precioPromedio.IdPortafolio = totalizador.IdPortafolio;
			precioPromedio.IdInstrumento = totalizador.IdInstrumento;
			precioPromedio.IdBoveda = totalizador.IdBoveda;
			
			precioPromedio.TotalCantidad = totalizador.TotalCantidadCarterasEnTransito.add(totalizador.TotalCantidadSubyacenteSimultaneaVenta, MathContext.DECIMAL128).add(totalizador.TotalCantidadSubyacenteSimultaneaCompra, MathContext.DECIMAL128).add(totalizador.TotalCantidadSubyacentePrestamo, MathContext.DECIMAL128);
			precioPromedio.TotalMonto = totalizador.TotalMontoCarterasEnTransito.add(totalizador.TotalMontoPlazoSubyacentesSimultaneaVenta, MathContext.DECIMAL128).add(totalizador.TotalMontoSubyacentesSimultaneaCompra, MathContext.DECIMAL128).add(totalizador.TotalMontoPlazoSubyacentesPrestamo, MathContext.DECIMAL128);
			precioPromedio.PrecioPromedio = precioPromedio.TotalCantidad.compareTo(BigDecimal.ZERO) != 0 ? precioPromedio.TotalMonto.divide(precioPromedio.TotalCantidad, MathContext.DECIMAL128) : BigDecimal.ZERO;
			
			return precioPromedio;
		}).collect(Collectors.toList());
		
		logger.trace("FIN::ConsultarDetallePrecioPromedio.obtenerPrecioPromedioPasivo");
		return preciosPromediosPasivo;
	}
	
	private void borrarPortafolioPaso(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ConsultarDetallePrecioPromedio.borrarPortafolioPaso");
		
		Portafolio portafolio = new Portafolio();
		portafolio.IdTrabajo = idTrabajo;
		List<Portafolio> portafolios = Arrays.asList(portafolio);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, portafolios, Portafolio.class);
		
		logger.debug("FIN::ConsultarDetallePrecioPromedio.borrarPortafolioPaso");
	}
	
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getDESAConnection()){
			
			EntradaConsultarDetallePrecioPromedio entrada = new EntradaConsultarDetallePrecioPromedio();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_IdModulo = "CUS";
			entrada.p_SumaVentaPlazoEnDisponible =  "N";
			entrada.p_FechaBusqueda = "2019-08-23";
			InPortafolio inPortafolio = new InPortafolio();
			inPortafolio.IdPortafolio = 1451729L;
			InInstrumento inInstrumento = new InInstrumento();
			inInstrumento.IdClasificacionTipoInstrumento = "RF";
			entrada.p_Portafolios.add(inPortafolio);
			entrada.p_Instrumentos.add(inInstrumento);
			ConsultarDetallePrecioPromedio demo = new ConsultarDetallePrecioPromedio();
			SalidaConsultarDetallePrecioPromedio salida = demo.logica(con, entrada);
			
			System.out.println(salida);
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}
}
