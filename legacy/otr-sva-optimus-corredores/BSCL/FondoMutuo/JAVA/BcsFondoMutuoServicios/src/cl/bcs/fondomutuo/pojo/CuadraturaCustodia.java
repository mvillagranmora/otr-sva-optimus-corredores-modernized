package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class CuadraturaCustodia {
	public Long IdCuadraturaCustodia;
	public Long IdEmpresa;
	public String IdSerieFondo;
	public BigDecimal TotalCuotasAGF;
	public BigDecimal TotalCuotas;
	public BigDecimal DiferenciaCuotas;
	public BigDecimal ValorizacionCuotasAGF;
	public BigDecimal ValorizacionCuotas;
	public BigDecimal DiferenciaValorizacion;
	public String FechaCuadratura;
	public Timestamp FechaIngresoRegistro;
	public BigDecimal ValorCuota;
	public String IdRegimen;
	public String IdEstado;
	public String ISIN;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
