package cl.bcs.socionegocio.pojo;

import cl.bcs.utils.Utils;

public class ErrorDetalle {
	public ErrorDetalle(String codigo, String descripcion){
		this.Codigo = codigo;
		this.Descripcion = descripcion;
		this.IdEntidad = 0L;
	}
	public ErrorDetalle() {
		this.IdEntidad= 0L;
	}
	
	public String Codigo;
	public String Descripcion;
	public Long IdEntidad;
	
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
