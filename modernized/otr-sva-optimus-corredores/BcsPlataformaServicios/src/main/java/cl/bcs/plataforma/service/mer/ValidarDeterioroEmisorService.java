package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ValidarDeterioroEmisorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ValidarDeterioroEmisorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.ValidarDeterioroEmisor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlEmisoresValidos = loadSql("cl/bcs/plataforma/mer/sql/ValidarDeterioroEmisor_SelectEmisoresValidos.sql");
            String sqlDeterioroEmisor = loadSql("cl/bcs/plataforma/mer/sql/ValidarDeterioroEmisor_SelectDeterioroEmisor.sql");

            JsonArray emisores = params.has("p_Emisores") && params.get("p_Emisores").isJsonArray()
                    ? params.getAsJsonArray("p_Emisores") : new JsonArray();

            List<String> idsEmisores = new ArrayList<>();
            for (JsonElement el : emisores) {
                JsonObject e = el.getAsJsonObject();
                if (e.has("IdEmisor") && !e.get("IdEmisor").isJsonNull())
                    idsEmisores.add(e.get("IdEmisor").getAsString());
            }

            if (idsEmisores.isEmpty()) idsEmisores.add("");

            List<Map<String, Object>> validos = jdbc.queryForList(sqlEmisoresValidos,
                    new MapSqlParameterSource().addValue("IdEmisor", idsEmisores));

            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull()
                    ? params.get("p_Fecha").getAsString() : null;

            List<Map<String, Object>> deterioros = jdbc.queryForList(sqlDeterioroEmisor,
                    new MapSqlParameterSource()
                            .addValue("IdEmisor", idsEmisores)
                            .addValue("Fecha", fecha));

            JsonArray arrValidos = new JsonArray();
            validos.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrValidos.add(o); });
            JsonArray arrDeteriorados = new JsonArray();
            deterioros.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrDeteriorados.add(o); });

            result.add("P_OUT_EMISORES_VALIDOS", arrValidos);
            result.add("P_OUT_DETERIOROS", arrDeteriorados);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
