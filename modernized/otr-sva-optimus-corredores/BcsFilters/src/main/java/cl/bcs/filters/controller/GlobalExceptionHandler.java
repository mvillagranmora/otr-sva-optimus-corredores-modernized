package cl.bcs.filters.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Centralised exception-to-HTTP-status mapping.
 *
 * Mandatory mappings (per project standards):
 *   NPE                     → 400 Bad Request
 *   IllegalArgumentException → 400 Bad Request
 *   NumberFormatException    → 400 Bad Request
 *   UnsupportedOperationException → 503 Service Unavailable
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNpe(NullPointerException ex) {
        log.error("NPE in controller", ex);
        return ResponseEntity.badRequest().body(Map.of("error", "Bad request (null value)"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIae(IllegalArgumentException ex) {
        log.warn("IllegalArgument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Map<String, String>> handleNfe(NumberFormatException ex) {
        log.warn("NumberFormat: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format"));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, String>> handleUoe(UnsupportedOperationException ex) {
        log.error("UnsupportedOperation: {}", ex.getMessage());
        return ResponseEntity.status(503).body(Map.of("error", "Service unavailable"));
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