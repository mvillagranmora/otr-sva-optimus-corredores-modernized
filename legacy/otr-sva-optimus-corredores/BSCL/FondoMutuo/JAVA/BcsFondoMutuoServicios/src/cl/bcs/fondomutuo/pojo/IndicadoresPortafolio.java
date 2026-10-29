package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class IndicadoresPortafolio {
	
	public Long IdCliente;
	public Long IdPortafolio;
	public String ValorIndicador;
	public String CodigoTipoIndicador;
	
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
