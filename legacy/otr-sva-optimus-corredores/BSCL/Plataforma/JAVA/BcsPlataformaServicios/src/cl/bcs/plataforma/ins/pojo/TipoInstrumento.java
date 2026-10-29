package cl.bcs.plataforma.ins.pojo;

import cl.bcs.utils.Utils;

public class TipoInstrumento {
	
	public String IdTipoInstrumento;
	public String Codigo;
	public String Nombre;
	public String Descripcion;
	public String IdEstado;
	public String IdClasificacionTipoInstrumento;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
