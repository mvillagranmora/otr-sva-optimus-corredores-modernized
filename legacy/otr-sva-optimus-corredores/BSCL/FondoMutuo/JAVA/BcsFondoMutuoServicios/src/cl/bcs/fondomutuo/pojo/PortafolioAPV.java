package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class PortafolioAPV {
	
	public Long IdPortafolio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
