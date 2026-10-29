package cl.bcs.apv.jobs.runtime;

/**
 * The requested legacy class is not run by this module (APVJ-09, APVJ-91, APVJ-96); the legacy Job Manager
 * keeps running it.
 */
public class JobNoMigradoException extends RuntimeException {

    private final String clase;

    public JobNoMigradoException(String clase) {
        super("Job class '" + clase + "' is not run by bcs-apv-jobs; the legacy Job Manager keeps it");
        this.clase = clase;
    }

    public String clase() {
        return clase;
    }
}
