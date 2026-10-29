package cl.bcs.fondomutuo.adapter.out.hana;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HANA adapter — executes stored procedures for FondoMutuo operations.
 *
 * Uses SimpleJdbcCall with HANA schema qualification (schema."procedure").
 * Results include all OUT parameters returned by the SP.
 */
@Component
public class HanaFondoMutuoAdapter implements HanaFondoMutuoPort {

    private static final Logger log = LoggerFactory.getLogger(HanaFondoMutuoAdapter.class);

    private final JdbcTemplate jdbc;
    private final ConcurrentHashMap<String, SimpleJdbcCall> callCache = new ConcurrentHashMap<>();

    public HanaFondoMutuoAdapter(NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = namedJdbc.getJdbcTemplate();
    }

    @Override
    public Map<String, Object> callStoredProcedure(String schema, String procedure, Map<String, Object> inParams) {
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(procedure, "procedure");
        Objects.requireNonNull(inParams, "inParams");

        String cacheKey = schema + "." + procedure;
        SimpleJdbcCall call = callCache.computeIfAbsent(cacheKey, k ->
                new SimpleJdbcCall(jdbc)
                        .withSchemaName(schema)
                        .withProcedureName(procedure)
                        .withoutProcedureColumnMetaDataAccess());

        try {
            Map<String, Object> result = call.execute(inParams);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            log.error("callStoredProcedure failed: {}.{} — {}", schema, procedure, e.getMessage(), e);
            throw e;
        }
    }
}
