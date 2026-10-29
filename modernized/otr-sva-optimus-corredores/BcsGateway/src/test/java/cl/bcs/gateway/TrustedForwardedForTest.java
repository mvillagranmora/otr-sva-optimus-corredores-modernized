package cl.bcs.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Behind a trusted balancer (EKS ALB) the chain is kept and the connection address is appended; with no CORS list no CORS header is ever written. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TrustedForwardedForTest extends SecurityFiltersTestBase {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("bcs.gateway.legacy-fallback-uri", SecurityFiltersTestBase::tomeeUrl);
        r.add("bcs.gateway.trust-forwarded-for", () -> "true");
    }

    @LocalServerPort
    private int port;

    @Test
    void chainIsKept_andTheConnectionAddressAppended() {
        WebTestClient c = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(10)).build();
        c.get().uri("/anything").header("X-Forwarded-For", "203.0.113.9").header("Origin", "https://ui.example.test").exchange().expectStatus().isOk()
                .expectHeader().doesNotExist("Access-Control-Allow-Origin");
        assertThat(XFF.get()).startsWith("203.0.113.9");
        assertThat(XFF.get()).contains("127.0.0.1");
    }
}
