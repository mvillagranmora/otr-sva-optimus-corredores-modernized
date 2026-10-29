package cl.bcs.tesoreria.adapter.in;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.JavaCallDispatcher;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * <pre>
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 * </pre>
 *
 * <p>The class-to-service table lives in {@link JavaCallDispatcher}. A class that is not migrated, or whose service reports
 * {@link UnsupportedOperationException}, answers 503 so that BcsGateway falls back to TomEE. Malformed bodies answer 400. An unexpected error
 * answers 500 with a generic message and a correlation id; the detail (which may name database objects) is only logged.
 *
 * <p>Authentication of the caller is done by {@code TokenInternoFilter} (fail closed).
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final JavaCallDispatcher dispatcher;

    public InternalJavaCallController(JavaCallDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping(path = "/internal/java-call", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody) {
        JsonObject body = objeto(rawBody, "Invalid JSON body");
        String clase = texto(body.get("class"));
        if (clase == null || clase.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }
        JsonObject params = body.has("params") && !body.get("params").isJsonNull() ? objeto(body.get("params"), "'params' must be an object") : new JsonObject();
        log.debug("InternalJavaCall: class={}", clase);

        try {
            if (!dispatcher.maneja(clase)) {
                throw noMigrada(clase);
            }
            return ResponseEntity.ok(String.valueOf(dispatcher.despachar(clase, params)));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (UnsupportedOperationException e) {
            log.warn("InternalJavaCall: class={} is not available: {}", clase, e.getMessage());
            throw noMigrada(clase);
        } catch (Exception e) {
            String correlacion = UUID.randomUUID().toString();
            log.error("InternalJavaCall: error dispatching class={} correlationId={}", clase, correlacion, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing JavaCall class '" + clase + "' (correlationId " + correlacion + ")");
        }
    }

    private static ResponseStatusException noMigrada(String clase) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "JavaCall class '" + clase + "' not yet migrated; TomEE fallback active");
    }

    private static JsonObject objeto(String json, String mensaje) {
        try {
            return objeto(JsonParser.parseString(json), mensaje);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private static JsonObject objeto(JsonElement e, String mensaje) {
        if (e == null || !e.isJsonObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
        return e.getAsJsonObject();
    }

    /** {@code class} must be a JSON string; anything else is a malformed request. */
    private static String texto(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' must be a string");
        }
        return e.getAsString();
    }

}
