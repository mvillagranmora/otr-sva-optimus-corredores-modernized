package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class DireccionElectronica extends BcsPojo {
	public String Accion;
	public Long IdInterno;
	public Long IdCliente;
	public Long IdDireccionElectronica;
	public String Direccion;
	public String CodigoProposito;
	public String Comentario;
	public String IdEstado;
	public Long IdPersona;
	public String Predeterminado;
	public Long IdUsuarioCreador;
	public String IdEstadoAutorizacion;
	public String TipoEnrolamiento;
	public String CodigoFormulario;		
	public String 	Tipo;
	public String EsBorrador;

	public DireccionElectronica(){
		this.Predeterminado = "N";
	}
}
