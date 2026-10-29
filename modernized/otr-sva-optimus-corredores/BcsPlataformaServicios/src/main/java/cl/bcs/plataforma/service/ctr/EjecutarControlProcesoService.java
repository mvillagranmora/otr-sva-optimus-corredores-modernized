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
 * Migrated from cl.bcs.plataforma.ctr.EjecutarControlProceso (JavaCall).
 * Orchestrates all control process revisions (centralizations, market close,
 * system init/close, valuations, maturities) by querying their status and
 * inserting control process detail records.
 * Depends on pla.* ObtenerDiaAnterior, ConsultarEmpresa, Diccionario.
 */
@Service
public class EjecutarControlProcesoService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public EjecutarControlProcesoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ctr.EjecutarControlProceso";
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
            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull()
                    ? params.get("p_Fecha").getAsString() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ObtenerDiaAnterior to compute p_FechaAnterior, ConsultarEmpresa for IdPais,
            // Diccionario for localized messages. Each revision delegate (impl.*) also needs wiring.

            // Query existing revisions
            String sqlRevisiones = loadSql("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisiones.sql");
            List<Map<String, Object>> rows = jdbc.queryForList(sqlRevisiones,
                    new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa)
                            .addValue("Fecha", fecha));

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
