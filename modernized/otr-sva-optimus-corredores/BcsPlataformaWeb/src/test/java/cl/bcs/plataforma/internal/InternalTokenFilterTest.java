package cl.bcs.plataforma.internal;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class InternalTokenFilterTest {

    private static final String SECRETO = "s3cr3t-interno";

    /** Runs the filter and returns {status, chainReached}. */
    private static Object[] correr(String secreto, String uri, String servletPath, String token) throws Exception {
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", uri);
        rq.setRequestURI(uri);
        if (servletPath != null) rq.setServletPath(servletPath);
        if (token != null) rq.addHeader("X-Internal-Token", token);
        MockHttpServletResponse rs = new MockHttpServletResponse();
        AtomicBoolean paso = new AtomicBoolean();
        FilterChain cadena = (a, b) -> paso.set(true);
        new InternalTokenFilter(secreto).doFilter(rq, rs, cadena);
        return new Object[] { rs.getStatus(), paso.get() };
    }

    @Test
    void sinToken_401() throws Exception {
        assertThat(correr(SECRETO, "/internal/java-call", "/internal/java-call", null)).containsExactly(401, false);
    }

    @Test
    void tokenIncorrecto_401() throws Exception {
        assertThat(correr(SECRETO, "/internal/java-call", "/internal/java-call", "otro")).containsExactly(401, false);
        assertThat(correr(SECRETO, "/internal/java-call", "/internal/java-call", SECRETO + "x")).containsExactly(401, false);
        assertThat(correr(SECRETO, "/internal/java-call", "/internal/java-call", "")).containsExactly(401, false);
    }

    @Test
    void tokenCorrecto_pasa() throws Exception {
        assertThat(correr(SECRETO, "/internal/java-call", "/internal/java-call", SECRETO)).containsExactly(200, true);
    }

    @Test
    void secretoVacioRechazaTodo_inclusoCabeceraVaciaYNula() throws Exception {
        assertThat(correr("", "/internal/java-call", null, "")).containsExactly(401, false);
        assertThat(correr("", "/internal/java-call", null, null)).containsExactly(401, false);
        assertThat(correr("", "/internal/java-call", null, "algo")).containsExactly(401, false);
        assertThat(correr(null, "/internal/java-call", null, "algo")).containsExactly(401, false);
    }

    @Test
    void trucosDeRuta_siguenProtegidos() throws Exception {
        for (String uri : new String[] { "/internal//java-call", "//internal/java-call", "/internal;x=1/java-call", "/internal/java-call;jsessionid=1",
                "/%69nternal/java-call", "/a/../internal/java-call", "/internal/./java-call", "/INTERNAL/java-call", "/internal\\java-call", "/internal/%2e%2e/internal/java-call" }) {
            assertThat(correr(SECRETO, uri, null, null)).as(uri).containsExactly(401, false);
            assertThat(correr(SECRETO, uri, null, SECRETO)).as(uri + " con token").containsExactly(200, true);
        }
    }

    @Test
    void rutaResueltaPorElContenedorTambienSeProtege() throws Exception {
        assertThat(correr(SECRETO, "/otra", "/internal/java-call", null)).containsExactly(401, false);
    }

    @Test
    void rutasExistentesNoSeTocan_sinToken() throws Exception {
        for (String uri : new String[] { "/call", "/CallBatch", "/CrearSesion", "/Ping", "/bootstrap", "/Configuraciones", "/internalx/call", "/api/internal" }) {
            assertThat(correr(SECRETO, uri, uri, null)).as(uri).containsExactly(200, true);
        }
    }

    @Test
    void saludPublica() throws Exception {
        assertThat(correr(SECRETO, "/actuator/health", null, null)).containsExactly(200, true);
        assertThat(correr("", "/actuator/health", null, null)).containsExactly(200, true);
    }
}
