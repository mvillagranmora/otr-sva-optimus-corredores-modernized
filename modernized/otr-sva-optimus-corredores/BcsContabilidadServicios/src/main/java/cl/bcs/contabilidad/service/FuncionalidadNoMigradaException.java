package cl.bcs.contabilidad.service;

/**
 * A request needs a legacy capability this module does not implement yet. It is raised before anything is written,
 * so the caller can route the request to the legacy runtime instead (the controller answers 503, the gateway's
 * TomEE fallback signal).
 */
public class FuncionalidadNoMigradaException extends RuntimeException {
    public FuncionalidadNoMigradaException(String message) {
        super(message);
    }
}
