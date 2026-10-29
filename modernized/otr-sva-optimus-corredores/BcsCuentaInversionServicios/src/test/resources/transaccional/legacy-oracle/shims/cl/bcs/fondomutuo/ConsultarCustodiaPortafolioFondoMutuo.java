package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM: only the ValorCuota POJO of ConsultarCustodiaPortafolioFondoMutuo (verbatim).
 */
public class ConsultarCustodiaPortafolioFondoMutuo {

	// legacy cl/bcs/fondomutuo/ConsultarCustodiaPortafolioFondoMutuo.java:309-317 (verbatim)
	public static class ValorCuota{
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public String FechaValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


}
