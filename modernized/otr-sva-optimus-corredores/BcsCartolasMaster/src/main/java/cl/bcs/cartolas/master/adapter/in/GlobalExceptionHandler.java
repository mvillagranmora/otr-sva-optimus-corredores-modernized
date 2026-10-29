package cl.bcs.cartolas.master.adapter.in;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.util.Map;

/**
 * Manejador global de excepciones para BcsCartolasMaster.
 *
 * <p>Mapeo:</p>
 * <ul>
 *   <li>{@link NullPointerException}          → 400 Bad Request</li>
 *   <li>{@link IllegalArgumentException}       → 400 Bad Request</li>
 *   <li>{@link NumberFormatException}          → 400 Bad Request</li>
 *   <li>{@link UnsupportedOperationException}  → 503 Service Unavailable (requiere HANA)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class,
            NumberFormatException.class})
    public ProblemDetail handleBadRequest(final RuntimeException ex) {
        final var detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Bad Request");
        return detail;
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleUnsupportedOperation(final UnsupportedOperationException ex) {
        final var detail = ProblemDetail.forStatusAndDetail(
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