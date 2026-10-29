package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class ClasificacionTipoInstrumento {
	public String IdClasificacionTipoInstrumento;
	public String Nombre;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
