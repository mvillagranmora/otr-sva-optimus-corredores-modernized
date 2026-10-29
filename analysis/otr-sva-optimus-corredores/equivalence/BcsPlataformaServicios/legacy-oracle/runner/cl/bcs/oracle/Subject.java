package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * The thing under test, seen through the production entry point of a JavaCall class: {@code call(Connection, JsonObject)}.
 *
 * <ul>
 *   <li>{@link LegacySubject} runs the REAL, unmodified legacy cl.bcs.plataforma.cre.ConsultarEstadoModulo.</li>
 *   <li>{@link MutantSubject} wraps it and breaks it on purpose (harness self check: the harness must be able to fail).</li>
 *   <li>The modernized implementation is plugged in later with
 *       {@code -Doracle.subject=<fully.qualified.ClassImplementingSubject>} (public no-arg constructor, on the classpath, same JDBC
 *       {@code Connection} on the same seeded H2 database). Nothing else changes: same cases, same dump format, same compare.py.</li>
 * </ul>
 */
public interface Subject {
    JsonObject call(Connection con, JsonObject jin) throws Exception;
}
