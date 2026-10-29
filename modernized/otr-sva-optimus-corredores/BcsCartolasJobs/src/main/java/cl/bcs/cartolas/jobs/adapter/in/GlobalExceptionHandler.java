package cl.bcs.cartolas.jobs.adapter.in;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.util.Map;

/**
 * Manejador global de excepciones para BcsCartolasJobs.
 *
 * Mapping:
 *   NullPointerException          → 400 Bad Request
 *   IllegalArgumentException      → 400 Bad Request
 *   NumberFormatException         → 400 Bad Request
 *   IllegalStateException         → 400 Bad Request  (nómina ya certificada)
 *   UnsupportedOperationException → 503 Service Unavailable (HANA no disponible)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            NullPointerException.class,
            IllegalArgumentException.class,
            NumberFormatException.class,
            IllegalStateException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Bad Request");
        return detail;
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleUnsupportedOperation(UnsupportedOperationException ex) {
        var detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        detail.setTitle("Service Unavailable — HANA required");
        return detail;
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