package cl.bcs.normativa.cartolas.entidades;

public class NominaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NominaException(
            final String message) {
        super(message);
    }

    public NominaException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
