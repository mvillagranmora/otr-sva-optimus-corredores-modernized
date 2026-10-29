package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.Utils;

public class RegistroPregunta {
	
	public Long IdEmpresa;
	public Long IdRegistroEncuesta;
	public Long IdRegistroPregunta;
	public java.sql.Timestamp FechaIngresoRegistro;
	public String TextoPregunta;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
