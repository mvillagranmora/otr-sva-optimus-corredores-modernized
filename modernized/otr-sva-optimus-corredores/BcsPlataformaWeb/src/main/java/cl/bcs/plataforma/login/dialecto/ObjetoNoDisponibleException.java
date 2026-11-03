package cl.bcs.plataforma.login.dialecto;

/**
 * A HANA object the login needs has no usable equivalent in the active dialect: it is marked {@code NO_DISPONIBLE} in the equivalence table or it is not in
 * it at all. It is an {@link UnsupportedOperationException} so that the module's {@code GlobalExceptionHandler}, unchanged, answers 503. It is raised
 * before anything is read, and the request never falls back to the other database.
 */
public class ObjetoNoDisponibleException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    private final String objetoHana;

    public ObjetoNoDisponibleException(String objetoHana) {
        super("Object not available in Postgres: " + objetoHana);
        this.objetoHana = objetoHana;
    }

    /** The HANA name of the object, as it appears in the equivalence table. */
    public String objetoHana() {
        return objetoHana;
    }
}
