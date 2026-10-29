package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class GrupoDireccionDetalle extends BcsPojo {
    public Long IdGrupoDireccionDetalle;
    public Long IdDireccionElectronica;
    public String IdTipoDestinatario;
    public Long IdGrupoDireccion;
    public Long IdInterno;
    public Long IdGrupoDireccionPersona;
    public Long IdGrupoDireccionDetallePersona;
    public GrupoDireccionDetalle() {
    	this.IdGrupoDireccionPersona = 0L;
    	this.IdGrupoDireccionDetallePersona = 0L;
    }
}
