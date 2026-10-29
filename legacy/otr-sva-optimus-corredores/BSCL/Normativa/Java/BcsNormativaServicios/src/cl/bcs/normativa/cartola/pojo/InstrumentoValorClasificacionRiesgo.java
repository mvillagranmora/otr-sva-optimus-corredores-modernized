package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class InstrumentoValorClasificacionRiesgo {
	public String Id;
	public String IdEntidadClasificadoraRiesgo;
	public String ValorClasificacionRiesgo;
	public Long IdInstrumento;
	
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
