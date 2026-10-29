package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class ValorCuota {
	public String IdSerieFondo;
    public String FechaCierre;
    public BigDecimal ValorCuota;
    public String IdMoneda;
    public String Observacion;     
    public String FechaCarga;
    public Timestamp FechaIngresoRegistro;
    public Long IdEmpresa;
    
    public String toString() {
		return Utils.printJson(this, false);
	}
}
