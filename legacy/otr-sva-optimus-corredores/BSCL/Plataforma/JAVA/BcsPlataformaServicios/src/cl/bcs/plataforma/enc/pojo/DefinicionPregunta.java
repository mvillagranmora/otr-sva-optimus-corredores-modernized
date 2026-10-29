package cl.bcs.plataforma.enc.pojo;

import cl.bcs.utils.Utils;

public class DefinicionPregunta {
	
	public Long IdEmpresa;
	public Long IdDefinicionEncuesta;
	public Long IdDefinicionPregunta;
	public Long IdUsuarioCreacion;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String TextoPregunta;
	public String Borrado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
