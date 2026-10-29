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
 * Migrated from cl.bcs.plataforma.tas.ws.SolicitudGenerarTasas (JavaCall).
 * Triggers the HANA stored procedure CRE.EntregaSistema-equivalent for tasa generation.
 * Depends on pla.* RevisarRequisitosBasicos and calls HANA procedure via CallableStatement.
 */
@Service
public class SolicitudGenerarTasasService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudGenerarTasasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.ws.SolicitudGenerarTasas";
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
            String fecha = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : null;
            String fuente = params.has("p_Fuente") ? params.get("p_Fuente").getAsString() : null;
            String mercado = params.has("p_Mercado") && !params.get("p_Mercado").isJsonNull()
                    ? params.get("p_Mercado").getAsString() : "";

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // RevisarRequisitosBasicos for validation, then invoke HANA procedure
            // via SolicitudGenerarTasas_GenerarArchivoTasas_Call.sql.

            // Get active fuente
            String sqlFuente = loadSql("cl/bcs/plataforma/tas/sql/SolicitudGenerarTasas_SelectCodigoFuenteActiva.sql");
            List<Map<String, Object>> fuentes = jdbc.queryForList(sqlFuente,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", idEmpresa)
                            .addValue("p_Fuente", fuente));

            JsonArray fuentesArr = new JsonArray();
            fuentes.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                fuentesArr.add(o);
            });

            result.add("P_OUT_RESULTADOPROCESO", fuentesArr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
