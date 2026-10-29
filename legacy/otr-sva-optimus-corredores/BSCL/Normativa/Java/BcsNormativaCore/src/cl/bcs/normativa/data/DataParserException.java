package cl.bcs.normativa.data;

/**
 * {@link Exception} para indicar que hubo un error al interpretar un dato.
 *
 * @author fpino@email.com
 *
 */
public class DataParserException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public DataParserException(final String message) {
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
    public DataParserException(final String message, final Throwable cause) {
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
