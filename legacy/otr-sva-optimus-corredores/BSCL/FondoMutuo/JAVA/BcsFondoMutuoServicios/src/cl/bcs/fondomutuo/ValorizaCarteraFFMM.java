package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos.Cartera;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos.SerieFondo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizaCarteraFFMM implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValorizaCarteraFFMM.class);
	private final String SQL_SELECT_SERIES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizaCarteraValorizadaFFMM_SelectSerieFondos.sql");
	
	
	
	public static class EntradaValorizaCarteraFFMM {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Cartera> carteras;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class SalidaValorizaCarteraFFMM {
		public Long P_OUT_ERRORESCONTADOR;
		public List <Cartera> P_OUT_CARTERAS_VALORIZADAS;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValorizaCarteraFFMM() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_CARTERAS_VALORIZADAS=new ArrayList<Cartera>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaValorizaCarteraFFMM jin = gson.fromJson(in.toString(), EntradaValorizaCarteraFFMM.class);
		SalidaValorizaCarteraFFMM out = logica(conn,jin);		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaValorizaCarteraFFMM logica(Connection con, EntradaValorizaCarteraFFMM entrada) throws Exception {
		logger.info("INICIO::ValorizaCarteraFFMM.logica");		
		String idMonedaSistema = obtenerMonedaSistema(con,entrada.p_IdEmpresa);
		String idMonedaAdicional = obtenerMonedaAdicional(con,entrada.p_IdEmpresa);		
		SalidaValorizaCarteraFFMM out = new SalidaValorizaCarteraFFMM ();
		List<String> listaSerieFondo = entrada.carteras.stream().map(c -> c.IdSerieFondo).collect(Collectors.toList());	
		List<SerieFondo> series = obtenerSerieFondos(con,listaSerieFondo);
		Map<String,EntregarPreciosTasas_Salida_PreciosTasas> precios = obtenerPrecios(con,entrada,series);
		out.P_OUT_CARTERAS_VALORIZADAS= valorizarCarteras(entrada,precios,idMonedaSistema,idMonedaAdicional);
		
		return out;
	}
	
	private List<Cartera> valorizarCarteras(EntradaValorizaCarteraFFMM entrada,Map<String,EntregarPreciosTasas_Salida_PreciosTasas> precios, String idMonedaSistema, String idMonedaAdicional) {
		logger.info("INICIO::ValorizaCarteraFFMM.valorizarCarteras");
		entrada.carteras.parallelStream().forEach(cartera->{
			EntregarPreciosTasas_Salida_PreciosTasas precio = precios.get(cartera.IdSerieFondo);
			if(precio.Precio == null) {
				precio.Precio = BigDecimal.ZERO;
			}
			if(cartera.EGRCantidad != null) {
				cartera.EGRPrecioMercado = precio.Precio;
				cartera.EGRFechaPrecioTasaMercado = precio.Precio.compareTo(BigDecimal.ZERO) == 1 ? Fecha.toFormat(precio.FechaFuente,"YYYY-MM-dd") : "0001-01-01";
				cartera.EGRFuenteTasaPrecioMercado = precio.CodigoFuente;
				cartera.EGRMontoATasaMercado = BigDecimal.ZERO;
				cartera.EGRMontoMercadoMonedaAdicional = BigDecimal.ZERO;
				cartera.EGRMonto = BigDecimal.ZERO;
				cartera.EGRMontoMonedaAdicional = BigDecimal.ZERO;
				cartera.EGRMontoMercadoMonedaInstrumento = cartera.EGRCantidad.multiply(precio.Precio, MathContext.DECIMAL128);
				cartera.EGRMontoMonedaInstrumento = cartera.EGRCantidad.multiply(cartera.EGRPrecio, MathContext.DECIMAL128);
				if(cartera.IdMoneda==null || cartera.IdMoneda.equals("")) {
					if(precio.IdMoneda.equals(idMonedaSistema)) {
						cartera.EGRMontoATasaMercado = cartera.EGRMontoMercadoMonedaInstrumento;
						cartera.EGRMonto = cartera.EGRMontoMonedaInstrumento;
					}else if(precio.IdMoneda.equals(idMonedaAdicional)) {
						cartera.EGRMontoMercadoMonedaAdicional = cartera.EGRMontoMercadoMonedaInstrumento;
						cartera.EGRMontoMonedaAdicional = cartera.EGRMontoMonedaInstrumento;
					}
					cartera.IdMoneda=precio.IdMoneda;
				}else {
					if(cartera.IdMoneda != null && cartera.IdMoneda.equals(idMonedaSistema)) {
						cartera.EGRMontoATasaMercado = cartera.EGRMontoMercadoMonedaInstrumento;
						cartera.EGRMonto = cartera.EGRMontoMonedaInstrumento;
					}else if(cartera.IdMoneda != null && cartera.IdMoneda.equals(idMonedaAdicional)) {
						cartera.EGRMontoMercadoMonedaAdicional = cartera.EGRMontoMercadoMonedaInstrumento;
						cartera.EGRMontoMonedaAdicional = cartera.EGRMontoMonedaInstrumento;
					}
				}
			}
			if(cartera.INGCantidad != null){
				cartera.INGPrecioMercadoActual = precio.Precio;
				cartera.INGFechaPrecioTasaMercadoActual = precio.Precio.compareTo(BigDecimal.ZERO) == 1 ? Fecha.toFormat(precio.FechaFuente,"YYYY-MM-dd") : "0001-01-01";
				cartera.INGFuenteTasaPrecioMercadoActual = precio.CodigoFuente;
				cartera.INGMontoATasaMercadoActual = BigDecimal.ZERO;
				cartera.INGMontoMercadoActualMonedaAdicional = BigDecimal.ZERO;
				cartera.INGMontoMercadoActualMonedaInstrumento = cartera.INGCantidad.multiply(precio.Precio, MathContext.DECIMAL128);
				if(cartera.IdMoneda==null || cartera.IdMoneda.equals("")) {
					if(precio.IdMoneda.equals(idMonedaSistema)) {
						cartera.INGMontoATasaMercadoActual = cartera.INGMontoMercadoActualMonedaInstrumento;
					}else if(precio.IdMoneda.equals(idMonedaAdicional)) {
						cartera.INGMontoMercadoActualMonedaAdicional = cartera.INGMontoMercadoActualMonedaInstrumento;
					}
					cartera.IdMoneda=precio.IdMoneda;
				}else {
					if(cartera.IdMoneda != null && cartera.IdMoneda.equals(idMonedaSistema)) {
						cartera.INGMontoATasaMercadoActual = cartera.INGMontoMercadoActualMonedaInstrumento;
						cartera.INGMontoATasaCompraActual = cartera.INGMontoMercadoActualMonedaInstrumento;
						cartera.INGMontoCompraActualMonedaInstrumento = cartera.INGMontoMercadoActualMonedaInstrumento;
					}else if(cartera.IdMoneda != null && cartera.IdMoneda.equals(idMonedaAdicional)) {
						cartera.INGMontoMercadoActualMonedaAdicional = cartera.INGMontoMercadoActualMonedaInstrumento;
					}
				}
			}
			
		});
		
		logger.info("FIN::ValorizaCarteraFFMM.valorizarCarteras");
		return entrada.carteras;
		
	}
	
	private Map<String,EntregarPreciosTasas_Salida_PreciosTasas> obtenerPrecios(Connection con, EntradaValorizaCarteraFFMM entrada, List<SerieFondo> series) throws Exception {
		logger.info("INICIO::ActualizaCarteraValorizadaFFMM.obtenerPrecios");
		String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
		java.sql.Date fechaConsulta = Fecha.toDate(fechaSistema);
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsPrecioTasa.p_IdModulo = entrada.p_IdModulo;
		paramsPrecioTasa.p_IdPais = obtenerPaisSistema(con,entrada.p_IdEmpresa);
		paramsPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;
		paramsPrecioTasa.p_DatosInstrumentos = series.parallelStream()
		.map(dato->{
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta = fechaConsulta;
			paramsRegIn.IdClasificacionTipoInstrumento = dato.IdClasificacionTipoInstrumento;
			paramsRegIn.IdInstrumento = dato.IdInstrumento;
			paramsRegIn.IdMoneda = dato.IdMoneda;
			paramsRegIn.IdRegistro = 0L;
			paramsRegIn.IdTipoCriterioTasa = "0";
			paramsRegIn.IdTipoInstrumento = dato.IdTipoInstrumento;
			paramsRegIn.IdUso = "CARTERA";
			paramsRegIn.Nemotecnico = dato.IdSerieFondo;
			paramsRegIn.PlazoResidual = 0L;
			paramsRegIn.TipoCartera = "0";
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		Map<String,EntregarPreciosTasas_Salida_PreciosTasas> precios = salidaPrecioTasa.P_OUT_PRECIOSTASAS.parallelStream().collect(Collectors.toMap(instrumento->instrumento.Nemotecnico, precio-> precio));
		logger.info("FIN::ActualizaCarteraValorizadaFFMM.obtenerPrecios");
		return precios;
	}
	
	private List<SerieFondo> obtenerSerieFondos(Connection con,List<String> listaSerieFondo) throws Exception{
		logger.info("INICIO::ActualizaCarteraValorizadaFFMM.obtenerSerieFondos");
		SQL sql=new SQL();		
		HashMap<String,Object> params = new HashMap<String,Object>();
		Set<String> hashSet = new HashSet<String>(listaSerieFondo);
		listaSerieFondo.clear();
		listaSerieFondo.addAll(hashSet);		
		params.put("ListaIdSerieFondo", listaSerieFondo);				
		List<SerieFondo> series = sql.query(con, SQL_SELECT_SERIES, params, SerieFondo.class);
		logger.info("FIN::ActualizaCarteraValorizadaFFMM.obtenerSerieFondos");
		return series;
	}
	
	private String obtenerMonedaAdicional(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ValorizaCarteraFFMM.obtenerMonedaAdicional");
		String idMonedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
		logger.info("FIN::ValorizaCarteraFFMM.obtenerMonedaAdicional");
		return idMonedaAdicional;
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ValorizaCarteraFFMM.obtenerMonedaSistema");
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		logger.info("INICIO::ValorizaCarteraFFMM.obtenerMonedaSistema");
		return idMonedaSistema;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ActualizaCarteraValorizadaFFMM.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.FechaSistema");
		logger.info("FIN::ActualizaCarteraValorizadaFFMM.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private Long obtenerPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ActualizaCarteraValorizadaFFMM.obtenerPaisSistema");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		logger.info("FIN::ActualizaCarteraValorizadaFFMM.obtenerPaisSistema");
		return paisSistema;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBolsaConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String json = "{\"p_IdEmpresa\":1\r\n" + 
				",\"p_IdUsuario\":14\r\n" + 
				",\"p_IdModulo\"=\"FMM\"\r\n" + 
				",\"carteras\":[{\r\n" + 
				"	\"IdSerieFondo\":\"00054APVUSD\",\r\n" + 
				"	\"IdCartera\":0,\r\n" + 
				"	\"INGCantidad\": 100,\r\n" + 
				"	\"INGMontoMercadoActualMonedaInstrumento\": 0,\r\n" + 
				"	\"INGMontoATasaMercadoActual\":0,\r\n" + 
				"	\"INGMontoMercadoActualMonedaAdicional\":0,\r\n" + 
				"	\"IdMoneda\": \"\",\r\n" + 
				"	\"INGPrecioMercadoActual\":0,\r\n" + 
				"	\"INGFechaPrecioTasaMercadoActual\":0,\r\n" + 
				"	\"INGFuenteTasaPrecioMercadoActual\":0\r\n" + 
				"}]}     ";
		
		EntradaValorizaCarteraFFMM Objin = gson.fromJson(json, EntradaValorizaCarteraFFMM.class);
		ValorizaCarteraFFMM  demo=new ValorizaCarteraFFMM();
			try {
				SalidaValorizaCarteraFFMM ObjOut=demo.logica(con, Objin);
				if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
					con.rollback();
				}else {
					con.rollback();
				}
				System.out.println(ObjOut.toString());
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
