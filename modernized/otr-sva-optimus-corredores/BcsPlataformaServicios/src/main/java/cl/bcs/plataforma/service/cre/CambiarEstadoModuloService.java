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
 * Migrated from cl.bcs.plataforma.cre.CambiarEstadoModulo (JavaCall).
 * Changes the TipoEstado (open/closed) of a module for a given company.
 */
@Service
public class CambiarEstadoModuloService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CambiarEstadoModuloService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.cre.CambiarEstadoModulo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("IdEmpresa") ? params.get("IdEmpresa").getAsLong() : null;
            String idModulo = params.has("IdModulo") ? params.get("IdModulo").getAsString() : null;
            String tipoEstado = params.has("TipoEstado") ? params.get("TipoEstado").getAsString() : null;

            // Select current module state
            final String SQL_SELECT = "select \"IdCREModuloEmpresa\",\"IdEmpresa\",\"IdModulo\","
                    + "\"NombreModuloEmpresa\",\"TipoApertura\",\"HoraApertura\",\"TipoCierre\","
                    + "\"HoraCierre\",\"IdEstado\",\"FechaProceso\",\"TipoEstado\""
                    + " from \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\""
                    + " where \"IdModulo\"=:IdModulo and \"IdEmpresa\"=:IdEmpresa";

            List<Map<String, Object>> modulos = jdbc.queryForList(SQL_SELECT,
                    new MapSqlParameterSource()
                            .addValue("IdModulo", idModulo)
                            .addValue("IdEmpresa", idEmpresa));

            if (modulos.isEmpty()) {
                result.addProperty("ErroresContador", 1);
                result.addProperty("Mensaje", "Modulo no encontrado");
                return result;
            }

            String tipoEstadoAnterior = modulos.get(0).get("TipoEstado") != null
                    ? modulos.get(0).get("TipoEstado").toString() : null;

            // Update module state
            final String SQL_UPDATE = "update \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\""
                    + " set \"TipoEstado\"=:TipoEstado"
                    + " where \"IdModulo\"=:IdModulo and \"IdEmpresa\"=:IdEmpresa";
            jdbc.update(SQL_UPDATE, new MapSqlParameterSource()
                    .addValue("TipoEstado", tipoEstado)
                    .addValue("IdModulo", idModulo)
                    .addValue("IdEmpresa", idEmpresa));

            result.addProperty("ErroresContador", 0);
            result.addProperty("TipoEstado", tipoEstado);
            result.addProperty("TipoEstadoAnterior", tipoEstadoAnterior);
            result.addProperty("Mensaje", "OK");
        } catch (Exception e) {
            result.addProperty("ErroresContador", 1);
            result.addProperty("Mensaje", e.getMessage());
        }
        return result;
    }
}
