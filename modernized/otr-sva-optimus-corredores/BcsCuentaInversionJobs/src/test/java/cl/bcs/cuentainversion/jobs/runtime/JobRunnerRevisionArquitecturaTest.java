package cl.bcs.cuentainversion.jobs.runtime;

import cl.bcs.cuentainversion.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.cuentainversion.jobs.support.H2Jobs;
import cl.bcs.cuentainversion.jobs.web.JobCallController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fixes for the architecture review of 2026-10-16, pinned against the legacy job framework
 * ({@code BcsPlataforma/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java}):
 * <ul>
 *   <li>CIJ-100/101 (HIGH-1): every transaction of a run is prepared with the session statements of
 *       BcsJob.java:130-132;</li>
 *   <li>CIJ-102/103 (HIGH-2): a failed intermediate commit never leaves the run in auto-commit;</li>
 *   <li>CIJ-104/105 (HIGH-3): one run per program and per job class at a time; HTTP 409 for the duplicate.</li>
 * </ul>
 */
class JobRunnerRevisionArquitecturaTest {

    static final String CLASE = "cl.bcs.cuentaInversion.Prueba";

    record Job(String clase, JobRunnerContractTest.Job.Cuerpo cuerpo) implements CuentaInversionJob {
        Job(JobRunnerContractTest.Job.Cuerpo cuerpo) {
            this(CLASE, cuerpo);
        }

        @Override
        public boolean transaction(JobContext ctx) throws Exception {
            return cuerpo.run(ctx);
        }
    }

    /** Transaction manager over the H2 fixture whose n-th physical commit or begin fails. */
    static final class Fallador extends DataSourceTransactionManager {
        private final int commitQueFalla;
        private final int inicioQueFalla;
        private final AtomicInteger commits = new AtomicInteger();
        private final AtomicInteger inicios = new AtomicInteger();

        Fallador(H2Jobs db, int commitQueFalla, int inicioQueFalla) {
            super(db.dataSource());
            this.commitQueFalla = commitQueFalla;
            this.inicioQueFalla = inicioQueFalla;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            if (inicios.incrementAndGet() == inicioQueFalla) {
                throw new org.springframework.transaction.CannotCreateTransactionException("pool exhausted (test)");
            }
            super.doBegin(transaction, definition);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            if (commits.incrementAndGet() == commitQueFalla) {
                throw new TransactionSystemException("commit failed (test)");
            }
            super.doCommit(status);
        }
    }

    @Test
    @DisplayName("CIJ-100: the session statements run at the start of the run AND after every intermediate commit")
    void tesJ100_sesionEnCadaTransaccion() {
        // BcsJob.java:130-132: SET SCHEMA + SET TRANSACTION AUTOCOMMIT DDL OFF on the job connection
        H2Jobs db = H2Jobs.nueva();
        List<String> sesion = List.of("INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES ('SESION')");
        JobRunner runner = new JobRunner(db.transactionManager(), db.jdbc(), List.of(new Job(ctx -> {
            db.marcar("A");
            ctx.commit();
            db.marcar("B");
            return true;
        })), db.estados(), sesion);

        assertThat(runner.ejecutar(CLASE, 100L, Map.of()).desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.marcas()).containsExactly("SESION", "A", "SESION", "B");
        assertThat(JobRunner.SESION_HANA).containsExactly("SET SCHEMA BSCL", "SET TRANSACTION AUTOCOMMIT DDL OFF");
    }

