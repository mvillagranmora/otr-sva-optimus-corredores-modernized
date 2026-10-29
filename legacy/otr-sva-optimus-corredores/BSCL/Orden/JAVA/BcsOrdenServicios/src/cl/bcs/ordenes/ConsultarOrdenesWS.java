package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


//import cl.bcs.ordenes.ConsultarOrdenes.ConsultarOrdenes_Entrada;
//import cl.bcs.ordenes.ConsultarOrdenes.ConsultarOrdenes_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.StatsD;


public class ConsultarOrdenesWS implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarOrdenesWS.class);
	
	private final String SQL_SELECT_ORDENES_WS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesWS_SelectOrdenes.sql");
	private final String SQL_SELECT_ORDENES_COBROS_WS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesWS_SelectOrdenesCobros.sql");
	private final String SQL_SELECT_ORDENES_FORMAS_DE_PAGO_WS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesWS_SelectOrdenesFormasDePago.sql");
	
	private final String SQL_SELECT_ORDENES_VALIDA_TIPOINSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesWS_SelectTipoInstrumento.sql");
	private final String SQL_SELECT_ORDENES_VALIDA_CANAL_INGRESO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesWS_SelectCanalIngreso.sql");
	
	public static final String METRIC_PREFIX = "ordenes.consultar_ordenes_ws";
	public static final String METRIC_TIEMPO_EJECUCION = METRIC_PREFIX + "." + "tiempo_ejecucion";
	public static final String METRIC_VOLUMEN_DATOS = METRIC_PREFIX + "." + "volumen_datos";
	
	public static class Clientes {
        public String DocumentoIdentidad;
        public Long IdPortafolio;
        public String Instrumento;
        public Long IdOrden;        
        public String IdEstadoOrden;
        public String CodigoPortafolio;        
        public String FechaInicioConsulta;
        public String FechaTerminoConsulta;
        public String IdMatrizAgrupador;
        public String EsADC; 
        public String InstrumentoNegociacion;
        public String GeneraSpot;
        public String IdTipoInstrumento;
        public String Canal;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarOrdenes_Entrada {		
		public Long IdEmpresa;
		public Long IdUsuario;
		public List<Clientes> Clientes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Ordenes {
		public Long IdInterno;
		public Long IdOrden;
		public String FolioOrden;
		public Date FechaRecepcion;
		public Date FechaIngresoRegistro;
		public Date FechaIngresoRegistroCorta;
		public Long IdCliente;
		public String NombreCompleto;
		public Long IdPortafolio;		
		public String CodigoPortafolio;	
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String Instrumento;
		public String DocumentoIdentidad;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdEstadoOrden;
		public String EstadoOrden;
		public String IdEstadoAsignacion;
		public String EstadoOrdenAsignacion;
		public String EsFungible;
		public String IdEmisor;
		public String EspecificaBolsa;
		public String IdBolsa;
		public String IdBolsaDistribucion;
		public BigDecimal CantidadOrden;
		public BigDecimal CantidadAsignadaOrden;
		public String EsSimultanea;
		public BigDecimal ValorRescate;
		public BigDecimal PrecioOrden;
		public Long Plazo;
		public BigDecimal Tasa;
		public String IdTipoPrecio;
		public String IdTipoTasa;
		public BigDecimal PrecioMercado;
		public String IdTipoReajuste;
		public String IdCondicionDeLiquidacion;
		public Date FechaLiquidacion;
		public String IdMonedaLiquidacionOrden;
		public BigDecimal MontoLiquidacion;
		public String IdMonedaOrden;
		public BigDecimal MontoOrden;
		public BigDecimal MontoLocal; 
		public BigDecimal TipoCambio;
		public Date FechaVencimiento;
		public String IdTipoVigenciaOrden;
		public Date FechaVigencia;
		public Date FechaOperacionOrden;
		public Date FechaSolicitudAnulacion;
		public Date FechaAnulacion;
		public Date FechaAceptacion;
		public Date FechaAutorizacion;
		public String GlosaOrden;
		public String IdTipoCanalIngreso;
		public String Canal;
		public Long IdLibroOrdenes;
		public String EsVentaCorta;
		public String IdTipoCanalDistribucion;
		public Long IdInstrumentoSubyacente;
		public String InstrumentoSubyacente;
		public String PorcentajeVisibilidad;
		public Long IdPorcentajeVisibilidad;
		public String IdTipoCustodia;
		public String IdFormaCustodia;		
		public Long IdEmpleadoPortafolio;
		public Long IdEmpleadoDigitador;
		public String CodigoClaseOrden;
		public Long IdMatrizAgrupador;
		public String InstrumentoNegociacion;
		public String GeneraSpot;
		//ORD."EsADC"
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobros {
		public Long IdInterno;
		public Long IdOrden;
		public String IdCobro;
		public String NombreCobro;
		public String CodigoCobroClasificacion;
		public BigDecimal ValorCobroPerfil;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public String FormaCobro;
		public BigDecimal MontoMinimo;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class FormasDePago {
		public Long IdInterno;
		public Long IdOrden;
		public String IdMedioDePago;
		public BigDecimal MontoPago;
		public Long IdBanco;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Ordenes_Salida{
		List<Ordenes> Ordenes;
		List<Cobros> Cobros;
		List<FormasDePago> FormasDePago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarOrdenes_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public Ordenes_Salida P_OUT_ORDENES;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoIntrumento extends BcsPojo {
		public String IdTipoInstrumento;
		public String Nombre;		
	}
	
	public static class CanalIngreso extends BcsPojo {
		public String IdTipoCanalIngreso;
		public String Nombre;		
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarOrdenes_Entrada in = gson.fromJson(jin.toString(), ConsultarOrdenes_Entrada.class);
		ConsultarOrdenes_Salida out = logica(conn, in);		
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}
	
	private List<Cobros> consultarCobros(Connection con, List<Long> idsOrdenes) throws SQLException {
		SQL sql = new SQL();
		List<Cobros> cobros = new ArrayList<Cobros>();		
		HashMap<String, Object> params =  new HashMap<String, Object>();
		for(Long IdOrden: idsOrdenes) {
			params.put("p_IdOrden", IdOrden);
			List<Cobros> cobro = sql.query(con, SQL_SELECT_ORDENES_COBROS_WS, params, Cobros.class);
			cobros.addAll(cobro);
		}
		return cobros;
	}
	
	private List<FormasDePago> consultarFormasDePago(Connection con, List<Long> idsOrdenes) throws SQLException {
		SQL sql = new SQL();
		List<FormasDePago> formasPagos = new ArrayList<FormasDePago>();		
		HashMap<String, Object> params =  new HashMap<String, Object>();
		for(Long IdOrden: idsOrdenes) {
			params.put("p_IdOrden", IdOrden);
			List<FormasDePago> formaPago = sql.query(con, SQL_SELECT_ORDENES_FORMAS_DE_PAGO_WS, params, FormasDePago.class);
			formasPagos.addAll(formaPago);
		}
		return formasPagos;
	}
	
	private List<Ordenes> consultarOrdenes(Connection con, ConsultarOrdenes_Entrada in) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params =  new HashMap<String, Object>();
		List<Ordenes> ordenes = new ArrayList<Ordenes>();		
		for(Clientes cliente: in.Clientes) {			
			String query = SQL_SELECT_ORDENES_WS.replace(":p_Filtros", generarFiltros(cliente));			
			List<Ordenes> orden = sql.query(con, query, params, Ordenes.class);
			ordenes.addAll(orden);
		}		
		return ordenes;
	}
	
	private ArrayList<Long> obtenerIdsOrdenes(List<Ordenes> ordenes){
		ArrayList<Long> ids = new ArrayList<Long>();
		for(Ordenes orden: ordenes)
			ids.add(orden.IdOrden);
		return ids;
		
	}
	
	private String generarFiltros(Clientes cliente) {		
		ArrayList<String> arrFiltros = new ArrayList<String>();
		String filtros = "";
		/* DocumentoIdentidad */
		if(cliente.DocumentoIdentidad != null)
			arrFiltros.add("ORD.\"DocumentoIdentidad\" = '" + cliente.DocumentoIdentidad + "'");
		/* IdPortafolio */
		if(cliente.IdPortafolio != null)
			arrFiltros.add("ORD.\"IdPortafolio\" = " + cliente.IdPortafolio);
		/* Instrumento */
		if(cliente.Instrumento != null)
			arrFiltros.add("ORD.\"Instrumento\" = '" + cliente.Instrumento + "'");
		/* IdOrden */
		if(cliente.IdOrden != null)
			arrFiltros.add("ORD.\"IdOrden\" = " + cliente.IdOrden);
		/* IdEstado */
		if(cliente.IdEstadoOrden != null)
			arrFiltros.add("ORD.\"IdEstadoOrden\" = '" + cliente.IdEstadoOrden + "'");
		/* CodigoPortafolio */
		if(cliente.CodigoPortafolio != null)
			arrFiltros.add("ORD.\"CodigoPortafolio\" = '" + cliente.CodigoPortafolio + "'");
		/* FechaInicioConsulta */
		if(cliente.FechaInicioConsulta != null)
			arrFiltros.add("TO_NVARCHAR (ORD.\"FechaOperacionOrden\", 'YYYY-MM-DD') >= '" + cliente.FechaInicioConsulta + "'");
		/* FechaTerminoConsulta */
		if(cliente.FechaTerminoConsulta != null)
			arrFiltros.add("TO_NVARCHAR (ORD.\"FechaOperacionOrden\", 'YYYY-MM-DD') <= '" + cliente.FechaTerminoConsulta + "'");
		/* IdMatrizAgrupador */ 
		if(cliente.IdMatrizAgrupador != null)
			arrFiltros.add("ORD.\"IdMatrizAgrupador\" = '" + cliente.IdMatrizAgrupador + "'");
		/* EsADC */
		if(cliente.EsADC != null)
			arrFiltros.add("ORD.\"EsADC\" = '" + cliente.EsADC + "'");
		/* InstrumentoNegociacion */
		if(cliente.InstrumentoNegociacion != null)
			arrFiltros.add("ORD.\"InstrumentoNegociacion\" = '" + cliente.InstrumentoNegociacion + "'");
		/* GeneraSpot */
		if(cliente.GeneraSpot != null)
			arrFiltros.add("ORD.\"GeneraSpot\" = '" + cliente.GeneraSpot + "'");
		/* IdTipoInstrumento */
		if(cliente.IdTipoInstrumento != null)
			arrFiltros.add("UPPER( ORD.\"IdTipoInstrumento\") = '" + cliente.IdTipoInstrumento.toUpperCase() + "'");
		/* Canal ingreso*/
		if(cliente.Canal != null) 
			arrFiltros.add("UPPER( ORD.\"Canal\" ) = '" + cliente.Canal.toUpperCase() + "'");
		
		
		for(String filtro: arrFiltros){
			if(arrFiltros.indexOf(filtro) != 0)
				filtros += " AND " + filtro;
			else filtros += filtro;
		}		
		return filtros;
	}
	
	private List<MensajeSalida> validarCatalogos(Connection con, ConsultarOrdenes_Entrada entrada) throws Exception {
		logger.debug("INICIO::ConsultarOrdenesWS.validarCatalogos");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();
		SQL sql = new SQL();	
		
		HashMap <String,Object> paramsEstado = new HashMap<>();
		paramsEstado.put("IdEstado", "VIG");
		List<TipoIntrumento> existeTipoInstrumentoList = sql.query(con, SQL_SELECT_ORDENES_VALIDA_TIPOINSTRUMENTO, paramsEstado, TipoIntrumento.class);
		paramsEstado.clear();
		paramsEstado.put("IdEstado", "ACT");
		List<CanalIngreso> existeCanalIngresoList = sql.query(con, SQL_SELECT_ORDENES_VALIDA_CANAL_INGRESO, paramsEstado, CanalIngreso.class);
		
		
		
		for(Clientes cliente: entrada.Clientes) {			
			if(cliente.IdTipoInstrumento != null) {		
				boolean existeTipoInstrumento = existeTipoInstrumentoList.stream().anyMatch(x -> x.IdTipoInstrumento.equals(cliente.IdTipoInstrumento.toUpperCase()));
				
				if (!existeTipoInstrumento) {
				    HashMap<String,String> parametros = new HashMap<>();
				    parametros.put("IdTipoInstrumento", cliente.IdTipoInstrumento);
				    mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ID_TIPO_INSTRUMENTO_NO_EXISTE", parametros, con, entrada.IdEmpresa));
				    return mensajes;
				}				
			}
			
			if(cliente.Canal != null) {
				boolean existeCanalIngreso = existeCanalIngresoList.stream().anyMatch(x -> x.Nombre.equals(cliente.Canal.toUpperCase()));
						
				if (!existeCanalIngreso) {
				    HashMap<String,String> parametros = new HashMap<>();
				    parametros.put("Nombre", cliente.Canal);
				    mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CANAL_INGRESO_NO_EXISTE", parametros, con, entrada.IdEmpresa));
				    return mensajes;
				}											
			}			
		
		}
		

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarOrdenesWS.validarCatalogos", fin - inicio);
		return mensajes;
	}
	
	public ConsultarOrdenes_Salida logica(Connection conn, ConsultarOrdenes_Entrada in) throws Exception{		
		long inicio = System.currentTimeMillis();
		ConsultarOrdenes_Salida out = new ConsultarOrdenes_Salida();
		out.P_OUT_MENSAJES = new ArrayList<>();
		out.P_OUT_MENSAJES.addAll(validarCatalogos(conn,in));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		} else {
			out.P_OUT_ORDENES = new Ordenes_Salida();		
			out.P_OUT_ORDENES.Ordenes = consultarOrdenes(conn, in);
			List<Long> idsOrdenes = obtenerIdsOrdenes(out.P_OUT_ORDENES.Ordenes);
			out.P_OUT_ORDENES.Cobros = consultarCobros(conn, idsOrdenes);
			out.P_OUT_ORDENES.FormasDePago = consultarFormasDePago(conn, idsOrdenes);
			StatsD.reportCount(METRIC_VOLUMEN_DATOS, idsOrdenes.size());
			StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_EJECUCION, (System.currentTimeMillis() - inicio));
		}
		return out;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("=== BEGIN TEST ===");
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String json = "{\r\n" + 
					"  \"IdEmpresa\": 1,\r\n" + 
					"  \"IdUsuario\": 1,\r\n" + 
					"  \"Clientes\": [\r\n" + 
					"    {\r\n" + 
					"      \"DocumentoIdentidad\": \"19170266-2\"\r\n" +										
					"    },\r\n" +
					"{\r\n" + 
						"      \"FechaInicioConsulta\": \"2020-06-05\"\r\n" +										
						"    },\r\n" +
						"{\r\n" + 
							"      \"FechaTerminoConsulta\": \"2020-06-05\"\r\n" +										
							"    }\r\n" +
					"  ]\r\n" + 
					"}";
			
			ConsultarOrdenes_Entrada in = gson.fromJson(json, ConsultarOrdenes_Entrada.class);			
			ConsultarOrdenesWS demo = new ConsultarOrdenesWS();
			try
			{
				ConsultarOrdenes_Salida ObjOut = demo.logica(con, in);
				//ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut);
			}catch(Exception er) {
				er.printStackTrace();				
				con.rollback();
			}
		}
		System.out.println("=== END TEST ===");
	}

}
