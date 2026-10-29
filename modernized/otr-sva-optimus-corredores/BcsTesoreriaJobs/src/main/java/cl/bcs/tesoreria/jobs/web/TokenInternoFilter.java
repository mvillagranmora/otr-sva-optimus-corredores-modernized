package cl.bcs.tesoreria.jobs.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Service-to-service authentication of {@code /internal/**}: the Job Manager sends the shared secret in
 * {@code X-Internal-Token}; anything else gets 401. Copy of the BcsTesoreriaServicios filter.
 *
 * <p>Fails closed: with no secret configured ({@code services.internal-token} / {@code INTERNAL_TOKEN}) every request
 * is rejected. The comparison is constant time. Only the exact health probes are public, after removing path
 * parameters and repeated slashes from the raw URI (an allow-list, so {@code /internal;x=1/job-call} is protected).
 */
@Component
public class TokenInternoFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Internal-Token";

    private static final Set<String> PUBLICAS = Set.of("/actuator/health", "/actuator/info");

    private final byte[] esperado;

    public TokenInternoFilter(@Value("${services.internal-token:}") String token) {
        this.esperado = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLICAS.contains(normalizar(request.getRequestURI()));
    }

    static String normalizar(String uri) {
        String p = uri == null ? "" : uri;
        p = p.replaceAll(";[^/]*", "");
        return p.replaceAll("/{2,}", "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibido = request.getHeader(CABECERA);
        boolean valido = esperado.length > 0 && recibido != null
                && MessageDigest.isEqual(esperado, recibido.getBytes(StandardCharsets.UTF_8));
        if (!valido) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "internal token required");
            return;
        }
        chain.doFilter(request, response);
    }
}
