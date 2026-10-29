package cl.bcs.normativa.esquema;

/**
 * {@link RuntimeException} para indicar rango inv&aacute;lido de fechas.
 *
 * @author fpino@email.com
 *
 */
public class RangoFechasInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor est&aacute;ndar dado un mensaje.
     *
     * @param message
     *            El mensaje.
     */
    public RangoFechasInvalidoException(final String message) {
        super(message);
    }

}
