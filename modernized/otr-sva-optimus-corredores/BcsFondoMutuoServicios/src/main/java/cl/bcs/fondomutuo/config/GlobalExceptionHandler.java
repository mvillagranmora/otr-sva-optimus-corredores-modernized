package cl.bcs.fondomutuo.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler. SEC-003/SEC-006 (DEV-F3): the body is a FIXED message plus a correlationId; the exception text, the JDBC root cause
 * (host, driver) and the stack trace are never returned. The detail goes to the server log only, with CR/LF/control characters replaced
 * ({@link LogSeguro}), under the same correlationId.
 *   NPE, IAE, NFE  to 400 Bad Request
 *   UOE            to 503 Service Unavailable (external dependency not available)
 *   JDBC connection to 503 (DATABASE_UNAVAILABLE)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNpe(NullPointerException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request", ex, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIae(IllegalArgumentException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request", ex, false);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ProblemDetail handleNfe(NumberFormatException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid number format", ex, false);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleUoe(UnsupportedOperationException ex) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "External dependency not available", ex, true);
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<Map<String, String>> handleJdbcConnection(CannotGetJdbcConnectionException ex) {
        String correlationId = UUID.randomUUID().toString();
        Throwable raiz = ex.getMostSpecificCause();
        log.error("JDBC connection unavailable: correlationId={} cause={}: {}", correlationId, raiz.getClass().getName(), LogSeguro.limpiar(raiz.getMessage()));
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "DATABASE_UNAVAILABLE");
        cuerpo.put("message", "Database not available");
        cuerpo.put("correlationId", correlationId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(cuerpo);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle, Exception ex, boolean error) {
        String correlationId = UUID.randomUUID().toString();
        String linea = "{}: correlationId={} message={}";
        if (error) {
            log.error(linea, ex.getClass().getSimpleName(), correlationId, LogSeguro.limpiar(ex.getMessage()));
        } else {
            log.warn(linea, ex.getClass().getSimpleName(), correlationId, LogSeguro.limpiar(ex.getMessage()));
        }
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(titulo);
        pd.setDetail(detalle);
        pd.setProperty("correlationId", correlationId);
        return pd;
    }
}
