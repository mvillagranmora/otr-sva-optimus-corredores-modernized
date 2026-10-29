package cl.bcs.contabilidad.domain.service.soporte;

/**
 * A column whose legacy formatting dereferenced the value ({@code Fecha.stringToSqlDate}, {@code x.toString()},
 * {@code toPlainString()}, ...) came back null. Legacy crashed with a NullPointerException that aborted the whole
 * centralization type; this is the same abort as a controlled error that names the column (approved deviation).
 */
public class DatoRequeridoException extends IllegalStateException {

    public DatoRequeridoException(String tipoDato, String columna) {
        super("Columna " + columna + " nula: no se puede formatear el dato " + tipoDato);
    }

    public DatoRequeridoException(String mensaje) {
        super(mensaje);
    }
}
