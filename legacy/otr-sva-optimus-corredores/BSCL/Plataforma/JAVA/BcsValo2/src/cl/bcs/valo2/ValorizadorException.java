package cl.bcs.valo2;

import cl.bcs.valo2.entidades.Bono;

public class ValorizadorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValorizadorException(
            final String message) {
        super(message);
    }

    public ValorizadorException(
            final String message,
            final Bono bono) {
        super(message + ": " + bono);
    }

    public ValorizadorException(
            final String message,
            final ValorizadorBonosParams params) {
        super(message + ": " + params);
    }

}
