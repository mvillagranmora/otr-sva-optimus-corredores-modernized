package cl.bcs.plataforma.service.demo;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Migrated from cl.bcs.plataforma.demo.PingSession (JavaCall).
 * Executes a dummy query to verify DB connectivity and optionally inserts a log entry.
 * Depends on pla.* AgregarLog and ConfiguracionSistema for FechaSistema.
 */
@Service
public class PingSessionService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public PingSessionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.demo.PingSession";
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long delayJava = params.has("p_DelayJava") && !params.get("p_DelayJava").isJsonNull()
                    ? params.get("p_DelayJava").getAsLong() : 0L;

            if (delayJava > 0) {
                Thread.sleep(delayJava);
            }

            // Execute dummy query to verify connectivity
            String sql = loadSql("cl/bcs/plataforma/demo/sql/PingSession_select_dummy.sql");
            List<Map<String, Object>> rows = jdbc.queryForList(sql, new MapSqlParameterSource());

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // AgregarLog insertion when p_InsertaLog = "S".

            result.add("P_OUT_RESULTADO", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
