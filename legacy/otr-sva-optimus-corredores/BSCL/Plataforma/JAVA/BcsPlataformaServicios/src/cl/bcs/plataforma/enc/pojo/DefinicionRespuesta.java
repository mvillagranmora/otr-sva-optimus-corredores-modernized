package cl.bcs.plataforma.enc.pojo;

import cl.bcs.utils.Utils;

public class DefinicionRespuesta {
	
	public Long IdEmpresa;
	public Long IdDefinicionRespuesta;
	public Long IdDefinicionPregunta;
	public Long IdUsuarioCreacion;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String TextoRespuesta;
	public Long Puntaje;
	public String Borrado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
