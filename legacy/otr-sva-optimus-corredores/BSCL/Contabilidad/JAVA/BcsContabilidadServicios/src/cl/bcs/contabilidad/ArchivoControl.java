package cl.bcs.contabilidad;

import cl.bcs.utils.Utils;

public class ArchivoControl  {
	
	private String tipRutina;
	private String desRutina;
	private String desSubRutina;
	private String desEstructura;
	private String desCampoValidar;
	private String posInicial;
	private String posFinal;
	private String mtoSumcampo;
	private String desCampoValidar2;
	private String posInicial2;
	private String posFinal2;
	private String mtoSumcampo2;
	private String mtoSumctareg;
	private String fecProceso;
	private String hraProceso;
	public String getTipRutina() {
		return tipRutina;
	}
	public void setTipRutina(String tipRutina) {
		this.tipRutina = tipRutina;
	}
	public String getDesRutina() {
		return desRutina;
	}
	public void setDesRutina(String desRutina) {
		this.desRutina = desRutina;
	}
	public String getDesSubRutina() {
		return desSubRutina;
	}
	public void setDesSubRutina(String desSubRutina) {
		this.desSubRutina = desSubRutina;
	}
	public String getDesEstructura() {
		return desEstructura;
	}
	public void setDesEstructura(String desEstructura) {
		this.desEstructura = desEstructura;
	}
	public String getDesCampoValidar() {
		return desCampoValidar;
	}
	public void setDesCampoValidar(String desCampoValidar) {
		this.desCampoValidar = desCampoValidar;
	}
	public String getPosInicial() {
		return posInicial;
	}
	public void setPosInicial(String posInicial) {
		this.posInicial = posInicial;
	}
	public String getPosFinal() {
		return posFinal;
	}
	public void setPosFinal(String posFinal) {
		this.posFinal = posFinal;
	}
	public String getMtoSumcampo() {
		return mtoSumcampo;
	}
	public void setMtoSumcampo(String mtoSumcampo) {
		this.mtoSumcampo = mtoSumcampo;
	}
	public String getDesCampoValidar2() {
		return desCampoValidar2;
	}
	public void setDesCampoValidar2(String desCampoValidar2) {
		this.desCampoValidar2 = desCampoValidar2;
	}
	public String getPosInicial2() {
		return posInicial2;
	}
	public void setPosInicial2(String posInicial2) {
		this.posInicial2 = posInicial2;
	}
	public String getPosFinal2() {
		return posFinal2;
	}
	public void setPosFinal2(String posFinal2) {
		this.posFinal2 = posFinal2;
	}
	public String getMtoSumcampo2() {
		return mtoSumcampo2;
	}
	public void setMtoSumcampo2(String mtoSumcampo2) {
		this.mtoSumcampo2 = mtoSumcampo2;
	}
	public String getMtoSumctareg() {
		return mtoSumctareg;
	}
	public void setMtoSumctareg(String mtoSumctareg) {
		this.mtoSumctareg = mtoSumctareg;
	}
	public String getFecProceso() {
		return fecProceso;
	}
	public void setFecProceso(String fecProceso) {
		this.fecProceso = fecProceso;
	}
	public String getHraProceso() {
		return hraProceso;
	}
	public void setHraProceso(String hraProceso) {
		this.hraProceso = hraProceso;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
