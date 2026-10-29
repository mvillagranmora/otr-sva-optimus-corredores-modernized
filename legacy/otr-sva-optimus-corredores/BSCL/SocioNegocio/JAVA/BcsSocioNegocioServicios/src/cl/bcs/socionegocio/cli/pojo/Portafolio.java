package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.Utils;

public class Portafolio {
	
	public Long	IdPortafolio;
	public String IdPerfilProductos;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
