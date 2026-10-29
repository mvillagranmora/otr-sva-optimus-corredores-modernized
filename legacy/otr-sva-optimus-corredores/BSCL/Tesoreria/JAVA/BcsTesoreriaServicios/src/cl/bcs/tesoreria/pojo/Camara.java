package cl.bcs.tesoreria.pojo;

import cl.bcs.utils.Utils;

public class Camara {
	public String Camara;
	public String NombreGrupoCamara;
	public String IdEstado;
	public String IdCamaraDeCompensacion;
	public String Descripcion;
	public String CodigoGrupo;
	
	
	public Camara(String camara) {
		super();
		Camara = camara;
	}


	public Camara() {
		super();
		Camara = "";
		NombreGrupoCamara = "";
		IdEstado = "";
		IdCamaraDeCompensacion = "";
		Descripcion = "";
		CodigoGrupo = "";
	}


	public String toString() {
		return Utils.printJson(this, false);
	}
}
