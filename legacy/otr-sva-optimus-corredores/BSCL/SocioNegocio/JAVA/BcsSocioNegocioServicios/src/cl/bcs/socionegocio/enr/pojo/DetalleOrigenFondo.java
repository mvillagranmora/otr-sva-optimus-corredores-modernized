package cl.bcs.socionegocio.enr.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;

public class DetalleOrigenFondo extends BcsPojo {
	public Long IdDeclaracionOrigenFondos;
	public String IdTipoOrigenFondo;
	public BigDecimal Valor;
	public String Otros;
	public java.sql.Timestamp FechaIngreso;
	public Long IdUsuarioCreador;
	
	public DetalleOrigenFondo() {
		IdTipoOrigenFondo = "";
		Valor = BigDecimal.ZERO;
		Otros = "";
	}
}