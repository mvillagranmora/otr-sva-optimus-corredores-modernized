package cl.bcs.normativa.esquema;

/**
 * {@link RuntimeException} para indicar que hay un error asociado a un
 * {@link Esquema} .
 *
 * @author fpino@email.com
 *
 */
public class EsquemaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public EsquemaException(final String message) {
        super(message);
    }

}
