package cl.bcs.cuentainversion.infrastructure.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Service-to-service authentication of {@code /internal/**} (architecture review H1, ARQ-H5): the platform router
 * ({@code BcsPlataformaWeb}) sends the shared secret in {@code X-Internal-Token}; anything else gets 401.
 *
 * <p>Fails closed: when no secret is configured ({@code services.internal-token} / {@code INTERNAL_TOKEN}) every request to
 * {@code /internal/**} is rejected, so a deployment that forgot the variable does not expose the endpoint. The comparison is
 * constant time.
 */
@Component
public class TokenInternoFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Internal-Token";

    private final byte[] esperado;

    public TokenInternoFilter(@Value("${services.internal-token:}") String token) {
        this.esperado = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    /** The only paths that do not need the token (health probes); everything else, including any variant of /internal, does. */
    private static final java.util.Set<String> PUBLICAS = java.util.Set.of("/actuator/health", "/actuator/info");

    /**
     * Allow-list instead of a block-list (architecture review H1): a request such as {@code /internal;x=1/java-call} or
     * {@code //internal/java-call} does not start with {@code /internal/} but still reaches the controller, so the filter protects
     * every path except the exact public ones, after removing path parameters and repeated slashes from the raw URI.
     */
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
