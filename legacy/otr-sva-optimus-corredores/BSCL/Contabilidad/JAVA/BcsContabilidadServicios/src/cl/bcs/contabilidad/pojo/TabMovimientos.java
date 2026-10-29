package cl.bcs.contabilidad.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class TabMovimientos {
	public Long IdTipoCentralizacion;
	public Long IdMovimiento;
	public String IdTipoMovimiento;
	public Long IdEmpresa;
	public Long Numero;
	public Long Linea;     
	public String Fecha;
	public String DocumentoIdentidad;
	public Long IdPortafolio;
	public Long Ejercicio;
	public String Periodo;
	public BigDecimal Monto;
	public String IdMoneda;
	public String Estado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
