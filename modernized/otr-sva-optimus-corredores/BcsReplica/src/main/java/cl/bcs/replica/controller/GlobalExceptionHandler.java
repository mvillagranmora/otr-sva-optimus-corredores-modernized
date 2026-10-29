package cl.bcs.replica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.util.Map;

/**
 * Global exception handler for BcsReplica REST layer.
 *
 * NPE  → 400 Bad Request  (missing required input)
 * IAE  → 400 Bad Request  (invalid argument)
 * NFE  → 400 Bad Request  (number format error)
 * UOE  → 503 Service Unavailable (HANA-specific path not available in this environment)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNpe(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Missing required field: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIae(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid argument: " + ex.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNfe(NumberFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid number format: " + ex.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> handleUoe(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Operation not supported in this environment: " + ex.getMessage());
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<Map<String, String>> handleJdbcConnection(
            CannotGetJdbcConnectionException ex) {
        String rootCause = ex.getMostSpecificCause().getMessage();
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "DATABASE_UNAVAILABLE",
                        "message", "No se pudo obtener conexión a la base de datos: " + rootCause));
    }

}