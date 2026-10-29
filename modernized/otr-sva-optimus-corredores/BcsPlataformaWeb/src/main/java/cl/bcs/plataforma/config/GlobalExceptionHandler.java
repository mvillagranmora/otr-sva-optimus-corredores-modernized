package cl.bcs.plataforma.config;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Global exception handler.
 *
 * Mapping rules:
 *   IllegalArgumentException → 400 Bad Request
 *   NumberFormatException    → 400 Bad Request
 *   UnsupportedOperationException → 503 Service Unavailable
 *   ResponseStatusException  → passes through its own status code
 *   Exception (fallback)     → 500 Internal Server Error (NPE falls here — it is a server bug)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIae(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return json(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNfe(NumberFormatException ex) {
        log.warn("NumberFormatException: {}", ex.getMessage());
        return json(HttpStatus.BAD_REQUEST, "NUMBER_FORMAT", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleRse(ResponseStatusException ex) {
        log.warn("ResponseStatusException {}: {}", ex.getStatusCode(), ex.getReason());
        JsonObject body = new JsonObject();
        body.addProperty("codigo", "STATUS_ERROR");
        body.addProperty("mensaje", ex.getReason() != null ? ex.getReason() : "");
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> handleUoe(UnsupportedOperationException ex) {
        log.warn("UnsupportedOperationException: {}", ex.getMessage());
        return json(HttpStatus.SERVICE_UNAVAILABLE, "NOT_IMPLEMENTED", ex.getMessage());
    }


    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<String> handleJdbcConnection(CannotGetJdbcConnectionException ex) {
        String rootCause = ex.getMostSpecificCause().getMessage();
        log.error("JDBC connection unavailable: {}", rootCause);
        return json(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_UNAVAILABLE",
                "No se pudo obtener conexión a la base de datos: " + rootCause);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return json(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An internal error occurred");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private static ResponseEntity<String> json(HttpStatus status, String code, String message) {
        JsonObject body = new JsonObject();
        body.addProperty("codigo", code);
        body.addProperty("mensaje", message != null ? message : "");
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString());
    }
}
