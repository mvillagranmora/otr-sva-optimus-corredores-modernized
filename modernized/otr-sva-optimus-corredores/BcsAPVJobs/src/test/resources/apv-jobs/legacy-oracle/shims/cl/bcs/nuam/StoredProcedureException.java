package cl.bcs.nuam;

/** ORACLE SHIM: the legacy cl.bcs.nuam.StoredProcedureException belongs to a platform library that is not in the repository (only its name and use are known). */
public class StoredProcedureException extends Exception {
    public StoredProcedureException(String message) {
        super(message);
    }
}
