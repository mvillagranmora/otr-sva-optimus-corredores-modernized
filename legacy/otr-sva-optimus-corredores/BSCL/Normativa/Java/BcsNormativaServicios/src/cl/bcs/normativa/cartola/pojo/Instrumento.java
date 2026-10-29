package cl.bcs.normativa.cartola.pojo;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.utils.Utils;

public class Instrumento {
	public Long IdInstrumento;
	public Date FechaVencimiento;
	public Date FechaEmision;
	public String IdTipoReajuste;
	public String IdEmisor;
	public String RazonSocial;
	public BigDecimal CorteMinimo;
	public BigDecimal Tasa;
	public String IdTipoInstrumento;
	public Long CodigoRubro;
	public String TieneCupon;
	public String toString() {
		return Utils.printJson(this, false);
	}
}
