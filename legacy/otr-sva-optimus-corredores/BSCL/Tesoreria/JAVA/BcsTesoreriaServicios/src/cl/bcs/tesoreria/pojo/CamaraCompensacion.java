package cl.bcs.tesoreria.pojo;

import cl.bcs.utils.Utils;

public class CamaraCompensacion {
	public String IdCamaraDeCompensacion;
	public String Codigo;
	public String Nombre;
	public Long  IdPersona;
	public Long  IdPaisCamara;
	public String IdEstadoCamara;
	public String DocumentoIdentidad;
	public String RazonSocial;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
