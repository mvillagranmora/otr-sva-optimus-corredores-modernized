package cl.bcs.utils;

/**
 * {@link RuntimeException} para indicar que una propiedad no existe.
 *
 * @author fpino@email.com
 *
 */
public class PropertyNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public PropertyNotFoundException(final String message) {
        super(message);
    }

}
