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
public class ModificarNivelClasificacionActivoService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ModificarNivelClasificacionActivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ModificarNivelClasificacionActivo"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            long idArbol = params.has("p_IdArbolClasificacionActivo") ? params.get("p_IdArbolClasificacionActivo").getAsLong() : 0L;
            String descripcion = params.has("p_Descripcion") && !params.get("p_Descripcion").isJsonNull()
                    ? params.get("p_Descripcion").getAsString() : null;
            long padre = params.has("p_Padre") ? params.get("p_Padre").getAsLong() : 0L;

            String sqlConsulta = loadSql("cl/bcs/plataforma/ins/sql/ConsultaNivelesDeClasificacion_SelectNivelesDeClasificacion.sql");
            List<Map<String, Object>> existing = jdbc.queryForList(sqlConsulta, new MapSqlParameterSource()
                    .addValue("p_Descripcion", descripcion)
                    .addValue("p_IdArbolClasificacionActivo", idArbol));

            if (existing.isEmpty()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "UI_NIVEL_NO_EXISTE");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                return result;
            }

            String sqlModificar = loadSql("cl/bcs/plataforma/ins/sql/ModificarNivelClasificacionActivo_UpdateNivelClasificacion.sql");
            jdbc.update(sqlModificar, new MapSqlParameterSource()
                    .addValue("p_Descripcion", descripcion)
                    .addValue("p_Padre", padre)
                    .addValue("p_IdArbolClasificacionActivo", idArbol));

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "UI_OPERACION_INGRESO_EXITO");
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

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
