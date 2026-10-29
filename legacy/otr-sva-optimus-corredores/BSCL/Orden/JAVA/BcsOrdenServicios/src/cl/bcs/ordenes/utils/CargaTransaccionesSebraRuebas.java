package cl.bcs.ordenes.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;

import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionIFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRVMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionSIMMarketData_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;

public class CargaTransaccionesSebraRuebas {

	public static List<TransaccionSIMMarketData_Entrada> loadPalosSIM(List<ObjTBRVTR> palos) throws ParseException {
		
		List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
		Long index = 10000L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBRVTR> palosPH = new HashMap<>();
		HashMap<String,ObjTBRVTR> palosTP = new HashMap<>();
		
		for (ObjTBRVTR lePalo: palos) {
			
			//eliminamos palos repetidos
			if (palosPH.containsKey(lePalo.FOLIO + lePalo.FECHA)) {
				continue;
			}
			
			
			//es simultanea
			if (!lePalo.COND_COD_004.trim().equalsIgnoreCase("S")) {
				continue;
			}
			
			index++;
			
			String llave = lePalo.FECHA + lePalo.FOLIO;
			if (!lePalo.COND_COD_009.trim().equalsIgnoreCase("S")) {
				palosPH.put(llave, lePalo);
			}
			else {
				palosTP.put(llave, lePalo);	
			}
			
			
			
		}
		
		
		for (ObjTBRVTR lePalo: palosPH.values()) {

			TransaccionSIMMarketData_Entrada tran = new TransaccionSIMMarketData_Entrada();
			
			tran.IdInterno = index++;
			boolean esOD = lePalo.COD_CORR_COM.equals(lePalo.COD_CORR_VEN);
			
			tran.action= "I";
			String llave = lePalo.FECHA + lePalo.FOLIO_SIMUL;
			ObjTBRVTR lePaloTP = palosTP.get(llave);
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.buyExecutingFirm= "0" + lePalo.COD_CORR_COM;
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C") && !lePalo.COD_OPE_COM.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= lePalo.COD_OPE_COM+"";
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyEnteringTrader= lePalo.OPE_INT_COM+"";
			}
			else {
				tran.buyEnteringTrader= "";
			}
			

			//corredor vende
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || esOD ) {
				tran.sellExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.sellExecutingFirm= "0" + lePalo.COD_CORR_VEN;
			}
			
