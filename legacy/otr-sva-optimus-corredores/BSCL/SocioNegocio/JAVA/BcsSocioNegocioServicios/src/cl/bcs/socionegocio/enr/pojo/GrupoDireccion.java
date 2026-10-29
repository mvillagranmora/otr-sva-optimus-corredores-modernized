package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class GrupoDireccion extends BcsPojo {
    public Long   IdEnrolamiento;
    public Long   IdGrupoDireccion;
    public Long   IdInterno;
    public String Grupo;
    public String Comentario;
    public Long IdGrupoDireccionPersona;
    
    public GrupoDireccion() {
    	this.IdGrupoDireccionPersona = 0L;
    }
}
