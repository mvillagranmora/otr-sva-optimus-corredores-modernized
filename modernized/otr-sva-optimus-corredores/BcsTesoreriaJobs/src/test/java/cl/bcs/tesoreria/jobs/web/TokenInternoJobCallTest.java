package cl.bcs.tesoreria.jobs.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.support.H2Jobs;

/**
 * {@code POST /internal/job-call} is protected by the internal token, as every {@code /internal/**} endpoint of BcsTesoreriaServicios
 * (TokenInternoFilter, architecture review H1): the shared secret travels in {@code X-Internal-Token}; anything else is 401 and the job
 * does not run. Fails closed: with no secret configured every call is rejected. Only the health probes are public.
 */
class TokenInternoJobCallTest {

    static final String CUERPO = "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":1,\"parametros\":{}}";
    static final String SECRETO = "fake-internal-token-0000";

    private final AtomicInteger corridas = new AtomicInteger();

    private MockMvc mvc(String secreto) {
        H2Jobs db = H2Jobs.nueva();
        TesoreriaJob job = new TesoreriaJob() {
            @Override
            public String clase() {
                return "cl.bcs.tesoreria.Prueba";
            }

            @Override
            public boolean transaction(JobContext ctx) {
                corridas.incrementAndGet();
                return true;
            }
        };
        return MockMvcBuilders.standaloneSetup(new JobCallController(db.runner(job))).addFilters(new TokenInternoFilter(secreto)).build();
    }

    private MockHttpServletResponse llamar(MockMvc mvc, String ruta, String token) throws Exception {
        var req = post(ruta).contentType(MediaType.APPLICATION_JSON).content(CUERPO);
        return mvc.perform(token == null ? req : req.header(TokenInternoFilter.CABECERA, token)).andReturn().getResponse();
    }

    @Test
    @DisplayName("TES-J-97: the right token runs the job")
    void tesJ97_tokenCorrecto() throws Exception {
        assertThat(llamar(mvc(SECRETO), "/internal/job-call", SECRETO).getStatus()).isEqualTo(200);
        assertThat(corridas).hasValue(1);
    }

    @ParameterizedTest(name = "TES-J-98: token {0} -> 401, the job does not run")
    @ValueSource(strings = {"incorrecto", "", "fake-internal-token-000", "fake-internal-token-00000"})
    void tesJ98_tokenIncorrecto(String token) throws Exception {
        assertThat(llamar(mvc(SECRETO), "/internal/job-call", token).getStatus()).isEqualTo(401);
        assertThat(corridas).hasValue(0);
    }

    @Test
    @DisplayName("TES-J-99: no token header -> 401")
    void tesJ99_sinToken() throws Exception {
        assertThat(llamar(mvc(SECRETO), "/internal/job-call", null).getStatus()).isEqualTo(401);
        assertThat(corridas).hasValue(0);
    }

    @ParameterizedTest(name = "TES-J-99b: no secret configured ({0}): every call is rejected, even with a header")
    @ValueSource(strings = {"", "x"})
    void tesJ99b_falloCerrado(String enviado) throws Exception {
        assertThat(llamar(mvc(""), "/internal/job-call", enviado).getStatus()).isEqualTo(401);
        assertThat(corridas).hasValue(0);
    }

    @ParameterizedTest(name = "TES-J-99c: path variant {0} does not bypass the filter")
    @ValueSource(strings = {"//internal/job-call", "/internal;x=1/job-call", "/internal//job-call"})
    void tesJ99c_variantesDeRuta(String ruta) throws Exception {
        assertThat(llamar(mvc(SECRETO), ruta, null).getStatus()).isEqualTo(401);
        assertThat(corridas).hasValue(0);
    }

    @Test
    @DisplayName("TES-J-99d: only /actuator/health and /actuator/info are public")
    void tesJ99d_actuatorPublico() throws Exception {
        MockMvc m = mvc(SECRETO);
        for (String ruta : List.of("/actuator/health", "/actuator/info")) {
            assertThat(m.perform(get(ruta)).andReturn().getResponse().getStatus()).as(ruta).isNotEqualTo(401);
        }
        assertThat(m.perform(get("/actuator/env")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
