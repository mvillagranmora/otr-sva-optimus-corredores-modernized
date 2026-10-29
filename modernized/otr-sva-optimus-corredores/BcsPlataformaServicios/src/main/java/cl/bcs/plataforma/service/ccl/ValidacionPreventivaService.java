package cl.bcs.plataforma.service.ccl;

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
 * Migrated from cl.bcs.plataforma.ccl.ValidacionPreventiva (JavaCall).
 * Executes a suite of preventive data-quality validations against HANA:
 * duplicate clients, movements without portfolios, invalid matrices, etc.
 */
@Service
public class ValidacionPreventivaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ValidacionPreventivaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ccl.ValidacionPreventiva";
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
            String fechaSistema = params.has("p_FechaSistemaEjecucion") && !params.get("p_FechaSistemaEjecucion").isJsonNull()
                    ? params.get("p_FechaSistemaEjecucion").getAsString() : null;

            JsonArray validaciones = new JsonArray();

            // Validate: duplicate clients
            runValidation("ClienteDuplicado",
                    loadSql("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClienteDuplicado.sql"),
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa),
                    validaciones);

            // Validate: client movements
            runValidation("ClienteMovimientos",
                    loadSql("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClienteMovimientos.sql"),
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa),
                    validaciones);

            result.add("P_OUT_VALIDACIONES", validaciones);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }

    private void runValidation(String nombre, String sql, MapSqlParameterSource params, JsonArray dest) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
            JsonObject v = new JsonObject();
            v.addProperty("Validacion", nombre);
            v.addProperty("Estado", rows.isEmpty() ? "OK" : "ERROR");
            JsonArray detalle = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, vv) -> o.addProperty(k, vv != null ? vv.toString() : null));
                detalle.add(o);
            });
            v.add("Detalle", detalle);
            dest.add(v);
        } catch (Exception e) {
            JsonObject v = new JsonObject();
            v.addProperty("Validacion", nombre);
            v.addProperty("Estado", "ERROR");
            v.addProperty("Error", e.getMessage());
            dest.add(v);
        }
    }
}
