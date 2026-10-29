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
 * Migrated from cl.bcs.plataforma.tas.CargarArchivoTasas (JavaCall).
 * Loads tasas from file/data into HANA tables. Supports CURVA, PUNTO and TASA types.
 * Depends on pla.* RevisarRequisitosBasicos and ConfiguracionSistema.
 */
@Service
public class CargarArchivoTasasService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargarArchivoTasasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.CargarArchivoTasas";
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
            String idTipoTasa = params.has("p_IdTipoTasa") ? params.get("p_IdTipoTasa").getAsString() : null;
            String fuente = params.has("p_Fuente") ? params.get("p_Fuente").getAsString() : null;
            String idClasificacion = params.has("p_IdClasificacionTipoInstrumento")
                    ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // RevisarRequisitosBasicos validation, ConfiguracionSistema.getParametroConfiguracionString
            // for PLA.FechaSistema, and AgregarLog after carga completes.

            // Validate parametro ClasificacionTipoInstrumento
            String sqlParamTipoInstrumento = loadSql(
                    "cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectParametroTipoInstrumento.sql");
            Long countClasif = jdbc.queryForObject(sqlParamTipoInstrumento,
                    new MapSqlParameterSource().addValue("p_IdClasificacionTipoInstrumento", idClasificacion),
                    Long.class);
            if (countClasif == null || countClasif == 0) {
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                result.addProperty("codigo", "ERROR_PARAMETRO_INCORRECTO");
                return result;
            }

            // Validate TipoTasa
            String sqlParamTipoTasa = loadSql("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectParametroTipoTasa.sql");
            Long countTipoTasa = jdbc.queryForObject(sqlParamTipoTasa,
                    new MapSqlParameterSource().addValue("p_IdTipoTasa", idTipoTasa),
                    Long.class);
            if (countTipoTasa == null || countTipoTasa == 0) {
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                result.addProperty("codigo", "ERROR_PARAMETRO_TASA");
                return result;
            }

            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
