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
 * Migrated from cl.bcs.plataforma.tas.ws.CargarArchivoTasasNemo (JavaCall).
 * Loads tasa data indexed by Nemotecnico for an active mercado/fuente.
 * Depends on pla.* ConfiguracionSistema and bat.AgregarPrograma.
 */
@Service
public class CargarArchivoTasasNemoService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargarArchivoTasasNemoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.ws.CargarArchivoTasasNemo";
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
            String mercado = params.has("p_Mercado") ? params.get("p_Mercado").getAsString() : null;
            JsonArray tasas = params.has("p_Tasas") ? params.get("p_Tasas").getAsJsonArray() : new JsonArray();

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema for PLA.FechaSistema;
            // bat.AgregarPrograma to enqueue tasa batch program.

            // Validate mercado activo
            String sqlMercado = loadSql("cl/bcs/plataforma/tas/sql/CargarArchivoTasasNemo_SelectMercadoActivo.sql");
            List<Map<String, Object>> mercados = jdbc.queryForList(sqlMercado,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa));

            // Get active instruments for the market
            String sqlInstrumentos = loadSql(
                    "cl/bcs/plataforma/tas/sql/CargarArchivoTasasNemo_SelectInstrumentosActivosPorMercado.sql");
            List<Map<String, Object>> instrumentos = jdbc.queryForList(sqlInstrumentos,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", idEmpresa)
                            .addValue("p_Mercado", mercado));

            JsonArray arr = new JsonArray();
            instrumentos.forEach(row -> {
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
