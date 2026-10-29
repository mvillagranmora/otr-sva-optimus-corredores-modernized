package cl.bcs.reports.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Service-to-service authentication of the sidecar (same pattern as the other modules' TokenInternoFilter, javax API
 * because this module is Spring Boot 2.7 / Java 11): the caller sends the shared secret in {@code X-Internal-Token}.
 * Allow-list: everything is protected except the exact public paths below, after removing path parameters and
 * repeated slashes. Fails closed: with no secret configured ({@code services.internal-token} / env INTERNAL_TOKEN)
 * every protected call is rejected. Constant-time comparison. The 401 body is the servlet's "401 - NOT AUTHORIZED".
 *
 * <p>OPEN FOLLOW-UP: {@code /reports/generate} is still public because its consumers (BcsUtils
 * CrystalReportsRestClient) do not send the token yet; remove it from PUBLICAS once they do.
 */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Token";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalTokenFilter.class);

    private static final Set<String> PUBLICAS = Set.of(
            "/reports/health", "/actuator/health",
            "/reports/generate");   // TEMPORARY, see class comment

    private final byte[] expected;

    public InternalTokenFilter(@Value("${services.internal-token:}") String token) {
        this.expected = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLICAS.contains(normalize(request.getRequestURI()));
    }

    static String normalize(String uri) {
        String p = uri == null ? "" : uri;
        p = p.replaceAll(";[^/]*", "");
        return p.replaceAll("/{2,}", "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String received = request.getHeader(HEADER);
        boolean valid = expected.length > 0 && received != null
                && MessageDigest.isEqual(expected, received.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            // never the token (sent or expected); only who and what, with the control characters of the path removed
            log.warn("rejected request: {} {} from {} ({})", request.getMethod(),
                    Reply.safeFileName(normalize(request.getRequestURI())), request.getRemoteAddr(),
                    received == null ? "no token" : "invalid token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().print("401 - NOT AUTHORIZED");
            response.getOutputStream().flush();
            return;
        }
        chain.doFilter(request, response);
    }
}
