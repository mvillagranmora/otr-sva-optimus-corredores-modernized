package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSolicitudAporteRescateUI implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarSolicitudAporteRescateUI.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql");
	
	public static class EntradaConsultarSolicitudAporteRescateUI extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaAceptacionDesde;
		public String p_FechaAceptacionHasta;
		public String p_IdTipoMovimientoFondoMutuo;
		public String p_Estado;
		public String p_IdEmisor;
		public String p_IdSerieFondo;
		public Long p_IdCliente;
		public Long p_IdProductoAgrupador;
		public Long p_IdPortafolio;
		public String p_TipoSolicitud;
		public String p_IdMoneda;
		public Long p_IdSolicitudAporteRescate;
	}
	
	public static class SalidaConsultarSolicitudAporteRescateUI extends BcsPojo{
		public List<Solicitud> P_OUT_RESULTADO; 
	}
	
	public static class Solicitud extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public java.sql.Date FechaEmision;
		public java.sql.Timestamp FechaIngresoRegistro;
		public String IdTipoMovimientoFondoMutuo;
		public String TipoSolicitud;
		public String DescripcionTipoMovimiento;
		public String CalculoMontoCuotaUI;
		public String EstadoSolicitud;
		public String NombreEstado;
		public String NombreFondo;
		public String NombreFondoReal;
		public String IdSerieFondo;
		public String NombreSerieFondo;
		public String CalculoPorMontoCuotaUI;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		
		public java.sql.Date FechaValorCuota;
		public BigDecimal Monto;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public Long IdInstrumento;
		public String IdRegimenTributario;
		public String IdRegimenTributarioAPV;
		public String DescripcionPortafolio;
		public java.sql.Date FechaCertificacion;
		public java.sql.Date FechaLiquidacion;
		public java.sql.Date FechaAceptacion;
		public java.sql.Date FechaAnulacion;
		public java.sql.Timestamp FechaAceptacionRegistro;
		public java.sql.Timestamp FechaCertificacionRegistro;
		public java.sql.Timestamp FechaLiquidacionRegistro;
		public java.sql.Timestamp FechaAnulacionRegistro;
		public String NombreEmpleado;
		public String IdEmisor;
		public Long IdEmpresa;
		public String IdMoneda;
		public String TextoPreferenciaPago;
		public String Glosa;
		public Long DecimalesValorCuota;
		public Long DecimalesCuota;
		public String EsOvernightUI;
		public Long IdSolicitudAporteRescateRelacionado;
		public String DescripcionPortafolioDestino;
		public Long IdPortafolioDestino;
		public Long IdClienteDestino;
		public String DocumentoIdentidadDestino;
		public String NombreCompletoDestino;
		public String EsIndicadorMLTUI;
		public BigDecimal MontoComision;
		public BigDecimal MontoComisionLocal;
		public String IdTipoMovimientoFondoMutuoRelacionado;
		public String DescripcionTipoMovimientoRelacionado;
		public String IdMonedaFondo;
		public String EsDiferido;
		public String EsDiferidoUI;
		public BigDecimal CostoMonedaLocal;
		public BigDecimal ParidadFechaInversion;
		public BigDecimal MontoInversion;
		public BigDecimal NumeroCuotasInversion;
		public BigDecimal ValorCuotaInversion;
		public BigDecimal CostoMonedaLocalInversion;
		public BigDecimal ParidadFechaCertificacion;
		public String CalculoPorMontoCuota;
		public String IdSerieFondoDestino;
		public String IdEmisorDestino;
		public String SolicitudBatch;
		public String OrigenOperacion;
		public String ComisionInformada;
		public String EstadoLiquidacion;
		public String NombreEstadoLiquidacion;
		public Long FolioCartaLiquidacion;
		public String CodigoPortafolioDestino;
		public BigDecimal ValorUFFechaInversion;
		public BigDecimal Spread;
		public java.sql.Date FechaOriginalInversion;
		public BigDecimal MontoMovimientoAsociado;
		public BigDecimal NumeroCuotasMovimientoAsociado;
		public String IndicadorMLT;
		public String IdEmisorOrigen;
		public String CodigoPortafolio;
		public String CodigoSerie;
		public String IdFondoRelacionado;
		public String CodigoSerieRelacionado;
		public String DescripcionRegimenTributario;
		public String IdFondo;
		public String DescripcionTipoSolicitud;
		public BigDecimal MontoMonedaSistema;
		public Long IdDividendo;
		public String Switch;
		public String SwitchUI;
		public String IdTipoCanalIngreso;
		public Long FolioContrato;
		public String Producto;
		public Long IdProductoAgrupador;
		public String CodigoUsuario;
		public Long IdIngresoEgreso;
		public String NombreEstadoTesoreria;
		public String FechaLiquidacionTesoreria;
		public Long IdOperacionSpot;
		public String NombreEstadoSpot;
		public String NombreEstadoConfirmacionSpot;
		public java.sql.Date FechaLiquidacionSpot;
		public String IdTipoOperacionSpot;
		public String NombreTipoOperacionSpot;
		public Long IdOmnibus;
		public Long IdAgrupador;
		public BigDecimal RentabilidadReal;
		public BigDecimal CostoUF;
		public Long IdMatrizAgrupador;
	    public String EsADC;
	    public BigDecimal MontoRetencion;
	    public String FolioContratoOperacion;
	    public String TipoContratoOperacion;
	    public Long IdTraspaso;
	    public String IdMedioDePago;
	    public String IdMonedaSistema;
	    public String IdMonedaPago;
	    public BigDecimal MontoMonedaSistemaInicial;
	    public Long FolioCarteraAsociadoComision;
	    public String Modificaciones;
	    public String ISIN;
	    public String EntidadOrigen;
	    public String EntidadDestino;
	    public String PeriodoPago;
	}
	
	public static class FiltrosValidos extends BcsPojo{
		public Long FiltraPorFechaAceptacionDesde;
		public Long FiltraPorFechaAceptacionHasta;
		public Long FiltraPorIdTipoMovimientoFondoMutuo;
		public Long FiltraPorEstado;
		public Long FiltraPorIdEmisor;
		public Long FiltraPorIdSerieFondo;
		public Long FiltraPorIdCliente;
		public Long FiltraPorIdProductoAgrupador;
		public Long FiltraPorIdPortafolio;
		public Long FiltraPorTipoSolicitud;
		public Long FiltraPorIdMoneda;
		public Long FiltraPorIdSolicitudAporteRescate;
		
		public FiltrosValidos() {
			this.FiltraPorFechaAceptacionDesde = 0L;
			this.FiltraPorFechaAceptacionHasta = 0L;
			this.FiltraPorIdTipoMovimientoFondoMutuo = 0L;
			this.FiltraPorEstado = 0L;
			this.FiltraPorIdEmisor = 0L;
			this.FiltraPorIdSerieFondo = 0L;
			this.FiltraPorIdCliente = 0L;
			this.FiltraPorIdProductoAgrupador = 0L;
			this.FiltraPorIdPortafolio = 0L;
			this.FiltraPorTipoSolicitud = 0L;
			this.FiltraPorIdMoneda = 0L;
			this.FiltraPorIdSolicitudAporteRescate = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarSolicitudAporteRescateUI jin = gson.fromJson(in.toString(), EntradaConsultarSolicitudAporteRescateUI.class);
		SalidaConsultarSolicitudAporteRescateUI out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarSolicitudAporteRescateUI logica(Connection con, EntradaConsultarSolicitudAporteRescateUI entrada) throws Exception {
		logger.info("INICIO::ConsultarSolicitudAporteRescateUI");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarSolicitudAporteRescateUI out = new SalidaConsultarSolicitudAporteRescateUI();
		List<Solicitud> solicitudes = obtenerSolicitudes(con, entrada);
		solicitudes = valorizarSolicitud(con, entrada.p_IdEmpresa, solicitudes);
		BigDecimal porcentajeRetencion = obtenerPorcentajeRetencion(con, entrada.p_IdEmpresa);
		solicitudes = calcularMontoRetencion(con, porcentajeRetencion, solicitudes);
		out.P_OUT_RESULTADO = solicitudes;
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarSolicitudAporteRescateUI", finServicio - inicioServicio);
		return out;
	}
	
	private List<Solicitud> obtenerSolicitudes(Connection con, EntradaConsultarSolicitudAporteRescateUI entrada) throws SQLException{
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.obtenerSolicitudes");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = obtenerParametros(entrada);
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, parametrosConsulta, Solicitud.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.obtenerSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private HashMap<String,Object> obtenerParametros(EntradaConsultarSolicitudAporteRescateUI entrada){
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.obtenerParametros");
		long inicio = System.currentTimeMillis();
		FiltrosValidos filtrosValidos = new FiltrosValidos();
		HashMap<String,Object> parametrosConsulta = new HashMap<String, Object>();
		Long idProductoAgrupadorReal = obtenerIdProductoAgrupadorReal(entrada.p_IdProductoAgrupador);
		parametrosConsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		parametrosConsulta.put("FechaAceptacionDesde", entrada.p_FechaAceptacionDesde);
		parametrosConsulta.put("FechaAceptacionHasta", entrada.p_FechaAceptacionHasta);
		parametrosConsulta.put("IdTipoMovimientoFondoMutuo", entrada.p_IdTipoMovimientoFondoMutuo);
		parametrosConsulta.put("Estado", entrada.p_Estado);
		parametrosConsulta.put("IdEmisor", entrada.p_IdEmisor);
		parametrosConsulta.put("IdSerieFondo", entrada.p_IdSerieFondo);
		parametrosConsulta.put("IdCliente", entrada.p_IdCliente);
		parametrosConsulta.put("IdProductoAgrupador", idProductoAgrupadorReal);
		parametrosConsulta.put("IdPortafolio", entrada.p_IdPortafolio);
		parametrosConsulta.put("TipoSolicitud", entrada.p_TipoSolicitud);
		parametrosConsulta.put("IdMoneda", entrada.p_IdMoneda);
		parametrosConsulta.put("IdSolicitudAporteRescate", entrada.p_IdSolicitudAporteRescate);
		
		if(esFiltroValido(entrada.p_FechaAceptacionDesde, "0001-01-01")) {
			filtrosValidos.FiltraPorFechaAceptacionDesde = 1L;
		}
		if(esFiltroValido(entrada.p_FechaAceptacionHasta, "0001-01-01")) {
			filtrosValidos.FiltraPorFechaAceptacionHasta = 1L;
		}
		if(esFiltroValido(entrada.p_IdTipoMovimientoFondoMutuo, "")) {
			filtrosValidos.FiltraPorIdTipoMovimientoFondoMutuo = 1L;
		}
		if(esFiltroValido(entrada.p_Estado, "")) {
			filtrosValidos.FiltraPorEstado = 1L;
		}
		if(esFiltroValido(entrada.p_IdEmisor, "")) {
			filtrosValidos.FiltraPorIdEmisor = 1L;
		}
		if(esFiltroValido(entrada.p_IdSerieFondo, "")) {
			filtrosValidos.FiltraPorIdSerieFondo = 1L;
		}
		if(esFiltroValido(entrada.p_IdCliente, 0L)) {
			filtrosValidos.FiltraPorIdCliente = 1L;
		}
		if(esFiltroValido(entrada.p_IdProductoAgrupador, 0L)) {
			filtrosValidos.FiltraPorIdProductoAgrupador = 1L;
		}
		if(esFiltroValido(entrada.p_IdPortafolio, 0L)) {
			filtrosValidos.FiltraPorIdPortafolio = 1L;
		}
		if(esFiltroValido(entrada.p_TipoSolicitud, "")) {
			filtrosValidos.FiltraPorTipoSolicitud = 1L;
		}
		if(esFiltroValido(entrada.p_IdMoneda, "")) {
			filtrosValidos.FiltraPorIdMoneda = 1L;
		}
		if(esFiltroValido(entrada.p_IdSolicitudAporteRescate, 0L)) {
			filtrosValidos.FiltraPorIdSolicitudAporteRescate = 1L;
		}
		parametrosConsulta.put("FiltraPorFechaAceptacionDesde", filtrosValidos.FiltraPorFechaAceptacionDesde);
		parametrosConsulta.put("FiltraPorFechaAceptacionHasta", filtrosValidos.FiltraPorFechaAceptacionHasta);
		parametrosConsulta.put("FiltraPorIdTipoMovimientoFondoMutuo", filtrosValidos.FiltraPorIdTipoMovimientoFondoMutuo);
		parametrosConsulta.put("FiltraPorEstado", filtrosValidos.FiltraPorEstado);
		parametrosConsulta.put("FiltraPorIdEmisor", filtrosValidos.FiltraPorIdEmisor);
		parametrosConsulta.put("FiltraPorIdSerieFondo", filtrosValidos.FiltraPorIdSerieFondo);
		parametrosConsulta.put("FiltraPorIdCliente", filtrosValidos.FiltraPorIdCliente);
		parametrosConsulta.put("FiltraPorIdProductoAgrupador", filtrosValidos.FiltraPorIdProductoAgrupador);
		parametrosConsulta.put("FiltraPorIdPortafolio", filtrosValidos.FiltraPorIdPortafolio);
		parametrosConsulta.put("FiltraPorTipoSolicitud", filtrosValidos.FiltraPorTipoSolicitud);
		parametrosConsulta.put("FiltraPorIdMoneda", filtrosValidos.FiltraPorIdMoneda);
		parametrosConsulta.put("FiltraPorIdSolicitudAporteRescate", filtrosValidos.FiltraPorIdSolicitudAporteRescate);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.obtenerParametros", fin - inicio);
		return parametrosConsulta;
	}
	
	public Long obtenerIdProductoAgrupadorReal(Long idProductoAgrupadorEntrada) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.obtenerIdProductoAgrupadorReal");
		long inicio = System.currentTimeMillis();
		Long idProductoAgrupadorReal = idProductoAgrupadorEntrada;
		if(esProductoAgrupadorNoAPV(idProductoAgrupadorEntrada)) {
			idProductoAgrupadorReal = 0L;
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.obtenerIdProductoAgrupadorReal", fin - inicio);
		return idProductoAgrupadorReal;
	}
	
	public Boolean esProductoAgrupadorNoAPV(Long idProductoAgrupadorEntrada) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esProductoAgrupadorNoAPV");
		long inicio = System.currentTimeMillis();
		Boolean esProductoAgrupadorNoAPV = _c.IdProductoAgrupadorNoAPV.equals(idProductoAgrupadorEntrada);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esProductoAgrupadorNoAPV", fin - inicio);
		return esProductoAgrupadorNoAPV;
	}
	
	private Boolean esFiltroValido(String filtro, String valorPorDefecto) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esFiltroValido");
		long inicio = System.currentTimeMillis();
		Boolean esFiltroValido = filtro != null && !valorPorDefecto.equals(filtro);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esFiltroValido", fin - inicio);
		return esFiltroValido;
	}
	
	private Boolean esFiltroValido(Long filtro, Long valorPorDefecto) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esFiltroValido");
		long inicio = System.currentTimeMillis();
		Boolean esFiltroValido = filtro != null && !valorPorDefecto.equals(filtro);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esFiltroValido", fin - inicio);
		return esFiltroValido;
	}
	
	private List<Solicitud> valorizarSolicitud(Connection con, Long idEmpresa, List<Solicitud> solicitudes) throws Exception{
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.valorizarSolicitud");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		
		entradaCalculo.p_Series = solicitudes.parallelStream()
		.filter(solicitud->{
			return esSolicitudPendienteCertificacion(solicitud.EstadoSolicitud);
		})
		.map(solicitud->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = solicitud.IdSolicitudAporteRescate;
			calculoMontoCuota.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			calculoMontoCuota.IdSerieFondo = solicitud.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			calculoMontoCuota.Monto = solicitud.Monto;
		 	calculoMontoCuota.NumeroCuotas = solicitud.NumeroCuotas;
		 	calculoMontoCuota.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = solicitud.IdMoneda;
		 	calculoMontoCuota.TipoSolicitud =  solicitud.TipoSolicitud;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = idEmpresa;
		 	calculoMontoCuota.IdTipoMovimientoFondoMutuo =solicitud.IdTipoMovimientoFondoMutuo == null ? "":solicitud.IdTipoMovimientoFondoMutuo ;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		solicitudes.parallelStream()
		.filter(solicitud->{
			return esSolicitudPendienteCertificacion(solicitud.EstadoSolicitud);
		})
		.forEach(solicitud->{
			CalcularMontoNumeroCuota.Resultado resultadoValorizacion = montosValorizados.get(solicitud.IdSolicitudAporteRescate);
			if(esValorizacionCorrecta(resultadoValorizacion)) {
				solicitud.CostoMonedaLocal = resultadoValorizacion.MontoDestino;
				solicitud.Monto = resultadoValorizacion.Monto;
				solicitud.ValorCuota = resultadoValorizacion.ValorCuota;
				solicitud.NumeroCuotas = resultadoValorizacion.NumeroCuotas;
				solicitud.ParidadFechaCertificacion = resultadoValorizacion.ValorParidad;
				solicitud.CostoUF = resultadoValorizacion.CostoEnFactorAjuste;
				solicitud.ValorUFFechaInversion = resultadoValorizacion.ValorFactorAjuste;
				solicitud.MontoMonedaSistema = esCambioMontoMonedaSistema(solicitud) ? resultadoValorizacion.MontoMonedaSistema : solicitud.MontoMonedaSistema;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.valorizarSolicitud", fin - inicio);
		return solicitudes;
	}
	
	private Boolean esSolicitudPendienteCertificacion(String EstadoSolicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.valorizarSolicitud");
		long inicio = System.currentTimeMillis();
		Boolean esSolicitudPendienteCertificacion = _c.EstadoIngresado.equals(EstadoSolicitud) || _c.EstadoPendiente.equals(EstadoSolicitud);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.valorizarSolicitud", fin - inicio);
		return esSolicitudPendienteCertificacion;
	}
	
	private Boolean esValorizacionCorrecta(CalcularMontoNumeroCuota.Resultado resultadoValorizacion) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esValorizacionCorrecta");
		long inicio = System.currentTimeMillis();
		Boolean esValorizacionCorrecta = resultadoValorizacion != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esValorizacionCorrecta", fin - inicio);
		return esValorizacionCorrecta;
	}
	
	private BigDecimal obtenerPorcentajeRetencion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.obtenerPorcentajeRetencion");
		long inicio = System.currentTimeMillis();
		BigDecimal porcentajeRetencion = BigDecimal.ZERO;
		try {
			porcentajeRetencion= new BigDecimal(ConfiguracionSistema.getParametroConfiguracionLong(con,idEmpresa, "PLATAFORMA","APV.PorcentajeRetencion"));
		}catch(Exception e) {
			logger.warn("ERROR::ConsultarSolicitudAporteRescateUI.obtenerPorcentajeRetencion:: Al encontrar parametro APV.PorcentajeRetencion");
		}
		porcentajeRetencion = porcentajeRetencion.divide(new BigDecimal("100"), MathContext.DECIMAL128);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.obtenerPorcentajeRetencion", fin - inicio);
		return porcentajeRetencion;
	}
	
	private List<Solicitud> calcularMontoRetencion(Connection con, BigDecimal porcentajeRetencion, List<Solicitud> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.calcularMontoRetencion");
		long inicio = System.currentTimeMillis();
		solicitudes.parallelStream()
		.filter(solicitud->{
			return esSolicitudConRetencion(solicitud);
		})
		.forEach(solicitud->{
			solicitud.MontoRetencion = obtenerMontoRetencion(con, porcentajeRetencion, solicitud);
		});
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.calcularMontoRetencion", fin - inicio);
		return solicitudes;
	}
	
	private Boolean esSolicitudConRetencion(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esSolicitudConRetencion");
		long inicio = System.currentTimeMillis();
		Boolean esSolicitudConRetencion = esRescateNormal(solicitud) && esGrupoRegimenAPVB(solicitud) && noEsTraspaso(solicitud) && esMedioPagoValidoRetencion(solicitud); 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esSolicitudConRetencion", fin - inicio);
		return esSolicitudConRetencion;
	}
	
	private Boolean esRescateNormal(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esRescateNormal");
		long inicio = System.currentTimeMillis();
		Boolean esRescateNormal = _c.RescateFondoMutuo.equals(solicitud.IdTipoMovimientoFondoMutuo); 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esRescateNormal", fin - inicio);
		return esRescateNormal;
	}
	
	private Boolean esGrupoRegimenAPVB(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esGrupoRegimenAPVB");
		long inicio = System.currentTimeMillis();
		Boolean esGrupoRegimenAPVB = (_c.APV_AN.equals(solicitud.IdRegimenTributarioAPV) || _c.APV_B.equals(solicitud.IdRegimenTributarioAPV)); 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esGrupoRegimenAPVB", fin - inicio);
		return esGrupoRegimenAPVB;
	}
	
	private Boolean noEsTraspaso(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.noEsTraspaso");
		long inicio = System.currentTimeMillis();
		Boolean noEsTraspaso = solicitud.IdTraspaso == null; 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.noEsTraspaso", fin - inicio);
		return noEsTraspaso;
	}
	private Boolean esMedioPagoValidoRetencion(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esMedioPagoValidoRetencion");
		long inicio = System.currentTimeMillis();
		Boolean esMedioPagoValidoRetencion = !_c.CuentaInversion.equals(solicitud.IdMedioDePago);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.esMedioPagoValidoRetencion", fin - inicio);
		return esMedioPagoValidoRetencion;
	}
	
	private BigDecimal obtenerMontoRetencion(Connection con, BigDecimal porcentajeRetencion, Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.obtenerMontoRetencion");
		long inicio = System.currentTimeMillis();
		BigDecimal montoRetencion = solicitud.Monto.multiply(porcentajeRetencion, MathContext.DECIMAL128);
		try {
			montoRetencion = MonedaFormato.formatear(con, montoRetencion, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		}catch(Exception e) {
			logger.warn("ERROR::ConsultarSolicitudAporteRescateUI.obtenerMontoRetencion:: Al formatear monto");
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarSolicitudAporteRescateUI.obtenerMontoRetencion", fin - inicio);
		return montoRetencion;
	}
	
	private Boolean esCambioMontoMonedaSistema(Solicitud solicitud) {
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esCambioMontoMonedaSistema");
		Boolean esCambioMontoMonedaSistema =  _c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) || (_c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud) && (solicitud.IdOperacionSpot == null || solicitud.IdOperacionSpot.equals(0L)));
		logger.trace("INICIO::ConsultarSolicitudAporteRescateUI.esCambioMontoMonedaSistema");
		return esCambioMontoMonedaSistema;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaAceptacionDesde\":\"2020-06-15\",\"p_FechaAceptacionHasta\":\"2020-06-15\",\"p_IdTipoMovimientoFondoMutuo\":\"\",\"p_Estado\":\"\",\"p_IdEmisor\":\"\",\"p_IdSerieFondo\":\"\",\"p_IdCliente\":0,\"p_IdProductoAgrupador\":0,\"p_IdPortafolio\":0,\"p_TipoSolicitud\":\"\",\"p_IdMoneda\":\"\"}";
	
			EntradaConsultarSolicitudAporteRescateUI Objin = gson.fromJson(json, EntradaConsultarSolicitudAporteRescateUI.class);
			ConsultarSolicitudAporteRescateUI  demo=new ConsultarSolicitudAporteRescateUI();				
			try {
				SalidaConsultarSolicitudAporteRescateUI ObjOut = demo.logica(con, Objin);
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
