package cl.bcs.cuentainversion.reportes;

import java.util.List;

/**
 * Message of the JavaCall output ({@code P_OUT_MENSAJES}) of the reporting family: same JSON as the legacy
 * {@code PlataformaUtils.MensajeSalida} ({@code id}, {@code tipo}, {@code codigo}, {@code descripcion}).
 * {@code descripcion} is the dictionary text of {@code codigo} with its parameters replaced (see {@link Mensajeria}).
 */
public record MensajeSalida(Long id, String tipo, String codigo, String descripcion) {

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String AUTORIZACION = "AUTORIZACION";
    public static final String INFORMACION = "INFORMACION";

    /** Number of messages of type ERROR (legacy {@code getNumeroErrores}; warnings are not errors). */
    public static long contarErrores(List<MensajeSalida> mensajes) {
        return mensajes.stream().filter(m -> ERROR.equals(m.tipo())).count();
    }
}
