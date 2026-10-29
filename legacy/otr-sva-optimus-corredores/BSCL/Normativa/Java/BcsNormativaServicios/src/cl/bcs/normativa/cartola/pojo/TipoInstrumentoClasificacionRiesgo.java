package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class TipoInstrumentoClasificacionRiesgo {
	public String IdTipoInstrumento;
	public String IdEntidadClasificadoraRiesgo;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
