package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class NombreFantasia extends BcsPojo{
	public Long IdInterno;
	public Long IdNombreFantasia;
	public Long IdEnrolamiento;
	public String NombreFantasia;
	public java.sql.Timestamp FechaIngreso;
	public Long IdUsuarioCreador;
	public String EsBorrador;
	public String IdEstado;
	
	public NombreFantasia() {
		this.IdNombreFantasia = 0L;
		this.NombreFantasia = "";
		this.EsBorrador = "S";
		this.IdEstado = "";
	}
}