package cl.bcs.plataforma.service.frm;

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
 * Migrated from cl.bcs.plataforma.frm.ConsultarCampoFormulario (JavaCall).
 * Returns field configuration, allowed values, and domain data for form rendering.
 */
@Service
public class ConsultarCampoFormularioService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarCampoFormularioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.frm.ConsultarCampoFormulario";
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
            String codigoCampo = params.has("p_CodigoCampo") && !params.get("p_CodigoCampo").isJsonNull()
                    ? params.get("p_CodigoCampo").getAsString() : null;
            String idFormulario = params.has("p_IdFormulario") && !params.get("p_IdFormulario").isJsonNull()
                    ? params.get("p_IdFormulario").getAsString() : null;
            String idCanal = params.has("p_IdCanal") && !params.get("p_IdCanal").isJsonNull()
                    ? params.get("p_IdCanal").getAsString() : null;

            String sql = loadSql("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select.sql");
            List<Map<String, Object>> rows = jdbc.queryForList(sql,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", idEmpresa)
                            .addValue("p_CodigoCampo", codigoCampo)
                            .addValue("p_IdFormulario", idFormulario)
                            .addValue("p_IdCanal", idCanal));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            result.add("P_OUT_RESULTADO", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
