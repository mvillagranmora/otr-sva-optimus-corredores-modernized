package cl.bcs.plataforma.service.tas;

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
 * Migrated from cl.bcs.plataforma.tas.EntregarPreciosTasas (JavaCall).
 * Delivers/publishes tasas prices applying criteria selection logic.
 * Depends on pla.* ObtenerDiaAnterior, ConfiguracionSistema, Diccionario.
 */
@Service
public class EntregarPreciosTasasService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public EntregarPreciosTasasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.EntregarPreciosTasas";
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
            JsonArray datosInstrumentos = params.has("p_DatosInstrumentos")
                    ? params.get("p_DatosInstrumentos").getAsJsonArray() : new JsonArray();

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ObtenerDiaAnterior for prior-day date computation, ConfiguracionSistema
            // for PLA.FechaSistema, Diccionario for error messages.

            // Example: query criterios de seleccion
            String sqlCriterios = loadSql("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectCriterios.sql");
            List<Map<String, Object>> criterios = jdbc.queryForList(sqlCriterios,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa));

            JsonArray arr = new JsonArray();
            criterios.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

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
