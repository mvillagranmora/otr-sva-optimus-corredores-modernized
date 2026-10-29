package cl.bcs.valo2.repositorios;

public class RepositorioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RepositorioException(
            final String message) {
        super(message);
    }

    public RepositorioException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
