package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class Concepto {
	public String IdConcepto;
	public String Nombre;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
