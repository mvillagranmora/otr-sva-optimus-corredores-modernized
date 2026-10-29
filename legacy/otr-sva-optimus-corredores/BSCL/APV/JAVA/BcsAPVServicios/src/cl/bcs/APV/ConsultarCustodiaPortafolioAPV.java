package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.EntradaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.Solicitud;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo;

public class ConsultarCustodiaPortafolioAPV implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioAPV.class);
	
	private final String SQL_SELECT_INFORMACION_PORTAFOLIO = loadResourceToString("cl/bcs/APV/sql/ConsultarCustodiaPortafolioAPV_SelectInformacionPortafolio.sql");
	private final String SQL_SELECT_INFORMACION_REGIMEN_APV_A = loadResourceToString("cl/bcs/APV/sql/ConsultarCustodiaPortafolioAPV_SelectRegimenTributarioAPV_A.sql");
	private final String SQL_SELECT_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/ConsultarCustodiaPortafolioAPV_SelectDetalleOrigenTraspaso.sql");
	
	public static class EntradaConsultarCustodiaPortafolioAPV{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Long p_IdInstrumento;
		public String p_IdBoveda;
		public String p_AgrupaPorBoveda;
		public String p_AgrupaPorRelacion;
		public String p_FechaConsulta;
		public String p_TipoFechaConsulta;
		public Long p_IdRegimen;
		public Integer p_IdProducto;
		public Integer p_IdPlan;
		public Long p_IdTraspaso;
		public String p_TipoAhorro;
		
		public EntradaConsultarCustodiaPortafolioAPV() {
			this.p_IdCliente = 0L;
			this.p_IdPortafolio = 0L;
			this.p_IdInstrumento = 0L;
			this.p_IdBoveda = "";
			this.p_AgrupaPorBoveda = "N";
			this.p_AgrupaPorRelacion = "N";
			this.p_FechaConsulta = "0001-01-01";
			this.p_TipoFechaConsulta = "LIQ";
			this.p_IdRegimen = 0L;
			this.p_IdProducto = 0;
			this.p_IdPlan = 0;
			this.p_IdTraspaso = 0L;
			this.p_TipoAhorro = "0";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCustodiaPortafolioAPV{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public BigDecimal P_OUT_TOTALCUSTODIA;
		public BigDecimal P_OUT_TOTALRV;
		public BigDecimal P_OUT_TOTALFFMM;
		public List<Custodia> P_OUT_CUSTODIA;
		
		public SalidaConsultarCustodiaPortafolioAPV() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TOTALCUSTODIA = BigDecimal.ZERO;
			this.P_OUT_TOTALRV = BigDecimal.ZERO;
			this.P_OUT_TOTALFFMM = BigDecimal.ZERO;
			this.P_OUT_CUSTODIA = new ArrayList<Custodia>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Custodia{
		public String NombreProductoAgrupador;
		public Integer IdProductoAgrupador;
		public String Regimen;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public BigDecimal CantidadCustodia;
		public BigDecimal PPPCustodia;
		public BigDecimal ValorPPPCustodia;
		public BigDecimal MontoPPPCustodia;
		public BigDecimal CantidadADesinvertir;
		public BigDecimal MontoADesinvertir;
		public Long IdPortafolio;
		public String IdEmisor;
		public Long IdInstrumento;
		public String IdMonedaInstrReajuste;
		public String DocumentoIdentidad;
		public String DocumentoIdentidadRutCavali;
		public String NombreCliente;
		public String Relacion;
		public String ISIN;
		public String ClasificacionRiesgoInstrumento;
		public String EsFungible;
		public String EsPagaderoEn;
		public String IdBoveda;
		public String NumeroCuentaBoveda;
		public BigDecimal TotalPosicion;
		public BigDecimal TotalPosicionPresunta;
		public BigDecimal Rentabilidad;
		public BigDecimal PrecioMercadoCustodia;
		public BigDecimal ValorMercadoCustodia;
		public BigDecimal MontoMercadoCustodia;
		public BigDecimal DiferenciaMercadoCustodia;
		public BigDecimal CantidadGarantia;
		public BigDecimal PrecioMercadoGarantia;
		public BigDecimal ValorMercadoGarantia;
		public BigDecimal DiferenciaMercadoGarantia;
		public BigDecimal CantidadSimulCom;
		public BigDecimal PrecioMercadoSimulCom;
		public BigDecimal ValorMercadoSimulCom;
		public BigDecimal DiferenciaMercadoSimulCom;
		public BigDecimal CantidadSimulVen;
		public BigDecimal PrecioMercadoSimulVen;
		public BigDecimal ValorMercadoSimulVen;
		public BigDecimal DiferenciaMercadoSimulVen;
		public BigDecimal CantidadPrestamo;
		public BigDecimal PrecioMercadoPrestamo;
		public BigDecimal ValorMercadoPrestamo;
		public BigDecimal DiferenciaMercadoPrestamo;
		public BigDecimal CantidadEmpresti;
		public BigDecimal PrecioMercadoEmpresti;
		public BigDecimal ValorMercadoEmpresti;
		public BigDecimal DiferenciaMercadoEmpresti;
		public BigDecimal CantidadPactosV;
		public BigDecimal PrecioMercadoPactosV;
		public BigDecimal ValorMercadoPactosV;
		public BigDecimal DiferenciaMercadoPactosV;
		public BigDecimal CantidadPactosC;
		public BigDecimal PrecioMercadoPactosC;
		public BigDecimal ValorMercadoPactosC;
		public BigDecimal DiferenciaMercadoPactosC;
		public BigDecimal CantidadReporteCompra;
		public BigDecimal PrecioMercadoReporteCompra;
		public BigDecimal ValorMercadoReporteCompra;
		public BigDecimal DiferenciaMercadoReporteCompra;
		public BigDecimal CantidadReporteVenta;
		public BigDecimal PrecioMercadoReporteVenta;
		public BigDecimal ValorMercadoReporteVenta;
		public BigDecimal DiferenciaMercadoReporteVenta;
		public BigDecimal CantidadTransIng;
		public BigDecimal CantidadTransEgr;
		public BigDecimal SaldoDisponibleCLP;
		public BigDecimal SaldoDisponibleUSD;
		public BigDecimal SaldoDisponibleEUR;
		public Long IdRegimenTributarioMatriz;
		public Integer IdPlanTipoInversion;
		public String CodigoPortafolio;
		public String CodigoRegimenTipoAhorro;
		public String CargaExtraParaAPVA;
		
		public Custodia() {
			this.NombreProductoAgrupador = "";
			this.IdProductoAgrupador = 0;
			this.Regimen = "";
			this.IdRegimenTributarioMatriz = 0L;
			this.IdPlanTipoInversion = 0;
			this.CodigoPortafolio ="";
			this.CodigoRegimenTipoAhorro = "";
			this.CargaExtraParaAPVA = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionPortafolio{
		public String NombreProductoAgrupador;
		public Integer IdProductoAgrupador;
		public String Regimen;
		public Long IdPortafolio;
		public Long IdRegimenTributario;
		public Integer IdPlanTipoInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOrigenTraspaso{
		public Long IdInstrumento;
		public Long IdTraspaso;
		public Long IdPortafolio;
		public String CodigoRegimenTributario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegimenTributario extends BcsPojo {
		public Long IdRegimenTributario;
		public String Codigo;
		
		public RegimenTributario() {
			this.IdRegimenTributario = 0L;
			this.Codigo = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarCustodiaPortafolioAPV jin = gson.fromJson(in.toString(), EntradaConsultarCustodiaPortafolioAPV.class);
		SalidaConsultarCustodiaPortafolioAPV out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCustodiaPortafolioAPV logica(Connection con, EntradaConsultarCustodiaPortafolioAPV entrada) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV");
		SalidaConsultarCustodiaPortafolioAPV out = new SalidaConsultarCustodiaPortafolioAPV();
		
		String fechaConsulta = obtenerFechaConsulta(con,entrada);
		List<Tabla_Salida> custodias = obtenerCustodia(con,entrada,fechaConsulta,out);
		List<SaldoPorFechaEfectiva> saldosCuentaInversion = obtenerSaldoFechaEfectiva(con,custodias,entrada,fechaConsulta);
		Map<Long,InformacionPortafolio> informacionPortafolios = obtenerInformacionPortafolio(con,custodias);
		
		RegimenTributario regimen = obtenerInformacionRegimenAPV_A(con);
		
		out.P_OUT_CUSTODIA = obtenerCustodiaSalida(entrada,custodias,saldosCuentaInversion,informacionPortafolios, regimen);
		if(!entrada.p_IdTraspaso.equals(0L)) {
			List<DetalleOrigenTraspaso> detalleTraspasos = obtenerInformacionTraspaso(con,custodias,entrada.p_IdTraspaso);
			out.P_OUT_CUSTODIA = obtenerCustodiaTraspasoSalida(entrada,detalleTraspasos,out.P_OUT_CUSTODIA);
		}
		
		if(!("0").equals(entrada.p_TipoAhorro) && entrada.p_IdRegimen.compareTo(0L) == 0) {
			out.P_OUT_CUSTODIA = out.P_OUT_CUSTODIA.stream().filter(custodia -> entrada.p_TipoAhorro.equals(custodia.CodigoRegimenTipoAhorro) || (("APV").equals(entrada.p_TipoAhorro) && ("S").equals(custodia.CargaExtraParaAPVA)) ).collect(Collectors.toList());
		}
		
		out.P_OUT_TOTALCUSTODIA = obtenerTotal(out.P_OUT_CUSTODIA, "");
		out.P_OUT_TOTALRV = obtenerTotal(out.P_OUT_CUSTODIA, "ACN");
		out.P_OUT_TOTALFFMM = obtenerTotal(out.P_OUT_CUSTODIA, "FFM");
		
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV");
		return out;
	}
	
	private RegimenTributario obtenerInformacionRegimenAPV_A(Connection con) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerInformacionRegimenAPV_A");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		RegimenTributario regimen = sql.query(con, SQL_SELECT_INFORMACION_REGIMEN_APV_A, params, RegimenTributario.class).stream().findFirst().orElse(new RegimenTributario());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerInformacionRegimenAPV_A", fin - inicio);
		return regimen;
	}

	private String obtenerFechaConsulta(Connection con, EntradaConsultarCustodiaPortafolioAPV entrada) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerFechaConsulta");
		String fechaSistema = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		String fechaConsulta = "0001-01-01".equals(entrada.p_FechaConsulta) ? fechaSistema : entrada.p_FechaConsulta;
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerFechaConsulta");
		return fechaConsulta;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private List<Tabla_Salida> obtenerCustodia(Connection con, EntradaConsultarCustodiaPortafolioAPV entrada,String fechaConsulta,SalidaConsultarCustodiaPortafolioAPV salida) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerCustodia");
		
		ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
		
		Entrada_ConsultaCustodiaPortafolio entradaConsultarCustodiaPortafolio = new Entrada_ConsultaCustodiaPortafolio();
		entradaConsultarCustodiaPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolio.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaPortafolio.p_IdCliente = entrada.p_IdCliente;
		entradaConsultarCustodiaPortafolio.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaConsultarCustodiaPortafolio.p_IdClasificacionTipoInstrumento = entrada.p_IdClasificacionTipoInstrumento;
		entradaConsultarCustodiaPortafolio.p_IdTipoInstrumento = entrada.p_IdTipoInstrumento;
		entradaConsultarCustodiaPortafolio.p_IdInstrumento = entrada.p_IdInstrumento;
		entradaConsultarCustodiaPortafolio.p_IdBoveda = entrada.p_IdBoveda;
		entradaConsultarCustodiaPortafolio.p_AgrupaPorBoveda = entrada.p_AgrupaPorBoveda;
		entradaConsultarCustodiaPortafolio.p_AgrupaPorRelacion = entrada.p_AgrupaPorRelacion;
		entradaConsultarCustodiaPortafolio.p_FechaConsulta = fechaConsulta;
		entradaConsultarCustodiaPortafolio.p_TipoFechaConsulta = "LIQ";
		
		Salida_ConsultaCustodiaPortafolio salidaConsultaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaConsultarCustodiaPortafolio);
		
		EntradaConsultarCustodiaPortafolioFondoMutuo entradaConsultarCustodiaPortafolioFFMM = new EntradaConsultarCustodiaPortafolioFondoMutuo();
		entradaConsultarCustodiaPortafolioFFMM.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioFFMM.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioFFMM.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaPortafolioFFMM.p_IdCliente = entrada.p_IdCliente;
		entradaConsultarCustodiaPortafolioFFMM.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaConsultarCustodiaPortafolioFFMM.p_IdInstrumento = entrada.p_IdInstrumento;
		entradaConsultarCustodiaPortafolioFFMM.p_FechaConsulta = fechaConsulta;
		ConsultarCustodiaPortafolioFondoMutuo consultarCustodiaPortafolioFFMM = new ConsultarCustodiaPortafolioFondoMutuo();
		SalidaConsultarCustodiaPortafolioFondoMutuo salidaConsultarCustodiaPortafolioFFMM = consultarCustodiaPortafolioFFMM.logica(con, entradaConsultarCustodiaPortafolioFFMM);
		
		salidaConsultarCustodiaPortafolioFFMM.P_OUT_TBL.parallelStream().forEach(validaCustodia->{
			if (validaCustodia.CantidadCustodia.equals(BigDecimal.ZERO) ) {
			    MensajeSalida  msg = new MensajeSalida();
				HashMap<String,String> valores = new HashMap<>();
				valores.put("Nemotecnico", validaCustodia.Nemotecnico);
				try {
					msg = new MensajeSalida( validaCustodia.IdInstrumento, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CLIENTE_SIN_CUSTODIA", valores, con ,validaCustodia.IdEmpresa);			
				} catch (Exception e) {
					e.printStackTrace();
				}		
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR++;
				return;
			};
		});
		
		Map<Object, List<SalidaCustodia>> solicitudesAgrupadasPorIdIntrumentoPortafolioRegimen = salidaConsultarCustodiaPortafolioFFMM.P_OUT_TBL.stream().filter(solicitud->solicitud.CantidadTransSolicitudesRescate.compareTo(BigDecimal.ZERO) != 0 ).collect(Collectors.groupingBy(solicitud->solicitud.IdInstrumento + "_" + solicitud.IdPortafolio + "_" + solicitud.IdRegimenFondoMutuo));
		for(Tabla_Salida custodia : salidaConsultaCustodiaPortafolio.P_OUT_TBL) {
			List<SalidaCustodia> solicitudesInstrumentoPortafolioRegimen = solicitudesAgrupadasPorIdIntrumentoPortafolioRegimen.get(custodia.IdInstrumento + "_" + custodia.IdPortafolio + "_" + custodia.IdRegimenTributario);
			if(solicitudesInstrumentoPortafolioRegimen != null){
				for(SalidaCustodia solicitud : solicitudesInstrumentoPortafolioRegimen) {
					custodia.CantidadCustodia = custodia.CantidadCustodia.subtract(solicitud.CantidadTransSolicitudesRescate);
				}
			}
		}
				
		salidaConsultaCustodiaPortafolio.P_OUT_TBL = salidaConsultaCustodiaPortafolio.P_OUT_TBL.parallelStream()
				.filter(custodia->!"Otros (No APV)".equals(custodia.Producto) && ("RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento)) && custodia.CantidadCustodia.compareTo(BigDecimal.ZERO)==1)
				.collect(Collectors.toList());
		
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerCustodia");
		
		return salidaConsultaCustodiaPortafolio.P_OUT_TBL;
	}
	
	private List<SaldoPorFechaEfectiva> obtenerSaldoFechaEfectiva(Connection con, List<Tabla_Salida> custodias, EntradaConsultarCustodiaPortafolioAPV entrada, String fechaSistema) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerSaldoFechaEfectiva");
		ConsultarSaldoCuentaInversion consultarSaldoCuentaInversion = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
		entradaConsultarSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarSaldoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarSaldoCuentaInversion.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarSaldoCuentaInversion.p_UsaReglasNegocio = "S";
		entradaConsultarSaldoCuentaInversion.p_FechaDesde = fechaSistema;
		entradaConsultarSaldoCuentaInversion.p_FechaHasta = fechaSistema;
		entradaConsultarSaldoCuentaInversion.p_SoloSaldoNegativo = "N";
		entradaConsultarSaldoCuentaInversion.p_SoloSaldoDisponible = "N";
		entradaConsultarSaldoCuentaInversion.p_IncluyeCargosPendientes = "N";
		entradaConsultarSaldoCuentaInversion.p_IncluyeAbonosPendientes = "N";
		entradaConsultarSaldoCuentaInversion.p_CuentaInversionAConsultar = custodias.parallelStream().map(custodia->custodia.IdPortafolio).distinct().map(portafolio->{
			InCuentaInversionConsultar inCuentaInversionConsultar = new InCuentaInversionConsultar();
			inCuentaInversionConsultar.IdPortafolio = portafolio;
			return inCuentaInversionConsultar;
		}).collect(Collectors.toList());
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuentaInversion = consultarSaldoCuentaInversion.logica(con, entradaConsultarSaldoCuentaInversion);
		
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerSaldoFechaEfectiva");
		
		return salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA;
		
	}
	
	private BigDecimal obtenerSaldoMoneda(List<SaldoPorFechaEfectiva> saldosCuentaInversion, String idMoneda, Long idPortafolio) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerSaldoMoneda");
		Optional<BigDecimal> saldo = saldosCuentaInversion.parallelStream().filter(saldoFechaEfectiva->idMoneda.equals(saldoFechaEfectiva.IdMoneda) && idPortafolio.equals(saldoFechaEfectiva.IdPortafolio)).map(saldoFechaEfectiva->saldoFechaEfectiva.Saldo).findFirst();
		BigDecimal saldoMoneda =  saldo.isPresent() ? saldo.get() : BigDecimal.ZERO;
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerSaldoMoneda");
		return saldoMoneda;
	}
	
	private Map<Long,InformacionPortafolio> obtenerInformacionPortafolio(Connection con, List<Tabla_Salida> custodias) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerInformacionPortafolio");
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> portafolios = custodias.parallelStream().map(custodia->custodia.IdPortafolio).distinct().collect(Collectors.toList());
		params.put("IdPortafolio", portafolios);
		SQL sql=new SQL();
		Map<Long,InformacionPortafolio> informacionPortafolios = sql.query(con, SQL_SELECT_INFORMACION_PORTAFOLIO, params, InformacionPortafolio.class).parallelStream().collect(Collectors.toMap(portafolio->portafolio.IdPortafolio, portafolio->portafolio));
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerInformacionPortafolio");
		return informacionPortafolios;
	}
	
	private List<DetalleOrigenTraspaso> obtenerInformacionTraspaso(Connection con, List<Tabla_Salida> custodias, Long idTraspaso) throws Exception{
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerInformacionTraspaso");
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> instrumentos = custodias.parallelStream().map(custodia->custodia.IdInstrumento).distinct().collect(Collectors.toList());
		params.put("IdInstrumento", instrumentos);
		params.put("IdTraspaso", idTraspaso);
		SQL sql=new SQL();
		List<DetalleOrigenTraspaso> detalleTraspasos = sql.query(con, SQL_SELECT_DETALLE_ORIGEN_TRASPASO, params, DetalleOrigenTraspaso.class).parallelStream().collect(Collectors.toList());
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerInformacionTraspaso");
		return detalleTraspasos;
	}
	
	private List<Custodia> obtenerCustodiaSalida(EntradaConsultarCustodiaPortafolioAPV entrada,List<Tabla_Salida> custodiasPortafolio, List<SaldoPorFechaEfectiva> saldosCuentaInversion,Map<Long,InformacionPortafolio> informacionPortafolios, RegimenTributario regimenAPV){
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerCustodiaSalida");
		List<Custodia> custodias = custodiasPortafolio.parallelStream().map(custodiaPortafolio->{
			BigDecimal saldoCLP = obtenerSaldoMoneda(saldosCuentaInversion,"CLP",custodiaPortafolio.IdPortafolio);
			BigDecimal saldoUSD = obtenerSaldoMoneda(saldosCuentaInversion,"USD",custodiaPortafolio.IdPortafolio);
			BigDecimal saldoEUR = obtenerSaldoMoneda(saldosCuentaInversion,"EUR",custodiaPortafolio.IdPortafolio);
			InformacionPortafolio informacionPortafolio = informacionPortafolios.get(custodiaPortafolio.IdPortafolio);
			Custodia custodia = new Custodia();
			if(informacionPortafolio != null) {
				custodia.NombreProductoAgrupador = informacionPortafolio.NombreProductoAgrupador;
				custodia.IdProductoAgrupador = informacionPortafolio.IdProductoAgrupador;
				custodia.Regimen = custodiaPortafolio.IdRegimenTributario;
				custodia.IdRegimenTributarioMatriz = informacionPortafolio.IdRegimenTributario;
				custodia.IdPlanTipoInversion = informacionPortafolio.IdPlanTipoInversion;
				custodia.CodigoPortafolio = custodiaPortafolio.CodigoPortafolio;
			}
			custodia.IdClasificacionTipoInstrumento = custodiaPortafolio.IdClasificacionTipoInstrumento;
			custodia.IdTipoInstrumento = custodiaPortafolio.IdTipoInstrumento;
			custodia.Nemotecnico = custodiaPortafolio.Nemotecnico;
			custodia.CantidadCustodia = custodiaPortafolio.CantidadCustodia;
			custodia.PPPCustodia = custodiaPortafolio.PPPCustodia;
			custodia.ValorPPPCustodia = custodiaPortafolio.ValorPPPCustodia;
			custodia.MontoPPPCustodia = custodiaPortafolio.MontoPPPCustodia;
			custodia.CantidadADesinvertir = BigDecimal.ZERO;
			custodia.MontoADesinvertir = BigDecimal.ZERO;
			custodia.IdPortafolio = custodiaPortafolio.IdPortafolio;
			custodia.IdEmisor = custodiaPortafolio.IdEmisor;
			custodia.IdInstrumento = custodiaPortafolio.IdInstrumento;
			custodia.IdMonedaInstrReajuste = custodiaPortafolio.IdMonedaInstrReajuste;
			custodia.DocumentoIdentidad = custodiaPortafolio.DocumentoIdentidad;
			custodia.DocumentoIdentidadRutCavali = custodiaPortafolio.DocumentoIdentidadRutCavali;
			custodia.NombreCliente = custodiaPortafolio.NombreCliente;
			custodia.Relacion = custodiaPortafolio.Relacion;
			custodia.ISIN = custodiaPortafolio.ISIN;
			custodia.ClasificacionRiesgoInstrumento = custodiaPortafolio.ClasificacionRiesgoInstrumento;
			custodia.EsFungible = custodiaPortafolio.EsFungible;
			custodia.EsPagaderoEn = custodiaPortafolio.EsPagaderoEn;
			custodia.IdBoveda = custodiaPortafolio.IdBoveda;
			custodia.NumeroCuentaBoveda = custodiaPortafolio.NumeroCuentaBoveda;
			custodia.TotalPosicion = custodiaPortafolio.TotalPosicion;
			custodia.TotalPosicionPresunta = custodiaPortafolio.TotalPosicionPresunta;
			custodia.Rentabilidad = custodiaPortafolio.Rentabilidad;
			custodia.PrecioMercadoCustodia = custodiaPortafolio.PrecioMercadoCustodia;
			custodia.ValorMercadoCustodia = custodiaPortafolio.ValorMercadoCustodia;
			custodia.MontoMercadoCustodia = custodiaPortafolio.MontoMercadoCustodia;
			custodia.DiferenciaMercadoCustodia = custodiaPortafolio.DiferenciaMercadoCustodia;
			custodia.CantidadGarantia = custodiaPortafolio.CantidadGarantia;
			custodia.PrecioMercadoGarantia = custodiaPortafolio.PrecioMercadoGarantia;
			custodia.ValorMercadoGarantia = custodiaPortafolio.ValorMercadoGarantia;
			custodia.DiferenciaMercadoGarantia = custodiaPortafolio.DiferenciaMercadoGarantia;
			custodia.CantidadSimulCom = custodiaPortafolio.CantidadSimulCom;
			custodia.PrecioMercadoSimulCom = custodiaPortafolio.PrecioMercadoSimulCom;
			custodia.ValorMercadoSimulCom = custodiaPortafolio.ValorMercadoSimulCom;
			custodia.DiferenciaMercadoSimulCom = custodiaPortafolio.DiferenciaMercadoSimulCom;
			custodia.CantidadSimulVen = custodiaPortafolio.CantidadSimulVen;
			custodia.PrecioMercadoSimulVen = custodiaPortafolio.PrecioMercadoSimulVen;
			custodia.ValorMercadoSimulVen = custodiaPortafolio.ValorMercadoSimulVen;
			custodia.DiferenciaMercadoSimulVen = custodiaPortafolio.DiferenciaMercadoSimulVen;
			custodia.CantidadPrestamo = custodiaPortafolio.CantidadPrestamo;
			custodia.PrecioMercadoPrestamo = custodiaPortafolio.PrecioMercadoPrestamo;
			custodia.ValorMercadoPrestamo = custodiaPortafolio.ValorMercadoPrestamo;
			custodia.DiferenciaMercadoPrestamo = custodiaPortafolio.DiferenciaMercadoPrestamo;
			custodia.CantidadEmpresti = custodiaPortafolio.CantidadEmpresti;
			custodia.PrecioMercadoEmpresti = custodiaPortafolio.PrecioMercadoEmpresti;
			custodia.ValorMercadoEmpresti = custodiaPortafolio.ValorMercadoEmpresti;
			custodia.DiferenciaMercadoEmpresti = custodiaPortafolio.DiferenciaMercadoEmpresti;
			custodia.CantidadPactosV = custodiaPortafolio.CantidadPactosV;
			custodia.PrecioMercadoPactosV = custodiaPortafolio.PrecioMercadoPactosV;
			custodia.ValorMercadoPactosV = custodiaPortafolio.ValorMercadoPactosV;
			custodia.DiferenciaMercadoPactosV = custodiaPortafolio.DiferenciaMercadoPactosV;
			custodia.CantidadPactosC = custodiaPortafolio.CantidadPactosC;
			custodia.PrecioMercadoPactosC = custodiaPortafolio.PrecioMercadoPactosC;
			custodia.ValorMercadoPactosC = custodiaPortafolio.ValorMercadoPactosC;
			custodia.DiferenciaMercadoPactosC = custodiaPortafolio.DiferenciaMercadoPactosC;
			custodia.CantidadReporteCompra = custodiaPortafolio.CantidadReporteCompra;
			custodia.PrecioMercadoReporteCompra = custodiaPortafolio.PrecioMercadoReporteCompra;
			custodia.ValorMercadoReporteCompra = custodiaPortafolio.ValorMercadoReporteCompra;
			custodia.DiferenciaMercadoReporteCompra = custodiaPortafolio.DiferenciaMercadoReporteCompra;
			custodia.CantidadReporteVenta = custodiaPortafolio.CantidadReporteVenta;
			custodia.PrecioMercadoReporteVenta = custodiaPortafolio.PrecioMercadoReporteVenta;
			custodia.ValorMercadoReporteVenta = custodiaPortafolio.ValorMercadoReporteVenta;
			custodia.DiferenciaMercadoReporteVenta = custodiaPortafolio.DiferenciaMercadoReporteVenta;
			custodia.CantidadTransIng = custodiaPortafolio.CantidadTransIng;
			custodia.CantidadTransEgr = custodiaPortafolio.CantidadTransEgr;
			custodia.SaldoDisponibleCLP = saldoCLP;
			custodia.SaldoDisponibleUSD = saldoUSD;
			custodia.SaldoDisponibleEUR = saldoEUR;
			custodia.CodigoRegimenTipoAhorro = ("BF").equals(informacionPortafolio.Regimen) ? "BF": custodiaPortafolio.CodigoRegimenTipoAhorro;
			custodia.CargaExtraParaAPVA = ("BF").equals(custodia.Regimen) && (entrada.p_IdRegimen.equals(regimenAPV.IdRegimenTributario) || entrada.p_IdRegimen.equals(0L)) && ("BF").equals(custodia.CodigoRegimenTipoAhorro) ? "S" : "N";

			return custodia;
		}).filter(custodia->{
			return (entrada.p_IdRegimen.equals(0L) || entrada.p_IdRegimen.equals(custodia.IdRegimenTributarioMatriz) || ("S").equals(custodia.CargaExtraParaAPVA))
					&& (entrada.p_IdProducto.equals(0) || entrada.p_IdProducto.equals(custodia.IdProductoAgrupador))
					&& (entrada.p_IdPlan.equals(0) || entrada.p_IdPlan.equals(custodia.IdPlanTipoInversion))
					&& (entrada.p_IdPortafolio.equals(0L) || entrada.p_IdPortafolio.equals(custodia.IdPortafolio))
					&& (!("").equals(custodia.CodigoRegimenTipoAhorro))
					&& ((("BF").equals(custodia.CodigoRegimenTipoAhorro) && ("BF").equals(custodia.Regimen))  || (!("BF").equals(custodia.CodigoRegimenTipoAhorro) && !("BF").equals(custodia.Regimen)) );
		}).collect(Collectors.toList());
		
		
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerCustodiaSalida");
		return custodias;
	}
	
	private List<Custodia> obtenerCustodiaTraspasoSalida(EntradaConsultarCustodiaPortafolioAPV entrada, List<DetalleOrigenTraspaso> detalleTraspasos, List<Custodia> custodias){
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerCustodiaTraspasoSalida");
		
		List<Custodia> custodiasTraspaso = custodias.parallelStream().filter(custodia->{
			List<DetalleOrigenTraspaso> detalleTraspasosFiltro = detalleTraspasos.parallelStream().filter(traspaso->custodia.IdInstrumento.equals(traspaso.IdInstrumento) && (custodia.IdPortafolio.equals(traspaso.IdPortafolio) || traspaso.IdPortafolio.equals(0L)) && custodia.Regimen.equalsIgnoreCase(traspaso.CodigoRegimenTributario)).collect(Collectors.toList());
			return detalleTraspasosFiltro.size() > 0;
		}).collect(Collectors.toList());
		
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerCustodiaTraspasoSalida");
		
		return custodiasTraspaso;
	}
	
	private BigDecimal obtenerTotal(List<Custodia> custodias, String idTipoInstrumento) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioAPV.obtenerTotal");
		BigDecimal total = custodias.parallelStream().filter(custodia-> idTipoInstrumento.isEmpty() || idTipoInstrumento.equals(custodia.IdTipoInstrumento)).map(custodia->custodia.MontoMercadoCustodia).reduce(BigDecimal.ZERO, BigDecimal::add);
		logger.debug("FIN::ConsultarCustodiaPortafolioAPV.obtenerTotal");
		return total;
	}
	
	/**************************************************
	 * 					MAIN 
	 * ************************************************/
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":61625,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_IdProducto\":0,\"p_IdPlan\":0,\"p_IdTraspaso\":2994653}";
			String todos_todos_todos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String todos_todos_apv_a = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481140,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String todos_todos_bf = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":1457963,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			
			String todos_apv_a_todos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":2,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String todos_apv_a_apv_a = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481140,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":2,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String todos_apv_a_bf = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481711,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":0,\"p_IdRegimen\":2,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			
			String apv_todos_todos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String apv_todos_apv_a = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481140,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String apv_todos_bf = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481711,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			
			String apv_apv_a_todos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":3,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String apv_apv_a_apv_a = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":83017,\"p_IdPortafolio\":481140,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":2,\"p_IdProducto\":0,\"p_IdPlan\":0}";
			String apv_apv_a_bf = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":1457963,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"APV\",\"p_IdRegimen\":2,\"p_IdProducto\":0,\"p_IdPlan\":0}";

			String dc_todos_todos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_IdCliente\":249194,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_TipoAhorro\":\"DC\",\"p_IdRegimen\":0,\"p_IdProducto\":0,\"p_IdPlan\":0}";
		 
			EntradaConsultarCustodiaPortafolioAPV entrada = gson.fromJson(json, EntradaConsultarCustodiaPortafolioAPV.class);
			ConsultarCustodiaPortafolioAPV demo = new ConsultarCustodiaPortafolioAPV();
			SalidaConsultarCustodiaPortafolioAPV salida = demo.logica(con, entrada);
			
			System.out.println(salida);
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}
