package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.domain.Programa;

import java.util.Map;
import java.util.Optional;

/**
 * Chooses the job module (base URL) for a program from {@code jobmanager.destinos}: an exact IdTipoPrograma wins;
 * otherwise the longest key that is a package prefix of TipoPrograma.NombreClasePrograma ("cl.bcs.tesoreria" matches
 * "cl.bcs.tesoreria.jobs.X" and "cl.bcs.tesoreria", not "cl.bcs.tesoreriax.Y").
 */
public final class DestinoResolver {

    private final Map<String, String> destinos;

    public DestinoResolver(Map<String, String> destinos) {
        this.destinos = destinos == null ? Map.of() : Map.copyOf(destinos);
    }

    public Optional<String> resolver(Programa programa) {
        String porTipo = limpio(destinos.get(programa.getIdTipoPrograma()));
        if (porTipo != null) {
            return Optional.of(porTipo);
        }
        String clase = programa.getNombreClasePrograma();
        if (clase == null) {
            return Optional.empty();
        }
        String mejorClave = null;
        for (String clave : destinos.keySet()) {
            boolean prefijo = clase.equals(clave) || clase.startsWith(clave + ".");
            if (prefijo && (mejorClave == null || clave.length() > mejorClave.length())) {
                mejorClave = clave;
            }
        }
        return mejorClave == null ? Optional.empty() : Optional.ofNullable(limpio(destinos.get(mejorClave)));
    }

    private static String limpio(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String u = url.trim();
        return u.endsWith("/") ? u.substring(0, u.length() - 1) : u;
    }
}
