package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class DireccionElectronica extends BcsPojo {
	public Long IdInterno;
	public Long IdDireccionElectronica;	
	public Long IdEnrolamiento;
	public String Direccion;
	public String CodigoProposito;
	public String Comentario;
	public String Predeterminado;
	public Long IdDireccionElectronicaPersona;
	public String IdEstado;
	public String EsBorrador;
	
	public DireccionElectronica() {
		this.IdDireccionElectronicaPersona = 0L;
		this.IdEstado = "";
		this.EsBorrador = "S";
	}
}
