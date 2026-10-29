package cl.bcs.tesoreria.jobs.guarda;

import cl.bcs.tesoreria.jobs.guarda.MarcaGuarda.Fase;
import cl.bcs.tesoreria.jobs.programa.EjecutorPrograma;
import cl.bcs.tesoreria.jobs.programa.ProgramaModo;
import cl.bcs.tesoreria.jobs.programa.ProgramaRepositorio;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.web.JobCallController;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Step 6 end to end: {@code POST /internal/job-call} with the real runner, executor, repository and guard on the H2 BAT schema. The job writes a
 * row ("TEST.Marca") in its own transaction and calls TomEE collaborators through the guard.
 */
class GuardaProgramaJobCallTest extends GuardaBase {

    private void revision(long id) {
        assertThat(db.fila(id)).containsEntry("idEstado", "ERR").containsEntry("Bloqueo", "S").containsEntry("Intento", 1);
        assertThat((String) db.fila(id).get("Resultado")).startsWith(ProgramaRepositorio.PREFIJO_REVISION);
    }

    @Test
    @DisplayName("G6-J01: success: the job commits, the TomEE call has INTENTO and HECHO, the program ends TER, the job row is kept")
    void caminoFeliz() throws Exception {
        programa(100);

        MockHttpServletResponse r = llamar(mvc(job(true, "CrearOrdenes", "AgregarLog")), 100);

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(mensajes(100)).containsExactly("H4G|INTENTO|100|1|CrearOrdenes|1", "H4G|HECHO|100|1|CrearOrdenes|1");
        assertThat(db.fila(100)).containsEntry("idEstado", "TER").containsEntry("Resultado", "Finalizado correctamente.");
        assertThat(db.marcas()).isEqualTo(1);
        assertThat(contexto.actual()).isEmpty(); // the scope of the run is closed
    }

    @Test
    @DisplayName("G6-J02: the TomEE call fails: FALLO mark, the job rolls back its own row, the marks survive, program in review (the write may be committed)")
    void fallaLaLlamada() throws Exception {
        programa(101);
        comportamientoTomee = n -> {
            throw new IllegalStateException("HTTP 500 simulado");
        };

        MockHttpServletResponse r = llamar(mvc(job(true, "CrearOrdenes")), 101);

        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(mensajes(101)).containsExactly("H4G|INTENTO|101|1|CrearOrdenes|1", "H4G|FALLO|101|1|CrearOrdenes|1");
        assertThat(db.marcas()).isZero(); // the job's own write rolled back, the independent marks did not
        revision(101);
        assertThat((String) db.fila(101).get("Resultado")).contains("CrearOrdenes#1").contains("NOT certain");
    }

    @Test
    @DisplayName("G6-J03: a TomEE write is done (HECHO) and the job then returns false: rollback of its own row, but the program goes to review")
    void rollbackTrasHecho() throws Exception {
        programa(102);

        MockHttpServletResponse r = llamar(mvc(job(false, "CrearOrdenes")), 102);

        assertThat(JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject().get("resultado").getAsString())
                .isEqualTo("ROLLBACK");
        assertThat(db.marcas()).isZero();
        assertThat(mensajes(102)).containsExactly("H4G|INTENTO|102|1|CrearOrdenes|1", "H4G|HECHO|102|1|CrearOrdenes|1");
        revision(102);
        assertThat((String) db.fila(102).get("Resultado")).contains("COMMITTED in TomEE: CrearOrdenes#1");
    }

    @Test
    @DisplayName("G6-J04: a rollback with no guarded call (only AgregarLog) is the legacy TER, not a review")
    void rollbackSinLlamadasGuardadas() throws Exception {
        programa(103);

        llamar(mvc(job(false, "AgregarLog", "ConsultarEstadoModulo")), 103);

        assertThat(db.fila(103)).containsEntry("idEstado", "TER");
        assertThat(mensajes(103)).isEmpty();
    }

