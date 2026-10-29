package cl.bcs.contabilidad.jobs.runtime;

import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The legacy job framework contract, {@code BcsPlataforma/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java}, that every
 * in-process job of this module runs under (non-daemon branch, BcsJob.java:126-151).
 *
 * <ul>
 *   <li>one connection per run; {@code transaction(con)} true = commit (:133-135), false = rollback (:136-138);</li>
 *   <li>an exception rolls back (:140-144) and reports {@code status(jobId, true, e.getMessage())} then
 *       {@code onError(e)} (:154-159);</li>
 *   <li>success AND rollback both report {@code status(jobId, false, "")} (:151): a rollback is not an error for the
 *       Job Manager (legacy defect, kept);</li>
 *   <li>{@code getParam} (:71-73), {@code getOptionalParam} (:75-77), {@code getParamInteger} (:54-61);</li>
 *   <li>{@code updateRutaSalida(con, ruta, idPrograma)} runs {@code UPDATE BAT.Programa SET RutaSalida} on the job
 *       connection (:171-185), so it commits or rolls back with the job.</li>
 * </ul>
 */
class JobRunnerContractTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.Prueba";

    /** Minimal job whose body is a lambda. */
    record Job(String clase, Cuerpo cuerpo, List<Throwable> errores) implements ContabilidadJob {
        interface Cuerpo {
            boolean run(JobContext ctx) throws Exception;
        }

        Job(Cuerpo cuerpo) {
            this(CLASE, cuerpo, new ArrayList<>());
        }

        @Override
        public boolean transaction(JobContext ctx) throws Exception {
            return cuerpo.run(ctx);
        }

        @Override
        public void onError(Throwable t) {
            errores.add(t);
        }
    }

    @Test
    @DisplayName("CONT-J-01: transaction() true -> COMMIT, the job's writes persist, status(id, false, \"\")")
    void contJ01_true_commit() {
        // BcsJob.java:133-135, :151
        H2Jobs db = H2Jobs.nueva();
        JobResultado r = db.runner(new Job(ctx -> { db.marcar("A"); return true; })).ejecutar(CLASE, 41L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(r.error()).isNull();
        assertThat(db.marcas()).containsExactly("A");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(41L, false, ""));
    }

    @Test
    @DisplayName("CONT-J-02: transaction() false -> ROLLBACK, writes discarded, but status(id, false, \"\") as if it had succeeded")
    void contJ02_false_rollbackReportadoComoExito() {
        // BcsJob.java:136-138 then :151 — LEGACY DEFECT kept: the listener cannot tell a rollback from a commit.
        H2Jobs db = H2Jobs.nueva();
        JobResultado r = db.runner(new Job(ctx -> { db.marcar("A"); return false; })).ejecutar(CLASE, 42L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(db.marcas()).isEmpty();
        assertThat(db.estados().llamadas()).containsExactly(new Estado(42L, false, ""));
    }

    @Test
    @DisplayName("CONT-J-03: exception -> ROLLBACK of the job's writes, status(id, true, message), onError(same exception)")
    void contJ03_excepcion_rollbackYEstadoError() {
        // BcsJob.java:140-144 (rollback + rethrow), :154-159 (status error + onError)
        H2Jobs db = H2Jobs.nueva();
        IllegalStateException boom = new IllegalStateException("fallo en paso 2");
        Job job = new Job(ctx -> { db.marcar("A"); throw boom; });
        JobResultado r = db.runner(job).ejecutar(CLASE, 43L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isSameAs(boom);
        assertThat(r.mensaje()).isEqualTo("fallo en paso 2");
        assertThat(db.marcas()).isEmpty();
        assertThat(db.estados().llamadas()).containsExactly(new Estado(43L, true, "fallo en paso 2"));
        assertThat(job.errores()).containsExactly(boom);
    }

    @Test
    @DisplayName("CONT-J-04: exception without message -> status(id, true, null)")
    void contJ04_excepcionSinMensaje_estadoConMensajeNulo() {
        // BcsJob.java:158 passes e.getMessage() as is.
        H2Jobs db = H2Jobs.nueva();
        JobResultado r = db.runner(new Job(ctx -> { throw new Exception(); })).ejecutar(CLASE, 44L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(44L, true, null));
    }

    @Test
    @DisplayName("CONT-J-05: ctx.commit() inside the job (legacy con.commit()) keeps earlier writes when the job later fails")
    void contJ05_commitIntermedio_sobreviveAFallaPosterior() {
        // Legacy jobs call con.commit() on the job connection (InterfazContable.java:244, CentralizaMultiSeleccionJob.java:91).
        H2Jobs db = H2Jobs.nueva();
        JobResultado r = db.runner(new Job(ctx -> {
            db.marcar("antes");
            ctx.commit();
            db.marcar("despues");
            throw new IllegalStateException("x");
        })).ejecutar(CLASE, 45L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.marcas()).containsExactly("antes");
    }

    @Test
    @DisplayName("CONT-J-06: ctx.rollback() inside the job discards earlier writes; the job continues and its later writes commit")
    void contJ06_rollbackIntermedio() {
        // CentralizaMultiSeleccionJob.java:96 calls con.rollback() itself before rethrowing.
        H2Jobs db = H2Jobs.nueva();
        JobResultado r = db.runner(new Job(ctx -> {
            db.marcar("descartada");
            ctx.rollback();
            db.marcar("final");
            return true;
        })).ejecutar(CLASE, 46L, Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.marcas()).containsExactly("final");
    }

    @Test
    @DisplayName("CONT-J-07: updateRutaSalida(ruta, idPrograma) updates BAT.Programa.RutaSalida of that IdPrograma only, inside the job transaction")
    void contJ07_updateRutaSalida() {
        // BcsJob.java:171 UPDATE "BSCL.Plataforma.BD::BAT.Programa" SET "RutaSalida" = :RutaSalida WHERE "IdPrograma" = :IdPrograma
        H2Jobs db = H2Jobs.nueva().programa(47L, "VIEJA").programa(48L, "OTRA");
        db.runner(new Job(ctx -> { ctx.updateRutaSalida("ITZ/salida.log", 47L); return true; })).ejecutar(CLASE, 47L, Map.of());

        assertThat(db.rutaSalida(47L)).isEqualTo("ITZ/salida.log");
        assertThat(db.rutaSalida(48L)).isEqualTo("OTRA");

        db.runner(new Job(ctx -> { ctx.updateRutaSalida("NO-QUEDA", 47L); return false; })).ejecutar(CLASE, 47L, Map.of());
        assertThat(db.rutaSalida(47L)).as("rolled back with the job").isEqualTo("ITZ/salida.log");

        db.runner(new Job(ctx -> { ctx.updateRutaSalida(null, 47L); return true; })).ejecutar(CLASE, 47L, Map.of());
        assertThat(db.rutaSalida(47L)).as("a null ruta is written as NULL").isNull();
    }

    @Test
    @DisplayName("CONT-J-08: getParam / getOptionalParam / getParamInteger keep the BcsJob semantics; idPrograma is the job id")
    void contJ08_parametros() {
        // BcsJob.java:54-61 (NumberFormatException -> null), :71-73, :75-77, :163-165
        H2Jobs db = H2Jobs.nueva();
        AtomicReference<List<Object>> visto = new AtomicReference<>();
        Map<String, String> p = new HashMap<>();
        p.put("Texto", "abc");
        p.put("Numero", "12");
        p.put("Vacio", "");
        db.runner(new Job(ctx -> {
            List<Object> v = new ArrayList<>();
            v.add(ctx.idPrograma());
            v.add(ctx.getParam("Texto"));
            v.add(ctx.getParam("NoExiste"));
            v.add(ctx.getOptionalParam("Texto"));
            v.add(ctx.getOptionalParam("NoExiste"));
            v.add(ctx.getOptionalParam("Vacio"));
            v.add(ctx.getParamInteger("Numero"));
            v.add(String.valueOf(ctx.getParamInteger("Texto")));
            v.add(String.valueOf(ctx.getParamInteger("NoExiste")));
            v.add(ctx.getParam("texto"));
            visto.set(v);
            return true;
        })).ejecutar(CLASE, 4711L, p);

        assertThat(visto.get()).containsExactly(4711L, "abc", null, Optional.of("abc"), Optional.empty(), Optional.of(""),
                12, "null", "null", null);
    }

    @Test
    @DisplayName("CONT-J-09: an unregistered class is not run: JobNoMigradoException, no transaction opened, no status reported")
    void contJ09_claseDesconocida() {
        H2Jobs db = H2Jobs.nueva();
        JobRunner runner = db.runner(new Job(ctx -> true));

        assertThat(runner.soporta(CLASE)).isTrue();
        assertThat(runner.soporta("cl.bcs.contabilidad.jobs.NoExiste")).isFalse();
        assertThat(runner.soporta(null)).isFalse();
        assertThatThrownBy(() -> runner.ejecutar("cl.bcs.contabilidad.jobs.NoExiste", 1L, Map.of()))
                .isInstanceOf(JobNoMigradoException.class)
                .hasMessageContaining("cl.bcs.contabilidad.jobs.NoExiste");
        assertThat(db.transactionManager().inicios()).isZero();
        assertThat(db.estados().llamadas()).isEmpty();
    }

    @Test
    @DisplayName("CONT-J-09b: two jobs with the same legacy class name are rejected at construction")
    void contJ09b_claseDuplicada() {
        H2Jobs db = H2Jobs.nueva();
        assertThatThrownBy(() -> db.runner(new Job(ctx -> true), new Job(ctx -> false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(CLASE);
    }

    @Test
    @DisplayName("CONT-J-09c: parameters belong to one run; a later run does not see them")
    void contJ09c_parametrosPorEjecucion() {
        H2Jobs db = H2Jobs.nueva();
        List<String> vistos = new ArrayList<>();
        JobRunner runner = db.runner(new Job(ctx -> { vistos.add(ctx.getParam("P")); return true; }));
        runner.ejecutar(CLASE, 1L, Map.of("P", "uno"));
        runner.ejecutar(CLASE, 2L, Map.of());

        assertThat(vistos).containsExactly("uno", null);
        assertThat(db.estados().llamadas()).extracting(Estado::idPrograma).containsExactly(1L, 2L);
    }
}
