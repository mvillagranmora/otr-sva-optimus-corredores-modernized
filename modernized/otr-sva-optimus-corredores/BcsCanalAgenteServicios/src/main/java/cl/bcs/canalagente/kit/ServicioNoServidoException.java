package cl.bcs.canalagente.kit;

/**
 * "This module does not serve this request": raised by a closed gate and by a collaborator that is not ported. The caller (the platform router) repeats the whole
 * request in TomEE. Always raised before the first write of the request.
 */
public class ServicioNoServidoException extends RuntimeException {

    private final String compuerta;

    public ServicioNoServidoException(String compuerta, String motivo) {
        super(compuerta + ": " + motivo);
        this.compuerta = compuerta;
    }

    /** Which gate closed (log only, never returned to the caller). */
    public String compuerta() {
        return compuerta;
    }
}
