package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionRVMarketData {
	public String codTicker;
	public String timeStamp;
	public String action;
	public String tradeTime;
	public String tradeID;
	public String instrument;
	public String market;
	public String settlementCondition;
	public String orderType;
	public String indivisible;
	public String shortSell;
	public BigDecimal quantity;
	public BigDecimal price;
	public BigDecimal amount;
	public BigDecimal closingPrice;
	public String buyerBroker;
	public String buyEnteringFirm;
	public String buyEnteringTrader;
	public String sellerBroker;
	public String sellEnteringFirm;
	public String sellEnteringTrader;
	public BigDecimal priceBuyingQTY;
	public BigDecimal priceBuyingPrice;
	public BigDecimal priceSellingQty;
	public BigDecimal priceSellingPrice;
	
	public String priceExchange;
	public String timeInForce;
	public String entryType;
	public String tradingSessionID;
	public String codIsin;
	public String securityType;
	public String priceTime;
	
	public String buyClientID;
	public String buyFundManager;
	public String sellClientID; 
	public String sellFundManager; 
	
	public String tradeCurrency;
	public String orderBookName;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
