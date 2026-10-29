package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class Emisor {
	public String IdEmisor;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
