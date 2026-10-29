package cl.bcs.cuentainversion.jobs.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.jobs.runtime.JobRunner;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.support.H2Jobs;

/** Architecture review step 5: M3 (lossless numbers), M8 (listener / onError / catch-all) and M2 (infrastructure messages). */
class JobCallRevisionPaso5Test {

    private final H2Jobs db = H2Jobs.nueva();

    private static CuentaInversionJob job(boolean falla, Throwable causa, boolean onErrorFalla, AtomicReference<Map<String, String>> visto) {
        return new CuentaInversionJob() {
            @Override
            public String clase() {
                return "cl.bcs.cuentaInversion.Prueba";
            }

            @Override
            public boolean transaction(JobContext ctx) throws Exception {
                visto.set(new java.util.HashMap<>());
                for (String k : List.of("Monto", "Entero", "Cero", "Grande")) {
                    if (ctx.getParam(k) != null) {
                        visto.get().put(k, ctx.getParam(k));
                    }
                }
                if (causa instanceof Exception e) {
                    throw e;
                }
                return !falla;
            }

            @Override
            public void onError(Throwable e) {
                if (onErrorFalla) {
                    throw new IllegalStateException("onError roto");
                }
            }
        };
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
    }

    private static JsonObject json(MockHttpServletResponse r) throws Exception {
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static final String CUERPO = "{\"clase\":\"cl.bcs.cuentaInversion.Prueba\",\"idPrograma\":%d,\"parametros\":%s}";

    @Test
    @DisplayName("M3: decimals and big numbers arrive with their plain text (no 1.0E8, no 1.5 for 1.50)")
    void numerosSinPerdida() throws Exception {
        AtomicReference<Map<String, String>> visto = new AtomicReference<>();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new JobCallController(db.runner(job(false, null, false, visto)))).build();

        MockHttpServletResponse r = llamar(mvc, CUERPO.formatted(1, "{\"Monto\":1.50,\"Entero\":100000000,\"Cero\":100000000.0,\"Grande\":12345678901234567890.123456}"));

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(visto.get()).containsExactlyInAnyOrderEntriesOf(
                Map.of("Monto", "1.50", "Entero", "100000000", "Cero", "100000000.0", "Grande", "12345678901234567890.123456"));
    }

    @Test
    @DisplayName("M8: a failing status listener does not lose the result of a committed run (still 200 COMMIT)")
    void listenerRotoEnCommit() throws Exception {
        JobRunner runner = new JobRunner(db.transactionManager(), db.jdbc(), List.of(job(false, null, false, new AtomicReference<>())),
                (id, error, mensaje) -> {
                    throw new IllegalStateException("listener roto");
                });
        MockHttpServletResponse r = llamar(MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build(), CUERPO.formatted(2, "{}"));

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json(r).get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(db.transactionManager().commits()).isEqualTo(1);
    }

    @Test
    @DisplayName("M8: a failing listener and a failing onError do not hide the job error (500 ERROR with the job message)")
    void listenerYOnErrorRotosEnError() throws Exception {
        JobRunner runner = new JobRunner(db.transactionManager(), db.jdbc(),
                List.of(job(false, new IllegalStateException("fallo del job"), true, new AtomicReference<>())),
                (id, error, mensaje) -> {
                    throw new IllegalStateException("listener roto");
                });
        MockHttpServletResponse r = llamar(MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build(), CUERPO.formatted(3, "{}"));

        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(json(r).get("resultado").getAsString()).isEqualTo("ERROR");
        assertThat(json(r).get("mensaje").getAsString()).isEqualTo("fallo del job");
    }

    @Test
    @DisplayName("M8: an unexpected exception gives a stable 500 body without its message")
    void catchAll() throws Exception {
        JobRunner runner = new JobRunner(db.transactionManager(), db.jdbc(), List.of(job(false, null, false, new AtomicReference<>())), db.estados()) {
            @Override
            public cl.bcs.cuentainversion.jobs.runtime.JobResultado ejecutar(String clase, long idPrograma, Map<String, String> parametros) {
                throw new IllegalStateException("secreto jdbc:hana://host");
            }
        };
        MockHttpServletResponse r = llamar(MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build(), CUERPO.formatted(4, "{}"));

        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(json(r).get("mensaje").getAsString()).isEqualTo("INTERNAL_ERROR");
        assertThat(r.getContentAsString()).doesNotContain("secreto");
    }

    @Test
    @DisplayName("M8: Spring's own client errors keep their status (405 for GET)")
    void catchAllRespetaErroresDeSpring() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new JobCallController(db.runner(job(false, null, false, new AtomicReference<>())))).build();
        MockHttpServletResponse r = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/internal/job-call"))
                .andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(405);
    }

    @Test
    @DisplayName("M2: infrastructure exceptions (SQL, I/O, also wrapped) are not echoed; job-level texts are")
    void mensajeInfraestructura() throws Exception {
        for (Exception e : List.of(new SQLException("jdbc:hana://host:443 user=X"), new IOException("/ruta/interna"),
                new IllegalStateException("envuelto", new SQLException("pwd=abc")))) {
            H2Jobs h = H2Jobs.nueva();
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new JobCallController(h.runner(job(false, e, false, new AtomicReference<>())))).build();
            MockHttpServletResponse r = llamar(mvc, CUERPO.formatted(5, "{}"));

            assertThat(r.getStatus()).isEqualTo(500);
            assertThat(json(r).get("mensaje").getAsString()).startsWith("INFRASTRUCTURE_ERROR: ").doesNotContain("host", "pwd", "ruta");
            // the legacy-pinned listener text is unchanged
            assertThat(h.estados().llamadas().get(0).mensaje()).isEqualTo(e.getMessage());
        }
    }
}
