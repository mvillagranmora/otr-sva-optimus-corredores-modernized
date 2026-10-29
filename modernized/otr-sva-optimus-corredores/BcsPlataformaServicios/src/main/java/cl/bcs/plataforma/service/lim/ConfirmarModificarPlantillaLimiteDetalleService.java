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
public class ConfirmarModificarPlantillaLimiteDetalleService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConfirmarModificarPlantillaLimiteDetalleService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ConfirmarModificarPlantillaLimiteDetalle"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectCambios = loadSql("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimiteDetalle_Select_Cambios.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimiteDetalle_Update_Detalle.sql");

            Long idDetalle = params.has("p_IdPlantillaLimiteDetalle") && !params.get("p_IdPlantillaLimiteDetalle").isJsonNull()
                    ? params.get("p_IdPlantillaLimiteDetalle").getAsLong() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull()
                    ? params.get("p_IdEstado").getAsString() : "ACT";

            List<Map<String, Object>> cambios = jdbc.queryForList(sqlSelectCambios,
                    new MapSqlParameterSource().addValue("p_IdPlantillaLimiteDetalle", idDetalle));

            jdbc.update(sqlUpdate, new MapSqlParameterSource()
                    .addValue("p_IdPlantillaLimiteDetalle", idDetalle)
                    .addValue("p_IdEstado", idEstado));

            JsonArray arrCambios = new JsonArray();
            cambios.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); arrCambios.add(o); });
            result.add("P_OUT_CAMBIOS", arrCambios);
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "OK"); msg.addProperty("Codigo", "DETALLE_MODIFICACION_CONFIRMADA"); mensajes.add(msg);
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
