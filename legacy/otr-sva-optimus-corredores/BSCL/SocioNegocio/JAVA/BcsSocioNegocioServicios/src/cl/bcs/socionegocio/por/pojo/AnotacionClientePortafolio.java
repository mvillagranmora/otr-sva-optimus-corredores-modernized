package cl.bcs.socionegocio.por.pojo;

import cl.bcs.utils.BcsPojo;

public class AnotacionClientePortafolio extends BcsPojo{
	public Long IdAnotacion;
	public Long IdPortafolio;
	public String Titulo;
	public String Detalle;
	public String IdAmbito;
	public String IdMotivo;
	public String IdRelevancia;
	public String ExisteArchivo;
	public String NombreArchivo;
	public String NombreAmbito;
	public String NombreMotivo;
	public String NombreRelevancia;
	public String FechaAnotacion;
	public String FechaIngreso;
	public String FechaCreacion;
	public String FechaModificacion;
	public String ValidoDesde;
	public String ValidoHasta;
	public String Borrado;
	public Long IdUsuarioCreador;
	public String Origen;
	
	public AnotacionClientePortafolio () {
		this.IdAnotacion = 0L;
		this.IdPortafolio = 0L;
		this.Titulo = "";
		this.Detalle = "";
		this.IdAmbito = "";
		this.IdMotivo = "";
		this.IdRelevancia = "";
		this.ExisteArchivo = "";
		this.NombreArchivo = "";
		this.NombreAmbito = "";
		this.NombreMotivo = "";
		this.NombreRelevancia = "";
		this.FechaAnotacion = "0001-01-01";
		this.FechaIngreso = "0001-01-01";
		this.FechaCreacion = "0001-01-01";
		this.FechaModificacion = "0001-01-01";
		this.ValidoDesde = "0001-01-01";
		this.ValidoHasta = "0001-01-01";
		this.Borrado = "N";
		this.IdUsuarioCreador = 0L;
		this.Origen = "";
	}
}
