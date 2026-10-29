package cl.bcs.plataforma.adapter.in.javacall;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
 * One database transaction (one connection) per JavaCall request: the legacy unit of work of {@code CallServlet}
 * ({@code CallServlet.java:441-456,495}), as DEV-21 of Contabilidad/Tesoreria. COMMIT only for a 2xx response whose JSON carries
 * {@code P_OUT_ERRORESCONTADOR} (or {@code P_OUT_ERRORCOUNTER}) equal to 0; ROLLBACK when the counter is absent or greater than 0,
 * for any other status (a 503 fallback or the generic 500 of an exception included). That is exactly what the oracle records as
 * {@code decisionServlet} (COMMIT / ROLLBACK / ROLLBACK_HTTP500).
 *
 * <p>The path is compared after {@link TokenInternoFilter#normalizar}, so {@code //internal/java-call} and
 * {@code /internal;x=1/java-call} are inside the unit of work too (the Contabilidad version used {@code endsWith}).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JavaCallUnitOfWorkFilter extends OncePerRequestFilter {

    public static final String RUTA = "/internal/java-call";
    private static final String[] CONTADORES = {"P_OUT_ERRORCOUNTER", "P_OUT_ERRORESCONTADOR"};
    private static final Logger log = LoggerFactory.getLogger(JavaCallUnitOfWorkFilter.class);

    private final TransactionTemplate transaccion;

    public JavaCallUnitOfWorkFilter(PlatformTransactionManager transactionManager) {
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !RUTA.equals(TokenInternoFilter.normalizar(request.getRequestURI()));
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
                if (!confirmable(respuesta.getStatus(), respuesta.getContentAsByteArray())) {
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
            respuesta.getOutputStream().write("{\"error\":\"transaction rolled back\"}".getBytes(StandardCharsets.UTF_8));
        }
        respuesta.copyBodyToResponse();
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
