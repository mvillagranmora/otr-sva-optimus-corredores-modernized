package cl.bcs.contabilidad.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TabDatosEvento {
	public Long IdTipoCentralizacion;
	public Long IdMovimiento;
	public String TipoDato;
	public String TipoEvento;
	public String ValorVarChar;
	public BigDecimal ValorDecimal;
	
	public TabDatosEvento(){
		this.ValorVarChar = "";
		this.ValorDecimal = BigDecimal.ZERO;
	}
	
	public TabDatosEvento(Long IdTipoCentralizacion,Long IdMovimiento, String TipoDato, String TipoEvento, String ValorVarChar, BigDecimal ValorDecimal){
		this.IdTipoCentralizacion = IdTipoCentralizacion;
		this.IdMovimiento = IdMovimiento;
		this.TipoDato = TipoDato;
		this.TipoEvento = TipoEvento;
		this.ValorVarChar = ValorVarChar == null ? "" : ValorVarChar;
		this.ValorDecimal = ValorDecimal == null ? BigDecimal.ZERO : ValorDecimal;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
