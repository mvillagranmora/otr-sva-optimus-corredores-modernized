package cl.bcs.tesoreria.jobs.runtime;

/**
 * Receives the outcome of each run.
 *
 * <p>Legacy: {@code BcsJobListener.status(jobId, error, mensaje)}, called with {@code (id, false, "")} after a commit
 * AND after a rollback (BcsJob.java:151), and with {@code (id, true, e.getMessage())} after an exception (:158).
 */
@FunctionalInterface
public interface JobStatusListener {

    void status(long idPrograma, boolean error, String mensaje);
}
