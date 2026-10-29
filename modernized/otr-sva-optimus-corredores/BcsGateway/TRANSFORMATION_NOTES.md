# BcsGateway — Transformation Notes

**Date:** 2026-10-08
**Legacy source:** No dedicated legacy gateway module. BcsGateway is a new module introduced in the modernization to implement the Strangler Fig routing pattern.
**Target:** Spring Boot 4.1.1 / Java 25 / Spring WebFlux reverse proxy

---

## Legacy → Target mapping

| Behavior | Legacy | Target |
|---|---|---|
| HTTP routing to backend services | TomEE monolith (no gateway) | `ProxyRouter.reverseProxyFilter` — AntPath-matched WebFilter |
| Circuit breaker fallback | N/A | Resilience4j `CircuitBreaker` per route, fallback to `/fallback/tomee` |
| Fallback response | N/A | `FallbackController.fallback()` → 503 JSON envelope |
| Route table | N/A | `GatewayProperties` (`bcs.gateway.routes`) loaded from `application.yml` |
| Actuator health | N/A | Spring Boot Actuator `/actuator/health` |

---

## Spring Cloud Gateway incompatibility — rationale for WebFlux replacement

The original scaffold used `spring-cloud-starter-gateway 2025.0.0`. This release was built against Spring Boot 3.x and references `org.springframework.boot.autoconfigure.web.embedded.NettyWebServerFactoryCustomizer` and `org.springframework.boot.web.context.WebServerInitializedEvent` — both removed or relocated in Spring Boot 4.x.

The errors manifested as `NoClassDefFoundError` during Spring condition evaluation (not at bean creation time), making auto-configuration exclusions ineffective:
- `SimpleDiscoveryClientAutoConfiguration` → `WebServerInitializedEvent` not found
- `GatewayAutoConfiguration$NettyConfiguration` → `NettyWebServerFactoryCustomizer` not found

**Decision:** Replace `spring-cloud-starter-gateway` with native Spring WebFlux routing + Resilience4j core. This:
- Eliminates the Spring Cloud compatibility dependency cycle for Spring Boot 4.x
- Gives explicit control over the routing implementation
- Uses the same Reactor Netty runtime (already pulled in by `spring-boot-starter-webflux`)
- Keeps Resilience4j circuit breakers without the Spring Cloud wrapper

**Follow-up:** When a Spring Cloud release compatible with Spring Boot 4.x is published, migrate back to `spring-cloud-starter-gateway` for richer filter capabilities (rate limiting, request modification, load balancer integration).

---

## Deliberate deviations

| Item | Reason |
|---|---|
| No Redis rate limiter | `RequestRateLimiter` from Spring Cloud Gateway removed; Redis is a deployment dependency not available in this scope. Add as a follow-up with `spring-boot-starter-data-redis-reactive` + custom `WebFilter`. |
| No `$orderby` / `$select` OData params forwarded separately | Gateway is a transparent proxy; downstream services receive the full query string as-is. |
| No `X-BCS-Gateway-Fallback` header on legacy fallback | The header was a Spring Cloud Gateway filter artifact; the WebFilter implementation skips hop-by-hop metadata. Add with an explicit `ServerWebExchange.mutate()` if needed. |
| `gateway/contabilidad-routes.yml` in separate folder | Configuration artifact from scaffolding; content absorbed into `BcsGateway/src/main/resources/application.yml`. The `gateway/` directory is not a buildable module. |

---

## Architecture critic findings (2026-10-08)

Applied (BLOCKER / HIGH):
| Finding | Applied fix |
|---|---|
| BLOCKER: Body forwarding used `publisher.getClass()` — returns Reactor class, not element type | Changed to `BodyInserters.fromDataBuffers(exchange.getRequest().getBody())` |
| HIGH: Circuit breaker never tripped on 5xx responses (only exceptions) | Added `if (status.is5xxServerError()) return Mono.error(...)` before CircuitBreakerOperator |
| HIGH: Fallback returned static 503 instead of proxying to TomEE | `onErrorResume` now calls `proxy(exchange, webClient, props.legacyFallbackUri(), ...)` directly |
| HIGH: Response body decoded to `byte[]` (unnecessary allocation) | Changed to `upstream.bodyToFlux(DataBuffer.class)` — zero-copy streaming |
| HIGH: Hop-by-hop headers forwarded verbatim (RFC 7230 §6.1) | Added `HOP_BY_HOP` deny-list applied to both request and response header copying |
| HIGH: No `X-Forwarded-For` injection; client-supplied values copied verbatim | Added `X-Forwarded-For` append, `X-Forwarded-Host`, `X-Forwarded-Proto` headers |
| HIGH: `FallbackController` Javadoc referenced removed Spring Cloud Gateway DSL | Rewritten to describe actual behavior (503 diagnostic endpoint) |

Listed in notes (MEDIUM / LOW — deferred):
- **MEDIUM**: `buildTargetUri` throws synchronously on bad URIs — wrapped with try/catch returning `Mono.error`
- **MEDIUM**: Timeout added to WebClient (`CONNECT_TIMEOUT_MILLIS=3000`, `responseTimeout=30s`)
- **LOW**: No WireMock-based routing/CB tests — deferred to Phase 5 integration test suite
- **LOW**: `AntPathMatcher` vs `PathPatternParser` — functional, not correctness issue; deferred
- **NOTE**: Port 8086 used by both `bcs-orden-servicios` and `bcs-portafolio-servicios` is NOT a collision — Docker Compose hostnames are distinct; same port is valid on separate containers. Retained as-is.

