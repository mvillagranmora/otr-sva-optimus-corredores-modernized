package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class PreferenciaPago {
	public String Id;
	public Long IdCliente;
	public Long IdPortafolio;
	public String IdMoneda;
	public String IdMedioDePago;
	public Long IdCuentaBancaria;
	public Long IdPreferenciaPagoPortafolio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
