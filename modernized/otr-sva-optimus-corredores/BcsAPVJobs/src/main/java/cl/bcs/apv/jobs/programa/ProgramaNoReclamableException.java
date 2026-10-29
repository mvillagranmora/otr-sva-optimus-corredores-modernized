package cl.bcs.apv.jobs.programa;

/**
 * The program could not be claimed: another instance or run already took it ({@code Bloqueo} 'S' or not PEN), or it has no attempts
 * left. HTTP 409: the caller must not retry blindly. The claim UPDATE matched 0 rows (see {@link ProgramaRepositorio#reclamar}).
 */
public class ProgramaNoReclamableException extends RuntimeException {

    private final long idPrograma;

    /** @param estado the state seen after the failed claim (see {@link Programa#resumen()}), or "unknown" */
    public ProgramaNoReclamableException(long idPrograma, String estado) {
        super("Program #" + idPrograma + " not started: it is not claimable (" + estado + ")");
        this.idPrograma = idPrograma;
    }

    public long idPrograma() {
        return idPrograma;
    }
}
