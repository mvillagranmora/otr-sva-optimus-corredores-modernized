package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
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

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarFlujoCustodiaFondoMutuo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarFlujoCustodiaFondoMutuo.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_RESCATE_TOTAL_TRANSITO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarFlujoCustodiaFondoMutuo_SelectRescateTotalTransito.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarFlujoCustodiaFondoMutuo_SelectSerieFondo.sql");
	
	public static class EntradaConsultarFlujoCustodiaFondoMutuo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPais;
		public List<InInstrumento> p_Instrumentos;
		public List<InCarteras> p_Carteras;
		public String p_FechaSaldo;
		public Integer p_DiasProyeccion;
		
		
		public EntradaConsultarFlujoCustodiaFondoMutuo() {
			this.p_Instrumentos = new ArrayList<InInstrumento>();
			this.p_Carteras = new ArrayList<InCarteras>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InInstrumento{
		public Long IdInstrumento;
		public String IdRegimenFondoMutuo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InCarteras{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorCuota{
		public BigDecimal ValorCuota;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RescateTotal{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdRegimenTributario;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SerieFondo{
		public Long IdInstrumento;
		public String IdSerieFondo;
		public String IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarFlujoCustodiaFondoMutuo{
		public List<FlujoFondoMutuoTotalizado> P_OUT_FLUJOFONDOMUTUOTOTALIZADO;
		
		public SalidaConsultarFlujoCustodiaFondoMutuo(){
			this.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = new ArrayList<FlujoFondoMutuoTotalizado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FlujoFondoMutuoTotalizado{
		public Long IdInterno;
		public Long IdInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal CantidadDisponible;
		public BigDecimal MontoDisponible;
		public BigDecimal MontoDisponibleCustodia;
		public String RescateTotal;
		public String Nemotecnico;
		public String IdRegimenTributario;
		public String IdMoneda;
		
		public FlujoFondoMutuoTotalizado(){
			this.CantidadDisponible = BigDecimal.ZERO;
			this.MontoDisponible = BigDecimal.ZERO;
			this.MontoDisponibleCustodia = BigDecimal.ZERO;
			this.Nemotecnico = "";
			this.IdMoneda = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarFlujoCustodiaFondoMutuo jin = gson.fromJson(in.toString(), EntradaConsultarFlujoCustodiaFondoMutuo.class);
		SalidaConsultarFlujoCustodiaFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaConsultarFlujoCustodiaFondoMutuo logica(Connection con, EntradaConsultarFlujoCustodiaFondoMutuo entrada) throws Exception{
		logger.info("INICIO::ConsultarFlujoCustodiaFondoMutuo");
		long inicioServicio = System.currentTimeMillis();		
		SalidaConsultarFlujoCustodiaFondoMutuo out = new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		FlujoCustodiaSalida salidaFlujoCustodia = obtenerFlujoCustodia(con, entrada);
		
		out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = obtenerSalidaSinValorizar(con, entrada, salidaFlujoCustodia);
		
		out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = obtenerSalidaValorizada(con, entrada, out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarFlujoCustodiaFondoMutuo", finServicio - inicioServicio);
		
		return out;
	}
	
	private FlujoCustodiaSalida obtenerFlujoCustodia(Connection con, EntradaConsultarFlujoCustodiaFondoMutuo entrada) throws Exception {
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerFlujoCustodia");
		long inicio = System.currentTimeMillis();
		
		ConsultarFlujoCustodia flujoCustodia = new ConsultarFlujoCustodia();
		
		FlujoCustodiaEntrada flujoCustodiaEntrada = new FlujoCustodiaEntrada();
		
		flujoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		flujoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		flujoCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
		flujoCustodiaEntrada.p_FechaSaldo = entrada.p_FechaSaldo;
		flujoCustodiaEntrada.p_IdPais = entrada.p_IdPais;
		flujoCustodiaEntrada.p_DiasProyeccion = entrada.p_DiasProyeccion;
		flujoCustodiaEntrada.p_DiaHabil = "S";
		flujoCustodiaEntrada.p_ValorizarFlujo = "N";
		
		
		for(InCarteras cartera : entrada.p_Carteras){
			FlujoUso uso = new FlujoUso();
			uso.UsoTipoOperacion = "RESCFM";
			uso.IdCliente = cartera.IdCliente;
			uso.IdPortafolio = cartera.IdPortafolio;
			uso.BusquedaEspecifica = "";
			uso.IdTipoInstrumentoEspecifico = "";
			flujoCustodiaEntrada.p_Uso.add(uso);
		}
		HashMap<String,String> keysInstrumentosEnviados = new HashMap<String,String>();
		for(InInstrumento instrumento : entrada.p_Instrumentos){
			String key = instrumento.IdInstrumento + "_" + instrumento.IdRegimenFondoMutuo;
			if(keysInstrumentosEnviados.get(key) == null) {
				FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
				busqueda.IdClasificacionTipoInstrumento = "FM";
				busqueda.IdInstrumento = instrumento.IdInstrumento.intValue() == 0 ? null : instrumento.IdInstrumento;
				busqueda.IdRegimenFondoMutuo = instrumento.IdRegimenFondoMutuo;
				flujoCustodiaEntrada.p_FiltrosBusqueda.add(busqueda);
				keysInstrumentosEnviados.put(key, key);
			}
			
		}
		
		FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
		agrupador.ClasificacionTipoInstrumento = "S";
		agrupador.TipoInstrumento = "S";
		agrupador.Instrumento = "S";
		agrupador.Emisor = "N";
		agrupador.Nemotecnico = "N";
		agrupador.NemoSVS = "N";
		agrupador.FechaEmisionInstrumento = "N";
		agrupador.FechaVencimientoInstrumento = "N";
		agrupador.FechaLiquidacionInstrumento = "N";
		agrupador.TipoReajuste = "N";
		agrupador.EsFungible = "N";
		agrupador.CodigoIFRS = "N";
		agrupador.Boveda = "N";
		agrupador.Bolsa = "N";
		agrupador.TipoOperacion = "N";
		agrupador.Mesa = "N";
		agrupador.FolioRegistro = "N";
		agrupador.ItemOrigen = "N";
		agrupador.IdRegimenFondoMutuo = "S";
		flujoCustodiaEntrada.p_EsAgrupadoPor.add(agrupador);
		
		FlujoCustodiaSalida salidaFlujoCustodia = flujoCustodia.logica(con, flujoCustodiaEntrada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerFlujoCustodia", fin - inicio);
		return salidaFlujoCustodia;
	}
	
	private List<FlujoFondoMutuoTotalizado> obtenerSalidaSinValorizar(Connection con, EntradaConsultarFlujoCustodiaFondoMutuo entrada, FlujoCustodiaSalida salidaFlujoCustodia) throws Exception {
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerSerieFondo.obtenerSalidaSinValorizar");
		long inicio = System.currentTimeMillis();
		Long idInterno = 1L;
		Map<Long, SerieFondo> seriePorIdInstrumento = obtenerSerieFondo(con, entrada.p_Instrumentos);
		List<RescateTotal> rescatesTotales = obtenerRescatesTotalTransito(con, entrada.p_Instrumentos, entrada.p_Carteras);
		List<FlujoFondoMutuoTotalizado> salida = new ArrayList<FlujoFondoMutuoTotalizado>();
		for(InCarteras cartera : entrada.p_Carteras){
			for(InInstrumento instrumento : entrada.p_Instrumentos){
				FlujoFondoMutuoTotalizado flujo = new FlujoFondoMutuoTotalizado();
				flujo.IdInterno = idInterno;
				flujo.IdCliente = cartera.IdCliente;
				flujo.IdPortafolio = cartera.IdPortafolio;
				flujo.IdInstrumento = instrumento.IdInstrumento;
				flujo.IdRegimenTributario = instrumento.IdRegimenFondoMutuo;
				SerieFondo serieFondo = (SerieFondo) seriePorIdInstrumento.get(flujo.IdInstrumento);
				if(existeSerieFondo(serieFondo)) {
					flujo.Nemotecnico = serieFondo.IdSerieFondo;
					flujo.IdMoneda = serieFondo.IdMoneda;
				}
				
				
				for(FlujoCustodia total : salidaFlujoCustodia.p_out_FlujoTotalizado){
					if(flujo.IdCliente.equals(total.IdCliente) && flujo.IdPortafolio.equals(total.IdPortafolio) && flujo.IdInstrumento.equals(total.IdInstrumento) && (FondoMutuoUtils.esRegimenTributarioVacio(flujo.IdRegimenTributario) || flujo.IdRegimenTributario.equals(total.IdRegimenFondoMutuo))){
						flujo.CantidadDisponible = total.Cantidad;
						flujo.MontoDisponibleCustodia = total.MontoAMercadoMonedaInstrumento;
						break;
					}
				}
				
				//flujo.MontoDisponible = flujo.CantidadDisponible.multiply(valorCuota, MathContext.DECIMAL128);
				flujo.RescateTotal = _c.NO;
				for(RescateTotal r : rescatesTotales){
					if(flujo.IdCliente.equals(r.IdCliente) && flujo.IdPortafolio.equals(r.IdPortafolio) && flujo.IdInstrumento.equals(r.IdInstrumento) && (FondoMutuoUtils.esRegimenTributarioVacio(flujo.IdRegimenTributario) || flujo.IdRegimenTributario.equals(r.IdRegimenTributario))){
						// Ahora para los rescates totales tambien se especifica el Regimen tributario (18T, 18Q, etc)
						flujo.RescateTotal = _c.SI;
						flujo.CantidadDisponible = BigDecimal.ZERO;
						flujo.MontoDisponible = BigDecimal.ZERO;
						flujo.MontoDisponibleCustodia = BigDecimal.ZERO;
						break;
					}
				}
				
				flujo.MontoDisponibleCustodia = MonedaFormato.formatear(con, flujo.MontoDisponibleCustodia, MonedaFormato.MONTO,flujo.IdMoneda).valor;
				idInterno++;
				salida.add(flujo);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerSalidaSinValorizar", fin - inicio);
		return salida;
	}
	
	private Map<Long,SerieFondo> obtenerSerieFondo(Connection con, List<InInstrumento> instrumentos) throws Exception{
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerSerieFondo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Long> idsInstrumentos = instrumentos.parallelStream().map(instrumento->instrumento.IdInstrumento).distinct().collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdInstrumento", idsInstrumentos);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondo> seriesFondo = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondo.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		Map<Long, SerieFondo> seriePorIdInstrumento = seriesFondo.parallelStream().collect(Collectors.toMap(serieFondo->serieFondo.IdInstrumento, serieFondo->serieFondo));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerSerieFondo", fin - inicio);
		return seriePorIdInstrumento;
	}
	
	private List<RescateTotal> obtenerRescatesTotalTransito(Connection con, List<InInstrumento> instrumentos, List<InCarteras> carteras) throws Exception{
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerRescatesTotalTransito");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Long> idsInstrumentos = instrumentos.parallelStream().map(instrumento->instrumento.IdInstrumento).distinct().collect(Collectors.toList());
		List<Long> idsClientes = carteras.parallelStream().map(cartera->cartera.IdCliente).distinct().collect(Collectors.toList());
		List<Long> idsPortafolio = carteras.parallelStream().map(cartera->cartera.IdPortafolio).distinct().collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdCliente", idsClientes);
		params.put("IdPortafolio", idsPortafolio);
		params.put("IdInstrumento", idsInstrumentos);
		logger.debug("INICIO::SQL_SELECT_RESCATE_TOTAL_TRANSITO");
		List<RescateTotal> rescatesTotales = sql.query(con, SQL_SELECT_RESCATE_TOTAL_TRANSITO, params, RescateTotal.class);
		logger.debug("FIN::SQL_SELECT_RESCATE_TOTAL_TRANSITO");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerRescatesTotalTransito", fin - inicio);
		return rescatesTotales;
	}
	
	private Boolean existeSerieFondo(SerieFondo serieFondo) {
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.existeSerieFondo");
		long inicio = System.currentTimeMillis();
		Boolean existeSerieFondo = serieFondo != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.existeSerieFondo", fin - inicio);
		return existeSerieFondo;
	}
	
	private List<FlujoFondoMutuoTotalizado> obtenerSalidaValorizada(Connection con, EntradaConsultarFlujoCustodiaFondoMutuo entrada, List<FlujoFondoMutuoTotalizado> salida) throws Exception{
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerSalidaValorizada");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalculo.p_Series = salida.parallelStream()
		.filter(custodia->{
			return esCantidadValida(custodia.CantidadDisponible);
		})
		.map(custodia->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = custodia.IdInterno;
			calculoMontoCuota.Fecha = entrada.p_FechaSaldo;
			calculoMontoCuota.IdSerieFondo = custodia.Nemotecnico;
			calculoMontoCuota.CalculoPorMontoCuota = _c.CalculoPorCuota;
			calculoMontoCuota.Monto = BigDecimal.ZERO;
		 	calculoMontoCuota.NumeroCuotas = custodia.CantidadDisponible;
		 	calculoMontoCuota.IdMonedaFondo = custodia.IdMoneda;
		 	calculoMontoCuota.IdMonedaInversion = custodia.IdMoneda;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudRescate;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		salida.parallelStream().forEach(custodia->{
			CalcularMontoNumeroCuota.Resultado resultadoValorizacion = montosValorizados.get(custodia.IdInterno);
			if(esValorizacionCorrecta(resultadoValorizacion)) {
				custodia.MontoDisponible = resultadoValorizacion.Monto;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerSalidaValorizada", fin - inicio);
		return salida;
	}
	
	private Boolean esCantidadValida(BigDecimal cantidad) {
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.obtenerSalidaValorizada");
		long inicio = System.currentTimeMillis();
		Boolean esCantidadValida = cantidad != null && cantidad.compareTo(BigDecimal.ZERO) == 1;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.obtenerSalidaValorizada", fin - inicio);
		return esCantidadValida;
	}
	
	private Boolean esValorizacionCorrecta(CalcularMontoNumeroCuota.Resultado resultadoValorizacion) {
		logger.trace("INICIO::ConsultarFlujoCustodiaFondoMutuo.esValorizacionCorrecta");
		long inicio = System.currentTimeMillis();
		Boolean esValorizacionCorrecta = resultadoValorizacion != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarFlujoCustodiaFondoMutuo.esValorizacionCorrecta", fin - inicio);
		return esValorizacionCorrecta;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_IdPais\":44,\"p_Instrumentos\":[{\"IdInstrumento\":910277,\"IdRegimenFondoMutuo\":\"DC-DL\"},{\"IdInstrumento\":910277,\"IdRegimenFondoMutuo\":\"DC-R\"}],\"p_Carteras\":[{\"IdCliente\":48985,\"IdPortafolio\":437526},{\"IdCliente\":48985,\"IdPortafolio\":205051}],\"p_FechaSaldo\":\"2020-10-16\",\"p_DiasProyeccion\":0}";		
		EntradaConsultarFlujoCustodiaFondoMutuo Objin = gson.fromJson(jsonRF, EntradaConsultarFlujoCustodiaFondoMutuo.class);
		ConsultarFlujoCustodiaFondoMutuo  demo=new ConsultarFlujoCustodiaFondoMutuo();				
		try {
			
			SalidaConsultarFlujoCustodiaFondoMutuo ObjOut=demo.logica(con, Objin);
			//con.rollback();
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
