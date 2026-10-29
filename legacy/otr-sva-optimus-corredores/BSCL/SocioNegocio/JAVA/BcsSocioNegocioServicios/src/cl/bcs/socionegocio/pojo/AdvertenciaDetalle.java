package cl.bcs.socionegocio.pojo;

import cl.bcs.utils.Utils;

public class AdvertenciaDetalle {
	public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion){
		this.Codigo = codigo;
		this.RequiereAutorizacion = requiereAutorizacion;
		this.Descripcion = descripcion;
	}
	
	public AdvertenciaDetalle() {
		
	}
	public String Codigo;
	public String RequiereAutorizacion;
	public String Descripcion;
	
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