    @Test
    @DisplayName("G6-J05: crash simulation: an INTENTO without close of the same program: the run is REFUSED (423), nothing runs, the program is in review")
    void intentoSinCierreSeRechaza() throws Exception {
        programa(104);
        marca(Fase.INTENTO, 104, 1, "CrearOrdenes", 2); // the earlier run died after the call started; someone re-opened the row (PEN, 'N', 0)
        MockMvc mvc = mvc(job(true, "CrearOrdenes"));

        MockHttpServletResponse r = llamar(mvc, 104);

        assertThat(r.getStatus()).isEqualTo(423);
        String cuerpo = r.getContentAsString(StandardCharsets.UTF_8);
        assertThat(cuerpo).contains("requires review").contains("CrearOrdenes#2(attempt 1)").contains("UNKNOWN");
        assertThat(tomee).isEmpty();
        assertThat(db.marcas()).isZero(); // the job did not run
        revision(104);
        assertThat((String) db.fila(104).get("Resultado")).contains("Outcome UNKNOWN").contains("CrearOrdenes#2(attempt 1)");
        assertThat(mensajes(104)).hasSize(1); // the refusal wrote no mark of its own
        assertThat(contexto.actual()).isEmpty();
    }

    @Test
    @DisplayName("G6-J06: HECHO marks of a completed attempt: a re-run of the SAME program is refused even though the earlier run ended normally")
    void hechoRechazaLaRepeticion() throws Exception {
        programa(105);
        MockMvc mvc = mvc(job(true, "CrearOrdenes"));
        assertThat(llamar(mvc, 105).getStatus()).isEqualTo(200);
        db.estado(105, "PEN", 0, "N"); // someone resets the row so that the engine could claim it again

        MockHttpServletResponse r = llamar(mvc, 105);

        assertThat(r.getStatus()).isEqualTo(423);
        assertThat(r.getContentAsString(StandardCharsets.UTF_8)).contains("COMMITTED").contains("CrearOrdenes#1(attempt 1)");
        assertThat(tomee).containsExactly("CrearOrdenes"); // not called a second time
        assertThat(db.marcas()).isEqualTo(1); // the second job run never happened
        assertThat(db.fila(105)).containsEntry("idEstado", "ERR").containsEntry("Bloqueo", "S").containsEntry("Intento", 1);
        assertThat((String) db.fila(105).get("Resultado")).startsWith(ProgramaRepositorio.PREFIJO_REVISION).contains("Already COMMITTED");
    }

    @Test
    @DisplayName("G6-J07: marks of ANOTHER program never refuse this one; a program with only FALLO marks is not refused by them")
    void otrosProgramasNoImportan() throws Exception {
        programa(106);
        marca(Fase.INTENTO, 999, 1, "CrearOrdenes", 1);
        marca(Fase.HECHO, 999, 1, "CrearOrdenes", 1);
        marca(Fase.INTENTO, 106, 1, "AgregarPrograma", 1);
        marca(Fase.FALLO, 106, 1, "AgregarPrograma", 1);

        assertThat(llamar(mvc(job(true, "CrearOrdenes")), 106).getStatus()).isEqualTo(200);

        assertThat(db.fila(106)).containsEntry("idEstado", "TER");
        assertThat(tomee).containsExactly("CrearOrdenes");
    }

    @Test
    @DisplayName("G6-J09: caller mode (no program row, parameters from the request) with the guard present: pass-through, no marks, BAT untouched")
    void modoLlamadorEsPassThrough() throws Exception {
        TesoreriaJob job = job(true, "CrearOrdenes");
        JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), (id, error, mensaje) -> { });
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new JobCallController(
                new EjecutorPrograma(runner, null, ProgramaModo.PARAMETROS_DEL_LLAMADOR, guarda))).build();

        MockHttpServletResponse r = mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON)
                .content("{\"clase\":\"" + CLASE + "\",\"idPrograma\":777,\"parametros\":{}}")).andReturn().getResponse();

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(tomee).containsExactly("CrearOrdenes");
        assertThat(mensajes(777)).isEmpty();
        assertThat(db.marcas()).isEqualTo(1);
    }

    @Test
    @DisplayName("G6-J08: the Intento of the marks is the Intento of the claim (it grows with each claim)")
    void intentoDeLaReclamacion() throws Exception {
        programa(107);
        db.estado(107, "PEN", 2, "N");
        db.jdbc().update("UPDATE \"BSCL.Plataforma.BD::BAT.TipoPrograma\" SET \"MaximoIntentos\" = 5 WHERE \"IdTipoPrograma\" = ?", TIPO + 107);

        llamar(mvc(job(true, "CrearOrdenes")), 107);

        assertThat(mensajes(107)).containsExactly("H4G|INTENTO|107|3|CrearOrdenes|1", "H4G|HECHO|107|3|CrearOrdenes|1");
    }
}
