package cl.bcs.socionegocio.wbs;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.socionegocio.wbs.pojo.Indicador;

public class DatosPortafolio {

	public Long IdPortafolio;
	public Long IdPersona;
	public Long IdEjecutivoComercial;
	public List<Indicador> Indicadores;
	
	public DatosPortafolio() {

		this.IdPersona = 0L;
		this.IdPortafolio = 0L;
		this.IdEjecutivoComercial = 0L;
		this.Indicadores = new ArrayList<>();
	}
	
}
