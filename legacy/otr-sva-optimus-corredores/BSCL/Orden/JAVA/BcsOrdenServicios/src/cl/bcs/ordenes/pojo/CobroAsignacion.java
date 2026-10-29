package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CobroAsignacion {
	
	public Long IdCobroAsignacion;
	public Long IdAsignacion;
	public String IdCobro;
	public Timestamp FechaIngresoCobro;
	public Timestamp FechaIngresoRegistro;
	public String FormaCobro;
	public String CodigoCobroClasificacion;
	public String CodigoCobroClasificacionBase;
	public BigDecimal ValorCobro;
	public BigDecimal ValorCobroFinal;
	public BigDecimal ValorCobroAplicado;
	public BigDecimal MontoCobroCalculado;
	public BigDecimal MontoCobroCalculadoLocal;
	public BigDecimal MontoCobroCalculadoLiquidacion;
	public String IdMoneda;

	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
