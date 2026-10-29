package cl.bcs.plataforma.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** DEV-A5: the internal token filter fails closed and protects every path except the exact public ones. */
class TokenInternoFilterTest {

    private static int estado(String secreto, String uri, String cabecera) throws Exception {
        TokenInternoFilter f = new TokenInternoFilter(secreto);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRequestURI(uri);
        if (cabecera != null) req.addHeader(TokenInternoFilter.CABECERA, cabecera);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        return chain.getRequest() != null ? 200 : res.getStatus();
    }

    @Test
    void tokenCorrectoPasa() throws Exception {
        assertThat(estado("s3cr3t", "/internal/java-call", "s3cr3t")).isEqualTo(200);
    }

    @Test
    void sinTokenOTokenMalo_401() throws Exception {
        assertThat(estado("s3cr3t", "/internal/java-call", null)).isEqualTo(401);
        assertThat(estado("s3cr3t", "/internal/java-call", "otro")).isEqualTo(401);
        assertThat(estado("s3cr3t", "/internal/java-call", "S3CR3T")).isEqualTo(401);
        assertThat(estado("s3cr3t", "/internal/java-call", "")).isEqualTo(401);
    }

    @Test
    void secretoVacioRechazaTodo_fallaCerrado() throws Exception {
        assertThat(estado("", "/internal/java-call", "")).isEqualTo(401);
        assertThat(estado("", "/internal/java-call", "x")).isEqualTo(401);
        assertThat(estado(null, "/internal/java-call", "x")).isEqualTo(401);
    }

    @Test
    void variantesDeRutaNoEvitanElFiltro() throws Exception {
        for (String uri : new String[] {"//internal/java-call", "/internal;x=1/java-call", "/internal//java-call",
                "/internal/java-call;jsessionid=1", "/INTERNAL/java-call", "/internal/%2e/java-call", "/api/plataforma/health"}) {
            assertThat(estado("s3cr3t", uri, null)).as(uri).isEqualTo(401);
        }
    }

    @Test
    void soloLaSaludEsPublica() throws Exception {
        assertThat(estado("s3cr3t", "/actuator/health", null)).isEqualTo(200);
        assertThat(estado("s3cr3t", "//actuator//health", null)).isEqualTo(200);
        assertThat(estado("s3cr3t", "/actuator/health/x", null)).isEqualTo(401);
        assertThat(estado("s3cr3t", "/actuator/env", null)).isEqualTo(401);
    }
}
