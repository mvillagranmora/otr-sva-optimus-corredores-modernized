package cl.bcs.tesoreria.jobs.programa;

/**
 * The program was NOT run because the guard marks of an earlier run say that TomEE collaborators were called (committed or with an unknown
 * outcome) and a re-run would repeat those writes. The program row was already set to "requires review" (ERR, Intento = MaximoIntentos,
 * Bloqueo 'S', Resultado {@code REQUIERE REVISION: ...}). HTTP 423 (Locked), deliberately NOT the 409 of {@link ProgramaNoReclamableException}:
 * a 409 means "somebody else has it, ask later"; this means "stopped for a human decision, do not retry, do not mark it ERR again".
 */
public class ProgramaRequiereRevisionException extends RuntimeException {

    private final long idPrograma;

    public ProgramaRequiereRevisionException(long idPrograma, String motivo) {
        super("Program #" + idPrograma + " requires review and was not started: " + motivo);
        this.idPrograma = idPrograma;
    }

    public long idPrograma() {
        return idPrograma;
    }
}
