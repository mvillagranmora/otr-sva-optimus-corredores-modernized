package cl.bcs.contabilidad.infrastructure.interfaz;

/**
 * A SAP row cannot be written: a date or an amount that the legacy dereferenced is null (CONT-A-29).
 *
 * <p>Legacy: {@code SimpleDateFormat.format(null)} or {@code BigDecimal.add(null)} threw a
 * {@code NullPointerException} before anything was written, aborting the whole file family. The family is still
 * aborted, but with an error that names the field.
 */
public class InterfazDatoInvalidoException extends IllegalStateException {

    public InterfazDatoInvalidoException(String archivo, String campo) {
        super("Cannot write " + archivo + ": field " + campo + " is null in a SAP row");
    }
}
