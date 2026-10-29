package cl.bcs.plataforma.service.evt;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrearMensajeService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearMensajeService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.CrearMensaje"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlDeshabilita = loadSql("cl/bcs/plataforma/evt/sql/ComunesMensaje_UpdateDeshabilitaMensajesPorTipo.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/evt/sql/CrearMensaje_InsertMensaje.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // PlataformaUtils.MensajeSalida used for output messages

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idTipoMensaje = params.has("p_IdTipoMensaje") && !params.get("p_IdTipoMensaje").isJsonNull() ? params.get("p_IdTipoMensaje").getAsString() : null;

            jdbc.update(sqlDeshabilita, new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdTipoMensaje", idTipoMensaje));

            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdTipoMensaje", idTipoMensaje)
                    .addValue("Asunto", params.has("p_Asunto") && !params.get("p_Asunto").isJsonNull() ? params.get("p_Asunto").getAsString() : null)
                    .addValue("Cuerpo", params.has("p_Cuerpo") && !params.get("p_Cuerpo").isJsonNull() ? params.get("p_Cuerpo").getAsString() : null)
                    .addValue("IdEstado", params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : "ACT");
            jdbc.update(sqlInsert, ps);

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
