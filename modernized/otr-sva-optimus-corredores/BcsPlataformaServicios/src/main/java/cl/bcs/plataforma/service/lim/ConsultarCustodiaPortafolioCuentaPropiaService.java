package cl.bcs.plataforma.service.lim;

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
public class ConsultarCustodiaPortafolioCuentaPropiaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarCustodiaPortafolioCuentaPropiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ConsultarCustodiaPortafolioCuentaPropia"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlCartera = loadSql("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectCartera.sql");
            String sqlGarantia = loadSql("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectGarantia.sql");
            String sqlTransito = loadSql("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectTransito.sql");
            String sqlSinFacturar = loadSql("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectAsignacionSinFacturar.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull() ? params.get("p_Fecha").getAsString() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa).addValue("p_Fecha", fecha);

            List<Map<String, Object>> cartera = jdbc.queryForList(sqlCartera, ps);
            List<Map<String, Object>> garantia = jdbc.queryForList(sqlGarantia, ps);
            List<Map<String, Object>> transito = jdbc.queryForList(sqlTransito, ps);
            List<Map<String, Object>> sinFacturar = jdbc.queryForList(sqlSinFacturar, ps);

            result.add("P_OUT_CARTERA", toJsonArray(cartera));
            result.add("P_OUT_GARANTIA", toJsonArray(garantia));
            result.add("P_OUT_TRANSITO", toJsonArray(transito));
            result.add("P_OUT_ASIGNACION_SIN_FACTURAR", toJsonArray(sinFacturar));
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
