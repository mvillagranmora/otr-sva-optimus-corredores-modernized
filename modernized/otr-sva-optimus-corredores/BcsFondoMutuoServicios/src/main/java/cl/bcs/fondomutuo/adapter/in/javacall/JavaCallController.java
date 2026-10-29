package cl.bcs.fondomutuo.adapter.in.javacall;

import java.sql.Connection;
import java.sql.SQLException;
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

import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Receives the J-type JavaCall dispatches of the platform router. Replaces the old {@code adapter.in.web.InternalJavaCallController}, which injected the
 * 132 stub services and answered 200 with invented data (that class stays in the tree, out of the Spring context: see {@code BcsFondoMutuoApplication}).
 *
 * <pre>
 *   POST /internal/java-call      {"class": "cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate", "params": {...}}
 *   200  the legacy JSON, unchanged (commit only if P_OUT_ERRORESCONTADOR = 0; a 200 with a counter &gt; 0 is rolled back; see JavaCallUnitOfWorkFilter)
 *   400  body that is not a JSON object, or without "class", or with "params" that is not an object
 *   401  (filter) missing or wrong X-Internal-Token
 *   415  (Spring, before this method) a Content-Type that is not application/json; the router must treat it as a non-retryable client error (L6)
 *   503  class outside the allow-list (or without handler yet), a closed gate G0..G5, or a collaborator unreachable: the router falls back to TomEE.
 *        ONLY while nothing of this request can be committed; afterwards it is the generic 500 (M3, see EscrituraConfirmada)
 *   500  generic body with a correlationId; the exception text and the stack trace are NEVER returned (DEV-F3)
 * </pre>
 * A non-2xx answer does NOT guarantee that nothing was committed: the lock DDL (CREATE LOCAL TEMPORARY TABLE / DROP TABLE) commits the open transaction
 * implicitly on HANA and the HTTP write collaborators commit in their module (option A, HANA_DDL_COMMIT_HALLAZGO). A 2xx does not mean "committed" either
 * (counter &gt; 0 = rollback). No state is kept between requests (DEV-F2): the connection comes from the transaction of the current unit of work.
 * The {@code Connection} passed to {@code handler.call} exists only for the signature of the legacy handlers (L3): the adapters take the connection
 * of the same transaction through {@code ConexionUnidadDeTrabajo}; a handler must not keep it nor close it.
 */
@RestController
public class JavaCallController {

    private static final Logger log = LoggerFactory.getLogger(JavaCallController.class);

    private final JavaCallDispatcher dispatcher;
    private final DataSource dataSource;

    public JavaCallController(JavaCallDispatcher dispatcher, DataSource dataSource) {
        this.dispatcher = dispatcher;
        this.dataSource = dataSource;
        log.info("JavaCallController: allow-list {}; handlers registered {}", JavaCallDispatcher.LISTA_BLANCA, dispatcher.servidas());
    }

    @PostMapping(path = "/internal/java-call", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
        Connection con = null;
        try {
            // SEC-003: inside the try, so a database outage yields the generic 500 (correlationId, no JDBC text) and not an exception that escapes
            con = DataSourceUtils.getConnection(dataSource);
            JsonObject result = handler.get().call(con, params);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result.toString());
        } catch (ServicioNoServidoException e) {
            if (EscrituraConfirmada.confirmada()) {
                // M3: something of this request is already committed (lock DDL on HANA / HTTP write collaborator): a 503 would make TomEE repeat it
                String correlationId = UUID.randomUUID().toString();
                log.error("JavaCall not served AFTER a committed write, answering 500: class={} gate={} correlationId={}", cl.bcs.fondomutuo.config.LogSeguro.limpiar(fqn), e.compuerta(), correlationId);
                return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal error", correlationId);
            }
            log.warn("JavaCall not served: class={} gate={}", cl.bcs.fondomutuo.config.LogSeguro.limpiar(fqn), e.compuerta());
            return error(HttpStatus.SERVICE_UNAVAILABLE, "request not served by this module; TomEE fallback", null);
        } catch (Exception e) {
            String correlationId = UUID.randomUUID().toString();
            log.error("JavaCall failed: class={} correlationId={}", cl.bcs.fondomutuo.config.LogSeguro.limpiar(fqn), correlationId, sinTextoDeBd(e));
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal error", correlationId);
        } finally {
            if (con != null) DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    /**
     * M3 (architecture review F3 W1): the text of a database exception can quote values (HANA does). When the cause chain holds a {@link SQLException} the log gets a
     * copy with the SAME stack trace but only the class names, SQLState and ErrorCode; any other exception is logged as is, unless a message holds control characters (L-1: cleaned copy).
     */
    static Throwable sinTextoDeBd(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof SQLException sql) {
                Throwable copia = new Throwable(cl.bcs.fondomutuo.config.LogSeguro.limpiar(e.getClass().getName() + " caused by " + sql.getClass().getName()
                        + " SQLState=" + sql.getSQLState() + " ErrorCode=" + sql.getErrorCode()));
                copia.setStackTrace(e.getStackTrace());
                return copia;
            }
        }
        // L-1 (architecture review F3 W2, CWE-117): a message with line breaks / control characters (anywhere in the chain) goes to the log as a copy, same stack trace,
        // whose text is the chain "class: message" cleaned by LogSeguro; a message with none is logged as before (the same exception)
        boolean controles = false;
        for (Throwable t = e; t != null && !controles; t = t.getCause() == t ? null : t.getCause()) {
            String m = t.getMessage();
            controles = m != null && m.chars().anyMatch(Character::isISOControl);
        }
        if (!controles) return e;
        StringBuilder cadena = new StringBuilder();
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (cadena.length() > 0) cadena.append(" | caused by ");
            cadena.append(t.getClass().getName()).append(": ").append(cl.bcs.fondomutuo.config.LogSeguro.limpiar(t.getMessage()));
        }
        Throwable copia = new Throwable(cadena.toString());
        copia.setStackTrace(e.getStackTrace());
        return copia;
    }

    private static ResponseEntity<String> error(HttpStatus status, String mensaje, String correlationId) {
        JsonObject o = new JsonObject();
        o.addProperty("error", mensaje);
        if (correlationId != null) o.addProperty("correlationId", correlationId);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(o.toString());
    }
}
