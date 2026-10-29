package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class SerieFondoCuadratura {
	public String RunFondo;
	public String Serie;
	public String ISIN;
	public String IdSerieFondo;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
