package cl.bcs.ordenes;

import java.math.BigDecimal;
//import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import javax.xml.ws.BindingProvider;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
//import cl.bcs.ordenes.jaxws.client.SpadditionaltradeinformationResponse;
//import cl.bcs.ordenes.jaxws.client.SpadditionaltradeinformationService;
//import cl.bcs.ordenes.jaxws.client.SpadditionaltradeinformationServiceService;
//import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.TransaccionFUTMarketData;
import cl.bcs.ordenes.pojo.TransaccionIFMarketData;
import cl.bcs.ordenes.pojo.TransaccionOPMarketData;
import cl.bcs.ordenes.pojo.TransaccionRFMarketData;
import cl.bcs.ordenes.pojo.TransaccionRVMarketData;
import cl.bcs.ordenes.pojo.TransaccionSIMMarketData;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
//import cl.bcs.plataforma.val.utils.ValorizadorProperties;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearTransaccionesMarketData implements JavaCall {
	
	
	private final static Logger logger = LogManager.getLogger(CrearTransaccionesMarketData.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String SQL_SELECT_CORREDORAS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectCorredoras.sql");
	private final String SQL_INSERT_TRANSACCIONES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_InsertTransaccionesWrapper.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectTransacciones.sql");
	private final String SQL_SELECT_SIMULTANEA = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectSimultanea.sql");
	private final String SQL_SELECT_LIBROS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectLibros.sql");
	private final String SQL_SELECT_OPERADORES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectOperadores.sql");
	
	private final String SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByOperador.sql");
	private final String SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByDefault.sql");
	
	public final String TIPO_INSTRUMENTO_MIL = "MIL";
	
	List<LibrosOrdenes> libroOrdenesDefault = null;
	List<LibrosOrdenes> libroOrdenesPorOperador = null;
	
	private Long obtenerLibroOrdenes(Connection conn, String codigoOperadorCorredor, Long idEmpresa) throws SQLException {
		SQL sql = new SQL();
		
		if (libroOrdenesDefault == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesDefault = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT, params, LibrosOrdenes.class);
		}
		
		if (libroOrdenesPorOperador == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesPorOperador = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR, params, LibrosOrdenes.class);	
		}
		
		if(codigoOperadorCorredor == null || codigoOperadorCorredor.equals("")) {
			return libroOrdenesDefault.get(0).IdLibroOrdenes;			
		}
		else {
			for (LibrosOrdenes conf : libroOrdenesPorOperador) {
				if (conf.CodigoOperador != null && conf.CodigoOperador.equalsIgnoreCase(codigoOperadorCorredor)) {
					return conf.IdLibroOrdenes;
				}
			}
		}		
		return libroOrdenesDefault.get(0).IdLibroOrdenes;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
	
	public static class Corredora {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LibroOrdenes_Proceso {
		public String CodigoOperador;
		public Long IdLibroOrdenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso extends cl.bcs.ordenes.CrearTransacciones.Transaccion_Entrada {
		public String Nemotecnico;
		public String AccionOrden;
		public String IdTipoInstrumento;
		public String OrigenTransaccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoTransaccion extends BcsPojo{
		public Long IdInterno;
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoReajuste;
		public BigDecimal CorteMinimo;
		public String IdMoneda;
				
		public InfoTransaccion() {
			this.IdMoneda = "";
		}
	}

	
	public static class OperadorFondo {
		public String KeyFondo;
		public String CodigoOperador;
	}
	
	public CrearTransaccionesMarketData_Salida logica(Connection conn,CrearTransaccionesMarketData_Entrada entrada) throws Exception {
			
		CrearTransaccionesMarketData_Salida salida = new CrearTransaccionesMarketData_Salida();
		SQL sql = new SQL();
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, entrada.p_IdEmpresa);
		
		String operadorRuteoAgente = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "OperadorRuteoAgente");
		String operadorRuteoRetail = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "OperadorRuteoRetail");
		
		String ActualizaBilateralConPagoUSD = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "ActualizaBilateralOperacionesPagoUSD");
        
		HashMap<String,Corredora> corredoras = new HashMap<String,Corredora>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			List<Corredora> _corredoras = sql.query(conn, SQL_SELECT_CORREDORAS, params1, Corredora.class);
			corredoras = new HashMap<>();
			for (Corredora corr : _corredoras) {
				corredoras.put(corr.CodigoCorredoraFix, corr);
			}
		}
		
		//para arreglar el codigo del fondo 
		HashMap<String,OperadorFondo> operadores = new HashMap<String,OperadorFondo>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("IdEmpresa", entrada.p_IdEmpresa);
			List<OperadorFondo> _operadores = sql.query(conn, SQL_SELECT_OPERADORES, params1, OperadorFondo.class);
			operadores = new HashMap<>();
			for (OperadorFondo ope : _operadores) {
				operadores.put(ope.KeyFondo, ope);
			}
		}
			
		List<Transaccion_Proceso> transacciones = new ArrayList<Transaccion_Proceso>();
		//PALOS IF
		for (TransaccionIFMarketData_Entrada tran : entrada.transaccionesIF) {
			
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "IF";
			Corredora corredoraCompra = corredoras.get(tran.BUYEXECUTINGFIRM.toString());
			Corredora corredoraVenta = corredoras.get(tran.SELLEXECUTINGFIRM.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.BUYEXECUTINGFIRM + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.SELLEXECUTINGFIRM + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				
				if (tran.BUYERFUND.length() > 0 && operadores.get(tran.BUYENTERINGFIRM + tran.BUYERFUND) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.BUYENTERINGFIRM + tran.BUYERFUND).CodigoOperador;
				}
				else if (!tran.BUYEXECUTINGFIRM.equalsIgnoreCase(tran.BUYENTERINGFIRM) && tran.BUYENTERINGFIRM.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.BUYENTERINGFIRM;
				}
				else {
					tx.CodigoOperadorCorredor = tran.TRADERBUY;
				}
				
				if (tran.SELLERFUND.length() > 0 && operadores.get(tran.SELLENTERINGFIRM + tran.SELLERFUND) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.SELLENTERINGFIRM + tran.SELLERFUND).CodigoOperador;
				}
				else if (!tran.SELLEXECUTINGFIRM.equalsIgnoreCase(tran.SELLENTERINGFIRM) && tran.SELLENTERINGFIRM.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.SELLENTERINGFIRM;
				}
				else {
					tx.CodigoOperadorContraparte = tran.TRADERSELL;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.SELLEXECUTINGFIRM.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {

				if (tran.SELLERFUND.length() > 0 && operadores.get(tran.SELLENTERINGFIRM + tran.SELLERFUND) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.SELLENTERINGFIRM + tran.SELLERFUND).CodigoOperador;
				}
				else if (!tran.SELLEXECUTINGFIRM.equalsIgnoreCase(tran.SELLENTERINGFIRM) && tran.SELLENTERINGFIRM.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.SELLENTERINGFIRM;
				}
				else {
					tx.CodigoOperadorCorredor = tran.TRADERSELL;
				}

				if (tran.BUYERFUND.length() > 0 && operadores.get(tran.BUYENTERINGFIRM + tran.BUYERFUND) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.BUYENTERINGFIRM + tran.BUYERFUND).CodigoOperador;
				}
				else if (!tran.BUYEXECUTINGFIRM.equalsIgnoreCase(tran.BUYENTERINGFIRM) && tran.BUYENTERINGFIRM.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.BUYENTERINGFIRM;
				}
				else {
					tx.CodigoOperadorContraparte = tran.TRADERBUY;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.BUYEXECUTINGFIRM.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.TRADEID + " Fecha:" + tran.TIMETICKER+ " C:" + tran.BUYEXECUTINGFIRM + " V:" + tran.SELLEXECUTINGFIRM;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			if (tran.SYMBOL.equalsIgnoreCase("PRBC") || tran.SYMBOL.equalsIgnoreCase("PDBC")) {
				tx.Cantidad = BigDecimal.ZERO;  //mas abajo se calcula
			}
			else {
				tx.Cantidad = BigDecimal.ONE;
			}
			
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			if (tran.SYMBOL.equalsIgnoreCase("PDBC") || tran.SYMBOL.equalsIgnoreCase("PDBC")) {
				tx.EsFungible ="N";
			}
			else {
				tx.EsFungible = "S";	
			}
			
			String ordenDirecta = tran.TRADECONDITION.substring(7, 8);
			if(tran.BUYEXECUTINGFIRM.equalsIgnoreCase(tran.SELLEXECUTINGFIRM)){
			if (!tran.BUYEXECUTINGFIRM.equalsIgnoreCase(tran.BUYENTERINGFIRM) || (!tran.SELLEXECUTINGFIRM.equalsIgnoreCase(tran.SELLENTERINGFIRM)) ) {
					tx.IndicadorOrdenDirecta = "OE";	
				}
				else {
					tx.IndicadorOrdenDirecta = "OD";
				}
			}
			else {
				tx.IndicadorOrdenDirecta = "";
			}
			
			tx.EsVentaCorta = "N";
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.TRADETIME)));
			
			tx.FechaEmision = "0001-01-01";
			
			SimpleDateFormat sdfSettleDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfSettleDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tx.FechaLiquidacionContado = sdfSettleDateOUT.format(sdfSettleDateIN.parse(tran.SETTLDATE));
			
			SimpleDateFormat sdfExpireTimeIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpireTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			tx.FechaVencimiento = sdfExpireTimeOUT.format(sdfExpireTimeIN.parse(tran.EXPIRETIME));	
			tx.FolioTransaccion = tran.TRADEID;
			tx.FolioTransaccionPlazo = "";
			
			SimpleDateFormat sdfTradeTimeHoraIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeHoraOUT = new SimpleDateFormat("HH:mm:ss");
			tx.HoraCreacion = sdfTradeTimeHoraOUT.format(sdfTradeTimeHoraIN.parse(tran.TRADETIME));
			tx.IdBolsa = "BCS";
			
			String codLiq = tran.TRADECONDITION.substring(1, 2); 
			if (codLiq.equalsIgnoreCase(" ")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (codLiq.equalsIgnoreCase("M")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (codLiq.equalsIgnoreCase("H")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else if (codLiq.equalsIgnoreCase("3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (codLiq.equalsIgnoreCase("5")) {
				tx.IdCondicionDeLiquidacionContado = "T5";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = tran.ISSUER;
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
			
			String idSistema = tran.AUCTIONID.substring(0, 1);
			if (idSistema.equalsIgnoreCase("H")) {
				tx.IdSistemaBursatil = "BCSREMATEHOLANDES";
			}
			else if (idSistema.equalsIgnoreCase("T")) {
				tx.IdSistemaBursatil = "BCSTELERENTA";
			}
			else if (idSistema.equalsIgnoreCase("E")) {
				tx.IdSistemaBursatil = "BCSREMATEELECTRONICO";
			}
			else {
				tx.IdSistemaBursatil = "BCSTELERENTA";
			}
			
			tx.Monto = tran.LOCALAMOUNT; //DEPENDIENDO DEL TIPO DE REAJUSTE REPASAMOS EL MONTO
			if (tran.RESETTYPE.equalsIgnoreCase("NR")) {
				tx.IdTipoReajuste = "CLP";
				tx.IdMoneda = "CLP";
				tx.IdMonedaEmision = "CLP";
			}			
			else if (tran.RESETTYPE.equalsIgnoreCase("UD")) {
				tx.IdTipoReajuste = "USD";
				tx.IdMoneda = "USD";
				tx.IdMonedaEmision = "USD";
				tx.Monto = tran.AMOUNT;
			}
			else if (tran.RESETTYPE.equalsIgnoreCase("US")) {
				tx.IdTipoReajuste = "USD";
				tx.IdMoneda = "CLP";
				tx.IdMonedaEmision = "USD";
			}
			else if (tran.RESETTYPE.equalsIgnoreCase("IC")) {
				tx.IdTipoReajuste = "ICP";
				tx.IdMoneda = "CLP";
				tx.IdMonedaEmision = "ICP";
			}
			else if (tran.RESETTYPE.equalsIgnoreCase("UF")) {
				tx.IdTipoReajuste = "UF";
				tx.IdMoneda = "CLP";
				tx.IdMonedaEmision = "UF";
			}
			else if (tran.RESETTYPE.equalsIgnoreCase("ES")) {
				tx.IdTipoReajuste = "EUR";
				tx.IdMoneda = "CLP";
				tx.IdMonedaEmision = "EUR";
			}
			else if (tran.RESETTYPE.equalsIgnoreCase("EU")) {
				tx.IdTipoReajuste = "EUR";
				tx.IdMoneda = "EUR";
				tx.IdMonedaEmision = "EUR";
			}
			else {
				tx.IdTipoReajuste = null;
				tx.IdMonedaEmision = null;
				tx.IdMoneda = null;
			}
			
			tx.IndicadorDCV =  tran.DCVTITLE;
			tx.MarcaNegociacion = "";
			tx.MejorPrecioCompra = BigDecimal.ZERO;
			tx.MejorPrecioVenta = BigDecimal.ZERO;
			
			
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = BigDecimal.ZERO;
			tx.Nemotecnico = tran.SYMBOL;
			tx.NemotecnicoSVS = tran.SYMBOLSVS;
			tx.Plazo = new Long(tran.TERM);
			tx.Precio = BigDecimal.ZERO;
			tx.PrecioPlazo = BigDecimal.ZERO;
			tx.Tasa = tran.RATE;
			
			if (tran.SIGNRATE.equalsIgnoreCase("-")) {
				tx.Tasa = tx.Tasa.multiply(new BigDecimal(-1));
			}
			
			
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = tran.RESCUEVALUE;
			tx.FlujoOrigen = "INGRESOORDEN";
			
			
			transacciones.add(tx);
			
		}
		
		
		//PALOS RF
		for (TransaccionRFMarketData_Entrada tran : entrada.transaccionesRF) {
			
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "RF";
			Corredora corredoraCompra = corredoras.get(tran.buyExecutingFirm.toString());
			Corredora corredoraVenta = corredoras.get(tran.sellExecutingFirm.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.buyExecutingFirm + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.sellExecutingFirm + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				
				if (tran.buyerFund.length() > 0 && operadores.get(tran.buyEnteringFirm + tran.buyerFund) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.buyEnteringFirm + tran.buyerFund).CodigoOperador;
				}
				else if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.traderBuy;
				}
				
				if (tran.sellerFund.length() > 0 && operadores.get(tran.sellEnteringFirm + tran.sellerFund) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.sellEnteringFirm + tran.sellerFund).CodigoOperador;
				}
				else if (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.traderSell;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.sellExecutingFirm.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
				tx.IdBolsa = corredoraCompra.IdBolsa;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {

				if (tran.sellerFund.length() > 0 && operadores.get(tran.sellEnteringFirm + tran.sellerFund) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.sellEnteringFirm + tran.sellerFund).CodigoOperador;
				}
				else if (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.traderSell;
				}
				
				if (tran.buyerFund.length() > 0 && operadores.get(tran.buyEnteringFirm + tran.buyerFund) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.buyEnteringFirm + tran.buyerFund).CodigoOperador;
				}
				else if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.traderBuy;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.buyExecutingFirm.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
				tx.IdBolsa = corredoraVenta.IdBolsa;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeTicker+ " C:" + tran.buyExecutingFirm + " V:" + tran.sellExecutingFirm;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			tx.Cantidad = tran.quantity;
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			tx.EsFungible ="N";
			
			String ordenDirecta = tran.tradeCondition.substring(7, 8);
			if(tran.buyExecutingFirm.equalsIgnoreCase(tran.sellExecutingFirm)){
				if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.buyEnteringFirm) || (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm)) ) {
					tx.IndicadorOrdenDirecta = "OE";	
				}
				else {
					tx.IndicadorOrdenDirecta = "OD";
				}
			}
			else {
				 tx.IndicadorOrdenDirecta = "";
			}
			
			if("V".equalsIgnoreCase(tran.tradeCondition.substring(3, 4))) {
				tx.EsVentaCorta = "S";
			}else{
				tx.EsVentaCorta = "N";
			}
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.tradeTime)));
			
			tx.FechaEmision = "0001-01-01";
			
			SimpleDateFormat sdfSettleDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfSettleDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tx.FechaLiquidacionContado = sdfSettleDateOUT.format(sdfSettleDateIN.parse(tran.settlDate));
			
			tx.FechaVencimiento = "0001-01-01";	
			tx.FolioTransaccion = tran.tradeID;
			tx.FolioTransaccionPlazo = "";
			
			SimpleDateFormat sdfTradeTimeHoraIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeHoraOUT = new SimpleDateFormat("HH:mm:ss");
			tx.HoraCreacion = sdfTradeTimeHoraOUT.format(sdfTradeTimeHoraIN.parse(tran.tradeTime));
			
			String codLiq = tran.tradeCondition.substring(1, 2); 
			if (codLiq.equalsIgnoreCase(" ")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
				tx.EsBilateral  = "S";
			}
			else if (codLiq.equalsIgnoreCase("M")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (codLiq.equalsIgnoreCase("H")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else if (codLiq.equalsIgnoreCase("3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (codLiq.equalsIgnoreCase("5")) {
				tx.IdCondicionDeLiquidacionContado = "T5";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
						
			String idSistema = tran.auctionId.substring(0, 1);
			if (idSistema.equalsIgnoreCase("H")) {
				tx.IdSistemaBursatil = "BCSREMATEHOLANDES";
			}
			else if (idSistema.equalsIgnoreCase("T")) {
				tx.IdSistemaBursatil = "BCSTELERENTA";
			}
			else if (idSistema.equalsIgnoreCase("E")) {
				tx.IdSistemaBursatil = "BCSREMATEELECTRONICO";
			}
			else {
				tx.IdSistemaBursatil = "BCSTELERENTA";
			}
			
			//txe.IdTipoOperacion;
			tx.IdTipoReajuste = "";
			tx.IdMoneda = "CLP";
			tx.IndicadorDCV =  tran.dcvTitle;
			tx.MarcaNegociacion = "";
			tx.MejorPrecioCompra = BigDecimal.ZERO;
			tx.MejorPrecioVenta = BigDecimal.ZERO;
			
			tx.Monto = tran.currencySettlementAmount;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = BigDecimal.ZERO;
			if (tran.symbol.startsWith("CERO") && tran.family.equalsIgnoreCase("BT")) {
				tx.Nemotecnico = "CERO";
			}
			else if (tran.symbol.startsWith("XERO") && tran.family.equalsIgnoreCase("BT")) {
				tx.Nemotecnico = "XERO";
			}
			else if (tran.symbol.startsWith("ZERO") && tran.family.equalsIgnoreCase("BT")) {
				tx.Nemotecnico = "ZERO";
			}
			else if (tran.symbol.startsWith("BR") && tran.family.equalsIgnoreCase("BT")) {
				tx.Nemotecnico = "BR";
			}
			else {
				tx.Nemotecnico = tran.symbol;	
			}
			
			tx.NemotecnicoSVS = "";
			tx.NemotecnicoEspecificoBolson = tran.symbol;
			tx.Plazo = 0L;
			tx.Precio = tran.price;
			tx.PrecioPlazo = BigDecimal.ZERO;
			tx.Tasa = tran.tir;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = BigDecimal.ZERO;
			tx.FlujoOrigen = "INGRESOORDEN";
			
			transacciones.add(tx);
			
		}
		
		
		//PALOS RV
		for (TransaccionRVMarketData_Entrada tran : entrada.transaccionesRV) {
			
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "RV";
			Corredora corredoraCompra = corredoras.get(tran.buyerBroker.toString());
			Corredora corredoraVenta = corredoras.get(tran.sellerBroker.toString());
			
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.buyerBroker + " no cargado";
				logger.error(tran.tradeID+ " " + tran.quantity + " " + tran.instrument);
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.sellerBroker + " no cargado";
				logger.error(tran.tradeID+ " " + tran.quantity + " " + tran.instrument);
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			
			if (corredoraCompra != null && empresa.IdCorredora.equals(corredoraCompra.IdCorredora)) {
				
				if (tran.buyFundManager!= null && tran.buyFundManager.length() > 0 && operadores.get(tran.buyEnteringFirm + tran.buyFundManager) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.buyEnteringFirm + tran.buyFundManager).CodigoOperador;
				}
				else if (!tran.buyerBroker.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.buyEnteringTrader;
				}
				
				if (tran.sellFundManager != null && tran.sellFundManager.length() > 0 && operadores.get(tran.sellEnteringFirm + tran.sellFundManager) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.sellEnteringFirm + tran.sellFundManager).CodigoOperador;
				}
				else if (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.sellEnteringTrader;
				}
				
				
				tx.IdCorredoraContraparte = corredoras.get(tran.sellerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
				tx.IdBolsa = corredoraCompra.IdBolsa;
			}
			else if (corredoraVenta != null && empresa.IdCorredora.equals(corredoraVenta.IdCorredora)) {

				if (tran.sellFundManager != null && tran.sellFundManager.length() > 0 && operadores.get(tran.sellEnteringFirm + tran.sellFundManager) != null) {
					tx.CodigoOperadorCorredor = operadores.get(tran.sellEnteringFirm + tran.sellFundManager).CodigoOperador;
				}
				else if (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.sellEnteringTrader;
				}
				
				if (tran.buyFundManager != null && tran.buyFundManager.length() > 0 && operadores.get(tran.buyEnteringFirm + tran.buyFundManager) != null) {
					tx.CodigoOperadorContraparte = operadores.get(tran.buyEnteringFirm + tran.buyFundManager).CodigoOperador;
				}
				else if (!tran.buyerBroker.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.buyEnteringTrader;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.buyerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
				tx.IdBolsa = corredoraVenta.IdBolsa;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp+ " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			tx.Cantidad = tran.quantity;
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			tx.EsFungible ="N";
			
			
			String ordenDirecta = tran.orderType.trim();
			if(tran.buyerBroker.equalsIgnoreCase(tran.sellerBroker)){
				if (!tran.buyerBroker.equalsIgnoreCase(tran.buyEnteringFirm) || (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm)) ) {
					tx.IndicadorOrdenDirecta = "OE";	
				}
				else {
					tx.IndicadorOrdenDirecta = "OD";
				}
			}
			else {
				tx.IndicadorOrdenDirecta = "";
			}
			
			
			
			
			if (tran.shortSell.equalsIgnoreCase("YES")) {
				tx.EsVentaCorta = "S";	
			}
			else {
				tx.EsVentaCorta = "N";
			}
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.tradeTime)));
			
			tx.FechaEmision = "0001-01-01";
			
			tx.FechaLiquidacionContado = "0001-01-01";
			
			tx.FechaVencimiento = "0001-01-01";	
			tx.FolioTransaccion = tran.tradeID;
			tx.FolioTransaccionPlazo = "";
			
			SimpleDateFormat sdfTradeTimeHoraIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeHoraOUT = new SimpleDateFormat("HH:mm:ss");
			tx.HoraCreacion = sdfTradeTimeHoraOUT.format(sdfTradeTimeHoraIN.parse(tran.tradeTime));
			
			if (tran.settlementCondition.trim().equalsIgnoreCase("T+3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("CN")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("PM")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("PH")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
			
			if (tran.tradeCurrency != null) {
				tx.IdMoneda = tran.tradeCurrency;
				tx.IdSistemaBursatil = "MME";
			}
			
			tx.IdSistemaBursatil = "BCSTELEPREGON";
			if (tx.IdBolsa.equalsIgnoreCase("BEC")) {
				tx.IdSistemaBursatil = "BEC";
			}
			
			
			//txe.IdTipoOperacion;
			tx.IdTipoReajuste = "";
			//tx.IdMoneda = "CLP";  mas abajo se completa
			tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
			if (tx.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
				tx.MarcaNegociacion = tran.buyClientID;
				tx.MarcaNegociacionContraparte = tran.sellClientID; //cuando es OD siempre procesamos como compra
			}
			else {
				tx.MarcaNegociacion = tran.sellClientID;
			}
			
			
			tx.MejorPrecioCompra = tran.priceBuyingPrice;
			tx.MejorPrecioVenta = tran.priceSellingPrice;
			
			tx.Monto = tran.amount;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = BigDecimal.ZERO;
			tx.Nemotecnico = tran.instrument;	
			
			
			tx.NemotecnicoSVS = "";
			tx.NemotecnicoEspecificoBolson = "";
			tx.Plazo = 0L;
			tx.Precio = tran.price;
			tx.PrecioPlazo = BigDecimal.ZERO;
			tx.Tasa = BigDecimal.ZERO;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = BigDecimal.ZERO;

			//consultar si la transaccion es por ruteo.
			//SpadditionaltradeinformationResponse response = serviceAddTrxInfo.spadditionaltradeinformationExecute(BigDecimal.valueOf(Long.parseLong(tx.FolioTransaccion)), tx.FechaCreacion);
			tx.EsRuteo  =/* (response.getAdditionaltradesResult()!=null && !response.getAdditionaltradesResult().isEmpty() && response.getAdditionaltradesResult().get(0).getCONDCOD012().equalsIgnoreCase("C")) ? "S" :*/ "N";
			if (tx.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoAgente) || tx.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoRetail)) {
				tx.EsRuteo = "S";
			}
			tx.FlujoOrigen = "INGRESOORDEN";
			transacciones.add(tx);
			
		}
				
		//PALOS SIM
		for (TransaccionSIMMarketData_Entrada tran : entrada.transaccionesSIM) {
			
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "SIM";
			Corredora corredoraCompra = corredoras.get(tran.buyExecutingFirm.toString());
			Corredora corredoraVenta = corredoras.get(tran.sellExecutingFirm.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.buyExecutingFirm + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.sellExecutingFirm + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				
				if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.buyEnteringTrader;
				}
				
				if (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.sellEnteringTrader;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.sellExecutingFirm.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
				tx.IdBolsa = corredoraCompra.IdBolsa;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {

				if (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.sellEnteringFirm;
					tx.CodigoOperadorContraparte = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.sellEnteringTrader;
					tx.CodigoOperadorContraparte = tran.buyEnteringTrader;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.buyExecutingFirm.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
				tx.IdBolsa = corredoraVenta.IdBolsa;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.folio + " Fecha:" + tran.transactionDay + " " + tran.transactionTime + " C:" + tran.buyExecutingFirm + " V:" + tran.sellExecutingFirm;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			tx.Cantidad = new BigDecimal(tran.qty);
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			tx.EsFungible ="N";
			
			if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.sellExecutingFirm)) {
				tx.IndicadorOrdenDirecta = "";
			}
			else if(tran.buyExecutingFirm.equalsIgnoreCase(tran.sellExecutingFirm)){
				
				if (!tran.buyExecutingFirm.equalsIgnoreCase(tran.buyEnteringFirm) || (!tran.sellExecutingFirm.equalsIgnoreCase(tran.sellEnteringFirm)) ) {
					tx.IndicadorOrdenDirecta = "OE";	
				}
				else {
					tx.IndicadorOrdenDirecta = "OD";
				}
				
			}
			
			tx.EsVentaCorta = "N";
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.transactionDay)));
			
			tx.FechaEmision = "0001-01-01";
			
			tx.FechaLiquidacionContado = "0001-01-01";
			
			SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tx.FechaVencimiento = sdfExpirationDateOUT.format(sdfExpirationDateIN.parse(tran.expirationDate));
			
			tx.FolioTransaccion = tran.folio;
			tx.FolioTransaccionPlazo = (Integer.parseInt(tran.folio) + 1)+"";
			
			tx.HoraCreacion = tran.transactionTime;
			
			if (tran.settlementType.trim().equalsIgnoreCase("T+3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (tran.settlementType.trim().equalsIgnoreCase("CN")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementType.trim().equalsIgnoreCase("")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementType.trim().equalsIgnoreCase("PM")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (tran.settlementType.trim().equalsIgnoreCase("PH")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
			tx.IdSistemaBursatil = "BCSREMATESERIALIZADO";
			if (tx.IdBolsa.equalsIgnoreCase("BEC")) {
				tx.IdSistemaBursatil = "BEC";
			}
			
			//txe.IdTipoOperacion;
			tx.IdTipoInstrumento = "SIM";			
			
			tx.IdTipoReajuste = "";
			tx.IdMoneda = "CLP";
			tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
			
			if (tx.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
				tx.MarcaNegociacion = tran.buyClientID;
				tx.MarcaNegociacionContraparte = tran.sellClientID; //cuando es OD siempre procesamos como compra
			}
			else {
				tx.MarcaNegociacion = tran.sellClientID;
			}
						
			tx.MejorPrecioCompra = BigDecimal.ZERO;
			tx.MejorPrecioVenta = BigDecimal.ZERO;
			
			tx.Monto = tran.amount;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = tran.forwardPrice.multiply(tx.Cantidad);
			tx.Nemotecnico = tran.symbol;	
			
			
			tx.NemotecnicoSVS = "";
			tx.NemotecnicoEspecificoBolson = "";
			tx.Plazo = -1L; //-1 PARA QUE EL SERVICIO LO CALCULE
			tx.Precio = tran.spotPrice;
			tx.PrecioPlazo = tran.forwardPrice;
			tx.Tasa = tran.pubRate;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = BigDecimal.ZERO;
			tx.FlujoOrigen = "INGRESOORDEN";
			
			transacciones.add(tx);
			
		}
		
		//PALOS FUT
		for (TransaccionFUTMarketData_Entrada tran : entrada.transaccionesFUT) {
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "FUT";
			Corredora corredoraCompra = corredoras.get(tran.buyerBroker.toString());
			Corredora corredoraVenta = corredoras.get(tran.sellerBroker.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.buyerBroker + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.sellerBroker + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				
				tx.CodigoOperadorCorredor = tran.buyerBroker;
				tx.CodigoOperadorContraparte = tran.sellerBroker;
				
				tx.IdCorredoraContraparte = corredoras.get(tran.sellerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
				tx.IdBolsa = corredoraCompra.IdBolsa;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
				
				tx.CodigoOperadorCorredor = tran.buyerBroker;
				tx.CodigoOperadorContraparte = tran.sellerBroker;
				
				tx.IdCorredoraContraparte = corredoras.get(tran.buyerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
				tx.IdBolsa = corredoraVenta.IdBolsa;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp+ " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			tx.Cantidad = tran.quantity;
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			tx.EsFungible ="N";
			
			String ordenDirecta = tran.od.trim();
			if (ordenDirecta.equalsIgnoreCase(" ")) {
				tx.IndicadorOrdenDirecta = "";
			}
			else if(tran.buyerBroker.equalsIgnoreCase(tran.sellerBroker)){
				tx.IndicadorOrdenDirecta = "OD";
			}
			
			tx.EsVentaCorta = "N";
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.tradeTime)));
			
			tx.FechaEmision = "0001-01-01";
			
			tx.FechaLiquidacionContado = "0001-01-01";
			
			tx.FechaVencimiento = "0001-01-01";	
			tx.FolioTransaccion = tran.tradeID;
			tx.FolioTransaccionPlazo = "";
			
			SimpleDateFormat sdfTradeTimeHoraIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeHoraOUT = new SimpleDateFormat("HH:mm:ss");
			tx.HoraCreacion = sdfTradeTimeHoraOUT.format(sdfTradeTimeHoraIN.parse(tran.tradeTime));
			
			if (tran.settlement.trim().equalsIgnoreCase("T+3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (tran.settlement.trim().equalsIgnoreCase("CN")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlement.trim().equalsIgnoreCase("")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlement.trim().equalsIgnoreCase("PM")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (tran.settlement.trim().equalsIgnoreCase("PH")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = "";
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
				
			tx.IdSistemaBursatil = "BCSTELEPREGON";
			if (tx.IdBolsa.equalsIgnoreCase("BEC")) {
				tx.IdSistemaBursatil = "BEC";
			}
			
			tx.IdTipoReajuste = "";
			tx.IdMoneda = "CLP";
			tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
			tx.MarcaNegociacion = "";
			tx.MejorPrecioCompra = tran.priceBuyingPrice;
			tx.MejorPrecioVenta = tran.priceSellingPrice;
			
			tx.Monto = tran.amount;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = BigDecimal.ZERO;
			tx.Nemotecnico = tran.instrument;
			
			tx.NemotecnicoSVS = "";
			tx.NemotecnicoEspecificoBolson = "";
			tx.Plazo = 0L;
			tx.Precio = tran.price;
			tx.PrecioPlazo = BigDecimal.ZERO;
			tx.Tasa = BigDecimal.ZERO;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = BigDecimal.ZERO;
			tx.FlujoOrigen = "INGRESOORDEN";
			
			transacciones.add(tx);
		}
		
		//PALOS OP 
		for (TransaccionOPMarketData_Entrada tran : entrada.transaccionesOP) {
			
			Transaccion_Proceso tx = new Transaccion_Proceso();
			tx.IdInterno = tran.IdInterno;
			tx.EsModificacionMarketData = tran.EsModificacionMarketData;
			tx.EstadoModificacion = tran.EstadoModificacion;
			tx.IdTransaccionHistoricoAnulacion = tran.IdTransaccionHistoricoAnulacion;
			tx.OrigenTransaccion = "OP";
			Corredora corredoraCompra = corredoras.get(tran.buyerBroker.toString());
			Corredora corredoraVenta = corredoras.get(tran.sellerBroker.toString());
						
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tran.buyerBroker + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tran.sellerBroker + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
						
			if (corredoraCompra != null && empresa.IdCorredora.equals(corredoraCompra.IdCorredora)) {
				
				if (!tran.buyerBroker.equalsIgnoreCase(tran.buyEnteringFirm) && tran.buyEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.buyEnteringTrader;
				}
				
				if (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorContraparte = tran.sellEnteringFirm;
				}
				else {
					tx.CodigoOperadorContraparte = tran.sellEnteringTrader;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.sellerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_COMPRA;
				tx.IdBolsa = corredoraCompra.IdBolsa;
			}
			else if (corredoraVenta != null && empresa.IdCorredora.equals(corredoraVenta.IdCorredora)) {

				if (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm) && tran.sellEnteringFirm.trim().length() > 0) {
					tx.CodigoOperadorCorredor = tran.sellEnteringFirm;
					tx.CodigoOperadorContraparte = tran.buyEnteringFirm;
				}
				else {
					tx.CodigoOperadorCorredor = tran.sellEnteringTrader;
					tx.CodigoOperadorContraparte = tran.buyEnteringTrader;
				}
				
				tx.IdCorredoraContraparte = corredoras.get(tran.buyerBroker.toString()).IdCorredora;
				tx.AccionOrden = _c.ACCION_VENTA;
				tx.IdBolsa = corredoraVenta.IdBolsa;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp+ " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
				salida.P_OUT_MENSAJES.add(msj);
				logger.error(msj);
				continue;
			}
			
			tx.Cantidad = tran.quantity;
			tx.EsBilateral  = "N";
			tx.EsCuentaPropia = "N";
			tx.EsFueraRueda = "N";
			tx.EsFungible ="N";
			
			
			String ordenDirecta = tran.orderType.trim();
			if (ordenDirecta.equalsIgnoreCase(" ")) {
				tx.IndicadorOrdenDirecta = "";
			}
			else if(tran.buyerBroker.equalsIgnoreCase(tran.sellerBroker)){
				
				if (!tran.buyerBroker.equalsIgnoreCase(tran.buyEnteringFirm) || (!tran.sellerBroker.equalsIgnoreCase(tran.sellEnteringFirm)) ) {
					tx.IndicadorOrdenDirecta = "OE";	
				}
				else {
					tx.IndicadorOrdenDirecta = "OD";
				}
				
			}
			
			
			
			
			if (tran.shortSell.equalsIgnoreCase("YES")) {
				tx.EsVentaCorta = "S";	
			}
			else {
				tx.EsVentaCorta = "N";
			}
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.tradeTime)));
			
			tx.FechaEmision = "0001-01-01";
			
			tx.FechaLiquidacionContado = "0001-01-01";
			
			tx.FechaVencimiento = "0001-01-01";	
			tx.FolioTransaccion = tran.tradeID;
			tx.FolioTransaccionPlazo = "";
			
			SimpleDateFormat sdfTradeTimeHoraIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SimpleDateFormat sdfTradeTimeHoraOUT = new SimpleDateFormat("HH:mm:ss");
			tx.HoraCreacion = sdfTradeTimeHoraOUT.format(sdfTradeTimeHoraIN.parse(tran.tradeTime));
			
			
			if (tran.settlementCondition.trim().equalsIgnoreCase("T+3")) {
				tx.IdCondicionDeLiquidacionContado = "T3";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("CN")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("")) {
				tx.IdCondicionDeLiquidacionContado = "T2";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("PM")) {
				tx.IdCondicionDeLiquidacionContado = "T1";
			}
			else if (tran.settlementCondition.trim().equalsIgnoreCase("PH")) {
				tx.IdCondicionDeLiquidacionContado = "T0";
			}
			else {
				tx.IdCondicionDeLiquidacionContado = null;
			}
			
			tx.IdCorredoraCorresponsal = 0L;
			tx.IdEmpleado = 0L;
			tx.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			tx.IdInstrumentoSubyacente = 0L;
			tx.IdInterno = tx.IdInterno;
			tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
			tx.IdMonedaLiquidacionCorresponsal = "";
				
			tx.IdSistemaBursatil = "BCSTELEPREGON";
			if (tx.IdBolsa.equalsIgnoreCase("BEC")) {
				tx.IdSistemaBursatil = "BEC";
			}
			
			//txe.IdTipoOperacion;
			tx.IdTipoReajuste = "";
			tx.IdMoneda = "CLP";
			tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
			tx.MarcaNegociacion = "";
			tx.MejorPrecioCompra = tran.closingPrice;
			tx.MejorPrecioVenta = tran.closingPrice;
			
			tx.Monto = tran.amount;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.MontoPlazo = BigDecimal.ZERO;
			tx.Nemotecnico = tran.instrument;	
			
			
			tx.NemotecnicoSVS = "";
			tx.NemotecnicoEspecificoBolson = "";
			tx.Plazo = 0L;
			tx.Precio = tran.price;
			tx.PrecioPlazo = BigDecimal.ZERO;
			tx.Tasa = BigDecimal.ZERO;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			tx.ValorRescate = BigDecimal.ZERO;

			//consultar si la transaccion es por ruteo.
			//SpadditionaltradeinformationResponse response = serviceAddTrxInfo.spadditionaltradeinformationExecute(BigDecimal.valueOf(Long.parseLong(tx.FolioTransaccion)), tx.FechaCreacion);
			tx.EsRuteo  = /*(response.getAdditionaltradesResult()!=null && !response.getAdditionaltradesResult().isEmpty() && response.getAdditionaltradesResult().get(0).getCONDCOD012().equalsIgnoreCase("C")) ? "S" :*/ "N";
			tx.FlujoOrigen = "INGRESOORDEN";
			transacciones.add(tx);
		}
		
		if (transacciones.size() == 0) {
			return salida;
		}

		sql.batch(conn, SQL_INSERT_TRANSACCIONES_WRAPPER, transacciones, Transaccion_Proceso.class);
		
		//QUERY TIPO OPERACION, DIAS LIQUIDACION
		List<InfoTransaccion> _infoTransacciones = sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<>(), InfoTransaccion.class);
		HashMap<Long,Object> infoTransacciones = Utils.List2Hash(_infoTransacciones, "IdInterno");
		
		List<InfoTransaccion> _infoTransaccionesSimultanea = sql.query(conn, SQL_SELECT_SIMULTANEA, new HashMap<>(), InfoTransaccion.class);
		HashMap<Long,Object> infoTransaccionesSimultanea = Utils.List2Hash(_infoTransaccionesSimultanea, "IdInterno");
		
		HashMap<String,Object> params1 = new HashMap<>();
		params1.put("idEmpresa", entrada.p_IdEmpresa);
		List<LibroOrdenes_Proceso> libros = sql.query(conn, SQL_SELECT_LIBROS, params1, LibroOrdenes_Proceso.class);
		
		for (Transaccion_Proceso tx : transacciones) {
			
			InfoTransaccion info = (InfoTransaccion) infoTransacciones.get(tx.IdInterno);
			
			if (info == null) {
				String codigo = "Error al obtener instrumento " + tx.Nemotecnico;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo));
				continue;
			}
			
			//completamos la moneda
			if (tx.OrigenTransaccion.equalsIgnoreCase("RV")) {
				tx.IdMoneda = info.IdMoneda;
			}
			
			//SIMULTANEAS LAS MARCAMOS MAS ARRIBA
			if (tx.IdTipoInstrumento != null && tx.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
				
				InfoTransaccion infoSim = (InfoTransaccion) infoTransaccionesSimultanea.get(tx.IdInterno);
				if (infoSim == null) {
					String codigo = "Error al obtener instrumento simultanea";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo));
					continue;
				}
				
				tx.IdTipoOperacion = infoSim.IdTipoOperacion;
				tx.IdInstrumento = infoSim.IdInstrumento;
				tx.IdInstrumentoSubyacente = info.IdInstrumento;
				tx.MontoPlazo = MonedaFormato.formatear(conn, tx.MontoPlazo, MonedaFormato.MONTO, tx.IdMoneda).valor;
			}
			else {
				tx.IdTipoOperacion = info.IdTipoOperacion;
				tx.IdTipoInstrumento = info.IdTipoInstrumento;
				tx.IdInstrumento = info.IdInstrumento;
			}
			
			if (tx.Nemotecnico.equalsIgnoreCase("PDBC") || tx.Nemotecnico.equalsIgnoreCase("PRBC")) {
				tx.Cantidad = tx.ValorRescate.divide(info.CorteMinimo);
			}
			
			for (LibroOrdenes_Proceso lib : libros) {
				if (lib.CodigoOperador.equalsIgnoreCase(tx.CodigoOperadorCorredor)) {					
					//tx.IdLibroOrdenes = lib.IdLibroOrdenes;
					tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, lib.CodigoOperador, entrada.p_IdEmpresa);
					break;
				}
			}
			tx.Intereses = BigDecimal.ZERO;
			tx.Reajustes = BigDecimal.ZERO;
			tx.GastosCorresponsal = BigDecimal.ZERO;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
			
			if(info.IdMoneda != null && info.IdMoneda != "") {
				definirIndicadorBilateral(tx, ActualizaBilateralConPagoUSD, info.IdMoneda);
			}else {
				definirIndicadorBilateral(tx, ActualizaBilateralConPagoUSD, tx.IdMoneda);
			}
		}
		
		//filtramos transacciones con idtipooperacion == null
		
		CrearTransacciones serviceCrearTransacciones = new CrearTransacciones();
		
		CrearTransacciones_Entrada entradaCrearTransacciones = new CrearTransacciones_Entrada();
		entradaCrearTransacciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransacciones.p_IdModulo = _c.ID_MODULO_ORDENES;
		entradaCrearTransacciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransacciones.p_simularOperacion = _c.NO;
		entradaCrearTransacciones.p_UsaReglasNegocio = _c.SI;
		for (Transaccion_Proceso tx : transacciones) {
			
			if (tx.IdTipoOperacion == null) {
				logger.error(String.format("Ignorando transaccion %s %s %s",tx.Nemotecnico, tx.FolioTransaccion, tx.FechaCreacion));
			}
			else {
				entradaCrearTransacciones.p_Transacciones.add(tx);	
			}
			
			
		}
		CrearTransacciones_Salida salidaCrearTransacciones = serviceCrearTransacciones.logica(conn, entradaCrearTransacciones );
		
		for (cl.bcs.ordenes.CrearTransacciones.Resultado_Salida resp : salidaCrearTransacciones.P_OUT_RESULTADO) {
			Resultado_Salida resp2 = new Resultado_Salida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_RESULTADO.add(resp2);
		}
		
		for (MensajeSalida resp : salidaCrearTransacciones.P_OUT_MENSAJES) {
			MensajeSalida resp2 = new MensajeSalida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_MENSAJES.add(resp2);
		}
		
		return salida;
	}
	
	public static class TransaccionIFMarketData_Entrada extends TransaccionIFMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class TransaccionRVMarketData_Entrada extends TransaccionRVMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class TransaccionRFMarketData_Entrada extends TransaccionRFMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class TransaccionSIMMarketData_Entrada extends TransaccionSIMMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class TransaccionFUTMarketData_Entrada extends TransaccionFUTMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class TransaccionOPMarketData_Entrada extends TransaccionOPMarketData {
		public Long IdInterno;
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion = "PRO";
		public Long IdTransaccionHistoricoAnulacion = 0L;
	}
	
	public static class CrearTransaccionesMarketData_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<TransaccionIFMarketData_Entrada> transaccionesIF;
		public List<TransaccionRVMarketData_Entrada> transaccionesRV;
		public List<TransaccionRFMarketData_Entrada> transaccionesRF;
		public List<TransaccionSIMMarketData_Entrada> transaccionesSIM;
		public List<TransaccionFUTMarketData_Entrada> transaccionesFUT;
		public List<TransaccionOPMarketData_Entrada> transaccionesOP;
		
		public CrearTransaccionesMarketData_Entrada() {
			this.transaccionesIF = new ArrayList<>();
			this.transaccionesRV = new ArrayList<>();
			this.transaccionesRF = new ArrayList<>();
			this.transaccionesSIM = new ArrayList<>();
			this.transaccionesFUT = new ArrayList<>();
			this.transaccionesOP = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdTransaccion;
		public String IdTipoOperacion;
		public String EsAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearTransaccionesMarketData_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CrearTransaccionesMarketData_Salida() {
			this.P_OUT_RESULTADO = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LibrosOrdenes {
		//public Long IdLibroOrdenesOperador;
		public Long IdEmpresa;
		public Long IdLibroOrdenes;
		public String CodigoOperador;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
			
	

		}	
	}
	
	private void definirIndicadorBilateral(Transaccion_Proceso tx, String actualizaBilateralConPagoUSD, String idMoneda){
		logger.trace("INICIO::CrearTransaccionesMarketData.definirIndicadorBilateral");
		long inicio = System.currentTimeMillis();
		
		//RV
		if(_c.CLASIFICACION_RV.equalsIgnoreCase(tx.OrigenTransaccion) ) {
			
			//MILA
			if(TIPO_INSTRUMENTO_MIL.equalsIgnoreCase(tx.IdTipoInstrumento)) {
				if("BVL".equalsIgnoreCase(tx.IdBolsa) || "BVC".equalsIgnoreCase(tx.IdBolsa) || "BMV".equalsIgnoreCase(tx.IdBolsa)) {
					tx.EsBilateral = "S";
				}
			}else {
				if("CLP".equalsIgnoreCase(idMoneda)) {
					if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T3".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "S";
					}
					if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "N";
					}
				}
				if("USD".equalsIgnoreCase(idMoneda)) {
					tx.EsBilateral = "S";
					if("S".equalsIgnoreCase(actualizaBilateralConPagoUSD)) {
						if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
							tx.EsBilateral = "N";
						}
					}
				}
			}
		}
		
		//SIM
		if(_c.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(tx.OrigenTransaccion)) {
			if("CLP".equalsIgnoreCase(idMoneda)) {
				if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "N";
				}
				if("T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "S";
				}
			}
			if("USD".equalsIgnoreCase(idMoneda)) {
				tx.EsBilateral = "S";
				if("S".equalsIgnoreCase(actualizaBilateralConPagoUSD)) {
					if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "N";
					}
					if("T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "S";
					}
				}
			}
		}
		
		//RF 
		if(_c.CLASIFICACION_RF.equalsIgnoreCase(tx.OrigenTransaccion)) {
			if("CLP".equalsIgnoreCase(idMoneda)) {
				if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "N";
				}
				if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T3".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "S";
				}
			}
			if("USD".equalsIgnoreCase(idMoneda)) {
				tx.EsBilateral = "S";
				if("S".equalsIgnoreCase(actualizaBilateralConPagoUSD)) {
					if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "N";
					}
					if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T3".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "S";
					}
				}
			}
		}
		
		//IF
		if(_c.CLASIFICACION_IF.equalsIgnoreCase(tx.OrigenTransaccion)) {
			if("CLP".equalsIgnoreCase(tx.IdMoneda)) {
				if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "N";
				}
				if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T3".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
					tx.EsBilateral = "S";
				}
			}
			if("USD".equalsIgnoreCase(tx.IdMoneda)) {
				tx.EsBilateral = "S";
				if("S".equalsIgnoreCase(actualizaBilateralConPagoUSD)) {
					if("T0".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T1".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "N";
					}
					if("T2".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado) || "T3".equalsIgnoreCase(tx.IdCondicionDeLiquidacionContado)) {
						tx.EsBilateral = "S";
					}
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTransaccionesMarketData.definirIndicadorBilateral tiempo: " + (fin - inicio));
	}
}
	