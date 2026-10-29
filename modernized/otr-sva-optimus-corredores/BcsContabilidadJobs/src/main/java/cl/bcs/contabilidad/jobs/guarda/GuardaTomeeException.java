package cl.bcs.contabilidad.jobs.guarda;

/**
 * The guard could not do its bookkeeping (a mark could not be written or read). It is thrown instead of going on without the record, so
 * the guard fails CLOSED: with no INTENTO mark the TomEE call is not made; with no HECHO mark the job is stopped (it rolls back its own work;
 * the unclosed INTENTO sends any later run of the program to review).
 */
public class GuardaTomeeException extends RuntimeException {

    public GuardaTomeeException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public GuardaTomeeException(String mensaje) {
        super(mensaje);
    }
}
