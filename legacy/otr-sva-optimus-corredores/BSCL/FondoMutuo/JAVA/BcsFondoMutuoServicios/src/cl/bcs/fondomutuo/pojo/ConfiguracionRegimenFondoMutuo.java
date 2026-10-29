package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ConfiguracionRegimenFondoMutuo {
	
	public String Id;
	public String IdRegimenFondoMutuoOrigen;
	public String IdRegimenFondoMutuoDestino;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
