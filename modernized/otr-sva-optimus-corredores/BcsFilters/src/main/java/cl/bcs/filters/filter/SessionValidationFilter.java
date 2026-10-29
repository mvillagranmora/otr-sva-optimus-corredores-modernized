package cl.bcs.filters.filter;

import cl.bcs.filters.domain.Sesion;
import cl.bcs.filters.port.out.SesionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Modernized replacement for the legacy {@code SesionFilter}.
 *
 * Checks that every request to a protected URL carries a valid X-SESSION-TOKEN header.
 *
 * Security fixes applied:
 *   RULE-005 — exceptions now deny access (fail-closed); the legacy granted access on any exception.
 *   RULE-025 — COR-channel sessions expire after {@code cor.session.timeout.minutes} (default 30).
 */
@Component
@Order(1)
public class SessionValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionValidationFilter.class);

    /** URLs that do not require a valid session token. */
    static final List<String> NO_AUTH_URLS = List.of(
            "/PLA/ValidaClaveProvisoria",
            "/PLA/ActualizarClave",
            "/PLA/RecuperacionClave",
            "/PLA/CrearSesion",
            "/PLA/bootstrap",
            "/PLA/LoginCliente",
            "/PLA/SolicitudRecuperacionClave",
            "/PLA/ConfirmarCambioClave",
            "/PLA/CallBatch",
            "/PLA/WebStatic",
            "/PLA/RepoStatic",
            "/PORTAL/validate",
            "/PLA/CallWebsocket",
            "/PLA/AuditoriaWS",
            "/PLA/Configuraciones",
            "/PLA/Ping",
            "/PLA/SSOInit",
            "/PLA/SSOLogon",
            "/PLA/SSOLogonBofa",
            "/PLA/SSOLogonGoogle",
            "/PLA/SSOLogonLocalGoogle",
            "/PLA/SSOLogout"
    );

    private final SesionRepository sesionRepository;
    private final int corTimeoutMinutes;

    public SessionValidationFilter(
            SesionRepository sesionRepository,
            @Value("${cor.session.timeout.minutes:30}") int corTimeoutMinutes) {
        this.sesionRepository = sesionRepository;
        this.corTimeoutMinutes = corTimeoutMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (NO_AUTH_URLS.contains(uri) || uri.startsWith("/PLA/WebStatic")) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("X-SESSION-TOKEN");
        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            return;
        }

        // RULE-005 FIX: any exception → deny access (fail-closed).
        // Legacy catch-block called chain.doFilter() → granted access on any DB error.
        try {
            Optional<Sesion> opt = sesionRepository.findByToken(token);
            if (opt.isEmpty() || !opt.get().tokenValido()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
                return;
            }

            Sesion sesion = opt.get();

            // RULE-025 FIX: enforce configurable timeout for COR channel.
            // Legacy created COR sessions with no expiry.
            if ("COR".equalsIgnoreCase(sesion.canal())) {
                LocalDateTime expiry = sesion.fechaCreacion().plusMinutes(corTimeoutMinutes);
                if (LocalDateTime.now().isAfter(expiry)) {
                    log.warn("COR session expired: token={}, created={}, timeout={}m",
                            token, sesion.fechaCreacion(), corTimeoutMinutes);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session Expired");
                    return;
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            // RULE-005: fail-closed — never grant access when an exception occurs.
            log.error("SessionValidationFilter: unexpected error for token={}", token, e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
        }
    }
}
