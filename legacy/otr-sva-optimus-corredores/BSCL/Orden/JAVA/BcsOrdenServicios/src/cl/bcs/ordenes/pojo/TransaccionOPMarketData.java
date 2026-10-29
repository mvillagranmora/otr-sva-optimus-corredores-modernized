package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionOPMarketData {
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
	public String buyFund;	
	public String sellFund;	
	public String currency;	
	public String tradeCondition;
	public String indDcv;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
