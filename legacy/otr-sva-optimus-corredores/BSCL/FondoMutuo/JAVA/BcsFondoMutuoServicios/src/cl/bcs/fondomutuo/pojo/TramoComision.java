package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.utils.Utils;

public class TramoComision {
	
	public Long IdTramo;
	public Long IdComision;
	public Long DiasDesde;
	public Long DiasHasta;
	public BigDecimal PorcentajeComision;
	public String EstadoTramo;
	public String IdSerieFondo;
	public String Descripcion;
	public String TipoComision;
	public Date FechaVigenciaDesde;
	public Date FechaVigenciaHasta;
	public BigDecimal PorcentajeLiberado;
	public String EstadoComision;
	public Long IdEmpresa;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
