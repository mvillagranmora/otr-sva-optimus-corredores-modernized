package cl.bcs.utils;

/**
 * {@link RuntimeException} para indicar que hubo un error tratar de bloquear un
 * archivo.
 *
 * @author fpino@email.com
 *
 */
public class LockerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public LockerException(final String message) {
        super(message);
    }

    /**
     * Constructor est&aacute;ndar dado un mensaje y la cusa.
     *
     * @param message
     *            El mensaje.
     * @param cause
     *            La causa.
     */
    public LockerException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
