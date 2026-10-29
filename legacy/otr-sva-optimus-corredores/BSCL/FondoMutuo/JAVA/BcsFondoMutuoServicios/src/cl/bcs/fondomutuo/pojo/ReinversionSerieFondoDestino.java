package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

public class ReinversionSerieFondoDestino {
	
	public Long IdReinversionSerieFondo;
	public Long IdSolicitudAporteRescate;
	public String IdSerieFondoDestino;
	public BigDecimal Porcentaje;
	public String IdRegimenTributario;
	public String IdFamiliaFondo;
	public String Switch;
	public String IdMoneda;
	
	
	public BigDecimal PorcentajeReal = BigDecimal.ZERO;
	public Date FechaFinalReinversion;
	
	public ReinversionSerieFondoDestino() {
		this.Switch = "N";
		this.FechaFinalReinversion = Fecha.toDate("0001-01-01");
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}