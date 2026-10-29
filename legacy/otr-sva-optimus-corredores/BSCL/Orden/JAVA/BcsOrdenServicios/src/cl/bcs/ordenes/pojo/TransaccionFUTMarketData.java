package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionFUTMarketData {
	public String codTicker;
	public String timeStamp;
	public String action;
	public String tradeTime;
	public String tradeID;
	public String instrument;
	public String market;
	public String settlement;
	public String od;
	//public String orderType;
	//public String indivisible;
	//public String shortSell;
	public BigDecimal quantity;
	public BigDecimal price;
	public BigDecimal amount;
	public BigDecimal closingPrice;
	public String buyerBroker;
	//public String buyEnteringFirm;
	//public String buyEnteringTrader;
	public String sellerBroker;
	//public String sellEnteringFirm;
	//public String sellEnteringTrader;
	public BigDecimal buyingQTY;
	public BigDecimal priceBuyingPrice;
	public BigDecimal sellingQty;
	public BigDecimal priceSellingPrice;
	public String tradeCondition;
	public String entryBuyer;
	public String entrySeller;
	public String orderID;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
