package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

public class FormaDePagoOrden {

	public Long IdOrden;
	public Long IdPreferenciaPagoPortafolio;
	public String IdMedioDePago;
	public Long IdCuentaBancaria;
	public String IdTipoCuentaBancaria;
	public String NumeroCuentaBancaria;
	public Long IdTarjetaBancaria;
	public String IdTipoTarjetaBancaria;
	public String NumeroTarjetaBancaria;
	public String IdModalidadDePago;
	public Long IdBanco;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}