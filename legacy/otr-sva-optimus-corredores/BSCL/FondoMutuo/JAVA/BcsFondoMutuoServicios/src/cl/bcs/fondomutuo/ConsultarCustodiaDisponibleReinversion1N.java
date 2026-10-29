package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
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
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.EntradaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarCustodiaDisponibleReinversion1N implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioFondoMutuo.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaConsultarCustodiaDisponibleReinversion1N extends BcsPojo{
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
		
		public EntradaConsultarCustodiaDisponibleReinversion1N() {
			this.p_IdCliente = 0L;
			this.p_IdPortafolio = 0L;
			this.p_IdInstrumento = 0L;
			this.p_FechaConsulta = "0001-01-01";
			this.p_IdFamiliaFondo = "";
			this.p_BuscaMLT = "N";
		}
	}
	
	public static class SalidaConsultarCustodiaDisponibleReinversion1N extends BcsPojo{
		public List<SalidaCustodia> P_OUT_TBL;
		
		public SalidaConsultarCustodiaDisponibleReinversion1N() {
			this.P_OUT_TBL = new ArrayList<SalidaCustodia>();
		}
	}
	
	public static class SalidaCustodia extends BcsPojo{
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
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarCustodiaDisponibleReinversion1N jin = gson.fromJson(in.toString(), EntradaConsultarCustodiaDisponibleReinversion1N.class);
		SalidaConsultarCustodiaDisponibleReinversion1N out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCustodiaDisponibleReinversion1N logica(Connection con, EntradaConsultarCustodiaDisponibleReinversion1N entrada) throws Exception {
		logger.info("INICIO::ConsultarCustodiaDisponibleReinversion1N");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarCustodiaDisponibleReinversion1N out = new SalidaConsultarCustodiaDisponibleReinversion1N();
		
		List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias = obtenerCustodias(con, entrada);
		List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> flujosInstrumentos = obtenerFlujosInstrumentos(con, entrada, custodias);
		List<SalidaCustodia> salidaCustodia = obtenerSalidaInicial(custodias);
		salidaCustodia = actualizarCantidadDisponiblePorFlujo(salidaCustodia, flujosInstrumentos);
		salidaCustodia = valorizarSalidaCustodia(con, entrada, salidaCustodia);
		out.P_OUT_TBL = salidaCustodia;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarCustodiaDisponibleReinversion1N", finServicio - inicioServicio);
		return out;
	}
	
	private List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> obtenerCustodias(Connection con, EntradaConsultarCustodiaDisponibleReinversion1N entrada) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.obtenerCustodias");
		long inicio = System.currentTimeMillis();
		ConsultarCustodiaPortafolioFondoMutuo consultarCustodiaPortafolioFondoMutuo = new ConsultarCustodiaPortafolioFondoMutuo();
		EntradaConsultarCustodiaPortafolioFondoMutuo entradaConsultarCustodiaPortafolioFondoMutuo = new EntradaConsultarCustodiaPortafolioFondoMutuo();
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdCliente = entrada.p_IdCliente;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdInstrumento = entrada.p_IdInstrumento;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_FechaConsulta = entrada.p_FechaConsulta;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdProductoAgrupador = entrada.p_IdProductoAgrupador;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdEmisor = entrada.p_IdEmisor;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdFamiliaFondo = entrada.p_IdFamiliaFondo;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_BuscaMLT = entrada.p_BuscaMLT;
		
		SalidaConsultarCustodiaPortafolioFondoMutuo salidaConsultarCustodiaPortafolioFondoMutuo = consultarCustodiaPortafolioFondoMutuo.logica(con, entradaConsultarCustodiaPortafolioFondoMutuo);
		List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodia = salidaConsultarCustodiaPortafolioFondoMutuo.P_OUT_TBL;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.obtenerCustodias", fin - inicio);
		return custodia;
	}
	
	private List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> obtenerFlujosInstrumentos(Connection con, EntradaConsultarCustodiaDisponibleReinversion1N entrada, List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.obtenerFlujosInstrumentos");
		long inicio = System.currentTimeMillis();
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, entrada);
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = entrada.p_FechaConsulta;
		entradaFlujo.p_DiasProyeccion = 0;
		
		ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
		cartera.IdCliente = entrada.p_IdCliente;
		cartera.IdPortafolio = entrada.p_IdPortafolio;
		entradaFlujo.p_Carteras.add(cartera);
		
		entradaFlujo.p_Instrumentos = custodias.parallelStream().map(custodia->{
			ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
			instrumento.IdInstrumento = custodia.IdInstrumento;
			instrumento.IdRegimenFondoMutuo = custodia.IdRegimenFondoMutuo;
			return instrumento;
		}).collect(Collectors.toList());
		
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = flujo.logica(con, entradaFlujo);
		List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> flujosInstrumentos = salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.obtenerFlujosInstrumentos", fin - inicio);
		
		return flujosInstrumentos;
	}
	
	private Long obtenerIdPaisEmpresa(Connection con, EntradaConsultarCustodiaDisponibleReinversion1N entrada) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.obtenerIdPaisEmpresa");
		long inicio = System.currentTimeMillis();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.obtenerIdPaisEmpresa", fin - inicio);
		return idPaisEmpresa;
	}
	
	private List<SalidaCustodia> obtenerSalidaInicial(List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias){
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.obtenerSalidaInicial");
		long inicio = System.currentTimeMillis();
		AtomicLong idInterno = new AtomicLong(1L);
		List<SalidaCustodia> salidaCustodia = custodias.parallelStream().map(custodia->{
			SalidaCustodia custodiaSalida = new SalidaCustodia();
			custodiaSalida.IdInterno = idInterno.getAndIncrement();
			custodiaSalida.IdClasificacionTipoInstrumento =  custodia.IdClasificacionTipoInstrumento;
			custodiaSalida.NombreClasificacionTipoInstrumento =  custodia.NombreClasificacionTipoInstrumento;
			custodiaSalida.IdTipoInstrumento =  custodia.IdTipoInstrumento;
			custodiaSalida.NombreTipoInstrumento =  custodia.NombreTipoInstrumento;
			custodiaSalida.IdInstrumento =  custodia.IdInstrumento;
			custodiaSalida.Nemotecnico =  custodia.Nemotecnico;
			custodiaSalida.IdSocioDeNegocio =  custodia.IdSocioDeNegocio;
			custodiaSalida.IdCliente =  custodia.IdCliente;
			custodiaSalida.IdPortafolio =  custodia.IdPortafolio;
			custodiaSalida.DocumentoIdentidad =  custodia.DocumentoIdentidad;
			custodiaSalida.RazonSocial =  custodia.RazonSocial;
			custodiaSalida.CodigoPortafolio =  custodia.CodigoPortafolio;
			custodiaSalida.IdBoveda =  custodia.IdBoveda;
			custodiaSalida.Relacion =  custodia.Relacion;
			custodiaSalida.CantidadCustodia =  custodia.CantidadCustodia;
			custodiaSalida.Monto =  custodia.Monto;
			custodiaSalida.IdMoneda =  custodia.IdMoneda;
			custodiaSalida.ValorCuota =  custodia.ValorCuota;
			custodiaSalida.DecimalesValorCuota =  custodia.DecimalesValorCuota;
			custodiaSalida.CantidadGarantia =  custodia.CantidadGarantia;
			custodiaSalida.CantidadTransSolicitudesAporte =  custodia.CantidadTransSolicitudesAporte;
			custodiaSalida.CantidadTransSolicitudesRescate =  custodia.CantidadTransSolicitudesRescate;
			custodiaSalida.MontoTransSolicitudesAporte =  custodia.MontoTransSolicitudesAporte;
			custodiaSalida.MontoTransSolicitudesRescate =  custodia.MontoTransSolicitudesRescate;
			custodiaSalida.INGPrecioPromedioCompra =  custodia.INGPrecioPromedioCompra;
			custodiaSalida.INGMontoPrecioPromedioCompra =  custodia.INGMontoPrecioPromedioCompra;
			custodiaSalida.INGMontoPrecioPromedioMonedaInstrumento =  custodia.INGMontoPrecioPromedioMonedaInstrumento;
			custodiaSalida.IdMonedaPrincipal =  custodia.IdMonedaPrincipal;
			custodiaSalida.IdMonedaFondo =  custodia.IdMonedaFondo;
			custodiaSalida.Producto =  custodia.Producto;
			custodiaSalida.IdEmisor =  custodia.IdEmisor;
			custodiaSalida.MLT =  custodia.MLT;
			custodiaSalida.IdSerieFondo =  custodia.IdSerieFondo;
			custodiaSalida.IdEmpresa =  custodia.IdEmpresa;
			custodiaSalida.IdFamiliaFondo =  custodia.IdFamiliaFondo;
			custodiaSalida.FondoExtranjero =  custodia.FondoExtranjero;
			custodiaSalida.MontoMonedaSistema =  custodia.MontoMonedaSistema;
			custodiaSalida.FechaValorCuota =  custodia.FechaValorCuota;
			custodiaSalida.IdRegimenFondoMutuo =  custodia.IdRegimenFondoMutuo;
			custodiaSalida.IdMatrizAgrupador =  custodia.IdMatrizAgrupador;
			custodiaSalida.EsADC =  custodia.EsADC;
			custodiaSalida.Fondo =  custodia.Fondo;
			custodiaSalida.NombreFondo =  custodia.NombreFondo;
			custodiaSalida.ValorCuotaMonedaSistema =  custodia.ValorCuotaMonedaSistema;
			custodiaSalida.RegimenTributario =  custodia.RegimenTributario;
			return custodiaSalida;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.obtenerSalidaInicial", fin - inicio);
		return salidaCustodia;
	}
	
	private List<SalidaCustodia> actualizarCantidadDisponiblePorFlujo(List<SalidaCustodia> salidaCustodia, List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> flujosCustodias){
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.actualizarCantidadMontoDisponible");
		long inicio = System.currentTimeMillis();
		salidaCustodia.parallelStream().forEach(custodia->{
			ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia = flujosCustodias.parallelStream().filter(flujo->esFlujoDeCustodiaConsultada(custodia, flujo)).findFirst().orElse(null);
			if(esFlujoCorrecto(flujoCustodia)) {
				custodia.CantidadCustodia = flujoCustodia.CantidadDisponible;
			}
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.actualizarCantidadMontoDisponible", fin - inicio);
		return salidaCustodia;
	}
	
	private Boolean esFlujoDeCustodiaConsultada(SalidaCustodia custodia, ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujo) {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.esFlujoDeCustodiaConsultada");
		long inicio = System.currentTimeMillis();
		Boolean esFlujoDeCustodiaConsultada = custodia.IdPortafolio.equals(flujo.IdPortafolio) && custodia.IdInstrumento.equals(flujo.IdInstrumento) && custodia.IdRegimenFondoMutuo.equals(flujo.IdRegimenTributario);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.esFlujoDeCustodiaConsultada", fin - inicio);
		return esFlujoDeCustodiaConsultada;
	}
	
	private Boolean esFlujoCorrecto(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujo) {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.esFlujoCorrecto");
		long inicio = System.currentTimeMillis();
		Boolean esFlujoCorrecto = flujo != null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.esFlujoCorrecto", fin - inicio);
		return esFlujoCorrecto;
	}
	
	private List<SalidaCustodia> valorizarSalidaCustodia(Connection con, EntradaConsultarCustodiaDisponibleReinversion1N entrada, List<SalidaCustodia> salidaCustodia) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.valorizarSalidaCustodia");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalculo.p_Series = salidaCustodia.parallelStream().map(custodia->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = custodia.IdInterno;
			calculoMontoCuota.Fecha = entrada.p_FechaConsulta;
			calculoMontoCuota.IdSerieFondo = custodia.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = _c.CalculoPorCuota;
			calculoMontoCuota.Monto = BigDecimal.ZERO;
		 	calculoMontoCuota.NumeroCuotas = custodia.CantidadCustodia;
		 	calculoMontoCuota.IdMonedaFondo = custodia.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = custodia.IdMonedaFondo;
		 	calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = entrada.p_IdEmpresa;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		salidaCustodia.parallelStream().forEach(custodia->{
			CalcularMontoNumeroCuota.Resultado valorizacion = montosValorizados.get(custodia.IdInterno);
			if(esValorizacionCorrecta(valorizacion)) {
				custodia.Monto = valorizacion.Monto;
				custodia.MontoMonedaSistema = valorizacion.MontoMonedaSistema;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.valorizarSalidaCustodia", fin - inicio);
		return salidaCustodia;
	}
	
	private Boolean esValorizacionCorrecta(CalcularMontoNumeroCuota.Resultado valorizacion) {
		logger.debug("INICIO::ConsultarCustodiaDisponibleReinversion1N.esValorizacionCorrecta");
		long inicio = System.currentTimeMillis();
		Boolean esValorizacionCorrecta = valorizacion != null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaDisponibleReinversion1N.esValorizacionCorrecta", fin - inicio);
		return esValorizacionCorrecta;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdCliente\":524891,\"p_IdPortafolio\":1451809,\"p_IdProductoAgrupador\":null,\"p_IdInstrumento\":0,\"p_FechaConsulta\":\"2020-06-12\",\"p_BuscaMLT\":\"N\"}";
			EntradaConsultarCustodiaDisponibleReinversion1N Objin = gson.fromJson(json, EntradaConsultarCustodiaDisponibleReinversion1N.class);
			ConsultarCustodiaDisponibleReinversion1N  demo=new ConsultarCustodiaDisponibleReinversion1N();				
			try {
				SalidaConsultarCustodiaDisponibleReinversion1N ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_TBL.toString());
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
