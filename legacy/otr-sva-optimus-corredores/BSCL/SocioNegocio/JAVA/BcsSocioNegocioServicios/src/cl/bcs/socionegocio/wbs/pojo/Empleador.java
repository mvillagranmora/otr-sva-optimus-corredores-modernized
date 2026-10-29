package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class Empleador extends BcsPojo{
	public Long IdEmpleador;
	public String IdTipoPersona;
	public String TipoDocumentoIdentidad;
	public String DocumentoIdentidad;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String RazonSocial;
	public String EsBorrador;
	public String IdTipoCanalIngresoCorredor;
	
	public Empleador (){
		this.IdEmpleador = 0L;
		this.IdTipoPersona = "";
		this.TipoDocumentoIdentidad= "";
		this.DocumentoIdentidad = "";
		this.Nombres = "";
		this.ApellidoPaterno = "";
		this.ApellidoMaterno = "";
		this.RazonSocial = "";
		this.EsBorrador = "N";
		this.IdTipoCanalIngresoCorredor = "";
	}
}