package cl.bcs.custodia.flujo.kit;

/**
 * Legacy {@code PlataformaUtils.MensajeSalida}: the message element of {@code P_OUT_MENSAJES}. Same public field names and order as the legacy, so its
 * JSON is the legacy one ({@code id}, {@code tipo}, {@code codigo}, {@code descripcion}).
 */
public class MensajeSalida {

    public static final String MENSAJE_ERROR = "ERROR";

    public Long id;
    public String tipo;         // ERROR, AUTORIZACION, ADVERTENCIA, INFORMACION
    public String codigo;
    public String descripcion;

    public MensajeSalida() {
    }

    public MensajeSalida(Long id, String tipo, String codigo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}
