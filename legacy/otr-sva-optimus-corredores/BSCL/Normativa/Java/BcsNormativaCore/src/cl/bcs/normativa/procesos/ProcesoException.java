package cl.bcs.normativa.procesos;

/**
 * {@link RuntimeException} para indicar que hubo un error durante un proceso.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public ProcesoException(final String message) {
        super(message);
    }

    /**
     * Constructor dado el mensaje y la causa.
     *
     * @param message
     *            El mensaje.
     * @param cause
     *            La causa.
     */
    public ProcesoException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
