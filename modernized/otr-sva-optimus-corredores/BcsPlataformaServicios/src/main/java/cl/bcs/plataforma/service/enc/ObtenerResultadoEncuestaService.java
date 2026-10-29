package cl.bcs.plataforma.service.enc;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ObtenerResultadoEncuestaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ObtenerResultadoEncuestaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.ObtenerResultadoEncuesta"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlEncuesta = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectEncuesta.sql");
            String sqlPreguntas = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPreguntas.sql");
            String sqlRespuestas = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectRespuestas.sql");
            String sqlPuntaje = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectDefinicionPuntaje.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa);

            List<Map<String, Object>> encuesta = jdbc.queryForList(sqlEncuesta, ps);
            List<Map<String, Object>> preguntas = jdbc.queryForList(sqlPreguntas, ps);
            List<Map<String, Object>> respuestas = jdbc.queryForList(sqlRespuestas, ps);
            List<Map<String, Object>> puntajes = jdbc.queryForList(sqlPuntaje, ps);

            result.add("P_OUT_ENCUESTA", toJsonArray(encuesta));
            result.add("P_OUT_PREGUNTAS", toJsonArray(preguntas));
            result.add("P_OUT_RESPUESTAS", toJsonArray(respuestas));
            result.add("P_OUT_PUNTAJES", toJsonArray(puntajes));
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
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
