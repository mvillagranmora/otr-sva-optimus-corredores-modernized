package cl.bcs.normativa.domain;

/**
 * RuntimeException para errores de esquema normativo.
 * Migrado desde legacy cl.bcs.normativa.esquema.EsquemaException.
 */
public class EsquemaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EsquemaException(String message) {
        super(message);
    }
}
