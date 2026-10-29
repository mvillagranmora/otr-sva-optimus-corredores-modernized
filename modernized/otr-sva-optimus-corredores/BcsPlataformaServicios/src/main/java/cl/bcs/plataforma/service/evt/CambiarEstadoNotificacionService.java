package cl.bcs.plataforma.service.evt;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CambiarEstadoNotificacionService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CambiarEstadoNotificacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.CambiarEstadoNotificacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectOrigen = loadSql("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_SelectOrigenEvento.sql");
            String sqlUpdateOrigen = loadSql("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_UpdateOrigenEvento.sql");
            String sqlUpdateNotificacion = loadSql("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_UpdateNotificacion.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // PlataformaUtils.MensajeSalida used for output messages

            Long idEvento = params.has("p_IdEvento") && !params.get("p_IdEvento").isJsonNull() ? params.get("p_IdEvento").getAsLong() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : null;

            List<Map<String, Object>> origenes = jdbc.queryForList(sqlSelectOrigen,
                    new MapSqlParameterSource().addValue("IdEvento", idEvento));

            jdbc.update(sqlUpdateOrigen, new MapSqlParameterSource().addValue("IdEvento", idEvento).addValue("IdEstado", idEstado));
            jdbc.update(sqlUpdateNotificacion, new MapSqlParameterSource().addValue("IdEvento", idEvento).addValue("IdEstado", idEstado));

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", mensajes);
        } catch (Exception e) {
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); mensajes.add(msg);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L); result.add("P_OUT_MENSAJES", mensajes);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
