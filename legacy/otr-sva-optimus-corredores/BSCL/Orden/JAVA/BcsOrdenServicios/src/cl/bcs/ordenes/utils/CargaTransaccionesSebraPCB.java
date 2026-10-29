package cl.bcs.ordenes.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;

public class CargaTransaccionesSebraPCB {

	public List<TransaccionSIMMarketData_Entrada> loadPalosSIM(List<ObjTBPLTBRV> palos) throws ParseException {
		
		List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
		Long index = 10000L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBPLTBRV> palosPH = new HashMap<>();
		HashMap<String,ObjTBPLTBRV> palosTP = new HashMap<>();
		
		for (ObjTBPLTBRV lePalo: palos) {
			
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
		
		
		for (ObjTBPLTBRV lePalo: palosPH.values()) {

			TransaccionSIMMarketData_Entrada tran = new TransaccionSIMMarketData_Entrada();
			
			tran.IdInterno = index++;
			boolean esOD = lePalo.COD_CORR_COM.equals(lePalo.COD_CORR_VEN);
			
			tran.action= "I";
			String llave = lePalo.FECHA + lePalo.FOLIO_SIMUL;
			ObjTBPLTBRV lePaloTP = palosTP.get(llave);
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				if (lePalo.COD_CORR_COM.toString().length() == 3) {
					tran.buyExecutingFirm= "" + lePalo.COD_CORR_COM;	
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 2) {
					tran.buyExecutingFirm= "0" + lePalo.COD_CORR_COM;		
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 1) {
					tran.buyExecutingFirm = "00" + lePalo.COD_CORR_COM;
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
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("V") || esOD ) {
				tran.sellExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				if (lePalo.COD_CORR_VEN.toString().length() == 3) {
					tran.sellExecutingFirm= "" + lePalo.COD_CORR_VEN;	
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 2) {
					tran.sellExecutingFirm= "0" + lePalo.COD_CORR_VEN;		
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 1) {
					tran.sellExecutingFirm = "00" + lePalo.COD_CORR_VEN;
				}
				
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
			
			if (lePalo.IND_LIQUID.trim().equalsIgnoreCase("")) {
				tran.settlementType = "";
			}
			else if (lePalo.IND_LIQUID.trim().contains("PM")) {
				tran.settlementType = "PM";
			}
			else if (lePalo.IND_LIQUID.trim().contains("PH")) {
				tran.settlementType = "PH";
			}
			else if (lePalo.IND_LIQUID.trim().contains("TP")) {
				continue;
			}
			
			SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
			tran.expirationDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lePaloTP.FEC_VCTO_TP));
			
			tran.amount= lePalo.MONTO;
			tran.transactionTime= "09:00:00";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(this.diferenciaHoraria);
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
	
	public List<TransaccionRFMarketData_Entrada> loadPalosRF(List<ObjTBPLTBRF> palosRF) throws ParseException {
		
		List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBPLTBRF> palosCalculados = new HashMap<>();
		
		for (ObjTBPLTBRF palo: palosRF) {
						
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(palo.hora + palo.FOLIO)) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(palo.hora + palo.FOLIO, palo);
			
			
		}
		
		for (ObjTBPLTBRF lePalo : palosCalculados.values()) {

			
			TransaccionRFMarketData_Entrada tran = new TransaccionRFMarketData_Entrada();
			
			boolean esOD = lePalo.COD_CORR_COM.equals(lePalo.COD_CORR_VEN);
			tran.IdInterno = index++;
			tran.codTicker = "TRADE";
			tran.action = "I";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(this.diferenciaHoraria);
			tran.tradeTime = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.tradeID = lePalo.FOLIO.toString();
			tran.symbol = lePalo.NEMO.trim();
			tran.condIsin = "";
			tran.quantity = lePalo.CANTIDAD;
			
			if (lePalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.buyExecutingFirm= codigoCuentaPropiaConCero;
			}
			else {
				if (lePalo.COD_CORR_COM.toString().length() == 3) {
					tran.buyExecutingFirm= "" + lePalo.COD_CORR_COM;	
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 2) {
					tran.buyExecutingFirm= "0" + lePalo.COD_CORR_COM;		
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 1) {
					tran.buyExecutingFirm = "00" + lePalo.COD_CORR_COM;
				}
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
				
				if (lePalo.COD_CORR_VEN.toString().length() == 3) {
					tran.sellExecutingFirm= "" + lePalo.COD_CORR_VEN;	
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 2) {
					tran.sellExecutingFirm= "0" + lePalo.COD_CORR_VEN;		
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 1) {
					tran.sellExecutingFirm = "00" + lePalo.COD_CORR_VEN;
				}
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
			if (lePalo.IND_LIQUID.trim().contains("PH")) {
				condliq = "H";
				
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tran.settlDate = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lePalo.FECHA));
			}
			else if (lePalo.IND_LIQUID.trim().contains("PM")) {
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
	
	public List<TransaccionIFMarketData_Entrada> loadPalosIF(List<ObjTBPLTBIF> palosIF) throws ParseException {
		
		List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBPLTBIF> palosCalculados = new HashMap<>();
		
		for (ObjTBPLTBIF palo: palosIF) {
						
			//eliminamos palos repetidos
			if (palosCalculados.containsKey(palo.HORA + palo.FOLIO)) {
				continue;
			}
			
			index++;
			
			palosCalculados.put(palo.HORA + palo.FOLIO, palo);
			
			
		}
		
		for (ObjTBPLTBIF lepalo : palosCalculados.values()) {

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
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(this.diferenciaHoraria);
			tran.TRADETIME = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.TRADEID = lepalo.FOLIO + "";
			tran.SYMBOL = lepalo.NEMO.trim();
			tran.CONDISIN = "";
			
			
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("C")) {
				tran.BUYEXECUTINGFIRM= codigoCuentaPropiaConCero;
			}
			else {
				tran.BUYEXECUTINGFIRM= "0" + lepalo.COD_CORR_COM;
				
				if (lepalo.COD_CORR_COM.toString().length() == 3) {
					tran.BUYEXECUTINGFIRM= "" + lepalo.COD_CORR_COM;	
				}
				else if (lepalo.COD_CORR_COM.toString().length() == 2) {
					tran.BUYEXECUTINGFIRM= "0" + lepalo.COD_CORR_COM;		
				}
				else if (lepalo.COD_CORR_COM.toString().length() == 1) {
					tran.BUYEXECUTINGFIRM = "00" + lepalo.COD_CORR_COM;
				}
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
			
			tran.BUYERFUND = "";
			tran.SELLERFUND = "";
			
			//corredor vende
			
			if (lepalo.TIPO_OPERAC.equalsIgnoreCase("V")) {
				tran.SELLEXECUTINGFIRM = codigoCuentaPropiaConCero;
			}
			else {
				
				if (lepalo.COD_CORR_VEN.toString().length() == 3) {
					tran.SELLEXECUTINGFIRM= "" + lepalo.COD_CORR_VEN;	
				}
				else if (lepalo.COD_CORR_VEN.toString().length() == 2) {
					tran.SELLEXECUTINGFIRM= "0" + lepalo.COD_CORR_VEN;		
				}
				else if (lepalo.COD_CORR_VEN.toString().length() == 1) {
					tran.SELLEXECUTINGFIRM = "00" + lepalo.COD_CORR_VEN;
				}
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
			
			if (lepalo.IND_LIQUID.trim().contains("PH")) {
				condliq = "H";
				
				
				tran.SETTLDATE = sdfExpirationDateIN.format(sdfExpirationDateOUT.parse(lepalo.FECHA));
			}
			else if (lepalo.IND_LIQUID.trim().contains("PM")) {
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
	
	public List<TransaccionRVMarketData_Entrada> loadPalosRV(List<ObjTBPLTBRV> palos) {
		
		List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
		Long index = 0L;
		
		//FOLIO+FECHA y ARRAY DE DATA
		HashMap<String,ObjTBPLTBRV> palosCalculados = new HashMap<>();
		
		for (ObjTBPLTBRV palo: palos) {
			
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
		
		for (ObjTBPLTBRV lePalo : palosCalculados.values()) {

			
			TransaccionRVMarketData_Entrada tran = new TransaccionRVMarketData_Entrada();
			
			if (lePalo.IND_BOLSA.equalsIgnoreCase("BEC")) {
				System.out.println(lePalo.FOLIO + " ES BEC");
			}
			
			tran.IdInterno = index++;
			tran.codTicker= "TRADE";
			
			LocalDate datePart = LocalDate.parse(lePalo.hora.substring(0,10));
			LocalTime timePart = LocalTime.parse(lePalo.hora.substring(11,19));
			LocalDateTime dt = LocalDateTime.of(datePart, timePart).plusHours(this.diferenciaHoraria);
			tran.timeStamp = dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"000";
			
			tran.action= "I";
			tran.tradeTime= tran.timeStamp;
			tran.tradeID= lePalo.FOLIO.trim();
			tran.instrument= lePalo.NEMO.trim();
			tran.market= "XSGO";
			if (lePalo.IND_LIQUID.trim().contains("PM")) {
				tran.settlementCondition = "PM";
			}
			else if (lePalo.IND_LIQUID.trim().contains("PH")) {
				tran.settlementCondition = "PH";
			}
			else if (lePalo.IND_LIQUID.trim().equalsIgnoreCase("")) {
				tran.settlementCondition = "";
			}
			else if (lePalo.IND_LIQUID.trim().contains("TP")) {
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
					tran.buyerBroker= "" + lePalo.COD_CORR_COM;	
				}
				else if (lePalo.COD_CORR_COM.toString().length() == 2) {
					tran.buyerBroker= "0" + lePalo.COD_CORR_COM;		
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
					tran.sellerBroker= "" + lePalo.COD_CORR_VEN;	
				}
				else if (lePalo.COD_CORR_VEN.toString().length() == 2) {
					tran.sellerBroker= "0" + lePalo.COD_CORR_VEN;		
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
	
	public static class ObjTBPLTBRV extends BcsPojo{
		public String MERCADO;
		public String hora;
		public String FOLIO;
		public String IND_BOLSA;
		public String TIPO_OPERAC;
		public String FECHA;
		public String NEMO;
		public BigDecimal CANTIDAD;
		public String IND_LIQUID;
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
		public String FEC_VCTO_TP;
		public BigDecimal TASA;
		public String FOLIO_SIMUL;
		public String COND_COD_009; //S, PUNTA A PLAZO EN SIM
	}
	
	public static class ObjTBPLTBRF extends BcsPojo{
		public String IND_LIQUID;
		public String hora;
		public String FOLIO;
		public String IND_BOLSA;
		public String TIPO_OPERAC;
		public String FECHA;
		public String NEMO;
		public BigDecimal CANTIDAD;
		public BigDecimal PRECIO;
		public BigDecimal MONTO;
		public BigDecimal COD_CORR_COM;
		public BigDecimal COD_CORR_VEN;
		public BigDecimal COD_OPE_COM;
		public BigDecimal COD_OPE_VEN;
		public BigDecimal OPE_INT_COM;
		public BigDecimal OPE_INT_VEN;
		public BigDecimal TIR;
		public String MONEDA;
		public String MONEDA_LIQ;
		public String REMATE;
	}
	
	public static class ObjTBPLTBIF extends BcsPojo{
		
		public String HORA;
		public String FOLIO;
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
		public String IND_LIQUID;
		public String FEC_EMI;
		public BigDecimal MONTO;
		public BigDecimal PLAZO;
		public String REMATE;
		public String TIPO_REAJ;
		public BigDecimal TASA;
	}
	
	
	static String tbpltbrvquery = "SELECT MERCADO, FEC_TRANS as hora, ltrim(RTRIM(FOLIO_TRANS)) as FOLIO,IND_BOLSA ,TIPO_OPERAC,FECHA,"
			+ "LTRIM(RTRIM(NEMO)) as NEMO ,CANTIDAD,IND_LIQUID,PRECIO ,MONTO,"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_COM,"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_VEN,"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_COM,"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_VEN,"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN OPE_INT ELSE 0 END AS OPE_INT_COM,"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN OPE_INT ELSE 0 END AS OPE_INT_VEN,CLIENTE_COM ,CLIENTE_VEN,"
			+ "CASE IND_SIMUL WHEN 'TS' THEN 'S' ELSE '' END AS COND_COD_004,FEC_VCTO_TP,TASA ,"
			+ "ltrim(RTRIM(FOLIO_SIMUL)) AS FOLIO_SIMUL ,CASE IND_LIQUID WHEN 'TP' THEN 'S' ELSE '' END AS COND_COD_009 "
			+ "from TBPLTBRV  WITH(NOLOCK) WHERE FECHA = :fecha;";
	
	static String tbpltbrfquery = "select FEC_TRANS as hora,FECHA,rtrim(ltrim(FOLIO_TRANS)) as FOLIO,IND_BOLSA ,TIPO_OPERAC ,fecha,rtrim(ltrim(TB.nemo)) as NEMO\r\n"
			+ ",CANTIDAD ,RTRIM(LTRIM(IND_LIQUID)) AS IND_LIQUID ,PRECIO ,tb.MONTO,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_VEN,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_VEN,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN OPE_INT ELSE 0 END AS OPE_INT_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN OPE_INT ELSE 0 END AS OPE_INT_VEN,TB.TIR,\r\n"
			+ "rtrim(ltrim(MI.MONEDA)) AS MONEDA,\r\n"
			+ "rtrim(ltrim(SE.TIPO_MONEDA_PAGO)) AS MONEDA_LIQ,\r\n"
			+ "RUEDA AS REMATE\r\n"
			+ "from TBPLTBRF TB  WITH(NOLOCK)\r\n"
			+ "left join RUE_TBRFMI MI ON RTRIM(LTRIM(TB.NEMO)) =  RTRIM(LTRIM(MI.NEMO))\r\n"
			+ "left join RUE_TBRFSE SE ON RTRIM(LTRIM(MI.COD_SERIE)) =  RTRIM(LTRIM(SE.COD_SERIE))\r\n"
			+ "WHERE tb.FECHA = :fecha";
			//+ "WHERE tb.FECHA = :fecha and folio_trans like '%161047%'";
	
	static String tbpltbifquery = "select FEC_TRANS as HORA,LTRIM(RTRIM(FOLIO_TRANS)) AS FOLIO,TIPO_OPERAC,FECHA ,NEMO ,VAL_RESC ,EMISOR ,FEC_VCTO,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN :codigo ELSE COD_CORR_CONTRA END AS COD_CORR_VEN,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN COD_OPE_EXT ELSE COD_CORR_CONTRA END AS COD_OPE_VEN,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'C' THEN OPE_INT ELSE 0 END AS OPE_INT_COM,\r\n"
			+ "CASE TIPO_OPERAC WHEN 'V' THEN OPE_INT ELSE 0 END AS OPE_INT_VEN,IND_LIQUID ,FEC_EMI ,MONTO ,PLAZO ,RUEDA AS REMATE,\r\n"
			+ "CASE TIPO_REAJ WHEN 'US' THEN 'UD' ELSE TIPO_REAJ END AS TIPO_REAJ,TASA\r\n"
			+ "from TBPLTBIF  TB  WITH(NOLOCK) "
			+ "where TB.fecha = :fecha ";
			//+ "WHERE tb.FECHA = :fecha and folio_trans like '%161824%'";
	
	public String codigoCuentaPropiaConCero = "";
	public String codigoCuentaPropiaSinCero = "";
	private long diferenciaHoraria = 3;
	private String fechaCarga = "";
	private String fechaCarga2 = fechaCarga.replace("-", "");
	private String fechaPM = "";
	private String fechaCN = "";
	private String ambiente = "";
	
	public CargaTransaccionesSebraPCB(String codigoCorredor,String fechaCarga, int diferenciaHoraria, String ambiente) {
		codigoCuentaPropiaConCero = codigoCorredor;
		codigoCuentaPropiaSinCero = codigoCorredor.substring(1,codigoCorredor.length());
		this.fechaCarga = fechaCarga;
		this.fechaCarga2 = fechaCarga.replace("-", "");
		this.diferenciaHoraria = diferenciaHoraria;
		this.ambiente = ambiente;
		
	}
	
	public void run() throws Exception {
		
		try(Connection conn = PlataformaUtils.getPropertiesConnection(ambiente, "connections.properties")){
			
			
			List<TransaccionSIMMarketData_Entrada> palosMarketDataSIM = new ArrayList<>();
			List<TransaccionRVMarketData_Entrada> palosMarketDataRV = new ArrayList<>();
			List<TransaccionRFMarketData_Entrada> palosMarketDataRF = new ArrayList<>();
			List<TransaccionIFMarketData_Entrada> palosMarketDataIF = new ArrayList<>();
			
			Properties prop = new Properties();
	    	InputStream input = null;
	    	try {
	    		input = new FileInputStream("pcb.properties");
	    	}
	    	catch (FileNotFoundException e) {
	    		input = new FileInputStream("pcb.properties");
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
	    	
	    	List<ObjTBPLTBRV> palosRV = _sql.query(dsTarget.getConnection(), tbpltbrvquery,params,ObjTBPLTBRV.class);
	    	List<ObjTBPLTBRF> palosRF = _sql.query(dsTarget.getConnection(), tbpltbrfquery,params,ObjTBPLTBRF.class);
	    	List<ObjTBPLTBIF> palosIF = _sql.query(dsTarget.getConnection(), tbpltbifquery,params,ObjTBPLTBIF.class);
	    	
			//PM y CN
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
		    InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = 44L;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			FechasIn pm = new FechasIn();
			pm.fechaInicial = fechaCarga;
			pm.numeroDias = 1L;
			pm.pais = 44L;
			
			FechasIn cn = new FechasIn();
			cn.fechaInicial = fechaCarga;
			cn.numeroDias = 2L;
			cn.pais = 44L;
			
			inObtenerDiaSiguiente.p_fechas.add(pm);
			inObtenerDiaSiguiente.p_fechas.add(cn);
			
			
			CrearTransaccionesMarketData ccc = new CrearTransaccionesMarketData();
			
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(conn, inObtenerDiaSiguiente);
			java.sql.Date fechaPMDate = Fecha.toDate(outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada);
			java.sql.Date fechaCNDate = Fecha.toDate(outObtenerDiaSiguiente.p_out_fechas.get(1).fechaCalculada);
			this.fechaPM = fechaPMDate.toString();
			this.fechaCN = fechaCNDate.toString();
			
			List<Usuario> usuarioSistema = _sql.query(conn, "SELECT \"IdUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"CodigoUsuario\" = 'SISTEMA'", 
					new HashMap<>(), Usuario.class);
			CrearTransaccionesMarketData_Entrada entradaPalos = new CrearTransaccionesMarketData_Entrada();

			
			palosMarketDataRV = loadPalosRV(palosRV);
			palosMarketDataSIM = loadPalosSIM(palosRV);
			palosMarketDataRF = loadPalosRF(palosRF);
			palosMarketDataIF = loadPalosIF(palosIF);
			
			//RV
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = palosMarketDataRV;
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			CrearTransaccionesMarketData_Salida salida1 = ccc.logica(conn, entradaPalos );
			System.out.println(salida1.P_OUT_RESULTADO.size());
			conn.commit();
			
			//RF
			entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = palosMarketDataRF;
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			CrearTransaccionesMarketData_Salida salida2 = ccc.logica(conn, entradaPalos );
			System.out.println(salida2.P_OUT_RESULTADO.size());
			conn.commit();
			
			//IF
			entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = new ArrayList<>();
			entradaPalos.transaccionesIF = palosMarketDataIF;
			 
			CrearTransaccionesMarketData_Salida salida3 = ccc.logica(conn, entradaPalos );
			System.out.println(salida3.P_OUT_RESULTADO.size());
			conn.commit();
			
			//SIM
			entradaPalos = new CrearTransaccionesMarketData_Entrada();
			
			entradaPalos.p_IdEmpresa = 1L;
			entradaPalos.p_IdUsuario = usuarioSistema.get(0).IdUsuario;
			entradaPalos.transaccionesRV = new ArrayList<>();
			entradaPalos.transaccionesRF = new ArrayList<>();
			entradaPalos.transaccionesSIM = palosMarketDataSIM;
			entradaPalos.transaccionesIF = new ArrayList<>();
			
			CrearTransaccionesMarketData_Salida salida4 = ccc.logica(conn, entradaPalos );
			System.out.println(salida4.P_OUT_RESULTADO.size());
			conn.commit();
			
			//repaso palos bec
			List<Transaccion> list = new ArrayList<>();
			for (ObjTBPLTBRV paloRV: palosRV) {
				if (paloRV.IND_BOLSA.equalsIgnoreCase("BEC")) {
					Transaccion t = new Transaccion();
					t.FechaSistemaCreacion= paloRV.FECHA;
					t.Folio = paloRV.FOLIO;
					list.add(t);
				}
			}
			
			if (list.size() > 0) {
				_sql.batch(conn,"UPDATE X SET \"IdBolsa\" = 'BEC' FROM \"BSCL.Orden.BD::ORD.Transaccion\" X WHERE \"FechaSistemaCreacion\" = :FechaSistemaCreacion AND \"Folio\" = :Folio ",list,Transaccion.class);
			}
			
			conn.commit();
		}
	}
	
	public static void main(String [] args) throws Exception {
		
		CargaTransaccionesSebraPCB pcb = new CargaTransaccionesSebraPCB("082", "2026-05-29",4, "bicepre");
		pcb.run();
		
	}
	
	public static class Transaccion {
		public String Folio;
		public String FechaSistemaCreacion;
	}
	
	public static class Usuario {
		public Long IdUsuario;
	}
	
	static String loadResourceToString(final String path){		
	    final InputStream stream = CargaTransaccionesSebraPCB.class.getClassLoader().getResourceAsStream(path);
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
