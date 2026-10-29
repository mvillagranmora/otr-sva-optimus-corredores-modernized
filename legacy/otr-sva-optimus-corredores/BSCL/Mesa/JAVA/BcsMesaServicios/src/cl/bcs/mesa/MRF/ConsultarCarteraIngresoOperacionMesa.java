package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCarteraIngresoOperacionMesa implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConsultarCarteraIngresoOperacionMesa.class);
	
	private final String SQL_SELECT_CARTERA_INGRESO_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraIngresoOperacionMesa_SelectConsultarCartera.sql");
	private final String SQL_SELECT_CUSTODIA_RUEDA       = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraIngresoOperacionMesa_SelectCustodiaRueda.sql");
	private final String SQL_SELECT_TRANSITOS      		 = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraIngresoOperacionMesa_SelectTransitos.sql");
	
	
	public static class EntradaConsultarCarteraIngresoOperacionMesa extends BcsPojo {

		public Long p_IdEmpresa;
		public Long p_IdPortafolio;
		public Long p_IdCliente;
		public Long p_IdMesa;
		public String p_FechaVencimiento;
		public String p_FechaLiquidacionMenor;
		public String p_FechaLiquidacionMayor;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdEmisor;
		public String p_IdTipoReajuste;
		public String p_Nemotecnico;
		public List<Long> p_FolioCartera;
		
		public EntradaConsultarCarteraIngresoOperacionMesa() {
			this.p_FolioCartera = new ArrayList<Long>();
		}

	}

	public static class DetalleConsulta extends BcsPojo   {
		public Long IdEmpresa;
		public Long IdPortafolio;
		public Long IdCliente;
		public Long IdMesa;
		public Long FolioCartera;
		public Long IdCartera;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ValorClasificacionRiesgo;
		public String IdClasificacionTipoInstrumento;
		public String IdEmisor;
		public String IdBoveda;
		public String IdBolsa;
		public String CodigoIFRS;
		public String CodigoDescIFRS;
		public String IdMonedaSistema;
		public String IdTipoReajuste;
		public String Moneda;
		public String MonedaInstrumento;
		public String EsFungible;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public String IdMonedaInstrumento;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal Cantidad;
		public BigDecimal INGCantidad;
		public BigDecimal INGCantidad_UI;
		public BigDecimal CorteMinimo;
		public BigDecimal INGTasaCompra;
		public BigDecimal INGValorRescate;
		public BigDecimal INGTasaMercadoActual;
		public Date FechaVencimientoInstrumento;
		public BigDecimal INGPrecioMercadoActual;
		public BigDecimal INGPrecioCompraActual;
		public Date FechaLiquidacion;
		public BigDecimal CantidadSimulacion;
		public BigDecimal PrecioSimulacion;
		public BigDecimal TasaSimulacion;
		public BigDecimal MontoSimulacion;
		public BigDecimal ValorRescateSimulacion;
		public Date FechaVencimientoSimulacion;
		public String CarteraPacto;
		public String ClasificacionTipoInstrumento;
		public String TipoValorizacion;
		public String NombreBoveda;
		public Date FechaEmisionInstrumento;
		public BigDecimal MontoCLP_Org;
		public Long IdUsuario;
		public Long IdUsuarioReserva;
		public String TipoCarteraCorredor;

	}
	
	public static class CustodiaPorRueda extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolioCliente;
		public String IdEstadoOrden;
		public String Nemotecnico;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate; 
		public BigDecimal CorteMinimo;
		public String IdEmisor;
		public String IdBoveda;
		public String EsFungible;
		public Date FechaVencimiento;
		public String IdTipoReajuste;
	}
	
	
	public static class Transito extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioCustodia;
		public String Nemotecnico;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate; 
	}

	public static class SalidaConsultarCarteraIngresoOperacionMesa extends BcsPojo {

		public Long P_OUT_ERRORESCONTADOR;
		public List<DetalleConsulta> P_OUT_SALIDA;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public SalidaConsultarCarteraIngresoOperacionMesa() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SALIDA = new ArrayList<DetalleConsulta>();
		}

	}

	public SalidaConsultarCarteraIngresoOperacionMesa logica(Connection conn, EntradaConsultarCarteraIngresoOperacionMesa entrada) throws Exception {
		logger.info("INICIO::ConsultarCarteraIngresoOperacionMesa");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConsultarCarteraIngresoOperacionMesa salida = new SalidaConsultarCarteraIngresoOperacionMesa();		
		
		List<DetalleConsulta> carterasIngresoMesa = this.obtenerConsultaCarteras(conn, entrada);
		
		Map<String,CustodiaPorRueda> mapCustodiasRueda = this.obtenerCustodiaPorRueda(conn,entrada,carterasIngresoMesa);		
		
		Map<Long,Transito> mapCarteraTransitos = this.obtenerTransitosCarteras(conn,entrada,carterasIngresoMesa);
		
		List<DetalleConsulta> listaCarteraIngresadas = this.calcularCarteraDisponibles(carterasIngresoMesa,mapCustodiasRueda,mapCarteraTransitos);		
				
		salida.P_OUT_SALIDA.addAll(listaCarteraIngresadas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarCarteraIngresoOperacionMesa :" , finServicio- inicioServicio);	
		return salida;
	}
	
	private List<DetalleConsulta> calcularCarteraDisponibles(List<DetalleConsulta> carterasIngresoMesa,Map<String, CustodiaPorRueda> mapCustodiasRueda, Map<Long, Transito> mapCarteraTransitos) {
		logger.trace("INICIO::ConsultarCarteraIngresoOperacionMesa.calcularCarteraDisponibles");
		long inicioServicio = System.currentTimeMillis();
		
		List<DetalleConsulta> listaCarterasCalculadas = new ArrayList<>();
		
		
		//Descontar Cartera en transito		
		carterasIngresoMesa.forEach(car ->{
			if(mapCarteraTransitos.get(car.FolioCartera)!=null) {
				BigDecimal cantidadValorRescateTransito = BigDecimal.ZERO;
				if("IF".equals(car.IdClasificacionTipoInstrumento) ) {
					BigDecimal cantidadActual = car.INGValorRescate;
					cantidadValorRescateTransito = mapCarteraTransitos.get(car.FolioCartera).ValorRescate;
					car.INGValorRescate=car.INGValorRescate.subtract(cantidadValorRescateTransito);
					car.INGMontoCompraActualMonedaInstrumento = calcularMontoPorcentual(car.INGMontoCompraActualMonedaInstrumento ,cantidadActual,car.INGValorRescate);
					car.INGMontoATasaCompraActual = calcularMontoPorcentual(car.INGMontoATasaCompraActual ,cantidadActual,car.INGValorRescate);
					car.MontoCLP_Org = calcularMontoPorcentual(car.MontoCLP_Org ,cantidadActual,car.INGValorRescate);
				}
				// RF + IF NO Fungible
				if ( "RF".equals(car.IdClasificacionTipoInstrumento )  ||
					 ( "IF".equals(car.IdClasificacionTipoInstrumento ) ) && "N".equals(car.EsFungible)) {
					
					BigDecimal cantidadActual = car.INGCantidad;
					cantidadValorRescateTransito = mapCarteraTransitos.get(car.FolioCartera).Cantidad;
					car.INGCantidad=car.INGCantidad.subtract(cantidadValorRescateTransito);
					car.INGMontoCompraActualMonedaInstrumento = calcularMontoPorcentual(car.INGMontoCompraActualMonedaInstrumento ,cantidadActual,car.INGCantidad);
					car.INGMontoATasaCompraActual = calcularMontoPorcentual(car.INGMontoATasaCompraActual ,cantidadActual,car.INGCantidad);
					car.MontoCLP_Org = calcularMontoPorcentual(car.MontoCLP_Org ,cantidadActual,car.INGCantidad);
				}				
			}			
		});
				
		//Descontar custodia por rueda	
		carterasIngresoMesa.forEach(car ->{
			String key = this.crearKey(car);
			CustodiaPorRueda asignado = mapCustodiasRueda.get(this.crearKey(car));

			if(asignado!=null && this.esMismoPapel(asignado, car) ) { 
				BigDecimal cantidadValorRescate = BigDecimal.ZERO;
				
				// IF (todos)
				if( "IF".equals(car.IdClasificacionTipoInstrumento ) ) {
					cantidadValorRescate =mapCustodiasRueda.get(key).ValorRescate;
					if ( cantidadValorRescate.compareTo(BigDecimal.ZERO) > 0 ) {
						BigDecimal cantidadActual = car.INGValorRescate;
						if(cantidadActual.compareTo(cantidadValorRescate)>0) {
							// Cant.Cartera Netea lo asignado por rueda
							car.INGValorRescate = car.INGValorRescate.subtract(cantidadValorRescate);
							mapCustodiasRueda.get(key).ValorRescate = BigDecimal.ZERO;
						} else {
							// Cant.Cartera no es suficiente, espera por otra cartera para matar la asignacion por rueda
							mapCustodiasRueda.get(key).ValorRescate = cantidadValorRescate.subtract(car.INGValorRescate);
							car.INGValorRescate = BigDecimal.ZERO;
						}
						car.INGMontoCompraActualMonedaInstrumento = calcularMontoPorcentual(car.INGMontoCompraActualMonedaInstrumento ,cantidadActual,car.INGValorRescate);
						car.INGMontoATasaCompraActual = calcularMontoPorcentual(car.INGMontoATasaCompraActual ,cantidadActual,car.INGValorRescate);
						car.MontoCLP_Org = calcularMontoPorcentual(car.MontoCLP_Org ,cantidadActual,car.INGValorRescate);
					}
				}
				
				// RF + IF NO Fungible
				if ( "RF".equals(car.IdClasificacionTipoInstrumento )  ||
					 ( "IF".equals(car.IdClasificacionTipoInstrumento ) ) && "N".equals(car.EsFungible)) {
					// Solo si queda por descontar					
					cantidadValorRescate =mapCustodiasRueda.get(key).Cantidad;
					if ( cantidadValorRescate.compareTo(BigDecimal.ZERO) > 0 ) {
						BigDecimal cantidadActual = car.INGCantidad;
						if(cantidadActual.compareTo(cantidadValorRescate)>0) {
							// Cant.Cartera Netea lo asignado por rueda
							car.INGCantidad = car.INGCantidad.subtract(cantidadValorRescate);
							mapCustodiasRueda.get(key).Cantidad = BigDecimal.ZERO;
						} else {
							// Cant.Cartera no es suficiente, espera por otra cartera para matar la asignacion por rueda
							mapCustodiasRueda.get(key).Cantidad = cantidadValorRescate.subtract(car.INGCantidad);
							car.INGCantidad = BigDecimal.ZERO;
						}
						car.INGMontoCompraActualMonedaInstrumento = calcularMontoPorcentual(car.INGMontoCompraActualMonedaInstrumento ,cantidadActual,car.INGCantidad);
						car.INGMontoATasaCompraActual = calcularMontoPorcentual(car.INGMontoATasaCompraActual ,cantidadActual,car.INGCantidad);
						car.MontoCLP_Org = calcularMontoPorcentual(car.MontoCLP_Org ,cantidadActual,car.INGCantidad);
					}
				}				
			}			
		});
		
		listaCarterasCalculadas = carterasIngresoMesa.stream().filter(car -> 
			!(car.INGCantidad.compareTo(BigDecimal.ZERO)==0)||
			!(car.INGValorRescate.compareTo(BigDecimal.ZERO)==0)
		).collect(Collectors.toList());	
		
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.calcularCarteraDisponibles :" , finServicio- inicioServicio);
		return listaCarterasCalculadas;
	}
	
	private Boolean esMismoPapel(CustodiaPorRueda asignacion , DetalleConsulta cartera) {
		Boolean igual = false;
		if(asignacion.IdInstrumento.equals(cartera.IdInstrumento) && 
		   asignacion.EsFungible.equals(cartera.EsFungible) &&
		   asignacion.IdBoveda.equals(cartera.IdBoveda) &&
		   asignacion.IdTipoReajuste.equals(cartera.IdTipoReajuste) &&
		   asignacion.FechaVencimiento.equals(cartera.FechaVencimientoInstrumento) &&
		   asignacion.IdEmisor.equals(cartera.IdEmisor)) {
			igual = true;
		}
		return igual;
	}
	
	private BigDecimal calcularMontoPorcentual (BigDecimal montoActual , BigDecimal cantidadActual , BigDecimal cantidadNueva) {
		logger.trace("INICIO::ConsultarCarteraIngresoOperacionMesa.calcularMontoPorcentual");
		long inicioServicio = System.currentTimeMillis();
		
		BigDecimal porcentaje = BigDecimal.ONE;
		
		if(montoActual.compareTo(BigDecimal.ZERO)!=0 && cantidadActual.compareTo(BigDecimal.ZERO)!=0) {
			 porcentaje = ((cantidadNueva.multiply(new BigDecimal (100))).divide(cantidadActual, MathContext.DECIMAL128)).divide(new BigDecimal (100), MathContext.DECIMAL128);
		}		
		
		BigDecimal montoNuevo = montoActual.multiply(porcentaje);
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.calcularMontoPorcentual :" , finServicio- inicioServicio);
		return montoNuevo;
		
	}


	private Map<Long, Transito> obtenerTransitosCarteras(Connection conn,EntradaConsultarCarteraIngresoOperacionMesa entrada, List<DetalleConsulta> carterasIngresoMesa) throws Exception {
		logger.debug("INICIO::ConsultarCarteraIngresoOperacionMesa.obtenerTransitosCarteras");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object>	params = new HashMap<String,Object>(3);
		List<Long> listaIdCliente = new ArrayList<Long>();
		List<Long> listaIdPortafolio = new ArrayList<Long>();
		List<Long> listaFolios = carterasIngresoMesa.stream().map(car->car.FolioCartera).collect(Collectors.toList())
									   .stream().distinct().collect(Collectors.toList());
		
		if(!entrada.p_FolioCartera.isEmpty()) {
			listaIdCliente = carterasIngresoMesa.stream().map(car-> car.IdCliente).distinct().collect(Collectors.toList());
			listaIdPortafolio = carterasIngresoMesa.stream().map(car-> car.IdPortafolio).distinct().collect(Collectors.toList());
		} else {
			listaIdCliente.add(entrada.p_IdCliente);
			listaIdPortafolio.add(entrada.p_IdPortafolio);
		}
		
		params.put("idCliente", listaIdCliente);
		params.put("idPortafolio", listaIdPortafolio);
		params.put("folioCustodia", listaFolios);
		
		Map<Long, Transito> mapCustodia = sql.query(conn, SQL_SELECT_TRANSITOS, params, Transito.class)
				.stream().collect(Collectors.toMap(cus -> cus.FolioCustodia , cus -> cus));
		
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.obtenerTransitosCarteras :" , finServicio- inicioServicio);
		return mapCustodia;
	}


	private Map<String, CustodiaPorRueda> obtenerCustodiaPorRueda(Connection conn,EntradaConsultarCarteraIngresoOperacionMesa entrada,List<DetalleConsulta> carterasIngresoMesa) throws Exception {
		logger.debug("INICIO::ConsultarCarteraIngresoOperacionMesa.obtenerCustodiaPorRueda");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object>	params = new HashMap<String,Object>(4);
		List<Long> listaIdCliente = new ArrayList<Long>();
		List<Long> listaIdPortafolio = new ArrayList<Long>();
		List<Long> listaInstrumentos = carterasIngresoMesa.stream().map(car->car.IdInstrumento).collect(Collectors.toList())
									   .stream().distinct().collect(Collectors.toList());
		
		if(!entrada.p_FolioCartera.isEmpty()) {
			listaIdCliente = carterasIngresoMesa.stream().map(car-> car.IdCliente).distinct().collect(Collectors.toList());
			listaIdPortafolio = carterasIngresoMesa.stream().map(car-> car.IdPortafolio).distinct().collect(Collectors.toList());
		} else {
			listaIdCliente.add(entrada.p_IdCliente);
			listaIdPortafolio.add(entrada.p_IdPortafolio);
		}
		
		params.put("idCliente", listaIdCliente);
		params.put("idPortafolio", listaIdPortafolio);
		params.put("idInstrumento", listaInstrumentos);
		
		List<CustodiaPorRueda> listaCustodia = sql.query(conn, SQL_SELECT_CUSTODIA_RUEDA, params, CustodiaPorRueda.class);
		Map<String, CustodiaPorRueda> mapCustodia = new HashMap<>();
		
		listaCustodia.forEach(cus -> {
			String key = crearKey(cus);
			mapCustodia.put(key, cus);			
		});
				
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.obtenerCustodiaPorRueda :" , finServicio- inicioServicio);
		
		return mapCustodia;
	}
	
	private String crearKey(CustodiaPorRueda cus) {
		return cus.IdInstrumento+cus.EsFungible+cus.IdEmisor+cus.IdBoveda+cus.IdTipoReajuste+cus.FechaVencimiento+cus.IdPortafolioCliente;
	}
	
	private String crearKey(DetalleConsulta cus) {
		return cus.IdInstrumento+cus.EsFungible+cus.IdEmisor+cus.IdBoveda+cus.IdTipoReajuste+cus.FechaVencimientoInstrumento+cus.IdPortafolio;
	}
	

	private List<DetalleConsulta> obtenerConsultaCarteras (Connection con , EntradaConsultarCarteraIngresoOperacionMesa entrada) throws Exception{
		logger.debug("INICIO::ConsultarCarteraIngresoOperacionMesa.obtenerConsultaCarteras");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> paramsConsultaCartera = new HashMap<String,Object>(12);
		Long filtrarPorFolio = 0L;
		List<Long> folioCartera = null;
		
		if(!entrada.p_FolioCartera.isEmpty()) {
			filtrarPorFolio = 1L;
			folioCartera = entrada.p_FolioCartera;
		}
		
		paramsConsultaCartera.put("IdEmpresa", entrada.p_IdEmpresa);
		paramsConsultaCartera.put("FechaLiquidacionMayor", entrada.p_FechaLiquidacionMayor);
		paramsConsultaCartera.put("FechaLiquidacionMenor", entrada.p_FechaLiquidacionMenor);
		paramsConsultaCartera.put("FechaVencimiento", entrada.p_FechaVencimiento);
		paramsConsultaCartera.put("IdCliente", entrada.p_IdCliente);
		paramsConsultaCartera.put("IdMesa", entrada.p_IdMesa);
		paramsConsultaCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsConsultaCartera.put("IdEmisor",entrada.p_IdEmisor);		
		paramsConsultaCartera.put("IdClasificacionTipoInstrumento",entrada.p_IdClasificacionTipoInstrumento);
		paramsConsultaCartera.put("Nemotecnico",entrada.p_Nemotecnico);
		paramsConsultaCartera.put("IdTipoReajuste",entrada.p_IdTipoReajuste);
		paramsConsultaCartera.put("FiltrarPorFolioCartera", filtrarPorFolio);
		paramsConsultaCartera.put("FolioCartera", entrada.p_FolioCartera);
		
		List<DetalleConsulta> carterasIngresoMesa = sql.query(con, SQL_SELECT_CARTERA_INGRESO_MESA,paramsConsultaCartera, DetalleConsulta.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::SalidaRegenerarDocumentosCliente.obtenerConsultaCarteras :" , finServicio- inicioServicio);	
		return carterasIngresoMesa;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCarteraIngresoOperacionMesa in = gson.fromJson(jin.toString(), EntradaConsultarCarteraIngresoOperacionMesa.class);
		SalidaConsultarCarteraIngresoOperacionMesa out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/

	public static void main(String[] args) throws Exception {

		try {
			Connection con = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties");
			System.out.println("== COMIENZA==");

			try {

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
				String json = "{\"p_IdEmpresa\":1,\"p_FechaLiquidacionMayor\":\"0001-01-01\",\"p_FechaLiquidacionMenor\":\"2026-01-19\",\"p_FechaVencimiento\":\"2026-01-19\",\"p_IdCliente\":4846,\"p_IdMesa\":39,\"p_IdPortafolio\":2225,\"p_IdEmisor\":\"\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_Nemotecnico\":\"\",\"p_IdTipoReajuste\":\"\"}";

				EntradaConsultarCarteraIngresoOperacionMesa entrada = gson.fromJson(json, EntradaConsultarCarteraIngresoOperacionMesa.class);
				ConsultarCarteraIngresoOperacionMesa demo = new ConsultarCarteraIngresoOperacionMesa();
				demo.logica(con, entrada);
			} catch (Exception e) {
				con.rollback();
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");

	}

}
