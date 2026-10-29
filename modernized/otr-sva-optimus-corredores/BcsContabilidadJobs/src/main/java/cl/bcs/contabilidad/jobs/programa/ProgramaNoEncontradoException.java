package cl.bcs.contabilidad.jobs.programa;

/** {@code BAT.Programa} has no row for the requested {@code idPrograma}: HTTP 404, nothing is started. */
public class ProgramaNoEncontradoException extends RuntimeException {

    private final long idPrograma;

    public ProgramaNoEncontradoException(long idPrograma) {
        super("Program #" + idPrograma + " not found in BAT.Programa");
        this.idPrograma = idPrograma;
    }

    public long idPrograma() {
        return idPrograma;
    }
}
