package cl.bcs.apv.application.autorizaciones;

/** Legacy {@code BcsAutorizaciones.ResultadoAutorizacion}: the outcome of one event ({@link #EXITO} or {@link #ERROR}). */
public record ResultadoAutorizacion(Long evento, String resultado) {

    public static final String EXITO = "EXITO";
    public static final String ERROR = "ERROR";
}
