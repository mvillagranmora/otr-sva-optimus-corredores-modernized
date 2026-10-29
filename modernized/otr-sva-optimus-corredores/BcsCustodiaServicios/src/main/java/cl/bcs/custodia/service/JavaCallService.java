package cl.bcs.custodia.service;

import com.google.gson.JsonObject;

/**
 * Marker interface for all migrated JavaCall service stubs.
 * Each implementing class represents one legacy JavaCall class.
 *
 * Implementations auto-register in InternalJavaCallController via Spring's
 * List&lt;JavaCallService&gt; injection.
 */
public interface JavaCallService {

    /**
     * Returns the fully-qualified legacy class name that this service replaces.
     * E.g., "cl.bcs.custodia.ConsultarCustodiaPortafolio"
     */
    String getFqn();

    /**
     * Executes the service logic and returns a JSON result.
     *
     * @param params merged jin + jout parameters (for JavaCallExtra: both merged flat)
     * @return result JsonObject
     */
    JsonObject execute(JsonObject params);
}
