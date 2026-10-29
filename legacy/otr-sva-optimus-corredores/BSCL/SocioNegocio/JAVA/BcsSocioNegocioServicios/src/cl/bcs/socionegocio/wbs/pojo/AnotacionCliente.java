package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class AnotacionCliente extends BcsPojo{
	public Long IdAnotacion;
	public Long IdCliente;
	public Long IdPortafolio;
	public String Titulo;
	public String IdAmbito;
	public String Ambito;
	public String IdMotivo;
	public String Motivo;
	public String IdRelevancia;
	public String Relevancia;
	public String TieneAdjunto;
	public String NombreArchivo;
	public String FechaIngreso;
	public String ValidacionDesde;
	public String ValidacionHasta;
	public String Origen;
	
	public AnotacionCliente () {
		this.IdAnotacion = 0L;
		this.IdCliente = 0L;
		this.IdPortafolio = 0L;
		this.Titulo = "";
		this.IdAmbito = "";
		this.Ambito = "";
		this.IdMotivo = "";
		this.Motivo = "";
		this.IdRelevancia = "";
		this.Relevancia = "";
		this.TieneAdjunto = "";
		this.NombreArchivo = "";
		this.FechaIngreso = "0001-01-01";
		this.ValidacionDesde = "0001-01-01";
		this.ValidacionHasta = "0001-01-01";
		this.Origen = "";
	}
}