package cl.bcs.plataforma.adapter.in;

import cl.bcs.plataforma.internal.Respuesta;
import cl.bcs.plataforma.internal.SolicitudJavaCall;
import cl.bcs.plataforma.service.InternalJavaCallRouter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.UUID;

/**
 * Internal JavaCall router for the jobs (step 5b): {@code POST /internal/java-call}, body {@code {"class","params"}}.
 * Authenticated by {@code InternalTokenFilter} (X-Internal-Token), never by a user session. Answers of the targets pass through
 * unchanged (status, body). Infrastructure errors answer a fixed JSON with a correlation id; the exception message is only logged.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final InternalJavaCallRouter router;
    private final int maxBytes;

    public InternalJavaCallController(InternalJavaCallRouter router,
            @Value("${plataforma.javacall.internal.max-body-bytes:16777216}") int maxBytes) {
        this.router = router;
        this.maxBytes = maxBytes;
    }

    @PostMapping(path = "/internal/java-call", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(HttpServletRequest request) {
        try {
            SolicitudJavaCall s = SolicitudJavaCall.parsear(leer(request));
            Respuesta r = router.enrutar(s, request.getHeader("X-SESSION-TOKEN"));
            ResponseEntity.BodyBuilder b = ResponseEntity.status(r.status());
            if (r.contentType() != null) b.header("Content-Type", r.contentType());
            return b.body(r.cuerpo());
        } catch (SolicitudJavaCall.Invalida e) {
            return error(HttpStatus.valueOf(e.status()), e.getMessage(), null);
        } catch (ResourceAccessException e) {
            String id = UUID.randomUUID().toString();
            log.error("internal java-call: transport failure correlationId={}", id, e);
            boolean timeout = e.getCause() instanceof SocketTimeoutException;
            return error(timeout ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY,
                    timeout ? "Target timed out" : "Target unreachable", id);
        } catch (RuntimeException e) {
            String id = UUID.randomUUID().toString();
            log.error("internal java-call: unexpected failure correlationId={}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", id);
        } catch (IOException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid request body", null);
        }
    }

    /** Reads at most {@code maxBytes}; one more byte means oversize (400). */
    private byte[] leer(HttpServletRequest request) throws IOException {
        try (InputStream in = request.getInputStream()) {
            byte[] b = in.readNBytes(maxBytes + 1);
            if (b.length > maxBytes) throw new SolicitudJavaCall.Invalida(400, "Request body too large");
            return b;
        }
    }

    private static ResponseEntity<String> error(HttpStatus status, String mensaje, String correlacion) {
        com.google.gson.JsonObject o = new com.google.gson.JsonObject();
        o.addProperty("error", mensaje);
        if (correlacion != null) o.addProperty("correlationId", correlacion);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(o.toString());
    }
}
