package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMMWSS.EntradaConsultarCustodiaPortafolioFFMMWSS;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMMWSS.SalidaConsultarCustodiaPortafolioFFMMWSS;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCustodiaPortafolioFondoMutuoWS implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioFondoMutuoWS.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final Long FACTOR_PARA_BUSQUEDA_APORTE_VALORIZADO = 1L;
	private final Long FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO = -1L;
	
	private final String SQL_SELECT_SOLICITUDES_WS = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuoWS_SelectSolicitudes.sql");
	private final String SQL_SELECT_SOLICITUDES_TOTALES_WS = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuoWS_SelectSolicitudesTotales.sql");
	private final String SQL_SELECT_SERIES_WS = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuoWS_SelectSeries.sql");
	private final String SQL_SELECT_REGIMEN_WS = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuoWS_SelectRegimen.sql");
	
	public static class EntradaConsultarCustodiaPortafolioFondoMutuo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdInstrumento;
		public String p_FechaConsulta;
		public Long p_IdProductoAgrupador;
		public String p_IdEmisor;
		public String p_IdFamiliaFondo;
		public String p_BuscaMLT;
		
		public EntradaConsultarCustodiaPortafolioFondoMutuo() {
			this.p_IdCliente = 0L;
			this.p_IdPortafolio = 0L;
			this.p_IdInstrumento = 0L;
			this.p_FechaConsulta = "0001-01-01";
			this.p_IdFamiliaFondo = "";
			this.p_BuscaMLT = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCustodiaPortafolioFondoMutuo{
		public List<SalidaCustodia> P_OUT_TBL;
		
		public SalidaConsultarCustodiaPortafolioFondoMutuo() {
			this.P_OUT_TBL = new ArrayList<SalidaCustodia>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCustodia{
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdSocioDeNegocio;
		public Long IdCliente;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public BigDecimal CantidadCustodia;
		public BigDecimal Monto;
		public String IdMoneda;
		public BigDecimal ValorCuota;
		public Long DecimalesValorCuota;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadTransSolicitudesAporte;
		public BigDecimal CantidadTransSolicitudesRescate;
		public BigDecimal MontoTransSolicitudesAporte;
		public BigDecimal MontoTransSolicitudesRescate;
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
		public String IdMonedaPrincipal;
		public String IdMonedaFondo;
		public String Producto;
		public String IdEmisor;
		public String MLT;
		public String IdSerieFondo;
		public Long IdEmpresa;
		public String IdFamiliaFondo;
		public String FondoExtranjero;
		public BigDecimal MontoMonedaSistema;
		public String FechaValorCuota;
		public String IdRegimenFondoMutuo;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String Fondo;
		public String NombreFondo;
		public BigDecimal ValorCuotaMonedaSistema;
		public String RegimenTributario;
		public String CodigoSerie;
		public String DocumentoIdentidadAdministradora;
		public String NombreAdministradora;
		public String RestringeRescate;
		
		
		public SalidaCustodia() {
			this.CantidadCustodia = BigDecimal.ZERO;
			this.Monto = BigDecimal.ZERO;
			this.ValorCuota = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadTransSolicitudesAporte = BigDecimal.ZERO;
			this.CantidadTransSolicitudesRescate = BigDecimal.ZERO;
			this.INGPrecioPromedioCompra = BigDecimal.ZERO;
			this.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
			this.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
			this.RegimenTributario ="";
			this.RestringeRescate  ="N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCustodiaTemp{
		public Long IdInterno;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdSocioDeNegocio;
		public Long IdCliente;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public BigDecimal CantidadCustodia;
		public BigDecimal Monto;
		public String IdMoneda;
		public BigDecimal ValorCuota;
		public Long DecimalesValorCuota;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadTransSolicitudesAporte;
		public BigDecimal CantidadTransSolicitudesRescate;
		public BigDecimal MontoTransSolicitudesAporte; //nuevos 
		public BigDecimal MontoTransSolicitudesRescate; // nuevos 
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
		public String IdMonedaPrincipal;
		public String IdMonedaFondo;
		public String Producto;
		public String IdEmisor;
		public String MLT;
		public String IdSerieFondo;
		public Long IdEmpresa;
		public String IdFamiliaFondo;
		public String FondoExtranjero;
		public BigDecimal MontoMonedaSistema;
		public String FechaValorCuota;
		public String IdRegimenFondoMutuo;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String Fondo;
		public String NombreFondo;
		public BigDecimal ValorCuotaMonedaSistema;
		public String RegimenTributario;
		public Boolean TieneRescateTotal;
		public String IdMonedaSistema;
		public String CodigoSerie;
		public String NombreAdministradora;
		public String DocumentoIdentidadAdministradora;
		public String RestringeRescate;
		
		
		public SalidaCustodiaTemp() {
			this.CantidadCustodia = BigDecimal.ZERO;
			this.Monto = BigDecimal.ZERO;
			this.ValorCuota = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadTransSolicitudesAporte = BigDecimal.ZERO;
			this.CantidadTransSolicitudesRescate = BigDecimal.ZERO;
			this.INGPrecioPromedioCompra = BigDecimal.ZERO;
			this.MontoTransSolicitudesAporte = BigDecimal.ZERO;
			this.MontoTransSolicitudesRescate = BigDecimal.ZERO;
			this.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
			this.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
			this.RegimenTributario = "";
			this.RestringeRescate  ="N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud{
		public Long IdInterno;
		public String IdSerieFondo;
		public BigDecimal CantidadAporte;
		public BigDecimal CantidadRescate;
		public BigDecimal CantidadMontoAporte;
		public BigDecimal CantidadMontoRescate;
		public BigDecimal MontoMonedaSistemaAporte;
		public Long IdCliente;
	    public Long IdPortafolio;
	    public Long IdSocioDeNegocio;
	    public String DocumentoIdentidad;
	    public String RazonSocial;
	    public String CodigoPortafolio;
	    public Long IdInstrumento;
	    public String IdEmisor;
	    public String Producto;
	    public String IdRegimenFondoMutuo;
	    public Long IdMatrizAgrupador;
	    public String EsADC;
	    public String CalculoPorMontoCuota;
	    public String RegimenTributario;
	    public String IdMonedaFondo;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SerieFondo{
		public String IdSerieFondo;
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdMoneda;
		public Long DecimalesValorCuota;
		public String IdFondo;
		public Long IdEmpresa;
		public String IdFamiliaFondo;
		public String IdEmisor;
		public String FondoExtranjero;
		public String NombreFondo;
		public String CodigoSerie;
		public String DocumentoIdentidadAdministradora;
		public String RestringeRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorCuota{
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public String FechaValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoPortafolioRegimenInstrumento{
		public String Id;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdRegimenFondoMutuo;
		public String RegimenTributario;
		public Boolean TieneRescateTotal;
		
		public Solicitud SolicitudesTransito;
		public ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS Custodia;
		
		public SaldoPortafolioRegimenInstrumento() {
			this.TieneRescateTotal = false;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		} 
	}
	
	public static class Regimen{
		public String IdSerieFondo;
		public String AceptaMandatoEnReinversion;
		public String RescateConMandato;
		
		public String toString() {
			return Utils.printJson(this, false);
		} 
	}
	
	public static class SolicitudTotal{
		public Long IdInstrumento;
		public Long IdPortafolio;
		public Long Contador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarCustodiaPortafolioFondoMutuo jin = gson.fromJson(in.toString(), EntradaConsultarCustodiaPortafolioFondoMutuo.class);
		SalidaConsultarCustodiaPortafolioFondoMutuo out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCustodiaPortafolioFondoMutuo logica(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada) throws Exception{
		logger.info("INICIO::ConsultarCustodiaPortafolioFondoMutuo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConsultarCustodiaPortafolioFondoMutuo out = new SalidaConsultarCustodiaPortafolioFondoMutuo();
		
		entrada.p_FechaConsulta = obtenerFechaConsultaReal(con, entrada);
				
		HashMap<String,Object> parametrosConsultaSolicitudes = obtenerParametrosConsultasSolicitudes(con, entrada);
		List<Solicitud> solicitudes = obtenerSolicitudesTransitoValorizadas(con, entrada, parametrosConsultaSolicitudes);
		Map<String,SolicitudTotal> rescatesTotales = obtenerSolicitudesTransitoTotales(con, parametrosConsultaSolicitudes);
		List<ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS> custodias = obtenerCustodiaPortafolio(con, entrada);
		
		HashMap<String, SaldoPortafolioRegimenInstrumento> saldoInstrumentosPorPortafolioRegimen = obtenerSaldoInstrumentosAgrupadosPorPortafolioRegimen(solicitudes, custodias, rescatesTotales);
		
		List<SalidaCustodiaTemp> salidaPreliminar = obtenerSalidaPreliminar(con, entrada, saldoInstrumentosPorPortafolioRegimen);
		salidaPreliminar = valorizarCustodiaSalidaPreliminar(con, entrada, salidaPreliminar);
		salidaPreliminar = valorizarTransitoSalidaPreliminar(con, entrada, salidaPreliminar);
		
		Map<Long, CalcularParidad.CalculoSalida> valoresCuotaMonedaSistema = obtenerValoresCuotasMonedaSistema(con, entrada, salidaPreliminar);
		
		out.P_OUT_TBL = obtenerSalidaCustodia(con, entrada.p_IdEmpresa, salidaPreliminar, valoresCuotaMonedaSistema);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarCustodiaPortafolioFondoMutuo", finServicio - inicioServicio);
		
		return out;
	}
	
	private String obtenerParametroFiltroSoloSolicitudesNoAutomaticas(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerParametroFiltroSoloSolicitudesNoAutomaticas");
		long inicio = System.currentTimeMillis();
		String filtraSoloSolicitudesNoAutomaticas = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.obtenerParametroFiltroSoloSolicitudesNoAutomaticas", fin - inicio);
		return filtraSoloSolicitudesNoAutomaticas;
	}
	
	private String obtenerFechaConsultaReal(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada)  throws Exception {
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerFechaConsultaReal");
		long inicio = System.currentTimeMillis();
		String fechaSistema = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		String fechaConsulta = _c.FechaDefecto;
		if(esFechaConsultaValida(entrada.p_FechaConsulta)) {
			fechaConsulta = fechaSistema;
		}else {
			fechaConsulta = entrada.p_FechaConsulta;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.obtenerFechaConsultaReal", fin - inicio);
		return fechaConsulta;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}
	
	private Boolean esFechaConsultaValida(String fechaConsulta) {
		logger.debug("INICIO::ConsultarSolicitudesFFMM.esFechaConsultaValida");
		long inicio = System.currentTimeMillis();
		Boolean esFechaConsultaValida = _c.FechaDefecto.equals(fechaConsulta) || fechaConsulta == null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.esFechaConsultaValida", fin - inicio);
		return esFechaConsultaValida;
	}
	
	private HashMap<String,Object> obtenerParametrosConsultasSolicitudes(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada) throws Exception{
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerParametrosConsultasSolicitudes");
		long inicio = System.currentTimeMillis();
		String filtraSoloSolicitudesNoAutomaticas = obtenerParametroFiltroSoloSolicitudesNoAutomaticas(con, entrada.p_IdEmpresa);
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		if(_c.SI.equals(filtraSoloSolicitudesNoAutomaticas)) {
			parametros.put("IngresoAutomatico", _c.NO);
			parametros.put("FiltraIngresoAutomatico", _c.SI);
		}else {
			parametros.put("IngresoAutomatico", "");
			parametros.put("FiltraIngresoAutomatico", _c.NO);
		}	
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdCliente", entrada.p_IdCliente);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.obtenerParametrosConsultasSolicitudes", fin - inicio);
		return parametros;
	}
	
	private List<Solicitud> obtenerSolicitudesTransitoValorizadas(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, HashMap<String, Object> parametrosConsultaSolicitudes) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerSolicitudesTransitoValorizadas");
		long inicio = System.currentTimeMillis();
		SQL sql=new SQL();
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES_WS, parametrosConsultaSolicitudes, Solicitud.class);
		solicitudes = asignarIdInternoSolicitudes(solicitudes);
		solicitudes = valorizarSolicitudes(con, entrada, solicitudes);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerSolicitudesTransitoValorizadas", fin - inicio);
		return solicitudes;
	}
	
	private Map<String,SolicitudTotal> obtenerSolicitudesTransitoTotales(Connection con , HashMap<String,Object> parametrosConsultaSolicitudes) throws Exception{
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerParametrosConsultasSolicitudes");
		long inicio = System.currentTimeMillis();
		SQL sql=new SQL();
		List<SolicitudTotal> solicitudesTotales = sql.query(con, SQL_SELECT_SOLICITUDES_TOTALES_WS, parametrosConsultaSolicitudes, SolicitudTotal.class);
		Map<String,SolicitudTotal> rescatesTotales = solicitudesTotales.parallelStream().collect(Collectors.toMap(solicitud->solicitud.IdPortafolio + "_" + solicitud.IdInstrumento, solicitud-> solicitud));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerSolicitudesTransitoValorizadas", fin - inicio);
		return rescatesTotales;
	}
	
	private List<Solicitud> valorizarSolicitudes(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<Solicitud> solicitudes) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.valorizarSolicitudes");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;

		List<CalcularMontoNumeroCuota.Serie> aportes = obtenerAportesEnTransitoParaValorizar(entrada, solicitudes);
		List<CalcularMontoNumeroCuota.Serie> rescates = obtenerRescatesEnTransitoParaValorizar(entrada, solicitudes);
		
		entradaCalculo.p_Series.addAll(aportes);
		entradaCalculo.p_Series.addAll(rescates);
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		solicitudes.parallelStream().forEach(solicitud->{
			solicitud = actualizarCantidadMontoAporteEnTransito(solicitud,montosValorizados);
			solicitud = actualizarCantidadMontoRescateEnTransito(solicitud,montosValorizados);
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.valorizarSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private List<Solicitud> asignarIdInternoSolicitudes(List<Solicitud> solicitudes){
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.asignarIdInternoSolicitudes");
		long inicio = System.currentTimeMillis();
		AtomicLong idInterno = new AtomicLong(1L);
		solicitudes.parallelStream().forEach(solicitud->{
			solicitud.IdInterno = idInterno.incrementAndGet();
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.asignarIdInternoSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private List<CalcularMontoNumeroCuota.Serie> obtenerAportesEnTransitoParaValorizar(EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<Solicitud> solicitudes){
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerAportesEnTransitoParaValorizar");
		long inicio = System.currentTimeMillis();
		List<CalcularMontoNumeroCuota.Serie> aportes = solicitudes.parallelStream().filter(solicitud->solicitud.CantidadAporte.compareTo(BigDecimal.ZERO) == 1).map(solicitud->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = solicitud.IdInterno * FACTOR_PARA_BUSQUEDA_APORTE_VALORIZADO;
			calculoMontoCuota.Fecha = entrada.p_FechaConsulta;
			calculoMontoCuota.IdSerieFondo = solicitud.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			calculoMontoCuota.Monto = solicitud.CantidadMontoAporte;
		 	calculoMontoCuota.NumeroCuotas = solicitud.CantidadAporte;
		 	calculoMontoCuota.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerAportesEnTransitoParaValorizar", fin - inicio);
		return aportes;
	}
	
	private List<CalcularMontoNumeroCuota.Serie> obtenerRescatesEnTransitoParaValorizar(EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<Solicitud> solicitudes){
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerRescatesEnTransitoParaValorizar");
		long inicio = System.currentTimeMillis();
		List<CalcularMontoNumeroCuota.Serie> rescates = solicitudes.parallelStream().filter(solicitud->solicitud.CantidadRescate.compareTo(BigDecimal.ZERO) == 1).map(solicitud->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = solicitud.IdInterno * FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO;
			calculoMontoCuota.Fecha = entrada.p_FechaConsulta;
			calculoMontoCuota.IdSerieFondo = solicitud.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			calculoMontoCuota.Monto = solicitud.CantidadMontoRescate;
		 	calculoMontoCuota.NumeroCuotas = solicitud.CantidadRescate;
		 	calculoMontoCuota.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudRescate;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerRescatesEnTransitoParaValorizar", fin - inicio);
		return rescates;
	}
	
	private Solicitud actualizarCantidadMontoAporteEnTransito(Solicitud solicitud, Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.actualizarCantidadMontoAporteEnTransito");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota.Resultado aporte = montosValorizados.get(solicitud.IdInterno * FACTOR_PARA_BUSQUEDA_APORTE_VALORIZADO);
		if(aporte != null) {
			solicitud.CantidadAporte = aporte.NumeroCuotas;
			solicitud.CantidadMontoAporte = _c.CalculoPorMonto.equals(solicitud.CalculoPorMontoCuota) ? solicitud.MontoMonedaSistemaAporte : aporte.MontoMonedaSistema;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.actualizarCantidadMontoAporteEnTransito",  fin - inicio);
		return solicitud;
	}
	
	private Solicitud actualizarCantidadMontoRescateEnTransito(Solicitud solicitud, Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.actualizarCantidadMontoRescateEnTransito");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota.Resultado rescate = montosValorizados.get(solicitud.IdInterno * FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO);
		if(rescate != null) {
			solicitud.CantidadRescate = rescate.NumeroCuotas;
			solicitud.CantidadMontoRescate = rescate.MontoMonedaSistema;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.actualizarCantidadMontoRescateEnTransito", fin - inicio);
		return solicitud;
	}
	
	private List<ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS> obtenerCustodiaPortafolio(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerCustodiaPortafolio");
		long inicio = System.currentTimeMillis();
		ConsultarCustodiaPortafolioFFMMWSS consultarCustodiaPortafolioFFMM = new ConsultarCustodiaPortafolioFFMMWSS();
		EntradaConsultarCustodiaPortafolioFFMMWSS entradaConsultarCustodiaPortafolioFFMM = new EntradaConsultarCustodiaPortafolioFFMMWSS();
		entradaConsultarCustodiaPortafolioFFMM.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioFFMM.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioFFMM.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaPortafolioFFMM.p_IdCliente = entrada.p_IdCliente;
		entradaConsultarCustodiaPortafolioFFMM.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaConsultarCustodiaPortafolioFFMM.p_IdProductoAgrupador = entrada.p_IdProductoAgrupador;
		entradaConsultarCustodiaPortafolioFFMM.p_IdEmisor = entrada.p_IdEmisor;
		entradaConsultarCustodiaPortafolioFFMM.p_IdInstrumento = entrada.p_IdInstrumento;
		entradaConsultarCustodiaPortafolioFFMM.p_FechaConsulta = entrada.p_FechaConsulta;
		entradaConsultarCustodiaPortafolioFFMM.p_IdFamiliaFondo = entrada.p_IdFamiliaFondo;
		SalidaConsultarCustodiaPortafolioFFMMWSS salidaConsultarCustodiaPortafolioFFMM = consultarCustodiaPortafolioFFMM.logica(con, entradaConsultarCustodiaPortafolioFFMM);
		List<ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS> custodias = salidaConsultarCustodiaPortafolioFFMM.p_out_tbl;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerCustodiaPortafolio",  fin - inicio);
		return custodias;
	}
	
	private HashMap<String, SaldoPortafolioRegimenInstrumento> obtenerSaldoInstrumentosAgrupadosPorPortafolioRegimen(List<Solicitud> solicitudes, List<ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS> custodias, Map<String,SolicitudTotal> rescateTotales){
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerInstrumentosEnCustodiaAgrupadosPorPortafolioRegimen");
		long inicio = System.currentTimeMillis();
		HashMap<String, SaldoPortafolioRegimenInstrumento> saldoInstrumentosPorPortafolioRegimen = new HashMap<String, SaldoPortafolioRegimenInstrumento>();
		for(Solicitud solicitud : solicitudes) {
			String key = solicitud.IdCliente + "_" + solicitud.IdPortafolio + "_" + solicitud.IdInstrumento + "_" + solicitud.IdRegimenFondoMutuo;
			SaldoPortafolioRegimenInstrumento instrumento = saldoInstrumentosPorPortafolioRegimen.get(key);
			if(instrumento == null) {
				instrumento = new SaldoPortafolioRegimenInstrumento();
				instrumento.Id = key;
				instrumento.IdCliente = solicitud.IdCliente;
				instrumento.IdPortafolio = solicitud.IdPortafolio;
				instrumento.IdInstrumento = solicitud.IdInstrumento;
				instrumento.IdRegimenFondoMutuo = solicitud.IdRegimenFondoMutuo;
				instrumento.RegimenTributario = solicitud.RegimenTributario;
				instrumento.SolicitudesTransito = solicitud;
			}else {
				instrumento.SolicitudesTransito.CantidadAporte = solicitud.CantidadAporte.add(instrumento.SolicitudesTransito.CantidadAporte, MathContext.DECIMAL128);
				instrumento.SolicitudesTransito.CantidadMontoAporte = solicitud.CantidadMontoAporte.add(instrumento.SolicitudesTransito.CantidadMontoAporte, MathContext.DECIMAL128);
				instrumento.SolicitudesTransito.CantidadRescate = solicitud.CantidadRescate.add(instrumento.SolicitudesTransito.CantidadRescate, MathContext.DECIMAL128);
				instrumento.SolicitudesTransito.CantidadMontoRescate = solicitud.CantidadMontoRescate.add(instrumento.SolicitudesTransito.CantidadMontoRescate, MathContext.DECIMAL128);
			}
			saldoInstrumentosPorPortafolioRegimen.put(key, instrumento);
		}
		
		for(ConsultarCustodiaPortafolioFFMMWSS.SalidaCustodiaWSS custodia : custodias) {
			String key = custodia.IdCliente + "_" + custodia.IdPortafolio + "_" + custodia.IdInstrumento + "_" + custodia.IdRegimenFondoMutuo;
			SaldoPortafolioRegimenInstrumento instrumento = saldoInstrumentosPorPortafolioRegimen.get(key);
			if(instrumento == null) {
				instrumento = new SaldoPortafolioRegimenInstrumento();
				instrumento.Id = key;
				instrumento.IdCliente = custodia.IdCliente;
				instrumento.IdPortafolio = custodia.IdPortafolio;
				instrumento.IdInstrumento = custodia.IdInstrumento;
				instrumento.IdRegimenFondoMutuo = custodia.IdRegimenFondoMutuo;
				instrumento.RegimenTributario = custodia.RegimenTributario;
			}
			instrumento.Custodia = custodia;
			saldoInstrumentosPorPortafolioRegimen.put(key, instrumento);
		}
		
		saldoInstrumentosPorPortafolioRegimen.entrySet().parallelStream().forEach(saldoInstrumento->{
			SaldoPortafolioRegimenInstrumento saldoPortafolioRegimenInstrumento = saldoInstrumento.getValue();
			String key = saldoPortafolioRegimenInstrumento.IdPortafolio + "_" + saldoPortafolioRegimenInstrumento.IdInstrumento;
			if(rescateTotales.get(key) != null) {
				saldoPortafolioRegimenInstrumento.TieneRescateTotal = true;
			}
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerInstrumentosEnCustodiaAgrupadosPorPortafolioRegimen", fin - inicio);
		return saldoInstrumentosPorPortafolioRegimen;
	}
	
	private List<SalidaCustodiaTemp> obtenerSalidaPreliminar(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, HashMap<String, SaldoPortafolioRegimenInstrumento> saldoInstrumentosPorPortafolioRegimen) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerSalidaPreliminar");
		long inicio = System.currentTimeMillis();
		List<SalidaCustodiaTemp> salidaPreliminar = new ArrayList<SalidaCustodiaTemp>();
		
		String idMonedaSistema = obtenerMonedaSistema(con, entrada.p_IdEmpresa);
		List<Long> idsInstrumentos = obtenerIdsInstrumentos(saldoInstrumentosPorPortafolioRegimen);
		List<SerieFondo> seriesFondo = obtenerSeriesFondos(con, idsInstrumentos);
		HashMap<Long,Object> seriesFondoMapeadaPorIdInstrumento = Utils.List2Hash(seriesFondo, "IdInstrumento");
		List<Regimen> regimenes = obtenerRegimenes(con, seriesFondo);
		
		AtomicLong idInterno = new AtomicLong(1L);
		for(Map.Entry<String,SaldoPortafolioRegimenInstrumento> obj : saldoInstrumentosPorPortafolioRegimen.entrySet()) {
			SaldoPortafolioRegimenInstrumento saldoPortafolioRegimenInstrumento = obj.getValue();
			SerieFondo serie = (SerieFondo) seriesFondoMapeadaPorIdInstrumento.get(saldoPortafolioRegimenInstrumento.IdInstrumento);
			
			if(serie != null) {
				SalidaCustodiaTemp salida = new SalidaCustodiaTemp();		
				salida.IdInterno = idInterno.getAndIncrement();
				salida.IdClasificacionTipoInstrumento = serie.IdClasificacionTipoInstrumento;
				salida.NombreClasificacionTipoInstrumento = serie.NombreClasificacionTipoInstrumento;
				salida.IdTipoInstrumento = serie.IdTipoInstrumento;
				salida.NombreTipoInstrumento = serie.NombreTipoInstrumento;
				salida.IdInstrumento = saldoPortafolioRegimenInstrumento.IdInstrumento;
				salida.Nemotecnico = serie.IdSerieFondo;
				salida.IdMoneda = serie.IdMoneda;
				salida.DecimalesValorCuota = serie.DecimalesValorCuota;
				salida.IdMonedaPrincipal = serie.IdMoneda;
				salida.IdMonedaFondo = serie.IdMoneda;
				salida.IdEmisor = serie.IdEmisor;
				salida.IdSerieFondo = serie.IdSerieFondo;
				salida.IdEmpresa = entrada.p_IdEmpresa;
				salida.IdFamiliaFondo = serie.IdFamiliaFondo;
				salida.FondoExtranjero = serie.FondoExtranjero;
				salida.Fondo = serie.IdFondo;
				salida.NombreFondo = serie.NombreFondo;
				salida.FechaValorCuota = "0001-01-01";
				salida.TieneRescateTotal = saldoPortafolioRegimenInstrumento.TieneRescateTotal;
				salida.IdMonedaSistema = idMonedaSistema;
				salida.CodigoSerie = serie.CodigoSerie;
				salida.DocumentoIdentidadAdministradora = serie.DocumentoIdentidadAdministradora;
				salida.NombreAdministradora = serie.IdEmisor;
				salida.RestringeRescate = serie.RestringeRescate; 
				if(saldoPortafolioRegimenInstrumento.Custodia == null) {
					salida.IdSocioDeNegocio = saldoPortafolioRegimenInstrumento.SolicitudesTransito.IdSocioDeNegocio;
					salida.IdCliente = saldoPortafolioRegimenInstrumento.SolicitudesTransito.IdCliente;
					salida.IdPortafolio = saldoPortafolioRegimenInstrumento.SolicitudesTransito.IdPortafolio;
					salida.DocumentoIdentidad = saldoPortafolioRegimenInstrumento.SolicitudesTransito.DocumentoIdentidad;
					salida.RazonSocial = saldoPortafolioRegimenInstrumento.SolicitudesTransito.RazonSocial;
					salida.CodigoPortafolio = saldoPortafolioRegimenInstrumento.SolicitudesTransito.CodigoPortafolio;
					salida.IdBoveda = "COR";
					salida.Relacion = "";
					salida.Producto = saldoPortafolioRegimenInstrumento.SolicitudesTransito.Producto;
					salida.IdRegimenFondoMutuo = saldoPortafolioRegimenInstrumento.SolicitudesTransito.IdRegimenFondoMutuo;
					salida.EsADC = saldoPortafolioRegimenInstrumento.SolicitudesTransito.EsADC;
					salida.IdMatrizAgrupador = saldoPortafolioRegimenInstrumento.SolicitudesTransito.IdMatrizAgrupador;
					salida.RegimenTributario = saldoPortafolioRegimenInstrumento.SolicitudesTransito.RegimenTributario;
				}else {
					salida.IdSocioDeNegocio = saldoPortafolioRegimenInstrumento.Custodia.IdSocioDeNegocio;
					salida.IdCliente = saldoPortafolioRegimenInstrumento.Custodia.IdCliente;
					salida.IdPortafolio = saldoPortafolioRegimenInstrumento.Custodia.IdPortafolio;
					salida.DocumentoIdentidad = saldoPortafolioRegimenInstrumento.Custodia.DocumentoIdentidad;
					salida.RazonSocial = saldoPortafolioRegimenInstrumento.Custodia.RazonSocial;
					salida.CodigoPortafolio = saldoPortafolioRegimenInstrumento.Custodia.CodigoPortafolio;
					salida.IdBoveda = saldoPortafolioRegimenInstrumento.Custodia.IdBoveda;
					salida.Relacion = saldoPortafolioRegimenInstrumento.Custodia.Relacion;
					salida.Producto = saldoPortafolioRegimenInstrumento.Custodia.Producto;
					salida.IdRegimenFondoMutuo = saldoPortafolioRegimenInstrumento.Custodia.IdRegimenFondoMutuo;
					salida.EsADC = saldoPortafolioRegimenInstrumento.Custodia.EsADC;
					salida.IdMatrizAgrupador = saldoPortafolioRegimenInstrumento.Custodia.IdMatrizAgrupador;
					salida.RegimenTributario = saldoPortafolioRegimenInstrumento.Custodia.RegimenTributario;
				}
				
				if(saldoPortafolioRegimenInstrumento.Custodia != null) {
					salida.CantidadCustodia = saldoPortafolioRegimenInstrumento.Custodia.CantidadCustodia;
					salida.CantidadGarantia = saldoPortafolioRegimenInstrumento.Custodia.CantidadGarantia;
					salida.INGPrecioPromedioCompra = saldoPortafolioRegimenInstrumento.Custodia.INGPrecioPromedioCompra;
					salida.INGMontoPrecioPromedioCompra = saldoPortafolioRegimenInstrumento.Custodia.INGMontoPrecioPromedioCompra;
					salida.INGMontoPrecioPromedioMonedaInstrumento = saldoPortafolioRegimenInstrumento.Custodia.INGMontoPrecioPromedioMonedaInstrumento;
					
				}
				
				if(saldoPortafolioRegimenInstrumento.SolicitudesTransito != null) {
					salida.CantidadTransSolicitudesAporte = saldoPortafolioRegimenInstrumento.SolicitudesTransito.CantidadAporte;
					salida.CantidadTransSolicitudesRescate = saldoPortafolioRegimenInstrumento.SolicitudesTransito.CantidadRescate;
					salida.MontoTransSolicitudesAporte = saldoPortafolioRegimenInstrumento.SolicitudesTransito.CantidadMontoAporte;
					salida.MontoTransSolicitudesRescate = saldoPortafolioRegimenInstrumento.SolicitudesTransito.CantidadMontoRescate;
				}
				
				if(saldoPortafolioRegimenInstrumento.TieneRescateTotal) {
					salida.CantidadTransSolicitudesRescate = salida.CantidadCustodia.add(salida.CantidadTransSolicitudesAporte, MathContext.DECIMAL128);
				}
				
				if(_c.SI.equals(entrada.p_BuscaMLT)) {
					for(Regimen regimen : regimenes) {
						if(regimen.IdSerieFondo.equals(serie.IdSerieFondo)) {
							salida.MLT = (_c.SI.equals(regimen.RescateConMandato) || _c.SI.equals(regimen.AceptaMandatoEnReinversion)) ? "S" : "N";
							break;
						}
					}
				}else {
					salida.MLT = "N";
				}
				salidaPreliminar.add(salida);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerSalidaPreliminar", fin - inicio);
		return salidaPreliminar;
		
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarSolicitudesFFMM.obtenerMonedaSistema");
		long inicio = System.currentTimeMillis();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSolicitudesFFMM.obtenerMonedaSistema", fin - inicio);
		return idMonedaSistema;
	}
	
	private List<Long> obtenerIdsInstrumentos(HashMap<String, SaldoPortafolioRegimenInstrumento> saldoInstrumentosPorPortafolioRegimen){
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerIdsInstrumentos");
		long inicio = System.currentTimeMillis();
		List<Long> idsInstrumentos = saldoInstrumentosPorPortafolioRegimen.entrySet().parallelStream().map(saldoInstrumento->saldoInstrumento.getValue().IdInstrumento).distinct().collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerIdsInstrumentos", fin - inicio);
		return idsInstrumentos;
	}
	
	private List<SerieFondo> obtenerSeriesFondos(Connection con, List<Long> idsInstrumentos) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerSeriesFondos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdInstrumento", idsInstrumentos);
		List<SerieFondo> seriesFondo = sql.query(con, SQL_SELECT_SERIES_WS, parametros, SerieFondo.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerSeriesFondos",  fin - inicio);
		return seriesFondo;
	}
	
	private List<Regimen> obtenerRegimenes(Connection con, List<SerieFondo> series) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerRegimenes");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> idSerieFondos = new ArrayList<String>();
		idSerieFondos = series.parallelStream().map(serie->serie.IdSerieFondo).distinct().collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdSerieFondo", idSerieFondos);
		List<Regimen> regimenes = sql.query(con, SQL_SELECT_REGIMEN_WS, parametros, Regimen.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerRegimenes", fin - inicio);
		return regimenes;
	}
	
	private List<SalidaCustodiaTemp> valorizarCustodiaSalidaPreliminar(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<SalidaCustodiaTemp> salidaPreliminar) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.valorizarCustodiaSalidaPreliminar");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		entradaCalculo.p_Series = salidaPreliminar.parallelStream().map(salida->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = salida.IdInterno;
			calculoMontoCuota.Fecha = entrada.p_FechaConsulta;
			calculoMontoCuota.IdSerieFondo = salida.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = _c.CalculoPorCuota;
			calculoMontoCuota.Monto = BigDecimal.ZERO;
		 	calculoMontoCuota.NumeroCuotas = salida.CantidadCustodia;
		 	calculoMontoCuota.IdMonedaFondo = salida.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = salida.IdMonedaFondo;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		Map<Long,CalcularMontoNumeroCuota.Resultado> hashSalidaCalculoMontoNumeroCuota = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(salidaCalculo->salidaCalculo.IdInterno, salidaCalculo->salidaCalculo));
		
		salidaPreliminar.parallelStream().forEach(salida->{
			CalcularMontoNumeroCuota.Resultado resultado = hashSalidaCalculoMontoNumeroCuota.get(salida.IdInterno);
			if(resultado != null) {
				salida.FechaValorCuota = resultado.FechaValorCuota;
				salida.Monto = resultado.Monto;
				salida.ValorCuota = resultado.ValorCuota;
				salida.MontoMonedaSistema = resultado.MontoMonedaSistema;
			}
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.valorizarCustodiaSalidaPreliminar", fin - inicio);
		return salidaPreliminar;
	}
	
	private List<SalidaCustodiaTemp> valorizarTransitoSalidaPreliminar(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<SalidaCustodiaTemp> salidaPreliminar) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.valorizarTransitoSalidaPreliminar");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		entradaCalculo.p_Series = salidaPreliminar.parallelStream().filter(salida->BigDecimal.ZERO.compareTo(salida.CantidadTransSolicitudesRescate) != 0).map(salida->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = salida.IdInterno * FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO;
			calculoMontoCuota.Fecha = entrada.p_FechaConsulta;
			calculoMontoCuota.IdSerieFondo = salida.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = _c.CalculoPorCuota;
			calculoMontoCuota.Monto = BigDecimal.ZERO;
		 	calculoMontoCuota.NumeroCuotas = salida.CantidadTransSolicitudesRescate;
		 	calculoMontoCuota.IdMonedaFondo = salida.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = salida.IdMonedaFondo;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		Map<Long,CalcularMontoNumeroCuota.Resultado> hashSalidaCalculoMontoNumeroCuota = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(salidaCalculo->salidaCalculo.IdInterno, salidaCalculo->salidaCalculo));
		
		salidaPreliminar.parallelStream().forEach(salida->{
			CalcularMontoNumeroCuota.Resultado resultadoRescate = hashSalidaCalculoMontoNumeroCuota.get(salida.IdInterno * FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO);
			if(resultadoRescate != null) {
				salida.MontoTransSolicitudesRescate = resultadoRescate.MontoMonedaSistema;
			}
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.valorizarTransitoSalidaPreliminar", fin - inicio);
		return salidaPreliminar;
	}
	
	private Map<Long, CalcularParidad.CalculoSalida> obtenerValoresCuotasMonedaSistema(Connection con, EntradaConsultarCustodiaPortafolioFondoMutuo entrada, List<SalidaCustodiaTemp> salidaPreliminar) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerValoresCuotasMonedaSistema");
		long inicio = System.currentTimeMillis();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 2L;
		
		String calculaValorCuotaDiaSiguiente = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.CalculaValorCuotaDiaSiguiente");
		
		Map<String, String> fechasValorCuotaDiaSiguiente = obtenerDiasSiguiente(con, entrada.p_IdEmpresa, salidaPreliminar);
		
		if("S".equals(calculaValorCuotaDiaSiguiente)) {
			calcularParidadEntrada.p_CalculosEntrada = salidaPreliminar.parallelStream().map(salida->{
				String fechaValorCuotaDiaSiguiente = fechasValorCuotaDiaSiguiente.get(salida.FechaValorCuota);
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 2L;
				calculoEntrada.IdInterno = salida.IdInterno;
				calculoEntrada.Fecha = fechaValorCuotaDiaSiguiente != null ? fechaValorCuotaDiaSiguiente : entrada.p_FechaConsulta;
				calculoEntrada.IdMonedaOrigen = salida.IdMonedaFondo;
				calculoEntrada.IdMonedaDestino = salida.IdMonedaSistema;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen =  salida.ValorCuota;
				calculoEntrada.IdUso = "";
			 	return calculoEntrada;
			}).collect(Collectors.toList());
		}
		else{
			calcularParidadEntrada.p_CalculosEntrada = salidaPreliminar.parallelStream().map(salida->{
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 2L;
				calculoEntrada.IdInterno = salida.IdInterno;
				calculoEntrada.Fecha = entrada.p_FechaConsulta;
				calculoEntrada.IdMonedaOrigen = salida.IdMonedaFondo;
				calculoEntrada.IdMonedaDestino = salida.IdMonedaSistema;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen =  salida.ValorCuota;
				calculoEntrada.IdUso = "";
			 	return calculoEntrada;
			}).collect(Collectors.toList());
			
		}
		
		CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		Map<Long, CalcularParidad.CalculoSalida> valoresCuotaMonedaSistema = calculoSalida.p_out_CalculosSalida.parallelStream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerValoresCuotasMonedaSistema", fin - inicio);
		return valoresCuotaMonedaSistema;
	}
	
	private Map<String, String> obtenerDiasSiguiente(Connection con, Long idEmpresa, List<SalidaCustodiaTemp> salidaPreliminar) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerDiasSiguiente");
		long inicio = System.currentTimeMillis();
		List<String> fechas = salidaPreliminar.parallelStream()
				.map(salida->{
					return salida.FechaValorCuota;
				})
				.distinct()
				.collect(Collectors.toList());
		
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, idEmpresa);
		
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_diaHabil = _c.SI;
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = _c.SI;
		inObtenerDiaSiguiente.p_fechas = fechas.parallelStream()
				.map(fecha->{
					ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fechaConsulta = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
					fechaConsulta.fechaInicial = fecha;
					fechaConsulta.numeroDias = 1L;
					fechaConsulta.pais = idPaisEmpresa;
					return fechaConsulta;
				})
				.collect(Collectors.toList());
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		
		Map<String, String> fechasValorCuotaDiaSiguiente = outDiaSiguiente.p_out_fechas.stream()
				.collect(Collectors.toMap(fecha->fecha.fechaInicial,fecha->fecha.fechaCalculada));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerDiasSiguiente", fin - inicio);
		return fechasValorCuotaDiaSiguiente;
	}
	
	private List<SalidaCustodia> obtenerSalidaCustodia(Connection con, Long idEmpresa, List<SalidaCustodiaTemp> salidaPreliminar, Map<Long, CalcularParidad.CalculoSalida> valoresCuotaMonedaSistema) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.obtenerSalidaCustodia");
		long inicio = System.currentTimeMillis();
		String monedaSistema = obtenerMonedaSistema(con, idEmpresa);
		List<SalidaCustodia> salidaCustodia = salidaPreliminar.parallelStream().map(salidaTemp->{
			CalcularParidad.CalculoSalida valorCuotaMonedaSistema = valoresCuotaMonedaSistema.get(salidaTemp.IdInterno);
			SalidaCustodia salida = new SalidaCustodia();
			salida.IdClasificacionTipoInstrumento = salidaTemp.IdClasificacionTipoInstrumento;
			salida.NombreClasificacionTipoInstrumento = salidaTemp.NombreClasificacionTipoInstrumento;
			salida.IdTipoInstrumento = salidaTemp.IdTipoInstrumento;
			salida.NombreTipoInstrumento = salidaTemp.NombreTipoInstrumento;
			salida.IdInstrumento = salidaTemp.IdInstrumento;
			salida.Nemotecnico = salidaTemp.Nemotecnico;
			salida.IdSocioDeNegocio = salidaTemp.IdSocioDeNegocio;
			salida.IdCliente = salidaTemp.IdCliente;
			salida.IdPortafolio = salidaTemp.IdPortafolio;
			salida.DocumentoIdentidad = salidaTemp.DocumentoIdentidad;
			salida.RazonSocial = salidaTemp.RazonSocial;
			salida.CodigoPortafolio = salidaTemp.CodigoPortafolio;
			salida.IdBoveda = salidaTemp.IdBoveda;
			salida.Relacion = salidaTemp.Relacion;
			salida.CantidadCustodia = salidaTemp.CantidadCustodia;
			salida.Monto = salidaTemp.Monto;
			salida.IdMoneda = salidaTemp.IdMoneda;
			salida.ValorCuota = salidaTemp.ValorCuota;
			salida.DecimalesValorCuota = salidaTemp.DecimalesValorCuota;
			salida.CantidadGarantia = salidaTemp.CantidadGarantia;
			salida.CantidadTransSolicitudesAporte = salidaTemp.CantidadTransSolicitudesAporte;
			salida.CantidadTransSolicitudesRescate = salidaTemp.CantidadTransSolicitudesRescate;
			salida.MontoTransSolicitudesAporte = salidaTemp.MontoTransSolicitudesAporte;
			salida.MontoTransSolicitudesRescate = salidaTemp.MontoTransSolicitudesRescate;
			salida.INGPrecioPromedioCompra = salidaTemp.INGPrecioPromedioCompra;
			salida.INGMontoPrecioPromedioCompra = salidaTemp.INGMontoPrecioPromedioCompra;
			salida.INGMontoPrecioPromedioMonedaInstrumento = salidaTemp.INGMontoPrecioPromedioMonedaInstrumento;
			salida.IdMonedaPrincipal = salidaTemp.IdMonedaPrincipal;
			salida.IdMonedaFondo = salidaTemp.IdMonedaFondo;
			salida.Producto = salidaTemp.Producto;
			salida.IdEmisor = salidaTemp.IdEmisor;
			salida.MLT = salidaTemp.MLT;
			salida.IdSerieFondo = salidaTemp.IdSerieFondo;
			salida.IdEmpresa = salidaTemp.IdEmpresa;
			salida.IdFamiliaFondo = salidaTemp.IdFamiliaFondo;
			salida.FondoExtranjero = salidaTemp.FondoExtranjero;
			salida.FechaValorCuota = salidaTemp.FechaValorCuota;
			salida.IdRegimenFondoMutuo = salidaTemp.IdRegimenFondoMutuo;
			salida.IdMatrizAgrupador = salidaTemp.IdMatrizAgrupador;
			salida.EsADC = salidaTemp.EsADC;
			salida.Fondo = salidaTemp.Fondo;
			salida.NombreFondo = salidaTemp.NombreFondo;
			salida.RegimenTributario = salidaTemp.RegimenTributario;
		 	salida.ValorCuotaMonedaSistema = esValorCuotaMonedaSistemaValido(valorCuotaMonedaSistema) ? valorCuotaMonedaSistema.MontoDestino : BigDecimal.ZERO;
		 	salida.CodigoSerie = salidaTemp.CodigoSerie;
		 	try {
				salida.MontoMonedaSistema = MonedaFormato.formatear(con, salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema), MonedaFormato.MONTO, monedaSistema).valor;
			} catch (Exception e) {
				salida.MontoMonedaSistema = salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema);
			}
		 	salida.DocumentoIdentidadAdministradora = salidaTemp.DocumentoIdentidadAdministradora;
		 	salida.NombreAdministradora = salidaTemp.IdEmisor;
		 	salida.RestringeRescate = salidaTemp.RestringeRescate;
			return salida;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.obtenerSalidaCustodia", fin - inicio);
		return salidaCustodia;
	}
	
	private Boolean esValorCuotaMonedaSistemaValido(CalcularParidad.CalculoSalida valorCuotaMonedaSistema) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioFondoMutuo.esValorCuotaMonedaSistemaValido");
		long inicio = System.currentTimeMillis();
		Boolean esValorCuotaMonedaSistemaValido = valorCuotaMonedaSistema != null && valorCuotaMonedaSistema.MontoDestino != null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioFondoMutuo.esValorCuotaMonedaSistemaValido", fin - inicio);
		return esValorCuotaMonedaSistemaValido;
	}
	
	private Long obtenerIdPaisEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerIdPaisEmpresa");
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresa.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerIdPaisEmpresa");
		return idPaisEmpresa;
	}
	
	//{"p_IdEmpresa":1,"p_IdModulo":"FMM","p_FechaConsulta":"0001-01-01","p_IdCliente":34726,"p_IdPortafolio":0,"p_IdInstrumento":0,"p_IdBoveda":"","p_AgrupaPorBoveda":"N","p_IdProductoAgrupador":null}
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":27,\"p_IdModulo\":\"FMM\",\"p_FechaConsulta\":\"0001-01-01\",\"p_IdCliente\":1555,\"p_IdPortafolio\":0,\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"N\",\"p_IdProductoAgrupador\":null}";
			String json = "{\"p_IdEmpresa\":\"1\",\"p_IdModulo\":\"FMM\",\"p_FechaConsulta\":\"0001-01-01\",\"p_IdCliente\":34726,\"p_IdPortafolio\":0,\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"N\",\"p_IdProductoAgrupador\":null}";
			EntradaConsultarCustodiaPortafolioFondoMutuo Objin = gson.fromJson(json, EntradaConsultarCustodiaPortafolioFondoMutuo.class);
			ConsultarCustodiaPortafolioFondoMutuoWS  demo=new ConsultarCustodiaPortafolioFondoMutuoWS();
			try {
				SalidaConsultarCustodiaPortafolioFondoMutuo ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_TBL);
				System.out.println(ObjOut.P_OUT_TBL.size());
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
