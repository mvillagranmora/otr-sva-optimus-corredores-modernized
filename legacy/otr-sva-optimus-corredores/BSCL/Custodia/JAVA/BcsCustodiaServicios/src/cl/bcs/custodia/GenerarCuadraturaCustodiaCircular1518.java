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

public class GenerarCuadraturaCustodiaCircular1518 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCuadraturaCustodiaCircular1518.class);
	
	private final String CPRO = "CPRO";
	private final String SQL_DELETE_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1518_DeleteCuadraturaCircular1518.sql");
	private final String SQL_DELETE_CUADRATURA_CIRCULAR1720 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1720_DeleteCuadraturaCircular1720.sql");
	private final String SQL_SELECT_DATOS_CUENTA_PROPIA = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1518_SelectDatosCuentaPropia.sql");
	private final String SQL_SELECT_CANTIDAD_CUS_NEG = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1518_SelectCantidadCustodiaNegativa.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1518_InsertCuadraturaCircular1518.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULAR1720 = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1720_InsertCuadraturaCircular1720.sql");
	private final String SQL_SELECT_COMPROBANTES_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/GenerarCuadraturaCustodiaCircular1518_SelectComprobanteTransito.sql");
	
	public static class EntradaGenerarCuadraturaCustodiaCircular1518 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
	}
	
	public static class SalidaGenerarCuadraturaCustodiaCircular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaGenerarCuadraturaCustodiaCircular1518() {
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
		public BigDecimal CantidadPlazoNom; 
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
		public Long IdInstrumento;							
		public String IdEmisor;
		public BigDecimal TotalCustodiaCuentaPropia;
		
		public Custodia() {
			this.CantidadCustodia = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadPlazoNom = BigDecimal.ZERO; 
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
			this.TotalCustodioEmisores = BigDecimal.ZERO;
			this.IdInstrumento = 0L;						
			this.IdEmisor = "";
			this.TotalCustodiaCuentaPropia = BigDecimal.ZERO;
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
	
	public static class DatoCuentaPropia extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
	}
	public static class CustodiaNegativa extends BcsPojo {
		public BigDecimal Cantidad;
		public String Nemotecnico;
	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarCuadraturaCustodiaCircular1518 in = gson.fromJson(jin.toString(), EntradaGenerarCuadraturaCustodiaCircular1518.class);
		SalidaGenerarCuadraturaCustodiaCircular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGenerarCuadraturaCustodiaCircular1518 logica(Connection con, EntradaGenerarCuadraturaCustodiaCircular1518 entrada) throws Exception {
		logger.info("INICIO::GenerarCuadraturaCustodiaCircular1518");
		long inicioServicio = System.currentTimeMillis();
		SalidaGenerarCuadraturaCustodiaCircular1518 out = new SalidaGenerarCuadraturaCustodiaCircular1518();
		
		borrarCargaCuadratura(con, entrada.p_FechaCuadratura);
		List<Tabla_Salida> custodiasPorCliente = obtenerCustodia(con, entrada);
		HashMap<String, Comprobante> comprobantesTotalizadosPorNemo = obtenerComprobantesTotalizadosPorNemo(con, entrada.p_FechaCuadratura);
		List<Custodia> custodias = obtenerCustodiaAgrupadaPorNemotecnico(custodiasPorCliente, entrada.p_FechaCuadratura, comprobantesTotalizadosPorNemo, con);
	
		insertarCargaCuadratura1720(con, custodias);
		insertarCargaCuadratura1518(con, custodias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarCuadraturaCustodiaCircular1518", finServicio - inicioServicio);
		return out;
	}
	
	private void borrarCargaCuadratura(Connection con, String fechaCuadratura)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.borrarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		Custodia custodia = new Custodia();
		custodia.FechaCuadratura = fechaCuadratura;
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULAR1518, Arrays.asList(custodia), Custodia.class);
		_sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULAR1720, Arrays.asList(custodia), Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.borrarCargaCuadratura", fin - inicio);
	}
	
	private List<Tabla_Salida> obtenerCustodia(Connection con, EntradaGenerarCuadraturaCustodiaCircular1518 entrada) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerCustodia");
		long inicio = System.currentTimeMillis();
		
		List<DatoCuentaPropia> datosCuentaPropia = obtenerDatosCuentaPropia(con, entrada.p_IdEmpresa);
		List<Tabla_Salida> custodiasCuentaTerceros = obtenerCustodiaCuentaTerceros(con, entrada, datosCuentaPropia);
		List<Tabla_Salida> custodiasCuentaPropia = obtenerCustodiaCuentaPropia(con, entrada, datosCuentaPropia);
		
		List<Tabla_Salida> custodiasClientes = new ArrayList<>();
		custodiasClientes.addAll(custodiasCuentaTerceros);
		custodiasClientes.addAll(custodiasCuentaPropia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerCustodia", fin - inicio);
		return custodiasClientes;
	}
	
	private List<DatoCuentaPropia> obtenerDatosCuentaPropia(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerDatosCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("IdEmpresa", idEmpresa);
		
		SQL _sql = new SQL();
		List<DatoCuentaPropia> datosCuentaPropia = _sql.query(con, SQL_SELECT_DATOS_CUENTA_PROPIA, parametro, DatoCuentaPropia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerDatosCuentaPropia", fin - inicio);
		return datosCuentaPropia;
		
	}
	
	private List<Tabla_Salida> obtenerCustodiaCuentaTerceros(Connection con, EntradaGenerarCuadraturaCustodiaCircular1518 entrada, List<DatoCuentaPropia> datosCuentaPropia) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaCuentaTerceros");
		long inicio = System.currentTimeMillis();
		
		Entrada_ConsultaCustodiaPortafolio entradaConsultarCustodia = new Entrada_ConsultaCustodiaPortafolio();
		entradaConsultarCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodia.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodia.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodia.p_FechaConsulta = entrada.p_FechaCuadratura;
		entradaConsultarCustodia.p_TipoFechaConsulta = "ADQ";
		entradaConsultarCustodia.p_AgrupaPorBoveda = "S";
		entradaConsultarCustodia.p_AgrupaPorRelacion = "S";
		entradaConsultarCustodia.p_SaldosNegativos = "N";
		entradaConsultarCustodia.p_SaldosNegativosPresuntos = "N";
		entradaConsultarCustodia.p_SolicitaDividendo = "S";
		
		ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
		Salida_ConsultaCustodiaPortafolio salidaConsultaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaConsultarCustodia);
		
		if(datosCuentaPropia.size() > 0) {
			Long idClienteCuentaPropia = datosCuentaPropia.get(0).IdCliente;
			salidaConsultaCustodiaPortafolio.P_OUT_TBL = salidaConsultaCustodiaPortafolio.P_OUT_TBL
					.stream()
					.filter(custodia->!idClienteCuentaPropia.equals(custodia.IdCliente))
					.collect(Collectors.toList());
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaCuentaTerceros", fin - inicio);
		return salidaConsultaCustodiaPortafolio.P_OUT_TBL;
	}
	
	private List<Tabla_Salida> obtenerCustodiaCuentaPropia(Connection con, EntradaGenerarCuadraturaCustodiaCircular1518 entrada, List<DatoCuentaPropia> datosCuentaPropia) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		List<Tabla_Salida> custodiaCuentaPropia = new ArrayList<>();
		
		for(DatoCuentaPropia datoCuentaPropia : datosCuentaPropia) {
			Entrada_ConsultaCustodiaPortafolio entradaConsultarCustodia = new Entrada_ConsultaCustodiaPortafolio();
			entradaConsultarCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCustodia.p_IdCliente = datoCuentaPropia.IdCliente;
			entradaConsultarCustodia.p_IdPortafolio = datoCuentaPropia.IdPortafolio;
			entradaConsultarCustodia.p_FechaConsulta = entrada.p_FechaCuadratura;
			entradaConsultarCustodia.p_TipoFechaConsulta = "ADQ";
			entradaConsultarCustodia.p_AgrupaPorBoveda = "S";
			entradaConsultarCustodia.p_AgrupaPorRelacion = "S";
			entradaConsultarCustodia.p_SaldosNegativos = "N";
			entradaConsultarCustodia.p_SaldosNegativosPresuntos = "N";
			entradaConsultarCustodia.p_SolicitaDividendo = "S";
			
			ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
			Salida_ConsultaCustodiaPortafolio salidaConsultaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaConsultarCustodia);
			custodiaCuentaPropia.addAll(salidaConsultaCustodiaPortafolio.P_OUT_TBL);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaCuentaPropia", fin - inicio);
		return custodiaCuentaPropia;
	}
	
	private HashMap<String, Comprobante> obtenerComprobantesTotalizadosPorNemo(Connection con, String fechaCuadratura)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerMovimientosTransito");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("FechaCuadratura", fechaCuadratura);
		
		SQL _sql = new SQL();
		List<Comprobante> comprobantes = _sql.query(con, SQL_SELECT_COMPROBANTES_TRANSITO, parametro, Comprobante.class);
		
		Map<String, List<Comprobante>> comprobantesPorNemo = comprobantes.stream().collect(Collectors.groupingBy(comprobante->comprobante.Nemotecnico + comprobante.IdClasificacionTipoInstrumento));
		
		HashMap<String, Comprobante> comprobantesTotalizadosPorNemo = new HashMap<String, Comprobante>();
		
		for(Entry<String, List<Comprobante>> comprobantesNemo : comprobantesPorNemo.entrySet()) {
			Comprobante comprobante = new Comprobante();
			comprobante.Nemotecnico = comprobantesNemo.getValue().get(0).Nemotecnico;
			for(Comprobante comprobanteNemo : comprobantesNemo.getValue()) {
				comprobante.CantidadCompra = comprobante.CantidadCompra.add(comprobanteNemo.CantidadCompra, MathContext.DECIMAL128);
				comprobante.CantidadVenta = comprobante.CantidadVenta.add(comprobanteNemo.CantidadVenta, MathContext.DECIMAL128);
				comprobante.Precio = comprobanteNemo.Precio;
				comprobante.IdClasificacionTipoInstrumento = comprobanteNemo.IdClasificacionTipoInstrumento;
				comprobante.IdTipoInstrumento = comprobanteNemo.IdTipoInstrumento;
			}
			comprobantesTotalizadosPorNemo.put(comprobante.Nemotecnico + comprobante.IdClasificacionTipoInstrumento, comprobante);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerMovimientosTransito", fin - inicio);
		return comprobantesTotalizadosPorNemo;
	}
	
	private List<Custodia> obtenerCustodiaAgrupadaPorNemotecnico(List<Tabla_Salida> custodiasPorCliente, String fechaCuadratura, HashMap<String, Comprobante> comprobantesTotalizadosPorNemo, Connection con) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaAgrupadaPorNemotecnico");
		long inicio = System.currentTimeMillis();
		
		Map<String, List<Tabla_Salida>> custodiasPorNemo = custodiasPorCliente.stream().collect(Collectors.groupingBy(custodia->custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento));
		List<Custodia> custodias = new ArrayList<>();
		
		for(Entry<String, List<Tabla_Salida>> saldoCustodia : custodiasPorNemo.entrySet()) {
			Custodia custodia = new Custodia();
			custodia.FechaCuadratura = fechaCuadratura;
			custodia.Nemotecnico = saldoCustodia.getValue().get(0).Nemotecnico;
			for(Tabla_Salida custodiaNemo : saldoCustodia.getValue()) {
				custodia.IdInstrumento = (custodiaNemo.IdInstrumento != null)? custodiaNemo.IdInstrumento : 0L;  			
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
				custodia.IdEmisor = (custodiaNemo.IdEmisor != null) ? custodiaNemo.IdEmisor : "";
				if (custodiaNemo.TotalPosicion.compareTo(BigDecimal.ZERO) < 0) { 
					custodia.CantidadPlazoNom=custodia.CantidadPlazoNom.add(custodiaNemo.TotalPosicion, MathContext.DECIMAL128);	 
					} 
				if(custodiaNemo.Relacion != null && (custodiaNemo.Relacion.equalsIgnoreCase(CPRO)== true)) {
					custodia.TotalCustodiaCuentaPropia = custodia.TotalCustodiaCuentaPropia.add(custodiaNemo.TotalPosicion, MathContext.DECIMAL128);
				}		
			}
			Comprobante comprobante = comprobantesTotalizadosPorNemo.get(custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento);
			if(comprobante != null) {
				custodia.CompraEnTransito = comprobante.CantidadCompra;
				custodia.VentaEnTransito = comprobante.CantidadVenta;
				custodia.TotalCustodioEmisores = custodia.CompraEnTransito.subtract(custodia.VentaEnTransito, MathContext.DECIMAL128);
				comprobante.Procesada = "S";
				comprobantesTotalizadosPorNemo.put(custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento, comprobante);
			}			
			custodia.TotalCustodia = calcularTotalCustodia(custodia, con);
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
				comprobantesTotalizadosPorNemo.put(custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento, comprobante);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaAgrupadaPorNemotecnico", fin - inicio);
		return custodias;
	}
	private List<CustodiaNegativa> obtenerCustodiaNegativaPorInstrumento(Connection con, Long IdInstrumento,String NemoSVS) throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaNegativa");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdInstrumento", IdInstrumento);
		params.put("NemoSVS", NemoSVS);
		SQL _sql = new SQL();
		List<CustodiaNegativa> CustodiaNegativaPorInstrumento = _sql.query(con, SQL_SELECT_CANTIDAD_CUS_NEG, params, CustodiaNegativa.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.obtenerCustodiaNegativa", fin - inicio);
		return CustodiaNegativaPorInstrumento;
		
	}
	private BigDecimal calcularTotalCustodia(Custodia custodia, Connection con) throws Exception {
		logger.trace("INICIO::GenerarCuadraturaCustodiaCircular1518.calcularTotalCustodia");
		long inicio = System.currentTimeMillis();
		if (("RF".equals(custodia.IdClasificacionTipoInstrumento) || "IF".equals(custodia.IdClasificacionTipoInstrumento)) && (custodia.CantidadPlazoNom.compareTo(BigDecimal.ZERO) < 0)) {
			custodia.CantidadCustodia = custodia.CantidadCustodia.subtract(custodia.CantidadPlazoNom, MathContext.DECIMAL128);
			List<CustodiaNegativa> CustodiaNegativaPorInstrumento = obtenerCustodiaNegativaPorInstrumento(con, custodia.IdInstrumento,custodia.Nemotecnico);
			if(CustodiaNegativaPorInstrumento.size()> 0 && CustodiaNegativaPorInstrumento.get(0).Cantidad!=null) {
				custodia.CantidadCustodia = custodia.CantidadCustodia.subtract(CustodiaNegativaPorInstrumento.get(0).Cantidad);
			}
		}
		
		BigDecimal totalCustodia = custodia.CantidadCustodia.add(custodia.CantidadGarantia, MathContext.DECIMAL128).add(custodia.CantidadCompraPlazo, MathContext.DECIMAL128).subtract(custodia.CantidadVentaPlazo, MathContext.DECIMAL128);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCuadraturaCustodiaCircular1518.calcularTotalCustodia", fin - inicio);
		return totalCustodia;
	}
	
	private void insertarCargaCuadratura1720(Connection con, List<Custodia> custodias)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1720.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsCarga = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::CargaCuadraturaCircular1720", (Long) 1L);
		Long idCarga = idsCarga.get(0);
		
		custodias.parallelStream().forEach(custodia->custodia.IdCarga = idCarga);
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULAR1720, custodias, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1720.insertarCargaCuadratura", fin - inicio);
	}
	
	private void insertarCargaCuadratura1518(Connection con, List<Custodia> custodias)  throws Exception {
		logger.debug("INICIO::GenerarCuadraturaCustodiaCircular1518.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsCarga = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::CargaCuadraturaCircular1518", (Long) 1L);
		Long idCarga = idsCarga.get(0);
		
		custodias.parallelStream().forEach(custodia->custodia.IdCarga = idCarga);
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULAR1518, custodias, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCuadraturaCustodiaCircular1518.insertarCargaCuadratura", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			EntradaGenerarCuadraturaCustodiaCircular1518 entradaGenerarCuadraturaCustodiaCircular1518 = new EntradaGenerarCuadraturaCustodiaCircular1518();
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdEmpresa = 1L;
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdUsuario = 97L;
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdModulo = "CUS";
			entradaGenerarCuadraturaCustodiaCircular1518.p_FechaCuadratura = "2025-01-24";
			
			GenerarCuadraturaCustodiaCircular1518 GenerarCuadraturaCustodiaCircular1518 = new GenerarCuadraturaCustodiaCircular1518();
			try {
				SalidaGenerarCuadraturaCustodiaCircular1518 salidaProcesarCargaMasivaInstruccion = GenerarCuadraturaCustodiaCircular1518.logica(con, entradaGenerarCuadraturaCustodiaCircular1518);
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
