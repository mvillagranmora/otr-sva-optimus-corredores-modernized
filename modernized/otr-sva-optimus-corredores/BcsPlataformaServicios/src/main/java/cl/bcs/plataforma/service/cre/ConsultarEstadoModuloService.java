package cl.bcs.plataforma.service.cre;

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
 * Migrated from cl.bcs.plataforma.cre.ConsultarEstadoModulo (JavaCall).
 * Returns the current state (IdEstadoModulo, NombreModulo) for all modules of a company.
 */
@Service
public class ConsultarEstadoModuloService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarEstadoModuloService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.cre.ConsultarEstadoModulo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

            final String SQL = "SELECT IFNULL(\"IdEstado\",'ABT') AS \"IdEstadoModulo\","
                    + " IFNULL(\"CodigoModulo\",'') as \"NombreModulo\""
                    + " FROM \"BSCL.Plataforma.BD::PAU.Modulo\""
                    + " where \"IdEmpresa\" = :IdEmpresa";

            List<Map<String, Object>> rows = jdbc.queryForList(SQL,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            result.add("p_out_ListaModulos", arr);
            result.add("p_out_ErroresDetalle", new JsonArray());
            result.add("p_out_AdvertenciasDetalle", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
