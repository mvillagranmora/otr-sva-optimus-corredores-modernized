package cl.bcs.ordenes.pojo;

public class ControlMercado {
	public String TipoMensaje;
	public String IdMensaje1;
	public String IdMensaje2;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Timestamp FechaProcesamiento;
	public String Procesado;
	public String Log;
	
	@Override
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,false);		
	}
	
	public String toString(boolean t) {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
