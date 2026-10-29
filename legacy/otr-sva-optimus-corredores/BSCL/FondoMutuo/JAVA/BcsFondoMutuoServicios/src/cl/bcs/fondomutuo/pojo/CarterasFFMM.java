package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class CarterasFFMM {
	public String IdSerieFondo;
	public String IdRegimen;
	public BigDecimal TotalCuotas;
	public String ISIN;
	public String FondoExtranjero;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
