package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ArchivoCuadraturaNacional {
	public String RutAdministradora;
	public String Cuenta;
	public String Saldo;
	public String RunFondo;
	public String Serie;
	public String AhorroNeto;
	public String IdSerieFondo;
	public String IdRegimen;
	public String toString() {
		return Utils.printJson(this, false);
	}
}
