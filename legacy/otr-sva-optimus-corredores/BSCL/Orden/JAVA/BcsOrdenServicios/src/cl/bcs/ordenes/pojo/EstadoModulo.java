package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EstadoModulo {
	
	public String IdEstadoModulo;
	public String NombreModulo;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
