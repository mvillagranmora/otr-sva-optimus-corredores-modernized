package cl.bcs.fondomutuo.jobs.domain;

/**
 * Mirrors legacy PlataformaUtils.MensajeSalida.
 * Carries error or warning messages returned from FFMM services.
 */
public record MensajeSalida(String tipo, String descripcion, String id) {

    public static final String MENSAJE_ERROR      = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";

    /** Convenience factory for error messages without an id. */
    public static MensajeSalida error(String descripcion) {
        return new MensajeSalida(MENSAJE_ERROR, descripcion, null);
    }
}
