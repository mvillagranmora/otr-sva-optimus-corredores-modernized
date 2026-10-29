package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConsultarLimitesCuentaPropiaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarLimitesCuentaPropiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ConsultarLimitesCuentaPropia"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlCreate = loadSql("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_CreateLocalTempTableLimiteCuentaPropia.sql");
            String sqlDrop = loadSql("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_DropLocalTempTableLimiteCuentaPropia.sql");
            String sqlInsertTemp = loadSql("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_InsertTempLimiteCuentaPropia.sql");
            String sqlSelect = loadSql("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_SelectLimiteCuentaPropia.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // Local temporary tables require connection-level control; adapt when transaction management is available
            jdbc.getJdbcTemplate().execute(sqlCreate);

            if (params.has("Clasificaciones") && params.get("Clasificaciones").isJsonArray()) {
                for (var el : params.getAsJsonArray("Clasificaciones")) {
                    JsonObject cls = el.getAsJsonObject();
                    MapSqlParameterSource ps = new MapSqlParameterSource()
                            .addValue("IdInterno", cls.has("IdInterno") && !cls.get("IdInterno").isJsonNull() ? cls.get("IdInterno").getAsLong() : null)
                            .addValue("IdInstrumento", cls.has("IdInstrumento") && !cls.get("IdInstrumento").isJsonNull() ? cls.get("IdInstrumento").getAsLong() : null)
                            .addValue("IdClasificacionTipoInstrumento", cls.has("IdClasificacionTipoInstrumento") && !cls.get("IdClasificacionTipoInstrumento").isJsonNull() ? cls.get("IdClasificacionTipoInstrumento").getAsString() : null)
                            .addValue("IdTipoInstrumento", cls.has("IdTipoInstrumento") && !cls.get("IdTipoInstrumento").isJsonNull() ? cls.get("IdTipoInstrumento").getAsString() : null)
                            .addValue("IdTipoLimite", cls.has("IdTipoLimite") && !cls.get("IdTipoLimite").isJsonNull() ? cls.get("IdTipoLimite").getAsString() : null)
                            .addValue("IdTipoOperacion", cls.has("IdTipoOperacion") && !cls.get("IdTipoOperacion").isJsonNull() ? cls.get("IdTipoOperacion").getAsString() : null)
                            .addValue("IdTipoCanalIngreso", cls.has("IdTipoCanalIngreso") && !cls.get("IdTipoCanalIngreso").isJsonNull() ? cls.get("IdTipoCanalIngreso").getAsString() : null)
                            .addValue("CodigoOperador", cls.has("CodigoOperador") && !cls.get("CodigoOperador").isJsonNull() ? cls.get("CodigoOperador").getAsString() : null)
                            .addValue("IdMonedaOperacion", cls.has("IdMonedaOperacion") && !cls.get("IdMonedaOperacion").isJsonNull() ? cls.get("IdMonedaOperacion").getAsString() : null)
                            .addValue("IdEmisor", cls.has("IdEmisor") && !cls.get("IdEmisor").isJsonNull() ? cls.get("IdEmisor").getAsString() : null);
                    jdbc.update(sqlInsertTemp, ps);
                }
            }

            List<Map<String, Object>> limites = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            jdbc.getJdbcTemplate().execute(sqlDrop);

            result.add("LimitesPorIdInterno", toJsonArray(limites));
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private JsonArray toJsonArray(List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        rows.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arr.add(o); });
        return arr;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
