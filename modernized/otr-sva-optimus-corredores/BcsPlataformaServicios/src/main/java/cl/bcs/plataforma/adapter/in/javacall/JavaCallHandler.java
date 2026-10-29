package cl.bcs.plataforma.adapter.in.javacall;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * In-process JavaCall entry, the same contract as legacy {@code cl.bcs.utils.JavaCall.call(Connection, JsonObject)}: the connection is the
 * one of the unit of work of the call, the JSON is the legacy one. Plain Java (no Spring) so it can also be run by the equivalence oracle.
 * Only the handlers registered in {@link JavaCallDispatcher} are served.
 */
public interface JavaCallHandler {

    /** Fully qualified name of the LEGACY class this handler replaces (what the router sends in {@code "class"}). */
    String fqn();

    JsonObject call(Connection con, JsonObject jin) throws Exception;
}
