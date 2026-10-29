package cl.bcs.plataforma.adapter.in.javacall;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Service-to-service authentication (DEV-A5; same pattern as BcsTesoreriaServicios, ARQ-H3): the platform router sends the shared secret
 * in {@code X-Internal-Token}; anything else gets 401. FAILS CLOSED: with no secret configured ({@code services.internal-token} /
 * {@code INTERNAL_TOKEN}) every request is rejected. It is an allow-list of public paths, not a block-list of {@code /internal}: a
 * request such as {@code //internal/java-call} or {@code /internal;x=1/java-call} still reaches the controller, so everything except
 * the exact public paths needs the token after path parameters and repeated slashes are removed. Constant-time comparison.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
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

    /** Removes {@code ;path-parameters} and collapses repeated slashes of the RAW request URI. */
    public static String normalizar(String uri) {
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
