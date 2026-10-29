package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

public class TransaccionRVBEC {
	public String		MERCADO;
	public String		NEMO;
	public String		HORA;
	public BigDecimal	FOLIO;
	public BigDecimal 	CANTIDAD;
	public BigDecimal 	CANTIDAD_AJU_VC;
	public BigDecimal 	CAN_CPC;
	public BigDecimal 	CAN_CPV;
	public BigDecimal	COD_CORR_COM;
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
	public String COND_COD_011;
	public String COND_COD_012;
	public String COND_COD_013;
	public String COND_COD_014;
	public String COND_COD_015;
	public String COND_DIS;
	public String EMISOR_NO_INS;
	public String FECHA;
	public String FEC_LIQ;
	public String FEC_DIG;
	public String IND_FIJ_CIE;
	public BigDecimal MONTO;
	public BigDecimal PRECIO;
	public BigDecimal PRECIO_AJU_VC;
	public BigDecimal REDONDEL;
	public String RUEDA;
	public String IND_DCV;
	public BigDecimal FOLIO_SIMUL;
	public BigDecimal TASA;
	public String CLIENTE_COM;
	public String CLIENTE_VEN;
	public String FCREOF_COM;
	public String FCREOF_VEN;
	public BigDecimal CAN_COM;
	public BigDecimal PRECIO_COM;
	public String BOLSA_COM;
	public BigDecimal CAN_VEN;
	public BigDecimal PRECIO_VEN;
	public String BOLSA_VEN;
	
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}		
}