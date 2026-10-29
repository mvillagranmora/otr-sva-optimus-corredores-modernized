package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class RegimenSerieDeFondo {
	
	public String IdRegimenFondoMutuoSerieFondo;
	public String IdRegimenFondoMutuo;
	public String IdSerieFondo;
	public String GrupoRegimen;
	public String PropositoSerieRelacionado;
	public String PermiteOtrosGrupos;
	public String ValidaPortafolioCliente;
	public String ValidaPortafolioAGF;
	public String PermiteRescate;
	public String TipoCarteraNormativa;
	public String PermiteCambioGrupo;
	public String RescateConMandato;
	public String AceptaMandatoEnReinversion;
	public String PermiteAporte;
	public String PermiteTraspaso;
	public String ValidaMontoMinimoInstrumento;
	public String CertificaSinAsociado;
	public String PermiteMLT;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
