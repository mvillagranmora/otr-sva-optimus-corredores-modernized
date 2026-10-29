package cl.bcs.plataforma.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Explicit class-to-owner map ({@code plataforma.javacall.destinos}): comma separated entries
 * {@code <package prefix ending in a dot>=<ESTRATEGIA>@<base url>}, e.g.
 * {@code cl.bcs.tesoreria.=JAVACALL@http://bcs-tesoreria-servicios:8073}. The longest matching prefix wins.
 * Strategies: {@code JAVACALL} (owner's own {@code POST /internal/java-call}), {@code APV_REST} (typed REST routes of apv-service).
 */
public record Destino(String prefijo, Estrategia estrategia, String url) {

    public enum Estrategia { JAVACALL, APV_REST }

    public static List<Destino> parsear(String configuracion) {
        List<Destino> r = new ArrayList<>();
        if (configuracion == null) return r;
        for (String e : configuracion.split(",")) {
            String t = e.trim();
            if (t.isEmpty()) continue;
            int eq = t.indexOf('=');
            int at = t.indexOf('@', eq + 1);
            if (eq <= 0 || at < 0 || at == t.length() - 1) {
                throw new IllegalArgumentException("plataforma.javacall.destinos: malformed entry '" + t + "'");
            }
            String prefijo = t.substring(0, eq).trim();
            if (!prefijo.endsWith(".")) throw new IllegalArgumentException("plataforma.javacall.destinos: prefix must end with a dot: " + prefijo);
            Estrategia es = Estrategia.valueOf(t.substring(eq + 1, at).trim());
            String url = t.substring(at + 1).trim();
            r.add(new Destino(prefijo, es, url.endsWith("/") ? url.substring(0, url.length() - 1) : url));
        }
        r.sort(Comparator.comparingInt((Destino d) -> d.prefijo().length()).reversed());
        return r;
    }

    /** The destination whose prefix is the longest one matching {@code clase}, or null. */
    public static Destino buscar(List<Destino> destinos, String clase) {
        for (Destino d : destinos) {
            if (clase.startsWith(d.prefijo())) return d;
        }
        return null;
    }
}
