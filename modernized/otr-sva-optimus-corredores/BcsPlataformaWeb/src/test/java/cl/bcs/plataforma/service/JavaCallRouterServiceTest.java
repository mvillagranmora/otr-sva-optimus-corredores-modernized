package cl.bcs.plataforma.service;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cl.bcs.plataforma.service.JavaCallRouterService.extractFqn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JavaCallRouterService helpers — no HANA or HTTP required.
 */
class JavaCallRouterServiceTest {

    @Test
    void extractFqn_with_jar_prefix() {
        assertThat(extractFqn("PLUGIN_MES.jar@cl.bcs.mesa.ConsultarPosicion"))
            .isEqualTo("cl.bcs.mesa.ConsultarPosicion");
    }

    @Test
    void extractFqn_with_plain_prefix() {
        assertThat(extractFqn("PLUGIN_OPTIMUS@cl.bcs.plataforma.ConsultarParametro"))
            .isEqualTo("cl.bcs.plataforma.ConsultarParametro");
    }

    @Test
    void extractFqn_no_prefix() {
        assertThat(extractFqn("cl.bcs.tesoreria.ConsultarSaldo"))
            .isEqualTo("cl.bcs.tesoreria.ConsultarSaldo");
    }

    @Test
    void extractFqn_null_returns_empty() {
        assertThat(extractFqn(null)).isEqualTo("");
    }

    @Test
    void resolveUrl_mesa_class() {
        JavaCallRouterService svc = buildRouter();
        assertThat(svc.resolveUrl("cl.bcs.mesa.some.Class"))
            .isEqualTo("http://mesa:8085");
    }

    @Test
    void resolveUrl_plataforma_class() {
        JavaCallRouterService svc = buildRouter();
        assertThat(svc.resolveUrl("cl.bcs.plataforma.SomeClass"))
            .isEqualTo("http://pla:8114");
    }

    @Test
    void resolveUrl_unknown_class_returns_null() {
        JavaCallRouterService svc = buildRouter();
        assertThat(svc.resolveUrl("cl.bcs.unknown.SomeClass")).isNull();
    }

    @Test
    void resolveUrl_camelCase_socionegocio() {
        JavaCallRouterService svc = buildRouter();
        assertThat(svc.resolveUrl("cl.bcs.socionegocio.ConsultarSocio"))
            .isEqualTo("http://soc:8098");
    }

    private static JavaCallRouterService buildRouter() {
        var builder = org.springframework.web.client.RestClient.builder();
        return new JavaCallRouterService(
            "http://pla:8114",   // plataforma
            "http://soc:8098",   // socionegocio
            "http://ord:8086",   // ordenes
            "http://nor:8113",   // normativa
            "http://mesa:8085",  // mesa
            "http://cus:8097",   // custodia
            "http://fmm:8111",   // fondomutuo
            "http://apv:8083",   // apv
            "http://tes:8073",   // tesoreria
            "http://fac:8099",   // facturacion
            "http://cta:8080",   // cuentainversion
            "http://con:8081",   // contabilidad
            "http://por:8076",   // portafolio
            "http://cag:8102",   // canalagente
            "http://tomee:8080", // tomee fallback
            "",                  // internal token
            builder,
            ""                   // clases-habilitadas (default: none)
        );
    }

    // ─── ARQ-H5: session identity imposed on the params (legacy CallServlet.java:680-683) ───

    @Test
    void h5_present_identity_keys_are_overwritten_with_the_session_values() {
        JsonObject p = new JsonObject();
        p.addProperty("IdEmpresa", 99); p.addProperty("p_IdEmpresa", 99);
        p.addProperty("IdUsuario", 88); p.addProperty("p_IdUsuario", 88);
        p.addProperty("Otro", 5);
        JavaCallRouterService.imponerIdentidadDeSesion(p, 1, 7);
        assertThat(p.get("IdEmpresa").getAsLong()).isEqualTo(1);
        assertThat(p.get("p_IdEmpresa").getAsLong()).isEqualTo(1);
        assertThat(p.get("IdUsuario").getAsLong()).isEqualTo(7);
        assertThat(p.get("p_IdUsuario").getAsLong()).isEqualTo(7);
        assertThat(p.get("Otro").getAsLong()).isEqualTo(5);
    }

