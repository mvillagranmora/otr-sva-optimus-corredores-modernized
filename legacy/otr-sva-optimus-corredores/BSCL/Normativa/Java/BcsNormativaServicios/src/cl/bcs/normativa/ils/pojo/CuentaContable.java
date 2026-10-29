package cl.bcs.normativa.ils.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class CuentaContable {
	public String			IdCodigoContabilidad;
	public String			IdCodigoContabilidadPadre;
	public BigDecimal		MontoDebe;
	public BigDecimal		MontoHaber;
	public BigDecimal		Saldo;
	public Integer			Nivel;
	public String			Imputable;
	public String			NombreCuenta;
	public String			TipoCuenta;
	
	public CuentaContable() {
		MontoDebe 	= BigDecimal.ZERO;
		MontoHaber 	= BigDecimal.ZERO;
		Saldo 		= BigDecimal.ZERO;
	}

	public String toString() {
		return Utils.printJson(this, false);
	}
}
