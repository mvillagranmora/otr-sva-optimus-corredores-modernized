package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarCuadraturaCustodiaCircular1548 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCuadraturaCustodiaCircular1548.class);
	
	private final String SQL_DELETE_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1548_DeleteCuadraturaCircular1518.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1548_InsertCuadraturaCircular1518.sql");
	private final String SQL_SELECT_COMPROBANTES_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1548_SelectComprobanteTransito.sql");
	
	public static class EntradaGenerarCuadraturaCustodiaCircular1548 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
	}
	
	public static class SalidaGenerarCuadraturaCustodiaCircular1548 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaGenerarCuadraturaCustodiaCircular1548() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Custodia extends BcsPojo {
		public Long IdCarga;
		public String FechaCuadratura;
		public String Nemotecnico;
		public BigDecimal CantidadCustodia;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadVentaPlazo;
		public BigDecimal CantidadCompraPlazo;
		public BigDecimal TotalCustodia;
		public BigDecimal CompraEnTransito;
		public BigDecimal VentaEnTransito;
		public BigDecimal TransitoIngreso;
		public BigDecimal TransitoEgreso;
		public BigDecimal Diferencia;
		public BigDecimal Precio;
		public BigDecimal Valorizacion;
		public BigDecimal DiferenciaValorizada;
		public BigDecimal TotalCustodioEmisores;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		
		public Custodia() {
			this.CantidadCustodia = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadVentaPlazo = BigDecimal.ZERO;
			this.CantidadCompraPlazo = BigDecimal.ZERO;
			this.TotalCustodia = BigDecimal.ZERO;
			this.CompraEnTransito = BigDecimal.ZERO;
			this.VentaEnTransito = BigDecimal.ZERO;
			this.TransitoIngreso = BigDecimal.ZERO;
			this.TransitoEgreso = BigDecimal.ZERO;
			this.Diferencia = BigDecimal.ZERO;
			this.Precio = BigDecimal.ZERO;
			this.Valorizacion = BigDecimal.ZERO;
			this.DiferenciaValorizada = BigDecimal.ZERO;
		}
	}
	
	public static class Comprobante extends BcsPojo {
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public BigDecimal CantidadCompra;
		public BigDecimal CantidadVenta;
		public BigDecimal Precio;
		public String Procesada;
		
		public Comprobante() {
			this.Procesada = "N";
			this.CantidadCompra = BigDecimal.ZERO;
			this.CantidadVenta = BigDecimal.ZERO;
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarCuadraturaCustodiaCircular1548 in = gson.fromJson(jin.toString(), EntradaGenerarCuadraturaCustodiaCircular1548.class);
		SalidaGenerarCuadraturaCustodiaCircular1548 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGenerarCuadraturaCustodiaCircular1548 logica(Connection con, EntradaGenerarCuadraturaCustodiaCircular1548 entrada) throws Exception {
		logger.info("INICIO::GenerarCuadraturaCustodiaCircular1548");
		long inicioServicio = System.currentTimeMillis();
		SalidaGenerarCuadraturaCustodiaCircular1548 out = new SalidaGenerarCuadraturaCustodiaCircular1548();
		
		borrarCargaCuadratura(con, entrada.p_FechaCuadratura);
		List<Tabla_Salida> custodiasPorCliente = obtenerCustodia(con, entrada);
		HashMap<String, Comprobante> comprobantesTotalizadosPorNemo = obtenerComprobantesTotalizadosPorNemo(con, entrada.p_FechaCuadratura);
		List<Custodia> custodias = obtenerCustodiaAgrupadaPorNemotecnico(custodiasPorCliente, entrada.p_FechaCuadratura, comprobantesTotalizadosPorNemo);
		insertarCargaCuadratura(con, custodias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarCuadraturaCustodiaCircular1548", finServicio - inicioServicio);
		return out;
	}
	
	private void borrarCargaCuadratura(Connection con, String fechaCuadratura)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1548.borrarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		Custodia custodia = new Custodia();
		custodia.FechaCuadratura = fechaCuadratura;
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULAR1518, Arrays.asList(custodia), Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1548.borrarCargaCuadratura", fin - inicio);
	}
	
	private List<Tabla_Salida> obtenerCustodia(Connection con, EntradaGenerarCuadraturaCustodiaCircular1548 entrada) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1548.obtenerCustodia");
		long inicio = System.currentTimeMillis();
		
		Entrada_ConsultaCustodiaPortafolio entradaConsultarCustodia = new Entrada_ConsultaCustodiaPortafolio();
		entradaConsultarCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodia.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodia.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodia.p_FechaConsulta = entrada.p_FechaCuadratura;
		entradaConsultarCustodia.p_TipoFechaConsulta = "ADQ";
		
		ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
		Salida_ConsultaCustodiaPortafolio salidaConsultaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaConsultarCustodia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1548.obtenerCustodia", fin - inicio);
		return salidaConsultaCustodiaPortafolio.P_OUT_TBL;
	}
	
	private HashMap<String, Comprobante> obtenerComprobantesTotalizadosPorNemo(Connection con, String fechaCuadratura)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1548.obtenerMovimientosTransito");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("FechaCuadratura", fechaCuadratura);
		
		SQL _sql = new SQL();
		List<Comprobante> comprobantes = _sql.query(con, SQL_SELECT_COMPROBANTES_TRANSITO, parametro, Comprobante.class);
		
		Map<String, List<Comprobante>> comprobantesPorNemo = comprobantes.stream().collect(Collectors.groupingBy(comprobante->comprobante.Nemotecnico));
		
		HashMap<String, Comprobante> comprobantesTotalizadosPorNemo = new HashMap<String, Comprobante>();
		
		for(Entry<String, List<Comprobante>> comprobantesNemo : comprobantesPorNemo.entrySet()) {
			Comprobante comprobante = new Comprobante();
			comprobante.Nemotecnico = comprobantesNemo.getKey();
			for(Comprobante comprobanteNemo : comprobantesNemo.getValue()) {
				comprobante.CantidadCompra = comprobante.CantidadCompra.add(comprobanteNemo.CantidadCompra, MathContext.DECIMAL128);
				comprobante.CantidadVenta = comprobante.CantidadVenta.add(comprobanteNemo.CantidadVenta, MathContext.DECIMAL128);
				comprobante.Precio = comprobanteNemo.Precio;
				comprobante.IdClasificacionTipoInstrumento = comprobanteNemo.IdClasificacionTipoInstrumento;
				comprobante.IdTipoInstrumento = comprobanteNemo.IdTipoInstrumento;
			}
			comprobantesTotalizadosPorNemo.put(comprobante.Nemotecnico, comprobante);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1548.obtenerMovimientosTransito", fin - inicio);
		return comprobantesTotalizadosPorNemo;
	}
	
	private List<Custodia> obtenerCustodiaAgrupadaPorNemotecnico(List<Tabla_Salida> custodiasPorCliente, String fechaCuadratura, HashMap<String, Comprobante> comprobantesTotalizadosPorNemo) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1548.obtenerCustodiaAgrupadaPorNemotecnico");
		long inicio = System.currentTimeMillis();
		
		Map<String, List<Tabla_Salida>> custodiasPorNemo = custodiasPorCliente.stream().collect(Collectors.groupingBy(custodia->custodia.Nemotecnico));
		
		List<Custodia> custodias = new ArrayList<>();
		
		for(Entry<String, List<Tabla_Salida>> saldoCustodia : custodiasPorNemo.entrySet()) {
			Custodia custodia = new Custodia();
			custodia.FechaCuadratura = fechaCuadratura;
			custodia.Nemotecnico = saldoCustodia.getKey();
			for(Tabla_Salida custodiaNemo : saldoCustodia.getValue()) {
				custodia.IdClasificacionTipoInstrumento = custodiaNemo.IdClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = custodiaNemo.IdTipoInstrumento;
				custodia.CantidadCustodia = custodia.CantidadCustodia.add(custodiaNemo.CantidadCustodia, MathContext.DECIMAL128);
				custodia.CantidadGarantia = custodia.CantidadGarantia.add(custodiaNemo.CantidadGarantia, MathContext.DECIMAL128);
				custodia.CantidadVentaPlazo = custodia.CantidadVentaPlazo.add(custodiaNemo.CantidadSimulVen, MathContext.DECIMAL128);
				custodia.CantidadVentaPlazo = custodia.CantidadVentaPlazo.add(custodiaNemo.CantidadPactosC, MathContext.DECIMAL128);
				custodia.CantidadCompraPlazo = custodia.CantidadCompraPlazo.add(custodiaNemo.CantidadSimulCom, MathContext.DECIMAL128);
				custodia.CantidadCompraPlazo = custodia.CantidadCompraPlazo.add(custodiaNemo.CantidadPactosV, MathContext.DECIMAL128);
				custodia.TransitoIngreso = custodia.TransitoIngreso.add(custodiaNemo.CantidadTransIng, MathContext.DECIMAL128);
				custodia.TransitoEgreso = custodia.TransitoEgreso.add(custodiaNemo.CantidadTransEgr, MathContext.DECIMAL128);
				custodia.Precio = "RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento) ? custodiaNemo.PrecioMercadoCustodia : BigDecimal.ONE;
			}
			Comprobante comprobante = comprobantesTotalizadosPorNemo.get(custodia.Nemotecnico);
			if(comprobante != null) {
				custodia.CompraEnTransito = comprobante.CantidadCompra;
				custodia.VentaEnTransito = comprobante.CantidadVenta;
				custodia.TotalCustodioEmisores = custodia.CompraEnTransito.subtract(custodia.VentaEnTransito, MathContext.DECIMAL128);
				comprobante.Procesada = "S";
				comprobantesTotalizadosPorNemo.put(custodia.Nemotecnico, comprobante);
			}
			custodia.TotalCustodia = calcularTotalCustodia(custodia);
			custodia.Valorizacion = "RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento) ? custodia.TotalCustodia.multiply(custodia.Precio, MathContext.DECIMAL128) : custodia.TotalCustodia;
			custodia.Diferencia = custodia.TotalCustodioEmisores.subtract(custodia.TotalCustodia, MathContext.DECIMAL128);
			custodia.DiferenciaValorizada = "RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento) ? custodia.Diferencia.multiply(custodia.Precio, MathContext.DECIMAL128).abs(MathContext.DECIMAL128) : custodia.Diferencia.abs(MathContext.DECIMAL128);
			custodias.add(custodia);
		}
		
		for(Entry<String, Comprobante> saldoComprobante : comprobantesTotalizadosPorNemo.entrySet()) {
			Comprobante comprobante = saldoComprobante.getValue();
			if("N".equals(comprobante.Procesada)) {
				Custodia custodia = new Custodia();
				custodia.FechaCuadratura = fechaCuadratura;
				custodia.Nemotecnico = comprobante.Nemotecnico;
				custodia.CompraEnTransito = comprobante.CantidadCompra;
				custodia.VentaEnTransito = comprobante.CantidadVenta;
				custodia.Precio = comprobante.Precio;
				custodia.IdClasificacionTipoInstrumento = comprobante.IdClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = comprobante.IdTipoInstrumento;
				custodia.TotalCustodioEmisores = custodia.CompraEnTransito.subtract(custodia.VentaEnTransito, MathContext.DECIMAL128);
				custodia.Diferencia = custodia.TotalCustodioEmisores;
				custodia.DiferenciaValorizada = "RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento) ? custodia.Diferencia.multiply(custodia.Precio, MathContext.DECIMAL128).abs(MathContext.DECIMAL128) : custodia.Diferencia.abs(MathContext.DECIMAL128);
				custodias.add(custodia);
				comprobante.Procesada = "S";
				comprobantesTotalizadosPorNemo.put(custodia.Nemotecnico, comprobante);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1548.obtenerCustodiaAgrupadaPorNemotecnico", fin - inicio);
		return custodias;
	}
	
	private BigDecimal calcularTotalCustodia(Custodia custodia) {
		logger.trace("INICIO::GenerarCuadraturaCustodiaCircular1548.calcularTotalCustodia");
		long inicio = System.currentTimeMillis();
		
		BigDecimal totalCustodia = custodia.CantidadCustodia.add(custodia.CantidadGarantia, MathContext.DECIMAL128).add(custodia.CantidadVentaPlazo, MathContext.DECIMAL128).subtract(custodia.CantidadCompraPlazo, MathContext.DECIMAL128);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCuadraturaCustodiaCircular1548.calcularTotalCustodia", fin - inicio);
		return totalCustodia;
	}
	
	private void insertarCargaCuadratura(Connection con, List<Custodia> custodias)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1548.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsCarga = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::CargaCuadraturaCircular1518", (Long) 1L);
		Long idCarga = idsCarga.get(0);
		
		custodias.parallelStream().forEach(custodia->custodia.IdCarga = idCarga);
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULAR1518, custodias, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1548.insertarCargaCuadratura", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarCuadraturaCustodiaCircular1548 entradaGenerarCuadraturaCustodiaCircular1548 = new EntradaGenerarCuadraturaCustodiaCircular1548();
			entradaGenerarCuadraturaCustodiaCircular1548.p_IdEmpresa = 1L;
			entradaGenerarCuadraturaCustodiaCircular1548.p_IdUsuario = 4L;
			entradaGenerarCuadraturaCustodiaCircular1548.p_IdModulo = "CUS";
			entradaGenerarCuadraturaCustodiaCircular1548.p_FechaCuadratura = "2017-08-14";
			
			GenerarCuadraturaCustodiaCircular1548 generarCuadraturaCustodiaCircular1548 = new GenerarCuadraturaCustodiaCircular1548();
			try {
				SalidaGenerarCuadraturaCustodiaCircular1548 salidaProcesarCargaMasivaInstruccion = generarCuadraturaCustodiaCircular1548.logica(con, entradaGenerarCuadraturaCustodiaCircular1548);
				System.out.println(salidaProcesarCargaMasivaInstruccion.toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}
	
	
}
