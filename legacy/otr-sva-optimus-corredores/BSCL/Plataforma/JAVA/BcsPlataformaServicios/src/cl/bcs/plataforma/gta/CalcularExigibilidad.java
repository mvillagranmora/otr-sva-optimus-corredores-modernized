package cl.bcs.plataforma.gta;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CalcularCoberturaOrdenes;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CalcularExigibilidad implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CalcularExigibilidad.class);
	private final String SQL_Select_InstrumentosGrupoTipoInstrumento = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectInstrumentosgrupoTipoInstrumento.sql");
	private final String SQL_Select_Exigibilidad 					 = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectExigibilidad.sql");
	private final String SQL_Select_OrdenesPortafolios 				 = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectOrdenesPortafolios.sql");
	private final String SQL_Select_OrdenesClasificacionRiesgo		 = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectOrdenesClasificacionRiesgo.sql");
	private final String SQL_Select_CustodiaVigente					 = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectCustodiaVigente.sql");
	private final String SQL_Select_CustodiaVigenteDerivados		 = loadResourceToString( "cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectCustodiaVigenteDerivados.sql");

	public final List<String> tiposOpcion = Arrays.asList(new String[]{"IOA", "IOE", "IOI", "IOD","IOB", "IOC", "IOU", "IOM", "IOF"});

	public static class CalcularExigibilidadEntrada{
		
		public Long    p_IdEmpresa;
        public Long    p_IdUsuario;
        public String  p_IdModulo;
        public String  p_simularOperacion;
        public String  p_UsaReglasNegocio;
        public List<ObjExigibilidadEntrada>  p_TableExigibilidadSimulacion;
        public String  p_IdTipoInstrumento;
        public Long    p_IdPortafolio;
        public String  p_IdBolsa;
        public Long    p_IdInstrumento;
        public String  p_IdMoneda;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CalcularExigibilidadSalida{
		public List<ObjExigibilidadSimulacion> P_OUT_TABLEEXIGIBILIDADSIMULACION;
		public List<ObjExigibilidadPortafolio> P_OUT_TABLEEXIGIBILIDADPORTAFOLIO;   
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public CalcularExigibilidadSalida() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TABLEEXIGIBILIDADPORTAFOLIO =new ArrayList<ObjExigibilidadPortafolio>();
			this.P_OUT_TABLEEXIGIBILIDADSIMULACION=new ArrayList<ObjExigibilidadSimulacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ObjExigibilidadSimulacion {
		 
		public String 	IdTipoInstrumento;
		public Long 	IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public String 	  IdBolsa;
		public BigDecimal MontoExigibilidadEntidad;
		public BigDecimal MontoExigibilidadCorredora; 
		public BigDecimal PorcentajeExigibilidadEntidad;
		public BigDecimal PorcentajeExigibilidadCorredora;
		
		public ObjExigibilidadSimulacion () {
			this.MontoExigibilidadEntidad  =BigDecimal.ZERO;
			this.MontoExigibilidadCorredora=BigDecimal.ZERO; 
			this.PorcentajeExigibilidadEntidad  =BigDecimal.ZERO;
			this.PorcentajeExigibilidadCorredora=BigDecimal.ZERO;
			this.Precio  =BigDecimal.ZERO;
			this.Monto 	 =BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ObjExigibilidadEntrada{
		public Long   		IdInterno;
		public String 		IdTipoInstrumento; 
		public Long   		IdInstrumento;
		public BigDecimal   Cantidad;
		public BigDecimal   Precio;
		public BigDecimal   Monto;
		public String 		IdBolsa;
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjExigibilidadPortafolio{
		
		public BigDecimal MontoExigibilidadEntidad;
		public BigDecimal MontoExigibilidadCorredora;
		
		public ObjExigibilidadPortafolio() {
			this.MontoExigibilidadCorredora=BigDecimal.ZERO;
			this.MontoExigibilidadEntidad =BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjInstrumentoTipoInstrumento{
		
		public String Nemotecnico;
		public Long   IdInstrumento;
		public String IdMonedaInstrumento;
		public String IdTipoInstrumento;
		public String ClasificacionRiesgo;
		public String IdEntidadClasificadoraRiesgo;
		public String IdTipoClasificacionRiesgo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjExigibilidad{
		public String IdBolsa ;
		public String IdTipoInstrumento ;
		public String IdTipoInstrumentoSubyacente ;
		public String ClasificacionRiesgo ;
		public String IdEntidadClasificadoraRiesgo ;
		public String IdTipoClasificacionRiesgo ;
		public Long IdInstrumento ;
		public BigDecimal ExigibilidadEntidad ;
		public BigDecimal ExigibilidadCorredora ;
	}
	
	public static class ObjGrupoTipoInstrumento{
		public String CodigoClasificacion;
		public String IdTipoClasificacionRiesgo;
	}
	
	public static class ObjOrdenPortafolio{
		public Long IdOrden;
		public Long IdInstrumento;
		public BigDecimal CantidadDisponibleOrden  ;
		public BigDecimal PrecioMercado;
		public BigDecimal Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjOrdenClasificacionRiesgo{
		public Long   IdInstrumento;
		public String IdTipoInstrumentoInstrumentoAsociado;
		public String NemoTecnicoInstrumentoAsociado;
		public String IdMonedaInstrumentoAsociado;
		public String IdEntidadClasificadoraRiesgo;
		public String ClasificacionRiesgo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjOrdenPortafolioClasificacionRiesgo{
		public Long IdOrden;
		public Long IdInstrumento;
		public BigDecimal CantidadDisponibleOrden  ;
		public BigDecimal PrecioMercado;
		public BigDecimal Monto;

		public String IdTipoInstrumentoInstrumentoAsociado;
		public String NemoTecnicoInstrumentoAsociado;
		public String IdMonedaInstrumentoAsociado;
		public String IdEntidadClasificadoraRiesgo;
		public String ClasificacionRiesgo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCustodiaVigente {
		public Long IdInterno;
		public BigDecimal Monto;
		public Long IdInstrumento;
		public String IdEntidadClasificadoraRiesgo;
		public String ClasificacionRiesgo;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public String IdMoneda;
		public String MultiplicaPorParidad;
		public String TipoOpcion;
		public String AccionOperacion;
		public String Origen;
		public ObjCustodiaVigente() {
			this.MultiplicaPorParidad="N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		CalcularExigibilidadEntrada in=gson.fromJson(jin.toString() ,CalcularExigibilidadEntrada.class);
		CalcularExigibilidadSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CalcularExigibilidadSalida logica(Connection con, CalcularExigibilidadEntrada in) throws Exception {
		logger.info("INICIO::CalcularExigibilidadSalida.logica");
		
		CalcularExigibilidadSalida out = new CalcularExigibilidadSalida();
		
		String monedaSistema = obtenerMonedaSistema(con, in.p_IdEmpresa);
		String fechaSistema  = obtenerFechaSistema (con, in.p_IdEmpresa);
		BigDecimal MontoCoberturaEntidad = BigDecimal.ZERO;
		BigDecimal MontoCoberturaCorredora = BigDecimal.ZERO;
		HashMap<Long,ObjInstrumentoTipoInstrumento> hashInstrumentoTipoInstrumento = new HashMap<Long,ObjInstrumentoTipoInstrumento>();
		List<ObjOrdenPortafolioClasificacionRiesgo> ordPortafolioClasificacionRiesgo = new ArrayList<>();
		List<Long> listaInstrumentosPadre = new ArrayList<Long>();
		List<Long> listaInstrumentosSimulacion = new ArrayList<Long>();
		
		listaInstrumentosSimulacion = in.p_TableExigibilidadSimulacion.stream().map(ins->ins.IdInstrumento).collect(Collectors.toList());
		listaInstrumentosPadre.add(in.p_IdInstrumento);
			
		hashInstrumentoTipoInstrumento =  this.obtenerInstrumentosGrupoInstrumento(con,listaInstrumentosSimulacion);
		HashMap<String,ObjExigibilidad> hashExigibilidad = this.obtenerExigibilidad(con, in, listaInstrumentosPadre, listaInstrumentosSimulacion);
		
		this.crearTablaExigibilidadSimulacion(out, in.p_TableExigibilidadSimulacion, hashInstrumentoTipoInstrumento, hashExigibilidad, in); 
	
		//Se Obtiene Ordenes por portafolio
		List<ObjOrdenPortafolio> listaOrdenPortafolio = obtenerOrdenPortafolio(con,in,fechaSistema);
		if(!listaOrdenPortafolio.isEmpty()) {
			HashMap<Long,ObjOrdenClasificacionRiesgo>  hashOrdenClasificacion = obtenetOrdenClasificacionRiesgo(con,listaOrdenPortafolio);
			ordPortafolioClasificacionRiesgo=ObtenerOrdenPortafolioClasificacionRiesgo(hashOrdenClasificacion,listaOrdenPortafolio);
		}
		
		//Se obtiene informacion desde la cartera
		List<ObjCustodiaVigente> listaCustodiaVigente= obtenerCustodiaVigente(con,in);
		List<ObjCustodiaVigente> listaExigibilidadOrdenCustodia = crearListaExigibilidad(listaCustodiaVigente,ordPortafolioClasificacionRiesgo);
		
		//calculo de paridad de tabla exigibilidad
		CalcularParidadSalida calcularParidad  = calcularParidad(con,in,listaExigibilidadOrdenCustodia,fechaSistema,monedaSistema);
		HashMap<Long,Object> hashParidad =Utils.List2Hash(calcularParidad.p_out_CalculosSalida, "IdInterno");
		
		List<Long> listaInstrumentoOrdenes = listaExigibilidadOrdenCustodia.stream().map(ins->ins.IdInterno).collect(Collectors.toList());
		List<Long> listaOrdenada = new ArrayList<Long>();
		for (ObjCustodiaVigente asdf : listaExigibilidadOrdenCustodia) {
			if (!listaOrdenada.contains(asdf.IdInstrumento)) listaOrdenada.add(asdf.IdInstrumento);
		}
		if (tiposOpcion.contains(in.p_IdTipoInstrumento)) {
			CalcularCoberturaOrdenes calcularCoberturaOrdenes = new CalcularCoberturaOrdenes();
			CalcularCoberturaOrdenes.EntradaCoberturasOrdenes entradaCoberturasOrdenes = new CalcularCoberturaOrdenes.EntradaCoberturasOrdenes();
			entradaCoberturasOrdenes.p_IdEmpresa = in.p_IdEmpresa;
			entradaCoberturasOrdenes.p_IdUsuario = in.p_IdUsuario;
			entradaCoberturasOrdenes.p_IdModulo = in.p_IdModulo;
			entradaCoberturasOrdenes.p_EsVentaCorta = "N";
			entradaCoberturasOrdenes.p_IdBolsa = in.p_IdBolsa;
			entradaCoberturasOrdenes.p_IdInstrumento = 0L;
			entradaCoberturasOrdenes.p_IdMoneda = in.p_IdMoneda;
			entradaCoberturasOrdenes.p_IdMonedaOrigen = in.p_IdMoneda;
			entradaCoberturasOrdenes.p_IdPortafolio = in.p_IdPortafolio;
			entradaCoberturasOrdenes.p_IdTipoInstrumento = "DER";
			entradaCoberturasOrdenes.p_Simulacion = new ArrayList<CalcularCoberturaOrdenes.InCoberturaSimulacion>();
			CalcularCoberturaOrdenes.SalidaCoberturasOrdenes salidaCobertura = calcularCoberturaOrdenes.logica(con, entradaCoberturasOrdenes);
			for (CalcularCoberturaOrdenes.OutCoberturaPortafolio cobertura : salidaCobertura.p_out_TableCoberturaPortafolio) {
				MontoCoberturaEntidad = MontoCoberturaEntidad.add(cobertura.MontoCoberturaEntidad);
				MontoCoberturaCorredora = MontoCoberturaCorredora.add(cobertura.MontoCoberturaCorredora);
			}
		}
		if(!listaInstrumentoOrdenes.isEmpty()) {
			HashMap<Long,ObjInstrumentoTipoInstrumento> hashIntrumentoTipoIntrumentoOrd =  this.obtenerInstrumentosGrupoInstrumento(con,listaOrdenada);
			HashMap<String,ObjExigibilidad> hashExigibilidadOrd = this.obtenerExigibilidad(con, in, listaInstrumentosPadre, listaOrdenada);
			
			this.crearTablainsgibilidadPortafolio(out, in, listaExigibilidadOrdenCustodia, hashParidad, hashIntrumentoTipoIntrumentoOrd, hashExigibilidadOrd, MontoCoberturaEntidad, MontoCoberturaCorredora);
		}
		
		
		logger.info("FIN::CalcularExigibilidadSalida.logica");
		return out;
	}
	
	private void crearTablainsgibilidadPortafolio (CalcularExigibilidadSalida out,CalcularExigibilidadEntrada in,List<ObjCustodiaVigente> listaExigibilidadAgrupada,HashMap<Long,Object> hashParidad,HashMap<Long,ObjInstrumentoTipoInstrumento> hashIntrumenoGrupoInstrumento,HashMap<String,ObjExigibilidad> hashExigibilidadOrd, BigDecimal montoCoberturaEntidad, BigDecimal montoCoberturaCorredora ) {
		BigDecimal MontoExigibilidadEntidad = BigDecimal.ZERO;
		BigDecimal MontoExigibilidadCorredora = BigDecimal.ZERO;
		BigDecimal MontoExigibilidadEntidadVentaCall = BigDecimal.ZERO;
		BigDecimal MontoExigibilidadCorredoraVentaCall = BigDecimal.ZERO;
		ObjExigibilidadPortafolio objOrd = new ObjExigibilidadPortafolio();
		
		for (ObjCustodiaVigente ord : listaExigibilidadAgrupada) {
			CalculoSalida  paridad = (CalculoSalida)hashParidad.get(ord.IdInterno);
			ObjInstrumentoTipoInstrumento objIns = hashIntrumenoGrupoInstrumento.get(ord.IdInstrumento);
			if (objIns != null) {
				String key = in.p_IdBolsa+in.p_IdTipoInstrumento+ord.IdTipoInstrumento+objIns.ClasificacionRiesgo+objIns.IdEntidadClasificadoraRiesgo+objIns.IdTipoClasificacionRiesgo+ (in.p_IdTipoInstrumento.equalsIgnoreCase("DER") ? objIns.IdInstrumento : in.p_IdInstrumento);
				
				if(hashExigibilidadOrd.get(key)!=null) {
					ObjExigibilidad temp =hashExigibilidadOrd.get(key);
					if (ord.TipoOpcion.equalsIgnoreCase("CALL") && ord.AccionOperacion.equalsIgnoreCase("VENTA") && ord.Origen.equalsIgnoreCase("CUSTODIA")) {
						MontoExigibilidadEntidadVentaCall = MontoExigibilidadEntidadVentaCall.add(paridad.MontoLocal.multiply(temp.ExigibilidadEntidad.divide(new BigDecimal(100),MathContext.DECIMAL128)));
						MontoExigibilidadCorredoraVentaCall = MontoExigibilidadCorredoraVentaCall.add(paridad.MontoLocal.multiply(temp.ExigibilidadCorredora.divide(new BigDecimal(100),MathContext.DECIMAL128)));
					} else {
						MontoExigibilidadEntidad = MontoExigibilidadEntidad.add(paridad.MontoLocal.multiply(temp.ExigibilidadEntidad.divide(new BigDecimal(100),MathContext.DECIMAL128)));
						MontoExigibilidadCorredora = MontoExigibilidadCorredora.add(paridad.MontoLocal.multiply(temp.ExigibilidadCorredora.divide(new BigDecimal(100),MathContext.DECIMAL128)));
					}
				} else {
					if (ord.TipoOpcion.equalsIgnoreCase("CALL") && ord.AccionOperacion.equalsIgnoreCase("VENTA") && ord.Origen.equalsIgnoreCase("CUSTODIA")) {
						MontoExigibilidadEntidadVentaCall = MontoExigibilidadEntidadVentaCall.add(paridad.MontoLocal);
						MontoExigibilidadCorredoraVentaCall = MontoExigibilidadCorredoraVentaCall.add(paridad.MontoLocal);
					} else {
						MontoExigibilidadEntidad = MontoExigibilidadEntidad.add(paridad.MontoLocal);
						MontoExigibilidadCorredora = MontoExigibilidadCorredora.add(paridad.MontoLocal);
					}
				}
			} else {
				if (ord.TipoOpcion.equalsIgnoreCase("CALL") && ord.AccionOperacion.equalsIgnoreCase("VENTA") && ord.Origen.equalsIgnoreCase("CUSTODIA")) {
					MontoExigibilidadEntidadVentaCall = MontoExigibilidadEntidadVentaCall.add(paridad.MontoLocal);
					MontoExigibilidadCorredoraVentaCall = MontoExigibilidadCorredoraVentaCall.add(paridad.MontoLocal);
				} else {
					MontoExigibilidadEntidad = MontoExigibilidadEntidad.add(paridad.MontoLocal);
					MontoExigibilidadCorredora = MontoExigibilidadCorredora.add(paridad.MontoLocal);
				}
			}
		};
		
		if (montoCoberturaEntidad.compareTo(MontoExigibilidadEntidadVentaCall) == 0) {
			objOrd.MontoExigibilidadEntidad = MontoExigibilidadEntidad;
		}
		if (montoCoberturaEntidad.compareTo(MontoExigibilidadEntidadVentaCall) == 1) {
			objOrd.MontoExigibilidadEntidad = MontoExigibilidadEntidad;
		}
		if (montoCoberturaEntidad.compareTo(MontoExigibilidadEntidadVentaCall) == -1) {
			objOrd.MontoExigibilidadEntidad = (MontoExigibilidadEntidadVentaCall.subtract(montoCoberturaEntidad)).add(MontoExigibilidadEntidad);
		}
		
		if (montoCoberturaCorredora.compareTo(MontoExigibilidadCorredoraVentaCall) == 0) {
			objOrd.MontoExigibilidadCorredora = MontoExigibilidadCorredora;
		}
		if (montoCoberturaCorredora.compareTo(MontoExigibilidadCorredoraVentaCall) == 1) {
			objOrd.MontoExigibilidadCorredora = MontoExigibilidadCorredora;
		}
		if (montoCoberturaCorredora.compareTo(MontoExigibilidadCorredoraVentaCall) == -1) {
			objOrd.MontoExigibilidadCorredora = (MontoExigibilidadCorredoraVentaCall.subtract(montoCoberturaEntidad)).add(MontoExigibilidadCorredora);
		}
		out.P_OUT_TABLEEXIGIBILIDADPORTAFOLIO.add(objOrd);
	}
	
	private void crearTablaExigibilidadSimulacion (CalcularExigibilidadSalida out,List<ObjExigibilidadEntrada>  p_TableExigibilidadSimulacion,  HashMap<Long,ObjInstrumentoTipoInstrumento> hashIntrumenoGrupoInstrumento,HashMap<String,ObjExigibilidad> hashExigibilidad, CalcularExigibilidadEntrada entrada ) {
		logger.debug("INICIO::CalcularExigibilidadSalida.crearTablainsgibilidadSimulacion");
		//no se usa calcular paridad por que los montos ya vienen en moneda local.
		p_TableExigibilidadSimulacion.stream().forEach(sim ->{
			ObjExigibilidadSimulacion objSim = new ObjExigibilidadSimulacion();
			String key = crearkey(hashIntrumenoGrupoInstrumento.get(sim.IdInstrumento),sim, entrada);
			System.out.println(key);
			objSim.IdTipoInstrumento 					  = sim.IdTipoInstrumento;
			objSim.IdInstrumento                          = sim.IdInstrumento;
			objSim.Cantidad                               = sim.Cantidad;
			objSim.Precio                                 = sim.Precio;
			objSim.Monto                                  = sim.Monto;
			objSim.IdBolsa                                = sim.IdBolsa;
			
			if(hashExigibilidad.get(key)!=null) {
				ObjExigibilidad temp =hashExigibilidad.get(key);
				objSim.MontoExigibilidadEntidad               = sim.Monto.multiply(temp.ExigibilidadEntidad.divide(new BigDecimal(100),MathContext.DECIMAL128));
				objSim.MontoExigibilidadCorredora             = sim.Monto.multiply(temp.ExigibilidadCorredora.divide(new BigDecimal(100),MathContext.DECIMAL128));
				objSim.PorcentajeExigibilidadEntidad          = temp.ExigibilidadEntidad;
				objSim.PorcentajeExigibilidadCorredora        = temp.ExigibilidadCorredora;
			}else {
				objSim.MontoExigibilidadEntidad               = sim.Monto.multiply(BigDecimal.ONE);
				objSim.MontoExigibilidadCorredora             = sim.Monto.multiply(BigDecimal.ONE);
			}
			out.P_OUT_TABLEEXIGIBILIDADSIMULACION.add(objSim);
		});
		logger.debug("FIN::CalcularExigibilidadSalida.crearTablainsgibilidadSimulacion");
	}
	private List<ObjCustodiaVigente> crearListaExigibilidad(List<ObjCustodiaVigente> custodia,List<ObjOrdenPortafolioClasificacionRiesgo>  orden){
		
		List<ObjCustodiaVigente> listaExigibilida = new ArrayList<>();
		AtomicLong i = new AtomicLong(0L);
		
		orden.parallelStream().forEach(ord ->{
			ObjCustodiaVigente cus = new ObjCustodiaVigente();
			cus.IdInterno = i.getAndIncrement();
			cus.Monto=ord.Monto;
			cus.IdInstrumento= ord.IdInstrumento;
			cus.IdEntidadClasificadoraRiesgo=ord.IdEntidadClasificadoraRiesgo;
			cus.ClasificacionRiesgo=ord.ClasificacionRiesgo;
			cus.IdTipoInstrumento=ord.IdTipoInstrumentoInstrumentoAsociado;
			cus.Nemotecnico=ord.NemoTecnicoInstrumentoAsociado;
			cus.IdMoneda=ord.IdMonedaInstrumentoAsociado;
			cus.MultiplicaPorParidad="S";
			cus.AccionOperacion="";
			cus.TipoOpcion="";
			cus.Origen="TRANSITO";
			listaExigibilida.add(cus);
		});
		
		custodia.parallelStream().forEach(cu ->{
			ObjCustodiaVigente cus = new ObjCustodiaVigente();
			cus.IdInterno = i.getAndIncrement();
			cus.Monto=cu.Monto;
			cus.IdInstrumento= cu.IdInstrumento;
			cus.IdEntidadClasificadoraRiesgo=cu.IdEntidadClasificadoraRiesgo;
			cus.ClasificacionRiesgo=cu.ClasificacionRiesgo;
			cus.IdTipoInstrumento=cu.IdTipoInstrumento;
			cus.Nemotecnico=cu.Nemotecnico;
			cus.IdMoneda=cu.IdMoneda;
			cus.MultiplicaPorParidad="S";
			cus.Origen=cu.Origen;
			cus.TipoOpcion=cu.TipoOpcion;
			cus.AccionOperacion=cu.AccionOperacion;
			listaExigibilida.add(cus);
		});
		
		return listaExigibilida;
	}
	
	private List<ObjCustodiaVigente> obtenerCustodiaVigente (Connection con,CalcularExigibilidadEntrada in) throws Exception {
		SQL sql = new SQL();	
		HashMap<String,Object> params=new HashMap<>();
		List<ObjCustodiaVigente> listaCustodia = new ArrayList<ObjCustodiaVigente>();
		params.put("p_IdBolsa", in.p_IdBolsa);
		params.put("p_IdPortafolio", in.p_IdPortafolio);
		if (in.p_IdTipoInstrumento.equalsIgnoreCase("PRT") || in.p_IdTipoInstrumento.equalsIgnoreCase("EMP") || in.p_IdTipoInstrumento.equalsIgnoreCase("SIM")) {
			params.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
			params.put("p_IdInstrumento", in.p_IdInstrumento);
			listaCustodia =  sql.query(con, SQL_Select_CustodiaVigente, params, ObjCustodiaVigente.class);
		} else {
			listaCustodia =  sql.query(con, SQL_Select_CustodiaVigenteDerivados, params, ObjCustodiaVigente.class);
		}
		
		return listaCustodia;
	}
	
	private List<ObjOrdenPortafolioClasificacionRiesgo> ObtenerOrdenPortafolioClasificacionRiesgo(HashMap<Long,ObjOrdenClasificacionRiesgo>  hashOrdenClasificacion,List<ObjOrdenPortafolio> listaOrdenPortafolio) {
		List<ObjOrdenPortafolioClasificacionRiesgo> ordPortafolioClasificacionRiesgo  = (List<ObjOrdenPortafolioClasificacionRiesgo>) listaOrdenPortafolio.stream().map(ord -> {
			ObjOrdenPortafolioClasificacionRiesgo obj = new ObjOrdenPortafolioClasificacionRiesgo();
			ObjOrdenClasificacionRiesgo temp =hashOrdenClasificacion.get(ord.IdInstrumento);
			
			obj.IdOrden = ord.IdOrden;
			obj.IdInstrumento = ord.IdInstrumento;
			obj.CantidadDisponibleOrden = ord.CantidadDisponibleOrden;
			obj.PrecioMercado 			= ord.PrecioMercado;
			obj.Monto 					=ord.Monto;
			obj.IdEntidadClasificadoraRiesgo=temp.IdEntidadClasificadoraRiesgo;
			obj.ClasificacionRiesgo = temp.ClasificacionRiesgo;
			obj.IdTipoInstrumentoInstrumentoAsociado = temp.IdTipoInstrumentoInstrumentoAsociado;
			obj.NemoTecnicoInstrumentoAsociado = temp.NemoTecnicoInstrumentoAsociado;
			obj.IdMonedaInstrumentoAsociado = temp.IdMonedaInstrumentoAsociado;
			
			return obj;
			
		}).collect(Collectors.toList());
		
		
		return ordPortafolioClasificacionRiesgo;
	}
	
	private String crearkey (ObjInstrumentoTipoInstrumento ins,ObjExigibilidadEntrada ent, CalcularExigibilidadEntrada entradaCalcularExigibilidad) {
		return ent.IdBolsa+ent.IdTipoInstrumento+ins.IdTipoInstrumento+ins.ClasificacionRiesgo+ins.IdEntidadClasificadoraRiesgo+ins.IdTipoClasificacionRiesgo+entradaCalcularExigibilidad.p_IdInstrumento; 
	}
	private HashMap<Long,ObjOrdenClasificacionRiesgo> obtenetOrdenClasificacionRiesgo(Connection con ,List<ObjOrdenPortafolio> listaOrden) throws Exception{
		logger.debug("INICIO::CalcularExigibilidadSalida.obtenerOrdenPortafolio");
		SQL sql = new SQL();	
		HashMap<String,Object> params=new HashMap<>();
		HashMap<Long,ObjOrdenClasificacionRiesgo> hashSalida= new HashMap<>();
		List<Long> listaInstrumento =listaOrden.stream().map(ins->ins.IdInstrumento).collect(Collectors.toList());
		params.put("IdInstrumento", listaInstrumento);
		List<ObjOrdenClasificacionRiesgo> listaOrdenClasificacionRiesgo = sql.query(con, SQL_Select_OrdenesClasificacionRiesgo, params, ObjOrdenClasificacionRiesgo.class);
		listaOrdenClasificacionRiesgo.parallelStream().forEach(ins->hashSalida.put(ins.IdInstrumento, ins));
		return hashSalida;
	}
	
	private List<ObjOrdenPortafolio> obtenerOrdenPortafolio(Connection con,CalcularExigibilidadEntrada in,String fechaSistema) throws Exception{
		logger.debug("INICIO::CalcularExigibilidadSalida.obtenerOrdenPortafolio");
		SQL sql = new SQL();		
		HashMap<String,Object> params=new HashMap<>();
		params.put("p_IdPortafolio", in.p_IdPortafolio);
		params.put("p_IdBolsa", 	 in.p_IdBolsa);
		params.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
		params.put("p_IdInstrumento", in.p_IdInstrumento);
		params.put("fechaSistema", fechaSistema);
		List<ObjOrdenPortafolio> listaOrdenPortafolio = sql.query(con, SQL_Select_OrdenesPortafolios, params, ObjOrdenPortafolio.class);
		logger.debug("FIN::CalcularExigibilidadSalida.obtenerOrdenPortafolio");
		return listaOrdenPortafolio;
	}
	
	private HashMap<Long,ObjInstrumentoTipoInstrumento> obtenerInstrumentosGrupoInstrumento(Connection con,List<Long> listaInstrumentoEntrada) throws Exception {
		logger.debug("INICIO::CalcularExigibilidadSalida.obtenerInstrumentos");
		SQL sql = new SQL();		
		HashMap<String,Object> params=new HashMap<>();
		params.put("IdInstrumento", listaInstrumentoEntrada);
		HashMap<Long,ObjInstrumentoTipoInstrumento> hashSalida =new  HashMap<Long,ObjInstrumentoTipoInstrumento>();
		
		List<ObjInstrumentoTipoInstrumento> consultaInstrumento = sql .query(con, SQL_Select_InstrumentosGrupoTipoInstrumento, params, ObjInstrumentoTipoInstrumento.class);
		consultaInstrumento.parallelStream().forEach(ins->hashSalida.put(ins.IdInstrumento, ins));
		
		logger.debug("FIN::CalcularExigibilidadSalida.obtenerInstrumentos");
		return hashSalida;
	}
	
	private HashMap<String,ObjExigibilidad> obtenerExigibilidad(Connection con, CalcularExigibilidadEntrada entrada, List<Long> listaInstrumentosPadre, List<Long> listaInstrumentosSimulacion) throws Exception {
		SQL sql = new SQL();		
		HashMap<String,Object> params=new HashMap<>();
		HashMap<String,ObjExigibilidad> hashSalida= new HashMap<>();
		if (entrada.p_IdTipoInstrumento.equalsIgnoreCase("SIM")) {
			params.put("IdInstrumento", listaInstrumentosPadre);
		} else {
			params.put("IdInstrumento", listaInstrumentosSimulacion);
		}
		List<ObjExigibilidad> consultaExigibilidad = sql .query(con, SQL_Select_Exigibilidad, params, ObjExigibilidad.class);
		consultaExigibilidad.parallelStream().forEach(exi->{
			String key = exi.IdBolsa+exi.IdTipoInstrumento+exi.IdTipoInstrumentoSubyacente+exi.ClasificacionRiesgo+exi.IdEntidadClasificadoraRiesgo+exi.IdTipoClasificacionRiesgo+exi.IdInstrumento;		
			hashSalida.put(key, exi);
		});
		return hashSalida;
	}
	
	
	
	private CalcularParidadSalida calcularParidad(Connection con ,CalcularExigibilidadEntrada in ,List<ObjCustodiaVigente> listaExigibilida, String fechaSistema,String MonedaSistema) throws Exception {
		logger.debug("INICIO::CalcularExigibilidadSalida.calcularParidad");
		CalcularParidad paridad = new CalcularParidad();
		CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
		paridadEntrada.p_IdEmpresa=in.p_IdEmpresa;
		paridadEntrada.p_IdTrabajo=1L;
		paridadEntrada.p_CalculosEntrada= listaExigibilida.parallelStream().map(ins->{
			CalculoEntrada cal = new CalculoEntrada();
			cal.IdInterno= ins.IdInterno;
			cal.Fecha    = fechaSistema;
			cal.IdMonedaOrigen =ins.IdMoneda;
			cal.IdMonedaDestino =MonedaSistema;
			cal.MontoOrigen =ins.Monto;
			cal.IdUso="";
			return cal;			
		}).collect(Collectors.toList());
		
		CalcularParidadSalida salidaParidad = paridad.logica(con, paridadEntrada);
		logger.debug("FIN::CalcularExigibilidadSalida.calcularParidad");
		return salidaParidad;
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::CalcularExigibilidadSalida.obtenerMonedaSistema");
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		logger.info("INICIO::CalcularExigibilidadSalida.obtenerMonedaSistema");
		return idMonedaSistema;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::CalcularExigibilidadSalida.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.FechaSistema");
		logger.info("FIN::CalcularExigibilidadSalida.obtenerFechaSistema");
		return fechaSistema;
	}
	
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_TableExigibilidadSimulacion\":[{\"IdInterno\":0,\"IdTipoInstrumento\":\"DER\",\"IdInstrumento\":476760,\"Cantidad\":0,\"Precio\":0,\"Monto\":6344000,\"IdBolsa\":\"BCS\"}],\"p_EsVentaCorta\":\"N\",\"p_IdTipoInstrumento\":\"IOA\",\"p_IdPortafolio\":27499,\"p_IdBolsa\":\"BCS\",\"p_IdInstrumento\":476760,\"p_IdMoneda\":\"CLP\"}";
			CalcularExigibilidadEntrada entrada = gson.fromJson(json, CalcularExigibilidadEntrada.class);
			CalcularExigibilidad demo = new CalcularExigibilidad();
			
			System.out.println(demo.logica(con, entrada));
			con.rollback();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}
