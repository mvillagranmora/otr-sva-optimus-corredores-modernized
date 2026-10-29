package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class IndicadoresPortafolioFondoMutuo {
	
	public Long IdPortafolio;
	public String OperaLineaSobregiro;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
