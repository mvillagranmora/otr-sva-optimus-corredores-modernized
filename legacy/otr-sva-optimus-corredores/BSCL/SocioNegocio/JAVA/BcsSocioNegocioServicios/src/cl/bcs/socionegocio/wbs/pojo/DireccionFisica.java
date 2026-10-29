package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class DireccionFisica extends BcsPojo {
	public String Accion;
	public Long IdDireccionFisica;
	public String CodigoProposito;
	public String Sector;
	public String CodigoPostal;
	public String Calle;
	public String Numero;
	public String Complemento;
	public Long IdCiudad;
	public String CodigoDivisionPais;
	public String Comentario;
	public String Predeterminado;
	public String IdEstado;
	public Long IdComuna;
	public Long IdPais;
	public String UsaDivisionPais;
	public String CodigoCompuesto;
	public String Departamento;

	public Long IdPersona;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public String Tipo;
	public Long IdCliente;
	public String EsBorrador;
	
	public DireccionFisica(){
		this.Predeterminado = "N";
		this.Calle = "";
		this.Numero = "";
		this.Sector = "";
		this.Departamento = "";
		
	}
}
