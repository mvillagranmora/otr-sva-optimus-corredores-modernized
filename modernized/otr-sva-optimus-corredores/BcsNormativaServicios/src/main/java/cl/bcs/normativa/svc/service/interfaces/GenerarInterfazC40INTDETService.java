package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GenerarInterfazC40INTDETService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazC40INTDETService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_LOG;

    public GenerarInterfazC40INTDETService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = "";
        this.SQL_LOG = "";
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazC40INTDETService.execute called");
        // TODO: GenerarInterfazC40INTDET — no SQL files identified in stub; review legacy implementation
        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_Comentario", "Pending: no SQL files configured");
        return out;
    }

    private static String loadSql(String path) {
        try (var is = GenerarInterfazC40INTDETService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
