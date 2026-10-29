package cl.bcs.gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opt-in routes + safe TomEE fallback, through the real filter and real local HTTP servers
 * (a fake "new service" and a fake TomEE on 127.0.0.1 ephemeral ports).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SafeFallbackTest {

    private static final String JSON = "{\"idCuenta\":42,\"monto\":1000}";

    // ---- fake upstream service + fake TomEE -------------------------------------------------
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final Map<String, AtomicInteger> UPSTREAM_CALLS = new ConcurrentHashMap<>();
    private static final Map<String, String> UPSTREAM_BODY = new ConcurrentHashMap<>();
    private static final AtomicInteger TOMEE_CALLS = new AtomicInteger();
    private static final AtomicReference<String> TOMEE_BODY = new AtomicReference<>();
    private static final AtomicReference<String> TOMEE_PATH = new AtomicReference<>();
    private static final HttpServer UPSTREAM;
    private static final HttpServer TOMEE;
    private static final int CLOSED_PORT;

    static {
        try {
            UPSTREAM = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            UPSTREAM.setExecutor(POOL);
            handler("/ok", 200, "{\"from\":\"upstream\"}", 0);
            handler("/disabled", 200, "{\"from\":\"upstream\"}", 0);
            handler("/override", 200, "{\"from\":\"upstream\"}", 0);
            handler("/s503", 503, "not migrated", 0);
            handler("/s500", 500, "boom-upstream-body", 0);
            handler("/slow", 200, "{\"from\":\"upstream-late\"}", 4000);
            handler("/circuit", 500, "boom", 0);
            handler("/big", 503, "not migrated", 0);
            handler("/chunk", 503, "not migrated", 0);
            UPSTREAM.start();

            TOMEE = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            TOMEE.setExecutor(POOL);
            TOMEE.createContext("/", ex -> {
                TOMEE_CALLS.incrementAndGet();
                TOMEE_PATH.set(ex.getRequestURI().getPath());
                TOMEE_BODY.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                reply(ex, 200, "{\"from\":\"tomee\"}");
            });
            TOMEE.start();

            try (ServerSocket s = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
                CLOSED_PORT = s.getLocalPort();
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void handler(String path, int status, String body, long delayMs) {
        UPSTREAM.createContext(path, ex -> {
            UPSTREAM_CALLS.computeIfAbsent(path, k -> new AtomicInteger()).incrementAndGet();
            UPSTREAM_BODY.put(path, new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (delayMs > 0) {
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            try { reply(ex, status, body); } catch (IOException ignored) { /* client gave up (timeout test) */ }
        });
    }

    private static void reply(HttpExchange ex, int status, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("X-Upstream", "yes");
        ex.sendResponseHeaders(status, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    @AfterAll
    static void stopServers() {
        UPSTREAM.stop(0);
        TOMEE.stop(0);
        POOL.shutdownNow();
    }

    private static int calls(String path) {
        AtomicInteger a = UPSTREAM_CALLS.get(path);
        return a == null ? 0 : a.get();
    }

    // ---- gateway under test -----------------------------------------------------------------
    @DynamicPropertySource
    static void gatewayProps(DynamicPropertyRegistry r) {
        String up = "http://127.0.0.1:" + UPSTREAM.getAddress().getPort();
        r.add("bcs.gateway.legacy-fallback-uri", () -> "http://127.0.0.1:" + TOMEE.getAddress().getPort());
        r.add("bcs.gateway.response-timeout-seconds", () -> "1");
        r.add("bcs.gateway.max-buffered-body-bytes", () -> "1024");
        r.add("bcs.gateway.enabled-routes", () -> "r-override");
        route(r, 0, "r-disabled", up, "/disabled/**", false);
        route(r, 1, "r-ok", up, "/ok/**", true);
        route(r, 2, "r-503", up, "/s503/**", true);
        route(r, 3, "r-500", up, "/s500/**", true);
        route(r, 4, "r-refused", "http://127.0.0.1:" + CLOSED_PORT, "/refused/**", true);
        route(r, 5, "r-slow", up, "/slow/**", true);
        route(r, 6, "r-circuit", up, "/circuit/**", true);
        route(r, 7, "r-big", up, "/big/**", true);
        route(r, 8, "r-override", up, "/override/**", false);
        route(r, 9, "r-chunk", up, "/chunk/**", true);
        route(r, 10, "r-refused-stream", "http://127.0.0.1:" + CLOSED_PORT, "/refusedstream/**", true);
    }

    private static void route(DynamicPropertyRegistry r, int i, String id, String uri, String path, boolean enabled) {
        String p = "bcs.gateway.routes[" + i + "].";
        r.add(p + "id", () -> id);
        r.add(p + "uri", () -> uri);
        r.add(p + "paths[0]", () -> path);
        r.add(p + "enabled", () -> String.valueOf(enabled));
    }

    @LocalServerPort
    private int port;
    @Autowired
    private CircuitBreakerRegistry registry;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(15)).build();
        UPSTREAM_CALLS.clear();
        UPSTREAM_BODY.clear();
        TOMEE_CALLS.set(0);
        TOMEE_BODY.set(null);
        TOMEE_PATH.set(null);
    }

    private WebTestClient.ResponseSpec post(String path, String body) {
        return client.post().uri(path).header("Content-Type", "application/json").bodyValue(body).exchange();
    }

    // ---- POINT 1: allow-list ----------------------------------------------------------------

    @Test
    void disabledRoute_goesToTomee_andTheServiceIsNotCalled() {
        post("/disabled/x", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_PATH).hasValue("/disabled/x");
        assertThat(calls("/disabled")).isZero();
    }

    @Test
    void enabledRoutesProperty_forcesADisabledRouteOn() {
        post("/override/x", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"upstream\"}");
        assertThat(calls("/override")).isEqualTo(1);
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void noRoute_goesStraightToTomee_withTheBody() {
        post("/not/a/route", JSON).expectStatus().isOk();
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_BODY.get()).isEqualTo(JSON);
    }

    // Properties.stringPropertyNames() skips non-String values (YAML booleans), so read the raw keys
    private static java.util.List<String> keys(Properties p) {
        return p.keySet().stream().map(String::valueOf).toList();
    }

    @Test
    void shippedApplicationYml_hasEveryRouteDisabledAndNoOverride() {
        YamlPropertiesFactoryBean y = new YamlPropertiesFactoryBean();
        y.setResources(new FileSystemResource("src/main/resources/application.yml"));
        Properties p = y.getObject();
        long ids = keys(p).stream().filter(k -> k.matches("bcs\\.gateway\\.routes\\[\\d+]\\.id")).count();
        long disabled = keys(p).stream()
                .filter(k -> k.matches("bcs\\.gateway\\.routes\\[\\d+]\\.enabled"))
                .filter(k -> "false".equals(String.valueOf(p.get(k)))).count();
        assertThat(ids).isGreaterThan(30);
        assertThat(disabled).isEqualTo(ids);
        assertThat(keys(p).stream().filter(k -> k.matches("bcs\\.gateway\\.routes\\[\\d+]\\.enabled"))
                .count()).isEqualTo(ids);
        assertThat(p.getProperty("bcs.gateway.enabled-routes", "")).isEqualTo("${BCS_GATEWAY_ENABLED_ROUTES:}");
    }

    // ---- POINT 2: safe fallback -------------------------------------------------------------

    @Test
    void enabledRoute_upstream200_returnsUpstream_tomeeNotCalled() {
        post("/ok/x", JSON).expectStatus().isOk().expectHeader().valueEquals("X-Upstream", "yes")
                .expectBody(String.class).isEqualTo("{\"from\":\"upstream\"}");
        assertThat(calls("/ok")).isEqualTo(1);
        assertThat(UPSTREAM_BODY.get("/ok")).isEqualTo(JSON);
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void upstream503_onPost_replaysTheSameBodyInTomee() {
        post("/s503/pay", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(calls("/s503")).isEqualTo(1);
        assertThat(UPSTREAM_BODY.get("/s503")).isEqualTo(JSON);
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_PATH).hasValue("/s503/pay");
        assertThat(TOMEE_BODY.get()).isEqualTo(JSON);
    }

    @Test
    void upstream500_reachesTheClient_tomeeNotCalled_andBreakerCountsTheFailure() {
        post("/s500/pay", JSON).expectStatus().isEqualTo(500).expectHeader().valueEquals("X-Upstream", "yes")
                .expectBody(String.class).isEqualTo("boom-upstream-body");
        assertThat(calls("/s500")).isEqualTo(1);
        assertThat(TOMEE_CALLS).hasValue(0);
        assertThat(registry.circuitBreaker("r-500").getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
    }

    @Test
    void connectionRefused_fallsBackToTomee_withTheSameBody() {
        post("/refused/pay", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_BODY.get()).isEqualTo(JSON);
        assertThat(registry.circuitBreaker("r-refused").getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
    }

    @Test
    void upstreamSlowerThanTheResponseTimeout_is504_tomeeNotCalled_andCounted() {
        post("/slow/pay", JSON).expectStatus().isEqualTo(504);
        assertThat(TOMEE_CALLS).hasValue(0);
        assertThat(registry.circuitBreaker("r-slow").getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
    }

    @Test
    void circuitOpen_goesToTomeeWithoutCallingTheUpstream() {
        for (int i = 0; i < 5; i++) {
            post("/circuit/pay", JSON).expectStatus().isEqualTo(500);
        }
        CircuitBreaker cb = registry.circuitBreaker("r-circuit");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(calls("/circuit")).isEqualTo(5);
        assertThat(TOMEE_CALLS).hasValue(0);

        post("/circuit/pay", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(calls("/circuit")).isEqualTo(5);
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_BODY.get()).isEqualTo(JSON);
    }

    @Test
    void bodyLargerThanTheBufferLimit_upstream503_returns503_tomeeNotCalled() {
        char[] big = new char[4096];
        Arrays.fill(big, 'x');
        post("/big/pay", new String(big)).expectStatus().isEqualTo(503)
                .expectBody(String.class).isEqualTo("not migrated");
        assertThat(calls("/big")).isEqualTo(1);
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void bodyWithoutLength_upstream503_returns503_tomeeNotCalled() {
        client.post().uri("/chunk/pay").header("Content-Type", "text/plain")
                .body(Flux.just("part-1,", "part-2"), String.class).exchange()
                .expectStatus().isEqualTo(503);
        assertThat(calls("/chunk")).isEqualTo(1);
        assertThat(UPSTREAM_BODY.get("/chunk")).isEqualTo("part-1,part-2");
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void bodyWithoutLength_connectionRefused_returns503_tomeeNotCalled() {
        client.post().uri("/refusedstream/pay").header("Content-Type", "text/plain")
                .body(Flux.just("part-1,", "part-2"), String.class).exchange()
                .expectStatus().isEqualTo(503);
        assertThat(TOMEE_CALLS).hasValue(0);
    }
}
