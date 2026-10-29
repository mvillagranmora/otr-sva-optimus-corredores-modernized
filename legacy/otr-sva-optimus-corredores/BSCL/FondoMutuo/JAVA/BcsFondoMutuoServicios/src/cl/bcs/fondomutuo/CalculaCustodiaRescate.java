package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
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

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CalculaCustodiaRescate implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CalculaCustodiaRescate.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/CalculaCustodiaRescate_SelectCarteras.sql");
	
	public static class EntradaCalculaCustodiaRescate{
		
		public Long p_IdEmpresa;		
		public List<InSolicitud> p_Solicitudes;
		
		public EntradaCalculaCustodiaRescate(){
			this.p_Solicitudes = new ArrayList<InSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitud{
		
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public String IdMoneda;
	 	public String IdMonedaFondo;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public String TipoSolicitud;
	 	public Long IdInstrumento;
	 	public Long IdCliente;
	 	public Long IdPortafolio;
	 	public String MontoMonedaSistema;
	 	public Date FechaValorCuota;
		public String IdRegimenTributario;
		public Long IdOmnibus;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudProceso{
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public String IdMoneda;
	 	public String IdMonedaFondo;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal CostoMonedaLocal;
	 	public BigDecimal ParidadFechaCertificacion;
	 	public String TipoSolicitud;
	 	public Long IdInstrumento;
	 	public Long IdCliente;
	 	public Long IdPortafolio;
	 	public BigDecimal MontoMonedaSistema;
	 	public Date FechaValorCuota;
	 	public BigDecimal CantidadDisponible;
	 	public String IdRegimenTributario;
	 	public Long IdOmnibus;
	 	public String EstadoProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdRegimenFondoMutuo;
		public BigDecimal CantidadDisponiblePortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalculaCustodiaRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCalculaCustodiaRescate> P_OUT_RESULTADO;
		
		public SalidaCalculaCustodiaRescate(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalculaCustodiaRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCalculaCustodiaRescate{
		
		public Long IdSolicitudAporteRescate;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal CantidadDisponible;
	 	public BigDecimal MontoMonedaSistema;
	 	public BigDecimal CostoEnFactorAjuste;
	 	public BigDecimal ValorFactorAjuste; 
	 	public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LlaveCarterasAgrupadas{
		public Long IdPortafolio;
		public Long IdInstrumento;
		
		@Override
		public int hashCode() {
			return 31 * this.IdPortafolio.hashCode() + this.IdInstrumento.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			LlaveCarterasAgrupadas llave = (LlaveCarterasAgrupadas) obj;
			return this.IdPortafolio.equals(llave.IdPortafolio) && this.IdInstrumento.equals(llave.IdInstrumento);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraAgrupada{
		public LlaveCarterasAgrupadas Id;
		public BigDecimal CantidadTotal;
		public Map<String, Cartera> CarterasPorRegimen;
		
		public CarteraAgrupada() {
			this.CarterasPorRegimen = new HashMap<String, Cartera>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroConsultarCartera {
		public List<Long> IdPortafolios;
		public List<Long> IdInstrumentos;
		public Long IdEmpresa;
		
		public ParametroConsultarCartera() {
			this.IdPortafolios = new ArrayList<Long>();
			this.IdInstrumentos = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalculaCustodiaRescate jin = gson.fromJson(in.toString(), EntradaCalculaCustodiaRescate.class);
		SalidaCalculaCustodiaRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCalculaCustodiaRescate logica(Connection con, EntradaCalculaCustodiaRescate entrada) throws Exception{
		logger.info("INICIO::CalculaCustodiaRescate");
		SalidaCalculaCustodiaRescate out = new SalidaCalculaCustodiaRescate();
		
		List<Cartera> carteras = obtenerCarterasDisponibles(con, entrada);
		List<SolicitudProceso> solicitudesProceso = obtenerSolicitudesProceso(entrada, carteras);
		Map<Long, CalcularMontoNumeroCuota.Resultado> montosActualizadosParaSolicitudesConAdvertenciasPendientes = obtenerMontosActualizadosParaSolicitudesConAdvertenciasPendientes(con,solicitudesProceso,entrada.p_IdEmpresa);
		out.P_OUT_RESULTADO = obtenerSalida(solicitudesProceso, montosActualizadosParaSolicitudesConAdvertenciasPendientes);

		logger.info("FIN::CalculaCustodiaRescate");
		return out;
	}
	
	private List<Cartera> obtenerCarterasDisponibles(Connection con, EntradaCalculaCustodiaRescate entrada) throws Exception{
		logger.info("INICIO::CalculaCustodiaRescate.obtenerCarterasDisponibles");
		SQL sql=new SQL();
		ParametroConsultarCartera parametrosConsultarCartera = obtenerParametrosConsultarCartera(entrada);
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", parametrosConsultarCartera.IdPortafolios);
		params.put("IdInstrumento", parametrosConsultarCartera.IdInstrumentos);
		params.put("IdEmpresa", parametrosConsultarCartera.IdEmpresa);
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
		logger.info("FIN::CalculaCustodiaRescate.obtenerCarterasDisponibles");
		return carteras;
	}
	
	private ParametroConsultarCartera obtenerParametrosConsultarCartera(EntradaCalculaCustodiaRescate entrada) {
		logger.info("INICIO::CalculaCustodiaRescate.obtenerParametrosConsultarCartera");
		ParametroConsultarCartera parametrosConsultarCartera = new ParametroConsultarCartera();
		parametrosConsultarCartera.IdPortafolios = entrada.p_Solicitudes.parallelStream().map(solicitud->solicitud.IdPortafolio).distinct().collect(Collectors.toList());
		parametrosConsultarCartera.IdInstrumentos = entrada.p_Solicitudes.parallelStream().map(solicitud->solicitud.IdInstrumento).distinct().collect(Collectors.toList());
		parametrosConsultarCartera.IdEmpresa = entrada.p_IdEmpresa;
		logger.info("FIN::CalculaCustodiaRescate.obtenerParametrosConsultarCartera");
		return parametrosConsultarCartera;
	}
	
	private List<SolicitudProceso> obtenerSolicitudesProceso(EntradaCalculaCustodiaRescate entrada, List<Cartera> carteras){
		logger.info("INICIO::CalculaCustodiaRescate.obtenerSolicitudesProceso");
		List<SolicitudProceso> solicitudes = new ArrayList<SolicitudProceso>();
		Map<LlaveCarterasAgrupadas, CarteraAgrupada> carterasAgrupadasPorPortafolioInstrumento = obtenerCarterasAgrupadasPorPortafolioInstrumento(carteras);
		
		for(InSolicitud solicitud : entrada.p_Solicitudes){
			SolicitudProceso solicitudProceso = crearSolicitudProcesoValoresIniciales(solicitud);
			LlaveCarterasAgrupadas llavePortafolioInstrumento = obtenerLlaveCarterasAgrupadasPorPortafolioInstrumento(solicitud);
			CarteraAgrupada carteraAgrupada = carterasAgrupadasPorPortafolioInstrumento.get(llavePortafolioInstrumento);
			if(esCarteraAgrupadaSinSaldo(carteraAgrupada)) {
				solicitudProceso.EstadoProceso = _c.EstadoRechazado;
				solicitudProceso.CantidadDisponible = obtenerCantidadCarteraAgrupada(carteraAgrupada);
			}else {
				
				if(esRescateTotalConModificacionDeCantidad(solicitudProceso)) {					
					Cartera cartera = carteraAgrupada.CarterasPorRegimen.get(solicitud.IdRegimenTributario);
					solicitudProceso = asignarCantidadEstadoRescateTotal(solicitudProceso, cartera);					
					carteraAgrupada = actualizarCarteraAgrupadaPorSolicitudRescatada(carteraAgrupada, solicitudProceso);				
				}else {
					Cartera cartera = carteraAgrupada.CarterasPorRegimen.get(solicitud.IdRegimenTributario);
					solicitudProceso = asignarCantidadEstadoRescateParcial(solicitudProceso, cartera);
					carteraAgrupada = actualizarCarteraAgrupadaPorSolicitudRescatada(carteraAgrupada, solicitudProceso);
				}
				
			}
			carterasAgrupadasPorPortafolioInstrumento.put(llavePortafolioInstrumento, carteraAgrupada);
			solicitudes.add(solicitudProceso);
		}
		
		logger.info("FIN::CalculaCustodiaRescate.obtenerSolicitudesProceso");
		return solicitudes;
	}
		
	private Map<LlaveCarterasAgrupadas, CarteraAgrupada> obtenerCarterasAgrupadasPorPortafolioInstrumento(List<Cartera> carteras) {
		logger.info("INICIO::CalculaCustodiaRescate.obtenerCarterasAgrupadasPorPortafolioInstrumento");
		Map<LlaveCarterasAgrupadas,CarteraAgrupada> carterasAgrupadasPorPortafolioInstrumento = new HashMap<LlaveCarterasAgrupadas,CarteraAgrupada>();
		Map<LlaveCarterasAgrupadas, List<Cartera>> listaCarterasAgrupadasPorPortafolioInstrumento = carteras.parallelStream().collect(Collectors.groupingBy(cartera->{
			LlaveCarterasAgrupadas llave = new LlaveCarterasAgrupadas();
			llave.IdPortafolio = cartera.IdPortafolio;
			llave.IdInstrumento = cartera.IdInstrumento;
			return llave;
		}));
		for(Entry<LlaveCarterasAgrupadas, List<Cartera>> grupo : listaCarterasAgrupadasPorPortafolioInstrumento.entrySet()) {
			LlaveCarterasAgrupadas llave = grupo.getKey();
			CarteraAgrupada carteraConTotalGrupo = obtenerCarteraTotalizadaPortafolioInstrumento(grupo);
			carterasAgrupadasPorPortafolioInstrumento.put(llave, carteraConTotalGrupo);
		}
		logger.info("FIN::CalculaCustodiaRescate.obtenerCarterasAgrupadasPorPortafolioInstrumento");
		return carterasAgrupadasPorPortafolioInstrumento;
	}
	
	private CarteraAgrupada obtenerCarteraTotalizadaPortafolioInstrumento(Entry<LlaveCarterasAgrupadas, List<Cartera>> grupo) {
		logger.info("INICIO::CalculaCustodiaRescate.obtenerCarteraTotalizadaPortafolioInstrumento");
		LlaveCarterasAgrupadas llave = grupo.getKey();
		List<Cartera> carterasGrupo = grupo.getValue();
		BigDecimal cantidadTotalGrupo = carterasGrupo.stream().map(cartera->cartera.CantidadDisponiblePortafolio).reduce(BigDecimal.ZERO, BigDecimal::add);
		CarteraAgrupada carteraConTotalGrupo = new CarteraAgrupada();
		carteraConTotalGrupo.Id = llave;
		carteraConTotalGrupo.CantidadTotal = cantidadTotalGrupo;
		carteraConTotalGrupo.CarterasPorRegimen = carterasGrupo.parallelStream().collect(Collectors.toMap(cartera->cartera.IdRegimenFondoMutuo,cartera->cartera));
		logger.info("FIN::CalculaCustodiaRescate.obtenerCarteraTotalizadaPortafolioInstrumento");
		return carteraConTotalGrupo;
	}
	
	private SolicitudProceso crearSolicitudProcesoValoresIniciales(InSolicitud solicitud) {
		logger.info("INICIO::CalculaCustodiaRescate.crearSolicitudProcesoValoresIniciales");
		SolicitudProceso solicitudProceso = new SolicitudProceso();
		solicitudProceso.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
		solicitudProceso.IdSerieFondo = solicitud.IdSerieFondo;
		solicitudProceso.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		solicitudProceso.IdMoneda = solicitud.IdMoneda;
		solicitudProceso.IdMonedaFondo = solicitud.IdMonedaFondo;
		solicitudProceso.Monto = solicitud.Monto;
		solicitudProceso.NumeroCuotas = solicitud.NumeroCuotas;
		solicitudProceso.ValorCuota = solicitud.ValorCuota;
		solicitudProceso.CostoMonedaLocal = BigDecimal.ZERO;
		solicitudProceso.ParidadFechaCertificacion = BigDecimal.ZERO;
		solicitudProceso.TipoSolicitud = solicitud.TipoSolicitud;
		solicitudProceso.IdInstrumento = solicitud.IdInstrumento;
		solicitudProceso.IdCliente = solicitud.IdCliente;
		solicitudProceso.IdPortafolio = solicitud.IdPortafolio;
		solicitudProceso.MontoMonedaSistema = BigDecimal.ZERO;
		solicitudProceso.FechaValorCuota = solicitud.FechaValorCuota;
		solicitudProceso.CantidadDisponible = BigDecimal.ZERO;
		solicitudProceso.IdRegimenTributario = solicitud.IdRegimenTributario;
		solicitudProceso.IdOmnibus = solicitud.IdOmnibus;
		solicitudProceso.EstadoProceso = _c.EstadoAprobado;
		logger.info("FIN::CalculaCustodiaRescate.crearSolicitudProcesoValoresIniciales");
		return solicitudProceso;
	}
	
	private LlaveCarterasAgrupadas obtenerLlaveCarterasAgrupadasPorPortafolioInstrumento(InSolicitud solicitud) {
		logger.info("INICIO::CalculaCustodiaRescate.obtenerLlaveCarterasAgrupadasPorPortafolioInstrumento");
		LlaveCarterasAgrupadas llavePortafolioInstrumento = new LlaveCarterasAgrupadas();
		llavePortafolioInstrumento.IdPortafolio = solicitud.IdPortafolio;
		llavePortafolioInstrumento.IdInstrumento = solicitud.IdInstrumento;
		logger.info("FIN::CalculaCustodiaRescate.obtenerLlaveCarterasAgrupadasPorPortafolioInstrumento");
		return llavePortafolioInstrumento;
	}
	
	private Boolean esRescateTotalConModificacionDeCantidad(SolicitudProceso solicitudProceso) {
		logger.info("INICIO::CalculaCustodiaRescate.esRescateTotal");
		Boolean esRescateTotal = _c.CalculoPorTotal.equalsIgnoreCase(solicitudProceso.CalculoPorMontoCuota) && (solicitudProceso.IdOmnibus == null || solicitudProceso.IdOmnibus.equals(0L));
		logger.info("FIN::CalculaCustodiaRescate.esRescateTotal");
		return esRescateTotal;
	}
	
	private Boolean esCarteraAgrupadaSinSaldo(CarteraAgrupada carteraAgrupada) {
		logger.info("INICIO::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		Boolean esCarteraAgrupadaSinSaldo = carteraAgrupada == null || carteraAgrupada.CantidadTotal.compareTo(BigDecimal.ZERO) != 1;
		logger.info("FIN::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		return esCarteraAgrupadaSinSaldo;
	}
	
	private BigDecimal obtenerCantidadCarteraAgrupada(CarteraAgrupada carteraAgrupada) {
		logger.info("INICIO::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		BigDecimal cantidadCartera = carteraAgrupada == null ? BigDecimal.ZERO : carteraAgrupada.CantidadTotal;
		logger.info("FIN::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		return cantidadCartera;
	}
	
	private Boolean esCarteraSinSaldo(Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.esCarteraSinSaldo");
		Boolean esCarteraSinSaldo = cartera == null || cartera.CantidadDisponiblePortafolio.compareTo(BigDecimal.ZERO) != 1;
		logger.info("FIN::CalculaCustodiaRescate.esCarteraSinSaldo");
		return esCarteraSinSaldo;
	}
	
	private BigDecimal obtenerCantidadCartera(Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		BigDecimal cantidadCartera = cartera == null ? BigDecimal.ZERO : cartera.CantidadDisponiblePortafolio;
		logger.info("FIN::CalculaCustodiaRescate.esCarteraAgrupadaSinSaldo");
		return cantidadCartera;
	}
	
	private SolicitudProceso asignarCantidadEstadoRescateTotal(SolicitudProceso solicitudProceso, Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.asignarCantidadRescateTotal");
		
		if(esCarteraSinSaldo(cartera)) {
			solicitudProceso.EstadoProceso = _c.EstadoRechazado;
			solicitudProceso.CantidadDisponible = obtenerCantidadCartera(cartera);
		}else {
			solicitudProceso.CantidadDisponible = cartera.CantidadDisponiblePortafolio;
			solicitudProceso.EstadoProceso 		= _c.EstadoPendiente;
			
			if(esRescateInsuficiente(solicitudProceso, cartera)){
				solicitudProceso = asignarCantidadRescateInsuficiente(solicitudProceso, cartera);
			}
		}
		
		logger.info("FIN::CalculaCustodiaRescate.asignarCantidadRescateTotal");
		return solicitudProceso;
	}

	
	private SolicitudProceso asignarCantidadEstadoRescateParcial(SolicitudProceso solicitudProceso, Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.asignarCantidadEstadoRescateParcial");
		if(esCarteraSinSaldo(cartera)) {
			solicitudProceso.EstadoProceso = _c.EstadoRechazado;
			solicitudProceso.CantidadDisponible = obtenerCantidadCartera(cartera);
		}else {
			solicitudProceso.CantidadDisponible = cartera.CantidadDisponiblePortafolio;
			if(esRescateInsuficiente(solicitudProceso, cartera)){
				solicitudProceso = asignarCantidadRescateInsuficiente(solicitudProceso, cartera);
			}
		}
		logger.info("FIN::CalculaCustodiaRescate.asignarCantidadEstadoRescateParcial");
		return solicitudProceso;
	}
	
	private Boolean esRescateInsuficiente(SolicitudProceso solicitudProceso, Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.esRescateInsuficiente");
		BigDecimal cantidad = cartera.CantidadDisponiblePortafolio.subtract(solicitudProceso.NumeroCuotas, MathContext.DECIMAL128);
		Boolean esRescateInsuficiente = cantidad.compareTo(BigDecimal.ZERO) == -1 && cartera.CantidadDisponiblePortafolio.compareTo(BigDecimal.ZERO) == 1;
		logger.info("FIN::CalculaCustodiaRescate.esRescateInsuficiente");
		return esRescateInsuficiente;
	}
	
	private SolicitudProceso asignarCantidadRescateInsuficiente(SolicitudProceso solicitudProceso, Cartera cartera) {
		logger.info("INICIO::CalculaCustodiaRescate.asignarCantidadRescateTotal");
		solicitudProceso.NumeroCuotas = cartera.CantidadDisponiblePortafolio;
		solicitudProceso.EstadoProceso = _c.EstadoAdvertencia;
		logger.info("FIN::CalculaCustodiaRescate.asignarCantidadRescateTotal");
		return solicitudProceso;
	}
		
	/****** jvillagran R REVISAR 2222 ****/
	private CarteraAgrupada actualizarCarteraAgrupadaPorSolicitudRescatada(CarteraAgrupada carteraAgrupada, SolicitudProceso solicitudProceso) {
		logger.info("INICIO::CalculaCustodiaRescate.actualizarCarteraAgrupadaPorCarteraRescatada");
		if(esSolicitudValida(solicitudProceso)) {
			carteraAgrupada.CantidadTotal = carteraAgrupada.CantidadTotal.subtract(solicitudProceso.NumeroCuotas, MathContext.DECIMAL128);
			Cartera carteraActualizar = carteraAgrupada.CarterasPorRegimen.get(solicitudProceso.IdRegimenTributario);
			carteraActualizar.CantidadDisponiblePortafolio = carteraActualizar.CantidadDisponiblePortafolio.subtract(solicitudProceso.NumeroCuotas, MathContext.DECIMAL128);
			carteraAgrupada.CarterasPorRegimen.put(solicitudProceso.IdRegimenTributario, carteraActualizar);
		}
		logger.info("FIN::CalculaCustodiaRescate.actualizarCarteraAgrupadaPorCarteraRescatada");
		return carteraAgrupada;
	}
	
	private Boolean esSolicitudValida(SolicitudProceso solicitudProceso) {
		logger.info("INICIO::CalculaCustodiaRescate.esSolicitudValida");
		Boolean esSolicitudValida = !_c.EstadoRechazado.equals(solicitudProceso.EstadoProceso);
		logger.info("FIN::CalculaCustodiaRescate.esSolicitudValida");
		return esSolicitudValida;
	}
		
	private Map<Long, CalcularMontoNumeroCuota.Resultado> obtenerMontosActualizadosParaSolicitudesConAdvertenciasPendientes(Connection con, List<SolicitudProceso> solicitudesProceso, Long idEmpresa) throws Exception{
		logger.info("INICIO::CalculaCustodiaRescate.obtenerMontosActualizadosParaSolicitudesConAdvertenciasPendientes");
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		for(SolicitudProceso sol : solicitudesProceso){
			if(esSolicitudConActualizacionDeMontos(sol)){
				CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
				serie.IdInterno = sol.IdSolicitudAporteRescate;
				serie.Fecha =  Fecha.stringToSqlDate(sol.FechaValorCuota);
				serie.IdSerieFondo = sol.IdSerieFondo;
			 	serie.CalculoPorMontoCuota = _c.CalculoPorCuota;
			 	serie.Monto = sol.CostoMonedaLocal;
			 	serie.NumeroCuotas = sol.NumeroCuotas;
			 	serie.IdMonedaFondo = sol.IdMonedaFondo;
			 	serie.IdMonedaInversion = sol.IdMoneda;
			 	serie.TipoSolicitud =  sol.TipoSolicitud;
			 	serie.TipoFactorAjuste = _c.UF;
			 	serie.IdEmpresa = idEmpresa;
			 	entradaCalculo.p_Series.add(serie);
			}
		}
		SalidaCalcularMontoNumeroCuota salidaCalculo = new SalidaCalcularMontoNumeroCuota();
		if(entradaCalculo.p_Series.size() > 0){
			salidaCalculo = calculo.logica(con, entradaCalculo);
		}
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> resultadoCalculo = salidaCalculo.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
		logger.info("FIN::CalculaCustodiaRescate.obtenerMontosActualizadosParaSolicitudesConAdvertenciasPendientes");
		return resultadoCalculo;
	}
	
	private List<ResultadoCalculaCustodiaRescate> obtenerSalida(List<SolicitudProceso> solicitudesProceso, Map<Long, CalcularMontoNumeroCuota.Resultado> montosActualizadosParaSolicitudesConAdvertenciasPendientes) {
		logger.info("INICIO::CalculaCustodiaRescate.obtenerSalida");
		List<ResultadoCalculaCustodiaRescate> resultados = solicitudesProceso.parallelStream().map(solicitud->{
			ResultadoCalculaCustodiaRescate resultado = new ResultadoCalculaCustodiaRescate();
			resultado.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
			resultado.CantidadDisponible = solicitud.CantidadDisponible;
			if(esSolicitudSinActualizacionDeMontos(solicitud)){
				resultado.Monto = solicitud.Monto;
				resultado.NumeroCuotas = solicitud.NumeroCuotas;
				resultado.ValorCuota = solicitud.ValorCuota;
			 	resultado.MontoMonedaSistema = solicitud.MontoMonedaSistema;
				resultado.Estado = solicitud.EstadoProceso;
			}
			if(esSolicitudConActualizacionDeMontos(solicitud)){
				CalcularMontoNumeroCuota.Resultado cal = montosActualizadosParaSolicitudesConAdvertenciasPendientes.get(solicitud.IdSolicitudAporteRescate);
				resultado.Monto = cal.Monto;
				resultado.NumeroCuotas = cal.NumeroCuotas;
				resultado.ValorCuota = cal.ValorCuota;
			 	resultado.MontoDestino = cal.MontoDestino;
			 	resultado.ValorParidad = cal.ValorParidad;
			 	resultado.MontoMonedaSistema = cal.MontoMonedaSistema;
			 	resultado.CostoEnFactorAjuste = cal.CostoEnFactorAjuste;
			 	resultado.ValorFactorAjuste = cal.ValorFactorAjuste;
			 	resultado.Estado = esEstadoPendiente(solicitud) ? _c.EstadoAprobado : solicitud.EstadoProceso;
			}
			return resultado;
		}).collect(Collectors.toList());
		logger.info("FIN::CalculaCustodiaRescate.obtenerSalida");
		return resultados;
	}
	
	private Boolean esSolicitudSinActualizacionDeMontos(SolicitudProceso solicitud) {
		logger.info("INICIO::CalculaCustodiaRescate.esSolicitudSinActualizacionDeMontos");
		Boolean esSolicitudSinActualizacionDeMontos = _c.EstadoAprobado.equalsIgnoreCase(solicitud.EstadoProceso) || _c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso);
		logger.info("FIN::CalculaCustodiaRescate.esSolicitudSinActualizacionDeMontos");
		return esSolicitudSinActualizacionDeMontos;
	}
	
	private Boolean esSolicitudConActualizacionDeMontos(SolicitudProceso solicitud) {
		logger.info("INICIO::CalculaCustodiaRescate.esSolicitudConActualizacionDeMontos");
		Boolean esSolicitudConActualizacionDeMontos = _c.EstadoPendiente.equalsIgnoreCase(solicitud.EstadoProceso) || _c.EstadoAdvertencia.equalsIgnoreCase(solicitud.EstadoProceso);
		logger.info("FIN::CalculaCustodiaRescate.esSolicitudConActualizacionDeMontos");
		return esSolicitudConActualizacionDeMontos;
	}
	
	private Boolean esEstadoPendiente(SolicitudProceso solicitud) {
		logger.info("INICIO::CalculaCustodiaRescate.esSolicitudConActualizacionDeMontos");
		Boolean esEstadoPendiente = _c.EstadoPendiente.equalsIgnoreCase(solicitud.EstadoProceso);
		logger.info("FIN::CalculaCustodiaRescate.esSolicitudConActualizacionDeMontos");
		return esEstadoPendiente;
	}
}
