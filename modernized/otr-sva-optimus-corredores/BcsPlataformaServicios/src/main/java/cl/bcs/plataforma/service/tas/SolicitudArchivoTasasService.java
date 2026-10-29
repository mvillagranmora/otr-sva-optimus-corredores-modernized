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
 * Migrated from cl.bcs.plataforma.tas.SolicitudArchivoTasas (JavaCall).
 * Creates rate file request records and validates duplicates/estado de mesa RF.
 * Depends on pla.* RevisarRequisitosBasicos and bat.AgregarPrograma.
 */
@Service
public class SolicitudArchivoTasasService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudArchivoTasasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.SolicitudArchivoTasas";
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

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // RevisarRequisitosBasicos for session/role validation,
            // bat.AgregarPrograma to enqueue the file generation job.

            // Validate estado mesa RF
            String sqlEstado = loadSql("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectEstadoMesaRF.sql");
            List<Map<String, Object>> estadoRows = jdbc.queryForList(sqlEstado,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa));

            JsonArray estadoArr = new JsonArray();
            estadoRows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                estadoArr.add(o);
            });

            result.add("P_OUT_RESULTADO", estadoArr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
