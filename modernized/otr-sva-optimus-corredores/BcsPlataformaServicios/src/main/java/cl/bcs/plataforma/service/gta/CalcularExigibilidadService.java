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
 * Migrated from cl.bcs.plataforma.gta.CalcularExigibilidad (JavaCall).
 * Calculates guarantee requirement (exigibilidad) for portfolios and orders.
 * Depends on pla.* ConfiguracionSistema, mer.CalcularParidad for multi-currency,
 * and cl.bcs.ordenes.CalcularCoberturaOrdenes.
 */
@Service
public class CalcularExigibilidadService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CalcularExigibilidadService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.gta.CalcularExigibilidad";
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
            Long idInstrumento = params.has("p_IdInstrumento") && !params.get("p_IdInstrumento").isJsonNull()
                    ? params.get("p_IdInstrumento").getAsLong() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema for PLA.FechaSistema, CalcularParidad for currency conversion,
            // and cl.bcs.ordenes.CalcularCoberturaOrdenes for order coverage.

            // Query exigibilidades
            String sql = loadSql("cl/bcs/plataforma/gta/sql/CalcularExigibilidad_SelectExigibilidad.sql");
            List<Map<String, Object>> rows = jdbc.queryForList(sql,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", idEmpresa)
                            .addValue("p_IdPortafolio", idPortafolio)
                            .addValue("p_IdInstrumento", idInstrumento));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            result.add("P_OUT_RESULTADO", arr);
            result.add("P_OUT_MENSAJES", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
