package cl.bcs.fondomutuo.jobs.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Global exception handler for Fondo Mutuo Jobs REST API.
 *
 * Mapping:
 *   NullPointerException       → 400 Bad Request
 *   IllegalArgumentException   → 400 Bad Request
 *   NumberFormatException      → 400 Bad Request
 *   UnsupportedOperationException → 503 Service Unavailable
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException ex) {
        log.warn("NPE in request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error(400, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error(400, ex.getMessage()));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Map<String, Object>> handleNumberFormat(NumberFormatException ex) {
        log.warn("Number format error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error(400, "Invalid number format: " + ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedOperation(UnsupportedOperationException ex) {
        log.warn("Operation not available: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error(503, ex.getMessage()));
    }

    private Map<String, Object> error(int status, String message) {
        return Map.of("status", status, "error", message != null ? message : "Unknown error");
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<Map<String, String>> handleJdbcConnection(
            CannotGetJdbcConnectionException ex) {
        String rootCause = ex.getMostSpecificCause().getMessage();
        log.error("JDBC connection unavailable: {}", rootCause);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "DATABASE_UNAVAILABLE",
                        "message", "No se pudo obtener conexión a la base de datos: " + rootCause));
    }

}