package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.ResultadoConsulta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCarterasWS implements JavaView{
	
	private final Logger logger =  LogManager.getLogger(ConsultarCarterasWS.class.getName());
	private final String SQL_SELECT_CARTERA_UI 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraUI_WS.sql");
	private final String SQL_SELECT_CARTERA_UI_HIS 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraUI_HIS_WS.sql");
	private final String SQL_SELECT_CARTERA_FWD_UI 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraFwdUI_WS.sql");
	private final String SQL_SELECT_CARTERA_FWD_UI_HIS 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarCarteraFwdUI_HIS_WS.sql");
	
	private final String SQL_SELECT_MESA_FORWARD 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteras_ConsultarMesaForward.sql");

	protected Integer skip;
	protected Integer take;
	protected Long total;
	protected String sort;
	
	public static class ConsultarCarteraWSIn {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo; 
		public String p_FechaConsulta;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Long p_IdInstrumento;
		public String p_Nemotecnico;
		public String p_IdMoneda;
		public String p_tipoFecha;
		public Long p_FolioCartera;
		public String p_IdEstado;
		public Long p_IdProductoAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ConsultarCarterasWSOut {
		public List<Tabla_Salida> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ConsultarCarterasWSOut() {
			this.P_OUT_TABLA = new ArrayList<Tabla_Salida>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Cartera {
		public Long RANKG;
	    public Long IdCartera;
	    public Date FechaOperacion;
	    public String IdEstado;
	    public String IdClasificacionTipoInstrumento;
	    public String IdTipoInstrumento;
	    public String Nemotecnico;
	    public String IdMonedaEmision;
	    public BigDecimal INGCantidad; 
	    public BigDecimal INGPrecioCompra;
	    public BigDecimal INGPrecioMercadoActual;
	    public BigDecimal Rentabilidad;
	    public BigDecimal INGMontoMercadoActualMonedaInstrumento;
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Tabla_Salida{
		public Long IdCartera;
	    public Date FechaOperacion;
	    public String IdEstado;
	    public String IdClasificacionTipoInstrumento;
	    public String IdTipoInstrumento;
	    public String Nemotecnico;
	    public String IdMonedaEmision;
	    public BigDecimal INGCantidad;
	    public BigDecimal INGPrecioCompra;
	    public BigDecimal INGPrecioMercadoActual;
	    public BigDecimal Rentabilidad;
	    public BigDecimal INGMontoMercadoActualMonedaInstrumento;
	    
	    public Tabla_Salida(){
		    this.INGCantidad = BigDecimal.ZERO;
		    this.INGPrecioCompra = BigDecimal.ZERO;
		    this.INGPrecioMercadoActual = BigDecimal.ZERO;
		    this.Rentabilidad = BigDecimal.ZERO;
		    this.INGMontoMercadoActualMonedaInstrumento = BigDecimal.ZERO;
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	private Tabla_Salida copyData(Cartera car){
		Tabla_Salida tbl = new Tabla_Salida();
		tbl.IdCartera		= car.IdCartera;
		tbl.FechaOperacion	= car.FechaOperacion;
		tbl.IdEstado			= car.IdEstado;
		tbl.IdClasificacionTipoInstrumento	= car.IdClasificacionTipoInstrumento;
		tbl.IdTipoInstrumento				= car.IdTipoInstrumento;
		tbl.Nemotecnico				= car.Nemotecnico;
		tbl.IdMonedaEmision			= car.IdMonedaEmision;
		tbl.INGCantidad				= car.INGCantidad;
		tbl.INGPrecioCompra			= car.INGPrecioCompra;
		tbl.INGPrecioMercadoActual	= car.INGPrecioMercadoActual;
		tbl.Rentabilidad			= car.Rentabilidad;
		tbl.INGMontoMercadoActualMonedaInstrumento = car.INGMontoMercadoActualMonedaInstrumento;
		return tbl;
	}
	
	
	public static class Mesa{		
		public Long 	IdMesa;
		public Long 	IdCliente;			
		public String 	DocumentoIdentidad;
		public Long 	IdPortafolio;
		public String 	CodigoPortafolio;
			
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarCarterasWSOut logica (Connection conn, ConsultarCarteraWSIn entrada) throws Exception {
		logger.info("INICIO::ConsultarCarterasWS");
		SQL sql=new SQL();
		ConsultarCarterasWSOut salida = new ConsultarCarterasWSOut();
		logger.debug("1");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.debug("2");
		

		
		//Consultamos si el portafolio consultado corresponde a la mesa forward para incluir las carteras forward
		HashMap<String,Object> paramsMesaForward = new HashMap<String,Object>();
		paramsMesaForward.put("IdEmpresa", entrada.p_IdEmpresa);
		paramsMesaForward.put("IdPortafolio", entrada.p_IdPortafolio);	
		List<Mesa> v_MesaForward = new ArrayList<>();
		
		logger.debug("3");
		
		if("FWD".equals(entrada.p_IdTipoInstrumento)) {
			v_MesaForward = sql.query(conn, SQL_SELECT_MESA_FORWARD, paramsMesaForward, Mesa.class);
		}
		if (v_MesaForward.size() > 0) {
			paramsMesaForward.clear();
			paramsMesaForward.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramsMesaForward.put("p_IdEstado", entrada.p_IdEstado);
			paramsMesaForward.put("p_IdCliente", entrada.p_IdCliente);
			paramsMesaForward.put("p_IdInstrumento", entrada.p_IdInstrumento);
			paramsMesaForward.put("p_Nemotecnico", entrada.p_Nemotecnico);
			paramsMesaForward.put("p_FolioCartera", entrada.p_FolioCartera);
			paramsMesaForward.put("p_tipoFecha", entrada.p_tipoFecha);
			paramsMesaForward.put("p_FechaConsulta", entrada.p_FechaConsulta);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEstado", entrada.p_IdEstado);
		params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_IdPortafolio", entrada.p_IdPortafolio);
		params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		params.put("p_IdTipoInstrumento", entrada.p_IdTipoInstrumento);
		params.put("p_IdInstrumento", entrada.p_IdInstrumento);
		params.put("p_Nemotecnico", entrada.p_Nemotecnico);
		params.put("p_FolioCartera", entrada.p_FolioCartera);
		params.put("p_tipoFecha", entrada.p_tipoFecha);
		params.put("p_FechaConsulta", entrada.p_FechaConsulta);
		
		HashMap<String,Object> paramsHis = new HashMap<String,Object>();
		paramsHis.putAll(params);
		paramsHis.put("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
		
		List<Cartera> carteras = new ArrayList<Cartera>();
		List<Cartera> carterasFWD = new ArrayList<Cartera>();
		//select Consulta cartera
		logger.debug("4");
		if(Fecha.toDate(entrada.p_FechaConsulta).compareTo(Fecha.toDate(fechaSistema)) != -1){
			logger.debug("4.0");//insert filtrando por fecha
			params.put("p_ExcluyeVigentes", 'N');
			this.total= sql.countQuery(conn, SQL_SELECT_CARTERA_UI, params);
						
			carteras = sql.query(conn, SQL_SELECT_CARTERA_UI, params, Cartera.class, this.take, this.skip, this.sort);
			
			
			if (v_MesaForward.size() > 0) {
				logger.debug("4.1.1");
				carterasFWD = sql.query(conn, SQL_SELECT_CARTERA_FWD_UI, paramsMesaForward, Cartera.class);
				carteras.addAll(carterasFWD);
			}
		}
		//select Consulta cartera historica
		else {
			logger.debug("4.2");
			this.total= sql.countQuery(conn, SQL_SELECT_CARTERA_UI_HIS, paramsHis);
			
			carteras = sql.query(conn, SQL_SELECT_CARTERA_UI_HIS, paramsHis, Cartera.class, this.take, this.skip, this.sort);
			
			/**** OBTIENE CARTERAS LIQUIDADAS Y VENCIDAS A LA FECHA ****/

			List<Cartera> carterasVCDLIQ = new ArrayList<Cartera>();
			params.put("p_ExcluyeVigentes", 'S');
			long totalLiqVcd = sql.countQuery(conn, SQL_SELECT_CARTERA_UI, params);
			this.total = this.total+totalLiqVcd;
			
			carterasVCDLIQ = sql.query(conn, SQL_SELECT_CARTERA_UI, params, Cartera.class, this.take, this.skip, this.sort);
			carteras.addAll(carterasVCDLIQ);
			
			
			if (v_MesaForward.size() > 0) {
				logger.debug("4.2.1");
				carterasFWD = sql.query(conn, SQL_SELECT_CARTERA_FWD_UI_HIS, paramsMesaForward, Cartera.class);
				carteras.addAll(carterasFWD);
			}
		}
		
	
		salida.P_OUT_TABLA = carteras.parallelStream().map(car -> copyData(car)).collect(Collectors.toList());
		
		logger.debug("FIN::ConsultarCarterasWS size() : " + salida.P_OUT_TABLA.size());
		logger.info("FIN::ConsultarCarterasWS");
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin, Integer skip, Integer take, String sort, String filters) throws Exception {
		//implementacion basica de limit y offset
		this.skip = skip;
		this.take = take;
		this.sort = sort;
		
		Gson gson = Utils.getGsonBuilder();	
		ConsultarCarteraWSIn in =gson.fromJson(jin.toString() ,ConsultarCarteraWSIn.class);
		ConsultarCarterasWSOut out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        dataout.add("total", new JsonPrimitive(this.total));
		return dataout;
	}

	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();
			
			String json = 	"{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_tipoFecha\":\"ADQ\",\"p_FechaConsulta\":\"2024-12-19\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_Nemotecnico\":\"COPEC\",\"p_FolioCartera\":0,\"p_IdMoneda\":\"\",\"p_IdEstado\":\"VIG\",\"p_IdProductoAgrupador\":0,\"p_IdCliente\":61625}";

			ConsultarCarteraWSIn entrada = gson.fromJson(json, ConsultarCarteraWSIn.class);
			ConsultarCarterasWS demo = new ConsultarCarterasWS();
			demo.skip = 0;
			demo.take = 200;
			demo.sort = "IdCartera::desc";
			long init = System.currentTimeMillis();
			ConsultarCarterasWSOut salida = demo.logica(con, entrada);
			System.out.println(salida);
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));
			System.out.println(salida.P_OUT_TABLA.size());
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}
