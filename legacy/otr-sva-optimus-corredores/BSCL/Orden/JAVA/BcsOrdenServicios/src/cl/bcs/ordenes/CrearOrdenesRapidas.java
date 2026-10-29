package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto.EntradaConsultarValoresPorDefecto;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.ordenes.ConsultarInformacionPortafolioOrdenes.EntradaConsultarInformacionPortafolioOrdenes;
import cl.bcs.ordenes.ConsultarInformacionPortafolioOrdenes.PortafolioOrdenes;
import cl.bcs.ordenes.ConsultarInformacionPortafolioOrdenes.SalidaConsultarInformacionPortafolioOrdenes;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.ObjCOBROS;
import cl.bcs.ordenes.CrearOrdenes.ObjGARANTIAS;

public class CrearOrdenesRapidas implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(CrearOrdenesRapidas.class);
	private final String SQL_SELECT_TIPO_OPERACIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesRapidas_SelectTipoOperaciones.sql");
	private final String SQL_INSERT_DATOS_PORTAFOLIO_TIPO_OPERACION_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesRapidas_InsertDatosTablaPaso.sql");
	private final String SQL_SELECT_PREFERENCIAS_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesRapidas_SelectPreferenciasDePagoPortafolio.sql");

	
	public static class EntradaCrearOrdenesRapidas extends BcsPojo {
		public Long					p_IdEmpresa;
		public Long					p_IdUsuario;
		public String				p_IdModulo;
		public String 				p_IdTipoCanalIngreso;
		public List<OrdenRapida>	p_OrdenesRapidas;
		
		public EntradaCrearOrdenesRapidas() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "ORD";
			this.p_IdTipoCanalIngreso = "AGE";
			this.p_OrdenesRapidas = new ArrayList<OrdenRapida>();
		}
	}
	
	public static class SalidaCrearOrdenesRapidas extends BcsPojo {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public List<ObjCOBROS>		P_OUT_COBROS;
		public List<ObjGARANTIAS>	P_OUT_GARANTIAS;
		public List<ObjRESULTADO>	P_OUT_RESULTADO;
		
		public SalidaCrearOrdenesRapidas() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_COBROS = new ArrayList<ObjCOBROS>();
			this.P_OUT_GARANTIAS = new ArrayList<ObjGARANTIAS>();
			this.P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
			
		}
		
	}
	
	public static class ConfiguracionOrdenRapida extends BcsPojo {
	    public String CodigoCampo;
	    public String Valor;
	    
	    public ConfiguracionOrdenRapida() {
	    	this.CodigoCampo = "";
	    	this.Valor = "";
	    }
	}

	public static class OrdenRapida extends BcsPojo {
		public Long IdPortafolio;
		public String TipoOperacion;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdEmisor;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal PrecioMercado;
		
		public OrdenRapida () {
			this.IdPortafolio = 0L;
			this.TipoOperacion = "";
			this.IdInstrumento = 0L;
			this.IdTipoInstrumento = "";
			this.IdEmisor= "";
			this.Cantidad = BigDecimal.ZERO;
			this.Precio = BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
		}
	}
	
	public static class PreferenciaDePagoPortafolio extends BcsPojo{
		public Long IdPreferenciaPagoPortafolio;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdMoneda;
		
		public PreferenciaDePagoPortafolio() {
			this.IdPreferenciaPagoPortafolio = 0L;
			this.IdPortafolio = 0L;
			this.IdTipoOperacion = "";
			this.IdMoneda = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCrearOrdenesRapidas in = gson.fromJson(jin.toString(), EntradaCrearOrdenesRapidas.class);
		SalidaCrearOrdenesRapidas out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearOrdenesRapidas logica(Connection conn, EntradaCrearOrdenesRapidas in) throws Exception {
		logger.info("INICIO::CrearOrdenesRapidas");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearOrdenesRapidas out = new SalidaCrearOrdenesRapidas();
		logger.info("Inicio servicio OrdenRapida");
		try {
			
			CrearOrdenesEntrada entrada = new CrearOrdenesEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "ORD";
			entrada.p_simularOperacion = "N";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_Ordenes = obtenerOrdenes(conn, in);
						
			CrearOrdenes crearOrdenes = new CrearOrdenes();
			CrearOrdenesSalida salidaCrear = crearOrdenes.logica(conn, entrada);
	
			out.P_OUT_MENSAJES = salidaCrear.P_OUT_MENSAJES;
			out.P_OUT_COBROS = salidaCrear.P_OUT_COBROS;
			out.P_OUT_GARANTIAS = salidaCrear.P_OUT_GARANTIAS;
			out.P_OUT_RESULTADO = salidaCrear.P_OUT_RESULTADO;
	        out.P_OUT_ERRORESCONTADOR = salidaCrear.P_OUT_ERRORESCONTADOR;
		} catch (Exception e) {
	        logger.error("Error en OrdenRapida", e);
	        out.P_OUT_MENSAJES.add(new MensajeSalida(2L,e.getMessage(),"UI_ERROR_ORDEN_RAPIDA","" ));
	        out.P_OUT_ERRORESCONTADOR++;
	    }
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearOrdenesRapidas", finServicio - inicioServicio);
		return out;
	}
		
	private List<ObjOrdenesEntrada> obtenerOrdenes(Connection conn, EntradaCrearOrdenesRapidas entrada) throws Exception {
		logger.info("INICIO::CrearOrdenesRapidas.obtenerOrdenes");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		ConsultarValoresPorDefecto consultarValoresDefecto = new ConsultarValoresPorDefecto();
		EntradaConsultarValoresPorDefecto entradaValoresDefecto = new EntradaConsultarValoresPorDefecto();
		entradaValoresDefecto.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValoresDefecto.p_TipoEntidad = "ORDENRAPIDA";
		entradaValoresDefecto.p_IdTipoCanalIngreso = "AGE";
		HashMap<String, String> hashConfig = consultarValoresDefecto.logica(conn, entradaValoresDefecto);
		
		HashMap<String,Object> params = new HashMap<>(2);
		List<String> tipoInstrumentos = entrada.p_OrdenesRapidas.stream().map(ord -> ord.IdTipoInstrumento.toUpperCase()).distinct().collect(Collectors.toList());
		List<String> acciones = entrada.p_OrdenesRapidas.stream().map(ord -> ord.TipoOperacion.toUpperCase()).distinct().collect(Collectors.toList());
		params.put("Acciones", acciones);
		params.put("IdsTipoInstrumento", tipoInstrumentos);
		List<ConfiguracionOrdenRapida> tipoOperaciones = sql.query(conn, SQL_SELECT_TIPO_OPERACIONES, params, ConfiguracionOrdenRapida.class);
		Map<String,String> hashTipoOper = tipoOperaciones.stream().collect(Collectors.toMap(tipo -> tipo.CodigoCampo, tipo -> tipo.Valor));

		ConsultarInformacionPortafolioOrdenes consultarInfoPortafolio = new ConsultarInformacionPortafolioOrdenes();
		EntradaConsultarInformacionPortafolioOrdenes entradaConsultarInfoPortafolio = new EntradaConsultarInformacionPortafolioOrdenes();
		entradaConsultarInfoPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarInfoPortafolio.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarInfoPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarInfoPortafolio.p_Portafolios = entrada.p_OrdenesRapidas.stream().map(ord -> ord.IdPortafolio).distinct().collect(Collectors.toList());
		SalidaConsultarInformacionPortafolioOrdenes salidaConsultarInfoPortafolio = consultarInfoPortafolio.logica(conn, entradaConsultarInfoPortafolio);
		Map<Long,PortafolioOrdenes> hashInfoPortafolio = salidaConsultarInfoPortafolio.P_OUT_PORTAFOLIOSORDENES.stream().collect(Collectors.toMap(info-> info.IdPortafolio , info-> info));
		
		List<ObjOrdenesEntrada> ordenes = new ArrayList<ObjOrdenesEntrada>();
		
		for(OrdenRapida ordenRapida : entrada.p_OrdenesRapidas) {
			ObjOrdenesEntrada orden = new ObjOrdenesEntrada();
			PortafolioOrdenes infoPortafolio = hashInfoPortafolio.get(ordenRapida.IdPortafolio);
			
			orden.Cantidad = ordenRapida.Cantidad;
			orden.PrecioLimite = ordenRapida.Precio;
			orden.IdPortafolio = ordenRapida.IdPortafolio;
			orden.BaseTasa = Long.parseLong(hashConfig.get("BaseTasa"));
			orden.EsFacturaInmediata = hashConfig.get("EsFacturaInmediata");
			orden.EsFungible = hashConfig.get("EsFungible");
			orden.EsOD = hashConfig.get("EsOD");
			orden.EsOrdenEspecial = hashConfig.get("EsOrdenEspecial");
			orden.EspecificaBolsa = hashConfig.get("EspecificaBolsa");
			orden.EsPorMonto = hashConfig.get("EsPorMonto");
			orden.EsVentaCorta = hashConfig.get("EsVentaCorta");
			orden.FechaEmision = hashConfig.get("FechaEmisionDefault");
			orden.FechaOperacion = hashConfig.get("FechaOperacionDefault");
			orden.FechaRecepcion = hashConfig.get("FechaRecepcionDefault");
			orden.FechaVigencia = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);
			orden.Glosa = "";
			orden.IdBolsa = hashConfig.get("IdBolsa");
			orden.IdBoveda = hashConfig.get("IdBoveda");
			orden.IdCondicionDeLiquidacion = hashConfig.get("IdCondicionDeLiquidacion");
			
			orden.IdCondicionDeLiquidacionPlazo = "";
			orden.IdCorredorCorresponsal = null;
			orden.IdCuentaCustodioPortafolio = null;
			orden.IdEmisor = ordenRapida.IdEmisor;
			orden.IdInstrumento = ordenRapida.IdInstrumento;
			orden.IdFormaCustodia = hashConfig.get("IdFormaCustodia");
			orden.IdInstrumentoSubyacente = 0L;
			orden.IdInterno = 0L;
			orden.IdLibroOrdenes = null;
			orden.IdMecanismoCaptacion = hashConfig.get("IdMecanismoCaptacion");
			//orden.IdMonedaLiquidacionCobros = "CLP"; //???
			orden.IdMonedaLiquidacionOrden = hashConfig.get("IdMonedaLiquidacionOrden"); //Agregar Caso cuando viene moneda
			orden.IdPersonaAutorizadoDarOrdenes = null;
			orden.IdPorcentajeVisibilidad = Long.parseLong(hashConfig.get("IdPorcentajeVisibilidad"));
			orden.IdTipoCanalDistribucion = hashConfig.get("CanalTransaccionRapida");
			orden.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			orden.IdTipoLibroOrden = hashConfig.get("IdTipoLibroOrden");
			orden.IdTipoOperacion = hashTipoOper.get(ordenRapida.TipoOperacion + "_" + ordenRapida.IdTipoInstrumento);
			orden.IdTipoPrecio = hashConfig.get("IdTipoPrecio");
			orden.IdTipoReajuste = "";
			orden.IdTipoVigencia = hashConfig.get("IdTipoVigencia");
			orden.MontoNeto = BigDecimal.ZERO;
			//orden.MontoPlazo = BigDecimal.ZERO;??
			orden.PagoCobroCuentaInversion = hashConfig.get("PagoCobroCuentaInversion");
			orden.PlazoIF = Long.parseLong(hashConfig.get("PlazoIF"));
			orden.PlazoSimultanea = Long.parseLong(hashConfig.get("PlazoSimultanea"));
			orden.PlazoVentaCorta = Long.parseLong(hashConfig.get("PlazoVentaCorta"));
			//orden.PrecioMercado =  ordenRapida.PrecioMercado;// BigDecimal.ZERO; ???
			orden.TasaRFIF = BigDecimal.ZERO;
			orden.TasaSimultanea = BigDecimal.ZERO;
			orden.TasaVentaCorta = BigDecimal.ZERO;
			orden.TipoCambio = null;
			orden.TipoInteres = hashConfig.get("TipoInteres");
			orden.ValorRescate = BigDecimal.ZERO;
			orden.IdPreferenciaPagoPortafolio = 0L;
			orden.PagoCobroCuentaInversion = infoPortafolio.OperaCuentaInversion;
			
			ordenes.add(orden);
		}
		
		List<PreferenciaDePagoPortafolio> portafoliosPaso = ordenes.stream().map(ord -> {
			PreferenciaDePagoPortafolio pref = new PreferenciaDePagoPortafolio();
			pref.IdPortafolio = ord.IdPortafolio;
			pref.IdTipoOperacion = ord.IdTipoOperacion;
			pref.IdMoneda = ord.IdMonedaLiquidacionOrden;
			return pref;
		}).collect(Collectors.toList());
		
		sql.batch(conn, SQL_INSERT_DATOS_PORTAFOLIO_TIPO_OPERACION_TABLA_PASO, portafoliosPaso, PreferenciaDePagoPortafolio.class);
		
		List<PreferenciaDePagoPortafolio> preferenciasDePagoPortafolio = sql.query(conn, SQL_SELECT_PREFERENCIAS_PAGO_PORTAFOLIO,new HashMap<>(0) , PreferenciaDePagoPortafolio.class);
		Map<String,Long> hashPrefPagoPort = preferenciasDePagoPortafolio.stream()
				.collect(Collectors.toMap(pref -> pref.IdTipoOperacion+"_"+pref.IdMoneda+"_"+pref.IdPortafolio, pref -> pref.IdPreferenciaPagoPortafolio));
		
		for(ObjOrdenesEntrada orden : ordenes) {
			String llave = orden.IdTipoOperacion+"_"+orden.IdMonedaLiquidacionOrden+"_"+orden.IdPortafolio;
			Long idPreferenciaPagoPortafolio = hashPrefPagoPort.get(llave);
			orden.IdPreferenciaPagoPortafolio = idPreferenciaPagoPortafolio != null ? idPreferenciaPagoPortafolio : 0L;
			orden.PagoCobroCuentaInversion = orden.IdPreferenciaPagoPortafolio != 0L ? "N" : orden.PagoCobroCuentaInversion;
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearOrdenesRapidas.obtenerOrdenes", finServicio - inicioServicio);
		return ordenes;
	}

	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;	
		//MASTER
		//String json = 	"{\"p_IdEmpresa\":1,\"p_IdUsuario\":9191,\"p_IdModulo\":\"ORD\",\"p_IdInstrumento\":266917,\"p_Cantidad\":10,\"p_Precio\":110,\"p_TipoOperacion\":\"Compra\",\"p_IdPortafolio\":1124,\"p_IdTipoCanalIngreso\":\"AGE\"}";
		//DESA
//		String json = 	"{\"p_IdEmpresa\":1,\"p_IdUsuario\":9784,\"p_IdModulo\":\"ORD\",\"p_IdInstrumento\":266917,\"p_Cantidad\":10,\"p_Precio\":110,\"p_TipoOperacion\":\"Compra\",\"p_IdPortafolio\":5931,\"p_IdTipoCanalIngreso\":\"AGE\"}";
		//QA02
		String json = "{\r\n"
				+ "  p_IdEmpresa: 1,\r\n"
				+ "  p_IdUsuario: 9564,\r\n"
				+ "  p_IdModulo: \"ORD\",\r\n"
				+ "  p_IdTipoCanalIngreso: \"AGE\",\r\n"
				+ "  p_OrdenesRapidas: [\r\n"
				+ "    {\r\n"
				+ "      IdInstrumento: 266917,\r\n"
				+ "      IdEmisor: \"ABC\",\r\n"
				+ "      IdTipoInstrumento: \"ACN\",\r\n"
				+ "      Cantidad: 1000,\r\n"
				+ "      Precio: 53,\r\n"
				+ "      TipoOperacion: \"COMPRA\",\r\n"
//				+ "      IdPortafolio: 15321\r\n"
				+ "      IdPortafolio: 12089\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearOrdenesRapidas entrada = gson.fromJson(json, EntradaCrearOrdenesRapidas.class);	
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
			CrearOrdenesRapidas Demo = new  CrearOrdenesRapidas();
			try {
				SalidaCrearOrdenesRapidas ObjOut = Demo.logica(conn, entrada);
				System.out.println(ObjOut);
//				conn.rollback();
				conn.commit();
			}catch(Exception e){
				e.printStackTrace();
				conn.rollback();
			}
			
		}catch (Exception e){	
			System.out.println(e);	
			} finally {		conn.close();		}
		System.out.println("== END ==");
	}
}