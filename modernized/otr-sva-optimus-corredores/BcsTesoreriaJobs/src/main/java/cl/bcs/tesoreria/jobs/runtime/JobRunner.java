package cl.bcs.tesoreria.jobs.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Runs one in-process job in ONE transaction with the legacy {@code BcsJob} contract (see JobRunnerContractTest).
 *
 * <p>Legacy: {@code BcsJob.run()}, non-daemon branch (BcsJob.java:126-160): one connection per run, prepared with
 * {@code SET SCHEMA BSCL} and {@code SET TRANSACTION AUTOCOMMIT DDL OFF} (:130-132); {@code transaction(con)} true
 * commits (:133-135), false rolls back (:136-138), an exception rolls back (:140-144), then
 * {@code status(jobId, true, e.getMessage())} and {@code onError(e)} (:154-159). Commit AND rollback report
 * {@code status(jobId, false, "")} (:151), so the listener cannot tell them apart (legacy defect TES-J-DEF-19, kept).
 * The job's own {@code con.commit()} / {@code con.rollback()} become {@link JobContext#commit()} /
 * {@link JobContext#rollback()}; {@code updateRutaSalida} runs on the job transaction (:171-185).
 *
 * <p>Architecture review (2026-10-16):
 * <ul>
 *   <li>HIGH-1: every transaction the run opens is prepared with the session statements (production: the two
 *       legacy ones), so a DDL statement inside a centralization cannot commit the open transaction.</li>
 *   <li>HIGH-2: an intermediate commit always leaves a transaction open. If the commit fails, a new transaction is
 *       still started and the failure is rethrown (the job handles it as legacy did); if no new transaction can be
 *       started the run is aborted with {@link CorridaInterrumpidaError}, which per-item {@code catch
 *       (RuntimeException)} blocks cannot swallow, so nothing ever runs in auto-commit.</li>
 *   <li>HIGH-3: at most one run per {@code IdPrograma} and per job class at a time in this instance
 *       ({@link JobEnCursoException}, HTTP 409); the legacy Job Manager claimed each program before running it.</li>
 * </ul>
 * Jobs are keyed by their legacy class name, which is case-sensitive; a duplicate name is a configuration error.
 */
public class JobRunner {

    static final String SQL_UPDATE_RUTA_SALIDA =
            "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = ? WHERE \"IdPrograma\" = ?";

    /** BcsJob.java:130-132 ({@code Utils.getSchema()} is the constant "BSCL", BcsUtils Utils.java:113-115). */
    public static final List<String> SESION_HANA = List.of("SET SCHEMA BSCL", "SET TRANSACTION AUTOCOMMIT DDL OFF");

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    private static final TransactionDefinition TRANSACCION_PROPIA =
            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    private final PlatformTransactionManager transacciones;
    private final JdbcTemplate jdbc;
    private final Map<String, TesoreriaJob> jobs;
    private final JobStatusListener listener;
    private final List<String> sentenciasSesion;
    private final Set<Long> programasEnCurso = ConcurrentHashMap.newKeySet();
    private final Set<String> clasesEnCurso = ConcurrentHashMap.newKeySet();

    /** Without session statements (H2 and other non-HANA databases). */
    public JobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc,
                     List<? extends TesoreriaJob> jobs, JobStatusListener listener) {
        this(transacciones, jdbc, jobs, listener, List.of());
    }

    public JobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc,
                     List<? extends TesoreriaJob> jobs, JobStatusListener listener, List<String> sentenciasSesion) {
        this.transacciones = Objects.requireNonNull(transacciones, "transacciones");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.sentenciasSesion = List.copyOf(sentenciasSesion);
        Map<String, TesoreriaJob> porClase = new LinkedHashMap<>();
        for (TesoreriaJob job : jobs) {
            if (porClase.putIfAbsent(job.clase(), job) != null) {
                throw new IllegalStateException("Two jobs are registered for the legacy class " + job.clase());
            }
        }
        this.jobs = Collections.unmodifiableMap(porClase);
    }

    public boolean soporta(String clase) {
        return clase != null && jobs.containsKey(clase);
    }

    public Set<String> clases() {
        return jobs.keySet();
    }

    /**
     * Runs the job registered for {@code clase}.
     *
     * @throws JobNoMigradoException when no job is registered for it; nothing is started or reported then
     * @throws JobEnCursoException   when the same program or the same job class is already running here
     */
    public JobResultado ejecutar(String clase, long idPrograma, Map<String, String> parametros) {
        return ejecutarReclamando(clase, idPrograma, () -> parametros);
    }

    /**
     * Job pattern H4: like {@link #ejecutar} but the parameters come from {@code reclamo}, which is called AFTER the per-JVM guards
     * were taken (so the JVM guard is the first net and the database claim the second) and BEFORE the job transaction opens. It
     * claims the program in the database and returns its parameters; if it throws (for example the program was already claimed) nothing
     * runs and the guards are released. It must not fail after it has claimed.
     */
    public JobResultado ejecutarReclamando(String clase, long idPrograma, Supplier<Map<String, String>> reclamo) {
        TesoreriaJob job = clase == null ? null : jobs.get(clase);
        if (job == null) {
            throw new JobNoMigradoException(clase);
        }
        if (!programasEnCurso.add(idPrograma)) {
            throw new JobEnCursoException(clase, idPrograma);
        }
        if (!clasesEnCurso.add(clase)) {
            programasEnCurso.remove(idPrograma);
            throw new JobEnCursoException(clase, idPrograma);
        }
        MDC.put("jobid", Long.toString(idPrograma));
        try {
            return correr(job, clase, idPrograma, reclamo.get());
        } finally {
            MDC.remove("jobid");
            clasesEnCurso.remove(clase);
            programasEnCurso.remove(idPrograma);
        }
    }

    private JobResultado correr(TesoreriaJob job, String clase, long idPrograma, Map<String, String> parametros) {
        Corrida corrida = new Corrida(idPrograma, parametros == null ? Map.of() : parametros);
        log.info("Job {} #{} started", clase, idPrograma);
        JobResultado resultado;
        try {
            corrida.iniciar();
            if (job.transaction(corrida)) {
                corrida.confirmar();
                resultado = JobResultado.commit(clase, idPrograma);
            } else {
                corrida.descartar();
                resultado = JobResultado.rollback(clase, idPrograma);
            }
        } catch (Exception | Error e) {
            // BcsJob.run() caught Throwable: every failure is rolled back and reported, Errors included.
            corrida.descartarSiAbierta();
            if (e instanceof InterruptedException) {
                Thread.interrupted(); // do not leave the pooled worker thread interrupted (review L2)
            }
            log.error("Job {} #{} failed", clase, idPrograma, e);
            // review M8: a failing listener or onError must not lose the JobResultado
            informar(clase, idPrograma, true, e.getMessage());
            try {
                job.onError(e);
            } catch (Exception | Error fallo) {
                log.error("Job {} #{}: onError failed", clase, idPrograma, fallo);
            }
            return JobResultado.error(clase, idPrograma, e);
        }
        log.info("Job {} #{} finished: {}", clase, idPrograma, resultado.desenlace());
        informar(clase, idPrograma, false, "");
        return resultado;
    }

    /** Reports the status; a failure is logged and never turns a finished run into an error (review M8). */
    private void informar(String clase, long idPrograma, boolean error, String mensaje) {
        try {
            listener.status(idPrograma, error, mensaje);
        } catch (Exception | Error fallo) {
            log.error("Job {} #{}: the status listener failed; the run result is kept", clase, idPrograma, fallo);
        }
    }

    /**
     * The run could not open a transaction after an intermediate commit or rollback. It is an {@link Error} on
     * purpose: per-item {@code catch (RuntimeException)} blocks in the services must not swallow it, or the rest of
     * the run would execute in auto-commit (review HIGH-2).
     */
    public static final class CorridaInterrumpidaError extends Error {
        CorridaInterrumpidaError(long idPrograma, Throwable causa) {
            super("Job #" + idPrograma + ": no transaction could be started after an intermediate commit or rollback",
                    causa);
        }
    }

    /** One run: its parameters and its current transaction. */
    private final class Corrida implements JobContext {

        private final long idPrograma;
        private final Map<String, String> parametros;
        private TransactionStatus transaccion;

        Corrida(long idPrograma, Map<String, String> parametros) {
            this.idPrograma = idPrograma;
            this.parametros = Collections.unmodifiableMap(new HashMap<>(parametros));
        }

        void iniciar() {
            transaccion = transacciones.getTransaction(TRANSACCION_PROPIA);
            try {
                sentenciasSesion.forEach(jdbc::execute);
            } catch (RuntimeException e) {
                transacciones.rollback(transaccion);
                throw e;
            }
        }

        void confirmar() {
            transacciones.commit(transaccion);
        }

        void descartar() {
            transacciones.rollback(transaccion);
        }

        void descartarSiAbierta() {
            if (transaccion == null || transaccion.isCompleted()) {
                return;
            }
            try {
                transacciones.rollback(transaccion);
            } catch (RuntimeException e) {
                log.warn("Job #{}: rollback after failure also failed: {}", idPrograma, e.toString());
            }
        }

        /** Starts the next transaction of the run; if that is impossible the run is aborted. */
        private void reabrir() {
            try {
                iniciar();
            } catch (RuntimeException e) {
                throw new CorridaInterrumpidaError(idPrograma, e);
            }
        }

        @Override
        public long idPrograma() {
            return idPrograma;
        }

        @Override
        public String getParam(String nombre) {
            return parametros.get(nombre);
        }

        @Override
        public Optional<String> getOptionalParam(String nombre) {
            return Optional.ofNullable(parametros.get(nombre));
        }

        @Override
        public Integer getParamInteger(String nombre) {
            try {
                return Integer.parseInt(parametros.get(nombre));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public void commit() {
            try {
                confirmar();
            } finally {
                reabrir();
            }
        }

        @Override
        public void rollback() {
            try {
                descartar();
            } finally {
                reabrir();
            }
        }

        @Override
        public void updateRutaSalida(String rutaSalida, Long idPrograma) {
            jdbc.update(SQL_UPDATE_RUTA_SALIDA, rutaSalida, idPrograma);
        }
    }
}
