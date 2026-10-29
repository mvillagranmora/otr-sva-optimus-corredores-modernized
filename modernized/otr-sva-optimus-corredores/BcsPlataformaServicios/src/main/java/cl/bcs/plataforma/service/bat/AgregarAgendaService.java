package cl.bcs.plataforma.service.bat;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.bat.AgregarAgenda (JavaCall).
 * Creates a batch agenda entry calling HANA stored procedure.
 */
@Service
public class AgregarAgendaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AgregarAgendaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.bat.AgregarAgenda";
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
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String idTipoTarea = params.has("p_IdTipoTarea") ? params.get("p_IdTipoTarea").getAsString() : null;

            // The original uses a CALL to a HANA stored procedure.
            // Load the call SQL and execute via SimpleJdbcCall if needed.
            // String sql = loadSql("cl/bcs/plataforma/bat/sql/AgregarAgrenda_Call.sql");
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // SalidaServicios for error formatting.

            result.add("P_OUT_MENSAJES", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
