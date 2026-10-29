package cl.bcs.custodia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Global exception handler for Custodia Servicios REST layer.
 *
 * Mapping:
 *   NullPointerException          -> 400 Bad Request
 *   IllegalArgumentException      -> 400 Bad Request
 *   NumberFormatException         -> 400 Bad Request
 *   UnsupportedOperationException -> 503 Service Unavailable (HANA SP not available)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNPE(NullPointerException ex) {
        log.warn("NullPointerException: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "Missing required parameter", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIAE(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "Invalid argument", ex.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Map<String, Object>> handleNFE(NumberFormatException ex) {
        log.warn("NumberFormatException: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "Invalid number format", ex.getMessage());
    }

    /**
     * HANA stored-procedure operations are not available outside HANA environment.
     * Returns 503 with the SP name so callers can identify the missing capability.
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUOE(UnsupportedOperationException ex) {
        log.warn("UnsupportedOperationException (HANA SP): {}", ex.getMessage());
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Operation requires HANA stored procedure", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatus status, String error, String detail) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", error,
                "detail", detail != null ? detail : ""));
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