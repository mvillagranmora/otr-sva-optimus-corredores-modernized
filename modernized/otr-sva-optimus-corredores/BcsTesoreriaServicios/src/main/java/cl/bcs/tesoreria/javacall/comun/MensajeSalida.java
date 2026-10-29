package cl.bcs.tesoreria.javacall.comun;

import java.util.List;

/**
 * Message of the JavaCall output contract ({@code P_OUT_MENSAJES}); the field names are the legacy JSON names
 * (legacy {@code PlataformaUtils.MensajeSalida}). {@code tipo} is one of the {@code MENSAJE_*} constants.
 */
public record MensajeSalida(Long id, String tipo, String codigo, String descripcion) {

    public static final String MENSAJE_OK = "OK";
    public static final String MENSAJE_ERROR = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
    public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
    public static final String MENSAJE_INFORMACION = "INFORMACION";

    /** Number of messages of type {@link #MENSAJE_ERROR} (legacy {@code getNumeroErrores}). */
    public static long numeroErrores(List<MensajeSalida> mensajes) {
        return mensajes.stream().filter(m -> MENSAJE_ERROR.equals(m.tipo())).count();
    }
}
