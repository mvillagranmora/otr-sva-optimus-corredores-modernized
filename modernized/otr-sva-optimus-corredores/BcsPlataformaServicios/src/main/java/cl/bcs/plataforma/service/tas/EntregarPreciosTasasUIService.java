package cl.bcs.plataforma.service.tas;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.tas.EntregarPreciosTasasUI (JavaCall).
 * UI-facing adapter that transforms simplified input and delegates to
 * EntregarPreciosTasas. Requires ConsultarEmpresa pla.* utility.
 */
@Service
public class EntregarPreciosTasasUIService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;
    private final EntregarPreciosTasasService entregarPreciosTasasService;

    public EntregarPreciosTasasUIService(NamedParameterJdbcTemplate jdbc,
                                          EntregarPreciosTasasService entregarPreciosTasasService) {
        this.jdbc = jdbc;
        this.entregarPreciosTasasService = entregarPreciosTasasService;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.EntregarPreciosTasasUI";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConsultarEmpresa needed to resolve IdPais for the delegated call to
            // EntregarPreciosTasas with the correct country parameter.

            // Delegate to underlying service once ConsultarEmpresa is available
            return entregarPreciosTasasService.execute(params);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
