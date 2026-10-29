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
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSolicitudesCertificacionMasiva implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarSolicitudesCertificacionMasiva.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarSolicitudesCertificacionMasiva_SelectSolicitudes.sql");
	
	public static class EntradaConsultarSolicitudesCertificacionMasiva extends BcsPojo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_IdEmisor;
	    public String p_IdMonedaFondo;
	    public String p_IdSerieFondo;
	    public String p_EsNacional;
	    public String p_TipoSolicitud;
	    public String p_IdTipoMovimientoFondoMutuo;
	    public String p_MoneyMarket;
	    public String p_FechaCertificacion;
	    
	    public EntradaConsultarSolicitudesCertificacionMasiva() {
	    	this.p_IdEmisor = "";
	    	this.p_IdMonedaFondo = "";
	    	this.p_IdSerieFondo = "";
	    	this.p_EsNacional = "";
	    	this.p_TipoSolicitud = "";
	    	this.p_IdTipoMovimientoFondoMutuo = "";
	    	this.p_MoneyMarket = "";
	    	this.p_FechaCertificacion = "0001-01-01";
	    }
	}
	
	public static class SalidaConsultarSolicitudesCertificacionMasiva extends BcsPojo{
		public List<SolicitudAgrupadaDatosGenerales> P_OUT_RESULTADO;
		
		public SalidaConsultarSolicitudesCertificacionMasiva() {
			this.P_OUT_RESULTADO = new ArrayList<SolicitudAgrupadaDatosGenerales>();
		}
	}
	
	public static class DatosGeneralesSolicitud extends BcsPojo{
		public String TipoSolicitud;
		public String NombreTipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String DescripcionTipoMovimiento;
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public java.sql.Date FechaCertificacion;
		public java.sql.Date FechaCierre;
		public String IdMonedaFondo;
		public String IdMoneda;
		public Long IdEmpresa;
		public Long DecimalesValorCuota;
		public String IdEmisor;
		public String EsFondoNacional;
		public String EsFondoNacionalUI;
		public String MoneyMarket;
		public String EsMoneyMarketUI;
		
		public DatosGeneralesSolicitud() {
			this.ValorCuota = BigDecimal.ZERO;
		}
	
		@Override
		public int hashCode() {
			return 31 * this.TipoSolicitud.hashCode() 
					+ this.NombreTipoSolicitud.hashCode() 
					+ this.IdTipoMovimientoFondoMutuo.hashCode() 
					+ this.DescripcionTipoMovimiento.hashCode() 
					+ this.IdSerieFondo.hashCode()
					+ this.ValorCuota.hashCode()
					+ this.FechaCertificacion.hashCode()
					+ this.FechaCierre.hashCode()
					+ this.IdMonedaFondo.hashCode()
					+ this.IdMoneda.hashCode()
					+ this.IdEmpresa.hashCode()
					+ this.DecimalesValorCuota.hashCode()
					+ this.IdEmisor.hashCode()
					+ this.EsFondoNacional.hashCode()
					+ this.EsFondoNacionalUI.hashCode()
					+ this.MoneyMarket.hashCode()
					+ this.EsMoneyMarketUI.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			DatosGeneralesSolicitud dato = (DatosGeneralesSolicitud) obj;
			return 	this.TipoSolicitud.equals(dato.TipoSolicitud) && 
					this.NombreTipoSolicitud.equals(dato.NombreTipoSolicitud) && 
					this.DescripcionTipoMovimiento.equals(dato.DescripcionTipoMovimiento) &&
					this.IdSerieFondo.equals(dato.IdSerieFondo) && 
					this.ValorCuota.equals(dato.ValorCuota) &&
					this.FechaCertificacion.equals(dato.FechaCertificacion) &&
					this.FechaCierre.equals(dato.FechaCierre) && 
					this.IdMonedaFondo.equals(dato.IdMonedaFondo) &&
					this.IdMoneda.equals(dato.IdMoneda) &&
					this.IdEmpresa.equals(dato.IdEmpresa) &&
					this.DecimalesValorCuota.equals(dato.DecimalesValorCuota) &&
					this.IdEmisor.equals(dato.IdEmisor) &&
					this.EsFondoNacional.equals(dato.EsFondoNacional) &&
					this.EsFondoNacionalUI.equals(dato.EsFondoNacionalUI) &&
					this.MoneyMarket.equals(dato.MoneyMarket) &&
					this.EsMoneyMarketUI.equals(dato.EsMoneyMarketUI);
		} 
	}
	
	public static class SolicitudAgrupadaPorDatosGeneralesTipoCalculo extends DatosGeneralesSolicitud{
		public Long IdInterno;
		public String CalculoPorMontoCuota;
		public BigDecimal NumeroCuotas;
		public BigDecimal Monto;
		public BigDecimal MontoValorizado;
		public Long CantidadRegistros;
		
		public SolicitudAgrupadaPorDatosGeneralesTipoCalculo() {
			this.MontoValorizado = BigDecimal.ZERO;
		}
	}
	
	public static class SolicitudAgrupadaDatosGenerales extends DatosGeneralesSolicitud{
		public Long CantidadRegistros;
		public BigDecimal Monto;
		
		public SolicitudAgrupadaDatosGenerales() {
			this.CantidadRegistros = 0L;
			this.Monto = BigDecimal.ZERO;
		}
	}
	
	public static class FiltrosValidos extends BcsPojo{
		public Long FiltraPorIdEmisor;
		public Long FiltraPorIdMonedaFondo;
		public Long FiltraPorIdSerieFondo;
		public Long FiltraPorEsNacional;
		public Long FiltraPorIdTipoMovimientoFondoMutuo;
		public Long FiltraPorMoneyMarket;
		
		public FiltrosValidos() {
			this.FiltraPorIdEmisor = 0L;
			this.FiltraPorIdMonedaFondo = 0L;
			this.FiltraPorIdSerieFondo = 0L;
			this.FiltraPorEsNacional = 0L;
			this.FiltraPorIdTipoMovimientoFondoMutuo = 0L;
			this.FiltraPorMoneyMarket = 0L;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarSolicitudesCertificacionMasiva jin = gson.fromJson(in.toString(), EntradaConsultarSolicitudesCertificacionMasiva.class);
		SalidaConsultarSolicitudesCertificacionMasiva out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarSolicitudesCertificacionMasiva logica(Connection con, EntradaConsultarSolicitudesCertificacionMasiva entrada) throws Exception{
		logger.info("INICIO::ConsultarSolicitudesCertificacionMasiva");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarSolicitudesCertificacionMasiva out = new SalidaConsultarSolicitudesCertificacionMasiva();
		List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudes = obtenerSolicitudesSinValorizar(con, entrada);
		solicitudes = asignarIdInternoSolicitudes(solicitudes);
		solicitudes = valorizarSolicitudes(con, entrada.p_IdEmpresa, solicitudes);
		out.P_OUT_RESULTADO = agruparPorDatosGenerales(solicitudes);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarSolicitudesCertificacionMasiva", finServicio - inicioServicio);
		
		return out;
	}
	
	private List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> obtenerSolicitudesSinValorizar(Connection con, EntradaConsultarSolicitudesCertificacionMasiva entrada) throws Exception{
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.obtenerSolicitudesSinValorizar");
		long inicio = System.currentTimeMillis();
		SQL sql=new SQL();
		HashMap<String,Object> parametrosConsulta = obtenerParametrosConsulta(entrada);
		List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudesSinValorizar = sql.query(con, SQL_SELECT_SOLICITUDES, parametrosConsulta, SolicitudAgrupadaPorDatosGeneralesTipoCalculo.class);
		solicitudesSinValorizar = asignarIdInternoSolicitudes(solicitudesSinValorizar);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.obtenerSolicitudesSinValorizar", fin - inicio);
		return solicitudesSinValorizar;
	}
	
	private HashMap<String,Object> obtenerParametrosConsulta(EntradaConsultarSolicitudesCertificacionMasiva entrada){
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.obtenerParametrosConsulta");
		long inicio = System.currentTimeMillis();
		FiltrosValidos filtrosValidos = new FiltrosValidos();
		HashMap<String,Object> parametrosConsulta = new HashMap<String, Object>();
		parametrosConsulta.put("FechaCertificacion", entrada.p_FechaCertificacion);
		parametrosConsulta.put("IdEmisor", entrada.p_IdEmisor);
		parametrosConsulta.put("IdMonedaFondo", entrada.p_IdMonedaFondo);
		parametrosConsulta.put("IdSerieFondo", entrada.p_IdSerieFondo);
		parametrosConsulta.put("EsNacional", entrada.p_EsNacional);
		parametrosConsulta.put("IdTipoMovimientoFondoMutuo", entrada.p_IdTipoMovimientoFondoMutuo);
		parametrosConsulta.put("MoneyMarket", entrada.p_MoneyMarket);
		
		if(esFiltroValido(entrada.p_IdEmisor)) {
			filtrosValidos.FiltraPorIdEmisor = 1L;
		}
		if(esFiltroValido(entrada.p_IdMonedaFondo)) {
			filtrosValidos.FiltraPorIdMonedaFondo = 1L;
		}
		if(esFiltroValido(entrada.p_IdSerieFondo)) {
			filtrosValidos.FiltraPorIdSerieFondo = 1L;
		}
		if(esFiltroValido(entrada.p_EsNacional)) {
			filtrosValidos.FiltraPorEsNacional = 1L;
		}
		if(esFiltroValido(entrada.p_IdTipoMovimientoFondoMutuo)) {
			filtrosValidos.FiltraPorIdTipoMovimientoFondoMutuo = 1L;
		}
		if(esFiltroValido(entrada.p_MoneyMarket)) {
			filtrosValidos.FiltraPorMoneyMarket = 1L;
		}
		parametrosConsulta.put("FiltraPorIdEmisor", filtrosValidos.FiltraPorIdEmisor);
		parametrosConsulta.put("FiltraPorIdMonedaFondo", filtrosValidos.FiltraPorIdMonedaFondo);
		parametrosConsulta.put("FiltraPorIdSerieFondo", filtrosValidos.FiltraPorIdSerieFondo);
		parametrosConsulta.put("FiltraPorEsNacional", filtrosValidos.FiltraPorEsNacional);
		parametrosConsulta.put("FiltraPorIdTipoMovimientoFondoMutuo", filtrosValidos.FiltraPorIdTipoMovimientoFondoMutuo);
		parametrosConsulta.put("FiltraPorMoneyMarket", filtrosValidos.FiltraPorMoneyMarket);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.obtenerParametrosConsulta", fin - inicio);
		return parametrosConsulta;
	}
	
	private Boolean esFiltroValido(String filtro) {
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.esFiltroValido");
		long inicio = System.currentTimeMillis();
		Boolean esFiltroValido = filtro != null && !filtro.isEmpty();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.esFiltroValido", fin - inicio);
		return esFiltroValido;
	}
	
	private List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> asignarIdInternoSolicitudes(List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.asignarIdInternoSolicitudes");
		long inicio = System.currentTimeMillis();
		AtomicLong idInterno = new AtomicLong(1L);
		solicitudes.parallelStream().forEach(solicitud->{
			solicitud.IdInterno = idInterno.incrementAndGet();
		});
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.asignarIdInternoSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> valorizarSolicitudes(Connection con, Long idEmpresa, List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudes) throws Exception {
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.valorizarSolicitudes");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		
		entradaCalculo.p_Series = solicitudes.parallelStream().map(solicitud->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = solicitud.IdInterno;
			calculoMontoCuota.Fecha = Fecha.stringToSqlDate(solicitud.FechaCierre);
			calculoMontoCuota.IdSerieFondo = solicitud.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			calculoMontoCuota.Monto = solicitud.Monto;
		 	calculoMontoCuota.NumeroCuotas = solicitud.NumeroCuotas;
		 	calculoMontoCuota.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = solicitud.IdMoneda;
		 	calculoMontoCuota.TipoSolicitud =  solicitud.TipoSolicitud;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = idEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		solicitudes.parallelStream().forEach(solicitud->{
			CalcularMontoNumeroCuota.Resultado resultadoValorizacion = montosValorizados.get(solicitud.IdInterno);
			if(esValorizacionCorrecta(resultadoValorizacion)) {
				solicitud.ValorCuota = esValorCuotaCorrecto(resultadoValorizacion.ValorCuota) ? resultadoValorizacion.ValorCuota : BigDecimal.ZERO;
				solicitud.MontoValorizado = resultadoValorizacion.MontoDestino;
			}
		});
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.valorizarSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private Boolean esValorizacionCorrecta(CalcularMontoNumeroCuota.Resultado resultadoValorizacion) {
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.esValorizacionCorrecta");
		long inicio = System.currentTimeMillis();
		Boolean esValorizacionCorrecta = resultadoValorizacion != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.esValorizacionCorrecta", fin - inicio);
		return esValorizacionCorrecta;
	}
	
	private Boolean esValorCuotaCorrecto(BigDecimal valorCuota) {
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.esValorCuotaCorrecto");
		long inicio = System.currentTimeMillis();
		Boolean esValorCuotaCorrecto = valorCuota != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.esValorCuotaCorrecto", fin - inicio);
		return esValorCuotaCorrecto;
	}
	
	public List<SolicitudAgrupadaDatosGenerales> agruparPorDatosGenerales(List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesCertificacionMasiva.agruparPorDatosGenerales");
		long inicio = System.currentTimeMillis();
		Map<DatosGeneralesSolicitud,List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo>> solicitudesTipoCalculoAgrupadaPorDatosGenerales = solicitudes.parallelStream().collect(Collectors.groupingBy(solicitud->{
			DatosGeneralesSolicitud datosGeneralesSolicitud = new DatosGeneralesSolicitud();
			datosGeneralesSolicitud.TipoSolicitud = solicitud.TipoSolicitud;
			datosGeneralesSolicitud.NombreTipoSolicitud = solicitud.NombreTipoSolicitud;
			datosGeneralesSolicitud.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
			datosGeneralesSolicitud.DescripcionTipoMovimiento = solicitud.DescripcionTipoMovimiento;
			datosGeneralesSolicitud.IdSerieFondo = solicitud.IdSerieFondo;
			datosGeneralesSolicitud.ValorCuota = solicitud.ValorCuota;
			datosGeneralesSolicitud.FechaCertificacion = solicitud.FechaCertificacion;
			datosGeneralesSolicitud.FechaCierre = solicitud.FechaCierre;
			datosGeneralesSolicitud.IdMonedaFondo = solicitud.IdMonedaFondo;
			datosGeneralesSolicitud.IdMoneda = solicitud.IdMoneda;
			datosGeneralesSolicitud.IdEmpresa = solicitud.IdEmpresa;
			datosGeneralesSolicitud.DecimalesValorCuota = solicitud.DecimalesValorCuota;
			datosGeneralesSolicitud.IdEmisor = solicitud.IdEmisor;
			datosGeneralesSolicitud.EsFondoNacional = solicitud.EsFondoNacional;
			datosGeneralesSolicitud.EsFondoNacionalUI = solicitud.EsFondoNacionalUI;
			datosGeneralesSolicitud.MoneyMarket = solicitud.MoneyMarket;
			datosGeneralesSolicitud.EsMoneyMarketUI = solicitud.EsMoneyMarketUI;
			return datosGeneralesSolicitud;
		}));
		
		List<SolicitudAgrupadaDatosGenerales> solicitudesAgrupadasPorDatosGenerales = solicitudesTipoCalculoAgrupadaPorDatosGenerales.entrySet().parallelStream().map(solicitudAgrupada->{
			DatosGeneralesSolicitud datosGeneralesSolicitud = solicitudAgrupada.getKey();
			List<SolicitudAgrupadaPorDatosGeneralesTipoCalculo> solicitudesAgrupadas = solicitudAgrupada.getValue();
			SolicitudAgrupadaDatosGenerales solicitudAgrupadaDatosGenerales = new SolicitudAgrupadaDatosGenerales();
			solicitudAgrupadaDatosGenerales.TipoSolicitud = datosGeneralesSolicitud.TipoSolicitud;
			solicitudAgrupadaDatosGenerales.NombreTipoSolicitud = datosGeneralesSolicitud.NombreTipoSolicitud;
			solicitudAgrupadaDatosGenerales.IdTipoMovimientoFondoMutuo = datosGeneralesSolicitud.IdTipoMovimientoFondoMutuo;
			solicitudAgrupadaDatosGenerales.DescripcionTipoMovimiento = datosGeneralesSolicitud.DescripcionTipoMovimiento;
			solicitudAgrupadaDatosGenerales.IdSerieFondo = datosGeneralesSolicitud.IdSerieFondo;
			solicitudAgrupadaDatosGenerales.ValorCuota = datosGeneralesSolicitud.ValorCuota;
			solicitudAgrupadaDatosGenerales.FechaCertificacion = datosGeneralesSolicitud.FechaCertificacion;
			solicitudAgrupadaDatosGenerales.FechaCierre = datosGeneralesSolicitud.FechaCierre;
			solicitudAgrupadaDatosGenerales.IdMonedaFondo = datosGeneralesSolicitud.IdMonedaFondo;
			solicitudAgrupadaDatosGenerales.IdMoneda = datosGeneralesSolicitud.IdMoneda;
			solicitudAgrupadaDatosGenerales.IdEmpresa = datosGeneralesSolicitud.IdEmpresa;
			solicitudAgrupadaDatosGenerales.DecimalesValorCuota = datosGeneralesSolicitud.DecimalesValorCuota;
			solicitudAgrupadaDatosGenerales.IdEmisor = datosGeneralesSolicitud.IdEmisor;
			solicitudAgrupadaDatosGenerales.EsFondoNacional = datosGeneralesSolicitud.EsFondoNacional;
			solicitudAgrupadaDatosGenerales.EsFondoNacionalUI = datosGeneralesSolicitud.EsFondoNacionalUI;
			solicitudAgrupadaDatosGenerales.MoneyMarket = datosGeneralesSolicitud.MoneyMarket;
			solicitudAgrupadaDatosGenerales.EsMoneyMarketUI = datosGeneralesSolicitud.EsMoneyMarketUI;
			
			for(SolicitudAgrupadaPorDatosGeneralesTipoCalculo solicitud : solicitudesAgrupadas) {
				solicitudAgrupadaDatosGenerales.CantidadRegistros += solicitud.CantidadRegistros;
				solicitudAgrupadaDatosGenerales.Monto = solicitudAgrupadaDatosGenerales.Monto.add(solicitud.MontoValorizado, MathContext.DECIMAL128);
			}
			
			return solicitudAgrupadaDatosGenerales;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudesCertificacionMasiva.agruparPorDatosGenerales", fin - inicio);
		return solicitudesAgrupadasPorDatosGenerales;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaCertificacion\":\"2020-06-08\"}";
			EntradaConsultarSolicitudesCertificacionMasiva Objin = gson.fromJson(json, EntradaConsultarSolicitudesCertificacionMasiva.class);
			ConsultarSolicitudesCertificacionMasiva  demo=new ConsultarSolicitudesCertificacionMasiva();				
			try {
				SalidaConsultarSolicitudesCertificacionMasiva ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_RESULTADO.toString());
				System.out.println(ObjOut.P_OUT_RESULTADO.size());
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
