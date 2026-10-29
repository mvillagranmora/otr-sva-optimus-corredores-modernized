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
public class GenerarInterfazC40InformacionContableService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazC40InformacionContableService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_2;
    private final String SQL_LOG;

    public GenerarInterfazC40InformacionContableService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08InformacionContable_SelectInformacionCorredor.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40InformacionContable_SelectInformacionContable.sql");
        this.SQL_LOG = "";
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazC40InformacionContableService.execute called");
        MapSqlParameterSource p = buildParams(params);
        List<Map<String, Object>> rows1 = jdbc.queryForList(SQL_QUERY, p);
        List<Map<String, Object>> rows2 = SQL_2.isBlank() ? java.util.Collections.emptyList() : jdbc.queryForList(SQL_2, p);
        // TODO: legacy wrote fixed-width files — file writing skipped in modernized service
        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_Comentario", "Rows1: " + rows1.size() + " Rows2: " + rows2.size());
        return out;
    }

    private MapSqlParameterSource buildParams(JsonObject params) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        params.entrySet().forEach(e -> {
            try { p.addValue(e.getKey(), e.getValue().getAsString()); } catch (Exception ex) { p.addValue(e.getKey(), null); }
        });
        return p;
    }

    private static String loadSql(String path) {
        try (var is = GenerarInterfazC40InformacionContableService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
