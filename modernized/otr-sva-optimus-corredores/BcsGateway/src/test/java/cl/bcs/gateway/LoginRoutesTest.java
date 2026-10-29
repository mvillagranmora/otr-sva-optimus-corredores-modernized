package cl.bcs.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Login MVP, gateway side (T-01, T-02): the public prefix {@code /BSCL/Plataforma/GWJAVA/PLA/**} is rewritten to {@code /PLA/**} for the modernized
 * service only, the login routes are opt-in and never replay in TomEE. Real filter, real local HTTP servers (fake service and fake TomEE).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginRoutesTest {

    private static final String PUBLIC = "/BSCL/Plataforma/GWJAVA";
    private static final String JSON = "{\"user\":\"u\",\"pass\":\"p\"}";

    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final AtomicInteger SERVICE_CALLS = new AtomicInteger();
    private static final AtomicReference<String> SERVICE_PATH = new AtomicReference<>();
    private static final AtomicReference<String> SERVICE_QUERY = new AtomicReference<>();
    private static final AtomicReference<String> SERVICE_BODY = new AtomicReference<>();
    private static final AtomicReference<String> SERVICE_TOKEN = new AtomicReference<>();
    private static final AtomicInteger TOMEE_CALLS = new AtomicInteger();
    private static final AtomicReference<String> TOMEE_PATH = new AtomicReference<>();
    private static final HttpServer SERVICE;
    private static final HttpServer TOMEE;
    private static final int CLOSED_PORT;

    static {
        try {
            SERVICE = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            SERVICE.setExecutor(POOL);
            SERVICE.createContext("/PLA/unavailable", ex -> {
                SERVICE_CALLS.incrementAndGet();
                ex.getRequestBody().readAllBytes();
                reply(ex, 503, "not available");
            });
            SERVICE.createContext("/PLA/", ex -> {
                SERVICE_CALLS.incrementAndGet();
                SERVICE_PATH.set(ex.getRequestURI().getPath());
                SERVICE_QUERY.set(ex.getRequestURI().getRawQuery());
                SERVICE_TOKEN.set(ex.getRequestHeaders().getFirst("X-SESSION-TOKEN"));
                SERVICE_BODY.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                reply(ex, 200, "{\"from\":\"service\"}");
            });
            SERVICE.start();

            TOMEE = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            TOMEE.setExecutor(POOL);
            TOMEE.createContext("/", ex -> {
                TOMEE_CALLS.incrementAndGet();
                TOMEE_PATH.set(ex.getRequestURI().getPath());
                ex.getRequestBody().readAllBytes();
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

    private static void reply(HttpExchange ex, int status, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    @AfterAll
    static void stopServers() {
        SERVICE.stop(0);
        TOMEE.stop(0);
        POOL.shutdownNow();
    }

    @DynamicPropertySource
    static void gatewayProps(DynamicPropertyRegistry r) {
        String up = "http://127.0.0.1:" + SERVICE.getAddress().getPort();
        r.add("bcs.gateway.legacy-fallback-uri", () -> "http://127.0.0.1:" + TOMEE.getAddress().getPort());
        r.add("bcs.gateway.response-timeout-seconds", () -> "2");
        // login-like route, ON, no TomEE fallback
        route(r, 0, "l-on", up, "/PLA/CrearSesion", true, true);
        route(r, 1, "l-on-boot", up, "/PLA/bootstrap/**", true, true);
        route(r, 2, "l-off", up, "/PLA/ActualizarClave", false, true);
        route(r, 3, "l-unavailable", up, "/PLA/unavailable", true, true);
        route(r, 4, "l-refused", "http://127.0.0.1:" + CLOSED_PORT, "/PLA/RecuperacionClave", true, true);
        // an ordinary route (fallback allowed) behind the prefix, to show the default is unchanged
        route(r, 5, "l-refused-fb", "http://127.0.0.1:" + CLOSED_PORT, "/PLA/ConfirmarCambioClave", true, false);
    }

    private static void route(DynamicPropertyRegistry r, int i, String id, String uri, String path, boolean enabled, boolean noFallback) {
        String p = "bcs.gateway.routes[" + i + "].";
        r.add(p + "id", () -> id);
        r.add(p + "uri", () -> uri);
        r.add(p + "paths[0]", () -> path);
        r.add(p + "enabled", () -> String.valueOf(enabled));
        r.add(p + "no-legacy-fallback", () -> String.valueOf(noFallback));
    }

    @LocalServerPort
    private int port;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(15)).build();
        SERVICE_CALLS.set(0);
        SERVICE_PATH.set(null);
        SERVICE_QUERY.set(null);
        SERVICE_BODY.set(null);
        SERVICE_TOKEN.set(null);
        TOMEE_CALLS.set(0);
        TOMEE_PATH.set(null);
    }

    private WebTestClient.ResponseSpec post(String path, String body) {
        return client.post().uri(path).header("Content-Type", "application/json").header("X-SESSION-TOKEN", "tok-1").bodyValue(body).exchange();
    }

    // ---- T-01: the prefix rewrite ----------------------------------------------------------

    @Test
    void publicPrefix_isRewrittenForTheService_withBodyHeadersAndQuery() {
        client.post().uri(java.net.URI.create("http://localhost:" + port + PUBLIC + "/PLA/CrearSesion?a=1&b=x%20y")).header("Content-Type", "application/json")
                .header("X-SESSION-TOKEN", "tok-1").bodyValue(JSON).exchange()
                .expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"service\"}");
        assertThat(SERVICE_CALLS).hasValue(1);
        assertThat(SERVICE_PATH).hasValue("/PLA/CrearSesion");
        assertThat(SERVICE_QUERY).hasValue("a=1&b=x%20y");
        assertThat(SERVICE_BODY).hasValue(JSON);
        assertThat(SERVICE_TOKEN).hasValue("tok-1");
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void publicPrefix_matchesWildcardRoutes() {
        client.get().uri(PUBLIC + "/PLA/bootstrap/login").exchange().expectStatus().isOk();
        assertThat(SERVICE_PATH).hasValue("/PLA/bootstrap/login");
    }

    @Test
    void routeWrittenWithoutThePrefix_alsoMatches() {
        post("/PLA/CrearSesion", JSON).expectStatus().isOk();
        assertThat(SERVICE_PATH).hasValue("/PLA/CrearSesion");
    }

    @Test
    void disabledRoute_goesToTomee_withThePathExactlyAsTheClientSentIt() {
        post(PUBLIC + "/PLA/ActualizarClave", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_PATH).hasValue(PUBLIC + "/PLA/ActualizarClave");
        assertThat(SERVICE_CALLS).hasValue(0);
    }

    @Test
    void pathNotCoveredByARoute_goesToTomee_unrewritten() {
        post(PUBLIC + "/PLA/OtraCosa", JSON).expectStatus().isOk();
        assertThat(TOMEE_PATH).hasValue(PUBLIC + "/PLA/OtraCosa");
        assertThat(SERVICE_CALLS).hasValue(0);
    }

    @Test
    void otherGwjavaModules_areNotRewritten() {
        post(PUBLIC + "/COR/CrearOrden", JSON).expectStatus().isOk();
        assertThat(TOMEE_PATH).hasValue(PUBLIC + "/COR/CrearOrden");
        assertThat(SERVICE_CALLS).hasValue(0);
    }

    // ---- T-02: no replay of the login in TomEE ----------------------------------------------

    @Test
    void serviceAnswers503_noReplayInTomee_theClientGetsTheService503() {
        post(PUBLIC + "/PLA/unavailable", JSON).expectStatus().isEqualTo(503);
        assertThat(SERVICE_CALLS).hasValue(1);
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void serviceUnreachable_noReplayInTomee_503FromTheGateway() {
        post(PUBLIC + "/PLA/RecuperacionClave", JSON).expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.gateway").isEqualTo("bcs-gateway");
        assertThat(TOMEE_CALLS).hasValue(0);
    }

    @Test
    void routeWithoutTheFlag_keepsTheDefault_replayInTomeeWhenUnreachable() {
        post(PUBLIC + "/PLA/ConfirmarCambioClave", JSON).expectStatus().isOk().expectBody(String.class).isEqualTo("{\"from\":\"tomee\"}");
        assertThat(TOMEE_CALLS).hasValue(1);
        assertThat(TOMEE_PATH).hasValue(PUBLIC + "/PLA/ConfirmarCambioClave");
    }

    // ---- unit: the rewrite function ---------------------------------------------------------

    @Test
    void publicToInternal_rules() {
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal(PUBLIC + "/PLA/CrearSesion")).isEqualTo("/PLA/CrearSesion");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal(PUBLIC + "/PLA/")).isEqualTo("/PLA/");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal(PUBLIC + "/PLAX/x")).isEqualTo(PUBLIC + "/PLAX/x");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal(PUBLIC + "/PLA")).isEqualTo(PUBLIC + "/PLA");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal(PUBLIC + "/PLA/../x")).isEqualTo(PUBLIC + "/PLA/../x");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal("/PLA/x")).isEqualTo("/PLA/x");
        assertThat(cl.bcs.gateway.config.ProxyRouter.publicToInternal("/x" + PUBLIC + "/PLA/y")).isEqualTo("/x" + PUBLIC + "/PLA/y");
    }

    // ---- the shipped table ------------------------------------------------------------------

    @Test
    void shippedApplicationYml_hasTheLoginRouteOff_withNoLegacyFallback_andTheLoginPaths() {
        YamlPropertiesFactoryBean y = new YamlPropertiesFactoryBean();
        y.setResources(new FileSystemResource("src/main/resources/application.yml"));
        Properties p = y.getObject();
        String idKey = p.keySet().stream().map(String::valueOf)
                .filter(k -> k.matches("bcs\\.gateway\\.routes\\[\\d+]\\.id") && "bcs-plataforma-login".equals(p.get(k))).findFirst().orElseThrow();
        String base = idKey.substring(0, idKey.length() - 2);
        assertThat(String.valueOf(p.get(base + "enabled"))).isEqualTo("false");
        assertThat(String.valueOf(p.get(base + "no-legacy-fallback"))).isEqualTo("true");
        assertThat(p.getProperty(base + "uri")).isEqualTo("http://bcs-plataforma-web:8117");
        List<String> paths = p.keySet().stream().map(String::valueOf).filter(k -> k.startsWith(base + "paths[")).map(k -> p.getProperty(k)).toList();
        assertThat(paths).contains("/PLA/CrearSesion", "/PLA/bootstrap/**", "/PLA/ValidaClaveProvisoria", "/PLA/ActualizarClave",
                "/PLA/SolicitudRecuperacionClave", "/PLA/RecuperacionClave", "/PLA/ConfirmarCambioClave", "/PLA/SSO*");
    }
}
