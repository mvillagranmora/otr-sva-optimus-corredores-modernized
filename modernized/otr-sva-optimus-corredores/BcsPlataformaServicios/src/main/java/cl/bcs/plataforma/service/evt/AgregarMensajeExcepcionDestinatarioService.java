package cl.bcs.plataforma.service.evt;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgregarMensajeExcepcionDestinatarioService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarMensajeExcepcionDestinatarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.AgregarMensajeExcepcionDestinatario"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlInsert = loadSql("cl/bcs/plataforma/evt/sql/AgregarMensajeExcepcionDestinatario_InsertMensajeExcepcionDestinatario.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // PlataformaUtils.MensajeSalida used for output messages

            if (params.has("p_MensajeExcepcionDestinatario") && params.get("p_MensajeExcepcionDestinatario").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_MensajeExcepcionDestinatario")) {
                    JsonObject item = el.getAsJsonObject();
                    MapSqlParameterSource ps = new MapSqlParameterSource()
                            .addValue("IdEmpresa", item.has("IdEmpresa") && !item.get("IdEmpresa").isJsonNull() ? item.get("IdEmpresa").getAsLong() : null)
                            .addValue("IdTipoMensaje", item.has("IdTipoMensaje") && !item.get("IdTipoMensaje").isJsonNull() ? item.get("IdTipoMensaje").getAsString() : null)
                            .addValue("Email", item.has("Email") && !item.get("Email").isJsonNull() ? item.get("Email").getAsString() : null)
                            .addValue("Nombre", item.has("Nombre") && !item.get("Nombre").isJsonNull() ? item.get("Nombre").getAsString() : null)
                            .addValue("IdEstado", item.has("IdEstado") && !item.get("IdEstado").isJsonNull() ? item.get("IdEstado").getAsString() : "ACT");
                    jdbc.update(sqlInsert, ps);
                }
            }

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
