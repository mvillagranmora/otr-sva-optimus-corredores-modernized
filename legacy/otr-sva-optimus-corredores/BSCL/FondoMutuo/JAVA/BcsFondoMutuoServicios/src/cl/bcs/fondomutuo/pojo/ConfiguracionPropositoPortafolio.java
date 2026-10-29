package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ConfiguracionPropositoPortafolio {
	
	public String Id;
	public String IdRegimenFondoMutuo;
	public String IdPropositoPortafolio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
