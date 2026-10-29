package cl.bcs.utils.clave;

/** RULE-031: one error message of the new-password check (legacy MensajeSalida: id 0, tipo "ERROR", codigo ""). */
public record MensajeSalida(long id, String tipo, String codigo, String descripcion) {

    public static final String TIPO_ERROR = "ERROR";

    /** The only shape the legacy emitted. */
    public static MensajeSalida error(String descripcion) {
        return new MensajeSalida(0L, TIPO_ERROR, "", descripcion);
    }
}
