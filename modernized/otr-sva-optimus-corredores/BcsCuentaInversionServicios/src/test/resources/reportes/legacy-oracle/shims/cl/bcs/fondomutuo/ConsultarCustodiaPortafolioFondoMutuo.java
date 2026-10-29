package cl.bcs.fondomutuo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo (BcsFondoMutuoServicios, outside this suite).
 * Only the nested data class ValorCuota is needed by GenerarArchivoClientesPorPagarYCobrar; it is copied VERBATIM from
 * ConsultarCustodiaPortafolioFondoMutuo.java:309-317. The service itself is not executed.
 */
public class ConsultarCustodiaPortafolioFondoMutuo {

	public static class ValorCuota{
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public String FechaValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
