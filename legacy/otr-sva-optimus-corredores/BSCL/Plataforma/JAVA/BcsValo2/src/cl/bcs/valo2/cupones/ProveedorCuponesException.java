package cl.bcs.valo2.cupones;

import cl.bcs.valo2.entidades.Bono;

public class ProveedorCuponesException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProveedorCuponesException(
            final String message,
            final Bono bono) {
        super(message + ": " + bono);
    }

}
