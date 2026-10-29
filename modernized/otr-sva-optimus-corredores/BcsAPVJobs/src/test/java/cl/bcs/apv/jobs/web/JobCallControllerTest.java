package cl.bcs.apv.jobs.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobContext;
import cl.bcs.apv.jobs.runtime.JobRunner;
import cl.bcs.apv.jobs.support.H2Jobs;

/**
 * {@code POST /internal/job-call} with body {@code {"clase": "<legacy FQN>", "idPrograma": n, "parametros": {...}}}: the entry point that replaces the Quartz
 * Job Manager instantiating the legacy BcsJob class by name. Binding decision: in-process, one transaction per run (BcsJob contract), protected by the
 * internal token ({@code X-Internal-Token}, same contract as BcsAPVServicios), no @Scheduled.
 *
 * <p>Response: 200 {@code {"clase","idPrograma","resultado":"COMMIT"|"ROLLBACK","error":false,"mensaje":""}}; a job exception gives 500 with
 * {@code "resultado":"ERROR","error":true,"mensaje":<message>}; a class this module does not run gives 503; a malformed request gives 400; no or a wrong
 * token gives 401 BEFORE anything runs. Ids APVJ-WEB-NN.
 */
class JobCallControllerTest {

    static final List<String> CLASES = List.of("cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired", "cl.bcs.APV.jobs.IngresarOrdenBonificacionFiscal",
            "cl.bcs.APV.jobs.IngresarOrdenFMBonificacionFiscal", "cl.bcs.APV.jobs.IngresarOrdenRVBonificacionFiscal");
    static final String TOKEN = "fake-internal-token-0000";

    interface Cuerpo {
        boolean run(JobContext ctx) throws Exception;
    }

    record Job(String clase, Cuerpo cuerpo, List<String> vistos) implements ApvJob {
        @Override
        public boolean transaction(JobContext ctx) throws Exception {
            vistos.add(clase + "#" + ctx.idPrograma());
            return cuerpo.run(ctx);
        }
    }

    private final H2Jobs db = H2Jobs.nueva();
    private final List<String> vistos = new ArrayList<>();

    private JobRunner runner(Cuerpo cuerpo) {
        return db.runner(CLASES.stream().map(c -> new Job(c, cuerpo, vistos)).toArray(ApvJob[]::new));
    }

    private static MockMvc mvc(JobRunner runner, String token) {
        return MockMvcBuilders.standaloneSetup(new JobCallController(runner)).addFilters(new TokenInternoFilter(token)).build();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo, String token) throws Exception {
        var req = post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (token != null) {
            req = req.header(TokenInternoFilter.CABECERA, token);
        }
        return mvc.perform(req).andReturn().getResponse();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo) throws Exception {
        return llamar(mvc, cuerpo, TOKEN);
    }

    private static JsonObject json(MockHttpServletResponse r) throws Exception {
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @ParameterizedTest(name = "APVJ-WEB-01: \"{0}\" routes to its own job")
    @ValueSource(strings = {"cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired", "cl.bcs.APV.jobs.IngresarOrdenBonificacionFiscal",
            "cl.bcs.APV.jobs.IngresarOrdenFMBonificacionFiscal", "cl.bcs.APV.jobs.IngresarOrdenRVBonificacionFiscal"})
    void apvjWeb01_rutea(String clase) throws Exception {
        JobRunner runner = runner(ctx -> true);
        assertThat(runner.clases()).containsExactlyInAnyOrderElementsOf(CLASES);
        MockHttpServletResponse r = llamar(mvc(runner, TOKEN), "{\"clase\":\"" + clase + "\",\"idPrograma\":12,\"parametros\":{}}");
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json(r).get("clase").getAsString()).isEqualTo(clase);
        assertThat(vistos).containsExactly(clase + "#12");
    }

