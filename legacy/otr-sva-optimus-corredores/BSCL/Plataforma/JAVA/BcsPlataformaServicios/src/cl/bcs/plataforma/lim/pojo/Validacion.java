package cl.bcs.plataforma.lim.pojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;

public class Validacion extends BcsPojo {
	public Long IdInternoOperacion;
	public Long IdInternoGrupoOperacion;
	public String TipoComportamiento;
	public PlataformaUtils.MensajeSalida Mensaje;
	
	public Validacion() {
		Mensaje = new PlataformaUtils.MensajeSalida();
	}
}