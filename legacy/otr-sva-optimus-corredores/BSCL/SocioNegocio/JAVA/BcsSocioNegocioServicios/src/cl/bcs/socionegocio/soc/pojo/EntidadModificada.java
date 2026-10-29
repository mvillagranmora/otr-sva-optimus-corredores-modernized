package cl.bcs.socionegocio.soc.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class EntidadModificada extends BcsPojo {
	
	public String					TipoEntidad;
	public Long						IdEntidad;
	public List<MensajesErrores>	Mensajes;
	
	public EntidadModificada(){
		Mensajes = new ArrayList<MensajesErrores>();
	}

}
