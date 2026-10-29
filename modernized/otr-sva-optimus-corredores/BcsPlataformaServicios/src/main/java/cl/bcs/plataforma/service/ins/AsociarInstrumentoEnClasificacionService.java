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
public class AsociarInstrumentoEnClasificacionService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AsociarInstrumentoEnClasificacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.AsociarInstrumentoEnClasificacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            int idInstrumento = params.has("IdInstrumento") ? params.get("IdInstrumento").getAsInt() : 0;
            int idArbol = params.has("IdArbolClasificacionActivo") ? params.get("IdArbolClasificacionActivo").getAsInt() : 0;
            String idClasificacion = params.has("IdClasificacionTipoInstrumento") && !params.get("IdClasificacionTipoInstrumento").isJsonNull()
                    ? params.get("IdClasificacionTipoInstrumento").getAsString() : null;
            String idTipo = params.has("IdTipoInstrumento") && !params.get("IdTipoInstrumento").isJsonNull()
                    ? params.get("IdTipoInstrumento").getAsString() : null;

            String sqlSelect = loadSql("cl/bcs/plataforma/ins/sql/AsociarInstrumentoEnClasificacion_SelectClasificacionIstrumento.sql");
            List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdInstrumento", idInstrumento));

            JsonArray mensajes = new JsonArray();
            if (existing.isEmpty()) {
                String sqlInsert = loadSql("cl/bcs/plataforma/ins/sql/AsociarInstrumentoEnClasificacion_InsertClasificacionIstrumento.sql");
                jdbc.update(sqlInsert, new MapSqlParameterSource()
                        .addValue("IdArbolClasificacionActivo", idArbol)
                        .addValue("IdClasificacionTipoInstrumento", idClasificacion)
                        .addValue("IdTipoInstrumento", idTipo)
                        .addValue("IdInstrumento", idInstrumento));
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "OK");
                msg.addProperty("Codigo", "UI_OPERACION_INGRESO_EXITO");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            } else {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "UI_EXISTE_INSTRUMENTO");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            }
        } catch (Exception e) {
            JsonArray mensajes = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Codigo", "ERROR_DE_EJECUCION");
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
