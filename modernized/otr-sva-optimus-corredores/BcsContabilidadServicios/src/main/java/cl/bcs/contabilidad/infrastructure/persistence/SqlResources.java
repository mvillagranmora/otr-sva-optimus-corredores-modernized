package cl.bcs.contabilidad.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single loader for the module's SQL statements.
 *
 * <p>The statements are the legacy files, copied verbatim to {@code src/main/resources/cl/bcs/contabilidad/sql/}
 * (legacy {@code BcsContabilidadServicios/src/cl/bcs/contabilidad/sql/} and {@code BcsContabilidadJobs/.../sql/}).
 * Legacy loaded them with {@code JavaCall.loadResourceToString} and threw {@code IllegalStateException("can't load
 * resource")} when one was missing; this loader keeps that contract: a missing or blank resource is an error that names
 * the file, never an empty statement.
 *
 * <p>Accepted names: the bare legacy file name (e.g. AnularCentralizacion_UpdateEvento + ".sql") or the same name
 * prefixed with {@link #BASE}. Any other path containing '/' is loaded as given (for the module's own non-legacy
 * resources under {@code sql/}).
 */
public final class SqlResources {

    /** Classpath folder holding the legacy SQL files. */
    public static final String BASE = "cl/bcs/contabilidad/sql/";

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private SqlResources() {}

    /** Thrown when a statement is not on the classpath (or is blank). */
    public static final class MissingSqlResourceException extends IllegalStateException {
        MissingSqlResourceException(String path) {
            super("SQL resource not found or empty on classpath: " + path);
        }
    }

    /** Classpath path for a statement name. */
    public static String path(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("SQL resource name is required");
        return name.contains("/") ? name : BASE + name;
    }

    /** Text of the statement; fails loudly when it is missing. */
    public static String load(String name) {
        return CACHE.computeIfAbsent(path(name), SqlResources::read);
    }

    private static String read(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = SqlResources.class.getClassLoader();
        InputStream found = cl.getResourceAsStream(path);
        if (found == null) found = SqlResources.class.getClassLoader().getResourceAsStream(path);
        if (found == null) throw new MissingSqlResourceException(path);
        try (InputStream in = found) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (text.isBlank()) throw new MissingSqlResourceException(path);
            return text;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read SQL resource: " + path, e);
        }
    }
}