    @Test
    @DisplayName("CIJ-101: a failing session statement fails the run before the job body runs; nothing persists")
    void tesJ101_sesionFallida() {
        H2Jobs db = H2Jobs.nueva();
        AtomicInteger cuerpos = new AtomicInteger();
        JobRunner runner = new JobRunner(db.transactionManager(), db.jdbc(),
                List.of(new Job(ctx -> { cuerpos.incrementAndGet(); db.marcar("A"); return true; })),
                db.estados(), List.of("SET TRANSACTION AUTOCOMMIT DDL OFF"));   // HANA-only syntax: fails on H2

        assertThat(runner.ejecutar(CLASE, 101L, Map.of()).desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(cuerpos).hasValue(0);
        assertThat(db.marcas()).isEmpty();
    }

    @Test
    @DisplayName("CIJ-102: a failed intermediate commit still leaves a transaction open: later writes follow the run outcome")
    void tesJ102_commitFallidoNoDejaAutocommit() {
        // InterfazContable.java:244 committed on the job connection; a failure there never turned the rest of the
        // run into auto-commit. The job swallows the failure like InterfazContableService does per voucher.
        H2Jobs db = H2Jobs.nueva();
        JobRunner runner = new JobRunner(new Fallador(db, 1, 0), db.jdbc(), List.of(new Job(ctx -> {
            db.marcar("A");
            assertThatThrownBy(ctx::commit).isInstanceOf(TransactionSystemException.class);
            db.marcar("B");
            return false;
        })), db.estados());

        assertThat(runner.ejecutar(CLASE, 102L, Map.of()).desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(db.marcas()).as("B must be rolled back with the run, not auto-committed").doesNotContain("B");
    }

    @Test
    @DisplayName("CIJ-103: no transaction can be reopened after a commit -> the run aborts with ERROR; a catch (RuntimeException) cannot swallow it")
    void tesJ103_sinTransaccionSeAborta() {
        H2Jobs db = H2Jobs.nueva();
        AtomicInteger despues = new AtomicInteger();
        JobRunner runner = new JobRunner(new Fallador(db, 0, 2), db.jdbc(), List.of(new Job(ctx -> {
            db.marcar("A");
            try {
                ctx.commit();
            } catch (RuntimeException tragada) {
                // what a per-item catch in a service does
            }
            despues.incrementAndGet();
            db.marcar("B");
            return true;
        })), db.estados());

        JobResultado r = runner.ejecutar(CLASE, 103L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(JobRunner.CorridaInterrumpidaError.class);
        assertThat(despues).hasValue(0);
        assertThat(db.marcas()).as("A was committed; nothing ran in auto-commit afterwards").containsExactly("A");
    }

    @Test
    @DisplayName("CIJ-104: one run per program and per job class at a time; free again when it ends")
    void tesJ104_unaCorridaALaVez() throws Exception {
        // The legacy Job Manager claimed each BAT.Programa row before running it, so a program never ran twice.
        H2Jobs db = H2Jobs.nueva();
        CountDownLatch dentro = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        JobRunner runner = db.runner(new Job(ctx -> {
            dentro.countDown();
            assertThat(soltar.await(10, TimeUnit.SECONDS)).isTrue();
            return true;
        }));

        CompletableFuture<JobResultado> primera = CompletableFuture.supplyAsync(() -> runner.ejecutar(CLASE, 104L, Map.of()));
        assertThat(dentro.await(10, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> runner.ejecutar(CLASE, 104L, Map.of())).isInstanceOf(JobEnCursoException.class);
        assertThatThrownBy(() -> runner.ejecutar(CLASE, 999L, Map.of())).isInstanceOf(JobEnCursoException.class);

        soltar.countDown();
        assertThat(primera.get(10, TimeUnit.SECONDS).desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(runner.ejecutar(CLASE, 104L, Map.of()).desenlace()).isEqualTo(Desenlace.COMMIT);
    }

    @Test
    @DisplayName("CIJ-105: /internal/job-call answers 409 for a duplicate run, without running it")
    void tesJ105_http409() throws Exception {
        H2Jobs db = H2Jobs.nueva();
        CountDownLatch dentro = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        AtomicInteger corridas = new AtomicInteger();
        JobRunner runner = db.runner(new Job(ctx -> {
            corridas.incrementAndGet();
            dentro.countDown();
            assertThat(soltar.await(10, TimeUnit.SECONDS)).isTrue();
            return true;
        }));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build();
        String cuerpo = "{\"clase\":\"" + CLASE + "\",\"idPrograma\":105,\"parametros\":{}}";

        CompletableFuture<Void> primera = CompletableFuture.runAsync(() -> {
            try {
                mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        assertThat(dentro.await(10, TimeUnit.SECONDS)).isTrue();

        mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());

        soltar.countDown();
        primera.get(10, TimeUnit.SECONDS);
        assertThat(corridas).hasValue(1);
    }
}
