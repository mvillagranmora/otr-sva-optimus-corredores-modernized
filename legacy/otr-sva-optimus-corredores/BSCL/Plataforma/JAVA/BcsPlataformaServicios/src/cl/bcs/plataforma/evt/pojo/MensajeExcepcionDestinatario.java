package cl.bcs.plataforma.evt.pojo;

import cl.bcs.utils.Utils;

public class MensajeExcepcionDestinatario {
	public Long 		IdInterno;
	public Long 		IdMensaje;
	public Long 		IdPersonaDestinatario;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
