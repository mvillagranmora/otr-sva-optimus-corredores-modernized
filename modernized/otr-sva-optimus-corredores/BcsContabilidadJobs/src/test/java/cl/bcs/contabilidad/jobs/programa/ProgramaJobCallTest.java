package cl.bcs.contabilidad.jobs.programa;

import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.support.ProgramaH2;
import cl.bcs.contabilidad.jobs.web.JobCallController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Job pattern H4 end to end: {@code POST /internal/job-call} in mode {@code programa} against the H2 BAT schema. */
class ProgramaJobCallTest {

    static final String CLASE = "cl.bcs.contabilidad.Prueba";
    static final String TIPO = "GENERARARCHIVOPAGO";

    private final ProgramaH2 db = new ProgramaH2();
    private final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());
    private final List<Map<String, String>> vistos = new CopyOnWriteArrayList<>();

    @AfterEach
    void cerrar() {
        db.close();
    }

    /** A job that records the parameters it sees, writes a marker row and then returns {@code resultado} or throws. */
    private ContabilidadJob job(Boolean resultado, String fallo, CountDownLatch dentro, CountDownLatch seguir) {
        return new ContabilidadJob() {
            @Override
            public String clase() {
                return CLASE;
            }

            @Override
            public boolean transaction(JobContext ctx) throws Exception {
                Map<String, String> p = new java.util.HashMap<>();
                for (String k : List.of("IdEmpresa", "IdUsuario", "Texto")) {
                    if (ctx.getParam(k) != null) {
                        p.put(k, ctx.getParam(k));
                    }
                }
                vistos.add(p);
                db.jdbc().update("INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES ('A')");
                if (dentro != null) {
                    dentro.countDown();
                    seguir.await(10, TimeUnit.SECONDS);
                }
                if (fallo != null) {
                    throw new IllegalStateException(fallo);
                }
                return resultado;
            }
        };
    }

    private MockMvc mvc(ContabilidadJob job) {
        JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), new ProgramaStatusListener(repo));
        return MockMvcBuilders.standaloneSetup(new JobCallController(new EjecutorPrograma(runner, repo, ProgramaModo.PROGRAMA))).build();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
    }

    private static JsonObject json(MockHttpServletResponse r) throws Exception {
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private void programa(long id) {
        db.tipo(TIPO + id, CLASE, 1).agenda(id).programa(id, TIPO + id, id);
        db.parametro(id, id * 10 + 1, "IdEmpresa", "1", null).parametro(id, id * 10 + 2, "IdUsuario", "99", null);
        db.parametro(id, id * 10 + 3, "Texto", "corto", "texto ñ largo".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("H4-J01: only idPrograma in the body: class and parameters (ValorBlob included) come from BAT.Programa; commit leaves TER")
    void soloIdProgramaYParametrosDeLaFila() throws Exception {
        programa(500);

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"idPrograma\":500}");

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json(r).get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(vistos).containsExactly(Map.of("IdEmpresa", "1", "IdUsuario", "99", "Texto", "texto ñ largo"));
        assertThat(db.fila(500)).containsEntry("idEstado", "TER").containsEntry("Resultado", "Finalizado correctamente.")
                .containsEntry("Intento", 1).containsEntry("Bloqueo", "S").containsEntry("fechaFin", ProgramaH2.RELOJ);
        assertThat(db.agendaUltima(500)).isEqualTo(ProgramaH2.RELOJ);
        assertThat(db.marcas()).isEqualTo(1);
    }

    @Test
    @DisplayName("H4-J02: parameters sent by the caller that differ from the row give 422 and nothing runs or changes")
    void parametrosDelLlamadorDistintosDan422() throws Exception {
        programa(501);

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"idPrograma\":501,\"parametros\":{\"IdEmpresa\":\"2\"}}");

        assertThat(r.getStatus()).isEqualTo(422);
        assertThat(r.getContentAsString()).doesNotContain("\"2\"");
        assertThat(vistos).isEmpty();
        assertThat(db.fila(501)).containsEntry("idEstado", "PEN").containsEntry("Bloqueo", "N").containsEntry("Intento", 0);
    }

    @Test
    @DisplayName("H4-J03: parameters and class sent by the caller that match the row are accepted (they are never used, the row is)")
    void parametrosYClaseCoincidentesSeAceptan() throws Exception {
        programa(502);
        String cuerpo = "{\"clase\":\"" + CLASE + "\",\"idPrograma\":502,\"parametros\":{\"IdEmpresa\":1,\"IdUsuario\":99,\"Texto\":\"texto \\u00f1 largo\"}}";

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), cuerpo);

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(db.fila(502)).containsEntry("idEstado", "TER");
    }

    @Test
    @DisplayName("H4-J04: a class that differs from TipoPrograma.NombreClasePrograma gives 422 and the row is untouched")
    void claseDistintaDa422() throws Exception {
        programa(503);

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"clase\":\"cl.bcs.contabilidad.Otra\",\"idPrograma\":503}");

        assertThat(r.getStatus()).isEqualTo(422);
        assertThat(vistos).isEmpty();
        assertThat(db.fila(503)).containsEntry("idEstado", "PEN").containsEntry("Bloqueo", "N");
    }

    @Test
    @DisplayName("H4-J05: an unknown idPrograma gives 404; a missing idPrograma gives 400")
    void inexistente404YSinIdPrograma400() throws Exception {
        MockMvc mvc = mvc(job(true, null, null, null));

        assertThat(llamar(mvc, "{\"idPrograma\":9999}").getStatus()).isEqualTo(404);
        assertThat(llamar(mvc, "{\"clase\":\"" + CLASE + "\"}").getStatus()).isEqualTo(400);
        assertThat(vistos).isEmpty();
    }

    @Test
    @DisplayName("H4-J06: an already claimed program (Bloqueo S) gives 409, the job does not run and the row keeps its state")
    void yaReclamadoDa409() throws Exception {
        programa(504);
        db.estado(504, "ASI", 1, "S");

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"idPrograma\":504}");

        assertThat(r.getStatus()).isEqualTo(409);
        assertThat(json(r).get("mensaje").getAsString()).contains("not claimable").contains("Bloqueo=S");
        assertThat(vistos).isEmpty();
        assertThat(db.fila(504)).containsEntry("idEstado", "ASI").containsEntry("Intento", 1);
    }

    @Test
    @DisplayName("H4-J07: a class of the row that this module does not run gives 503 and the row is not claimed")
    void claseNoMigradaDa503() throws Exception {
        db.tipo("OTRACLASE505", "cl.bcs.otro.NoMigrada", 1).programa(505, "OTRACLASE505", 0);

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"idPrograma\":505}");

        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(db.fila(505)).containsEntry("idEstado", "PEN").containsEntry("Bloqueo", "N");
    }

    @Test
    @DisplayName("H4-J08: transaction() false (rollback) is reported as TER like the legacy; the job's writes are rolled back")
    void rollbackQuedaTer() throws Exception {
        programa(506);

        MockHttpServletResponse r = llamar(mvc(job(false, null, null, null)), "{\"idPrograma\":506}");

        assertThat(json(r).get("resultado").getAsString()).isEqualTo("ROLLBACK");
        assertThat(db.fila(506)).containsEntry("idEstado", "TER").containsEntry("Resultado", "Finalizado correctamente.");
        assertThat(db.marcas()).isZero();
    }

    @Test
    @DisplayName("H4-J09: an exception rolls the job back and leaves ERR 'Terminado con errores:<msg>' (written after the rollback, own transaction)")
    void excepcionQuedaErr() throws Exception {
        programa(507);

        MockHttpServletResponse r = llamar(mvc(job(null, "boom", null, null)), "{\"idPrograma\":507}");

        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(db.fila(507)).containsEntry("idEstado", "ERR").containsEntry("Resultado", "Terminado con errores:boom")
                .containsEntry("fechaFin", ProgramaH2.RELOJ);
        assertThat(db.marcas()).isZero();
        assertThat(db.agendaUltima(507)).isEqualTo(ProgramaH2.RELOJ);
    }

    @Test
    @DisplayName("H4-J10: the same program called twice at once: one run, the other call gets 409, the job body runs once")
    void dosLlamadasSimultaneas() throws Exception {
        programa(508);
        CountDownLatch dentro = new CountDownLatch(1);
        CountDownLatch seguir = new CountDownLatch(1);
        MockMvc mvc = mvc(job(true, null, dentro, seguir));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<MockHttpServletResponse> primera = pool.submit(() -> llamar(mvc, "{\"idPrograma\":508}"));
            assertThat(dentro.await(10, TimeUnit.SECONDS)).isTrue();
            MockHttpServletResponse segunda = pool.submit(() -> llamar(mvc, "{\"idPrograma\":508}")).get();
            seguir.countDown();

            assertThat(segunda.getStatus()).isEqualTo(409);
            assertThat(primera.get().getStatus()).isEqualTo(200);
            assertThat(vistos).hasSize(1);
            assertThat(db.fila(508)).containsEntry("idEstado", "TER").containsEntry("Intento", 1);
        } finally {
            seguir.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("H4-J11: a program claimed meanwhile by another instance (the per-JVM guard cannot see it) is stopped by the database claim")
    void otraInstanciaYaReclamo() throws Exception {
        programa(509);
        assertThat(repo.reclamar(509)).isTrue(); // "another instance" took it between the 404 check and this call

        MockHttpServletResponse r = llamar(mvc(job(true, null, null, null)), "{\"idPrograma\":509}");

        assertThat(r.getStatus()).isEqualTo(409);
        assertThat(vistos).isEmpty();
    }

    @Test
    @DisplayName("H4-J12: two calls for two different programs of the same class run one after the other, each ends TER")
    void dosProgramasSeguidos() throws Exception {
        programa(510);
        db.tipo(TIPO + 511, CLASE, 1).agenda(511).programa(511, TIPO + 511, 511);
        MockMvc mvc = mvc(job(true, null, null, null));
        List<Integer> estados = new ArrayList<>();

        estados.add(llamar(mvc, "{\"idPrograma\":510}").getStatus());
        estados.add(llamar(mvc, "{\"idPrograma\":511}").getStatus());

        assertThat(estados).containsExactly(200, 200);
        assertThat(db.fila(510)).containsEntry("idEstado", "TER");
        assertThat(db.fila(511)).containsEntry("idEstado", "TER");
    }
}
