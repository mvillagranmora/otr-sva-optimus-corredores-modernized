package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class TipoCliente extends BcsPojo {
    public String IdTipoCliente;
    public String Nombre;
    public Long IdUsuarioCreador;
    public java.sql.Date FechaCreacion;
    public java.sql.Date FechaModificacion;
    public Long IdEmpresa;
    public String IdEstado;
    public String TipoEnrolamiento;
    public String PorDefecto;
}