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
public class ResponderEncuestaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ResponderEncuestaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.ResponderEncuesta"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlEncuesta = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectEncuesta.sql");
            String sqlPreguntas = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPreguntas.sql");
            String sqlRespuestas = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectRespuestas.sql");
            String sqlPuntaje = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectDefinicionPuntaje.sql");
            String sqlUpdateCategoria = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_UpdateCategoriaCliente.sql");
            String sqlSelectCliente = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectCliente.sql");
            String sqlSelectPortafolio = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPortafolio.sql");
            String sqlInsertRegistro = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroEncuesta.sql");
            String sqlInsertPreguntas = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroPreguntas.sql");
            String sqlInsertRespuestasReg = loadSql("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroRespuestas.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // FechaDB.ObtenerFechaHoraSistemaTs needed for FechaIngresoRegistro and FechaEncuesta
            // PlataformaUtils.MensajeSalida used for output messages

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idCliente = params.has("p_IdCliente") && !params.get("p_IdCliente").isJsonNull() ? params.get("p_IdCliente").getAsLong() : null;
            Long idPortafolio = params.has("p_IdPortafolio") && !params.get("p_IdPortafolio").isJsonNull() ? params.get("p_IdPortafolio").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa);

            List<Map<String, Object>> encuesta = jdbc.queryForList(sqlEncuesta, ps);
            List<Map<String, Object>> preguntas = jdbc.queryForList(sqlPreguntas, ps);
            List<Map<String, Object>> respuestas = jdbc.queryForList(sqlRespuestas, ps);
            List<Map<String, Object>> puntajes = jdbc.queryForList(sqlPuntaje, ps);

            result.add("P_OUT_ENCUESTA", toJsonArray(encuesta));
            result.add("P_OUT_PREGUNTAS", toJsonArray(preguntas));
            result.add("P_OUT_RESPUESTAS", toJsonArray(respuestas));
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", mensajes);
        } catch (Exception e) {
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); mensajes.add(msg);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L); result.add("P_OUT_MENSAJES", mensajes);
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
