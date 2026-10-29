package cl.bcs.portafolio.pojo;

import cl.bcs.utils.Utils;

public class MatrizAgrupadorNOAPV {
	public Long IdMatrizAgrupadorNOAPV;
	public String CodigoPortafolio;
	public String EsADC;
	public String IdEstado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
