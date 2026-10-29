package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class EstadoOperacion {
	public Long FolioCartera;
	public Long numeroAnu;
	public String toString() {
		return Utils.printJson(this, false);
	}
}
