package cl.bcs.sura;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Routing behaviour of the HTTP layer. Legacy: the JavaCall dispatcher can run all four SURA classes (the three
 * socio/cliente ones are routed by InternalJavaCallController; CrearClienteFullWS... is NOT, and /crear-cliente and
 * /modificar-cliente throw). These tests fail today (503 "not yet migrated" / "requires SURA SOAP sandbox").
 * We do not assert the business result here (H2 cannot run the HANA SQL), only that the class is routed
 * and that it is not answered by the "not yet migrated" fallback.
 */
// own in-memory H2 name: the shared one is already seeded by the other @SpringBootTest context (PK violation otherwise)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:sura_dispatch;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class SuraDispatchTest {

    @Value("${local.server.port}") int port;

    private HttpResponse<String> post(String path, String body) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void bSura23_crearClienteFullWSModificarPerfilesPortafolio_estaEnrutadoEnJavaCall() throws Exception {
        HttpResponse<String> r = post("/internal/java-call",
                "{\"class\":\"cl.bcs.sura.socionegocio.CrearClienteFullWSModificarPerfilesPortafolio\",\"params\":{\"p_IdEmpresa\":1,\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ENTIDADCREADA\":[],\"p_SocioNegocio\":[]}}");
        assertThat(r.statusCode()).isEqualTo(200);   // empty p_SocioNegocio: no document -> no DB writes
    }

    @Test
    void bSura23_claseDesconocida_devuelve503ParaFallbackATomEE() throws Exception {
        HttpResponse<String> r = post("/internal/java-call", "{\"class\":\"cl.bcs.sura.socionegocio.NoExiste\",\"params\":{}}");
        assertThat(r.statusCode()).isEqualTo(503);   // body text is not exposed by default (server.error.include-message)
    }

    @Test
    void bSura23_javaCallSinClase_400() throws Exception {
        assertThat(post("/internal/java-call", "{\"params\":{}}").statusCode()).isEqualTo(400);
        assertThat(post("/internal/java-call", "no es json").statusCode()).isEqualTo(400);
    }

    @Test
    void bSura23_endpointCrearCliente_noDebeSerUnStub() throws Exception {
        HttpResponse<String> r = post("/api/sura/perfiles/crear-cliente", "{}");
        assertThat(r.statusCode()).isNotEqualTo(503);   // today: UnsupportedOperationException -> 503
    }

    @Test
    void bSura23_endpointModificarCliente_noDebeSerUnStub() throws Exception {
        HttpResponse<String> r = post("/api/sura/perfiles/modificar-cliente", "{}");
        assertThat(r.statusCode()).isNotEqualTo(503);   // today: UnsupportedOperationException -> 503
    }
}
