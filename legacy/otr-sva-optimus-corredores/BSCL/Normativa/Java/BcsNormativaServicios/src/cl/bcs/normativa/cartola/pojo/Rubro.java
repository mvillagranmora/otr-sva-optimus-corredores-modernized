package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class Rubro {
	public Long CodigoRubro;
	public String Descripcion;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
