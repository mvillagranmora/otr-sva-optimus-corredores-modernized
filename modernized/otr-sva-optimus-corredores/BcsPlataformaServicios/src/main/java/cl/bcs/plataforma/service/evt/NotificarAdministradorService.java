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
public class NotificarAdministradorService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public NotificarAdministradorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.NotificarAdministrador"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlDestinatarios = loadSql("cl/bcs/plataforma/evt/sql/ObtenerDestinatariosNotificacionAdministrativa.sql");

            Long idEmpresa = params.has("IdEmpresa") && !params.get("IdEmpresa").isJsonNull() ? params.get("IdEmpresa").getAsLong() : null;

            List<Map<String, Object>> destinatarios = jdbc.queryForList(sqlDestinatarios,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            JsonArray destArr = new JsonArray();
            destinatarios.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); destArr.add(o); });

            result.add("P_OUT_DESTINATARIOS", destArr);
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
