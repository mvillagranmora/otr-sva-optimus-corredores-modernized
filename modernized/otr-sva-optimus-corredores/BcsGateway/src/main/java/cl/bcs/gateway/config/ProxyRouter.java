package cl.bcs.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Reverse-proxy WebFilter that routes incoming requests to upstream services based on
 * the bcs.gateway.routes path table (only routes that are ENABLED: opt-in, default off).
 * A request with no active route goes straight to the legacy TomEE.
 *
 * A request that matched a route is replayed in TomEE ONLY when the new service provably did not
 * execute it: (i) it answered HTTP 503, (ii) the connection could not be established, (iii) the
 * circuit of that route is open (the call is not even attempted). Any other 5xx, a response timeout
 * or a failure after the request was sent is propagated to the client (never replayed) and counted
 * in the circuit breaker.
 *
 * Replaces spring-cloud-starter-gateway (incompatible with Spring Boot 4.x:
 * spring-cloud-commons 2025.0.0 references removed class NettyWebServerFactoryCustomizer).
 */
@Configuration
public class ProxyRouter {

    private static final Logger log = LoggerFactory.getLogger(ProxyRouter.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    // RFC 7230 §6.1 hop-by-hop headers — must not be forwarded by a proxy
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade"
    );

    @Bean
    public WebClient proxyWebClient(GatewayProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                .responseTimeout(Duration.ofSeconds(props.responseTimeoutSeconds()));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .recordException(ex -> true)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public WebFilter reverseProxyFilter(WebClient webClient,
                                        GatewayProperties props,
                                        CircuitBreakerRegistry cbRegistry) {
        return (exchange, chain) -> route(exchange, chain, webClient, props, cbRegistry);
    }

    private Mono<Void> route(ServerWebExchange exchange,
                             WebFilterChain chain,
                             WebClient webClient,
                             GatewayProperties props,
                             CircuitBreakerRegistry cbRegistry) {
        String path = publicToInternal(exchange.getRequest().getPath().value());

        // Actuator and fallback diagnostic endpoint are handled locally — skip proxy
        if (path.startsWith("/actuator") || path.startsWith("/fallback/")) {
            return chain.filter(exchange);
        }

        // First matching ACTIVE route. A disabled route is skipped as if it did not exist (opt-in).
        GatewayProperties.RouteDefinition route = null;
        if (props.routes() != null) {
            for (var r : props.routes()) {
                if (!props.isActive(r)) {
                    continue;
                }
                if (r.paths() != null && r.paths().stream().anyMatch(p -> MATCHER.match(p, path))) {
                    route = r;
                    break;
                }
            }
        }

        if (route == null) {
            // No active route — proxy directly to TomEE (legacy catch-all), body streamed as is
            log.debug("No active route for {} {} — forwarding to TomEE", exchange.getRequest().getMethod(), path);
            return forwardToLegacy(exchange, webClient, props, null);
        }

        final String upstreamUri = route.uri();
        final String cbName = route.id();
        CircuitBreaker cb = cbRegistry.circuitBreaker(cbName);

        log.debug("Routing {} {} → {} (cb={})", exchange.getRequest().getMethod(), path, upstreamUri, cbName);

        final boolean legacyFallback = route.allowsLegacyFallback();
        return bufferBody(exchange, props.maxBufferedBodyBytes())
                .flatMap(body -> routeToUpstream(exchange, webClient, props, upstreamUri, cbName, cb, body, legacyFallback))
                .onErrorResume(BodyTooLargeException.class, e ->
                        writeError(exchange, HttpStatus.CONTENT_TOO_LARGE, "Request body exceeds the gateway buffer limit"));
    }

    /**
     * Safe fallback: TomEE gets the request only if the new service provably did not execute it, i.e.
     * (i) it answered 503, (ii) the connection could not be established, (iii) the circuit is open
     * (the call is not even attempted). Everything else is propagated and counted in the breaker.
     */
    private Mono<Void> routeToUpstream(ServerWebExchange exchange, WebClient webClient, GatewayProperties props,
                                       String upstreamUri, String cbName, CircuitBreaker cb, BufferedBody body,
                                       boolean legacyFallback) {
        final String path = exchange.getRequest().getPath().value();

        if (!cb.tryAcquirePermission()) {
            if (!legacyFallback) {
                log.warn("Circuit open (cb={} path={}) — upstream not called, route has no TomEE fallback — 503", cbName, path);
                return writeError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Upstream unavailable");
            }
            log.warn("Circuit open (cb={} path={}) — upstream not called, forwarding to TomEE", cbName, path);
            return forwardToLegacy(exchange, webClient, props, body.bytes());
        }

        final AtomicBoolean recorded = new AtomicBoolean();
        final long start = System.nanoTime();

        Mono<Boolean> attempt = send(exchange, webClient, upstreamUri, true, props.trustForwardedFor(), body.bytes(), upstream -> {
            HttpStatusCode status = upstream.statusCode();
            if (status.value() == 503 && body.replayable() && legacyFallback) {
                // "not migrated / unavailable": the service did not run it — safe to replay in TomEE
                record(cb, recorded, start, new UpstreamServerException(cbName, 503));
                return upstream.releaseBody().thenReturn(Boolean.TRUE);
            }
            if (status.is5xxServerError()) {
                // a real answer: reaches the client untouched, and counts as a breaker failure
                record(cb, recorded, start, new UpstreamServerException(cbName, status.value()));
            } else if (recorded.compareAndSet(false, true)) {
                cb.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            }
            return writeUpstream(exchange, upstream).thenReturn(Boolean.FALSE);
        });

        return attempt
                .onErrorResume(ex -> {
                    record(cb, recorded, start, ex);
                    if (isConnectFailure(ex)) {
                        if (body.replayable() && legacyFallback) {
                            log.warn("Connection to upstream failed (cb={} path={}): {} — forwarding to TomEE",
                                    cbName, path, ex.getMessage());
                            return Mono.just(Boolean.TRUE);
                        }
                        log.warn("Connection to upstream failed (cb={} path={}), body not replayable or route without TomEE fallback — 503",
                                cbName, path);
                        return writeError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Upstream unavailable")
                                .thenReturn(Boolean.FALSE);
                    }
                    if (exchange.getResponse().isCommitted()) {
                        return Mono.error(ex);
                    }
                    if (isReadTimeout(ex)) {
                        log.warn("Upstream response timeout (cb={} path={}) — 504, NO fallback", cbName, path);
                        return writeError(exchange, HttpStatus.GATEWAY_TIMEOUT, "Upstream response timeout")
                                .thenReturn(Boolean.FALSE);
                    }
                    log.warn("Upstream failed after the request was sent (cb={} path={}): {} — 502, NO fallback",
                            cbName, path, ex.toString());
                    return writeError(exchange, HttpStatus.BAD_GATEWAY, "Upstream failed")
                            .thenReturn(Boolean.FALSE);
                })
                .doOnCancel(() -> {
                    if (recorded.compareAndSet(false, true)) {
                        cb.releasePermission();
                    }
                })
                .flatMap(fallback -> fallback
                        ? forwardToLegacy(exchange, webClient, props, body.bytes())
                        : Mono.<Void>empty());
    }

    private static void record(CircuitBreaker cb, AtomicBoolean recorded, long start, Throwable ex) {
        if (recorded.compareAndSet(false, true)) {
            cb.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, ex);
        }
    }

