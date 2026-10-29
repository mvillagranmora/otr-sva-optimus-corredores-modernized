package cl.bcs.plataforma.service.ins;

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
public class AsignarClasificacionIndiceService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AsignarClasificacionIndiceService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.AsignarClasificacionIndice"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String p_Indice = params.has("Indice") && !params.get("Indice").isJsonNull()
                    ? params.get("Indice").getAsString() : null;
            long p_Padre = params.has("Padre") && !params.get("Padre").isJsonNull()
                    ? params.get("Padre").getAsLong() : 0L;

            String sql = loadSql("cl/bcs/plataforma/ins/sql/AsignarClasificacionIndice_UpdateIndices.sql");
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("p_Indice", p_Indice)
                    .addValue("p_Padre", p_Padre));

            JsonArray mensajes = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "UI_OPERACION_INGRESO_EXITO");
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            JsonArray mensajes = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Codigo", "UI_OPERACION_TIENE_ERRORES");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
