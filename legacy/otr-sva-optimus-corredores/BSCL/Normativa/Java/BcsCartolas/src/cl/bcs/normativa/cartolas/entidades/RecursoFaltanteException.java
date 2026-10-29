package cl.bcs.normativa.cartolas.entidades;

public class RecursoFaltanteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoFaltanteException(
            final String message) {
        super(message);
    }

}
