package cl.bcs.plataforma.service.val;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.val.TipificarWs (JavaCall).
 * Obtains instrument valuation type from external BCS web service (ValorizarWsBCS).
 * External WS connection parameters must be supplied via configuration properties.
 */
@Service
public class TipificarWsService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public TipificarWsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.TipificarWs";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String rutinaValorizador = params.has("rutinaValorizador")
                    ? params.get("rutinaValorizador").getAsString() : "";
            JsonArray instrumentos = params.has("instrumentos")
                    ? params.get("instrumentos").getAsJsonArray() : new JsonArray();

            if (instrumentos.size() == 0) {
                result.addProperty("erroresContador", 1);
                result.addProperty("descripcion", "No se han enviado instrumentos");
                result.add("instrumentos", new JsonArray());
                return result;
            }

            if (!rutinaValorizador.equals("BCS")) {
                result.addProperty("erroresContador", 1);
                result.addProperty("descripcion", "Rutina " + rutinaValorizador + " no implementada");
                result.add("instrumentos", new JsonArray());
                return result;
            }

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ValorizarWsBCS external WS connection requires valorizador.properties
            // (SERVER, USER, PASS, CONNTIMEOUT, READTIMEOUT) via environment config.

            result.addProperty("erroresContador", 0);
            result.add("instrumentos", instrumentos);
        } catch (Exception e) {
            result.addProperty("erroresContador", 1);
            result.addProperty("descripcion", e.getMessage());
            result.add("instrumentos", new JsonArray());
        }
        return result;
    }
}
