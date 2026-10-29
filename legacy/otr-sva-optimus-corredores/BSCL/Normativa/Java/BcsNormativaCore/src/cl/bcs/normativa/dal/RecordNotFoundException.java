package cl.bcs.normativa.dal;

/**
 * {@link RuntimeException} para indicar que no se encontr&oacute; un registro.
 *
 * @author fpino@email.com
 *
 */
public class RecordNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public RecordNotFoundException(final String message) {
        super(message);
    }

}
