package cl.bcs.jobmanager.exception;

/**
 * Domain exception for BcsJobManager errors.
 * Migrated from legacy cl.bcs.scheduler.BcsJobManagerException.
 */
public class BcsJobManagerException extends Exception {

    private static final long serialVersionUID = 1L;

    public BcsJobManagerException(Throwable t) {
        super(t);
    }

    public BcsJobManagerException(String msg) {
        super(msg);
    }

    public BcsJobManagerException(Throwable t, String msg) {
        super(msg, t);
    }
}
