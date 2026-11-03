package cl.bcs.plataforma.config;

/**
 * Something the active deployment cannot do yet: a statement, sequence or stored procedure with no text for the active SQL dialect, or a generic dispatcher
 * that only exists for HANA. {@link GlobalExceptionHandler} answers it with 503. It extends {@link UnsupportedOperationException} so existing callers keep
 * working, but only this class means "not available": other {@code UnsupportedOperationException}s (Gson on a JSON null, {@code List.of}...) are real
 * errors and must not look like a missing feature.
 */
public class NoDisponibleException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    public NoDisponibleException(String message) {
        super(message);
    }
}
