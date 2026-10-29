package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class RegimenMandato {
	
	public Long IdEmpresa;
	public String IdSerieFondo;
	public String RescateConMandato;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
