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
public class ConfirmarModificarPlantillaLimiteService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConfirmarModificarPlantillaLimiteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ConfirmarModificarPlantillaLimite"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectCambios = loadSql("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimite_Select_Cambios.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimite_Update_Plantilla.sql");

            Long idPlantilla = params.has("p_IdPlantillaLimite") && !params.get("p_IdPlantillaLimite").isJsonNull()
                    ? params.get("p_IdPlantillaLimite").getAsLong() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull()
                    ? params.get("p_IdEstado").getAsString() : "ACT";

            List<Map<String, Object>> cambios = jdbc.queryForList(sqlSelectCambios,
                    new MapSqlParameterSource().addValue("p_IdPlantillaLimite", idPlantilla));

            jdbc.update(sqlUpdate, new MapSqlParameterSource()
                    .addValue("p_IdPlantillaLimite", idPlantilla)
                    .addValue("p_IdEstado", idEstado));

            JsonArray arrCambios = new JsonArray();
            cambios.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrCambios.add(o); });
            result.add("P_OUT_CAMBIOS", arrCambios);
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "OK"); msg.addProperty("Codigo", "PLANTILLA_MODIFICACION_CONFIRMADA"); mensajes.add(msg);
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
