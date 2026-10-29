package cl.bcs.ordenes.pojo;

public class RegistroControlMercado {
	public String Procesado;
	public String Log;
	public String IdMensaje1;
	public String IdMensaje2;
	public String TipoMensaje;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
