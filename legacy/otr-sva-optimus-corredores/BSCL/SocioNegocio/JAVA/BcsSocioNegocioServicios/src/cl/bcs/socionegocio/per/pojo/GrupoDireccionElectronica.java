package cl.bcs.socionegocio.per.pojo;

import cl.bcs.utils.BcsPojo;

import java.util.ArrayList;
import java.util.List;

public class GrupoDireccionElectronica extends BcsPojo {

    public Long IdInterno;
    public Long IdGrupoDireccion;
    public String Grupo;
    public String Comentario;
    public Long IdPersona;
    public java.sql.Timestamp FechaCreacion;
    public java.sql.Timestamp FechaModificacion;
    public java.sql.Timestamp FechaIngreso;
    public String IdEstado;
    public String IdEstadoAutorizacion;
    public String Borrado;
    public Long IdUsuarioCreador;
    public List<Detalle> Detalles;

    public GrupoDireccionElectronica() {
        this.Detalles = new ArrayList<>();
    }

    public static class Detalle extends BcsPojo {
        public Long IdInterno;
        public Long IdGrupoDireccionDetalle;
        public Long IdDireccionElectronica;
        public Long IdGrupoDireccion;
        public String IdTipoDestinatario;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public java.sql.Timestamp FechaIngreso;
        public String IdEstado;
        public String IdEstadoAutorizacion;
        public String Borrado;
        public Long IdUsuarioCreador;

        public String DireccionElectronica;
    }
}
