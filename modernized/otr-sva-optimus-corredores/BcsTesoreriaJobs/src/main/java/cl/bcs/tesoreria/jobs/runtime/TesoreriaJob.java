package cl.bcs.tesoreria.jobs.runtime;

/**
 * One legacy {@code BcsJob} subclass, run in-process by {@link JobRunner} ().
 *
 * <p>Legacy: {@code BcsJobManager/src/cl/bcs/scheduler/BcsJob.java}, {@code transaction(Connection)} (:81) and
 * {@code onError(Throwable)} (:83). The Job Manager selected a job by its fully qualified class name; {@link #clase()}
 * keeps that name as the routing key.
 */
public interface TesoreriaJob {

    /** Legacy fully qualified class name, for example {@code cl.bcs.tesoreria.BcsJobProcesarDistribucion}. */
    String clase();

    /**
     * Legacy {@code transaction(con)}: {@code true} commits, {@code false} rolls back, an exception rolls back and
     * is reported as an error (BcsJob.java:133-159).
     */
    boolean transaction(JobContext ctx) throws Exception;

    /** Legacy {@code onError(t)}, called after the error status was reported (BcsJob.java:159). */
    default void onError(Throwable error) {
    }
}
