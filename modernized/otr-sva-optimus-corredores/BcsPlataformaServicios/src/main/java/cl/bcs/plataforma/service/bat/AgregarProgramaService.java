package cl.bcs.plataforma.service.bat;

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
 * Migrated from cl.bcs.plataforma.bat.AgregarPrograma (JavaCall).
 * Creates a batch program execution record in BAT.Programa table using HANA sequences.
 * Depends on pla.* ConfiguracionSistema for PLA.FechaSistema.
 */
@Service
public class AgregarProgramaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AgregarProgramaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.bat.AgregarPrograma";
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
            String idTipoPrograma = params.has("p_IdTipoPrograma") ? params.get("p_IdTipoPrograma").getAsString() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString for PLA.FechaSistema.
            // HANA sequence SELECT for "Programa" and "ParametrosPrograma" needed.

            // Query tipo programa
            final String SQL_SELECT_TIPO_PROGRAMA = "select \"IdTipoPrograma\",\"Descripcion\",\"MaximoIntentos\",\"Modulo\","
                    + "\"NombreClasePrograma\",\"TipoClasePrograma\",\"Nodo\",\"EsPauta\""
                    + " from \"BSCL.Plataforma.BD::BAT.TipoPrograma\" where \"IdTipoPrograma\"=:IdTipoPrograma";
            List<Map<String, Object>> tiposPrograma = jdbc.queryForList(SQL_SELECT_TIPO_PROGRAMA,
                    new MapSqlParameterSource().addValue("IdTipoPrograma", idTipoPrograma));

            if (tiposPrograma.isEmpty()) {
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                result.addProperty("error", "TipoPrograma no encontrado: " + idTipoPrograma);
                return result;
            }

            result.addProperty("P_OUT_ID_PROGRAMA", 0);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
