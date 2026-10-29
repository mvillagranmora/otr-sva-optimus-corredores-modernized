package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.EntradaCobroComisionConsultarRegistros;
import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.SalidaCobroComisionEditarRegistro;
import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.Tabla_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.Cobro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class ProcesarCobroComisionPatrimonio implements JavaCall{
	static final Logger logger = LogManager.getLogger(ProcesarCobroComisionPatrimonio.class);
	
	private final String SQL_SELECT_COBRO_IVA = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectCobroIva.sql");
	private final String SQL_UPDATE_ESTADOS_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioCajaProcesado.sql");
	private final String SQL_UPDATE_ESTADOS_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioCustodiaProcesado.sql");
	private final String SQL_UPDATE_ESTADOS_PATRIMONIO_EDITADOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioEditadosProcesado.sql");
	
	public static class EntradaProcesarCobroComisionPatrimonio extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_EsProcesoMasivo;
		public Filtro p_Filtros;
		public List<ClientePortafolio> p_PortafoliosSeleccionados;

		public EntradaProcesarCobroComisionPatrimonio () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CTA";
			this.p_UsaReglasNegocio = "N";
			this.p_EsProcesoMasivo = "N";
			this.p_Filtros = new Filtro();
			this.p_PortafoliosSeleccionados = new ArrayList<ClientePortafolio>();
		}
	}
	
	public static class Filtro extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdProductoAgrupador;
		public String IdEstado;
		public String FechaDesde;
		public String FechaHasta;
		public Long Periodo;
		
		public Filtro () {
			this.IdCliente = 0L;
			this.IdPortafolio = 0L;
			this.IdProductoAgrupador = 0L;
			this.IdEstado = "";
			this.FechaDesde = "0001-01-01";
			this.FechaHasta = "0001-01-01";
			this.Periodo = 0L;
		}
	}
	
	public static class ClientePortafolio extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal MontoComision;
		
		public ClientePortafolio () {
			this.IdCliente = 0L;
			this.IdPortafolio = 0L;
			this.MontoComision = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaProcesarCobroComisionPatrimonio extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaProcesarCobroComisionPatrimonio () {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}
	
	public static class PatrimonioUpdate extends BcsPojo {
		public Long IdPortafolio;
		public Long IdPeriodo;
		
		public PatrimonioUpdate() {
			this.IdPeriodo = 0L;
			this.IdPortafolio = 0L;
		}
		
		public PatrimonioUpdate(Long idPortafolio, Long idPeriodo) {
			this.IdPortafolio = idPortafolio;
			this.IdPeriodo = idPeriodo;
		}
	}
	
	public SalidaProcesarCobroComisionPatrimonio logica(Connection con, EntradaProcesarCobroComisionPatrimonio entrada) throws Exception {
		SalidaProcesarCobroComisionPatrimonio salida = new SalidaProcesarCobroComisionPatrimonio();
		SQL sql = new SQL();
		
		if("N".equals(entrada.p_EsProcesoMasivo) && entrada.p_PortafoliosSeleccionados.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0l,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_PORTAFOLIOS",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		ConsultarCobroComisionRegistros servicioConsulta = new ConsultarCobroComisionRegistros();
		EntradaCobroComisionConsultarRegistros entradaConsultarRegistro = new EntradaCobroComisionConsultarRegistros();
		entradaConsultarRegistro.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarRegistro.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarRegistro.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarRegistro.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultarRegistro.p_FechaDesde = entrada.p_Filtros.FechaDesde;
		entradaConsultarRegistro.p_FechaHasta = entrada.p_Filtros.FechaHasta;
		entradaConsultarRegistro.p_Cliente = entrada.p_Filtros.IdCliente;
		entradaConsultarRegistro.p_Portafolio = entrada.p_Filtros.IdPortafolio;
		entradaConsultarRegistro.p_IdEstado = entrada.p_Filtros.IdEstado;
		entradaConsultarRegistro.p_IdProductoAgrupador = entrada.p_Filtros.IdProductoAgrupador;
		entradaConsultarRegistro.p_Periodo = entrada.p_Filtros.Periodo;
		SalidaCobroComisionEditarRegistro consultaRegistros = servicioConsulta.logica(con, entradaConsultarRegistro);
		
		List<Tabla_Salida> registrosAProcesar = new ArrayList<>();
		if("S".equals(entrada.p_EsProcesoMasivo)) {
			registrosAProcesar.addAll(consultaRegistros.P_OUT_TABLA.stream().filter(reg -> "VIG".equals(reg.EstadoComision)).collect(Collectors.toList()));
		}else {
			List<Long> idsPortafolio = entrada.p_PortafoliosSeleccionados.stream().map(por -> por.IdPortafolio).collect(Collectors.toList());
			registrosAProcesar.addAll(consultaRegistros.P_OUT_TABLA.stream().filter(reg -> idsPortafolio.contains(reg.IdPortafolio) && "VIG".equals(reg.EstadoComision)).collect(Collectors.toList()));
		}
		
		if(registrosAProcesar.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0l,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_SIN_COMISIONES_A_PROCESAR",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = creaMovimientosFacturacion(con, registrosAProcesar , entrada);
		
		if(salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR > 0L) {
			salida.P_OUT_MENSAJES.addAll(salidaCrearMovimientoComprobante.P_OUT_MENSAJES.stream().filter(msj -> MensajeSalida.MENSAJE_ERROR.equals(msj.tipo)).collect(Collectors.toList()));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		List<PatrimonioUpdate> patrimonios = salidaCrearMovimientoComprobante.p_out_Movimientos.stream().map(mov -> new PatrimonioUpdate(mov.NumeroMvtoCmbte,entrada.p_Filtros.Periodo)).collect(Collectors.toList());
		
		logger.info("INFO::ProcesarCobroComisionPatrimonio: Se actualizan los Estados de comisiones procesadas");
		sql.batch(con,SQL_UPDATE_ESTADOS_PATRIMONIO_CAJA, patrimonios, PatrimonioUpdate.class);
		sql.batch(con,SQL_UPDATE_ESTADOS_PATRIMONIO_CUSTODIA, patrimonios, PatrimonioUpdate.class);
		sql.batch(con,SQL_UPDATE_ESTADOS_PATRIMONIO_EDITADOS, patrimonios, PatrimonioUpdate.class);
		
		HashMap<String, String> paramsMensaje= new HashMap<String, String>(1);
		paramsMensaje.put("CantidadPatrimoniosProcesados", String.valueOf(patrimonios.size()) );
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0l,MensajeSalida.MENSAJE_OK,"UI_PATRIMONIOS_PROCESADOS_OK", paramsMensaje,con,entrada.p_IdEmpresa));
		
		
		return salida;
	}
	
	private CrearMovimientoComprobante_Salida creaMovimientosFacturacion(Connection con, List<Tabla_Salida> registros, EntradaProcesarCobroComisionPatrimonio entrada) throws Exception {
		logger.info("INICIO::ProcesarCobroComisionPatrimonio.creaMovimientosFacturacion");
		SQL sql=new SQL();
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String bolsaDefectoSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String glosaMovimientosPortafolioAPV = Diccionario.getMensaje(con,entrada.p_IdEmpresa, "UI_GLOSA_MOVIMIENTO_COMPROBANTE_APV", new HashMap<String, String>());
		String glosaMovimientosPortafolioNoAPV = Diccionario.getMensaje(con,entrada.p_IdEmpresa, "UI_GLOSA_MOVIMIENTO_COMPROBANTE_NO_APV", new HashMap<String, String>());
		
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = new CrearMovimientoComprobante_Salida();
		
		List<Cobro> cobros = sql.query(con, SQL_SELECT_COBRO_IVA, new HashMap<>(), Cobro.class);
		BigDecimal IVA = new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));
		BigDecimal CalculaImporteIva = IVA.add(new BigDecimal(1));
		
		Long indexCobro = 0L;
		Long indexFormaDePago = 0L;
		
		CrearMovimientoComprobante servicioCrearMovimientoComprobante = new CrearMovimientoComprobante();
		CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante_Entrada();
		entradaCrearMovimientoComprobante.p_IdEmpresa =entrada.p_IdEmpresa;
		entradaCrearMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
		entradaCrearMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearMovimientoComprobante.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
		entradaCrearMovimientoComprobante.p_EsCargaMasiva = "N";
		entradaCrearMovimientoComprobante.p_simularOperacion = "N";
		
		for(Tabla_Salida reg : registros) {
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			mov.NumeroMvtoCmbte = reg.IdPortafolio;
			mov.IdEmpresa = entrada.p_IdEmpresa;
			mov.IdUsuario = entrada.p_IdUsuario;
			mov.IdConcepto = "S".equals(reg.PortafolioAPV) ? "CMTAPV" : "CMMNRV";
			mov.FechaMovimientoComprobante = fechaSistema;
			mov.ActualizarCuentaInversion = "S";
			mov.ActualizarCustodia = "N";
			mov.ActualizarCustodiaRelacionado 	= "N";
			mov.ActualizarTesoreria 			= "N";
			mov.SecuenciaAgrupacion = "";
			mov.IdCliente = reg.IdCliente;
			mov.IdPortafolio = reg.IdPortafolio;
			mov.ModPrefFact = "";
			mov.IdEmpleadoDigitador = 0L;
			mov.IdInstrumentoRelacionado = 0L;
			mov.IdInstrumentoSubyacente = 0L;
			mov.IdTipoProducto = "";
			mov.IdMoneda = monedaSistema;
			mov.IdMonedaLiquidacion = monedaSistema;
			mov.IdMonedaSubyacente = "";
			mov.IdMonedaSpot= "";
			mov.IdTipoComprobante = "S".equals(reg.PortafolioAPV) ? "FOX" : "FOE";
			mov.Glosa = "S".equals(reg.PortafolioAPV) ? glosaMovimientosPortafolioAPV : glosaMovimientosPortafolioNoAPV;
			mov.IdTipoOperacion = "COMOTC";
			mov.IdBoveda = "";
			mov.IdCorredoraContraparte = 0L;
			mov.IdCondicionLiquidacion = "T0";
			mov.FechaLiquidacion = fechaSistema;
			mov.IdBolsa = bolsaDefectoSistema;
			mov.IdTipoReajuste = "CLP";
			mov.IdTipoConvenio = "SCV";
			mov.IdFormaCustodia = "";
			mov.IdOrden = 0L;
			mov.IdAsignacion = 0L;
			mov.IdAsignacionRef = 0L;
			mov.IdTipoCanalIngreso = "AGE";
			mov.FechaOrden = "0001-01-01";
			mov.IdTransaccion = 0L;
			mov.FolioTransaccion = "";
			mov.FechaTransaccion = "0001-01-01";
			mov.FechaVencimiento = "0001-01-01";
			mov.PrecioAPlazo = BigDecimal.ZERO;
			mov.MontoAPlazo = BigDecimal.ZERO;
			mov.FechaParidad = "0001-01-01";
			mov.ValorParidad = BigDecimal.ZERO;
			mov.FechaTipoCambio = "0001-01-01";
			mov.ValorTipoCambio = BigDecimal.ZERO;
			mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
			mov.Tasa = BigDecimal.ZERO;
			mov.Cantidad = BigDecimal.ZERO;
			mov.CrearComprobanteAutomatico = "S";
			mov.EsAPlazo = "N";
			mov.IdOperacion = 0L;
			mov.EsComprobIndividual	= "S";
			mov.DiasPlazo = BigDecimal.ZERO;
			mov.EsCuentaPropia = "N";
			mov.EsEnRueda = "S";
			mov.EsFungible = "N";
			mov.EsPrecioPrima = "N";
			mov.EsRuteo = "N";
			mov.EsVentaCorta = "N";
			mov.FechaEmision = "0001-01-01";
			mov.FolioCartera = 0L;
			mov.IdCuentaCustodioPortafolio = 0L;
			mov.IdEmisor = "";
			mov.IdMesa = 0L;
			mov.IdTipoCliente = "";
			mov.IndicadorOrdenDirecta = "N";
			mov.Intereses = BigDecimal.ZERO;
			mov.LiquidaBancoCustodio = "N";
			mov.ModPrefFact = null;
			mov.Monto = reg.MontoComision;
			mov.MontoDescuento = BigDecimal.ZERO;
			mov.MontoInteres = BigDecimal.ZERO;
			mov.MontoLiquidacion = reg.MontoComision;
			mov.MontoMovimientoComprobante = reg.MontoComision;
			mov.NemoSVS = "";
			mov.Precio = BigDecimal.ZERO;
			mov.ReajustesVAC = BigDecimal.ZERO;
			mov.Spread = BigDecimal.ZERO;
			mov.TasaAgente = BigDecimal.ZERO;
			mov.TasaEmision = BigDecimal.ZERO;
			mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
			mov.ValorRescate = BigDecimal.ZERO;
			mov.IdTipoLiquidacionComisionFactura = "";
			entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
			
			BigDecimal totalCobros = BigDecimal.ZERO;
			
			CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada forma = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
			forma.FechaDocumentoPago = "0001-01-01";
			forma.FechaVctoTarjetaBancaria = "0001-01-01";
			forma.IdBanco = 0L;
			forma.IdCuentaBancaria = 0L;
			forma.IdMedioDePago = "CIN";
			forma.IdModalidadDePago = "MODDEP";
			forma.IdMoneda = monedaSistema;
			forma.IdTarjetaBancaria = 0L;
			forma.IdTipoCuentaBancaria = "";
			forma.MontoLiquidacionFormaDePago = reg.MontoComision;
			forma.NumeroCuentaBancaria = "";
			forma.IdTipoTarjetaBancaria = "";
			forma.NumeroDocumento = 0L;
			forma.NumeroMvtoCmbte = mov.NumeroMvtoCmbte;
			forma.NumeroMvtoCmbteFormaDePago = indexFormaDePago++;
			forma.NumeroSerie = "";
			forma.NumeroTarjetaBancaria = "";
			forma.AfectarTesoreria = "N";
			forma.AfectarCuentaInversion = "S";
			
			entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(forma);
			
			
		}
		
		salidaCrearMovimientoComprobante = servicioCrearMovimientoComprobante.logica(con,entradaCrearMovimientoComprobante);
		
		logger.info("FIN::ProcesarCobroComisionPatrimonio.creaMovimientosFacturacion");
		return salidaCrearMovimientoComprobante;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 0,\r\n"
					+ "  \"p_IdModulo\": \"CTA\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_EsProcesoMasivo\": \"N\",\r\n"
					+ "  \"p_Filtros\": {\"IdCliente\": 0 ,\"IdProductoAgrupador\": 0,\"IdPortafolio\":0, \"p_Periodo\": 2, \"p_FechaDesde\": \"2024-04-01\",\"p_FechaHasta\": \"2024-06-30\" },\r\n"
					+ "  \"p_PortafoliosSeleccionados\": [251953]\r\n"
					+ "}";
			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_EsProcesoMasivo\":\"N\",\"p_Filtros\":{\"IdCliente\":0,\"IdPortafolio\":0,\"IdProductoAgrupador\":0,\"IdEstado\":\"\",\"FechaDesde\":\"2024-04-01\",\"FechaHasta\":\"2024-06-30\",\"Periodo\":2},\"p_PortafoliosSeleccionados\":[22589,117602]}\r\n"
					+ "";
			EntradaProcesarCobroComisionPatrimonio Objin = gson.fromJson(json2, EntradaProcesarCobroComisionPatrimonio.class);
			ProcesarCobroComisionPatrimonio demo = new ProcesarCobroComisionPatrimonio();
			try {
				SalidaProcesarCobroComisionPatrimonio salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
//				con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}

	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaProcesarCobroComisionPatrimonio jin=gson.fromJson(in.toString() , EntradaProcesarCobroComisionPatrimonio.class);
		SalidaProcesarCobroComisionPatrimonio out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}

}