---

## What was NOT migrated

- Redis rate limiting (`RequestRateLimiter` filter) — external dependency, deferred
- Global CORS filter — deferred; not exercised by any existing test; add `CorsWebFilter` bean when frontend integration begins
- Load balancer integration (`lb://` URIs) — not present in the route table; all URIs are static

---

## Canary

**Change:** `FallbackController.fallback()` — return `HttpStatus.OK` instead of `SERVICE_UNAVAILABLE`
**Result:** 1 test failed as expected (`GW-02: fallbackEndpoint_returns503WithGatewayEnvelope`); 2 tests unaffected (GW-01 contextLoads, GW-03 actuatorHealth)
**Canary evidence:** `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsGateway/`

---

## Equivalence cases

- **Executed:** 3
- **Legacy runnable:** No (no legacy gateway equivalent existed)
- **Fresh-input check:** N/A — this is a new module; no legacy oracle for comparison

---

## Test results (2026-10-08)

| Test | Rule | Result |
|---|---|---|
| `contextLoads` | GW-01 | PASS |
| `fallbackEndpoint_returns503WithGatewayEnvelope` | GW-02 | PASS |
| `actuatorHealth_returns200` | GW-03 | PASS |

**3 tests, 0 failures, 0 skipped**

---

## Follow-ups for next dependent modules

- Redis rate limiter bean (when Redis is available in deployment environment)
- Global CORS `CorsWebFilter` (when frontend integration begins)
- Migrate to Spring Cloud Gateway 4.x-compatible release when available
- `gateway/contabilidad-routes.yml` artifact cleanup (delete orphan folder after confirming content is absorbed)

## Login MVP routes (T-01, T-02) (2026-10-26)

- **T-01** `ProxyRouter.publicToInternal`: `/BSCL/Plataforma/GWJAVA/PLA/**` → `/PLA/**` before matching routes and when building the URI toward a modernized service (query, body and headers kept). TomEE (fallback or no active route) still gets the path exactly as the client sent it. Paths with a dot segment and every other module under the prefix are not rewritten.
- **T-02** new route `bcs-plataforma-login` → `bcs-plataforma-web:8117` (`/PLA/CrearSesion`, `/PLA/bootstrap/**`, `/PLA/ValidaClaveProvisoria`, `/PLA/ActualizarClave`, `/PLA/SolicitudRecuperacionClave`, `/PLA/RecuperacionClave`, `/PLA/ConfirmarCambioClave`, `/PLA/SSO*`), `enabled: false` until G-01. New flag `routes[].no-legacy-fallback` (default false: every other route behaves as before): with it, 503 from the service, no connection or an open circuit answer 503 instead of replaying in TomEE (a replayed login would open a second session or count a second attempt).
- **Not done (P1):** T-02b security headers/CORS, T-02c minimal compose.
- **Tests:** `LoginRoutesTest` 11 (rewrite with body/query/header, wildcard, disabled route to TomEE unrewritten, other modules, no replay on 503 / unreachable, default kept, rewrite rules, shipped table); module suite 27, 0 failures (SafeFallbackTest 13 and context 3 unchanged).
- **Canary:** `publicToInternal` returns the path unchanged → 5 tests failed. Restored.
- The nginx of the web project still maps `/optimus` to a bootstrap HTML: obsolete with option B, to be changed by UI/infra.

## Gateway security (T-02b, T-02c) (2026-10-26, night)
- **Security headers:** `SecurityFilters.securityHeadersFilter` sets `X-Frame-Options: SAMEORIGIN` and `X-Content-Type-Options: nosniff` on every answer (its own, the services', TomEE's), replacing the upstream value.
- **CORS:** `bcs.gateway.cors-allowed-origins` (env `BCS_GATEWAY_CORS_ALLOWED_ORIGINS`), exact origins, never a wildcard, no credentials; empty by default. The UI will be served from the same origin as the gateway, where a browser needs no CORS header, so the list stays empty in the EKS deployment; an origin that is listed gets `Access-Control-Allow-Origin` + `Vary: Origin` and its preflight is answered by the gateway (`X-SESSION-TOKEN, Content-Type`). Same-origin does not need CORS "activated"; if it must be on for another origin, list it.
- **X-Forwarded-For:** the client's `X-Forwarded-For`, `Forwarded` and `X-Real-IP` are discarded; the connection address is the only value. `bcs.gateway.trust-forwarded-for=true` (env `BCS_GATEWAY_TRUST_FORWARDED_FOR`) keeps the chain and appends the connection address: for behind the ALB, where the services must read the client from the right end of the chain. Fixed on the way: with the chain kept, appending to the copied header list failed with 500 (the source list is read-only).
- **Rewrite of the public prefix:** `publicToInternal` now refuses (leaves unrewritten, so TomEE gets it as sent) `..`, `%2e`, `%2f`, `%5c`, `%25`, `%00`, `;`, `\`, `//` and control characters.
- **Deployment:** `BcsGateway/deploy/eks/mvp-login.yaml`: namespace, ConfigMap, two Deployments + Services (gateway 9090, plataforma-web 8117), a NetworkPolicy so only the gateway reaches the service, and an ALB Ingress for `/BSCL/Plataforma/GWJAVA`. Values in `<...>` and the secret (HANA, INTERNAL_TOKEN) are for whoever deploys; the manifest has not been applied to any cluster.
- **Tests:** `SecurityFiltersTest` 9, `TrustedForwardedForTest` 1; module suite 37, 0 failures.
