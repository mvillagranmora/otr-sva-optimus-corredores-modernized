package cl.bcs.fondomutuo.kit;

/**
 * "This module does not serve this request": the controller answers 503 (no body detail) so the platform router repeats the WHOLE request in
 * TomEE. Thrown by the gates G0..G5 (always BEFORE the first write of the request, after releasing the anti-duplicate lock) and when a
 * collaborator of the router is unavailable. The unit of work rolls back (a non-2xx answer never commits).
 */
public class ServicioNoServidoException extends RuntimeException {

    private final String compuerta;

    public ServicioNoServidoException(String compuerta, String motivo) {
        super(compuerta + ": " + motivo);
        this.compuerta = compuerta;
    }

    /** G0..G5 / G-CUS / G-SPT / ROUTER: which gate closed (log only, never returned to the caller). */
    public String compuerta() {
        return compuerta;
    }
}
