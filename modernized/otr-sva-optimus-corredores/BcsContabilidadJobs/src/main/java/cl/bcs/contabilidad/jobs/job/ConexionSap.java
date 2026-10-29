package cl.bcs.contabilidad.jobs.job;

/**
 * Obtains the SAP B1 database connection before the interface files are generated (CONT-J-38, CONT-J-56).
 *
 * <p>Legacy: CreaInterfazDiaria.java:111-113 and CreaInterfazDemanda.java:112-114, {@code dsb1.getConnection()} on
 * the platform-managed SAP B1 pool, inside {@code logica}'s try. When it failed, no file was generated and nothing
 * was uploaded: the outer catch logged the error and the job still returned true.
 */
@FunctionalInterface
public interface ConexionSap {

    /** For a SAP source that has no separate connection step (it connects on each query). */
    ConexionSap IMPLICITA = () -> { };

    /** Obtains (and releases) one connection; throws when the SAP B1 database cannot be reached. */
    void comprobar();
}
