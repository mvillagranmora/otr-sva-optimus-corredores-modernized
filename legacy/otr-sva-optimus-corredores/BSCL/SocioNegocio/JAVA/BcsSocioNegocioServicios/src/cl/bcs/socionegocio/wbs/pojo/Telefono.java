package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class Telefono extends BcsPojo {
	public String Accion;
	public Long IdInterno;
	public Long IdCliente;
	public Long IdTelefono;
	public Long IdPais;
	public String IdTipoTelefono;
	public String Numero;
	public String Anexo;
	public String CodigoProposito;
	public String Comentario;
	public String IdEstado;
	public Long IdPersona;
	public String Predeterminado;
	public Long IdUsuarioCreador;
	public String IdEstadoAutorizacion;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public String Tipo;
	public String EsBorrador;
	
	public Telefono(){
		this.Predeterminado = "N";
	}
}
