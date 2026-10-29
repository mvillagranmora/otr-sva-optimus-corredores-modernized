package cl.bcs.utils;

/**
 * {@link RuntimeException} para indicar que hay alg&uacute;n error en la ruta
 * de un archivo.
 *
 * @author fpino@email.com
 *
 */
public class BadFilePathException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public BadFilePathException(final String message) {
        super(message);
    }

}
