package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Thin bridge that calls another service's /internal/java-call endpoint via RestClient.
 * Singleton bean — pre-configured RestClients with connect/read timeouts and a shared pool.
 */
@Service
public class JavaCallRestBridge {

    private static final Logger log = LoggerFactory.getLogger(JavaCallRestBridge.class);
    private static final Gson GSON = new Gson();

    private final RestClient plataformaClient;
    private final RestClient replicaClient;
    private final RestClient socioNegocioClient;

    public JavaCallRestBridge(
            @Value("${bcs.plataforma.url}") String plataformaUrl,
            @Value("${bcs.replica.url}") String replicaUrl,
            @Value("${bcs.socionegocio.url:}") String socionegocioUrl) {
        this.plataformaClient = buildClient(plataformaUrl);
        this.replicaClient = buildClient(replicaUrl);
        this.socioNegocioClient = socionegocioUrl.isBlank() ? plataformaClient : buildClient(socionegocioUrl);
    }

    private static RestClient buildClient(String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(30_000);
        return RestClient.builder()
            .requestFactory(factory)
            .baseUrl(baseUrl)
            .build();
    }

    public JsonObject callPlataforma(String fqn, JsonObject params) {
        return call(plataformaClient, fqn, params);
    }

    public JsonObject callReplica(String fqn, JsonObject params) {
        return call(replicaClient, fqn, params);
    }

    public JsonObject callSocioNegocio(String fqn, JsonObject params) {
        return call(socioNegocioClient, fqn, params);
    }

    private JsonObject call(RestClient client, String fqn, JsonObject params) {
        log.debug("RestBridge → {} :: {}", client, fqn);
        String requestBody = GSON.toJson(Map.of("fqn", fqn, "params", GSON.fromJson(params, Map.class)));
        String responseBody = client.post()
            .uri("/internal/java-call")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .retrieve()
            .body(String.class);
        return responseBody != null ? JsonParser.parseString(responseBody).getAsJsonObject() : new JsonObject();
    }

    public JsonObject callPlataforma(String fqn, Object params) {
        return callPlataforma(fqn, JsonParser.parseString(GSON.toJson(params)).getAsJsonObject());
    }

    public JsonObject callReplica(String fqn, Object params) {
        return callReplica(fqn, JsonParser.parseString(GSON.toJson(params)).getAsJsonObject());
    }

    public JsonObject callSocioNegocio(String fqn, Object params) {
        return callSocioNegocio(fqn, JsonParser.parseString(GSON.toJson(params)).getAsJsonObject());
    }
}
