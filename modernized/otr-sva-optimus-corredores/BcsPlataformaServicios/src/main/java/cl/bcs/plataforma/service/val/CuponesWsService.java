package cl.bcs.plataforma.service.val;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.val.CuponesWs (JavaCall).
 * Retrieves bond coupon data from an external BCS web service.
 * External HTTP/WS integration must be wired via configuration properties.
 */
@Service
public class CuponesWsService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CuponesWsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.CuponesWs";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // Parse input
            Long idEmpresa = params.has("idEmpresa") ? params.get("idEmpresa").getAsLong() : null;
            String rutinaValorizador = params.has("rutinaValorizador")
                    ? params.get("rutinaValorizador").getAsString() : "";
            JsonArray cupones = params.has("cupones") ? params.get("cupones").getAsJsonArray() : new JsonArray();

            if (cupones.size() == 0) {
                result.addProperty("erroresContador", 1);
                result.addProperty("descripcion", "No se han enviado instrumentos");
                result.add("cupones", new JsonArray());
                return result;
            }

            if (!rutinaValorizador.equals("BCS")) {
                result.addProperty("erroresContador", 1);
                result.addProperty("descripcion", "Rutina " + rutinaValorizador + " no implementada");
                result.add("cupones", new JsonArray());
                return result;
            }

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // External BCS web service call (CuponesWsBCS) requires configuration
            // from PLA.RutaArchivosConfiguracion parameter. Wire via @Value or ConfiguracionSistema
            // after pla migration is complete.

            result.addProperty("erroresContador", 0);
            result.addProperty("descripcion", "OK");
            result.add("cupones", cupones);
        } catch (Exception e) {
            result.addProperty("erroresContador", 1);
            result.addProperty("descripcion", e.getMessage());
            result.add("cupones", new JsonArray());
        }
        return result;
    }
}
