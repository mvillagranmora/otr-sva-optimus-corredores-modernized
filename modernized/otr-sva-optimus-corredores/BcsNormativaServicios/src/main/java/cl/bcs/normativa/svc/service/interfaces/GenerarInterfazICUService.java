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
public class GenerarInterfazICUService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazICUService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_LOG;

    public GenerarInterfazICUService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = "";
        this.SQL_LOG = "";
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazICUService.execute called");
        // TODO: GenerarInterfazICU — no SQL files configured in stub; review legacy implementation
        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_Comentario", "Pending: no SQL files configured");
        return out;
    }

    private static String loadSql(String path) {
        try (var is = GenerarInterfazICUService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
