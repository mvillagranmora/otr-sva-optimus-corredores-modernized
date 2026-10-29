package cl.bcs.sql.utils;

/**
 * {@link RuntimeException} para indicar que hubo un error durante una
 * ejecuci&oacute;n masiva de sentencias SQL, mediante JDBC.
 *
 * @author fpino@email.com
 *
 */
public class SqlBulkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public SqlBulkException(final String message) {
        super(message);
    }

}
