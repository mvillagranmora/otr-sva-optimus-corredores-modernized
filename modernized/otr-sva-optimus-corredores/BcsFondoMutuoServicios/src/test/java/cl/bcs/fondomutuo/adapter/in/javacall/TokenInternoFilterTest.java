package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** FM-KIT-50..59: {@link TokenInternoFilter} fails closed and the path normalisation closes the known bypasses (DEV-F6, plan step 2 point 3). */
class TokenInternoFilterTest {

    private static MockHttpServletResponse llamar(String token, String cabecera, String uri) throws Exception {
        TokenInternoFilter f = new TokenInternoFilter(token);
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", uri);
        rq.setRequestURI(uri);
        if (cabecera != null) rq.addHeader(TokenInternoFilter.CABECERA, cabecera);
        MockHttpServletResponse rs = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(rq, rs, chain);
        if (chain.getRequest() != null && rs.getStatus() == 200) rs.setHeader("X-Paso", "si");
        return rs;
    }

    private static boolean paso(MockHttpServletResponse rs) {
        return "si".equals(rs.getHeader("X-Paso"));
    }

    @Test
    void fmKit50_conTokenCorrecto_pasa() throws Exception {
        assertThat(paso(llamar("s3cr3t", "s3cr3t", "/internal/java-call"))).isTrue();
    }

    @Test
    void fmKit51_sinTokenOTokenMalo_401() throws Exception {
        assertThat(llamar("s3cr3t", null, "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(llamar("s3cr3t", "otro", "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(llamar("s3cr3t", "s3cr3t ", "/internal/java-call").getStatus()).isEqualTo(401);
    }

    @Test
    void fmKit52_sinSecretoConfigurado_fallaCerrado_inclusoSiElLlamadorMandaVacio() throws Exception {
        assertThat(llamar("", "", "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(llamar("", null, "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(llamar(null, "x", "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(llamar("", "x", "/cualquier/otra").getStatus()).isEqualTo(401);
    }

    @Test
    void fmKit53_variantesDeRutaNoSeSaltanElToken() throws Exception {
        for (String ruta : new String[] {"//internal/java-call", "/internal;x=1/java-call", "/internal/java-call;jsessionid=1", "/internal/./java-call",
                "/x/../internal/java-call", "/internal/%6Aava-call", "/internal%2Fjava-call", "/%2e%2e/internal/java-call", "/internal/java-call/",
                "/actuator/health/../../internal/java-call", "/actuator/health;/../internal/java-call", "/actuator/%252e%252e/internal/java-call", "\\internal\\java-call"}) {
            assertThat(llamar("s3cr3t", null, ruta).getStatus()).as(ruta).isEqualTo(401);
        }
    }

    @Test
    void fmKit54_soloLasRutasPublicasExactasPasanSinToken() throws Exception {
        assertThat(paso(llamar("s3cr3t", null, "/actuator/health"))).isTrue();
        assertThat(llamar("s3cr3t", null, "/actuator/info").getStatus()).as("SEC-007: info is no longer public").isEqualTo(401);
        assertThat(paso(llamar("s3cr3t", null, "//actuator//health"))).isTrue();
        assertThat(llamar("s3cr3t", null, "/actuator/health/liveness").getStatus()).isEqualTo(401);
        assertThat(llamar("s3cr3t", null, "/actuator/env").getStatus()).isEqualTo(401);
        assertThat(llamar("s3cr3t", null, "/actuator").getStatus()).isEqualTo(401);
        assertThat(llamar("s3cr3t", null, "/api/fondo-mutuo/solicitud/crear").getStatus()).isEqualTo(401);
    }

    @Test
    void fmKit55_normalizar_formaCanonica() {
        assertThat(TokenInternoFilter.normalizar("//internal;a=b//java-call")).isEqualTo("/internal/java-call");
        assertThat(TokenInternoFilter.normalizar("/a/b/../c/./d")).isEqualTo("/a/c/d");
        assertThat(TokenInternoFilter.normalizar("/internal/%6Aava-call")).isEqualTo("/internal/java-call");
        assertThat(TokenInternoFilter.normalizar("/a/%252e%252e/b")).isEqualTo("/b");
        assertThat(TokenInternoFilter.normalizar("/")).isEqualTo("/");
        assertThat(TokenInternoFilter.normalizar(null)).isEqualTo("/");
    }

    @Test
    void fmKit56_rutasQueNoSePuedenInterpretar_nuncaSonPublicasNiJavaCall() {
        for (String ruta : new String[] {"/../internal", "/a/%zz", "/a\\b", "/a/%00b", "/a/%0d%0a"}) {
            assertThat(TokenInternoFilter.normalizar(ruta)).as(ruta).isEqualTo(TokenInternoFilter.INSEGURA);
        }
    }

    @Test
    void fmKit57_sinEstadoCompartidoEntrePeticiones_dosFiltrosDistintosNoSeInfluyen() throws Exception {
        assertThat(paso(llamar("a", "a", "/internal/java-call"))).isTrue();
        assertThat(llamar("b", "a", "/internal/java-call").getStatus()).isEqualTo(401);
        assertThat(paso(llamar("b", "b", "/internal/java-call"))).isTrue();
    }
}
