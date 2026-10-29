package cl.bcs.apv.jobs.runtime;

/**
 * The same program, or another run of the same job class, is already running in this instance (review HIGH-3).
 * The legacy Job Manager claimed each {@code BAT.Programa} row before running it (BcsJobManager poolJobs), so a
 * program never ran twice at once; a retried or duplicated {@code /internal/job-call} must not either.
 */
public class JobEnCursoException extends RuntimeException {

    private final String clase;
    private final long idPrograma;

    public JobEnCursoException(String clase, long idPrograma) {
        super("Job '" + clase + "' #" + idPrograma + " not started: the same program or job class is already running");
        this.clase = clase;
        this.idPrograma = idPrograma;
    }

    public String clase() {
        return clase;
    }

    public long idPrograma() {
        return idPrograma;
    }
}
