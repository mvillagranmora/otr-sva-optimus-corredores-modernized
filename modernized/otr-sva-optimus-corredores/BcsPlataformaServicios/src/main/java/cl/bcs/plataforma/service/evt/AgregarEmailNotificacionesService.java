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
public class AgregarEmailNotificacionesService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarEmailNotificacionesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.AgregarEmailNotificaciones"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlObtenerEmail = loadSql("cl/bcs/plataforma/evt/sql/ObtenerEmailNotificaciones.sql");
            String sqlDestinatarios = loadSql("cl/bcs/plataforma/evt/sql/ObtenerDestinatariosNotificaciones.sql");
            String sqlArchivos = loadSql("cl/bcs/plataforma/evt/sql/ObtenerArchivosAdjuntosNotificaciones.sql");
            String sqlActualizarEstado = loadSql("cl/bcs/plataforma/evt/sql/ActualizarEstadoNotificaciones.sql");
            String sqlActualizarDetalle = loadSql("cl/bcs/plataforma/evt/sql/ActualizarEstadoNotificacionDetalle.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long tanda = params.has("p_Tanda") && !params.get("p_Tanda").isJsonNull() ? params.get("p_Tanda").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("Tanda", tanda);

            List<Map<String, Object>> emails = jdbc.queryForList(sqlObtenerEmail, ps);
            List<Map<String, Object>> destinatarios = jdbc.queryForList(sqlDestinatarios, ps);
            List<Map<String, Object>> archivos = jdbc.queryForList(sqlArchivos, ps);

            JsonArray emailArr = new JsonArray();
            emails.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); emailArr.add(o); });

            result.add("P_OUT_EMAILS", emailArr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
