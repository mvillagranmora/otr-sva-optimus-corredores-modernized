package cl.bcs.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * GW-01: Spring context must load — all beans wired, route table parsed, circuit breakers registered.
 * GW-02: /fallback/tomee must return 503 with gateway envelope.
 * GW-03: /actuator/health must return 200.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BcsGatewayApplicationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    // GW-01
    @Test
    void contextLoads() {
        // Context booted — GatewayProperties parsed, ProxyRouter registered, circuit breakers wired.
    }

    // GW-02
    @Test
    void fallbackEndpoint_returns503WithGatewayEnvelope() {
        webTestClient.get()
                .uri("/fallback/tomee")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.gateway").isEqualTo("bcs-gateway");
    }

    // GW-03
    @Test
    void actuatorHealth_returns200() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
