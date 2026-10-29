package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * The thing under test, seen through the production entry point of a JavaCall class: {@code call(Connection, JsonObject)}.
 * "servicio" is the simple class name of cl.bcs.fondomutuo (ManejadorSolicitudAporteRescate | CrearSolicitudAporteRescateNormal).
 * LegacySubject runs the REAL unmodified legacy; MutantSubject breaks it on purpose (harness self check); the modernized implementation is
 * plugged in later with -Doracle.subject=<FQCN of a Subject> (public no-arg constructor) on the same seeded H2 session, same cases, same dump.
 */
public interface Subject {
    JsonObject call(String servicio, Connection con, JsonObject jin) throws Exception;
}