			if (esOD || (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") && !lePalo.COD_OPE_VEN.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero))) {
				tran.sellEnteringFirm= lePalo.COD_OPE_VEN+"";
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || esOD) {
				tran.sellEnteringTrader= lePalo.OPE_INT_VEN+"";
			}
			else {
				tran.sellEnteringTrader= "";
			}
			
			tran.qty= lePalo.CANTIDAD+"";
			tran.symbol= lePalo.NEMO.trim();
			tran.spotPrice= lePalo.PRECIO;
			tran.forwardPrice= lePaloTP.PRECIO;
			tran.maxRate= BigDecimal.ZERO;
			tran.pubRate= lePalo.TASA;
			
			if (lePalo.COND_DIS.trim().equalsIgnoreCase("")) {
				tran.settlementType = "";
			}
			else if (lePalo.COND_DIS.trim().contains("PM")) {
				tran.settlementType = "PM";
			}
			else if (lePalo.COND_DIS.trim().contains("PH")) {
				tran.settlementType = "PH";
			}
			else if (lePalo.COND_DIS.trim().contains("TP")) {
				continue;
			}
			
			SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tran.expirationDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lePaloTP.FEC_LIQ));
			
			tran.amount= lePalo.MONTO;
			tran.transactionTime= "09:00:00";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.transactionDay = dt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			tran.transactionTime = dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			
			
			tran.prepaid= "";
			tran.folio= lePalo.FOLIO.toString();
			tran.bestStance= "";
			tran.divisible= "";
			tran.modified= "";
			tran.codIsin= "";
			tran.securityType= "";
			tran.securityExchange= "";
			tran.orderID= "";
			tran.buyClientID = lePalo.CLIENTE_COM;
			tran.sellClientID = lePalo.CLIENTE_VEN;
			
			
			palosMarketDataSIM.add(tran);
			
		}
		
		return palosMarketDataSIM;
	}
	
	public static List<TransaccionRFMarketData_Entrada> loadPalosRF(List<ObjTBRFTR> palosRF) throws ParseException {
		
		List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBRFTR> palosCalculados = new HashMap<>();
		
		for (ObjTBRFTR palo: palosRF) {
						
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(palo.hora + palo.FOLIO)) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(palo.hora + palo.FOLIO, palo);
			
			
		}
		
		for (ObjTBRFTR lePalo : palosCalculados.values()) {

			
			TransaccionRFMarketData_Entrada tran = new TransaccionRFMarketData_Entrada();
			
			boolean esOD = lePalo.COD_CORR_COM.equals(lePalo.COD_CORR_VEN);
			tran.IdInterno = index++;
			tran.codTicker = "TRADE";
			tran.action = "I";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.tradeTime = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.tradeID = lePalo.FOLIO.toString();
			tran.symbol = lePalo.NEMO.trim();
			tran.condIsin = "";
			tran.quantity = lePalo.CANTIDAD;
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.buyExecutingFirm= "0" + lePalo.COD_CORR_COM;
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C") && !lePalo.COD_OPE_COM.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= lePalo.COD_OPE_COM+"";
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.traderBuy= lePalo.OPE_INT_COM+"";
			}
			else {
				tran.traderBuy= "";
			}
			

			//corredor vende
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || esOD) {
				tran.sellExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.sellExecutingFirm= "0" +  lePalo.COD_CORR_VEN;
			}
			
			if (esOD || (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") && !lePalo.COD_OPE_VEN.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero))) {
				tran.sellEnteringFirm= lePalo.COD_OPE_VEN+"";
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.toString().equalsIgnoreCase("V") || esOD) {
				tran.traderSell= lePalo.OPE_INT_VEN+"";
			}
			else {
				tran.traderSell= "";
			}
			
			String condliq = "";
			if (lePalo.COND_DIS.trim().contains("PH")) {
				condliq = "H";
				
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.settlDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lePalo.FECHA));
			}
			else if (lePalo.COND_DIS.trim().contains("PM")) {
				condliq = "M";
				
				tran.settlDate = fechaPM;
			}
			else {
				condliq = "";
				
				tran.settlDate = fechaCN;
			}
			
			tran.tradeCondition = " " + (condliq.length()==1?condliq:" ")+ "     " + (esOD?"O":" ");
			tran.condition = "";
			tran.amount = lePalo.MONTO;
			tran.term = "";
			tran.coraMaturity = "";
			tran.price = lePalo.PRECIO;					
			tran.auctionId = lePalo.REMATE;
			tran.tir = lePalo.TIR;
			tran.signTir = "";
			tran.calculationType = "";
			tran.dcvTitle = "N";					
			
			tran.valAdjustmentCurrency = "";
			tran.currency = "";
			tran.currencySettlementAmount = lePalo.MONTO;
			tran.duration = BigDecimal.ZERO;
			tran.family = "";
			tran.buyerFund = "";
			tran.sellerFund = "";
			
			
			palosMarketDataRF.add(tran);
			
		}
		
		return palosMarketDataRF;
	}
	
	public static List<TransaccionIFMarketData_Entrada> loadPalosIF(List<ObjTBIFTR> palosIF) throws ParseException {
		
		List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBIFTR> palosCalculados = new HashMap<>();
		
		for (ObjTBIFTR palo: palosIF) {
						
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(palo.HORA + palo.FOLIO)) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(palo.HORA + palo.FOLIO, palo);
			
			
		}
		
		for (ObjTBIFTR lepalo : palosCalculados.values()) {

			//FEC_TRANS(0);FOLIO_TRANS(1);IND_BOLSA(2);TIPO_OPERAC(3);FECHA(4);EMISOR(5);TIPO_REAJ(6);FEC_VCTO(7);COD_CORR_CONTRA(8);COD_OPE_EXT(9);FEC_DIG(10);FEC_EMI(11);IND_ASIGNAC(12);IND_COMPROMISO(13);
			//IND_CTA_PRO(14);IND_DCV(15);IND_LAMINA(16);IND_LIQUID(17);IND_LUGAR_EMI(18);IND_MADUREZ;IND_MODIF;IND_OD(21);IND_RESERVADO;MONTO(23);MONTO_CP;NEMO(25);PLAZO(26);RUEDA(27);TASA(28);VAL_RESC(29);
			//VAL_RESC_ASIG;OPE_INT(31);SEC_MOVTO_FAC;IND_FUNGIBILIDAD;FEC_VCTO_TP;IND_SIMUL;TASA_SIMUL;FOLIO_SIMUL;TASA_EMISION;SPREAD_TASA_EMISION;MODALIDAD_PAGO;
			//PERIODICIDAD_PAGO;FECHA_EMISION;PARIDAD_MORIGEN;CAPTACION_MORIGEN;CODCATIFRSID;MON_MORIGEN;
			//FCREOF_COM;FCREOF_VEN;CAN_COM;PRECIO_COM;BOLSA_COM;CAN_VEN;PRECIO_VEN;BOLSA_VEN
			TransaccionIFMarketData_Entrada tran = new TransaccionIFMarketData_Entrada();
			boolean esOD = lepalo.COD_CORR_COM.equals(lepalo.COD_CORR_VEN)?true:false;
			
			tran.IdInterno = index++;
			tran.CODTICKER = "TRADE";
			tran.ACTION = "I";
			
			LocalDate datePart = LocalDate.parse(lepalo.HORA.substring(0,10));
			LocalTime timePart = LocalTime.parse(lepalo.HORA.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.TRADETIME = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.TRADEID = lepalo.FOLIO + "";
			tran.SYMBOL = lepalo.NEMO.trim();
			tran.CONDISIN = "";
			
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.BUYEXECUTINGFIRM= codigoCuentaPropiaConCero;
			}
			else {
				tran.BUYEXECUTINGFIRM= "0" + lepalo.COD_CORR_COM;
			}
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("C") && !lepalo.COD_OPE_COM.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.BUYENTERINGFIRM= lepalo.COD_OPE_COM+"";
			}
			else {
				tran.BUYENTERINGFIRM= "";
			}
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.TRADERBUY= lepalo.OPE_INT_COM+"";
			}
			else {
				tran.TRADERBUY= "";
			}
			

			//corredor vende
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("V")) {
				tran.SELLEXECUTINGFIRM = codigoCuentaPropiaConCero;
			}
			else {
				tran.SELLEXECUTINGFIRM = "0" +  lepalo.COD_CORR_VEN.toString().trim();
			}
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("V") && !lepalo.COD_OPE_VEN.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.SELLENTERINGFIRM = lepalo.COD_OPE_VEN+"";
			}
			else {
				tran.SELLENTERINGFIRM = "";
			}
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("V")) {
				tran.TRADERSELL= lepalo.OPE_INT_VEN+"";
			}
			else {
				tran.TRADERSELL= "";
			}
			
			String condliq = "";
			
			SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			if (lepalo.COND_DIS.trim().contains("PH")) {
				condliq = "H";
				
				
				tran.SETTLDATE = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lepalo.FECHA));
			}
			else if (lepalo.COND_DIS.trim().contains("PM")) {
				condliq = "M";
				//fechaCarga2
				Date fechaCargaParsed = sdfExpirationDateIN.parse(fechaCarga2);
				tran.SETTLDATE = fechaPM;
			}
			else {
				//t2
				condliq = "";
				tran.SETTLDATE = fechaCN;
			}
			
			
			tran.TRADECONDITION = " " + (condliq.length()==1?condliq: " ") + "     " + (esOD?"O":" ");
			tran.CONDITION = "";
			tran.LOCALAMOUNT = lepalo.MONTO; //44
			tran.AMOUNT = lepalo.MONTO;
			tran.TERM = 0;
			tran.AUCTIONID = lepalo.REMATE;
			tran.RATE = lepalo.TASA;
			tran.SIGNRATE = "";
			tran.SYMBOLSVS = "";
			tran.DCVTITLE = "";
			
			{
				sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.EXPIRETIME = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lepalo.FEC_VCTO));
			}
			
			tran.ISSUER = lepalo.EMISOR.trim();
			tran.RESETTYPE = lepalo.TIPO_REAJ;
			tran.RESCUEVALUE = lepalo.VAL_RESC;
			
			
			palosMarketDataIF.add(tran);
			
		}
		
		return palosMarketDataIF;
	}
	
	public static List<TransaccionRVMarketData_Entrada> loadPalosRV(List<ObjTBRVTR> palos) {
		
		List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBRVTR> palosCalculados = new HashMap<>();
		
		for (ObjTBRVTR palo: palos) {
			
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(palo.FOLIO + palo.FECHA)) {
				continue;
			}
			
			
			//es simultanea
			if (palo.COND_COD_004.trim().equalsIgnoreCase("S")) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(palo.FOLIO + palo.FECHA, palo);
			
		}
		
		for (ObjTBRVTR lePalo : palosCalculados.values()) {

			
			TransaccionRVMarketData_Entrada tran = new TransaccionRVMarketData_Entrada();
			
			
			tran.IdInterno = index++;
			tran.codTicker= "TRADE";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.timeStamp = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.action= "I";
			tran.tradeTime= tran.timeStamp;
			tran.tradeID= lePalo.FOLIO.toPlainString();
			tran.instrument= lePalo.NEMO.trim();
			tran.market= "XSGO";
			if (lePalo.COND_DIS.trim().contains("PM")) {
				tran.settlementCondition = "PM";
			}
			else if (lePalo.COND_DIS.trim().contains("PH")) {
				tran.settlementCondition = "PH";
			}
			else if (lePalo.COND_DIS.trim().equalsIgnoreCase("")) {
				tran.settlementCondition = "";
			}
			else if (lePalo.COND_DIS.trim().contains("TP")) {
				continue;
			}
			else {
				tran.settlementCondition = "";
			}
			
			tran.orderType= lePalo.COD_CORR_COM.equals(lePalo.COD_CORR_VEN)?"OD":"";
			tran.indivisible= "";
			tran.shortSell= lePalo.COND_COD_004.trim().equalsIgnoreCase("V")?"YES":"NO";
			tran.buyClientID = "";
			tran.sellClientID = "";
			tran.quantity= lePalo.CANTIDAD;
			tran.price= lePalo.PRECIO;
			tran.amount= lePalo.MONTO;
			tran.closingPrice= BigDecimal.ZERO;
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyerBroker= codigoCuentaPropiaConCero;
			}
			else {
				
				if (lePalo.COD_CORR_COM.toString().length() == 3) {
					tran.buyerBroker= lePalo.COD_CORR_COM+"";
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 2) {
					tran.buyerBroker = "0" + lePalo.COD_CORR_COM;
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 1) {
					tran.buyerBroker = "00" + lePalo.COD_CORR_COM;
				}
				
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C") && !lePalo.COD_OPE_COM.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= lePalo.COD_OPE_COM+"";
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyEnteringTrader= lePalo.OPE_INT_COM+"";
			}
			else {
				tran.buyEnteringTrader= "";
			}
			

			//corredor vende
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || tran.orderType.equals("OD")) {
				tran.sellerBroker= codigoCuentaPropiaConCero;
			}
			else {
				if (lePalo.COD_CORR_VEN.toString().length() == 3) {
					tran.sellerBroker= lePalo.COD_CORR_VEN+"";
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 2) {
					tran.sellerBroker = "0" + lePalo.COD_CORR_VEN;
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 1) {
					tran.sellerBroker = "00" + lePalo.COD_CORR_VEN;
				}
			}
			
			if (tran.orderType.equals("OD") || (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") && !lePalo.COD_OPE_VEN.toString().equalsIgnoreCase(codigoCuentaPropiaSinCero))) {
				tran.sellEnteringFirm= lePalo.COD_OPE_VEN+"";
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || tran.orderType.equals("OD")) {
				tran.sellEnteringTrader= lePalo.OPE_INT_VEN+"";
			}
			else {
				tran.sellEnteringTrader= "";
			}
			
			
			tran.priceBuyingQTY= BigDecimal.ZERO;
			tran.priceBuyingPrice= BigDecimal.ZERO;;
			tran.priceSellingQty= BigDecimal.ZERO;
			tran.priceSellingPrice= BigDecimal.ZERO;
			tran.priceExchange= "XSGO";
			tran.timeInForce= "";
			tran.entryType= "";
			tran.tradingSessionID= "";
			tran.codIsin= "";
			tran.securityType= "";
			tran.priceTime= "";
			tran.buyClientID = lePalo.CLIENTE_COM.trim();
			tran.sellClientID = lePalo.CLIENTE_VEN.trim();
			
			palosMarketDataRV.add(tran);
			
		}
		
		return palosMarketDataRV;
	}
	
	public static class ObjTBRVTR extends BcsPojo{
		public String MERCADO;
		public String hora;
		public BigDecimal FOLIO;
		public String IND_BOLSA = "BCS";
		public String TIPO_OPERAC;
		public String FECHA;
		public String NEMO;
		public BigDecimal CANTIDAD;
		public String COND_DIS;
		public BigDecimal PRECIO;
		public BigDecimal MONTO;
		public BigDecimal COD_CORR_COM;
		public BigDecimal COD_CORR_VEN;
		public BigDecimal COD_OPE_COM;
		public BigDecimal COD_OPE_VEN;
		public BigDecimal OPE_INT_COM;
		public BigDecimal OPE_INT_VEN;
		public String CLIENTE_COM;
		public String CLIENTE_VEN;
		public String COND_COD_004; //S SIMULTANEA, V VENTA CORTA
		public String FEC_LIQ;
		public BigDecimal TASA;
		public BigDecimal FOLIO_SIMUL;
		public String COND_COD_009; //S, PUNTA A PLAZO EN SIM
	}
	
	public static class ObjTBRFTR extends BcsPojo{
		public String hora;
		public BigDecimal FOLIO;
		public String IND_BOLSA = "BCS";
		public String TIPO_OPERAC;
		public String FECHA;
		public String NEMO;
		public BigDecimal CANTIDAD;
		public String COND_DIS;
		public BigDecimal PRECIO;
		public BigDecimal MONTO;
		public BigDecimal COD_CORR_COM;
		public BigDecimal COD_CORR_VEN;
		public BigDecimal COD_OPE_COM;
		public BigDecimal COD_OPE_VEN;
		public BigDecimal OPE_INT_COM;
		public BigDecimal OPE_INT_VEN;
		public String CLIENTE_COM;
		public String CLIENTE_VEN;
		public String COND_COD_004;
		public BigDecimal TIR;
		public String MONEDA;
		public String MONEDA_LIQ;
		public String REMATE;
	}
	
	public static class ObjTBIFTR extends BcsPojo{
		
		public String HORA;
		public BigDecimal FOLIO;
		public String TIPO_OPERAC;
		public String FECHA;
		public String NEMO;
		public BigDecimal VAL_RESC;
		public String EMISOR;
		public String FEC_VCTO;
		public BigDecimal COD_CORR_COM;
		public BigDecimal COD_CORR_VEN;
		public BigDecimal COD_OPE_COM;
		public BigDecimal COD_OPE_VEN;
		public BigDecimal OPE_INT_COM;
		public BigDecimal OPE_INT_VEN;
		public String COND_DIS;
		public String FEC_EMI;
		public BigDecimal MONTO;
		public BigDecimal PLAZO;
		public String REMATE;
		public String TIPO_REAJ;
		public BigDecimal TASA;
	}
	
	
	static String tbrvtrquery = "select tbrvtr.MERCADO,tbrvtr.hora,tbrvtr.FOLIO,'BCS' as IND_BOLSA, case when COD_CORR_COM = :codigo THEN 'C' WHEN COD_CORR_VEN = :codigo THEN 'V' END AS TIPO_OPERAC, "
			+ "tbrvtr.FECHA,tbrvtr.NEMO,tbrvtr.CANTIDAD ,tbrvtr.COND_DIS,tbrvtr.PRECIO ,tbrvtr.MONTO,COD_CORR_COM ,COD_CORR_VEN ,COD_OPE_COM ,COD_OPE_VEN ,OPE_INT_COM ,OPE_INT_VEN, "
			+ "COALESCE(CLIENTE_COM,'') AS CLIENTE_COM,COALESCE(CLIENTE_VEN,'') AS CLIENTE_VEN,COND_COD_004,FEC_LIQ,TASA,FOLIO_SIMUL,COND_COD_009 "
			+ "from tbrvtr "
			+ "left join TBRVTRBO on TBRVTRBO.folio = tbrvtr.FOLIO and tbrvtr.HORA  = tbrvtrbo.HORA "
			+ "where (COD_CORR_COM = :codigo OR COD_CORR_VEN = :codigo) and tbrvtr.fecha= :fecha and TBRVTR.MERCADO != 'MO'";
	
	static String tbrftrquery = "select tbrftr.hora,tbrftr.FOLIO,'BCS' as IND_BOLSA, case when COD_CORR_COM = :codigo THEN 'C' WHEN COD_CORR_VEN = :codigo THEN 'V' END AS TIPO_OPERAC,"
			+ "tbrftr.FECHA,tbrftr.NEMO,tbrftr.CANTIDAD ,tbrftr.COND_DIS,tbrftr.PRECIO ,tbrftr.MONTO,COD_CORR_COM ,COD_CORR_VEN ,COD_OPE_COM ,COD_OPE_VEN ,OPE_INT_COM ,OPE_INT_VEN,COND_COD_004,TIR,"
			+ "MONEDA,MONEDA_LIQ,REMATE "
			+ "from tbrftr "
			+ "where (COD_CORR_COM = :codigo OR COD_CORR_VEN = :codigo) and tbrftr.fecha= :fecha";
	
	static String tbiftrquery = "select tbiftr.HORA,tbiftr.FOLIO, case when COD_CORR_COM =  :codigo THEN 'C' WHEN COD_CORR_VEN =  :codigo THEN 'V' END AS TIPO_OPERAC,"
			+ "tbiftr.FECHA ,tbiftr.NEMO ,tbiftr.VAL_RESC ,tbiftr.EMISOR ,tbiftr.FEC_VCTO ,COD_CORR_COM ,COD_CORR_VEN ,COD_OPE_COM ,COD_OPE_VEN ,OPE_INT_COM ,OPE_INT_VEN,COND_DIS,"
			+ "FEC_EMI ,MONTO ,PLAZO ,REMATE ,TIPO_REAJ,TASA "
			+ "from tbiftr "
			+ "where (COD_CORR_COM = :codigo OR COD_CORR_VEN = :codigo) and tbiftr.fecha = :fecha";
	
	public static final String codigoCuentaPropiaConCero = "062";
	public static final String codigoCuentaPropiaSinCero = "62";
	private static final long DIFERENCIA_HORARIA = 3;
	private static final String fechaCarga = "2025-03-27";
	private static final String fechaCarga2 = "20250327";
	private static final String fechaPM = "20250328";
	private static final String fechaCN = "20250329";
	
	
	public static void main(String [] args) throws Exception {
		
		List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
		List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
		List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
		List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
		
		Properties prop = new Properties();
    	InputStream input = null;
    	try {
    		input = new FileInputStream("ruebas.properties");
    	}
    	catch (FileNotFoundException e) {
    		input = new FileInputStream("ruebas.properties");
    	}
        prop.load(input);
        
        String DBdriver,DBuser,DBpass,DBurl,DBprop;
        
        DBdriver=prop.getProperty("DBdriver");
        DBuser=prop.getProperty("DBuser");
        DBpass=prop.getProperty("DBpass");
        DBurl=prop.getProperty("DBurl");
        DBprop=prop.getProperty("DBprop");
        input.close();
		SQL _sql = new SQL();
		
		BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
           
    	}catch(Exception e){
    		
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        	
        } 
    	
    	HashMap<String,Object> params = new HashMap<String, Object>();
    	params.put("codigo", codigoCuentaPropiaSinCero);
    	params.put("fecha", fechaCarga);
    	
    	List<ObjTBRVTR> palosRV = _sql.query(dsTarget.getConnection(), tbrvtrquery,params,ObjTBRVTR.class);
    	List<ObjTBRFTR> palosRF = _sql.query(dsTarget.getConnection(), tbrftrquery,params,ObjTBRFTR.class);
    	List<ObjTBIFTR> palosIF = _sql.query(dsTarget.getConnection(), tbiftrquery,params,ObjTBIFTR.class);
    	
		palosMarketDataRV = CargaTransaccionesSebraRuebas.loadPalosRV(palosRV);
		palosMarketDataSIM = CargaTransaccionesSebraRuebas.loadPalosSIM(palosRV);
		palosMarketDataRF = CargaTransaccionesSebraRuebas.loadPalosRF(palosRF);
		palosMarketDataIF = CargaTransaccionesSebraRuebas.loadPalosIF(palosIF);
		
		CrearTransaccionesMarketData ccc = new CrearTransaccionesMarketData();
		try(Connection conn = PlataformaUtils.getPropertiesConnection("mbi", "connections.properties")){
			
			List<Usuario> usuarioSistema = _sql.query(conn, "SELECT \"IdUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"CodigoUsuario\" = 'SISTEMA'", 
					new HashMap<>(), Usuario.class);
			CrearTransaccionesMarketData_Entrada entradaPalos = new CrearTransaccionesMarketData_Entrada();
			CrearTransaccionesMarketData_Salida salida = null;
			//RV
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = palosMarketDataRV;
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			salida = ccc.logica(conn, entradaPalos );
			System.out.println(salida.P_OUT_RESULTADO);
			conn.commit();
			return;
			//RF
			/*entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = palosMarketDataRF;
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			salida = ccc.logica(conn, entradaPalos );
			
			
			//IF
			entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = palosMarketDataIF;
			
			salida = ccc.logica(conn, entradaPalos );
			
			
			//SIM
			entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = palosMarketDataSIM;
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			salida = ccc.logica(conn, entradaPalos );
			conn.commit();*/
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static class Usuario {
		public Long IdUsuario;
	}
	
	static String loadResourceToString(final String path){		
	    final InputStream stream = CargaTransaccionesSebraRuebas.class.getClassLoader().getResourceAsStream(path);
	    if (stream==null) {
	    	throw new IllegalStateException("can't load resource:"+path);
	    }
	    try{
	        return IOUtils.toString(stream);
	    } catch(final Exception e){
	        throw new IllegalStateException(e);
	    } finally{
	        IOUtils.closeQuietly(stream);
	    }
	}	
	
	
	

	
}
