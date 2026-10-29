package cl.bcs.fondomutuo.adapter.in.javacall;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * In-process JavaCall entry, the contract of legacy {@code cl.bcs.utils.JavaCall.call(Connection, JsonObject)}: the connection is the one of the
 * unit of work of the call (the Spring transaction opened by {@code JavaCallUnitOfWorkFilter}), the JSON is the legacy one. Only handlers whose
 * {@link #fqn()} is on the allow-list of {@link JavaCallDispatcher} can be registered.
 */
public interface JavaCallHandler {

    /** Fully qualified name of the LEGACY class this handler replaces (what the router sends in {@code "class"}). */
    String fqn();

    JsonObject call(Connection con, JsonObject jin) throws Exception;
}
