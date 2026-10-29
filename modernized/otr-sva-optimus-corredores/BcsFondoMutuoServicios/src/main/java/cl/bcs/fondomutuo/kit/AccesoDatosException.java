package cl.bcs.fondomutuo.kit;

/** A SQL failure inside a port adapter (unchecked so the ports stay free of {@code SQLException}); the controller answers the generic 500. */
public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
