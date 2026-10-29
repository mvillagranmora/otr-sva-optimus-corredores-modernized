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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarValorCuotaHistorico implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarValorCuotaHistorico.class);
	
	private final String SQL_SELECT_VALORES_CUOTA_HISTORICO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarValorCuotaHistorico_SelectValoresCuotaHistorico.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaConsultarValorCuotaHistorico extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Nemotecnico;
		public String p_IdSerieFondo;
		public String p_IdFamiliaFondo;
		public Long p_IdInstrumento;
		public String p_IdEmisor;
		public String p_FechaVigenciaDesde;
		public String p_FechaVigenciaHasta;
	}
	
	public static class SalidaConsultarValorCuotaHistorico extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ValorCuotaHistoricoSalida> P_OUT_VALORES_CUOTAS;
		
		public SalidaConsultarValorCuotaHistorico() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_VALORES_CUOTAS = new ArrayList<ValorCuotaHistoricoSalida>();
		}
	}
	
	public static class ValorCuotaHistorico extends BcsPojo{
		public Long IdValorCuotaHistorico;
		public String IdSerieFondo;
		public java.sql.Date FechaVigencia;
		public BigDecimal ValorCuotaMonedaOrigen;
		public String IdMoneda;
		public String Observacion;
		public java.sql.Date FechaCalculo;
		public java.sql.Timestamp FechaIngresoRegistro;
		public Long IdEmpresa;
		public Long DecimalesValorCuota;
		public String CodigoSerie;
		public String IdFamiliaFondo;
		public Long IdInstrumento;
		public String NombreSerieFondo;
		public String Nemotecnico;
		public String IdEmisor;
		public String RazonSocialFondo;
		public String NombreFamilia;
		public String NombreEmisor;
		public java.sql.Date FechaTipoCambioEsperado;
	}
	
	public static class ValorCuotaHistoricoSalida extends ValorCuotaHistorico{
		public BigDecimal ValorCuota;
		public java.sql.Date FechaTipoCambio; 
		public BigDecimal TipoCambio;
	}
	
	public static class FiltrosValidos extends BcsPojo{
		public Long FiltraPorNemotecnico;
		public Long FiltraPorIdSerieFondo;
		public Long FiltraPorIdFamiliaFondo;
		public Long FiltraPorIdInstrumento;
		public Long FiltraPorIdEmisor;
		public Long FiltraPorFechaVigenciaDesde;
		public Long FiltraPorFechaVigenciaHasta;
		
		public FiltrosValidos() {
			this.FiltraPorNemotecnico = 0L;
			this.FiltraPorIdSerieFondo = 0L;
			this.FiltraPorIdFamiliaFondo = 0L;
			this.FiltraPorIdInstrumento = 0L;
			this.FiltraPorIdEmisor = 0L;
			this.FiltraPorFechaVigenciaDesde = 0L;
			this.FiltraPorFechaVigenciaHasta = 0L;
			
		}
	}
	
	public static class ValorCuotaMonedaSistema extends BcsPojo{
		public Long IdValorCuotaHistorico;
		public BigDecimal ValorCuota;
		public BigDecimal TipoCambio;
		public java.sql.Date FechaTipoCambio;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarValorCuotaHistorico jin = gson.fromJson(in.toString(), EntradaConsultarValorCuotaHistorico.class);
		SalidaConsultarValorCuotaHistorico out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarValorCuotaHistorico logica(Connection con, EntradaConsultarValorCuotaHistorico entrada) throws Exception {
		logger.info("INICIO::ConsultarValorCuotaHistorico");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarValorCuotaHistorico out = new SalidaConsultarValorCuotaHistorico();
		List<ValorCuotaHistorico> valoresCuotaHistorico = obtenerValoresCuotaHistorico(con, entrada);
		out.P_OUT_VALORES_CUOTAS = obtenerValoresCuotaHistoricoSalida(con, entrada.p_IdEmpresa, valoresCuotaHistorico);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarValorCuotaHistorico", finServicio - inicioServicio);
		return out;
	}
	
	private List<ValorCuotaHistorico> obtenerValoresCuotaHistorico(Connection con, EntradaConsultarValorCuotaHistorico entrada) throws Exception{
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerValoresCuotaHistorico");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> parametros = obtenerParametros(entrada);
		List<ValorCuotaHistorico> valoresCuotaHistoricos = sql.query(con, SQL_SELECT_VALORES_CUOTA_HISTORICO, parametros, ValorCuotaHistorico.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerValoresCuotaHistorico", fin - inicio);
		return valoresCuotaHistoricos;
	}
	
	private HashMap<String,Object> obtenerParametros(EntradaConsultarValorCuotaHistorico entrada){
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerParametros");
		long inicio = System.currentTimeMillis();
		FiltrosValidos filtrosValidos = new FiltrosValidos();
		HashMap<String,Object> parametrosConsulta = new HashMap<String, Object>();
		parametrosConsulta.put("Nemotecnico", entrada.p_Nemotecnico);
		parametrosConsulta.put("IdSerieFondo", entrada.p_IdSerieFondo);
		parametrosConsulta.put("IdFamiliaFondo", entrada.p_IdFamiliaFondo);
		parametrosConsulta.put("IdInstrumento", entrada.p_IdInstrumento);
		parametrosConsulta.put("IdEmisor", entrada.p_IdEmisor);
		parametrosConsulta.put("FechaVigenciaDesde", entrada.p_FechaVigenciaDesde);
		parametrosConsulta.put("FechaVigenciaHasta", entrada.p_FechaVigenciaHasta);
		
		if(esFiltroValido(entrada.p_Nemotecnico, "")) {
			filtrosValidos.FiltraPorNemotecnico = 1L;
		}
		if(esFiltroValido(entrada.p_IdSerieFondo, "")) {
			filtrosValidos.FiltraPorIdSerieFondo = 1L;
		}
		if(esFiltroValido(entrada.p_IdFamiliaFondo, "")) {
			filtrosValidos.FiltraPorIdFamiliaFondo = 1L;
		}
		if(esFiltroValido(entrada.p_IdInstrumento, 0L)) {
			filtrosValidos.FiltraPorIdInstrumento = 1L;
		}
		if(esFiltroValido(entrada.p_IdEmisor, "")) {
			filtrosValidos.FiltraPorIdEmisor = 1L;
		}
		if(esFiltroValido(entrada.p_FechaVigenciaDesde, "0001-01-01")) {
			filtrosValidos.FiltraPorFechaVigenciaDesde = 1L;
		}
		if(esFiltroValido(entrada.p_FechaVigenciaHasta, "0001-01-01")) {
			filtrosValidos.FiltraPorFechaVigenciaHasta = 1L;
		}
		
		parametrosConsulta.put("FiltraPorNemotecnico", filtrosValidos.FiltraPorNemotecnico);
		parametrosConsulta.put("FiltraPorIdSerieFondo", filtrosValidos.FiltraPorIdSerieFondo);
		parametrosConsulta.put("FiltraPorIdFamiliaFondo", filtrosValidos.FiltraPorIdFamiliaFondo );
		parametrosConsulta.put("FiltraPorIdInstrumento", filtrosValidos.FiltraPorIdInstrumento);
		parametrosConsulta.put("FiltraPorIdEmisor", filtrosValidos.FiltraPorIdEmisor);
		parametrosConsulta.put("FiltraPorFechaVigenciaDesde", filtrosValidos.FiltraPorFechaVigenciaDesde);
		parametrosConsulta.put("FiltraPorFechaVigenciaHasta", filtrosValidos.FiltraPorFechaVigenciaHasta);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerParametros", fin - inicio);
		return parametrosConsulta;
	}
	
	private Boolean esFiltroValido(String filtro, String valorPorDefecto) {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.esFiltroValido");
		long inicio = System.currentTimeMillis();
		Boolean esFiltroValido = filtro != null && !valorPorDefecto.equals(filtro);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.esFiltroValido", fin - inicio);
		return esFiltroValido;
	}
	
	private Boolean esFiltroValido(Long filtro, Long valorPorDefecto) {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.esFiltroValido");
		long inicio = System.currentTimeMillis();
		Boolean esFiltroValido = filtro != null && !valorPorDefecto.equals(filtro);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.esFiltroValido", fin - inicio);
		return esFiltroValido;
	}
	
	private List<ValorCuotaHistoricoSalida> obtenerValoresCuotaHistoricoSalida(Connection con, Long idEmpresa, List<ValorCuotaHistorico> valoresCuotaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerValoresCuotaHistoricoSalida");
		long inicio = System.currentTimeMillis();
		List<ValorCuotaHistorico> valoresCuotaHistoricoConFechaTipoCambioEsperado = obtenerValoresCuotaConFechaTipoCambioEsperado(con, idEmpresa, valoresCuotaHistorico);
		Map<Long, ValorCuotaMonedaSistema> valoresCuotaMonedaSistema = obtenerValoresCuotaMonedaSistema(con, idEmpresa, valoresCuotaHistoricoConFechaTipoCambioEsperado);
		List<ValorCuotaHistoricoSalida> valoresCuotaHistoricoSalida = valoresCuotaHistoricoConFechaTipoCambioEsperado.parallelStream().map(valorCuota->{
			ValorCuotaMonedaSistema valorCuotaMonedaSistema = valoresCuotaMonedaSistema.get(valorCuota.IdValorCuotaHistorico);
			ValorCuotaHistoricoSalida valorCuotaHistoricoSalida = new ValorCuotaHistoricoSalida();
			valorCuotaHistoricoSalida.IdValorCuotaHistorico = valorCuota.IdValorCuotaHistorico;
			valorCuotaHistoricoSalida.IdSerieFondo = valorCuota.IdSerieFondo;
			valorCuotaHistoricoSalida.FechaVigencia = valorCuota.FechaVigencia;
			valorCuotaHistoricoSalida.ValorCuotaMonedaOrigen = valorCuota.ValorCuotaMonedaOrigen;
			valorCuotaHistoricoSalida.IdMoneda = valorCuota.IdMoneda;
			valorCuotaHistoricoSalida.Observacion = valorCuota.Observacion;
			valorCuotaHistoricoSalida.FechaCalculo = valorCuota.FechaCalculo;
			valorCuotaHistoricoSalida.FechaIngresoRegistro = valorCuota.FechaIngresoRegistro;
			valorCuotaHistoricoSalida.IdEmpresa = valorCuota.IdEmpresa;
			valorCuotaHistoricoSalida.DecimalesValorCuota = valorCuota.DecimalesValorCuota;
			valorCuotaHistoricoSalida.CodigoSerie = valorCuota.CodigoSerie;
			valorCuotaHistoricoSalida.IdFamiliaFondo = valorCuota.IdFamiliaFondo;
			valorCuotaHistoricoSalida.IdInstrumento = valorCuota.IdInstrumento;
			valorCuotaHistoricoSalida.NombreSerieFondo = valorCuota.NombreSerieFondo;
			valorCuotaHistoricoSalida.Nemotecnico = valorCuota.Nemotecnico;
			valorCuotaHistoricoSalida.IdEmisor = valorCuota.IdEmisor;
			valorCuotaHistoricoSalida.RazonSocialFondo = valorCuota.RazonSocialFondo;
			valorCuotaHistoricoSalida.NombreFamilia = valorCuota.NombreFamilia;
			valorCuotaHistoricoSalida.NombreEmisor = valorCuota.NombreEmisor;
			valorCuotaHistoricoSalida.FechaTipoCambioEsperado = valorCuota.FechaTipoCambioEsperado;
			if(valorCuotaMonedaSistema != null) {
				valorCuotaHistoricoSalida.ValorCuota = valorCuotaMonedaSistema.ValorCuota;
				valorCuotaHistoricoSalida.TipoCambio = valorCuotaMonedaSistema.TipoCambio;
				valorCuotaHistoricoSalida.FechaTipoCambio = valorCuotaMonedaSistema.FechaTipoCambio;
			}
			return valorCuotaHistoricoSalida;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerValoresCuotaHistoricoSalida", fin - inicio);
		return valoresCuotaHistoricoSalida;
	}
	
	private List<ValorCuotaHistorico> obtenerValoresCuotaConFechaTipoCambioEsperado(Connection con, Long idEmpresa, List<ValorCuotaHistorico> valoresCuotaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerValoresCuotaConFechaTipoCambioEsperado");
		long inicio = System.currentTimeMillis();
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, idEmpresa);
		List<String> fechas = valoresCuotaHistorico.parallelStream()
				.map(valorCuota->{
					return Fecha.toStringDate(valorCuota.FechaVigencia);
				})
				.distinct()
				.collect(Collectors.toList());
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
		Map<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCalculadas = outDiaSiguiente.p_out_fechas.parallelStream()
				.collect(Collectors.toMap(fecha->fecha.fechaInicial,fecha->fecha));
		List<ValorCuotaHistorico> valoresCuotaConFechaTipoCambioEsperado = valoresCuotaHistorico.parallelStream()
				.map(valorCuota->{
					ValorCuotaHistoricoSalida valorCuotaHistoricoConFechaTipoCambioEsperado = new ValorCuotaHistoricoSalida();
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdValorCuotaHistorico = valorCuota.IdValorCuotaHistorico;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdSerieFondo = valorCuota.IdSerieFondo;
					valorCuotaHistoricoConFechaTipoCambioEsperado.FechaVigencia = valorCuota.FechaVigencia;
					valorCuotaHistoricoConFechaTipoCambioEsperado.ValorCuotaMonedaOrigen = valorCuota.ValorCuotaMonedaOrigen;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdMoneda = valorCuota.IdMoneda;
					valorCuotaHistoricoConFechaTipoCambioEsperado.Observacion = valorCuota.Observacion;
					valorCuotaHistoricoConFechaTipoCambioEsperado.FechaCalculo = valorCuota.FechaCalculo;
					valorCuotaHistoricoConFechaTipoCambioEsperado.FechaIngresoRegistro = valorCuota.FechaIngresoRegistro;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdEmpresa = valorCuota.IdEmpresa;
					valorCuotaHistoricoConFechaTipoCambioEsperado.DecimalesValorCuota = valorCuota.DecimalesValorCuota;
					valorCuotaHistoricoConFechaTipoCambioEsperado.CodigoSerie = valorCuota.CodigoSerie;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdFamiliaFondo = valorCuota.IdFamiliaFondo;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdInstrumento = valorCuota.IdInstrumento;
					valorCuotaHistoricoConFechaTipoCambioEsperado.NombreSerieFondo = valorCuota.NombreSerieFondo;
					valorCuotaHistoricoConFechaTipoCambioEsperado.Nemotecnico = valorCuota.Nemotecnico;
					valorCuotaHistoricoConFechaTipoCambioEsperado.IdEmisor = valorCuota.IdEmisor;
					valorCuotaHistoricoConFechaTipoCambioEsperado.RazonSocialFondo = valorCuota.RazonSocialFondo;
					valorCuotaHistoricoConFechaTipoCambioEsperado.NombreFamilia = valorCuota.NombreFamilia;
					valorCuotaHistoricoConFechaTipoCambioEsperado.NombreEmisor = valorCuota.NombreEmisor;
					valorCuotaHistoricoConFechaTipoCambioEsperado.FechaTipoCambioEsperado = Fecha.toDate("0001-01-01");
					ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut fechaCalculada = fechasCalculadas.get(Fecha.toStringDate(valorCuota.FechaVigencia));
					if(fechaCalculada != null) {
						valorCuotaHistoricoConFechaTipoCambioEsperado.FechaTipoCambioEsperado = Fecha.toDate(fechaCalculada.fechaCalculada);
					}
					return valorCuotaHistoricoConFechaTipoCambioEsperado;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerValoresCuotaConFechaTipoCambioEsperado", fin - inicio);
		return valoresCuotaConFechaTipoCambioEsperado;
	}
	
	private Long obtenerIdPaisEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerIdPaisEmpresa");
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresa.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerIdPaisEmpresa");
		return idPaisEmpresa;
	}
	
	private Map<Long, ValorCuotaMonedaSistema> obtenerValoresCuotaMonedaSistema(Connection con, Long idEmpresa, List<ValorCuotaHistorico> valoresCuotaHistorico) throws Exception{
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerValoresCuotaMonedaSistema");
		long inicio = System.currentTimeMillis();
		String idMonedaSistema = obtenerMonedaSistema(con, idEmpresa);
		List<ValorCuotaHistorico> valoresCuotaMonedaInternacional = valoresCuotaHistorico.parallelStream().filter(valorCuota->esMonedaInternacional(valorCuota.IdMoneda, idMonedaSistema)).collect(Collectors.toList());
		List<CalculoEntrada> calculosValoresCuota = valoresCuotaMonedaInternacional.parallelStream().map(valorCuota->{
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 999L;
			calculoEntrada.IdInterno = valorCuota.IdValorCuotaHistorico;
			calculoEntrada.Fecha = Fecha.stringToSqlDate(valorCuota.FechaTipoCambioEsperado);
			calculoEntrada.IdMonedaOrigen = valorCuota.IdMoneda;
			calculoEntrada.IdMonedaDestino = idMonedaSistema;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = valorCuota.ValorCuotaMonedaOrigen;
			calculoEntrada.IdUso = "";
			return calculoEntrada;
		}).collect(Collectors.toList());
		List<CalculoEntrada> calculosTipoCambio = valoresCuotaMonedaInternacional.parallelStream().map(valorCuota->{
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 999L;
			calculoEntrada.IdInterno = valorCuota.IdValorCuotaHistorico * -1;
			calculoEntrada.Fecha = Fecha.stringToSqlDate(valorCuota.FechaTipoCambioEsperado);
			calculoEntrada.IdMonedaOrigen = valorCuota.IdMoneda;
			calculoEntrada.IdMonedaDestino = idMonedaSistema;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = BigDecimal.ONE;
			calculoEntrada.IdUso = "";
			return calculoEntrada;
		}).collect(Collectors.toList());
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 999L;
		calcularParidadEntrada.p_CalculosEntrada.addAll(calculosValoresCuota);
		calcularParidadEntrada.p_CalculosEntrada.addAll(calculosTipoCambio);
		CalcularParidadSalida calculosSalida = calcularParidad.logica(con, calcularParidadEntrada);
		Map<Long, CalculoSalida> calculosMonedaInternacional = calculosSalida.p_out_CalculosSalida.parallelStream().collect(Collectors.toMap(paridad->paridad.IdInterno, paridad->paridad));
		List<ValorCuotaMonedaSistema> valoresCuotaMonedaSistema = valoresCuotaHistorico.parallelStream().map(valorCuota->{
			ValorCuotaMonedaSistema valorCuotaMonedaSistema = new ValorCuotaMonedaSistema();
			valorCuotaMonedaSistema.IdValorCuotaHistorico = valorCuota.IdValorCuotaHistorico;
			valorCuotaMonedaSistema.TipoCambio = BigDecimal.ZERO;
			valorCuotaMonedaSistema.ValorCuota = BigDecimal.ZERO;
			if(esMonedaInternacional(valorCuota.IdMoneda, idMonedaSistema)) {
				CalculoSalida calculoValorCuota = calculosMonedaInternacional.get(valorCuota.IdValorCuotaHistorico);
				CalculoSalida calculoTipoCambio = calculosMonedaInternacional.get(valorCuota.IdValorCuotaHistorico * -1);
				if(calculoValorCuota != null) {
					valorCuotaMonedaSistema.ValorCuota = calculoValorCuota.MontoDestino;
				}
				if(calculoTipoCambio != null) {
					valorCuotaMonedaSistema.TipoCambio = calculoTipoCambio.MontoDestino;
					valorCuotaMonedaSistema.FechaTipoCambio = calculoTipoCambio.FechaParametroFinanciero;
				}
			} else {
				valorCuotaMonedaSistema.ValorCuota = valorCuota.ValorCuotaMonedaOrigen;
			}
			return valorCuotaMonedaSistema;
		}).collect(Collectors.toList());
		Map<Long, ValorCuotaMonedaSistema> resultado = valoresCuotaMonedaSistema.parallelStream().collect(Collectors.toMap(valorCuota->valorCuota.IdValorCuotaHistorico, valorCuota->valorCuota));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerValoresCuotaMonedaSistema", fin - inicio);
		return resultado;
	} 
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.obtenerMonedaSistema");
		long inicio = System.currentTimeMillis();
		String idMoneda = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.obtenerMonedaSistema", fin - inicio);
		return idMoneda;
	}
	
	private Boolean esMonedaInternacional(String idMoneda, String idMonedaSistema) {
		logger.trace("INICIO::ConsultarValorCuotaHistorico.esMonedaInternacional");
		long inicio = System.currentTimeMillis();
		Boolean esMonedaInternacional = !idMoneda.equals(idMonedaSistema);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarValorCuotaHistorico.esMonedaInternacional", fin - inicio);
		return esMonedaInternacional;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_IdModulo\":\"FMM\",\"p_FechaVigenciaDesde\":\"2020-10-02\",\"p_FechaVigenciaHasta\":\"2020-10-02\",\"p_IdSerieFondo\":\"00049APVUSD\"}";
			EntradaConsultarValorCuotaHistorico Objin = gson.fromJson(json, EntradaConsultarValorCuotaHistorico.class);
			ConsultarValorCuotaHistorico  demo=new ConsultarValorCuotaHistorico();				
			try {
				SalidaConsultarValorCuotaHistorico ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_VALORES_CUOTAS.toString());
				System.out.println(ObjOut.P_OUT_VALORES_CUOTAS.size());
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
