package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ArchivoCuadraturaInternacional {
	public String ISIN;
	public String Cantidad;
	public String Fecha;
	public String IdSerieFondo;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
