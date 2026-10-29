package cl.bcs.custodia.controller;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * All 180 registered JavaCall classes are dispatched via the JavaCallService registry.
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 * Registered-but-deferred classes also return 503 (via service stub) → TomEE handles them.
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    /** FQN → service map, auto-populated from all JavaCallService beans in context. */
    private final Map<String, JavaCallService> registry;

    public InternalJavaCallController(List<JavaCallService> allServices) {
        this.registry = allServices.stream()
            .collect(Collectors.toMap(JavaCallService::getFqn, Function.identity()));
        log.info("InternalJavaCallController: {} JavaCall services registered", registry.size());
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN",
                                                          required = false) String token) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn    = body.has("class")  ? body.get("class").getAsString()        : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject() : new JsonObject();
        int skip      = body.has("skip")   ? body.get("skip").getAsInt()             : 0;
        int take      = body.has("take")   ? body.get("take").getAsInt()             : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        JavaCallService svc = registry.get(fqn);
        if (svc == null) {
            // Unknown class → 503, BcsGateway falls back to TomEE
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' not registered; TomEE fallback active");
        }

        // Invoke service (may itself throw 503 if deferred)
        JsonObject result = svc.execute(params);
        return ResponseEntity.ok(result.toString());
    }
}
