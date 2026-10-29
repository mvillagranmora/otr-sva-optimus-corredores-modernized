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
 * Calls BcsFacturacionServicios via POST /internal/java-call.
 * Used for cross-service calls: cl.bcs.facturacion.CrearComprobante, etc.
 */
@Component
public class FacturacionCallClient {

    private static final Logger log = LoggerFactory.getLogger(FacturacionCallClient.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final RestClient rest;

    public FacturacionCallClient(
            @Value("${bcs.facturacion.url:http://bcs-facturacion-servicios:8099}") String facturacionUrl) {
        this.rest = RestClient.builder().baseUrl(facturacionUrl).build();
    }

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
            log.warn("FacturacionCallClient.call({}) failed: {}", fqn, e.getMessage());
            return new JsonObject();
        }
    }

    public long callAndGetErrorCount(String fqn, JsonObject params) {
        JsonObject resp = call(fqn, params);
        if (resp.has("P_OUT_ERRORESCONTADOR"))
            return resp.get("P_OUT_ERRORESCONTADOR").getAsLong();
        return 0L;
    }
}
