package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

public class Asociacion {
	public Long					IdOrdenAsociada; 
	public Long					IdOrdenN; 
	public Long					IdOrdenM; 
	public Long					IdOrdenMOrigen;
	public java.sql.Date		FechaIngresoRegistroCorta; 
	public java.sql.Timestamp	FechaIngresoRegistro; 
	public String				ModalidadCalculo;
	public BigDecimal			Monto;
	public BigDecimal			Cantidad;
	public BigDecimal			Precio;
	public Long					IdUsuario; 
		
	public Asociacion() {
		ModalidadCalculo="M";
	}
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}	
}