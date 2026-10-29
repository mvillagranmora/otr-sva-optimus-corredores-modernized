package cl.bcs.contabilidad.jobs.runtime;

import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.web.JobCallController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * RULE-061 (P0, BUSINESS_RULES.md): legacy {@code BcsJob.run()} has a daemon branch (BcsJob.java:90-125): when the
 * agenda flags the program {@code EsDemonio='S'}, {@code transaction()} runs in an endless loop, commit or rollback per
 * cycle, sleeping the cycle delay (5 s for {@code Minuto='**'}, else {@code Minuto} minutes) or 60 s after an exception.
 *
 * <p>Decision (user, 2026-10-16): none of the five Contabilidad jobs is a daemon. The repository configures daemon
 * agendas only for Orden jobs ({@code BSCL/Orden/BD/Scripts/ConfiguracionJobs/*}); the Contabilidad program types
 * ({@code InsertTipoProgramaSIGA.sql}) carry none. So the daemon branch is NOT ported: one {@code /internal/job-call}
 * is one {@code transaction()}, and a future daemon cycle belongs to the Job Manager (Phase 5), which would call the
 * endpoint once per cycle. These tests pin that decision so a loop cannot creep in unnoticed.
 */
class Rule061SinModoDemonioTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.Prueba";
    static final Pattern DEMONIO = Pattern.compile("(?i)daemon|dameon|demonio|delay|intervalo|ciclo|sleep|minuto");

    record Job(JobRunnerContractTest.Job.Cuerpo cuerpo) implements ContabilidadJob {
        @Override
        public String clase() {
            return CLASE;
        }

        @Override
        public boolean transaction(JobContext ctx) throws Exception {
            return cuerpo.run(ctx);
        }
    }

    private static JobResultado unaVez(H2Jobs db, AtomicInteger veces, JobRunnerContractTest.Job.Cuerpo cuerpo) {
        return assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                db.runner(new Job(ctx -> { veces.incrementAndGet(); return cuerpo.run(ctx); })).ejecutar(CLASE, 61L, Map.of()));
    }

    @Test
    @DisplayName("RULE-061: transaction() true -> exactly one cycle, COMMIT, the call returns (no daemon loop)")
    void rule061_unaEjecucionPorLlamada_commit() {
        // BcsJob.java:126-151 (non-daemon branch) instead of :90-125 (daemon while(true))
        H2Jobs db = H2Jobs.nueva();
        AtomicInteger veces = new AtomicInteger();
        assertThat(unaVez(db, veces, ctx -> true).desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(veces).hasValue(1);
    }

    @Test
    @DisplayName("RULE-061: transaction() false -> one cycle, ROLLBACK, no retry and no cycle delay")
    void rule061_unaEjecucionPorLlamada_rollback() {
        // the daemon branch would roll back and sleep the cycle delay, then run again (BcsJob.java:100-104, :121-123)
        H2Jobs db = H2Jobs.nueva();
        AtomicInteger veces = new AtomicInteger();
        assertThat(unaVez(db, veces, ctx -> false).desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(veces).hasValue(1);
    }

    @Test
    @DisplayName("RULE-061: an exception -> one cycle, ERROR, no 60 s cooldown and no retry after the exception")
    void rule061_unaEjecucionPorLlamada_excepcion() {
        // the daemon branch would log, sleep 60 000 ms and loop again (BcsJob.java:116-119). A sequential second call
        // after a normal return is caught by the commit and rollback cases above, not here (review M4).
        H2Jobs db = H2Jobs.nueva();
        AtomicInteger veces = new AtomicInteger();
        JobResultado r = unaVez(db, veces, ctx -> { throw new IllegalStateException("falla"); });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(veces).hasValue(1);
    }

    @Test
    @DisplayName("RULE-061: neither the job contract nor /internal/job-call carries a daemon flag or a cycle delay")
    void rule061_sinParametrosDeDemonio() {
        // legacy setParameters(jobId, parameters, ms, isDaemon, datasource, minuto) (BcsJob.java:35-52) took both
        assertThat(Arrays.stream(JobCallController.JobCall.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("clase", "idPrograma", "parametros");
        assertThat(Arrays.stream(ContabilidadJob.class.getMethods()).map(Method::getName))
                .noneMatch(n -> DEMONIO.matcher(n).find());
        assertThat(Arrays.stream(JobRunner.class.getMethods()).map(Method::getName))
                .noneMatch(n -> DEMONIO.matcher(n).find());
    }
}
