package cl.bcs.portalconfirmacion.domain;

/**
 * Immutable value object representing a template tag (key/value pair) bound to an event type.
 *
 * Modernized from legacy cl.bcs.notificacion.template.KeyValue (mutable POJO).
 */
public record KeyValue(String codigo, String key, String value, int idEvento) {

    /**
     * Convenience constructor without idEvento (defaults to 0).
     */
    public KeyValue(String codigo, String key, String value) {
        this(codigo, key, value, 0);
    }
}
