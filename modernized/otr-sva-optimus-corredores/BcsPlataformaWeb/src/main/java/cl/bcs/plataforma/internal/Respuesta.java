package cl.bcs.plataforma.internal;

/** What a target answered: status, content type and body, to be returned to the job unchanged. */
public record Respuesta(int status, String contentType, String cuerpo) {

    /** HTTP 503: the target did not run the call (class not migrated / not available): the only status that falls back to TomEE. */
    public boolean noDisponible() {
        return status == 503;
    }
}
