package cl.bcs.valo2.repositorios;

public class RegistroNoEncontradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegistroNoEncontradoException(
            final String message) {
        super(message);
    }

}
