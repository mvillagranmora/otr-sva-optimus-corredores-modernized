package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionSIMMarketData {
	public String action;
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
	public BigDecimal amount;
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
	public String buyClientID;
	public String sellClientID;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
