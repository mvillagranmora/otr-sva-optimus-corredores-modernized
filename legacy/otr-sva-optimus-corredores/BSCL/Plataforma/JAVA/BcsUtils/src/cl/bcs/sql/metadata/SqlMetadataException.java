package cl.bcs.sql.metadata;

/**
 * {@link RuntimeException} para indicar que hubo un error asociado a la
 * metadata de una base de datos.
 *
 * @author fpino@email.com
 *
 */
public class SqlMetadataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public SqlMetadataException(final String message) {
        super(message);
    }

}
