package cl.bcs.plataforma.adapter.in.rest;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.plataforma.adapter.in.javacall.JavaCallHandler;

/**
 * Receives the J-type JavaCall dispatches of the platform router ({@code JavaCallRouterService}).
 *
 * <pre>
 *   POST /internal/java-call      {"class": "cl.bcs.plataforma.cre.ConsultarEstadoModulo", "params": {...}}
 *   200  the legacy JSON, unchanged (same keys, same order)
 *   400  body that is not a JSON object, or without "class", or with "params" that is not an object
 *   401  (filter) missing or wrong X-Internal-Token
 *   503  class not on the allow-list ({@link JavaCallDispatcher}): the router falls back to TomEE
 *   500  generic body with a correlationId; the exception text is NEVER returned (DEV-A3)
 * </pre>
 * The token ({@code TokenInternoFilter}) and the unit of work ({@code JavaCallUnitOfWorkFilter}: one connection, commit only for
 * 2xx with error counter 0) are filters that wrap this method; the handler gets the connection of that unit of work.
 * The 326 stub services of the skeleton are not in the context any more (see {@code PlataformaApplication}).
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final JavaCallDispatcher dispatcher;
    private final DataSource dataSource;

    public InternalJavaCallController(JavaCallDispatcher dispatcher, DataSource dataSource) {
        this.dispatcher = dispatcher;
        this.dataSource = dataSource;
        log.info("InternalJavaCallController: {} JavaCall class(es) served: {}", dispatcher.servidas().size(), dispatcher.servidas());
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, "invalid JSON body", null);
        }
        JsonElement clase = body.get("class");
        String fqn = clase != null && clase.isJsonPrimitive() ? clase.getAsString() : "";
        if (fqn.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "'class' field required", null);
        }
        JsonElement p = body.get("params");
        if (p != null && !p.isJsonNull() && !p.isJsonObject()) {
            return error(HttpStatus.BAD_REQUEST, "'params' must be an object", null);
        }
        JsonObject params = p != null && p.isJsonObject() ? p.getAsJsonObject() : new JsonObject();

        Optional<JavaCallHandler> handler = dispatcher.buscar(fqn);
        if (handler.isEmpty()) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "JavaCall class not served by this module; TomEE fallback", null);
        }

        Connection con = DataSourceUtils.getConnection(dataSource);
        try {
            JsonObject result = handler.get().call(con, params);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result.toString());
        } catch (Exception e) {
            String correlationId = UUID.randomUUID().toString();
            log.error("JavaCall failed: class={} correlationId={}", fqn, correlationId, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal error", correlationId);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private static ResponseEntity<String> error(HttpStatus status, String mensaje, String correlationId) {
        JsonObject o = new JsonObject();
        o.addProperty("error", mensaje);
        if (correlationId != null) o.addProperty("correlationId", correlationId);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(o.toString());
    }
}
