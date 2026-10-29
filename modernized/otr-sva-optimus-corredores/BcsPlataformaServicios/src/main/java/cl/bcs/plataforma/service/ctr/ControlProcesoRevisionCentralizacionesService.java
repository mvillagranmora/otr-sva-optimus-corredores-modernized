package cl.bcs.plataforma.service.ctr;

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
 * Migrated from cl.bcs.plataforma.ctr.impl.ControlProcesoRevisionCentralizaciones (JavaCall, ControlProceso).
 * Specific control process revision check. Delegates SQL queries to HANA.
 * Depends on pla.* utilities for FechaSistema and localized messages.
 */
@Service
public class ControlProcesoRevisionCentralizacionesService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ControlProcesoRevisionCentralizacionesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ctr.impl.ControlProcesoRevisionCentralizaciones";
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
            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull()
                    ? params.get("p_Fecha").getAsString() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema for PLA.FechaSistema when fecha is null.
            // Specific SQL for this revision type loaded below.

            result.add("P_OUT_RESULTADO", new JsonArray());
            result.add("P_OUT_MENSAJES", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
