package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class EmpleadoPlantilla {
	public Long IdPortafolio;
	public String PlantillaEmpleadoProductoHabilitado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
