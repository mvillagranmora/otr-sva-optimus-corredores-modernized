package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.GenerarCartola.NominaPaso;
import cl.bcs.normativa.cartola.GenerarCartola.ServicioEjecutado;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.normativa.cartola.pojo.Boveda;
import cl.bcs.normativa.cartola.pojo.Instrumento;
import cl.bcs.normativa.cartola.pojo.InstrumentoValorClasificacionRiesgo;
import cl.bcs.normativa.cartola.pojo.Rubro;
import cl.bcs.normativa.cartola.pojo.TipoInstrumento;
import cl.bcs.normativa.cartola.pojo.TipoInstrumentoClasificacionRiesgo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraRF implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraRF");
	private final static Logger logger = LogManager.getLogger(CarteraRF.class);
	
	//FECHA ACTUAL
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesCartera.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesCarteraNegativa.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesGarantia.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesAPlazo.sql");
	private final String SQL_SELECT_MOVIMIENTOS_REPORTES = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesReportes.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectSorteoCuponDividendos.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_TBCECCRF = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectSorteoCuponDividendos_TBCECCRF.sql");
		
	//HISTORICOS
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesCarteraHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesCarteraNegativaHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesGarantiaHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesAPlazoHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_REPORTES_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectMovimientosVigentesReportesHistorico.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectSorteoCuponDividendosHistorico.sql");
	private final String SQL_SELECT_DIVIDENDOS_DINERO_TBCECCRF_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectSorteoCuponDividendos_TBCECCRFHistorico.sql");
		
	//GENERICOS
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumento.sql");
	private final String SQL_SELECT_BOVEDA = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectBoveda.sql");
	private final String SQL_SELECT_INSTRUMENTO_VALOR_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumentoValorClasificacionRiesgo.sql");
	private final String SQL_SELECT_RUBRO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectRubro.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumento.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumentoClasificacionRiesgo.sql");
	
	
	//PORTAFOLIOS
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	
private final String SQL_SELECT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectNominaPaso.sql");
	
	private final String SQL_SELECT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectServicioEjecutado.sql");
	private final String SQL_INSERT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertServicioEjecutado.sql");
	private final String SQL_INSERT_CARTERA_RF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_InsertOutServicioEjecutado.sql");
	private final String SQL_SELECT_CARTERA_RF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_SelectOUTServicioEjecutado.sql");
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioCartera;
		public Date FechaOperacion;
		public Date FechaLiquidacion;
		public String IdTipoInstrumento;
		public String IdBoveda;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal CantidadOriginal;
		public BigDecimal CantidadCartera;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadAPlazo;
		public BigDecimal CantidadReporte;
		public String IdMonedaInstrumento;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompraMonedaSistema;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaAdicional;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaSistema;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaAdicional;
		public String TieneCorteDeCupon;
		public Date FechaPagoCorteCupon;
		public BigDecimal CuponesPagadosMonedaSistema;
		public BigDecimal MontoCompraInicial;
		public String IdClasificacionTipoInstrumento;
		
		public Movimiento(){
			this.CantidadCartera = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadAPlazo = BigDecimal.ZERO;
			this.CantidadReporte = BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
			this.MontoCompraInicial = BigDecimal.ZERO;
			this.TasaMercado = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraRF{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioCartera;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String IdBoveda;
		public String CodigoBoveda;
		public String NombreBoveda;
		public String Ubicacion;
		public String UbicacionSolicitada;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String FechaVencimiento;
		public String FechaEmision;
		public String FechaPagoCorteCupon;
		public String PlazoResidual;
		public String Rubro;
		public String IdTipoReajuste;
		public String IdEmisor;
		public String RazonSocial;
		public BigDecimal CorteMinimo;
		public String TieneCupon;
		public BigDecimal TasaEmision;
		public BigDecimal CuponesPagadosMonedaSistema;
		public String ValorClasificacionRiesgo;
		public String IdMonedaInstrumento;
		public BigDecimal CantidadContable;
		public BigDecimal CantidadOriginal;
		public BigDecimal CantidadCartera;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadAPlazo;
		public BigDecimal CantidadReporte;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompraMonedaSistema;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaAdicional;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaSistema;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaAdicional;
		public String Moneda;
		public BigDecimal MontoMercado;
		public BigDecimal MontoInteresCorrido;
		public BigDecimal Rentabilidad;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente {
		public String Id;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdBoveda;
		public Long IdInstrumento;
		public BigDecimal Disponible;
		public BigDecimal InteresesPagados = BigDecimal.ZERO;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraRFPaso extends SalidaCarteraRF{
		public Long IdNomina;
		public Long IdTrabajo;
        
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		long inicioServicio = System.currentTimeMillis();
		log.write("INICIO::CarteraRF");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		NominaPaso nomina = new NominaPaso();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<NominaPaso> nominas = new ArrayList();
		Boolean primeraEjecucion = false;
		Long idTrabajoEjecutado = 0L;
		try {
			nominas = sql.query(con, SQL_SELECT_NOMINA_PASO, new HashMap<>(), NominaPaso.class);
		} catch (Exception e) {
			logger.info("INICIO::CarteraRF -ERROR SQL_SELECT_NOMINA_PASO "+e);
		}
		if(!nominas.isEmpty()) {
			nomina = nominas.get(0);
			if(nomina.IdNomina.compareTo(0L) != 0L) {
				String jeisonString = entrada.p_Datos.toString();
				String clase = this.getClass().getSimpleName();
				HashMap<String,Object> paramsJson = new HashMap<String,Object>();
				List<ServicioEjecutado> serviciosEjecutados = new ArrayList();
				List<SalidaCarteraRFPaso> outGuardado = new ArrayList();
				List<SalidaCarteraRF> listOutExistente = new ArrayList();
				paramsJson.put("IdNomina", nomina.IdNomina);
				paramsJson.put("Recurso", clase);
				try {
					serviciosEjecutados = sql.query(con, SQL_SELECT_SERVICIOS_EJECUTADOS, paramsJson, ServicioEjecutado.class);
				} catch (Exception e) {
					logger.info("INICIO::CarteraRF -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
				}
				
				if(serviciosEjecutados.isEmpty()) {
					List<ServicioEjecutado> portObj = new ArrayList<>();
					ServicioEjecutado se = new ServicioEjecutado();
					se.IdNomina = nomina.IdNomina;
					se.IdTrabajo = idTrabajo;
					se.Entrada = jeisonString;
					se.Recurso = clase;
					portObj.add(se);
					try {
						sql.batch(con, SQL_INSERT_SERVICIOS_EJECUTADOS, portObj, ServicioEjecutado.class);
					} catch (Exception e) {
						logger.info("INICIO::CarteraRF -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
					}
					primeraEjecucion = true;
				}
				else
				{
					HashMap<String,Object> paramsOut = new HashMap<String,Object>();
					paramsOut.put("IdNomina", nomina.IdNomina);
					try {
						outGuardado = sql.query(con, SQL_SELECT_CARTERA_RF_OUT,paramsOut, SalidaCarteraRFPaso.class);
					} catch (Exception e) {
						logger.info("INICIO::CarteraRF -ERROR SQL_SELECT_OUT "+e);
					}
					for(ServicioEjecutado se : serviciosEjecutados) {
						if(se.Entrada.equals(jeisonString) && se.IdNomina.compareTo(nomina.IdNomina) == 0L && se.Recurso.equals(clase)) {
							idTrabajoEjecutado = se.IdTrabajo;
							primeraEjecucion = false;
							break;
						}
						else {
							primeraEjecucion = true;
						}
					}
					if(primeraEjecucion == false) {
						
						listOutExistente = parseaObjetoDePaso(outGuardado,idTrabajoEjecutado);
						if(listOutExistente.size() > 0) {
							List<HashMap<String,Object>> resultadoCarteraRF = Utils.List2ListHashGenericObject(listOutExistente);
							
							out.P_OUT_SALIDA.put("carteraRF", resultadoCarteraRF);
							
							log.write("FIN::CarteraRF");
							return out;
						}
					}
				}
			}
			else {
				primeraEjecucion = false;
			}
		}
		
		String monedaAdicional = null;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String usaDividendosTBCECCRF = "N";
		
		try	{usaDividendosTBCECCRF		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.DividendosTBCEDVRV_TBCECCRF");} catch(Exception ex){log.write(ex);};
		
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		List<Long> portafolios = new ArrayList<Long>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<Movimiento> movimientos = new ArrayList<Movimiento>();
		List<EventoCapitalCliente> eventosCapitalCliente = new ArrayList<EventoCapitalCliente>();
		List<EventoCapitalCliente> eventosCapitalCliente_TBCECCRF = new ArrayList<EventoCapitalCliente>();
		
		
		List<Portafolio> portObj = new ArrayList<>();
		for (Long p : portafolios) {
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = p;
			portObj.add(por);
		}
		
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
		
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_REPORTES, params, Movimiento.class));
			eventosCapitalCliente = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO, params, EventoCapitalCliente.class);
		}else{
			
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_REPORTES_HISTORICO, params, Movimiento.class));
			eventosCapitalCliente = sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_HISTORICO, params, EventoCapitalCliente.class);
			

		}
		
		HashMap<String,Object> hashEventosCapitalCliente = Utils.List2Hash(eventosCapitalCliente, "Id");
		
		if("S".equalsIgnoreCase(usaDividendosTBCECCRF)) {
			String FechaGoLive	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaGoLive");
			params.put("FechaGoLive", FechaGoLive);

			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				eventosCapitalCliente_TBCECCRF= sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_TBCECCRF, params, EventoCapitalCliente.class);	
			}else {
				eventosCapitalCliente_TBCECCRF= sql.query(con, SQL_SELECT_DIVIDENDOS_DINERO_TBCECCRF_HISTORICO, params, EventoCapitalCliente.class);
			}
			
			for(EventoCapitalCliente eventoTBCECCRF:  eventosCapitalCliente_TBCECCRF) {
				if(hashEventosCapitalCliente.containsKey(eventoTBCECCRF.Id)){
					EventoCapitalCliente registroEvento = (EventoCapitalCliente) hashEventosCapitalCliente.get(eventoTBCECCRF.Id);
					registroEvento.Disponible = registroEvento.Disponible.add(eventoTBCECCRF.Disponible,  MathContext.DECIMAL128);
					registroEvento.InteresesPagados = registroEvento.InteresesPagados.add(eventoTBCECCRF.InteresesPagados,  MathContext.DECIMAL128);
				}else {
					hashEventosCapitalCliente.put(eventoTBCECCRF.Id, eventoTBCECCRF);
				}
			}
		}
		
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		List<Long> idsInstrumentos = new ArrayList<Long>();
		List<String> idsBovedas = new ArrayList<String>();
		List<String> idsEntidadClasificadoraRiesgo = new ArrayList<String>();
		List<Long> codigosRubros = new ArrayList<Long>();
		List<String> idsTipoInstrumento = new ArrayList<String>();
		
		//RECORREMOS PARA SACAR LOS IDS DE BUSQUEDA
		for(Movimiento mov : movimientos){
			if(!idsInstrumentos.contains(mov.IdInstrumento)) idsInstrumentos.add(mov.IdInstrumento);
			if(!idsBovedas.contains(mov.IdBoveda)) idsBovedas.add(mov.IdBoveda);
			if(!idsTipoInstrumento.contains(mov.IdTipoInstrumento)) idsTipoInstrumento.add(mov.IdTipoInstrumento);
		}
		
		params.clear();
		params.put("IdInstrumento", idsInstrumentos);
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		params.clear();
		params.put("IdBoveda", idsBovedas);
		List<Boveda> bovedas = sql.query(con, SQL_SELECT_BOVEDA, params, Boveda.class);
		params.clear();
		params.put("IdTipoInstrumento", idsTipoInstrumento);
		List<TipoInstrumento> tiposInstrumentos = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params, TipoInstrumento.class);
		List<TipoInstrumentoClasificacionRiesgo> tiposInstrumentosClasificacionRiesgo = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO, params, TipoInstrumentoClasificacionRiesgo.class);
		
		//RECORREMOS LOS INSTRUMENTOS PARA SACAR RUBROS
		for(Instrumento ins : instrumentos){
			if(!codigosRubros.contains(ins.CodigoRubro)) codigosRubros.add(ins.CodigoRubro);
		}
		
		//RECORREMOS LOS CLASIFICACION RIESGO PARA SACAR ENTIDADES
		for(TipoInstrumentoClasificacionRiesgo tip : tiposInstrumentosClasificacionRiesgo){
			if(!idsEntidadClasificadoraRiesgo.contains(tip.IdEntidadClasificadoraRiesgo)) idsEntidadClasificadoraRiesgo.add(tip.IdEntidadClasificadoraRiesgo);
		}
		
		params.clear();
		params.put("CodigoRubro",codigosRubros);
		List<Rubro> rubros = sql.query(con, SQL_SELECT_RUBRO, params, Rubro.class);
		params.clear();
		params.put("IdInstrumento", idsInstrumentos);
		params.put("IdEntidadClasificadoraRiesgo", idsEntidadClasificadoraRiesgo);
		List<InstrumentoValorClasificacionRiesgo> instrumentosValorClasificacionRiesgo = sql.query(con, SQL_SELECT_INSTRUMENTO_VALOR_CLASIFICACION_RIESGO, params, InstrumentoValorClasificacionRiesgo.class);
		
		//PASAMOS A HASHMAP LAS LISTAS
		HashMap<Long,Object> hInstrumentos = Utils.List2Hash(instrumentos, "IdInstrumento");
		HashMap<String,Object> hBovedas = Utils.List2Hash(bovedas, "IdBoveda");
		HashMap<String,Object> hTiposInstrumentos = Utils.List2Hash(tiposInstrumentos, "IdTipoInstrumento");
		HashMap<String,Object> hTiposInstrumentosClasificacionRiesgo = Utils.List2Hash(tiposInstrumentosClasificacionRiesgo, "IdTipoInstrumento");
		HashMap<Long,Object> hCodigosRubros = Utils.List2Hash(rubros, "CodigoRubro");
		HashMap<String,Object> hInstrumentosValorClasificacionRiesgo = Utils.List2Hash(instrumentosValorClasificacionRiesgo, "Id");
		
		SimpleDateFormat formatoStringDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatoDateString = new SimpleDateFormat("dd-MM-yyyy");
		
		List<SalidaCarteraRF> salidas = new ArrayList<SalidaCarteraRF>();
		
		Map<String, List<Movimiento>> movAgrupados = movimientos.stream()
			    .collect(Collectors.groupingBy(mov -> mov.IdCliente + "_" + mov.IdPortafolio + "_" + mov.Nemotecnico + "_" + mov.TasaCompra + "_" + mov.PrecioCompra));

		List<Movimiento> movimientosFiltrados = movAgrupados.values().stream()
		    .map(lista -> lista.stream().reduce((movimiento1, movimiento2) -> {
		    	Movimiento base = (movimiento1.CantidadOriginal == null) ? movimiento2 : (movimiento2.CantidadOriginal == null) ? movimiento1 : (movimiento1.CantidadOriginal.compareTo(movimiento2.CantidadOriginal) >= 0 ? movimiento1 : movimiento2);
		    	base.FechaOperacion = movimiento1.FechaOperacion.after(movimiento2.FechaOperacion) ? movimiento1.FechaOperacion : movimiento2.FechaOperacion;
		    	base.FechaLiquidacion = movimiento1.FechaLiquidacion.after(movimiento2.FechaLiquidacion) ? movimiento1.FechaLiquidacion : movimiento2.FechaLiquidacion;

		    	BigDecimal cantidadPlazo1 = controlDeNull(movimiento1.CantidadAPlazo);
		    	BigDecimal cantidadPlazo2 = controlDeNull(movimiento2.CantidadAPlazo);
		    	base.CantidadAPlazo = cantidadPlazo1.add(cantidadPlazo2);
		    	
		    	BigDecimal cantidadGarantia1 = controlDeNull(movimiento1.CantidadGarantia);
		    	BigDecimal cantidadGarantia2 = controlDeNull(movimiento2.CantidadGarantia);
		    	base.CantidadGarantia = cantidadGarantia1.add(cantidadGarantia2);
		    	
		    	BigDecimal cantidadCartera1 = controlDeNull(movimiento1.CantidadCartera);
		    	BigDecimal cantidadCartera2 = controlDeNull(movimiento2.CantidadCartera);
		    	base.CantidadCartera = cantidadCartera1.add(cantidadCartera2);
		    	
		    	BigDecimal montoCompraMonedaSistema1 = controlDeNull(movimiento1.MontoCompraMonedaSistema);
		    	BigDecimal montoCompraMonedaSistema2 = controlDeNull(movimiento2.MontoCompraMonedaSistema);
		    	base.MontoCompraMonedaSistema = montoCompraMonedaSistema1.add(montoCompraMonedaSistema2);
		    	
		    	BigDecimal montoMercadoMonedaAdicional1 = controlDeNull(movimiento1.MontoMercadoMonedaAdicional);
		    	BigDecimal montoMercadoMonedaAdicional2 = controlDeNull(movimiento2.MontoMercadoMonedaAdicional);
		    	base.MontoMercadoMonedaAdicional = montoMercadoMonedaAdicional1.add(montoMercadoMonedaAdicional2);
		    	
		    	BigDecimal montoMercadoMonedaSistema1 = controlDeNull(movimiento1.MontoMercadoMonedaSistema);
		    	BigDecimal montoMercadoMonedaSistema2 = controlDeNull(movimiento2.MontoMercadoMonedaSistema);
		    	base.MontoMercadoMonedaSistema = montoMercadoMonedaSistema1.add(montoMercadoMonedaSistema2);
		    	
		    	BigDecimal montoCompreInicial1 = controlDeNull(movimiento1.MontoCompraInicial);
		    	BigDecimal montoCompreInicial2 = controlDeNull(movimiento2.MontoCompraInicial);
		    	base.MontoCompraInicial = montoCompreInicial1.add(montoCompreInicial2);
		        
		    	return base;
			    }).orElse(null))
				 .filter(Objects::nonNull)
				 .collect(Collectors.toList());

		
		for(Movimiento mov : movimientosFiltrados){
			Instrumento instrumento = (Instrumento) hInstrumentos.get(mov.IdInstrumento);
			Boveda boveda = (Boveda) hBovedas.get(mov.IdBoveda);
			TipoInstrumento tipoInstrumento = (TipoInstrumento) hTiposInstrumentos.get(mov.IdTipoInstrumento);
			TipoInstrumentoClasificacionRiesgo tipoInstrumentoClasificacionRiesgo = (TipoInstrumentoClasificacionRiesgo) hTiposInstrumentosClasificacionRiesgo.get(mov.IdTipoInstrumento);
			
			Rubro rubro = instrumento == null ? null : (Rubro) hCodigosRubros.get(instrumento.CodigoRubro);
			InstrumentoValorClasificacionRiesgo instrumentoValorClasificacionRiesgo = tipoInstrumentoClasificacionRiesgo == null ? null : (InstrumentoValorClasificacionRiesgo) hInstrumentosValorClasificacionRiesgo.get(tipoInstrumentoClasificacionRiesgo.IdEntidadClasificadoraRiesgo + "_" + mov.IdInstrumento);
			
			if(instrumento != null && tipoInstrumento != null && ((boveda != null && entrada.p_FiltroUbicacionBoveda.equals(boveda.Ubicacion)) || entrada.p_FiltroUbicacionBoveda.isEmpty())){
				BigDecimal cuponesPagados = BigDecimal.ZERO;
				BigDecimal interesesPagados = BigDecimal.ZERO;
				
				String id = mov.FolioCartera + "_" + mov.IdCliente + "_" + mov.IdPortafolio + "_" + mov.IdInstrumento + "_" + monedaSistema;
				
				EventoCapitalCliente eventoCapitalCliente = (EventoCapitalCliente) hashEventosCapitalCliente.get(id);
				
				if(eventoCapitalCliente != null) {
					cuponesPagados = eventoCapitalCliente.Disponible;
					interesesPagados = eventoCapitalCliente.InteresesPagados;
				}
				
				SalidaCarteraRF salidaCarteraRF = new SalidaCarteraRF();
				salidaCarteraRF.IdCliente = mov.IdCliente;
				salidaCarteraRF.IdPortafolio = mov.IdPortafolio;
				salidaCarteraRF.FolioCartera = mov.FolioCartera;
				salidaCarteraRF.FechaOperacion = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaOperacion)));
				salidaCarteraRF.FechaLiquidacion = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaLiquidacion)));
				salidaCarteraRF.IdBoveda = mov.IdBoveda;
				salidaCarteraRF.UbicacionSolicitada = entrada.p_FiltroUbicacionBoveda;
				if(boveda != null){
					salidaCarteraRF.CodigoBoveda = boveda.IdBoveda;
					salidaCarteraRF.NombreBoveda = boveda.Nombre;
					salidaCarteraRF.Ubicacion = boveda.Ubicacion;
					salidaCarteraRF.UbicacionSolicitada = entrada.p_FiltroUbicacionBoveda.isEmpty() ? boveda.Ubicacion : salidaCarteraRF.UbicacionSolicitada;
				}
				salidaCarteraRF.IdTipoInstrumento = mov.IdTipoInstrumento;
				salidaCarteraRF.NombreTipoInstrumento = tipoInstrumento.Nombre;
				salidaCarteraRF.IdInstrumento = mov.IdInstrumento;
				salidaCarteraRF.Nemotecnico = mov.Nemotecnico;
				salidaCarteraRF.FechaVencimiento = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(instrumento.FechaVencimiento)));
				salidaCarteraRF.FechaEmision = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(instrumento.FechaEmision)));
				salidaCarteraRF.FechaPagoCorteCupon = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaPagoCorteCupon)));
				salidaCarteraRF.PlazoResidual = (Long.valueOf(Fecha.stringToSqlDate(instrumento.FechaVencimiento).split("-")[0]) - Long.valueOf(Fecha.stringToSqlDate(entrada.p_FechaHasta).split("-")[0])) + "/00";
				if(rubro != null) salidaCarteraRF.Rubro = rubro.Descripcion;
				salidaCarteraRF.IdTipoReajuste = instrumento.IdTipoReajuste;
				salidaCarteraRF.IdEmisor = instrumento.IdEmisor;
				salidaCarteraRF.RazonSocial = instrumento.RazonSocial;
				salidaCarteraRF.CorteMinimo = instrumento.CorteMinimo;
				salidaCarteraRF.TieneCupon = "N".equals(instrumento.TieneCupon) ? "N/A" : instrumento.TieneCupon;
				salidaCarteraRF.TasaEmision = instrumento.Tasa;
				salidaCarteraRF.CuponesPagadosMonedaSistema = cuponesPagados;
				salidaCarteraRF.ValorClasificacionRiesgo = instrumentoValorClasificacionRiesgo == null ? "" : instrumentoValorClasificacionRiesgo.ValorClasificacionRiesgo;
				salidaCarteraRF.IdMonedaInstrumento  = mov.IdMonedaInstrumento;
				salidaCarteraRF.CantidadContable = instrumento.CorteMinimo.longValue() > 0 ? mov.CantidadOriginal.divide(instrumento.CorteMinimo, MathContext.DECIMAL128) : mov.CantidadOriginal;
				salidaCarteraRF.CantidadOriginal = mov.CantidadOriginal;
				salidaCarteraRF.CantidadCartera = mov.CantidadCartera;
				salidaCarteraRF.CantidadGarantia = mov.CantidadGarantia;
				salidaCarteraRF.CantidadAPlazo = mov.CantidadAPlazo;
				salidaCarteraRF.CantidadReporte = mov.CantidadReporte;
				salidaCarteraRF.TasaCompra = mov.TasaCompra;
				salidaCarteraRF.PrecioCompra = mov.PrecioCompra;
				salidaCarteraRF.MontoCompraMonedaSistema = mov.MontoCompraMonedaSistema;
				salidaCarteraRF.TasaMercado = mov.TasaMercado;
				salidaCarteraRF.PrecioMercado = mov.PrecioMercado;
				salidaCarteraRF.MontoMercadoMonedaSistema = mov.MontoMercadoMonedaSistema;
				salidaCarteraRF.MontoMercadoMonedaAdicional = mov.MontoMercadoMonedaAdicional;
				salidaCarteraRF.MontoInteresCorridoMercadoActualMonedaSistema = mov.MontoInteresCorridoMercadoActualMonedaSistema;
				salidaCarteraRF.MontoInteresCorridoMercadoActualMonedaAdicional = mov.MontoInteresCorridoMercadoActualMonedaAdicional;
				if(monedaSistema.equals(mov.IdMonedaInstrumento) || (!monedaSistema.equals(mov.IdMonedaInstrumento) && "S".equals(entrada.p_FiltroSoloMonedaSistema))){
					salidaCarteraRF.Moneda = monedaSistema;
					salidaCarteraRF.MontoMercado = mov.MontoMercadoMonedaSistema;
					salidaCarteraRF.MontoInteresCorrido = mov.MontoInteresCorridoMercadoActualMonedaSistema;
				} else {
					salidaCarteraRF.Moneda = monedaAdicional;
					salidaCarteraRF.MontoMercado = mov.MontoMercadoMonedaAdicional;
					salidaCarteraRF.MontoInteresCorrido = mov.MontoInteresCorridoMercadoActualMonedaAdicional;
				}
				salidaCarteraRF.Rentabilidad = (mov.MontoCompraInicial.compareTo(BigDecimal.ZERO) != 0 ? mov.MontoMercadoMonedaSistema.subtract(mov.MontoCompraInicial, MathContext.DECIMAL128).add(cuponesPagados,  MathContext.DECIMAL128).divide(mov.MontoCompraInicial, MathContext.DECIMAL128) : BigDecimal.ZERO).multiply(new BigDecimal("100"), MathContext.DECIMAL128);
				salidaCarteraRF.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
				
				salidas.add(salidaCarteraRF);
				
			}
			
		}
		if(primeraEjecucion) {
			try {
				List<SalidaCarteraRFPaso> listOutPaso =  new ArrayList();
				for(SalidaCarteraRF salida : salidas) {
					SalidaCarteraRFPaso outPaso = new SalidaCarteraRFPaso();
					outPaso.IdNomina = nomina.IdNomina;
					outPaso.IdTrabajo = idTrabajo;
					outPaso.IdCliente = salida.IdCliente;
					outPaso.IdPortafolio = salida.IdPortafolio;
					outPaso.IdCliente = salida.IdCliente;
					outPaso.IdPortafolio = salida.IdPortafolio;
					outPaso.FolioCartera = salida.FolioCartera;
					outPaso.FechaOperacion = salida.FechaOperacion;
					outPaso.FechaLiquidacion = salida.FechaLiquidacion;
					outPaso.IdBoveda = salida.IdBoveda;
					outPaso.CodigoBoveda = salida.CodigoBoveda;
					outPaso.NombreBoveda = salida.NombreBoveda;
					outPaso.Ubicacion = salida.Ubicacion;
					outPaso.UbicacionSolicitada = salida.UbicacionSolicitada;
					outPaso.IdTipoInstrumento = salida.IdTipoInstrumento;
					outPaso.NombreTipoInstrumento = salida.NombreTipoInstrumento;
					outPaso.IdInstrumento = salida.IdInstrumento;
					outPaso.Nemotecnico = salida.Nemotecnico;
					outPaso.FechaVencimiento = salida.FechaVencimiento;
					outPaso.FechaEmision = salida.FechaEmision;
					outPaso.FechaPagoCorteCupon = salida.FechaPagoCorteCupon;
					outPaso.PlazoResidual = salida.PlazoResidual;
					outPaso.Rubro = salida.Rubro;
					outPaso.IdTipoReajuste = salida.IdTipoReajuste;
					outPaso.IdEmisor = salida.IdEmisor;
					outPaso.RazonSocial = salida.RazonSocial;
					outPaso.CorteMinimo = salida.CorteMinimo;
					outPaso.TieneCupon = salida.TieneCupon;
					outPaso.TasaEmision = salida.TasaEmision;
					outPaso.CuponesPagadosMonedaSistema = salida.CuponesPagadosMonedaSistema;
					outPaso.ValorClasificacionRiesgo = salida.ValorClasificacionRiesgo;
					outPaso.IdMonedaInstrumento = salida.IdMonedaInstrumento;
					outPaso.CantidadContable = salida.CantidadContable;
					outPaso.CantidadOriginal = salida.CantidadOriginal;
					outPaso.CantidadCartera = salida.CantidadCartera;
					outPaso.CantidadGarantia = salida.CantidadGarantia;
					outPaso.CantidadAPlazo = salida.CantidadAPlazo;
					outPaso.CantidadReporte = salida.CantidadReporte;
					outPaso.TasaCompra = salida.TasaCompra;
					outPaso.PrecioCompra = salida.PrecioCompra;
					outPaso.MontoCompraMonedaSistema = salida.MontoCompraMonedaSistema;
					outPaso.TasaMercado = salida.TasaMercado;
					outPaso.PrecioMercado = salida.PrecioMercado;
					outPaso.MontoMercadoMonedaSistema = salida.MontoMercadoMonedaSistema;
					outPaso.MontoMercadoMonedaAdicional = salida.MontoMercadoMonedaAdicional;
					outPaso.MontoInteresCorridoMercadoActualMonedaSistema = salida.MontoInteresCorridoMercadoActualMonedaSistema;
					outPaso.MontoInteresCorridoMercadoActualMonedaAdicional = salida.MontoInteresCorridoMercadoActualMonedaAdicional;
					outPaso.Moneda = salida.Moneda;
					outPaso.MontoMercado = salida.MontoMercado;
					outPaso.MontoInteresCorrido = salida.MontoInteresCorrido;
					outPaso.Rentabilidad = salida.Rentabilidad;
					outPaso.IdClasificacionTipoInstrumento = salida.IdClasificacionTipoInstrumento;
					listOutPaso.add(outPaso);
				}
				sql.batch(con, SQL_INSERT_CARTERA_RF_OUT, listOutPaso, SalidaCarteraRFPaso.class);
			} catch (Exception e) {
				logger.info("INICIO::Cartera RF -ERROR SQL_INSERT_CARTERA_FM_OUT "+e);
			}
		}
		
		List<HashMap<String,Object>> resultadoCarteraRF = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("carteraRF", resultadoCarteraRF);
		
		long finServicio = System.currentTimeMillis();
		log.write("FIN::CarteraRF : " + (finServicio - inicioServicio));
		return out;
	}
	
	private List<SalidaCarteraRF> parseaObjetoDePaso(List<SalidaCarteraRFPaso> listObjPaso,Long idTrabajo){
		List<SalidaCarteraRF> listSalidaCartera = new ArrayList<>();
		for(SalidaCarteraRFPaso p : listObjPaso) {
			if(p.IdTrabajo.compareTo(idTrabajo) == 0L) {
				SalidaCarteraRF outExistente = new SalidaCarteraRF();
				outExistente.IdCliente = p.IdCliente;
				outExistente.IdPortafolio = p.IdPortafolio;
				outExistente.IdCliente = p.IdCliente;
				outExistente.IdPortafolio = p.IdPortafolio;
				outExistente.FolioCartera = p.FolioCartera;
				outExistente.FechaOperacion = p.FechaOperacion;
				outExistente.FechaLiquidacion = p.FechaLiquidacion;
				outExistente.IdBoveda = p.IdBoveda;
				outExistente.CodigoBoveda = p.CodigoBoveda;
				outExistente.NombreBoveda = p.NombreBoveda;
				outExistente.Ubicacion = p.Ubicacion;
				outExistente.UbicacionSolicitada = p.UbicacionSolicitada;
				outExistente.IdTipoInstrumento = p.IdTipoInstrumento;
				outExistente.NombreTipoInstrumento = p.NombreTipoInstrumento;
				outExistente.IdInstrumento = p.IdInstrumento;
				outExistente.Nemotecnico = p.Nemotecnico;
				outExistente.FechaVencimiento = p.FechaVencimiento;
				outExistente.FechaEmision = p.FechaEmision;
				outExistente.FechaPagoCorteCupon = p.FechaPagoCorteCupon;
				outExistente.PlazoResidual = p.PlazoResidual;
				outExistente.Rubro = p.Rubro;
				outExistente.IdTipoReajuste = p.IdTipoReajuste;
				outExistente.IdEmisor = p.IdEmisor;
				outExistente.RazonSocial = p.RazonSocial;
				outExistente.CorteMinimo = p.CorteMinimo;
				outExistente.TieneCupon = p.TieneCupon;
				outExistente.TasaEmision = p.TasaEmision;
				outExistente.CuponesPagadosMonedaSistema = p.CuponesPagadosMonedaSistema;
				outExistente.ValorClasificacionRiesgo = p.ValorClasificacionRiesgo;
				outExistente.IdMonedaInstrumento = p.IdMonedaInstrumento;
				outExistente.CantidadContable = p.CantidadContable;
				outExistente.CantidadOriginal = p.CantidadOriginal;
				outExistente.CantidadCartera = p.CantidadCartera;
				outExistente.CantidadGarantia = p.CantidadGarantia;
				outExistente.CantidadAPlazo = p.CantidadAPlazo;
				outExistente.CantidadReporte = p.CantidadReporte;
				outExistente.TasaCompra = p.TasaCompra;
				outExistente.PrecioCompra = p.PrecioCompra;
				outExistente.MontoCompraMonedaSistema = p.MontoCompraMonedaSistema;
				outExistente.TasaMercado = p.TasaMercado;
				outExistente.PrecioMercado = p.PrecioMercado;
				outExistente.MontoMercadoMonedaSistema = p.MontoMercadoMonedaSistema;
				outExistente.MontoMercadoMonedaAdicional = p.MontoMercadoMonedaAdicional;
				outExistente.MontoInteresCorridoMercadoActualMonedaSistema = p.MontoInteresCorridoMercadoActualMonedaSistema;
				outExistente.MontoInteresCorridoMercadoActualMonedaAdicional = p.MontoInteresCorridoMercadoActualMonedaAdicional;
				outExistente.Moneda = p.Moneda;
				outExistente.MontoMercado = p.MontoMercado;
				outExistente.MontoInteresCorrido = p.MontoInteresCorrido;
				outExistente.Rentabilidad = p.Rentabilidad;
				outExistente.IdClasificacionTipoInstrumento = p.IdClasificacionTipoInstrumento;
				listSalidaCartera.add(outExistente);
			}
		}
		return listSalidaCartera;
	}
	
	private static BigDecimal controlDeNull(BigDecimal valor) {
	    return valor != null ? valor : BigDecimal.ZERO;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIConnection()) {		
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 288L;
		Objin.p_FechaDesde = Fecha.toDate("2023-09-01");
		Objin.p_FechaHasta = Fecha.toDate("2023-09-28");
		Dato dato = new Dato();
		dato.IdCliente =4841L;
		dato.IdPortafolio =5943L;
		Objin.p_Datos.add(dato);
		CarteraRF  demo= new CarteraRF();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
