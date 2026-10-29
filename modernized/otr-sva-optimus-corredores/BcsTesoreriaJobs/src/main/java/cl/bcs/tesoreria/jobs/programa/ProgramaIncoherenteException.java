package cl.bcs.tesoreria.jobs.programa;

/**
 * What the caller declared ({@code clase} or {@code parametros}) does not match the {@code BAT.Programa} row, or the row has no usable
 * type: HTTP 422, nothing is started. Parameter VALUES are never part of the message (they may be sensitive).
 */
public class ProgramaIncoherenteException extends RuntimeException {

    public ProgramaIncoherenteException(String mensaje) {
        super(mensaje);
    }
}
