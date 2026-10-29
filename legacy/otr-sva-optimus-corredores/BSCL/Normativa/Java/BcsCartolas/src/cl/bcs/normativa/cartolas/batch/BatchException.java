package cl.bcs.normativa.cartolas.batch;

public class BatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BatchException(final String message) {
        super(message);
    }

}