    /** A request body held in memory (replayable) or left as the original single-use stream. */
    private record BufferedBody(byte[] bytes) {
        boolean replayable() {
            return bytes != null;
        }
    }

    private static final class BodyTooLargeException extends RuntimeException {
        BodyTooLargeException() {
            super("Request body exceeds the buffer limit", null, false, false);
        }
    }

    /**
     * Buffers the request body once when it is small and its length is known, so the same bytes can go to
     * the upstream and, if needed, to TomEE. A body larger than the limit or without a known length stays
     * a stream (single subscription) and is never replayed.
     */
    private static Mono<BufferedBody> bufferBody(ServerWebExchange exchange, long max) {
        HttpHeaders h = exchange.getRequest().getHeaders();
        long len = h.getContentLength();
        boolean chunked = h.containsHeader(HttpHeaders.TRANSFER_ENCODING);
        if (len == 0 || (len < 0 && !chunked)) {
            return Mono.just(new BufferedBody(new byte[0]));
        }
        if (len < 0 || len > max) {
            return Mono.just(new BufferedBody(null));
        }
        return DataBufferUtils.join(exchange.getRequest().getBody(), (int) max)
                .map(db -> {
                    try {
                        byte[] b = new byte[db.readableByteCount()];
                        db.read(b);
                        return new BufferedBody(b);
                    } finally {
                        DataBufferUtils.release(db);
                    }
                })
                .defaultIfEmpty(new BufferedBody(new byte[0]))
                .onErrorMap(DataBufferLimitException.class, e -> new BodyTooLargeException());
    }

