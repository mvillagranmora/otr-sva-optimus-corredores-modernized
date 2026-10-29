package cl.bcs.ordenes.utils;

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

import org.apache.commons.io.IOUtils;

import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionIFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRVMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionSIMMarketData_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;

public class CargaTransaccionesSebra {

	
	public static final String codigoCuentaPropiaConCero = "085";
	public static final String codigoCuentaPropiaSinCero = "85";
	private static final long DIFERENCIA_HORARIA = 3;
	public static String liqCN = "20230507";
	public static String liqPM = "20230506";
	
	public static List<TransaccionSIMMarketData_Entrada> loadPalosSIM(String palos) throws ParseException {
		
		List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
		Long index = 10000L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,String[]> palosPH = new HashMap<>();
		HashMap<String,String[]> palosTP = new HashMap<>();
		
		for (String palo: palos.split("\r\n")) {
			String[] campos = palo.split(";");
			if (campos.length < 3)
				continue;
			
			if (index == 0) {
				index++;
				continue;
			}
			
			//eliminamos palos repetidos
			if (palosPH.containsKey(campos[1] + campos[2])) {
				continue;
			}
			
			
			//es simultanea
			if (!campos[23].trim().equalsIgnoreCase("TS")) {
				continue;
			}
			
			index++;
			
			String llave = campos[5] + campos[2].trim();
			if (!campos[19].equalsIgnoreCase("TP")) {
				palosPH.put(llave, campos);
			}
			else {
				palosTP.put(llave, campos);	
			}
			
			
			
		}
		
		
		for (String[] campos: palosPH.values()) {

			TransaccionSIMMarketData_Entrada tran = new TransaccionSIMMarketData_Entrada();
			
			tran.IdInterno = index++;
			
			tran.action= "I";
			String llave = campos[5] + campos[16].trim();
			String[] camposTP = palosTP.get(llave);
			
			if (campos[4].equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.buyExecutingFirm= "0" + campos[11];
			}
			
			if (campos[4].equalsIgnoreCase("C") && !campos[12].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= campos[12];
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (campos[4].equalsIgnoreCase("C")) {
				tran.buyEnteringTrader= campos[29];
			}
			else {
				tran.buyEnteringTrader= "";
			}
			

			//corredor vende
			
			if (campos[4].equalsIgnoreCase("V")) {
				tran.sellExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.sellExecutingFirm= "0" + campos[11];
			}
			
			if (campos[4].equalsIgnoreCase("V") && !campos[12].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.sellEnteringFirm= campos[12];
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (campos[4].equalsIgnoreCase("V")) {
				tran.sellEnteringTrader= campos[29];
			}
			else {
				tran.sellEnteringTrader= "";
			}
			
			tran.qty= campos[10];
			tran.symbol= campos[6].trim();
			tran.spotPrice= new BigDecimal(campos[25]);
			tran.forwardPrice= new BigDecimal(camposTP[25]);
			tran.maxRate= BigDecimal.ZERO;
			tran.pubRate= new BigDecimal(camposTP[28]);
			tran.settlementType= campos[19].trim();
			
			SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tran.expirationDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(camposTP[15]));
			
			tran.amount= new BigDecimal(campos[24]);
			tran.transactionTime= "09:00:00";
			
			LocalDate datePart = LocalDate.parse(campos[1].substring(0,10));
			LocalTime timePart = LocalTime.parse(campos[1].substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.transactionDay = dt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			tran.transactionTime = dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			
			
			tran.prepaid= "";
			tran.folio= campos[2].trim();
			tran.bestStance= "";
			tran.divisible= "";
			tran.modified= "";
			tran.codIsin= "";
			tran.securityType= "";
			tran.securityExchange= "";
			tran.orderID= "";
			tran.buyClientID = campos[31].trim();
			tran.sellClientID = campos[32].trim();
			
			
			palosMarketDataSIM.add(tran);
			
		}
		
		return palosMarketDataSIM;
	}
	
	public static List<TransaccionRFMarketData_Entrada> loadPalosRF(String palos) throws ParseException {
		
		List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,String[]> palosCalculados = new HashMap<>();
		
		for (String palo: palos.split("\r\n")) {
			String[] campos = palo.split(";");
			if (campos.length < 3)
				continue;
			
			if (index == 0) {
				index++;
				continue;
			}
			
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(campos[0] + campos[1])) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(campos[0] + campos[1], campos);
			
			
		}
		
		for (String[] campos : palosCalculados.values()) {

			
			TransaccionRFMarketData_Entrada tran = new TransaccionRFMarketData_Entrada();
			
			
			tran.IdInterno = index++;
			tran.codTicker = "TRADE";
			tran.action = "I";
			
			LocalDate datePart = LocalDate.parse(campos[0].substring(0,10));
			LocalTime timePart = LocalTime.parse(campos[0].substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.tradeTime = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.tradeID = campos[1].trim();
			tran.symbol = campos[5].trim();
			tran.condIsin = "";
			tran.quantity = new BigDecimal(campos[8]);
			
			if (campos[3].equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.buyExecutingFirm= "0" + campos[9].trim();
			}
			
			if (campos[4].equalsIgnoreCase("C") && !campos[10].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= campos[10];
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (campos[3].equalsIgnoreCase("C")) {
				tran.traderBuy= campos[28];
			}
			else {
				tran.traderBuy= "";
			}
			

			//corredor vende
			
			if (campos[3].equalsIgnoreCase("V")) {
				tran.sellExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				tran.sellExecutingFirm= "0" +  campos[9].trim();
			}
			
			if (campos[3].equalsIgnoreCase("V") && !campos[10].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.sellEnteringFirm= campos[10];
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (campos[3].equalsIgnoreCase("V")) {
				tran.traderSell= campos[28];
			}
			else {
				tran.traderSell= "";
			}
			
			String condliq = "";
			if (campos[17].trim().equalsIgnoreCase("PH")) {
				condliq = "H";
				
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.settlDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(campos[4]));
			}
			else if (campos[17].trim().equalsIgnoreCase("PM")) {
				condliq = "M";
				
				
				
				tran.settlDate = liqPM;
			}
			else {
				condliq = "";
				
				tran.settlDate = liqCN; 
			}
			
			tran.tradeCondition = " " + (condliq.length()==1?condliq:" ")+ "     " + (campos[19].trim().isEmpty()?" ":"O");
			tran.condition = "";
			tran.amount = new BigDecimal(campos[21]);
			tran.term = "";
			tran.coraMaturity = "";
			tran.price = new BigDecimal(campos[24]);
			tran.auctionId = "T";
			tran.tir = new BigDecimal(campos[27]);
			tran.signTir = "";
			tran.calculationType = "";
			tran.dcvTitle = "N";					
			
			tran.valAdjustmentCurrency = "";
			tran.currency = "";
			tran.currencySettlementAmount = new BigDecimal(campos[21]);
			tran.duration = BigDecimal.ZERO;
			tran.family = "";
			tran.buyerFund = "";
			tran.sellerFund = "";
			
			
			palosMarketDataRF.add(tran);
			
		}
		
		return palosMarketDataRF;
	}
	
	public static List<TransaccionIFMarketData_Entrada> loadPalosIF(String palos) throws ParseException {
		
		List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,String[]> palosCalculados = new HashMap<>();
		
		for (String palo: palos.split("\r\n")) {
			String[] campos = palo.split(";");
			if (campos.length < 3)
				continue;
			
			if (index == 0) {
				index++;
				continue;
			}
			
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(campos[0] + campos[1])) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(campos[0] + campos[1], campos);
			
			
		}
		
		for (String[] campos : palosCalculados.values()) {

			//FEC_TRANS(0);FOLIO_TRANS(1);IND_BOLSA(2);TIPO_OPERAC(3);FECHA(4);EMISOR(5);TIPO_REAJ(6);FEC_VCTO(7);COD_CORR_CONTRA(8);COD_OPE_EXT(9);FEC_DIG(10);FEC_EMI(11);IND_ASIGNAC(12);IND_COMPROMISO(13);
			//IND_CTA_PRO(14);IND_DCV(15);IND_LAMINA(16);IND_LIQUID(17);IND_LUGAR_EMI(18);IND_MADUREZ;IND_MODIF;IND_OD(21);IND_RESERVADO;MONTO(23);MONTO_CP;NEMO(25);PLAZO(26);RUEDA(27);TASA(28);VAL_RESC(29);
			//VAL_RESC_ASIG;OPE_INT(31);SEC_MOVTO_FAC;IND_FUNGIBILIDAD;FEC_VCTO_TP;IND_SIMUL;TASA_SIMUL;FOLIO_SIMUL;TASA_EMISION;SPREAD_TASA_EMISION;MODALIDAD_PAGO;
			//PERIODICIDAD_PAGO;FECHA_EMISION;PARIDAD_MORIGEN;CAPTACION_MORIGEN;CODCATIFRSID;MON_MORIGEN;
			//FCREOF_COM;FCREOF_VEN;CAN_COM;PRECIO_COM;BOLSA_COM;CAN_VEN;PRECIO_VEN;BOLSA_VEN
			TransaccionIFMarketData_Entrada tran = new TransaccionIFMarketData_Entrada();
			
			
			tran.IdInterno = index++;
			tran.CODTICKER = "TRADE";
			tran.ACTION = "I";
			
			LocalDate datePart = LocalDate.parse(campos[0].substring(0,10));
			LocalTime timePart = LocalTime.parse(campos[0].substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.TRADETIME = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.TRADEID = campos[1].trim();
			tran.SYMBOL = campos[25].trim();
			tran.CONDISIN = "";
			
			
			if (campos[3].equalsIgnoreCase("C")) {
				tran.BUYEXECUTINGFIRM= codigoCuentaPropiaConCero;
			}
			else {
				tran.BUYEXECUTINGFIRM= "0" + campos[8].trim();
			}
			
			if (campos[3].equalsIgnoreCase("C") && !campos[9].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.BUYENTERINGFIRM= campos[9];
			}
			else {
				tran.BUYENTERINGFIRM= "";
			}
			
			if (campos[3].equalsIgnoreCase("C")) {
				tran.TRADERBUY= campos[31];
			}
			else {
				tran.TRADERBUY= "";
			}
			

			//corredor vende
			
			if (campos[3].equalsIgnoreCase("V")) {
				tran.SELLEXECUTINGFIRM = codigoCuentaPropiaConCero;
			}
			else {
				tran.SELLEXECUTINGFIRM = "0" +  campos[8].trim();
			}
			
			if (campos[3].equalsIgnoreCase("V") && !campos[9].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.SELLENTERINGFIRM = campos[9];
			}
			else {
				tran.SELLENTERINGFIRM = "";
			}
			
			if (campos[3].equalsIgnoreCase("V")) {
				tran.TRADERSELL= campos[31];
			}
			else {
				tran.TRADERSELL= "";
			}
			
			String condliq = "";
			if (campos[17].trim().equalsIgnoreCase("PH")) {
				condliq = "H";
				
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.SETTLDATE = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(campos[4]));
			}
			else if (campos[17].trim().equalsIgnoreCase("PM")) {
				condliq = "M";
				
				tran.SETTLDATE = liqPM;
			}
			else {
				//t2
				condliq = "";
				tran.SETTLDATE = liqCN;
			}
			
			
			tran.TRADECONDITION = " " + (condliq.length()==1?condliq: " ") + "     " + (campos[21].trim().isEmpty()?" ":"O");
			tran.CONDITION = "";
			tran.LOCALAMOUNT = new BigDecimal(campos[23]); //44
			tran.AMOUNT = new BigDecimal(campos[44]); //44
			tran.TERM = 0;
			tran.AUCTIONID = campos[27];
			tran.RATE = new BigDecimal(campos[28]);
			tran.SIGNRATE = "";
			tran.SYMBOLSVS = "";
			tran.DCVTITLE = campos[15].trim();;
			
			{
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.EXPIRETIME = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(campos[7]));
			}
			
			tran.ISSUER = campos[5].trim();
			tran.RESETTYPE = campos[6].trim();
			tran.RESCUEVALUE = new BigDecimal(campos[29]);
			
			
			palosMarketDataIF.add(tran);
			
		}
		
		return palosMarketDataIF;
	}
	
	public static List<TransaccionRVMarketData_Entrada> loadPalosRV(String palos) {
		
		List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,String[]> palosCalculados = new HashMap<>();
		
		for (String palo: palos.split("\r\n")) {
			String[] campos = palo.split(";");
			if (campos.length < 3)
				continue;
			
			if (index == 0) {
				index++;
				continue;
			}
			if (campos[0].startsWith("//")) {
				continue;
			}
			
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(campos[1] + campos[2])) {
				continue;
			}
			
			
			//es simultanea
			if (campos[23].trim().length() > 0) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(campos[1] + campos[2], campos);
			
		}
		
		for (String[] campos : palosCalculados.values()) {

			
			TransaccionRVMarketData_Entrada tran = new TransaccionRVMarketData_Entrada();
			
			
			tran.IdInterno = index++;
			tran.codTicker= "TRADE";
			
			LocalDate datePart = LocalDate.parse(campos[1].substring(0,10));
			LocalTime timePart = LocalTime.parse(campos[1].substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(DIFERENCIA_HORARIA);
			tran.timeStamp = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.action= "I";
			tran.tradeTime= tran.timeStamp;
			tran.tradeID= campos[2].trim();
			tran.instrument= campos[6].trim();
			tran.market= "XSGO";
			if (campos[19].trim().equalsIgnoreCase("")) {
				tran.settlementCondition = "";
			}
			else if (campos[19].trim().equalsIgnoreCase("PM")) {
				tran.settlementCondition = "PM";
			}
			else if (campos[19].trim().equalsIgnoreCase("PH")) {
				tran.settlementCondition = "PH";
			}
			else if (campos[19].trim().equalsIgnoreCase("TP")) {
				continue;
			}
			
			tran.orderType= campos[21].trim();
			tran.indivisible= "";
			tran.shortSell= "NO";
			tran.buyClientID = "";
			tran.sellClientID = "";
			tran.quantity= new BigDecimal(campos[10]);
			tran.price= new BigDecimal(campos[25]);
			tran.amount= new BigDecimal(campos[24]);
			tran.closingPrice= BigDecimal.ZERO;
			if (campos[4].equalsIgnoreCase("C")) {
				tran.buyerBroker= codigoCuentaPropiaConCero;
			}
			else {
				tran.buyerBroker= "0" + campos[11];
				if (tran.buyerBroker.length() == 2) {
					tran.buyerBroker = "0" + tran.buyerBroker;
				}
			}
			
			if (campos[4].equalsIgnoreCase("C") && !campos[12].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.buyEnteringFirm= campos[12];
			}
			else {
				tran.buyEnteringFirm= "";
			}
			
			if (campos[4].equalsIgnoreCase("C")) {
				tran.buyEnteringTrader= campos[29];
			}
			else {
				tran.buyEnteringTrader= "";
			}
			

			//corredor vende
			
			if (campos[4].equalsIgnoreCase("V")) {
				tran.sellerBroker= codigoCuentaPropiaConCero;
			}
			else {
				tran.sellerBroker= "0" + campos[11];
				if (tran.sellerBroker.length() == 2) {
					tran.sellerBroker = "0" + tran.sellerBroker;
				}
			}
			
			if (campos[4].equalsIgnoreCase("V") && !campos[12].equalsIgnoreCase(codigoCuentaPropiaSinCero)) {
				tran.sellEnteringFirm= campos[12];
			}
			else {
				tran.sellEnteringFirm= "";
			}
			
			if (campos[4].equalsIgnoreCase("V")) {
				tran.sellEnteringTrader= campos[29];
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
			
			
			palosMarketDataRV.add(tran);
			
		}
		
		return palosMarketDataRV;
	}
	
	public static void main(String [] args) throws Exception {
		
		List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
		List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
		List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
		List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
		
		palosMarketDataRV = CargaTransaccionesSebra.loadPalosRV(CargaTransaccionesSebra.loadResourceToString("cl/bcs/ordenes/utils/PALOS_FYNSA_20230705_RV.csv"));
		//palosMarketDataSIM = CargaTransaccionesSebra.loadPalosSIM(CargaTransaccionesSebra.loadResourceToString("cl/bcs/ordenes/utils/PALOS_FYNSA_20230705_SIM.csv"));
		//palosMarketDataRF = CargaTransaccionesSebra.loadPalosRF(CargaTransaccionesSebra.loadResourceToString("cl/bcs/ordenes/utils/PALOS_FYNSA_20230705_RF.csv"));
		//palosMarketDataIF = CargaTransaccionesSebra.loadPalosIF(CargaTransaccionesSebra.loadResourceToString("cl/bcs/ordenes/utils/PALOS_FYNSA_20230705_IF.csv"));
		
		CrearTransaccionesMarketData ccc = new CrearTransaccionesMarketData();
		try(Connection conn = PlataformaUtils.getPropertiesConnection("scotia","connections.properties")){
			CrearTransaccionesMarketData_Entrada entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = 6408L;
			entradaPalos.transaccionesRV = palosMarketDataRV;
			entradaPalos.transaccionesRF = palosMarketDataRF;
			entradaPalos.transaccionesSIM = palosMarketDataSIM;
			entradaPalos.transaccionesIF = palosMarketDataIF;
			
			CrearTransaccionesMarketData_Salida salida = ccc.logica(conn, entradaPalos );
			conn.commit();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	static String loadResourceToString(final String path){		
	    final InputStream stream = CargaTransaccionesSebra.class.getClassLoader().getResourceAsStream(path);
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
