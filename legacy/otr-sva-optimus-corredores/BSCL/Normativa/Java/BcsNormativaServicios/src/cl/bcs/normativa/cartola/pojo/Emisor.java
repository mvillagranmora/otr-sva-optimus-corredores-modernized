package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class Emisor {
	public String IdEmisor;
	public String NombreCompleto;
	public String NombreCorto;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
