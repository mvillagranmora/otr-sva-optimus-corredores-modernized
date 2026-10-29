package cl.bcs.socionegocio.per.pojo;

import cl.bcs.utils.BcsPojo;

public class DireccionElectronica extends BcsPojo {
	public Long IdDireccionElectronica;
	public Long IdPersona;
	public String NombreCompleto;
	public String DocumentoIdentidad;
	public String Direccion;
	public String CodigoProposito;
	public String Comentario;
	public String IdEstado;
	public String Predeterminado;
	
	public DireccionElectronica(){
		this.Predeterminado = "N";
	}
}
