package cl.bcs.contabilidad.domain.service.basilea;

/**
 * Controlled failure of the Basilea interface, raised where legacy ProcesarInterfazBasilea crashed with a
 * NullPointerException, IndexOutOfBoundsException or NumberFormatException: no own account configured (:291), an
 * unparseable broker RUT (:332-333) or a null filter value (:349-358). Nothing is inserted (approved deviation).
 */
public class InterfazBasileaException extends RuntimeException {

    public InterfazBasileaException(String message) {
        super(message);
    }
}
