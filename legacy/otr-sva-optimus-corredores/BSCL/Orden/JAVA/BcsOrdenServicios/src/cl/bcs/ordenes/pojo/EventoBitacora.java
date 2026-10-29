package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class EventoBitacora {
	public Long IdEventoBitacora;
	public String NombreEntidad;
	public Long IdOrden;
	public Long IdAsignacion;
	public Long IdTransaccion;
	public String MensajeEventoBitacora;
	public java.sql.Timestamp FechaEventoBitacora;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String IdTipoEventoBitacora;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
