package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class GrupoRegimen {
	
	public String IdGrupoRegimenSerieFondo;
	public String IdSerieFondo;
	public String GrupoRegimen;
	public String PermiteCambioGrupo;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
