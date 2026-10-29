package cl.bcs.contabilidad.jobs.runtime;

/**
 * The requested legacy class is not run by this module (CONT-J-09, CONT-J-91, CONT-J-96); the legacy Job Manager
 * keeps running it.
 */
public class JobNoMigradoException extends RuntimeException {

    private final String clase;

    public JobNoMigradoException(String clase) {
        super("Job class '" + clase + "' is not run by bcs-contabilidad-jobs; the legacy Job Manager keeps it");
        this.clase = clase;
    }

    public String clase() {
        return clase;
    }
}
