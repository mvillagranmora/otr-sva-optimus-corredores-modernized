package cl.bcs.contabilidad.infrastructure.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * One database transaction per JavaCall request, on one connection, the legacy unit of work.
 *
 * <p>Legacy {@code BcsPlataformaWeb CallServlet.java:436-441,495}: a J-type JavaCall ran on a single
 * {@code serviceConn}. That connection was rolled back when {@code P_OUT_ERRORESCONTADOR} (or
 * {@code P_OUT_ERRORCOUNTER}, {@code :439}) was missing or greater than 0, and committed otherwise. Exceptions also
 * rolled back. Here the same rule applies to {@code /internal/java-call}:
 * <ul>
 *   <li>commit only for a 2xx response whose JSON carries an error counter equal to 0;</li>
 *   <li>roll back for any other status (a 503 fallback included), a missing or positive counter, or an exception.</li>
 * </ul>
 *
 * <p>Holding one connection for the whole call also keeps HANA global temporary tables
 * ({@code GBL.CentralizaGenerico_*}, {@code GBL.CentralizaTesoreria_*}) on the session that wrote them.
 *
 * <p>If a participant marked the transaction rollback-only while the response reported success, the commit throws
 * {@link UnexpectedRollbackException}; the client then gets a 500 instead of a false success.
 */
@Component
public class JavaCallUnitOfWorkFilter extends OncePerRequestFilter {

    static final String RUTA = "/internal/java-call";
    private static final String[] CONTADORES = {"P_OUT_ERRORCOUNTER", "P_OUT_ERRORESCONTADOR"};
    private static final Logger log = LoggerFactory.getLogger(JavaCallUnitOfWorkFilter.class);

    private final TransactionTemplate transaccion;

    public JavaCallUnitOfWorkFilter(PlatformTransactionManager transactionManager) {
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().endsWith(RUTA);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper respuesta = new ContentCachingResponseWrapper(response);
        try {
            transaccion.executeWithoutResult(status -> {
                try {
                    chain.doFilter(request, respuesta);
                } catch (IOException | ServletException e) {
                    throw new EnvolturaExcepcion(e);
                }
                if (!confirmable(respuesta)) {
                    status.setRollbackOnly();
                }
            });
        } catch (EnvolturaExcepcion e) {
            if (e.getCause() instanceof IOException io) throw io;
            throw (ServletException) e.getCause();
        } catch (UnexpectedRollbackException e) {
            log.error("JavaCall {} rolled back after reporting success", RUTA, e);
            respuesta.resetBuffer();
            respuesta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
            respuesta.getOutputStream().write(
                    "{\"error\":\"transaction rolled back\"}".getBytes(StandardCharsets.UTF_8));
        }
        respuesta.copyBodyToResponse();
    }

    /** Legacy rule: commit only when the response carries an error counter equal to 0. */
    static boolean confirmable(ContentCachingResponseWrapper respuesta) {
        int status = respuesta.getStatus();
        if (status < 200 || status >= 300) return false;
        byte[] cuerpo = respuesta.getContentAsByteArray();
        if (cuerpo.length == 0) return false;
        try {
            JsonElement json = JsonParser.parseString(new String(cuerpo, StandardCharsets.UTF_8));
            if (!json.isJsonObject()) return false;
            JsonObject objeto = json.getAsJsonObject();
            for (String clave : CONTADORES) {
                JsonElement contador = objeto.get(clave);
                if (contador != null && !contador.isJsonNull()) {
                    return contador.getAsLong() == 0L;
                }
            }
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static final class EnvolturaExcepcion extends RuntimeException {
        EnvolturaExcepcion(Exception causa) {
            super(causa);
        }
    }
}
