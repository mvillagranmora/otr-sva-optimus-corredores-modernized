package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class Telefono extends BcsPojo {
	public Long IdInterno;
	public Long IdTelefono;
	public Long IdEnrolamiento;
	public Long IdPais;
	public String IdTipoTelefono;
	public String Numero;
	public String Anexo;
	public String CodigoProposito;
	public String Comentario;
	public String Predeterminado;
	public Long IdTelefonoPersona;
	public String IdEstado;
	public String EsBorrador;
	
	public Telefono() {
		this.IdTelefonoPersona = 0L;
		this.IdEstado = "";
		this.EsBorrador = "S";
	}
}
