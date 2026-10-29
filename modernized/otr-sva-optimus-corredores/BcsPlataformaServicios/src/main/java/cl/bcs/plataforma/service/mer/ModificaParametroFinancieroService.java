package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ModificaParametroFinancieroService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ModificaParametroFinancieroService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.ModificaParametroFinanciero"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlUpdate = loadSql("cl/bcs/plataforma/mer/sql/ModificaParametroFinanciero_UpdateParametroFinanciero.sql");

            JsonArray registros = params.has("p_Registros") && params.get("p_Registros").isJsonArray()
                    ? params.getAsJsonArray("p_Registros") : new JsonArray();

            for (JsonElement el : registros) {
                JsonObject reg = el.getAsJsonObject();
                String idParametro = reg.has("IdParametroFinanciero") && !reg.get("IdParametroFinanciero").isJsonNull()
                        ? reg.get("IdParametroFinanciero").getAsString() : null;
                String fecha = reg.has("Fecha") && !reg.get("Fecha").isJsonNull()
                        ? reg.get("Fecha").getAsString() : null;
                BigDecimal valor = reg.has("Valor") && !reg.get("Valor").isJsonNull()
                        ? reg.get("Valor").getAsBigDecimal() : BigDecimal.ZERO;

                jdbc.update(sqlUpdate, new MapSqlParameterSource()
                        .addValue("IdParametroFinanciero", idParametro)
                        .addValue("Fecha", fecha)
                        .addValue("Valor", valor));
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "PARAMETRO_MODIFICADO");
            mensajes.add(msg);
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
