package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ModalidadPago{
	
	public String  IdMedioDePago;
	public String  IdModalidadDePago;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
