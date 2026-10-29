package cl.bcs.socionegocio.soc.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class PortafolioCliente extends BcsPojo {
	
	List<CuentaCustodio> CuentaCustodio;
	List<PreferenciaPago> PreferenciaPago;
	List<PreferenciaNegocio> PreferenciaNegocio;
	
	public PortafolioCliente() {
		
		CuentaCustodio = new ArrayList<CuentaCustodio>();
		PreferenciaPago = new ArrayList<PreferenciaPago>();
		PreferenciaNegocio = new ArrayList<PreferenciaNegocio>();
		
	}

}
