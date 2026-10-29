package cl.bcs.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Route table for the BcsGateway reverse proxy.
 * Configured under bcs.gateway in application.yml.
 *
 * <ul>
 *   <li>{@code routes[].enabled} — opt-in, default false. A disabled route is skipped as if it did not
 *       exist (the request goes to TomEE).</li>
 *   <li>{@code routes[].no-legacy-fallback} — default false. true: when the service cannot serve the request the
 *       client gets 503 instead of a replay in TomEE (routes whose legacy equivalent must not run, e.g. the login).</li>
 *   <li>{@code enabled-routes} — comma list of route ids that are forced ON (ops override, e.g.
 *       {@code BCS_GATEWAY_ENABLED_ROUTES=bcs-tesoreria-servicios}) without editing the table.</li>
 *   <li>{@code max-buffered-body-bytes} — request bodies up to this size are buffered once so that the
 *       same bytes can be replayed to TomEE (default 10 MB). Larger / unknown-length bodies are
 *       streamed and never replayed.</li>
 *   <li>{@code cors-allowed-origins} — comma list of origins allowed to call the gateway from a browser. Empty (default): no CORS headers, which is
 *       right when the UI is served from the same origin as the gateway. Never a wildcard.</li>
 *   <li>{@code trust-forwarded-for} — default false: the {@code X-Forwarded-For} the client sends is discarded and replaced by the address of the
 *       connection. true behind a trusted load balancer (EKS ALB): the received chain is kept and the connection address is appended.</li>
 *   <li>{@code response-timeout-seconds} — upstream response (read) timeout (default 30).</li>
 * </ul>
 */
@ConfigurationProperties("bcs.gateway")
public record GatewayProperties(
        String legacyFallbackUri,
        List<RouteDefinition> routes,
        List<String> enabledRoutes,
        long maxBufferedBodyBytes,
        int responseTimeoutSeconds,
        List<String> corsAllowedOrigins,
        boolean trustForwardedFor
) {
    public static final long DEFAULT_MAX_BUFFERED_BODY_BYTES = 10L * 1024 * 1024;
    public static final int DEFAULT_RESPONSE_TIMEOUT_SECONDS = 30;

    public GatewayProperties {
        corsAllowedOrigins = corsAllowedOrigins == null ? List.of()
                : corsAllowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        enabledRoutes = enabledRoutes == null ? List.of()
                : enabledRoutes.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (maxBufferedBodyBytes <= 0) maxBufferedBodyBytes = DEFAULT_MAX_BUFFERED_BODY_BYTES;
        if (responseTimeoutSeconds <= 0) responseTimeoutSeconds = DEFAULT_RESPONSE_TIMEOUT_SECONDS;
    }

    /** A route receives traffic only if its own flag is true or its id is in bcs.gateway.enabled-routes. */
    public boolean isActive(RouteDefinition r) {
        return r.enabled() || (r.id() != null && enabledRoutes.contains(r.id()));
    }

    public record RouteDefinition(
            String id,
            String uri,
            List<String> paths,
            boolean enabled,
            boolean noLegacyFallback
    ) {
        /** Default true: a request this route cannot serve (503, no connection, open circuit) is replayed in TomEE. */
        public boolean allowsLegacyFallback() {
            return !noLegacyFallback;
        }
    }
}