    /** Proxies to TomEE: whatever TomEE answers (any status, headers, body) reaches the client as is. */
    private static Mono<Void> forwardToLegacy(ServerWebExchange exchange, WebClient webClient,
                                              GatewayProperties props, byte[] bytes) {
        return send(exchange, webClient, props.legacyFallbackUri(), false, props.trustForwardedFor(), bytes,
                upstream -> writeUpstream(exchange, upstream).thenReturn(Boolean.FALSE))
                .then();
    }

    /** bytes == null streams the original (single-subscription) request body. */
    private static Mono<Boolean> send(ServerWebExchange exchange, WebClient webClient, String upstreamBase,
                                      boolean internalPath, boolean trustForwardedFor, byte[] bytes, Function<ClientResponse, Mono<Boolean>> handler) {
        URI targetUri;
        try {
            targetUri = buildTargetUri(upstreamBase, exchange, internalPath);
        } catch (IllegalArgumentException e) {
            log.error("Invalid upstream URI for {}: {}", upstreamBase, e.getMessage());
            return Mono.error(e);
        }
        WebClient.RequestBodySpec spec = webClient.method(exchange.getRequest().getMethod())
                .uri(targetUri)
                .headers(h -> copyRequestHeaders(h, exchange.getRequest().getHeaders(), exchange, trustForwardedFor));
        WebClient.RequestHeadersSpec<?> req;
        if (bytes == null) {
            req = spec.body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()));
        } else if (bytes.length == 0) {
            req = spec;
        } else {
            req = spec.body(BodyInserters.fromValue(bytes));
        }
        return req.exchangeToMono(handler);
    }

    private static Mono<Void> writeUpstream(ServerWebExchange exchange, ClientResponse upstream) {
        exchange.getResponse().setStatusCode(upstream.statusCode());
        copyResponseHeaders(exchange.getResponse().getHeaders(), upstream.headers().asHttpHeaders());
        // Stream response as DataBuffer — zero-copy, avoids unnecessary heap allocation
        return exchange.getResponse().writeWith(upstream.bodyToFlux(DataBuffer.class));
    }

    private static Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase()
                + "\",\"message\":\"" + message + "\",\"gateway\":\"bcs-gateway\"}";
        DataBuffer buf = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buf));
    }

    /** The connection was not established (refused, connect timeout, unresolvable host): nothing was sent. */
    static boolean isConnectFailure(Throwable ex) {
        for (Throwable c = ex; c != null; c = (c.getCause() == c ? null : c.getCause())) {
            if (c instanceof ConnectException || c instanceof UnknownHostException) {
                return true;
            }
        }
        return false;
    }

    /** The request was sent and no (more) response arrived within bcs.gateway.response-timeout-seconds. */
    static boolean isReadTimeout(Throwable ex) {
        for (Throwable c = ex; c != null; c = (c.getCause() == c ? null : c.getCause())) {
            if (c instanceof io.netty.handler.timeout.ReadTimeoutException
                    || c instanceof java.util.concurrent.TimeoutException) {
                return true;
            }
        }
        return false;
    }

    /** Prefix under which the legacy gateway published the platform servlet (T-01); the modernized service answers at {@code /PLA/...}. */
    public static final String PUBLIC_PREFIX = "/BSCL/Plataforma/GWJAVA";

    /** {@code /BSCL/Plataforma/GWJAVA/PLA/x} → {@code /PLA/x}; any other path, and any path with a dot segment, unchanged. */
    public static String publicToInternal(String path) {
        if (path.startsWith(PUBLIC_PREFIX + "/PLA/") && esRutaLimpia(path)) {
            return path.substring(PUBLIC_PREFIX.length());
        }
        return path;
    }

    /** No dot segments, encoded dots / slashes / backslashes, matrix parameters or control characters: nothing a rewrite should have to interpret. */
    private static boolean esRutaLimpia(String path) {
        String p = path.toLowerCase();
        if (p.contains("..") || p.contains("%2e") || p.contains("%2f") || p.contains("%5c") || p.contains(";") || p.contains("\\") || p.contains("%00")
                || p.contains("//") || p.contains("%25")) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) < 0x20 || path.charAt(i) == 0x7f) {
                return false;
            }
        }
        return true;
    }

    /** internalPath: the target is a modernized service (rewrite the public prefix); false: TomEE gets the path exactly as the client sent it. */
    private static URI buildTargetUri(String upstreamBase, ServerWebExchange exchange, boolean internalPath) {
        String rawPath = exchange.getRequest().getURI().getRawPath();
        if (internalPath) {
            rawPath = publicToInternal(rawPath);
        }
        String query = exchange.getRequest().getURI().getRawQuery();
        String full = upstreamBase.replaceAll("/$", "") + rawPath
                + (query != null ? "?" + query : "");
        return URI.create(full);
    }

    private static void copyRequestHeaders(HttpHeaders target, HttpHeaders source,
                                           ServerWebExchange exchange, boolean trustForwardedFor) {
        source.forEach((name, values) -> {
            if (name.equalsIgnoreCase("host") || HOP_BY_HOP.contains(name.toLowerCase())) {
                return;
            }
            // what the client says about itself is not evidence: without a trusted balancer in front, its forwarding headers are dropped
            if (!trustForwardedFor && (name.equalsIgnoreCase("x-forwarded-for") || name.equalsIgnoreCase("forwarded")
                    || name.equalsIgnoreCase("x-real-ip"))) {
                return;
            }
            target.put(name, new java.util.ArrayList<>(values));   // a copy: the source list is read-only and X-Forwarded-For is appended to
        });
        // Append the address of the connection
        if (exchange.getRequest().getRemoteAddress() != null) {
            target.add("X-Forwarded-For",
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        }
        String originalHost = source.getFirst(HttpHeaders.HOST);
        if (originalHost != null) {
            target.set("X-Forwarded-Host", originalHost);
        }
        target.set("X-Forwarded-Proto", exchange.getRequest().getURI().getScheme());
    }

    private static void copyResponseHeaders(HttpHeaders target, HttpHeaders source) {
        source.forEach((name, values) -> {
            if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                target.put(name, new java.util.ArrayList<>(values));   // a copy: the source list is read-only and X-Forwarded-For is appended to
            }
        });
    }

    // Signals a 5xx response so the circuit breaker records a failure
    static final class UpstreamServerException extends RuntimeException {
        UpstreamServerException(String route, int status) {
            super("Upstream " + route + " returned HTTP " + status);
        }
    }
}
