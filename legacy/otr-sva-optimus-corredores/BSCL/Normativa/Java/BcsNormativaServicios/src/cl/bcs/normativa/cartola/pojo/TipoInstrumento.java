package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class TipoInstrumento {
	public String IdTipoInstrumento;
	public String Nombre;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
