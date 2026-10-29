package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConsultaIndicesCorredoraService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultaIndicesCorredoraService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.ConsultaIndicesCorredora"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlTipoCambio = loadSql("cl/bcs/plataforma/mer/sql/ConsultaIndicesCorredora_SelectIndicesCorredoraTipoCambio.sql");
            String sqlIndices = loadSql("cl/bcs/plataforma/mer/sql/ConsultaIndicesCorredora_SelectIndicesCorredoraIndices.sql");
            String sqlValores = loadSql("cl/bcs/plataforma/mer/sql/ConsultaIndicesCorredora_SelectIndicesCorredoraValores.sql");
            String sqlDiaInhabil = loadSql("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectDiaInhabil.sql");
            String sqlFeriado = loadSql("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectDiaFeriado.sql");

            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull()
                    ? params.get("p_Fecha").getAsString() : null;
            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull()
                    ? params.get("p_IdEmpresa").getAsLong() : null;

            MapSqlParameterSource paramSource = new MapSqlParameterSource()
                    .addValue("p_Fecha", fecha)
                    .addValue("p_IdEmpresa", idEmpresa);

            List<Map<String, Object>> tipoCambio = jdbc.queryForList(sqlTipoCambio, paramSource);
            List<Map<String, Object>> indices = jdbc.queryForList(sqlIndices, paramSource);
            List<Map<String, Object>> valores = jdbc.queryForList(sqlValores, paramSource);

            JsonArray arrTipoCambio = new JsonArray();
            tipoCambio.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrTipoCambio.add(o); });
            JsonArray arrIndices = new JsonArray();
            indices.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrIndices.add(o); });
            JsonArray arrValores = new JsonArray();
            valores.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrValores.add(o); });

            result.add("P_OUT_TIPO_CAMBIO", arrTipoCambio);
            result.add("P_OUT_INDICES", arrIndices);
            result.add("P_OUT_VALORES", arrValores);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            msgs.add(msg);
            result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
