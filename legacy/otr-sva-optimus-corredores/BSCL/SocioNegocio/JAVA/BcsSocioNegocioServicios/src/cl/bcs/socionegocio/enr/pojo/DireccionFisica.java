package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class DireccionFisica extends BcsPojo {
	public Long IdInterno;
	public Long IdDireccionFisica;
	public Long IdEnrolamiento;
	public String Calle;
	public String Numero;
	public String Departamento;
	public String Sector;
	public String CodigoPostal;
	public Long IdCiudad;
	public Long IdDivisionPais;
	public String CodigoProposito;
	public String Comentario;
	public String CodigoCompuesto;
	public String Predeterminado;
	public Long IdDireccionFisicaPersona;
	public Long IdPais;
	public String EsBorrador;
	public String UsaDivisionPais;
	public String CodigoDivisionPais;
	public String IdEstado;
	
	public DireccionFisica() {
		this.IdDireccionFisicaPersona = 0L;
		this.EsBorrador = "S";
		this.CodigoDivisionPais = "";
		this.CodigoCompuesto = "";
		this.IdEstado = "";
	}
}
