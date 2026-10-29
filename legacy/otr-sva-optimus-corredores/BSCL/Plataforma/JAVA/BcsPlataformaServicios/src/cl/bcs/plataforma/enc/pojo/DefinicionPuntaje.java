package cl.bcs.plataforma.enc.pojo;

import cl.bcs.utils.Utils;

public class DefinicionPuntaje {
	
	public Long IdEmpresa;
	public String IdCategoriaInversionista;
	public Long PuntajeHasta;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
