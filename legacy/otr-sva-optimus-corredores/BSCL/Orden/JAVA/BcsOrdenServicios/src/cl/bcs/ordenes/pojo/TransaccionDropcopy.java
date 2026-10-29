package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TransaccionDropcopy {
	public Long ID;
	public String TRADEREPORTID;
	public String TRADEID;
	public Long TRADEREPORTTYPE;
	public Long TRDTYPE;
	public String TRADEHANDLINGINSTR;
	public String ORIGTRADEID;
	public String EXECTYPE;
	public String TRADELINKID;
	public String TRDMATCHID;
	public String SECONDARYEXECID;
	public Boolean PREVIOUSLYREPORTED;
	public String VENUETYPE;
	public String MARKETSEGMENTID;
	public String MARKETID;
	public BigDecimal LASTQTY;
	public BigDecimal LASTPX;
	public String LASTMKT;
	public String TRADEDATE;
	public String TRANSACTTIME;
	public String SETTLDATE;
	public String MATCHSTATUS;
	public String MATCHTYPE;
	public Boolean COPYMSGINDICATOR;
	public String SIDE;
	public String SIDETRADEID;
	public String SYMBOL;
	public String SECURITYID;
	public String SECURITYIDSOURCE;
	public String ENTERINGFIRM;
	public String ENTERINGTRADER;
	public String EXECUTINGFIRM;
	public String EXECUTINGTRADER;
	public String CUSTOMERACCOUNT;
	public String CONTRAFIRM;
	public String ORDERID;
	public String CLORDID;
	public String ORDERCAPACITY;
	public String AGGRESSORINDICATOR;
	public String TIMESTAMP;
	public String TEXT;
	public String FONDO;
	public String CODIGOOPERADORASOCIADO;
	public String toString() {
		return Utils.printJson(this, false);
	}
}
