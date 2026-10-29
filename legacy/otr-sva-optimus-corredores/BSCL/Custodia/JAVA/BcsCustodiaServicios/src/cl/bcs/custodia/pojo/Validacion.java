package cl.bcs.custodia.pojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;

public class Validacion extends BcsPojo {
	public Long IdInterno;
	public Boolean EsError;
	public PlataformaUtils.MensajeSalida Mensaje;
	
	public Validacion() {
		IdInterno = 0L;
		EsError = false;
		Mensaje = new PlataformaUtils.MensajeSalida();
	}
}
