package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class OmnibusDetalle {
	public Long IdOmnibusDetalle;
	public Long IdOmnibus;
	public String IdSerieFondo;
	public String FechaCertificacion;
	public Long CantidadOperaciones;
	public BigDecimal ValorCuota;
	public BigDecimal UltimoValorCuota;
	public BigDecimal Monto;
	public BigDecimal CantidadCuotas;
	public BigDecimal ValorCuotaAGF;
	public BigDecimal MontoAGF;
	public BigDecimal CantidadCuotasAGF;
	public BigDecimal DiferenciaValorCuota;
	public BigDecimal DiferenciaValorCuotaAGF;
	public String IdEstado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
