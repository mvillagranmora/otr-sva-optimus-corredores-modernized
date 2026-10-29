package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;


public class CalcularFechaSolicitudAporteRescate implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(CalcularFechaSolicitudAporteRescate.class);
	private final String SQL_SELECT_CONFIGURACION_FECHAS_FFMM = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularFechaSolicitudAporteRescate_SelectConfiguracionFechasFFMM.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_DIAESFERIADO = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularFechaSolicitudAporteRescate_SelectDiaEsFeriado.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCalcularFechaSolicitudAporteRescate{
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public List<InFechas> p_SolicitudAporteRescate;
		
		public EntradaCalcularFechaSolicitudAporteRescate() {
			this.p_SolicitudAporteRescate = new ArrayList<InFechas>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFechas{
		public Long IdInterno;
		public String IdSerieFondo;
		public String FechaEmision;
		public String EstadoFondo;
		public String EstadoLiquidacion;
		public Long NumeroDiasRetencion;
		public Long NumeroValutaValorCuota;
		public Long NumeroValutaCertificacion;
		public Long NumeroValutaCurse;
		public String SolicitudBatch;
		public String TipoSolicitud;
		public String FondoExtranjero;
		public Long IdPaisFondo;
		public Long IdPaisEmisor;
		public Long IdPaisMoneda;
		//REINVERSIONES
		public String Switch;
		public String RescateGeneraSpotCertificacion;
		public String FechaCertificacionRescateOrigen;
		public String FechaLiquidacionRescateOrigen;
		public String FechaValorCuotaRescateOrigen;
		public String FondoNacionalDolar;
		public String IdMercadoFondoMutuo;
		public InFechas() {
			this.Switch = "N";
			this.RescateGeneraSpotCertificacion = "N";
			this.FechaCertificacionRescateOrigen = "0001-01-01";
			this.FechaLiquidacionRescateOrigen = "0001-01-01";
			this.FechaValorCuotaRescateOrigen = "0001-01-01";
			this.IdMercadoFondoMutuo = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ProcesamientoFechas{
		public Long IdInterno;
		public String IdSerieFondo;
		public String FechaInicial;
		public String FechaEmision;
		public String FechaDigitacion;
		public String FechaCurse;
		public String FechaValorCuota;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String EstadoFondo;
		public String EstadoLiquidacion;
		public Long NumeroDiasRetencion;
		public Long NumeroValutaValorCuota;
		public Long NumeroValutaCertificacion;
		public Long NumeroValutaCurse;
		public String SolicitudBatch;
		public String TipoSolicitud;
		public String FondoExtranjero;
		public Long IdPaisFondo;
		public Long IdPaisEmisor;
		public Long IdPaisMoneda;
		public Long IdPaisLocal;
		public Long IdPaisEmpresa;
		public Long IdPaisEmisorSerieFondo;
		//REINVERSIONES
		public String Switch;
		public String FechaCertificacionRescateOrigen;
		public String FechaLiquidacionRescateOrigen;
		public String FechaValorCuotaRescateOrigen;
		public String RescateGeneraSpotCertificacion;
		public Boolean PermiteDiaNoHabilFechaValorCuota;
		public Map<String, ConfiguracionFechaDias> ConfiguracionFechas;
		public String FondoNacionalDolar;
		public String IdMercadoFondoMutuo;
		public String MoneyMarket;
		public Boolean RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket;
		
		public ProcesamientoFechas() {
			this.ConfiguracionFechas = new HashMap<String, ConfiguracionFechaDias>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CopiarProcesamientoFechas{
		public static ProcesamientoFechas copiarObjeto(ProcesamientoFechas original) {
			
			if(original == null) {
				return new ProcesamientoFechas();
			}
			
			ProcesamientoFechas copiaPFechas = new ProcesamientoFechas();
			
			copiaPFechas.IdInterno = (original.IdInterno != null) ? original.IdInterno : 0L;
			copiaPFechas.IdSerieFondo = (original.IdSerieFondo != null) ? original.IdSerieFondo : "";
			copiaPFechas.FechaInicial = (original.FechaInicial != null) ? original.FechaInicial : "";
			copiaPFechas.FechaEmision = (original.FechaEmision != null) ? original.FechaEmision : "";
			copiaPFechas.FechaDigitacion = (original.FechaDigitacion != null) ? original.FechaDigitacion : "";
			copiaPFechas.FechaCurse = (original.FechaCurse != null) ? original.FechaCurse : "";
			copiaPFechas.FechaValorCuota = (original.FechaValorCuota != null) ? original.FechaValorCuota : "";
			copiaPFechas.FechaCertificacion = (original.FechaCertificacion != null) ? original.FechaCertificacion : "";
			copiaPFechas.FechaLiquidacion = (original.FechaLiquidacion != null) ? original.FechaLiquidacion : "";
			copiaPFechas.EstadoFondo = (original.EstadoFondo != null) ? original.EstadoFondo : "";
			copiaPFechas.EstadoLiquidacion = (original.EstadoLiquidacion != null) ? original.EstadoLiquidacion : "";
			copiaPFechas.NumeroDiasRetencion = (original.NumeroDiasRetencion != null) ? original.NumeroDiasRetencion : 0L;
			copiaPFechas.NumeroValutaValorCuota = (original.NumeroValutaValorCuota != null) ? original.NumeroValutaValorCuota : 0L;
			copiaPFechas.NumeroValutaCertificacion = (original.NumeroValutaCertificacion != null) ? original.NumeroValutaCertificacion : 0L;
			copiaPFechas.NumeroValutaCurse = (original.NumeroValutaCurse != null) ? original.NumeroValutaCurse : 0L;
			copiaPFechas.SolicitudBatch = (original.SolicitudBatch != null) ? original.SolicitudBatch : "";
			copiaPFechas.TipoSolicitud = (original.TipoSolicitud != null) ? original.TipoSolicitud : "";
			copiaPFechas.FondoExtranjero = (original.FondoExtranjero != null) ? original.FondoExtranjero : "";
			copiaPFechas.IdPaisFondo = (original.IdPaisFondo != null) ? original.IdPaisFondo : 0L;
			copiaPFechas.IdPaisEmisor = (original.IdPaisEmisor != null) ? original.IdPaisEmisor : 0L;
			copiaPFechas.IdPaisMoneda = (original.IdPaisMoneda != null) ? original.IdPaisMoneda : 0L;
			copiaPFechas.IdPaisLocal = (original.IdPaisLocal != null) ? original.IdPaisLocal : 0L;
			copiaPFechas.IdPaisEmpresa = (original.IdPaisEmpresa != null) ? original.IdPaisEmpresa : 0L;
			copiaPFechas.IdPaisEmisorSerieFondo = (original.IdPaisEmisorSerieFondo != null) ? original.IdPaisEmisorSerieFondo : 0L;
			copiaPFechas.Switch = (original.Switch != null) ? original.Switch : "";
			copiaPFechas.FechaCertificacionRescateOrigen = (original.FechaCertificacionRescateOrigen != null) ? original.FechaCertificacionRescateOrigen : "";
			copiaPFechas.FechaLiquidacionRescateOrigen = (original.FechaLiquidacionRescateOrigen != null) ? original.FechaLiquidacionRescateOrigen : "";
			copiaPFechas.FechaValorCuotaRescateOrigen = (original.FechaValorCuotaRescateOrigen != null) ? original.FechaValorCuotaRescateOrigen : "";
			copiaPFechas.RescateGeneraSpotCertificacion = (original.RescateGeneraSpotCertificacion != null) ? original.RescateGeneraSpotCertificacion : "";
			copiaPFechas.PermiteDiaNoHabilFechaValorCuota = (original.PermiteDiaNoHabilFechaValorCuota != null) ? original.PermiteDiaNoHabilFechaValorCuota : false;
			copiaPFechas.FondoNacionalDolar = (original.FondoNacionalDolar != null) ? original.FondoNacionalDolar : "";
			copiaPFechas.IdMercadoFondoMutuo = (original.IdMercadoFondoMutuo != null) ? original.IdMercadoFondoMutuo : "";
			copiaPFechas.MoneyMarket = (original.MoneyMarket != null) ? original.MoneyMarket : "";
			copiaPFechas.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket = (original.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket != null) ? original.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket : false;
	        
			copiaPFechas.ConfiguracionFechas = new HashMap<>();
		
			if(original.ConfiguracionFechas != null) {
				for (Map.Entry<String, ConfiguracionFechaDias> ConfigFechaDiasOriginal : original.ConfiguracionFechas.entrySet()) {
					ConfiguracionFechaDias copiaConfigFechaDias = new ConfiguracionFechaDias();
					copiaConfigFechaDias.CantidadDiasValuta = ConfigFechaDiasOriginal.getValue().CantidadDiasValuta;
					copiaConfigFechaDias.EsHabil = (ConfigFechaDiasOriginal.getValue().EsHabil != null) ? ConfigFechaDiasOriginal.getValue().EsHabil : "";
					copiaConfigFechaDias.IdTipoFechaOrigen = (ConfigFechaDiasOriginal.getValue().IdTipoFechaOrigen != null) ? ConfigFechaDiasOriginal.getValue().IdTipoFechaOrigen : "";
					copiaConfigFechaDias.DebeSerHabilFechaOrigen = (ConfigFechaDiasOriginal.getValue().DebeSerHabilFechaOrigen != null) ? ConfigFechaDiasOriginal.getValue().DebeSerHabilFechaOrigen : "";
					copiaConfigFechaDias.IdPaisFondo = ConfigFechaDiasOriginal.getValue().IdPaisFondo;
					copiaConfigFechaDias.IdPaisEmisor = ConfigFechaDiasOriginal.getValue().IdPaisEmisor;
					copiaConfigFechaDias.IdPaisMoneda = ConfigFechaDiasOriginal.getValue().IdPaisMoneda;
					copiaConfigFechaDias.IdPaisLocal = ConfigFechaDiasOriginal.getValue().IdPaisLocal;
					copiaConfigFechaDias.IdPaisEmpresa = ConfigFechaDiasOriginal.getValue().IdPaisEmpresa;
					copiaConfigFechaDias.paisCalculo = ConfigFechaDiasOriginal.getValue().paisCalculo;
					copiaPFechas.ConfiguracionFechas.put(ConfigFechaDiasOriginal.getKey(), copiaConfigFechaDias);
				}
			}

	        return copiaPFechas;
		}
	}
		
	public static List<ProcesamientoFechas> CopiarListaProcesamientoFechas(List<ProcesamientoFechas> PFechasListaOriginal) {

		if (PFechasListaOriginal == null) {
			return new ArrayList<>();
		}

		List<ProcesamientoFechas> copiaPFechasLista = new ArrayList<>();

		for (ProcesamientoFechas PFechasListaOriginalElementos : PFechasListaOriginal) {
			if (PFechasListaOriginalElementos != null) {
				copiaPFechasLista.add(CopiarProcesamientoFechas.copiarObjeto(PFechasListaOriginalElementos)); 
			} else {
				copiaPFechasLista.add(new ProcesamientoFechas());
			}
		}

        return copiaPFechasLista;
    }

	public static class ConfiguracionFechaFFMM {
		public String IdModalidad;
		public String IdTipoFecha;
		public String TipoSolicitud;
		public String EstadoFondo;
		public String FondoExtranjero;
		public String Descripcion;
		public Long CantidadDias;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		public String ValutaCurse;
		public String ValutaValorCuota;
		public String ValutaCertificacion;
		public String ValutaRetencion;
		public String CalendarioLocal;
		public String CalendarioEmisor;
		public String CalendarioFondo;
		public String CalendarioMoneda;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionFechaDias {

		public long CantidadDiasValuta;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		public long IdPaisFondo;
		public long IdPaisEmisor;
		public long IdPaisMoneda;
		public long IdPaisLocal;
		public long IdPaisEmpresa;
		public long paisCalculo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class FechasPivote{
		public String KeyConfig;
		public long IdInternoProceso;
		public long IdInterno;
		public String IdSerieFondo;
		public Date FechaEmision;
		public Date FechaInicial;
		
		public String FechaInicialEsHabil;
		public Date FechaFinal;
		public String FechaFinalEsHabil;
		
		public long CantidadDiasValuta;
		public long IdPaisLocal;
		public long IdPaisEmisor;
		public long IdPaisFondo;
		public long IdPaisMoneda;
		public long IdConfiguracionFechasFondoMutuo;
		public String IdTipoFecha;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularFechaSolicitudAporteRescate{
		public List <ResultadoFecha> P_FECHASAPORTERESCATE;
		
		public SalidaCalcularFechaSolicitudAporteRescate(){
			this.P_FECHASAPORTERESCATE = new ArrayList<ResultadoFecha>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoFecha{
		public Long IdInterno;
		public String IdSerieFondo;
		public Date FechaEmision;
		public Date FechaDigitacion;        
		public Date FechaAceptacion;
		public Date FechaValorCuota;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FechaCalculadaFondoMutuo{
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasDigitacion;
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasCurse;
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasValorCuota;
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasCertificacion;
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasRetencion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DiaFeriado{
		public java.sql.Date Fecha; 
		public Long	IdPais;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularFechaSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaCalcularFechaSolicitudAporteRescate.class);
		SalidaCalcularFechaSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public SalidaCalcularFechaSolicitudAporteRescate logica(Connection con, EntradaCalcularFechaSolicitudAporteRescate entrada) throws Exception{
		logger.info("INICIO::CalcularFechaSolicitudAporteRescate");
		SalidaCalcularFechaSolicitudAporteRescate out = new SalidaCalcularFechaSolicitudAporteRescate();
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, entrada.p_IdEmpresa);
		try{
			List<ProcesamientoFechas> solicitudesAProcesar = obtenerSolicitudesAProcesar(con, entrada, entrada.p_SolicitudAporteRescate, idPaisEmpresa);
			List<ProcesamientoFechas> solicitudesAProcesarSwitch = obtenerSolicitudesSwitch(solicitudesAProcesar);
			List<ProcesamientoFechas> solicitudesAProcesarNoSwitch = obtenerSolicitudesNoSwitch(solicitudesAProcesar);
			
			if(solicitudesAProcesarSwitch.size() > 0) {
			  	solicitudesAProcesarSwitch = setFechasSolicitudesSwitch(solicitudesAProcesarSwitch);
			}
			
			if(solicitudesAProcesarNoSwitch.size() > 0) {
				solicitudesAProcesarNoSwitch = setConfiguracionesSolicitudesNoSwitchAProcesar(con, entrada.p_IdEmpresa, solicitudesAProcesarNoSwitch);
				solicitudesAProcesarNoSwitch = setFechasSolicitudesNoSwitch(con, solicitudesAProcesarNoSwitch);
				solicitudesAProcesarNoSwitch = repasarFechaCertificacionLiquidacionSeriesExtranjeras(con, solicitudesAProcesarNoSwitch, idPaisEmpresa);
				solicitudesAProcesarNoSwitch = repasarFechaCertificacionRescateGeneraSpot(con, solicitudesAProcesarNoSwitch, idPaisEmpresa);
				solicitudesAProcesarNoSwitch = repasarFechaFondoNacionalDolar(con, solicitudesAProcesarNoSwitch, idPaisEmpresa);
			}
			
			List<ProcesamientoFechas> solicitudesFinales = new ArrayList<ProcesamientoFechas>();
			solicitudesFinales.addAll(solicitudesAProcesarSwitch);
			solicitudesFinales.addAll(solicitudesAProcesarNoSwitch);
			
			out.P_FECHASAPORTERESCATE = obtenerResultadoFechas(solicitudesFinales);			
		}catch(Exception e){
			logger.error("Error::CalcularFechaSolicitudAporteRescate",e);
			throw e;
		}
		logger.info("FIN::CalcularFechaSolicitudAporteRescate");
		
		return out;
	}
	
	private List<ProcesamientoFechas> repasarFechaFondoNacionalDolar(Connection con, List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.repasarFechaFondoNacionalDolar");
		
		List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud-> "S".equals(solicitud.FondoNacionalDolar)).collect(Collectors.toList());

		if(solicitudesAConsultar.size() > 0) {
			
			List<ProcesamientoFechas> fechasSinProcesar = CopiarListaProcesamientoFechas(solicitudesAProcesarNoSwitch);
			
			//para fecha curse
			Map<String,DiaFeriado> feriadosCurse = obtenerFeriadosFondosNacionalesUSD(con, solicitudesAConsultar, idPaisEmpresa);
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCurse = obtenerFechaAConsultarCurse(solicitudesAProcesarNoSwitch,feriadosCurse,idPaisEmpresa);
			if(fechasAConsultarCurse.size() > 0) {
				List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
				HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(con, fechasAConsultarCurse, idPaisEmpresa);
				solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
					if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
						solicitud.FechaCurse = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
					}
				});
			}
			//para fecha certificacion
			Map<String,DiaFeriado> feriadosCertificacion = obtenerFeriadosFondosNacionalesUSD(con, solicitudesAConsultar, idPaisEmpresa);
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = obtenerFechaAConsultarCertificacion(solicitudesAProcesarNoSwitch,feriadosCertificacion,feriadosCurse,idPaisEmpresa,fechasSinProcesar);
			if(fechasAConsultarCertificacion.size() > 0) {
				List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
				HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(con, fechasAConsultarCertificacion, idPaisEmpresa);
				solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
					if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
						solicitud.FechaCertificacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
					}
				});
			}
			//para fecha pago/liquidacion
			Map<String,DiaFeriado> feriadosPago = obtenerFeriadosFondosNacionalesUSD(con, solicitudesAConsultar, idPaisEmpresa);
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarPago = obtenerFechaAConsultarLiquidacion(solicitudesAProcesarNoSwitch,feriadosPago,feriadosCurse,idPaisEmpresa,fechasSinProcesar);
			if(fechasAConsultarPago.size() > 0) {
				List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
				HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(con, fechasAConsultarPago, idPaisEmpresa);
				solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
					if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
						solicitud.FechaLiquidacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
					}
				});
			}
		
			solicitudesAProcesarNoSwitch = obtenerFechaValorCuotaRepaso(con, solicitudesAProcesarNoSwitch);
			
		}
			
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.repasarFechaFondoNacionalDolar");
		return solicitudesAProcesarNoSwitch;
	}

	private Long obtenerIdPaisEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmpresa");
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresa.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmpresa");
		return idPaisEmpresa;
	}
	
	private List<ProcesamientoFechas> obtenerSolicitudesAProcesar(Connection con, EntradaCalcularFechaSolicitudAporteRescate entrada, List<InFechas> solicitudes, Long idPaisEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesAProcesar");
		Map<String,SerieFondoSolicitudes> mapSeriesFondo = obtenerSeriesFondos(con,entrada);
		String permiteDiaNoHabilFechaValorCuotaMoneyMarket = obtenerPermiteDiaNoHabilFechaValorCuotaMoneyMarket(con, entrada.p_IdEmpresa);
		List<ProcesamientoFechas> solicitudesAProcesar = new ArrayList<ProcesamientoFechas>();
		for (InFechas solicitud : solicitudes) {
			ProcesamientoFechas fechaProceso = new ProcesamientoFechas();
			SerieFondoSolicitudes serieFondo = mapSeriesFondo.get(solicitud.IdSerieFondo);
			fechaProceso.IdInterno = solicitud.IdInterno;
			fechaProceso.IdSerieFondo = solicitud.IdSerieFondo;
			fechaProceso.FechaEmision = solicitud.FechaEmision;
			fechaProceso.EstadoFondo = solicitud.EstadoFondo;
			fechaProceso.EstadoLiquidacion = solicitud.EstadoLiquidacion;
			fechaProceso.NumeroDiasRetencion = (solicitud.NumeroDiasRetencion  != null ? solicitud.NumeroDiasRetencion : 0L);
			fechaProceso.NumeroValutaValorCuota = (solicitud.NumeroValutaValorCuota  != null ? solicitud.NumeroValutaValorCuota : 0L);	
			fechaProceso.NumeroValutaCertificacion = (solicitud.NumeroValutaCertificacion  != null ? solicitud.NumeroValutaCertificacion : 0L);
			fechaProceso.NumeroValutaCurse = (solicitud.NumeroValutaCurse != null ? solicitud.NumeroValutaCurse : 0L);
			fechaProceso.SolicitudBatch = solicitud.SolicitudBatch;
			fechaProceso.TipoSolicitud = solicitud.TipoSolicitud;
			fechaProceso.FondoExtranjero = solicitud.FondoExtranjero;
			fechaProceso.IdPaisFondo = (solicitud.IdPaisFondo  != null ? solicitud.IdPaisFondo : 0L);
			fechaProceso.IdPaisEmisor = (solicitud.IdPaisEmisor != null ? solicitud.IdPaisEmisor : 0L);
			fechaProceso.IdPaisMoneda = (solicitud.IdPaisMoneda != null ? solicitud.IdPaisMoneda : 0L);
			fechaProceso.IdPaisLocal = (solicitud.IdPaisEmisor != null ? solicitud.IdPaisEmisor : 0L);
			fechaProceso.IdPaisEmpresa = idPaisEmpresa;
			fechaProceso.IdPaisEmisorSerieFondo = serieFondo != null ? serieFondo.IdPaisEmisor : 0L;
			fechaProceso.PermiteDiaNoHabilFechaValorCuota = permiteDiaNoHabilFechaValorCuota(serieFondo, fechaProceso.TipoSolicitud, permiteDiaNoHabilFechaValorCuotaMoneyMarket);
			fechaProceso.RescateGeneraSpotCertificacion = solicitud.RescateGeneraSpotCertificacion;
			fechaProceso.Switch = solicitud.Switch;
			fechaProceso.FechaCertificacionRescateOrigen = solicitud.FechaCertificacionRescateOrigen;
			fechaProceso.FechaLiquidacionRescateOrigen = solicitud.FechaLiquidacionRescateOrigen;
			fechaProceso.FechaValorCuotaRescateOrigen = solicitud.FechaValorCuotaRescateOrigen;
			fechaProceso.FondoNacionalDolar = solicitud.FondoNacionalDolar;
			fechaProceso.IdMercadoFondoMutuo = solicitud.IdMercadoFondoMutuo;
			fechaProceso.MoneyMarket = serieFondo != null ? serieFondo.IndicadorMoneyMarket : "N";
			fechaProceso.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket = repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket(serieFondo, permiteDiaNoHabilFechaValorCuotaMoneyMarket);
			solicitudesAProcesar.add(fechaProceso);
		}
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesAProcesar");
		return solicitudesAProcesar;
	}
	
	private List<ProcesamientoFechas> obtenerSolicitudesSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesSwitch");
		List<ProcesamientoFechas> solicitudesAProcesarSwitch = solicitudesAProcesar.stream().filter(solicitud->_c.SI.equals(solicitud.Switch)).collect(Collectors.toList());
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesSwitch");
		return solicitudesAProcesarSwitch;
	}
	
	private List<ProcesamientoFechas> obtenerSolicitudesNoSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesNoSwitch");
		List<ProcesamientoFechas> solicitudesAProcesarSwitch = solicitudesAProcesar.stream().filter(solicitud->_c.NO.equals(solicitud.Switch)).collect(Collectors.toList());
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerSolicitudesNoSwitch");
		return solicitudesAProcesarSwitch;
		
	}
	
	private List<ProcesamientoFechas> setFechasSolicitudesSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setFechasSolicitudesSwitch");
		solicitudesAProcesar.stream().forEach(solicitud->{
			solicitud.FechaDigitacion = solicitud.FechaCertificacionRescateOrigen;
			solicitud.FechaEmision = solicitud.FechaCertificacionRescateOrigen;
			solicitud.FechaCurse = solicitud.FechaCertificacionRescateOrigen;
			solicitud.FechaValorCuota = solicitud.FechaValorCuotaRescateOrigen;
			solicitud.FechaCertificacion = solicitud.FechaCertificacionRescateOrigen;
			solicitud.FechaLiquidacion = solicitud.FechaLiquidacionRescateOrigen;
		});
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setFechasSolicitudesSwitch");
		return solicitudesAProcesar;
	}
	
	private Map<String,SerieFondoSolicitudes> obtenerSeriesFondos(Connection con, EntradaCalcularFechaSolicitudAporteRescate entrada) throws SQLException{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerSeriesFondos");
		SQL sql=new SQL();
		HashMap<String, Object>  parametros = new HashMap<String, Object>();
		List<String> idSeriesFondo = entrada.p_SolicitudAporteRescate.stream().map(solicitud->solicitud.IdSerieFondo).distinct().collect(Collectors.toList());
		parametros.put("IdSerieFondo", idSeriesFondo);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesFondo = sql.query(con, SQL_SELECT_SERIE_FONDO, parametros, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		Map<String,SerieFondoSolicitudes> mapSeriesFondo = seriesFondo.stream().collect(Collectors.toMap(serie->serie.IdSerieFondo, serie->serie));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerSeriesFondos");
		return mapSeriesFondo;
	}
	
	private String obtenerPermiteDiaNoHabilFechaValorCuotaMoneyMarket(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate");
		String permiteDiaNoHabilFechaValorCuotaMoneyMarket = _c.NO;
		try {
			permiteDiaNoHabilFechaValorCuotaMoneyMarket = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate::no existe parametro PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET, se deja valor por defecto.");
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate");
		return permiteDiaNoHabilFechaValorCuotaMoneyMarket;
	}
	
	private Boolean permiteDiaNoHabilFechaValorCuota(SerieFondoSolicitudes serieFondo, String tipoSolicitud, String permiteDiaNoHabilFechaValorCuotaMoneyMarket) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarket");
		Boolean permiteDiaNoHabilFechaValorCuota = _c.SI.equals(permiteDiaNoHabilFechaValorCuotaMoneyMarket);
		Boolean esFechaMoneyMarket = false;
		try {
			if(_c.TipoSolicitudAporte.equals(tipoSolicitud)) {
				esFechaMoneyMarket = esFechaMoneyMarketAporte(serieFondo);
			}else if(_c.TipoSolicitudRescate.equals(tipoSolicitud)) {
				esFechaMoneyMarket = esFechaMoneyMarketRescate(serieFondo);
			}else {
				throw new Exception("Tipo Solicitud no valida.");
			}
		}catch(Exception e) {
			logger.error("ERROR::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarket", e);
			throw e;
		}
		Boolean resultado = esFechaMoneyMarket && permiteDiaNoHabilFechaValorCuota;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarket");
		return resultado;
	}
	
	private Boolean esFechaMoneyMarketAporte(SerieFondoSolicitudes serieFondo) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarketAporte");
		Boolean esFechaMoneyMarketAporte = _c.SI.equals(serieFondo.MoneyMarket);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarketAporte");
		return esFechaMoneyMarketAporte;
	}
	
	private Boolean esFechaMoneyMarketRescate(SerieFondoSolicitudes serieFondo) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarketRescate");
		Boolean esFechaMoneyMarketRescate = _c.SI.equals(serieFondo.MoneyMarket) || _c.MERCADO_RF.equals(serieFondo.IdMercadoFondoMutuo);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esFechaMoneyMarketRescate");
		return esFechaMoneyMarketRescate;
	}
	
	private List<ProcesamientoFechas> setConfiguracionesSolicitudesNoSwitchAProcesar(Connection con, Long idEmpresa, List<ProcesamientoFechas> solicitudesAProcesar) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setConfiguracionesSolicitudesNoSwitchAProcesar");
		String modalidadSolicitud = obtenerModalidadSolicitudFechaAporteRescate(con,idEmpresa);
		List<ConfiguracionFechaFFMM> configuracionFechaFFMM = obtenerConfiguracionFechaFondoMutuo(con, idEmpresa);
		solicitudesAProcesar.stream().forEach(solicitud->{
			for (ConfiguracionFechaFFMM configuracionFechaFondoMutuo : configuracionFechaFFMM){
				if (esConfiguracionValida(configuracionFechaFondoMutuo, solicitud, modalidadSolicitud)){
					ConfiguracionFechaDias configuracionFechaDias = new ConfiguracionFechaDias();
					configuracionFechaDias.CantidadDiasValuta = obtenerValuta(configuracionFechaFondoMutuo, solicitud);
					configuracionFechaDias.EsHabil = configuracionFechaFondoMutuo.EsHabil;
					configuracionFechaDias.IdTipoFechaOrigen = configuracionFechaFondoMutuo.IdTipoFechaOrigen;
					configuracionFechaDias.DebeSerHabilFechaOrigen = configuracionFechaFondoMutuo.DebeSerHabilFechaOrigen;
					configuracionFechaDias.IdPaisEmpresa = solicitud.IdPaisEmpresa;
					configuracionFechaDias.IdPaisFondo = obtenerIdPaisFondo(configuracionFechaFondoMutuo, solicitud);
					configuracionFechaDias.IdPaisEmisor = obtenerIdPaisEmisor(configuracionFechaFondoMutuo, solicitud);
					configuracionFechaDias.IdPaisMoneda = obtenerIdPaisMoneda(configuracionFechaFondoMutuo, solicitud);
					configuracionFechaDias.IdPaisLocal = obtenerIdPaisLocal(configuracionFechaFondoMutuo, solicitud);
					solicitud.ConfiguracionFechas.put(configuracionFechaFondoMutuo.IdTipoFecha, configuracionFechaDias);
				}
			}
		});
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setConfiguracionesSolicitudesNoSwitchAProcesar");
		return solicitudesAProcesar;
	}
	
	private String obtenerModalidadSolicitudFechaAporteRescate(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate");
		String modalidadSolicitud = "";
		try {
			modalidadSolicitud = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate::no existe parametro MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE, se deja valor por defecto.");
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerModalidadSolicitudFechaAporteRescate");
		return modalidadSolicitud;
	}
	
	private List<ConfiguracionFechaFFMM> obtenerConfiguracionFechaFondoMutuo(Connection con, Long idEmpresa) throws SQLException{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerConfiguracionFechaFondoMutuo");
		SQL sql=new SQL();
		HashMap<String, Object>  parametros = new HashMap<String, Object>();
		parametros.put("p_IdEmpresa", idEmpresa);
		logger.debug("INICIO::SQL_SELECT_CONFIGURACION_FECHAS_FFMM");
		List<ConfiguracionFechaFFMM> configuracionFechaFondoMutuo = sql.query(con, SQL_SELECT_CONFIGURACION_FECHAS_FFMM, parametros, ConfiguracionFechaFFMM.class);
		logger.debug("FIN::SQL_SELECT_CONFIGURACION_FECHAS_FFMM");
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerConfiguracionFechaFondoMutuo");
		return configuracionFechaFondoMutuo;
	}
	
	private Boolean esConfiguracionValida(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esConfiguracionValida");
		Boolean esConfiguracionValida = configuracionCumpleRequisitosSolicitud(configuracionFechaFondoMutuo, solicitud) && configuracionCumpleRequisitosModalidad(configuracionFechaFondoMutuo, solicitud, modalidadSolicitud);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esConfiguracionValida");
		return esConfiguracionValida;
	}
	
	private Boolean configuracionCumpleRequisitosSolicitud(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.configuracionCumpleRequisitosSolicitud");
		Boolean configuracionCumpleRequisitosSolicitud = esTipoSolicitudValida(configuracionFechaFondoMutuo, solicitud) && esEstadoFondoValido(configuracionFechaFondoMutuo, solicitud) && esFondoExtranjeroValido(configuracionFechaFondoMutuo, solicitud);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.configuracionCumpleRequisitosSolicitud");
		return configuracionCumpleRequisitosSolicitud;
	}
	
	private Boolean esTipoSolicitudValida(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esTipoSolicitudValida");
		Boolean esTipoSolicitudValida = configuracionFechaFondoMutuo.TipoSolicitud == null || configuracionFechaFondoMutuo.TipoSolicitud.equalsIgnoreCase(solicitud.TipoSolicitud);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esTipoSolicitudValida");
		return esTipoSolicitudValida;
	}
	
	private Boolean esEstadoFondoValido(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esEstadoFondoValido");
		Boolean esEstadoFondoValido = configuracionFondoMutuo.EstadoFondo == null || configuracionFondoMutuo.EstadoFondo.equalsIgnoreCase(solicitud.EstadoFondo);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esEstadoFondoValido");
		return esEstadoFondoValido;
	}
	
	private Boolean esFondoExtranjeroValido(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esFondoExtranjeroValido");
		Boolean esFondoExtranjeroValido = configuracionFondoMutuo.FondoExtranjero == null || configuracionFondoMutuo.FondoExtranjero.equalsIgnoreCase(solicitud.FondoExtranjero);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esFondoExtranjeroValido");
		return esFondoExtranjeroValido;
	}
	
	private Boolean configuracionCumpleRequisitosModalidad(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.configuracionCumpleRequisitosModalidad");
		Boolean configuracionCumpleRequisitosModalidad = esModalidadValidaSolicitud(configuracionFondoMutuo, solicitud, modalidadSolicitud) || esModalidadConfiguradaEnParametro(configuracionFondoMutuo, modalidadSolicitud);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.configuracionCumpleRequisitosModalidad");
		return configuracionCumpleRequisitosModalidad;
	}
	
	private Boolean esModalidadValidaSolicitud(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esModalidadValidaSolicitud");
		Boolean esModalidadValidaSolicitud = esModalidadNula(modalidadSolicitud) && (esModalidadUnitario(configuracionFondoMutuo, solicitud) || esModalidadBatch(configuracionFondoMutuo, solicitud));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esModalidadValidaSolicitud");
		return esModalidadValidaSolicitud;
	}
	
	private Boolean esModalidadNula(String modalidadSolicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esModalidadNula");
		Boolean esModalidadNula = modalidadSolicitud == null || modalidadSolicitud.equalsIgnoreCase("");
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esModalidadNula");
		return esModalidadNula;
	}
	
	private Boolean esModalidadUnitario(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esModalidadUnitario");
		Boolean esModalidadUnitario = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase("UNITARIO") && solicitud.SolicitudBatch.equalsIgnoreCase("N");
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esModalidadUnitario");
		return esModalidadUnitario;
	}
	
	private Boolean esModalidadBatch(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esModalidadBatch");
		Boolean esModalidadBatch = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase("BATCH") && solicitud.SolicitudBatch.equalsIgnoreCase("S");
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esModalidadBatch");
		return esModalidadBatch;
	}
	
	private Boolean esModalidadConfiguradaEnParametro(ConfiguracionFechaFFMM configuracionFondoMutuo, String modalidadSolicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esModalidadConfiguradaEnParametro");
		Boolean esModalidadConfiguradaEnParametro = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase(modalidadSolicitud);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esModalidadConfiguradaEnParametro");
		return esModalidadConfiguradaEnParametro;
	}
	
	private Long obtenerValuta(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerValuta");
		Long valuta = configuracionFondoMutuo.CantidadDias + obtenerValutaFechaCurse(configuracionFondoMutuo, solicitud) + obtenerValutaFechaCertificacion(configuracionFondoMutuo, solicitud) + obtenerValutaFechaRetencion(configuracionFondoMutuo, solicitud) + obtenerValutaFechaValorCuota(configuracionFondoMutuo, solicitud); 
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerValuta");
		return valuta;
	}
	
	private Long obtenerValutaFechaCurse(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaCurse");
		Long valuta = _c.SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaCurse) ? solicitud.NumeroValutaCurse : 0L;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaCurse");
		return valuta;
	}
	
	private Long obtenerValutaFechaCertificacion(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaCertificacion");
		Long valuta = _c.SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaCertificacion) ? solicitud.NumeroValutaCertificacion : 0L;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaCertificacion");
		return valuta;
	}
	
	private Long obtenerValutaFechaRetencion(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaRetencion");
		Long valuta = _c.SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaRetencion) ? solicitud.NumeroDiasRetencion : 0L;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaRetencion");
		return valuta;
	}
	
	private Long obtenerValutaFechaValorCuota(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaValorCuota");
		Long valuta = _c.SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaValorCuota) ? solicitud.NumeroValutaValorCuota : 0L;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerValutaFechaValorCuota");
		return valuta;
	}
	
	private Long obtenerIdPaisFondo(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisFondo");
		Long idPaisFondo = configuracionFondoMutuo.CalendarioFondo.equalsIgnoreCase("S") ? solicitud.IdPaisFondo : solicitud.IdPaisEmpresa;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisFondo");
		return idPaisFondo;
	}
	
	private Long obtenerIdPaisEmisor(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmisor");
		Long idPaisEmisor = configuracionFondoMutuo.CalendarioEmisor.equalsIgnoreCase("S") ? solicitud.IdPaisEmisor : solicitud.IdPaisEmpresa;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmisor");
		return idPaisEmisor;
	}
	
	private Long obtenerIdPaisMoneda(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisMoneda");
		Long idPaisMoneda = "S".equalsIgnoreCase(configuracionFondoMutuo.CalendarioMoneda) || "S".equals(solicitud.FondoNacionalDolar) ? solicitud.IdPaisMoneda : solicitud.IdPaisEmpresa;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisMoneda");
		return idPaisMoneda;
	}
	
	private Long obtenerIdPaisLocal(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisLocal");
		Long idPaisLocal = configuracionFondoMutuo.CalendarioLocal.equalsIgnoreCase("S") ? solicitud.IdPaisLocal : solicitud.IdPaisEmpresa;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisLocal");
		return idPaisLocal;
	}
	
	private List<ProcesamientoFechas> setFechasSolicitudesNoSwitch(Connection con, List<ProcesamientoFechas> solicitudesAProcesar) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setFechasSolicitudes");
		FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo = new FechaCalculadaFondoMutuo();	
		fechaCalculadaFondoMutuo.FechasDigitacion = obtenerFechaOperacion(con, solicitudesAProcesar, _c.TIPO_FECHA_DIGITACION);
		solicitudesAProcesar = setFechaDigitacion(solicitudesAProcesar,fechaCalculadaFondoMutuo.FechasDigitacion);
		
		fechaCalculadaFondoMutuo.FechasCurse = obtenerFechaOperacion(con, solicitudesAProcesar, _c.TIPO_FECHA_CURSE);
		solicitudesAProcesar = setFechaCurse(solicitudesAProcesar,fechaCalculadaFondoMutuo.FechasCurse);
		
		fechaCalculadaFondoMutuo.FechasValorCuota = obtenerFechaOperacion(con, solicitudesAProcesar, _c.TIPO_FECHA_VALOR_CUOTA);
		fechaCalculadaFondoMutuo.FechasCertificacion = obtenerFechaOperacion(con, solicitudesAProcesar, _c.TIPO_FECHA_CERTIFICACION);
		fechaCalculadaFondoMutuo.FechasRetencion = obtenerFechaOperacion(con, solicitudesAProcesar, _c.TIPO_FECHA_RETENCION);
		solicitudesAProcesar = setFechaDependientesFechaCurse(solicitudesAProcesar,fechaCalculadaFondoMutuo);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setFechasSolicitudes");
		return solicitudesAProcesar;
	}
	
	private HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> obtenerFechaOperacion(Connection con, List<ProcesamientoFechas> solicitudesAProcesar, String tipoFecha) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaOperacion");
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		//Obtiene la fecha con el IdPaisEmisor como preferencia
		inObtenerDiaSiguiente.p_diaHabil = _c.SI;
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = _c.SI;
		inObtenerDiaSiguiente.p_fechas = solicitudesAProcesar.stream().map(solicitud->{
			ConfiguracionFechaDias configuracionFechaDias = solicitud.ConfiguracionFechas.get(tipoFecha);
			configuracionFechaDias.paisCalculo = (configuracionFechaDias.IdPaisEmisor > 0 ? configuracionFechaDias.IdPaisEmisor : (configuracionFechaDias.IdPaisFondo > 0 ? configuracionFechaDias.IdPaisFondo : (configuracionFechaDias.IdPaisLocal > 0 ? configuracionFechaDias.IdPaisLocal : (configuracionFechaDias.IdPaisMoneda > 0 ? configuracionFechaDias.IdPaisMoneda : configuracionFechaDias.IdPaisEmpresa)))); 
			
			ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			fecha.fechaInicial = obtenerFechaInicial(solicitud,tipoFecha);
			fecha.numeroDias = configuracionFechaDias.CantidadDiasValuta;
			fecha.pais = configuracionFechaDias.paisCalculo;
			
			return fecha;
		}).collect(Collectors.toList());
		
		//Obtiene la fecha con el IdPaisFondo como preferencia
		inObtenerDiaSiguiente.p_fechas.addAll(solicitudesAProcesar.stream().map(solicitud->{
			ConfiguracionFechaDias configuracionFechaDias = solicitud.ConfiguracionFechas.get(tipoFecha);
			if("S".equals(solicitud.FondoNacionalDolar) && _c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)) {
				configuracionFechaDias.paisCalculo = solicitud.IdPaisMoneda;
			}else {
				configuracionFechaDias.paisCalculo = (configuracionFechaDias.IdPaisFondo > 0 ? configuracionFechaDias.IdPaisFondo : (configuracionFechaDias.IdPaisEmisor > 0 ? configuracionFechaDias.IdPaisEmisor : (configuracionFechaDias.IdPaisLocal > 0 ? configuracionFechaDias.IdPaisLocal : (configuracionFechaDias.IdPaisMoneda > 0 ? configuracionFechaDias.IdPaisMoneda : configuracionFechaDias.IdPaisEmpresa)))); 
			}
			
			ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			fecha.fechaInicial = obtenerFechaInicial(solicitud,tipoFecha);
			fecha.numeroDias = configuracionFechaDias.CantidadDiasValuta;
			fecha.pais = configuracionFechaDias.paisCalculo;
			
			return fecha;
		}).collect(Collectors.toList()));
				
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
		HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = new HashMap<>();
		
		for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas)
			hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDiasInicial + fechaSalida.idPais, fechaSalida);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaOperacion");
		return hashFechasSalida;
	}
	
	private String obtenerFechaInicial(ProcesamientoFechas solicitud, String tipoFecha) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaInicial");
		String fecha = "";
		switch(tipoFecha) {
			case "DIGITA" : {
				fecha = solicitud.FechaEmision;
				break;
			}
			case "CURSE": {
				fecha = solicitud.FechaDigitacion;
				break;
			}
			default: {
				fecha = solicitud.FechaCurse;
				break;
			}
		}
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaInicial");
		return fecha;
	}
	
	private List<ProcesamientoFechas> setFechaDigitacion(List<ProcesamientoFechas> solicitudesAProcesar, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setFechaDigitacion");
		solicitudesAProcesar.stream().forEach(solicitud->{
			solicitud.FechaDigitacion = obtenerFecha(solicitud, fechas, _c.TIPO_FECHA_DIGITACION);
		});
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setFechaDigitacion");
		return solicitudesAProcesar;
	}
	
	private List<ProcesamientoFechas> setFechaCurse(List<ProcesamientoFechas> solicitudesAProcesar, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setFechaCurse");
		solicitudesAProcesar.stream().forEach(solicitud->{
			solicitud.FechaCurse = obtenerFecha(solicitud, fechas, _c.TIPO_FECHA_CURSE);
		});
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setFechaCurse");
		return solicitudesAProcesar;
	}
	
	private List<ProcesamientoFechas> setFechaDependientesFechaCurse(List<ProcesamientoFechas> solicitudesAProcesar, FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.setFechaDependientesFechaCurse");
		solicitudesAProcesar.stream().forEach(solicitud->{
			solicitud.FechaValorCuota = obtenerFechaValorCuota(solicitud, fechaCalculadaFondoMutuo);
			solicitud.FechaCertificacion = obtenerFecha(solicitud, fechaCalculadaFondoMutuo.FechasCertificacion, _c.TIPO_FECHA_CERTIFICACION);
			solicitud.FechaLiquidacion = obtenerFecha(solicitud, fechaCalculadaFondoMutuo.FechasRetencion, _c.TIPO_FECHA_RETENCION);
		});
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.setFechaDependientesFechaCurse");
		return solicitudesAProcesar;
	}
		
	private String obtenerFecha(ProcesamientoFechas solicitud, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas, String tipoFecha) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFecha");
		ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get(tipoFecha);
		if((tipoFecha.equalsIgnoreCase("CERTIF") || tipoFecha.equalsIgnoreCase("LIQUID")) && configuracion.IdPaisEmisor != configuracion.paisCalculo) {
		    String fechaEmisor ="";
			String fechaFondo = "";		
			if( ("A".equalsIgnoreCase(solicitud.EstadoFondo))&&("A".equalsIgnoreCase(solicitud.EstadoLiquidacion))) {
				fechaEmisor = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?solicitud.NumeroValutaCertificacion:solicitud.NumeroDiasRetencion)+solicitud.IdPaisEmisor).fechaCalculada;
				fechaFondo = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?solicitud.NumeroValutaCertificacion:solicitud.NumeroDiasRetencion)+solicitud.IdPaisFondo).fechaCalculada;	
			  }else{
				 	fechaEmisor = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?(solicitud.ConfiguracionFechas.get("CERTIF").CantidadDiasValuta):(solicitud.ConfiguracionFechas.get("LIQUID").CantidadDiasValuta))+solicitud.IdPaisEmisor).fechaCalculada;
					fechaFondo = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?(solicitud.ConfiguracionFechas.get("CERTIF").CantidadDiasValuta):(solicitud.ConfiguracionFechas.get("LIQUID").CantidadDiasValuta))+solicitud.IdPaisFondo).fechaCalculada;				
				 	}
			if(fechaEmisor.compareTo(fechaFondo) > 0)
			   configuracion.paisCalculo = solicitud.IdPaisEmisor;	
		}
		FechasOut fecha = fechas.get(obtenerKeyFecha(solicitud, configuracion, tipoFecha));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFecha");
		return fecha.fechaCalculada;
	}
	
	private String obtenerFechaValorCuota(ProcesamientoFechas solicitud, FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFecha");
		
		String fechaValorCuota = null;
		if(solicitud.PermiteDiaNoHabilFechaValorCuota) {
			FechasOut fechaCalculada = null;
			if(_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)) {
				ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( _c.TIPO_FECHA_CERTIFICACION);
				HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCertificacion = fechaCalculadaFondoMutuo.FechasCertificacion;
				fechaCalculada = fechasCertificacion.get(obtenerKeyFecha(solicitud, configuracion,  _c.TIPO_FECHA_CERTIFICACION));
			}else {
				ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( _c.TIPO_FECHA_CURSE);
				HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCertificacion = fechaCalculadaFondoMutuo.FechasCurse;
				fechaCalculada = fechasCertificacion.get(obtenerKeyFecha(solicitud, configuracion,  _c.TIPO_FECHA_CURSE));
			}
			LocalDate fechaCalculadaConFormato = LocalDate.parse(fechaCalculada.fechaCalculada, DateTimeFormatter.ISO_LOCAL_DATE);
			fechaValorCuota = fechaCalculadaConFormato.minusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE);
		}else {
			ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( _c.TIPO_FECHA_VALOR_CUOTA);
			if(configuracion.IdPaisEmisor != configuracion.IdPaisFondo) {
				String fechaEmisor = fechaCalculadaFondoMutuo.FechasValorCuota.get(solicitud.FechaCurse+solicitud.NumeroValutaValorCuota+solicitud.IdPaisEmisor).fechaCalculada;
				String fechaFondo = fechaCalculadaFondoMutuo.FechasValorCuota.get(solicitud.FechaCurse+solicitud.NumeroValutaValorCuota+solicitud.IdPaisFondo).fechaCalculada;
				if(fechaEmisor.compareTo(fechaFondo) > 0)
					configuracion.paisCalculo = solicitud.IdPaisEmisor;
			}
			HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasValorCuota = fechaCalculadaFondoMutuo.FechasValorCuota;
			FechasOut fechaCalculada = fechasValorCuota.get(obtenerKeyFecha(solicitud, configuracion,  _c.TIPO_FECHA_VALOR_CUOTA));
			fechaValorCuota = fechaCalculada.fechaCalculada;
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFecha");
		return fechaValorCuota;
	}
	
	private List<ProcesamientoFechas> obtenerFechaValorCuotaRepaso(Connection con, List<ProcesamientoFechas> solicitudes) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaValorCuotaRepaso");
		
		for(ProcesamientoFechas solicitud : solicitudes) {
			if("S".equals(solicitud.FondoNacionalDolar)) {

				if(_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) && !solicitud.IdMercadoFondoMutuo.equals(_c.MERCADO_RF)) {
					solicitud.FechaValorCuota = LocalDate.parse(solicitud.FechaCurse, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ISO_LOCAL_DATE);
					continue;
				}
				
				String fechaValorCuota = solicitud.FechaValorCuota;
				String fecha = _c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) ? solicitud.FechaCertificacion : solicitud.FechaCurse;
				
				ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
				InObtenerDiaAnterior inObtenerDiaAnteriorUSA = new InObtenerDiaAnterior();
				InObtenerDiaAnterior.FechasIn fechasInUSA = new InObtenerDiaAnterior.FechasIn();
				
				if(solicitud.FondoNacionalDolar.equals("S") && solicitud.IdMercadoFondoMutuo.equals("RF") && solicitud.TipoSolicitud.equals("RES")) {
					inObtenerDiaAnteriorUSA.excluirConsultarDiaFeriado = true;
				}
				
				fechasInUSA.fechaInicial = fecha;
				fechasInUSA.numeroDias = 1L;
				inObtenerDiaAnteriorUSA.fechas.add(fechasInUSA);
				inObtenerDiaAnteriorUSA.idPais = solicitud.IdPaisMoneda;
				inObtenerDiaAnteriorUSA.diaHabil = calculoValorDiaHabil(solicitud,fecha);
				inObtenerDiaAnteriorUSA.soloCaeDiaHabil = calculoValorSoloCaeDiaHabil(solicitud,fecha);
				
				OutObtenerDiaAnterior outObtenerDiaAnteriorUSA = obtenerDiaAnterior.logica(con, inObtenerDiaAnteriorUSA);
				
				InObtenerDiaAnterior inObtenerDiaAnteriorChile = new InObtenerDiaAnterior();
				InObtenerDiaAnterior.FechasIn fechasInChile = new InObtenerDiaAnterior.FechasIn();
				
				if(solicitud.FondoNacionalDolar.equals("S") && solicitud.IdMercadoFondoMutuo.equals("RF") && solicitud.TipoSolicitud.equals("RES")) {
					inObtenerDiaAnteriorChile.excluirConsultarDiaFeriado = true;
				}
				
				fechasInChile.fechaInicial = fecha;
				fechasInChile.numeroDias = 1L;
				inObtenerDiaAnteriorChile.fechas.add(fechasInChile);
				inObtenerDiaAnteriorChile.idPais = solicitud.IdPaisFondo;
				inObtenerDiaAnteriorChile.diaHabil = calculoValorDiaHabil(solicitud,fecha);//
				inObtenerDiaAnteriorChile.soloCaeDiaHabil = calculoValorSoloCaeDiaHabil(solicitud,fecha);
				
				OutObtenerDiaAnterior outObtenerDiaAnteriorChile = obtenerDiaAnterior.logica(con, inObtenerDiaAnteriorChile);
				
				if(solicitud.PermiteDiaNoHabilFechaValorCuota) {
					java.sql.Date fUSA = Fecha.toDate(outObtenerDiaAnteriorUSA.fechas.get(0).fechaCalculada);
					java.sql.Date fChile = Fecha.toDate(outObtenerDiaAnteriorChile.fechas.get(0).fechaCalculada);
					fechaValorCuota =  fUSA.compareTo(fChile) > 0 ? Fecha.toStringDate(fUSA) : Fecha.toStringDate(fChile);
				}
				if(solicitud.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket && _c.TipoSolicitudAporte.equalsIgnoreCase(solicitud.TipoSolicitud)) {
					fechaValorCuota = solicitud.FechaCurse;
				}
				
				solicitud.FechaValorCuota = fechaValorCuota;
			}
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaValorCuotaRepaso");
		return solicitudes;
	}
	
	private String calculoValorDiaHabil(ProcesamientoFechas solicitud, String fecha) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.calculoValorDiaHabil");
		
		String diaHabil = "N";
		
		Calendar c = Calendar.getInstance();
		java.sql.Date fPivot = Fecha.toDate(fecha);
		c.setTime(fPivot);
		Long day = Long.valueOf(c.get(Calendar.DAY_OF_WEEK));
		
		if(!day.equals(2L)) {
			diaHabil = _c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) || fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";
			
		} else {
			diaHabil = (_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) && !_c.MERCADO_RF.equals(solicitud.IdMercadoFondoMutuo)) || fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";
		}

		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.calculoValorDiaHabil");
		return diaHabil;
	}

	private String calculoValorSoloCaeDiaHabil(ProcesamientoFechas solicitud, String fecha) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.calculoValorSoloCaeDiaHabil");
		String soloCaeDiaHabil = "N";
		
		if(_c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud)) {
			soloCaeDiaHabil = fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";
			logger.trace("FIN::CalcularFechaSolicitudAporteRescate.fechaCaeDiaLunes");
			return soloCaeDiaHabil;
		}
		
		if(_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)) {
			Calendar c = Calendar.getInstance();
			java.sql.Date fPivot = Fecha.toDate(fecha);
			c.setTime(fPivot);
			Long day = Long.valueOf(c.get(Calendar.DAY_OF_WEEK));
			soloCaeDiaHabil = day.equals(2L) && (_c.SI.equals(solicitud.MoneyMarket) || _c.MERCADO_RF.equals(solicitud.IdMercadoFondoMutuo)) ? "N" : "S";
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.calculoValorSoloCaeDiaHabil");
		return soloCaeDiaHabil;
	}

	private Boolean fechaCaeDiaLunes(String fecha, String tipoSolicitud, String moneyMarket) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.fechaCaeDiaLunes");
		Boolean caeDiaLunes = false;
		
		Calendar c = Calendar.getInstance();
		java.sql.Date fPivot = Fecha.toDate(fecha);
		c.setTime(fPivot);
		Long day = Long.valueOf(c.get(Calendar.DAY_OF_WEEK));
		caeDiaLunes = day.equals(2L) && _c.TipoSolicitudAporte.equals(tipoSolicitud) && "N".equals(moneyMarket);
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.fechaCaeDiaLunes");
		return caeDiaLunes;
	}

	private String obtenerKeyFecha(ProcesamientoFechas solicitud, ConfiguracionFechaDias configuracion, String tipoFecha) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerKeyFecha");
		String key = obtenerFechaInicial(solicitud, tipoFecha) + configuracion.CantidadDiasValuta + configuracion.paisCalculo;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerKeyFecha");
		return key;
	}
	
	private List<ProcesamientoFechas> repasarFechaCertificacionRescateGeneraSpot(Connection con, List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.repasarFechaCertificacionRescateGeneraSpot");
		List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud->!solicitud.IdPaisEmpresa.equals(solicitud.IdPaisEmisorSerieFondo) || !solicitud.IdPaisEmpresa.equals(solicitud.IdPaisFondo)).collect(Collectors.toList());
		
		//Se calcula el dia limite de retroceso de la fecha de certificacion (t+2)
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente diaSiguienteIn = new InObtenerDiaSiguiente();
		diaSiguienteIn.p_diaHabil = _c.SI;
		diaSiguienteIn.p_idPais = idPaisEmpresa;
	
		//Se calcula dia anterior si corresponde
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior diaAnteriorIn = new InObtenerDiaAnterior();
		diaAnteriorIn.idPais = idPaisEmpresa;
		diaAnteriorIn.diaHabil = _c.SI;
		
		HashMap<String,OutObtenerDiaAnterior.FechasOut> hashDiaAnteriorLiquidacion = new HashMap<String,OutObtenerDiaAnterior.FechasOut>();
		HashMap<String,OutObtenerDiaSiguiente.FechasOut> hashDosDiasSiguientesEmision = new HashMap<String,OutObtenerDiaSiguiente.FechasOut>();
		
		List<ProcesamientoFechas> solicitudesFiltradas = new ArrayList<ProcesamientoFechas>();
		
		for(ProcesamientoFechas sol : solicitudesAConsultar) {
			if(_c.SI.equalsIgnoreCase(sol.RescateGeneraSpotCertificacion)) {
				InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
				fecha.fechaInicial = sol.FechaLiquidacion;
				fecha.numeroDias = 1L;
				diaAnteriorIn.fechas.add(fecha);
				hashDiaAnteriorLiquidacion.put(fecha.fechaInicial, new OutObtenerDiaAnterior.FechasOut());
				
				InObtenerDiaSiguiente.FechasIn fechaSig = new InObtenerDiaSiguiente.FechasIn();
				fechaSig.fechaInicial = sol.FechaEmision;
				fechaSig.numeroDias = 2L;
				fechaSig.pais = idPaisEmpresa;
				diaSiguienteIn.p_fechas.add(fechaSig);
				hashDosDiasSiguientesEmision.put(fechaSig.fechaInicial, new OutObtenerDiaSiguiente.FechasOut());
				solicitudesFiltradas.add(sol);
			}
		}
		if(diaAnteriorIn.fechas.size() > 0) {
			OutObtenerDiaAnterior diaAnteriorOut = obtenerDiaAnterior.logica(con, diaAnteriorIn);
			
			for(OutObtenerDiaAnterior.FechasOut fecha : diaAnteriorOut.fechas) {
				hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).fechaCalculada = fecha.fechaCalculada;
				hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).fechaInicial = fecha.fechaInicial;
				hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).numeroDias = fecha.numeroDias;
				hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).numeroDiasInicial = fecha.numeroDiasInicial;
			}
			
			OutObtenerDiaSiguiente diaSiguienteOut2 = obtenerDiaSiguiente.logica(con,diaSiguienteIn);
			
			for(OutObtenerDiaSiguiente.FechasOut fecha : diaSiguienteOut2.p_out_fechas) {
				hashDosDiasSiguientesEmision.get(fecha.fechaInicial).fechaCalculada = fecha.fechaCalculada;
				hashDosDiasSiguientesEmision.get(fecha.fechaInicial).fechaInicial = fecha.fechaInicial;
				hashDosDiasSiguientesEmision.get(fecha.fechaInicial).numeroDias = fecha.numeroDias;
				hashDosDiasSiguientesEmision.get(fecha.fechaInicial).numeroDiasInicial = fecha.numeroDiasInicial;
			}
			
			for(ProcesamientoFechas sol : solicitudesFiltradas) {
				if(hashDiaAnteriorLiquidacion.containsKey(sol.FechaLiquidacion)
					&& sol.FechaLiquidacion.equalsIgnoreCase(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaInicial)
					&& ((sol.FechaCertificacion.compareTo(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada) < 0)
					|| sol.FechaCertificacion.equalsIgnoreCase(sol.FechaLiquidacion))) {
					solicitudesAConsultar.get(solicitudesAConsultar.indexOf(sol)).FechaCertificacion = (hashDosDiasSiguientesEmision.get(sol.FechaEmision).fechaCalculada.compareTo(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada) <= 0) ? hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada : sol.FechaLiquidacion;
				}	
			}				
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.repasarFechaCertificacionRescateGeneraSpot");
		return solicitudesAProcesarNoSwitch;
	}
	
	private List<ProcesamientoFechas> repasarFechaCertificacionLiquidacionSeriesExtranjeras(Connection con, List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.repasarFechaCertificacionLiquidacionSeriesExtranjeras");
		List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud->esSerieExtranjera(solicitud.IdPaisEmpresa, solicitud.IdPaisEmisorSerieFondo, solicitud.IdPaisFondo, solicitud.IdPaisMoneda)).collect(Collectors.toList());
		if(solicitudesAConsultar.size() > 0) {
			Map<String,DiaFeriado> feriados = obtenerFeriados(con, solicitudesAConsultar, idPaisEmpresa);
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = obtenerFechaAConsultar(solicitudesAProcesarNoSwitch,feriados,idPaisEmpresa);
			if(fechasAConsultar.size() > 0) {
				HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(con, fechasAConsultar, idPaisEmpresa);
				solicitudesAProcesarNoSwitch.stream().filter(solicitud->!solicitud.IdPaisEmpresa.equals(solicitud.IdPaisEmisorSerieFondo)).forEach(solicitud->{
					if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
						solicitud.FechaCertificacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
					}
					if(fechasCalculadas.get((solicitud.IdInterno * 3) + 2) != null) {
						solicitud.FechaLiquidacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 2).fechaCalculada;
					}
				});
			}
			
		}
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.repasarFechaCertificacionLiquidacionSeriesExtranjeras");
		return solicitudesAProcesarNoSwitch;
	}
	
	private Boolean esSerieExtranjera(Long idPaisEmpresa, Long idPaisEmisorSerieFondo, Long idPaisFondo, Long idPaisMoneda) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.esSerieExtranjera");
		Boolean esSerieExtranjera = !idPaisEmpresa.equals(idPaisEmisorSerieFondo) || !idPaisEmpresa.equals(idPaisFondo) || !idPaisEmpresa.equals(idPaisMoneda);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.esSerieExtranjera");
		return esSerieExtranjera;
	}

	private Map<String,DiaFeriado> obtenerFeriados(Connection con, List<ProcesamientoFechas> solicitudesAConsultar, Long idPaisEmpresa) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFeriados");
		
		List<String> fechas = solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisEmisor.equals(idPaisEmpresa) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.FechaCertificacion)
				.collect(Collectors.toList());
		fechas.addAll(solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisEmisor.equals(idPaisEmpresa) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.FechaLiquidacion)
				.collect(Collectors.toList()));
		List<java.sql.Date> fechasFiltro = fechas.stream().distinct().map(fecha->Fecha.toDate(fecha)).collect(Collectors.toList());
		
		List<Long> idsPaisFiltro = solicitudesAConsultar.stream()
				.filter(solicitud->!solicitud.IdPaisFondo.equals(idPaisEmpresa))
				.map(solicitud->solicitud.IdPaisFondo)
				.collect(Collectors.toList());
		idsPaisFiltro.addAll(solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisFondo.equals(solicitud.IdPaisEmisor) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.IdPaisEmisor)
				.collect(Collectors.toList()));
		idsPaisFiltro.addAll(solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisMoneda.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.IdPaisMoneda)
				.collect(Collectors.toList()));
		idsPaisFiltro.add(idPaisEmpresa);
		idsPaisFiltro = idsPaisFiltro.stream().distinct().collect(Collectors.toList());
		
		
		HashMap<String, Object>  parametros = new HashMap<String, Object>(2);
		parametros.put("Fechas", fechasFiltro);
		parametros.put("IdPais", idsPaisFiltro);
		
		logger.debug("INICIO::SQL_SELECT_DIAESFERIADO");
		SQL sql=new SQL();
		List<DiaFeriado> feriados = sql.query(con, SQL_SELECT_DIAESFERIADO, parametros, DiaFeriado.class);
		List<DiaFeriado> feriadosUnicos = new ArrayList<DiaFeriado>();
		boolean[] agregarFeriadoUnico = {true};
		feriados.forEach(feriado -> {
			agregarFeriadoUnico[0] = true;
			feriadosUnicos.forEach(feriadoUnico -> {
				logger.debug("if:" + (feriadoUnico.IdPais == feriado.IdPais));
				logger.debug("if:" + (feriadoUnico.Fecha.equals(feriado.Fecha)));
				if(feriadoUnico.IdPais == feriado.IdPais && feriadoUnico.Fecha.equals(feriado.Fecha)) {
					agregarFeriadoUnico[0] = false;
				}
			}); 
			if(agregarFeriadoUnico[0] == true) {
				logger.debug("agregarFeriadoUnico:" + agregarFeriadoUnico[0]);
				feriadosUnicos.add(feriado);
			}
		});
		logger.debug("FIN::SQL_SELECT_DIAESFERIADO");
		Map<String,DiaFeriado> hashFeriados = feriadosUnicos.stream().collect(Collectors.toMap(feriado->(Fecha.toStringDate(feriado.Fecha)+feriado.IdPais.toString()), feriado->feriado));
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFeriados");
		
		return hashFeriados;
	}
	
	private Map<String,DiaFeriado> obtenerFeriadosFondosNacionalesUSD(Connection con, List<ProcesamientoFechas> solicitudesAConsultar, Long idPaisEmpresa) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFeriadosFondosNacionalesUSD");

		List<String> fechas = new ArrayList<String>();
		solicitudesAConsultar.stream().forEach(solicitud-> {
			fechas.add(solicitud.FechaCurse);
			fechas.add(solicitud.FechaCertificacion);
			fechas.add(solicitud.FechaLiquidacion);
		});
		
		fechas.addAll(solicitudesAConsultar.stream()
				.map(solicitud-> solicitud.FechaCertificacion)
				.collect(Collectors.toList()));
		
		List<java.sql.Date> fechasFiltro = fechas.stream().distinct().map(fecha->Fecha.toDate(fecha)).collect(Collectors.toList());
		
		List<Long> idsPaisFiltro = solicitudesAConsultar.stream()
				.filter(solicitud->!solicitud.IdPaisFondo.equals(idPaisEmpresa))
				.map(solicitud->solicitud.IdPaisFondo)
				.collect(Collectors.toList());
		idsPaisFiltro.addAll(solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisFondo.equals(solicitud.IdPaisEmisor) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.IdPaisEmisor)
				.collect(Collectors.toList()));
		idsPaisFiltro.addAll(solicitudesAConsultar.stream()
				.filter(solicitud->(!solicitud.IdPaisMoneda.equals(idPaisEmpresa)))
				.map(solicitud->solicitud.IdPaisMoneda)
				.collect(Collectors.toList()));
		idsPaisFiltro.add(idPaisEmpresa);
		idsPaisFiltro = idsPaisFiltro.stream().distinct().collect(Collectors.toList());
		
		
		HashMap<String, Object>  parametros = new HashMap<String, Object>(2);
		parametros.put("Fechas", fechasFiltro);
		parametros.put("IdPais", idsPaisFiltro);
		
		logger.debug("INICIO::SQL_SELECT_DIAESFERIADO");
		SQL sql=new SQL();
		List<DiaFeriado> feriados = sql.query(con, SQL_SELECT_DIAESFERIADO, parametros, DiaFeriado.class);
		logger.debug("FIN::SQL_SELECT_DIAESFERIADO");
		Map<String,DiaFeriado> hashFeriados = feriados.stream().collect(Collectors.toMap(feriado->(Fecha.toStringDate(feriado.Fecha)+feriado.IdPais.toString()), feriado->feriado));
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFeriadosFondosNacionalesUSD");
		
		return hashFeriados;
	}
	
	private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultar(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Long idPaisEmpresa){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultar");
		List<ProcesamientoFechas> solicitudesFechaCertificacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCertificacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
		List<ProcesamientoFechas> solicitudesFechaLiquidacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaLiquidacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCertificacion.stream().distinct().map(solicitud->{
			ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
			fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
			fechaEntrada.numeroDias = 1L;
			fechaEntrada.paises.add(idPaisEmpresa);
			fechaEntrada.paises.add(solicitud.IdPaisFondo);
			fechaEntrada.paises.add(solicitud.IdPaisEmisor);
			fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
			return fechaEntrada;
		}).collect(Collectors.toList());
		
		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaLiquidacion.stream().distinct().map(solicitud->{
			ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
			fechaEntrada.idInterno = ((solicitud.IdInterno * 3) + 2);
			fechaEntrada.numeroDias = 1L;
			fechaEntrada.paises.add(idPaisEmpresa);
			fechaEntrada.paises.add(solicitud.IdPaisFondo);
			fechaEntrada.paises.add(solicitud.IdPaisEmisor);
			fechaEntrada.paises.add(solicitud.IdPaisMoneda);
			fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
			return fechaEntrada;
		}).collect(Collectors.toList());
		fechasAConsultar.addAll(fechasAConsultarCertificacion);
		fechasAConsultar.addAll(fechasAConsultarLiquidacion);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultar");
		return fechasAConsultar;
	}

	private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarCurse(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Long idPaisEmpresa){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarCurse");
		List<ProcesamientoFechas> solicitudesFechaCurse = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCurse(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCurse = solicitudesFechaCurse.stream().distinct().map(solicitud->{
			ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
			fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
			fechaEntrada.numeroDias = 1L;
			fechaEntrada.paises.add(idPaisEmpresa);
			fechaEntrada.paises.add(solicitud.IdPaisFondo);
			fechaEntrada.paises.add(solicitud.IdPaisEmisor);
			fechaEntrada.paises.add(solicitud.IdPaisMoneda);
			fechaEntrada.fechaInicial = solicitud.FechaCurse;
			return fechaEntrada;
		}).collect(Collectors.toList());
		
		fechasAConsultar.addAll(fechasAConsultarCurse);
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarCurse");
		return fechasAConsultar;
	}

	private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarCertificacion(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriadosCertificacion, Map<String,DiaFeriado> feriadosCurse, Long idPaisEmpresa, List<ProcesamientoFechas> fechasSinProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarCertificacion");
		List<ProcesamientoFechas> solicitudesFechaCurse = fechasSinProcesar.stream().filter(solicitud->esFeriadoFechaCurse(feriadosCurse, solicitud, idPaisEmpresa)).collect(Collectors.toList());
		List<ProcesamientoFechas> solicitudesFechaCertificacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCertificacion(feriadosCertificacion, solicitud, idPaisEmpresa)).collect(Collectors.toList());

		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
		
		if(solicitudesFechaCurse.isEmpty() == false) {
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCurse.stream().distinct().map(solicitud->{
				ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
				fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
				fechaEntrada.numeroDias = 1L;
				fechaEntrada.paises.add(idPaisEmpresa);
				fechaEntrada.paises.add(solicitud.IdPaisFondo);
				fechaEntrada.paises.add(solicitud.IdPaisEmisor);
				fechaEntrada.paises.add(solicitud.IdPaisMoneda);
				fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
				return fechaEntrada;
			}).collect(Collectors.toList());
			fechasAConsultar.addAll(fechasAConsultarCertificacion);

		}else {
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCertificacion.stream().distinct().map(solicitud->{
				ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
				fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
				fechaEntrada.numeroDias = 1L;
				fechaEntrada.paises.add(idPaisEmpresa);
				fechaEntrada.paises.add(solicitud.IdPaisFondo);
				fechaEntrada.paises.add(solicitud.IdPaisEmisor);
				fechaEntrada.paises.add(solicitud.IdPaisMoneda);
				fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
				return fechaEntrada;
			}).collect(Collectors.toList());
			fechasAConsultar.addAll(fechasAConsultarCertificacion);	
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarCertificacion");
		return fechasAConsultar;
	}

	private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarLiquidacion(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Map<String,DiaFeriado> feriadosCurse, Long idPaisEmpresa, List<ProcesamientoFechas> fechasSinProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarLiquidacion");
		List<ProcesamientoFechas> solicitudesFechaCurse = fechasSinProcesar.stream().filter(solicitud->esFeriadoFechaCurse(feriadosCurse, solicitud, idPaisEmpresa)).collect(Collectors.toList());
		List<ProcesamientoFechas> solicitudesFechaLiquidacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaLiquidacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());

		List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
		if(solicitudesFechaCurse.isEmpty() == false) {
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaCurse.stream().distinct().map(solicitud->{
				ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
				fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
				fechaEntrada.numeroDias = 1L;
				fechaEntrada.paises.add(idPaisEmpresa);
				fechaEntrada.paises.add(solicitud.IdPaisFondo);
				fechaEntrada.paises.add(solicitud.IdPaisEmisor);
				fechaEntrada.paises.add(solicitud.IdPaisMoneda);
				fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
				return fechaEntrada;
			}).collect(Collectors.toList());
			
			fechasAConsultar.addAll(fechasAConsultarLiquidacion);
		}else {
			List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaLiquidacion.stream().distinct().map(solicitud->{
				ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
				fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
				fechaEntrada.numeroDias = 1L;
				fechaEntrada.paises.add(idPaisEmpresa);
				fechaEntrada.paises.add(solicitud.IdPaisFondo);
				fechaEntrada.paises.add(solicitud.IdPaisEmisor);
				fechaEntrada.paises.add(solicitud.IdPaisMoneda);
				fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
				return fechaEntrada;
			}).collect(Collectors.toList());
			
			fechasAConsultar.addAll(fechasAConsultarLiquidacion);
		}
		
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechaAConsultarLiquidacion");
		return fechasAConsultar;
	}
	
	private Boolean esFeriadoFechaCertificacion(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaCertificacion+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisMoneda.toString()) != null));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		return esFeriadoFechaCertificacion;
	}
	
	private Boolean esFeriadoFechaLiquidacion(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaLiquidacion+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisMoneda.toString()) != null));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		return esFeriadoFechaCertificacion;
	}

	private Boolean esFeriadoFechaCurse(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaCurse+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisMoneda.toString()) != null) || (feriados.get(solicitud.FechaDigitacion+solicitud.IdPaisMoneda.toString()) != null) || (feriados.get(solicitud.FechaDigitacion+solicitud.IdPaisFondo.toString()) != null));
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFecharAConsultar");
		return esFeriadoFechaCertificacion;
	}
	
	private HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> obtenerFechasCalculadasRepaso(Connection con, List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar, Long idPaisEmpresa) throws Exception{
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerFechasCalculadasRepaso");
		ObtenerDiaHabilMultiplesPaises obtenerDiaSiguiente = new ObtenerDiaHabilMultiplesPaises();
		EntradaObtenerDiaHabilMultiplesPaises inObtenerDiaSiguiente = new EntradaObtenerDiaHabilMultiplesPaises();
		inObtenerDiaSiguiente.p_diaHabil = _c.SI;
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = _c.SI;
		inObtenerDiaSiguiente.p_fechas = fechasAConsultar;
		SalidaObtenerDiaHabilMultiplesPaises outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> hashFechasSalida = new HashMap<>();
		for (ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
			hashFechasSalida.put(fechaSalida.idInterno, fechaSalida);
		}
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerFechasCalculadasRepaso");
		return hashFechasSalida;
	}
	
	
	public List<ResultadoFecha> obtenerResultadoFechas(List<ProcesamientoFechas> solicitudesAProcesar){
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerResultadoFechas");
		List<ResultadoFecha> resultados = solicitudesAProcesar.stream().map(solicitud->{
			ResultadoFecha resultadoFecha = new ResultadoFecha();
			resultadoFecha.IdInterno = solicitud.IdInterno;
			resultadoFecha.IdSerieFondo = solicitud.IdSerieFondo;
			resultadoFecha.FechaEmision = Fecha.toDate(solicitud.FechaEmision);
			resultadoFecha.FechaAceptacion = Fecha.toDate(solicitud.FechaCurse);
			resultadoFecha.FechaDigitacion = Fecha.toDate(solicitud.FechaDigitacion);
			resultadoFecha.FechaValorCuota = Fecha.toDate(solicitud.FechaValorCuota);
			resultadoFecha.FechaCertificacion = Fecha.toDate(solicitud.FechaCertificacion);
			resultadoFecha.FechaLiquidacion = Fecha.toDate(solicitud.FechaLiquidacion);
			return resultadoFecha;
		}).collect(Collectors.toList());
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerResultadoFechas");
		return resultados;
	}
		
	private Boolean repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket(SerieFondoSolicitudes serieFondo, String permiteDiaNoHabilFechaValorCuotaMoneyMarket) {
		
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket");
		Boolean result = false;
		result = _c.NO.equalsIgnoreCase(serieFondo.MoneyMarket) && _c.SI.equalsIgnoreCase(serieFondo.FondoNacionalDolar) && _c.SI.equalsIgnoreCase(permiteDiaNoHabilFechaValorCuotaMoneyMarket);
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket");
		return result;
	}

		
	public static void main(String[] args){
		
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_SolicitudAporteRescate\":[{\"IdInterno\":1,\"IdSerieFondo\":\"00115CUSD\",\"FechaEmision\":\"2019-12-18\",\"EstadoFondo\":\"C\",\"EstadoLiquidacion\":\"C\",\"NumeroDiasRetencion\":4,\"NumeroValutaValorCuota\":1,\"NumeroValutaCertificacion\":3,\"NumeroValutaCurse\":0,\"SolicitudBatch\":\"N\",\"TipoSolicitud\":\"RES\",\"FondoExtranjero\":\"S\",\"IdPaisFondo\":0,\"IdPaisEmisor\":0,\"IdPaisMoneda\":223}]}";
			Gson gson = Utils.getGsonBuilder();
			EntradaCalcularFechaSolicitudAporteRescate entrada = gson.fromJson(json, EntradaCalcularFechaSolicitudAporteRescate.class);
			CalcularFechaSolicitudAporteRescate calcularFechaSolicitudAporteRescate = new CalcularFechaSolicitudAporteRescate();
			SalidaCalcularFechaSolicitudAporteRescate objOut = calcularFechaSolicitudAporteRescate.logica(con, entrada);
			System.out.println(objOut.toString());
			con.rollback();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

}