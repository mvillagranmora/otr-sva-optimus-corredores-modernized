package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class MarketData_CrearTransacciones implements JavaCall {
	
	private final String SQL_CALL_MARKETDATA_CREARTRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_CallMarketDataCrearTransacciones.sql");
	private final String SQL_INSERT_ENTRADA_SIMULTANEAS = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_InsertPalosSim.sql");
	
	private final String SQL_SELECT_RESULTADO = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_SelectResultado.sql");
	private final String SQL_SELECT_INFORMACION = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_SelectInformacion.sql");
	private final String SQL_SELECT_ADVERTENCIAS = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_SelectAdvertencia.sql");
	private final String SQL_SELECT_ERRORES = loadResourceToString("cl/bcs/ordenes/sql/MarketData_CrearTransacciones_SelectErrores.sql");
	
	public MarketData_CrearTransacciones_Salida logica(Connection conn,MarketData_CrearTransacciones_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		
		MarketData_CrearTransacciones_Salida salida = new MarketData_CrearTransacciones_Salida();
		
		sql.batch(conn,SQL_INSERT_ENTRADA_SIMULTANEAS, entrada.p_simultaneas,MarketData_CrearTransacciones_Entrada_Simultanea.class);
		
		HashMap<String,Object> parmsIN=new HashMap<String,Object>();
		parmsIN.put("p_IdEmpresa", entrada.p_IdEmpresa);	
		parmsIN.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsIN.put("p_SegundosZonaHoraria", entrada.p_SegundosZonaHoraria);
		
		HashMap<String,Class> parmsOUT=new HashMap<String,Class>();
		
		parmsOUT.put("p_out_InformacionContador", Long.class);
		parmsOUT.put("p_out_AdvertenciasContador", Long.class);
		parmsOUT.put("p_out_ErroresContador", Long.class);
		
		HashMap<String,Object> respuesta=sql.call(conn, SQL_CALL_MARKETDATA_CREARTRANSACCIONES, parmsIN, parmsOUT);
		
		salida.p_out_resultado = sql.query(conn, SQL_SELECT_RESULTADO, new HashMap<>(),MarketData_CrearTransacciones_Salida_Resultado.class);
		salida.p_out_Mensajes = new ArrayList<PlataformaUtils.MensajeSalida>();
		
		List<OrdenUtils.InformacionDetalle> infoDet = sql.query(conn, SQL_SELECT_INFORMACION, new HashMap<>(), OrdenUtils.InformacionDetalle.class);
		List<OrdenUtils.AdvertenciaDetalle> advDet = sql.query(conn, SQL_SELECT_ADVERTENCIAS, new HashMap<>(), OrdenUtils.AdvertenciaDetalle.class);
		List<OrdenUtils.ErrorDetalle> errDet = sql.query(conn, SQL_SELECT_ERRORES, new HashMap<>(), OrdenUtils.ErrorDetalle.class);
		
		salida.p_out_Mensajes = OrdenUtils.convertirMensajes(errDet, advDet, infoDet);

		return salida;
	}
	
	//ENTRADAS
	public static class MarketData_CrearTransacciones_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_SegundosZonaHoraria;
		
		public List<MarketData_CrearTransacciones_Entrada_Simultanea> p_simultaneas;
		
		public MarketData_CrearTransacciones_Entrada() {
			p_simultaneas = new ArrayList();
		}
	}
	
	//SALIDAS
	public static class MarketData_CrearTransacciones_Salida {
		public Long P_OUT_ERRORESCONTADOR;

		public List<MarketData_CrearTransacciones_Salida_Resultado> p_out_resultado;
		public List<MensajeSalida> p_out_Mensajes;
		
	}
	
	public static class MarketData_CrearTransacciones_Salida_Resultado {
		public Long IdInterno;
		public Long IdTransaccion;
	}
	
	public static class MarketData_CrearTransacciones_Entrada_Simultanea {
		public Long IdInterno;
		public String action;
		public String timesTamp;
		public String sellExecutingFirm;
		public String sellEnteringFirm;
		public String sellEnteringTrader;
		public String buyExecutingFirm;
		public String buyEnteringFirm;
		public String buyEnteringTrader;
		public String qty;
		public String symbol;
		public BigDecimal spotPrice;
		public BigDecimal forwardPrice;
		public BigDecimal maxRate;
		public BigDecimal pubRate;
		public String settlementType;
		public String expirationDate;
		public Integer amount;
		public String transactionTime;
		public String transactionDay;
		public String prepaid;
		public String folio;
		public String bestStance;
		public String divisible;
		public String modified;
		public String codIsin;
		public String securityType;
		public String securityExchange;
		public String orderID;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public static void main(String[]args) throws Exception{
		
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
	