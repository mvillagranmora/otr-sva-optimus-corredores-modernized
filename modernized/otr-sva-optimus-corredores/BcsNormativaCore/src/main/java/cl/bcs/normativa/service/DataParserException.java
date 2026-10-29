package cl.bcs.normativa.service;

/**
 * Excepcion para errores al parsear datos normativos.
 * Migrado desde legacy cl.bcs.normativa.data.DataParserException.
 */
public class DataParserException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataParserException(String message) {
        super(message);
    }

    public DataParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
