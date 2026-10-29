package cl.bcs.ordenes.pojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;

public class Validacion extends BcsPojo {
	public Boolean EsError;
	public PlataformaUtils.MensajeSalida Mensaje;

	public Validacion() {
		EsError = false;
	}
}
