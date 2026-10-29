package cl.bcs.gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

/**
 * Security headers and CORS of the gateway (T-02b). Until now nginx put the headers; with the UI served outside the core the gateway does it, for every
 * answer (its own, the modernized services' and TomEE's).
 *
 * <ul>
 *   <li>{@code X-Frame-Options: SAMEORIGIN} and {@code X-Content-Type-Options: nosniff} on every response, replacing whatever the upstream sent.</li>
 *   <li>CORS only for the origins listed in {@code bcs.gateway.cors-allowed-origins} (exact match, never a wildcard). With the UI on the same origin as the
 *       gateway the list stays empty and no CORS header is ever written: a browser does not need them for same-origin calls, and an empty list refuses
 *       every other origin. A listed origin gets {@code Access-Control-Allow-Origin} (and {@code Vary: Origin}); its preflight is answered here with the
 *       methods and headers the platform uses ({@code X-SESSION-TOKEN}, {@code Content-Type}), without credentials.</li>
 * </ul>
 */
@Configuration
public class SecurityFilters {

    static final List<String> METODOS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    static final String CABECERAS_PERMITIDAS = "X-SESSION-TOKEN, Content-Type";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter securityHeadersFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> {
                HttpHeaders h = exchange.getResponse().getHeaders();
                h.set("X-Frame-Options", "SAMEORIGIN");
                h.set("X-Content-Type-Options", "nosniff");
                return Mono.empty();
            });
            return chain.filter(exchange);
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public WebFilter corsFilter(GatewayProperties props) {
        return (exchange, chain) -> {
            String origen = exchange.getRequest().getHeaders().getOrigin();
            if (origen == null || !props.corsAllowedOrigins().contains(origen)) {
                return chain.filter(exchange);
            }
            boolean preflight = exchange.getRequest().getMethod() == HttpMethod.OPTIONS
                    && exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null;
            if (preflight) {
                HttpHeaders h = exchange.getResponse().getHeaders();
                h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origen);
                h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", METODOS));
                h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, CABECERAS_PERMITIDAS);
                h.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "600");
                h.add(HttpHeaders.VARY, "Origin");
                exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
                return exchange.getResponse().setComplete();
            }
            exchange.getResponse().beforeCommit(() -> {
                HttpHeaders h = exchange.getResponse().getHeaders();
                h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origen);
                h.add(HttpHeaders.VARY, "Origin");
                return Mono.empty();
            });
            return chain.filter(exchange);
        };
    }
}
