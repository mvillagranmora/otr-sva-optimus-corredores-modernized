package cl.bcs.valo2.data;

public class ProveedorDatosException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProveedorDatosException(
            final String message) {
        super(message);
    }

    public ProveedorDatosException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
