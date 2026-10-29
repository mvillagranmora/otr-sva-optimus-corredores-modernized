package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarTransaccionesAsignacionPrecioPromedio implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarTransaccionesAsignacionPrecioPromedio.class);
	
	private final String SQL_SELECT_CONSULTAR_TRANSACCIONES_DETALLE = loadResourceToString("cl/bcs/ordenes/sql/ConsultarTransaccionesAsignacionPrecioPromedio_SelectTransaccionesDetalle.sql");
	
	public static class EntradaConsultarTransaccionesAsignacionPrecioPromedio extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public FiltroBusqueda p_FiltrosBusqueda;
		
		public EntradaConsultarTransaccionesAsignacionPrecioPromedio () {
			this.p_IdEmpresa = 1L;
			this.p_IdModulo = "ORD";
			this.p_IdUsuario = 0L;
			this.p_FiltrosBusqueda = new FiltroBusqueda();
		}
	}
	
	public static class FiltroBusqueda extends BcsPojo {
		public String IdClasificacionTipoInstrumento;
		public String IdBolsa;
		public List<String> MarcasNegociacion;
		public List<Long> IdInstrumentos;
		public String IdCondicionDeLiquidacion;
		public String TipoOperacion;
		public String FechaDesde;
		public String FechaHasta;
		public String HoraDesde;
		public String HoraHasta;
		public BigDecimal PrecioDesde;
		public BigDecimal PrecioHasta;
		public String EsDetalle;
		public Long IdLibroOrdenes;
		public String IdBoveda;
		public List<String> FolioTransacciones;
		
		public FiltroBusqueda () {
			this.IdClasificacionTipoInstrumento = "";
			this.IdBolsa = "";
			this.MarcasNegociacion = new ArrayList<String>();
			this.IdInstrumentos = new ArrayList<Long>();
			this.IdCondicionDeLiquidacion = "";
			this.TipoOperacion = "";
			this.FechaDesde = "";
			this.FechaHasta = "";
			this.HoraDesde = "";
			this.HoraHasta = "";
			this.PrecioDesde = BigDecimal.ZERO;
			this.PrecioHasta = BigDecimal.ZERO;
			this.EsDetalle = "N";
			this.IdLibroOrdenes = 0L;
			this.IdBoveda = "";
			this.FolioTransacciones = new ArrayList<String>();
		}
	}
	
	public static class SalidaConsultarTransaccionesAsignacionPrecioPromedio extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<AgrupacionTransaccion> P_OUT_TRANSACCIONES_AGRUPADAS;
		public List<TransaccionDetalle> P_OUT_TRANSACCIONES_DETALLE;
		
		public SalidaConsultarTransaccionesAsignacionPrecioPromedio () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TRANSACCIONES_AGRUPADAS = new ArrayList<AgrupacionTransaccion>();
			this.P_OUT_TRANSACCIONES_DETALLE = new ArrayList<TransaccionDetalle>();
		}
	}
	
	public static class AgrupacionTransaccion extends BcsPojo {
		public BigDecimal CantidadDisponibleTotal;
	    public BigDecimal MontoDisponibleTotal;
	    public String InstrumentoUI;
		public Long IdInstrumentoUI;
		public String IdMonedaInstrumento;
		public String IdTipoInstrumento;
		public Long CantidadTransacciones;
		
		public AgrupacionTransaccion () {
			this.CantidadDisponibleTotal = BigDecimal.ZERO;
			this.MontoDisponibleTotal = BigDecimal.ZERO;
			this.InstrumentoUI = "";
			this.IdInstrumentoUI = 0L;
			this.IdMonedaInstrumento = "CLP";
			this.IdTipoInstrumento = "";
			this.CantidadTransacciones = 0L;
		}
		public AgrupacionTransaccion (Long idInstrumentoUI, String instrumentoUI, String idMonedaInstrumento, String idTipoInstrumento, Long cantidadTransacciones) {
			this.IdInstrumentoUI = idInstrumentoUI;
			this.InstrumentoUI = instrumentoUI;
			this.CantidadDisponibleTotal = BigDecimal.ZERO;
			this.MontoDisponibleTotal = BigDecimal.ZERO;
			this.IdMonedaInstrumento = idMonedaInstrumento;
			this.IdTipoInstrumento = idTipoInstrumento;
			this.CantidadTransacciones = cantidadTransacciones;
		}
	}
	
	public static class TransaccionDetalle extends BcsPojo {
		public Long IdTransaccion;
		public String FolioTransaccion;
		public java.sql.Timestamp FechaOperacionTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String AccionTipoOperacion;
		public Long IdInstrumentoUI;
		public String InstrumentoUI;
		public BigDecimal CantidadTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal MontoDisponibleTransaccion;
		public String NombreCondicionDeLiquidacion;
		public String MarcaNegociacion;
		public String NombreBolsa;
		public String IdMonedaInstrumento;
		public String IdTipoInstrumento;
		public Long IdLibroOrdenes;
		//public String EsVentaCorta;
		public String IdTipoOperacion;
		public String IdMonedaTransaccion;
		public String IdBoveda;
		public String IdModeloCustodia;
		
		public TransaccionDetalle () {
			this.IdTransaccion = 0L;
			this.FolioTransaccion = "";
			this.FechaOperacionTransaccion = Fecha.toTimeStamp("0001-01-01 00:00:00");
			this.IdClasificacionTipoInstrumento = "";
			this.AccionTipoOperacion = "";
			this.IdInstrumentoUI = 0L;
			this.InstrumentoUI = "";
			this.CantidadTransaccion = BigDecimal.ZERO;
			this.PrecioTransaccion = BigDecimal.ZERO;
			this.CantidadDisponibleTransaccion = BigDecimal.ZERO;
			this.MontoDisponibleTransaccion = BigDecimal.ZERO;;
			this.NombreCondicionDeLiquidacion = "";
			this.MarcaNegociacion = "";
			this.NombreBolsa = "";
			this.IdMonedaInstrumento = "";
			this.IdTipoInstrumento = "";
			this.IdLibroOrdenes = 0L;
			//this.EsVentaCorta = "N";
			this.IdTipoOperacion = "";
			this.IdMonedaTransaccion = "";
			this.IdModeloCustodia = "";
		}
	}
	
	public SalidaConsultarTransaccionesAsignacionPrecioPromedio logica (Connection con, EntradaConsultarTransaccionesAsignacionPrecioPromedio entrada) throws Exception {
		logger.info("INICIO::ConsultarTransaccionesAsignacionPrecioPromedio");
	    long inicio = System.currentTimeMillis();
	    
	    SalidaConsultarTransaccionesAsignacionPrecioPromedio salida = new SalidaConsultarTransaccionesAsignacionPrecioPromedio();
	    String OmitePalosReservados = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
		
		SQL sql = new SQL();
		if(validaDatosObligatoriosEntrada(entrada)) {
			HashMap<String,Object> params = new HashMap<>(15);
			params.put("IdClasificacionTipoInstrumento", entrada.p_FiltrosBusqueda.IdClasificacionTipoInstrumento);
			params.put("IdBolsa", entrada.p_FiltrosBusqueda.IdBolsa);
			params.put("IdInstrumentos", entrada.p_FiltrosBusqueda.IdInstrumentos);
			params.put("IdCondicionDeLiquidacion", entrada.p_FiltrosBusqueda.IdCondicionDeLiquidacion);
			params.put("TipoOperacion", entrada.p_FiltrosBusqueda.TipoOperacion);
			params.put("FechaDesde", entrada.p_FiltrosBusqueda.FechaDesde);
			params.put("FechaHasta", entrada.p_FiltrosBusqueda.FechaHasta);
			params.put("HoraDesde", entrada.p_FiltrosBusqueda.HoraDesde);
			params.put("HoraHasta", entrada.p_FiltrosBusqueda.HoraHasta);
			params.put("PrecioDesde", entrada.p_FiltrosBusqueda.PrecioDesde);
			params.put("PrecioHasta", entrada.p_FiltrosBusqueda.PrecioHasta);
			params.put("IdLibroOrdenes", entrada.p_FiltrosBusqueda.IdLibroOrdenes);
			params.put("IdBoveda", entrada.p_FiltrosBusqueda.IdBoveda);
			
			
			String queryConsulta = SQL_SELECT_CONSULTAR_TRANSACCIONES_DETALLE;
			
			if("S".equalsIgnoreCase(OmitePalosReservados)) {
				queryConsulta = queryConsulta.replace("<<FILTRO_MARCAS_RESERVADO>>", "AND \"Reservado\" = 'N'");
			}else {
				params.put("IdUsuarioReserva", entrada.p_IdUsuario);
				queryConsulta = queryConsulta.replace("<<FILTRO_MARCAS_RESERVADO>>", "AND ((\"IdUsuarioReserva\" = :IdUsuarioReserva AND \"Reservado\" = 'S') OR (COALESCE(\"IdUsuarioReserva\", 0) = 0))");
			}
			
			
			if(entrada.p_FiltrosBusqueda.MarcasNegociacion.isEmpty()) {
				queryConsulta = queryConsulta.replace("<<FILTRO_MARCAS_NEGOCIACION>>", "");
			}else {
				entrada.p_FiltrosBusqueda.MarcasNegociacion.replaceAll(marca -> "UI_VACIO_SIN_MARCA".equals(marca) ? "" : marca);
				params.put("MarcasNegociacion", entrada.p_FiltrosBusqueda.MarcasNegociacion);
				queryConsulta = queryConsulta.replace("<<FILTRO_MARCAS_NEGOCIACION>>", "AND \"MarcaNegociacion\" IN (:MarcasNegociacion)");
			}
			
			if(entrada.p_FiltrosBusqueda.FolioTransacciones == null || entrada.p_FiltrosBusqueda.FolioTransacciones.isEmpty()) {
			    queryConsulta = queryConsulta.replace("<<FILTRO_FOLIO_TRANSACCIONES>>", "");
			} else {
			    params.put("FolioTransacciones", entrada.p_FiltrosBusqueda.FolioTransacciones);
			    queryConsulta = queryConsulta.replace(
			        "<<FILTRO_FOLIO_TRANSACCIONES>>",
			        "AND \"FolioTransaccion\" IN (:FolioTransacciones)"
			    );
			}
			
			List<TransaccionDetalle> transaccionesDetalladas = sql.query(con,queryConsulta,params,TransaccionDetalle.class);
			
			if(!transaccionesDetalladas.isEmpty()) {
				if("S".equals(entrada.p_FiltrosBusqueda.EsDetalle)) {
					salida.P_OUT_TRANSACCIONES_DETALLE.addAll(transaccionesDetalladas);
				} else {
					salida.P_OUT_TRANSACCIONES_AGRUPADAS.addAll(agruparTransaciones(transaccionesDetalladas));
				}
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_BUSQUEDA_TRANSACCIONES_OK", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			} else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BUSQUEDA_SIN_TRANSACCIONES", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			}
	    	
		} else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENTRADA_SIN_DATOS_MINIMOS", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
	    	salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}
		
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::ConsultarTransaccionesAsignacionPrecioPromedio", fin - inicio );
		return salida;
	}
	
	private List<AgrupacionTransaccion> agruparTransaciones(List<TransaccionDetalle> transaccionesDetalladas) {
		logger.info("INICIO::ConsultarTransaccionesAsignacionPrecioPromedio.agruparTransaciones");
	    long inicio = System.currentTimeMillis();
	    
	    Map<String, AgrupacionTransaccion> agrupadas = new LinkedHashMap<>();
	    
	    for (TransaccionDetalle transaccion : transaccionesDetalladas) {
	    	Long idInstrumentoUI = transaccion.IdInstrumentoUI;
	    	String instrumentoUI = transaccion.InstrumentoUI;
	    	String idTipoInstrumento = transaccion.IdTipoInstrumento;
	    	String idMonedaInstrumento = transaccion.IdMonedaInstrumento;
	    	String llave = idInstrumentoUI + "_" + instrumentoUI;
	    	
	    	AgrupacionTransaccion grupo = agrupadas.get(llave);
	    	
	    	if(grupo == null) {
	    		grupo = new AgrupacionTransaccion(idInstrumentoUI,instrumentoUI,idMonedaInstrumento,idTipoInstrumento,0L);
	    		agrupadas.put(llave, grupo);
	    	}
	    	BigDecimal cantDisp = transaccion.CantidadDisponibleTransaccion;
	    	BigDecimal montoDisp = transaccion.MontoDisponibleTransaccion;
	    	
	    	if (cantDisp != null) {
	    		grupo.CantidadDisponibleTotal = grupo.CantidadDisponibleTotal.add(cantDisp);
	        }
	    	
	    	if (montoDisp != null) {
	    		grupo.MontoDisponibleTotal = grupo.MontoDisponibleTotal.add(montoDisp);
	        }
	    	grupo.CantidadTransacciones++;
	    }
	    
	    
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::ConsultarTransaccionesAsignacionPrecioPromedio.agruparTransaciones", fin - inicio );
		return new ArrayList<AgrupacionTransaccion>(agrupadas.values());
	}

	private boolean validaDatosObligatoriosEntrada(EntradaConsultarTransaccionesAsignacionPrecioPromedio entrada) {
		logger.info("INICIO::ConsultarTransaccionesAsignacionPrecioPromedio.validaDatosObligatoriosEntrada");
	    long inicio = System.currentTimeMillis();
	    
	    boolean validacion = !entrada.p_FiltrosBusqueda.IdInstrumentos.isEmpty() && !"".equals(entrada.p_FiltrosBusqueda.TipoOperacion);
		
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::ConsultarTransaccionesAsignacionPrecioPromedio.validaDatosObligatoriosEntrada", fin - inicio );
	    return validacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarTransaccionesAsignacionPrecioPromedio jin = gson.fromJson(in.toString(), EntradaConsultarTransaccionesAsignacionPrecioPromedio.class);
		SalidaConsultarTransaccionesAsignacionPrecioPromedio out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			ConsultarTransaccionesAsignacionPrecioPromedio demo = new ConsultarTransaccionesAsignacionPrecioPromedio();
			
			String entrada = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_FiltrosBusqueda\": {\r\n"
					+ "        \"IdClasificacionTipoInstrumento\": \"RV\",\r\n"
					+ "        \"IdBolsa\": \"BCS\",\r\n"
					+ "        \"MarcasNegociacion\": [],\r\n"
					+ "        \"IdInstrumentos\": [\r\n"
					+ "            502\r\n"
					+ "        ],\r\n"
					+ "        \"IdCondicionDeLiquidacion\": \"T0\",\r\n"
					+ "        \"TipoOperacion\": \"COMPRA\",\r\n"
					+ "        \"FechaDesde\": \"2025-11-27\",\r\n"
					+ "        \"FechaHasta\": \"2025-11-27\",\r\n"
					+ "        \"HoraDesde\": \"00:00\",\r\n"
					+ "        \"HoraHasta\": \"23:59\",\r\n"
					+ "        \"PrecioDesde\": 0,\r\n"
					+ "        \"PrecioHasta\": 0,\r\n"
					+ "		   \"EsDetalle\": \"N\"\r\n"
					+ "    }\r\n"
					+ "}";
			

			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9563,\"p_IdModulo\":\"ORD\",\"p_FiltrosBusqueda\":{\"IdClasificacionTipoInstrumento\":\"RV\",\"IdBolsa\":\"\",\"MarcasNegociacion\":[],\"IdInstrumentos\":[2220],\"IdCondicionDeLiquidacion\":\"T0\",\"TipoOperacion\":\"COMPRA\",\"FechaDesde\":\"2026-07-06\",\"FechaHasta\":\"2026-07-06\",\"HoraDesde\":\"00:00:00\",\"HoraHasta\":\"23:59:59\",\"PrecioDesde\":0,\"PrecioHasta\":0,\"EsDetalle\":\"N\",\"IdLibroOrdenes\":0,\"IdBoveda\":\"\"}}\r\n"
					+ "";
			
			EntradaConsultarTransaccionesAsignacionPrecioPromedio Objin = gson.fromJson(json2, EntradaConsultarTransaccionesAsignacionPrecioPromedio.class);
			
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				SalidaConsultarTransaccionesAsignacionPrecioPromedio salida = demo.logica(con, Objin);
				System.out.println("SALIDA");
				System.out.println(salida.toString());
				
				con.rollback();
			}catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch(Exception er) {
			er.printStackTrace();
		}
		
		System.out.println("== END ==");

	}

}