    @Test
    @DisplayName("APVJ-WEB-02: COMMIT and ROLLBACK are both 200; the body names the outcome and the job id")
    void apvjWeb02_commitYRollback() throws Exception {
        MockMvc ok = mvc(runner(ctx -> true), TOKEN);
        JsonObject c = json(llamar(ok, "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":7}"));
        assertThat(c.get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(c.get("error").getAsBoolean()).isFalse();
        assertThat(c.get("mensaje").getAsString()).isEmpty();
        assertThat(c.get("idPrograma").getAsLong()).isEqualTo(7L);
        MockHttpServletResponse rb = llamar(mvc(runner(ctx -> false), TOKEN), "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":8}");
        assertThat(rb.getStatus()).isEqualTo(200);
        assertThat(json(rb).get("resultado").getAsString()).isEqualTo("ROLLBACK");
    }

    @Test
    @DisplayName("APVJ-WEB-03: a job exception is 500 with resultado ERROR and the exception message")
    void apvjWeb03_error() throws Exception {
        MockHttpServletResponse r = llamar(mvc(runner(ctx -> { throw new IllegalStateException("boom"); }), TOKEN),
                "{\"clase\":\"" + CLASES.get(1) + "\",\"idPrograma\":9}");
        assertThat(r.getStatus()).isEqualTo(500);
        JsonObject j = json(r);
        assertThat(j.get("resultado").getAsString()).isEqualTo("ERROR");
        assertThat(j.get("error").getAsBoolean()).isTrue();
        assertThat(j.get("mensaje").getAsString()).isEqualTo("boom");
    }

    @Test
    @DisplayName("APVJ-WEB-04: a class this module does not run is 503 and nothing is started")
    void apvjWeb04_noMigrada() throws Exception {
        MockHttpServletResponse r = llamar(mvc(runner(ctx -> true), TOKEN), "{\"clase\":\"cl.bcs.APV.jobs.NoExiste\",\"idPrograma\":1}");
        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(json(r).get("clase").getAsString()).isEqualTo("cl.bcs.APV.jobs.NoExiste");
        assertThat(vistos).isEmpty();
    }

    @ParameterizedTest(name = "APVJ-WEB-05: malformed request {0} is 400")
    @ValueSource(strings = {"{\"idPrograma\":1}", "{\"clase\":\"cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired\"}", "{\"clase\":\"\",\"idPrograma\":1}",
            "{\"clase\":\"cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired\",\"idPrograma\":\"abc\"}", "not json",
            "{\"clase\":\"cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired\",\"idPrograma\":1,\"parametros\":{\"a\":{\"b\":1}}}",
            "{\"clase\":\"cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired\",\"idPrograma\":1,\"parametros\":{\"a\":[1]}}"})
    void apvjWeb05_peticionInvalida(String cuerpo) throws Exception {
        assertThat(llamar(mvc(runner(ctx -> true), TOKEN), cuerpo).getStatus()).isEqualTo(400);
        assertThat(vistos).isEmpty();
    }

    @Test
    @DisplayName("APVJ-WEB-06: parameter values reach the job as text (numbers, booleans) and JSON nulls are dropped")
    void apvjWeb06_parametros() throws Exception {
        List<Map<String, String>> recibidos = new ArrayList<>();
        JobRunner runner = runner(ctx -> {
            recibidos.add(Map.of("p", String.valueOf(ctx.getParam("p_parametros")), "n", String.valueOf(ctx.getParam("n")), "b", String.valueOf(ctx.getParam("b")),
                    "nulo", String.valueOf(ctx.getParam("nulo"))));
            return true;
        });
        llamar(mvc(runner, TOKEN), "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":3,\"parametros\":{\"p_parametros\":\"{\\\"x\\\":1}\",\"n\":5,\"b\":true,\"nulo\":null}}");
        assertThat(recibidos).containsExactly(Map.of("p", "{\"x\":1}", "n", "5", "b", "true", "nulo", "null"));
    }

    @Test
    @DisplayName("APVJ-WEB-07: no X-Internal-Token header: 401 and the job does not run")
    void apvjWeb07_sinToken() throws Exception {
        MockHttpServletResponse r = llamar(mvc(runner(ctx -> true), TOKEN), "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":1}", null);
        assertThat(r.getStatus()).isEqualTo(401);
        assertThat(vistos).isEmpty();
    }

    @Test
    @DisplayName("APVJ-WEB-08: a wrong token: 401")
    void apvjWeb08_tokenIncorrecto() throws Exception {
        assertThat(llamar(mvc(runner(ctx -> true), TOKEN), "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":1}", TOKEN + "x").getStatus()).isEqualTo(401);
        assertThat(vistos).isEmpty();
    }

    @ParameterizedTest(name = "APVJ-WEB-09: no token configured (\"{0}\"): the endpoint fails closed, 401 even with a header")
    @ValueSource(strings = {"", "  "})
    void apvjWeb09_sinTokenConfigurado(String configurado) throws Exception {
        String t = configurado.isBlank() ? "" : configurado;
        assertThat(llamar(mvc(runner(ctx -> true), t), "{\"clase\":\"" + CLASES.get(0) + "\",\"idPrograma\":1}", configurado.isEmpty() ? "x" : configurado).getStatus())
                .isEqualTo(401);
        assertThat(vistos).isEmpty();
    }

    @Test
    @DisplayName("APVJ-WEB-10: the module has no @Scheduled method (RULE-061: no daemon, no cycle)")
    void apvjWeb10_sinScheduled() {
        for (Class<?> c : List.of(JobCallController.class, JobRunner.class, TokenInternoFilter.class)) {
            for (var m : c.getDeclaredMethods()) {
                assertThat(m.isAnnotationPresent(org.springframework.scheduling.annotation.Scheduled.class)).as(c.getSimpleName() + "." + m.getName()).isFalse();
            }
        }
    }
}
