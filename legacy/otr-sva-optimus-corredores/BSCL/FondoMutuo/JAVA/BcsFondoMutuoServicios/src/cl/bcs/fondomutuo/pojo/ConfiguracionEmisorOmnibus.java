package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.BcsPojo;

public class ConfiguracionEmisorOmnibus extends BcsPojo{
	public String IdEmisor;
	public String IdentificadorCuenta;
	public String NumeroCuenta;
	public String GlosaComision;
	public Long FolioOperacion;
	public String ReiniciaFolioDiariamente;
	
	public ConfiguracionEmisorOmnibus() {
		this.IdentificadorCuenta = "";
		this.NumeroCuenta = "";
		this.GlosaComision = "";
		this.FolioOperacion = 0L;
		this.ReiniciaFolioDiariamente = "N";
	}
}
