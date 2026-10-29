package cl.bcs.mesa.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * RestClient bridge to BcsPlataformaServicios /internal/java-call endpoint.
 * Used by Mesa @Service implementations to call plataforma utilities.
 */
@Component
public class PlataformaBridge {

    private static final Logger log = LoggerFactory.getLogger(PlataformaBridge.class);
    private static final Gson GSON = new Gson();

    private final RestClient client;

    public PlataformaBridge(
            @Value("${bcs.plataforma.url:http://bcs-plataforma-servicios:8114}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public JsonObject call(String fqn, JsonObject params) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("class", fqn);
            body.add("params", params);

            String response = client.post()
                    .uri("/internal/java-call")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            return response != null ? JsonParser.parseString(response).getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            log.warn("PlataformaBridge call failed fqn={} error={}", fqn, e.getMessage());
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    public JsonObject callObtenerDiaSiguiente(Long idEmpresa, String fecha) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", idEmpresa);
        JsonObject fechasIn = new JsonObject();
        fechasIn.addProperty("fecha", fecha);
        p.add("p_Fechas", GSON.toJsonTree(Map.of("Fecha", fecha)));
        return call("cl.bcs.plataforma.pla.ObtenerDiaSiguiente", p);
    }

    public JsonObject callRevisarRequisitosBasicos(Long idEmpresa, Long idUsuario, String idModulo) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", idEmpresa);
        p.addProperty("p_IdUsuario", idUsuario);
        p.addProperty("p_IdModulo", idModulo);
        return call("cl.bcs.plataforma.pla.RevisarRequisitosBasicos", p);
    }

    public JsonObject callAgregarLog(Long idEmpresa, Long idUsuario, String idModulo, String tipo, String descripcion) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", idEmpresa);
        p.addProperty("p_IdUsuario", idUsuario);
        p.addProperty("p_IdModulo", idModulo);
        p.addProperty("p_TipoLog", tipo);
        p.addProperty("p_Descripcion", descripcion);
        return call("cl.bcs.plataforma.pla.AgregarLog", p);
    }
}
