package cl.bcs.socionegocio.pojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public class Validacion extends BcsPojo {
    public Boolean EsError;
    public MensajeSalida MensajeError;
    public Long IdEntidad;
    public String IdTipoEntidad;

    public Validacion() {
        this.EsError = false;
    }
}
