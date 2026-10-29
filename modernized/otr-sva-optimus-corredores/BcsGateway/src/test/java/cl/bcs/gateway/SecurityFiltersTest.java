package cl.bcs.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import cl.bcs.gateway.config.ProxyRouter;

/** T-02b: security headers, CORS for listed origins only, no trust in the client's X-Forwarded-For, strict path rewrite. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityFiltersTest extends SecurityFiltersTestBase {

    static final String UI = "https://ui.example.test";

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("bcs.gateway.legacy-fallback-uri", SecurityFiltersTestBase::tomeeUrl);
        r.add("bcs.gateway.cors-allowed-origins", () -> UI + " , https://otra.example.test");
    }

    @LocalServerPort
    private int port;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(10)).build();
        XFF.set(null);
        PATH.set(null);
    }

    @Test
    void everyAnswerCarriesTheSecurityHeaders_andTheUpstreamValueIsReplaced() {
        client.get().uri("/anything").exchange().expectStatus().isOk()
                .expectHeader().valueEquals("X-Frame-Options", "SAMEORIGIN")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff");
    }

    @Test
    void theGatewaysOwnAnswersCarryThemToo() {
        client.get().uri("/actuator/health").exchange()
                .expectHeader().valueEquals("X-Frame-Options", "SAMEORIGIN")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff");
    }

    @Test
    void listedOrigin_getsAllowOrigin_andVary() {
        client.get().uri("/anything").header("Origin", UI).exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", UI)
                .expectHeader().valueEquals("Vary", "Origin");
    }

    @Test
    void unlistedOrigin_getsNoCorsHeader() {
        client.get().uri("/anything").header("Origin", "https://malo.example.test").exchange().expectStatus().isOk()
                .expectHeader().doesNotExist("Access-Control-Allow-Origin");
        client.get().uri("/anything").exchange().expectHeader().doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    void originMatchIsExact_noPrefixNoSubdomain() {
        client.get().uri("/anything").header("Origin", UI + ".malo.test").exchange().expectHeader().doesNotExist("Access-Control-Allow-Origin");
        client.get().uri("/anything").header("Origin", "https://x.ui.example.test").exchange().expectHeader().doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    void preflightOfListedOrigin_isAnsweredByTheGateway_withoutReachingTomee() {
        client.options().uri("/PLA/CrearSesion").header("Origin", UI).header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "x-session-token").exchange()
                .expectStatus().isNoContent()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", UI)
                .expectHeader().valueEquals("Access-Control-Allow-Headers", "X-SESSION-TOKEN, Content-Type")
                .expectHeader().valueEquals("Access-Control-Max-Age", "600")
                .expectHeader().doesNotExist("Access-Control-Allow-Credentials");
        assertThat(PATH.get()).isNull();
    }

    @Test
    void preflightOfUnlistedOrigin_getsNoPermission() {
        client.options().uri("/PLA/CrearSesion").header("Origin", "https://malo.example.test").header("Access-Control-Request-Method", "POST").exchange()
                .expectHeader().doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    void clientForwardedFor_isDiscarded_theConnectionAddressIsTheOnlyValue() {
        client.get().uri("/anything").header("X-Forwarded-For", "6.6.6.6").header("X-Real-IP", "6.6.6.6").exchange().expectStatus().isOk();
        assertThat(XFF.get()).doesNotContain("6.6.6.6").matches("127\\.0\\.0\\.1|0:0:0:0:0:0:0:1");
    }

    @Test
    void rewriteRefusesEncodedAndOddPaths() {
        String p = ProxyRouter.PUBLIC_PREFIX + "/PLA/";
        for (String sucio : new String[] { p + "a/%2e%2e/b", p + "a/%2E%2e/b", p + "a%2fb", p + "a%5cb", p + "a;jsessionid=1", p + "a//b", p + "a%00b",
                p + "a%25b", p + "a\\b", p + "a\tb" }) {
            assertThat(ProxyRouter.publicToInternal(sucio)).as(sucio).isEqualTo(sucio);
        }
        assertThat(ProxyRouter.publicToInternal(p + "bootstrap/login")).isEqualTo("/PLA/bootstrap/login");
    }
}
