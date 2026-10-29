package cl.bcs.filters.filter;

import cl.bcs.filters.port.out.ServicioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Modernized replacement for the legacy {@code ServiceFilter}.
 *
 * Validates that the caller has access to the requested service (query param {@code t}).
 *
 * Security fixes applied:
 *   RULE-004 — channel bypass: the legacy condition {@code ch != null} bypassed ALL
 *              authorisation whenever any channel was supplied (even unknown ones like "HACKER").
 *              The fix uses {@code noAuthChannels.contains(ch)} so only WS, CLI and AGE bypass.
 *   RULE-005 — fail-closed: exceptions now return 401 instead of calling chain.doFilter().
 */
@Component
@Order(2)
public class ServiceAccessFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ServiceAccessFilter.class);

    /** Same whitelist as {@link SessionValidationFilter} — no service-access check needed. */
    private static final List<String> NO_AUTH_URLS = SessionValidationFilter.NO_AUTH_URLS;

    /** Service IDs that are always publicly accessible without access-control validation. */
    private static final List<String> NO_AUTH_SERVICES = List.of(
            "700729", "600017", "171721", "1381", "14", "1556", "384");

    /**
     * RULE-004 FIX: Only these explicit channel codes bypass service-access validation.
     *
     * Legacy bug: the condition was {@code ch != null} which meant ANY non-null channel value
     * (including attacker-supplied arbitrary strings) would skip the entire access check.
     * Correct behaviour: only the three trusted integration channels (WS, CLI, AGE) bypass.
     */
    static final List<String> NO_AUTH_CHANNELS = List.of("WS", "CLI", "AGE");

    private final ServicioRepository servicioRepository;

    public ServiceAccessFilter(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String t = getParameter(request, "t");
        String ch = getParameter(request, "ch");

        // RULE-004 FIX: `ch != null` replaced with `NO_AUTH_CHANNELS.contains(ch)`.
        // This means: a request with ch=HACKER no longer bypasses access control.
        // Note: null ch means no channel supplied — does not match any whitelisted channel.
        // t == null must be evaluated before NO_AUTH_SERVICES.contains(t) to avoid NPE —
        // List.of() (immutable) throws NullPointerException on contains(null).
        if (NO_AUTH_URLS.contains(uri)
                || uri.startsWith("/PLA/WebStatic")
                || t == null
                || NO_AUTH_SERVICES.contains(t)
                || (ch != null && NO_AUTH_CHANNELS.contains(ch))) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("X-SESSION-TOKEN");
        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            return;
        }

        // RULE-005 FIX: any exception → deny access (fail-closed).
        // Legacy catch-block called chain.doFilter() — granted access on any DB error.
        try {
            Optional<String> validaConfig =
                    servicioRepository.getConfiguracion("PLA.TipoValidaServicio");

            // "NADIE" → no access control, allow everyone
            if (validaConfig.isPresent() && "NADIE".equalsIgnoreCase(validaConfig.get())) {
                chain.doFilter(request, response);
                return;
            }

            // "LISTA" → only validate for users on the list
            if (validaConfig.isPresent() && "LISTA".equalsIgnoreCase(validaConfig.get())) {
                Optional<String> listaOpt =
                        servicioRepository.getConfiguracion("PLA.ListaValidaServicio");
                String codigoUsuario = servicioRepository.getCodigoUsuario(token);
                if (listaOpt.isPresent()) {
                    List<String> lista = Arrays.asList(
                            listaOpt.get().replaceAll("\\s", "").split(","));
                    if (!lista.contains(codigoUsuario)) {
                        // User is not on the monitoring list → allow through
                        chain.doFilter(request, response);
                        return;
                    }
                }
            }

            // "TODOS" or default → validate service access per user
            Optional<String> validaServicio = servicioRepository.getValidaAcceso(t);
            if (validaServicio.isPresent() && "S".equalsIgnoreCase(validaServicio.get())) {
                int count = servicioRepository.countAccesoServicio(token, t);
                if (count > 0) {
                    chain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized Service");
                }
            } else {
                // Service does not require access validation
                chain.doFilter(request, response);
            }

        } catch (Exception e) {
            // RULE-005: fail-closed — never grant access on exception.
            log.error("ServiceAccessFilter: unexpected error for token={} service={}", token, t, e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Error");
        }
    }

    /** Extracts a single query-string parameter by name (same logic as legacy). */
    static String getParameter(HttpServletRequest request, String paramName) {
        String qs = request.getQueryString();
        if (qs == null) return null;
        for (String part : qs.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return kv[1];
            }
        }
        return null;
    }
}
