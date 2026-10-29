package cl.bcs.plataforma.domain;

/**
 * Standard output message wrapper used across all platform services.
 */
public class MensajeSalida {

    public static final String MENSAJE_ERROR       = "E";
    public static final String MENSAJE_INFORMACION = "I";
    public static final String MENSAJE_ADVERTENCIA = "W";

    public Long   idMensaje;
    public String tipoMensaje;
    public String codigo;
    public String descripcion;

    public MensajeSalida() {}

    public MensajeSalida(Long idMensaje, String tipoMensaje, String codigo, String descripcion) {
        this.idMensaje    = idMensaje;
        this.tipoMensaje  = tipoMensaje;
        this.codigo       = codigo;
        this.descripcion  = descripcion;
    }
}
