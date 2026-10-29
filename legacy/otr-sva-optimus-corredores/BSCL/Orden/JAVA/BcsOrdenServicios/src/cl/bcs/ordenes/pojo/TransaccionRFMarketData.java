package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionRFMarketData {
	public String codTicker;
	public String timeTicker;
	public String action;
	public String tradeTime;
	public String tradeID;
	public String symbol;
	public String condIsin;
	public BigDecimal quantity;
	public String buyExecutingFirm;
	public String sellExecutingFirm;
	public String buyEnteringFirm;
	public String sellEnteringFirm;
	public String tradeCondition;
	public String condition;
	public BigDecimal amount;
	public String term;
	public String coraMaturity;
	public BigDecimal price;
	public String auctionId;
	public BigDecimal tir;
	public String signTir;
	public String calculationType;
	public String traderBuy;
	public String traderSell;
	public String dcvTitle;
	public String settlDate;
	public String valAdjustmentCurrency;
	public String currency;
	public BigDecimal currencySettlementAmount;
	public BigDecimal duration;
	public String family;
	public String buyerFund;
	public String sellerFund;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
