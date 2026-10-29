package cl.bcs.mesa.controller;

import cl.bcs.mesa.javacall.JavaCallService;
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
 * All 120 deferred JavaCall classes are now implemented as @Service beans that implement
 * JavaCallService. Spring auto-collects them into the registry at startup.
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final Map<String, JavaCallService> registry;

    public InternalJavaCallController(List<JavaCallService> services) {
        this.registry = services.stream()
                .collect(Collectors.toMap(JavaCallService::fqn, Function.identity()));
        // cl.bcs.Mesa.* alias (legacy mixed-case registration in PLA.Servicio)
        services.forEach(svc -> {
            String fqn = svc.fqn();
            if (fqn.startsWith("cl.bcs.mesa.")) {
                registry.putIfAbsent(fqn.replace("cl.bcs.mesa.", "cl.bcs.Mesa."), svc);
            }
        });
        log.info("JavaCall registry loaded: {} entries", registry.size());
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

        String     fqn    = body.has("class")  ? body.get("class").getAsString()      : "";
        JsonObject params  = body.has("params") ? body.get("params").getAsJsonObject() : new JsonObject();

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={}", fqn);

        JavaCallService svc = registry.get(fqn);
        if (svc == null) {
            log.warn("InternalJavaCall: class={} not in registry; TomEE fallback", fqn);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class '" + fqn + "' not migrated; TomEE fallback active");
        }

        try {
            JsonObject result = svc.execute(params);
            return ResponseEntity.ok(result.toString());
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("JavaCall execution failed for class={}", fqn, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "JavaCall execution error: " + e.getMessage());
        }
    }
}
