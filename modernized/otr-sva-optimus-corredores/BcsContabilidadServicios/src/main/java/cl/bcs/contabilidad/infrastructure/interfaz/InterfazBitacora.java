package cl.bcs.contabilidad.infrastructure.interfaz;

/**
 * Run log of the job that drives the interface generators (CONT-A-06, CONT-J-24 / CONT-J-45).
 *
 * <p>Legacy: every {@code Crea*.logica(..., cl.bcs.contabilidad.Log log)} received the job's {@code Log} and wrote
 * into it {@code "INF - Creando Archivo: <name>"}, {@code "INF - Fin de creacion de Archivo..."} and, for a write
 * failure it swallowed, {@code "ERR - <message>"} (for example CreaBroadCuentas.java:83, :130, :138, :153, :201).
 * The generators report the same events here. {@link #NINGUNA} discards them; it is the default, so a caller that
 * does not keep a run log (the Servicios daily job) sees no change.
 */
@FunctionalInterface
public interface InterfazBitacora {

    /** Discards every line. */
    InterfazBitacora NINGUNA = (tipo, mensaje) -> { };

    /** Legacy {@code Log.EscribirLog(tipo, mensaje)}: {@code tipo} is "INF" or "ERR". Must not throw. */
    void escribir(String tipo, String mensaje);

    default void info(String mensaje) {
        escribir("INF", mensaje);
    }

    default void error(String mensaje) {
        escribir("ERR", mensaje);
    }
}
