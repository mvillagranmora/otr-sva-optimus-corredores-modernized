package cl.bcs.fondomutuo.adapter.in.javacall;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Deque;
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
 * Service-to-service authentication (DEV-F6): the platform router sends the shared secret in {@code X-Internal-Token}; anything else gets 401.
 * FAILS CLOSED: with no secret configured ({@code services.internal-token} / {@code INTERNAL_TOKEN}) every request is rejected, public ones excepted.
 * It is an allow-list of public paths, not a block-list of {@code /internal}: the RAW request URI is normalised first ({@link #normalizar}:
 * {@code ;path-params}, repeated slashes, {@code %xx} escapes (decoded until stable), {@code .} and {@code ..} segments), so {@code //internal/java-call},
 * {@code /internal;x=1/java-call}, {@code /internal/%6Aava-call} or {@code /x/../internal/java-call} all need the token, and a path that cannot be
 * normalised safely (malformed escape, backslash, control character, climbing above the root) is never public. Constant-time comparison. No state.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TokenInternoFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Internal-Token";
    private static final Set<String> PUBLICAS = Set.of("/actuator/health");   // SEC-007: only health is public ("info" removed)
    /** What {@link #normalizar} returns for a path it refuses to interpret; it is not a public path and not the java-call path. */
    static final String INSEGURA = "/\u0000unsafe";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenInternoFilter.class);

    private final byte[] esperado;

    public TokenInternoFilter(@Value("${services.internal-token:}") String token) {
        this.esperado = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLICAS.contains(normalizar(request.getRequestURI()));
    }

    /** Canonical form of a raw request URI (path only), or {@link #INSEGURA}. */
    public static String normalizar(String uri) {
        String p = uri == null ? "" : uri;
        try {
            for (int i = 0; i < 4; i++) {
                String antes = p;
                p = p.replaceAll(";[^/]*", "");
                if (p.indexOf('%') >= 0) {
                    p = URLDecoder.decode(p.replace("+", "%2B"), StandardCharsets.UTF_8);
                }
                if (p.equals(antes)) break;
            }
        } catch (IllegalArgumentException e) {
            return INSEGURA;
        }
        if (p.indexOf('%') >= 0 || p.indexOf('\\') >= 0 || p.chars().anyMatch(c -> c < 0x20 || c == 0x7f)) {
            return INSEGURA;
        }
        Deque<String> segmentos = new ArrayDeque<>();
        for (String s : p.split("/")) {
            if (s.isEmpty() || s.equals(".")) continue;
            if (s.equals("..")) {
                if (segmentos.isEmpty()) return INSEGURA;
                segmentos.removeLast();
            } else {
                segmentos.addLast(s);
            }
        }
        return "/" + String.join("/", segmentos);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibido = request.getHeader(CABECERA);
        boolean valido = esperado.length > 0 && recibido != null
                && MessageDigest.isEqual(esperado, recibido.getBytes(StandardCharsets.UTF_8));
        if (!valido) {
            // L5: who knocked, never what they sent (the token and the raw URI are not logged; the path is the normalised one, free of control characters)
            log.warn("Internal token rejected: remote={} path={} tokenHeaderPresent={}", request.getRemoteAddr(), normalizar(request.getRequestURI()),
                    recibido != null);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "internal token required");
            return;
        }
        chain.doFilter(request, response);
    }
}
