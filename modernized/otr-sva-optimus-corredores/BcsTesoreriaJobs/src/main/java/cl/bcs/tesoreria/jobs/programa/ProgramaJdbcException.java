package cl.bcs.tesoreria.jobs.programa;

/** A database error while reading or writing {@code BAT.Programa}; never swallowed (the legacy parameter reader swallowed it). */
public class ProgramaJdbcException extends RuntimeException {

    public ProgramaJdbcException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
