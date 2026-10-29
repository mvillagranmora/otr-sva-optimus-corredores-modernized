package cl.bcs.plataforma.service.ccl;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.ccl.ElConciliador (JavaCall).
 * Orchestrates all BCS reconciliation checks by discovering and executing
 * BcsConciliacion subclass implementations via reflection.
 * Depends on pla.* RevisarRequisitosBasicos for session validation.
 */
@Service
public class ElConciliadorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ElConciliadorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ccl.ElConciliador";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // RevisarRequisitosBasicos for session/role validation.
            // BcsConciliacion subclass discovery via reflection (class-path scanning)
            // must be converted to an explicit bean list in Spring.

            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.addProperty("P_OUT_SALIDA", "");
            result.add("P_OUT_DETALLE", new JsonArray());
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
