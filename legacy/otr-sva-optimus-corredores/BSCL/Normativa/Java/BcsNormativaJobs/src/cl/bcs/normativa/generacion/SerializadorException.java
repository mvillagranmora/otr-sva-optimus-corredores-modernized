package cl.bcs.normativa.generacion;

/**
 * {@link Exception} para indicar que hay un error al serializar.
 *
 * @author fpino@email.com
 *
 */
public class SerializadorException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public SerializadorException(final String message) {
        super(message);
    }

    /**
     * Constructor est&aacute;ndar dado un mensaje y la causa.
     *
     * @param message
     *            El mensaje.
     * @param cause
     *            La causa.
     */
    public SerializadorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * M&eacute;todo conveniente para obtener el mensaje representativo de la
     * {@link Exception}, privilegiando el mensaje asociado a la causa de la
     * misma.
     *
     * @return El mensaje representativo de la {@link Exception}
     */
    public String getCauseMessage() {
        if (getCause() != null) {
            return getCause().getMessage();
        }

        return getMessage();
    }

}
