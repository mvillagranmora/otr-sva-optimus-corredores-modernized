package cl.bcs.contabilidad.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TabEventos {
	public Long IdTipoCentralizacion;
	public Long IdMovimiento;
	public String IdEvento;
	public String Nombre;
	public BigDecimal MontoEvento;
	public String CodigoMoneda;
	
	public TabEventos(){}
	
	public TabEventos(Long IdTipoCentralizacion, Long IdMovimiento, String IdEvento, String Nombre, BigDecimal MontoEvento, String CodigoMoneda){
		this.IdTipoCentralizacion = IdTipoCentralizacion;
		this.IdMovimiento = IdMovimiento;
		this.IdEvento = IdEvento;
		this.Nombre = Nombre;
		this.MontoEvento = MontoEvento;
		this.CodigoMoneda = CodigoMoneda;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
