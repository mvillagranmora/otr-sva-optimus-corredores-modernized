package cl.bcs.fondomutuo.port.out;

import java.util.Map;

/**
 * Outbound port: executes HANA stored procedures for FondoMutuo operations.
 * All legacy services that map to a single SP call go through this port.
 */
public interface HanaFondoMutuoPort {
    Map<String, Object> callStoredProcedure(String schema, String procedure, Map<String, Object> inParams);
}
