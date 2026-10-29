package cl.bcs.contabilidad.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TabDatosMovimiento {
	public Long IdTipoCentralizacion;
	public Long IdMovimiento;
	public String TipoDato;
	public String ValorVarChar;
	public BigDecimal ValorDecimal;
	
	public TabDatosMovimiento(){
		this.ValorVarChar = "";
		this.ValorDecimal = BigDecimal.ZERO;
	}
	
	public TabDatosMovimiento(Long IdTipoCentralizacion,Long IdMovimiento, String TipoDato, String ValorVarChar, BigDecimal ValorDecimal){
		this.IdTipoCentralizacion = IdTipoCentralizacion;
		this.IdMovimiento = IdMovimiento;
		this.TipoDato = TipoDato;
		this.ValorVarChar = ValorVarChar == null ? "" : ValorVarChar;
		this.ValorDecimal = ValorDecimal == null ? BigDecimal.ZERO : ValorDecimal;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
