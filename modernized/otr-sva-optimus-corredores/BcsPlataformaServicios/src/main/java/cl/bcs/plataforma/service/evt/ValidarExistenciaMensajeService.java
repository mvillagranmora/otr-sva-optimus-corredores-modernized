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
public class ValidarExistenciaMensajeService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ValidarExistenciaMensajeService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.ValidarExistenciaMensaje"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/evt/sql/ComunesMensaje_SelectCountMensaje.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // PlataformaUtils.MensajeSalida used for output messages

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idTipoMensaje = params.has("p_IdTipoMensaje") && !params.get("p_IdTipoMensaje").isJsonNull() ? params.get("p_IdTipoMensaje").getAsString() : null;

            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdTipoMensaje", idTipoMensaje));

            long count = rows.isEmpty() ? 0L : Long.parseLong(rows.get(0).values().iterator().next().toString());
            result.addProperty("P_OUT_EXISTE", count > 0 ? "S" : "N");
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
