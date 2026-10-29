package cl.bcs.plataforma.enc.pojo;

import cl.bcs.utils.Utils;

public class DefinicionEncuesta {
	
	public Long IdEmpresa;
	public Long IdDefinicionEncuesta;
	public java.sql.Timestamp FechaIngresoRegistro;
	public Long IdUsuarioCreacion;
	public String Nombre;
	public String IdEstado;
	public String Borrado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
