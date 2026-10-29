package cl.bcs.utils;

/**
 * {@link RuntimeException} para indicar que hubo un error al interpretar
 * {@link java.util.Date} desde {@link String}.
 *
 * @author fpino@email.com
 *
 */
public class DateFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public DateFormatException(
            final String message) {
        super(message);
    }

    /**
     * Constructor est&aacute;ndar dado un mensaje y la causa del error.
     *
     * @param message
     *            El mensaje.
     * @param cause
     *            La causa del error.
     */
    public DateFormatException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
