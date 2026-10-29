package cl.bcs.cartolas.domain;

/**
 * Constantes de estado de proceso, equivalentes a
 * {@code cl.bcs.normativa.cartolas.entidades.EstadoProceso} del legacy.
 */
public final class EstadoProceso {

    public static final String EN_PROCESO = "EN PROCESO";
    public static final String ERROR      = "ERROR";
    public static final String INGRESADO  = "INGRESADO";
    public static final String NORMAL     = "NORMAL";
    public static final String TERMINADO  = "TERMINADO";

    private EstadoProceso() {
    }
}
