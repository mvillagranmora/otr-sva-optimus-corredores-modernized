package cl.bcs.apv.jobs.job;

/** Legacy exception class name kept (cl.bcs.nuam.StoredProcedureException): a configuration parameter read through a HANA procedure does not exist. */
public class StoredProcedureException extends Exception {

    public StoredProcedureException(String message) {
        super(message);
    }
}
