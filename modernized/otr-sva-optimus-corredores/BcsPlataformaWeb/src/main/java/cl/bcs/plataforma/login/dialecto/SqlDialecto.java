package cl.bcs.plataforma.login.dialecto;

import cl.bcs.plataforma.config.NoDisponibleException;

/**
 * The parts of the login code that differ between SAP HANA and Postgres and that Java still has to write: the next value of a sequence and a select
 * without a table. Everything else lives in the statement resources, one per statement and dialect. Bind parameters are always {@code :name}.
 */
public interface SqlDialecto {

    /** {@code hana} or {@code postgres}: the value of {@code plataforma.db.dialect}. */
    String nombre();

    /** The next value of a sequence given by its full HANA name. */
    String siguienteValor(String secuenciaHana);

    /** A select of an expression with no table. */
    String seleccionarSinTabla(String expresion);

    /**
     * Refuses, with {@link NoDisponibleException} (503), something that only exists in HANA (a stored procedure called by its HANA name, a view
     * named by a registry row) when the active dialect is not HANA, so no HANA text ever reaches another database.
     */
    default void exigirHana(String que) {
        if (!"hana".equals(nombre())) {
            throw new NoDisponibleException(que + " is available only in hana mode; the active dialect is " + nombre());
        }
    }
}
