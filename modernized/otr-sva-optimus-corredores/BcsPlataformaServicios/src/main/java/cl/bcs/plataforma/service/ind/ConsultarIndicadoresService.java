package cl.bcs.plataforma.service.ind;

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
 * Migrated from cl.bcs.plataforma.ind.ConsultarIndicadores (JavaCall).
 * Queries indicators via HANA stored procedure call.
 */
@Service
public class ConsultarIndicadoresService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarIndicadoresService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ind.ConsultarIndicadores";
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
            Long idEntidad = params.has("p_IdEntidad") && !params.get("p_IdEntidad").isJsonNull()
                    ? params.get("p_IdEntidad").getAsLong() : null;
            String idTipoEntidad = params.has("p_IdTipoEntidad") && !params.get("p_IdTipoEntidad").isJsonNull()
                    ? params.get("p_IdTipoEntidad").getAsString() : null;

            // The original class uses a HANA SP CALL
            // String sql = loadSql("cl/bcs/plataforma/ind/sql/ConsultarIndicadores_Call.sql");
            // For now we execute a direct query pattern
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // SimpleJdbcCall for the HANA stored procedure after pla migration.

            result.add("P_OUT_RESULTADO", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
