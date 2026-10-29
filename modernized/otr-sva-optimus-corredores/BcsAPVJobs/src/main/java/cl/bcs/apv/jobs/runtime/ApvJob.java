package cl.bcs.apv.jobs.runtime;

/**
 * One legacy {@code BcsJob} subclass of BcsAPVJobs, run in-process by {@link JobRunner}.
 * Legacy: {@code BcsJobManager/src/cl/bcs/scheduler/BcsJob.java}, {@code transaction(Connection)} and {@code onError(Throwable)}.
 * The Job Manager selected a job by its fully qualified class name; {@link #clase()} keeps that name as the routing key.
 */
public interface ApvJob {

    /** Legacy fully qualified class name, for example {@code cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired}. */
    String clase();

    /** Legacy {@code transaction(con)}: true commits, false rolls back, an exception rolls back and is reported as an error. */
    boolean transaction(JobContext ctx) throws Exception;

    /** Legacy {@code onError(t)}, called after the error status was reported. */
    default void onError(Throwable error) {
    }
}
