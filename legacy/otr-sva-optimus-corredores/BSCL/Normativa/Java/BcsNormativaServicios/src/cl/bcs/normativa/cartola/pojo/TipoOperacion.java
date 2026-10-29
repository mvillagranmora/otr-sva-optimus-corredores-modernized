package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class TipoOperacion {
	public String IdTipoOperacion;
	public String Nombre;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
