package cl.bcs.apv.infrastructure.web;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import cl.bcs.apv.application.comun.Sneaky;

/**
 * The ONE place that turns an exception of a typed REST adapter into an HTTP answer (architecture review of Tesoreria: 400 / 503 / generic 500).
 * <ul>
 *   <li>400: the request cannot be read or bound (malformed JSON, wrong type, missing query parameter);</li>
 *   <li>503: a collaborator is not available ({@link RestClientException} toward the platform router, {@link UnsupportedOperationException}):
 *       the caller may retry or fall back;</li>
 *   <li>500: anything else, with a GENERIC message and a correlation id; the detail (class, SQL, parameters) is only logged. The legacy behaviours that
 *       the use cases reproduce (NullPointerException, SQLException, IndexOutOfBoundsException...) end here: they are defects kept bug-for-bug and the
 *       response must not leak them.</li>
 * </ul>
 */
@RestControllerAdvice(basePackages = "cl.bcs.apv.infrastructure.web")
public class ApvExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApvExceptionHandler.class);

    @ExceptionHandler({ HttpMessageNotReadableException.class, TypeMismatchException.class, MissingServletRequestParameterException.class,
            BindException.class, com.google.gson.JsonParseException.class })
    ResponseEntity<Map<String, String>> peticionInvalida(Exception e) {
        log.debug("Invalid request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
    }

    @ExceptionHandler({ RestClientException.class, UnsupportedOperationException.class })
    ResponseEntity<Map<String, String>> colaboradorNoDisponible(Exception e) {
        String correlacion = UUID.randomUUID().toString();
        log.warn("Collaborator not available correlationId={}", correlacion, e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service not available", "correlationId", correlacion));
    }

    /** A checked legacy exception (SQLException...) thrown through Sneaky arrives wrapped by the transaction proxy: classify by the real cause. */
    @ExceptionHandler(UndeclaredThrowableException.class)
    ResponseEntity<Map<String, String>> envuelta(UndeclaredThrowableException e) {
        Throwable causa = Sneaky.causa(e);
        if (causa instanceof RestClientException || causa instanceof UnsupportedOperationException) {
            return colaboradorNoDisponible((Exception) causa);
        }
        return inesperado(causa instanceof Exception ex ? ex : e);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> inesperado(Exception e) {
        String correlacion = UUID.randomUUID().toString();
        log.error("APV request failed correlationId={}", correlacion, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal error", "correlationId", correlacion));
    }
}
