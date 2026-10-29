package cl.bcs.plataforma.service.gta;

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
 * Migrated from cl.bcs.plataforma.gta.CrearExigibilidad (JavaCall).
 * Creates or updates guarantee requirement records (GTA.ExigibilidadGarantia).
 */
@Service
public class CrearExigibilidadService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CrearExigibilidadService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.gta.CrearExigibilidad";
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
            Long idPortafolio = params.has("p_IdPortafolio") && !params.get("p_IdPortafolio").isJsonNull()
                    ? params.get("p_IdPortafolio").getAsLong() : null;

            // Check if exigibilidad exists
            String sqlSelect = loadSql(
                    "cl/bcs/plataforma/gta/sql/CrearExigibilidad_SelectExigibilidadGarantia.sql");
            List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", idEmpresa)
                            .addValue("p_IdPortafolio", idPortafolio));

            if (existing.isEmpty()) {
                // Insert
                String sqlInsert = loadSql(
                        "cl/bcs/plataforma/gta/sql/CrearExigibilidad_InsertExigibilidadGarantia.sql");
                jdbc.update(sqlInsert, new MapSqlParameterSource()
                        .addValue("p_IdEmpresa", idEmpresa)
                        .addValue("p_IdPortafolio", idPortafolio));
            } else {
                // Update
                String sqlUpdate = loadSql(
                        "cl/bcs/plataforma/gta/sql/CrearExigibilidad_UpdateExigibilidadGarantia.sql");
                jdbc.update(sqlUpdate, new MapSqlParameterSource()
                        .addValue("p_IdEmpresa", idEmpresa)
                        .addValue("p_IdPortafolio", idPortafolio));
            }

            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
