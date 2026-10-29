package cl.bcs.normativa.jobs.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generic parameter container for a normativa job execution.
 *
 * The legacy BcsJob used a {@code Map<String, String> parameters} field
 * populated by the Job Manager (BAT.ParametrosPrograma). This record wraps
 * that map and provides typed accessors for the most common parameters.
 *
 * Rules preserved:
 *   - B-NJB-01: jobId uniquely identifies the BAT.Programa row for this run
 *   - B-NJB-02: parameters are key/value string pairs from BAT.ParametrosPrograma
 */
public record ParametrosJob(long jobId, Map<String, String> parameters) {

    public ParametrosJob {
        Objects.requireNonNull(parameters, "parameters must not be null");
        parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    /** Returns a required parameter, throwing {@link IllegalArgumentException} if absent. */
    public String requerido(String nombre) {
        String valor = parameters.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Parámetro requerido ausente: " + nombre);
        }
        return valor;
    }

    /** Returns an optional parameter or the given default. */
    public String opcional(String nombre, String defecto) {
        String valor = parameters.get(nombre);
        return (valor != null && !valor.isBlank()) ? valor : defecto;
    }

    /** Parses a required parameter as {@code long}. */
    public long requeridoLong(String nombre) {
        try {
            return Long.parseLong(requerido(nombre));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Parámetro " + nombre + " no es numérico: " + parameters.get(nombre));
        }
    }

    /** Parses an optional parameter as {@code Long}, returning null if absent. */
    public Long opcionalLong(String nombre) {
        String valor = parameters.get(nombre);
        if (valor == null || valor.isBlank()) return null;
        return Long.parseLong(valor);
    }
}
