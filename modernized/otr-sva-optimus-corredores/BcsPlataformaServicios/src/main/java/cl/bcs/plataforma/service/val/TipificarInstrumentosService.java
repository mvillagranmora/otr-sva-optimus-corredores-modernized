package cl.bcs.plataforma.service.val;

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
 * Migrated from cl.bcs.plataforma.val.TipificarInstrumentos (JavaCall).
 * Assigns valuation type to instruments via external BCS WS then updates the DB.
 * Depends on pla.* ConfiguracionSistema and TipificarWs (external WS).
 */
@Service
public class TipificarInstrumentosService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public TipificarInstrumentosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.TipificarInstrumentos";
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
            Long idEmpresa = params.has("idEmpresa") ? params.get("idEmpresa").getAsLong() : null;
            String fechaTipificar = params.has("fechaTipificar") && !params.get("fechaTipificar").isJsonNull()
                    ? params.get("fechaTipificar").getAsString() : null;
            Long cantidadTipificar = params.has("cantidadTipificar") ? params.get("cantidadTipificar").getAsLong() : 100L;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString for PLA.FechaSistema
            // TipificarWs (external BCS WS) for instrument type assignment

            // Query: select top N nemotecnicos from instruments without TipoValorizacion
            String sql = "select top " + cantidadTipificar.intValue()
                    + " i.\"Nemotecnico\""
                    + " from \"BSCL.Plataforma.BD::INS.Instrumento\" i"
                    + " join \"BSCL.Plataforma.BD::INS.TipoInstrumento\" ti"
                    + " on (i.\"IdTipoInstrumento\" = ti.\"IdTipoInstrumento\")"
                    + " where ti.\"IdClasificacionTipoInstrumento\" in ('RF','IF')"
                    + " and i.\"TipoValorizacion\" is null or i.\"TipoValorizacion\" = ''"
                    + " group by i.\"Nemotecnico\"";

            List<Map<String, Object>> rows = jdbc.queryForList(sql, new MapSqlParameterSource());

            JsonArray instrumentos = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                instrumentos.add(o);
            });

            result.add("instrumentos", instrumentos);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
