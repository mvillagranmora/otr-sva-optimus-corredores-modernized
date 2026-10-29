package cl.bcs.tesoreria.jobs.runtime;

/**
 * Outcome of one run.
 *
 * <p>Legacy: BcsJob.java:133-159. {@code ROLLBACK} is the {@code transaction() == false} branch, which the legacy
 * reported exactly like a commit (, legacy defect kept).
 *
 * @param error the exception of an {@code ERROR} run, {@code null} otherwise
 */
public record JobResultado(String clase, long idPrograma, Desenlace desenlace, Throwable error) {

    public enum Desenlace { COMMIT, ROLLBACK, ERROR }

    public static JobResultado commit(String clase, long idPrograma) {
        return new JobResultado(clase, idPrograma, Desenlace.COMMIT, null);
    }

    public static JobResultado rollback(String clase, long idPrograma) {
        return new JobResultado(clase, idPrograma, Desenlace.ROLLBACK, null);
    }

    public static JobResultado error(String clase, long idPrograma, Throwable error) {
        return new JobResultado(clase, idPrograma, Desenlace.ERROR, error);
    }

    /** The status message: {@code ""} unless ERROR, then the exception message (possibly {@code null}). */
    public String mensaje() {
        return desenlace == Desenlace.ERROR ? error.getMessage() : "";
    }
}
