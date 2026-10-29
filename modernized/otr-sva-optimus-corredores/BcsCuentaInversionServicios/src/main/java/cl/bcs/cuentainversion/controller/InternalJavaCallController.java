package cl.bcs.cuentainversion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentainversion.service.JavaCallDispatcher;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * <pre>
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 * </pre>
 * Unknown classes return 503, so BcsGateway falls back to TomEE. The class-to-service table lives in {@link JavaCallDispatcher}.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final JavaCallDispatcher dispatcher;

    public InternalJavaCallController(JavaCallDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN", required = false) String token) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn = body.has("class") ? body.get("class").getAsString() : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject() : new JsonObject();

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }
        log.debug("InternalJavaCall: class={}", fqn);

        if (!dispatcher.maneja(fqn)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
        }
        try {
            JsonObject result = dispatcher.despachar(fqn, params);
            return ResponseEntity.ok(String.valueOf(result));
        } catch (UnsupportedOperationException uoe) {
            log.warn("InternalJavaCall: class={} is not available: {}", fqn, uoe.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class '" + fqn + "' not fully implemented; TomEE fallback active");
        } catch (Exception e) {
            log.error("InternalJavaCall: error dispatching class={}", fqn, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing JavaCall class '" + fqn + "': " + e.getMessage());
        }
    }
}
