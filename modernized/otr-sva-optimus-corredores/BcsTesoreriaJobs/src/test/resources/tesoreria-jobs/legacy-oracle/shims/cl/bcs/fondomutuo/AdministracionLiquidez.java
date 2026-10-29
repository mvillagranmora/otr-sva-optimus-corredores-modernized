package cl.bcs.fondomutuo;

import cl.bcs.utils.Utils;

/** ORACLE SHIM of legacy cl.bcs.fondomutuo.AdministracionLiquidez: only the nested POJO the job reads (AdministracionLiquidez.java:123-129, verbatim). */
public class AdministracionLiquidez {

	public static class FechaActual {
		public String Fecha;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
