package cl.bcs.fondomutuo.adapter.in.javacall;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The unit of work of a JavaCall request (C1): ONE SPRING TRANSACTION on the module {@code DataSource}, not a raw {@code Connection} held by the filter.
 * Everything that runs inside the request (the handler, the JDBC port adapters through {@code DataSourceUtils}, and later Tesoreria's in-process library with
 * its {@code @Transactional}, REQUIRED) joins it. Rule of legacy {@code CallServlet:441-456,495}: COMMIT only for a 2xx whose JSON carries
 * {@code P_OUT_ERRORESCONTADOR} (or {@code P_OUT_ERRORCOUNTER}) equal to 0; ROLLBACK when the counter is absent or greater than 0, and for every other status
 * (the 503 of a gate or of an unserved class, the generic 500 of an exception). The response is held until the commit decision and sent after it; if the
 * commit itself fails the client gets 500.
 *
 * <p><b>What this filter does NOT guarantee</b> (architecture review, Step 6): (1) a 2xx whose counter is greater than 0 (or absent) is ROLLED BACK, so a
 * 2xx alone does not mean "committed"; the client must read the counter. (2) A non-2xx answer does NOT always leave nothing committed: the lock of
 * {@code CrearSolicitudAporteRescateNormal} runs {@code CREATE LOCAL TEMPORARY TABLE} / {@code DROP TABLE} on the connection of this transaction and on HANA
 * those DDL implicitly COMMIT the open transaction (measured, HANA_DDL_COMMIT_HALLAZGO; accepted legacy behaviour, option A), and the HTTP write
 * collaborators commit in their own module. Whatever was written before such a point stays committed even if this filter rolls back. That is why a
 * 503 is no longer answered once {@code EscrituraConfirmada} is set (generic 500 instead: the router does not repeat). (3) The autonomous release of
 * the lock (procedure with AUTONOMOUS TRANSACTION) runs BEFORE the commit decision of this filter, the same window the legacy had (M1): a crash
 * between the release and the commit leaves the request unlocked and uncommitted.
 *
 * <p>M2: the transaction has a timeout ({@code services.javacall.transaction-timeout-seconds}, default 120 s). It is enforced by Spring at the next
 * JDBC statement (statement timeout / deadline check), it does not interrupt an HTTP call: those are bounded by the read timeout of {@code RouterClient}.
 * The manager is {@code TransaccionConfig}'s (global-rollback-on-participation-failure = false, C1b).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JavaCallUnitOfWorkFilter extends OncePerRequestFilter {

    public static final String RUTA = "/internal/java-call";
    private static final String[] CONTADORES = {"P_OUT_ERRORCOUNTER", "P_OUT_ERRORESCONTADOR"};
    private static final Logger log = LoggerFactory.getLogger(JavaCallUnitOfWorkFilter.class);

    private final TransactionTemplate transaccion;
    /** Same manager and timeout, READ ONLY: for the eight UI reads (architecture review F3 W1, H2; {@link JavaCallDispatcher#CLASES_LECTURA}). */
    private final TransactionTemplate transaccionLectura;

    /** Default of the transaction timeout (seconds); property {@code services.javacall.transaction-timeout-seconds}. */
    public static final int TIMEOUT_POR_DEFECTO = 120;

    @org.springframework.beans.factory.annotation.Autowired
    public JavaCallUnitOfWorkFilter(PlatformTransactionManager transactionManager,
            @org.springframework.beans.factory.annotation.Value("${services.javacall.transaction-timeout-seconds:120}") int timeoutSegundos) {
        this.transaccion = new TransactionTemplate(transactionManager);
        this.transaccion.setTimeout(timeoutSegundos);
        this.transaccionLectura = new TransactionTemplate(transactionManager);
        this.transaccionLectura.setTimeout(timeoutSegundos);
        this.transaccionLectura.setReadOnly(true);
    }

    public JavaCallUnitOfWorkFilter(PlatformTransactionManager transactionManager) {
        this(transactionManager, TIMEOUT_POR_DEFECTO);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = TokenInternoFilter.normalizar(request.getRequestURI());
        return !(RUTA.equals(ruta) || ruta.startsWith("/internal/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper respuesta = new ContentCachingResponseWrapper(response);
        try {
            // the class was extracted by JavaCallLimitesFilter (it runs first); without it (never in production) the transaction is the read-write one
            boolean lectura = request.getAttribute(JavaCallLimitesFilter.ATRIBUTO_CLASE) instanceof String clase && JavaCallDispatcher.CLASES_LECTURA.contains(clase);
            (lectura ? transaccionLectura : transaccion).executeWithoutResult(status -> {
                try {
                    chain.doFilter(request, respuesta);
                } catch (IOException | ServletException e) {
                    throw new EnvolturaExcepcion(e);
                }
                if (!confirmable(respuesta.getStatus(), respuesta.getContentAsByteArray())) {
                    status.setRollbackOnly();
                }
            });
        } catch (EnvolturaExcepcion e) {
            if (e.getCause() instanceof IOException io) throw io;
            throw (ServletException) e.getCause();
        } catch (UnexpectedRollbackException e) {
            String correlationId = UUID.randomUUID().toString();
            log.error("JavaCall {} rolled back after reporting success: correlationId={}", RUTA, correlationId, e);
            reemplazarPorError(respuesta, correlationId);
        } catch (RuntimeException e) {
            // failure to open the transaction (database down), commit failure or an exception thrown after the handler answered: nothing was
            // confirmed, the client must not see a success nor any internal text (SEC-003): generic 500 with correlationId
            String correlationId = UUID.randomUUID().toString();
            log.error("JavaCall {} unit of work failed: correlationId={}", RUTA, correlationId, e);
            reemplazarPorError(respuesta, correlationId);
        }
        respuesta.copyBodyToResponse();
    }

    private static void reemplazarPorError(ContentCachingResponseWrapper respuesta, String correlationId) throws IOException {
        respuesta.resetBuffer();
        respuesta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.getOutputStream().write(("{\"error\":\"internal error\",\"correlationId\":\"" + correlationId + "\"}").getBytes(StandardCharsets.UTF_8));
    }

    /** Legacy rule: commit only for a 2xx whose JSON object carries an error counter equal to 0 (no counter = rollback). */
    public static boolean confirmable(int status, byte[] cuerpo) {
        if (status < 200 || status >= 300 || cuerpo == null || cuerpo.length == 0) return false;
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
