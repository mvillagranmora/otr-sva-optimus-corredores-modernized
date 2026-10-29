package cl.bcs.sura.socionegocio.pojo;

import cl.bcs.utils.BcsPojo;

public class IdsEntidadYPerfiles extends BcsPojo {
	
	public Long IdPortafolio;
	public Long IdCliente;
	public String DocumentoIdentidad;
	public Long IdIndicador;
	public String 	IdFormaCobro;
	public String 	IdCategoriaInversionista;
    public String	IdPerfilComercial;
    public String	IdPerfilCobros;
    public String	IdPerfilProductos;
    public String	Proposito;
    public String	ActualizarFormaCobro;
	
	public IdsEntidadYPerfiles() {
		this.IdPortafolio = 0L;
		this.IdCliente = 0L;
		this.DocumentoIdentidad = "";
		this.IdIndicador = 0L;
		this.IdFormaCobro = "";
		this.IdCategoriaInversionista = "";
    	this.IdPerfilCobros = "";
    	this.IdPerfilComercial = "";
    	this.IdPerfilProductos = "";
    	this.Proposito = "";
    	this.ActualizarFormaCobro = "S";
	}
}
