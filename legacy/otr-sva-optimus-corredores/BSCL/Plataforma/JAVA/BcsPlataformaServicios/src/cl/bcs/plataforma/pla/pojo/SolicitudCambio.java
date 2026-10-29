package cl.bcs.plataforma.pla.pojo;

import cl.bcs.utils.BcsPojo;

public class SolicitudCambio extends BcsPojo {
	public Long IdSolicitudCambio;
	public String CampoModificado;
	public String TipoDato;
	public String ValorAntiguo;
	public String ValorNuevo;
	public Long IdEntidadOrigenLong;
	public String IdTipoEntidad;
	public String IdEstadoAutorizacion;
	
	public SolicitudCambio() {
	    this.IdEstadoAutorizacion = "";
	}
}
