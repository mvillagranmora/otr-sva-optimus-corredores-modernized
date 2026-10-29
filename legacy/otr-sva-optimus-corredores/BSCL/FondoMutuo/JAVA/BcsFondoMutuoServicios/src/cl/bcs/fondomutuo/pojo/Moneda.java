package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class Moneda {
	
	public String IdMoneda;
	public Long IdPais;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
