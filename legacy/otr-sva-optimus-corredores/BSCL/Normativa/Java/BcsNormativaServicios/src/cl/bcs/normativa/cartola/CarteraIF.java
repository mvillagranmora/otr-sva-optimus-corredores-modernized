package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.GenerarCartola.NominaPaso;
import cl.bcs.normativa.cartola.GenerarCartola.ServicioEjecutado;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.normativa.cartola.pojo.Emisor;
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

public class CarteraIF implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraIF");
	private final static Logger logger = LogManager.getLogger(CarteraIF.class);
	
	//FECHA ACTUAL
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesCartera.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesCarteraNegativa.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesGarantia.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesAPlazo.sql");
	
	//FECHA HISTORICO
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesCarteraHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesCarteraNegativaHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesGarantiaHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectMovimientosVigentesAPlazoHistorico.sql");
	
	//GENERICOS
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumento.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumento.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumentoClasificacionRiesgo.sql");
	private final String SQL_SELECT_INSTRUMENTO_VALOR_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumentoValorClasificacionRiesgo.sql");
	private final String SQL_SELECT_RUBRO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectRubro.sql");
	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectEmisor.sql");
	
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
private final String SQL_SELECT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectNominaPaso.sql");
	
	private final String SQL_SELECT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectServicioEjecutado.sql");
	private final String SQL_INSERT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertServicioEjecutado.sql");
	private final String SQL_INSERT_CARTERA_IF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_InsertOutServicioEjecutado.sql");
	private final String SQL_SELECT_CARTERA_IF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_SelectOUTServicioEjecutado.sql");
	
	
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioCartera;
		public Date FechaOperacion;
		public Date FechaLiquidacion;
		public String IdBoveda;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String IdEmisor;
		public Date FechaVencimientoInstrumento;
		public String IdTipoReajuste;
		public Date FechaEmisionInstrumento;
		public String Nemotecnico;
		public BigDecimal CantidadOriginal;
		public BigDecimal ValorRescateOriginal;
		public BigDecimal CantidadCartera;
		public BigDecimal ValorRescateCartera;
		public BigDecimal CantidadGarantia;
		public BigDecimal ValorRescateGarantia;
		public BigDecimal CantidadAPlazo;
		public BigDecimal ValorRescateAPlazo;
		public String IdMonedaInstrumento;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompraMonedaSistema;
		public BigDecimal MontoCompraMonedaAdicional;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaAdicional;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaSistema;
		public BigDecimal MontoInteresCorridoMercadoActualMonedaAdicional;
		public String TieneCorteDeCupon;
		public Date FechaPagoCorteCupon;
		public BigDecimal CuponesPagadosMonedaSistema;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal MontoMercadoGarantiaActual;
		public BigDecimal MontoGarantiaValorizadoActual;
		
		public Movimiento(){
			this.CantidadCartera = BigDecimal.ZERO;
			this.ValorRescateCartera = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.ValorRescateGarantia = BigDecimal.ZERO;
			this.CantidadAPlazo = BigDecimal.ZERO;
			this.ValorRescateAPlazo = BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
			this.TasaMercado = BigDecimal.ZERO;
			this.IdClasificacionTipoInstrumento = "IF";
			this.MontoMercadoGarantiaActual = BigDecimal.ZERO;
			this.MontoGarantiaValorizadoActual = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraIF{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioCartera;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String IdBoveda;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String IdEmisor;
		public String Emisor;
		public String Nemotecnico;
		public String FechaVencimiento;
		public String FechaEmision;
		public String FechaPagoCorteCupon;
		public String PlazoResidual;
		public String Rubro;
		public String IdTipoReajuste;
		public String RazonSocial;
		public String TieneCupon;
		public BigDecimal CuponesPagadosMonedaSistema;
		public String ValorClasificacionRiesgo;
		public String IdMonedaInstrumento;
		public BigDecimal CantidadContable;
		public BigDecimal CantidadOriginal;
		public BigDecimal CantidadCartera;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadAPlazo;
		public BigDecimal CantidadTotal;
		public BigDecimal ValorRescateOriginal;
		public BigDecimal ValorRescateCartera;
		public BigDecimal ValorRescateGarantia;
		public BigDecimal ValorRescateAPlazo;
		public BigDecimal ValorRescateTotal;
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
		public BigDecimal MontoCompra;
		public BigDecimal MontoMercado;
		public BigDecimal MontoInteresCorrido;
		public BigDecimal Rentabilidad;
		public String Mercado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraIFPaso extends SalidaCarteraIF{
		public Long IdNomina;
		public Long IdTrabajo;
        
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		long inicioServicio = System.currentTimeMillis();
		log.write("INICIO::CarteraIF");
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
			logger.info("INICIO::CarteraIF -ERROR SQL_SELECT_NOMINA_PASO "+e);
		}
		if(!nominas.isEmpty()) {
			nomina = nominas.get(0);
			if(nomina.IdNomina.compareTo(0L) != 0L) {
				String jeisonString = entrada.p_Datos.toString();
				String clase = this.getClass().getSimpleName();
				HashMap<String,Object> paramsJson = new HashMap<String,Object>();
				List<ServicioEjecutado> serviciosEjecutados = new ArrayList();
				List<SalidaCarteraIFPaso> outGuardado = new ArrayList();
				List<SalidaCarteraIF> listOutExistente = new ArrayList();
				paramsJson.put("IdNomina", nomina.IdNomina);
				paramsJson.put("Recurso", clase);
				try {
					serviciosEjecutados = sql.query(con, SQL_SELECT_SERVICIOS_EJECUTADOS, paramsJson, ServicioEjecutado.class);
				} catch (Exception e) {
					logger.info("INICIO::CarteraIF -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
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
						logger.info("INICIO::CarteraIF -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
					}
					primeraEjecucion = true;
				}
				else
				{
					HashMap<String,Object> paramsOut = new HashMap<String,Object>();
					paramsOut.put("IdNomina", nomina.IdNomina);
					try {
						outGuardado = sql.query(con, SQL_SELECT_CARTERA_IF_OUT,paramsOut, SalidaCarteraIFPaso.class);
					} catch (Exception e) {
						logger.info("INICIO::CarteraIF -ERROR SQL_SELECT_OUT "+e);
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
							List<HashMap<String,Object>> resultadoCarteraIF = Utils.List2ListHashGenericObject(listOutExistente);
							
							out.P_OUT_SALIDA.put("carteraIF", resultadoCarteraIF);
							
							log.write("FIN::CarteraIF");
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
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		List<Portafolio> portObj = new ArrayList<>();
		
		
		for(Dato dato : entrada.p_Datos){
			//portafolios.add(dato.IdPortafolio);
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
		}
	
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<Movimiento> movimientos = new ArrayList<Movimiento>();
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO, params, Movimiento.class));
		}else{
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_CARTERA_NEGATIVA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_GARANTIA_HISTORICO, params, Movimiento.class));
			movimientos.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_VIGENTES_A_PLAZO_HISTORICO, params, Movimiento.class));
		}
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		List<Long> idsInstrumentos = new ArrayList<Long>();
		List<String> idsBovedas = new ArrayList<String>();
		List<String> idsEntidadClasificadoraRiesgo = new ArrayList<String>();
		List<Long> codigosRubros = new ArrayList<Long>();
		List<String> idsTipoInstrumento = new ArrayList<String>();
		List<String> idEmisores = new ArrayList<String>();
		
		//RECORREMOS PARA SACAR LOS IDS DE BUSQUEDA
		for(Movimiento mov : movimientos){
			if(!idsInstrumentos.contains(mov.IdInstrumento)) idsInstrumentos.add(mov.IdInstrumento);
			if(!idsBovedas.contains(mov.IdBoveda)) idsBovedas.add(mov.IdBoveda);
			if(!idsTipoInstrumento.contains(mov.IdTipoInstrumento)) idsTipoInstrumento.add(mov.IdTipoInstrumento);
			if(!idEmisores.contains(mov.IdEmisor)) idEmisores.add(mov.IdEmisor);
		}
		
		params.clear();
		params.put("IdInstrumento", idsInstrumentos);
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		params.clear();
		params.put("IdTipoInstrumento", idsTipoInstrumento);
		List<TipoInstrumento> tiposInstrumentos = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params, TipoInstrumento.class);
		List<TipoInstrumentoClasificacionRiesgo> tiposInstrumentosClasificacionRiesgo = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO, params, TipoInstrumentoClasificacionRiesgo.class);
		params.clear();
		params.put("IdEmisor", idEmisores);
		List<Emisor> emisores = sql.query(con, SQL_SELECT_EMISOR, params, Emisor.class);
		
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
		HashMap<String,Object> hEmisores = Utils.List2Hash(emisores, "IdEmisor");
		HashMap<String,Object> hTiposInstrumentos = Utils.List2Hash(tiposInstrumentos, "IdTipoInstrumento");
		HashMap<String,Object> hTiposInstrumentosClasificacionRiesgo = Utils.List2Hash(tiposInstrumentosClasificacionRiesgo, "IdTipoInstrumento");
		HashMap<Long,Object> hCodigosRubros = Utils.List2Hash(rubros, "CodigoRubro");
		HashMap<String,Object> hInstrumentosValorClasificacionRiesgo = Utils.List2Hash(instrumentosValorClasificacionRiesgo, "Id");
		
		List<SalidaCarteraIF> salidas = new ArrayList<SalidaCarteraIF>();
		
		for(Movimiento mov : movimientos){
			Instrumento instrumento = (Instrumento) hInstrumentos.get(mov.IdInstrumento);
			Emisor emisor = (Emisor) hEmisores.get(mov.IdEmisor);
			TipoInstrumento tipoInstrumento = (TipoInstrumento) hTiposInstrumentos.get(mov.IdTipoInstrumento);
			TipoInstrumentoClasificacionRiesgo tipoInstrumentoClasificacionRiesgo = (TipoInstrumentoClasificacionRiesgo) hTiposInstrumentosClasificacionRiesgo.get(mov.IdTipoInstrumento);
			
			Rubro rubro = instrumento == null ? null : (Rubro) hCodigosRubros.get(instrumento.CodigoRubro);
			InstrumentoValorClasificacionRiesgo instrumentoValorClasificacionRiesgo = tipoInstrumentoClasificacionRiesgo == null ? null : (InstrumentoValorClasificacionRiesgo) hInstrumentosValorClasificacionRiesgo.get(tipoInstrumentoClasificacionRiesgo.IdEntidadClasificadoraRiesgo + "_" + mov.IdInstrumento);
			
			SimpleDateFormat formatoStringDate = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatoDateString = new SimpleDateFormat("dd-MM-yyyy");
			if(instrumento != null && emisor != null && tipoInstrumento != null){
				SalidaCarteraIF salidaIF = new SalidaCarteraIF();
				salidaIF.IdCliente = mov.IdCliente;
				salidaIF.IdPortafolio = mov.IdPortafolio;
				salidaIF.FolioCartera = mov.FolioCartera;
				salidaIF.FechaOperacion = Fecha.stringToSqlDate(mov.FechaOperacion);
				salidaIF.FechaLiquidacion = Fecha.stringToSqlDate(mov.FechaLiquidacion);
				salidaIF.IdBoveda = mov.IdBoveda;
				salidaIF.IdTipoInstrumento = mov.IdTipoInstrumento;
				salidaIF.NombreTipoInstrumento = tipoInstrumento.Nombre;
				salidaIF.IdMonedaInstrumento = mov.IdMonedaInstrumento;
				salidaIF.IdInstrumento = mov.IdInstrumento;
				salidaIF.IdEmisor = mov.IdEmisor;
				salidaIF.Emisor = emisor.NombreCompleto == null ? emisor.NombreCorto : emisor.NombreCompleto;
				salidaIF.Nemotecnico = mov.Nemotecnico;
				salidaIF.FechaVencimiento = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaVencimientoInstrumento)));
				salidaIF.FechaEmision = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaEmisionInstrumento)));
				salidaIF.FechaPagoCorteCupon = Fecha.stringToSqlDate(mov.FechaPagoCorteCupon);
				salidaIF.PlazoResidual = (Long.valueOf(Fecha.stringToSqlDate(instrumento.FechaVencimiento).split("-")[0]) - Long.valueOf(Fecha.stringToSqlDate(entrada.p_FechaHasta).split("-")[0])) + "/00";
				if(rubro != null) salidaIF.Rubro = rubro.Descripcion;
				salidaIF.IdTipoReajuste = mov.IdTipoReajuste;
				salidaIF.RazonSocial = instrumento.RazonSocial;
				salidaIF.TieneCupon = "N".equals(instrumento.TieneCupon) ? "N/A" : instrumento.TieneCupon;
				salidaIF.CuponesPagadosMonedaSistema = mov.CuponesPagadosMonedaSistema;
				salidaIF.ValorClasificacionRiesgo = instrumentoValorClasificacionRiesgo == null ? "" : instrumentoValorClasificacionRiesgo.ValorClasificacionRiesgo;
				salidaIF.CantidadContable = instrumento.CorteMinimo.longValue() > 0 ? mov.CantidadOriginal.divide(instrumento.CorteMinimo, MathContext.DECIMAL128) : mov.CantidadOriginal;
				salidaIF.CantidadOriginal = mov.CantidadOriginal;
				salidaIF.CantidadCartera = mov.CantidadCartera == null ? BigDecimal.ZERO : mov.CantidadCartera;
				salidaIF.CantidadGarantia = mov.CantidadGarantia == null ? BigDecimal.ZERO : mov.CantidadGarantia;
				salidaIF.CantidadAPlazo = mov.CantidadAPlazo == null ? BigDecimal.ZERO : mov.CantidadAPlazo;
				salidaIF.CantidadTotal = mov.CantidadOriginal.add(salidaIF.CantidadCartera, MathContext.DECIMAL128).add(salidaIF.CantidadGarantia, MathContext.DECIMAL128).add(salidaIF.CantidadAPlazo, MathContext.DECIMAL128);
				salidaIF.ValorRescateOriginal = mov.ValorRescateOriginal;
				salidaIF.ValorRescateCartera = mov.ValorRescateCartera == null ? BigDecimal.ZERO : mov.ValorRescateCartera;
				salidaIF.ValorRescateGarantia = mov.ValorRescateGarantia == null ? BigDecimal.ZERO : mov.ValorRescateGarantia;
				salidaIF.ValorRescateAPlazo = mov.ValorRescateAPlazo == null ? BigDecimal.ZERO : mov.ValorRescateAPlazo;
				salidaIF.ValorRescateTotal = mov.ValorRescateCartera == null ? BigDecimal.ZERO : mov.ValorRescateCartera;
				salidaIF.TasaCompra = mov.TasaCompra;
				salidaIF.PrecioCompra = mov.PrecioCompra;
				salidaIF.MontoCompraMonedaSistema = mov.MontoCompraMonedaSistema.add(mov.MontoGarantiaValorizadoActual, MathContext.DECIMAL128);//Valorizado
				salidaIF.TasaMercado = mov.TasaMercado;
				salidaIF.PrecioMercado = mov.PrecioMercado;
				salidaIF.MontoMercadoMonedaSistema = mov.MontoMercadoMonedaSistema.add(mov.MontoMercadoGarantiaActual, MathContext.DECIMAL128);//MontoMercado
				salidaIF.MontoMercadoMonedaAdicional = mov.MontoMercadoMonedaAdicional;
				salidaIF.MontoInteresCorridoMercadoActualMonedaSistema = mov.MontoInteresCorridoMercadoActualMonedaSistema;
				salidaIF.MontoInteresCorridoMercadoActualMonedaAdicional = mov.MontoInteresCorridoMercadoActualMonedaAdicional;
				if(monedaSistema.equals(mov.IdMonedaInstrumento) || (!monedaSistema.equals(mov.IdMonedaInstrumento) && "S".equals(entrada.p_FiltroSoloMonedaSistema))){
					salidaIF.Moneda = monedaSistema;
					salidaIF.MontoCompra = mov.MontoCompraMonedaSistema.add(mov.MontoGarantiaValorizadoActual, MathContext.DECIMAL128);//Valorizado
					salidaIF.MontoMercado = mov.MontoMercadoMonedaSistema.add(mov.MontoMercadoGarantiaActual, MathContext.DECIMAL128);//MontoMercado
					salidaIF.MontoInteresCorrido = mov.MontoInteresCorridoMercadoActualMonedaSistema;
				} else {
					salidaIF.Moneda = monedaAdicional;
					salidaIF.MontoCompra = BigDecimal.ZERO;
					salidaIF.MontoMercado = mov.MontoMercadoMonedaAdicional;
					salidaIF.MontoInteresCorrido = mov.MontoInteresCorridoMercadoActualMonedaAdicional;
				}
				
				salidaIF.Mercado = mov.IdClasificacionTipoInstrumento;
				salidaIF.Rentabilidad = ((mov.MontoCompraMonedaAdicional.add(mov.MontoGarantiaValorizadoActual, MathContext.DECIMAL128)).compareTo(BigDecimal.ZERO) != 0 ? (mov.MontoMercadoMonedaSistema.add(mov.MontoMercadoGarantiaActual, MathContext.DECIMAL128)).subtract((mov.MontoCompraMonedaAdicional.add(mov.MontoGarantiaValorizadoActual, MathContext.DECIMAL128)), MathContext.DECIMAL128).divide((mov.MontoCompraMonedaAdicional.add(mov.MontoGarantiaValorizadoActual, MathContext.DECIMAL128)), MathContext.DECIMAL128) : BigDecimal.ZERO).multiply(new BigDecimal("100"), MathContext.DECIMAL128);
				
				salidas.add(salidaIF);
			}
			
		}
		
		if(primeraEjecucion) {
			try {
				List<SalidaCarteraIFPaso> listOutPaso =  new ArrayList();
				for(SalidaCarteraIF salida : salidas) {
					SalidaCarteraIFPaso outPaso = new SalidaCarteraIFPaso();
					outPaso.IdNomina = nomina.IdNomina;
					outPaso.IdTrabajo = idTrabajo;
					outPaso.IdCliente = salida.IdCliente;
					outPaso.IdPortafolio = salida.IdPortafolio;
					outPaso.FolioCartera = salida.FolioCartera;
					outPaso.FechaOperacion = salida.FechaOperacion;
					outPaso.FechaLiquidacion = salida.FechaLiquidacion;
					outPaso.IdBoveda = salida.IdBoveda;
					outPaso.IdTipoInstrumento = salida.IdTipoInstrumento;
					outPaso.NombreTipoInstrumento = salida.NombreTipoInstrumento;
					outPaso.IdInstrumento = salida.IdInstrumento;
					outPaso.IdEmisor = salida.IdEmisor;
					outPaso.Emisor = salida.Emisor;
					outPaso.Nemotecnico = salida.Nemotecnico;
					outPaso.FechaVencimiento = salida.FechaVencimiento;
					outPaso.FechaEmision = salida.FechaEmision;
					outPaso.FechaPagoCorteCupon = salida.FechaPagoCorteCupon;
					outPaso.PlazoResidual = salida.PlazoResidual;
					outPaso.Rubro = salida.Rubro;
					outPaso.IdTipoReajuste = salida.IdTipoReajuste;
					outPaso.RazonSocial = salida.RazonSocial;
					outPaso.TieneCupon = salida.TieneCupon;
					outPaso.CuponesPagadosMonedaSistema = salida.CuponesPagadosMonedaSistema;
					outPaso.ValorClasificacionRiesgo = salida.ValorClasificacionRiesgo;
					outPaso.IdMonedaInstrumento = salida.IdMonedaInstrumento;
					outPaso.CantidadContable = salida.CantidadContable;
					outPaso.CantidadOriginal = salida.CantidadOriginal;
					outPaso.CantidadCartera = salida.CantidadCartera;
					outPaso.CantidadGarantia = salida.CantidadGarantia;
					outPaso.CantidadAPlazo = salida.CantidadAPlazo;
					outPaso.CantidadTotal = salida.CantidadTotal;
					outPaso.ValorRescateOriginal = salida.ValorRescateOriginal;
					outPaso.ValorRescateCartera = salida.ValorRescateCartera;
					outPaso.ValorRescateGarantia = salida.ValorRescateGarantia;
					outPaso.ValorRescateAPlazo = salida.ValorRescateAPlazo;
					outPaso.ValorRescateTotal = salida.ValorRescateTotal;
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
					outPaso.MontoCompra = salida.MontoCompra;
					outPaso.MontoMercado = salida.MontoMercado;
					outPaso.MontoInteresCorrido = salida.MontoInteresCorrido;
					outPaso.Rentabilidad = salida.Rentabilidad;
					outPaso.Mercado = salida.Mercado;
					listOutPaso.add(outPaso);
				}
				sql.batch(con, SQL_INSERT_CARTERA_IF_OUT, listOutPaso, SalidaCarteraIFPaso.class);
			} catch (Exception e) {
				logger.info("INICIO::Cartera IF -ERROR SQL_INSERT_CARTERA_FM_OUT "+e);
			}
		}
		
		List<HashMap<String,Object>> resultadoCarteraIF = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("carteraIF", resultadoCarteraIF);
		
		long finServicio = System.currentTimeMillis();
		log.write("FIN::CarteraIF : "+ (finServicio - inicioServicio));
		return out;
	}
	
	private List<SalidaCarteraIF> parseaObjetoDePaso(List<SalidaCarteraIFPaso> listObjPaso,Long idTrabajo){
		List<SalidaCarteraIF> listSalidaCartera = new ArrayList<>();
		for(SalidaCarteraIFPaso p : listObjPaso) {
			if(p.IdTrabajo.compareTo(idTrabajo) == 0L) {
				SalidaCarteraIF outExistente = new SalidaCarteraIF();
				outExistente.IdCliente = p.IdCliente;
				outExistente.IdPortafolio = p.IdPortafolio;
				outExistente.FolioCartera = p.FolioCartera;
				outExistente.FechaOperacion = p.FechaOperacion;
				outExistente.FechaLiquidacion = p.FechaLiquidacion;
				outExistente.IdBoveda = p.IdBoveda;
				outExistente.IdTipoInstrumento = p.IdTipoInstrumento;
				outExistente.NombreTipoInstrumento = p.NombreTipoInstrumento;
				outExistente.IdInstrumento = p.IdInstrumento;
				outExistente.IdEmisor = p.IdEmisor;
				outExistente.Emisor = p.Emisor;
				outExistente.Nemotecnico = p.Nemotecnico;
				outExistente.FechaVencimiento = p.FechaVencimiento;
				outExistente.FechaEmision = p.FechaEmision;
				outExistente.FechaPagoCorteCupon = p.FechaPagoCorteCupon;
				outExistente.PlazoResidual = p.PlazoResidual;
				outExistente.Rubro = p.Rubro;
				outExistente.IdTipoReajuste = p.IdTipoReajuste;
				outExistente.RazonSocial = p.RazonSocial;
				outExistente.TieneCupon = p.TieneCupon;
				outExistente.CuponesPagadosMonedaSistema = p.CuponesPagadosMonedaSistema;
				outExistente.ValorClasificacionRiesgo = p.ValorClasificacionRiesgo;
				outExistente.IdMonedaInstrumento = p.IdMonedaInstrumento;
				outExistente.CantidadContable = p.CantidadContable;
				outExistente.CantidadOriginal = p.CantidadOriginal;
				outExistente.CantidadCartera = p.CantidadCartera;
				outExistente.CantidadGarantia = p.CantidadGarantia;
				outExistente.CantidadAPlazo = p.CantidadAPlazo;
				outExistente.CantidadTotal = p.CantidadTotal;
				outExistente.ValorRescateOriginal = p.ValorRescateOriginal;
				outExistente.ValorRescateCartera = p.ValorRescateCartera;
				outExistente.ValorRescateGarantia = p.ValorRescateGarantia;
				outExistente.ValorRescateAPlazo = p.ValorRescateAPlazo;
				outExistente.ValorRescateTotal = p.ValorRescateTotal;
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
				outExistente.MontoCompra = p.MontoCompra;
				outExistente.MontoMercado = p.MontoMercado;
				outExistente.MontoInteresCorrido = p.MontoInteresCorrido;
				outExistente.Rentabilidad = p.Rentabilidad;
				outExistente.Mercado = p.Mercado;
				listSalidaCartera.add(outExistente);
			}
		}
		return listSalidaCartera;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-12-21");
		Objin.p_FechaHasta = Fecha.toDate("2018-12-21");
		Dato dato = new Dato();
		dato.IdCliente = 18139L;
		dato.IdPortafolio = 24136L;
		Objin.p_Datos.add(dato);
		CarteraIF  demo= new CarteraIF();				
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
