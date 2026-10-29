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
public class ModificarDeterioroEmisorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ModificarDeterioroEmisorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.ModificarDeterioroEmisor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/mer/sql/ModificarDeterioroEmisor_SelectDeterioroEmisor.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/mer/sql/ModificarDeterioroEmisor_UpdateDeterioroEmisor.sql");

            Long idDeterioro = params.has("p_IdDeterioroEmisor") && !params.get("p_IdDeterioroEmisor").isJsonNull()
                    ? params.get("p_IdDeterioroEmisor").getAsLong() : null;

            List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("p_IdDeterioroEmisor", idDeterioro));

            if (existing.isEmpty()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "DETERIORO_NO_EXISTE");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                return result;
            }

            jdbc.update(sqlUpdate, new MapSqlParameterSource()
                    .addValue("p_IdDeterioroEmisor", idDeterioro)
                    .addValue("p_ValorDeterioroEmisor", getS(params, "p_ValorDeterioroEmisor"))
                    .addValue("p_Observacion", getS(params, "p_Observacion"))
                    .addValue("p_IdEstado", getS(params, "p_IdEstado")));

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "DETERIORO_MODIFICADO");
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
        }
        return result;
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
