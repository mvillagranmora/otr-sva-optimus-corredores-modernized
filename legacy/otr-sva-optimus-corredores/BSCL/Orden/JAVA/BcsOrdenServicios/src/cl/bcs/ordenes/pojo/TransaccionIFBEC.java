package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

public class TransaccionIFBEC {
	public BigDecimal FOLIO;
	public String HORA;
	public BigDecimal CAN_CPC;
	public BigDecimal CAN_CPV;
	public BigDecimal COD_CORR_COM;
	public BigDecimal COD_CORR_VEN;
	public BigDecimal COD_OPE_COM;
	public BigDecimal COD_OPE_VEN;
	public BigDecimal OPE_INT_COM;
	public BigDecimal OPE_INT_VEN;
	public String COND_COD_001;
	public String COND_COD_002;
	public String COND_COD_003;
	public String COND_COD_004;
	public String COND_COD_005;
	public String COND_COD_006;
	public String COND_COD_007;
	public String COND_COD_008;
	public String COND_COD_009;
	public String COND_COD_010;
	public String COND_DIS;
	public String EMISOR;
	public String EMISOR_NO_INS;
	public String FAMILIA;
	public String FECHA;
	public String FEC_DIG;
	public String FEC_EMI;
	public String FEC_VCTO;
	public String LAMINA;
	public String LUG_EMI;
	public String MADUREZ;
	public BigDecimal MONTO;
	public String NEMO;
	public String OPE_ASO_COMP;
	public BigDecimal PLAZO;
	public String REMATE;
	public BigDecimal TASA;
	public String TIPO_PLAZ;
	public String TIPO_REAJ;
	public BigDecimal VAL_RESC;
	public String OFEREF;
	public BigDecimal CUPVAR;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
	
}
