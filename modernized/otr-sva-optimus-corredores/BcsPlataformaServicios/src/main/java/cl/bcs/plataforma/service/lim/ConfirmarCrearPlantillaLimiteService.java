package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConfirmarCrearPlantillaLimiteService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConfirmarCrearPlantillaLimiteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ConfirmarCrearPlantillaLimite"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sql = loadSql("cl/bcs/plataforma/lim/sql/ConfirmarCrearPlantillaLimite_Update_Plantilla.sql");
            Long idPlantilla = params.has("p_IdPlantillaLimite") && !params.get("p_IdPlantillaLimite").isJsonNull()
                    ? params.get("p_IdPlantillaLimite").getAsLong() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull()
                    ? params.get("p_IdEstado").getAsString() : "ACT";

            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("p_IdPlantillaLimite", idPlantilla)
                    .addValue("p_IdEstado", idEstado));

            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "OK"); msg.addProperty("Codigo", "PLANTILLA_CONFIRMADA"); mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes); result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes); result.addProperty("P_OUT_ERRORESCONTADOR", 1);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
