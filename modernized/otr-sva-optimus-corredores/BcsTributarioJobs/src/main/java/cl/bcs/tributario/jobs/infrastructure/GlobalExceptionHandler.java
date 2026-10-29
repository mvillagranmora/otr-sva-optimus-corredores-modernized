package cl.bcs.tributario.jobs.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.util.Map;

/**
 * Centralised exception mapping for the tributario-jobs REST API.
 *
 * <table>
 *   <tr><th>Exception</th><th>HTTP status</th><th>Rationale</th></tr>
 *   <tr><td>NullPointerException</td><td>400 Bad Request</td>
 *       <td>Required parameter or field was not supplied</td></tr>
 *   <tr><td>IllegalArgumentException</td><td>400 Bad Request</td>
 *       <td>Parameter value violates a business constraint</td></tr>
 *   <tr><td>NumberFormatException</td><td>400 Bad Request</td>
 *       <td>Non-numeric string supplied for a numeric parameter</td></tr>
 *   <tr><td>UnsupportedOperationException</td><td>503 Service Unavailable</td>
 *       <td>Operation requires a live HANA environment / stored procedure</td></tr>
 * </table>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointer(NullPointerException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Missing required value");
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Invalid argument");
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(NumberFormatException.class)
    public ProblemDetail handleNumberFormat(NumberFormatException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Invalid number format");
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleUnsupportedOperation(UnsupportedOperationException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        detail.setTitle("Operation requires HANA environment");
        detail.setDetail(ex.getMessage());
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