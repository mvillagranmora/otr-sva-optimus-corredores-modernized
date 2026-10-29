package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CargaCustodiaCircular1518Utils.Instrumento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoCircular1518 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarArchivoCircular1518.class);
	
	private final String SQL_DELETE_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircular1518_DeleteCuadraturaCircular1518.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircular1518_InsertCuadraturaCircular1518.sql");
	
	public static class EntradaCargarArchivoCircular1518 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<Posicion> p_DatosArchivo;
	}
	
	public static class Posicion extends BcsPojo {
		public String Nemotecnico;
		public BigDecimal CantidadDisponible;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadVentaPlazo;
		public BigDecimal CantidadCompraPlazo;
		public BigDecimal VentaEnTransito;
		public BigDecimal CompraEnTransito;
		public BigDecimal Precio;
		
		public Posicion() {
			this.CantidadDisponible = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadVentaPlazo = BigDecimal.ZERO;
			this.CantidadCompraPlazo = BigDecimal.ZERO;
			this.VentaEnTransito = BigDecimal.ZERO;
			this.CompraEnTransito = BigDecimal.ZERO;
			this.Precio = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaCargarArchivoCircular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaCargarArchivoCircular1518() {
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
		public BigDecimal Precio;
		public BigDecimal Diferencia;
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
			this.Diferencia = BigDecimal.ZERO;
			this.TotalCustodioEmisores = BigDecimal.ZERO;
			this.Valorizacion = BigDecimal.ZERO;
			this.DiferenciaValorizada = BigDecimal.ZERO;
			this.Precio = BigDecimal.ZERO;
			this.IdClasificacionTipoInstrumento = "";
			this.IdTipoInstrumento = "";
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargarArchivoCircular1518 in = gson.fromJson(jin.toString(), EntradaCargarArchivoCircular1518.class);
		SalidaCargarArchivoCircular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarArchivoCircular1518 logica(Connection con, EntradaCargarArchivoCircular1518 entrada) throws Exception {
		logger.info("INICIO::CargarArchivoCircular1518");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCargarArchivoCircular1518 out = new SalidaCargarArchivoCircular1518();
		
		borrarCargaCuadratura(con, entrada.p_FechaCuadratura);
		List<Posicion> posicionesPorNemo = obtenerPosicionPorNemo(entrada.p_DatosArchivo);
		List<Custodia> custodias = obtenerCustodiasInsertar(con, posicionesPorNemo, entrada.p_FechaCuadratura);
		insertarCargaCuadratura(con, custodias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoCircular1518", finServicio - inicioServicio);
		return out;
	}
	
	private void borrarCargaCuadratura(Connection con, String fechaCuadratura)  throws Exception {
		logger.debug("INICIO::CargarArchivoCircular1518.borrarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		Custodia custodia = new Custodia();
		custodia.FechaCuadratura = fechaCuadratura;
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULAR1518, Arrays.asList(custodia), Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoCircular1518.borrarCargaCuadratura", fin - inicio);
	}
	
	private List<Posicion> obtenerPosicionPorNemo(List<Posicion> posiciones){
		logger.debug("INICIO::CargarArchivoCircular1518.obtenerPosicionPorNemo");
		long inicio = System.currentTimeMillis();
		
		Map<String, List<Posicion>> posicionesAgrupadas = posiciones.stream().collect(Collectors.groupingBy(custodia->custodia.Nemotecnico));
		
		List<Posicion> posicionesPorNemo = new ArrayList<>();
		
		for(Entry<String, List<Posicion>> posicionesNemo : posicionesAgrupadas.entrySet()) {
			Posicion posicionNemo = new Posicion();
			posicionNemo.Nemotecnico = posicionesNemo.getKey();
			for(Posicion posicionArchivo : posicionesNemo.getValue()) {
				posicionNemo.CantidadDisponible = posicionNemo.CantidadDisponible.add(posicionArchivo.CantidadDisponible, MathContext.DECIMAL128);
				posicionNemo.CantidadGarantia = posicionNemo.CantidadGarantia.add(posicionArchivo.CantidadGarantia, MathContext.DECIMAL128);
				posicionNemo.CantidadVentaPlazo = posicionNemo.CantidadVentaPlazo.add(posicionArchivo.CantidadVentaPlazo, MathContext.DECIMAL128);
				posicionNemo.CantidadCompraPlazo = posicionNemo.CantidadCompraPlazo.add(posicionArchivo.CantidadCompraPlazo, MathContext.DECIMAL128);
				posicionNemo.VentaEnTransito = posicionNemo.VentaEnTransito.add(posicionArchivo.VentaEnTransito, MathContext.DECIMAL128);
				posicionNemo.CompraEnTransito = posicionNemo.CompraEnTransito.add(posicionArchivo.CompraEnTransito, MathContext.DECIMAL128);
				posicionNemo.Precio = posicionArchivo.Precio;
			}
			
			posicionesPorNemo.add(posicionNemo);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoCircular1518.obtenerPosicionPorNemo", fin - inicio);
		
		return posicionesPorNemo;
	}
	
	private List<Custodia> obtenerCustodiasInsertar(Connection con, List<Posicion> posicionesPorNemo, String fechaCuadratura) throws Exception {
		logger.debug("INICIO::CargarArchivoCircular1518.obtenerPosicionPorNemo");
		long inicio = System.currentTimeMillis();
		
		List<String> nemotecnicos = posicionesPorNemo.stream().map(custodia->custodia.Nemotecnico).collect(Collectors.toList());
		Map<String, Instrumento> instrumentosPorNemo = CargaCustodiaCircular1518Utils.obtenerInstrumentosPorNemo(con, nemotecnicos, new ArrayList<String>());
		
		List<Long> idsCarga = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::CargaCuadraturaCircular1518", (Long) 1L);
		Long idCarga = idsCarga.get(0);
		
		List<Custodia> custodias = posicionesPorNemo.stream().map(posicion->{
			Custodia custodia = new Custodia();
			custodia.IdCarga = idCarga;
			custodia.FechaCuadratura = fechaCuadratura;
			custodia.Nemotecnico = posicion.Nemotecnico;
			custodia.CantidadCustodia = posicion.CantidadDisponible;
			custodia.CantidadGarantia = posicion.CantidadGarantia;
			custodia.CantidadVentaPlazo = posicion.CantidadVentaPlazo;
			custodia.CantidadCompraPlazo = posicion.CantidadCompraPlazo;
			custodia.TotalCustodia = calcularTotalCustodia(custodia);
			custodia.CompraEnTransito = posicion.CompraEnTransito;
			custodia.VentaEnTransito = posicion.VentaEnTransito;
			custodia.TotalCustodioEmisores = custodia.CompraEnTransito.subtract(custodia.VentaEnTransito, MathContext.DECIMAL128);;
			custodia.Diferencia = custodia.TotalCustodioEmisores.subtract(custodia.TotalCustodia);
			Instrumento instrumento = instrumentosPorNemo.get(custodia.Nemotecnico);
			if(instrumento != null) {
				custodia.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = instrumento.IdTipoInstrumento;
				custodia.Precio = "RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento) ? posicion.Precio : BigDecimal.ONE;
			} else {
				custodia.Precio = BigDecimal.ONE;
			}
			custodia.Valorizacion = custodia.Precio.multiply(custodia.TotalCustodia, MathContext.DECIMAL128);
			custodia.DiferenciaValorizada = custodia.Precio.multiply(custodia.Diferencia, MathContext.DECIMAL128);
			return custodia;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoCircular1518.obtenerCustodiasInsertar", fin - inicio);
		return custodias;
	}
	
	private void insertarCargaCuadratura(Connection con, List<Custodia> custodias)  throws Exception {
		logger.debug("INICIO::CargarArchivoCircular1518.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		_sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULAR1518, custodias, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoCircular1518.insertarCargaCuadratura", fin - inicio);
	}
	
	private BigDecimal calcularTotalCustodia(Custodia custodia) {
		logger.trace("INICIO::CargarArchivoCircular1518.calcularTotalCustodia");
		long inicio = System.currentTimeMillis();
		
		BigDecimal totalCustodia = custodia.CantidadCustodia.add(custodia.CantidadGarantia, MathContext.DECIMAL128).add(custodia.CantidadCompraPlazo, MathContext.DECIMAL128).subtract(custodia.CantidadVentaPlazo, MathContext.DECIMAL128);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CargarArchivoCircular1518.calcularTotalCustodia", fin - inicio);
		return totalCustodia;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			EntradaCargarArchivoCircular1518 entradaCargarArchivoCircular1518 = new EntradaCargarArchivoCircular1518();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_FechaCuadratura\":\"2021-12-30\",\"p_DatosArchivo\":[{\"Nemotecnico\":\"AGUAS-A\",\"CantidadDisponible\":\"30000\",\"CantidadGarantia\":\"4000.5\",\"CantidadVentaPlazo\":\"5000.555\",\"CantidadCompraPlazo\":\"60000.6666\",\"VentaEnTransito\":\"7000.77\",\"CompraEnTransito\":\"8.78\",\"Validacion\":\"OK\"}]}";
			Gson gson = Utils.getGsonBuilder();
			entradaCargarArchivoCircular1518 = gson.fromJson(json, EntradaCargarArchivoCircular1518.class);	
			CargarArchivoCircular1518 cargarArchivoCircular1518 = new CargarArchivoCircular1518();
			
			try {
				SalidaCargarArchivoCircular1518 salidaCargarArchivoCircular1518 = cargarArchivoCircular1518.logica(con, entradaCargarArchivoCircular1518);
				System.out.println(salidaCargarArchivoCircular1518.toString());
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
