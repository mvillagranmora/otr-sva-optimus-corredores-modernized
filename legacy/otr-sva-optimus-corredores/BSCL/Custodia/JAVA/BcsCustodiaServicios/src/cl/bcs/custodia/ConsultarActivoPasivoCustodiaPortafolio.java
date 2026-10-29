package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
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

import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarActivoPasivoCustodiaPortafolio implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarActivoPasivoCustodiaPortafolio.class);
	
	//FECHA ACTUAL
	private final String SQL_SELECT_CARTERA_ACTIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectCarteraActivoActual.sql");
	private final String SQL_SELECT_CARTERA_PASIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectCarteraPasivoActual.sql");
	private final String SQL_SELECT_GARANTIA_ACTIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectGarantiaActivoActual.sql");
	private final String SQL_SELECT_PRESTAMO_ACTIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPrestamoActivoActual.sql");
	private final String SQL_SELECT_PRESTAMO_PASIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPrestamoPasivoActual.sql");
	private final String SQL_SELECT_PLAZO_ACTIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPlazoActivoActual.sql");
	private final String SQL_SELECT_PLAZO_PASIVO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPlazoPasivoActual.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectDividendosActual.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectDividendosTBCEDVRVActual.sql");
	
	//FECHA HISTORICO
	private final String SQL_SELECT_CARTERA_ACTIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectCarteraActivoHistorico.sql");
	private final String SQL_SELECT_CARTERA_PASIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectCarteraPasivoHistorico.sql");
	private final String SQL_SELECT_GARANTIA_ACTIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectGarantiaActivoHistorico.sql");
	private final String SQL_SELECT_PRESTAMO_ACTIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPrestamoActivoHistorico.sql");
	private final String SQL_SELECT_PRESTAMO_PASIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPrestamoPasivoHistorico.sql");
	private final String SQL_SELECT_PLAZO_ACTIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPlazoActivoHistorico.sql");
	private final String SQL_SELECT_PLAZO_PASIVO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectPlazoPasivoHistorico.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectDividendosHistorico.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/ConsultarActivoPasivoCustodiaPortafolio_SelectDividendosTBCEDVRVHistorico.sql");
	
	
	
	//Portafolios
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");

	
	/**************************************************
	 * 					CLASES 
	 * ************************************************/
	
	public static class Entrada_ConsultaActivoPasivo{
		
		public Long p_IdEmpresa;
		public Date p_FechaDesde;
		public Date p_FechaHasta;
		public List<Long> p_Clientes;
		
		public static class ClientesIn {
			public Long 	IdPortafolio;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public Entrada_ConsultaActivoPasivo() {
			this.p_Clientes = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida_ConsultaActivoPasivo{
		
		public ArrayList<Cartera> p_Carteras;
		
		public Salida_ConsultaActivoPasivo() {
			this.p_Carteras = new ArrayList<Cartera>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

		
	public static class Cartera{
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Long  		IdInstrumento;
		public String 		Nemotecnico;		
		public BigDecimal	DisponibleActivo;
		public BigDecimal	DisponiblePasivo;
		public BigDecimal	GarantiaActivo;
		public BigDecimal	GarantiaPasivo;
		public BigDecimal	PrestamoActivo;
		public BigDecimal	PrestamoPasivo;
		public BigDecimal	PlazoActivo;
		public BigDecimal	PlazoPasivo;
		public BigDecimal	DisponibleActivoTra;
		public BigDecimal	DisponiblePasivoTra;
		
		public String 		IdBoveda;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public String 		IdMonedaInstrumento;
		public String 		IdEmisor; 
		public String		FechaEfectivaMovimiento;
		public String		EsDividendo;
		public BigDecimal 	PrecioPromedioCompra;
		public BigDecimal 	MontoPromedioCompraMonedaSistemaActivo;
		public BigDecimal 	MontoPromedioCompraMonedaAdicionalActivo;
		public BigDecimal 	MontoATasaCompraActivo;
		public BigDecimal	PrecioMercado;
		public BigDecimal	MontoMercadoMonedaSistemaActivo;
		public BigDecimal	MontoMercadoMonedaAdicionalActivo;
		public BigDecimal 	MontoPromedioCompraMonedaSistemaPasivo;
		public BigDecimal 	MontoPromedioCompraMonedaAdicionalPasivo;
		public BigDecimal 	MontoATasaCompraPasivo;
		public BigDecimal	MontoMercadoMonedaSistemaPasivo;
		public BigDecimal	MontoMercadoMonedaAdicionalPasivo;
		public BigDecimal   Dividendo;
		
		public Cartera() {
			this.DisponibleActivo = BigDecimal.ZERO;
			this.DisponiblePasivo = BigDecimal.ZERO;
			this.GarantiaActivo = BigDecimal.ZERO;
			this.GarantiaPasivo = BigDecimal.ZERO;
			this.PrestamoActivo = BigDecimal.ZERO;
			this.PrestamoPasivo = BigDecimal.ZERO;
			this.PlazoActivo = BigDecimal.ZERO;
			this.PlazoPasivo = BigDecimal.ZERO;
			this.DisponibleActivoTra = BigDecimal.ZERO;
			this.DisponiblePasivoTra = BigDecimal.ZERO;
			this.PrecioPromedioCompra = BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaSistemaActivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaAdicionalActivo = BigDecimal.ZERO;
			this.MontoATasaCompraActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaSistemaActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaAdicionalActivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaSistemaPasivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaAdicionalPasivo = BigDecimal.ZERO;
			this.MontoATasaCompraPasivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaSistemaPasivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaAdicionalPasivo = BigDecimal.ZERO;
			this.Dividendo = BigDecimal.ZERO;
		}

		
		public String toString() {
			return Utils.printJson(this, false);
		}		

	}
	
	public static class CarteraSQL {
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		IdBoveda;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public Long 		IdInstrumento;
		public String 		Nemotecnico;
		public BigDecimal 	Disponible;
		public BigDecimal 	DisponibleTra = BigDecimal.ZERO;
		public String 		IdMonedaInstrumento;
		public String 		IdEmisor;
		public String		FechaEfectivaMovimiento;
		public BigDecimal 	PrecioPromedioCompra;
		public BigDecimal	PrecioMercado;
		public BigDecimal	INGMontoPrecioPromedioCompra;
		public BigDecimal	INGMontoPrecioPromedioMonedaAdicional;
		public BigDecimal 	INGMontoATasaCompraActual;
		public BigDecimal 	INGMontoATasaMercadoActual;
		public BigDecimal 	INGMontoMercadoActualMonedaAdicional;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class CarteraSQLDiv extends CarteraSQL{
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}

		
	public Salida_ConsultaActivoPasivo logica (Connection con, Entrada_ConsultaActivoPasivo entrada) throws Exception {
		logger.info("INICIO::ConsultarActivoPasivoCustodiaPortafolio");
		
		Salida_ConsultaActivoPasivo out = new Salida_ConsultaActivoPasivo();
		SQL sql=new SQL();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			
			String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
			Date dateFechaSistema = Fecha.toDate(fechaSistema);
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("IdEmpresa",  entrada.p_IdEmpresa);
			param.put("FechaHasta",  entrada.p_FechaHasta);
			param.put("IdTrabajo", idTrabajo);
			List<CarteraSQL> carterasActivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> carterasPasivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> garantiasActivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> prestamosActivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> prestamosPasivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> plazoActivos = new ArrayList<CarteraSQL>();
			List<CarteraSQL> plazoPasivos = new ArrayList<CarteraSQL>();
			List<CarteraSQLDiv> dividendos = new ArrayList<CarteraSQLDiv>();
			List<CarteraSQLDiv> dividendos_TBCEDVRV = new ArrayList<CarteraSQLDiv>();
			
			List<Portafolio> portafoliosBusqueda = entrada.p_Clientes.stream()
					.map(portafolio->{
						Portafolio portafolioBusqueda = new Portafolio();
						portafolioBusqueda.IdPortafolio = portafolio;
						portafolioBusqueda.IdTrabajo = idTrabajo;
						return portafolioBusqueda;
					})
					.collect(Collectors.toList());
			
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portafoliosBusqueda, Portafolio.class);
			
			String usaDividendosTBCEDVRV = "N";
			
			try	{usaDividendosTBCEDVRV		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.DividendosTBCEDVRV_TBCECCRF");} catch(Exception ex){logger.debug(ex);};
			
			if(entrada.p_FechaHasta.compareTo(dateFechaSistema) != -1) {
				carterasActivos = sql.query(con, SQL_SELECT_CARTERA_ACTIVO_ACTUAL, param, CarteraSQL.class);
				carterasPasivos = sql.query(con, SQL_SELECT_CARTERA_PASIVO_ACTUAL, param, CarteraSQL.class);
				garantiasActivos = sql.query(con, SQL_SELECT_GARANTIA_ACTIVO_ACTUAL, param, CarteraSQL.class);
				prestamosActivos = sql.query(con, SQL_SELECT_PRESTAMO_ACTIVO_ACTUAL, param, CarteraSQL.class);
				prestamosPasivos = sql.query(con, SQL_SELECT_PRESTAMO_PASIVO_ACTUAL, param, CarteraSQL.class);
				plazoActivos = sql.query(con, SQL_SELECT_PLAZO_ACTIVO_ACTUAL, param, CarteraSQL.class);
				plazoPasivos = sql.query(con, SQL_SELECT_PLAZO_PASIVO_ACTUAL, param, CarteraSQL.class);
				dividendos = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_ACTUAL, param, CarteraSQLDiv.class);
			}else {
				carterasActivos = sql.query(con, SQL_SELECT_CARTERA_ACTIVO_HISTORICO, param, CarteraSQL.class);
				carterasPasivos = sql.query(con, SQL_SELECT_CARTERA_PASIVO_HISTORICO, param, CarteraSQL.class);
				garantiasActivos = sql.query(con, SQL_SELECT_GARANTIA_ACTIVO_HISTORICO, param, CarteraSQL.class);
				prestamosActivos = sql.query(con, SQL_SELECT_PRESTAMO_ACTIVO_HISTORICO, param, CarteraSQL.class);
				prestamosPasivos = sql.query(con, SQL_SELECT_PRESTAMO_PASIVO_HISTORICO, param, CarteraSQL.class);
				plazoActivos = sql.query(con, SQL_SELECT_PLAZO_ACTIVO_HISTORICO, param, CarteraSQL.class);
				plazoPasivos = sql.query(con, SQL_SELECT_PLAZO_PASIVO_HISTORICO, param, CarteraSQL.class);
				dividendos = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_HISTORICO, param, CarteraSQLDiv.class);
			}
			
			if("S".equalsIgnoreCase(usaDividendosTBCEDVRV)) {
				String FechaGoLive	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaGoLive");
				param.remove("FechaDesde");
				param.put("FechaGoLive", FechaGoLive);

				if(entrada.p_FechaHasta.compareTo(dateFechaSistema) != -1) {
					dividendos_TBCEDVRV = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_ACTUAL, param, CarteraSQLDiv.class);
				}else {
					dividendos_TBCEDVRV = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_HISTORICO, param, CarteraSQLDiv.class);
				}
				dividendos.addAll(dividendos_TBCEDVRV);
			}
			
			HashMap<String,Cartera> hashCartera = new HashMap<String,Cartera>();
			//CARTERA LIBRE ACTIVOS
			for(CarteraSQL carteraSQL:carterasActivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,true, "DisponibleActivo");
				hashCartera.put(id, cartera);
			}
			
			//CARTERA LIBRE PASIVO
			for(CarteraSQL carteraSQL:carterasPasivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,false, "DisponiblePasivo");
				hashCartera.put(id, cartera);
			}
			
			//CARTERA LIBRE ACTIVOS TRANSITO
			for(CarteraSQL carteraSQL:carterasActivos) {
				if(carteraSQL.DisponibleTra.compareTo(new BigDecimal(0)) != 0) {
					String id = this.crearId(carteraSQL);
					Cartera cartera = hashCartera.get(id);
					cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,true, "DisponibleActivoTra");
					hashCartera.put(id, cartera);
				}
			}
			
			//CARTERA LIBRE PASIVO TRANSITO
			for(CarteraSQL carteraSQL:carterasPasivos) {
				if(carteraSQL.DisponibleTra.compareTo(new BigDecimal(0)) != 0) {
					String id = this.crearId(carteraSQL);
					Cartera cartera = hashCartera.get(id);
					cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,false, "DisponiblePasivoTra");
					hashCartera.put(id, cartera);
				}
			}
			
			//GARANTIA ACTIVO
			for(CarteraSQL carteraSQL:garantiasActivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,true, "GarantiaActivo");
				hashCartera.put(id, cartera);
			}
			
			//PRESTAMOS ACTIVO
			for(CarteraSQL carteraSQL:prestamosActivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,true, "PrestamoActivo");
				hashCartera.put(id, cartera);
			}
			
			//PRESTAMOS PASIVO
			for(CarteraSQL carteraSQL:prestamosPasivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,false, "PrestamoPasivo");
				hashCartera.put(id, cartera);
			}
			
			//PLAZO ACTIVO
			for(CarteraSQL carteraSQL: plazoActivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,false, "PlazoActivo");
				hashCartera.put(id, cartera);
			}
			
			//PLAZO PASIVO
			for(CarteraSQL carteraSQL:plazoPasivos) {
				String id = this.crearId(carteraSQL);
				Cartera cartera = hashCartera.get(id);
				cartera = this.setCartera(cartera, carteraSQL, true, entrada.p_FechaHasta.compareTo(dateFechaSistema) == -1,false, "PlazoPasivo");
				hashCartera.put(id, cartera);
			}
			
			//DIVIDENDOS			
			for(CarteraSQLDiv carteraSQLDiv:dividendos) {
				//CarteraSQL carterasql=(CarteraSQL) carteraSQLDiv;
				String id = this.crearId(carteraSQLDiv);
				Cartera cartera = hashCartera.get(id);
				
				if(cartera==null && id!=null) {
                    CalcularParidad calculoParidad= new CalcularParidad();
                    CalcularParidadEntrada in = new CalcularParidadEntrada();
                    in.p_IdEmpresa=entrada.p_IdEmpresa;
                    in.p_IdTrabajo=0L;
                    CalculoEntrada calculo = new CalculoEntrada();
                    
                    
                    calculo.IdMonedaOrigen = carteraSQLDiv.IdMonedaInstrumento; // 
                    
                    List<CarteraSQL> carteraInstrumentoDestino = carterasActivos.stream()
                            .filter(x -> x.IdPortafolio.equals(carteraSQLDiv.IdPortafolio))
                            .filter(x -> x.IdInstrumento.equals(carteraSQLDiv.IdInstrumento))
                            .filter(x -> x.IdBoveda.equalsIgnoreCase(carteraSQLDiv.IdBoveda))
                            .collect(Collectors.toList());
                    
                    carteraInstrumentoDestino.addAll(garantiasActivos.stream()
                            .filter(x -> x.IdPortafolio.equals(carteraSQLDiv.IdPortafolio))
                            .filter(x -> x.IdInstrumento.equals(carteraSQLDiv.IdInstrumento))
                            .filter(x -> x.IdBoveda.equalsIgnoreCase(carteraSQLDiv.IdBoveda))
                            .collect(Collectors.toList()));
                    
                    if(carteraInstrumentoDestino.size() > 0 ) {
                    	calculo.IdMonedaDestino = carteraInstrumentoDestino.get(0).IdMonedaInstrumento;
    					calculo.Fecha= carteraSQLDiv.FechaEfectivaMovimiento;
    					calculo.IdInterno=0L;
    					calculo.IdTrabajo = 0L;
    					calculo.IdUso = "";
    					calculo.MontoOrigen = carteraSQLDiv.Disponible;
    					calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
    					in.p_CalculosEntrada.add(calculo);
    					CalcularParidadSalida salidaCalculoParidad = calculoParidad.logica(con, in);
    					carteraSQLDiv.IdMonedaInstrumento=carteraInstrumentoDestino.get(0).IdMonedaInstrumento;
    					carteraSQLDiv.Disponible=salidaCalculoParidad.p_out_CalculosSalida.get(0).MontoDestino;
    					id = this.crearId(carteraSQLDiv);
    					cartera = hashCartera.get(id);
                    }
                }
				
				if(cartera != null) {
					cartera = this.setCartera(cartera, carteraSQLDiv, false, false,false, "Dividendo");
					hashCartera.put(id, cartera);
				}
				
			}
			
			for(Map.Entry<String, Cartera> obj : hashCartera.entrySet()) {
				out.p_Carteras.add(obj.getValue());
			}
		} catch (Exception e) {
			logger.error("ERROR::ConsultarActivoPasivoCustodiaPortafolio", e);
			throw e;
		} finally {
			Portafolio portafolio = new Portafolio();
			portafolio.IdTrabajo = idTrabajo;
			List<Portafolio> portafolios = Arrays.asList(portafolio);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, portafolios, Portafolio.class);
		}
		
		logger.info("FIN::ConsultarActivoPasivoCustodiaPortafolio");
		
		return out;
	}
	
	private Cartera crearCartera(CarteraSQL carteraSQL) {
		Cartera cartera = new Cartera();
		cartera.IdCliente = carteraSQL.IdCliente;
		cartera.IdPortafolio = carteraSQL.IdPortafolio;
		cartera.IdBoveda = carteraSQL.IdBoveda;
		cartera.IdClasificacionTipoInstrumento = carteraSQL.IdClasificacionTipoInstrumento;
		cartera.IdTipoInstrumento = carteraSQL.IdTipoInstrumento;
		cartera.IdInstrumento = carteraSQL.IdInstrumento;
		cartera.Nemotecnico = carteraSQL.Nemotecnico;
		cartera.IdEmisor = carteraSQL.IdEmisor;
		cartera.IdMonedaInstrumento = carteraSQL.IdMonedaInstrumento;
		cartera.PrecioPromedioCompra = carteraSQL.PrecioPromedioCompra != null ? carteraSQL.PrecioPromedioCompra : BigDecimal.ZERO;
		cartera.PrecioMercado = carteraSQL.PrecioMercado != null ? carteraSQL.PrecioMercado : BigDecimal.ZERO;
		return cartera;
	}
	
	private Cartera setCartera(Cartera cartera, CarteraSQL carteraSQL, Boolean sumaMontos, Boolean esMercado, Boolean esActivo, String tipo) {
		if(cartera == null) {
			cartera = this.crearCartera(carteraSQL);
		}
		if (tipo.equalsIgnoreCase("Dividendo")) {
			cartera.EsDividendo = "S";
		}else {
			cartera.EsDividendo = "N";
		}
		
		switch(tipo) {
		case "DisponibleActivo":
			cartera.DisponibleActivo = cartera.DisponibleActivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "DisponiblePasivo":
			cartera.DisponiblePasivo = cartera.DisponiblePasivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "DisponibleActivoTra":
			cartera.DisponibleActivoTra = cartera.DisponibleActivoTra.add(carteraSQL.DisponibleTra, MathContext.DECIMAL128);
			break;
		case "DisponiblePasivoTra":
			cartera.DisponiblePasivoTra = cartera.DisponiblePasivoTra.add(carteraSQL.DisponibleTra, MathContext.DECIMAL128);
			break;
		case "GarantiaActivo":
			cartera.GarantiaActivo = cartera.GarantiaActivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "PrestamoActivo":
			cartera.PrestamoActivo = cartera.PrestamoActivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "PrestamoPasivo":
			cartera.PrestamoPasivo = cartera.PrestamoPasivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "PlazoActivo":
			cartera.PlazoActivo = cartera.PlazoActivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
		break;
		case "PlazoPasivo":
			cartera.PlazoPasivo = cartera.PlazoPasivo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			break;
		case "Dividendo":
			cartera.Dividendo = cartera.Dividendo.add(carteraSQL.Disponible, MathContext.DECIMAL128);
			cartera.FechaEfectivaMovimiento = carteraSQL.FechaEfectivaMovimiento;
			break;
		}
		if(sumaMontos) {
			if(esActivo) {
				cartera.MontoPromedioCompraMonedaSistemaActivo = cartera.MontoPromedioCompraMonedaSistemaActivo.add(carteraSQL.INGMontoPrecioPromedioCompra, MathContext.DECIMAL128);
				cartera.MontoPromedioCompraMonedaAdicionalActivo = cartera.MontoPromedioCompraMonedaAdicionalActivo.add(carteraSQL.INGMontoPrecioPromedioMonedaAdicional, MathContext.DECIMAL128);
				cartera.MontoATasaCompraActivo = cartera.MontoATasaCompraActivo.add(carteraSQL.INGMontoATasaCompraActual, MathContext.DECIMAL128);
			}else {
				cartera.MontoPromedioCompraMonedaSistemaPasivo = cartera.MontoPromedioCompraMonedaSistemaPasivo.add(carteraSQL.INGMontoPrecioPromedioCompra, MathContext.DECIMAL128);
				cartera.MontoPromedioCompraMonedaAdicionalPasivo = cartera.MontoPromedioCompraMonedaAdicionalPasivo.add(carteraSQL.INGMontoPrecioPromedioMonedaAdicional, MathContext.DECIMAL128);
				cartera.MontoATasaCompraPasivo = cartera.MontoATasaCompraPasivo.add(carteraSQL.INGMontoATasaCompraActual, MathContext.DECIMAL128);
			}
			if(esMercado) {
				if(esActivo) {
					cartera.MontoMercadoMonedaSistemaActivo = cartera.MontoMercadoMonedaSistemaActivo.add(carteraSQL.INGMontoATasaMercadoActual, MathContext.DECIMAL128);
					cartera.MontoMercadoMonedaAdicionalActivo = cartera.MontoMercadoMonedaAdicionalActivo.add(carteraSQL.INGMontoMercadoActualMonedaAdicional, MathContext.DECIMAL128);
				}else {
					cartera.MontoMercadoMonedaSistemaPasivo = cartera.MontoMercadoMonedaSistemaPasivo.add(carteraSQL.INGMontoATasaMercadoActual, MathContext.DECIMAL128);
					cartera.MontoMercadoMonedaAdicionalPasivo = cartera.MontoMercadoMonedaAdicionalPasivo.add(carteraSQL.INGMontoMercadoActualMonedaAdicional, MathContext.DECIMAL128);
				}
				
			}
		}
		return cartera;
		
	}
	
	private String crearId(CarteraSQL carteraSQL) {
		return carteraSQL.IdPortafolio + "_" + carteraSQL.IdBoveda + "_" + carteraSQL.IdInstrumento + "_" + carteraSQL.IdMonedaInstrumento;
	}
	
	/**************************************************
	 * 					CALL 
	 * ************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultaActivoPasivo in=gson.fromJson(jin.toString() ,Entrada_ConsultaActivoPasivo.class);
		Salida_ConsultaActivoPasivo out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**************************************************
	 * 					MAIN 
	 * ************************************************/
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");
		try (Connection con =  PlataformaUtils.getQAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":100435,\"p_IdPortafolio\":100743,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":502,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_FechaConsulta\":\"2018-09-17\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":100822,\"p_IdPortafolio\":101517,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":271,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2023-07-13\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":377,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":18180,\"p_IdPortafolio\":24212,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":2042,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2019-02-11\"}";
			
			Entrada_ConsultaActivoPasivo entrada = gson.fromJson(json, Entrada_ConsultaActivoPasivo.class);
			ConsultarActivoPasivoCustodiaPortafolio demo = new ConsultarActivoPasivoCustodiaPortafolio();
			
			System.out.println(demo.logica(con, entrada));
			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}
}
