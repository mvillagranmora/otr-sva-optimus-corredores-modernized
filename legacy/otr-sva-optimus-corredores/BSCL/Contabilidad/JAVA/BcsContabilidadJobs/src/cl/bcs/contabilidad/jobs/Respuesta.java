package cl.bcs.contabilidad.jobs;

import cl.bcs.utils.Utils;

public class Respuesta {
	public int Codigo;
	public String Mensaje;

	public Respuesta() {
	}
	
	public Respuesta(int codigo, String mensaje) {
		this.Codigo = codigo;
		this.Mensaje = mensaje;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
