package cl.bcs.ordenes.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls BcsPlataformaServicios via POST /internal/java-call.
 * Used for cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad,
 * cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP, etc.
 */
@Component
public class PlataformaCallClient {

    private static final Logger log = LoggerFactory.getLogger(PlataformaCallClient.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final RestClient rest;
    private final String plataformaUrl;

    public PlataformaCallClient(
            @Value("${bcs.plataforma.url:http://bcs-plataforma-servicios:8114}") String plataformaUrl) {
        this.plataformaUrl = plataformaUrl;
        this.rest = RestClient.builder().baseUrl(plataformaUrl).build();
    }

    /**
     * Calls BcsPlataformaServicios for a given FQN class.
     * Returns empty JsonObject on failure (non-fatal for notification calls).
     */
    public JsonObject call(String fqn, JsonObject params) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("class", fqn);
            body.add("params", params);
            String resp = rest.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);
            if (resp == null || resp.isBlank()) return new JsonObject();
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            log.warn("PlataformaCallClient.call({}) failed: {}", fqn, e.getMessage());
            return new JsonObject();
        }
    }

    /**
     * Calls and returns error count from response (0 if call fails).
     */
    public long callAndGetErrorCount(String fqn, JsonObject params) {
        JsonObject resp = call(fqn, params);
        if (resp.has("P_OUT_ERRORESCONTADOR"))
            return resp.get("P_OUT_ERRORESCONTADOR").getAsLong();
        if (resp.has("p_out_ErroresContador"))
            return resp.get("p_out_ErroresContador").getAsLong();
        return 0L;
    }
}