    @Test
    void h5_absent_identity_keys_stay_absent() {
        JsonObject p = new JsonObject();
        p.addProperty("Otro", 5);
        JavaCallRouterService.imponerIdentidadDeSesion(p, 1, 7);
        assertThat(p.keySet()).containsExactly("Otro");
    }

    // ─── ARQ-H6: TomEE fallback only when the service did not run the call ───

    @Test
    void h6_fallback_allowed_for_503_and_refused_connection_only() {
        assertThat(JavaCallRouterService.permiteFallbackATomee(
            HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "x", null, null, null))).isTrue();
        assertThat(JavaCallRouterService.permiteFallbackATomee(
            new ResourceAccessException("x", new ConnectException("refused")))).isTrue();
        assertThat(JavaCallRouterService.permiteFallbackATomee(
            HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "x", null, null, null))).isFalse();
        assertThat(JavaCallRouterService.permiteFallbackATomee(
            HttpServerErrorException.create(HttpStatus.GATEWAY_TIMEOUT, "x", null, null, null))).isFalse();
        assertThat(JavaCallRouterService.permiteFallbackATomee(
            new ResourceAccessException("x", new SocketTimeoutException("read timed out")))).isFalse();
    }

    @Test
    void h6_service_500_is_propagated_and_tomee_is_not_called() throws Exception {
        AtomicInteger tomeeCalls = new AtomicInteger();
        try (var service = server(500, "boom", new AtomicInteger(), new AtomicReference<>());
             var tomee = server(200, "{\"tomee\":true}", tomeeCalls, new AtomicReference<>())) {
            JavaCallRouterService r = routerTo(service, tomee);
            assertThatThrownBy(() -> r.route(1, "cl.bcs.cuentaInversion.X", new JsonObject(), 1, 7, "t", 0, 0))
                .isInstanceOf(RestClientException.class);
            assertThat(tomeeCalls).hasValue(0);
        }
    }

    @Test
    void h6_service_503_falls_back_to_tomee_with_the_session_identity() throws Exception {
        AtomicInteger tomeeCalls = new AtomicInteger();
        AtomicReference<String> tomeeBody = new AtomicReference<>();
        try (var service = server(503, "no migrada", new AtomicInteger(), new AtomicReference<>());
             var tomee = server(200, "{\"tomee\":true}", tomeeCalls, tomeeBody)) {
            JavaCallRouterService r = routerTo(service, tomee);
            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa", 99);
            String out = r.route(1, "cl.bcs.cuentaInversion.X", p, 1, 7, "t", 0, 0);
            assertThat(out).contains("tomee");
            assertThat(tomeeCalls).hasValue(1);
            assertThat(tomeeBody.get()).contains("\"p_IdEmpresa\":1");
        }
    }

    @Test
    void h5_the_service_receives_the_session_identity_and_the_internal_token() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> header = new AtomicReference<>();
        try (var service = server(200, "{}", new AtomicInteger(), body, header);
             var tomee = server(200, "{}", new AtomicInteger(), new AtomicReference<>())) {
            JavaCallRouterService r = routerTo(service, tomee, "fake-internal-token");
            JsonObject p = new JsonObject();
            p.addProperty("IdUsuario", 88);
            r.route(1, "cl.bcs.cuentaInversion.X", p, 1, 7, "t", 0, 0);
            assertThat(body.get()).contains("\"IdUsuario\":7");
            assertThat(header.get()).isEqualTo("fake-internal-token");
        }
    }

    // ─── helpers: loopback HTTP servers ───

    private interface Srv extends AutoCloseable { String url(); @Override void close(); }

    private static Srv server(int status, String reply, AtomicInteger calls, AtomicReference<String> body) throws IOException {
        return server(status, reply, calls, body, new AtomicReference<>());
    }

    private static Srv server(int status, String reply, AtomicInteger calls, AtomicReference<String> body,
                              AtomicReference<String> internalHeader) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        s.createContext("/", ex -> {
            calls.incrementAndGet();
            body.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            internalHeader.set(ex.getRequestHeaders().getFirst(JavaCallRouterService.INTERNAL_TOKEN_HEADER));
            byte[] b = reply.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        s.start();
        String url = "http://127.0.0.1:" + s.getAddress().getPort();
        return new Srv() { public String url() { return url; } public void close() { s.stop(0); } };
    }

    private static JavaCallRouterService routerTo(Srv service, Srv tomee) {
        return routerTo(service, tomee, "");
    }

    private static JavaCallRouterService routerTo(Srv service, Srv tomee, String token) {
        return routerTo(service, tomee, token, "cl.bcs.*");
    }

    private static JavaCallRouterService routerTo(Srv service, Srv tomee, String token, String habilitadas) {
        String u = service.url();
        return new JavaCallRouterService(u, u, u, u, u, u, u, u, u, u, u, u, u, u, tomee.url(), token,
            RestClient.builder(), habilitadas);
    }

    // ─── Allow-list plataforma.javacall.clases-habilitadas (opt-in, default empty) ───

    private record Hit(String out, int serviceCalls, int tomeeCalls) {}

    private static Hit callWith(String habilitadas, String ruta) throws Exception {
        AtomicInteger sc = new AtomicInteger(), tc = new AtomicInteger();
        try (var service = server(200, "{\"svc\":true}", sc, new AtomicReference<>());
             var tomee = server(200, "{\"tomee\":true}", tc, new AtomicReference<>())) {
            String out = routerTo(service, tomee, "", habilitadas).route(1, ruta, new JsonObject(), 1, 7, "t", 0, 0);
            return new Hit(out, sc.get(), tc.get());
        }
    }

    @Test
    void allowlist_default_empty_sends_everything_to_tomee() throws Exception {
        Hit h = callWith("", "cl.bcs.cuentaInversion.CrearCargoAbono");
        assertThat(h.serviceCalls()).isZero();
        assertThat(h.tomeeCalls()).isEqualTo(1);
        assertThat(h.out()).contains("tomee");
    }

    @Test
    void allowlist_class_not_listed_goes_to_tomee_without_calling_the_service() throws Exception {
        Hit h = callWith("cl.bcs.cuentaInversion.CrearCargoAbono", "PLUGIN_X.jar@cl.bcs.cuentaInversion.OtraClase");
        assertThat(h.serviceCalls()).isZero();
        assertThat(h.tomeeCalls()).isEqualTo(1);
    }

    @Test
    void allowlist_exact_fqn_reaches_the_service() throws Exception {
        Hit h = callWith("cl.bcs.cuentaInversion.CrearCargoAbono", "PLUGIN_X.jar@cl.bcs.cuentaInversion.CrearCargoAbono");
        assertThat(h.serviceCalls()).isEqualTo(1);
        assertThat(h.tomeeCalls()).isZero();
        assertThat(h.out()).contains("svc");
    }

    @Test
    void allowlist_prefix_star_reaches_the_service_and_is_case_sensitive() throws Exception {
        assertThat(callWith(" cl.bcs.mesa.X , cl.bcs.tesoreria.* ", "cl.bcs.tesoreria.ConsultarSaldo").serviceCalls()).isEqualTo(1);
        Hit otherCase = callWith("cl.bcs.tesoreria.*", "cl.bcs.Tesoreria.ConsultarSaldo");
        assertThat(otherCase.serviceCalls()).isZero();
        assertThat(otherCase.tomeeCalls()).isEqualTo(1);
        // the prefix must be a package boundary, not a bare string prefix
        Hit sibling = callWith("cl.bcs.tesoreria.*", "cl.bcs.tesoreriaX.Y");
        assertThat(sibling.serviceCalls()).isZero();
    }
}
