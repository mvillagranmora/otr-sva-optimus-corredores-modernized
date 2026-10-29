package cl.bcs.contabilidad.pojo;

import cl.bcs.utils.Utils;

public class HomologaCuenta {
	public String CuentaOptimus;
	public String CuentaContabilidad;
	public String CodigoContabilidad;
	public String CuentaProveedor;
	public String CuentaEEFF;
	public String CentroCosto;
	public String NombreCuenta;
	public String DetalleCuenta;
	public String Imputable;
	public String TipoCuenta;// Activo o Pasivo
	
	public String toString(boolean pretty) {
		return Utils.printJson(this, pretty);
	}
	
	public String toString() {
		return Utils.printJson(this, true);
	}
	
}
