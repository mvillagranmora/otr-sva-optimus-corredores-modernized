package cl.bcs.normativa.cartola.pojo;

import cl.bcs.utils.Utils;

public class Boveda {
	public String IdBoveda;
	public String Codigo;
	public String Nombre;
	public String Ubicacion;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
