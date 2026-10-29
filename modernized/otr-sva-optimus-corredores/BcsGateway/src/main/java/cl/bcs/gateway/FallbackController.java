package cl.bcs.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Diagnostic endpoint that returns a 503 sentinel indicating the gateway's
 * TomEE fallback path. This endpoint is NOT in the proxy filter path — the
 * {@code ProxyRouter} proxies directly to {@code TOMEE_URL} when a circuit
 * is open or no modernized route matches.
 *
 * Use cases:
 *   - Health probes that want to exercise the 503 response shape
 *   - Integration tests that verify the circuit-break envelope format
 *
 * Call: GET /fallback/tomee
 * Returns: 503 JSON { status, error, message, gateway }
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/tomee")
    public Mono<Map<String, Object>> fallback(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(Map.of(
            "status", 503,
            "error", "Service Unavailable",
            "message", "Gateway circuit-break sentinel — upstream unavailable.",
            "gateway", "bcs-gateway"
        ));
    }
}
