package cl.bcs.plataforma.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Set;

/**
 * Service-to-service authentication of {@code /internal/**} (step 5b). The jobs send the shared secret in
 * {@code X-Internal-Token}; anything else gets 401. Mirrors BcsTesoreriaJobs {@code TokenInternoFilter}.
 *
 * <p>Scope: ONLY paths that, once normalized, are under {@code /internal}. Every other route of this module
 * ({@code /call}, {@code /CrearSesion}, ...) keeps its own behavior: this filter does not touch it and no session is
 * involved in the internal route.
 *
 * <p>Fails closed: with no secret configured ({@code services.internal-token} / {@code INTERNAL_TOKEN}) every request under
 * {@code /internal} is rejected. The comparison is constant time. Public paths (exact allow-list): the actuator health probe.
 * The path is percent-decoded, stripped of {@code ;params}, collapsed of repeated slashes and resolved of dot segments before
 * deciding, and the servlet path the container resolved is checked too, so {@code /internal//java-call},
 * {@code /internal;x=1/java-call}, {@code /%69nternal/java-call} and {@code /a/../internal/java-call} are all protected.
 */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Internal-Token";

    private static final Set<String> PUBLICAS = Set.of("/actuator/health");

    private final byte[] esperado;

    public InternalTokenFilter(@Value("${services.internal-token:}") String token) {
        this.esperado = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String raw = normalizar(request.getRequestURI());
        String servlet = normalizar((request.getServletPath() == null ? "" : request.getServletPath())
                + (request.getPathInfo() == null ? "" : request.getPathInfo()));
        return !(bajoInternal(raw) || bajoInternal(servlet));
    }

    private static boolean bajoInternal(String p) {
        String m = p.toLowerCase(Locale.ROOT);
        return !PUBLICAS.contains(p) && (m.equals("/internal") || m.startsWith("/internal/"));
    }

    static String normalizar(String uri) {
        String p = uri == null ? "" : uri;
        p = p.replaceAll(";[^/]*", "");
        try {
            p = URLDecoder.decode(p.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "/internal/malformed"; // undecodable: fail closed
        }
        p = p.replaceAll(";[^/]*", "").replace('\\', '/').replaceAll("/{2,}", "/");
        java.util.Deque<String> out = new java.util.ArrayDeque<>();
        for (String s : p.split("/")) {
            if (s.isEmpty() || s.equals(".")) continue;
            if (s.equals("..")) { if (!out.isEmpty()) out.removeLast(); continue; }
            out.addLast(s);
        }
        return "/" + String.join("/", out);
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
