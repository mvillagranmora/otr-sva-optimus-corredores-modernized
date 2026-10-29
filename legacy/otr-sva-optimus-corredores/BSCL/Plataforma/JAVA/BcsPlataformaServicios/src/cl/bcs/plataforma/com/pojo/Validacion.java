package cl.bcs.plataforma.com.pojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public class Validacion extends BcsPojo{

	public Boolean EsError;
	public MensajeSalida MensajeError;
	
	public Validacion() {
		this.EsError = false;
	}

	
}
