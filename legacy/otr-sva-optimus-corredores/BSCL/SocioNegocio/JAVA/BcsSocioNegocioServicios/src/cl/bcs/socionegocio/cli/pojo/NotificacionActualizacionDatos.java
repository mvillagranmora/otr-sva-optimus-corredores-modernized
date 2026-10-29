package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.Utils;

public class NotificacionActualizacionDatos {

	public Long IdCliente;
	public Long IdDireccion;
	public Long IdPersona;
	public String FechaVencimiento;
	public String Nombre;
	public String DocumentoIdentidad;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
