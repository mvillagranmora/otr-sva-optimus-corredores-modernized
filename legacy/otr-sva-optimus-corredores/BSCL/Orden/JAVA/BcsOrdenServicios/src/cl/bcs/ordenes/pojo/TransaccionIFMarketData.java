package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionIFMarketData {
	public String CODTICKER;
	public String TIMETICKER;
	public String ACTION;
	public String TRADETIME;
	public String TRADEID;
	public String SYMBOL;
	public String CONDISIN;
	public String BUYEXECUTINGFIRM;
	public String SELLEXECUTINGFIRM;
	public String BUYENTERINGFIRM;
	public String SELLENTERINGFIRM;
	public String TRADECONDITION;
	public String CONDITION;
	public String ISSUER;
	public String FAMILY;
	public String ENTERINGDATE;
	public String EXPIRETIME;
	public String LAMINA;
	public BigDecimal AMOUNT;
	public Integer TERM;
	public String AUCTIONID;
	public BigDecimal RATE;
	public String SIGNRATE;
	public String RESETTYPE;
	public BigDecimal RESCUEVALUE;
	public String TRADERBUY;
	public String TRADERSELL;
	public String DCVTITLE;
	public String SETTLDATE;
	public String SYMBOLSVS;
	public BigDecimal LOCALAMOUNT;
	public String BUYERFUND;
	public String SELLERFUND;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
