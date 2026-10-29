package cl.bcs.fondomutuo.pojo;

import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class Omnibus {
	public Long IdOmnibus;
	public String FechaOmnibus;
	public Timestamp FechaIngresoRegistro;
	public String IdEstado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
