package cl.bcs.contabilidad.jobs.programa;

import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.support.ProgramaH2;
import cl.bcs.contabilidad.jobs.web.JobCallController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** The switch {@code contabilidad.jobs.programa.modo}: its values, its defaults and what caller mode does not touch. */
class ProgramaModoTest {

    @Test
    @DisplayName("H4-M01: the two values parse; anything else stops the start")
    void valores() {
        assertThat(ProgramaModo.desde("programa")).isEqualTo(ProgramaModo.PROGRAMA);
        assertThat(ProgramaModo.desde(" parametros-del-llamador ")).isEqualTo(ProgramaModo.PARAMETROS_DEL_LLAMADOR);
        assertThatThrownBy(() -> ProgramaModo.desde("Programa")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProgramaModo.desde(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("H4-M02: application.yml (production) defaults to 'programa'; the test profile sets 'parametros-del-llamador'")
    void valoresPorDefecto() throws Exception {
        String yml = texto("/application.yml");
        assertThat(yml).containsPattern("(?s)\ncontabilidad:\\s*\n\\s+jobs:.*?\n\\s+programa:\\s*\n\\s+modo: programa\\s*\n");
        Properties p = new Properties();
        p.load(new java.io.StringReader(texto("/application-test.properties")));
        assertThat(p.getProperty("contabilidad.jobs.programa.modo")).isEqualTo("parametros-del-llamador");
    }

    private static String texto(String recurso) throws Exception {
        try (InputStream in = ProgramaModoTest.class.getResourceAsStream(recurso)) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    @Test
    @DisplayName("H4-M03: caller mode keeps the old contract: class required (400), class and parameters used as sent, BAT never read or written")
    void modoLlamadorNoTocaBat() throws Exception {
        try (ProgramaH2 db = new ProgramaH2()) {
            AtomicReference<String> visto = new AtomicReference<>();
            ContabilidadJob job = new ContabilidadJob() {
                @Override
                public String clase() {
                    return "cl.bcs.contabilidad.Prueba";
                }

                @Override
                public boolean transaction(JobContext ctx) {
                    visto.set(ctx.getParam("IdEmpresa"));
                    return true;
                }
            };
            ProgramaRepositorio sinUso = new ProgramaRepositorioJdbc(db.dataSource()) {
                @Override
                public java.util.Optional<Programa> cargarPrograma(long id) {
                    throw new AssertionError("caller mode must not read BAT.Programa");
                }
            };
            JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), (i, e, m) -> { });
            var mvc = MockMvcBuilders.standaloneSetup(new JobCallController(new EjecutorPrograma(runner, sinUso, ProgramaModo.PARAMETROS_DEL_LLAMADOR))).build();

            var sinClase = mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content("{\"idPrograma\":1}")).andReturn().getResponse();
            var ok = mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"clase\":\"cl.bcs.contabilidad.Prueba\",\"idPrograma\":1,\"parametros\":{\"IdEmpresa\":7}}")).andReturn().getResponse();

            assertThat(sinClase.getStatus()).isEqualTo(400);
            assertThat(ok.getStatus()).isEqualTo(200);
            assertThat(visto.get()).isEqualTo("7");
        }
    }

    @Test
    @DisplayName("H4-M04: program mode needs a repository; caller mode does not")
    void repositorioObligatorioEnModoPrograma() {
        try (ProgramaH2 db = new ProgramaH2()) {
            JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(), (i, e, m) -> { });
            assertThatThrownBy(() -> new EjecutorPrograma(runner, null, ProgramaModo.PROGRAMA)).isInstanceOf(NullPointerException.class);
            assertThat(EjecutorPrograma.parametrosDelLlamador(runner).exigeClase()).isTrue();
        }
    }
}
