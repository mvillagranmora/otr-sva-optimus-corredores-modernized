package cl.bcs.plataforma.service.evt;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrearSolicitudService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearSolicitudService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.CrearSolicitud"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString needed for fechaSistema
            // Relies on pla.* system date configuration and evt SP integration

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }
}
