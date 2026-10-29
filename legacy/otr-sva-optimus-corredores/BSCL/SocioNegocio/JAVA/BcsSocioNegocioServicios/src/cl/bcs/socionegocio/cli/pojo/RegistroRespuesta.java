package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.Utils;

public class RegistroRespuesta {
	
	public Long IdEmpresa;
	public Long IdRegistroRespuesta;
	public Long IdRegistroPregunta;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String TextoRespuesta;
	public Long Puntaje;
	public String Seleccionado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
