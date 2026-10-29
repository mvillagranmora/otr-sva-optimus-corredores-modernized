package cl.bcs.normativa.svc.service.circulares;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class GenerarInformeMensualInversionistasRV13419Service {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeMensualInversionistasRV13419Service.class);
    private final NamedParameterJdbcTemplate jdbc;
    // no SQL files referenced

    public GenerarInformeMensualInversionistasRV13419Service(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // no SQL files
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInformeMensualInversionistasRV13419Service.execute called");
        // TODO: No SQL fields configured — review legacy cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.addProperty("P_OUT_MENSAJE", "Implemented — no SQL configured");
        return out;
    }

    private static String loadSql(String path) {
        try (var is = GenerarInformeMensualInversionistasRV13419Service.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
